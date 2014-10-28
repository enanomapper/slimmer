package com.github.enanomapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

public class Configuration {

	private Set<Instruction> irisToSave = new HashSet<Instruction>();
	private Set<Instruction> irisToRemove = new HashSet<Instruction>();
	
	public void removeTreePart(Instruction instruction) {
		irisToSave.add(instruction);
	}
	
	public void addTreePart(Instruction instruction) {
		irisToRemove.add(instruction);
	}

	public Set<Instruction> getTreePartsToSave() {
		return irisToSave;
	}

	public Set<Instruction> getTreePartsToRemove() {
		return irisToRemove;
	}

	public void read(File file) throws Exception {
		read(new FileReader(file));
	}

	public void read(Reader file) throws Exception {
		BufferedReader reader = new BufferedReader(file);
		String line = reader.readLine();
		while (line != null) {
			String instruction = line.trim();
			char addRemoveInstruct = instruction.charAt(0);
			if (addRemoveInstruct != '+' && addRemoveInstruct != '-') {
				reader.close();
				throw new Exception("Invalid configuration input: first character should be '+' or '-'.");
			}
			char upDownInstruct = instruction.charAt(1);
			Instruction.Scope scope = Instruction.Scope.SINGLE;
			int startURI = 2;
			if (upDownInstruct != ':') {
				if (upDownInstruct == 'U') {
					scope = Instruction.Scope.UP;
					if (instruction.charAt(2) != ':') {
						reader.close();
						throw new Exception("Invalid configuration input: expected ':' at position 3.");
					}
					startURI = 3;
				} else if (upDownInstruct == 'D') {
					scope = Instruction.Scope.DOWN;
					if (instruction.charAt(2) != ':') {
						reader.close();
						throw new Exception("Invalid configuration input: expected ':' at position 3.");
					}
					startURI = 3;
				} else {
					reader.close();
					throw new Exception("Invalid configuration input: second instruction should be 'U', 'D', or empty.");
				}
			} else {
				// OK, SINGLE
			}

			String iri = instruction.substring(startURI);
			int index = iri.indexOf(' ');
			String comment = "";
			if (index != -1) {
				comment = iri.substring(index).trim();
				iri = iri.substring(0, index);
			}
			Instruction ins = new Instruction(iri, scope, comment);
			if (addRemoveInstruct == '+') {
				irisToSave.add(ins);
			} else {
				irisToRemove.add(ins);
			}
			
			line = reader.readLine();
		}
		reader.close();
	}
}