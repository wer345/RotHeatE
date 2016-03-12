import java.util.ArrayList;
import java.util.List;




import com.altheadx.pgx.utils.DataTable;


public class Engine {
	static DataTable t = new DataTable();

	static void tableTempGap()
	{
		HeatNetArgon net=new HeatNetArgon();
		List <Double> lstMass = new ArrayList<Double>();
		List <Integer> lstNode = new ArrayList<Integer>();
		for (double m=0.1;m<1.5;m+=0.1) 
			lstMass.add(m);

		for (int n=10;n<=60;n+=5)
			lstNode.add(n);
		
		
		int row=4;
		int col=4;
		
		
		for (double m:lstMass) 
			t.SetItem(row,col++,String.format("%6.4f",m));
		
		row++;
		for(int nodes:lstNode) {
			net.nodes=nodes;
			col=3;	
			t.SetItem(row,col++,String.format("%d", nodes));
			for (double m:lstMass) {
				net.mass=m;
				net.init();
				net.solver();
				
				t.SetItem(row,col++,String.format("%6.3f", net.getTempGap()));
	
				System.out.printf("temp gap=%6.2f\n", net.getTempGap());
			}
			row++;
		}
		t.SaveToCSV("TempGap_nodes_mass.csv");
	}

	static void table_Compressor()
	{
		double P1=500e3;
		double T1=350;
		double rpm=6000;
		double r1=0.1;
		double r2=0.2;
		
		GasProcess gp = new GasProcess();
		gp.lamda=1.667; //Argon
		gp.molMass=0.039948;

		int segs=500;
		
		int row=4;
		
		for(rpm=6000;rpm<=12000;rpm+=1000) {
			int col=4;
			State [] states = gp.compressor(rpm, r1,r2,P1,T1,segs);
			t.SetItem(row,col++,"6.0",rpm);
			for (int idx=0;idx<=500;idx+=10) 
				t.SetItem(row,col++,"6.4",states[idx].P);
			row++;
		}
		
		t.SaveToCSV("CompressorP_rpm_r.csv");
		
	}
	
	static void table_power() {
		
		GasProcess gp = new GasProcess();
		double P_low=500e3;
		//double T1=350;
		double rpm=12000;
		double r1=0.1;
		double r2=0.2;
		
		double T_low=350;
		double T_high=1000;
		
		double T_gap=40;
		double P_drop=1000;
		
		
		gp.lamda=1.667; //Argon
		gp.molMass=0.039948;
		double Q;
		double dT;
		double dP;
		double Cp;

		gp.lamda=1.667; //Argon
		gp.molMass=0.039948;
		
		State [] states = gp.compressor(rpm, r1,r2,P_low,T_low);
		int segs=states.length;
		State s1=states[0];
		State s2=states[segs-1];
		
		double mass=0.5;
		
		double P2=s2.P;
		double T2=s2.T;
		System.out.printf("s1= %s\n", s1);
		System.out.printf("s2= %s\n", s2);
		
		double power1=gp.adiabaticPower(states[0], P2, mass);
		System.out.printf("Cold wheel power is %f\n", power1);
		double dT_compressor=T2-T_low;
		Cp=0.52;  
		Q=Cp*dT_compressor*mass*1000;
//		System.out.printf("Temperature increase %6.2f, Q=%fW\n", dT, Q);

		// Estimate state at expander input 
		State s3Est = new State(s2.P-P_drop,T_high);
		double s4_P_est= P_low+P_drop;
		State s5=gp.adiabatic_P(s3Est,s4_P_est - s3Est.P);
		double dT_expander=s5.T - T_high;
		
		HeaterArgon heater = new HeaterArgon(5);
		HeatNetArgon net=new HeatNetArgon();
		net.nodes=40;
		net.mass=mass;
		net.T_heater=T_high;
		net.T_cooler=T_low;
		
		net.a=heater.get_A_f_m(mass);
		net.a_cooler=net.a;
		net.a_heater=net.a;
		net.dT_compressor=dT_compressor;
		net.dT_expander=dT_expander;
		
		net.init(); 
		net.solver();
		net.showT();
		
		double cold_in=net.get_T(1, 1);
		System.out.printf("temp cold in =%6.2f\n", cold_in);
		System.out.printf("temp gap=%6.2f\n", net.getTempGap());
		System.out.printf("temp cold in =%6.2f\n", cold_in);

		
		double T3=T_high;
		double P3=P2-P_drop;
		states = gp.compressor(rpm, r2, r1, P3,T3);
		State s3=states[0];
		State s4=states[segs-1];
		
		double P4=s4.P;
		double T4=s4.T;
		
//		System.out.printf("s3 %s\n",s3);
//		System.out.printf("s4 %s\n",s4);

		double power2=gp.adiabaticPower(s3, P4, mass);
//		System.out.printf("Hot wheel power is %f\n", power2);
//		System.out.printf("Pressure diff=%6.3f\n",P4-Pin);

		double P5=gp.Pin+gp.P_drop;
		s5=gp.adiabatic_P(s4,P5-P4);
//		System.out.printf("s5 %s\n",s5);
		
		double power3 = -gp.adiabaticPower(s4, gp.Pin, gp.mass);
//		System.out.printf("Net power output %f\n", power3);
		
		dT=gp.T_high-s5.T+gp.T_gap;
		
		Q=Cp*dT*mass*1000;
		double efficiency=100*power3/Q;
//		System.out.printf("dT=%6.3f Q=%5.2f, efficiency=%5.2f\n", dT,Q,efficiency);
		
		// find the speed of expander
		double r=rpm;
		while(r<2*rpm) {
			states = gp.compressor(r, r2, r1, s3.P,s3.T);
			P4=states[segs-1].P;
			//System.out.printf("rpm=%6.0f, P4=%6.0f\n",r,P4);
			if(P4>P_low)
				r+=0.01*rpm;
			else
				break;
		}
//		System.out.printf("Expander rpn=%6.0f\n",r);
//		System.out.printf("rpm diff=%6.0f, velocity diff at r2 %5.2f\n",r-rpm,2*Math.PI*r2*(r-rpm)/60);
	}
	
	public static void main(String[] args) {
		//tableTempGap();
		//table_Compressor();
		table_power();
		System.out.printf("Done\n");
	}

}
