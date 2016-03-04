package com.altheadx.pgx.utils;

public class PGXUtil {
	
	public static boolean pairEquals(String A1,String A2,String B1,String B2)
	{
		if(A1.equals(B1) && A2.equals(B2) || A1.equals(B2) && A2.equals(B1))
			return true;
		else
			return false;
	}
	
	public static String toSampleID(String text)
	{
		String[] cols=text.split("_");
		if (cols.length>=1) {
			return cols[0];
		}
/*		
		else {
			cols=text.split("_2_1_1");
			if (cols.length>=1) {
				return cols[0];
			}
		}
*/		
		return text;
	}
	
}
