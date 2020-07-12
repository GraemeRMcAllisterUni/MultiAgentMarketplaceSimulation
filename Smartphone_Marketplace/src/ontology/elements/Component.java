package ontology.elements;

import java.util.Objects;

public class Component extends Item {
	private String type;	
	private String spec;
	

	public Component(){
		super();
		setId(0);
	}
	
	public Component(String type, String spec) {
		super();
		setId(0);
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
	
	@Override
	public String toString() {	
		if(id == 0)
		return getType() + " " + getSpec();		
		else
			return getId() + " " +getType() + " " + getSpec();	
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
        boolean same =  (id == c.getId() && type.equals(c.getType()) && spec.equals(c.getSpec()));
        return same;
    }


	@Override
	public int hashCode() {
		return 17;
	}
    
     
		
}