
public class WaveGen extends Gas {

	WaveGen()
	{
		
	}

	WaveGen(String gasName)
	{
		super(gasName);
	}
	
	void test()
	{
		double e=0.95; // compressor and expender efficiency 
		System.out.printf("Gas %s\n",gasName);
		GasState s1 = new GasState();
		double d=this.getDensity(s1);
		double mass=d;
		System.out.printf("S0=%s, density=%f kg/m3, mass rate=%6.4f\n", s1,d,mass);
		double dp=100000;
		double P1=s1.P;
		double P2=P1+dp;
		GasState s2 = adiabatic_on_P(s1,P2);
		double d1=this.getDensity(s2);
		// get the power to compress 1 m^3 air
		double powerIn=adiabaticPower(s1,P2,mass);
		System.out.printf("S1=%s, density=%f kg/m3\n", s2,d1);
		System.out.printf("power Comp =%9.0fJ",powerIn);
		System.out.printf(", u changed %9.0fJ",mass*(get_u(s2.T)-get_u(s1.T)));
		System.out.printf(", h changed %9.0fJ",mass*(get_h(s2.T)-get_h(s1.T)));
		System.out.printf(", density rate= %6.2f\n",d1/d);
		
		double Theating=200;
		GasState s3=new GasState(s2.P,s2.T+Theating);
		double d3=this.getDensity(s3);
		System.out.printf("S3=%s, density=%f kg/m3\n", s3,d3);
		
		GasState s4 = adiabatic_on_P(s3,P1);
		double heat=(s3.T-s4.T)*Cp*mass;
		System.out.printf(", Heat  needed from %5.0fK to %5.0fK: %9.0fJ \n",s4.T,s3.T,heat);
		double d4=this.getDensity(s4);
		System.out.printf("S4=%s, density=%f kg/m3\n", s4,d4);
		double powerOut=adiabaticPower(s3,P1,mass);
		double output=-(powerIn/e+powerOut*e);
		System.out.printf("power Expander=%9.0fJ,power output=%9.0f, efficiency %f\n",powerOut,output,output/heat);
		System.out.printf("u changed %9.0fJ",mass*(get_u(s4.T)-get_u(s3.T)));
		System.out.printf(", h changed %9.0fJ\n",mass*(get_h(s4.T)-get_h(s3.T)));
		System.out.printf("\n");
	}
	
	public static void main(String[] args) {
		new WaveGen("Air").test();
//		new WaveGen("Argon").test();
//		new WaveGen("CO2").test();
//		new WaveGen("N2").test();
	}
}
