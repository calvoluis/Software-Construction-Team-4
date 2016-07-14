
public class CFGNode {
	private int idNum;
	private String representedCode;
	
	public CFGNode(String code, int id){
		this.representedCode = code;
		this.idNum = id;
	}
	
	public String getCode(){
		return this.representedCode;
	}
	
	public int getIdNum(){
		return this.idNum;
	}
}
