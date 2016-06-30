package br.ufpe.cin;

import java.util.Arrays;
import java.util.List;

public class Properties {
	private static Properties instance;

	public static Properties getInstance() {
		if (instance == null) {
			instance = new Properties();
		}
		return instance;
	}


	public boolean isShowEntities() {
		return System.getenv("ShowEntitiesInGraph") == "true";
	}
	
	public String getPathSourceCode() {
		return System.getenv("PathSourceCode");
	}
	
	public List<String> getPackagesToIgnore() {
		String value = System.getenv("IgnorePackages");
		return Arrays.asList(value.split(","));
	}
	
	public List<String> getClassesToIdentifyInGraph() {
		String value = System.getenv("IdentifyClassesInGraph");
		return Arrays.asList(value.split(","));
	}
	
	public String getPathToConnectionStrengthCSVFile() {
		return System.getenv("PathToConnectionStrengthCSVFile");
	}
	
	public String getPathToComplexityCSVFile() {
		return System.getenv("PathToComplexityCSVFile");
	}
	
	public String getPathToPersistenceMigrationFile() {
		return System.getenv("PathToPersistenceMigrationFile");
	}
	
	public String getPathToPersistenceMigrationJTranformerFinderFile() {
		return System.getenv("PathToPersistenceMigrationJTranformerFinderFile");
	}
	
	public String getPersistencePackage() {
		return System.getenv("PersistencePackage");
	}
	
	public String getBusinessPackage() {
		return System.getenv("BusinessPackage");
	}
	
	public String getEntityPackage() {
		return System.getenv("EntityPackage");
	}
	
	public String getPathToIsolatedClustersMigration() {
		return System.getenv("PathToIsolatedClustersMigration");
	}

}