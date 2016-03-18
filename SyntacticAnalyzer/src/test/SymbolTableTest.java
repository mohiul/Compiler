package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import lex.Constants;
import lex.Token;
import smbl.SymbolTable;
import smbl.VariableKind;

public class SymbolTableTest {
	
	private SymbolTable table;
	
	@Before
	public void setUp() throws Exception {
		table = new SymbolTable();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String tokenName = "testId";
		Token token = new Token(Constants.ID, tokenName, 0, 0);
		table.addRowAndTable(token, VariableKind.CLASS);
		assertNotEquals(0, table.tableMap.size());
	}

}
