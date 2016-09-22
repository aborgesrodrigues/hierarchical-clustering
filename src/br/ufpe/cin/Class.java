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
	private Map<String, Integer> businessDependencies;
	private Map<String, Integer> persistenceDependencies;
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
		this.dependenciesCalculated = false;
		this.setSuperClass(null);
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
	
	public int getAmountBusinessDependencies(){
		calculateDependencies();
		
		return this.businessDependencies.keySet().size();
	}
	
	public int getAmountPersistenceDependencies(){
		calculateDependencies();
		
		return this.persistenceDependencies.keySet().size();
	}
	
	private void calculateDependencies(){
		if(!this.dependenciesCalculated){
			this.businessDependencies = new HashMap<String, Integer>();
			this.persistenceDependencies = new HashMap<String, Integer>();
			
			for(Class variable : this.getVariables())
				addDependenciesCount(this, variable);
			
			for(Method method : this.getMethods().values()){
				for(Class instantiation : method.getInstantiationsType())
					addDependenciesCount(this, instantiation);
				
				for(Method methodInvocation : method.getMethodsInvocation().values())
					addDependenciesCount(this, methodInvocation.getClassType());
			}
			this.dependenciesCalculated = true;
		}
	}
	
    private void addDependenciesCount(Class classOrigin, Class classDestiny){
    	if(!classOrigin.equals(classDestiny)){
    		if(classDestiny.getTypeClass().equals(Class.TypeClass.business) || classDestiny.getTypeClass().equals(Class.TypeClass.interfaceType)){
				if(classDestiny.getTypeClass().equals(Class.TypeClass.interfaceType))
					classDestiny = classDestiny.getImplementClass().get(0);//EJB has only 1 implemented class
				
				if(!classOrigin.getColor().equals(classDestiny.getColor())){					
					Integer amountDependenciesBusiness = this.businessDependencies.get(classDestiny.getName());
					
					amountDependenciesBusiness = amountDependenciesBusiness == null ? 1 : amountDependenciesBusiness++;
					this.businessDependencies.put(classDestiny.getName(), amountDependenciesBusiness);
					System.out.println(classOrigin.getName() + ": " + classOrigin.getColor() + " - " + classDestiny.getName() + ": " + classDestiny.getColor() + " - " + classOrigin.getColor().equals(classDestiny.getColor()));
				}
			}
    		
			/*if(classDestiny.getTypeClass().equals(Class.TypeClass.business) || classDestiny.getTypeClass().equals(Class.TypeClass.interfaceType)){
				if(classDestiny.getTypeClass().equals(Class.TypeClass.interfaceType))
					classDestiny = classDestiny.getImplementClass().get(0);//EJB has only 1 implemented class
				
				Integer amountDependenciesBusiness = this.businessDependencies.get(classDestiny.getName());
				
				amountDependenciesBusiness = amountDependenciesBusiness == null ? 1 : amountDependenciesBusiness++;
				this.businessDependencies.put(classDestiny.getName(), amountDependenciesBusiness);
				
			}
			else if(classDestiny.getTypeClass().equals(Class.TypeClass.persistence)){
				Integer amountDependenciesPersistence = this.persistenceDependencies.get(classDestiny.getName());
				
				amountDependenciesPersistence = amountDependenciesPersistence == null ? 1 : amountDependenciesPersistence++;
				this.persistenceDependencies.put(classDestiny.getName(), amountDependenciesPersistence);
			}*/
    	}
    }
    
    public int getANA(){
		Class ancestor = this.superClass;
		int amountOfAncestor = 0;
		
		while(ancestor != null){
			ancestor = ancestor.superClass;
			amountOfAncestor++;
		}
		
		return amountOfAncestor;
    }
    
    public double getDAM(){
    	if(this.getMethods().size() == 0)
    		return 0.0;
    	return (double)(this.getPrivateMethods().size() + this.getProtectedMethods().size()) / this.getMethods().size();
    }
    
    private List<Class> getParameters(){
    	List<Class> parameters = new ArrayList<Class>();
    	
    	for(Method method : this.methods.values()){
    		for(Class parameter : method.getParametersType()){
    			if(!parameters.contains(parameter))
    				parameters.add(parameter);
    		}
    	}
    	
    	return parameters;
    }
    
    public double getCAM(){
    	List<Class> parameters = this.getParameters();
    	
    	int auxTotal = 0;
    	
    	for(Method method : this.methods.values()){
    		int auxMethod = 0;
    		for(Class parameter : method.getParametersType()){
    			if(parameters.contains(parameter))
    				auxMethod++;
    		}
    		
    		auxTotal += auxMethod;
    	}
    	
    	if(parameters.size() == 0 || this.methods.size() == 0 )
    		return 0.0;
    	return (double)auxTotal / (parameters.size() * this.methods.size());
    }
    
    public int getMOA(){
    	int amountOfBusinessFields = 0;
    	for(Class field : this.getVariables()){
    		if(field.getTypeClass().equals(Class.TypeClass.interfaceType) || field.getTypeClass().equals(Class.TypeClass.business))
    			amountOfBusinessFields++;
    	}
    	
    	return amountOfBusinessFields;
    }
    
    private int getAmountAcessedMethods(){
    	Class ancestral = this.superClass;
    	int amountAncestralMethods = 0;
    	
    	if(ancestral != null)
    		amountAncestralMethods = ancestral.getPublicMethods().size() + ancestral.getProtectedMethods().size() + ancestral.getAmountAcessedMethods();
    	
    	return amountAncestralMethods;
    }
    
    public double getMFA(){	
    	return (double)this.getAmountAcessedMethods() / (this.getAmountAcessedMethods() + this.getPublicMethods().size() + this.getProtectedMethods().size()); 
    }
    
    public int getCIS(){
    	return this.getPublicMethods().size();
    }
    
    public int getNOP(){
    	Class ancestral = this.superClass;
    	int nop = 0;
    	
    	if(ancestral != null){
    		for(Method ancestralMethod : ancestral.getAllMethods()){
    			for(Method method : this.methods.values()){
    				if(ancestralMethod.getName().equals(method.getName()) && ancestralMethod.getParametersType().size() == method.getParametersType().size()){
    					boolean equal = true;
    					for(int i = 0 ; i < ancestralMethod.getParametersType().size(); i++ ){
    						if(!ancestralMethod.getParametersType().get(i).equals(method.getParametersType().get(i)) 
    								&& !method.getParametersType().get(i).getSuperClassess().contains(ancestralMethod.getParametersType().get(i))
    								&& !(ancestralMethod.getParametersType().get(i).getName().contains("$") && this.getParameterizeds().contains(method.getParametersType().get(i)))){
    							equal = false;
    							break;
    						}
    					}
    					
    	    			if(equal)
    	    				nop++;
    				}
    			}
    		}
    	}
    	return nop;
    }

}
