package ontology.elements;

import jade.content.Predicate;

@SuppressWarnings("serial")
public class Invoice implements Predicate {

	double monies;

	public Invoice(double d) {
		this.monies = d;
	}
	
	public Invoice() {
	}

	@Override
	public String toString() {
		return "Invoice [monies=" + monies + "]";
	}



	public double getMonies() {
		return monies;
	}

	public void setMonies(double monies) {
		this.monies = monies;
	}
	
	
	
}
