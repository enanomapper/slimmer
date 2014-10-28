package com.github.enanomapper;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
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
	public void testDeleteUp() throws Exception {
		String test = "+U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant\n"
				    + "-U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant\n";
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
}
