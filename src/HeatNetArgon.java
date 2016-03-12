import com.altheadx.pgx.utils.DataTable;

import Jama.Matrix;


public class HeatNetArgon {
	public int nodes=30;
	public double a=0.426;	// W/K heater transfer rate of fluid channel
	public double mass=2;	// g/s mass flow rate of fluid channel
	public double Cp=0.52;	// J/(g*K) fluid heat capacity
	public double T_heater=600;	// K  temperature of heater
	public double T_cooler=400;	// K	 temperature of cooler
	public double linkRes=0.5;		// W/K heater transfer rate of the middle metal 
	public double a_heater=1.5; // W/K heater transfer rate of the heater
	public double a_cooler=1.5; // W/K heater transfer rate of the cooler
	double dT_compressor = 30;
	double dT_expander = -30;
	
	Matrix A,b;
	int matrixSize;
	
	Heater [] exHs; // heaters on exchanger 
	Heater [] exCs; // coolers on exchanger
	
	Heater heater,cooler;
	
	double[] T;
	
	int index(int level,int position)
	{
		int idx=nodes*(level-1)+position-1;
		if(level!=1) 
			idx++;
		return idx;
	}

	HeatNetArgon()
	{
		
	}
	
	double get_T(int level,int position) {
		return T[index(level,position)];
	}
	
	void init()
	{
		matrixSize=4*nodes+2;
		A = new Matrix(matrixSize,matrixSize);
		b = new Matrix(matrixSize,1);
		exHs = new Heater[nodes];
		exCs = new Heater[nodes];
		HeaterArgon ht = new HeaterArgon(5);

		a=ht.get_A_f_m(mass);
		for(int i=0;i<nodes;i++) {
			exHs[i]=new Heater(a,Cp,mass);
			exCs[i]=new Heater(a,Cp,mass);
		}
		heater=new Heater(a_heater,Cp,mass);
		heater.T=T_heater;
		cooler=new Heater(a_cooler,Cp,mass);
		cooler.T=T_cooler;
		
		
		int row=0;
		// set cooler
		A.set(row,index(4,1),cooler.k-1);
		A.set(row,index(1,1),1);
		b.set(row,0,cooler.k*cooler.T + dT_compressor);
		// set heater
		row=1;
		A.set(row,index(1,nodes+1),heater.k-1);
		A.set(row,index(4,nodes+1),1);
		b.set(row,0,heater.k*heater.T + dT_expander);
		
		for(int node=1;node<=nodes;node++) {
			// ex heater
			row++;
			A.set(row,index(1,node),1-exHs[node-1].k);
			A.set(row,index(1,node+1),-1);
			A.set(row,index(2,node),exHs[node-1].k);
			b.set(row,0,0);
			// ex cooler
			row++;
			A.set(row,index(4,node+1),1-exCs[node-1].k);
			A.set(row,index(4,node),-1);
			A.set(row,index(3,node),exCs[node-1].k);
			b.set(row,0,0);
			// Q ex
			row++;
			A.set(row,index(2,node),exHs[node-1].a);
			A.set(row,index(1,node),-exHs[node-1].a);
			A.set(row,index(4,node+1),-exCs[node-1].a);
			A.set(row,index(3,node),exCs[node-1].a);
			b.set(row,0,0);
			
			// Q passed
			row++;
			A.set(row,index(2,node),exHs[node-1].a+linkRes);
			A.set(row,index(1,node),-exHs[node-1].a);
			A.set(row,index(3,node),-linkRes);
			b.set(row,0,0);

		}
		
	}

	void showT()
	{
		for(int i=0;i<=nodes;i++) 
			System.out.printf("%6.2f ", T[i]);
		System.out.printf("\n    ");
		
		for(int i=0;i<nodes;i++) 
			System.out.printf("%6.2f ", T[nodes+1+i]);
		System.out.printf("\n    ");
		
		for(int i=0;i<nodes;i++) 
			System.out.printf("%6.2f ", T[2*nodes+1+i]);
		System.out.printf("\n");
		
		for(int i=0;i<=nodes;i++) 
			System.out.printf("%6.2f ", T[3*nodes+1+i]);
		System.out.printf("\n");

		
		System.out.printf("dT_compressor %6.3f, dT_expander %6.3f\n", dT_compressor, dT_expander);
		System.out.printf("T cooler out %6.3f, T heater out %6.3f\n",cooler.Tout,heater.Tout);
		
		System.out.printf("T gap at nodes\n"); 
		for(int i=0;i<=nodes;i++) 
			System.out.printf("%6.2f ", T[3*nodes+1+i]-T[i]);
		System.out.printf("\n");
		
		for(int node=1;node<=nodes;node++) {
			exHs[node-1].setT(T[index(1,node)],T[index(1,node+1)],T[index(2,node)]);
			exCs[node-1].setT(T[index(4,node+1)],T[index(4,node)],T[index(3,node)]);
		}
		
		System.out.printf("Q at nodes\n"); 
		for(int node=1;node<=nodes;node++) 
			System.out.printf("%6.2f ",exHs[node-1].getQ());
		System.out.printf("\n");
		
		System.out.printf("Temp diff at links\n"); 
		for(int node=1;node<=nodes;node++) 
			System.out.printf("%6.2f ",linkRes*(T[index(3,node)]-T[index(2,node)]));
		System.out.printf("\n");
		
	}
	
