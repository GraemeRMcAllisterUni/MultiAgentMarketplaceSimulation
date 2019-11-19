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
		List <Device> devices;


	public OrderDetails(Device device, double quantity, double price, double fee) {
		Price = price;
		Fee = fee;
		devices = new ArrayList<Device>();
		for(double i = 0; i<quantity; i++)
		{
			devices.add(device);
		}		
	}
	
	public Device getDevice()
	{
		return devices.get(0);
	}
	
	/*public String toString()
	{		
		return String.valueOf(devices.size()) + getDevice() + "s"; 
	}
	*/

}
