package ontology.elements;

import java.util.ArrayList;
import java.util.List;

import jade.content.Concept;
import jade.content.Predicate;
import jade.core.AID;

@SuppressWarnings("serial")
public class ComponentSupplier implements Predicate {


	private AID supplier;

	private Stock stock = new Stock();

	private double deliveryTime;
	
	
	@Override
	public String toString() {
		return "ComponentSupplier [supplier=" + supplier + ", stock=" + stock + ", deliveryTime=" + deliveryTime + "]";
	}

	public ComponentSupplier() {
		super();
	}

	public ComponentSupplier(AID name) {
		this.supplier = name;
	}

	public AID getSupplier() {
		return supplier;
	}

	public void setSupplier(AID supplier) {
		this.supplier = supplier;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public double getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(double deliveryTime) {
		this.deliveryTime = deliveryTime;
	}
	
	public double quote(PC pc) {
		double quote = 0;
		for(Component c:pc.getComponents())
		{
			quote = quote + stock.getComponentPrice(c);
		}
		return quote;
	}
	

}
