import Jama.Matrix;


public class HeatNet {
	public int n;
	public double a=0.2;	// W/K
	public double m=0.6;	// g/s
	public double Cp=0.8; // J/(g*K)
	public double T_heater=700; 	//K
	public double T_cooler=300;	//K
	public double r=0.6;	// heater transfer rate of the midle metal W/K
	
	Matrix A,b;
	int size;
	
	Heater [] exHs; // heaters on exchanger 
	Heater [] exCs; // coolers on exchanger
	
	Heater heater,cooler;
	
	double[] T;
	
	int index(int level,int position)
	{
		int idx=n*(level-1)+position-1;
		if(level!=1) 
			idx++;
		return idx;
	}

	HeatNet()
	{
		
	}
	
	void init(int nodes)
	{
		this.n=nodes;
		size=4*nodes+2;
		A = new Matrix(size,size);
		b = new Matrix(size,1);
		exHs = new Heater[nodes];
		exCs = new Heater[nodes];
		for(int i=0;i<nodes;i++) {
			exHs[i]=new Heater(a,Cp,m);
			exCs[i]=new Heater(a,Cp,m);
		}
		heater=new Heater(0.3,Cp,m);
		heater.T=700;
		cooler=new Heater(0.3,Cp,m);
		cooler.T=300;
		
		int row=0;
		// set cooler
		A.set(row,index(4,1),1-cooler.k);
		A.set(row,index(1,1),-1);
		b.set(row,0,-cooler.k*cooler.T);
		// set heater
		row=1;
		A.set(row,index(1,n+1),1-heater.k);
		A.set(row,index(4,n+1),-1);
		b.set(row,0,-heater.k*heater.T);
		
		for(int node=1;node<=n;node++) {
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
			A.set(row,index(2,node),exHs[node-1].a+r);
			A.set(row,index(1,node),-exHs[node-1].a);
			A.set(row,index(3,node),-r);
			b.set(row,0,0);

		}
		
	}

	void solver() {
	      Matrix X = A.solve(b);
	      
	      for(int i=0;i<=n;i++) {
	    	  System.out.printf("%6.2f ", X.get(i,0));
	      }
	      System.out.printf("\n    ");

	      for(int i=0;i<n;i++) {
	    	  System.out.printf("%6.2f ", X.get(n+1+i,0));
	      }
	      System.out.printf("\n    ");
	      
	      for(int i=0;i<n;i++) {
	    	  System.out.printf("%6.2f ", X.get(2*n+1+i,0));
	      }
	      System.out.printf("\n");

	      for(int i=0;i<=n;i++) {
	    	  System.out.printf("%6.2f ", X.get(3*n+1+i,0));
	      }
	      System.out.printf("\n\n");
	      
	      for(int i=0;i<=n;i++) {
	    	  System.out.printf("%6.2f ", X.get(3*n+1+i,0)-X.get(i,0));
	      }
	      System.out.printf("\n");

	      cooler.setT(X.get(index(4,1),0),X.get(index(1,1),0),cooler.T);
	      heater.setT(X.get(index(1,n+1),0),X.get(index(4,n+1),0),heater.T);
	      for(int node=1;node<=n;node++) {
	    	  exHs[node-1].setT(X.get(index(1,node),0),X.get(index(1,node+1),0),X.get(index(2,node),0));
	    	  exCs[node-1].setT(X.get(index(4,node+1),0),X.get(index(4,node),0),X.get(index(3,node),0));
	      }
	      
	      for(int node=1;node<=n;node++) {
		      System.out.printf("%6.2f ",exHs[node-1].getQ());
	      }
	      System.out.printf("\n");

	      for(int node=1;node<=n;node++) {
		      System.out.printf("%6.2f ",r*(X.get(index(3,node),0)-X.get(index(2,node),0)));
	      }
	      System.out.printf("\n");
	      
	      for(int node=1;node<=n;node++) {
		      System.out.printf("%6.2f ",exCs[node-1].getQ());
	      }
	      System.out.printf("\n");
	      
	      System.out.printf("Q: Heater %6.2f, Cooler %6.2f\n",heater.getQ(),cooler.getQ());
	      
	}
	
	public static void main(String[] args) {
		HeatNet net=new HeatNet();
		net.init(50);
		net.solver();
		
//	      double[][] vals = {{1.,2.,3},{4.,5.,6.},{7.,8.,11.}};
//	      Matrix A = new Matrix(vals);
//	      System.out.printf("A=%s\n", A.toString());
//	      Matrix b = Matrix.random(3,1);
//	      System.out.printf("b=%s\n", b.toString());
//	      Matrix x = A.solve(b);
//	      System.out.printf("x=%s\n", x.toString());
//	      Matrix r = A.times(x).minus(b);
//	      System.out.printf("r=%s\n", r.toString());
//	      double rnorm = r.normInf();
	}

}
