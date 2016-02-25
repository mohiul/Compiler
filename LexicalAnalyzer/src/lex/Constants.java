package lex;

public class Constants {
	public static final int INIT_STATE = 0;
	public static final int ERR_STATE = 37;
	public static final int SINGLE_COMMENT_STATE = 14;
	public static final int MULTI_COMMENT_STATE1 = 15;
	public static final int MULTI_COMMENT_STATE2 = 16;
	
	public static final String ID = "ID";
	public static final String NUM = "NUM";
	public static final String DIV = "DIV";
	public static final String COMMENT = "COMMENT";
	public static final String CLOSESQBRACKET = "CLOSESQBRACKET";
	public static final String OPENSQBRACKET = "OPENSQBRACKET";
	public static final String CLOSECRLBRACKET = "CLOSECRLBRACKET";
	public static final String OPENCRLBRACKET = "OPENCRLBRACKET";
	public static final String OPENPAR = "OPENPAR";
	public static final String CLOSEPAR = "CLOSEPAR";
	public static final String LT = "LT";
	public static final String LESSEQ = "LESSEQ";
	public static final String NOTEQ = "NOTEQ";
	public static final String GT = "GT";
	public static final String GREATEQ = "GREATEQ";
	public static final String EQ = "EQ";
	public static final String EQCOMP = "EQCOMP";
	public static final String SEMICOLON = "SEMICOLON";
	public static final String COMMA = "COMMA";
	public static final String POINT = "POINT";
	public static final String PLUS = "PLUS";
	public static final String MINUS = "MINUS";
	public static final String MULTIPLY = "MULTIPLY";
	public static final String ERR = "ERR";
	public static final String RESERVED_WORD = "RESERVEDWORD";
	
	public static final String RESERVED_WORD_AND = "and";
	public static final String RESERVED_WORD_NOT = "not";
	public static final String RESERVED_WORD_OR = "or";
	public static final String RESERVED_WORD_IF = "if";
	public static final String RESERVED_WORD_THEN = "then";
	public static final String RESERVED_WORD_ELSE = "else";
	public static final String RESERVED_WORD_FOR = "for";
	public static final String RESERVED_WORD_CLASS = "class";
	public static final String RESERVED_WORD_INT = "int";
	public static final String RESERVED_WORD_FLOAT = "float";
	public static final String RESERVED_WORD_GET = "get";
	public static final String RESERVED_WORD_PUT = "put";
	public static final String RESERVED_WORD_RETURN = "return";
	public static final String RESERVED_WORD_PROGRAM = "program";
	public static final String DOLLAR = "$";
}
