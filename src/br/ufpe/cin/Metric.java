package br.ufpe.cin;

public class Metric {
	public enum Type{
		complexity,
		connectionStrength
	}
	private double valor;
	private Type type;
	public static double wabs = 0.7;
	public static final double wpri = 0.3;
	private boolean keepTogether;
	
	public Metric(Type type){
		this.type = type;
		this.valor = 0;
		this.keepTogether = false;
	}
	
	public double getValor() {
		return valor;
	}
	public void setValor(double valor) {
		this.valor = valor;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public boolean isKeepTogether() {
		return keepTogether;
	}

	public void setKeepTogether(boolean keepTogether) {
		this.keepTogether = keepTogether;
	}

	
}
