package com.github.enanomapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.search.Searcher;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * The purpose of this class is to take one or more OWL ontology files and remove everything that
 * we do not want in. This includes, in the first place, 3rd party classes and predicates that some
 * ontology imports from other ontologies (from the BAO ontology, we only want to keep terms added
 * by BAO and not used from IAO or SIO). But we may also only use part of the ontology itself. For
 * example, keep some key terms but not full subtrees.
 * 
 * <p>What is kept to be, is specified by a configuration file read by the {@link Configuration}
 * class. This class specifies the format used by these config files. If nothing is specified, all
 * content (classes, object and data properties) is removed.
 *
 * @author egonw
 */
public class Slimmer {

	private OWLOntologyManager man;
	private OWLOntology onto;

	public Slimmer(File owlFile, String mergedOntologyIRI) throws OWLOntologyCreationException, FileNotFoundException {
		this(owlFile.getName(), new FileInputStream(owlFile), mergedOntologyIRI);
	}

	public Slimmer(InputStream owlFile) throws OWLOntologyCreationException {
		this("undefined for InputStream", owlFile, null);
	}

	/**
	 * Constructs a new Slimmer object that will slim the given OWL file.
	 *
	 * @param owlFile
	 * @param mergedOntologyIRI
	 * @throws OWLOntologyCreationException
	 */
	public Slimmer(String filename, InputStream owlFile, String mergedOntologyIRI) throws OWLOntologyCreationException {
		System.out.println("Loading OWL file: " + filename);
		man = OWLManager.createOWLOntologyManager();
		if (System.getenv("WORKSPACE") != null) {
			String root = System.getenv("WORKSPACE");
			System.out.println("Adding mappings with root: " + root);
			addMappings(man, root);
		}
		onto = man.loadOntologyFromOntologyDocument(owlFile);
		if (mergedOntologyIRI != null) {
			Set<OWLImportsDeclaration> importDeclarations = onto.getImportsDeclarations();
			for (OWLImportsDeclaration declaration : importDeclarations) {
				if (!man.contains(declaration.getIRI())) {
					try {
						man.getOntology(declaration.getIRI());
						System.out.println("Loaded imported ontology: " + declaration.getIRI());
					} catch (Exception exception) {
						System.out.println("Failed to load imported ontology: " + declaration.getIRI());
					}
				}
			}
			// Merge all of the loaded ontologies, specifying an IRI for the new ontology
			OWLOntologyMerger merger = new OWLOntologyMerger(man);
			onto = merger.createMergedOntology(man, IRI.create(mergedOntologyIRI));
			for (OWLOntology ontology : man.getOntologies()) {
				System.out.println("  Copying annotations from " + ontology.getOntologyID());
				for (OWLAnnotation annotation : ontology.getAnnotations()) {
					System.out.println("  copying annotation: " + annotation.getProperty() + " -> " + annotation.getValue());
					AddOntologyAnnotation annotationAdd = new AddOntologyAnnotation(onto, annotation);
					man.applyChange(annotationAdd);
				}
			}
		}
	}

	public OWLOntology getOntology() {
		return this.onto;
	}

