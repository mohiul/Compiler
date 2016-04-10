package parser;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import lex.Constants;
import lex.Token;
import smbl.SymbolTableHandler;
import smbl.SymbolTableRow;
import smbl.VariableKind;
import smbl.VariableType;

public class CodeGenerator {
	protected Writer codeWriterData;
	protected Writer codeWriterProgram;
	SymbolTableHandler tableHandler;
	
	public CodeGenerator(String codeFilename, 
			SymbolTableHandler tableHandler) throws UnsupportedEncodingException, FileNotFoundException {
		this.tableHandler = tableHandler;
		codeWriterData = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("codeData.txt"), "utf-8"));
		codeWriterProgram = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("codeProgram.txt"), "utf-8"));
	}

	public void closeWriter() throws IOException {
		if(codeWriterData != null) codeWriterData.close();
		if(codeWriterProgram != null) codeWriterProgram.close();
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
		
	}

}
