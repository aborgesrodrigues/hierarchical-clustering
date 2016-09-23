package br.ufpe.cin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Type;

public class Class {
	public enum TypeClass{
		persistence,
		business,
		entity,
		interfaceType,
		other,
		outside
	}
	
	private Type tipo;
	private String name;
	private Metric comlexity;
	private Class superClass;
	private boolean ignored;
	private boolean inProject;
	private boolean primitiveType;
	private boolean isInterface;
	private TypeClass typeClass;
	private String filePath;
	private List<Class> annotations;
	private Map<Class, Metric> connectivityStrength;
	private Map<String, Method> methods;
	private List<Class> variables;
	private List<Class> parameterizeds;
	private List<Class> interfaces;
	private Class belongsTo;
	private String packageInfo;
	private List<Class> implementClass;
	private Color color;
	private List<Class> innerClassDependencies;
	private List<Class> outerClassDependencies;
	private boolean dependenciesCalculated;
	
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
		this.primitiveType = false;
		this.packageInfo = "";
		this.implementClass = new ArrayList<Class>();
		this.innerClassDependencies = new ArrayList<Class>();
		this.outerClassDependencies = new ArrayList<Class>();
		this.setSuperClass(null);
		dependenciesCalculated = false;
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
	
	public String getReducedName() {
		return name.replace(this.packageInfo + ".", "");
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

	public Class getSuperClass() {
		return superClass;
	}
	
	public List<Class> getSuperClassess() {
		List<Class> superClasses = new ArrayList<Class>();
		Class ancestral = this.superClass;
		
		if(ancestral != null){
			superClasses.addAll(ancestral.getSuperClassess());
			superClasses.add(ancestral);
		}
		
		return superClasses;
	}

	public void setSuperClass(Class superClass) {
		this.superClass = superClass;
	}

	public Map<String, Method> getMethods() {
		return methods;
	}
	
	public List<Method> getAllMethods() {
		List<Method> allMethods = new ArrayList<Method>();
		Class ancestral = this.getSuperClass();
		
		if(ancestral != null)
			allMethods.addAll(ancestral.getAllMethods());
		
		allMethods.addAll(this.methods.values());
		return allMethods;
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
		
		if(this.superClass != null)
			fullyName += " extends " + this.superClass.getName();
		
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

	public boolean isPrimitiveType() {
		return primitiveType;
	}

	public void setPrimitiveType(boolean primitiveType) {
		this.primitiveType = primitiveType;
	}

	public Class getBelongsTo() {
		return belongsTo;
	}

	public void setBelongsTo(Class belongsTo) {
		this.belongsTo = belongsTo;
	}

	public String getPackageInfo() {
		return packageInfo;
	}

	public void setPackageInfo(String packageInfo) {
		this.packageInfo = packageInfo;
	}

	public boolean isInterface() {
		return isInterface;
	}

	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}

	public List<Class> getImplementClass() {
		return implementClass;
	}

	public void setImplementClass(List<Class> implementClass) {
		this.implementClass = implementClass;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public List<Method> getPublicMethods(){
		List<Method> publicMethods = new ArrayList<Method>();
		
		for(Method method : this.methods.values()){
			if(method.getModifier().equals(Method.Modifier.publicMethod))
				publicMethods.add(method);
		}
		
		return publicMethods;
	}
	
	public List<Method> getPrivateMethods(){
		List<Method> privateMethods = new ArrayList<Method>();
		
		for(Method method : this.methods.values()){
			if(method.getModifier().equals(Method.Modifier.privateMethod))
				privateMethods.add(method);
		}
		
		return privateMethods;
	}
	
	public List<Method> getProtectedMethods(){
		List<Method> protectedMethods = new ArrayList<Method>();
		
		for(Method method : this.methods.values()){
			if(method.getModifier().equals(Method.Modifier.protectedMethod))
				protectedMethods.add(method);
		}
		
		return protectedMethods;
	}
	
	private void calculateDependencies(){
		if(!dependenciesCalculated){
			for(Class variable : this.getVariables())
				addClassDependencies(variable);
			
			for(Method method : this.getMethods().values()){
				for(Method methodInvocation : method.getMethodsInvocation().values())
					addClassDependencies(methodInvocation.getClassType());
			}
			this.dependenciesCalculated = true;
		}
	}
	
	private void addClassDependencies(Class dependentClass){
		if(dependentClass.getTypeClass().equals(Class.TypeClass.business) || dependentClass.getTypeClass().equals(Class.TypeClass.interfaceType)){
			if(dependentClass.getTypeClass().equals(Class.TypeClass.interfaceType))
				dependentClass = dependentClass.getImplementClass().get(0);
			
			if(dependentClass.getColor().equals(this.getColor())){
				if(!this.innerClassDependencies.contains(dependentClass))
					this.innerClassDependencies.add(dependentClass);
			}
			else{
				if(!this.outerClassDependencies.contains(dependentClass))
					this.outerClassDependencies.add(dependentClass);
			}
		}
		else if(dependentClass.getTypeClass().equals(Class.TypeClass.persistence)){
			if(!this.innerClassDependencies.contains(dependentClass))
				this.innerClassDependencies.add(dependentClass);
		}
	}

	public List<Class> getInnerClassDependencies() {
		calculateDependencies();
		return innerClassDependencies;
	}

	public List<Class> getOuterClassDependencies() {
		calculateDependencies();
		return outerClassDependencies;
	}

}
