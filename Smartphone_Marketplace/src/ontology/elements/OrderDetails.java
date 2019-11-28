package ontology.elements;

import java.util.List;

import jade.content.onto.annotations.AggregateSlot;
import jade.content.onto.annotations.Slot;
import ontology.elements.Item;

public class OrderDetails extends Item {

	private double Price;
	private double Fee;
	private double DueDate;
	private boolean Fulfilled;
	private Device device;
	//private double OrderPrice;
	
	private List<Component> components;

	public OrderDetails() {
		super();
	}

	public void setPrice(double price) {

		this.Price = price;
	}

	@AggregateSlot(cardMin= 1)
	public List<Component> getComponents() {

		return components;
	}

	public void setComponents(List<Component> c) {
		this.components = c;
	}

	@Slot(mandatory = true)
	public double getPrice() {

		return Price;
	}

	public double getFee() {
		return Fee;
	}

	public void setFee(double fee) {
		this.Fee = fee;

	}

	public void setDevice(Device d) {
		this.device = d;
	}

	public Device getDevice() {
		return device;
	}

	public OrderDetails(Device d, double quantity, double price, double fee, double dueDate, List<Component> c) {
		Price = price;
		Fee = fee;
		device = d;
		setQuantity(quantity);
		DueDate = dueDate;
		components = c;
	}

	/**
	 * @return the dueDate
	 */
	public double getDueDate() {
		return DueDate;
	}

	/**
	 * @param dueDate the dueDate to set
	 */
	public void setDueDate(double dueDate) {
		DueDate = dueDate;
	}

	@Override
	public String toString() {
		return "OrderDetails [Price=" + Price + ", Fee=" + Fee + ", DueDate=" + DueDate + ", Fulfilled=" + Fulfilled
				+ ", device=" + device +"]"; //", OrderPrice=" + OrderPrice + 
	}

}
