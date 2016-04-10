package test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import parser.SyntacticAnalyzer;

public class SyntacticAnalyzerTest {
	
	private SyntacticAnalyzer parser;
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

	@Before
	public void setUpStreams() {
	    System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
	    System.setErr(null);
	}
	
	@Before
	public void setUp() throws Exception {
		parser = new SyntacticAnalyzer("errors.txt", "grammars.txt", "code.txt");
	}

	@After
	public void tearDown() throws Exception {
		parser.closeWriter();
	}

	@Test
	public void testProg() throws IOException{
		parser.setLexReaderStr("program { };");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProgErr1() throws IOException{
		parser.setLexReaderStr("program id { };");
		assertTrue(parser.parse());
		assertEquals("Syntax error at line: 1 position: 9: id\n", errContent.toString());
	}
	
	@Test
	public void testProgErr2() throws IOException{
		parser.setLexReaderStr("program { id };");
		assertFalse(parser.parse());
		assertEquals("Syntax error at line: 1 position: 14: }\n", errContent.toString());
	}
	
	@Test
	public void testProgErr3() throws IOException{
		parser.setLexReaderStr("program { } id;");
		assertFalse(parser.parse());
		assertEquals("Syntax error at line: 1 position: 13: id\n", errContent.toString());
	}
	
	@Test
	public void testProgErr4() throws IOException{
		parser.setLexReaderStr("program { }; id");
		assertFalse(parser.parse());
		assertEquals("Syntax error at line: 1 position: 16\n", errContent.toString());
	}
	
	@Test
	public void testClassDecl() throws IOException{
		parser.setLexReaderStr("class test { }; program { };");
		assertTrue(parser.parse());
		
	}
	
	@Test
	public void testClassDeclErr1() throws IOException{
		parser.setLexReaderStr("class test id { }; program { };");
		assertFalse(parser.parse());
		assertEquals("Syntax error at line: 1 position: 12: id\n", errContent.toString());
		
	}
	
	@Test
	public void testClassDeclErr2() throws IOException{
		parser.setLexReaderStr("class test { id }; program { };");
		assertFalse(parser.parse());
		assertEquals("Syntax error at line: 1 position: 17: }\n", errContent.toString());
	}
	
	@Test
	public void testClassDeclErr3() throws IOException{
		parser.setLexReaderStr("class test { } id; program { };");
		assertFalse(parser.parse());
		assertEquals("Syntax error at line: 1 position: 16: id\n", errContent.toString());
	}
	
	@Test
	public void testClassDeclErr4() throws IOException{
		parser.setLexReaderStr("class test { }; id program { };");
		assertTrue(parser.parse());
		assertEquals("Syntax error at line: 1 position: 17: id\n", errContent.toString());
	}
	
	@Test
	public void testClassDecls() throws IOException{
		parser.setLexReaderStr("class test { }; class test1 { }; program { };");
		assertTrue(parser.parse());
		
	}
	
	@Test
	public void testClassIdDecl() throws IOException{
		parser.setLexReaderStr("class test { int id; }; program { };");
		assertTrue(parser.parse());
		
	}
	
	@Test
	public void testClassIdDeclErr1() throws IOException{
		parser.setLexReaderStr("class; test { int id; }; program { };");
		assertFalse(parser.parse());
		assertEquals("Syntax error at line: 1 position: 6: ;\n", errContent.toString());
	}
	
	@Test
	public void testClassIdDeclErr2() throws IOException{
		parser.setLexReaderStr("class test; { int id; }; program { };");
		assertFalse(parser.parse());
		assertEquals("Syntax error at line: 1 position: 11: ;\n", errContent.toString());
	}
	
	@Test
	public void testClassIdDeclErr3() throws IOException{
		parser.setLexReaderStr("class test {; int id; }; program { };");
		assertTrue(parser.parse());
		assertEquals("Syntax error at line: 1 position: 13: ;\n", errContent.toString());
	}
	
	@Test
	public void testClassIdDeclErr4() throws IOException{
		parser.setLexReaderStr("class test { int; id; }; program { };");
		assertFalse(parser.parse());
		assertEquals("Syntax error at line: 1 position: 17: ;\n", errContent.toString());
	}
	
	@Test
	public void testClassIdFuncDecls() throws IOException{
		parser.setLexReaderStr("class test { int id; float func(){}; }; program { };");
		assertTrue(parser.parse());
		
	}
	
	@Test
	public void testClassIdFuncDeclsErr1() throws IOException{
		parser.setLexReaderStr("class test { int id; float func;(){}; }; program { };");
		assertTrue(parser.parse());
		assertEquals("Syntax error at line: 1 position: 33: (\nSyntax error at line: 1 position: 39: }\n", errContent.toString());
	}
	
	@Test
	public void testClassIdFuncDeclsErr2() throws IOException{
		parser.setLexReaderStr("class test { int id; float func(;){}; }; program { };");
		assertTrue(parser.parse());
		assertEquals("Syntax error at line: 1 position: 33: ;\n", errContent.toString());
	}

	@Test
	public void testClassIdFuncDeclsErr3() throws IOException{
		parser.setLexReaderStr("class test { int id; float func();{}; }; program { };");
		assertTrue(parser.parse());
		assertEquals("Syntax error at line: 1 position: 34: ;\n", errContent.toString());
	}
	
	@Test
	public void testClassIdFuncDeclsErr4() throws IOException{
		parser.setLexReaderStr("class test { int id; float func(){;}; }; program { };");
		assertTrue(parser.parse());
		assertEquals("Syntax error at line: 1 position: 35: ;\n", errContent.toString());
	}
	
	@Test
	public void testClassIdDecls() throws IOException{
		parser.setLexReaderStr("class test { int id; float id2; }; program { };");
		assertTrue(parser.parse());
		
	}
	
	@Test
	public void testClassIdDeclsErr1() throws IOException{
		parser.setLexReaderStr("class test { int [id; float id2; }; program { };");
		assertFalse(parser.parse());
		assertEquals("Syntax error at line: 1 position: 18: [\n", errContent.toString());
		
	}
	
	@Test
	public void testClassIdDeclsErr2() throws IOException{
		parser.setLexReaderStr("class test { int id[][][; float id2; }; program { };");
		assertFalse(parser.parse());
		assertEquals("Syntax error at line: 1 position: 21: ]\n", errContent.toString());
		
	}

	@Test
	public void testMultiArrays() throws IOException{
		parser.setLexReaderStr("class test { int var1[4][5][7][8][9][1][0]; float id2[11][12]; }; program { };");
		assertTrue(parser.parse());
		
	}
	
	@Test
	public void testMultiArraysErr1() throws IOException{
		parser.setLexReaderStr("class test { int var1[4][5][7].[8][9][1][0]; float id2[11][12]; }; program { };");
		assertTrue(parser.parse());
		assertEquals("Syntax error at line: 1 position: 31: .\n", errContent.toString());
	}
	
	@Test
	public void testMultiArraysErr2() throws IOException{
		parser.setLexReaderStr("class test { int var1[4][5][7]{}[8][9][1][0]; float id2[11][12]; }; program { };");
		assertTrue(parser.parse());
		assertEquals("Syntax error at line: 1 position: 31: {\n", errContent.toString());
	}
	
	@Test
	public void testClassIdDeclFuncDecl() throws IOException{
		parser.setLexReaderStr("class test { int id; float func(int array[100]){}; }; program { };");
		assertTrue(parser.parse());
		
	}
	
	@Test
	public void testClassIdDeclFuncDecls() throws IOException{
		parser.setLexReaderStr("class test { int id; float func(int array[100]){}; float func1(test testId){}; }; program { };");
		assertTrue(parser.parse());
		
	}
	
	@Test
	public void testFunctionWithReturn() throws IOException{
		parser.setLexReaderStr("program { int id; }; float func(int array[100]){ int minValue; return (minValue); };");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testFunctionWithReturnErr1() throws IOException{
		parser.setLexReaderStr("program { int id; }; float func(int array[100]){ int minValue; return; (minValue); };");
		assertFalse(parser.parse());
		assertEquals("Syntax error at line: 1 position: 70: ;\n", errContent.toString());
	}
	
	@Test
	public void testIdExpressions() throws IOException{
		parser.setLexReaderStr("program { int id; id = 1 + 2; };");
		assertTrue(parser.parse());
		
	}
	
	@Test
	public void testIdExpressionsErr1() throws IOException{
		parser.setLexReaderStr("program { int id; id = 1 + !@#$2; };");
		assertTrue(parser.parse());
		assertEquals("Invalid Character at line: 1 position: 31: !@#$\n", errContent.toString());
	}
	
	@Test
	public void testIdNest() throws IOException{
		parser.setLexReaderStr("class ArrayUtility { int var1[4][1][0][0][0][0][0];}; program { ArrayUtility arrayUtility[1][1][1][1]; arrayUtility[1][1][1][1].var1[4][1][0][0][0][0][0] = 2; };");
		assertTrue(parser.parse());
		
	}
	
	@Test
	public void testIdNestErr1() throws IOException{
		parser.setLexReaderStr("program { arrayUtility[1][1][1][1]..var1[4][1] = 2; };");
		assertFalse(parser.parse());
		assertEquals("Syntax error at line: 1 position: 36: .\n", errContent.toString());
	}
	
	@Test
	public void testIF() throws IOException{
		parser.setLexReaderStr("program { int array[10]; int idx; int maxValue; if(array[idx] > maxValue) then { maxValue = array[idx]; } else{}; };");
		assertTrue(parser.parse());
		
	}
	
	@Test
	public void testForIF() throws IOException{
		parser.setLexReaderStr("program { int array[10]; int maxValue; for( int idx = 1; idx <= 99; idx = ( idx ) + 1){if(array[idx] < maxValue) then {maxValue = array[idx];}else{};}; };");
		assertTrue(parser.parse());
		
	}
	
	@Test
	public void testGetPut() throws IOException{
		parser.setLexReaderStr("program { int sample[10]; int t; float maxValue; get(sample[t]); put(maxValue); };");
		assertTrue(parser.parse());
		
	}
	
	@Test
	public void testExpressions() throws IOException{
		parser.setLexReaderStr("program { float value; value = 100.1 * (2.2 + 3.0 / 7.0006); value = 1.05 + ((2.04 * 2.47) - 3.0) + 7.0006 ; return (value); };");
		assertTrue(parser.parse());
		
	}

	@Test
	public void testProg18() throws IOException{
		parser.setLexReaderStr("program { int id[1][2][3]; get ( id[1][2] ) ; } ; ");
		assertFalse(parser.parse());
		assertEquals("Variable id incorrect array dimension size at line: 1 position: 34\n", errContent.toString());
	}

	@Test
	public void testProg19() throws IOException{
		parser.setLexReaderStr("program { int i; float j; i = i * j;};");
		assertFalse(parser.parse());
		assertEquals("Type compatibility error for * at line: 1 position: 33\n", errContent.toString());
	}
	
	@Test
	public void testProg20() throws IOException{
		parser.setLexReaderStr("class U {int test(int i, int j, float k){ }; }; program { U u; int i; i = u.test(1, 2, 3.0); };");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg21() throws IOException{
		parser.setLexReaderStr("class U {int test(int i, int j, int k){ }; }; program { U u; int i; i = u.test(1, 2, 3.0); };");
		assertFalse(parser.parse());
		assertEquals("Incorrect type for parameter no: 3, function test at line: 1 position: 75\n", errContent.toString());
	}
	
	@Test
	public void testProg22() throws IOException{
		parser.setLexReaderStr("class U {int test(int i, int j){ }; }; program { U u; int i; i = u.test(1, 2, 3.0); };");
		assertFalse(parser.parse());
		assertEquals("Incorrect number of parameters for function test at line: 1 position: 68\n", errContent.toString());
	}
	
	@Test
	public void testProg23() throws IOException{
		parser.setLexReaderStr("program {int x; float y; if(x > y) then {} else{}; };");
		assertFalse(parser.parse());
		assertEquals("Type compatibility error for > at line: 1 position: 31\n", errContent.toString());
	}
	
}
