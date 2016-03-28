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
	private SymbolTable classTableScope;
	private SymbolTable currentClassVariable;
	private String currentClassName;
	private Writer errWriter;
	private Writer tableWriter;
	private boolean secondPass;
	
	public SymbolTableHandler(boolean secondPass, Writer errWriter) throws UnsupportedEncodingException, FileNotFoundException {
		this.secondPass = secondPass;
		this.errWriter = errWriter;
		tableWriter = new BufferedWriter(
				new OutputStreamWriter(
				new FileOutputStream("symbolTables.txt"), "utf-8"));
	}
	
	public void closeWriter() throws IOException {
		tableWriter.close();
	}

	public boolean createGlobalTable() {
		if(!secondPass){
			globalTable = new SymbolTable("Global");
			currentTableScope = globalTable;
			functionTableScope = globalTable;			
		}
		return true;
	}
	
	public boolean createClassEntryAndTable(Token id) throws IOException {
		boolean toReturn = false;
		if(!secondPass && globalTable.tableRowMap.containsKey(id.getValue())) {
			String errMsg = "Class Id: " + id.getValue() + " already exists at line: " 
					+ id.getLineNo()  
					+ " position: " 
					+ id.getPositionInLine();
			errWriter.write(errMsg + "\n");
			System.err.println(errMsg);
		} else {
			currentTableScope = globalTable.addRowAndTable(id, VariableKind.CLASS);
			functionTableScope = currentTableScope;
			classTableScope = currentTableScope;
			toReturn = true;
		}
		return toReturn;
	}
	
	public boolean createProgramTable(Token id) throws IOException {
		
		boolean toReturn = false;
		if(!secondPass && globalTable.tableRowMap.containsKey(id.getValue())) {
			String errMsg = "Function name " + id.getValue() + " already exists at line: " 
					+ id.getLineNo()  
					+ " position: " 
					+ id.getPositionInLine();
			errWriter.write(errMsg + "\n");
			System.err.println(errMsg);
		} else {
			currentTableScope = globalTable.addRowAndTable(id, VariableKind.FUNCTION);
			functionTableScope = globalTable;
			classTableScope = null;
			toReturn = true;
		}
		return toReturn;
	}
	
	public boolean createFunctionEntryAndTable(Token type, Token id) throws IOException {
		
		boolean toReturn = false;
		if(!secondPass && functionTableScope.tableRowMap.containsKey(id.getValue())) {
			String errMsg = "Function name " + id.getValue() + " already exists at line: " 
					+ id.getLineNo()  
					+ " position: " 
					+ id.getPositionInLine();
			errWriter.write(errMsg + "\n");
			System.err.println(errMsg);
		} else {
			currentTableScope = functionTableScope.addRowAndTable(type, id, VariableKind.FUNCTION);
			toReturn = true;
		}
		return toReturn;		
	}
	
	public boolean createVariableEntry(Token type, Token id, List<Token> arraySizeList) throws IOException {
		
		boolean toReturn = false;
		if(!secondPass && currentTableScope.tableRowMap.containsKey(id.getValue())) {
			String errMsg = "Variable " + id.getValue() + " already exists at line: " 
					+ id.getLineNo()  
					+ " position: " 
					+ id.getPositionInLine();
			errWriter.write(errMsg + "\n");
			System.err.println(errMsg);
		} else {
			currentTableScope.addRow(type, id, arraySizeList, VariableKind.VARIABLE);
			toReturn = true;
		}
		return toReturn;
		
	}
	
	public boolean createParameterEntry(Token type, Token id, List<Token> arraySizeList) throws IOException {
		boolean toReturn = false;
		if(!secondPass && currentTableScope.tableRowMap.containsKey(id.getValue())) {
			String errMsg = "Parameter " + id.getValue() + " already exists at line: " 
					+ id.getLineNo()  
					+ " position: " 
					+ id.getPositionInLine();
			errWriter.write(errMsg + "\n");
			System.err.println(errMsg);
		} else {
			currentTableScope.addRow(type, id, arraySizeList, VariableKind.PARAMETER);
			toReturn = true;
		}
		return toReturn;
	}

	public boolean checkClassExists(Token type) throws IOException {
		boolean toReturn = false;
		if(secondPass && !globalTable.tableRowMap.containsKey(type.getValue())) {
			String errMsg = "Type " + type.getValue() + " does not exist at line: " 
					+ type.getLineNo()  
					+ " position: " 
					+ type.getPositionInLine();
			errWriter.write(errMsg + "\n");
			System.err.println(errMsg);
		} else {
			toReturn = true;
		}
		return toReturn;
	}
	
	public boolean checkVariableExists(Token id) throws IOException {
		boolean toReturn = false;
		if(secondPass && !currentTableScope.tableRowMap.containsKey(id.getValue()) 
				&& !existsInClassTableScope(id.getValue())
				&& !functionInGlobalTableExists(id.getValue())) {
			String errMsg = "Variable " + id.getValue() + " not declared at line: " 
					+ id.getLineNo()  
					+ " position: " 
					+ id.getPositionInLine();
			errWriter.write(errMsg + "\n");
			System.err.println(errMsg);
		} else {
			toReturn = true;
			if(secondPass){
				SymbolTableRow row = null;
				if(currentTableScope.tableRowMap.containsKey(id.getValue())){
					row = currentTableScope.tableRowMap.get(id.getValue());
				} else if(existsInClassTableScope(id.getValue())){
					row = classTableScope.tableRowMap.get(id.getValue());
				}
				if(row != null){
					String typeName = row.getTypeList().get(0).getTypeName();
					if(globalTable.tableRowMap.containsKey(typeName)){
						row = globalTable.tableRowMap.get(typeName);
						if(row.getKind() == VariableKind.CLASS){
							currentClassName = typeName;
							currentClassVariable = globalTable.tableRowMap.get(typeName).getLink();
						}
					}
				}
			}
		}
		return toReturn;
	}
	
	private boolean functionInGlobalTableExists(String name){
		boolean toReturn = false;
		if(globalTable.tableRowMap.containsKey(name)){
			SymbolTableRow row = globalTable.tableRowMap.get(name);
			if(row.getKind() == VariableKind.FUNCTION){
				toReturn = true;
			}
		}
		return toReturn;
	}
	
	public boolean checkVariableInClassExists(Token id) throws IOException {
		boolean toReturn = false;
		if(secondPass){
			if(currentClassVariable == null){
				String errMsg = "Variable " + id.getValue() + " is not a class variable at line: " 
						+ id.getLineNo()  
						+ " position: " 
						+ id.getPositionInLine();
				errWriter.write(errMsg + "\n");
				System.err.println(errMsg);
			} else if(!currentClassVariable.tableRowMap.containsKey(id.getValue())) {
				String errMsg = "Variable " + id.getValue() + " does not exist in class " + currentClassName + " at line: " 
						+ id.getLineNo()  
						+ " position: " 
						+ id.getPositionInLine();
				errWriter.write(errMsg + "\n");
				System.err.println(errMsg);
			} else {
				toReturn = true;
				if(currentClassVariable != null){
					SymbolTableRow row = currentClassVariable.tableRowMap.get(id.getValue());
					if(row != null){
						String typeName = row.getTypeList().get(0).getTypeName();
						if(globalTable.tableRowMap.containsKey(typeName)){
							row = globalTable.tableRowMap.get(typeName);
							if(row.getKind() == VariableKind.CLASS){
								currentClassName = typeName;
								currentClassVariable = globalTable.tableRowMap.get(typeName).getLink();
							}
						} else {
							currentClassName = null;
							currentClassVariable = null;					
						}
					}
				}
			} 
		} else {
			toReturn = true;
		}
		
		return toReturn;
	}
	
	private boolean existsInClassTableScope(String key) {
		boolean exists = false;
		if(classTableScope != null 
				&& classTableScope.tableRowMap.containsKey(key)){
			exists = true;
		}
		return exists;
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

	public void setSecondPass(boolean secondPass) {
		this.secondPass = secondPass;
	}

	public void setErrWriter(Writer errWriter) {
		this.errWriter = errWriter;
	}

	public boolean delFuncTable(Token funcId) throws IOException {
		if(secondPass && functionTableScope.tableRowMap.containsKey(funcId.getValue())){
			SymbolTableRow row = functionTableScope.tableRowMap.get(funcId.getValue());
			print(row.getLink());
			row.setLink(null);
		}
		return true;
	}

}
