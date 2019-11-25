package ontology.elements;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;


public class Item implements Concept {
	
	private double Quantity;
	/*
	private int orderNumber;
	@Slot (mandatory = true)
	public int getOrderNumber() {
		return orderNumber;
	}

	
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
	
	*/

	public double getQuantity() {
		return Quantity;
	}

	public void setQuantity(double quantity) {
		this.Quantity = quantity;
	}

}
