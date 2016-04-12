package parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import parser.SyntacticAnalyzer;

public class Main {
	
	private static SyntacticAnalyzer parser;

	public static void main(String[] args) throws IOException{		
		//input file name
		String programFileName = "test-program.txt";
		System.out.print("Please enter input program file name [test-program.txt]:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String inputFile = br.readLine();
		if(inputFile.length() > 0){
			programFileName = inputFile;
		}
		
		//grammar file name
		String grammarFileName = "grammars.txt";
		System.out.print("Please enter output grammar file name [grammars.txt]:");
		inputFile = br.readLine();
		if(inputFile.length() > 0){
			grammarFileName = inputFile;
		}

		//symbol table file name
		String symbolTableFileName = "symbolTables.txt";
		System.out.print("Please enter output symbol tables file name [symbolTables.txt]:");
		inputFile = br.readLine();
		if(inputFile.length() > 0){
			symbolTableFileName = inputFile;
		}
		
		//error file name
		String errorFileName = "errors.txt";
		System.out.print("Please enter output error file name [errors.txt]:");
		inputFile = br.readLine();
		if(inputFile.length() > 0){
			errorFileName = inputFile;
		}
		
		//error file name
		String codeFileName = "code.m";
		System.out.print("Please enter output file name for moon code [code.m]:");
		inputFile = br.readLine();
		if(inputFile.length() > 0){
			codeFileName = inputFile;
		}
		
		parser = new SyntacticAnalyzer(errorFileName, grammarFileName, codeFileName);		
		parser.handleFile(programFileName);
		boolean parserReturn = parser.parse();
		if(!parserReturn)
			System.out.println("parserReturn: " + parserReturn);
		else{
			System.out.println("Successfully parsed!");
			System.out.println("Output grammar file: " + grammarFileName);
			System.out.println("Output symbol table file: " + symbolTableFileName);
			System.out.println("Output error file: " + errorFileName);
			System.out.println("Output code file: " + codeFileName);
		}
		parser.closeWriter();
	}

}
