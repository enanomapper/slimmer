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
	public void testSingularemove() throws Exception {
		String test = "-:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Assert.assertEquals(1, conf.getTreePartsToRemove().size());
	}
}
