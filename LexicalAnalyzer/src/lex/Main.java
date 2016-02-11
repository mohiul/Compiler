package lex;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Main {
	
	private static LexicalAnalyzer lex; 

	public static void main(String[] args) throws IOException {
		lex = new LexicalAnalyzer();
		lex.handleFile("test-program.txt");
		Writer writer = new BufferedWriter(
				new OutputStreamWriter(
				new FileOutputStream("tokens.txt"), "utf-8"));
		Token token = lex.getNextToken();
		while(token != null){
			String tokenStr = token.toString();
			System.out.println(tokenStr);
			writer.write(tokenStr + "\n");
			token = lex.getNextToken();
		}
		try {writer.close();} catch (Exception ex) {/*ignore*/}
		lex.closeWriter();
	}
	
}
