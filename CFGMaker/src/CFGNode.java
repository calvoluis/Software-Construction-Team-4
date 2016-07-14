
public class CFGNode {
	private int idNum;
	private int beginLine;
	private int endLine;
	private String representedCode;
	
	public CFGNode(int id, int begin, int end, String code){
		this.idNum = id;
		this.beginLine = begin;
		this.endLine = end;
		this.representedCode = code;
	}
	
	public String getCode(){
		return this.representedCode;
	}
	
	public int getIdNum(){
		return this.idNum;
	}
	
	public int beginLine(){
		return this.beginLine;
	}
	
	public int endLine(){
		return this.endLine;
	}
}
