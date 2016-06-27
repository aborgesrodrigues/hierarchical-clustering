package hierarchicalclustering.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
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

import br.ufpe.cin.Class;
import br.ufpe.cin.Method;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class HierarchicalClusteringHandler extends AbstractHandler {
	private Map<String, Class> classes;
	private Class classType;
	private Method methodType;
	//private Properties config = Properties.getInstance();
	/**
	 * The constructor.
	 */
	public HierarchicalClusteringHandler() {
		classes = new HashMap<String, Class>();
	}
	
	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException {
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
											
											//classType.setName(node.resolveBinding().getBinaryName());
											classType.setFilePath(unit.getPath().toOSString());
											classType.setInProject(true);
											
											/*if(config.getPersistencePackage().equals(cu.getPackage().getName().toString()))
												classType.setTypeClass(Class.TypeClass.persistence);
											else if(config.getBusinessPackage().equals(cu.getPackage().getName().toString()))
												classType.setTypeClass(Class.TypeClass.business);
											else if(config.getEntityPackage().equals(cu.getPackage().getName().toString()))
												classType.setTypeClass(Class.TypeClass.entity);*/
											
											//System.out.println("class " + node.resolveBinding().getBinaryName() + " - ann " + node.resolveBinding().getAnnotations().length);
											for(IAnnotationBinding annotation : node.resolveBinding().getAnnotations()){
												if(annotation.getName().toString().equals("IdClass")){
													//System.out.println("annotation " + " - " + annotation.getAnnotationType().getBinaryName() + getType(annotation.getAnnotationType()));
													for(IMemberValuePairBinding memberValuePair : annotation.getDeclaredMemberValuePairs()){
														classType.getAnnotations().add(getClassType(((ITypeBinding)memberValuePair.getValue()).getBinaryName()));
														//System.out.println("value " + ((ITypeBinding)memberValuePair.getValue()).getBinaryName() + " - " + getType(memberValuePair.getValue()));
													}
												}
											}
											
											if(node.getSuperclassType() != null && node.getSuperclassType().resolveBinding() != null){											
												Class inheritage = getClassType(node.getSuperclassType().resolveBinding().getBinaryName(), getType(node.getSuperclassType()));
												
												classType.setInheritage(inheritage);
												
												//System.out.println("--extends " + node.getSuperclassType().resolveBinding().getBinaryName() + " - " + getType(node.getSuperclassType()));
											}
											
											for(Object object : node.superInterfaceTypes()){
												for(String interfaceType : getType(object))
													classType.getInterfaces().add(getClassType(interfaceType));
												
												//System.out.println("--implements "  + getType(object));
											}
										}
										
										return true;
									}
									
									public void endVisit(TypeDeclaration node){
										
									}
									
											    			
								    public boolean visit(final MethodDeclaration node) {
								    	List<Class> parameters = new ArrayList<Class>();
								    	for(Object parameter : node.parameters()){
							    			SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameter;
							    			//System.out.print(" param " + variableDeclaration.resolveBinding().toString() + " - " + getType(variableDeclaration.getType()));
							    			
							    			parameters.add(getClassType(variableDeclaration.resolveBinding().toString(), getType(variableDeclaration.getType())));
							    		}
								    	
								    	methodType = classType.getMethod(node.getName().getFullyQualifiedName(), parameters);
								    	methodType.setName(node.getName().getFullyQualifiedName());
								    	methodType.setClassType(classType);
								    	if(!node.isConstructor())
								    		methodType.setReturnType(getClassType(node.getReturnType2().resolveBinding().getBinaryName(), getType(node.getReturnType2())));
								    	else
								    		methodType.setReturnType(null);
								    	
								    	methodType.getParametersType().addAll(parameters);
								    	
							    		//System.out.print("--method " + methodType.getFullName() + " - " + node.getName().getFullyQualifiedName() + " - " + node.typeParameters().size() + " - " + node.parameters().size());
							    		
							    		//System.out.print(" return " + (!node.isConstructor() ? node.getReturnType2().resolveBinding().getBinaryName() + "*" + getType(node.getReturnType2()) : "null"));
							    		
							    		for(Object parameter : node.parameters()){
							    			SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameter;
							    			//System.out.print(" param " + variableDeclaration.resolveBinding().toString() + " - " + getType(variableDeclaration.getType()));
							    		}
							    		
							    		classType.getMethods().put(methodType.getFullName(), methodType);
							    		
							    		//System.out.println("");
								    	return true;
								    }
								    
								    public void endVisit(final MethodDeclaration node){
								    	methodType = null;
							    	}
								    
								    public boolean visit(MethodInvocation node)
								    {
								    	Class classInvocation = null;
								    	if(node.getExpression() != null)
								    		classInvocation = getClassType(node.getExpression().resolveTypeBinding().getBinaryName());
								    	else
								    		classInvocation = classType;
								    	
								    	List<Class> parameters = new ArrayList<Class>();
								    	
								    	for(ITypeBinding parameter : node.resolveMethodBinding().getParameterTypes()){
								        	//String parameterAux = typeFromBinding(node.getAST(), parameter).toString().replace(parameter.getBinaryName(), "").replace("<", "").replace(">", "");
								        	parameters.add(getClassType(parameter.getBinaryName(), getType(typeFromBinding(node.getAST(), parameter))));
							    		}
								    	
								        //System.out.print("----methodInvocation return " + (node.getExpression() != null ? node.getExpression().resolveTypeBinding().getBinaryName() : "void") + " " + getType(node.resolveTypeBinding()) + " " + node.resolveMethodBinding().getName() + " - " + node.resolveMethodBinding().getTypeArguments().length + node.resolveMethodBinding().getTypeParameters().length);
								        
								        Method method = classInvocation.getMethods().get(Method.getFullName(node.resolveMethodBinding().getName(), parameters));
								        
								        for(ITypeBinding parameter : node.resolveMethodBinding().getParameterTypes()){
								        	//String parameterAux = typeFromBinding(node.getAST(), parameter).toString().replace(parameter.getBinaryName(), "").replace("<", "").replace(">", "");
								        	Type type = typeFromBinding(node.getAST(), parameter);
								        	//System.out.print(" paramInvocation " + parameter.getBinaryName() + " - " + " - paramInvocationType(" +  getType(type) + ")");
							    		}
								        
								        /*for(Object parameter : node.typeArguments()){
							    			SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameter;
							    			System.out.print(" paramInvocation " + variableDeclaration.resolveBinding().toString() + " - " + getType(variableDeclaration.getType()));
							    		}*/
								        
								        //System.out.println("");
								        
								        return true;
								    }
								    
								    
									public boolean visit(VariableDeclarationFragment node) {
										//System.out.println("VariableDeclarationFragment " + " - " + node.resolveBinding().getType().getBinaryName() + " - " + typeFromBinding(node.getAST(), node.resolveBinding().getType())  + " - " + getType(typeFromBinding(node.getAST(), node.resolveBinding().getType())) + " - " + node.getName() + " - ");
										
										if(methodType != null)
											methodType.getInstantiationsType().add(getClassType(node.resolveBinding().getType().getBinaryName(), getType(typeFromBinding(node.getAST(), node.resolveBinding().getType()))));
										else
											classType.getVariables().add(getClassType(node.resolveBinding().getType().getBinaryName(), getType(typeFromBinding(node.getAST(), node.resolveBinding().getType()))));
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
	    
	    
	    for(Class classAux : classes.values()){
	    	if(classAux.isInProject() || classAux.getParameterizeds().size() > 0){
	    		System.out.println("Classe " + classAux.getFullName());
	    		
	    		for(Class variable : classAux.getVariables()){
	    			System.out.println("**Variable " + variable.getFullName());
	    		}
	    		
	    		for(Method methodAux : classAux.getMethods().values()){
	    			System.out.println("--MethodDeclaration " + methodAux.getFullName());
	    			for(Class variable : methodAux.getInstantiationsType()){
		    			System.out.println("****Variable " + variable.getFullName());
		    		}
	    			
	    			for(Method methodInvocation : methodAux.getMethodsInvocation().values()){
	    				System.out.println("--MethodInvocation " + methodInvocation.getFullName());
	    			}
	    		}
	    	}
	    }
		return null;
	}
	
	private List<String> getType(Object object){
    	List<String> results = new ArrayList<String>();
    	String result = "";
    	if(object instanceof ArrayType){
    		//System.out.println("ArrayType " + object);
    		for(Object objectAux : ((ArrayType)object).dimensions()){
    			results.addAll(getType(objectAux));
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
			if(!fullNameClass.isEmpty())
				fullNameClass += ",";
			
			fullNameClass += type;
		}
		
		if(!fullNameClass.isEmpty())
			fullNameClass = name + ";" + fullNameClass;
		else
			fullNameClass = name;
		
		Class classType = classes.get(fullNameClass);
		
		if(classType == null){
			classType = new Class();
			classType.setName(name);
			
			for(String type : parameterizedTypes)
				classType.getParameterizeds().add(getClassType(type));
			
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
}