	/**
	 * Main method to allow running the Slimmer from the command line. The full slimming
	 * process consists of a number of steps:
	 * <ol>
	 *   <li>read the instructions that specify which ontology to slim</li>
	 *   <li>read the ontology to slim (including imports)</li>
	 *   <li>read the instructions that specify how the ontology is to be slimmed</li>
	 *   <li>remove everything from the ontology except what is to be kept, but after that still
	 *       delete things explicitly marked to be removed</li>
	 *   <li>remove owl:import statements from the OWL file</li>
	 *   <li>normalize term labels</li>
	 *   <li>save as OWL/XML (which includes updating the ontology metadata)</li>
	 * </ol>
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		boolean allSucceeded = true;
		String rootFolder = args[0];
		System.out.println("Searching configuration files in " + rootFolder);
		File dir = new File(rootFolder);
		File[] files = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".props");
		    }
		});
		for (File file : files) {
			try {
				System.out.println("Slimming for " + file.getName());

				// read the information about the ontology to process
				Properties props = new Properties();
				props.load(new FileReader(file));
				String owlURL = props.getProperty("owl"); // for step 1
				String owlFilename = owlURL;
				if (owlFilename.contains("/")) {
					owlFilename = owlFilename.substring(owlFilename.lastIndexOf('/')+1);
				}
				String iriFilename = props.getProperty("iris"); // for step 2,3
				String slimmedURI = props.getProperty("slimmed"); // for step 5
				String slimmedFilename = slimmedURI;
				if (slimmedFilename.contains("/")) {
					slimmedFilename = slimmedFilename.substring(slimmedFilename.lastIndexOf('/')+1);
				}

				// 1. read the original ontology
				File owlFile = new File(owlFilename);
				Slimmer slimmer = new Slimmer(owlFile, slimmedFilename);
				OWLOntology onto = slimmer.getOntology();
				System.out.println("Loaded axioms: " + onto.getAxiomCount());

				// 2. read the configuration of what to keep/remove
				File configFile = new File(rootFolder,iriFilename);
				Configuration config = new Configuration();
				try {
					System.out.println("Reading config file: " + configFile);
					config.read(configFile);
				} catch (Exception exception) {
					System.out.println("Error while reading the config file: " + exception.getMessage());
					System.exit(-1);
				}

				// 3. remove everything except for what is defined by the instructions
				Set<Instruction> irisToSave = config.getTreePartsToSave();
				slimmer.removeAllExcept(irisToSave);
				Set<Instruction> irisToRemove = config.getTreePartsToRemove();
				slimmer.removeAll(irisToRemove);

				// 4. remove owl:imports
				Set<OWLImportsDeclaration> importDeclarations = onto.getImportsDeclarations();
				for (OWLImportsDeclaration declaration : importDeclarations) {
					System.out.println("Removing imports: " + declaration.getIRI());
					RemoveImport removeImport = new RemoveImport(onto, declaration);
					slimmer.man.applyChange(removeImport);
				}

				// 5. update descriptions and labels
				Set<OWLClass> entities = onto.getClassesInSignature();
				for (OWLClass clazz : entities) {
					Stream<OWLAnnotation> annotations = EntitySearcher.getAnnotations(clazz, onto);
					annotations.forEach(annot -> {
						if (annot.getProperty().getIRI().toString().equals("http://purl.org/dc/elements/1.1/description") ||
							annot.getProperty().getIRI().toString().equals("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P97")) {
							System.out.println("  description: " + annot.getValue());
							OWLDataFactory factory = slimmer.man.getOWLDataFactory();
							OWLAnnotationProperty newDescription =
								factory.getOWLAnnotationProperty(IRI.create("http://purl.obolibrary.org/obo/IAO_0000115"));
							OWLAnnotation commentAnno = factory.getOWLAnnotation(
								newDescription,
								annot.getValue()
							);
							System.out.println("  new description: " + commentAnno);
							OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(
								clazz.getIRI(), commentAnno
							);
							slimmer.man.applyChange(new AddAxiom(onto, ax));
						}
					});
				}

				// 6. remove some nasty NPO properties (WORKAROUND: may be removed later)
				entities = onto.getClassesInSignature();
				for (OWLClass clazz : entities) {
					Set<OWLAnnotationAssertionAxiom> annots = onto.getAnnotationAssertionAxioms(clazz.getIRI());
					Set<OWLAnnotationAssertionAxiom> toRemove = new HashSet<OWLAnnotationAssertionAxiom>();
					for (OWLAnnotationAssertionAxiom axiom : annots) {
						if (axiom.getProperty().getIRI().toString().equals("http://purl.bioontology.org/ontology/npo#FULL_SYN") ||
							axiom.getProperty().getIRI().toString().equals("http://purl.bioontology.org/ontology/npo#definition")) {
							toRemove.add(axiom);
						}
					}
					slimmer.man.removeAxioms(onto, toRemove);
				}

				// 7. save in OWL/XML format
				SetOntologyID ontologyIDChange = new SetOntologyID(onto, IRI.create(slimmedURI));
				slimmer.man.applyChange(ontologyIDChange);
				File output = new File(slimmedFilename);
				System.out.println("Saving to: " + output.getAbsolutePath());
				slimmer.saveAs(output, owlURL);
			} catch (Exception e) {
				e.printStackTrace();
				allSucceeded = false;
			}
		}
		if (!allSucceeded) System.exit(-1);
	}

	public void saveAs(File output, String orinalOWL) throws OWLOntologyStorageException, FileNotFoundException {
		saveAs(new FileOutputStream(output), orinalOWL);
	}

	/**
	 *  Save the ontology as OWL/XML. It first includes new meta data about the slimming process.
	 *
	 * @param output
	 * @param originalOWL
	 * @throws OWLOntologyStorageException
	 */
	public void saveAs(OutputStream output, String originalOWL) throws OWLOntologyStorageException {
		// add provenance
		OWLDataFactory dataFac = man.getOWLDataFactory();

		// version info
		OWLLiteral lit = dataFac.getOWLLiteral(
			"This SLIM file was generated automatically by the eNanoMapper Slimmer "
			+ "software library. For more information see "
			+ "http://github.com/enanomapper/slimmer.");
		OWLAnnotationProperty owlAnnotationProperty =
			dataFac.getOWLAnnotationProperty(OWLRDFVocabulary.OWL_VERSION_INFO.getIRI());
		OWLAnnotation anno = dataFac.getOWLAnnotation(owlAnnotationProperty, lit);
		man.applyChange(new AddOntologyAnnotation(onto, anno));
		OWLAnnotationProperty pavImportedFrom = dataFac.getOWLAnnotationProperty(
			IRI.create("http://purl.org/pav/importedFrom")
		);
		anno = dataFac.getOWLAnnotation(pavImportedFrom, dataFac.getOWLLiteral(originalOWL));
		man.applyChange(new AddOntologyAnnotation(onto, anno));

		// generation tool
		lit = dataFac.getOWLLiteral("Slimmer");
		owlAnnotationProperty = dataFac.getOWLAnnotationProperty(
			IRI.create("http://www.geneontology.org/formats/oboInOwl#auto-generated-by")
		);
		anno = dataFac.getOWLAnnotation(owlAnnotationProperty, lit);
		man.applyChange(new AddOntologyAnnotation(onto, anno));

		// generation date
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		lit = dataFac.getOWLLiteral(dateFormat.format(date));
		owlAnnotationProperty = dataFac.getOWLAnnotationProperty(
			IRI.create("http://www.geneontology.org/formats/oboInOwl#date")
		);
		anno = dataFac.getOWLAnnotation(owlAnnotationProperty, lit);
		man.applyChange(new AddOntologyAnnotation(onto, anno));

		// save to file
		RDFXMLDocumentFormat format = new RDFXMLDocumentFormat();
		format.setPrefix("ncicp", "http://ncicb.nci.nih.gov/xml/owl/EVS/ComplexProperties.xsd#");
		man.saveOntology(onto, format, output);
	}

