package sdt;

import java.util.ArrayList;
import java.util.List;

import lex.Token;

public class ArraySizeList {

	public ArraySize arraySize;
	public ArraySizeList arraySizeList;
	public List<Token> getArraySizeList() {
		List<Token> sizeList = new ArrayList<Token>();
		ArraySize arraySize1 = arraySize;
		ArraySizeList arraySizeList1 = arraySizeList;
		while(arraySize1 != null){
			sizeList.add(arraySize1.intnum);
			if(arraySizeList1 != null){
				arraySize1 = arraySizeList1.arraySize;				
				arraySizeList1 = arraySizeList1.arraySizeList;
			} else {
				arraySize1 = null;
			}
		}
		return sizeList;
	}

}
