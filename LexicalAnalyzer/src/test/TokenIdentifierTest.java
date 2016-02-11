package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import lex.TokenIdentifier;

public class TokenIdentifierTest {

	TokenIdentifier identifier;
	
	@Before
	public void setUp() throws Exception {
		identifier = new TokenIdentifier();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIsNonZero() {
		assertFalse(identifier.isNonZero('0'));
		assertTrue(identifier.isNonZero('1'));
		assertTrue(identifier.isNonZero('5'));
		assertTrue(identifier.isNonZero('9'));
		assertFalse(identifier.isNonZero(':'));
		assertFalse(identifier.isNonZero('a'));
		assertFalse(identifier.isNonZero('A'));

	}

	@Test
	public void testIsDigit() {
		assertFalse(identifier.isDigit('/'));
		assertTrue(identifier.isDigit('0'));
		assertTrue(identifier.isDigit('1'));
		assertTrue(identifier.isDigit('5'));
		assertTrue(identifier.isDigit('9'));
		assertFalse(identifier.isDigit(':'));
		assertFalse(identifier.isDigit('a'));
		assertFalse(identifier.isDigit('A'));
	}

	@Test
	public void testIsLetter() {
		assertFalse(identifier.isLetter('@'));
		assertTrue(identifier.isLetter('A'));
		assertTrue(identifier.isLetter('M'));
		assertTrue(identifier.isLetter('Z'));
		assertFalse(identifier.isLetter('['));
		
		assertFalse(identifier.isLetter('`'));
		assertTrue(identifier.isLetter('a'));
		assertTrue(identifier.isLetter('m'));
		assertTrue(identifier.isLetter('z'));
		assertFalse(identifier.isLetter('{'));

	}

}
