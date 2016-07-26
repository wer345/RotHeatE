
public class GasProcess_air extends GasProcess {

	GasProcess_air()
	{
		lamda=1.4;
		molMass=28.9645;
	}
	
	void test()
	{
		State s = new State();
		double d=this.getDensity(s);
		System.out.printf("S0=%s, density=%f g/m3\n", s,d);
		double dp=100000;
		double p1=s.P+dp;
		State s1 = adiabatic_P(s,p1);
		double d1=this.getDensity(s1);
		// get the power to compress 1 m^3 air
		double power=adiabaticPowerPerMass(s,p1,d);
		System.out.printf("S1=%s, density=%f g/m3\n", s1,d1);
		System.out.printf("power=%9.0fJ, rate=%6.2f\n",power,power/dp);
		System.out.printf("density rate %6.2f\n",d1/d);
	}
	
	public static void main(String[] args) {
		new GasProcess_air().test();
	}
}
