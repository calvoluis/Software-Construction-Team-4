import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
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
	    	System.out.println("Looking at: "+n.getBeginLine()+" ends: "+n.getEndLine()+"   Children: "+children.size()+"\n");
	    	//adds the current BlockStmt's children to the CFG if the children do not have children themselves
//	    	if(children.size()>1){
//	    		String basicBlock = "";
//	    		int basicBlockBegin = -1;
//	    		int basicBlockEnd = -1;
//	    		boolean newBlockNeeded = false;
//	    		for(int i=0; i<children.size(); i++){
//	    			Node child = children.get(i);
//	    			int begin = child.getBeginLine();
//    	    		int end = child.getEndLine();
//    	    		String code = child.toStringWithoutComments();
//	    			if(child.getChildrenNodes().size()==1){
////	    				if(basicBlockBegin==-1){
////	    					basicBlockBegin = begin;
////	    					basicBlockEnd = end;
////	    					basicBlock += code+"\n";
////	    				}
////	    				else{
////	    					basicBlockEnd = end;
////	    					basicBlock += code+"\n";
////	    					this.cfg.addNode(basicBlockBegin, basicBlockEnd, basicBlock);
////	    					basicBlock = "";
////	    		    		basicBlockBegin = -1;
////	    		    		basicBlockEnd = -1;
////	    				}
////	    	    		this.cfg.addNode(begin, end, code);
////	    				System.out.println("Line: "+child.getBeginLine()+" "+child.getClass());
////	    				basicBlockBegin = begin;
//	    				if(basicBlockBegin == -1){
//	    					basicBlockBegin = begin;
//	    				}
//    					basicBlockEnd = end;
//    					basicBlock += code+"\n";
//	    			}
//	    			else{
//	    				if(basicBlock != ""){
//		    				this.cfg.addNode(basicBlockBegin, basicBlockEnd, basicBlock);
//		    				basicBlock = "";
//	    		    		basicBlockBegin = -1;
//	    		    		basicBlockEnd = -1;
//	    				}
//	    			}
//	    		}
//	    	}
	    	int begin = -1;
	    	int end = -1;
	    	String code = "";
	    	boolean newNodeMade = false;
	    	for(int i=0; i<children.size(); i++){
	    		Node child = children.get(i);
	    		//System.out.println("Child begin: "+child.getBeginLine()+" Class: "+child.getClass());
	    		if(child instanceof ExpressionStmt){
		    		if(begin == -1){
		    			begin = child.getBeginLine();
		    		}
		    		end = child.getEndLine();
		    		code += child.toStringWithoutComments()+"\n";
		    		//Continue used to skip duplicate code in other if statements below
		    		continue;
	    		}
	    		 if(child instanceof IfStmt){
	    			if(code!=""){
		    			this.cfg.addNode(begin, end, code);
		    			this.cfg.addNode(child.getBeginLine(), child.getEndLine(), "if("+((IfStmt) child).getCondition().toStringWithoutComments()+")");
		    			String ifCond = ((IfStmt) child).getCondition().toStringWithoutComments();
		    			//Adding multiple conditioning still needs work
	    				if(ifCond.indexOf("|") >= 0)
	    				{
	    					String[] split = ifCond.split(Pattern.quote("||"));
	    					this.cfg.addNode(begin, end, split[0]);
	    					this.cfg.addNode(begin, end, split[1]);
	    					System.out.println("TEST if OR cond "+split[0]);
	    				}
	    				else if(ifCond.indexOf("&")>=0){
	    					String[] split = ifCond.split(Pattern.quote("&&"));
	    					this.cfg.addNode(begin, begin, split[0]);
	    					this.cfg.addNode(begin, end, split[1]);
	    					System.out.println("TEST while AND cond "+split[0]+" "+ split[1]);
	    				}
		    			
	    			}
	    		}
	    		else if(child instanceof ForStmt){
	    			if(code!=""){
	    				this.cfg.addNode(begin, end, code);
	    				this.cfg.addNode(child.getBeginLine(), child.getEndLine(), "for("+((ForStmt) child).getCompare().toStringWithoutComments()+")");
	    			}
	    		}	
	    		
	    		else if(child instanceof WhileStmt){
	    			if(code!=""){
	    				String whileCond = ((WhileStmt) child).getCondition().toStringWithoutComments();
	    				this.cfg.addNode(begin, end, code);
	    				this.cfg.addNode(child.getBeginLine(), child.getEndLine(), "while("+((WhileStmt) child).getCondition().toStringWithoutComments()+")");
	    				
	    				//Adding multiple conditioning still needs work
	    				if(whileCond.indexOf("|") >= 0)
	    				{
	    					String[] split = whileCond.split(Pattern.quote("||"));
	    					this.cfg.addNode(begin, end, split[0]);
	    					this.cfg.addNode(begin, end, split[1]);
	    					System.out.println("TEST while Or cond "+split[0]);
	    				}
	    				else if(whileCond.indexOf("&")>=0){
	    					String[] split = whileCond.split(Pattern.quote("&&"));
	    					this.cfg.addNode(begin, begin, split[0]);
	    					this.cfg.addNode(begin, end, split[1]);
	    					System.out.println("TEST while AND cond "+split[0]+" "+ split[1]);
	    				}
	    			}
	    		}
	    		else{
	    			if(code!=""){
		    			this.cfg.addNode(begin, end, code);
	    			}
	    		}
	    		 	begin = -1;
	    		 	end = -1;
	    		 	code = "";
	    	}
	        return super.visit(n, arg);
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