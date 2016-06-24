package br.ufpe.cin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Type;

public class Class {
	public enum TypeClass{
		persistence,
		business,
		entity
	}
	
	private Type tipo;
	private String name;
	private Metric comlexity;
	private String extendses;
	private Class extendType;
	private boolean ignored;
	private TypeClass typeClass;
	private String filePath;
	private List<String> annotations;
	private List<Class> annotationsType;
	private Map<Class, Metric> connectivityStrength;
	private List<String> interfaces;
	private Map<String, Method> methods;
	private Map<String, Variable> variables;
	private Map<Variable, Class> variablesType;
	private List<Class> interfacesType;
	
	public Class(){
		setConnectivityStrength(new HashMap<Class, Metric>());
		setMethods(new HashMap<String, Method>());
		this.setVariables(new HashMap<String, Variable>());
		this.variablesType = new HashMap<Variable, Class>();
		this.setInterfaces(new ArrayList<String>());
		this.setInterfacesType(new ArrayList<Class>());
		this.comlexity = new Metric(Metric.Type.complexity);
		this.annotations = new ArrayList<String>();
		this.annotationsType = new ArrayList<Class>();
		this.ignored = false;
	}
	
	public boolean equals(Object object) {
	    if(object instanceof Class) {
	    	if(this.getName().equals(((Class) object).getName()))
	    		return true;
	    	else
	    		return false;
	    } else {
	        return false;
	    }
	}

	public Type getTipo() {
		return tipo;
	}

	public void setTipo(Type tipo) {
		this.tipo = tipo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Metric getComlexity() {
		return comlexity;
	}

	public void setComlexity(Metric comlexity) {
		this.comlexity = comlexity;
	}

	public String getExtendses() {
		return extendses;
	}

	public void setExtendses(String extendses) {
		this.extendses = extendses;
	}

	public boolean isIgnored() {
		return ignored;
	}

	public void setIgnored(boolean ignored) {
		this.ignored = ignored;
	}

	public TypeClass getTypeClass() {
		return typeClass;
	}

	public void setTypeClass(TypeClass typeClass) {
		this.typeClass = typeClass;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Map<Class, Metric> getConnectivityStrength() {
		return connectivityStrength;
	}

	public void setConnectivityStrength(Map<Class, Metric> connectivityStrength) {
		this.connectivityStrength = connectivityStrength;
	}

	public List<String> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<String> interfaces) {
		this.interfaces = interfaces;
	}

	public Map<String, Method> getMethods() {
		return methods;
	}

	public void setMethods(Map<String, Method> methods) {
		this.methods = methods;
	}

	public Map<String, Variable> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, Variable> variables) {
		this.variables = variables;
	}

	public Map<Variable, Class> getVariablesType() {
		return variablesType;
	}

	public void setVariablesType(Map<Variable, Class> variablesType) {
		this.variablesType = variablesType;
	}

	public List<Class> getInterfacesType() {
		return interfacesType;
	}

	public void setInterfacesType(List<Class> interfacesType) {
		this.interfacesType = interfacesType;
	}

	public List<Class> getAnnotationsType() {
		return annotationsType;
	}

	public void setAnnotationsType(List<Class> annotationsType) {
		this.annotationsType = annotationsType;
	}

	public Class getExtendType() {
		return extendType;
	}

	public void setExtendType(Class extendType) {
		this.extendType = extendType;
	}

	public List<String> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<String> annotations) {
		this.annotations = annotations;
	}

}
