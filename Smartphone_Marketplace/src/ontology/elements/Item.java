package ontology.elements;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;


public class Item implements Concept {
	
	public int id;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setQuantity(double quantity) {
		this.Quantity = quantity;
	}

}
