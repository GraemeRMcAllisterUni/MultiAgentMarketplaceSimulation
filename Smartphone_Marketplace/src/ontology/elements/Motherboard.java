package ontology.elements;

import Manufacturer.CPUManufacturer;

public class Motherboard extends Component {
	
	//private String manufacturer;
	
	CPUManufacturer manufacturer;
	
	public Motherboard(){
		super();
	}
		
	public Motherboard(CPUManufacturer manufacturer){
		super();
		this.manufacturer = manufacturer;
	}

	public CPUManufacturer getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(CPUManufacturer manufacturer) {
		this.manufacturer = manufacturer;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Motherboard)) {
            return false;
        }
        Motherboard mb = (Motherboard)o;
        boolean value = (this.getId() == mb.getId() && this.getManufacturer() == mb.getManufacturer());
        return value;
    }

	@Override
	public int hashCode() {
		return 33;
	}
	
	@Override
	public String toString() {	
		return this.getManufacturer() + " " + super.toString();
	}

}