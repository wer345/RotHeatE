
public class GasState {
	double P,
	T,
	d; // density
	
	public GasState() {
		P=100130;
		T=300;
	}
	
	public GasState(double P,double T) {
		this.P=P;
		this.T=T;
	}
	
	
	public String toString() {
		return String.format("P=%6.0f Pa, T=%5.1fK",  P,T);
	}
}
