package com.github.enanomapper;

public class Instruction {

	public enum Scope {
		UP,
		SINGLE,
		DOWN
	}

	private String uriString;
	private Scope scope;
	
	public Instruction(String uriString, Scope scope) {
		this.uriString = uriString;
		this.scope = scope;
	}

	public String getUriString() {
		return uriString;
	}

	public Scope getScope() {
		return scope;
	}
	
}
