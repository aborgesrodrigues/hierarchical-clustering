package br.ufpe.cin;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WildcardType;

public class ClassParser {
	private Map<String, Class> classes;
	private Class classType;
	private Method methodType;
	private Properties config = Properties.getInstance();
	
	@SuppressWarnings("unchecked")
	public ClassParser(){
		classes = new HashMap<String, Class>();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    IWorkspaceRoot root = workspace.getRoot();
	    // Get all projects in the workspace
	    IProject[] projects = root.getProjects();
	    // Loop over all projects
		
	    for (IProject project : projects) {
	    	try {
				//
				
				//parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
				
				
		    	if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
		    		IJavaProject javaProject = JavaCore.create(project);
		    		
				    IPackageFragment[] packages = javaProject.getPackageFragments();
				    for (IPackageFragment mypackage : packages) {
						// Package fragments include all packages in the
						// classpath
						// We will only look at the package from the source
						// folder
						// K_BINARY would include also included JARS, e.g.
						// rt.jar
					    if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
					        System.out.println("Package " + mypackage.getElementName());
						    for (final ICompilationUnit unit : mypackage.getCompilationUnits()) {
								ASTParser parser = ASTParser.newParser(AST.JLS8);
								parser.setKind(ASTParser.K_COMPILATION_UNIT);
								parser.setBindingsRecovery(true);
								parser.setResolveBindings(true);
								parser.setStatementsRecovery(true);
								//parser.setProject(javaProject);
								
								Map<String, String> options = JavaCore.getOptions();
								parser.setCompilerOptions(options);
						    	parser.setUnitName(unit.getPath().toOSString());
						    	parser.setSource(unit);
						    	
						    	final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
						    	
						    	cu.accept(new ASTVisitor() {
									 									
									public boolean visit(TypeDeclaration node){
										if(!node.isMemberTypeDeclaration() && !node.isLocalTypeDeclaration()){
											classType = getClassType(node.resolveBinding().getBinaryName());
											
											classType.setFilePath(config.getPathSourceCode() + "/" + unit.getPath().toOSString());
											classType.setInProject(true);
											classType.setPackageInfo(cu.getPackage().getName().toString());
											
											if(config.getPersistencePackage().equals(cu.getPackage().getName().toString()))
												classType.setTypeClass(Class.TypeClass.persistence);
											else if(config.getBusinessPackage().equals(cu.getPackage().getName().toString()))
												classType.setTypeClass(Class.TypeClass.business);
											else if(config.getEntityPackage().equals(cu.getPackage().getName().toString()))
												classType.setTypeClass(Class.TypeClass.entity);
											else
												classType.setTypeClass(Class.TypeClass.other);
											
											classType.setIgnored(false);
											classType.setInterface(node.isInterface());
											for(String ignore : config.getPackagesToIgnore()){
												if(cu.getPackage().getName().toString().contains(ignore)){
													classType.setIgnored(true);
													break;
												}
											}
											
											//System.out.println("class " + node.resolveBinding().getBinaryName() + " - " + node.resolveBinding().getAnnotations().length + " - " + classType.isInProject());
											for(IAnnotationBinding annotation : node.resolveBinding().getAnnotations()){
												if(annotation.getName().toString().equals("IdClass")){
													//System.out.println("annotation " + " - " + annotation.getAnnotationType().getBinaryName() + getType(annotation.getAnnotationType()));
													for(IMemberValuePairBinding memberValuePair : annotation.getDeclaredMemberValuePairs()){
														classType.getAnnotations().add(getClassType(((ITypeBinding)memberValuePair.getValue()).getBinaryName()));
														//System.out.println("value " + ((ITypeBinding)memberValuePair.getValue()).getBinaryName() + " - " + getType(memberValuePair.getValue()));
													}
												}
											}
											
											if(!node.isInterface()){
												if(node.getSuperclassType() != null && node.getSuperclassType().resolveBinding() != null){
													//Class inheritage = getClassType(node.getSuperclassType().resolveBinding().getBinaryName(), superClassTypes);
									    			Class inheritage = getClassType(node.getSuperclassType().resolveBinding().getBinaryName());
													
													classType.setInheritage(inheritage);
													
													List<String> superClassTypes = getType(node.getSuperclassType());
													
													if(superClassTypes.contains(node.getSuperclassType().resolveBinding().getBinaryName()))
									    				superClassTypes.remove(node.getSuperclassType().resolveBinding().getBinaryName());
													
													for(String paremetized : superClassTypes)
														classType.getParameterizeds().add(getClassType(paremetized));
													
													//System.out.println("--extends " + node.getSuperclassType().resolveBinding().getBinaryName() + " - " + getType(node.getSuperclassType()) + " - " + getType(node.getSuperclassType().resolveBinding()));
												}
												
												for(Object object : node.superInterfaceTypes()){
													for(String interfaceType : getType(object)){
														Class interfaceClass = getClassType(interfaceType);
														
														if(!interfaceClass.getImplementClass().contains(classType))
															interfaceClass.getImplementClass().add(classType);
														classType.getInterfaces().add(interfaceClass);
													}
													
													//System.out.println("--implements "  + getType(object));
												}
											}
											else{
												if(node.superInterfaceTypes().size() > 0){
													Type type = (Type)node.superInterfaceTypes().get(0);
													
													//classType.setInheritage(getClassType(type.resolveBinding().getBinaryName(), getType(type)));
													classType.setInheritage(getClassType(type.resolveBinding().getBinaryName()));
												}
											}
											
											//System.out.println("classModel " + classType.getFullName());
										}
										
										return true;
									}
									
									public void endVisit(TypeDeclaration node){
										
									}
									
											    			
								    public boolean visit(final MethodDeclaration node) {
								    	List<Class> parameters = new ArrayList<Class>();
								    	for(Object parameter : node.parameters()){
							    			SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameter;
							    			
							    			List<String> parametersTypes = getType(variableDeclaration.getType());
							    			
							    			//System.out.println(" param1 " + variableDeclaration.resolveBinding().getType().getBinaryName() + " - " + parametersTypes.size() + " - " + parametersTypes);
							    			if(parametersTypes.contains(variableDeclaration.resolveBinding().getType().getBinaryName()))
							    				parametersTypes.remove(variableDeclaration.resolveBinding().getType().getBinaryName());
							    			
							    			//System.out.println(" param2 " + variableDeclaration.resolveBinding().getType().getBinaryName() + " - " + parametersTypes.size() + " - " + parametersTypes);
							    			
							    			Class parameterType = getClassType(variableDeclaration.resolveBinding().getType().getBinaryName(), parametersTypes);
							    			parameterType.setPrimitiveType(variableDeclaration.getType().isPrimitiveType());
							    			
							    			parameters.add(parameterType);
							    		}
								    	
								    	methodType = classType.getMethod(node.getName().toString(), parameters);
								    	methodType.setName(node.getName().getFullyQualifiedName());
								    	methodType.setClassType(classType);
								    	if(!node.isConstructor()){
								    		
							    			List<String> types = getType(node.getReturnType2());
							    			
							    			//System.out.println(" param1 " + variableDeclaration.resolveBinding().getType().getBinaryName() + " - " + parametersTypes.size() + " - " + parametersTypes);
							    			if(types.contains(node.getReturnType2().resolveBinding().getBinaryName()))
							    				types.remove(node.getReturnType2().resolveBinding().getBinaryName());
								    		//System.out.println("returntype " + node.getReturnType2().resolveBinding().getBinaryName() + " - " + getType(node.getReturnType2()));
								    		
								    		Class returnType = getClassType(node.getReturnType2().resolveBinding().getBinaryName(), types);
								    		returnType.setPrimitiveType(node.getReturnType2().resolveBinding().isPrimitive());
								    		
								    		methodType.setReturnType(returnType);
								    	}
								    	else
								    		methodType.setReturnType(null);								    	
							    		
							    		/*System.out.print(" return " + (!node.isConstructor() ? node.getReturnType2().resolveBinding().getBinaryName() + "*" + getType(node.getReturnType2()) : "null"));
							    		System.out.print(node.getName().getFullyQualifiedName());
							    		
							    		for(Object parameter : node.parameters()){
							    			SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameter;
							    			System.out.print(" param " + variableDeclaration.resolveBinding().getName() + " - " + getType(variableDeclaration.getType()));
							    		}
							    		System.out.println("");*/
							    		
							    		classType.getMethods().put(methodType.getFullName(), methodType);
							    		
							    		//System.out.println("--methodModel " + methodType.getFullName());
								    	return true;
								    }
								    
								    public void endVisit(final MethodDeclaration node){
								    	methodType = null;
							    	}
								    
								    public boolean visit(MethodInvocation node)
								    {
								    	if(methodType == null)
								    		return false;
								    	
								    	Class classInvocation = null;
								    	if(node.getExpression() != null)
								    		classInvocation = getClassType(node.getExpression().resolveTypeBinding().getBinaryName());
								    	else
								    		classInvocation = classType;
								    	
								    	List<Class> parameters = new ArrayList<Class>();
								    	
								    	for(ITypeBinding parameter : node.resolveMethodBinding().getParameterTypes()){
								        	//String parameterAux = typeFromBinding(node.getAST(), parameter).toString().replace(parameter.getBinaryName(), "").replace("<", "").replace(">", "");
								    		
							    			List<String> parametersTypes = getType(typeFromBinding(node.getAST(), parameter));
							    			if(parametersTypes.contains(parameter.getBinaryName()))
							    				parametersTypes.remove(parameter.getBinaryName());
							    			
							    			Class classParameter = getClassType(parameter.getBinaryName(), parametersTypes); 
							    			classParameter.setPrimitiveType(parameter.isPrimitive());
							    			
								        	parameters.add(classParameter);
							    		}
								        
								        Method methodInvocation = classInvocation.getMethod(node.resolveMethodBinding().getName(), parameters);
										        
								        /*System.out.print("----methodInvocation return " + (node.getExpression() != null ? node.getExpression().resolveTypeBinding().getBinaryName() : "void") + " " + getType(node.resolveTypeBinding()) + " " + node.resolveMethodBinding().getName() + " - " + node.resolveMethodBinding().getTypeArguments().length + node.resolveMethodBinding().getTypeParameters().length);
								        
								        for(ITypeBinding parameter : node.resolveMethodBinding().getParameterTypes()){
								        	//String parameterAux = typeFromBinding(node.getAST(), parameter).toString().replace(parameter.getBinaryName(), "").replace("<", "").replace(">", "");
								        	Type type = typeFromBinding(node.getAST(), parameter);
								        	//System.out.print(" paramInvocation " + parameter.getBinaryName() + " - " + " - paramInvocationType(" +  getType(type) + ")");
							    		}
								        
								        System.out.println("");*/
								        
								        //System.out.println("----methodInvocationModel " + methodInvocation.getFullName() + " - " + (methodType == null ? "null" : "not null"));
								        //System.out.println("----methodInvocation return " + (node.getExpression() != null ? node.getExpression().resolveTypeBinding().getBinaryName() : "void") + " " + getType(node.resolveTypeBinding()) + " " + node.resolveMethodBinding().getName() + " - " + node.resolveMethodBinding().getTypeArguments().length + node.resolveMethodBinding().getTypeParameters().length);
								        
							        	methodType.getMethodsInvocation().put(methodInvocation.getFullName(), methodInvocation);
							        	
								        return true;
								    }
								    
								    
									public boolean visit(VariableDeclarationFragment node) {
										List<String> types = getType(typeFromBinding(node.getAST(), node.resolveBinding().getType()));
										
										if(types.contains(node.resolveBinding().getType().getBinaryName()))
											types.remove(node.resolveBinding().getType().getBinaryName());
										
										//System.out.println("VariableDeclarationFragment " + classType.getName() + " - " + node.resolveBinding().getType().getBinaryName() + " - " + types + " - " + node.getName() + " - ");
										
										Class variable = getClassType(node.resolveBinding().getType().getBinaryName(), types);
										variable.setPrimitiveType(node.resolveBinding().getType().isPrimitive());
										
										if(methodType != null)
											methodType.getInstantiationsType().add(variable);
										else
											classType.getVariables().add(variable);
										
										//System.out.println("VariableDeclarationFragmentModel " + variable.getFullName() + " - " + node.resolveBinding().getType().getBinaryName() + " - " + types);
										return true;
									}
									
									public boolean visit(EnumDeclaration node){
						    			if(!node.isMemberTypeDeclaration() && !node.isLocalTypeDeclaration()){
					    					classType = getClassType(node.resolveBinding().getBinaryName());
											
											classType.setFilePath(config.getPathSourceCode() + "/" + unit.getPath().toOSString());
											classType.setInProject(true);
											classType.setPackageInfo(cu.getPackage().getName().toString());
											
											classType.setIgnored(false);
						    			}
						    			
						    			return true;
						    		}
						    		
						    		public boolean visit(InstanceofExpression node){
						    			
						    			List<String> types = getType(node.getRightOperand());
										
										if(types.contains(node.getRightOperand().resolveBinding().getBinaryName()))
											types.remove(node.getRightOperand().resolveBinding().getBinaryName());
										
										//System.out.println("VariableDeclarationFragment " + classType.getName() + " - " + node.resolveBinding().getType().getBinaryName() + " - " + types + " - " + node.getName() + " - ");
										
										Class variable = getClassType(node.getRightOperand().resolveBinding().getBinaryName(), types);
										variable.setPrimitiveType(node.getRightOperand().isPrimitiveType());
										
										if(methodType != null)
											methodType.getInstantiationsType().add(variable);
										else
											classType.getVariables().add(variable);
										
										//System.out.println("VariableDeclarationFragmentModel " + variable.getFullName() + " - " + node.resolveBinding().getType().getBinaryName() + " - " + types);
										
						    			//System.out.println("InstanceofExpression " + node.toString() + " - " + node.getRightOperand().resolveBinding().getBinaryName() + " - " + node.getRightOperand() + " - " + getType(node.getRightOperand()) + " = " + (classType != null ? classType.getName() : "null") + " - " + (methodType != null ? methodType.getName() : "null"));
						    			
						    			return true;
						    		}
						    
								});
						    }
					    }
				    }
		    	}
		    
	    	} catch (CoreException e) {
		        e.printStackTrace();
	    	}
	    }
	    
	    //printClasses();
	    
	    System.out.println("Término carregamento classess");
		System.out.println("Início Cálculo Complexidade");
		calculateComplexities();
		System.out.println("Término Cálculo Complexidade");
		System.out.println("Início Cálculo força conectividade");
		calculateConnetvitiesStrength();
		System.out.println("Término Cálculo força conectividade");
		System.out.println("Início Verificação migração de persistência");
		checkPersistenceMigration();
		System.out.println("Término Verificação migração de persistência");
	}
	
	public Map<String, Class> getClasses() {
		return classes;
	}

	public void setClasses(Map<String, Class> classes) {
		this.classes = classes;
	}

	private List<String> getType(Object object){
    	List<String> results = new ArrayList<String>();
    	String result = "";
    	if(object instanceof ArrayType){
    		//System.out.println("ArrayType " + object);
    		//all index has the same type so I need only 1 type 
    		for(Object objectAux : ((ArrayType)object).dimensions()){
    			results.addAll(getType(objectAux));
    			break;
			}
		}
    	else if(object instanceof ParameterizedType){
			//System.out.println("ParameterizedType " + object + " - " + ((ParameterizedType)object).typeArguments().size());
			for(Object objectAux : ((ParameterizedType)object).typeArguments()){
				results.addAll(getType(objectAux));
			}
    	}
		else if(object instanceof PrimitiveType){
			//System.out.println("PrimitiveType " + object);
			result = ((PrimitiveType)object).toString();
		}
		else if(object instanceof QualifiedType){
			//System.out.println("QualifiedType " + object);
			result = ((QualifiedType)object).resolveBinding().getBinaryName();
		}
		else if(object instanceof SimpleType){
			//System.out.println("SimpleType " + object + " - " + ((SimpleType)object).resolveBinding());
			if(((SimpleType)object).resolveBinding() != null)
				result = ((SimpleType)object).resolveBinding().getBinaryName();
			else if(object.toString().indexOf(".") > 0)
				result = object.toString();
		}
		else if(object instanceof WildcardType){
			if(((WildcardType)object).resolveBinding() != null){
				//System.out.println("WildcardType");
				result = ((WildcardType)object).resolveBinding().getBinaryName();
			}
		}
		/*else
			System.out.println("nenhum " + object);*/
    	
    	if(!results.contains(result) && !result.isEmpty())
    		results.add(result);
    	
    	return results;
    }
	
	public Class getClassType(String name){
		/*Class classType = classes.get(name);
		
		if(classType == null){
			classType = new Class();
			classType.setName(name);
			classes.put(name, classType);
		}*/
		
		return getClassType(name, new ArrayList<String>());
	}
	
	public Class getClassType(String name, List<String> parameterizedTypes){
		String fullNameClass = "";
		for(String type : parameterizedTypes){
			if(!type.equals(name)){
				if(!fullNameClass.isEmpty())
					fullNameClass += ",";
				
				fullNameClass += type;
			}
		}
		
		if(!fullNameClass.isEmpty())
			fullNameClass = name + ";" + fullNameClass;
		else
			fullNameClass = name;
		
		Class classType = classes.get(fullNameClass);
		
		if(classType == null){
			classType = new Class();
			classType.setName(name);
			classType.setTypeClass(Class.TypeClass.outside);
			
			for(String type : parameterizedTypes)
				classType.getParameterizeds().add(getClassType(type));
			
			//System.out.println("getClassType " + fullNameClass + " - " + classType.getName() + " - " + classType.isInProject());
			classes.put(fullNameClass, classType);
		}
		
		return classType;
	}
	
	public static Type typeFromBinding(AST ast, ITypeBinding typeBinding) {
	    if( typeBinding == null )
	        throw new NullPointerException("typeBinding is null");

	    try{
	    	if( typeBinding.isPrimitive() ) {
		        return ast.newPrimitiveType(
		            PrimitiveType.toCode(typeBinding.getName()));
		    }

		    if( typeBinding.isCapture() ) {
		        ITypeBinding wildCard = typeBinding.getWildcard();
		        WildcardType capType = ast.newWildcardType();
		        ITypeBinding bound = wildCard.getBound();
		        if( bound != null ) {
		            capType.setBound(typeFromBinding(ast, bound),
		                wildCard.isUpperbound());
		        }
		        return capType;
		    }

		    if( typeBinding.isArray() ) {
		        Type elType = typeFromBinding(ast, typeBinding.getElementType());
		        return ast.newArrayType(elType, typeBinding.getDimensions());
		    }

		    if( typeBinding.isParameterizedType() ) {
		        ParameterizedType type = ast.newParameterizedType(
		            typeFromBinding(ast, typeBinding.getErasure()));

		        @SuppressWarnings("unchecked")
		        List<Type> newTypeArgs = type.typeArguments();
		        for( ITypeBinding typeArg : typeBinding.getTypeArguments() ) {
		            newTypeArgs.add(typeFromBinding(ast, typeArg));
		        }

		        return type;
		    }

		    // simple or raw type
		    String qualName = typeBinding.getQualifiedName();
		    if( "".equals(qualName) ) {
		        throw new IllegalArgumentException("No name for type binding.");
		    }
		    return ast.newSimpleType(ast.newName(qualName));
	    }
	    catch(Exception ex){
	    	return null;
	    }
	    
	}
	
	private void printClasses(){
		for(Class classAux : classes.values()){
	    	if(classAux.isInProject()){
	    		System.out.println("Class " + classAux.getFullName() + " Variables " + classAux.getVariables().size() + " methods " + classAux.getMethods().size() + " primitive " + classAux.isPrimitiveType());
	    		
	    		for(Class variable : classAux.getVariables()){
	    			System.out.println("**Variable " + variable.getFullName());
	    		}
	    		
	    		for(Method methodAux : classAux.getMethods().values()){
	    			System.out.println("--MethodDeclaration " + methodAux.getFullName() + " - parameters " + methodAux.getParametersType().size() + " - instantiations " + methodAux.getInstantiationsType().size());
	    			
	    			for(Class parameter : methodAux.getParametersType()){
	    				System.out.println("****Parameter " + parameter.getFullName() + " parametizeds " + parameter.getParameterizeds().size());
	    			}
	    			
	    			for(Class variable : methodAux.getInstantiationsType()){
		    			System.out.println("****Variable " + variable.getFullName() + " primitive " + classAux.isPrimitiveType());
		    		}
	    			
	    			for(Method methodInvocation : methodAux.getMethodsInvocation().values()){
	    				System.out.println("----MethodInvocation " + methodInvocation.getClassType().getName() + " . " + methodInvocation.getFullName());
	    			}
	    		}
	    	}
	    	else
	    		System.out.println("class not in project " + classAux.getName());
	    }
	}
	
    private void calculateComplexities(){
    	FileWriter fileWriter = null;
    	try {
    		fileWriter = new FileWriter(config.getPathToComplexityCSVFile());
    		
    		fileWriter.append("Class;Complexity Value\n");

        	for(Class classType : classes.values()){
        		if(classType.isInProject()){
        			classType.getComlexity().setValor(calculateComplexity(classType, new ArrayList<Class>()));
        			//System.out.println("Complexity;" + classType.getName() + ";" + classType.getComlexity().getValor());
        			
        			fileWriter.append(classType.getName() + ";" + classType.getComlexity().getValor() + "\n");
        		}
        	}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
            try {
	            fileWriter.flush();
	            fileWriter.close();
	        } catch (IOException e) {
	            System.out.println("Error while flushing/closing fileWriter !!!");
	            e.printStackTrace();
	        }
		}

    }
    
    private double calculateComplexity(Class classe, List<Class> calculated){
    	double primitiveValue = 0.0;
    	double abstractValue = 0.0;
    	
    	//System.out.println("calculo " + classe.getFullName() + " - " + " variables - " + classe.getVariables().size() + " complexity " + classe.getComlexity().getValor());
    	
    	if(calculated.contains(classe))//case of having cyclic relation
    		return classe.getComlexity().getValor();
    	
    	if(classe.getComlexity().getValor() != 0)
    		return classe.getComlexity().getValor();
    	
    	/*if(classe.getParameterizeds().size() > 0){   		
    		for(Class parameterized : classe.getParameterizeds()){
        		calculated.add(classe);
        		abstractValue += this.calculateComplexity(parameterized, calculated);
    		}
    	}*/
    	
    	if(classe.getInheritage() != null){
    		calculated.add(classe);
    		abstractValue += this.calculateComplexity(classe.getInheritage(), calculated);
    	}
    	
		for(Class annotation : classe.getAnnotations()){
			//System.out.println("calculoannotation " + " - " + classe.getName() + " - " + annotation.getName() + " - primitive " + annotation.isPrimitiveType());
			if(annotation.isPrimitiveType()){
				//dependes of the types not the amount of use of them
				primitiveValue++;
			}
			else if(!annotation.equals(classe)){//se classe tiver atributo com seu tipo não considerar
				calculated.add(classe);
				abstractValue += this.calculateComplexity(annotation, calculated);
			}
		}
    	
		for(Class variable : classe.getVariables()){
			//System.out.println("calculoclass " + " - " + classe.getName() + " - " + variable.getName() + " - primitive " + variable.isPrimitiveType());
			if(variable.isPrimitiveType() || !variable.isInProject()){
				//dependes of the types not the amount of use of them
				primitiveValue++;
			}
			else if(!variable.equals(classe)){//se classe tiver atributo com seu tipo não considerar
				calculated.add(classe);
				abstractValue += this.calculateComplexity(variable, calculated);
			}
		}
		
		//System.out.println("calculo " + classe.getFullName() + " - " + primitiveValue + " - " + abstractValue + " variables - " + classe.getVariables().size());
		
		classe.getComlexity().setValor((primitiveValue * Metric.wpri) + (abstractValue * Metric.wabs));

		return classe.getComlexity().getValor();
    }
    
    private void calculateConnetvitiesStrength(){
    	FileWriter fileWriter = null;
    	try {
    		fileWriter = new FileWriter(config.getPathToConnectionStrengthCSVFile());
    		
    		fileWriter.append("Class origin; Class destiny;Connection Strength Value\n");
    		for(Class classType : classes.values()){
        		if(!classType.getTypeClass().equals(Class.TypeClass.other) && !classType.getTypeClass().equals(Class.TypeClass.entity)){
        			
        			if(classType.getParameterizeds().size() > 0){
        				for(Class parameterized : classType.getParameterizeds()){
        					if(!parameterized.getTypeClass().equals(Class.TypeClass.other) && !parameterized.getTypeClass().equals(Class.TypeClass.entity)){
        						//System.out.println("keeptogether " + classType.getName() + " - " + parameterized.getName());
        						Metric metric = classType.getConnectivityStrength().get(parameterized);
        						if(metric == null){
            						metric = new Metric(Metric.Type.connectionStrength);
            						metric.setValor(0);
        						}
        						
        						metric.setKeepTogether(true);
        						
            					classType.getConnectivityStrength().put(parameterized, metric);
           						parameterized.getConnectivityStrength().put(classType, metric);
        					}
        				}
        			}
        			
        			for(Method method : classType.getMethods().values()){
        				for(Method methodInvocation : method.getMethodsInvocation().values()){
        			    	if(methodInvocation.getClassType().isInProject() && !methodInvocation.getClassType().equals(classType)){
        				    	//System.out.println(methodInvocation.getClassName() + " - " + methodInvocation.getNome() + " qtdmi =" + methodInvocation.getParametrosClasse().size());
        						if (methodInvocation.getClassType() != null && !methodInvocation.getClassType().equals(Class.TypeClass.entity)){
        							mountConnectivityStrength(classType, methodInvocation);
        						}
        			    	}
        				}
        			}
        		}
        	}
    		for(Class classType : classes.values()){
    			if(!classType.getTypeClass().equals(Class.TypeClass.other) && !classType.getTypeClass().equals(Class.TypeClass.entity)){
        			for(Map.Entry<Class, Metric> entry : classType.getConnectivityStrength().entrySet()){
        				fileWriter.append(classType.getName() + ";" + entry.getKey().getName() + ";" + entry.getValue().getValor() + "\n");
        			}
        		}
    		}

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
            try {
	            fileWriter.flush();
	            fileWriter.close();
	        } catch (IOException e) {
	            System.out.println("Error while flushing/closing fileWriter !!!");
	            e.printStackTrace();
	        }
		}
    }
    
    private void mountConnectivityStrength(Class classType, Method method){
    	//if(method.getClassType().isInProject() && !method.getClassType().equals(classType)){
	    	//System.out.println(methodInvocation.getClassName() + " - " + methodInvocation.getNome() + " qtdmi =" + methodInvocation.getParametrosClasse().size());
			//if (method.getClassType() != null && !method.getClassType().equals(Class.TypeClass.entity)){
				if(method.getParametersType().size() > 0){
					for(Class parameter : method.getParametersType()){
						double wcs = 0;
						if(parameter.isPrimitiveType() || !parameter.isInProject())
							wcs = Metric.wpri;
						else
							wcs = parameter.getComlexity().getValor();
						
						Metric metric = classType.getConnectivityStrength().get(method.getClassType());
						
						if(metric == null)
							metric = new Metric(Metric.Type.connectionStrength);
						
						metric.setValor(metric.getValor() + wcs);
						
						//The connection strengh is equal for both sides of a method call
						classType.getConnectivityStrength().put(method.getClassType(), metric);
						if(method.getClassType() != null)
							method.getClassType().getConnectivityStrength().put(classType, metric);
					
					}
				}
				else{
					Metric metric = classType.getConnectivityStrength().get(method.getClassType());
					//Class variable = classType.getVariable(method.getClassType().getNomeQualificado());
					
					if(metric == null)
						metric = new Metric(Metric.Type.connectionStrength);
					
					metric.setValor(metric.getValor());
					
					/*if(variable != null)
						metric.setKeepTogether(variable.isKeepTogether());*/
					
					//The connection strengh is equal for both sides of a method call
					classType.getConnectivityStrength().put(method.getClassType(), metric);
					if(method.getClassType() != null)
						method.getClassType().getConnectivityStrength().put(classType, metric);
				}
			//}
    	//}
    }
    
	private void checkPersistenceMigration(){
		String lineSeparation = System.getProperty( "line.separator" );
		String jDownloaderFinderMethod = 	"analysis_api:analysis_result('persistence_call', _, Result) :-  " + lineSeparation +
											"persistence_call(CallId, MethodCall, MethodCalled, MethodCalledName, MethodCalledParameters, MethodCalledReturnType, MethodCalledExceptions, MethodCalledTypeParameters, CallParameters, DAO, Business, NotBusiness, 'ClassDAO', 'ClassBusiness', 'org.sigaept.nucleo.dao.GenericDAO') ," + lineSeparation + 
											"Description = 'Call to DAO'," + lineSeparation + 
											"make_result_term(persistence_call(CallId, MethodCall, MethodCalled, MethodCalledName, MethodCalledParameters, MethodCalledReturnType, MethodCalledExceptions, MethodCalledTypeParameters, CallParameters, DAO, Business, NotBusiness), Description, Result)." + lineSeparation + lineSeparation;
		
		/* jDownloaderReplacerMethod = 	"transformation_api:transformation( " + lineSeparation +
											"_,                                        % Individual result (No group) " + lineSeparation +
											"persistence_call(CallId),                 % RoleTerm " + lineSeparation +
											"[addEJBAnnotation(CallId, 'org.sigaept.edu.dao.UnidadeOrganizacionalDAO', 'org.sigaept.edu.negocio.ejb.ManterDiarioClasseEJB', 'org.sigaept.nucleo.dao.GenericDAO'), replaceDAOCallforBusinessCall(CallId, 'ClassDAO', 'ClassBusiness', 'org.sigaept.nucleo.dao.GenericDAO')],         % CTHead " + lineSeparation +
											"'Replace DAO call for EJB call',      % Description " + lineSeparation +
											"[global, preview]).                               % Option: Show Preview" + lineSeparation + lineSeparation;
		*/
    	FileWriter fileWriter = null;
    	FileWriter fileWriterJDownloaderFinder = null;
    	//FileWriter fileWriterJDownloaderReplacer = null;
    	try {
    		fileWriter = new FileWriter(config.getPathToPersistenceMigrationFile());
    		fileWriterJDownloaderFinder = new FileWriter(config.getPathToPersistenceMigrationJTranformerFinderFile());
    		//fileWriterJDownloaderReplacer = new FileWriter(config.getPathToPersistenceMigrationJTranformerReplacerFile());
    		
    		fileWriter.append("Persistence Class;Destination Class;Connectivity Strength\n");
			int contDifferents = 0;
			int contEquals = 0;
			
    		for(Class classType : classes.values()){
    			//System.out.println("checkPersistenceMigration " + classe.getNome() + " - " + classe.getTypeClass() + " - " + (classe.getTypeClass() != null ? classe.getTypeClass().toString() : "null"));
    			if(classType.getTypeClass().equals(Class.TypeClass.persistence)){
    				double biggestStrength = 0;
    				Class biggestClass = null;
    				Class keepTogether = null;
    				double keepTogetherStrength = 0;
    				
    				for(Map.Entry<Class, Metric> entry : classType.getConnectivityStrength().entrySet()){
    					if(entry.getKey().getTypeClass().equals(Class.TypeClass.business)){
    						/*if(entry.getValue().isKeepTogether()){
    							biggestClass = entry.getKey();
    							biggestStrength = entry.getValue().getValor();
    							break;
    						}*/
    						if(entry.getValue().getValor() >= biggestStrength){
	    						biggestStrength = entry.getValue().getValor();
	    						biggestClass = entry.getKey();
    						}
    						if(entry.getValue().isKeepTogether()){
    							keepTogether = entry.getKey();
    							keepTogetherStrength = entry.getValue().getValor();
    						}
    					}
    				}
    				
    				if(biggestClass != null && keepTogether != null && !biggestClass.equals(keepTogether) && keepTogether.getMethods().size() > 0){
    					//System.out.println("different " + classType.getName() + " - " + biggestClass.getName() + " - " + keepTogether.getName());
    					contDifferents++;
    				}
    				else if(biggestClass != null && keepTogether != null){
    					//System.out.println("equal " + classType.getName() + " - " + biggestClass.getName() + " - " + keepTogether.getName());
    					contEquals++;
    				}
    				
    				Class belongsTo = null;
    				double belongsToStrength = 0;
    				if(config.keepParametizedClassesTogether() && keepTogether != null && keepTogether.getMethods().size() > 0){
    					belongsTo = keepTogether;
    					belongsToStrength = keepTogetherStrength;
    				}
    				else{
    					belongsTo = biggestClass;
    					belongsToStrength = biggestStrength;
    				}
    				
    				if(belongsTo != null){
    					classType.setBelongsTo(belongsTo);
    					fileWriter.append(classType.getName() + ";" + belongsTo.getName() + ";" + belongsToStrength + "\n");
    					
    					fileWriterJDownloaderFinder.append(jDownloaderFinderMethod.replace("ClassDAO", classType.getName()).replace("ClassBusiness", belongsTo.getName()));
    				}
    			}
    		}
    		//System.out.println("total different " + contDifferents + " - total equals " + contEquals);

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
            try {
	            fileWriter.flush();
	            fileWriter.close();
	            
	            fileWriterJDownloaderFinder.flush();
	            fileWriterJDownloaderFinder.close();
	            
	            //fileWriterJDownloaderReplacer.flush();
	            //ileWriterJDownloaderReplacer.close();
	        } catch (IOException e) {
	            System.out.println("Error while flushing/closing fileWriter !!!");
	            e.printStackTrace();
	        }
		}

	}
}
