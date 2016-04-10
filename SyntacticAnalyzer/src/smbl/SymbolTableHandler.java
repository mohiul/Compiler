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

import lex.Constants;
import lex.Token;
import sdt.Factor;
import sdt.Type;

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
	
	public SymbolTableHandler() {}
	
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

	public boolean checkVariableExists(Token id, Type type) throws IOException {
		return checkVariableExists(id, false, 0, type);
	}
	
	public boolean checkVariableExists(Token id, int noOfDim, Type type) throws IOException {
		return checkVariableExists(id, true, noOfDim, type);
	}
	
	public boolean checkVariableExists(Token id, boolean checkDim, int noOfDim, Type typeToReturn) throws IOException {
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
					VariableType type = row.getTypeList().get(0);
					if(checkDim){
						int dimLength = 0;
						if(type.getDimension() != null){
							dimLength = type.getDimension().length;
						}
						if(dimLength != noOfDim){
							String errMsg = "Variable " + id.getValue() + " incorrect array dimension size at line: " 
									+ id.getLineNo()  
									+ " position: " 
									+ id.getPositionInLine();
							errWriter.write(errMsg + "\n");
							System.err.println(errMsg);
							toReturn = false;
						}						
					}
					String typeName = type.getTypeName();
					typeToReturn.typeName = typeName;
					if(globalTable.tableRowMap.containsKey(typeName)){
						row = globalTable.tableRowMap.get(typeName);
						if(row.getKind() == VariableKind.CLASS){
							currentClassName = typeName;
							currentClassVariable = globalTable.tableRowMap.get(typeName).getLink();
						}
					}
				}
				if(functionInGlobalTableExists(id.getValue())){
					typeToReturn.typeName = globalTable.tableRowMap.get(id.getValue()).getTypeList().get(0).getTypeName();
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
//	TODO Remove parentId
	public boolean checkVariableInClassExists(Token parentId, Token id, int noOfDim, Type type) throws IOException {
		boolean toReturn = false;
		if(secondPass){
			if(currentClassVariable == null){
				updateCurrentClassTable(parentId);
			}
			
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
						VariableType varType = row.getTypeList().get(0);
						int dimLength = 0;
						if(varType.getDimension() != null){
							dimLength = varType.getDimension().length;
						}
						if(dimLength != noOfDim){
							String errMsg = "Variable " + id.getValue() + " incorrect array dimension size at line: " 
									+ id.getLineNo()  
									+ " position: " 
									+ id.getPositionInLine();
							errWriter.write(errMsg + "\n");
							System.err.println(errMsg);
							toReturn = false;
						}
						String typeName = varType.getTypeName();
						type.typeName = typeName;
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

	private void updateCurrentClassTable(Token parentId) {
		SymbolTableRow row = null;
		if(currentTableScope.tableRowMap.containsKey(parentId.getValue())){
			row = currentTableScope.tableRowMap.get(parentId.getValue());
		} else if(existsInClassTableScope(parentId.getValue())){
			row = classTableScope.tableRowMap.get(parentId.getValue());
		}
		
		if(row != null){
			VariableType type = row.getTypeList().get(0);
			String typeName = type.getTypeName();
			if(globalTable.tableRowMap.containsKey(typeName)){
				row = globalTable.tableRowMap.get(typeName);
				if(row.getKind() == VariableKind.CLASS){
					currentClassName = typeName;
					currentClassVariable = globalTable.tableRowMap.get(typeName).getLink();
				}
			}
		}
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

	public SymbolTableRow getVariable(String value) {
		return currentTableScope.tableRowMap.get(value);
		
	}

	public boolean checkCompatableType(Type type1, Type type2, Token multOp) throws IOException {
		boolean toReturn = false;
		if(secondPass){
			String typeName1 = type1.typeName;
			if(typeName1.equalsIgnoreCase(Constants.INTEGERNUM)) typeName1 = Constants.RESERVED_WORD_INT;
			if(typeName1.equalsIgnoreCase(Constants.FLOATNUM)) typeName1 = Constants.RESERVED_WORD_FLOAT;
			
			String typeName2 = type2.typeName;
			if(typeName2.equalsIgnoreCase(Constants.INTEGERNUM)) typeName2 = Constants.RESERVED_WORD_INT;
			if(typeName2.equalsIgnoreCase(Constants.FLOATNUM)) typeName2 = Constants.RESERVED_WORD_FLOAT;
			
			if((typeName1.equalsIgnoreCase(Constants.RESERVED_WORD_INT)
					&& typeName2.equalsIgnoreCase(Constants.RESERVED_WORD_INT))
					|| (typeName1.equalsIgnoreCase(Constants.RESERVED_WORD_FLOAT)
							&& typeName2.equalsIgnoreCase(Constants.RESERVED_WORD_FLOAT))){
				toReturn = true;
			}
			if(!toReturn){
				String errMsg = "Type compatibility error for " + multOp.getValue() + " at line: " 
						+ multOp.getLineNo()  
						+ " position: " 
						+ multOp.getPositionInLine();
				errWriter.write(errMsg + "\n");
				System.err.println(errMsg);
			}			
		} else {
			toReturn = true;
		}
		return toReturn;
	}
}
