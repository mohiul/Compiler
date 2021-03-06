package test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import parser.SyntacticAnalyzer;

public class SyntacticAnalyzerTest2 {
	
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
		parser = new SyntacticAnalyzer("errors.txt", "grammars.txt", "code.m");
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
	public void testProg1() throws IOException{
		parser.setLexReaderStr("class id { }; program { } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg2() throws IOException{
		parser.setLexReaderStr("class id { }; program { } ; id id1 ( ) { } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg3() throws IOException{
		parser.setLexReaderStr("class id { int i; }; program { } ; id id1 ( id id1 , id id2 ) { id1.i = id2.i + id2.i ; } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg4() throws IOException{
		parser.setLexReaderStr("class id { id id2 ;  }; program { } ; id id3 ( id id4 , id  id5 ) { } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg5() throws IOException{
		parser.setLexReaderStr("class id { id1 id2 ;  }; program { id id1 ; } ; id id1 ( id id1 , id  id2 ) { } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg6() throws IOException{
		parser.setLexReaderStr("class id { id id2 [ 10 ] ;  }; program { id id1 ; } ; id id3 ( id id1 , id  id2 ) { } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg7() throws IOException{
		parser.setLexReaderStr("class id { id1 id2 [ 10 ][ 10 ] ;  }; program { id id1 ; } ; id id1 ( id id1, id  id2 ) { } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg8() throws IOException{
		parser.setLexReaderStr("class id { id id1 [ 10 ][ 10 ] ;  id id2 (id id1 , id id2 ){ id id3 ; }; }; program { id id1 ; } ; id id1 ( id id1 [ 10 ] [ 10 ] , id  id2 ) { } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg9() throws IOException{
		parser.setLexReaderStr("class id { id id1 [ 10 ][ 10 ] ; int i;  id id2 (id id1 , id id2 ){ id id3 ; }; }; program { id id1 ; id1.i = id1.i ; } ; id id1 ( id id1 [ 10 ] [ 10 ] , id  id2 ) { } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg10() throws IOException{
		parser.setLexReaderStr("class id { id id1 [ 10 ][ 10 ] ; int i;  id id2 (id id1 , id id2 ){ id id3 ; }; }; program { id id1 ; id1.i = id1.i ; id1 . id1 [ id1 ] [ id1 ].i = id1 . id1[1][1].i + id1.i * id1.i > id1 . id1[1][1].i ; } ; id id1 ( id id1 [ 10 ] [ 10 ] , id  id2 ) { } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg11() throws IOException{
		parser.setLexReaderStr("class id { id id1 [ 10 ][ 10 ] ; int i;  id id2 (id id3 , id id4 ){ id id5 ; };  id id6 ; }; program { id id1 ; id1.i = id1.i ; id1 . id1 [ id1 ] [ id1 ].i = id1 . id1[ 10 ][ 10 ].i + id1.i * id1.i > id1 . id1[ 10 ][ 10 ].i ; } ; id id1 ( id id1 [ 10 ] [ 10 ] , id  id2 ) { } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg12() throws IOException{
		parser.setLexReaderStr("class id { } ; program { int id; if ( id + id ) then { id = id + id ; } else { id = id + id - id > id + id ; } ; } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg13() throws IOException{
		parser.setLexReaderStr("class id { int i; } ; program { id id ;  id.i = id.i + id.i ; id.i = id.i + id.i ; } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg14() throws IOException{
		parser.setLexReaderStr("class id { id id; id id1[10]; int i; } ; program { id id ; id id1[10];  id.i = id.i ; id1 [ id ].i = id . id1[ id . id1[ id ].i ].i  ; } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg15() throws IOException{
		parser.setLexReaderStr("class id1 {int id[1];}; program { id1 id; put ( id . id [ id . id [ id ] ] ) ; } ;");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg16() throws IOException{
		parser.setLexReaderStr("program { int id; get ( id ) ; } ; ");
		assertTrue(parser.parse());
	}	
	
	@Test
	public void testProg17() throws IOException{
		parser.setLexReaderStr("program { int id[1][2][3]; get ( id[1][2][3] ) ; } ; ");
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg18() throws IOException{
		parser.setLexReaderStr("program { int i; i = test(); } ; int test(){ return (0);};");
		assertTrue(parser.parse());
	}
	
}
