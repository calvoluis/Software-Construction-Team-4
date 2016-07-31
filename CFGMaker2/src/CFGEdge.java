
public class CFGEdge {
	private String fromNode;
	private String toNode;
	
	public CFGEdge(String from, String to){
		this.fromNode = from.trim();
		this.toNode = to.trim();
	}
	
	public String getFromNode(){
		return this.fromNode;
	}
	
	public String getToNode(){
		return this.toNode;
	}
}
