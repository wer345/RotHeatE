
public class Heater {
	public double a, Cp, m, k;
	public double Tin,Tout,T;
	Heater(double a,double Cp, double m)
	{
		this.a=a;
		this.Cp=Cp;
		this.m=m;
		k=a/(Cp*m);
	}
	
	double getTout(double Tin, double T)
	{
		return Tin+k*(T-Tin);
	}
	
	void setT(double Tin, double Tout, double T)
	{
		this.Tin=Tin;
		this.Tout=Tout;
		this.T=T;
		if(!verify()) {
			System.out.printf("Fail\n");
		}
	}
	
	boolean verify() {
		double err=getTout(Tin,T)-Tout;
		if (Math.abs(err)<0.01) 
			return true;
		else
			return false;
	}

	double getQ() {
		return a*(T-Tin);
	}
}
