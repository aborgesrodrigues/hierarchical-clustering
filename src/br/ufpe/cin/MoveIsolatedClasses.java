package br.ufpe.cin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MoveIsolatedClasses {
	private static Properties config = Properties.getInstance();
	
	public static void main() {
    	ClassParser classesGeneration = new ClassParser();
    	
    	//move isolated classes
    	MoveIsolatedClasses.move(classesGeneration.getClasses().values());
    }
	
	
	private static String removePartPath(String path){
		path = path.replace("/sigaept-edu-ejb/ejbModule", "");
		path = path.replace("/sigaept-edu-persistencia/src", "");
		path = path.replace("/sigaept-infra/ejbModule", "");
		
		return path;
	}

	public static void move(Collection<Class> classes){
		List<Class> isolatedClasses = load();
		
		for(Class classe : classes){
			if(isolatedClasses.contains(classe)){
				String folder = config.getPathToIsolatedClustersMigration() + "/" + classe.getName() + "/" + classe.getFilePath().replace(config.getPathSourceCode(), "").replace(classe.getReducedName() + ".java", "");
				folder = removePartPath(folder);
				System.out.println("MoveIsolatedClasses " + classe.getName());
				
				//create folder
				//new File(config.getPathToIsolatedClustersMigration() + classe.getName()).mkdir();
				//System.out.println("Create folder " + config.getPathToIsolatedClustersMigration() + classe.getName());
				
				//move business file
				new File(folder).mkdirs();
				File businessFile = new File(classe.getFilePath());
				businessFile.renameTo(new File(folder + classe.getReducedName() + ".java"));
				
				System.out.println("Business file origin " + classe.getFilePath());
				System.out.println("Move Business file to " + folder + classe.getReducedName() + ".java");
				
				for(Class parameterized : classe.getParameterizeds()){
					if(!parameterized.isIgnored() && parameterized.isInProject()){
						iterateClass(parameterized, classe, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class>());
					}
				}
				
				//move interface
				for(Class interfaceClass : classe.getInterfaces()){
					if(interfaceClass != null){
						folder = config.getPathToIsolatedClustersMigration() + "/" + classe.getName() + "/" + interfaceClass.getFilePath().replace(config.getPathSourceCode(), "").replace(interfaceClass.getReducedName() + ".java", "");
						folder = removePartPath(folder);
						new File(folder).mkdirs();
						
						File interfaceFile = new File(interfaceClass.getFilePath());
						interfaceFile.renameTo(new File(folder + interfaceClass.getReducedName() + ".java"));
						
						System.out.println("Interface file origin " + interfaceClass.getFilePath());
						System.out.println("Move Interface file to " + folder + interfaceClass.getReducedName() + ".java");
					}
				}
				
				//move interface
				if(classe.getSuperClass() != null && !classe.getSuperClass().isIgnored() && classe.getSuperClass().isInProject()){
					folder = config.getPathToIsolatedClustersMigration() + "/" + classe.getName() + "/" + classe.getSuperClass().getFilePath().replace(config.getPathSourceCode(), "").replace(classe.getSuperClass().getReducedName() + ".java", "");
					folder = removePartPath(folder);
					new File(folder).mkdirs();
					
					File inheritageFile = new File(classe.getSuperClass().getFilePath());
					inheritageFile.renameTo(new File(folder + classe.getSuperClass().getReducedName() + ".java"));
					
					System.out.println("Inheritage file origin " + classe.getSuperClass().getFilePath());
					System.out.println("Move Inheritage file to " + folder + classe.getSuperClass().getReducedName() + ".java");
				}
				
				//move persistences e copy entities
				for(Method method : classe.getMethods().values()){
					//System.out.println("Method " + classe.getName() + "." + method.getName());
					
					if(method.getReturnType() != null){
						if(!method.getReturnType().isIgnored() && method.getReturnType().isInProject())						
							iterateClass(method.getReturnType(), classe, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class>());
					}
					
					for(Method methodInvocation : method.getMethodsInvocation().values()){
						Class persistenceClass = methodInvocation.getClassType();
						if(persistenceClass.isInProject() && !persistenceClass.isIgnored()){
							if(persistenceClass.getTypeClass().equals(Class.TypeClass.entity))
								iterateClass(persistenceClass, classe, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class>());
							else{
								folder = config.getPathToIsolatedClustersMigration() + "/" + classe.getName() + "/" + persistenceClass.getFilePath().replace(config.getPathSourceCode(), "").replace(persistenceClass.getReducedName() + ".java", "");
								folder = removePartPath(folder);
								new File(folder).mkdirs();
								
								File persistenceFile = new File(persistenceClass.getFilePath());
								persistenceFile.renameTo(new File(folder + persistenceClass.getReducedName() + ".java"));
								
								System.out.println("PersistenceI file origin " + persistenceClass.getFilePath());
								System.out.println("Move PersistenceI file to " + folder + persistenceClass.getReducedName() + ".java");
							}
						}
						
					}
					
					for(Class instantiationClass : method.getInstantiationsType()){
						if(instantiationClass.isInProject() && !instantiationClass.isIgnored()){
							if(instantiationClass.getTypeClass().equals(Class.TypeClass.entity))
								iterateClass(instantiationClass, classe, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class>());
							else{
								folder = config.getPathToIsolatedClustersMigration() + "/" + classe.getName() + "/" + instantiationClass.getFilePath().replace(config.getPathSourceCode(), "").replace(instantiationClass.getReducedName() + ".java", "");
								folder = removePartPath(folder);
								new File(folder).mkdirs();
								
								File instantiationFile = new File(instantiationClass.getFilePath());
								instantiationFile.renameTo(new File(folder + instantiationClass.getReducedName() + ".java"));
								
								System.out.println("Instatiation file origin " + instantiationClass.getFilePath());
								System.out.println("Move isntantiationClass file to " + folder + instantiationClass.getReducedName() + ".java");
							}
						}
					}
					
					for(Class entityClass : method.getParametersType()){
						iterateClass(entityClass, classe, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class>());
						
						//System.out.println("Entity file origin " + entityClass.getFilePath());
						//System.out.println("Move Entity file to " + config.getPathToIsolatedClustersMigration() + classe.getName() + "/" + entityClass.getName() + ".java");
					}
					
					if(method.getReturnType() != null){
						iterateClass(method.getReturnType(), classe, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class>());
					}
				}
				
				for(Class variableClasse : classe.getVariables()){
					if(variableClasse != null && !variableClasse.isIgnored() && variableClasse.isInProject()){
						if(variableClasse.getTypeClass().equals(Class.TypeClass.entity))
							iterateClass(variableClasse, classe, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class>());
						else{
							folder = config.getPathToIsolatedClustersMigration() + "/" + classe.getName() + "/" + variableClasse.getFilePath().replace(config.getPathSourceCode(), "").replace(variableClasse.getReducedName() + ".java", "");
							folder = removePartPath(folder);
							new File(folder).mkdirs();
							
							File persistenceFile = new File(variableClasse.getFilePath());
							persistenceFile.renameTo(new File(folder + variableClasse.getReducedName() + ".java"));
							
							System.out.println("Variable file origin " + variableClasse.getFilePath() + variableClasse.getTypeClass().toString());
							System.out.println("Move Variable file to " + folder + variableClasse.getReducedName() + ".java");
						}
						
					}
				}
				
				System.out.println("");
				System.out.println("");
			}
		}
	}
	
	private static void iterateClass(Class classe, Class businessClass, String newPath, List<Class> calculated){
		if(classe.getParameterizeds().size() > 0){
			for(Class parametized : classe.getParameterizeds())
				iterateClass(parametized, businessClass, newPath, calculated);
			
			return;
			
		}
		else if(classe.isIgnored()){
			System.out.println("iterateClass Ignored class " + classe.getName());
			return;
		}
		else if(!classe.isInProject()){
			System.out.println("iterateClass class not in project " + classe.getName());
			return;
		}
		else if(calculated.contains(classe)){
			System.out.println("iterateClass contains class " + classe.getName());
			return;
		}
		if(classe.isInProject())
			calculated.add(classe);
		//System.out.println("class " + classe.getName());
		
		String folder = config.getPathToIsolatedClustersMigration() + "/" + businessClass.getName() + "/" + classe.getFilePath().replace(config.getPathSourceCode(), "").replace(classe.getReducedName() + ".java", "");
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
			//copy(file, config.getPathToIsolatedClustersMigration() + classe.getName() + "/");
			
			//System.out.println("Entity file origin " + classe.getSuperClass().getFilePath());
			//System.out.println("Move Entity file to " + config.getPathToIsolatedClustersMigration() + classe.getName() + "/" + classe.getSuperClass().getName() + ".java");
			
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
	
	private static List<Class> load(){
		String csvFile = config.getPathToIsolatedClassesCSVFile();// RemoveUselessClasses.class.getClassLoader().getResource(".").getFile() + "/isolated_classes.csv";
		List<Class> isolatedClasses = new ArrayList<Class>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		
		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
			        // use comma as separator
				String[] columns = line.split(cvsSplitBy);
				Class classe = new Class();
				classe.setName(config.getBusinessPackage() + "." + columns[0].replace(".java", ""));
				classe.setFilePath(columns[1] + "/" + columns[0]);
				
				isolatedClasses.add(classe);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return isolatedClasses;
	}
	
	/*private class IsolatedClass{
		private String className;
		private String path;
		
		public String getClassName() {
			return className;
		}
		public void setClassName(String className) {
			this.className = className;
		}
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		
		
	}*/
}
