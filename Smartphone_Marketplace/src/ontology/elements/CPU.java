package ontology.elements;

import Manufacturer.CPUManufacturer;

public class CPU extends Component {
	
//	public enum Manufacturer{
//		Mintel,
//		IMD
//	}
	
	CPUManufacturer manufacturer;

	
	public CPU(){
		super();
	}
	
	public CPU(CPUManufacturer manufacturer){
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
        if (!(o instanceof CPU)) {
            return false;
        }
        CPU cpu = (CPU)o;
        boolean value = (this.getId() == cpu.getId() && this.getManufacturer() == cpu.getManufacturer());
        return value;
    }
	
	
	@Override
	public int hashCode() {
		return 30;
	}
	
	@Override
	public String toString() {		
		return this.getManufacturer() + " " + super.toString();
	}
	

}
