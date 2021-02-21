package com.github.enanomapper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class InstructionTest {

	@Test
	public void testHashCode() throws Exception {
		String uri = "http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Instruction superIns = new Instruction(uri, Instruction.Scope.SINGLE, "User as Superclass");
		Instruction superIns2 = new Instruction(uri, Instruction.Scope.SINGLE, "User as Superclass");
		assertEquals(superIns.hashCode(), superIns2.hashCode());
	}

	@Test
	public void testEquals() throws Exception {
		String uri = "http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Instruction superIns = new Instruction(uri, Instruction.Scope.SINGLE, "User as Superclass");
		Instruction superIns2 = new Instruction(uri, Instruction.Scope.SINGLE, "User as Superclass");
		assertEquals(superIns, superIns2);
	}

	@Test
	public void testHashCode_Different() throws Exception {
		String uri = "http://www.ifomis.org/bfo/1.1/snap#DependentContinuant";
		Instruction superIns = new Instruction(uri, Instruction.Scope.SINGLE, "User as Superclass");
		Instruction superIns2 = new Instruction(uri, Instruction.Scope.SINGLE, "User as Subclass");
		assertNotSame(superIns.hashCode(), superIns2.hashCode());
	}

}
