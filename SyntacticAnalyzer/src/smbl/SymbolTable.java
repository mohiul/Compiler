package smbl;

import java.util.HashMap;
import java.util.Map;

import lex.Token;

public class SymbolTable {
	public Map<String, SymbolTableRow> tableMap;

	public SymbolTable() {
		tableMap = new HashMap<String, SymbolTableRow>();
	}

	public boolean addRowAndTable(Token token, VariableKind kind) {
		String name = token.getValue();
		SymbolTableRow row = new SymbolTableRow(name);
		row.setKind(kind);
		row.setLink(new SymbolTable());
		tableMap.put(name, row);
		return true;
	}
}