	/**
	 * This functions applies the <code>D</code> and <code>U</code> statements from the configuration
	 * files by traversing the OWL hierarchy and either including all parents or all children.
	 *
	 * @param instructions
	 * @return
	 */
	private Set<String> explode(Set<Instruction> instructions) {
		Set<String> singleIRIs = new HashSet<String>();
		for (Instruction instruction : instructions) {
			String iri = instruction.getUriString();
			if (instruction.getScope() == Instruction.Scope.UP) {
				System.out.println("Extracting " + iri + "...");
				Set<OWLEntity> entities = onto.getEntitiesInSignature(IRI.create(iri));
				if (entities.size() > 0) {
					OWLEntity entity = entities.iterator().next();
					if (entity instanceof OWLClass) {
						OWLClass clazz = (OWLClass)entity;
						System.out.println("Class " + clazz);
						Set<String> superClasses = allSuperClasses(clazz, onto);
						for (String superClass : superClasses) {
							System.out.println("Extracting " + superClass + "...");
							singleIRIs.add(superClass);
						}
					}
				}
				singleIRIs.add(iri);
			} else if (instruction.getScope() == Instruction.Scope.DOWN) {
				System.out.println("Extracting " + iri + "...");
				Set<OWLEntity> entities = onto.getEntitiesInSignature(IRI.create(iri));
				if (entities.size() > 0) {
					OWLEntity entity = entities.iterator().next();
					if (entity instanceof OWLClass) {
						OWLClass clazz = (OWLClass)entity;
						System.out.println("Class " + clazz);
						Set<String> subClasses = allSubClasses(clazz, onto);
						for (String subClass : subClasses) {
							System.out.println("Extracting " + subClass + "...");
							singleIRIs.add(subClass);
						}
					}
				}
				singleIRIs.add(iri);
			} else if (instruction.getScope() == Instruction.Scope.SINGLE) {
				System.out.println("Extracting " + iri + "...");
				singleIRIs.add(iri);
			} else {
				System.out.println("Cannot handle this instruction: " + instruction.getScope());
			}
		}
		return singleIRIs;
	}

