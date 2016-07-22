import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
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
			this.cfg = blockVisitor.returnCFG(cu, null);
//			StatementVisitor statementVisitor = new StatementVisitor();
//			statementVisitor.returnCFG(cu, null);
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
	    	System.out.println("Looking at: "+n.getBeginLine()+"     Children: "+children.size()+"\n"+n.toString()+"\n");
	    	//adds the current BlockStmt's children to the CFG if the children do not have children themselves
	    	int begin = -1;
	    	int end = -1;
	    	String nodeId = "";
	    	String code = "";
	    	for(int i=0; i<children.size(); i++){
	    		Node child = children.get(i);
	    		int childBegin = child.getBeginLine();
	    		int childEnd = child.getEndLine();
	    		String childId = Integer.toString(childBegin);
	    		String childCode = child.toStringWithoutComments();
	    		
	    		System.out.println("Child begin: "+child.getBeginLine()+" Class: "+child.getClass().getSimpleName());
	    		
	    		if(child instanceof ExpressionStmt){
		    		if(begin == -1){
		    			begin = childBegin;
		    		}
		    		end = childEnd;
		    		nodeId += childId+" ";
		    		code += childCode+"\n";
	    		}
	    		else{
	    			if(code!=""){
		    			addNode(begin, end, nodeId, code);
	    			}
	    			handleConditionals(child);
	    			begin = -1;
	    			end = -1;
	    			code = "";
	    		}
	    	}
	        return super.visit(n, arg);
	    }
	    
	    private void handleConditionals(Node child){
	    	int childBegin = child.getBeginLine();
    		int childEnd = child.getEndLine();
    		String childId = Integer.toString(childBegin);
    		
	    	if(child instanceof IfStmt){
    			String condition = ((IfStmt) child).getCondition().toStringWithoutComments();
				addNode(childBegin, childEnd, childId, "if("+condition+")");
    		}
    		else if(child instanceof DoStmt){
    			String condition = ((DoStmt) child).getCondition().toStringWithoutComments();
    			addNode(childBegin, childEnd, childId, "do{...}while("+condition+");");
    		}
    		else if(child instanceof ForStmt){
    			String condition = ((ForStmt) child).getCompare().toStringWithoutComments();
    			addNode(childBegin, childEnd, childId, "for("+condition+")");
    		}
    		else if(child instanceof ForeachStmt){
    			String condition = ((ForeachStmt) child).getIterable().toStringWithoutComments();
    			addNode(childBegin, childEnd, childId, "foreach("+condition+")");
    		}
    		else if(child instanceof WhileStmt){
    			String condition = ((WhileStmt) child).getCondition().toStringWithoutComments();
    			addNode(childBegin, childEnd, childId, "foreach("+condition+")");
    		}
    		else if(child instanceof SwitchStmt){
    			String condition = ((SwitchStmt) child).getSelector().toStringWithoutComments();
    			addNode(childBegin, childEnd, childId, "switch("+condition+")");
    		}
    		else if(child instanceof SwitchEntryStmt){
    			System.out.println("-------------------------SWITCH ENTRY STATEMENT FOUND");
    			String condition = ((SwitchEntryStmt) child).getLabel().toStringWithoutComments();
    			addNode(childBegin, childEnd, childId, "case "+condition+":");
    		}
    		else if(child instanceof TryStmt){
    			addNode(childBegin, childEnd, childId, "try");
    		}
    		else if(child instanceof CatchClause){
    			String parameters = ((CatchClause) child).getParam().toStringWithoutComments();
    			addNode(childBegin, childEnd, childId, "catch("+parameters+")");
    		}
    		else if(child instanceof AssertStmt){
    			String check = ((AssertStmt) child).toStringWithoutComments();
    			addNode(childBegin, childEnd, childId, check);
    		}
    		else if(child instanceof AssertStmt){
    			String check = ((AssertStmt) child).toStringWithoutComments();
    			addNode(childBegin, childEnd, childId, check);
    		}
	    }
	    
	    private void addNode(int begin, int end, String nodeId, String code){
	    	this.cfg.addNode(begin, end, nodeId, code);
	    }

		public CFG returnCFG(CompilationUnit cu, Object arg){
	    	visit(cu, arg);
	    	return this.cfg;
	    }
	}
	
	private static class StatementVisitor extends GenericVisitorAdapter<Object, Object>{
		private CFG cfg = new CFG();
		
	    @Override
	    public Object visit(ExpressionStmt s, Object arg) {
	    	System.out.println("Begin: "+s.getBeginLine()+"\nCode:\n"+s.toStringWithoutComments()+"\nEnd: "+s.getEndLine()+"\n");
	        return super.visit(s, arg);
	    }
	    
	    public CFG returnCFG(CompilationUnit cu, Object arg){
	    	visit(cu, arg);
	    	return this.cfg;
	    }
	}
}