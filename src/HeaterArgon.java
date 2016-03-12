
public class HeaterArgon {
	static double Cp=0.52; // Cp of Argon
	int nofChannels=1;
	
	
	HeaterArgon(int n)
	{
		nofChannels=n;
	}
	/**
	 * get heat transfer rate from mass flow speed, the data is from the CAD for gas Argon
	 * @param m - mass flow speed in g/s
	 * @return heat transfer rate W/K
	 */
	double get_A_f_m(double m) {
		double a;
		m=m/nofChannels;
		if(m<0.2)
			a=Cp*m/(1+2.84042*m);
		else 
			a=0.219024635*(Math.pow(m, 0.15)-1) + 0.1133;

		return a*nofChannels;
	}

	/**
	 * Get pressure loss 
	 * @param m - mass flow g/s
	 * @param P - prussure of flow Pa
	 * @return pressure loss Pa
	 */
	
	double get_P_loss_f_m(double m,double P) {
		m=m/nofChannels;
		double dP= 86E6*m*(m+2.132)/P;
		return dP/3; // The data are from CAD with 3 heaters
	}

	public static void main(String[] args) {
		HeaterArgon ht = new HeaterArgon(5);
		double m=0.1;
		double P=500e3;
		while(m<=5) {
			double A=ht.get_A_f_m(m);
			double dP=ht.get_P_loss_f_m(m,P);
			System.out.printf("m=%5.2f, A=%6.5f, dP=%6.3f\n",m,A,dP);
			m+=0.1;
		}
	}

}
