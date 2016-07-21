import java.util.LinkedList;
import java.util.List;

public class CFG {
	private List<CFGNode> nodes;
	private List<CFGEdge> edges;
	
	public CFG(){
		this.nodes = new LinkedList<CFGNode>();
		this.edges = new LinkedList<CFGEdge>();
	}
	
	public boolean addNode(int begin, int end, String nodeId, String code){
		CFGNode node = new CFGNode(begin, end, nodeId, code);
		return this.nodes.add(node);
	}
	
	public boolean addEdge(CFGEdge edge){
		return this.edges.add(edge);
	}
	
	public void printNodes(){
		for(int i=0; i<this.nodes.size(); i++){
			CFGNode node = this.nodes.get(i);
			System.out.println("ID: "+node.getIdNum());
			System.out.println("Begin: "+node.getBeginLine());
			System.out.println("Code:\n"+node.getCode());
			System.out.println("End: "+node.getEndLine()+"\n\n");
		}
	}
}
