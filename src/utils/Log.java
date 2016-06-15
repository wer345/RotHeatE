package utils;

public class Log {
	public static int UnknownError=0;
	public static int Debug=1;
	public static int Info=2;
	public static int CodeError=3;
	public static int CodeWarning=4;
	public static int DataError=5;
	public static int DataWarning=6;
	public static int ResourceError=7;
	public static int ResourceWarning=8;
	public static int AnalysisError=9;
	public static int AnalysisWarning=10;
	public static int AlgorithmError=11;
	public static int nofErrors=12;

	private static String[] ErrorTypes={
		"UnknownError",
		"Debug",
		"Information",
		"Code Error",
		"Code Warning",
		"Data Error",
		"Data Warning",
		"Resource Error",
		"Resource Warning",
		"Analysis Error",
		"Analysis Warning",
		"Algrithm Error"
	};
	
	public String className="";
	public String label="";
	public int type=0;
	public String errorText;
	
	public static String typeName(int type)
	{
		if(type>=0 && type<ErrorTypes.length)
			return ErrorTypes[type];
		else
			return "error type Unkown";
	}

	public String toString()
	{
		return String.format("[%s] %s/%s$ %s",Log.typeName(type), className, label, errorText);
	}
	
}
