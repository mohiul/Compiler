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
import java.nio.file.Files;

import lex.Constants;
import lex.Token;
import sdt.Factor;
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
	int tempVarCount;
	
	public CodeGenerator(String codeFilename, 
			SymbolTableHandler tableHandler) throws UnsupportedEncodingException, FileNotFoundException {
		codeDataFileName = "codeData.txt";
		codeProgramFileName = "codeProgram.txt";
		this.codeFileName = codeFilename;
		this.tableHandler = tableHandler;
		codeWriterData = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(codeDataFileName), "utf-8"));
		codeWriterProgram = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(codeProgramFileName), "utf-8"));
		tempVarCount = 0;
	}

	public void closeWriter() throws IOException {
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
		}
		String tempVar = "t" + tempVarCount++;
		codeWriterData.write(tempVar + "\t dw \t 0\n");			
		codeWriterProgram.write("\t sw \t " + tempVar + "(r0),r3\n");			
		codeWriterProgram.write("\n");
		Token tempT = new Token();
		tempT.setValue(tempVar);
		tempT.setType(Constants.ID);
		f1.tempVar = tempT;
		
//		lw r1,a(r0)
//	    lw r2,b(r0)
//	    add r3,r1,r2
//	t1  dw 0
//	    sw t1(r0),r3
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

}
