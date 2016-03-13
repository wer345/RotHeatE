
public class L {
	public static boolean on=true;
	
	public static void on()
	{
		on=true;
	}
	
	public static void off()
	{
		on=false;
	}
	
	public static void p(String format, Object... args) {
		if(on)
			System.out.format(format, args);
	}

}
