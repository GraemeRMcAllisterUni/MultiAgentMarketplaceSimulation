package ontology.elements;

import java.util.Objects;

public class Component extends Item {


	public Component(){
		super();
		//setId(0);
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
        boolean value = (this == c);  
        return value;
    }
    

	@Override
	public int hashCode() {
		return 34;
	}
       
    
		
}