package com.github.enanomapper;

import java.io.StringReader;

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
}
