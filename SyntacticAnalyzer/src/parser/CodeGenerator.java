package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import lex.Constants;
import lex.Token;
import sdt.ArithExpr;
import sdt.ConditionCount;
import sdt.Expression;
import sdt.Factor;
import sdt.IndiceList;
import sdt.Variable;
import smbl.SymbolTable;
import smbl.SymbolTableHandler;
import smbl.SymbolTableRow;
import smbl.VariableKind;
import smbl.VariableType;

public class CodeGenerator {
	protected Writer codeWriterDataDW;
	protected Writer codeWriterDataRES;
	protected Writer currentProgramWriter;
	protected Writer codeWriterProgram;
	protected Writer codeWriterFunction;
	SymbolTableHandler tableHandler;
	private String codeDataDWFileName;
	private String codeDataRESFileName;
	private String codeProgramFileName;
	private String codeFuncFileName;
	private String codeFileName;
	boolean secondPass;
	final int floatSize = 2;
	int tempVarCount;
	int zeroCount;
	int endandCount;
	int ifCount;
	int forCount;
	int funcCount;
	int regCount;
	
	public CodeGenerator(){
		secondPass = false;
	}
	
	public CodeGenerator(String codeFilename, 
			SymbolTableHandler tableHandler) throws UnsupportedEncodingException, FileNotFoundException {
		secondPass = false;
		codeDataDWFileName = "codeDataDW.txt";
		codeDataRESFileName = "codeDataRES.txt";
		codeProgramFileName = "codeProgram.txt";
		codeFuncFileName = "codeFunc.txt";
		this.codeFileName = codeFilename;
		this.tableHandler = tableHandler;
		codeWriterDataDW = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(codeDataDWFileName), "utf-8"));
		codeWriterDataRES = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(codeDataRESFileName), "utf-8"));
		codeWriterProgram = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(codeProgramFileName), "utf-8"));
		currentProgramWriter = codeWriterProgram;
		codeWriterFunction = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(codeFuncFileName), "utf-8"));
		tempVarCount = 0;
		zeroCount = 0;
		endandCount = 0;
		ifCount = 0;
		forCount = 0;
		funcCount = 0;
		regCount = 0;
	}

	public void closeWriter() throws IOException {
		if(secondPass){
			if(codeWriterDataDW != null) codeWriterDataDW.close();
			if(codeWriterDataRES != null) codeWriterDataRES.close();
			if(codeWriterFunction != null) codeWriterFunction.close();
			if(codeWriterProgram != null) codeWriterProgram.close();
			
			currentProgramWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(codeFileName), "utf-8"));
			
			currentProgramWriter.write("\t entry\n");
			BufferedReader br = new BufferedReader(new FileReader(codeProgramFileName));
			String line = null;
			while ((line = br.readLine()) != null) {
				currentProgramWriter.write(line + "\n");
			}
			currentProgramWriter.write("\t hlt\n");
			currentProgramWriter.write("\n");
			br = new BufferedReader(new FileReader(codeFuncFileName));
			while ((line = br.readLine()) != null) {
				currentProgramWriter.write(line + "\n");
			}
			br = new BufferedReader(new FileReader(codeDataDWFileName));
			while ((line = br.readLine()) != null) {
				currentProgramWriter.write(line + "\n");
			}
			br = new BufferedReader(new FileReader(codeDataRESFileName));
			while ((line = br.readLine()) != null) {
				currentProgramWriter.write(line + "\n");
			}
			currentProgramWriter.close();
			new File(codeDataDWFileName).delete();
			new File(codeDataRESFileName).delete();
			new File(codeProgramFileName).delete();
			new File(codeFuncFileName).delete();
