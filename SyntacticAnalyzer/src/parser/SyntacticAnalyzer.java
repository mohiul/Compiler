package parser;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import lex.Constants;
import lex.LexicalAnalyzer;
import lex.Token;
import smbl.SymbolTableHandler;
import smbl.SymbolTableRow;
import smbl.VariableKind;
import smbl.VariableType;

public class SyntacticAnalyzer {

	private LexicalAnalyzer lex;
	boolean error;
	Token lookAheadToken;
	Token prevLookAheadToken;
	String lookAhead;
	String errorFileName;
	Writer errWriter;
	String grammarFileName;
	Writer grammarWriter;
	String codeFilename;
	Writer codeWriterData;
	Writer codeWriterProgram;
	SymbolTableHandler tableHandler;
	String file;
	boolean secondPass;
	boolean handlefile;
	int registerCount;
	
	public SyntacticAnalyzer(String errorFileName, String grammarFileName, String codeFileName) throws IOException{
		secondPass = false;
		handlefile = false;
		error = false;
		lookAhead = null;
		lex = new LexicalAnalyzer(errorFileName);
		errWriter = lex.getWriter();
		tableHandler = new SymbolTableHandler(secondPass, errWriter);
		registerCount = 0;
		this.errorFileName = errorFileName;
		this.grammarFileName = grammarFileName;
		this.codeFilename = codeFileName;
	}
	
	public void handleFile(String file) throws IOException{
		this.file = file;
		lex.handleFile(file);
		handlefile = true;
	}
	
	public void closeWriter() throws IOException {
		lex.closeWriter();
		if(grammarWriter != null) grammarWriter.close();
		tableHandler.closeWriter();
		if(codeWriterData != null) codeWriterData.close();
		if(codeWriterProgram != null) codeWriterProgram.close();
	}

	public void setLexReaderStr(String str) throws IOException {
		lex.setReaderStr(str);
		lex.readFirstChar();
	}
	
	private boolean skipErrors(String[] first, String[] follow) throws IOException {
		if(lookAheadIsIn(lookAhead, first) 
				|| lookAheadIsIn(lookAhead, follow)) {
			return true;
		} else {
			if(!secondPass) writeError();
		    while (!lookAheadIsIn(lookAhead, first) 
					&& !lookAheadIsIn(lookAhead, follow) && lookAheadToken != null ){
				lookAheadToken = lex.getNextToken();
				lookAhead = getLookAhead();
		    }
			return true;
		}
	}

	private void writeError() throws IOException {
		String errMsg = "Syntax error at line: " 
				+ ( lookAheadToken != null? lookAheadToken.getLineNo() : lex.getLineNo() )  
				+ " position: " 
				+ ( lookAheadToken != null? lookAheadToken.getPositionInLine() : lex.getPosition() )
				+ ( lookAheadToken != null? ": " + lookAheadToken.getValue(): "");
		errWriter.write(errMsg + "\n");
		System.err.println(errMsg);
	}

	private boolean match(String strToMatch, Token matchedToken) throws IOException {
		boolean match = match(strToMatch);
		copyValue(matchedToken, prevLookAheadToken);
		return match;
	}
	
	private void copyValue(Token toToken, Token fromToken) {
		toToken.setType(fromToken.getType());
		toToken.setValue(fromToken.getValue());
		toToken.setLineNo(fromToken.getLineNo());
		toToken.setPositionInLine(fromToken.getPositionInLine());
	}

	private boolean match(String strToMatch) throws IOException {
		boolean match = false;
		if(strToMatch.equalsIgnoreCase(lookAhead)){
			match = true;
		} else {
			if(!secondPass) writeError();
		}
		if(lookAheadToken != null){
			prevLookAheadToken = lookAheadToken;
			lookAheadToken = lex.getNextToken();
			lookAhead = getLookAhead();
		}
		return match;
	}
	
	private boolean lookAheadIsIn(String lookAhead, String... strToMatchArr) {
		boolean matches = false;
		for(String str : strToMatchArr){
			if(str.equalsIgnoreCase(lookAhead)){
				matches = true;
			}
		}
		return matches;
	}
	
	private String getLookAhead() {
		String lookAhead = Constants.DOLLAR;
		if(lookAheadToken != null){
			lookAhead = lookAheadToken.getType();
			if(lookAheadToken.getType().equalsIgnoreCase(Constants.RESERVED_WORD)){
				lookAhead = lookAheadToken.getValue();
			}
		}

		return lookAhead;
	}
	
