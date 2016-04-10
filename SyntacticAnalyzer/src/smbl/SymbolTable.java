package smbl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lex.Token;

public class SymbolTable {
	private String tableName;

	public Map<String, SymbolTableRow> tableRowMap;

	public SymbolTable(String tableName) {
		this.tableName = tableName;
		tableRowMap = new LinkedHashMap<String, SymbolTableRow>();
	}
	
	public SymbolTable addRowAndTable(Token id, VariableKind kind) {
		String name = id.getValue();
		SymbolTableRow row = new SymbolTableRow(name);
		row.setKind(kind);
		SymbolTable linkTable = new SymbolTable(name);
		row.setLink(linkTable);
		tableRowMap.put(name, row);
		return linkTable;
	}
	
	public SymbolTable addRowAndTable(Token type, Token id, VariableKind kind) {
		String name = id.getValue();
		SymbolTableRow row = new SymbolTableRow(name);
		row.setKind(kind);
		row.setType(getTypeByToken(type));
		SymbolTable linkTable = new SymbolTable(name);
		row.setLink(linkTable);
		tableRowMap.put(name, row);
		return linkTable;
	}
	
	private VariableType getTypeByToken(Token type) {
		VariableType varType = new VariableType();
		varType.setTypeName(type.getValue());
		return varType;
	}

	public boolean addRow(Token type, Token id, List<Token> arraySizeList, VariableKind kind) {
		String name = id.getValue();
		SymbolTableRow row = new SymbolTableRow(name);
		row.setKind(kind);
		row.setType(getTypeByToken(type, arraySizeList));
		tableRowMap.put(name, row);
		return true;
	}

	public static VariableType getTypeByToken(Token type, List<Token> arraySizeList) {
		VariableType varType = new VariableType();
		if(arraySizeList != null){
			varType.setDimension(getArraySizesFromList(arraySizeList));			
		}
		varType.setTypeName(type.getValue());
		return varType;
	}
	
	private static int[] getArraySizesFromList(List<Token> arraySizeList) {
		int[] sizeArray = new int[arraySizeList.size()];
		int i = arraySizeList.size() - 1;
		for(Token token : arraySizeList){
			//TODO Check if the token is int
			sizeArray[i--] = Integer.parseInt(token.getValue());
		}
		return sizeArray;
	}

	public String toString(Map<String, SymbolTable> tableMap) {
		String str = tableName + "\n" ;
		if(tableRowMap.size() == 0){
			str = "No Entry\n" ;
		} else {
			for( String name : tableRowMap.keySet()){
				str += tableRowMap.get(name).toString(tableMap) + "\n";
			}
		}
		return str;
	}
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}	
}
