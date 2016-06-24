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
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import br.ufpe.cin.Class;
import br.ufpe.cin.Method;
import br.ufpe.cin.Properties;
import br.ufpe.cin.Variable;

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
											classType = new Class();
											
											classType.setName(node.resolveBinding().getBinaryName());
											classType.setFilePath(unit.getPath().toOSString());
											
											/*if(config.getPersistencePackage().equals(cu.getPackage().getName().toString()))
												classType.setTypeClass(Class.TypeClass.persistence);
											else if(config.getBusinessPackage().equals(cu.getPackage().getName().toString()))
												classType.setTypeClass(Class.TypeClass.business);
											else if(config.getEntityPackage().equals(cu.getPackage().getName().toString()))
												classType.setTypeClass(Class.TypeClass.entity);*/
											
											System.out.println("class " + node.resolveBinding().getBinaryName() + " - ann " + node.resolveBinding().getAnnotations().length);
											for(IAnnotationBinding annotation : node.resolveBinding().getAnnotations()){
												if(annotation.getName().toString().equals("IdClass")){
													System.out.println("annotation " + " - " + annotation.getAnnotationType().getBinaryName() + getType(annotation.getAnnotationType()));
													for(IMemberValuePairBinding memberValuePair : annotation.getDeclaredMemberValuePairs()){
														classType.getAnnotations().add(((ITypeBinding)memberValuePair.getValue()).getBinaryName());
														System.out.println("value " + ((ITypeBinding)memberValuePair.getValue()).getBinaryName() + " - " + getType(memberValuePair.getValue()));
													}
												}
											}
											
											if(node.getSuperclassType() != null && node.getSuperclassType().resolveBinding() != null){
												classType.setExtendses(node.getSuperclassType().resolveBinding().getBinaryName());
												
												for(String type : getType(getType(node.getSuperclassType()))){
													if(!type.equals(classType.getExtendses())){
														Variable variable = classType.getVariables().get(type);
														
														if(variable == null){
										                	variable = new Variable();
										                	variable.setName(type);
										    				variable.setQuantidade(0);
										                }
														
														variable.setQuantidade(variable.getQuantidade() + 1);
														
														classType.getVariables().put(variable.getName(), variable);
													}
														
												}
												System.out.println("--extends " + node.getSuperclassType().resolveBinding().getBinaryName() + " - " + getType(node.getSuperclassType()));
											}
											
											for(Object object : node.superInterfaceTypes()){
												classType.getInterfaces().addAll(getType(object));
												System.out.println("--implements "  + getType(object));
											}
										}
										
										return true;
									}
									
									public void endVisit(TypeDeclaration node){
										
									}
									
											    			
								    public boolean visit(final MethodDeclaration node) {
							    		System.out.print("--method " + node.getName().getFullyQualifiedName() + " - " + node.typeParameters().size() + " - " + node.parameters().size());
							    		
							    		System.out.print(" return " + getType(node.getReturnType2()));
							    		for(Object parameter : node.parameters()){
							    			SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameter;
							    			System.out.print(" param " + variableDeclaration.resolveBinding() + " - " + getType(variableDeclaration.getType()));
							    		}
							    		
							    		System.out.println("");
								    	return true;
								    }
								    
								    public void endVisit(final MethodDeclaration node){
								    	methodType = null;
							    	}
								    
								    public boolean visit(MethodInvocation node)
								    {
								        System.out.print("----methodInvocation return " + (node.getExpression() != null ? node.getExpression().resolveTypeBinding().getBinaryName() : "void") + " " + getType(node.resolveTypeBinding()) + " " + node.resolveMethodBinding().getName());
								        
								        for(ITypeBinding parameter : node.resolveMethodBinding().getParameterTypes()){
							    			System.out.print(" param " + parameter.getBinaryName());
							    		}
								        
								        System.out.println("");
								        
								        return true;
								    }
								    
								    
									public boolean visit(VariableDeclarationFragment node) {
										System.out.println("VariableDeclarationFragment " + node.getName() + " - " + node.resolveBinding().getType().getBinaryName() + " - " + getType(node.resolveBinding()));
									
										Expression expression= node.getInitializer();

										if(expression instanceof QualifiedName){

										}
										else if(node.getParent() instanceof FieldDeclaration){

										}
										else if(node.getParent() instanceof VariableDeclarationStatement){

										}
										else{
											//System.out.println("4Declaration of '" + node.getName().getFullyQualifiedName() + " - " + node.getParent().getClass().toString());
										}
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
		return null;
	}
	
	private List<String> getType(Object object){
    	List<String> results = new ArrayList<String>();
    	String result = "";
    	if(object instanceof ArrayType){
    		//System.out.println("ArrayType");
    		for(Object objectAux : ((ArrayType)object).dimensions()){
    			results.addAll(getType(objectAux));
			}
		}
    	else if(object instanceof ParameterizedType){
			//System.out.println("ParameterizedType");
			for(Object objectAux : ((ParameterizedType)object).typeArguments()){
				results.addAll(getType(objectAux));
			}
    	}
		else if(object instanceof PrimitiveType){
			//System.out.println("PrimitiveType");
			result = ((PrimitiveType)object).toString();
		}
		else if(object instanceof QualifiedType){
			//System.out.println("QualifiedType");
			result = ((QualifiedType)object).resolveBinding().getBinaryName();
		}
		else if(object instanceof SimpleType){
			//System.out.println("SimpleType ");
			if(((SimpleType)object).resolveBinding() != null)
				result = ((SimpleType)object).resolveBinding().getBinaryName();
		}
		else if(object instanceof WildcardType){
			if(((WildcardType)object).resolveBinding() != null){
				//System.out.println("WildcardType");
				result = ((WildcardType)object).resolveBinding().getBinaryName();
			}
		}
    	
    	if(!results.contains(result) && !result.isEmpty())
    		results.add(result);
    	
    	return results;
    }
}
