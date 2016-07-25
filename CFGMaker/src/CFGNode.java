
public class CFGNode {
	private String nodeId;
	private int beginLine;
	private int endLine;
	private String representedCode;
	
	public CFGNode(int begin, int end, String id, String code){
		this.nodeId = id.trim();
		this.beginLine = begin;
		this.endLine = end;
		this.representedCode = code.trim();
	}
	
	public String getCode(){
		return this.representedCode;
	}
	
	public String getIdNum(){
		return this.nodeId;
	}
	
	public int getBeginLine(){
		return this.beginLine;
	}
	
	public int getEndLine(){
		return this.endLine;
	}
}
