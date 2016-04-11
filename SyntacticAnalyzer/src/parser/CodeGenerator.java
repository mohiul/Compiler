package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import lex.Constants;
import lex.Token;
import sdt.Expression;
import sdt.Factor;
import sdt.Variable;
import sdt.ArithExpr;
import sdt.ConditionCount;
import smbl.SymbolTableHandler;
import smbl.SymbolTableRow;
import smbl.VariableKind;
import smbl.VariableType;

public class CodeGenerator {
	protected Writer codeWriterData;
	protected Writer codeWriterProgram;
	SymbolTableHandler tableHandler;
	private String codeDataFileName;
	private String codeProgramFileName;
	private String codeFileName;
	boolean secondPass;
	int tempVarCount;
	int zeroCount;
	int endandCount;
	int ifCount;
	int forCount;
	
	public CodeGenerator(){
		secondPass = false;
	}
	
	public CodeGenerator(String codeFilename, 
			SymbolTableHandler tableHandler) throws UnsupportedEncodingException, FileNotFoundException {
		secondPass = false;
		codeDataFileName = "codeData.txt";
		codeProgramFileName = "codeProgram.txt";
		this.codeFileName = codeFilename;
		this.tableHandler = tableHandler;
		codeWriterData = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(codeDataFileName), "utf-8"));
		codeWriterProgram = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(codeProgramFileName), "utf-8"));
		tempVarCount = 0;
		zeroCount = 0;
		endandCount = 0;
		ifCount = 0;
		forCount = 0;
	}

	public void closeWriter() throws IOException {
		if(secondPass){
			if(codeWriterData != null) codeWriterData.close();
			if(codeWriterProgram != null) codeWriterProgram.close();
			
			codeWriterProgram = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(codeFileName), "utf-8"));
			
			codeWriterProgram.write("\t entry\n");
			BufferedReader br = new BufferedReader(new FileReader(codeProgramFileName));
			String line = null;
			while ((line = br.readLine()) != null) {
				codeWriterProgram.write(line + "\n");
			}
			codeWriterProgram.write("\t hlt\n");
			codeWriterProgram.write("\n");
			br = new BufferedReader(new FileReader(codeDataFileName));
			while ((line = br.readLine()) != null) {
				codeWriterProgram.write(line + "\n");
			}
			codeWriterProgram.close();
		}
	}
	
	public void genCodeCreateVariable(Token id) throws IOException {
		SymbolTableRow row = tableHandler.getVariable(id.getValue());
		if (row.getKind() == VariableKind.VARIABLE) {
			VariableType varType = row.getType();
			if (varType.getTypeName().equalsIgnoreCase(Constants.RESERVED_WORD_INT)) {
				int[] dimArr = varType.getDimension();
				if (dimArr == null || dimArr.length == 0) {
					codeWriterData.write(row.getVarName() + "\tdw 0\n");
				} else {
					int i = 0;
					int dim = dimArr[i++];
					while (i < dimArr.length) {
						dim *= dimArr[i++];
					}
					codeWriterData.write(row.getVarName() + "\tres " + dim + "\n");
				}
			}
		} else {
			System.err.println("Variable " + row.getVarName() + " TypeList should be > 0");
		}
	}

	public void genCodeAssignment(Token id, Token token) throws IOException {
		if(token.getType().equalsIgnoreCase(Constants.INTEGERNUM)){
			codeWriterProgram.write("\t sub \t r1,r1,r1\n");
			codeWriterProgram.write("\t addi \t r1,r1," + token.getValue() + "\n");
			codeWriterProgram.write("\t sw \t " + id.getValue() + "(r0),r1\n");
		} else if(token.getType().equalsIgnoreCase(Constants.ID)){
			codeWriterProgram.write("\t lw \t r1," + token.getValue() + "(r0)\n");
			codeWriterProgram.write("\t sw \t " + id.getValue() + "(r0),r1\n");
//			lw r1,b(r0)
//			sw a(r0),r1
		}
		codeWriterProgram.write("\n");
		
	}

	public void genCodeNotOperation(Factor f1, Token not) throws IOException {
		loadFactorInReg(f1, "r1");
		if(not.getValue().equalsIgnoreCase(Constants.RESERVED_WORD_NOT)){
			codeWriterProgram.write("\t not \t r3,r1\n");
			conditionalBranch(createTempVar(f1, new Factor()));
		}
	}

	public void genCodeOperation(Factor f1, Factor f2, Token op) throws IOException {
		loadFactorInReg(f1, "r1");
		loadFactorInReg(f2, "r2");		
		
		if(op.getType().equalsIgnoreCase(Constants.PLUS)){
			codeWriterProgram.write("\t add \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.MINUS)){
			codeWriterProgram.write("\t sub \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.MULTIPLY)){
			codeWriterProgram.write("\t mul \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.DIV)){
			codeWriterProgram.write("\t div \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.DIV)){
			codeWriterProgram.write("\t div \t r3,r1,r2\n");
		} else if(op.getValue().equalsIgnoreCase(Constants.RESERVED_WORD_AND)){
			codeWriterProgram.write("\t and \t r3,r1,r2\n");
		} else if(op.getValue().equalsIgnoreCase(Constants.RESERVED_WORD_OR)){
			codeWriterProgram.write("\t or \t r3,r1,r2\n");
		}
		
		String tempVar = createTempVar(f1, f2);
		if(op.getValue().equalsIgnoreCase(Constants.RESERVED_WORD_AND)
				|| op.getValue().equalsIgnoreCase(Constants.RESERVED_WORD_OR)){
			conditionalBranch(tempVar);
		} else {
			codeWriterProgram.write("\t sw \t " + tempVar + "(r0),r3\n");			
		}
		codeWriterProgram.write("\n");
	}

	private void conditionalBranch(String tempVar) throws IOException {
		String zero = "zero" + (zeroCount++);
		codeWriterProgram.write("\t bz \t r3," + zero + "\n");
		codeWriterProgram.write("\t addi \t r1,r0,1\n");
		codeWriterProgram.write("\t sw \t " + tempVar + "(r0),r1\n");
		String endand = "endand" + (endandCount++);
		codeWriterProgram.write("\t j \t " + endand + "\n");
		codeWriterProgram.write(zero + "\t sw \t " + tempVar + "(r0), r0\n");
		codeWriterProgram.write(endand + "\n");
	}
	
	private String createTempVar(Factor f1, Factor f2) throws IOException {
		String tempVar = null;
		if(f1.tempVar == null && f2.tempVar == null){
			tempVar = "t" + tempVarCount++;
			Token tempT = new Token();
			tempT.setValue(tempVar);
			tempT.setType(Constants.ID);
			f1.tempVar = tempT;
			codeWriterData.write(tempVar + "\t dw \t 0\n");			
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
			codeWriterProgram.write("\t ceq \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.NOTEQ)){
			codeWriterProgram.write("\t cne \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.LT)){
			codeWriterProgram.write("\t clt \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.LESSEQ)){
			codeWriterProgram.write("\t cle \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.GT)){
			codeWriterProgram.write("\t cgt \t r3,r1,r2\n");
		} else if(op.getType().equalsIgnoreCase(Constants.GREATEQ)){
			codeWriterProgram.write("\t cge \t r3,r1,r2\n");
		}
		String tempVar = "t" + tempVarCount++;
		codeWriterData.write(tempVar + "\t dw \t 0\n");			
		codeWriterProgram.write("\t sw \t " + tempVar + "(r0),r3\n");
		codeWriterProgram.write("\n");
		Token tempT = new Token();
		tempT.setValue(tempVar);
		tempT.setType(Constants.ID);
		f1.tempVar = tempT;
	}

	private void loadFactorInReg(Factor f1, String reg) throws IOException {
		if(f1.tempVar != null){
			codeWriterProgram.write("\t lw \t " + reg + "," + f1.tempVar.getValue() + "(r0)\n");			
		} else if(f1.upNum != null){
			if(f1.upNum.getType().equalsIgnoreCase(Constants.INTEGERNUM)){
				codeWriterProgram.write("\t sub \t " + reg + "," + reg + "," + reg + "\n");
				codeWriterProgram.write("\t addi \t " + reg + "," + reg + "," + f1.upNum.getValue() + "\n");			
			}
		} else if(f1.upId != null){
			codeWriterProgram.write("\t lw \t " + reg + "," + f1.upId.getValue() + "(r0)\n");			
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
				codeWriterProgram.write("\t bz \t r1, " + elseStr + "\n");
				codeWriterProgram.write("\n");
			}
		}
		return true;
	}

	public boolean genCodeIfCondElse(ConditionCount cond) throws IOException {
		if(secondPass){
			String endif = "endif" + cond.count;
			codeWriterProgram.write("\t j \t " + endif + "\n");
			codeWriterProgram.write("else" + cond.count + " \t \n");
		}
		return true;
	}

	public boolean genCodeEndIf(ConditionCount cond) throws IOException {
		if(secondPass){
			codeWriterProgram.write("endif" + cond.count + " \t \n");
		}
		return true;
	}

	public void setSecondPass(boolean secondPass) {
		this.secondPass = secondPass;
	}

	public boolean genCodeForDecl(Token id, Expression expression, ConditionCount condition) throws IOException {
		if(secondPass){
			genCodeCreateVariable(id);
			if(id != null){
				if(expression.arithExpr != null
						&& expression.arithExpr.term != null
						&& expression.arithExpr.term.factor != null){
					if(expression.arithExpr.term.factor.tempVar != null){
						genCodeAssignment(id, expression.arithExpr.term.factor.tempVar);
					} else if(expression.arithExpr.term.factor.upNum != null){
						genCodeAssignment(id, expression.arithExpr.term.factor.upNum);
					} else if(expression.arithExpr.term.factor.upId != null){
						genCodeAssignment(id, expression.arithExpr.term.factor.upId);
					}
				}
			}
			condition.count = forCount++;
			codeWriterProgram.write("gofor" + condition.count + " \t \n");
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
			codeWriterProgram.write("\t bz \t r1, " + elseStr + "\n");
			codeWriterProgram.write("\t j \t " + "forstat" + condition.count + "\n");
			codeWriterProgram.write("goforIncr" + condition.count + " \t \n");

		}
		return true;
	}

	public boolean genCodeForEnd(ConditionCount condition) throws IOException {
		if(secondPass){
			codeWriterProgram.write("\t j \t " + "goforIncr" + condition.count + "\n");
			codeWriterProgram.write("endfor" + condition.count + " \t \n");
		}
		return true;
	}

	public boolean genCodeForIncr(ConditionCount condition) throws IOException {
		if(secondPass){
			codeWriterProgram.write("\t j \t " + "gofor" + condition.count + "\n");
			codeWriterProgram.write("forstat" + condition.count + " \t \n");
		}
		return true;
	}

	public void genCodePut(Expression expression) throws IOException {
		if(secondPass){
			if(expression.arithExpr != null
					&& expression.arithExpr.term != null
					&& expression.arithExpr.term.factor != null){
				loadFactorInReg(expression.arithExpr.term.factor, "r1");
				codeWriterProgram.write("\t putc \t r1\n");
			}
		}
	}

	public void genCodeGet(Variable variable) throws IOException {
		if(secondPass){
			if(variable.upIdnest != null
					&& variable.upIdnest.id != null){
				codeWriterProgram.write("\t getc \t r1\n");
				codeWriterProgram.write("\t sw \t " + variable.upIdnest.id.getValue() + "(r0),r1\n");
				codeWriterProgram.write("\n");
			}
		}		
	}
}
