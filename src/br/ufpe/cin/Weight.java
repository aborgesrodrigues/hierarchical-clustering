package br.ufpe.cin;

public class Weight {
	private String origin;
	private String destination;
	
	public static enum TypeRelationship{
		association,
		generalization,
		interfacing,
		implementation,
		callMethod
	}
	
	public Weight(String origin, String destination, TypeRelationship typeRelationship){
		this.typeRelationship = typeRelationship;
		this.origin = origin;
		this.destination = destination;
	}
	
	private TypeRelationship typeRelationship;

	public TypeRelationship getTypeRelationship() {
		return typeRelationship;
	}

	public void setTypeRelationship(TypeRelationship typeRelationship) {
		this.typeRelationship = typeRelationship;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	public boolean equals(Object object) {
	    if(object instanceof Weight) {
	    	if(this.getOrigin().equals(((Weight) object).getOrigin()) && this.getDestination().equals(((Weight) object).getDestination()) && this.getTypeRelationship().equals(((Weight) object).getTypeRelationship()))
	    		return true;
	    	else
	    		return false;
	    } else {
	        return false;
	    }
	}


}
