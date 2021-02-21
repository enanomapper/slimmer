package com.github.enanomapper;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigurationTest {

	@Test
	public void testParsingUp() throws Exception {
		String test = "+U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		assertEquals(1, conf.getTreePartsToSave().size());
	}

	@Test
	public void testParsingWithComment() throws Exception {
		String test = "+U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant Comment";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		assertEquals(1, conf.getTreePartsToSave().size());
		assertEquals("Comment", conf.getTreePartsToSave().iterator().next().getComment());
	}

	@Test
	public void testParsingWithNewSuperClass() throws Exception {
		String test = "+D(http://www.ifomis.org/bfo/1.1/snap#Entity):http://www.ifomis.org/bfo/1.1/snap#MaterialEntity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		assertEquals(2, conf.getTreePartsToSave().size());
		Iterator<Instruction> toSaveInstructions = conf.getTreePartsToSave().iterator();
		Instruction instruction = toSaveInstructions.next();
		while (!instruction.getUriString().equals("http://www.ifomis.org/bfo/1.1/snap#MaterialEntity")) // ok, the next one
			instruction = toSaveInstructions.next();
		assertEquals("http://www.ifomis.org/bfo/1.1/snap#Entity", instruction.getNewSuperClass());
	}

	@Test
	public void testParsingSingleWithNewSuperClass() throws Exception {
		String test = "+(http://www.ifomis.org/bfo/1.1/snap#Entity):http://www.ifomis.org/bfo/1.1/snap#MaterialEntity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		assertEquals(2, conf.getTreePartsToSave().size());
		Iterator<Instruction> toSaveInstructions = conf.getTreePartsToSave().iterator();
		Instruction instruction = toSaveInstructions.next();
		if (!instruction.getUriString().equals("http://www.ifomis.org/bfo/1.1/snap#MaterialEntity")) // ok, the next one
			instruction = toSaveInstructions.next();
		assertEquals("http://www.ifomis.org/bfo/1.1/snap#Entity", instruction.getNewSuperClass());
		assertEquals("http://www.ifomis.org/bfo/1.1/snap#MaterialEntity", instruction.getUriString());
	}

	@Test
	public void testMakeNewSubclassProperty() throws Exception {
		String test = "+:http://www.ifomis.org/bfo/1.1#Entity\n"
	                + "+(http://www.ifomis.org/bfo/1.1#Entity):http://www.ifomis.org/bfo/1.1/snap#MaterialEntity";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		Instruction instruction1 = null;
		Instruction instruction2 = null;
		for (Instruction instruction : irisToSave) {
			System.out.println("Instr: " + instruction);
			if (instruction.getUriString().endsWith("#MaterialEntity")) instruction1 = instruction;
			if (instruction.getUriString().endsWith("#Entity")) instruction2 = instruction;
		}
		assertEquals("http://www.ifomis.org/bfo/1.1/snap#MaterialEntity", instruction1.getUriString());
		assertNotNull(instruction1.getNewSuperClass());
		assertEquals("http://www.ifomis.org/bfo/1.1#Entity", instruction1.getNewSuperClass());
		assertEquals("http://www.ifomis.org/bfo/1.1#Entity", instruction2.getUriString());
		assertNull(instruction2.getNewSuperClass());
	}
	
	@Test
	public void testRemoveUp() throws Exception {
		String test = "-U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		assertEquals(0, conf.getTreePartsToSave().size());
		assertEquals(1, conf.getTreePartsToRemove().size());
	}

	@Test
	public void testMissingAdd() {
		Exception exception = assertThrows(Exception.class, () -> {
		    String test = "U:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		    Configuration conf = new Configuration();
		    conf.read(new StringReader(test));
		});
		assertNotNull(exception);
	}

	@Test
	public void testMissingColon() {
		Exception exception = assertThrows(Exception.class, () -> {
		    String test = "+Uhttp://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		    Configuration conf = new Configuration();
		    conf.read(new StringReader(test));
		});
		assertNotNull(exception);
	}

	@Test
	public void testSingularAdd() throws Exception {
		String test = "+:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		assertEquals(1, conf.getTreePartsToSave().size());
	}

	@Test
	public void testDown() throws Exception {
		String test = "+D:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		assertEquals(1, conf.getTreePartsToSave().size());
	}

	@Test
	public void bug45() throws Exception {
		// the next line was the actual code; the problem is that after the IRI there is a TAB where a space was expected
		String test = "+D(http://purl.bioontology.org/ontology/npo#NPO_707):http://purl.obolibrary.org/obo/CHEBI_50828	silicon dioxide nanoparticle ";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		Set<Instruction> irisToSave = conf.getTreePartsToSave();
		assertEquals(2, irisToSave.size());
		Instruction instruction1 = null;
		for (Instruction instruction : irisToSave) {
			System.out.println("Instr: \"" + instruction + "\"");
			if (instruction.getUriString().contains("CHEBI_50828")) instruction1 = instruction;
		}
		assertEquals("http://purl.obolibrary.org/obo/CHEBI_50828", instruction1.getUriString());
		assertEquals("http://purl.bioontology.org/ontology/npo#NPO_707", instruction1.getNewSuperClass());
		assertEquals("silicon dioxide nanoparticle", instruction1.getComment());
	}

	@Test
	public void testSingularemove() throws Exception {
		String test = "-:http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		assertEquals(1, conf.getTreePartsToRemove().size());
	}

	@Test
	public void testTwoLiner() throws Exception {
		String test = "+U:http://www.ifomis.org/bfo/1.1/snap#MaterialEntity\n"
				    + "-D:http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart\n";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		assertEquals(1, conf.getTreePartsToSave().size());
		assertEquals(1, conf.getTreePartsToRemove().size());
	}

	@Test
	public void testAnotherTwoLiner() throws Exception {
                String test = "+U:http://www.ifomis.org/bfo/1.1/snap#MaterialEntity\n"
				    + "+D:http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart\n";
		Configuration conf = new Configuration();
		conf.read(new StringReader(test));
		assertEquals(2, conf.getTreePartsToSave().size());
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
		System.out.println(conf.getTreePartsToSave());
		assertEquals(7, conf.getTreePartsToSave().size());
		assertEquals(1, conf.getTreePartsToRemove().size());
	}
}
