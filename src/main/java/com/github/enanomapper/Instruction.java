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

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (scope == Scope.SINGLE) {}
		else if (scope == Scope.UP) { buffer.append("U"); }
		else if (scope == Scope.UP) { buffer.append("D"); }
		if (newSuperClass != null) {
			buffer.append('(').append(newSuperClass).append(')');
		}
		buffer.append(':').append(this.uriString);
		if (comment != null) {
			buffer.append(' ').append(comment);
		}
		return buffer.toString();
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Instruction)) return false;
		return this.hashCode() == obj.hashCode();
	}
}
