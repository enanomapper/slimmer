package com.github.enanomapper;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationTest {

	@Test
	public void testParsingUp() throws Exception {
		String test = "+U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Assert.assertEquals(1, conf.getTreePartsToSave().size());
	}

	@Test
	public void testParsingWithComment() throws Exception {
		String test = "+U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant Comment";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		Assert.assertEquals("Comment", conf.getTreePartsToSave().iterator().next().getComment());
	}

	@Test
	public void testParsingWithNewSuperClass() throws Exception {
		String test = "+D(http://www.ifomis.org/bfo/1.1/snap#Entity):http://www.ifomis.org/bfo/1.1/snap#MaterialEntity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		Assert.assertEquals("http://www.ifomis.org/bfo/1.1/snap#Entity", conf.getTreePartsToSave().iterator().next().getNewSuperClass());
	}

	@Test
	public void testParsingSingleWithNewSuperClass() throws Exception {
		String test = "+(http://www.ifomis.org/bfo/1.1/snap#Entity):http://www.ifomis.org/bfo/1.1/snap#MaterialEntity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		Instruction instruction = conf.getTreePartsToSave().iterator().next();
		Assert.assertEquals("http://www.ifomis.org/bfo/1.1/snap#Entity", instruction.getNewSuperClass());
		Assert.assertEquals("http://www.ifomis.org/bfo/1.1/snap#MaterialEntity", instruction.getUriString());
	}

	@Test
	public void testMakeNewSubclassProperty() throws Exception {
		String test = "+:http://www.ifomis.org/bfo/1.1#Entity\n"
	                + "+(http://www.ifomis.org/bfo/1.1#Entity):http://www.ifomis.org/bfo/1.1/snap#MaterialEntity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Iterator<Instruction> iter = irisToSave.iterator();
		Instruction instruction1 = iter.next(); System.out.println(instruction1);
		Instruction instruction2 = iter.next(); System.out.println(instruction2);
		if (instruction1.getUriString().endsWith("MaterialEntity")) {
			Assert.assertEquals("http://www.ifomis.org/bfo/1.1/snap#MaterialEntity", instruction1.getUriString());
			Assert.assertNotNull(instruction1.getNewSuperClass());
			Assert.assertEquals("http://www.ifomis.org/bfo/1.1#Entity", instruction1.getNewSuperClass());
			Assert.assertEquals("http://www.ifomis.org/bfo/1.1#Entity", instruction2.getUriString());
			Assert.assertNull(instruction2.getNewSuperClass());
		} else {
			Assert.assertEquals("http://www.ifomis.org/bfo/1.1/snap#MaterialEntity", instruction2.getUriString());
			Assert.assertNotNull(instruction2.getNewSuperClass());
			Assert.assertEquals("http://www.ifomis.org/bfo/1.1#Entity", instruction2.getNewSuperClass());
			Assert.assertEquals("http://www.ifomis.org/bfo/1.1#Entity", instruction1.getUriString());
			Assert.assertNull(instruction1.getNewSuperClass());
		}
	}
	
	@Test
	public void testRemoveUp() throws Exception {
		String test = "-U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Assert.assertEquals(0, conf.getTreePartsToSave().size());
		Assert.assertEquals(1, conf.getTreePartsToRemove().size());
	}

	@Test(expected=Exception.class)
	public void testMissingAdd() throws Exception {
		String test = "U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
	}

	@Test(expected=Exception.class)
	public void testMissingColon() throws Exception {
		String test = "+Uhttp://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
	}

	@Test
	public void testSingularAdd() throws Exception {
		String test = "+:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Assert.assertEquals(1, conf.getTreePartsToSave().size());
	}

	@Test
	public void testDown() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Assert.assertEquals(1, conf.getTreePartsToSave().size());
	}

	@Test
	public void testSingularemove() throws Exception {
		String test = "-:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Assert.assertEquals(1, conf.getTreePartsToRemove().size());
	}

	@Test
	public void testTwoLiner() throws Exception {
		String test = "+U:http://www.ifomis.org/bfo/1.1/snap#MaterialEntity\n"
				    + "-D:http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart\n";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Assert.assertEquals(1, conf.getTreePartsToSave().size());
		Assert.assertEquals(1, conf.getTreePartsToRemove().size());
	}

	@Test
	public void testAnotherTwoLiner() throws Exception {
		String test = "+U:http://www.ifomis.org/bfo/1.1/snap#MaterialEntity\n"
				    + "+D:http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart\n";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Assert.assertEquals(2, conf.getTreePartsToSave().size());
	}

	/** See bug <a href="https://github.com/enanomapper/slimmer/issues/19">#19</a>. */
	@Test
	public void bug19() throws Exception {
		String test = "+D(http://purl.obolibrary.org/obo/IAO_0000030):http://semanticscience.org/resource/CHEMINF_000123 chemical descriptor\n"
				    + "+(http://purl.obolibrary.org/obo/BFO_0000019):http://semanticscience.org/resource/CHEMINF_000247 surface area\n"
				    + "+D(http://purl.obolibrary.org/obo/BFO_0000019):http://semanticscience.org/resource/CHEMINF_000101 chemical substance quality\n"
				    + "+D(http://purl.obolibrary.org/obo/BFO_0000019):http://semanticscience.org/resource/CHEMINF_000031 molecular entity quality\n"
				    + "+D(http://purl.obolibrary.org/obo/IAO_0000030):http://semanticscience.org/resource/CHEMINF_000014 chemical entity information format description\n"
				    + "-:http://purl.obolibrary.org/obo/PATO_0000125 mass";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Assert.assertEquals(5, conf.getTreePartsToSave().size());
		Assert.assertEquals(1, conf.getTreePartsToRemove().size());
	}
}
