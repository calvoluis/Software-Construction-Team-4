import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

public class SimpleCFGParser {
	private String file;
	private CFG cfg;
	
	public SimpleCFGParser(String filename){
		this.file = filename;
		this.cfg = new CFG();
	}
	
	public void parse(){
		FileInputStream in;
		try {
			in = new FileInputStream(this.file);
			CompilationUnit cu = JavaParser.parse(in);
			in.close();
			BlockVisitor blockVisitor = new BlockVisitor();
//			Node result = (Node) blockVisitor.visit(cu, null);
			this.cfg = blockVisitor.returnCFG(cu, null);
//			System.out.println("----------Main:\n"+result.toStringWithoutComments()+"\n");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public CFG getCFG(){
		return this.cfg;
	}
	
	private static class BlockVisitor extends GenericVisitorAdapter<Object, Object> {
		private CFG cfg = new CFG();
		
	    @Override
	    public Object visit(BlockStmt n, Object arg) {
	    	List<Node> children = n.getChildrenNodes();
	    	n.toStringWithoutComments();
	    	//adds the current BlockStmt's children to the CFG if the children do not have children themselves
	    	if(children.size()>1){
	    		String basicBlock = "";
	    		int basicBlockBegin = -1;
	    		int basicBlockEnd = -1;
	    		for(int i=0; i<children.size(); i++){
	    			Node child = children.get(i);
	    			int begin = child.getBeginLine();
    	    		int end = child.getEndLine();
    	    		String code = child.toStringWithoutComments();
	    			if(child.getChildrenNodes().size()==1){
	    				if(basicBlockBegin==-1){
	    					basicBlockBegin = begin;
	    					basicBlockEnd = end;
	    					basicBlock += code+"\n";
	    				}
	    				else{
	    					basicBlockEnd = end;
	    					basicBlock += code+"\n";
	    					this.cfg.addNode(basicBlockBegin, basicBlockEnd, basicBlock);
	    					basicBlock = "";
	    		    		basicBlockBegin = -1;
	    		    		basicBlockEnd = -1;
	    				}
//	    	    		this.cfg.addNode(begin, end, code);
	    			}
	    		}
	    	}
	        return super.visit(n, arg);
	    }

		public CFG returnCFG(CompilationUnit cu, Object arg){
	    	visit(cu, arg);
	    	return this.cfg;
	    }
	}
}