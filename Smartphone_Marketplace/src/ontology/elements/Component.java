package ontology.elements;

import jade.content.onto.annotations.Slot;

public class Component extends Item {
	private String type;	
	private String spec;
	
	public Component(String type, String spec) {
		setType(type);
		setSpec(spec);		
	}
	
	public Component(){
		super();
	}

	@Slot(mandatory = true)
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	@Slot(mandatory = true)
	public String getSpec() {
		return spec;
	}
	
	public void setSpec(String spec) {
		this.spec = spec;
	}
	
	public String toString() {
				
		return getType() + " " + getSpec();
	
		
	}
		
}