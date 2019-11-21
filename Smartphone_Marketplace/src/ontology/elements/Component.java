package ontology.elements;


public class Component extends Item {
	private String type;	
	private String spec;	

	public Component(){
		super();
	}
	
	public Component(String type, String spec) {
		setType(type);
		setSpec(spec);		
	}
	

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
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