package parser;
import java.io.IOException;

import lex.Constants;
import lex.LexicalAnalyzer;
import lex.Token;

public class SyntacticAnalyzer {

	private LexicalAnalyzer lex;
	boolean error;
	String lookAhead;
	
	public SyntacticAnalyzer(String file) throws IOException{
		error = false;
		lookAhead = null;
		lex = new LexicalAnalyzer();
		lex.handleFile(file);		
	}
	
	public void closeLexWriter() throws IOException {
		lex.closeWriter();		
	}
	
	public boolean parse() throws IOException{
		Token token = lex.getNextToken();
		lookAhead = getLookAhead(token);
		
		if(prog() && match(Constants.DOLLAR)) 
			return true;
		else 
			return false;
	}
	
	private boolean prog() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.RESERVED_WORD_CLASS, 
				Constants.RESERVED_WORD_PROGRAM})){
			if(classDeclList() && progBody()){
				System.out.println("prog -> classDeclList progBody");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean classDeclList() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_CLASS })){
			if(classDecl() && classDeclList()){
				System.out.println("classDeclList -> classDecl classDeclList");
			} else {
				error = true;
			}
		}else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_PROGRAM })){
			System.out.println("classDeclList -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean classDecl() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_CLASS })){
			if(match(Constants.RESERVED_WORD_CLASS) 
					&& match(Constants.ID)
					&& match(Constants.OPENCRLBRACKET)
					&& varFuncDefs()
					&& match(Constants.CLOSECRLBRACKET)
					&& match(Constants.SEMICOLON)){
				System.out.println("classDecl -> 'class' 'id' '{' varFuncDefs '}'';'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varFuncDefs() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.RESERVED_WORD_FLOAT, 
				Constants.ID,
				Constants.RESERVED_WORD_INT})){
			if(varFuncDef() && varFuncDefs()){
				System.out.println("varFuncDefs	-> varFuncDef varFuncDefs");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSECRLBRACKET,
				Constants.DOLLAR})){
			System.out.println("varFuncDefs	-> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varFuncDef() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.RESERVED_WORD_FLOAT, 
				Constants.ID,
				Constants.RESERVED_WORD_INT})){
			if(type() && match(Constants.ID) && varFuncDefTail()){
				System.out.println("varFuncDef	-> type 'id' varFuncDefTail");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varFuncDefTail() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.OPENSQBRACKET, 
				Constants.SEMICOLON})){
			if(varDefTail()){
				System.out.println("varFuncDefTail -> varDefTail");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR})) {
			if(funcDefTail()){
				System.out.println("varFuncDefTail -> funcDefTail");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varDefTail() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.OPENSQBRACKET, 
				Constants.SEMICOLON})){
			if(arraySizeList() && match(Constants.SEMICOLON)){
				System.out.println("varDefTail	-> arraySizeList ';'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean funcDefTail() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR})) {
			if(match(Constants.OPENPAR) 
					&& fParams()
					&& match(Constants.CLOSEPAR)
					&& funcBody()
					&& match(Constants.SEMICOLON)){
				System.out.println("funcDefTail	-> '(' fParams ')' funcBody ';'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean progBody() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_PROGRAM})){
			if(match(Constants.RESERVED_WORD_PROGRAM)
					&& funcBody()
					&& match(Constants.SEMICOLON)
					&& funcDefList()){
				System.out.println("progBody -> 'program' funcBody ';' funcDefList");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}

	private boolean funcDefList() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT,
				Constants.ID})){
			if(funcDef() && funcDefList()){
				System.out.println("funcDefList -> funcDef funcDefList");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.DOLLAR})){
			System.out.println("funcDefList -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean funcDef() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT,
				Constants.ID})){
			if(funcHead() && funcBody() && match(Constants.SEMICOLON)){
				System.out.println("funcDef -> funcHead funcBody ';'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean funcHead() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT,
				Constants.ID})){
			if(type() && match(Constants.ID) 
					&& match(Constants.OPENPAR)
					&& fParams()
					&& match(Constants.CLOSEPAR)){
				System.out.println("funcHead -> type 'id' '(' fParams ')'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean funcBody() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENCRLBRACKET})){
			if(match(Constants.OPENCRLBRACKET) 
					&& varDeclStatList()
					&& match(Constants.CLOSECRLBRACKET)){
				System.out.println("funcBody -> '{' varDeclStatList '}'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varDeclStatList() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.ID,
				Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN,
				Constants.RESERVED_WORD_FLOAT,
				Constants.RESERVED_WORD_INT})){
			if(varDeclStat() && varDeclStatList()){
				System.out.println("varDeclStatList -> varDeclStat varDeclStatList");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.CLOSECRLBRACKET})){
			System.out.println("varDeclStatList -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varDeclStat() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_FLOAT,
				Constants.RESERVED_WORD_INT})){
			if(nonidtype() && varDeclTail()){
				System.out.println("varDeclStat -> nonidtype varDeclTail");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN})){
			if(altstatement()){
				System.out.println("varDeclStat -> altstatement");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			if(match(Constants.ID) && varDeclStatTail()){
				System.out.println("varDeclStat -> 'id' varDeclStatTail");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}

	private boolean varDeclStatTail() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			if(varDeclTail()){
				System.out.println("varDeclStatTail -> varDeclTail");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET,
				Constants.POINT,
				Constants.EQ})){
			if(statmentTail()){
				System.out.println("varDeclStatTail -> statmentTail");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varDeclTail() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			if(match(Constants.ID) 
					&& arraySizeList()
					&& match(Constants.SEMICOLON)){
				System.out.println("varDeclTail -> id arraySizeList ;");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean statmentTail() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET,
				Constants.POINT,
				Constants.EQ})){
			if(variableTail1()
					&& assignOp()
					&& expr()
					&& match(Constants.SEMICOLON)){
				System.out.println("statmentTail -> variableTail1 assignOp expr ';'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean variableTail1() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET,
				Constants.POINT})){
			if(indiceList() && variableTail2()){
				System.out.println("variableTail1 -> indiceList variableTail2");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.EQ})){
			System.out.println("variableTail1 -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean variableTail2() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.POINT})){
			if(match(Constants.POINT) && match(Constants.ID) && indiceList() && variableTail2()){
				System.out.println("variableTail2 -> '.' 'id' indiceList variableTail2");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.EQ})){
			System.out.println("variableTail2 -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean statementList() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN,
				Constants.ID})){
			if(statement() && statementList()){
				System.out.println("statementList -> statement statementList");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.CLOSECRLBRACKET})){
			System.out.println("statementList -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean statement() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.RESERVED_WORD_FOR,
				Constants.RESERVED_WORD_IF,
				Constants.RESERVED_WORD_GET,
				Constants.RESERVED_WORD_PUT,
				Constants.RESERVED_WORD_RETURN})){
			if(altstatement()){
				System.out.println("statement -> altstatement");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			if(assignStat() && match(Constants.SEMICOLON)){
				System.out.println("statement -> assignStat ';'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean altstatement() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_RETURN })){
			if(match(Constants.RESERVED_WORD_RETURN) 
					&& match(Constants.OPENPAR)
					&& expr()
					&& match(Constants.CLOSEPAR)
					&& match(Constants.SEMICOLON)){
				System.out.println("statement -> 'return' '(' expr ')' ';'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_PUT })){
			if(match(Constants.RESERVED_WORD_PUT) 
					&& match(Constants.OPENPAR)
					&& expr()
					&& match(Constants.CLOSEPAR)
					&& match(Constants.SEMICOLON)){
				System.out.println("statement -> 'put' '(' expr ')' ';'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_GET })){
			if(match(Constants.RESERVED_WORD_GET) 
					&& match(Constants.OPENPAR)
					&& variable()
					&& match(Constants.CLOSEPAR)
					&& match(Constants.SEMICOLON)){
				System.out.println("statement -> 'get' '(' variable ')' ';'");
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
				System.out.println("statement -> 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_FOR })){
			if(match(Constants.RESERVED_WORD_FOR)
					&& match(Constants.OPENPAR)
					&& type()
					&& match(Constants.ID)
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
				System.out.println("statement -> 'for' '(' type 'id' assignOp expr ';' arithExpr relOp arithExpr ';' assignStat ')' statBlock ';'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean assignStat() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			if(variable() && assignOp() && expr()){
				System.out.println("assignStat -> variable assignOp expr");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}

	private boolean statBlock() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENCRLBRACKET})){
			if(match(Constants.OPENCRLBRACKET) 
					&& statementList() 
					&& match(Constants.CLOSECRLBRACKET)){
				System.out.println("statBlock -> '{' statementList '}'");
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
				System.out.println("statBlock -> statement");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{
				Constants.SEMICOLON,
				Constants.RESERVED_WORD_ELSE})){
			if(statement()){
				System.out.println("statBlock -> EPSILON");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}

	private boolean expr() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.NUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS})) {
			if(arithExpr() && relExprTail()){
				System.out.println("expr -> arithExpr relExprTail");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean relExprTail() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.LT,
				Constants.LESSEQ,
				Constants.NOTEQ,
				Constants.EQCOMP,
				Constants.GT,
				Constants.GREATEQ})) {
			if(relOp() && arithExpr()){
				System.out.println("relExprTail -> relOp arithExpr");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.SEMICOLON,
				Constants.CLOSEPAR,
				Constants.COMMA})) {
			System.out.println("relExprTail -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean arithExpr() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.NUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS})) {
			if(term() && arithExprTail()){
				System.out.println("arithExpr -> term arithExprTail");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean arithExprTail() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.PLUS,
				Constants.MINUS,
				Constants.RESERVED_WORD_OR})) {
			if(addOp() && term() && arithExprTail()){
				System.out.println("arithExprTail -> addOp term arithExprTail");
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
			System.out.println("arithExprTail -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean sign() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.PLUS })) {
			if(match(Constants.PLUS)){
				System.out.println("sign -> '+'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.MINUS })) {
			if(match(Constants.MINUS )){
				System.out.println("sign -> '-'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean term() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.NUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS})) {
			if(factor() && termTail()){
				System.out.println("term -> factor termTail");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean termTail() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.MULTIPLY,
				Constants.DIV,
				Constants.RESERVED_WORD_AND})) {
			if(multOp() && factor() && termTail()){
				System.out.println("termTail -> multOp factor termTail");
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
			System.out.println("termTail -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean factor() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.ID })){
			if(match(Constants.ID) && factorTail()){
				System.out.println("factor -> 'id' factorTail");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.NUM })){
			if(match(Constants.NUM)){
				System.out.println("factor -> 'num'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR })){
			if(match(Constants.OPENPAR) && arithExpr() && match(Constants.CLOSEPAR)){
				System.out.println("factor -> '(' arithExpr ')'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_NOT })){
			if(match(Constants.RESERVED_WORD_NOT) && factor()){
				System.out.println("factor -> 'not' factor");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.PLUS, Constants.MINUS })){
			if(sign() && factor()){
				System.out.println("factor -> sign factor");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean factorTail() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.POINT})){
			if( match(Constants.POINT)
					&& match(Constants.ID)
					&& factorTail()){
				System.out.println("factorTail -> '.' 'id' factorTail");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENSQBRACKET })){
			if(indice() && indiceList() && factorTail2()){
				System.out.println("factorTail -> indice indiceList factorTail2");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR })){
			if(match(Constants.OPENPAR) && aParams() && match(Constants.CLOSEPAR)){
				System.out.println("factorTail -> '(' aParams ')'");
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
			System.out.println("factorTail -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean factorTail2() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.POINT})){
			if( match(Constants.POINT)
					&& match(Constants.ID)
					&& factorTail()){
				System.out.println("factorTail2 -> '.' 'id' factorTail");
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
			System.out.println("factorTail2 -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean variable() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.ID })){
			if(idnest() && variableTail()){ 
				System.out.println("variable -> idnest variableTail");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean variableTail() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.POINT })){
			if(match(Constants.POINT) && idnest() && variableTail()){ 
				System.out.println("variableTail -> '.' idnest variableTail");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR,
				Constants.EQ})){
			System.out.println("variableTail -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean idnest() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.ID })){
			if(match(Constants.ID) && indiceList()){ 
				System.out.println("idnest -> 'id' indiceList");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean indiceList() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENSQBRACKET })){
			if(indice() && indiceList()){ 
				System.out.println("indiceList -> indice indiceList");
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
					System.out.println("indiceList -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean indice() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENSQBRACKET })){
			if(match(Constants.OPENSQBRACKET) && arithExpr() && match(Constants.CLOSESQBRACKET)){
				System.out.println("indice -> '[' arithExpr ']'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean arraySizeList() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET})){
			if(arraySize() && arraySizeList()){
				System.out.println("arraySizeList -> arraySize arraySizeList");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.CLOSEPAR,
				Constants.SEMICOLON,
				Constants.COMMA})){
			System.out.println("arraySizeList -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean arraySize() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET})){
			if(match(Constants.OPENSQBRACKET) 
//					&& match(Constants.RESERVED_WORD_INT)
					&& match(Constants.NUM)
					&& match(Constants.CLOSESQBRACKET)){
				System.out.println("arraySize -> '[' 'num' ']'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean type() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.ID })){
			if(match(Constants.ID)){ 
				System.out.println("type -> 'id'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_INT,
				Constants.RESERVED_WORD_FLOAT})){
			if(nonidtype()){ 
				System.out.println("type -> nonidtype");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean nonidtype() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{Constants.RESERVED_WORD_INT})){
			if(match(Constants.RESERVED_WORD_INT)){ 
				System.out.println("type -> 'int'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_FLOAT })){
			if(match(Constants.RESERVED_WORD_FLOAT)){ 
				System.out.println("type -> 'float'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean fParams() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_FLOAT,
				Constants.ID,
				Constants.RESERVED_WORD_INT})) {
			if(type() && match(Constants.ID) && arraySizeList() && fParamsTailList()){
				System.out.println("fParams -> type 'id' arraySizeList fParamsTailList");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR })) {
			System.out.println("fParams -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean aParams() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.NUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS})) {
			if(expr() && aParamsTails()){
				System.out.println("aParams -> expr aParamsTails");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR })) {
			System.out.println("aParams -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean fParamsTailList() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.COMMA})) {
			if(fParamsTail() && fParamsTailList()){
				System.out.println("fParamsTailList -> fParamsTail fParamsTailList");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR })) {
			System.out.println("fParamsTailList -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean fParamsTail() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.COMMA})) {
			if(match(Constants.COMMA) && type() && match(Constants.ID) && arraySizeList()){
				System.out.println("fParamsTail -> ',' type 'id' arraySizeList");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean aParamsTails() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.COMMA})) {
			if(aParamsTail() && aParamsTails()){
				System.out.println("aParamsTails -> aParamsTail aParamsTails");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.CLOSEPAR })) {
			System.out.println("aParamsTails -> EPSILON");
		} else {
			error = true;
		}
		return !error;
	}

	private boolean aParamsTail() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.COMMA})) {
			if(match(Constants.COMMA) && expr()){
				System.out.println("aParamsTail -> ',' expr");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean assignOp() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.EQ })) {
			if(match(Constants.EQ)){
				System.out.println("assignOp -> '='");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean relOp() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.GT })) {
			if(match(Constants.GT)){
				System.out.println("relOp -> '>'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.LT })) {
			if(match(Constants.LT )){
				System.out.println("relOp -> '<'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.LESSEQ })) {
			if(match(Constants.LESSEQ )){
				System.out.println("relOp -> '<='");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.GREATEQ })) {
			if(match(Constants.GREATEQ )){
				System.out.println("relOp -> '>='");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.NOTEQ })) {
			if(match(Constants.NOTEQ )){
				System.out.println("relOp -> '<>'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.EQCOMP })) {
			if(match(Constants.EQCOMP )){
				System.out.println("relOp -> '=='");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean addOp() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.PLUS })) {
			if(match(Constants.PLUS)){
				System.out.println("addOp -> '+'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.MINUS })) {
			if(match(Constants.MINUS )){
				System.out.println("addOp -> '-'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_OR })) {
			if(match(Constants.RESERVED_WORD_OR )){
				System.out.println("addOp -> 'or'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}

	private boolean multOp() throws IOException{
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.MULTIPLY })) {
			if(match(Constants.MULTIPLY)){
				System.out.println("multOp -> '*'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.DIV })) {
			if(match(Constants.DIV )){
				System.out.println("multOp -> '/'");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_AND })) {
			if(match(Constants.RESERVED_WORD_AND )){
				System.out.println("multOp -> 'and'");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean match(String strToMatch) throws IOException{
		boolean match = false;
		if(strToMatch.equalsIgnoreCase(lookAhead)){
			match = true;
			lookAhead = getLookAhead(lex.getNextToken());				
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
	
	private String getLookAhead(Token token) {
		String lookAhead = Constants.DOLLAR;
		if(token != null){
			lookAhead = token.getType();
			if(token.getType().equalsIgnoreCase(Constants.RESERVED_WORD)){
				lookAhead = token.getValue();
			}
		}

		return lookAhead;
	}

}
