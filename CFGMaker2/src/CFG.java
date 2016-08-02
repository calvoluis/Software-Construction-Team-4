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
	
	public boolean addEdge(String fromNode, String toNode){
		CFGEdge edge = new CFGEdge(fromNode, toNode);
		return this.edges.add(edge);
	}
	
	public boolean containsNode(String nodeId){
		for(CFGNode node : this.nodes){
			if(node.getId()==nodeId){
				return true;
			}
		}
		return false;
	}
	
	public void printNodes(){
		for(int i=0; i<this.nodes.size(); i++){
			CFGNode node = this.nodes.get(i);
			System.out.println("ID: "+node.getId());
			System.out.println("Begin: "+node.getBeginLine());
			System.out.println("Code:\n"+node.getCode());
			System.out.println("End: "+node.getEndLine()+"\n\n");
		}
	}
	
	public void printEdges(){
		for(int i=0; i<this.edges.size(); i++){
			CFGEdge edge = this.edges.get(i);
			System.out.println("From: "+edge.getFromNode());
			System.out.println("To: "+edge.getToNode()+"\n\n");
		}
	}
	
	public String cfgToDotFormat(){
		String dotFormat = "";
		
		for(CFGEdge edge : this.edges){
			String fromId = edge.getFromNode();
			String toId = edge.getToNode();
			dotFormat += fromId + "->" + toId + ";";
		}
		
		return dotFormat;
	}
}
