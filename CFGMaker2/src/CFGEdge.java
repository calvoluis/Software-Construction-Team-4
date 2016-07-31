
public class CFGEdge {
	private String fromNode;
	private String toNode;
	
	public CFGEdge(String from, String to){
		this.fromNode = from;
		this.toNode = to;
	}
	
	public String getFromNode(){
		return this.fromNode;
	}
	
	public String getToNode(){
		return this.toNode;
	}
}
