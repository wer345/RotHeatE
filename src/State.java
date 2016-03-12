
public class State {
	double P,
	T,
	d; // density
	
	public State() {
		P=100130;
		T=300;
	}
	
	public State(double P,double T) {
		this.P=P;
		this.T=T;
	}
	
	public String toString() {
		return String.format("P=%6.0f Pa, T=%5.1fK",  P,T);
	}
}
