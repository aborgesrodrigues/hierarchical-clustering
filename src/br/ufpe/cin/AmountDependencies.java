package br.ufpe.cin;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AmountDependencies{
	private Properties config = Properties.getInstance();
	private Map<String, Integer> businessDependencies = new HashMap<String, Integer>();
	private Map<String, Integer> persistenceDependencies = new HashMap<String, Integer>();

    public void main() {
    	FileWriter fileWriter = null;
    	ClassParser classParser = new ClassParser();
    	
    	try {
    		//int amountDependenciesBusiness = 0;
    		//int amountDependenciesPersistence = 0;
    		fileWriter = new FileWriter(config.getPathToAmountDependenciesCSVFile());
    		
    		fileWriter.append("Business Class;Amount Business Dependencies; Amount Persistence Dependencies\n");
    		
	    	for(Class classType : classParser.getClasses().values()){
	    		this.businessDependencies = new HashMap<String, Integer>();
	    		this.persistenceDependencies = new HashMap<String, Integer>();
	    		if(classType.getTypeClass().equals(Class.TypeClass.business)){
	    			for(Class variable : classType.getVariables())
	    				addDependenciesCount(classType, variable);
	    			
	    			for(Method method : classType.getMethods().values()){
	    				for(Class instantiation : method.getInstantiationsType())
	    					addDependenciesCount(classType, instantiation);
	    				
	    				for(Method methodInvocation : method.getMethodsInvocation().values())
	    					addDependenciesCount(classType, methodInvocation.getClassType());
	    			}
	    			
	    			fileWriter.append(classType.getName() + ";" + businessDependencies.keySet().size() + ";" + persistenceDependencies.keySet().size()  + "\n");
	    			System.out.println("---" + classType.getName());
	    			for(String classAux : businessDependencies.keySet())
	    				System.out.println("------" + classAux);
	    			for(String classAux : persistenceDependencies.keySet())
	    				System.out.println("------" + classAux);
	    			
	    			System.out.println("");
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
    
    private void addDependenciesCount(Class classOrigin, Class classDestiny){
    	if(!classOrigin.equals(classDestiny)){
			if(classDestiny.getTypeClass().equals(Class.TypeClass.business) || classDestiny.getTypeClass().equals(Class.TypeClass.interfaceType)){
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
			}
    	}
    }
}
