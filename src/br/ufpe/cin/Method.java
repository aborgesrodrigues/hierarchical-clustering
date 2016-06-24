package br.ufpe.cin;

import java.util.HashMap;
import java.util.Map;

public class Method  implements Cloneable{
	private Class classType;
	private String name;
	private Map<Variable, Class> parametersType;
	private Map<Variable, Class> variablesType;
	private Map<Variable, Class> instantiationsType;
	private Class returnType;
	private String returnClass;
	private Map<String, Variable> parameters;
	private Map<String, Variable> variables;
	private Map<String, Variable> instantiations;
	private Map<String, Method> methodsInvocation;
	
	public Method(){
		this.setParametersType(new HashMap<Variable, Class>());
		this.setVariablesType(new HashMap<Variable, Class>());
		this.setInstantiationsType(new HashMap<Variable, Class>());
		this.setParameters(new HashMap<String, Variable>());
		this.setVariables(new HashMap<String, Variable>());
		this.setInstantiations(new HashMap<String, Variable>());
		this.setMethodsInvocation(new HashMap<String, Method>());
	}
	
    
	public boolean equals(Object object) {
	    if(object instanceof Method) {
	    	if(this.getClassType().equals(((Method) object).getClassType()) && this.getName().equals(((Method) object).getName())){
	    		for(String parameter : this.getParameters().keySet()){
	    			for(String parameterAux : ((Method) object).getParameters().keySet()){
	    				if(!parameter.equals(parameterAux))
	    					return false;
	    			}
	    			
	    		}
	    		return true;
	    	}
	    	else
	    		return false;
	    } else {
	        return false;
	    }
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


	public Map<Variable, Class> getParametersType() {
		return parametersType;
	}


	public void setParametersType(Map<Variable, Class> parametersType) {
		this.parametersType = parametersType;
	}


	public Map<Variable, Class> getVariablesType() {
		return variablesType;
	}


	public void setVariablesType(Map<Variable, Class> variablesType) {
		this.variablesType = variablesType;
	}


	public Map<Variable, Class> getInstantiationsType() {
		return instantiationsType;
	}


	public void setInstantiationsType(Map<Variable, Class> instantiationsType) {
		this.instantiationsType = instantiationsType;
	}


	public Class getReturnType() {
		return returnType;
	}


	public void setReturnType(Class returnType) {
		this.returnType = returnType;
	}


	public String getReturnClass() {
		return returnClass;
	}


	public void setReturnClass(String returnClass) {
		this.returnClass = returnClass;
	}


	public Map<String, Variable> getInstantiations() {
		return instantiations;
	}


	public void setInstantiations(Map<String, Variable> instantiations) {
		this.instantiations = instantiations;
	}


	public Map<String, Method> getMethodsInvocation() {
		return methodsInvocation;
	}


	public void setMethodsInvocation(Map<String, Method> methodsInvocation) {
		this.methodsInvocation = methodsInvocation;
	}


	public Map<String, Variable> getParameters() {
		return parameters;
	}


	public void setParameters(Map<String, Variable> parameters) {
		this.parameters = parameters;
	}


	public Map<String, Variable> getVariables() {
		return variables;
	}


	public void setVariables(Map<String, Variable> variables) {
		this.variables = variables;
	}

}
