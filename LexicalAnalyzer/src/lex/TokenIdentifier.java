package lex;

public class TokenIdentifier {
	
	public boolean isTangableChar(char ch){
		boolean tangableChar = false;
		int chInt = ch;
		if(chInt >= 33 && chInt <= 126){
			tangableChar = true;
		}
		return tangableChar;
	}
	
	public boolean isNonZero(char ch){
		boolean nonZero = false;
		int chInt = ch;
		if(chInt >= 49 && chInt <= 57){
			nonZero = true;
		}
		return nonZero;
	}
	
	public boolean isDigit(char ch){
		boolean digit = false;
		int chInt = ch;
		if(chInt >= 48 && chInt <= 57){
			digit = true;
		}
		return digit;
	}
	
	public boolean isLetter(char ch){
		boolean letter = false;
		int chInt = ch;
		if((chInt >= 65 && chInt <= 90) 
				|| (chInt >= 97 && chInt <= 122)){
			letter = true;
		}
		return letter;
	}
}
