package ontology.elements;

import java.util.List;

import ontology.elements.Item;

public class OrderDetails extends Item{
	
	private double Price;
	private double Fee;
	private double DueDate;
	private boolean Fulfilled;
	private	Device device;
	private double OrderPrice;
	
	private List<Component> components;

	public double getOrderPrice() {
		return OrderPrice;
	}


	public void setOrderPrice(double orderPrice) {
		OrderPrice = orderPrice;
	}

	
	
	public OrderDetails() {
		super();	
	}
	
	
	public void setPrice(double price) {
		
		this.Price = price;		
	}
	
	public List<Component> getComponents() {
		
		return components;	
	}
	
	public void setComponents(List<Component> c)
	{
		this.components = c;
	}
	
	
	
	public double getPrice() {
		
		return Price;
	}
	
	
	public double getFee() {
		return Fee;
	}
	
	public void setFee(double fee) {
		this.Fee = fee;	
			
	}
	
	
	public void setDevice(Device d)
	{
		this.device = d;
	}
	
	public Device getDevice()
	{
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
		return "OrderDetails [getComponents()=" + getComponents() + ", getPrice()=" + getPrice() + ", getFee()="
				+ getFee() + ", getQuantity()=" + getQuantity() + ", getDevice()=" + getDevice() + ", getDueDate()="
				+ getDueDate() + "]";
	}
	


	

}
