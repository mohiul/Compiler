package smbl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lex.Token;

public class SymbolTableHandler {
	private SymbolTable globalTable;
	private SymbolTable currentScopeTable;
	
	public boolean createGlobalTable() {
		globalTable = new SymbolTable();
		currentScopeTable = new SymbolTable();
		return true;
	}
	
	public boolean createClassEntryAndTable(Token id) {
		currentScopeTable = globalTable.addRowAndTable(id, VariableKind.CLASS);
		return true;
	}
	
	public boolean createProgramTable(Token id) {
		currentScopeTable = globalTable.addRowAndTable(id, VariableKind.FUNCTION);
		return true;
	}
	
	public boolean createFunctionEntryAndTable(Token type, Token id) {
		currentScopeTable = globalTable.addRowAndTable(type, id, VariableKind.FUNCTION);
		return true;
	}
	
	public boolean createVariableEntry(Token type, Token id, List<Token> arraySizeList) {
		return currentScopeTable.addRow(type, id, arraySizeList, VariableKind.VARIABLE);
	}
	
	public boolean createParameterEntry(Token type, Token id, List<Token> arraySizeList) {
		return currentScopeTable.addRow(type, id, arraySizeList, VariableKind.PARAMETER);
	}

	public void print() {
		Map<String, SymbolTable> tableMap = new LinkedHashMap<String, SymbolTable>();
		System.out.println("Global Table: \n" + globalTable.toString(tableMap));
		for(String tableName: tableMap.keySet()){
			System.out.println("Table: " + tableName + "\n" + tableMap.get(tableName).toString(tableMap));	
		}
		
	}

}
