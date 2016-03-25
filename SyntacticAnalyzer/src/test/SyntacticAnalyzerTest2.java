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
		parser = new SyntacticAnalyzer();
	}

	@After
	public void tearDown() throws Exception {
		parser.closeWriter();
	}

	@Test
	public void testProg() throws IOException{
		parser.setLexReader(Utils.getReader("program { };"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg1() throws IOException{
		parser.setLexReader(Utils.getReader("class id { }; program { } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg2() throws IOException{
		parser.setLexReader(Utils.getReader("class id { }; program { } ; id id1 ( ) { } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg3() throws IOException{
		parser.setLexReader(Utils.getReader("class id { }; program { } ; id id1 ( id id1 , id id2 ) { id = id + id ; } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg4() throws IOException{
		parser.setLexReader(Utils.getReader("class id { id id ;  }; program { } ; id id ( id id , id  id ) { } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg5() throws IOException{
		parser.setLexReader(Utils.getReader("class id { id id ;  }; program { id id ; } ; id id ( id id , id  id ) { } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg6() throws IOException{
		parser.setLexReader(Utils.getReader("class id { id id [ 10 ] ;  }; program { id id ; } ; id id ( id id , id  id ) { } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg7() throws IOException{
		parser.setLexReader(Utils.getReader("class id { id id [ 10 ][ 10 ] ;  }; program { id id ; } ; id id ( id id, id  id ) { } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg8() throws IOException{
		parser.setLexReader(Utils.getReader("class id { id id [ 10 ][ 10 ] ;  id id (id id , id id ){ id id ; }; }; program { id id ; } ; id id ( id id [ 10 ] [ 10 ] , id  id ) { } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg9() throws IOException{
		parser.setLexReader(Utils.getReader("class id { id id [ 10 ][ 10 ] ;  id id (id id , id id ){ id id ; }; }; program { id id ; id = id ; } ; id id ( id id [ 10 ] [ 10 ] , id  id ) { } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg10() throws IOException{
		parser.setLexReader(Utils.getReader("class id { id id [ 10 ][ 10 ] ;  id id (id id , id id ){ id id ; }; }; program { id id ; id = id ; id . id [ id ] [ id ] = id . id + id * id > id . id ; } ; id id ( id id [ 10 ] [ 10 ] , id  id ) { } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg11() throws IOException{
		parser.setLexReader(Utils.getReader("class id { id id [ 10 ][ 10 ] ;  id id (id id , id id ){ id id ; };  id id ; }; program { id id ; id = id ; id . id [ id ] [ id ] = id . id + id * id > id . id ; } ; id id ( id id [ 10 ] [ 10 ] , id  id ) { } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg12() throws IOException{
		parser.setLexReader(Utils.getReader("class id { } ; program { if ( id + id ) then { id = id + id ; } else { id = id + id - id > id + id ; } ; } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg13() throws IOException{
		parser.setLexReader(Utils.getReader("class id { } ; program { id id ;  id = id + id ; id = id + id ; } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg14() throws IOException{
		parser.setLexReader(Utils.getReader("class id { } ; program { id id ;  id = id ; id [ id ] = id . id [ id . id [ id ] ] . id ( id . id [ id ] )  ; } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg15() throws IOException{
		parser.setLexReader(Utils.getReader("program { put ( id . id [ id . id [ id ] ] ) ; } ;"));
		assertTrue(parser.parse());
	}
	
	@Test
	public void testProg16() throws IOException{
		parser.setLexReader(Utils.getReader("program { get ( id ) ; } ; "));
		assertTrue(parser.parse());
	}	

}
