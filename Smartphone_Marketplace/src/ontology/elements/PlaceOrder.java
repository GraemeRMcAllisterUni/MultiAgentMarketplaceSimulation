package ontology.elements;


import jade.content.AgentAction;
import jade.core.AID;
import ontology.elements.OrderDetails;
import ontology.elements.Item;

public class PlaceOrder implements AgentAction {
	private AID customer;
	private Item item;
	
	public AID getCustomer() {
		return customer;
	}
	
	public void setCustomer(AID customer) {
		this.customer = customer;
	}
	
	public Item getItem() {
		return item;
	}
	
	public void setItem(Item item) {
		this.item = item;
	}	
	
}
