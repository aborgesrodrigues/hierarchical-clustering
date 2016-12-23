package br.ufpe.cin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ComponentsCreator {
	private static Properties config = Properties.getInstance();
	
	public static void create(Class classType) {
		String folder = config.getPathToClustersMigration() + "/" + classType.getColor().toString() + "/" + classType.getFilePath().replace(config.getPathSourceCode(), "").replace(classType.getReducedName() + ".java", "");
		folder = removePartPath(folder);
		new File(folder).mkdirs();
		
		File file = new File(classType.getFilePath());
		copyFile(file, folder);
		
		System.out.println("Business file origin " + classType.getFilePath());
		System.out.println("Copy Business file to " + folder + classType.getReducedName() + ".java");
		
		for(Class parameterized : classType.getParameterizeds()){
			if(!parameterized.isIgnored() && parameterized.isInProject()){
				iterateClass(parameterized, classType, config.getPathToClustersMigration() + classType.getName() + "/", new ArrayList<Class>());
			}
		}
		
		//move interface
		for(Class interfaceClass : classType.getInterfaces()){
			if(interfaceClass != null){
				folder = config.getPathToClustersMigration() + "/" + classType.getColor().toString() + "/" + interfaceClass.getFilePath().replace(config.getPathSourceCode(), "").replace(interfaceClass.getReducedName() + ".java", "");
				folder = removePartPath(folder);
				new File(folder).mkdirs();
				
				file = new File(interfaceClass.getFilePath());
				copyFile(file, folder);
				
				System.out.println("Interface file origin " + interfaceClass.getFilePath());
				System.out.println("Copy Interface file to " + folder + interfaceClass.getReducedName() + ".java");
			}
		}
		
		//move superclass
		if(classType.getSuperClass() != null && !classType.getSuperClass().isIgnored() && classType.getSuperClass().isInProject()){
			folder = config.getPathToClustersMigration() + "/" + classType.getColor().toString() + "/" + classType.getSuperClass().getFilePath().replace(config.getPathSourceCode(), "").replace(classType.getSuperClass().getReducedName() + ".java", "");
			folder = removePartPath(folder);
			new File(folder).mkdirs();
			
			file = new File(classType.getSuperClass().getFilePath());
			copyFile(file, folder);
			
			System.out.println("Inheritage file origin " + classType.getSuperClass().getFilePath());
			System.out.println("Copy Inheritage file to " + folder + classType.getSuperClass().getReducedName() + ".java");
		}
		
		//move persistences e copy entities
		for(Method method : classType.getMethods().values()){
			//System.out.println("Method " + classe.getName() + "." + method.getName());
			
			if(method.getReturnType() != null){
				if(!method.getReturnType().isIgnored() && method.getReturnType().isInProject())						
					iterateClass(method.getReturnType(), classType, config.getPathToClustersMigration() + classType.getName() + "/", new ArrayList<Class>());
			}
			
			for(Method methodInvocation : method.getMethodsInvocation().values()){
				Class persistenceClass = methodInvocation.getClassType();
				if(persistenceClass.isInProject() && !persistenceClass.isIgnored()){
					if(persistenceClass.getTypeClass().equals(Class.TypeClass.entity))
						iterateClass(persistenceClass, classType, config.getPathToClustersMigration() + classType.getName() + "/", new ArrayList<Class>());
					else{
						folder = config.getPathToClustersMigration() + "/" + classType.getColor().toString() + "/" + persistenceClass.getFilePath().replace(config.getPathSourceCode(), "").replace(persistenceClass.getReducedName() + ".java", "");
						folder = removePartPath(folder);
						new File(folder).mkdirs();
						
						file = new File(persistenceClass.getFilePath());
						copyFile(file, folder);
						
						System.out.println("PersistenceI file origin " + persistenceClass.getFilePath());
						System.out.println("Move PersistenceI file to " + folder + persistenceClass.getReducedName() + ".java");
					}
				}
				
			}
			
			for(Class instantiationClass : method.getInstantiationsType()){
				if(instantiationClass.isInProject() && !instantiationClass.isIgnored()){
					if(instantiationClass.getTypeClass().equals(Class.TypeClass.entity))
						iterateClass(instantiationClass, classType, config.getPathToClustersMigration() + classType.getName() + "/", new ArrayList<Class>());
					else{
						folder = config.getPathToClustersMigration() + "/" + classType.getColor().toString() + "/" + instantiationClass.getFilePath().replace(config.getPathSourceCode(), "").replace(instantiationClass.getReducedName() + ".java", "");
						folder = removePartPath(folder);
						new File(folder).mkdirs();
						
						file = new File(instantiationClass.getFilePath());
						copyFile(file, folder);
						
						System.out.println("Instatiation file origin " + instantiationClass.getFilePath());
						System.out.println("Copy isntantiationClass file to " + folder + instantiationClass.getReducedName() + ".java");
					}
				}
			}
			
			for(Class entityClass : method.getParametersType()){
				iterateClass(entityClass, classType, config.getPathToClustersMigration() + classType.getName() + "/", new ArrayList<Class>());
				
				//System.out.println("Entity file origin " + entityClass.getFilePath());
				//System.out.println("Move Entity file to " + config.getPathToClustersMigration() + classe.getName() + "/" + entityClass.getName() + ".java");
			}
			
			if(method.getReturnType() != null){
				iterateClass(method.getReturnType(), classType, config.getPathToClustersMigration() + classType.getName() + "/", new ArrayList<Class>());
			}
		}
		
		for(Class variableClass : classType.getVariables()){
			if(variableClass != null && !variableClass.isIgnored() && variableClass.isInProject()){
				if(variableClass.getTypeClass().equals(Class.TypeClass.entity))
					iterateClass(variableClass, classType, config.getPathToClustersMigration() + classType.getName() + "/", new ArrayList<Class>());
				else{
					folder = config.getPathToClustersMigration() + "/" + classType.getColor().toString() + "/" + variableClass.getFilePath().replace(config.getPathSourceCode(), "").replace(variableClass.getReducedName() + ".java", "");
					folder = removePartPath(folder);
					new File(folder).mkdirs();
					
					file = new File(variableClass.getFilePath());
					copyFile(file, folder);
					
					System.out.println("Variable file origin " + variableClass.getFilePath() + variableClass.getTypeClass().toString());
					System.out.println("Copy Variable file to " + folder + variableClass.getReducedName() + ".java");
				}
				
			}
		}
		
		System.out.println("");
		System.out.println("");
    }
	
	
	private static String removePartPath(String path){
		path = path.replace("/sigaept-edu-ejb/ejbModule", "");
		path = path.replace("/sigaept-edu-persistencia/src", "");
		path = path.replace("/sigaept-infra/ejbModule", "");
		
		return path;
	}
	
	private static void iterateClass(Class classe, Class businessClass, String newPath, List<Class> calculated){
		if(classe.getParameterizeds().size() > 0){
			for(Class parametized : classe.getParameterizeds())
				iterateClass(parametized, businessClass, newPath, calculated);
			
			return;
			
		}
		else if(classe.isIgnored()){
			//System.out.println("iterateClass Ignored class " + classe.getName());
			return;
		}
		else if(!classe.isInProject()){
			//System.out.println("iterateClass class not in project " + classe.getName());
			return;
		}
		else if(calculated.contains(classe)){
			//System.out.println("iterateClass contains class " + classe.getName());
			return;
		}
		if(classe.isInProject())
			calculated.add(classe);
		//System.out.println("class " + classe.getName());
		
		String folder = config.getPathToClustersMigration() + "/" + businessClass.getColor().toString() + "/" + classe.getFilePath().replace(config.getPathSourceCode(), "").replace(classe.getReducedName() + ".java", "");
		folder = removePartPath(folder);
		new File(folder).mkdirs();
		
		File file = new File(classe.getFilePath());
		copyFile(file, folder);
		
		System.out.println("businessClass " + businessClass.getReducedName());
		System.out.println("Entity file origin " + classe.getFilePath());
		System.out.println("Copy Entity file to " + folder + classe.getReducedName() + ".java");
		
		for(Class annotationClass : classe.getAnnotations()){
			if(annotationClass != null){
				//calculated.add(interfaceClass);
				iterateClass(annotationClass, businessClass, newPath, calculated);
			}
		}
		
		for(Class interfaceClass : classe.getInterfaces()){
			if(interfaceClass != null){
				//calculated.add(interfaceClass);
				iterateClass(interfaceClass, businessClass, newPath, calculated);
			}
		}
		
		if(classe.getSuperClass() != null){
			//File file = new File(classe.getSuperClass());
			//copy(file, config.getPathToClustersMigration() + classe.getName() + "/");
			
			//System.out.println("Entity file origin " + classe.getSuperClass().getFilePath());
			//System.out.println("Move Entity file to " + config.getPathToClustersMigration() + classe.getName() + "/" + classe.getSuperClass().getName() + ".java");
			
			if(!classe.getSuperClass().equals(classe) && !calculated.contains(classe.getSuperClass())){
				//System.out.println("classe.getSuperClass() ");
				//calculated.add(classe.getSuperClass());
				iterateClass(classe.getSuperClass(), businessClass, newPath, calculated);
			}
		}
		
		for(Class classAux : classe.getVariables()){
			//System.out.println("variable " + classe.getName() + " - " + classAux.getName() + " - " + calculated.contains(classAux));
				
			if(!classAux.equals(classe) && !calculated.contains(classAux)){
				if(classAux.getParameterizeds().size() == 0){
					//System.out.println("not parametized " + classe.getName() + " - " + classAux.getName() + " - " + calculated.contains(classAux));
					//calculated.add(classAux);
					iterateClass(classAux, businessClass, newPath, calculated);
				}
				else{
					for(Class parametized : classAux.getParameterizeds()){
						//System.out.println("parametized " + classe.getName() + " - " + parametized.getName() + " - " + calculated.contains(parametized));
						if(!parametized.equals(classAux) && !calculated.contains(parametized)){
							//calculated.add(classAux);
							iterateClass(parametized, businessClass, newPath, calculated);
						}
					}
				}
			}
		}
		
		for(Method method : classe.getMethods().values()){
			if(method.getReturnType() != null){
				if(!method.getReturnType().equals(classe) && !calculated.contains(method.getReturnType())){
					//calculated.add(method.getReturnType());
					iterateClass(method.getReturnType(), businessClass, newPath, calculated);
				}
			}
			for(Class instantiationClass : method.getInstantiationsType()){
				if(instantiationClass != null){
					if(!instantiationClass.equals(classe) && !calculated.contains(instantiationClass)){
						//calculated.add(classAux);
						iterateClass(instantiationClass, businessClass, newPath, calculated);
					}
				}
			}
			for(Class parameter : method.getParametersType()){
				if(parameter != null){
					if(!parameter.equals(classe) && !calculated.contains(parameter)){
						//calculated.add(classAux);
						iterateClass(parameter, businessClass, newPath, calculated);
					}
				}
			}
		}
		
	}
	
	private static void copyFile(File file, String newPath){
		InputStream inStream = null;
		OutputStream outStream = null;
			
	    	try{
	    		
	    	    File afile =file;
	    	    File bfile =new File(newPath + file.getName());
	    	    
	    	    if(afile.exists() && !bfile.exists()){
		    		
		    	    inStream = new FileInputStream(afile);
		    	    outStream = new FileOutputStream(bfile);
		        	
		    	    byte[] buffer = new byte[1024];
		    		
		    	    int length;
		    	    //copy the file content in bytes 
		    	    while ((length = inStream.read(buffer)) > 0){
		    	  
		    	    	outStream.write(buffer, 0, length);
		    	 
		    	    }
		    	 
		    	    inStream.close();
		    	    outStream.close();
		    	    System.out.println("File is copied successful!");
		    	    
		    	    //delete the original file
		    	    //afile.delete();
	    	    }
	    	    else{
	    	    	System.out.println("Erro ao copiar " + file.getName() + " afile (" + afile.getAbsolutePath() + ") " + afile.exists() + " bfile (" + bfile.getAbsolutePath() + ") " + bfile.exists());
	    	    }
	    	    
	    	}catch(IOException e){
	    	    e.printStackTrace();
	    	}
	}
}
