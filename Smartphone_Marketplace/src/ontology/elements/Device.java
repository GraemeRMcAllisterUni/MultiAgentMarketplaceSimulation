/**
 * 
 */
package ontology.elements;

import java.awt.Component;
import java.util.List;

import jade.content.onto.annotations.AggregateSlot;
import jade.content.onto.annotations.Slot;

public class Device extends Item {
	private String name;
	private List<Component> components;
	
	@Slot(mandatory = true)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@AggregateSlot(cardMin = 1)
	public List<Component> getComponents() {
		return components;
	}
	
	public void setComponents(List<Component> components) {
		this.components = components;
	}
	
}

