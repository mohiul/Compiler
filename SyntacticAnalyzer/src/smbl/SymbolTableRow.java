package smbl;

import java.util.List;

public class SymbolTableRow {
	String varName;
	List<VariableType> typeList;
	VariableKind kind;
	SymbolTable link;
	
	public SymbolTableRow(String varName) {
		this.varName = varName;
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
	
}
