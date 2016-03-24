package smbl;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lex.Token;

public class SymbolTableHandler {
	private SymbolTable globalTable;
	private SymbolTable currentTableScope;
	private SymbolTable functionTableScope;
	private Writer errWriter;
	private Writer tableWriter;
	
	public SymbolTableHandler(Writer errWriter) throws UnsupportedEncodingException, FileNotFoundException {
		this.errWriter = errWriter;
		tableWriter = new BufferedWriter(
				new OutputStreamWriter(
				new FileOutputStream("symbolTables.txt"), "utf-8"));
	}
	
	public void closeWriter() throws IOException {
		tableWriter.close();
	}

	public boolean createGlobalTable() {
		globalTable = new SymbolTable("Global");
		currentTableScope = globalTable;
		functionTableScope = globalTable;
		return true;
	}
	
	public boolean createClassEntryAndTable(Token id) throws IOException {
		boolean toReturn = false;
		if(globalTable.tableRowMap.containsKey(id.getValue())) {
			String errMsg = "Class Id: " + id.getValue() + " already exists at line: " 
					+ id.getLineNo()  
					+ " position: " 
					+ id.getPositionInLine()
					+ "\n";
			errWriter.write(errMsg);
			System.err.println(errMsg);
		} else {
			currentTableScope = globalTable.addRowAndTable(id, VariableKind.CLASS);
			functionTableScope = currentTableScope;
			toReturn = true;
		}
		return toReturn;
	}
	
	public boolean createProgramTable(Token id) throws IOException {
		
		boolean toReturn = false;
		if(globalTable.tableRowMap.containsKey(id.getValue())) {
			String errMsg = "Function name " + id.getValue() + " already exists at line: " 
					+ id.getLineNo()  
					+ " position: " 
					+ id.getPositionInLine()
					+ "\n";
			errWriter.write(errMsg);
			System.err.println(errMsg);
		} else {
			currentTableScope = globalTable.addRowAndTable(id, VariableKind.FUNCTION);
			functionTableScope = globalTable;
			toReturn = true;
		}
		return toReturn;
	}
	
	public boolean createFunctionEntryAndTable(Token type, Token id) throws IOException {
		
		boolean toReturn = false;
		if(functionTableScope.tableRowMap.containsKey(id.getValue())) {
			String errMsg = "Function name " + id.getValue() + " already exists at line: " 
					+ id.getLineNo()  
					+ " position: " 
					+ id.getPositionInLine()
					+ "\n";
			errWriter.write(errMsg);
			System.err.println(errMsg);
		} else {
			currentTableScope = functionTableScope.addRowAndTable(type, id, VariableKind.FUNCTION);
			toReturn = true;
		}
		return toReturn;		
	}
	
	public boolean createVariableEntry(Token type, Token id, List<Token> arraySizeList) throws IOException {
		
		boolean toReturn = false;
		if(currentTableScope.tableRowMap.containsKey(id.getValue())) {
			String errMsg = "Variable " + id.getValue() + " already exists at line: " 
					+ id.getLineNo()  
					+ " position: " 
					+ id.getPositionInLine()
					+ "\n";
			errWriter.write(errMsg);
			System.err.println(errMsg);
		} else {
			currentTableScope.addRow(type, id, arraySizeList, VariableKind.VARIABLE);
			toReturn = true;
		}
		return toReturn;
		
	}
	
	public boolean createParameterEntry(Token type, Token id, List<Token> arraySizeList) throws IOException {
		boolean toReturn = false;
		if(currentTableScope.tableRowMap.containsKey(id.getValue())) {
			String errMsg = "Parameter " + id.getValue() + " already exists at line: " 
					+ id.getLineNo()  
					+ " position: " 
					+ id.getPositionInLine()
					+ "\n";
			errWriter.write(errMsg);
			System.err.println(errMsg);
		} else {
			currentTableScope.addRow(type, id, arraySizeList, VariableKind.PARAMETER);
			toReturn = true;
		}
		return toReturn;
	}

	public void print(SymbolTable table) throws IOException {
		Map<String, SymbolTable> tableMap = new LinkedHashMap<String, SymbolTable>();
		tableWriter.write("Table: " + table.toString(tableMap) + "\n");
		for(String tableName: tableMap.keySet()){
			print(tableMap.get(tableName));	
		}
	}
	
	public SymbolTable getGlobalTable() {
		return globalTable;
	}

}
