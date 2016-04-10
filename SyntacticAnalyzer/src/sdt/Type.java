package sdt;

import lex.Token;

public class Type {
	public String typeName;

	public Type() {}
	
	public Type(Token type) {
		typeName = type.getValue();
	}

	public static void copyType(Type type1, Type type2) {
		if(type1 == null) throw new RuntimeException("type1 is null");
		if(type2 == null) throw new RuntimeException("type2 is null");
		type1.typeName = type2.typeName;
		
	}
}
