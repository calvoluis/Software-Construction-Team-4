import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

public class SimpleVisitor extends GenericVisitorAdapter<Object, Object> {
	private CFG cfg = new CFG();
	
    @Override
    public Object visit(BlockStmt n, Object arg) {
    	List<Node> children = n.getChildrenNodes();
    	System.out.println("Looking at: "+n.getBeginLine()+"     Children: "+children.size()+"\n");
    	//adds the current BlockStmt's children to the CFG if the children do not have children themselves
    	int begin = -1;
    	int end = -1;
    	String nodeId = "";
    	String code = "";
    	for(int i=0; i<children.size(); i++){
    		Node child = children.get(i);
    		
    		boolean isConditional = child instanceof IfStmt;
    		isConditional = isConditional || child instanceof DoStmt;
    		isConditional = isConditional || child instanceof ForStmt;
    		isConditional = isConditional || child instanceof ForeachStmt;
    		isConditional = isConditional || child instanceof WhileStmt;
    		isConditional = isConditional || child instanceof SwitchStmt;
    		
    		boolean isCtrlFlowBreak = child instanceof TryStmt;
    		isCtrlFlowBreak = isCtrlFlowBreak || child instanceof CatchClause;
    		isCtrlFlowBreak = isCtrlFlowBreak || child instanceof AssertStmt;
    		isCtrlFlowBreak = isCtrlFlowBreak || child instanceof ContinueStmt;
    		isCtrlFlowBreak = isCtrlFlowBreak || child instanceof LabeledStmt;
    		isCtrlFlowBreak = isCtrlFlowBreak || child instanceof ReturnStmt;
    		isCtrlFlowBreak = isCtrlFlowBreak || child instanceof BreakStmt;
    		isCtrlFlowBreak = isCtrlFlowBreak || child instanceof ThrowStmt;
    		
    		int childBegin = child.getBeginLine();
    		int childEnd = child.getEndLine();
    		String childId = Integer.toString(childBegin);
    		String childCode = child.toStringWithoutComments();
    		
    		System.out.println("Child begin: "+childBegin+" Class: "+child.getClass().getSimpleName());
    		
    		if(isConditional){
    			if(code!=""){
	    			addNode(begin, end, nodeId, code);
	    			checkEdge(i,children,childBegin);
	    			System.out.println("ADDED BASIC BLOCK "+nodeId);
    			}
    			handleConditionals(child,children,i);
    			System.out.println("ADDED CONDITIONAL "+childId);
    			begin = -1;
    			end = -1;
    			code = "";
    		}
    		else if(isCtrlFlowBreak){
    			if(code!=""){
	    			addNode(begin, end, nodeId, code);
	    			checkEdge(i,children,childBegin);
	    			System.out.println("ADDED BASIC BLOCK "+nodeId);
    			}
    			handleBreaks(child,children,i);
    			System.out.println("ADDED CONDITIONAL "+childId);
    			begin = -1;
    			end = -1;
    			code = "";
    		}
    		else if(child instanceof EmptyStmt || child instanceof Comment){
    			if(begin == -1){
	    			begin = childBegin;
	    		}
	    		end = childEnd;
	    		nodeId += childId+" ";
    		}
    		else{
    			if(i==children.size()-1){
    				addNode(childBegin, childEnd, childId, childCode);
    				checkEdge(i,children,childBegin);
    				System.out.println("ADDED NON CONDITIONAL "+childId);
    			}
    			else{
		    		if(begin == -1){
		    			begin = childBegin;
		    		}
		    		end = childEnd;
		    		nodeId += childId+" ";
		    		code += childCode+"\n";
    			}
    		}
    	}
        return super.visit(n, arg);
    }
    
    @Override
    public Object visit(SwitchStmt s, Object arg){
    	List<Node> children = s.getChildrenNodes();
    	System.out.println("Looking at switch: "+s.getBeginLine()+"     Children: "+children.size()+"\n");
    	Node switchParent = s;
    	for(int i=0; i<children.size(); i++){
    		Node child = children.get(i);
    		
    		int childBegin = child.getBeginLine();
    		int childEnd = child.getEndLine();
    		String childId = Integer.toString(childBegin);
    		String childCode = child.toStringWithoutComments();
    		
    		System.out.println("Child begin: "+childBegin+" Class: "+child.getClass().getSimpleName());
    		if(child instanceof SwitchEntryStmt){
    			String condition = ((SwitchEntryStmt) child).toStringWithoutComments();
    			addEdge(s.getBeginLine(),childBegin);
    		}
    	}
    	return super.visit(s, arg);
    }
    