//			codeDataDWFileName = "codeDataDW.txt";
//			codeDataRESFileName = "codeDataRES.txt";
//			codeProgramFileName = "codeProgram.txt";
//			codeFuncFileName = "codeFunc.txt";

		}
	}
	
	public void genCodeCreateVariable(Token id, boolean addFuncName) throws IOException {
		SymbolTableRow row = tableHandler.getVariable(id.getValue());		
		if (row.getKind() == VariableKind.VARIABLE) {
			String varName = row.getVarName();
			if(addFuncName){
				varName = tableHandler.getVariableScope(id.getValue()) + varName;
				row.setTempVarName(varName);
			}
			createVariable(varName, row.getType());
		}
	}

	private void createVariable(String varName, VariableType varType) throws IOException {
		int dim = getTotalDimSize(varType.getDimension());
		if (varType.getTypeName().equalsIgnoreCase(Constants.RESERVED_WORD_INT)) {
			if (dim == 0) {
				codeWriterDataDW.write(varName + "\tdw 0\n");
			} else if(dim > 0){
				codeWriterDataRES.write(varName + "\tres " + dim + "\n");
			}
		}else if (varType.getTypeName().equalsIgnoreCase(Constants.RESERVED_WORD_FLOAT)) {
			if (dim == 0) {
				codeWriterDataRES.write(varName + "\tres " + floatSize + "\n");
			} else if(dim > 0){
				codeWriterDataRES.write(varName + "\tres " + dim*floatSize + "\n");
			}
		} else {
			int classDim = getTotalClassSize(varType.getTypeName());
			if(classDim > 0 && dim > 0){
				dim *= classDim;
			} else if(classDim > 0 && dim == 0){
				dim = classDim;
			}
			if (dim == 0) {
				codeWriterDataDW.write(varName + "\tdw 0\n");
			} else if(dim > 0){
				codeWriterDataRES.write(varName + "\tres " + dim + "\n");
			}
		}
	}

	private int getTotalClassSize(String varTypeName) throws IOException {
		int totalDim = 0;
		SymbolTable table = tableHandler.getClass(varTypeName).getLink();
		for(String key : table.tableRowMap.keySet()){
			VariableType varType = table.tableRowMap.get(key).getType();
			if (varType.getTypeName().equalsIgnoreCase(Constants.RESERVED_WORD_INT)) {
				int dim = getTotalDimSize(varType.getDimension());
				if(dim > 0){
					totalDim += dim;
				} else {
					totalDim ++;					
				}
			}else if (varType.getTypeName().equalsIgnoreCase(Constants.RESERVED_WORD_FLOAT)) {
				int dim = getTotalDimSize(varType.getDimension());
				if(dim > 0){
					totalDim += dim*floatSize;
				} else {
					totalDim += floatSize;
				}
			}
		}
		return totalDim;
	}

	private int getTotalDimSize(int[] dimArr) {
		int i = 0;
		int dim = 0;
		if (dimArr != null && dimArr.length > 0) {
			dim = dimArr[i++];
			while (i < dimArr.length) {
				dim *= dimArr[i++];
			}
		}
		return dim;
	}

	public void genCodeAssignment(Token id, IndiceList indiceList1, Token token, IndiceList indiceList2) throws IOException {
		if(token.getType().equalsIgnoreCase(Constants.INTEGERNUM)){
			regCount = 3;
			currentProgramWriter.write("\t sub \t r" + regCount + ",r" + regCount + ",r" + regCount + "\n");
			currentProgramWriter.write("\t addi \t r" + regCount + ",r" + regCount + "," + token.getValue() + "\n");
			storeInIdFromReg(id, indiceList1, "r" + regCount + "");
		} else if(token.getType().equalsIgnoreCase(Constants.ID)){
			loadIdIntoRegister(token, indiceList2, "r" + regCount + "");
			storeInIdFromReg(id, indiceList1, "r" + regCount + "");
		}
		currentProgramWriter.write("\n");
		
	}

	private void storeInIdFromReg(Token id, IndiceList indiceList, String reg) throws IOException {
		if(indiceList != null && indiceList.indice != null){
			loadFactorToR0(indiceList.indice.arithExpr.term.factor);
			currentProgramWriter.write("\t sw \t " + checkTempVarName(id.getValue()) + "(r0)," + reg + "\n");
		} else {
			currentProgramWriter.write("\t sw \t " + checkTempVarName(id.getValue()) + "(r0)," + reg + "\n");
		}
	}

	private void loadIdIntoRegister(Token id, IndiceList indiceList, String reg) throws IOException {
		if(indiceList != null && indiceList.indice != null){
			loadFactorToR0(indiceList.indice.arithExpr.term.factor);
			currentProgramWriter.write("\t lw \t " + reg + "," + checkTempVarName(id.getValue()) + "(r0)\n");
		} else {
			currentProgramWriter.write("\t lw \t " + reg + "," + checkTempVarName(id.getValue()) + "(r0)\n");
		}
	}

	private void loadFactorToR0(Factor factor) throws IOException {
		if(factor.tempVar != null){
			currentProgramWriter.write("\t lw \t r2," + factor.tempVar.getValue() + "(r0)\n");
			currentProgramWriter.write("\t sub \t r1,r1,r1\n");
			currentProgramWriter.write("\t add \t r0,r1,r2\n");
		} else if(factor.upNum != null){
			currentProgramWriter.write("\t sub \t r1,r1,r1\n");
			currentProgramWriter.write("\t addi \t r0,r1," + factor.upNum.getValue() + "\n");
		} else if(factor.upId != null){
			regCount++;
			if(regCount > 14){
				String errMsg = "Code Generation Error: Number of registers exceeded 14 at line: " 
						+ factor.upId.getLineNo()
						+ " position: " 
						+ factor.upId.getPositionInLine();
				tableHandler.getErrWriter().write(errMsg + "\n");
				System.err.println(errMsg);
			}
			loadIdIntoRegister(factor.upId, factor.upIdicesList, "r" + regCount);
			currentProgramWriter.write("\t sub \t r1,r1,r1\n");
			currentProgramWriter.write("\t add \t r0,r1,r" + regCount + "\n");
		}
	}

	private String checkTempVarName(String tokenValue) {
		SymbolTableRow row = tableHandler.getVariable(tokenValue);
		if(row != null && row.getTempVarName() != null){
			tokenValue = row.getTempVarName();
		}
		return tokenValue;
	}

	public void genCodeNotOperation(Factor f1, Token not) throws IOException {
		loadFactorInReg(f1, "r1");
		if(not.getValue().equalsIgnoreCase(Constants.RESERVED_WORD_NOT)){
			currentProgramWriter.write("\t not \t r3,r1\n");
			conditionalBranch(createTempVar(f1, new Factor()));
		}
	}

	public void genCodeOperation(Factor f1, Factor f2, Token op) throws IOException {
		loadFactorInReg(f1, "r1");
		loadFactorInReg(f2, "r2");		
		
		if(op.getType().equalsIgnoreCase(Constants.PLUS)){
			currentProgramWriter.write("\t add \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.MINUS)){
			currentProgramWriter.write("\t sub \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.MULTIPLY)){
			currentProgramWriter.write("\t mul \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.DIV)){
			currentProgramWriter.write("\t div \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.DIV)){
			currentProgramWriter.write("\t div \t r3,r1,r2\n");
		} else if(op.getValue().equalsIgnoreCase(Constants.RESERVED_WORD_AND)){
			currentProgramWriter.write("\t and \t r3,r1,r2\n");
		} else if(op.getValue().equalsIgnoreCase(Constants.RESERVED_WORD_OR)){
			currentProgramWriter.write("\t or \t r3,r1,r2\n");
		}
		
		String tempVar = createTempVar(f1, f2);
		if(op.getValue().equalsIgnoreCase(Constants.RESERVED_WORD_AND)
				|| op.getValue().equalsIgnoreCase(Constants.RESERVED_WORD_OR)){
			conditionalBranch(tempVar);
		} else {
			currentProgramWriter.write("\t sw \t " + tempVar + "(r0),r3\n");			
		}
		currentProgramWriter.write("\n");
	}

	private void conditionalBranch(String tempVar) throws IOException {
		String zero = "zero" + (zeroCount++);
		currentProgramWriter.write("\t bz \t r3," + zero + "\n");
		currentProgramWriter.write("\t addi \t r1,r0,1\n");
		currentProgramWriter.write("\t sw \t " + tempVar + "(r0),r1\n");
		String endand = "endand" + (endandCount++);
		currentProgramWriter.write("\t j \t " + endand + "\n");
		currentProgramWriter.write(zero + "\t sw \t " + tempVar + "(r0), r0\n");
		currentProgramWriter.write(endand + "\n");
	}
	
	private String createTempVar(Factor f1, Factor f2) throws IOException {
		String tempVar = null;
		if(f1.tempVar == null && f2.tempVar == null){
			tempVar = "t" + tempVarCount++;
			Token tempT = new Token();
			tempT.setValue(tempVar);
			tempT.setType(Constants.ID);
			f1.tempVar = tempT;
			codeWriterDataDW.write(tempVar + "\t dw \t 0\n");			
		} else if(f1.tempVar != null){
			tempVar = f1.tempVar.getValue();
		} else if(f2.tempVar != null){
			tempVar = f2.tempVar.getValue();
		}
		return tempVar;
	}
	
	public void genCodeRelOperation(Factor f1, Factor f2, Token op) throws IOException {
		loadFactorInReg(f1, "r1");
		loadFactorInReg(f2, "r2");		
		
		if(op.getType().equalsIgnoreCase(Constants.EQCOMP)){
			currentProgramWriter.write("\t ceq \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.NOTEQ)){
			currentProgramWriter.write("\t cne \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.LT)){
			currentProgramWriter.write("\t clt \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.LESSEQ)){
			currentProgramWriter.write("\t cle \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.GT)){
			currentProgramWriter.write("\t cgt \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.GREATEQ)){
			currentProgramWriter.write("\t cge \t r3,r1,r2\n");
		}
		String tempVar = "t" + tempVarCount++;
		codeWriterDataDW.write(tempVar + "\t dw \t 0\n");			
		currentProgramWriter.write("\t sw \t " + tempVar + "(r0),r3\n");
		currentProgramWriter.write("\n");
		Token tempT = new Token();
		tempT.setValue(tempVar);
		tempT.setType(Constants.ID);
		f1.tempVar = tempT;
	}

	private void loadFactorInReg(Factor f1, String reg) throws IOException {
		if(f1.tempVar != null){
			currentProgramWriter.write("\t lw \t " + reg + "," + f1.tempVar.getValue() + "(r0)\n");			
		} else if(f1.upNum != null){
			if(f1.upNum.getType().equalsIgnoreCase(Constants.INTEGERNUM)){
				currentProgramWriter.write("\t sub \t " + reg + "," + reg + "," + reg + "\n");
				currentProgramWriter.write("\t addi \t " + reg + "," + reg + "," + f1.upNum.getValue() + "\n");			
			}
		} else if(f1.upId != null){
			currentProgramWriter.write("\t lw \t " + reg + "," + checkTempVarName(f1.upId.getValue()) + "(r0)\n");			
		}
	}

	public boolean genCodeIfCondition(Expression expression, ConditionCount cond) throws IOException {
		if(secondPass){
			if(expression.arithExpr != null
					&& expression.arithExpr.term != null
					&& expression.arithExpr.term.factor != null){
				loadFactorInReg(expression.arithExpr.term.factor, "r1");
				cond.count = ifCount;
				String elseStr = "else" + (ifCount++);
				currentProgramWriter.write("\t bz \t r1, " + elseStr + "\n");
				currentProgramWriter.write("\n");
			}
		}
		return true;
	}

	public boolean genCodeIfCondElse(ConditionCount cond) throws IOException {
		if(secondPass){
			String endif = "endif" + cond.count;
			currentProgramWriter.write("\t j \t " + endif + "\n");
			currentProgramWriter.write("else" + cond.count + " \t \n");
		}
		return true;
	}

	public boolean genCodeEndIf(ConditionCount cond) throws IOException {
		if(secondPass){
			currentProgramWriter.write("endif" + cond.count + " \t \n");
		}
		return true;
	}

	public void setSecondPass(boolean secondPass) {
		this.secondPass = secondPass;
	}

	public boolean genCodeForDecl(Token id, Expression expression, ConditionCount condition) throws IOException {
		if(secondPass){
			genCodeCreateVariable(id, false);
			if(id != null){
				if(expression.arithExpr != null
						&& expression.arithExpr.term != null
						&& expression.arithExpr.term.factor != null){
					if(expression.arithExpr.term.factor.tempVar != null){
						genCodeAssignment(id, null, expression.arithExpr.term.factor.tempVar, null);
					} else if(expression.arithExpr.term.factor.upNum != null){
						genCodeAssignment(id, null, expression.arithExpr.term.factor.upNum, null);
					} else if(expression.arithExpr.term.factor.upId != null){
						genCodeAssignment(id, null, expression.arithExpr.term.factor.upId,
								expression.arithExpr.term.factor.upIdicesList);
					}
				}
			}
			condition.count = forCount++;
			currentProgramWriter.write("gofor" + condition.count + " \t \n");
		}
		return true;
	}

	public boolean genCodeForCond(ArithExpr arithExpr, Token relOp, Expression expression1, ConditionCount condition) throws IOException {
		if(secondPass){
			Factor f1 = null;
			Factor f2 = null;
			if(arithExpr.term != null
					&& arithExpr.term.factor != null){
				f1 = arithExpr.term.factor;
			}
			if(expression1.arithExpr != null
					&& expression1.arithExpr.term != null
					&& expression1.arithExpr.term.factor != null){
				f2 = expression1.arithExpr.term.factor;
			}
			if(f1!= null && f2 != null){
				genCodeRelOperation(f1, f2, relOp);
			}
			loadFactorInReg(f1, "r1");
			String elseStr = "endfor" + condition.count;
			currentProgramWriter.write("\t bz \t r1, " + elseStr + "\n");
			currentProgramWriter.write("\t j \t " + "forstat" + condition.count + "\n");
			currentProgramWriter.write("goforIncr" + condition.count + " \t \n");

		}
		return true;
	}

	public boolean genCodeForEnd(ConditionCount condition) throws IOException {
		if(secondPass){
			currentProgramWriter.write("\t j \t " + "goforIncr" + condition.count + "\n");
			currentProgramWriter.write("endfor" + condition.count + " \t \n");
		}
		return true;
	}

	public boolean genCodeForIncr(ConditionCount condition) throws IOException {
		if(secondPass){
			currentProgramWriter.write("\t j \t " + "gofor" + condition.count + "\n");
			currentProgramWriter.write("forstat" + condition.count + " \t \n");
		}
		return true;
	}

	public void genCodePut(Expression expression) throws IOException {
		if(secondPass){
			if(expression.arithExpr != null
					&& expression.arithExpr.term != null
					&& expression.arithExpr.term.factor != null){
				loadFactorInReg(expression.arithExpr.term.factor, "r1");
				currentProgramWriter.write("\t putc \t r1\n");
			}
		}
	}

	public void genCodeGet(Variable variable) throws IOException {
		if(secondPass){
			if(variable.upIdnest != null
					&& variable.upIdnest.id != null){
				currentProgramWriter.write("\t getc \t r1\n");
				currentProgramWriter.write("\t sw \t " + variable.upIdnest.id.getValue() + "(r0),r1\n");
				currentProgramWriter.write("\n");
			}
		}		
	}

	public boolean genCodeCreateFunction(Token id) throws IOException {
		if(secondPass){
			SymbolTableRow row = tableHandler.getFunction(id.getValue());
			if (row.getKind() == VariableKind.FUNCTION) {
				createVariable(row.getVarName() + "res", row.getType());
				codeWriterFunction.write(row.getVarName());
				currentProgramWriter = codeWriterFunction;
			}
		}
		return true;
	}

	public boolean genCodeCreateParameter(Token functionId, Token id, ConditionCount count) throws IOException {
		if(secondPass){
			SymbolTableRow row = tableHandler.getVariable(id.getValue());
			if (row.getKind() == VariableKind.PARAMETER) {
				String paramName = functionId.getValue() + id.getValue();
				row.setTempVarName(paramName);
				createVariable(paramName, row.getType());
				int regCount = count.count + 2;
				if(regCount > 14){
					String errMsg = "Number of parameters cannot be more than 14 at line: " 
							+ id.getLineNo()  
							+ " position: " 
							+ id.getPositionInLine();
					tableHandler.getErrWriter().write(errMsg + "\n");
					System.err.println(errMsg);
					return false;
				}
				String instr = "\t sw \t " + paramName + "(r0),r" + regCount + "\n";
				codeWriterFunction.write(instr);
				count.count++;
			}
		}
		return true;
	}

	public void genCodeReturn(Expression expression) throws IOException {
		if(secondPass){
			if(expression.arithExpr != null
					&& expression.arithExpr.term != null
					&& expression.arithExpr.term.factor != null){
				String funcName = tableHandler.currentFunctionName();
				loadFactorInReg(expression.arithExpr.term.factor, "r1");
				currentProgramWriter.write("\t sw \t " + funcName + "res(r0),r1\n");
				currentProgramWriter.write("\t jr \t r15\n");
				currentProgramWriter = codeWriterProgram;
			}
		}
	}

	public void genCodeFuncCall(Token functionId, List<Expression> exprList, Token tempVarToken) throws IOException {
		if(secondPass){
			int r = 2;
			for(Expression expression: exprList){
				if(expression.arithExpr != null
						&& expression.arithExpr.term != null
						&& expression.arithExpr.term.factor != null){
					String reg = "r" + r;
					loadFactorInReg(expression.arithExpr.term.factor, reg);
				}
				r++;
			}
			currentProgramWriter.write("\t jl \t r15," + functionId.getValue() + "\n");
			String tempVar = null;
			tempVar = "t" + tempVarCount++;
			tempVarToken.setValue(tempVar);
			tempVarToken.setType(Constants.ID);
			codeWriterDataDW.write(tempVar + "\t dw \t 0\n");			

			currentProgramWriter.write("\t sw \t " + tempVar + "(r0),r1\n");
		}		
	}
}
