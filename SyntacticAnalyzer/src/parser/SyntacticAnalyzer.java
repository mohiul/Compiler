package parser;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import lex.Constants;
import lex.LexicalAnalyzer;
import lex.Token;
import sdt.AParams;
import sdt.AParamsTail;
import sdt.AParamsTails;
import sdt.ArithExpr;
import sdt.ArithExprTail;
import sdt.ArraySize;
import sdt.ArraySizeList;
import sdt.AssignStat;
import sdt.Expression;
import sdt.Factor;
import sdt.FactorTail;
import sdt.FactorTail2;
import sdt.FuncDefTail;
import sdt.Idnest;
import sdt.Indice;
import sdt.IndiceList;
import sdt.RelExprTail;
import sdt.StatmentTail;
import sdt.Term;
import sdt.TermTail;
import sdt.Type;
import sdt.VarDeclStatTail;
import sdt.VarDeclTail;
import sdt.VarDefTail;
import sdt.VarFuncDefTail;
import sdt.Variable;
import sdt.VariableTail;
import sdt.VariableTail1;
import sdt.VariableTail2;
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
	CodeGenerator codeGenerator;
	String codeFilename;
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
		if(codeGenerator != null) codeGenerator.closeWriter();
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
			codeGenerator = new CodeGenerator(codeFilename, tableHandler);
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
//		To pass type and id down the tree
		VarFuncDefTail varFuncDefTail = new VarFuncDefTail();
		varFuncDefTail.type = type;
		varFuncDefTail.id = id;
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
					&& varFuncDefTail(varFuncDefTail)){
				if(secondPass){
					grammarWriter.write("varFuncDef	-> type 'id' varFuncDefTail\n");
				}
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varFuncDefTail(VarFuncDefTail varFuncDefTail) throws IOException{
		if (!skipErrors(new String[] { Constants.OPENSQBRACKET, 
				Constants.SEMICOLON,
				Constants.OPENPAR },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.OPENSQBRACKET, 
				Constants.SEMICOLON})){
//			To pass type and id down the tree
			VarDefTail varDefTail = new VarDefTail();
			varDefTail.type = varFuncDefTail.type;
			varDefTail.id = varFuncDefTail.id;
			if(varDefTail(varDefTail)){
				if(secondPass) grammarWriter.write("varFuncDefTail -> varDefTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR})) {
//			To pass type and id down the tree
			FuncDefTail funcDefTail = new FuncDefTail();
			funcDefTail.type = varFuncDefTail.type;
			funcDefTail.id = varFuncDefTail.id;
			if(funcDefTail(funcDefTail)){
				if(secondPass) grammarWriter.write("varFuncDefTail -> funcDefTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varDefTail(VarDefTail varDefTail) throws IOException{
		if (!skipErrors(new String[] { Constants.OPENSQBRACKET, 
				Constants.SEMICOLON },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{
				Constants.OPENSQBRACKET, 
				Constants.SEMICOLON})){
			ArraySizeList arraySizeList = new ArraySizeList();
			if(arraySizeList(arraySizeList)
					&& match(Constants.SEMICOLON)
					&& tableHandler.createVariableEntry(varDefTail.type, 
							varDefTail.id, 
							arraySizeList.getArraySizeList())){
				if(secondPass){
					grammarWriter.write("varDefTail	-> arraySizeList ';'\n");
					codeGenerator.genCodeCreateVariable(varDefTail.id);
				}
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean funcDefTail(FuncDefTail funcDefTail) throws IOException{
		if (!skipErrors(new String[] { Constants.OPENPAR },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR })) {
			if(match(Constants.OPENPAR) 
					&& tableHandler.createFunctionEntryAndTable(funcDefTail.type, funcDefTail.id)
					&& fParams()
					&& match(Constants.CLOSEPAR)
					&& funcBody()
					&& match(Constants.SEMICOLON)
					&& tableHandler.delFuncTable(funcDefTail.id)){
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
			Token type = new Token();
			VarDeclTail varDeclTail = new VarDeclTail();
			varDeclTail.type = type;
			if(nonidtype(type) && varDeclTail(varDeclTail)){
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
			Token id = new Token();
			VarDeclStatTail varDeclStatTail = new VarDeclStatTail();
			varDeclStatTail.id = id;
			if(match(Constants.ID, id)
					&& varDeclStatTail(varDeclStatTail)){
				if(secondPass) grammarWriter.write("varDeclStat -> 'id' varDeclStatTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}

	private boolean varDeclStatTail(VarDeclStatTail varDeclStatTail) throws IOException{
		if (!skipErrors(new String[] { Constants.ID, 
				Constants.OPENSQBRACKET,
				Constants.POINT,
				Constants.EQ },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			VarDeclTail varDeclTail = new VarDeclTail();
			varDeclTail.type = varDeclStatTail.id;
			if(tableHandler.checkClassExists(varDeclStatTail.id) && varDeclTail(varDeclTail)){
				if(secondPass) grammarWriter.write("varDeclStatTail -> varDeclTail\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET,
				Constants.POINT,
				Constants.EQ})){
			StatmentTail statmentTail = new StatmentTail();
			statmentTail.id = varDeclStatTail.id;
			Type type = new Type();
			statmentTail.downType = type;
			if(tableHandler.checkVariableExists(varDeclStatTail.id, type) 
					&& statmentTail(statmentTail)){
				if(secondPass) grammarWriter.write("varDeclStatTail -> statmentTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean varDeclTail(VarDeclTail varDeclTail) throws IOException{
		if (!skipErrors(new String[] { Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			Token id = new Token();
			ArraySizeList arraySizeList = new ArraySizeList();
			if(match(Constants.ID, id) 
					&& arraySizeList(arraySizeList)
					&& match(Constants.SEMICOLON)
					&& tableHandler.createVariableEntry(varDeclTail.type, id, arraySizeList.getArraySizeList())){
				if(secondPass){
					grammarWriter.write("varDeclTail -> id arraySizeList ;\n");
					codeGenerator.genCodeCreateVariable(id);
				}
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean statmentTail(StatmentTail statmentTail) throws IOException{
		if (!skipErrors(new String[] { Constants.OPENSQBRACKET,
				Constants.POINT,
				Constants.EQ },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET,
				Constants.POINT,
				Constants.EQ})){
			VariableTail1 variableTail1 = new VariableTail1();
			variableTail1.upType = statmentTail.downType;
			Expression expression = new Expression();
			variableTail1.id = statmentTail.id;
			Token assignOp = new Token();
			if(variableTail1(variableTail1)
					&& assignOp(assignOp)
					&& expr(expression)
					&& tableHandler.checkCompatableType(variableTail1.upType, expression.arithExpr.upType, assignOp)
					&& match(Constants.SEMICOLON)){
				if(secondPass){
					grammarWriter.write("statmentTail -> variableTail1 assignOp expr ';'\n");
//					TODO Create and assignment statement
					if(variableTail1.id != null){
						if(expression.arithExpr != null
								&& expression.arithExpr.term != null
								&& expression.arithExpr.term.factor != null){
							if(expression.arithExpr.term.factor.tempVar != null){
								codeGenerator.genCodeAssignment(variableTail1.id, expression.arithExpr.term.factor.tempVar);
							} else if(expression.arithExpr.term.factor.upNum != null){
								codeGenerator.genCodeAssignment(variableTail1.id, expression.arithExpr.term.factor.upNum);
							} else if(expression.arithExpr.term.factor.upId != null){
								codeGenerator.genCodeAssignment(variableTail1.id, expression.arithExpr.term.factor.upId);
							} 
						}
						
					}
				}
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean variableTail1(VariableTail1 variableTail1) throws IOException{
		if (!skipErrors(new String[] { Constants.OPENSQBRACKET,
				Constants.POINT },
				new String[] { Constants.EQ }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET,
				Constants.POINT})){
			IndiceList indiceList = new IndiceList();
			VariableTail2 variableTail2 = new VariableTail2();
			variableTail2.id = variableTail1.id;
			variableTail2.indiceList = indiceList;
			Type type = new Type();
			variableTail1.upType = type;
			if(indiceList(indiceList)
					&& tableHandler.checkVariableExists(variableTail1.id, indiceList.getNoOfDim(), type)
					&& variableTail2(variableTail2)){
				if(secondPass){
					grammarWriter.write("variableTail1 -> indiceList variableTail2\n");
					if(variableTail2.upType != null && variableTail2.upType.typeName != null){
						Type.copyType(variableTail1.upType, variableTail2.upType);
					}
				}
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
	
	private boolean variableTail2(VariableTail2 variableTail2) throws IOException{
		if (!skipErrors(new String[] { Constants.POINT },
				new String[] { Constants.EQ }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.POINT})){
			Token id = new Token();
			IndiceList indiceList = new IndiceList();
			VariableTail2 variableTail2_1 = new VariableTail2();
			variableTail2_1.id = id;
			variableTail2_1.indiceList = indiceList;
			Type type = new Type();
			variableTail2.upType = type;
			if(match(Constants.POINT) 
					&& match(Constants.ID, id)
					&& tableHandler.checkVariableInClassExists(variableTail2.id, id, type)
					&& indiceList(indiceList) 
					&& tableHandler.checkVariableInClassExists(variableTail2.id, id, indiceList.getNoOfDim(), type) 
					&& variableTail2(variableTail2_1)){
				if(secondPass){
					grammarWriter.write("variableTail2 -> '.' 'id' indiceList variableTail2\n");
					if(variableTail2_1.upType != null && variableTail2_1.upType.typeName != null){
						Type.copyType(variableTail2.upType, variableTail2_1.upType);
					}
				}
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
			AssignStat assignStat = new AssignStat();
			if(assignStat(assignStat) && match(Constants.SEMICOLON)){
				if(secondPass){
					grammarWriter.write("statement -> assignStat ';'\n");
//					TODO Generate code for assignment statement
				}
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
			Expression expression = new Expression();
			if(match(Constants.RESERVED_WORD_RETURN) 
					&& match(Constants.OPENPAR)
					&& expr(expression)
					&& match(Constants.CLOSEPAR)
					&& match(Constants.SEMICOLON)){
				if(secondPass){
					grammarWriter.write("statement -> 'return' '(' expr ')' ';'\n");
//					TODO Generate code for return
				}
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_PUT })){
			Expression expression = new Expression();
			if(match(Constants.RESERVED_WORD_PUT) 
					&& match(Constants.OPENPAR)
					&& expr(expression)
					&& match(Constants.CLOSEPAR)
					&& match(Constants.SEMICOLON)){
				if(secondPass){
					grammarWriter.write("statement -> 'put' '(' expr ')' ';'\n");
//					TODO: Generate code for put
				}
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_GET })){
			Variable variable = new Variable();
			if(match(Constants.RESERVED_WORD_GET) 
					&& match(Constants.OPENPAR)
					&& variable(variable)
					&& match(Constants.CLOSEPAR)
					&& match(Constants.SEMICOLON)){
				if(secondPass){
					grammarWriter.write("statement -> 'get' '(' variable ')' ';'\n");
//					TODO Generate code for get
				}
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_IF })){
			Expression expression = new Expression();
			if(match(Constants.RESERVED_WORD_IF)
					&& match(Constants.OPENPAR)
					&& expr(expression)
					&& match(Constants.CLOSEPAR)
					&& match(Constants.RESERVED_WORD_THEN)
					&& statBlock()
					&& match(Constants.RESERVED_WORD_ELSE)
					&& statBlock()
					&& match(Constants.SEMICOLON)){
				if(secondPass){
					grammarWriter.write("statement -> 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'\n");
//					TODO Generate code for if
				}
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_FOR })){
			Token type = new Token();
			Token id = new Token();
			Expression expression = new Expression();
			Expression expression1 = new Expression();
			ArithExpr arithExpr = new ArithExpr();
			Token relOp = new Token();
			AssignStat assignStat = new AssignStat();
			Token assignOp = new Token(); 
			if(match(Constants.RESERVED_WORD_FOR)
					&& match(Constants.OPENPAR)
					&& type(type)
					&& match(Constants.ID, id)
					&& tableHandler.createVariableEntry(type, id, null)
					&& assignOp(assignOp)
					&& expr(expression)
					&& tableHandler.checkCompatableType(new Type(type), expression.arithExpr.upType, assignOp)
					&& match(Constants.SEMICOLON)
					&& arithExpr(arithExpr)
					&& relOp(relOp)
					&& expr(expression1)
					&& tableHandler.checkCompatableType(arithExpr.upType, expression1.arithExpr.upType, relOp)
					&& match(Constants.SEMICOLON)
					&& assignStat(assignStat)
					&& match(Constants.CLOSEPAR)
					&& statBlock()
					&& match(Constants.SEMICOLON)){
				if(secondPass){
					grammarWriter.write("statement -> 'for' '(' type 'id' assignOp expr ';' arithExpr relOp expr ';' assignStat ')' statBlock ';'\n");
					codeGenerator.genCodeCreateVariable(id);
					if(id != null){
						if(expression.arithExpr != null
								&& expression.arithExpr.term != null
								&& expression.arithExpr.term.factor != null){
							if(expression.arithExpr.term.factor.tempVar != null){
								codeGenerator.genCodeAssignment(id, expression.arithExpr.term.factor.tempVar);
							} else if(expression.arithExpr.term.factor.upNum != null){
								codeGenerator.genCodeAssignment(id, expression.arithExpr.term.factor.upNum);
							} else if(expression.arithExpr.term.factor.upId != null){
								codeGenerator.genCodeAssignment(id, expression.arithExpr.term.factor.upId);
							}
						}
					}
//					TODO Generate code for for
				}
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean assignStat(AssignStat assignStat) throws IOException{
		if (!skipErrors(new String[] { Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.ID})){
			Variable variable = new Variable();
			Expression expression = new Expression();
			assignStat.variable = variable;
			assignStat.expression = expression;
			Token assignOp = new Token();
			if(variable(variable) 
					&& assignOp(assignOp) 
					&& expr(expression)
					&& tableHandler.checkCompatableType(variable.upType, expression.arithExpr.upType, assignOp)){
				if(secondPass) {
					grammarWriter.write("assignStat -> variable assignOp expr\n");
					if(variable.upIdnest != null
							&& variable.upIdnest.id != null){
						if(expression.arithExpr != null
								&& expression.arithExpr.term != null
								&& expression.arithExpr.term.factor != null){
							if(expression.arithExpr.term.factor.tempVar != null){
								codeGenerator.genCodeAssignment(variable.upIdnest.id, expression.arithExpr.term.factor.tempVar);
							} else if(expression.arithExpr.term.factor.upNum != null){
								codeGenerator.genCodeAssignment(variable.upIdnest.id, expression.arithExpr.term.factor.upNum);
							} else if(expression.arithExpr.term.factor.upId != null){
								codeGenerator.genCodeAssignment(variable.upIdnest.id, expression.arithExpr.term.factor.upId);
							}
						}
					}
				}
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

	private boolean expr(Expression expression) throws IOException{
		if (!skipErrors(new String[] { Constants.OPENPAR,
				Constants.ID,
				Constants.FLOATNUM,
				Constants.INTEGERNUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.FLOATNUM,
				Constants.INTEGERNUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS})) {
			ArithExpr arithExpr = new ArithExpr();
			RelExprTail relExprTail = new RelExprTail();
			expression.arithExpr = arithExpr;
			expression.relExprTail = relExprTail;
			relExprTail.downArithExpr = arithExpr;
			if(arithExpr(arithExpr) && relExprTail(relExprTail)){
				if(secondPass) grammarWriter.write("expr -> arithExpr relExprTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean relExprTail(RelExprTail relExprTail) throws IOException{
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
			Token relOp = new Token();
			Expression expression = new Expression();
			relExprTail.relOp = relOp;
			relExprTail.expression = expression;
			if(relOp(relOp) 
					&& expr(expression)
					&& tableHandler.checkCompatableType(relExprTail.downArithExpr.upType, expression.arithExpr.upType, relOp)){
				if(secondPass){
					grammarWriter.write("relExprTail -> relOp expr\n");
					Factor f1 = null;
					Factor f2 = null;
					if(relExprTail.downArithExpr != null
							&& relExprTail.downArithExpr.term != null
							&& relExprTail.downArithExpr.term.factor != null){
						f1 = relExprTail.downArithExpr.term.factor;
					}
					if(expression.arithExpr != null
							&& expression.arithExpr.term != null
							&& expression.arithExpr.term.factor != null){
						f2 = expression.arithExpr.term.factor;
					}
					if(f1!= null && f2 != null){
						codeGenerator.genCodeRelOperation(f1, f2, relOp);
					}
				}
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
	
	private boolean arithExpr(ArithExpr arithExpr) throws IOException{
		if (!skipErrors(new String[] { Constants.OPENPAR,
				Constants.ID,
				Constants.FLOATNUM,
				Constants.INTEGERNUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.FLOATNUM,
				Constants.INTEGERNUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS})) {
			Term term = new Term();
			Type type = new Type();
			term.upType = type;
			ArithExprTail arithExprTail = new ArithExprTail();
			arithExpr.upType = type;
			arithExpr.term = term;
			arithExpr.arithExprTail = arithExprTail;
			arithExprTail.downTerm = term;
			if(term(term) && arithExprTail(arithExprTail)){
				if(secondPass){
					grammarWriter.write("arithExpr -> term arithExprTail\n");
					Type.copyType(arithExpr.upType,term.upType);
				}
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean arithExprTail(ArithExprTail arithExprTail) throws IOException{
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
			Token addOp = new Token();
			Term term = new Term();
			term.upType = new Type();
			ArithExprTail arithExprTail1 = new ArithExprTail();
			arithExprTail1.downTerm = term;
			arithExprTail.addOp = addOp;
			arithExprTail.upTerm = term;
			arithExprTail.arithExprTail = arithExprTail1;
			if(addOp(addOp) 
					&& term(term)
					&& tableHandler.checkCompatableType(arithExprTail.downTerm.upType, term.upType, addOp)
					&& arithExprTail(arithExprTail1)){
				if(secondPass){
					grammarWriter.write("arithExprTail -> addOp term arithExprTail\n");
					Factor f1 = null;
					Factor f2 = null;
					if(arithExprTail.downTerm != null
							&& arithExprTail.downTerm.factor != null){
						f1 = arithExprTail.downTerm.factor;
					}
					if(term.factor != null){
						f2 = term.factor;
					}
					if(f1!= null && f2 != null){
						codeGenerator.genCodeOperation(f1, f2, addOp);
					}					
				}
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

	private boolean sign(Token sign) throws IOException{
		error = !skipErrors(new String[] { Constants.PLUS,
				Constants.MINUS },
				new String[] { });
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.PLUS })) {
			if(match(Constants.PLUS, sign)){
				if(secondPass) grammarWriter.write("sign -> '+'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.MINUS })) {
			if(match(Constants.MINUS, sign)){
				if(secondPass) grammarWriter.write("sign -> '-'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean term(Term term) throws IOException{
		if (!skipErrors(new String[] { Constants.OPENPAR,
				Constants.ID,
				Constants.FLOATNUM,
				Constants.INTEGERNUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.FLOATNUM,
				Constants.INTEGERNUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS})) {
			Factor factor = new Factor();
			TermTail termTail = new TermTail();
			termTail.downFactor = factor;
			term.factor = factor;
			term.termTail = termTail;
			if(factor(factor) && termTail(termTail)){
				if(secondPass){
					grammarWriter.write("term -> factor termTail\n");
					if(term.upType == null) term.upType = new Type();
					if(factor.upType == null) factor.upType = new Type();
					Type.copyType(term.upType, factor.upType);
				}
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean termTail(TermTail termTail) throws IOException{
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
			Token multOp = new Token();
			Factor factor = new Factor();
			TermTail termTail1 = new TermTail();
			termTail1.downFactor = factor;
			if(multOp(multOp) 
					&& factor(factor)
					&& tableHandler.checkCompatableType(termTail.downFactor.upType, factor.upType, multOp)
					&& termTail(termTail1)){
				if(secondPass) {
					grammarWriter.write("termTail -> multOp factor termTail\n");
					Factor f1 = null;
					Factor f2 = null;
					if(termTail.downFactor != null){
						f1 = termTail.downFactor;
					}
					f2 = factor;
					if(f1!= null && f2 != null){
						codeGenerator.genCodeOperation(f1, f2, multOp);
					}
				}
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

	private boolean factor(Factor factor) throws IOException{
		if (!skipErrors(new String[] { Constants.ID,
				Constants.FLOATNUM,
				Constants.INTEGERNUM,
				Constants.OPENPAR,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.ID })){
			Token id = new Token();
			FactorTail factorTail = new FactorTail();
			factorTail.downId = id;
			factor.upId = id;
			factor.factorTail = factorTail;
			Type type = new Type();
			factor.upType = type;
			if(match(Constants.ID, id)
					&& tableHandler.checkVariableExists(id, type)
					&& factorTail(factorTail)
					&& tableHandler.checkVariableExists(id, factorTail.upIndiceListToCalc.getNoOfDim(), type)){
				if(secondPass){
					grammarWriter.write("factor -> 'id' factorTail\n");
					if(factorTail.upType != null && factorTail.upType.typeName != null){
						Type.copyType(factor.upType, factorTail.upType);
					}
				}
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.INTEGERNUM, Constants.FLOATNUM })){
			Token num = new Token();
			factor.upNum = num;
			if(num(num)){
				if(secondPass) {
					grammarWriter.write("factor -> num\n");
					factor.upType = new Type();
					factor.upType.typeName = num.getType();
				}
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR })){
			ArithExpr arithExpr = new ArithExpr();
			factor.upArithExpr = arithExpr;
			if(match(Constants.OPENPAR) && arithExpr(arithExpr) && match(Constants.CLOSEPAR)){
				if(secondPass) {
					grammarWriter.write("factor -> '(' arithExpr ')'\n");
					if(factor.upType == null) factor.upType = new Type();
					if(arithExpr.upType == null) arithExpr.upType = new Type();
					Type.copyType(factor.upType, arithExpr.upType);
				}
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_NOT })){
			Factor factor1 = new Factor();
			factor.factor = factor1;
			Token not = new Token();
			if(match(Constants.RESERVED_WORD_NOT, not) && factor(factor1)){
				if(secondPass){
					grammarWriter.write("factor -> 'not' factor\n");
					if(factor.upType == null) factor.upType = new Type();
					if(factor1.upType == null) factor1.upType = new Type();
					Type.copyType(factor.upType, factor1.upType);
//					TODO Generate code for Not factor
					codeGenerator.genCodeNotOperation(factor1, not);
					factor.tempVar = factor1.tempVar;
				}
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.PLUS, Constants.MINUS })){
			Token sign = new Token();
			Factor factor1 = new Factor();
			factor1.factor = factor1;
			if(sign(sign) && factor(factor1)){
				if(secondPass){
					grammarWriter.write("factor -> sign factor\n");
					if(factor.upType == null) factor.upType = new Type();
					if(factor1.upType == null) factor1.upType = new Type();
					Type.copyType(factor.upType, factor1.upType);
//					TODO Generate code for sign factor
				}
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean factorTail(FactorTail factorTail) throws IOException{
		IndiceList indiceListToCalc = new IndiceList();
		factorTail.upIndiceListToCalc = indiceListToCalc;
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
			Token id = new Token();
			FactorTail factorTail1 = new FactorTail();
			factorTail1.downId = id;
			factorTail.factorTail = factorTail1;
			Type type = new Type();
			factorTail.upType = type;
			if( match(Constants.POINT)
					&& match(Constants.ID, id)
					&& tableHandler.checkVariableInClassExists(factorTail.downId, id, type)
					&& factorTail(factorTail1)
					&& tableHandler.checkVariableInClassExists(factorTail.downId, id, factorTail1.upIndiceListToCalc.getNoOfDim(), type)){
				if(secondPass) {
					grammarWriter.write("factorTail -> '.' 'id' factorTail\n");
					if(factorTail1.upType != null && factorTail1.upType.typeName != null){
						Type.copyType(factorTail.upType, factorTail1.upType);
					}
				}
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENSQBRACKET })){
//			Create indiceList from indice and indiceList
			Indice indice = new Indice();
			IndiceList indiceList = new IndiceList();
			indiceListToCalc.indice = indice;
			indiceListToCalc.indiceList = indiceList;
			
			FactorTail2 factorTail2 = new FactorTail2();
			factorTail2.downId = factorTail.downId;
			factorTail2.downIndiceList = indiceListToCalc;
			factorTail.factorTail2 = factorTail2;
			
			if(indice(indice) 
					&& indiceList(indiceList)
					&& factorTail2(factorTail2)){
				if(secondPass) {
					grammarWriter.write("factorTail -> indice indiceList factorTail2\n");
					if(factorTail2.upType != null && factorTail2.upType.typeName != null){
						if(factorTail.upType == null) factorTail.upType = new Type();
						Type.copyType(factorTail.upType, factorTail2.upType);
					}
				}
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR })){
			AParams aParams = new AParams();
			factorTail.upAParams = aParams;
			aParams.downId = factorTail.downId;
			if(match(Constants.OPENPAR) 
					&& aParams(aParams) 
					&& match(Constants.CLOSEPAR)){
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
	
	private boolean factorTail2(FactorTail2 factorTail2) throws IOException{
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
			Token id = new Token();
			FactorTail factorTail = new FactorTail();
			factorTail.downId = id;
			factorTail2.upId = id;
			factorTail2.upFactorTail = factorTail;
			Type type = new Type();
			factorTail2.upType = type;
			if( match(Constants.POINT)
					&& match(Constants.ID, id)
					&& tableHandler.checkVariableInClassExists(factorTail2.downId, id, type)
					&& factorTail(factorTail)
					&& tableHandler.checkVariableInClassExists(factorTail2.downId, id, factorTail.upIndiceListToCalc.getNoOfDim(), type)){
				if(secondPass){
					grammarWriter.write("factorTail2 -> '.' 'id' factorTail\n");
					if(factorTail.upType != null && factorTail.upType.typeName != null){
						Type.copyType(factorTail2.upType, factorTail.upType);
					}
				}
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

	private boolean variable(Variable variable) throws IOException{
		if (!skipErrors(new String[] { Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.ID })){
			Idnest idnest = new Idnest();
			VariableTail variableTail = new VariableTail();
			variableTail.downIdnest = idnest;
			variable.upIdnest = idnest;
			variable.upVariableTail = variableTail;
			Type type = new Type();
			variable.upType = type;
			if(idnest(idnest)
					&& tableHandler.checkVariableExists(idnest.id, idnest.indiceList.getNoOfDim(), type)
					&& variableTail(variableTail)){ 
				if(secondPass) grammarWriter.write("variable -> idnest variableTail\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean variableTail(VariableTail variableTail) throws IOException{
		if (!skipErrors(new String[] { Constants.POINT },
				new String[] { Constants.CLOSEPAR, Constants.EQ }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.POINT })){
			Idnest idnest = new Idnest();
			VariableTail variableTail1 = new VariableTail();
			variableTail.upIdnest = idnest;
			variableTail.variableTail = variableTail1;
			Type type = new Type();
			variableTail.upType = type;
			if(match(Constants.POINT) 
					&& idnest(idnest)
					&& tableHandler.checkVariableInClassExists(variableTail.downIdnest.id, idnest.id, idnest.indiceList.getNoOfDim(), type)
					&& variableTail(variableTail1)){ 
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
	
	private boolean idnest(Idnest idnest) throws IOException{
		if (!skipErrors(new String[] { Constants.ID },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.ID })){
			Token id = new Token();
			IndiceList indiceList = new IndiceList();
			idnest.id = id;
			idnest.indiceList = indiceList;
			if(match(Constants.ID, id)
					&& indiceList(indiceList)){ 
				if(secondPass) grammarWriter.write("idnest -> 'id' indiceList\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean indiceList(IndiceList indiceList) throws IOException{
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
			Indice indice = new Indice();
			IndiceList indiceList1 = new IndiceList();
			indiceList.indice = indice;
			indiceList.indiceList = indiceList1;
			if(indice(indice) && indiceList(indiceList1)){ 
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

	private boolean indice(Indice indice) throws IOException{
		if (!skipErrors(new String[] { Constants.OPENSQBRACKET },
				new String[] { }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENSQBRACKET })){
			ArithExpr arithExpr = new ArithExpr();
			indice.arithExpr = arithExpr;
			if(match(Constants.OPENSQBRACKET) && arithExpr(arithExpr) && match(Constants.CLOSESQBRACKET)){
				if(secondPass) grammarWriter.write("indice -> '[' arithExpr ']'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean arraySizeList(ArraySizeList arraySizeList) throws IOException{
		if (!skipErrors(new String[] { Constants.OPENSQBRACKET },
				new String[] { Constants.CLOSEPAR, Constants.SEMICOLON, Constants.COMMA }))
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET})){
			ArraySize arraySize = new ArraySize();
			ArraySizeList arraySizeList1 = new ArraySizeList();
			arraySizeList.arraySize = arraySize;
			arraySizeList.arraySizeList = arraySizeList1;
			if(arraySize(arraySize) && arraySizeList(arraySizeList1)){
				if(secondPass) grammarWriter.write("arraySizeList -> arraySize arraySizeList\n");
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
	
	private boolean arraySize(ArraySize arraySize) throws IOException{
		if ( !skipErrors(new String[]{ Constants.OPENSQBRACKET },
				new String[]{ }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{Constants.OPENSQBRACKET})){
			Token intnum = new Token();
			arraySize.intnum = intnum;
			if(match(Constants.OPENSQBRACKET)
					&& match(Constants.INTEGERNUM, intnum)
					&& match(Constants.CLOSESQBRACKET)){
				if(secondPass) grammarWriter.write("arraySize -> '[' 'intnum' ']'\n");
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
	
	private boolean num(Token num) throws IOException{
		if ( !skipErrors(new String[]{ Constants.INTEGERNUM,
				Constants.FLOATNUM },
				new String[]{ }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.INTEGERNUM })){
			if(match(Constants.INTEGERNUM, num)){ 
				if(secondPass) grammarWriter.write("num -> 'intnum'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{Constants.FLOATNUM})){
			if(match(Constants.FLOATNUM, num)){ 
				if(secondPass) grammarWriter.write("num -> 'floatnum'\n");
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
		if ( !skipErrors(new String[]{ Constants.RESERVED_WORD_FLOAT,
				Constants.ID,
				Constants.RESERVED_WORD_INT }, 
				new String[]{ Constants.CLOSEPAR }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_FLOAT,
				Constants.ID,
				Constants.RESERVED_WORD_INT })) {
			Token type = new Token();
			Token id = new Token();
			ArraySizeList arraySizeList = new ArraySizeList();
			if(type(type) 
					&& match(Constants.ID, id)
					&& arraySizeList(arraySizeList)
					&& tableHandler.createParameterEntry(type, id, arraySizeList.getArraySizeList())
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
	
	private boolean aParams(AParams aParams) throws IOException{
		if ( !skipErrors(new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.INTEGERNUM,
				Constants.FLOATNUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS }, 
				new String[]{ Constants.CLOSEPAR }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.OPENPAR,
				Constants.ID,
				Constants.INTEGERNUM,
				Constants.FLOATNUM,
				Constants.RESERVED_WORD_NOT,
				Constants.PLUS,
				Constants.MINUS})) {
			Expression expression = new Expression();
			AParamsTails aParamsTails = new AParamsTails();
			aParams.expression = expression;
			aParams.aParamsTails = aParamsTails;
			if(expr(expression) 
					&& aParamsTails(aParamsTails)
					&& tableHandler.checkFuncParams(aParams.downId, aParams.getExprList())){
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
		if ( !skipErrors(new String[]{ Constants.COMMA }, 
				new String[]{ }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.COMMA})) {
			Token type = new Token();
			Token id = new Token();
			ArraySizeList arraySizeList = new ArraySizeList();
			if(match(Constants.COMMA) 
					&& type(type) 
					&& match(Constants.ID, id) 
					&& arraySizeList(arraySizeList) 
					&& tableHandler.createParameterEntry(type, id, arraySizeList.getArraySizeList())){
				if(secondPass) grammarWriter.write("fParamsTail -> ',' type 'id' arraySizeList\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean aParamsTails(AParamsTails aParamsTails) throws IOException{
		if ( !skipErrors(new String[]{ Constants.COMMA }, 
				new String[]{ Constants.CLOSEPAR }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.COMMA})) {
			AParamsTail aParamsTail = new AParamsTail();
			AParamsTails aParamsTails1 = new AParamsTails();
			aParamsTails.aParamsTail = aParamsTail;
			aParamsTails.aParamsTails = aParamsTails1;
			if(aParamsTail(aParamsTail) && aParamsTails(aParamsTails1)){
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

	private boolean aParamsTail(AParamsTail aParamsTail) throws IOException{
		if ( !skipErrors(new String[]{ Constants.COMMA }, 
				new String[]{ }) )
			return false;
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.COMMA})) {
			Expression expression = new Expression();
			aParamsTail.expression = expression;
			if(match(Constants.COMMA) && expr(expression)){
				if(secondPass) grammarWriter.write("aParamsTail -> ',' expr\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean assignOp(Token assignOp) throws IOException{
		error = !skipErrors(new String[]{ Constants.EQ }, 
				new String[]{ });
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.EQ })) {
			if(match(Constants.EQ, assignOp)){
				if(secondPass) grammarWriter.write("assignOp -> '='\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean relOp(Token relOp) throws IOException{
		error = !skipErrors(new String[]{ Constants.GT, 
				Constants.LT, 
				Constants.LESSEQ,
				Constants.GREATEQ,
				Constants.NOTEQ,
				Constants.EQCOMP}, 
				new String[]{ });
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.GT })) {
			if(match(Constants.GT, relOp)){
				if(secondPass) grammarWriter.write("relOp -> '>'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.LT })) {
			if(match(Constants.LT, relOp)){
				if(secondPass) grammarWriter.write("relOp -> '<'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.LESSEQ })) {
			if(match(Constants.LESSEQ, relOp)){
				if(secondPass) grammarWriter.write("relOp -> '<='\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.GREATEQ })) {
			if(match(Constants.GREATEQ, relOp)){
				if(secondPass) grammarWriter.write("relOp -> '>='\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.NOTEQ })) {
			if(match(Constants.NOTEQ, relOp)){
				if(secondPass) grammarWriter.write("relOp -> '<>'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.EQCOMP })) {
			if(match(Constants.EQCOMP, relOp)){
				if(secondPass) grammarWriter.write("relOp -> '=='\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
	private boolean addOp(Token addOp) throws IOException{
		error = !skipErrors(new String[]{ Constants.PLUS, 
				Constants.MINUS, 
				Constants.RESERVED_WORD_OR }, 
				new String[]{ });
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.PLUS })) {
			if(match(Constants.PLUS, addOp)){
				if(secondPass) grammarWriter.write("addOp -> '+'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.MINUS })) {
			if(match(Constants.MINUS, addOp)){
				if(secondPass) grammarWriter.write("addOp -> '-'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_OR })) {
			if(match(Constants.RESERVED_WORD_OR, addOp)){
				if(secondPass) grammarWriter.write("addOp -> 'or'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}

	private boolean multOp(Token multOp) throws IOException{
		error = !skipErrors(new String[]{ Constants.MULTIPLY, 
				Constants.DIV, 
				Constants.RESERVED_WORD_AND }, 
				new String[]{ });
		if(lookAheadIsIn(lookAhead, new String[]{ Constants.MULTIPLY })) {
			if(match(Constants.MULTIPLY, multOp)){
				if(secondPass) grammarWriter.write("multOp -> '*'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.DIV })) {
			if(match(Constants.DIV, multOp)){
				if(secondPass) grammarWriter.write("multOp -> '/'\n");
			} else {
				error = true;
			}
		} else if(lookAheadIsIn(lookAhead, new String[]{ Constants.RESERVED_WORD_AND })) {
			if(match(Constants.RESERVED_WORD_AND, multOp)){
				if(secondPass) grammarWriter.write("multOp -> 'and'\n");
			} else {
				error = true;
			}
		} else {
			error = true;
		}
		return !error;
	}
	
}
