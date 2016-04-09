package test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import lex.Token;
import sdt.ArithExpr;
import sdt.ArraySize;
import sdt.ArraySizeList;
import sdt.Indice;
import sdt.IndiceList;

public class SDTObjectTest {
	ArraySizeList arraySizeList;
	IndiceList indiceList;

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testArraySizeList() {
		
		arraySizeList = new ArraySizeList();
		arraySizeList.arraySize = new ArraySize();
		arraySizeList.arraySize.intnum = new Token();
		
		arraySizeList.arraySizeList = new ArraySizeList();
		arraySizeList.arraySizeList.arraySize = new ArraySize();
		arraySizeList.arraySizeList.arraySize.intnum = new Token();

		arraySizeList.arraySizeList.arraySizeList = new ArraySizeList();
		arraySizeList.arraySizeList.arraySizeList.arraySize = new ArraySize();
		arraySizeList.arraySizeList.arraySizeList.arraySize.intnum = new Token();
		
		List<Token> tokenList = arraySizeList.getArraySizeList();
		assertEquals(3, tokenList.size());
	}
	
	@Test
	public void testIndiceList() {
		
		indiceList = new IndiceList();
		indiceList.indice = new Indice();
		indiceList.indice.arithExpr = new ArithExpr();
		
		indiceList.indiceList = new IndiceList();
		indiceList.indiceList.indice = new Indice();
		indiceList.indiceList.indice.arithExpr = new ArithExpr();

		indiceList.indiceList.indiceList = new IndiceList();
		indiceList.indiceList.indiceList.indice = new Indice();
		indiceList.indiceList.indiceList.indice.arithExpr = new ArithExpr();
		
		assertEquals(3, indiceList.getNoOfDim());
	}

}
