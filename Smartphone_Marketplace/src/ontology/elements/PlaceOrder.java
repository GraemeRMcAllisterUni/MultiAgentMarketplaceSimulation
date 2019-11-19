package ontology.elements;


import jade.content.AgentAction;
import jade.core.AID;
import ontology.elements.OrderDetails;

public class PlaceOrder implements AgentAction {
	private AID manufacturer;
	private OrderDetails orderDetails;
	
	public AID getManufacturer() {
		return manufacturer;
	}
	
	public void setManufacturer(AID manufacturer) {
		this.manufacturer = manufacturer;
	}
	
	public OrderDetails getOrderDetails() {
		return orderDetails;
	}
	
	public void setItem(OrderDetails orderDetails) {
		this.orderDetails = orderDetails;
	}	
	
}
