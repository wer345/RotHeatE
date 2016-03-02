import Jama.Matrix;


public class Test1 {

	public static void main(String[] args) {
	      double[][] vals = {{1.,2.,3},{4.,5.,6.},{7.,8.,11.}};
	      Matrix A = new Matrix(vals);
	      System.out.printf("A=%s\n", A.toString());
	      Matrix b = Matrix.random(3,1);
	      System.out.printf("b=%s\n", b.toString());
	      Matrix x = A.solve(b);
	      System.out.printf("x=%s\n", x.toString());
	      Matrix r = A.times(x).minus(b);
	      System.out.printf("r=%s\n", r.toString());
	      double rnorm = r.normInf();
	}

}
