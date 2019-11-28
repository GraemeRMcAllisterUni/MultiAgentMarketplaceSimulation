package ontology.elements;

import java.util.List;

import jade.core.AID;

public class OrderManifest {
	
	private OrderDetails orderDetails;	
	private AID customer;
	private List<Component> bulk1;
	private List<Component> bulk2;
	private double ExpectedProfit;
	
	
	public double getExpectedProfit() {
		return ExpectedProfit;
	}

	public void setExpectedProfit(double expectedProfit) {
		this.ExpectedProfit = expectedProfit;
	}

	public OrderManifest(){
		super();
		}
	
	public List<Component> getBulk1() {
		return bulk1;
	}

	public void setBulk1(List<Component> bulk1) {
		this.bulk1 = bulk1;
	}

	public List<Component> getBulk2() {
		return bulk2;
	}

	public void setBulk2(List<Component> bulk2) {
		this.bulk2 = bulk2;
	}

	public OrderManifest(OrderDetails orderDetails, AID customer, double expectedProfit) {
		super();
		this.orderDetails = orderDetails;
		this.customer = customer;
		this.ExpectedProfit = expectedProfit;
		}
	
	public OrderDetails getOrderDetails() {
		return orderDetails;
	}
	public void setOrderDetails(OrderDetails orderDetails) {
		this.orderDetails = orderDetails;
	}
	public AID getCustomer() {
		return customer;
	}
	public void setCustomer(AID customer) {
		this.customer = customer;
	}
	
}
