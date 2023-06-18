package com.github.enanomapper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.Searcher;

public class SlimmerTest {

	@Test
	public void testLoading() throws OWLOntologyCreationException {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertNotSame(0, ontology.getAxiomCount());
	}

	@Test
	public void testParsingUp() throws Exception {
		String test = "+U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		
		assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(3, ontology.getClassesInSignature().size());
	}

	@Test
	public void testParsingSingle() throws Exception {
		String test = "+:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		
		assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(1, ontology.getClassesInSignature().size());
	}

	@Test
	public void testParsingDown() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1/snap#MaterialEntity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		
		assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(4, ontology.getClassesInSignature().size());
	}

	@Test
	public void testKeepAll() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1#Entity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		
		assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(39, ontology.getClassesInSignature().size());
	}

	@Test
	public void bug19() throws Exception {
		String test = "+(http://www.ifomis.org/bfo/1.1#Foo):http://www.ifomis.org/bfo/1.1#Entity\n"
		            + "+(http://www.ifomis.org/bfo/1.1#Foo):http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();

		assertEquals(3, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(3, ontology.getClassesInSignature().size());
	}

	@Test
	public void testParsingDownLeave() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		
		assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(1, ontology.getClassesInSignature().size());
	}

	@Test
	public void testDeleteUp() throws Exception {
		String test = "+U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant\n"
				    + "-U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Set<Instruction> irisToRemove = conf.getTreePartsToRemove();

		assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		slimmer.removeAll(irisToRemove);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(0, ontology.getClassesInSignature().size());
	}

	@Test
	public void testKeepAllButOne() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1#Entity\n"
				    + "-D:http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart\n";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Set<Instruction> irisToRemove = conf.getTreePartsToRemove();

		assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		slimmer.removeAll(irisToRemove);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(38, ontology.getClassesInSignature().size());
	}

	@Test
	public void testDeleteDown() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1/snap#MaterialEntity\n"
				    + "-D:http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Set<Instruction> irisToRemove = conf.getTreePartsToRemove();

		assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		slimmer.removeAll(irisToRemove);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(3, ontology.getClassesInSignature().size());
	}

	@Test
	public void testDeleteDownWithComment() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1/snap#MaterialEntity Comment\n"
				    + "-D:http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Set<Instruction> irisToRemove = conf.getTreePartsToRemove();

		assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		slimmer.removeAll(irisToRemove);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(3, ontology.getClassesInSignature().size());
	}

	@Test
	public void testParsingWithNewSuperClass() throws Exception {
		String test = "+D(http://www.ifomis.org/bfo/1.1/snap#Entity):http://www.ifomis.org/bfo/1.1/snap#Object";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Iterator<Instruction> instructions = irisToSave.iterator();
		Instruction instruction = instructions.next();
		if (!instruction.getUriString().endsWith("Object")) instruction = instructions.next();
		String baseClass = instruction.getUriString();

		assertEquals(2, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(2, ontology.getClassesInSignature().size());

		Set<OWLEntity> entities = ontology.getEntitiesInSignature(IRI.create(baseClass));
		assertEquals(1, entities.size());
		OWLEntity entity = entities.iterator().next();
		assertTrue(entity.isOWLClass());
		OWLClass owlClass = entity.asOWLClass();
		Set<OWLClassAxiom> axioms = ontology.getAxioms(owlClass, Imports.INCLUDED);
		assertEquals(1, axioms.size());
		assertEquals("SubClassOf", axioms.iterator().next().getAxiomType().getName());
	}

	@Test
	public void testMakeNewSubclassProperty() throws Exception {
		String test = "+:http://www.ifomis.org/bfo/1.1#Entity\n"
	                + "+(http://www.ifomis.org/bfo/1.1#Entity):http://www.ifomis.org/bfo/1.1/snap#MaterialEntity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		String baseClass = null;
		for (Instruction instruction : irisToSave) {
			if (instruction.getUriString().endsWith("#MaterialEntity")) 
				baseClass = instruction.getUriString();
		}

		assertEquals(3, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(2, ontology.getClassesInSignature().size());
		Set<OWLEntity> entities = ontology.getEntitiesInSignature(IRI.create(baseClass));
		assertEquals(1, entities.size());
		OWLEntity entity = entities.iterator().next();
		assertTrue(entity.isOWLClass());
		OWLClass owlClass = entity.asOWLClass();
		Set<OWLClassAxiom> axioms = ontology.getAxioms(owlClass, Imports.INCLUDED);
		assertEquals(1, axioms.size());
		assertEquals("SubClassOf", axioms.iterator().next().getAxiomType().getName());
	}

	@Test
	public void testMakeNewSuperClassFromOtherOntology() throws Exception {
		String test = "+(http://purl.obolibrary.org/obo/CHEBI_23367):http://www.ifomis.org/bfo/1.1/snap#MaterialEntity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Iterator<Instruction> iterator = irisToSave.iterator();
		Instruction instruction = iterator.next();
		while (!instruction.getUriString().equals("http://www.ifomis.org/bfo/1.1/snap#MaterialEntity") && iterator.hasNext())
			instruction = iterator.next();
		String baseClass = instruction.getUriString();

		assertEquals(2, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(2, ontology.getClassesInSignature().size());
		Set<OWLEntity> entities = ontology.getEntitiesInSignature(IRI.create(baseClass));
		assertEquals(1, entities.size());
		Iterator<OWLEntity> entityIter = entities.iterator();
		OWLEntity entity = entityIter.next();
		
		assertTrue(entity.isOWLClass());
		OWLClass owlClass = entity.asOWLClass();
		assertEquals(1, Searcher.sup(ontology.subClassAxiomsForSubClass(owlClass)).count());
		Set<OWLClassAxiom> axioms = ontology.getAxioms(owlClass, Imports.INCLUDED);
		assertEquals("SubClassOf", axioms.iterator().next().getAxiomType().getName());
	}

	@Test
	public void testNotRemoveDeclaredProperties() throws Exception {
		String test = "+:http://purl.obolibrary.org/obo/uo#is_unit_of";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		assertNotNull(irisToSave);
		assertEquals(1, conf.getTreePartsToSave().size());

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("uo.owl");
		Slimmer slimmer = new Slimmer(stream);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(3203, ontology.getAxiomCount());

		// test the removing; should result in exactly one less axiom
		slimmer.removeAllExcept(irisToSave);
		ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(68, ontology.getAxiomCount());
	}

	@Test
	public void testRemoveDeclaredProperties() throws Exception {
		Configuration conf = new Configuration();
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		assertNotNull(irisToSave);
		assertEquals(0, conf.getTreePartsToSave().size());

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("uo.owl");
		Slimmer slimmer = new Slimmer(stream);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(3203, ontology.getAxiomCount());

		// test the removing; should result in exactly one less axiom
		slimmer.removeAllExcept(irisToSave);
		ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(67, ontology.getAxiomCount());
	}

	@Test
	public void testRemoveMoreDeclaredProperties() throws Exception {
		String test = "+:http://www.bioassayontology.org/bao#BAO_0000555";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		assertNotNull(irisToSave);
		assertEquals(1, conf.getTreePartsToSave().size());

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bao_core.owl");
		Slimmer slimmer = new Slimmer(stream);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(899, ontology.getAxiomCount());

		// test the removing; should result in exactly one less axiom
		slimmer.removeAllExcept(irisToSave);
		ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(46, ontology.getAxiomCount());
	}

	@Test
	public void testRemoveSpecificProperty() throws Exception {
		String test = "-:http://www.bioassayontology.org/bao#BAO_0000335";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		assertEquals(1, conf.getTreePartsToRemove().size());

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bao_core.owl");
		Slimmer slimmer = new Slimmer(stream);
		OWLOntology ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(899, ontology.getAxiomCount());

		// test the removing; should result in exactly one less axiom
		slimmer.removeAll(conf.getTreePartsToRemove());
		ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(895, ontology.getAxiomCount());
	}

	@Test
	public void testExtraNamespaces() throws Exception {
		Configuration conf = new Configuration();
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		String ontoFile = "uo.owl";
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream(ontoFile);
		Slimmer slimmer = new Slimmer(stream);
		OWLOntology ontology = slimmer.getOntology();
		slimmer.removeAllExcept(irisToSave);
		ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(67, ontology.getAxiomCount());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		slimmer.saveAs(output, ontoFile);
		String owlOutput = output.toString();
		System.out.println(owlOutput);
		assertTrue(owlOutput.contains("xmlns:ncicp"));
	}

	@Test
	public void testSlimmingVersionAnnotation() throws Exception {
		Configuration conf = new Configuration();
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		String ontoFile = "uo.owl";
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream(ontoFile);
		Slimmer slimmer = new Slimmer(stream);
		OWLOntology ontology = slimmer.getOntology();
		slimmer.removeAllExcept(irisToSave);
		ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(67, ontology.getAxiomCount());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		slimmer.saveAs(output, ontoFile);
		String owlOutput = output.toString();
		assertTrue(owlOutput.contains("This SLIM file"));
	}

	@Test
	public void testSourceAnnotation() throws Exception {
		Configuration conf = new Configuration();
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		String ontoFile = "uo.owl";
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream(ontoFile);
		Slimmer slimmer = new Slimmer(stream);
		OWLOntology ontology = slimmer.getOntology();
		slimmer.removeAllExcept(irisToSave);
		ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(67, ontology.getAxiomCount());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		slimmer.saveAs(output, ontoFile);
		String owlOutput = output.toString();
		assertTrue(owlOutput.contains("pav:importedFrom"));
	}

	@Test
	public void testGenerationDate() throws Exception {
		Configuration conf = new Configuration();
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		String ontoFile = "uo.owl";
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream(ontoFile);
		Slimmer slimmer = new Slimmer(stream);
		OWLOntology ontology = slimmer.getOntology();
		slimmer.removeAllExcept(irisToSave);
		ontology = slimmer.getOntology();
		assertNotNull(ontology);
		assertEquals(67, ontology.getAxiomCount());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		slimmer.saveAs(output, ontoFile);
		String owlOutput = output.toString();
		System.out.println("Output: " + owlOutput);
		assertTrue(owlOutput.contains(">2023-"), "Did not find the generation data."); // TODO: update every year :)
	}
}
