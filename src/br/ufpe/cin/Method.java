package br.ufpe.cin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Method  implements Cloneable{
	private Class classType;
	private String name;
	private Class returnType;
	private List<Class> parametersType;
	private List<Class> ClasssType;
	private List<Class> instantiationsType;
	private Map<String, Method> methodsInvocation;
	
	public Method(){
		this.parametersType = new ArrayList<Class>();
		this.ClasssType = new ArrayList<Class>();
		this.instantiationsType = new ArrayList<Class>();
		this.setMethodsInvocation(new HashMap<String, Method>());
	}
	
    
	public boolean equals(Object object) {
	    if(object instanceof Method) {
	    	Method other = ((Method) object);
	    	if(this.getClassType().equals(other.getClassType()) && this.getName().equals(other.getName())){
	    		if(this.getParametersType().size() !=  other.getParametersType().size())
	    			return false;
	    		
	    		for(int i = 0; i < this.getParametersType().size(); i++){
	    			if(!this.getParametersType().get(i).equals(other.getParametersType().get(i)))
	    				return false;
	    		}
	    		return true;
	    	}
	    	else
	    		return false;
	    } else {
	        return false;
	    }
	}

	
	public Class getParameter(String parameter, List<String> types) {
		for(Class classType : this.getParametersType()){
			if(!classType.getName().equals(parameter))
				return null;
			
			if(classType.getParameterizeds().size() != types.size())
				return null;
			
			for(int i = 0; i < classType.getParameterizeds().size(); i++){
				if(!classType.getParameterizeds().get(i).getName().equals(types.get(i)))
					return null;
			}
			
			return classType;
		}
		return null;
	}


	public Class getClassType() {
		return classType;
	}


	public void setClassType(Class classType) {
		this.classType = classType;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Class getReturnType() {
		return returnType;
	}


	public void setReturnType(Class returnType) {
		this.returnType = returnType;
	}


	public List<Class> getParametersType() {
		return parametersType;
	}


	public void setParametersType(List<Class> parametersType) {
		this.parametersType = parametersType;
	}


	public List<Class> getClasssType() {
		return ClasssType;
	}


	public void setClasssType(List<Class> ClasssType) {
		this.ClasssType = ClasssType;
	}


	public List<Class> getInstantiationsType() {
		return instantiationsType;
	}


	public void setInstantiationsType(List<Class> instantiationsType) {
		this.instantiationsType = instantiationsType;
	}


	public Map<String, Method> getMethodsInvocation() {
		return methodsInvocation;
	}


	public void setMethodsInvocation(Map<String, Method> methodsInvocation) {
		this.methodsInvocation = methodsInvocation;
	}

	public String getFullName(){
		String fullyName = "";
		
		for(Class parameter: this.getParametersType()){
			if(!fullyName.isEmpty())
				fullyName += ",";
			
			fullyName += parameter.getName();
			
			if(parameter.getParameterizeds().size() > 0){
				fullyName += "<";
				
				for(Class parameterized : parameter.getParameterizeds())
					fullyName += parameterized.getName();
				
				fullyName += ">";
			}
		}
		
		fullyName = this.getName() + "(" + fullyName + ")";
		
		return fullyName;
	}
	
	public static String getFullName(String name, List<Class> parameters){
		String fullyName = "";
		
		for(Class parameter: parameters){
			if(!fullyName.isEmpty())
				fullyName += ",";
			
			fullyName = parameter.getName();
			
			if(parameters.size() > 0){
				fullyName += "<";
				
				for(Class parameterized : parameter.getParameterizeds())
					fullyName += parameterized.getName();
				
				fullyName += ">";
			}
		}

		fullyName = name + "(" + fullyName + ")";
		
		return fullyName;
	}
	
}
