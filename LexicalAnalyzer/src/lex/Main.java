package lex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Main {
	
	private static LexicalAnalyzer lex; 

	public static void main(String[] args) throws IOException {
		
		//input file name
		String programFileName = "test-program.txt";
		System.out.print("Please enter input program file name [test-program.txt]:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String inputFile = br.readLine();
		if(inputFile.length() > 0){
			programFileName = inputFile;
		}
		
		//token file name
		String tokenFileName = "tokens.txt";
		System.out.print("Please enter output token file name [tokens.txt]:");
		inputFile = br.readLine();
		if(inputFile.length() > 0){
			tokenFileName = inputFile;
		}
		
		//error file name
		String errorFileName = "errors.txt";
		System.out.print("Please enter output error file name [errors.txt]:");
		inputFile = br.readLine();
		if(inputFile.length() > 0){
			errorFileName = inputFile;
		}
		
		lex = new LexicalAnalyzer(errorFileName);
		lex.handleFile(programFileName);
		Writer writer = new BufferedWriter(
				new OutputStreamWriter(
				new FileOutputStream(tokenFileName), "utf-8"));
		Token token = lex.getNextToken();
		while(token != null){
			String tokenStr = token.toString();
			System.out.println(tokenStr);
			writer.write(tokenStr + "\n");
			token = lex.getNextToken();
		}
		try {writer.close();} catch (Exception ex) {/*ignore*/}
		lex.closeWriter();
		System.out.println("Output token file: " + tokenFileName);
		System.out.println("Output error file: " + errorFileName);
	}
	
}
