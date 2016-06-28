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
	private Class inheritage;
	private boolean ignored;
	private boolean inProject;
	private TypeClass typeClass;
	private String filePath;
	private List<Class> annotations;
	private Map<Class, Metric> connectivityStrength;
	private Map<String, Method> methods;
	private List<Class> variables;
	private List<Class> parameterizeds;
	private List<Class> interfaces;
	
	public Class(){
		setConnectivityStrength(new HashMap<Class, Metric>());
		setMethods(new HashMap<String, Method>());
		this.setVariables(new ArrayList<Class>());
		this.setParameterizeds(new ArrayList<Class>());
		this.setInterfaces(new ArrayList<Class>());
		this.comlexity = new Metric(Metric.Type.complexity);
		this.annotations = new ArrayList<Class>();
		this.ignored = false;
		this.inProject = false;
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

	public Map<String, Method> getMethods() {
		return methods;
	}

	public void setMethods(Map<String, Method> methods) {
		this.methods = methods;
	}

	public List<Class> getVariables() {
		return variables;
	}

	public void setVariables(List<Class> variables) {
		this.variables = variables;
	}

	public List<Class> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<Class> interfacesType) {
		this.interfaces = interfacesType;
	}

	public List<Class> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<Class> annotationsType) {
		this.annotations = annotationsType;
	}
	
	public Class getInheritage() {
		return inheritage;
	}

	public void setInheritage(Class inheritage) {
		this.inheritage = inheritage;
	}

	public List<Class> getParameterizeds() {
		return parameterizeds;
	}

	public void setParameterizeds(List<Class> parameterizeds) {
		this.parameterizeds = parameterizeds;
	}

	public boolean isInProject() {
		return inProject;
	}

	public void setInProject(boolean inProject) {
		this.inProject = inProject;
	}

	public Class getVariable(String parameter, List<String> types) {
		for(Class variable : this.variables){
			if(!variable.getName().equals(parameter))
				return null;
			
			if(variable.getParameterizeds().size() != types.size())
				return null;
			
			for(int i = 0; i < variable.getParameterizeds().size(); i++){
				if(!variable.getParameterizeds().get(i).getName().equals(types.get(i)))
					return null;
			}
			
			return variable;
		}
		return null;
	}
	
	public Method getMethod(String name, List<Class> parameters){
		String fullName = Method.getFullName(name, parameters);
		Method method = this.getMethods().get(fullName);
		
		if(method ==null){
			method = new Method();
			method.setName(name);
			method.setClassType(this);
			
			for(Class parameter : parameters)
				method.getParametersType().add(parameter);
		}
		
		return method;
	}
	
	public String getFullName(){
		String fullyName = "";
				
		if(this.getParameterizeds().size() > 0){
			fullyName += "<";
			for(Class parameter: this.getParameterizeds()){
				if(!fullyName.equals("<"))
					fullyName += ",";
				
				fullyName += parameter.getName();
				
				if(parameter.getParameterizeds().size() > 0){
					fullyName += "<";
					
					for(Class parameterized : parameter.getParameterizeds())
						fullyName += parameterized.getName();
					
					fullyName += ">";
				}
			}
			fullyName += ">";
		}
		
		fullyName = this.getName() + fullyName;
		
		if(this.inheritage != null)
			fullyName += " extends " + this.inheritage.getName();
		
		if(this.getInterfaces().size() > 0){
			String interfaces = "";
			for(Class interfaceAux : this.getInterfaces()){
				if(!interfaces.isEmpty())
					interfaces += ",";
				
				interfaces += interfaceAux.getName();
			}
			
			fullyName += " implements " + interfaces;
		}
		return fullyName;
	}
}
