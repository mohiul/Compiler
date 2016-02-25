package parser;

import java.io.IOException;

import parser.SyntacticAnalyzer;

public class Main {
	
	private static SyntacticAnalyzer parser;

	public static void main(String[] args) throws IOException{
		parser = new SyntacticAnalyzer("test-program.txt");
		boolean parserReturn = parser.parse();
		System.out.println("parserReturn: " + parserReturn);
	}

}
