package test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class Utils {
	public static Reader getReader(String str){
		return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(str.getBytes())));
	}
}
