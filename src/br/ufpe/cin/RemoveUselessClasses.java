package br.ufpe.cin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class RemoveUselessClasses {

	public static void main(String[] args) {
		String csvFile = RemoveUselessClasses.class.getClassLoader().getResource(".").getFile() + "/useless_classes.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
			        // use comma as separator
				String[] columns = line.split(cvsSplitBy);

				//System.out.println(columns[0] + ";" + columns[1] + ";" + columns[2]);
				File file = new File(columns[2] + "/" + columns[1]);
				if(!file.delete())
					System.out.println("Erro ao excluir arquivo " + columns[2] + "/" + columns[1]);
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

		System.out.println("Done");
	}

}
