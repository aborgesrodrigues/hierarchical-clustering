package br.ufpe.cin;

public class Variable {
	private String name;
	private boolean primitive;
	private int quantidade;
	private boolean keepTogether;
	private Class classType;
	
	public Variable(){
		this.keepTogether = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isPrimitive() {
		return primitive;
	}

	public void setPrimitive(boolean primitive) {
		this.primitive = primitive;
	}

	public int getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(int quantidade) {
		this.quantidade = quantidade;
	}

	public boolean isKeepTogether() {
		return keepTogether;
	}

	public void setKeepTogether(boolean keepTogether) {
		this.keepTogether = keepTogether;
	}

	public Class getClassType() {
		return classType;
	}

	public void setClassType(Class classType) {
		this.classType = classType;
	}
	
	
}
