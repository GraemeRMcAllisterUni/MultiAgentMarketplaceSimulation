/**
 * 
 */
package ontology.elements;

import java.util.ArrayList;
import java.util.List;

import jade.content.onto.annotations.AggregateSlot;
import jade.content.onto.annotations.Slot;

public class Device extends Item {
		
	protected String deviceType;
	
	protected int noOfComponents = 3;
	
	protected List<Component> components;// = new ArrayList<Component>();

	public int getComponentsNo() {		
		return noOfComponents;
	}
	
	
	
	@Slot(mandatory = true)
	public String getName() {
		return deviceType.toString();
	}
	
	public void setName(String deviceType) {		
		this.deviceType = deviceType;
		this.components = new ArrayList<Component>();
	}
	
	@AggregateSlot(cardMin = 4)
	public List<Component> getComponents() {
		return components;
	}
	
	public void setComponents(List<Component> components) {
		this.components = components;
	}
	
	public void setComponent(Component component) {
		this.components.add(component);
	}
	
	public String toString() {
		
		return getName();	
		
	}
	
}

