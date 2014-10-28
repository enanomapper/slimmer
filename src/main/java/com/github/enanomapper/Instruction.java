package com.github.enanomapper;

public class Instruction {

	public enum Scope {
		UP,
		SINGLE,
		DOWN
	}

	private String uriString;
	private Scope scope;
	private String comment;
	private String newSuperClass;
	
	public Instruction(String uriString, Scope scope, String comment) {
		this.uriString = uriString;
		this.scope = scope;
		this.comment = comment;
	}

	public String getUriString() {
		return uriString;
	}

	public Scope getScope() {
		return scope;
	}

	public String getComment() {
		return comment;
	}

	public String getNewSuperClass() {
		return newSuperClass;
	}

	public void setNewSuperClass(String newSuperClass) {
		this.newSuperClass = newSuperClass;
	}
	
}
