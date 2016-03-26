package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import lex.Constants;
import lex.LexicalAnalyzer;
import lex.Token;

public class LexicalAnalyzerTest {

	private LexicalAnalyzer lex;
	
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

	@Before
	public void setUpStreams() {
	    System.setOut(new PrintStream(outContent));
	    System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
	    System.setOut(null);
	    System.setErr(null);
	}
	
	@Before
	public void setUp() throws Exception {
		lex = new LexicalAnalyzer();
	}
	
	@After
	public void cleanUp() throws IOException{
		lex.closeWriter();
	}

	private void testStrWithLex(String str, String[] strArray, String[] tokenArray) {
		lex.setReader(str);
		try {
			lex.readFirstChar();
			int i = 0;
			for(String tokenVal:strArray){
				Token token = lex.getNextToken();
				assertEquals(tokenArray[i++], token.getType());
				assertEquals(tokenVal, token.getValue());			
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReservedWords() {
		String str = "and not or if then else for class int float get put return";
		String strArray[] = {"and", "not", "or", "if", "then", "else", "for", 
				"class", "int", "float", "get", "put", "return"};
		lex.setReader(str);
		try {
			lex.readFirstChar();
			for(String tokenVal:strArray){
				Token token = lex.getNextToken();
				assertEquals(Constants.RESERVED_WORD, token.getType());
				assertEquals(tokenVal, token.getValue());			
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testParenthesis() {
		String str = "[ ] { } ( )";
		String strArray[] = {"[", "]", "{", "}", "(", ")"};
		String tokenArray[] = {"OPENSQBRACKET", "CLOSESQBRACKET", 
				"OPENCRLBRACKET", "CLOSECRLBRACKET", "OPENPAR", "CLOSEPAR"};

		testStrWithLex(str, strArray, tokenArray);
	}
	
	@Test
	public void testOperators() {
		String str = "== <> < > <= >= ; , . + - * / =";
		String strArray[] = {"==", "<>", "<", ">", "<=", ">=", ";", ",", ".", "+", "-", "*", "/", "="};
		String tokenArray[] = {"EQCOMP", "NOTEQ", "LT", "GT", "LESSEQ", "GREATEQ", "SEMICOLON", "COMMA",
				"POINT", "PLUS", "MINUS", "MULTIPLY", "DIV", "EQ"};

		testStrWithLex(str, strArray, tokenArray);
	}	
	
	@Test
	public void testValidIdentifier() {
		String str = "a123 a_123 a123_ abc abc_ ab_c ab____c ab___123__bc a123___ ABC A_Bc ABc_";
		String strArray[] = {"a123", "a_123", "a123_", "abc", "abc_", "ab_c", "ab____c", 
				"ab___123__bc", "a123___", "ABC", "A_Bc", "ABc_"};
		lex.setReader(str);
		try {
			lex.readFirstChar();
			for(String tokenVal:strArray){
				Token token = lex.getNextToken();
				assertEquals(Constants.ID, token.getType());
				assertEquals(tokenVal, token.getValue());			
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testInvalidId_123a() throws IOException{
		String str = "123a";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNull(token);
		assertEquals("Invalid Number at line: 1 position: 4: " + str + "\n", errContent.toString());
	}
	
	@Test
	public void testInvalidId__abc() throws IOException{
		String str = "_abc";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNotNull(token);
		assertEquals("Invalid Character at line: 1 position: 1: _\n", errContent.toString());
	}

	@Test
	public void testValidNumber() {
		String str = "99.0000000003\n"
					+ "3.0\n"
					+ "0.0\n"
					+ "0\n"
					+ "0 0 0 6\n"
					+ "44\n"
					+ "30.9";
		String strArray[] = {"99.0000000003", "3.0", "0.0", "0", "0", "0", "0", "6", 
				"44", "30.9"};
		lex.setReader(str);
		try {
			lex.readFirstChar();
			for(String tokenVal:strArray){
				Token token = lex.getNextToken();
				assertEquals(Constants.NUM, token.getType());
				assertEquals(tokenVal, token.getValue());			
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testInvalidNum_77Point00() throws IOException{
		String str = "77.00 ";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNull(token);
		assertEquals("Invalid Number at line: 1 position: 6: 77.00\n", errContent.toString());
	}
	
	@Test
	public void testInvalidNum_0088() throws IOException{
		String str = "0088";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNotNull(token);
		assertEquals("Invalid Number at line: 1 position: 2: 00\n", errContent.toString());
	}

	@Test
	public void testInvalidNum_08() throws IOException{
		String str = "08";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNull(token);
		assertEquals("Invalid Number at line: 1 position: 2: " + str + "\n", errContent.toString());
	}
	
	@Test
	public void testInvalidNum_03Point9() throws IOException{
		String str = "03.9";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNotNull(token);
		assertEquals("Invalid Number at line: 1 position: 2: 03\n", errContent.toString());
	}
	
	@Test
	public void testInvalidNum_03Point0() throws IOException{
		String str = "03.0";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNotNull(token);
		assertEquals("Invalid Number at line: 1 position: 2: 03\n", errContent.toString());
	}
	
	@Test
	public void testInvalidNum_03Point90() throws IOException{
		String str = "03.90";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNotNull(token);
		assertEquals("Invalid Number at line: 1 position: 2: 03\n", errContent.toString());
	}
	
	@Test
	public void testInvalidNum_1Point1Point1() throws IOException{
		String str = "1.1.1";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNotNull(token);
		assertEquals("Invalid Number at line: 1 position: 4: 1.1.\n", errContent.toString());
	}
	
	@Test
	public void testInvalidSymbol_Percentage() throws IOException{
		String str = "%";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNull(token);
		assertEquals("Invalid Character at line: 1 position: 1: %\n", errContent.toString());
	}
	
	@Test
	public void testInvalidSymbol_Dollar() throws IOException{
		String str = "$";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNull(token);
		assertEquals("Invalid Character at line: 1 position: 1: $\n", errContent.toString());
	}
	
	@Test
	public void testInvalidSymbol_Hash() throws IOException{
		String str = "#";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNull(token);
		assertEquals("Invalid Character at line: 1 position: 1: #\n", errContent.toString());
	}
	
	@Test
	public void testInvalidSymbol_atTheRate() throws IOException{
		String str = "@";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNull(token);
		assertEquals("Invalid Character at line: 1 position: 1: @\n", errContent.toString());
	}
	
	@Test
	public void testInvalidSymbol_ExclamationAmpersandTilda() throws IOException{
		String str = "! & ~";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNull(token);
		assertEquals("Invalid Character at line: 1 position: 1: !\n"
				+ "Invalid Character at line: 1 position: 3: &\n"
				+ "Invalid Character at line: 1 position: 5: ~\n"
				, errContent.toString());
	}
	
	@Test
	public void testValidComment() throws IOException{
		String str = "// bla\n"
				+ "// bla%\n"
				+ "/* bla bla\n"
				+ "bla */"
				+ "/* bla bla\n"
				+ "bla # */";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNull(token);
		assertEquals("", errContent.toString());
	}

	@Test
	public void testValidComment2() throws IOException{
		String str = "/* bla\n"
					+"/*\n"
					+"bla\n"
					+"*/\n"
					+"*/\n";

		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNotNull(token);
		assertEquals("", errContent.toString());
	}
	
	@Test
	public void testInValidComment() throws IOException{
		String str = "/* bla\n";
		lex.setReader(str);
		lex.readFirstChar();
		Token token = lex.getNextToken();
		assertNull(token);
		assertEquals("Comment not ended at line: 1 position: 1: /* bla\n", errContent.toString());
	}
	
}
