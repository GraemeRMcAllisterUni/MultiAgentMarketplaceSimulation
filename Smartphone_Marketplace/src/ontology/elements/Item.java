package ontology.elements;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;


public class Item implements Concept {
	
	private double Quantity = 1;
	private double Price = 0;

	public double getQuantity() {
		return Quantity;
	}

	public double getPrice() {
		return Price;
	}

	public void setPrice(double price) {
		Price = price;
	}

	public void setQuantity(double quantity) {
		this.Quantity = quantity;
	}

}
