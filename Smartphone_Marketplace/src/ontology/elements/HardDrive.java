package ontology.elements;

public class HardDrive extends Component {
	
	private int capacity;//in Gb
	
	public HardDrive(){
		super();
	}
	
	public HardDrive(int capacity){
		super();
		this.capacity = capacity;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HardDrive)) {
            return false;
        }
        HardDrive hdd = (HardDrive)o;
        boolean value = (this.getId() == hdd.getId() && this.getCapacity() == hdd.getCapacity());
        return value;
    }

	@Override
	public int hashCode() {
		return 31;
	}
	
	@Override
	public String toString() {		
		return (this.getCapacity()/1024) + "Tb " + super.toString();
	}
	
}
