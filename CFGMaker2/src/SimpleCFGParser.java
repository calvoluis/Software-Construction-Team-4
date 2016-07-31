import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

public class SimpleCFGParser extends GenericVisitorAdapter<Object, Object>{
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
			visit(cu, null);
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
	
	@Override
	public Object visit(BlockStmt blockStmt, Object arg){
		List<Node> children = blockStmt.getChildrenNodes();
		
		int begin = -1;
		int end = -1;
		String nodeId = "";
		String code = "";
		for(int i=0; i<children.size(); i++){
			Node child = children.get(i);
			int childBegin = child.getBegin().line;
			int childEnd = child.getEnd().line;
			String childId = Integer.toString(childBegin);
			String childCode = child.toStringWithoutComments();
			
			if(child instanceof ExpressionStmt){
				if(begin==-1){
					begin = childBegin;
				}
				end = childEnd;
				nodeId += childId+" ";
				code += childCode+"\n";
				
				if(i == children.size()-1){
					this.cfg.addNode(begin, end, nodeId, code);
				}
			}
			else if(child instanceof EmptyStmt || child instanceof Comment){
				if(begin==-1){
					begin = childBegin;
				}
				end = childEnd;
				nodeId += childId+" ";
				code += childCode+"\n";
			}
			else{
				if(begin != -1){
					this.cfg.addNode(begin, end, nodeId, code);
					this.cfg.addEdge(nodeId, childId);
				}
				begin = -1;
				end = -1;
				nodeId = "";
				code = "";
			}
		}
		
		return super.visit(blockStmt, arg);
	}
	
	@Override
	public Object visit(ReturnStmt retStmt, Object arg){
		int begin = retStmt.getBegin().line;
		int end = retStmt.getEnd().line;
		String nodeId = Integer.toString(begin);
		String code = retStmt.toStringWithoutComments();
		this.cfg.addNode(begin, end, nodeId, code);
		return super.visit(retStmt, arg);
	}
	
	@Override
	public Object visit(BreakStmt breakStmt, Object arg){
		int begin = breakStmt.getBegin().line;
		int end = breakStmt.getEnd().line;
		String nodeId = Integer.toString(begin);
		String code = breakStmt.toStringWithoutComments();
		this.cfg.addNode(begin, end, nodeId, code);
		return super.visit(breakStmt, arg);
	}
	
	@Override
	public Object visit(IfStmt ifStmt, Object arg){
		Expression condition = ifStmt.getCondition();
		int begin = condition.getBegin().line;
		int end = condition.getEnd().line;
		String nodeId = Integer.toString(begin);
		String code = "if("+condition.toStringWithoutComments()+")";
		this.cfg.addNode(begin, end, nodeId, code);
		return super.visit(ifStmt, arg);
	}
	
//	@Override
//	public Object visit(ExpressionStmt exprStmt, Object arg){
//		int begin = exprStmt.getBegin().line;
//		int end = exprStmt.getEnd().line;
//		String nodeId = exprStmt.getBegin().toString();
//		String code = exprStmt.toStringWithoutComments();
//		this.cfg.addNode(begin, end, nodeId, code);
//		return super.visit(exprStmt, arg);
//	}
	
	
}