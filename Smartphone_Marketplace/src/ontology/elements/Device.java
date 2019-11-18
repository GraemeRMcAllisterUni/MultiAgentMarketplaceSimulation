/**
 * 
 */
package ontology.elements;

import java.awt.Component;
import java.util.List;

import jade.content.onto.annotations.AggregateSlot;
import jade.content.onto.annotations.Slot;

public class Device extends Item {
		
	protected String deviceType;
	
	protected int noOfComponents = 3;
	

	public int getComponentsNo() {		
		return noOfComponents;
	}
	
	
	private List<Component> components;
	
	@Slot(mandatory = true)
	public String getName() {
		return deviceType.toString();
	}
	
	public void setName(String deviceType) {
		
		this.deviceType = deviceType;
	}
	
	@AggregateSlot(cardMin = 4)
	public List<Component> getComponents() {
		return components;
	}
	
	public void setComponents(List<Component> components) {
		this.components = components;
	}
	
}

