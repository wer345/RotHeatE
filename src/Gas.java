
public class Gas {
	String gasName="Default";
	double R,
		Cp,
		Cv,
		lamda
	;

	public Gas() {
	}
	
	public Gas(String gasName) {
		this.gasName=gasName;
		if(gasName.equalsIgnoreCase("air")) 
			set(1010,	718);
		else if(gasName.equalsIgnoreCase("Argon") || gasName.equalsIgnoreCase("Ar")) 
			set(520,	312);
		else if(gasName.equalsIgnoreCase("Carbon dioxide") ||gasName.equalsIgnoreCase("CO2")) 
			set(844,	655);
		else if(gasName.equalsIgnoreCase("Nitrogen") ||gasName.equalsIgnoreCase("N2")) 
			set(1040, 743);
		else
			gasName="Default";
		
	}
	
	//get state of adiabatic process to a new pressure
	
	public GasState adiabatic_on_P(GasState s,double P)
	{
		double P2=P;
		double T2=s.T*Math.pow(s.P/P2,(1-lamda)/lamda);
		GasState rst = new GasState(P2,T2);
		return rst;
	}
	
	public void set(double Cp,double Cv) {
		this.Cp=Cp;
		this.Cv=Cv;
		R=Cp-Cv;
		lamda=Cp/Cv;
	}
	
	public double getDensity(GasState s)
	{
		double d=s.P/(R*s.T);
		return d;
	}
	
	double adiabaticPowerPerVolume(GasState s,double P2, double V1)
	{
		double k=(lamda-1)/lamda;
		double power=s.P*V1/k*(Math.pow(P2/s.P, k)-1);
		return power;
	}
	
	//Open system
	/**
	 * get the power of adiabatic process
	 * @param s	  -- the state of start process
	 * @param P2  -- the end presure (Pa) 
	 * @param mass -- the speed of mass, g/s
	 * @return
	 */
	double adiabaticPower(GasState s,double P2, double mass)
	{
		double density=getDensity(s);
		double V1=mass/density;
		return adiabaticPowerPerVolume(s,P2,V1);
//		double k=(lamda-1)/lamda;
//		double power=s.P*V1/k*(Math.pow(P2/s.P, k)-1);
//		return power;
	}
	
	public String toString() {
		return String.format("Cp=%4.0f, Cv=%4.0f",  Cp,Cv);
	}

	public static void main(String[] args) {
		Gas g=new Gas("Air");
		L.p("gas=%s\n", g);
		GasState s= new GasState(200000,300);
		L.p("desity of [%s] is %f kg/m3\n",s,g.getDensity(s));
	}
	
}
