package lex;

public class Token {
	private String type;
	private String value;
	private int lineNo;
	private int positionInLine;
	
	public Token( String type, String value, int lineNo, int positionInLine ){
		this.type = type;
		this.value = value;
		this.lineNo = lineNo;
		this.positionInLine = positionInLine;
	}

	public Token() {
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getLineNo() {
		return lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

	public int getPositionInLine() {
		return positionInLine;
	}

	public void setPositionInLine(int positionInLine) {
		this.positionInLine = positionInLine;
	}

	@Override
	public String toString() {
		return "Token"
				+ " line: " + lineNo 
				+ " position: " + positionInLine
				+ " type: " + type 
				+ " value: " + value; 
	}
	
}
