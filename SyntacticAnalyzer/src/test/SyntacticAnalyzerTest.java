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
		parser = new SyntacticAnalyzer("test-program.txt");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParse() throws IOException{
		assertTrue(parser.parse());
	}

}
