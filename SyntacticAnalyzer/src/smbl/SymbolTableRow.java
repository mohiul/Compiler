package smbl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SymbolTableRow {
	String varName;
	VariableType type;
	List<VariableType> paramTypeList;
	VariableKind kind;
	SymbolTable link;
	
	public SymbolTableRow(String varName) {
		this.varName = varName;
		type = new VariableType();
		paramTypeList = new ArrayList<VariableType>();
	}
	
	public SymbolTableRow(String varName, VariableType type, VariableKind kind) {
		this.varName = varName;
		this.type = type;
		this.kind = kind;
	}
	
	public SymbolTableRow(String varName, VariableType type, VariableKind kind, SymbolTable link) {
		this.varName = varName;
		this.type = type;
		this.kind = kind;
		this.link = link;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public VariableType getType() {
		return type;
	}

	public void setType(VariableType type) {
		this.type = type;
	}

	public List<VariableType> getParamTypeList() {
		return paramTypeList;
	}

	public void setParamTypeList(List<VariableType> paramTypeList) {
		this.paramTypeList = paramTypeList;
	}
	
	public void addParamType(VariableType paramType) {
		paramTypeList.add(paramType);
	}

	public VariableKind getKind() {
		return kind;
	}

	public void setKind(VariableKind kind) {
		this.kind = kind;
	}

	public SymbolTable getLink() {
		return link;
	}

	public void setLink(SymbolTable link) {
		this.link = link;
	}

	public String getVariableKindStr(){
		String str = "";
		switch(kind){
		case FUNCTION:
			str += "FUNCTION";
			break;
		case CLASS:
			str += "CLASS";
			break;
		case PARAMETER:
			str += "PARAMETER";
			break;
		case VARIABLE:
			str += "VARIABLE";
			break;
		}
		return str;
	}
	
	public String toString(Map<String, SymbolTable> tableMap) {
		if(link != null) tableMap.put(varName, link);
		String str = "Name: " + varName + " kind: " + getVariableKindStr();
		str += " type: " + type.toString();
		return str;
	}
	
}
