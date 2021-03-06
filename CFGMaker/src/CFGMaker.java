import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.thoughtworks.xstream.XStream;

public class CFGMaker {

	public static void main(String[] args) {
		BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Please enter file name (full path):");
		String fileName = "";
		try {
			boolean fileFound = false;
			do{
				fileName = userInput.readLine();
				File sourceCode = new File(fileName);
				if(sourceCode.isFile()){
					System.out.println("File exists");
					fileFound = true;
				}
				else{
					System.out.println("File doesn't exist\nPlease enter file name (full path):");
				}
			} while(!fileFound);
			if(fileFound){
				SimpleCFGParser simpleParser = new SimpleCFGParser(fileName);
				simpleParser.parse();
				CFG simpleCFG = simpleParser.getCFG();
				simpleCFG.printNodes();
				simpleCFG.printEdges();
				saveCFG(simpleCFG, "simple");
				
				MultiCondCFGParser multi = new MultiCondCFGParser(fileName);
				multi.parse();
				CFG multiCFG = multi.getCFG();
				System.out.println("Printing Multiple Condition CFG");
				multiCFG.printNodes();
				multiCFG.printEdges();
				saveCFG(multiCFG, "multi");
				
				String dotFormat = simpleCFG.cfgToDotFormat();
				System.out.println(dotFormat);
		        VisualizeCFG.createDotGraph(dotFormat, "SimpleCFG");
		        
		        dotFormat = multiCFG.cfgToDotFormat();
				System.out.println(dotFormat);
		        VisualizeCFG.createDotGraph(dotFormat, "MultiCFG");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void saveCFG(CFG cfg, String filename){
//		BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
//		System.out.println("Please enter file name (full path with .xml extension):");
//		String fileName = "";
//		try{
//			fileName = userInput.readLine();
//		}
//		catch (IOException e){
//			e.printStackTrace();
//		}
		XStream xstream=new XStream();
		String xml = xstream.toXML(cfg);
		System.out.println(xml);
		try{
			FileWriter out = new FileWriter(filename+".xml");
			out.write(xml);
			out.flush();
			out.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

}