	public boolean parse() throws IOException{
		boolean toReturn = false;
		//First Pass
		lookAheadToken = lex.getNextToken();
		lookAhead = getLookAhead();
		
		toReturn = prog() && match(Constants.DOLLAR);
		
		if(toReturn){
			//Second Pass
			lex.closeWriter();
			
			error = false;
			lookAhead = null;
			String strToRead = lex.getStrToRead();
			lex = new LexicalAnalyzer(errorFileName);
			if(handlefile){
				lex.handleFile(file);
			} else {
				setLexReaderStr(strToRead);
			}
			errWriter = lex.getWriter();
			grammarWriter = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream(grammarFileName), "utf-8"));
			codeWriterData = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream("codeData.txt"), "utf-8"));
			codeWriterProgram = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream("codeProgram.txt"), "utf-8"));
			secondPass = true;
			lex.setSecondPass(secondPass);
			tableHandler.setSecondPass(secondPass);
			tableHandler.setErrWriter(errWriter);
			
			lookAheadToken = lex.getNextToken();
			lookAhead = getLookAhead();
			if(prog() && match(Constants.DOLLAR)){
				tableHandler.print(tableHandler.getGlobalTable());
				toReturn = true;
			} else {
				toReturn = false;
			}
		}
		return toReturn;
	}
	
	private boolean prog() throws IOException{
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_CLASS, 
				Constants.RESERVED_WORD_PROGRAM },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.RESERVED_WORD_CLASS, 
				Constants.RESERVED_WORD_PROGRAM})){
			if( tableHandler.createGlobalTable() && classDeclList() && progBody()){
				if(secondPass) grammarWriter.write("prog -> classDeclList progBody\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean classDeclList() throws IOException{
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_CLASS },
				new String[] { Constants.RESERVED_WORD_PROGRAM }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_CLASS })){
			if(classDecl() && classDeclList()){
				if(secondPass) grammarWriter.write("classDeclList -> classDecl classDeclList\n");
			} else {
				error = true;
			}
		}else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_PROGRAM })){
			if(secondPass) grammarWriter.write("classDeclList -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean classDecl() throws IOException{
		Token id = new Token();
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_CLASS },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_CLASS })){
			if(match(Constants.RESERVED_WORD_CLASS) 
					&& match(Constants.ID, id)
					&& tableHandler.createClassEntryAndTable(id)
					&& match(Constants.OPENCRLBRACKET)
					&& varFuncDefs()
					&& match(Constants.CLOSECRLBRACKET)
					&& match(Constants.SEMICOLON)){
				if(secondPass) grammarWriter.write("classDecl -> 'class' 'id' '{' varFuncDefs '}'';'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varFuncDefs() throws IOException{
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_FLOAT, 
				Constants.ID,
				Constants.RESERVED_WORD_INT },
				new String[] { Constants.CLOSECRLBRACKET,
						Constants.DOLLAR }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.RESERVED_WORD_FLOAT, 
				Constants.ID,
				Constants.RESERVED_WORD_INT})){
			if(varFuncDef() && varFuncDefs()){
				if(secondPass) grammarWriter.write("varFuncDefs	-> varFuncDef varFuncDefs\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSECRLBRACKET,
				Constants.DOLLAR})){
			if(secondPass) grammarWriter.write("varFuncDefs	-> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varFuncDef() throws IOException{
		Token type = new Token();
		Token id = new Token();
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_FLOAT, 
				Constants.ID,
				Constants.RESERVED_WORD_INT },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.RESERVED_WORD_FLOAT, 
				Constants.ID,
				Constants.RESERVED_WORD_INT})){
			if(type(type) 
					&& match(Constants.ID, id) 
					&& varFuncDefTail(type, id)){
				if(secondPass) grammarWriter.write("varFuncDef	-> type 'id' varFuncDefTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varFuncDefTail(Token type, Token id) throws IOException{
		if (!skipErrors(new String[] { Constants.OPENSQBRACKET, 
				Constants.SEMICOLON,
				Constants.OPENPAR },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.OPENSQBRACKET, 
				Constants.SEMICOLON})){
			if(varDefTail(type, id)){
				if(secondPass) grammarWriter.write("varFuncDefTail -> varDefTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR})) {
			if(funcDefTail(type, id)){
				if(secondPass) grammarWriter.write("varFuncDefTail -> funcDefTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varDefTail(Token type, Token id) throws IOException{
		List<Token> arraySizeList = new ArrayList<Token>();
		if (!skipErrors(new String[] { Constants.OPENSQBRACKET, 
				Constants.SEMICOLON },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.OPENSQBRACKET, 
				Constants.SEMICOLON})){
			if(arraySizeList(arraySizeList)
					&& match(Constants.SEMICOLON) 
					&& tableHandler.createVariableEntry(type, id, arraySizeList)){
				if(secondPass){
					grammarWriter.write("varDefTail	-> arraySizeList ';'\n");
					genCodeCreateVariable(id);
				}
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean funcDefTail(Token type, Token id) throws IOException{
		if (!skipErrors(new String[] { Constants.OPENPAR },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR })) {
			if(match(Constants.OPENPAR) 
					&& tableHandler.createFunctionEntryAndTable(type, id)
					&& fParams()
					&& match(Constants.CLOSEPAR)
					&& funcBody()
					&& match(Constants.SEMICOLON)
					&& tableHandler.delFuncTable(id)){
				if(secondPass) grammarWriter.write("funcDefTail	-> '(' fParams ')' funcBody ';'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean progBody() throws IOException{
		Token program = new Token();
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_PROGRAM },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_PROGRAM })){
			if(match(Constants.RESERVED_WORD_PROGRAM, program)
					&& tableHandler.createProgramTable(program)
					&& funcBody()
					&& match(Constants.SEMICOLON)
					&& funcDefList()){
				if(secondPass) grammarWriter.write("progBody -> 'program' funcBody ';' funcDefList\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}

	private boolean funcDefList() throws IOException{
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT,
				Constants.ID },
				new String[] { Constants.DOLLAR }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT,
				Constants.ID})){
			if(funcDef() && funcDefList()){
				if(secondPass) grammarWriter.write("funcDefList -> funcDef funcDefList\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.DOLLAR })){
			if(secondPass) grammarWriter.write("funcDefList -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean funcDef() throws IOException{
		Token funcId = new Token();
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT,
				Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT,
				Constants.ID})){
			if(funcHead(funcId) 
					&& funcBody() 
					&& match(Constants.SEMICOLON)
					&& tableHandler.delFuncTable(funcId)){
				if(secondPass) grammarWriter.write("funcDef -> funcHead funcBody ';'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean funcHead(Token id) throws IOException{
		Token type = new Token();
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT,
				Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT,
				Constants.ID})){
			if(type(type) && match(Constants.ID, id)
					&& match(Constants.OPENPAR)
					&& tableHandler.createFunctionEntryAndTable(type, id)
					&& fParams()
					&& match(Constants.CLOSEPAR)){
				if(secondPass) grammarWriter.write("funcHead -> type 'id' '(' fParams ')'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean funcBody() throws IOException{
		if (!skipErrors(new String[] { Constants.OPENCRLBRACKET },
				new String[] { Constants.CLOSECRLBRACKET }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENCRLBRACKET})){
			if(match(Constants.OPENCRLBRACKET) 
					&& varDeclStatList()
					&& match(Constants.CLOSECRLBRACKET)){
				if(secondPass) grammarWriter.write("funcBody -> '{' varDeclStatList '}'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varDeclStatList() throws IOException{
		if (!skipErrors(new String[] { Constants.ID,
				Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN,
				Constants.RESERVED_WORD_FLOAT,
				Constants.RESERVED_WORD_INT },
				new String[] { Constants.CLOSECRLBRACKET }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.ID,
				Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN,
				Constants.RESERVED_WORD_FLOAT,
				Constants.RESERVED_WORD_INT})){
			if(varDeclStat() && varDeclStatList()){
				if(secondPass) grammarWriter.write("varDeclStatList -> varDeclStat varDeclStatList\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.CLOSECRLBRACKET})){
			if(secondPass) grammarWriter.write("varDeclStatList -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varDeclStat() throws IOException{
		Token type = new Token();
		Token id = new Token();
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_FLOAT,
				Constants.RESERVED_WORD_INT, 
				Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN,
				Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_FLOAT,
				Constants.RESERVED_WORD_INT})){
			if(nonidtype(type) && varDeclTail(type)){
				if(secondPass) grammarWriter.write("varDeclStat -> nonidtype varDeclTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN})){
			if(altstatement()){
				if(secondPass) grammarWriter.write("varDeclStat -> altstatement\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			if(match(Constants.ID, id)
					&& varDeclStatTail(id)){
				if(secondPass) grammarWriter.write("varDeclStat -> 'id' varDeclStatTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}

	private boolean varDeclStatTail(Token typeOrId) throws IOException{
		if (!skipErrors(new String[] { Constants.ID, 
				Constants.OPENSQBRACKET,
				Constants.POINT,
				Constants.EQ },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			if(tableHandler.checkClassExists(typeOrId) && varDeclTail(typeOrId)){
				if(secondPass) grammarWriter.write("varDeclStatTail -> varDeclTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET,
				Constants.POINT,
				Constants.EQ})){
			if(tableHandler.checkVariableExists(typeOrId) && statmentTail()){
				if(secondPass) grammarWriter.write("varDeclStatTail -> statmentTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varDeclTail(Token type) throws IOException{
		Token id = new Token();
		List<Token> arraySizeList = new ArrayList<Token>();
		if (!skipErrors(new String[] { Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			if(match(Constants.ID, id) 
					&& arraySizeList(arraySizeList)
					&& match(Constants.SEMICOLON)
					&& tableHandler.createVariableEntry(type, id, arraySizeList)){
				if(secondPass){
					grammarWriter.write("varDeclTail -> id arraySizeList ;\n");
					genCodeCreateVariable(id);
				}
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean statmentTail() throws IOException{
		if (!skipErrors(new String[] { Constants.OPENSQBRACKET,
				Constants.POINT,
				Constants.EQ },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET,
				Constants.POINT,
				Constants.EQ})){
			if(variableTail1()
					&& assignOp()
					&& expr()
					&& match(Constants.SEMICOLON)){
				if(secondPass) grammarWriter.write("statmentTail -> variableTail1 assignOp expr ';'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean variableTail1() throws IOException{
		if (!skipErrors(new String[] { Constants.OPENSQBRACKET,
				Constants.POINT },
				new String[] { Constants.EQ }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET,
				Constants.POINT})){
			if(indiceList() && variableTail2()){
				if(secondPass) grammarWriter.write("variableTail1 -> indiceList variableTail2\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.EQ})){
			if(secondPass) grammarWriter.write("variableTail1 -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean variableTail2() throws IOException{
		Token id = new Token();
		if (!skipErrors(new String[] { Constants.POINT },
				new String[] { Constants.EQ }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.POINT})){
			if(match(Constants.POINT) 
					&& match(Constants.ID, id)
					&& tableHandler.checkVariableInClassExists(id) 
					&& indiceList() 
					&& variableTail2()){
				if(secondPass) grammarWriter.write("variableTail2 -> '.' 'id' indiceList variableTail2\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.EQ})){
			if(secondPass) grammarWriter.write("variableTail2 -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean statementList() throws IOException{
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN,
				Constants.ID },
				new String[] { Constants.CLOSECRLBRACKET }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN,
				Constants.ID})){
			if(statement() && statementList()){
				if(secondPass) grammarWriter.write("statementList -> statement statementList\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.CLOSECRLBRACKET})){
			if(secondPass) grammarWriter.write("statementList -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean statement() throws IOException{
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN,
				Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN })){
			if(altstatement()){
				if(secondPass) grammarWriter.write("statement -> altstatement\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			if(assignStat() && match(Constants.SEMICOLON)){
				if(secondPass) grammarWriter.write("statement -> assignStat ';'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean altstatement() throws IOException{
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_RETURN,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_FOR },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_RETURN })){
			if(match(Constants.RESERVED_WORD_RETURN) 
					&& match(Constants.OPENPAR)
					&& expr()
					&& match(Constants.CLOSEPAR)
					&& match(Constants.SEMICOLON)){
				if(secondPass) grammarWriter.write("statement -> 'return' '(' expr ')' ';'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_PUT })){
			if(match(Constants.RESERVED_WORD_PUT) 
					&& match(Constants.OPENPAR)
					&& expr()
					&& match(Constants.CLOSEPAR)
					&& match(Constants.SEMICOLON)){
				if(secondPass) grammarWriter.write("statement -> 'put' '(' expr ')' ';'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_GET })){
			if(match(Constants.RESERVED_WORD_GET) 
					&& match(Constants.OPENPAR)
					&& variable()
					&& match(Constants.CLOSEPAR)
					&& match(Constants.SEMICOLON)){
				if(secondPass) grammarWriter.write("statement -> 'get' '(' variable ')' ';'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_IF })){
			if(match(Constants.RESERVED_WORD_IF)
					&& match(Constants.OPENPAR)
					&& expr()
					&& match(Constants.CLOSEPAR)
					&& match(Constants.RESERVED_WORD_THEN)
					&& statBlock()
					&& match(Constants.RESERVED_WORD_ELSE)
					&& statBlock()
					&& match(Constants.SEMICOLON)){
				if(secondPass) grammarWriter.write("statement -> 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_FOR })){
			Token type = new Token();
			Token id = new Token();
			if(match(Constants.RESERVED_WORD_FOR)
					&& match(Constants.OPENPAR)
					&& type(type)
					&& match(Constants.ID, id)
					&& tableHandler.createVariableEntry(type, id, null)
					&& assignOp()
					&& expr()
					&& match(Constants.SEMICOLON)
					&& arithExpr()
					&& relOp()
					&& arithExpr()
					&& match(Constants.SEMICOLON)
					&& assignStat()
					&& match(Constants.CLOSEPAR)
					&& statBlock()
					&& match(Constants.SEMICOLON)){
				if(secondPass) grammarWriter.write("statement -> 'for' '(' type 'id' assignOp expr ';' arithExpr relOp arithExpr ';' assignStat ')' statBlock ';'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean assignStat() throws IOException{
		if (!skipErrors(new String[] { Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			if(variable() && assignOp() && expr()){
				if(secondPass) grammarWriter.write("assignStat -> variable assignOp expr\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}

	private boolean statBlock() throws IOException{
		if (!skipErrors(new String[] { Constants.OPENCRLBRACKET,
				Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN,
				Constants.ID },
				new String[] { Constants.SEMICOLON,
						Constants.RESERVED_WORD_ELSE }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENCRLBRACKET})){
			if(match(Constants.OPENCRLBRACKET) 
					&& statementList() 
					&& match(Constants.CLOSECRLBRACKET)){
				if(secondPass) grammarWriter.write("statBlock -> '{' statementList '}'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{
				Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN,
				Constants.ID})){
			if(statement()){
				if(secondPass) grammarWriter.write("statBlock -> statement\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{
				Constants.SEMICOLON,
				Constants.RESERVED_WORD_ELSE})){
			if(secondPass) grammarWriter.write("statBlock -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean expr() throws IOException{
		if (!skipErrors(new String[] { Constants.OPENPAR,
				Constants.ID,
				Constants.NUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.NUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS})) {
			if(arithExpr() && relExprTail()){
				if(secondPass) grammarWriter.write("expr -> arithExpr relExprTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean relExprTail() throws IOException{
		if (!skipErrors(new String[] { Constants.LT,
				Constants.LESSEQ,
				Constants.NOTEQ,
				Constants.EQCOMP,
				Constants.GT,
				Constants.GREATEQ },
				new String[] { Constants.SEMICOLON,
						Constants.CLOSEPAR,
						Constants.COMMA }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.LT,
				Constants.LESSEQ,
				Constants.NOTEQ,
				Constants.EQCOMP,
				Constants.GT,
				Constants.GREATEQ})) {
			if(relOp() && arithExpr()){
				if(secondPass) grammarWriter.write("relExprTail -> relOp arithExpr\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.SEMICOLON,
				Constants.CLOSEPAR,
				Constants.COMMA})) {
			if(secondPass) grammarWriter.write("relExprTail -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean arithExpr() throws IOException{
		if (!skipErrors(new String[] { Constants.OPENPAR,
				Constants.ID,
				Constants.NUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.NUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS})) {
			if(term() && arithExprTail()){
				if(secondPass) grammarWriter.write("arithExpr -> term arithExprTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean arithExprTail() throws IOException{
		if (!skipErrors(new String[] { Constants.PLUS,
				Constants.MINUS,
				Constants.RESERVED_WORD_OR },
				new String[] { Constants.LT,
						Constants.LESSEQ,
						Constants.NOTEQ,
						Constants.EQCOMP,
						Constants.GT,
						Constants.GREATEQ,
						Constants.SEMICOLON,
						Constants.CLOSEPAR,
						Constants.COMMA,
						Constants.CLOSESQBRACKET }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.PLUS,
				Constants.MINUS,
				Constants.RESERVED_WORD_OR})) {
			if(addOp() && term() && arithExprTail()){
				if(secondPass) grammarWriter.write("arithExprTail -> addOp term arithExprTail\n");
			} else {
				error = true;
			}
		}else if(lookAheadIsIn(lookAhead, new String[]{ Constants.LT,
				Constants.LESSEQ,
				Constants.NOTEQ,
				Constants.EQCOMP,
				Constants.GT,
				Constants.GREATEQ,
				Constants.SEMICOLON,
				Constants.CLOSEPAR,
				Constants.COMMA,
				Constants.CLOSESQBRACKET})) {
			if(secondPass) grammarWriter.write("arithExprTail -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean sign() throws IOException{
		error = !skipErrors(new String[] { Constants.PLUS,
				Constants.MINUS },
				new String[] { });
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.PLUS })) {
			if(match(Constants.PLUS)){
				if(secondPass) grammarWriter.write("sign -> '+'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.MINUS })) {
			if(match(Constants.MINUS )){
				if(secondPass) grammarWriter.write("sign -> '-'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean term() throws IOException{
		if (!skipErrors(new String[] { Constants.OPENPAR,
				Constants.ID,
				Constants.NUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.NUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS})) {
			if(factor() && termTail()){
				if(secondPass) grammarWriter.write("term -> factor termTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean termTail() throws IOException{
		if (!skipErrors(new String[] { Constants.MULTIPLY,
				Constants.DIV,
				Constants.RESERVED_WORD_AND },
				new String[] { Constants.LT,
						Constants.LESSEQ,
						Constants.NOTEQ,
						Constants.EQCOMP,
						Constants.GT,
						Constants.GREATEQ,
						Constants.SEMICOLON,
						Constants.CLOSEPAR,
						Constants.COMMA,
						Constants.CLOSESQBRACKET,
						Constants.PLUS,
						Constants.MINUS,
						Constants.RESERVED_WORD_OR }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.MULTIPLY,
				Constants.DIV,
				Constants.RESERVED_WORD_AND})) {
			if(multOp() && factor() && termTail()){
				if(secondPass) grammarWriter.write("termTail -> multOp factor termTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.LT,
				Constants.LESSEQ,
				Constants.NOTEQ,
				Constants.EQCOMP,
				Constants.GT,
				Constants.GREATEQ,
				Constants.SEMICOLON,
				Constants.CLOSEPAR,
				Constants.COMMA,
				Constants.CLOSESQBRACKET,
				Constants.PLUS,
				Constants.MINUS,
				Constants.RESERVED_WORD_OR })) {
			if(secondPass) grammarWriter.write("termTail -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean factor() throws IOException{
		Token id = new Token();
		if (!skipErrors(new String[] { Constants.ID,
				Constants.NUM,
				Constants.OPENPAR,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.ID })){
			if(match(Constants.ID, id)
					&& tableHandler.checkVariableExists(id)
					&& factorTail()){
				if(secondPass){
					grammarWriter.write("factor -> 'id' factorTail\n");
					genCodeLoadVariable(id);
				}
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.NUM })){
			if(match(Constants.NUM)){
				if(secondPass) grammarWriter.write("factor -> 'num'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR })){
			if(match(Constants.OPENPAR) && arithExpr() && match(Constants.CLOSEPAR)){
				if(secondPass) grammarWriter.write("factor -> '(' arithExpr ')'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_NOT })){
			if(match(Constants.RESERVED_WORD_NOT) && factor()){
				if(secondPass) grammarWriter.write("factor -> 'not' factor\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.PLUS, Constants.MINUS })){
			if(sign() && factor()){
				if(secondPass) grammarWriter.write("factor -> sign factor\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean factorTail() throws IOException{
		Token id = new Token();
		if (!skipErrors(new String[] { Constants.POINT,
				Constants.OPENSQBRACKET,
				Constants.OPENPAR },
				new String[] { Constants.LT,
						Constants.LESSEQ,
						Constants.NOTEQ,
						Constants.EQCOMP,
						Constants.GT,
						Constants.GREATEQ,
						Constants.SEMICOLON,
						Constants.CLOSEPAR,
						Constants.COMMA,
						Constants.CLOSESQBRACKET,
						Constants.PLUS,
						Constants.MINUS,
						Constants.RESERVED_WORD_OR,
						Constants.MULTIPLY,
						Constants.DIV,
						Constants.RESERVED_WORD_AND}))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.POINT})){
			if( match(Constants.POINT)
					&& match(Constants.ID, id)
					&& tableHandler.checkVariableInClassExists(id)
					&& factorTail()){
				if(secondPass) grammarWriter.write("factorTail -> '.' 'id' factorTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENSQBRACKET })){
			if(indice() && indiceList() && factorTail2()){
				if(secondPass) grammarWriter.write("factorTail -> indice indiceList factorTail2\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR })){
			if(match(Constants.OPENPAR) && aParams() && match(Constants.CLOSEPAR)){
				if(secondPass) grammarWriter.write("factorTail -> '(' aParams ')'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.LT,
				Constants.LESSEQ,
				Constants.NOTEQ,
				Constants.EQCOMP,
				Constants.GT,
				Constants.GREATEQ,
				Constants.SEMICOLON,
				Constants.CLOSEPAR,
				Constants.COMMA,
				Constants.CLOSESQBRACKET,
				Constants.PLUS,
				Constants.MINUS,
				Constants.RESERVED_WORD_OR,
				Constants.MULTIPLY,
				Constants.DIV,
				Constants.RESERVED_WORD_AND})) {
			if(secondPass) grammarWriter.write("factorTail -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean factorTail2() throws IOException{
		Token id = new Token();
		if (!skipErrors(new String[] { Constants.POINT },
				new String[] { Constants.LT,
						Constants.LESSEQ,
						Constants.NOTEQ,
						Constants.EQCOMP,
						Constants.GT,
						Constants.GREATEQ,
						Constants.SEMICOLON,
						Constants.CLOSEPAR,
						Constants.COMMA,
						Constants.CLOSESQBRACKET,
						Constants.PLUS,
						Constants.MINUS,
						Constants.RESERVED_WORD_OR,
						Constants.MULTIPLY,
						Constants.DIV,
						Constants.RESERVED_WORD_AND}))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.POINT})){
			if( match(Constants.POINT)
					&& match(Constants.ID, id)
					&& tableHandler.checkVariableInClassExists(id)
					&& factorTail()){
				if(secondPass) grammarWriter.write("factorTail2 -> '.' 'id' factorTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.LT,
				Constants.LESSEQ,
				Constants.NOTEQ,
				Constants.EQCOMP,
				Constants.GT,
				Constants.GREATEQ,
				Constants.SEMICOLON,
				Constants.CLOSEPAR,
				Constants.COMMA,
				Constants.CLOSESQBRACKET,
				Constants.PLUS,
				Constants.MINUS,
				Constants.RESERVED_WORD_OR,
				Constants.MULTIPLY,
				Constants.DIV,
				Constants.RESERVED_WORD_AND})) {
			if(secondPass) grammarWriter.write("factorTail2 -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean variable() throws IOException{
		Token id = new Token();
		if (!skipErrors(new String[] { Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.ID })){
			if(idnest(id)
					&& tableHandler.checkVariableExists(id) 
					&& variableTail()){ 
				if(secondPass) grammarWriter.write("variable -> idnest variableTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean variableTail() throws IOException{
		Token id = new Token();
		if (!skipErrors(new String[] { Constants.POINT },
				new String[] { Constants.CLOSEPAR, Constants.EQ }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.POINT })){
			if(match(Constants.POINT) 
					&& idnest(id)
					&& tableHandler.checkVariableInClassExists(id)
					&& variableTail()){ 
				if(secondPass) grammarWriter.write("variableTail -> '.' idnest variableTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR,
				Constants.EQ})){
			if(secondPass) grammarWriter.write("variableTail -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean idnest(Token id) throws IOException{
		if (!skipErrors(new String[] { Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.ID })){
			if(match(Constants.ID, id)
					&& indiceList()){ 
				if(secondPass) grammarWriter.write("idnest -> 'id' indiceList\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean indiceList() throws IOException{
		if (!skipErrors(new String[] { Constants.OPENSQBRACKET },
				new String[] { Constants.EQ, 
						Constants.POINT, 
						Constants.LT, 
						Constants.LESSEQ, 
						Constants.NOTEQ,
						Constants.EQCOMP, 
						Constants.GT, 
						Constants.GREATEQ, 
						Constants.SEMICOLON, 
						Constants.CLOSEPAR,
						Constants.COMMA, 
						Constants.CLOSESQBRACKET, 
						Constants.PLUS, 
						Constants.MINUS,
						Constants.RESERVED_WORD_OR, 
						Constants.MULTIPLY, 
						Constants.DIV, 
						Constants.RESERVED_WORD_AND }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENSQBRACKET })){
			if(indice() && indiceList()){ 
				if(secondPass) grammarWriter.write("indiceList -> indice indiceList\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.EQ, 
				Constants.POINT, 
				Constants.LT,
				Constants.LESSEQ,
				Constants.NOTEQ,
				Constants.EQCOMP,
				Constants.GT,
				Constants.GREATEQ,
				Constants.SEMICOLON,
				Constants.CLOSEPAR,
				Constants.COMMA,
				Constants.CLOSESQBRACKET,
				Constants.PLUS,
				Constants.MINUS,
				Constants.RESERVED_WORD_OR,
				Constants.MULTIPLY,
				Constants.DIV,
				Constants.RESERVED_WORD_AND })){
					if(secondPass) grammarWriter.write("indiceList -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean indice() throws IOException{
		if (!skipErrors(new String[] { Constants.OPENSQBRACKET },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENSQBRACKET })){
			if(match(Constants.OPENSQBRACKET) && arithExpr() && match(Constants.CLOSESQBRACKET)){
				if(secondPass) grammarWriter.write("indice -> '[' arithExpr ']'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean arraySizeList(List<Token> arraySizeList) throws IOException{
		Token arraySize = new Token();
		if (!skipErrors(new String[] { Constants.OPENSQBRACKET },
				new String[] { Constants.CLOSEPAR, Constants.SEMICOLON, Constants.COMMA }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET})){
			if(arraySize(arraySize) && arraySizeList(arraySizeList)){
				if(secondPass) grammarWriter.write("arraySizeList -> arraySize arraySizeList\n");
				arraySizeList.add(arraySize);
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.CLOSEPAR,
				Constants.SEMICOLON,
				Constants.COMMA})){
			if(secondPass) grammarWriter.write("arraySizeList -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean arraySize(Token arraySize) throws IOException{
		if ( !skipErrors(new String[]{ Constants.OPENSQBRACKET },
				new String[]{ }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET})){
			if(match(Constants.OPENSQBRACKET)
					&& match(Constants.NUM, arraySize)
					&& match(Constants.CLOSESQBRACKET)){
				if(secondPass) grammarWriter.write("arraySize -> '[' 'num' ']'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean type(Token type) throws IOException{
		if ( !skipErrors(new String[]{ Constants.RESERVED_WORD_FLOAT,
				Constants.RESERVED_WORD_INT, 
				Constants.ID },
				new String[]{ }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.ID })){
			if(match(Constants.ID, type) && tableHandler.checkClassExists(type)){ 
				if(secondPass) grammarWriter.write("type -> 'id'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT})){
			if(nonidtype(type)){ 
				if(secondPass) grammarWriter.write("type -> nonidtype\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean nonidtype(Token type) throws IOException{
		error = !skipErrors(new String[]{ Constants.RESERVED_WORD_FLOAT,
				Constants.RESERVED_WORD_INT }, 
				new String[]{ });
		if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_INT})){
			if(match(Constants.RESERVED_WORD_INT, type)){ 
				if(secondPass) grammarWriter.write("type -> 'int'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_FLOAT })){
			if(match(Constants.RESERVED_WORD_FLOAT, type)){ 
				if(secondPass) grammarWriter.write("type -> 'float'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean fParams() throws IOException{
		Token type = new Token();
		Token id = new Token();
		List<Token> arraySizeList = new ArrayList<Token>();
		if ( !skipErrors(new String[]{ Constants.RESERVED_WORD_FLOAT,
				Constants.ID,
				Constants.RESERVED_WORD_INT }, 
				new String[]{ Constants.CLOSEPAR }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_FLOAT,
				Constants.ID,
				Constants.RESERVED_WORD_INT })) {
			if(type(type) 
					&& match(Constants.ID, id)
					&& arraySizeList(arraySizeList)
					&& tableHandler.createParameterEntry(type, id, arraySizeList)
					&& fParamsTailList()){
				if(secondPass) grammarWriter.write("fParams -> type 'id' arraySizeList fParamsTailList\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR })) {
			if(secondPass) grammarWriter.write("fParams -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean aParams() throws IOException{
		if ( !skipErrors(new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.NUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS }, 
				new String[]{ Constants.CLOSEPAR }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.NUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS})) {
			if(expr() && aParamsTails()){
				if(secondPass) grammarWriter.write("aParams -> expr aParamsTails\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR })) {
			if(secondPass) grammarWriter.write("aParams -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean fParamsTailList() throws IOException{
		if ( !skipErrors(new String[]{ Constants.COMMA }, 
				new String[]{ Constants.CLOSEPAR }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.COMMA})) {
			if(fParamsTail() && fParamsTailList()){
				if(secondPass) grammarWriter.write("fParamsTailList -> fParamsTail fParamsTailList\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR })) {
			if(secondPass) grammarWriter.write("fParamsTailList -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean fParamsTail() throws IOException{
		Token type = new Token();
		Token id = new Token();
		List<Token> arraySizeList = new ArrayList<Token>();
		if ( !skipErrors(new String[]{ Constants.COMMA }, 
				new String[]{ }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.COMMA})) {
			if(match(Constants.COMMA) 
					&& type(type) 
					&& match(Constants.ID, id) 
					&& arraySizeList(arraySizeList) 
					&& tableHandler.createParameterEntry(type, id, arraySizeList)){
				if(secondPass) grammarWriter.write("fParamsTail -> ',' type 'id' arraySizeList\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean aParamsTails() throws IOException{
		if ( !skipErrors(new String[]{ Constants.COMMA }, 
				new String[]{ Constants.CLOSEPAR }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.COMMA})) {
			if(aParamsTail() && aParamsTails()){
				if(secondPass) grammarWriter.write("aParamsTails -> aParamsTail aParamsTails\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR })) {
			if(secondPass) grammarWriter.write("aParamsTails -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean aParamsTail() throws IOException{
		if ( !skipErrors(new String[]{ Constants.COMMA }, 
				new String[]{ }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.COMMA})) {
			if(match(Constants.COMMA) && expr()){
				if(secondPass) grammarWriter.write("aParamsTail -> ',' expr\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean assignOp() throws IOException{
		error = !skipErrors(new String[]{ Constants.EQ }, 
				new String[]{ });
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.EQ })) {
			if(match(Constants.EQ)){
				if(secondPass) grammarWriter.write("assignOp -> '='\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean relOp() throws IOException{
		error = !skipErrors(new String[]{ Constants.GT, 
				Constants.LT, 
				Constants.LESSEQ,
				Constants.GREATEQ,
				Constants.NOTEQ,
				Constants.EQCOMP}, 
				new String[]{ });
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.GT })) {
			if(match(Constants.GT)){
				if(secondPass) grammarWriter.write("relOp -> '>'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.LT })) {
			if(match(Constants.LT )){
				if(secondPass) grammarWriter.write("relOp -> '<'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.LESSEQ })) {
			if(match(Constants.LESSEQ )){
				if(secondPass) grammarWriter.write("relOp -> '<='\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.GREATEQ })) {
			if(match(Constants.GREATEQ )){
				if(secondPass) grammarWriter.write("relOp -> '>='\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.NOTEQ })) {
			if(match(Constants.NOTEQ )){
				if(secondPass) grammarWriter.write("relOp -> '<>'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.EQCOMP })) {
			if(match(Constants.EQCOMP )){
				if(secondPass) grammarWriter.write("relOp -> '=='\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean addOp() throws IOException{
		error = !skipErrors(new String[]{ Constants.PLUS, 
				Constants.MINUS, 
				Constants.RESERVED_WORD_OR }, 
				new String[]{ });
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.PLUS })) {
			if(match(Constants.PLUS)){
				if(secondPass) grammarWriter.write("addOp -> '+'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.MINUS })) {
			if(match(Constants.MINUS )){
				if(secondPass) grammarWriter.write("addOp -> '-'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_OR })) {
			if(match(Constants.RESERVED_WORD_OR )){
				if(secondPass) grammarWriter.write("addOp -> 'or'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}

	private boolean multOp() throws IOException{
		error = !skipErrors(new String[]{ Constants.MULTIPLY, 
				Constants.DIV, 
				Constants.RESERVED_WORD_AND }, 
				new String[]{ });
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.MULTIPLY })) {
			if(match(Constants.MULTIPLY)){
				if(secondPass) grammarWriter.write("multOp -> '*'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.DIV })) {
			if(match(Constants.DIV )){
				if(secondPass) grammarWriter.write("multOp -> '/'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_AND })) {
			if(match(Constants.RESERVED_WORD_AND )){
				if(secondPass) grammarWriter.write("multOp -> 'and'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}

// Code Generation
	
	private void genCodeCreateVariable(Token id) throws IOException {
		SymbolTableRow row = tableHandler.getVariable(id.getValue());
		if(row.getKind() == VariableKind.VARIABLE
				&& row.getTypeList().size() > 0){
			VariableType varType = row.getTypeList().get(0);
			if(varType.getTypeName().equalsIgnoreCase(Constants.RESERVED_WORD_INT)){
				int[] dimArr = varType.getDimension();
				if(dimArr.length == 0){
					codeWriterData.write(row.getVarName() + "\t\tdw 0\n");
				} else {
					int i = 0;
					int dim = dimArr[i++];
					while(i < dimArr.length){
						dim *= dimArr[i++]; 
					}
					codeWriterData.write(row.getVarName() + "\t\tres " + dim + "\n");
				}
			}
		} else {
			System.err.println("Variable " + row.getVarName() + " TypeList should be > 0");
		}
	}
	
	private void genCodeLoadVariable(Token id) throws IOException {
		SymbolTableRow row = tableHandler.getVariable(id.getValue());
		if(row.getKind() == VariableKind.VARIABLE
				&& row.getTypeList().size() > 0){
			VariableType varType = row.getTypeList().get(0);
			if(varType.getTypeName().equalsIgnoreCase(Constants.RESERVED_WORD_INT)){
				int[] dimArr = varType.getDimension();
				if(dimArr == null || dimArr.length == 0){
					codeWriterProgram.write("\t\tlw r1, " + row.getVarName() + "(r0)" + "\n");
				} else {
					int i = 0;
					int dim = dimArr[i++];
					while(i < dimArr.length){
						dim *= dimArr[i++]; 
					}
					int r0 = registerCount++;
					int r1 = registerCount++;
					codeWriterProgram.write("\t\taddi r" + r0 + ",r" + r1 + "," + dim + "\n");
					codeWriterProgram.write("\t\tlw r" + r1 + "," + row.getVarName() + "(r" + r0 + ")" + "\n");
				}
			}
		} else {
			System.err.println("Variable " + row.getVarName() + " TypeList should be > 0");
		}
	}
	
}
