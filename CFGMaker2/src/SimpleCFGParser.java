import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.Type;
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
	
	private boolean searchForNode(String nodeId){
		return this.cfg.containsNode(nodeId);
	}
	
	@Override
	public Object visit(ReturnStmt retStmt, Object arg){
		int begin = retStmt.getBegin().line;
		int end = retStmt.getEnd().line;
		String nodeId = Integer.toString(begin);
		String code = retStmt.toStringWithoutComments();
		addNode(begin, end, nodeId, code);
		addEdge(nodeId, Integer.toString(retStmt.getParentNode().getParentNode().getEnd().line));
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
	
	private String iterateThroughChildren(Node parent){
		int parentBegin = parent.getBegin().line;
		int parentEnd = parent.getEnd().line;
		String parentId = Integer.toString(parentBegin);
		List<Node> children = parent.getChildrenNodes();
		
		System.out.println("Parent: "+parentId+" class: "+parent.getClass().getSimpleName());
		
		int blockBegin = -1;
		int blockEnd = -1;
		String blockId = "";
		String blockCode = "";
		boolean isFirstChildBlock = true;
		String prevId = "";
		String lastChildId = "";
		for(int i=0; i<children.size(); i++){
			Node child = children.get(i);
			int childBegin = child.getBegin().line;
			int childEnd = child.getEnd().line;
			String childId = Integer.toString(childBegin);
			String childCode = child.toStringWithoutComments();
			
			System.out.println("-Child: "+childId+" class: "+child.getClass().getSimpleName());
			
			if(child instanceof ExpressionStmt){
				if(blockBegin==-1){
					blockBegin = childBegin;
				}
				blockEnd = childEnd;
				blockId += childId+" ";
				blockCode += childCode+"\n";
				
				if(i == children.size()-1){
					addNode(blockBegin, blockEnd, blockId, blockCode);
					if(isFirstChildBlock && parentBegin != -1){
						addEdge(parentId, blockId);
					}
					else if(prevId != ""){
						addEdge(prevId, blockId);
					}
					lastChildId = blockId;
				}
			}
			else if(child instanceof EmptyStmt || child instanceof Comment){
				if(blockBegin==-1){
					blockBegin = childBegin;
				}
				blockEnd = childEnd;
				blockId += childId+" ";
				
				if(i == children.size()-1 && blockCode.trim() != ""){
					addNode(blockBegin, blockEnd, blockId, blockCode);
					if(isFirstChildBlock && parentBegin != -1){
						addEdge(parentId, blockId);
					}
					else if(prevId != ""){
						addEdge(prevId, blockId);
					}
					lastChildId = blockId;
				}
			}
			else if (!(child instanceof Expression || child instanceof Type || child instanceof Parameter)){
				if(blockBegin != -1){
					if(isFirstChildBlock){
						addEdge(parentId, blockId);
					}
					else if(prevId != ""){
						addEdge(prevId, blockId);
					}
					addNode(blockBegin, blockEnd, blockId, blockCode);
					addEdge(blockId, childId);
				}
				else{
					if(isFirstChildBlock){
						addEdge(parentId, childId);
					}
					else if(prevId != ""){
						addEdge(prevId, childId);
					}
				}
				prevId = childId;
				blockBegin = -1;
				blockEnd = -1;
				blockId = "";
				blockCode = "";
				isFirstChildBlock = false;
				if(i == children.size()-1){
					lastChildId = childId;
				}
			}
		}
		return lastChildId;
	}
	
	@Override
	public Object visit(MethodDeclaration methodDecl, Object arg){
		int begin = methodDecl.getBegin().line;
		int end = methodDecl.getEnd().line;
		String nodeId = Integer.toString(begin);
		String code = methodDecl.getDeclarationAsString();
		
		addNode(begin, end, nodeId, code);
		
		return super.visit(methodDecl, arg);
	}
	
	@Override
	public Object visit(BlockStmt blockStmt, Object arg){
		int begin = blockStmt.getBegin().line;
		int end = blockStmt.getEnd().line;
		
		List<Node> children = blockStmt.getChildrenNodes();
		
		String lastChildId = iterateThroughChildren(blockStmt);
		if(children.get(children.size()-1) instanceof ExpressionStmt){
			addEdge(lastChildId, Integer.toString(end));
		}
		addEdge(Integer.toString(end), Integer.toString(blockStmt.getParentNode().getParentNode().getEnd().line));
		
		return super.visit(blockStmt, arg);
	}
	
	@Override
	public Object visit(IfStmt ifStmt, Object arg){
		Expression condition = ifStmt.getCondition();
		int begin = condition.getBegin().line;
		int end = condition.getEnd().line;
		String nodeId = Integer.toString(begin);
		String code = "if("+condition.toStringWithoutComments()+")";
		
		addNode(begin, end, nodeId, code);
		
		if(ifStmt.getChildrenNodes().get(1) instanceof ExpressionStmt){
			iterateThroughChildren(ifStmt);
		}
		
		Statement elseStmt = ifStmt.getElseStmt();
		if(elseStmt != null){
			addEdge(nodeId, Integer.toString(elseStmt.getBegin().line));
		}
		
		return super.visit(ifStmt, arg);
	}
	
	@Override
	public Object visit(ForStmt forStmt, Object arg){
		int begin = forStmt.getBegin().line;
		int end = forStmt.getEnd().line;
		String nodeId = Integer.toString(begin);
		String initialization = "";
		for(Expression init : forStmt.getInit()){
			initialization += init.toStringWithoutComments();
		}
		String update = "";
		for(Expression upd : forStmt.getUpdate()){
			update += upd.toStringWithoutComments();
		}
		String code = "for("+initialization+"; "+forStmt.getCompare().toStringWithoutComments()+"; "+update+")";
		
		addNode(begin, end, nodeId, code);
		
		if(forStmt.getChildrenNodes().get(1) instanceof ExpressionStmt){
			iterateThroughChildren(forStmt);
		}
		
		addEdge(Integer.toString(end), nodeId);
		
		return super.visit(forStmt, arg);
	}
	
	@Override
	public Object visit(SwitchStmt s, Object arg){
		List<Node> children = s.getChildrenNodes();
		int begin = s.getBegin().line;
		
		for(int i=0; i<children.size(); i++){
			Node child = children.get(i);
			
			if(child instanceof SwitchEntryStmt){
				int childBegin = child.getBegin().line;
				int childEnd = child.getEnd().line;
				String childId = Integer.toString(childBegin);
				Expression label = ((SwitchEntryStmt) child).getLabel();
				String childCode  = "default";
				if(label != null){
					childCode = label.toStringWithoutComments();
				}
				
				addNode(childBegin, childEnd, childId, childCode);
				addEdge(Integer.toString(begin), childId);
			}
		}
		
		return super.visit(s, null);
	}
	
	@Override
	public Object visit(TryStmt tryStmt, Object arg){
		int begin = tryStmt.getBegin().line;
		int end = tryStmt.getEnd().line;
		String nodeId = Integer.toString(begin);
		String code = "try";
		
		addNode(begin, end, nodeId, code);
		
		List<CatchClause> catchStmts = tryStmt.getCatchs();
		for(CatchClause catchStmt : catchStmts){
			addEdge(nodeId, Integer.toString(catchStmt.getBegin().line));
		}
		return super.visit(tryStmt, arg);
	}
}