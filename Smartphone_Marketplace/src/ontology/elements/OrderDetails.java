package ontology.elements;

import java.util.ArrayList;
import java.util.List;

import jade.content.Concept;
import jade.content.onto.annotations.AggregateSlot;

public class OrderDetails implements Concept{
	private double Price;
	private double Fee;
		
	private boolean Fulfilled;
		
		@AggregateSlot(cardMin = 1)
		List <Device> Devices;


	public OrderDetails(Device device, double quantity, double price, double fee) {
		this.Price = price;
		this.Fee = fee;
		this.Devices = new ArrayList<Device>();
		for(double i = 0; i<quantity; i++)
		{
			this.Devices.add(device);
		}		
	}
	
	public Device getDevice()
	{
		return Devices.get(0);
	}

}
