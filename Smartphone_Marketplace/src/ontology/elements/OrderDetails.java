package ontology.elements;

import java.util.ArrayList;
import java.util.List;

import jade.content.Concept;
import jade.content.onto.annotations.AggregateSlot;
import ontology.elements.Item;

public class OrderDetails extends Item{
	
	private double Price;
	private double Fee;
	private double Quantity;	
	private boolean Fulfilled;
	private	Device device;
	private Component[] components = new Component[4];
	
	
	public void setPrice(double price) {
		
		this.Price = price;		
	}
	
	public Component[] getComponents() {
		
		return components;	
	}
	
	public void setComponents(Component[] c)
	{
//		int i=0;
//		for(Component comp : components) {i++;}
		this.components = c;
	}
	
	
	
	public double getPrice() {
		
		return Price;
	}
	
	public void setQuantity(double quantity) {
		this.Quantity = quantity;		
	}
	
	public double getFee() {
		return Fee;
	}
	
	public void setFee(double fee) {
		this.Fee = fee;	
			
	}
	
	public double getQuantity() {
		return Quantity;
	}
	
	public void setDevice(Device d)
	{
		this.device = d;
	}
	
	public Device getDevice()
	{
		return device;
	}
	

	public OrderDetails(Device d, double quantity, double price, double fee, Component[] c) {
		Price = price;
		Fee = fee;
		device = d;
		Quantity = quantity;
		components = c;
	}
	
	public OrderDetails() {
		super();	
	}
	

}
