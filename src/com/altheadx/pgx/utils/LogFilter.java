package com.altheadx.pgx.utils;

public class LogFilter {
	static public String callerClassName="";
	static public String allClasses="*";
	static public String labelCallerMethodName="";
	static public String allLabels="*";
	static public int allTypes=0;
	String className="";  // value "*" for all classes
	String label=""; // value "*" for all labels
	int type=0;	// value 0 for all types
	
	public boolean equals(LogFilter filter)
	{
		if( this.className.equals(filter.className) &&
			this.label.equals(filter.label) &&
			this.type==filter.type)
			return true;
		else
			return false;
				
	}
	
	
}
