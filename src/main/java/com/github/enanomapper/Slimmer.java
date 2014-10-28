package com.github.enanomapper;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.OWLEntityRemover;

public class Slimmer {

	private OWLOntologyManager man;
	private OWLOntology onto;

	public Slimmer(File owlFile) throws OWLOntologyCreationException {
		man = OWLManager.createOWLOntologyManager();
		onto = man.loadOntology(
			IRI.create("file://" + owlFile.getAbsoluteFile())
		);
	}

	public Slimmer(InputStream owlFile) throws OWLOntologyCreationException {
		man = OWLManager.createOWLOntologyManager();
		onto = man.loadOntologyFromOntologyDocument(owlFile);
	}

	public OWLOntology getOntology() {
		return this.onto;
	}

	public static void main(String[] args) {
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
				String owlFilename = props.getProperty("owl"); // for step 1
				String iriFilename = props.getProperty("iris"); // for step 2,3
				String slimmedFilename = props.getProperty("slimmed"); // for step 4

				// 1. read the original ontology
				File owlFile = new File(owlFilename);
				Slimmer slimmer = new Slimmer(owlFile);
				OWLOntology onto = slimmer.getOntology();
				System.out.println("Loaded axioms: " + onto.getAxiomCount());

				// 2. read the configuration of what to keep/remove
				File configFile = new File(rootFolder,iriFilename);
				Configuration config = new Configuration();
				config.read(configFile);

				// 3. remove everything except for what is defined by the instructions
				Set<Instruction> irisToSave = config.getTreePartsToSave();
				slimmer.removeAllExcept(irisToSave);
				Set<Instruction> irisToRemove = config.getTreePartsToRemove();
				slimmer.removeAll(irisToRemove);

				// 4. save in OWL/XML format
				File output = new File(slimmedFilename);
				slimmer.saveAs(output);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void saveAs(File output) throws OWLOntologyStorageException {
		IRI documentIRI2 = IRI.create(output);
		man.saveOntology(onto, new RDFXMLOntologyFormat(), documentIRI2);
	}

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

	public void removeAllExcept(Set<Instruction> irisToSave) {
		Set<String> singleIRIs = explode(irisToSave);
		System.out.println("" + singleIRIs);

		OWLEntityRemover remover = new OWLEntityRemover(
			man, Collections.singleton(onto)
		);
		for (OWLClass ind : onto.getClassesInSignature()) {
			String indIRI = ind.getIRI().toString();
			System.out.println(indIRI);
			if (!singleIRIs.contains(indIRI)) {
				System.out.println("Remove: " + indIRI);
				ind.accept(remover);
			}
		}
		man.applyChanges(remover.getChanges());
	}
	
	public void removeAll(Set<Instruction> irisToRemove) {
		Set<String> singleIRIs = explode(irisToRemove);
		System.out.println("" + singleIRIs);

		OWLEntityRemover remover = new OWLEntityRemover(
			man, Collections.singleton(onto)
		);
		for (OWLClass ind : onto.getClassesInSignature()) {
			String indIRI = ind.getIRI().toString();
			System.out.println(indIRI);
			if (singleIRIs.contains(indIRI)) {
				System.out.println("Remove: " + indIRI);
				ind.accept(remover);
			}
		}
		man.applyChanges(remover.getChanges());
	}

	private Set<String> allSuperClasses(OWLClass clazz,
			OWLOntology onto) {
		Set<String> allSuperClasses = new HashSet<String>();
		Set<OWLClassExpression> superClasses = clazz.getSuperClasses(onto);
		for (OWLClassExpression superClass : superClasses) {
			OWLClass superOwlClass = superClass.asOWLClass();
			String superIri = superOwlClass.getIRI().toString();
			allSuperClasses.add(superIri);
			// recurse
			allSuperClasses.addAll(allSuperClasses(superOwlClass, onto));
		}
		return allSuperClasses;
	}

	private Set<String> allSubClasses(OWLClass clazz,
			OWLOntology onto) {
		Set<String> allSubClasses = new HashSet<String>();
		Set<OWLClassExpression> subClasses = clazz.getSubClasses(onto);
		for (OWLClassExpression subClass : subClasses) {
			OWLClass subOwlClass = subClass.asOWLClass();
			String subIri = subOwlClass.getIRI().toString();
			allSubClasses.add(subIri);
			// recurse
			allSubClasses.addAll(allSubClasses(subOwlClass, onto));
		}
		return allSubClasses;
	}

}