package ontology.elements;


import jade.content.AgentAction;
import jade.core.AID;
import ontology.elements.OrderDetails;

public class PlaceOrder implements AgentAction {
	private AID customer;
	private OrderDetails orderDetails;
	
	public AID getCustomer() {
		return customer;
	}
	
	public void setCustomer(AID customer) {
		this.customer = customer;
	}
	
	public OrderDetails getOrderDetails() {
		return orderDetails;
	}
	
	public void setItem(OrderDetails orderDetails) {
		this.orderDetails = orderDetails;
	}	
	
	/*
	public String toString()
	{		
		return this.orderDetails.toString();
	}
	*/
	
}
