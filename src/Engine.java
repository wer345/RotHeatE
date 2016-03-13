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
	
				L.p("temp gap=%6.2f\n", net.getTempGap());
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
		double P_drop=100;
		
		
		gp.lamda=1.667; //Argon
		gp.molMass=0.039948;
		double dT;
		double dP;
		double Cp=0.52;

		gp.lamda=1.667; //Argon
		gp.molMass=0.039948;
		
		double mass=0.9;
		State [] states;
		double P2=0,T2=0;
		int segs=0;
		State s4;
		
		double T_compressor_in=T_low;
		double T_expander_in=T_high;
		int loopSize=8;
		double power1=0;
		State s3=null;
		double P4_est=0;
		HeatNetArgon net=null;
		double dT_compressor=0;
		double dT_expander=0;
		
		// Find heat balance for the temperature increase of compressor and decrease of expander
		
		L.off();
		for(int loop=1;loop<=loopSize;loop++)
		{
			//if(loop==loopSize)
			//	L.on();

			L.p("========== Loop %d ==========\n",loop);
			states = gp.compressor(rpm, r1,r2, P_low, T_compressor_in);
			segs=states.length;
			State s1=states[0];
			State s2=states[segs-1];
			
			
			P2=s2.P;
			T2=s2.T;
			L.p("s1= %s\n", s1);
			L.p("s2= %s\n", s2);
			
			power1=gp.adiabaticPower(states[0], P2, mass);
			L.p("Compressor power is %6.2f\n", power1);
			dT_compressor=T2-T_compressor_in;
			
			L.p("Compressor temperature increase %6.2f\n", dT_compressor);
	
			// Estimate state at expander input 
			s3 = new State(s2.P - P_drop,T_expander_in);
			
			L.p("Expander input state = %s\n", s3);
			P4_est= P_low + P_drop;
			s4=gp.adiabatic_P(s3,P4_est);
			
			L.p("Expander output state = %s\n", s4);
			dT_expander = s4.T - T_expander_in;
			L.p("Expander Temperature drop = %5.2f\n", dT_expander);
			
			HeaterArgon heater = new HeaterArgon(5);
			net=new HeatNetArgon();
			
			net.nodes=40;
			net.mass=mass;
			net.T_heater=T_high;
			net.T_cooler=T_low;
			
			net.a=heater.get_A_f_m(mass);
			net.a_cooler = net.a;
			net.a_heater = net.a;
			net.dT_compressor=dT_compressor;
			net.dT_expander=dT_expander;
			
			net.init(); 
			net.solver();
			net.showT();

			double T_reg_HeaterIn=net.get_T(1, 1);
			double T_reg_HeaterOut=net.get_T(1, net.nodes);
			double T_reg_CoolerIn=net.get_T(4, net.nodes);
			double T_reg_CoolerOut=net.get_T(4, 1);
			L.p("T reg_HeaterIn = %6.2f\n", T_reg_HeaterIn);
			L.p("T reg_HeaterOut = %6.2f\n", T_reg_HeaterOut);
			L.p("T reg_CoolerIn = %6.2f\n", T_reg_CoolerIn);
			L.p("T reg_CoolerOut = %6.2f\n", T_reg_CoolerOut);
			L.p("T gap = %6.2f\n", net.getTempGap());
			//L.p("temp cold in =%6.2f\n", cold_in);

			T_compressor_in = net.cooler.Tout;
			T_expander_in = net.heater.Tout;
			
			L.p("T CoolerOut = %6.2f\n", T_compressor_in);
			L.p("T HeaterOut = %6.2f\n", T_expander_in);

			L.p("Cooler = %s\n", net.cooler);
			L.p("Heater = %s\n", net.heater);
			
			
		}
		L.p("========== Loop End ==========\n");
		L.on();
		net.showT();
		double power2=gp.adiabaticPower(s3, P4_est, mass);
		L.p("Compressor power is %6.2f\n", power1);
		L.p("Expander power is %6.2f\n", power2);
		L.p("Expander Temperature drop = %5.2f, Qexp=%6.3f\n", dT_expander,Cp*mass*dT_expander);
		
		double netPower=power2+power1;
		L.p("Net power is %6.3f\n", netPower);
		double Qheater=net.heater.getQ();
		double Qcooler=net.cooler.getQ();
		
		double efficiency=100*netPower/Qheater;
		L.p(" Qheater=%5.2f, Qcooler=%5.2f, efficiency=%5.2f\n", Qheater, Qcooler, efficiency);


		L.p("Cooler = %s\n", net.cooler);
		T_compressor_in = net.cooler.Tout;
		
		double T_reg_HeaterIn=net.get_T(1, 1);
		L.p("T reg_HeaterIn = %6.2f\n", T_reg_HeaterIn);
		dT=T_reg_HeaterIn-T_compressor_in;
		double Qcomp=Cp*mass*(dT);
		L.p("dTcomp=%6.2f, Qcomp = %6.2f\n", dT, Qcomp);
		Qcomp=Cp*mass*(dT_compressor);
		L.p("dTcomp=%6.2f, Qcomp = %6.2f\n", dT_compressor, Qcomp);
		
		double T_expander_out=net.get_T(4, net.nodes+1);
		L.p("T reg_CoolerIn = %6.2f\n", T_expander_out);
		L.p("Heater = %s\n", net.heater);

		T_expander_in = net.heater.Tout;
		L.p("T expander in = %6.2f\n", T_expander_in);
		L.p("T expander out = %6.2f\n", T_expander_out);

		dT=T_expander_in-T_expander_out;
		double Qexp=Cp*mass*(dT);
		L.p("dTexp=%6.2f, Qexp = %6.3f\n", dT, Qexp);
		L.p("Qdiff = %6.3f\n", net.heater.getQ() + net.cooler.getQ());
	}
	
	public static void main(String[] args) {
		//tableTempGap();
		//table_Compressor();
		table_power();
		L.p("Done\n");
	}

}
