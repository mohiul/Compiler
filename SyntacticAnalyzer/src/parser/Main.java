package parser;

import java.io.IOException;

import parser.SyntacticAnalyzer;

public class Main {
	
	private static SyntacticAnalyzer parser;

	public static void main(String[] args) throws IOException{
		parser = new SyntacticAnalyzer();
		parser.handleFile("test-program.txt");
		boolean parserReturn = parser.parse();
		if(!parserReturn)
			System.out.println("parserReturn: " + parserReturn);
		else
			System.out.println("Successfully parsed!");
		parser.closeWriter();
	}

}
