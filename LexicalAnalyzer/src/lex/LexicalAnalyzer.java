package lex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import test.Utils;

public class LexicalAnalyzer {
	private File file;
	private Reader reader; 
	private String strToRead;
	private TokenIdentifier identifier;
	private StateTransitionTable table;
	private int charInt;
	private char ch;
	private int lineNo;
	private int position;
	private int state;
	private boolean continueRead;
	Writer writer;
	boolean secondPass;
	
	public LexicalAnalyzer() throws IOException {
		secondPass = false;
		identifier = new TokenIdentifier();
		table = new StateTransitionTable();

		ch = 0;
		lineNo = 1;
		position = 0;
		state = Constants.INIT_STATE;
		continueRead = true;
		
		writer = new BufferedWriter(
				new OutputStreamWriter(
				new FileOutputStream("errors.txt"), "utf-8"));
	}
	
	public void closeWriter() throws IOException{
		writer.close();
	}
	
	public void handleFile(String filePath) throws IOException {
		file = new File(filePath);
		InputStream in = new FileInputStream(file);
		reader = new BufferedReader(new InputStreamReader(in));
		readFirstChar();
	}

	public void readFirstChar() throws IOException {
		if((charInt = reader.read()) != -1){
			ch = (char) charInt;
			position++;
		}
	}

	public Token getNextToken() throws IOException {
		Token tokenToReturn = null;
		String tokenValue = "";
		int currentPosition = position;
		int currentLine = lineNo;
		boolean doOncePosition = true;
		boolean doOnceLineNo = true;
		int prevState = 0;
		while (continueRead) {
			if(ch == '\n'){ lineNo++; position = 0;}
			
			if(identifier.isTangableChar(ch)){
				if(doOncePosition){ currentPosition = position; doOncePosition = false;}
				if(doOnceLineNo){currentLine = lineNo; doOnceLineNo = false; }
			}
			
			prevState = state;
			state = table.getNextState(state, charConverter(identifier, ch));
			tokenValue += ch;
			
			int nextState = -1;
			position++;
			if((charInt = reader.read()) != -1){
				ch = (char) charInt;
				nextState = table.getNextState(state, charConverter(identifier, ch));
			} else {
				nextState = Constants.INIT_STATE;
				continueRead = false;
			}
			if (nextState == Constants.INIT_STATE && table.isFinalState(state)) {
				String token = table.getFinalState(state);
				tokenValue = tokenValue.trim();
				if (token.equalsIgnoreCase(Constants.ID) && table.isReservedWord(tokenValue)) {
					token = Constants.RESERVED_WORD;
				}
				tokenToReturn = new Token(token, tokenValue, currentLine, currentPosition);
				
				tokenValue = "";
				state = Constants.INIT_STATE;
				
				boolean initialize = false;
				if(tokenToReturn.getType().equalsIgnoreCase(Constants.COMMENT)){
					initialize = true;
				} else if(tokenToReturn.getType().equalsIgnoreCase(Constants.ERR)){
					initialize = true;
					if(!secondPass){
						handleErrorMsg(tokenToReturn.getValue(), tokenToReturn.getLineNo(), prevState, position);
					}
				} else {
					break;
				}
				if(initialize){
					tokenToReturn = null;
					tokenValue = "";
					currentPosition = position;
					currentLine = lineNo;
					doOncePosition = true;
					doOnceLineNo = true;
				}
			}
		}
		if(state != 0 && !table.isFinalState(state) && tokenValue.length() > 0){
			if(!secondPass){
				handleErrorMsg(tokenValue.trim(), currentLine, prevState, currentPosition + 1);
			}
		}
		return tokenToReturn;
	}

	private void handleErrorMsg(String tokenValue, 
			int lineNo, 
			int prevState, 
			int position) throws IOException {
		String errMsg = "";
		switch(prevState){
		case 1:
		case 4:
		case 5:
		case 6:
			errMsg = "Invalid Identifier";
			break;
		case 2:
		case 3:
		case 7:
		case 8:
		case 9:
		case 10:
		case 11:
		case 12:
			errMsg = "Invalid Number";
			break;
		case 14:
		case 15:
		case 16:
			errMsg = "Comment not ended";
			break;
		default:
			errMsg = "Invalid Character";
		}
		errMsg += " at line: " 
				+ lineNo
				+ " position: " + (position - 1)
				+ ": " + tokenValue;
		System.err.println(errMsg);
		writer.write(errMsg + "\n");
	}

	private char charConverter(TokenIdentifier identifier, char ch) {
		if (ch == ' ' || ch == '\t') {
			ch = ' ';
		} else if (identifier.isLetter(ch)) {
			ch = 'l';
		} else if (identifier.isNonZero(ch)) {
			ch = 'z';
		} else if (identifier.isDigit(ch)) {
			ch = 'd';
		}
		return ch;
	}

	public Reader getReader() {
		return reader;
	}

	public void setReader(String str) {
		this.strToRead = str;
		this.reader = Utils.getReader(str);
	}
	
	public String getStrToRead() {
		return strToRead;
	}

	public int getLineNo() {
		return lineNo;
	}

	public int getPosition() {
		return position;
	}

	public Writer getWriter() {
		return writer;
	}

	public void setSecondPass(boolean secondPass) {
		this.secondPass = secondPass;
	}

}
