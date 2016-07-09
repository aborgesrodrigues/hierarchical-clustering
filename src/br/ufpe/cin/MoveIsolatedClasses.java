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
				String folder = config.getPathToIsolatedClustersMigration() + "/" + classe.getName() + "/" + classe.getFilePath().replace(config.getPathSourceCode(), "").replace(classe.getName() + ".java", "");
				folder = removePartPath(folder);
				System.out.println("MoveIsolatedClasses " + classe.getName());
				
				//create folder
				//new File(config.getPathToIsolatedClustersMigration() + classe.getName()).mkdir();
				//System.out.println("Create folder " + config.getPathToIsolatedClustersMigration() + classe.getName());
				
				//move business file
				new File(folder).mkdirs();
				File businessFile = new File(classe.getFilePath());
				businessFile.renameTo(new File(folder + classe.getName() + ".java"));
				
				System.out.println("Business file origin " + classe.getFilePath());
				System.out.println("Move Business file to " + folder + classe.getName() + ".java");
				
				//move interface
				for(Class interfaceClass : classe.getInterfaces()){
					if(interfaceClass != null){
						folder = config.getPathToIsolatedClustersMigration() + "/" + classe.getName() + "/" + interfaceClass.getFilePath().replace(config.getPathSourceCode(), "").replace(interfaceClass.getName() + ".java", "");
						folder = removePartPath(folder);
						new File(folder).mkdirs();
						
						File interfaceFile = new File(interfaceClass.getFilePath());
						interfaceFile.renameTo(new File(folder + interfaceClass.getName() + ".java"));
						
						System.out.println("Interface file origin " + interfaceClass.getFilePath());
						System.out.println("Move Interface file to " + folder + interfaceClass.getName() + ".java");
					}
				}
				
				//move interface
				if(classe.getInheritage() != null && !classe.getInheritage().isIgnored()){
					folder = config.getPathToIsolatedClustersMigration() + "/" + classe.getName() + "/" + classe.getInheritage().getFilePath().replace(config.getPathSourceCode(), "").replace(classe.getInheritage().getName() + ".java", "");
					folder = removePartPath(folder);
					new File(folder).mkdirs();
					
					File inheritageFile = new File(classe.getInheritage().getFilePath());
					inheritageFile.renameTo(new File(folder + classe.getInheritage().getName() + ".java"));
					
					System.out.println("Inheritage file origin " + classe.getInheritage().getFilePath());
					System.out.println("Move Inheritage file to " + folder + classe.getInheritage().getName() + ".java");
				}
				
				//move persistences e copy entities
				for(Method method : classe.getMethods().values()){
					for(Class persistenceClass : method.getInstantiationsType()){
						if(method.getReturnType() != null)
							iterateClass(method.getReturnType(), classe, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class>());
						
						if(persistenceClass != null && !persistenceClass.isIgnored()){
							if(persistenceClass.getTypeClass() != null && persistenceClass.getTypeClass().equals(Class.TypeClass.entity))
								iterateClass(persistenceClass, classe, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class>());
							else{
								folder = config.getPathToIsolatedClustersMigration() + "/" + classe.getName() + "/" + persistenceClass.getFilePath().replace(config.getPathSourceCode(), "").replace(persistenceClass.getName() + ".java", "");
								folder = removePartPath(folder);
								new File(folder).mkdirs();
								
								File persistenceFile = new File(persistenceClass.getFilePath());
								persistenceFile.renameTo(new File(folder + persistenceClass.getName() + ".java"));
								
								System.out.println("Persistence file origin " + persistenceClass.getFilePath());
								System.out.println("Move Persistence file to " + folder + persistenceClass.getName() + ".java");
							}
						}
					}
					
					for(Class entityClass : method.getParametersType()){
						if(entityClass != null){
							//System.out.println("entityClass " + entityClass.getNome());
							//File entityFile = new File(entityClass.getFilePath());
							//copy(persistenceFile, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class());
							iterateClass(entityClass, classe, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class>());
							
							//System.out.println("Entity file origin " + entityClass.getFilePath());
							//System.out.println("Move Entity file to " + config.getPathToIsolatedClustersMigration() + classe.getName() + "/" + entityClass.getName() + ".java");
						}
					}
					
					for(Class variableClasse : classe.getVariables()){
						if(variableClasse != null && !variableClasse.isIgnored()){
							if(variableClasse.getTypeClass() != null && variableClasse.getTypeClass().equals(Class.TypeClass.entity))
								iterateClass(variableClasse, classe, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class>());
							else{
								folder = config.getPathToIsolatedClustersMigration() + "/" + classe.getName() + "/" + variableClasse.getFilePath().replace(config.getPathSourceCode(), "").replace(variableClasse.getName() + ".java", "");
								folder = removePartPath(folder);
								new File(folder).mkdirs();
								
								File persistenceFile = new File(variableClasse.getFilePath());
								persistenceFile.renameTo(new File(folder + variableClasse.getName() + ".java"));
								
								System.out.println("Variable file origin " + variableClasse.getFilePath() + (variableClasse.getTypeClass() != null ? variableClasse.getTypeClass().toString() : "null"));
								System.out.println("Move Variable file to " + folder + variableClasse.getName() + ".java");
							}
							
						}
					}
					
					if(method.getReturnType() != null){
						//File returnFile = new File(method.getReturnType().getFilePath());
						//copy(returnFile, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class());
						
						//System.out.println("Return file origin " + method.getReturnType().getFilePath());
						//System.out.println("Move Return file to " + config.getPathToIsolatedClustersMigration() + classe.getName() + "/" + method.getReturnType().getName() + ".java");
						//System.out.println("classe " + classe.getNome());
						//System.out.println("getReturnType " + method.getReturnType().getNome());
						iterateClass(method.getReturnType(), classe, config.getPathToIsolatedClustersMigration() + classe.getName() + "/", new ArrayList<Class>());
					}
				}
				
				System.out.println("");
				System.out.println("");
			}
		}
	}
	
	private static void iterateClass(Class classe, Class businessClass, String newPath, List<Class> calculated){
		if(classe.isIgnored()){
			System.out.println("Ignored class " + classe.getName());
			return;
		}
		//System.out.println("class " + classe.getName());
		
		String folder = config.getPathToIsolatedClustersMigration() + "/" + businessClass.getName() + "/" + classe.getFilePath().replace(config.getPathSourceCode(), "").replace(classe.getName() + ".java", "");
		folder = removePartPath(folder);
		new File(folder).mkdirs();
		
		File file = new File(classe.getFilePath());
		copyFile(file, folder);
		
		System.out.println("businessClass " + businessClass.getName());
		System.out.println("Entity file origin " + classe.getFilePath());
		System.out.println("Copy Entity file to " + folder + classe.getName() + ".java");
		
		for(Class interfaceClass : classe.getInterfaces()){
			if(interfaceClass != null){
				calculated.add(interfaceClass);
				iterateClass(interfaceClass, businessClass, newPath, calculated);
			}
		}
		
		if(classe.getInheritage() != null){
			//File file = new File(classe.getInheritage());
			//copy(file, config.getPathToIsolatedClustersMigration() + classe.getName() + "/");
			
			//System.out.println("Entity file origin " + classe.getInheritage().getFilePath());
			//System.out.println("Move Entity file to " + config.getPathToIsolatedClustersMigration() + classe.getName() + "/" + classe.getInheritage().getName() + ".java");
			
			if(!classe.getInheritage().equals(classe) && !calculated.contains(classe.getInheritage())){
				//System.out.println("classe.getInheritage() ");
				calculated.add(classe.getInheritage());
				iterateClass(classe.getInheritage(), businessClass, newPath, calculated);
			}
		}
		
		for(Class classAux : classe.getVariables()){
			if(classAux != null){
				/*folder = config.getPathToIsolatedClustersMigration() + "/" + businessClass.getName() + "/" + classAux.getFilePath().replace(config.getPathSourceCode(), "").replace(classAux.getName() + ".java", "");
				folder = removePartPath(folder);
				new File(folder).mkdirs();
				
				File fileAux = new File(classAux.getFilePath());
				copyFile(fileAux, folder);*/
				//System.out.println("classe.getVariablesClass() " + classe.getVariablesClass().values().size());
				
				//System.out.println("Entity file origin " + classAux.getFilePath());
				//System.out.println("Move Entity file to " + folder + classAux.getName() + ".java");
				
				if(!classAux.equals(classe) && !calculated.contains(classAux)){
					calculated.add(classAux);
					iterateClass(classAux, businessClass, newPath, calculated);
				}
			}
		}
		
		for(Method method : classe.getMethods().values()){
			if(method.getReturnType() != null){
				if(!method.getReturnType().equals(classe) && !calculated.contains(method.getReturnType())){
					calculated.add(method.getReturnType());
					iterateClass(method.getReturnType(), businessClass, newPath, calculated);
				}
			}
			for(Class classAux : method.getInstantiationsType()){
				if(classAux != null){
					if(!classAux.equals(classe) && !calculated.contains(classAux)){
						calculated.add(classAux);
						iterateClass(classAux, businessClass, newPath, calculated);
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
