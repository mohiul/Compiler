package parser;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import lex.Constants;
import lex.LexicalAnalyzer;
import lex.Token;
import smbl.SymbolTableHandler;

public class SyntacticAnalyzer {

	private LexicalAnalyzer lex;
	boolean error;
	Token lookAheadToken;
	Token prevLookAheadToken;
	String lookAhead;
	Writer errWriter;
	Writer grammarWriter;
	SymbolTableHandler tableHandler; 
	
	public SyntacticAnalyzer() throws IOException{
		error = false;
		lookAhead = null;
		lex = new LexicalAnalyzer();
		errWriter = lex.getWriter();
		grammarWriter = new BufferedWriter(
				new OutputStreamWriter(
				new FileOutputStream("grammars.txt"), "utf-8"));
		tableHandler = new SymbolTableHandler(errWriter);
	}
	
	public void handleFile(String file) throws IOException{
		lex.handleFile(file);
	}
	
	public void closeWriter() throws IOException {
		lex.closeWriter();
		grammarWriter.close();
		tableHandler.closeWriter();
	}

	public void setLexReader(Reader reader) throws IOException {
		lex.setReader(reader);
		lex.readFirstChar();
	}
	
	private boolean skipErrors(String[] first, String[] follow) throws IOException {
		if(lookAheadIsIn(lookAhead, first) 
				|| lookAheadIsIn(lookAhead, follow)) {
			return true;
		} else {
			writeError();
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
			writeError();
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
		lookAheadToken = lex.getNextToken();
		lookAhead = getLookAhead();
		
		if(prog() && match(Constants.DOLLAR)){
			tableHandler.print(tableHandler.getGlobalTable());
			return true;
		} else {
			return false;
		}
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
				grammarWriter.write("prog -> classDeclList progBody\n");
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
				grammarWriter.write("classDeclList -> classDecl classDeclList\n");
			} else {
				error = true;
			}
		}else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_PROGRAM })){
			grammarWriter.write("classDeclList -> EPSILON\n");
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
				grammarWriter.write("classDecl -> 'class' 'id' '{' varFuncDefs '}'';'\n");
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
				grammarWriter.write("varFuncDefs	-> varFuncDef varFuncDefs\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSECRLBRACKET,
				Constants.DOLLAR})){
			grammarWriter.write("varFuncDefs	-> EPSILON\n");
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
				grammarWriter.write("varFuncDef	-> type 'id' varFuncDefTail\n");
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
				grammarWriter.write("varFuncDefTail -> varDefTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR})) {
			if(funcDefTail(type, id)){
				grammarWriter.write("varFuncDefTail -> funcDefTail\n");
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
				grammarWriter.write("varDefTail	-> arraySizeList ';'\n");
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
					&& match(Constants.SEMICOLON)){
				grammarWriter.write("funcDefTail	-> '(' fParams ')' funcBody ';'\n");
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
				grammarWriter.write("progBody -> 'program' funcBody ';' funcDefList\n");
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
				grammarWriter.write("funcDefList -> funcDef funcDefList\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.DOLLAR })){
			grammarWriter.write("funcDefList -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean funcDef() throws IOException{
		if (!skipErrors(new String[] { Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT,
				Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT,
				Constants.ID})){
			if(funcHead() && funcBody() && match(Constants.SEMICOLON)){
				grammarWriter.write("funcDef -> funcHead funcBody ';'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean funcHead() throws IOException{
		Token type = new Token();
		Token id = new Token();
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
				grammarWriter.write("funcHead -> type 'id' '(' fParams ')'\n");
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
				grammarWriter.write("funcBody -> '{' varDeclStatList '}'\n");
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
				grammarWriter.write("varDeclStatList -> varDeclStat varDeclStatList\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.CLOSECRLBRACKET})){
			grammarWriter.write("varDeclStatList -> EPSILON\n");
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
				grammarWriter.write("varDeclStat -> nonidtype varDeclTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN})){
			if(altstatement()){
				grammarWriter.write("varDeclStat -> altstatement\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			if(match(Constants.ID, id)
					&& varDeclStatTail(id)){
				grammarWriter.write("varDeclStat -> 'id' varDeclStatTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}

	private boolean varDeclStatTail(Token type) throws IOException{
		if (!skipErrors(new String[] { Constants.ID, 
				Constants.OPENSQBRACKET,
				Constants.POINT,
				Constants.EQ },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			if(tableHandler.checkClassExists(type) && varDeclTail(type)){
				grammarWriter.write("varDeclStatTail -> varDeclTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET,
				Constants.POINT,
				Constants.EQ})){
			if(statmentTail()){
				grammarWriter.write("varDeclStatTail -> statmentTail\n");
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
				grammarWriter.write("varDeclTail -> id arraySizeList ;\n");
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
				grammarWriter.write("statmentTail -> variableTail1 assignOp expr ';'\n");
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
				grammarWriter.write("variableTail1 -> indiceList variableTail2\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.EQ})){
			grammarWriter.write("variableTail1 -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean variableTail2() throws IOException{
		if (!skipErrors(new String[] { Constants.POINT },
				new String[] { Constants.EQ }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.POINT})){
			if(match(Constants.POINT) && match(Constants.ID) && indiceList() && variableTail2()){
				grammarWriter.write("variableTail2 -> '.' 'id' indiceList variableTail2\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.EQ})){
			grammarWriter.write("variableTail2 -> EPSILON\n");
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
				grammarWriter.write("statementList -> statement statementList\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.CLOSECRLBRACKET})){
			grammarWriter.write("statementList -> EPSILON\n");
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
				grammarWriter.write("statement -> altstatement\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			if(assignStat() && match(Constants.SEMICOLON)){
				grammarWriter.write("statement -> assignStat ';'\n");
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
				grammarWriter.write("statement -> 'return' '(' expr ')' ';'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_PUT })){
			if(match(Constants.RESERVED_WORD_PUT) 
					&& match(Constants.OPENPAR)
					&& expr()
					&& match(Constants.CLOSEPAR)
					&& match(Constants.SEMICOLON)){
				grammarWriter.write("statement -> 'put' '(' expr ')' ';'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_GET })){
			if(match(Constants.RESERVED_WORD_GET) 
					&& match(Constants.OPENPAR)
					&& variable()
					&& match(Constants.CLOSEPAR)
					&& match(Constants.SEMICOLON)){
				grammarWriter.write("statement -> 'get' '(' variable ')' ';'\n");
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
				grammarWriter.write("statement -> 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'\n");
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
				grammarWriter.write("statement -> 'for' '(' type 'id' assignOp expr ';' arithExpr relOp arithExpr ';' assignStat ')' statBlock ';'\n");
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
				grammarWriter.write("assignStat -> variable assignOp expr\n");
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
				grammarWriter.write("statBlock -> '{' statementList '}'\n");
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
				grammarWriter.write("statBlock -> statement\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{
				Constants.SEMICOLON,
				Constants.RESERVED_WORD_ELSE})){
			if(statement()){
				grammarWriter.write("statBlock -> EPSILON\n");
			} else {
				error = true;
			}
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
				grammarWriter.write("expr -> arithExpr relExprTail\n");
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
				grammarWriter.write("relExprTail -> relOp arithExpr\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.SEMICOLON,
				Constants.CLOSEPAR,
				Constants.COMMA})) {
			grammarWriter.write("relExprTail -> EPSILON\n");
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
				grammarWriter.write("arithExpr -> term arithExprTail\n");
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
				grammarWriter.write("arithExprTail -> addOp term arithExprTail\n");
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
			grammarWriter.write("arithExprTail -> EPSILON\n");
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
				grammarWriter.write("sign -> '+'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.MINUS })) {
			if(match(Constants.MINUS )){
				grammarWriter.write("sign -> '-'\n");
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
				grammarWriter.write("term -> factor termTail\n");
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
				grammarWriter.write("termTail -> multOp factor termTail\n");
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
			grammarWriter.write("termTail -> EPSILON\n");
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
				grammarWriter.write("factor -> 'id' factorTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.NUM })){
			if(match(Constants.NUM)){
				grammarWriter.write("factor -> 'num'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR })){
			if(match(Constants.OPENPAR) && arithExpr() && match(Constants.CLOSEPAR)){
				grammarWriter.write("factor -> '(' arithExpr ')'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_NOT })){
			if(match(Constants.RESERVED_WORD_NOT) && factor()){
				grammarWriter.write("factor -> 'not' factor\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.PLUS, Constants.MINUS })){
			if(sign() && factor()){
				grammarWriter.write("factor -> sign factor\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean factorTail() throws IOException{
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
					&& match(Constants.ID)
					&& factorTail()){
				grammarWriter.write("factorTail -> '.' 'id' factorTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENSQBRACKET })){
			if(indice() && indiceList() && factorTail2()){
				grammarWriter.write("factorTail -> indice indiceList factorTail2\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR })){
			if(match(Constants.OPENPAR) && aParams() && match(Constants.CLOSEPAR)){
				grammarWriter.write("factorTail -> '(' aParams ')'\n");
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
			grammarWriter.write("factorTail -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean factorTail2() throws IOException{
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
					&& match(Constants.ID)
					&& factorTail()){
				grammarWriter.write("factorTail2 -> '.' 'id' factorTail\n");
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
			grammarWriter.write("factorTail2 -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean variable() throws IOException{
		if (!skipErrors(new String[] { Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.ID })){
			if(idnest() && variableTail()){ 
				grammarWriter.write("variable -> idnest variableTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean variableTail() throws IOException{
		if (!skipErrors(new String[] { Constants.POINT },
				new String[] { Constants.CLOSEPAR, Constants.EQ }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.POINT })){
			if(match(Constants.POINT) && idnest() && variableTail()){ 
				grammarWriter.write("variableTail -> '.' idnest variableTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR,
				Constants.EQ})){
			grammarWriter.write("variableTail -> EPSILON\n");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean idnest() throws IOException{
		Token id = new Token();
		if (!skipErrors(new String[] { Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.ID })){
			if(match(Constants.ID, id)
					&& tableHandler.checkVariableExists(id)
					&& indiceList()){ 
				grammarWriter.write("idnest -> 'id' indiceList\n");
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
				grammarWriter.write("indiceList -> indice indiceList\n");
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
					grammarWriter.write("indiceList -> EPSILON\n");
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
				grammarWriter.write("indice -> '[' arithExpr ']'\n");
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
				grammarWriter.write("arraySizeList -> arraySize arraySizeList\n");
				arraySizeList.add(arraySize);
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.CLOSEPAR,
				Constants.SEMICOLON,
				Constants.COMMA})){
			grammarWriter.write("arraySizeList -> EPSILON\n");
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
				grammarWriter.write("arraySize -> '[' 'num' ']'\n");
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
				grammarWriter.write("type -> 'id'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT})){
			if(nonidtype(type)){ 
				grammarWriter.write("type -> nonidtype\n");
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
				grammarWriter.write("type -> 'int'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_FLOAT })){
			if(match(Constants.RESERVED_WORD_FLOAT, type)){ 
				grammarWriter.write("type -> 'float'\n");
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
				grammarWriter.write("fParams -> type 'id' arraySizeList fParamsTailList\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR })) {
			grammarWriter.write("fParams -> EPSILON\n");
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
				grammarWriter.write("aParams -> expr aParamsTails\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR })) {
			grammarWriter.write("aParams -> EPSILON\n");
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
				grammarWriter.write("fParamsTailList -> fParamsTail fParamsTailList\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR })) {
			grammarWriter.write("fParamsTailList -> EPSILON\n");
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
				grammarWriter.write("fParamsTail -> ',' type 'id' arraySizeList\n");
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
				grammarWriter.write("aParamsTails -> aParamsTail aParamsTails\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR })) {
			grammarWriter.write("aParamsTails -> EPSILON\n");
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
				grammarWriter.write("aParamsTail -> ',' expr\n");
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
				grammarWriter.write("assignOp -> '='\n");
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
				grammarWriter.write("relOp -> '>'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.LT })) {
			if(match(Constants.LT )){
				grammarWriter.write("relOp -> '<'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.LESSEQ })) {
			if(match(Constants.LESSEQ )){
				grammarWriter.write("relOp -> '<='\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.GREATEQ })) {
			if(match(Constants.GREATEQ )){
				grammarWriter.write("relOp -> '>='\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.NOTEQ })) {
			if(match(Constants.NOTEQ )){
				grammarWriter.write("relOp -> '<>'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.EQCOMP })) {
			if(match(Constants.EQCOMP )){
				grammarWriter.write("relOp -> '=='\n");
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
				grammarWriter.write("addOp -> '+'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.MINUS })) {
			if(match(Constants.MINUS )){
				grammarWriter.write("addOp -> '-'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_OR })) {
			if(match(Constants.RESERVED_WORD_OR )){
				grammarWriter.write("addOp -> 'or'\n");
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
				grammarWriter.write("multOp -> '*'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.DIV })) {
			if(match(Constants.DIV )){
				grammarWriter.write("multOp -> '/'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_AND })) {
			if(match(Constants.RESERVED_WORD_AND )){
				grammarWriter.write("multOp -> 'and'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
}
