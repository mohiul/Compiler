package smbl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SymbolTableRow {
	String varName;
	List<VariableType> typeList;
	VariableKind kind;
	SymbolTable link;
	
	public SymbolTableRow(String varName) {
		this.varName = varName;
		typeList = new ArrayList<VariableType>();
	}
	
	public SymbolTableRow(String varName, List<VariableType> typeList, VariableKind kind) {
		this.varName = varName;
		this.typeList = typeList;
		this.kind = kind;
	}
	
	public SymbolTableRow(String varName, List<VariableType> typeList, VariableKind kind, SymbolTable link) {
		this.varName = varName;
		this.typeList = typeList;
		this.kind = kind;
		this.link = link;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public List<VariableType> getTypeList() {
		return typeList;
	}

	public void setTypeList(List<VariableType> typeList) {
		this.typeList = typeList;
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
		if(typeList.size() > 0){
			str += " type: " + printTypeList();
		}
		return str;
	}

	private String printTypeList() {
		String str = "";
		for(VariableType type: typeList){
			str += type.toString();
		}
		return str;
	}
	
}
