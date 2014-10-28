package com.github.enanomapper;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class SlimmerTest {

	@Test
	public void testLoading() throws OWLOntologyCreationException {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		OWLOntology ontology = slimmer.getOntology();
		Assert.assertNotNull(ontology);
		Assert.assertNotSame(0, ontology.getAxiomCount());
	}

	@Test
	public void testParsingUp() throws Exception {
		String test = "+U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		
		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		Assert.assertNotNull(ontology);
		Assert.assertEquals(3, ontology.getClassesInSignature().size());
	}

	@Test
	public void testParsingSingle() throws Exception {
		String test = "+:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		
		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		Assert.assertNotNull(ontology);
		Assert.assertEquals(1, ontology.getClassesInSignature().size());
	}

	@Test
	public void testParsingDown() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1/snap#MaterialEntity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		
		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		Assert.assertNotNull(ontology);
		Assert.assertEquals(4, ontology.getClassesInSignature().size());
	}

	@Test
	public void testKeepAll() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1#Entity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		
		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		Assert.assertNotNull(ontology);
		Assert.assertEquals(39, ontology.getClassesInSignature().size());
	}

	@Test
	public void testParsingDownLeave() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		
		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		Assert.assertNotNull(ontology);
		Assert.assertEquals(1, ontology.getClassesInSignature().size());
	}

	@Test
	public void testDeleteUp() throws Exception {
		String test = "+U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant\n"
				    + "-U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Set<Instruction> irisToRemove = conf.getTreePartsToRemove();

		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		slimmer.removeAll(irisToRemove);
		OWLOntology ontology = slimmer.getOntology();
		Assert.assertNotNull(ontology);
		Assert.assertEquals(0, ontology.getClassesInSignature().size());
	}

	@Test
	public void testKeepAllButOne() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1#Entity\n"
				    + "-D:http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart\n";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Set<Instruction> irisToRemove = conf.getTreePartsToRemove();

		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		slimmer.removeAll(irisToRemove);
		OWLOntology ontology = slimmer.getOntology();
		Assert.assertNotNull(ontology);
		Assert.assertEquals(38, ontology.getClassesInSignature().size());
	}

	@Test
	public void testDeleteDown() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1/snap#MaterialEntity\n"
				    + "-D:http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Set<Instruction> irisToRemove = conf.getTreePartsToRemove();

		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		slimmer.removeAll(irisToRemove);
		OWLOntology ontology = slimmer.getOntology();
		Assert.assertNotNull(ontology);
		Assert.assertEquals(3, ontology.getClassesInSignature().size());
	}

	@Test
	public void testDeleteDownWithComment() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1/snap#MaterialEntity Comment\n"
				    + "-D:http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Set<Instruction> irisToRemove = conf.getTreePartsToRemove();

		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		slimmer.removeAll(irisToRemove);
		OWLOntology ontology = slimmer.getOntology();
		Assert.assertNotNull(ontology);
		Assert.assertEquals(3, ontology.getClassesInSignature().size());
	}

	@Test
	public void testMakeNewSubclassProperty() throws Exception {
		String test = "+:http://www.ifomis.org/bfo/1.1#Entity\n"
	                + "+(http://www.ifomis.org/bfo/1.1#Entity):http://www.ifomis.org/bfo/1.1/snap#MaterialEntity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Instruction instruction = irisToSave.iterator().next();
		String baseClass = instruction.getUriString();

		Assert.assertEquals(2, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		Assert.assertNotNull(ontology);
		Assert.assertEquals(2, ontology.getClassesInSignature().size());
		Set<OWLEntity> entities = ontology.getEntitiesInSignature(IRI.create(baseClass));
		Assert.assertEquals(1, entities.size());
		OWLEntity entity = entities.iterator().next();
		Assert.assertTrue(entity.isOWLClass());
		OWLClass owlClass = entity.asOWLClass();
		Assert.assertEquals(1, owlClass.getSuperClasses(ontology).size());
	}

	@Test
	public void testMakeNewSuperClassFromOtherOntology() throws Exception {
		String test = "+(http://purl.obolibrary.org/obo/CHEBI_23367):http://www.ifomis.org/bfo/1.1/snap#MaterialEntity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Instruction instruction = irisToSave.iterator().next();
		String baseClass = instruction.getUriString();

		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("bfo-1.1.owl");
		Slimmer slimmer = new Slimmer(stream);
		slimmer.removeAllExcept(irisToSave);
		OWLOntology ontology = slimmer.getOntology();
		Assert.assertNotNull(ontology);
		Assert.assertEquals(2, ontology.getClassesInSignature().size());
		Set<OWLEntity> entities = ontology.getEntitiesInSignature(IRI.create(baseClass));
		Assert.assertEquals(1, entities.size());
		OWLEntity entity = entities.iterator().next();
		Assert.assertTrue(entity.isOWLClass());
		OWLClass owlClass = entity.asOWLClass();
		Assert.assertEquals(1, owlClass.getSuperClasses(ontology).size());
		Set<OWLClassAxiom> axioms = ontology.getAxioms(owlClass);
		Assert.assertEquals("SubClassOf", axioms.iterator().next().getAxiomType().getName());
	}
}
