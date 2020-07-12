/**
 * 
 */
package ontology.elements;

import java.util.ArrayList;
import java.util.List;

import jade.content.onto.annotations.AggregateSlot;
import jade.content.onto.annotations.Slot;

public class PC extends Item {
		
	private String deviceType;
	
	private List<Component> components;// = new ArrayList<Component>();

	public PC(){
		super();
	}
	
	
	@Slot(mandatory = true)
	public String getName() {
		return deviceType.toString();
	}
	
	public void setName(String deviceType) {		
		this.deviceType = deviceType;
		this.components = new ArrayList<Component>();
	}

	
	public void setComponents(List<Component> comps) {
		this.components = comps;
	}
	
	public List<Component> getComponents() {
		return components;
	}
	
	public void setComponent(Component component) {
		this.components.add(component);
	}
	
	public String toString() {		
		return getName();			
	}
	
}

