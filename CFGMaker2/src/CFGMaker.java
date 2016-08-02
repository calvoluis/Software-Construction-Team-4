import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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
				CFG cfg = simpleParser.getCFG();
				cfg.printNodes();
				cfg.printEdges();
				
//				String dotFormat="1->2;1->3;1->4;4->5;4->6;6->7;5->7;3->8;3->6;8->7;2->8;2->5;";
				String dotFormat = cfg.cfgToDotFormat();
//				System.out.println(dotFormat);
		        VisualizeCFG.createDotGraph(dotFormat, "DotGraph");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
