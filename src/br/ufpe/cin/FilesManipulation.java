package br.ufpe.cin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FilesManipulation {
	private Map<String,String> filesContent;
	
	public FilesManipulation(String path){
		try {
			filesContent = new HashMap<String,String>();
			this.walk(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void walk( String path ) throws Exception {
        File root = new File( path );
        
        File[] list = root.listFiles();
        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
            	System.out.println( "Dir:" + f.getAbsoluteFile() );
                walk( f.getAbsolutePath() );
            }
            else {
            	String[] file =f.getAbsoluteFile().getName().split("\\."); 
            	//System.out.println( "File:" + f.getAbsoluteFile() );
            	if(file[file.length - 1].equals("java")){
	                System.out.println( "File:" + f.getAbsolutePath() );
	                filesContent.put(f.getAbsolutePath(), readFileToString(f.getAbsolutePath()));
            	}
                
                //this.setFilesContent(this.getFilesContent() + readFileToString(f.getAbsolutePath()));
                
                
            }
        }
    }
    
	//read file content into a string
	public String readFileToString(String filePath) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
 
		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
 
		reader.close();
 
		return  fileData.toString();	
	}

	public Map<String,String>  getFilesContent() {
		return filesContent;
	}

	public void setFilesContent(Map<String,String> filesContent) {
		this.filesContent = filesContent;
	}
}
