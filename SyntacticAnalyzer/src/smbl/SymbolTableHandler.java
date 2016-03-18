package smbl;

import lex.Token;

public class SymbolTableHandler {
	private SymbolTable globalTable;
	
	public boolean createGlobalTable() {
		globalTable = new SymbolTable();
		return true;
	}
	
	public boolean createClassEntryAndTable(Token token) {
		globalTable.addRowAndTable(token, VariableKind.CLASS);
		return true;
	}
	
	public boolean createProgramTable(Token token) {
		globalTable.addRowAndTable(token, VariableKind.FUNCTION);
		return true;
	}
	
	public boolean createFunctionEntryAndTable(Token token) {
		globalTable.addRowAndTable(token, VariableKind.FUNCTION);
		return true;
	}
	
	public boolean createVariableEntry(Token token) {
		globalTable.addRowAndTable(token, VariableKind.VARIABLE);
		return true;
	}
	
	public boolean createParameterEntry(Token token) {
		globalTable.addRowAndTable(token, VariableKind.PARAMETER);
		return true;
	}

}
