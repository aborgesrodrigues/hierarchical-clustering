package br.ufpe.cin;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

public class Properties {
	public static final String CAMINHO_ARQUIVO = Properties.class.getClassLoader().getResource(".").getFile() + "/config.properties";
	
	private java.util.Properties properties = null;
	private static Properties instance;

	private Properties(String pathFile) {
		this.properties = new java.util.Properties();
		try {
			properties.load(new FileInputStream(pathFile));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Properties getInstance() {
		if (instance == null) {
			instance = new Properties(CAMINHO_ARQUIVO);
		}
		return instance;
	}


	public boolean isShowEntities() {
		return properties.getProperty("ShowEntitiesInGraph") == "true";
	}
	
	public String getPathSourceCode() {
		return properties.getProperty("PathSourceCode");
	}
	
	public List<String> getPackagesToIgnore() {
		String value = properties.getProperty("IgnorePackages");
		return Arrays.asList(value.split(","));
	}
	
	public List<String> getClassesToIdentifyInGraph() {
		String value = properties.getProperty("IdentifyClassesInGraph");
		return Arrays.asList(value.split(","));
	}
	
	public String getPathToConnectionStrengthCSVFile() {
		return properties.getProperty("PathToConnectionStrengthCSVFile");
	}
	
	public String getPathToComplexityCSVFile() {
		return properties.getProperty("PathToComplexityCSVFile");
	}
	
	public String getPathToPersistenceMigrationFile() {
		return properties.getProperty("PathToPersistenceMigrationFile");
	}
	
	public String getPathToPersistenceMigrationJTranformerFinderFile() {
		return properties.getProperty("PathToPersistenceMigrationJTranformerFinderFile");
	}
	
	public String getPersistencePackage() {
		return properties.getProperty("PersistencePackage");
	}
	
	public String getBusinessPackage() {
		return properties.getProperty("BusinessPackage");
	}
	
	public String getEntityPackage() {
		return properties.getProperty("EntityPackage");
	}
	
	public String getPathToIsolatedClustersMigration() {
		return properties.getProperty("PathToIsolatedClustersMigration");
	}

}