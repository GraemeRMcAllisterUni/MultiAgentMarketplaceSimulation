package ontology.elements;

import java.util.Objects;

public class Component extends Item {
	private String type;	
	private String spec;	
	

	public Component(){
		super();
	}
	
	public Component(String type, String spec) {
		//super();
		this.type = type;
		this.spec = spec;
	}
	

	public String getType() {
		return type;
	}
	
	public void setType(String type) {		
		this.type = type;
	}
	
	public void setSpec(String spec) {
		this.spec = spec;
	}
	
	
	public String getSpec() {
		return spec;
	}
	
	public String toString() {				
		return getType() + " " + getSpec();		
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Component)) {
            return false;
        }
        Component c = (Component)o;
        boolean same = (type.equals(c.getType())  && spec.equals(c.getSpec()));
        return same;
    }

	@Override
	public int hashCode() {
		return 17;
	}
    
    
    
    
		
}