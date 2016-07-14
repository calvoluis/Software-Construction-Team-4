
public class CFGEdge {
	private int fromNode;
	private int toNode;
	
	public CFGEdge(int from, int to){
		this.fromNode = from;
		this.toNode = to;
	}
	
	public int getFromNode(){
		return this.fromNode;
	}
	
	public int getToNode(){
		return this.toNode;
	}
}
