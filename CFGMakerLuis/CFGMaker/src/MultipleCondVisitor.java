import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.BinaryExpr;
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

public class MultipleCondVisitor extends GenericVisitorAdapter<Object, Object>{
	private CFG cfg = new CFG();

    @Override
    public Object visit(BlockStmt n, Object arg) {
    	List<Node> children = n.getChildrenNodes();
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
    		
    		if(isConditional){
    			if(code!=""){
    				this.cfg.addNode(begin, end, nodeId, code);
    			}
    			handleConditionals(child,children,i);
    			begin = -1;
    			end = -1;
    			code = "";
    		}
    		else if(isCtrlFlowBreak){
    			if(code!=""){
	    			this.cfg.addNode(begin, end, nodeId, code);
    			}
    			handleBreaks(child,children,i);
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
    				this.cfg.addNode(childBegin, childEnd, childId, childCode);
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
    	for(int i=0; i<children.size(); i++){
    		Node child = children.get(i);
    		
    		int childBegin = child.getBeginLine();
    		int childEnd = child.getEndLine();
    		String childId = Integer.toString(childBegin);
    		
    		if(child instanceof SwitchEntryStmt){
    			String condition = ((SwitchEntryStmt) child).toStringWithoutComments();
    			checkStmt(condition,childBegin,childEnd,childId,child);
    			this.cfg.addEdge(Integer.toString(s.getBeginLine()),childId);
    		}
    	}  	
    	return super.visit(s, arg);
    }
    
    @Override
	public Object visit(IfStmt s, Object arg){
		List<Node> children = s.getChildrenNodes();
    	Node switchParent = s;
    	for(int i=0; i<children.size(); i++){
    		Node child = children.get(i);
    		
    		int childBegin = child.getBeginLine();
    		int childEnd = child.getEndLine();
    		String childId = Integer.toString(childBegin);
    		String childCode = child.toStringWithoutComments();
    		
    		if(child instanceof BlockStmt || child instanceof IfStmt ||child instanceof BinaryExpr){
    			String condition = child.toStringWithoutComments();
    			checkStmt(condition,childBegin,childEnd,childId,child);
    			this.cfg.addEdge(Integer.toString(s.getBeginLine()),Integer.toString(children.get(i).getEndLine()+1));
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
			this.cfg.addNode(childBegin, childEnd, childId, condition);
			checkEdge(i,children,childId);
		}
		else if(child instanceof BreakStmt){
			this.cfg.addNode(childBegin, childEnd, childId, "break;");
			checkEdge(i,children,childId);
		}
		else if(child instanceof TryStmt){
			this.cfg.addNode(childBegin, childEnd, childId, "try");
			checkEdge(i,children,childId);
		}
		else if(child instanceof CatchClause){
			String parameters = ((CatchClause) child).getParam().toStringWithoutComments();
			this.cfg.addNode(childBegin, childEnd, childId, "catch("+parameters+")");
			checkEdge(i,children,childId);
		}
		else if(child instanceof ContinueStmt){
			this.cfg.addNode(childBegin, childEnd, childId, "continue");
			checkEdge(i,children,childId);
		}
		else if(child instanceof LabeledStmt){
			String label = ((LabeledStmt) child).getLabel();
			this.cfg.addNode(childBegin, childEnd, childId, label);
			checkEdge(i,children,childId);
		}
	}

	private void handleConditionals(Node child, List<Node> children, int i){
    	int childBegin = child.getBeginLine();
		int childEnd = child.getEndLine();
		String childId = Integer.toString(childBegin);
				
    	if(child instanceof IfStmt){
    		String condition = ((IfStmt) child).getCondition().toStringWithoutComments();
			childEnd = ((IfStmt) child).getCondition().getEndLine();
			checkStmt(condition,childBegin,childEnd,childId,child);
			if(child.getChildrenNodes().get(1) instanceof ExpressionStmt){
				visit((IfStmt)child,null);
			}
			checkEdge(i,children,childId);
    	}
		else if(child instanceof DoStmt){
			String condition = ((DoStmt) child).getCondition().toStringWithoutComments();
			checkStmt(condition,childBegin,childEnd,childId,child);
			checkEdge(i,children,childId);
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
			checkStmt(condition,childBegin,childEnd,childId,child);
			checkEdge(i,children,childId);
		}
		else if(child instanceof ForeachStmt){
			String condition = ((ForeachStmt) child).getIterable().toStringWithoutComments();
			checkStmt(condition,childBegin,childEnd,childId,child);
			checkEdge(i,children,childId);
		}
		else if(child instanceof WhileStmt){
			String condition = ((WhileStmt) child).getCondition().toStringWithoutComments();
			checkStmt(condition,childBegin,childEnd,childId,child);
			checkEdge(i,children,childId);
		}
		else if(child instanceof SwitchStmt){
			String condition = ((SwitchStmt) child).getSelector().toStringWithoutComments();
			checkStmt(condition,childBegin,childEnd,childId,child);
			visit((SwitchStmt) child, null);
		}
    }
    
	private void checkAnd(String condition, int childBegin, int childEnd, String childId, Node type){
		char[] alph = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		int ltr =0;
		List<Node> nodes = type.getChildrenNodes();
		String insideNode = Integer.toString(nodes.get(1).getBeginLine());

		String[] children = condition.split("[&]");
		for(int i=0; i<children.length;i++)
		{
			//section checks for other type of conditional within conditional
			String[] otherChildren = children[i].split("[|]");
			if(otherChildren.length>1)
			{
				checkOr(children[i],childBegin,childEnd,childId,type);
				for(int j=2;j<children.length;j+=2)
				{
					otherChildren = children[j].split("[&]");
					if(otherChildren.length>1)
						checkOr(children[j],childBegin,childEnd,childId,type);					
				}				
				ltr+=2;
			}
			
			//add && nodes
			else if(!children[i].equals("")){
				if(i+2<children.length)
				{
					this.cfg.addNode(childBegin,childEnd,childId+alph[ltr],children[i]+"&&");
					this.cfg.addNode(childBegin,childEnd,childId+alph[ltr+1],children[i+=2]+")");
					this.cfg.addEdge(childId+alph[ltr],insideNode);
					this.cfg.addEdge(childId+alph[ltr+1],insideNode);
					this.cfg.addEdge(childId+alph[ltr++],childId+alph[ltr++]);
				}
			}
		}
		
	}
	
	private void checkOr(String condition, int childBegin, int childEnd, String childId, Node type){
		char[] alph = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		int ltr = 0;
		List<Node> nodes = type.getChildrenNodes();
		String insideNode = Integer.toString(nodes.get(1).getBeginLine());
		String[] children = condition.split("[|]");
		for(int i=0; i<children.length;i++)
		{
			//section checks for other type of conditional within conditional
			String[] otherChildren = children[i].split("[&]");
			if(otherChildren.length>1)
			{
				checkAnd(children[i],childBegin,childEnd,childId,type);
				for(int j=2;j<children.length;j+=2)
				{
					otherChildren = children[j].split("[&]");
					if(otherChildren.length>1)
						checkAnd(children[j],childBegin,childEnd,childId,type);					
				}				
				ltr+=2;
			}
			
			//add || nodes
			if(!children[i].equals("")){
				if(i+2<children.length)
				{
					this.cfg.addNode(childBegin,childEnd,childId+alph[ltr],children[i]+"||");
					this.cfg.addNode(childBegin,childEnd,childId+alph[ltr+1],children[i+=2]+")");
					this.cfg.addEdge(childId+alph[ltr],insideNode);
					this.cfg.addEdge(childId+alph[ltr+1],insideNode);
					this.cfg.addEdge(childId+alph[ltr++],childId+alph[ltr++]);
				}
			}
		}
	}
	
	private void checkStmt(String condition, int childBegin, int childEnd, String childId, Node type){
		
		if(condition.contains("||")){
			checkOr(condition,childBegin,childEnd,childId,type);
		}
		else if(condition.contains("&&")){
			checkAnd(condition,childBegin,childEnd,childId,type);
		}
		else{
		this.cfg.addNode(childBegin, childEnd, childId, condition);
		}
	}
	
    private void checkEdge(int i, List<Node> children, String childBegin){
    	if(i>0)
    		this.cfg.addEdge(Integer.toString(children.get(i-1).getParentNode().getBeginLine()),childBegin);
		else
			this.cfg.addEdge(Integer.toString(children.get(0).getBeginLine()),childBegin);
    }
    
	public CFG returnCFG(CompilationUnit cu, Object arg){
    	visit(cu, arg);
    	return this.cfg;
    }
}