    private void handleBreaks(Node child, List<Node> children, int i){
    	int childBegin = child.getBeginLine();
		int childEnd = child.getEndLine();
		String childId = Integer.toString(childBegin);
		
		if(child instanceof AssertStmt){
    		String condition = ((AssertStmt) child).toStringWithoutComments();
			childEnd = ((AssertStmt) child).getEndLine();
			addNode(childBegin, childEnd, childId, condition);
			checkEdge(i,children,childBegin);
		}
		else if(child instanceof BreakStmt){
			addNode(childBegin, childEnd, childId, "break;");
			checkEdge(i,children,childBegin);
		}
		else if(child instanceof TryStmt){
			addNode(childBegin, childEnd, childId, "try");
			checkEdge(i,children,childBegin);
		}
		else if(child instanceof CatchClause){
			String parameters = ((CatchClause) child).getParam().toStringWithoutComments();
			addNode(childBegin, childEnd, childId, "catch("+parameters+")");
			checkEdge(i,children,childBegin);
		}
		else if(child instanceof ContinueStmt){
			addNode(childBegin, childEnd, childId, "continue");
			checkEdge(i,children,childBegin);
		}
		else if(child instanceof LabeledStmt){
			String label = ((LabeledStmt) child).getLabel();
			addNode(childBegin, childEnd, childId, label);
			checkEdge(i,children,childBegin);
		}
	}

	private void handleConditionals(Node child, List<Node> children, int i){
    	int childBegin = child.getBeginLine();
		int childEnd = child.getEndLine();
		String childId = Integer.toString(childBegin);
		
    	if(child instanceof IfStmt){
    		String condition = ((IfStmt) child).getCondition().toStringWithoutComments();
			childEnd = ((IfStmt) child).getCondition().getEndLine();
			addNode(childBegin, childEnd, childId, "if("+condition+")");
			checkEdge(i,children,childBegin);
		}
		else if(child instanceof DoStmt){
			String condition = ((DoStmt) child).getCondition().toStringWithoutComments();
			addNode(childBegin, childEnd, childId, "do{...}while("+condition+");");
			checkEdge(i,children,childBegin);
		}
		else if(child instanceof ForStmt){
			String condition = ((ForStmt) child).getCompare().toStringWithoutComments();
			String initializations = "";
			List<Expression> initList = ((ForStmt) child).getInit();
			for(Expression initialization : initList){
				initializations+=initialization.toStringWithoutComments();
			}
			String updates = "";
			List<Expression> updatesList = ((ForStmt) child).getUpdate();
			for(Expression update : updatesList){
				updates+=update.toStringWithoutComments();
			}
			addNode(childBegin, childEnd, childId, "for("+initializations+"; "+condition+"; "+updates+")");
			checkEdge(i,children,childBegin);
		}
		else if(child instanceof ForeachStmt){
			String condition = ((ForeachStmt) child).getIterable().toStringWithoutComments();
			addNode(childBegin, childEnd, childId, "foreach("+condition+")");
			checkEdge(i,children,childBegin);
		}
		else if(child instanceof WhileStmt){
			String condition = ((WhileStmt) child).getCondition().toStringWithoutComments();
			addNode(childBegin, childEnd, childId, "foreach("+condition+")");
			checkEdge(i,children,childBegin);
		}
		else if(child instanceof SwitchStmt){
			String condition = ((SwitchStmt) child).getSelector().toStringWithoutComments();
//			List<SwitchEntryStmt> entryStmts = ((SwitchStmt) child).getEntries();
			addNode(childBegin, childEnd, childId, "switch("+condition+")");
			checkEdge(i,children,childBegin);
			visit((SwitchStmt) child, null);
		}
		else if(child instanceof SwitchEntryStmt){
			System.out.println("-------------------------SWITCH ENTRY STATEMENT FOUND");
			String condition = ((SwitchEntryStmt) child).getLabel().toStringWithoutComments();
			addNode(childBegin, childEnd, childId, "case "+condition+":");
			checkEdge(i,children,childBegin);
		}
    }
    
    private void addNode(int begin, int end, String nodeId, String code){
    	this.cfg.addNode(begin, end, nodeId, code);
    }

    private void addEdge(int from, int to){
    	this.cfg.addEdge(from,to);
    }
    
    private void checkEdge(int i, List<Node> children, int childBegin){
    	if(i>0)
    		addEdge(children.get(i-1).getParentNode().getBeginLine(),childBegin);
		else
			addEdge(children.get(0).getBeginLine(),childBegin);
    }
    
	public CFG returnCFG(CompilationUnit cu, Object arg){
    	visit(cu, arg);
    	return this.cfg;
    }
}