	/**
	 * This methods removes all classes, data properties, and object properties, except those
	 * URIs specified by the parameter. If a class is kept, the instructions also indicates
	 * what the new parent of the class is.
	 *
	 * @param irisToSave which IRIs are to be kept
	 */
	public void removeAllExcept(Set<Instruction> irisToSave) {
		Set<String> singleIRIs = explode(irisToSave);
		Map<String,String> newSuperClasses = getNewSuperClasses(irisToSave);
		System.out.println("" + singleIRIs);

		// remove classes
		OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(onto));
		for (OWLClass ind : onto.getClassesInSignature()) {
			String indIRI = ind.getIRI().toString();
			System.out.println(indIRI);
			if (!singleIRIs.contains(indIRI)) {
				System.out.println("Remove: " + indIRI);
				ind.accept(remover);
			} else {
				// OK, keep this one. But does it have a new super class?
				if (newSuperClasses.containsKey(indIRI)) {
					String newSuperClass = newSuperClasses.get(indIRI);
					OWLDataFactory factory = man.getOWLDataFactory();
					System.out.println("Super class: " + newSuperClass);
					OWLClass superClass = factory.getOWLClass(IRI.create(newSuperClass));
					OWLAxiom axiom = factory.getOWLSubClassOfAxiom(ind, superClass);
					System.out.println("Adding super class axiom: " + axiom);
					AddAxiom addAxiom = new AddAxiom(onto, axiom);
					man.applyChange(addAxiom);
				}
			}
		}

		// remove properties
		for (OWLObjectProperty axiom : onto.getObjectPropertiesInSignature()) {
			String propIRI = axiom.getIRI().toString();
			System.out.println(propIRI);
			if (!singleIRIs.contains(propIRI)) {
				System.out.println("Remove: " + propIRI);
				axiom.accept(remover);
			}
		}
		for (OWLDataProperty axiom : onto.getDataPropertiesInSignature()) {
			String propIRI = axiom.getIRI().toString();
			System.out.println(propIRI);
			if (!singleIRIs.contains(propIRI)) {
				System.out.println("Remove: " + propIRI);
				axiom.accept(remover);
			}
		}

