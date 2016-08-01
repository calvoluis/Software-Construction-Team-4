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
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
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
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public CFG getCFG(){
		return this.cfg;
	}
	
	private void addNode(int begin, int end, String nodeId, String code){
		this.cfg.addNode(begin, end, nodeId, code);
	}
	
	private void addEdge(String fromId, String toId){
		this.cfg.addEdge(fromId, toId);
	}
	
	private int getRealBegin(Node parent){
		int begin = parent.getBegin().line;
		
		return begin;
	}
	
	@Override
	public Object visit(BlockStmt blockStmt, Object arg){
		Node parent = blockStmt.getParentNode();
		int parentBegin = getRealBegin(parent);
		String parentId = Integer.toString(parentBegin);
		List<Node> children = blockStmt.getChildrenNodes();
		
		System.out.println("Parent :"+parentBegin + " class: "+parent.getClass().getSimpleName());
		
		int begin = -1;
		int end = -1;
		String nodeId = "";
		String code = "";
		boolean isFirstBlockChild = true;
		String prevId = "";
		for(int i=0; i<children.size(); i++){
			Node child = children.get(i);
			int childBegin = child.getBegin().line;
			int childEnd = child.getEnd().line;
			String childId = Integer.toString(childBegin);
			String childCode = child.toStringWithoutComments();
			
			System.out.println("-Child :"+childBegin+" class: "+child.getClass().getSimpleName());
			
			if(child instanceof ExpressionStmt){
				if(begin==-1){
					begin = childBegin;
				}
				end = childEnd;
				nodeId += childId+" ";
				code += childCode+"\n";
				
				if(i == children.size()-1){
					addNode(begin, end, nodeId, code);
					if(isFirstBlockChild && parentBegin != -1){
						addEdge(parentId, nodeId);
					}
					else if(prevId != ""){
						addEdge(prevId, nodeId);
					}
				}
			}
			else if(child instanceof EmptyStmt || child instanceof Comment){
				if(begin==-1){
					begin = childBegin;
				}
				end = childEnd;
				nodeId += childId+" ";
			}
			else{
				if(begin != -1){
					if(isFirstBlockChild){
						addEdge(parentId, nodeId);
					}
					else if(prevId != ""){
						addEdge(prevId, nodeId);
					}
					addNode(begin, end, nodeId, code);
					addEdge(nodeId, childId);
				}
				else{
					if(isFirstBlockChild){
						addEdge(parentId, childId);
					}
					else if(prevId != ""){
						addEdge(prevId, childId);
					}
//					addNode(begin, end, childId, code);
//					addEdge(nodeId, childId);
				}
				prevId = childId;
				begin = -1;
				end = -1;
				nodeId = "";
				code = "";
				isFirstBlockChild = false;
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
		addNode(begin, end, nodeId, code);
		return super.visit(retStmt, arg);
	}
	
	@Override
	public Object visit(BreakStmt breakStmt, Object arg){
		int begin = breakStmt.getBegin().line;
		int end = breakStmt.getEnd().line;
		String nodeId = Integer.toString(begin);
		String code = breakStmt.toStringWithoutComments();
		addNode(begin, end, nodeId, code);
		return super.visit(breakStmt, arg);
	}
	
	@Override
	public Object visit(IfStmt ifStmt, Object arg){
		Expression condition = ifStmt.getCondition();
		int begin = condition.getBegin().line;
		int end = condition.getEnd().line;
		String nodeId = Integer.toString(begin);
		String code = "if("+condition.toStringWithoutComments()+")";
		
		addNode(begin, end, nodeId, code);
		
		Statement elseStmt = ifStmt.getElseStmt();
		if(elseStmt != null){
			addEdge(nodeId, Integer.toString(elseStmt.getBegin().line));
		}
		return super.visit(ifStmt, arg);
	}
	
	@Override
	public Object visit(TryStmt tryStmt, Object arg){
		int begin = tryStmt.getBegin().line;
		int end = tryStmt.getEnd().line;
		String nodeId = Integer.toString(begin);
		String code = "try";
		
		addNode(begin, end, nodeId, code);
		
		List<CatchClause> catchStmts = tryStmt.getCatchs();
//		if(elseStmt != null){
//			addEdge(nodeId, Integer.toString(elseStmt.getBegin().line));
//		}
		for(CatchClause catchStmt : catchStmts){
			addEdge(nodeId, Integer.toString(catchStmt.getBegin().line));
		}
		return super.visit(tryStmt, arg);
	}
}