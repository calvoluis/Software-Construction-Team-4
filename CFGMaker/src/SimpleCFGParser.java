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
			Node result = (Node) blockVisitor.visit(cu, null);
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
	        // here you can access the attributes of the method.
	        // this method will be called for all methods in this 
	        // CompilationUnit, including inner class methods
//	    	Node tempNode = returnSimpleStatement(n);
//	    	System.out.println("-------------Begin: "+tempNode.getBeginLine()+"\n"+tempNode.toStringWithoutComments()+"\n-------------End: "+tempNode.getEndLine());
	    	List<Node> children = n.getChildrenNodes();
	    	if(children.size()>1){
	    		for(int i=0; i<children.size(); i++){
	    			Node child = children.get(i);
//	    			System.out.println("Looking at a child:\n-Begin:"+child.getBeginLine()+"\n"+child.toStringWithoutComments()+"\n-End:"+child.getEndLine());
	    			
	    		}
	    	}
	    	else{
//	    		System.out.println("Looking at a node:\n-Begin:"+n.getBeginLine()+"\n"+n.toStringWithoutComments()+"\n-End:"+n.getEndLine());
	    		this.cfg.addNode(new CFGNode(n.toStringWithoutComments(), n.getBeginLine()));
	    	}
	        return super.visit(n, arg);
//	    	return tempNode;
	    }

		public CFG returnCFG(CompilationUnit cu, Object arg){
	    	visit(cu, arg);
	    	return this.cfg;
	    }
	    
//	    private Node returnSimpleStatement(Node n){
//	    	List<Node> children = n.getChildrenNodes();
//	    	if(children.size()>1){
//	    		for(int i=0; i<children.size(); i++){
//	    			Node child = children.get(i);
//	    			return returnSimpleStatement(child);
//	    		}
//	    	}
//	    	return n;
//	    }
	}
}