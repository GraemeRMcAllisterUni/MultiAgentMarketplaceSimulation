package ontology.elements;

public class Memory extends Component {
	
	private int size;
	
	
	public Memory(){
		super();
	}
	
	public Memory(int size){
		super();
		this.size = size;
		
	}
	

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Memory)) {
            return false;
        }
        Memory mem = (Memory)o;
        boolean value = (this.getId() == mem.getId() && this.getSize() == mem.getSize());
        return value;
    }

	
	@Override
	public int hashCode() {
		return 32;
	}
	
	
	@Override
	public String toString() {		
		return this.getSize() + "Gb " + super.toString();
	}
}
