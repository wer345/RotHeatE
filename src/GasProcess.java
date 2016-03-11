
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
			return String.format("P=%6.0f Pa, T=%5.1fK",  P,T);
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
	
	State adiabatic_P(State s,double dP)
	{
		double P2=s.P+dP;
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

	State [] compressor(double rpm, double r1, double r2, double P1,double T1) {
		int segs=500; // the number of segment from r1 to r2
		State [] states= new State[segs+1];
		states[0]=new State(P1,T1);
		double rot=2*Math.PI*rpm/60;
		double dr=(r2-r1)/segs;
		double power=0;
		for(int i=0;i<segs;i++) {
			double density=getDensity(states[i]);
			double r=r1+i*dr;
			double acc=r*rot*rot;
			double dp=acc_pressure(acc,dr,density);
			states[i+1]=adiabatic_P(states[i],dp);
			//System.out.printf("%d,  %9.5f, %9.0f, %9.3f,  %6.2f\n",i,density,states[i].P,states[i].T,dp);
		}
		return states;
	}
	
	static void test_1() {
		GasProcess p= new GasProcess();
		State s= p.new State(300000,300);
		double density=p.getDensity(s);
		System.out.printf("density=%f\n", density);
		
		double dp=p.acc_pressure(270000,0.1,density);
		System.out.printf("dP=%f\n", dp);
		State s1=p.adiabatic_P(s,dp);
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
	
	void test_powout() {
		double r1=0.1;
		double r2=0.2;
		double Pin=100E3;
		double T_low=350;
		double T_high=1000;
		double mass=0.002;
		double rpm=9000;
		double T_gap=40;
		
		double Q;
		double dT;
		double dP;
		double Cp;

		State [] states = compressor(rpm, r1,r2,Pin,T_low);
		int segs=states.length;
		
		
		double P2=states[segs-1].P;
		double T2=states[segs-1].T;
		double power1=adiabaticPower(states[0], P2, mass);
		System.out.printf("Cool side power is %f\n", power1);
		System.out.printf("P2= %6.0f, T2=%6.2f\n", P2,T2);
		dT=T2-T_low;
		Cp=0.94;  //C4F8
		
		Q=Cp*dT*mass*1000;
		System.out.printf("Temperature inc %6.2f\n", dT);
		
		State s2=adiabatic_P(states[0],P2-states[0].P);
		System.out.printf("adiabatic T2'=%6.2f\n", s2.T);
		
		
		T2=T_high;
		states = compressor(rpm, r2, r1, P2,T2);
		State s3=states[segs-1];
		
		double P3=s3.P;
		double T3=s3.T;
		System.out.printf("state at hot %s\n",s3);
		System.out.printf("P3= %6.0f, T3=%6.2f\n", P3,T3);
		double power2=adiabaticPower(states[0], P3, mass);
		System.out.printf("Hot side power is %f\n", power2);
		System.out.printf("P3-Pin=%6.3f\n",P3-Pin);

		double P4=Pin;
		State s4=adiabatic_P(s3,P4-P3);
		double power3=adiabaticPower(s3, P4, mass);
		System.out.printf("Net power output %f\n", power3);
		System.out.printf("P4= %6.0f, T4=%6.2f\n", P4,s4.T);
		dT=T_high-s4.T+T_gap;
		System.out.printf("Temperature drop %6.2f\n",dT);
		Cp=1.1;
		Q=Cp*dT*mass*1000;
		System.out.printf("dT=%6.3f Q=%5.2f, efficiency=%5.2f\n", dT,Q,100*power3/Q);
		
		double r=rpm;
		while(r<2*rpm) {
			states = compressor(r, r2, r1, P2,T2);
			P4=states[segs-1].P;
			//System.out.printf("rpm=%6.0f, P4=%6.0f\n",r,P4);
			if(P4>Pin)
				r+=0.01*rpm;
			else
				break;
		}
		
		System.out.printf("rpm diff=%6.0f, velocity diff at r2 %5.2f\n",r-rpm,2*Math.PI*r2*(r-rpm)/60);
	}

	double r1=0.1;
	double r2=0.2;
	double Pin=100E3;
	double T_low=350;
	double T_high=900;
	double mass=0.001;
	double rpm=6000;
	double T_gap=30;
	double P_drop=1000;
	
	Peformance test_powout_Argon(String [] names, double[] values) {
		double Q;
		double dT;
		double dP;
		double Cp;

		if (names.length==values.length) {
			for(int i=0;i<names.length;i++) {
				String name=names[i];
				double value=values[i];
				if(name.equals("r1"))
					r1=value;
				else if(name.equals("Pin"))
					Pin=value;
				else if(name.equals("T_low"))
					T_low=value;
				else if(name.equals("T_high"))
					T_high=value;
				else if(name.equals("T_gap"))
					T_gap=value;
				else if(name.equals("rpm"))
					rpm=value;
				else if(name.equals("P_drop"))
					P_drop=value;
			}
		}
		lamda=1.667; //Argon
		molMass=0.039948;
		
		State [] states = compressor(rpm, r1,r2,Pin,T_low);
		int segs=states.length;
		State s1=states[0];
		State s2=states[segs-1];
		
		
		double P2=s2.P;
		double T2=s2.T;
//		System.out.printf("s1= %s\n", s1);
//		System.out.printf("s2= %s\n", s2);
		
		double power1=adiabaticPower(states[0], P2, mass);
//		System.out.printf("Cold wheel power is %f\n", power1);
		dT=T2-T_low;
		Cp=0.52;  
		Q=Cp*dT*mass*1000;
//		System.out.printf("Temperature increase %6.2f, Q=%fW\n", dT, Q);
		
		double T3=T_high;
		double P3=P2-P_drop;
		states = compressor(rpm, r2, r1, P3,T3);
		State s3=states[0];
		State s4=states[segs-1];
		
		double P4=s4.P;
		double T4=s4.T;
//		System.out.printf("s3 %s\n",s3);
//		System.out.printf("s4 %s\n",s4);

		double power2=adiabaticPower(s3, P4, mass);
//		System.out.printf("Hot wheel power is %f\n", power2);
//		System.out.printf("Pressure diff=%6.3f\n",P4-Pin);

		double P5=Pin+P_drop;
		State s5=adiabatic_P(s4,P5-P4);
//		System.out.printf("s5 %s\n",s5);
		
		double power3 = -adiabaticPower(s4, Pin, mass);
//		System.out.printf("Net power output %f\n", power3);
		
		dT=T_high-s5.T+T_gap;
		
		Q=Cp*dT*mass*1000;
		double efficiency=100*power3/Q;
//		System.out.printf("dT=%6.3f Q=%5.2f, efficiency=%5.2f\n", dT,Q,efficiency);
		
		// find the speed of expander
		double r=rpm;
		while(r<2*rpm) {
			states = compressor(r, r2, r1, s3.P,s3.T);
			P4=states[segs-1].P;
			//System.out.printf("rpm=%6.0f, P4=%6.0f\n",r,P4);
			if(P4>Pin)
				r+=0.01*rpm;
			else
				break;
		}
//		System.out.printf("Expander rpn=%6.0f\n",r);
//		System.out.printf("rpm diff=%6.0f, velocity diff at r2 %5.2f\n",r-rpm,2*Math.PI*r2*(r-rpm)/60);
		Peformance p = new Peformance(power1,power3,efficiency);
		p.P1=Pin;
		p.P2=P2;
		return p;
	}
	
	void test_heatPump() {
		double P1=100e3;
		double T1=300;
		double dP=50e3;
		double mass=0.001;
		
		State s1= new State(P1,T1);
		State s2 = adiabatic_P(s1,dP);
		//System.out.printf("lamda=%f,S2=%s\n", lamda,s2);
		
		lamda=1.667; //Argon
		molMass=0.039948;
		s2 = adiabatic_P(s1,dP);
		System.out.printf("lamda=%f,S2=%s\n", lamda,s2);
		double power1=adiabaticPower(s1,P1+dP,mass);
		System.out.printf("power1 = %fW\n",power1);
		double dT=s2.T-s1.T;
		double Cp=0.52; // J/(g*K)
		double dQ=Cp*dT;
		System.out.printf("dT=%f, dQ=%fW\n",dT,dQ);
		
		double P3=100e3;
		double T3=800;
		State s3= new State(P3,T3);
		State s4 = adiabatic_P(s3,dP);
		System.out.printf("s3=%s\n", s3);
		System.out.printf("s4=%s\n", s4);
		double power2=adiabaticPower(s3,P3+dP,mass);
		System.out.printf("power2 = %fW\n",power2);
		System.out.printf("dPower=%fW\n",power2-power1);
	}
	
	void test_power_chart() {
		String [] variables = {"rpm"};
		double [] values = {0};
		Pin=600e3;
		double value=1000;
		for(int i=0;i<15;i++) {
			values[0]=value;
			value+=1000;
			Peformance  pfm=test_powout_Argon(variables,values);
			System.out.printf("RPM =%f,P1=%7.0f, ,P2=%7.0f, dPower=%6.3fW, efficiency=%4.1f\n",value,pfm.P1, pfm.P2, pfm.powerNet,pfm.efficiency);
			
		}
		
	}
	public static void main(String[] args) {
		//(new GasProcess()).test_powout();
		(new GasProcess()).test_power_chart();
		
		//(new GasProcess()).test_heatPump();
		
//		(new GasProcess()).compressor(6000,0.2, 0.1, 954324.5,307.1669);
//		(new GasProcess()).compressor(6000,0.2, 0.1, 958040.099666,307.227678);
	
	}

}
