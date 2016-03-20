package smbl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lex.Token;

public class SymbolTableHandler {
	private SymbolTable globalTable;
	private SymbolTable currentTableScope;
	private SymbolTable functionTableScope;
	
	public boolean createGlobalTable() {
		globalTable = new SymbolTable("Global");
		currentTableScope = globalTable;
		functionTableScope = globalTable;
		return true;
	}
	
	public boolean createClassEntryAndTable(Token id) {
		currentTableScope = globalTable.addRowAndTable(id, VariableKind.CLASS);
		functionTableScope = currentTableScope;
		return true;
	}
	
	public boolean createProgramTable(Token id) {
		currentTableScope = globalTable.addRowAndTable(id, VariableKind.FUNCTION);
		functionTableScope = globalTable;
		return true;
	}
	
	public boolean createFunctionEntryAndTable(Token type, Token id) {
		currentTableScope = functionTableScope.addRowAndTable(type, id, VariableKind.FUNCTION);
		return true;
	}
	
	public boolean createVariableEntry(Token type, Token id, List<Token> arraySizeList) {
		return currentTableScope.addRow(type, id, arraySizeList, VariableKind.VARIABLE);
	}
	
	public boolean createParameterEntry(Token type, Token id, List<Token> arraySizeList) {
		return currentTableScope.addRow(type, id, arraySizeList, VariableKind.PARAMETER);
	}

	public void print(SymbolTable table) {
		Map<String, SymbolTable> tableMap = new LinkedHashMap<String, SymbolTable>();
		System.out.println("Table: " + table.toString(tableMap));
		for(String tableName: tableMap.keySet()){
			print(tableMap.get(tableName));	
		}
	}
	
	public SymbolTable getGlobalTable() {
		return globalTable;
	}

}
