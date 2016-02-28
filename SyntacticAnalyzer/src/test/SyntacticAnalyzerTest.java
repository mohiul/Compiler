package test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import parser.SyntacticAnalyzer;

public class SyntacticAnalyzerTest {
	
	private SyntacticAnalyzer parser;

	@Before
	public void setUp() throws Exception {
		parser = new SyntacticAnalyzer();
	}

	@After
	public void tearDown() throws Exception {
		parser.closeWriter();
	}

	@Test
	public void testParse() throws IOException{
		parser.setLexReader(Utils.getReader("class test { }; program { int id; int id1; id = 1; id1 = 4; };"));
		assertTrue(parser.parse());
	}

}
