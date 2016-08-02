import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.thoughtworks.xstream.XStream;

public class XMLLoader {
	public static void main(String[] args){
		BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Please enter file name (full path) for the XML:");
		String fileName = "";
		try {
			boolean fileFound = false;
			do{
				fileName = userInput.readLine();
				File file = new File(fileName);
				if(file.isFile()){
					System.out.println("File exists");
					fileFound = true;
				}
				else{
					System.out.println("File doesn't exist\nPlease enter file name (full path):");
				}
			} while(!fileFound);
		} catch (IOException e){
			e.printStackTrace();
		}
		File file = new File(fileName);
		XStream xstream = new XStream();
		CFG cfg =(CFG)xstream.fromXML(file);
		cfg.printNodes();	
		cfg.printEdges();
	}
}