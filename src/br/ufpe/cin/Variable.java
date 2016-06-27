package br.ufpe.cin;

import java.util.ArrayList;
import java.util.List;

public class Variable {
	private boolean primitive;
	private boolean keepTogether;
	private Class nameType;
	private List<Class> classTypes;
	
	public Variable(){
		this.keepTogether = false;
		this.classTypes = new ArrayList<Class>();
	}

	public boolean isPrimitive() {
		return primitive;
	}

	public void setPrimitive(boolean primitive) {
		this.primitive = primitive;
	}

	public boolean isKeepTogether() {
		return keepTogether;
	}

	public void setKeepTogether(boolean keepTogether) {
		this.keepTogether = keepTogether;
	}
	
	public boolean equals(Object object) {
	    if(object instanceof Variable) {
	    	Variable other = ((Variable) object);
	    	if(!this.getNameType().equals(other.getNameType()))
	    		return false;
	    	
	    	if(this.getClassTypes().size() != other.getClassTypes().size())
	    		return false;
	    	
	    	for(int i = 0; i < this.getClassTypes().size(); i++){
	    		if(!this.getClassTypes().get(i).equals(other.getClassTypes().get(i)))
	    			return false;
	    	}
	    	return true;
	    } else {
	        return false;
	    }
	}

	public List<Class> getClassTypes() {
		return classTypes;
	}

	public void setClassTypes(List<Class> classTypes) {
		this.classTypes = classTypes;
	}
 
	public Class getNameType() {
		return nameType;
	}

	public void setNameType(Class nameType) {
		this.nameType = nameType;
	}
	
}
