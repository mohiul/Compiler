package test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import parser.SyntacticAnalyzer;

public class SymbolTableTest {
	
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
		parser = new SyntacticAnalyzer();
	}

	@After
	public void tearDown() throws Exception {
		parser.closeWriter();
	}

	@Test
	public void testProg1() throws IOException{
		parser.setLexReaderStr("class id { }; program { }; id1 func() {};");
		assertFalse(parser.parse());
		assertEquals("Type id1 does not exist at line: 1 position: 28\n", errContent.toString());
	}
	
	@Test
	public void testProg2() throws IOException{
		parser.setLexReaderStr("class classId { int int1; }; program { }; classId func() { classId class1; class1.id1 = 1; };");
		assertFalse(parser.parse());
		assertEquals("Variable id1 does not exist in class classId at line: 1 position: 83\n", errContent.toString());
	}
	
}
