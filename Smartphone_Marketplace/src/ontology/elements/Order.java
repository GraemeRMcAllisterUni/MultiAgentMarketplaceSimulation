package ontology.elements;


import jade.content.AgentAction;
import jade.core.AID;

public class Order implements AgentAction {
	
	@Override
	public String toString() {
		return "[item=" + item + ", quantity=" + quantity +"]"; // + ", itemPrice=" + itemPrice + ", duedate=" + duedate + ", fulfilled=" + fulfilled + "]";
	}

	private AID customer;
	private Item item;
	private double quantity;// = 1;
	private double itemPrice;
	private double duedate;
	private boolean fulfilled;
	
	public Order() {
		super();
	}

	
	public Order(Item item, double quantity, double price, double duedate) {
		this.item = item;
		this.quantity = quantity;
		this.itemPrice = price;
		this.duedate = duedate;
	}

	public AID getCustomer() {
		return customer;
	}
	

	public double getDueDate() {
		return duedate;
	}

	public void setDueDate(double dueDate) {
		this.duedate = dueDate;
	}

	public boolean isFulfilled() {
		return fulfilled;
	}

	public void setFulfilled(boolean fulfilled) {
		this.fulfilled = fulfilled;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
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

	public double getPrice() {
		return itemPrice;
	}

	public void setPrice(double price) {
		this.itemPrice = price;
	}
	
	
	
	
	
}