	void solver() {
		Matrix X = A.solve(b);
		T= new double[matrixSize];
		for(int i=0;i<matrixSize;i++)
			T[i]=X.get(i,0);

	      cooler.setT(X.get(index(4,1),0),X.get(index(1,1),0) - dT_compressor,cooler.T);
	      heater.setT(X.get(index(1,nodes+1),0),X.get(index(4,nodes+1),0) - dT_expander,heater.T);
	      for(int node=1;node<=nodes;node++) {
	    	  exHs[node-1].setT(X.get(index(1,node),0),X.get(index(1,node+1),0),X.get(index(2,node),0));
	    	  exCs[node-1].setT(X.get(index(4,node+1),0),X.get(index(4,node),0),X.get(index(3,node),0));
	      }
	}
	
	double getTempGap()
	{
		return T[index(4,1)]-T[index(1,1)];
	}
	
	static void showAllResult()
	{
		HeatNetArgon net=new HeatNetArgon();
		net.nodes=20;
		net.mass=0.5;
		net.init();
		net.solver();
		net.showT();
		System.out.printf("temp gap=%6.2f\n", net.getTempGap());
	}

	static void plot_nodes_r()
	{
		HeatNetArgon net=new HeatNetArgon();
		DataTable table = new DataTable();
		int[] listNodes = {10,15,20,25,30,35,40,45,50};
		double[] list_r ={0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.50};
		int shift=list_r.length+5;

		int row=1;
		int col=1;
		
		for (int nodes:listNodes) {
			table.SetItem(shift+col, row, " "+nodes);
			table.SetItem(row, col++, " "+nodes);
		}
		
		
		for(double r:list_r) {
			row++;
			col=0;
			table.SetItem(shift+col, row, " "+r);
			table.SetItem(row, col++, " "+r);
			net.linkRes=r;
			for (int nodes:listNodes) {
				net.nodes=nodes;
				net.init();
				net.solver();
				table.SetItem(shift+col, row, String.format(" %6.2f",net.getTempGap()));
				table.SetItem(row, col++, String.format(" %6.2f",net.getTempGap()));
			}
		}
		table.SaveToCSV("on_r.csv");
	}

	static void plot_a_r()
	{
		HeatNetArgon net=new HeatNetArgon();
		DataTable table = new DataTable();
		double[] list_a = {0.04, 0.045, 0.05, 0.055, 0.06, 0.065, 0.07, 0.075, 0.08}; // heat transfer rate
		double[] list_r ={0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.50};
		int shift=list_r.length+5;

		net.nodes=70;
		net.mass=0.4;
		
		int dups=6;
		net.mass*=dups;
		for(int i=0;i<list_a.length;i++)
			list_a[i]*=dups;
		net.a_heater*=dups;
		net.a_cooler*=dups;
		
		table.SetItem(0,0,String.format("mass flow=%6.2f g/s, number of node=%d", net.mass,net.nodes));
		table.SetItem(1,0,String.format("T: heater=%6.2fK , cooler =%6.2fK", net.T_heater,net.T_cooler));
		table.SetItem(2,0,String.format("a: heater=%6.3fW/K , cooler =%6.3fW/K", net.a_heater,net.a_cooler));
		int row=3;
		int col=1;
		
		table.SetItem(row,0,"r\\a");
		for (double a:list_a) {
			//table.SetItem(shift+col, row, String.format(" %6.3f",a));
			table.SetItem(row, col++, String.format(" %6.3f",a));
		}
		
		
		for(double r:list_r) {
			row++;
			col=0;
			//table.SetItem(shift+col, row, String.format(" %6.3f",r));
			table.SetItem(row, col++, String.format(" %6.3f",r));
			net.linkRes=r;
			for (double a:list_a) {
				net.a=a;
				net.init();
				net.solver();
				//table.SetItem(shift+col, row, String.format(" %6.2f",net.getTempGap()));
				table.SetItem(row, col++, String.format(" %6.2f",net.getTempGap()));
			}
		}
		table.SaveToCSV("on_a_r.csv");
	}
	
	public static void main(String[] args) {
		showAllResult();
		//plot_nodes_r();
		//plot_a_r();
		System.out.printf("Done\n");
	}

}