		man.applyChanges(remover.getChanges());
	}
	
	private Map<String, String> getNewSuperClasses(Set<Instruction> irisToSave) {
		Map<String,String> newSuperClasses = new HashMap<String, String>();
		for (Instruction instruction : irisToSave) {
			if (instruction.getNewSuperClass() != null) {
				newSuperClasses.put(instruction.getUriString(), instruction.getNewSuperClass());
			}
		}
		return newSuperClasses;
	}

	/**
	 * This method removes all IRIs given by the parameter.
	 *
	 * @param irisToRemove
	 */
	public void removeAll(Set<Instruction> irisToRemove) {
		Set<String> singleIRIs = explode(irisToRemove);
		System.out.println("" + singleIRIs);

		OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(onto));
		for (OWLClass ind : onto.getClassesInSignature()) {
			String indIRI = ind.getIRI().toString();
			System.out.println(indIRI);
			if (singleIRIs.contains(indIRI)) {
				System.out.println("Remove: " + indIRI);
				ind.accept(remover);
			}
		}

		// remove properties
		for (OWLObjectProperty axiom : onto.getObjectPropertiesInSignature()) {
			String propIRI = axiom.getIRI().toString();
			System.out.println(propIRI);
			if (singleIRIs.contains(propIRI)) {
				System.out.println("Remove: " + propIRI);
				axiom.accept(remover);
			}
		}
		for (OWLDataProperty axiom : onto.getDataPropertiesInSignature()) {
			String propIRI = axiom.getIRI().toString();
			System.out.println(propIRI);
			if (singleIRIs.contains(propIRI)) {
				System.out.println("Remove: " + propIRI);
				axiom.accept(remover);
			}
		}

		man.applyChanges(remover.getChanges());
	}

	private Set<String> allSuperClasses(OWLClass clazz,
			OWLOntology onto) {
		Set<String> allSuperClasses = new HashSet<String>();
		Stream<OWLClassExpression> superClasses = Searcher.sup(onto.subClassAxiomsForSubClass(clazz));
		superClasses.forEach(superClass -> {
			if (superClass.isOWLClass()) {
				OWLClass superOwlClass = superClass.asOWLClass();
				String superIri = superOwlClass.getIRI().toString();
				allSuperClasses.add(superIri);
				// recurse
				allSuperClasses.addAll(allSuperClasses(superOwlClass, onto));
			}
		});
		return allSuperClasses;
	}

	/**
	 * Helper method that returns a collection sub classes of the given class.
	 *
	 * @param clazz
	 * @param onto
	 * @return
	 */
	private Set<String> allSubClasses(OWLClass clazz,
			OWLOntology onto) {
		Set<String> allSubClasses = new HashSet<String>();
		System.out.println("clazz: " + clazz);
		Stream<OWLClassExpression> subClasses = Searcher.sub(onto.subClassAxiomsForSuperClass(clazz));
		subClasses.forEach(subClass -> {
		  // skip itself
		  OWLClass subOwlClass = subClass.asOWLClass();
		  System.out.println("subclass: " + subOwlClass);
		  String subIri = subOwlClass.getIRI().toString();
		  allSubClasses.add(subIri);
		  // recurse
		  allSubClasses.addAll(allSubClasses(subOwlClass, onto));
		});
		System.out.println("subclass count: " + allSubClasses.size());
		return allSubClasses;
	}

	@SuppressWarnings("serial")
	Map<String,String> mappings = new HashMap<String,String>() {{
		// put("http://purl.obolibrary.org/obo/oae/RO_dev_import", "RO_dev_import.owl");
		// put("https://raw.githubusercontent.com/obophenotype/human-phenotype-ontology/master/hp.owl", "hp.owl");
	}};

	private void addMappings(OWLOntologyManager m, String root) {
		if (!root.endsWith("/")) root = (root + "/").replace(" ", "%20");
		for (String ontoIRI : mappings.keySet()) {
			String localPart = mappings.get(ontoIRI);
			m.addIRIMapper(new SimpleIRIMapper(
				IRI.create(ontoIRI), IRI.create("file://" + root + localPart)
		    ));
			System.out.println("  added: " + IRI.create("file://" + root + localPart));
		}
	}

}
