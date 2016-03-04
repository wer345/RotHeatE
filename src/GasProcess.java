
public class GasProcess {

	class State {
		double P,T,
		d; // density
		
		public State() {
			P=100130;
			T=300;
		}
		
		State(double P,double T) {
			this.P=P;
			this.T=T;
		}
		
		public String toString() {
			return String.format("P=%f Pa, T=%fK",  P,T);
		}
	}
	
	double R=8.3144598;
	double lamda = 1.0536;  // of C4F8
	double	molMass = 0.20004; //kg/mol
	
	double getDensity(State s)
	{
		double d=molMass*s.P/(R*s.T);
		return d;
	}
	/**
	 * @param molWeight  kg/mol
	 */
	void set_R(double molWeight) 
	{
		
	}
	
	State adiabatic_P(State s,double P2)
	{
		double T2=s.T*Math.pow(s.P/P2,(1-lamda)/lamda);
		State rst = new State(P2,T2);
		return rst;
	}

	//Open system
	double adiabaticPower(State s,double P2, double mass)
	{
		double density=getDensity(s);
		double V1=mass/density;
		double k=(lamda-1)/lamda;
		double power=s.P*V1/k*(Math.pow(P2/s.P, k)-1);
		return power;
	}
	
	double acc_pressure(double acc,double distance,double density)
	{
		return density*distance*acc;
	}
	
	public static void main(String[] args) {
		GasProcess p= new GasProcess();
		State s= p.new State(300000,300);
		double density=p.getDensity(s);
		System.out.printf("density=%f\n", density);
		
		double dp=p.acc_pressure(270000,0.1,density);
		System.out.printf("dP=%f\n", dp);
		State s1=p.adiabatic_P(s,s.P+dp);
		System.out.printf("new state is %s\n", s1);
		double P1=s.P;
		double P2=P1+dp;
		double power1=p.adiabaticPower(s, P2, 0.002);
		System.out.printf("Cool side power is %f\n", power1);
		
		s= p.new State(300000,900);
		double power2=p.adiabaticPower(s, P2, 0.002);
		System.out.printf("Hot side power is %f\n", power2);
		
		System.out.printf("Net power is %f\n", power2-power1);
	}

}
