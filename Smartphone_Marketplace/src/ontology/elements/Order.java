package ontology.elements;

import java.util.ArrayList;
import java.util.List;

import jade.content.AgentAction;
import jade.content.onto.annotations.AggregateSlot;

public class Order implements AgentAction{
	private double Price;
	private double Fee;
		
	private boolean Fulfilled;
		
		@AggregateSlot(cardMin = 1)
		List <Device> Devices;


	public Order(Device device, double quantity, double price, double fee) {
		Price = price;
		Fee = fee;
		this.Devices = new ArrayList<Device>();
		for(double i = 0; i<quantity; i++)
		{
			Devices.add(device);
		}		
	}
	
	public Device getDevice()
	{
		return Devices.get(0);
	}

}
