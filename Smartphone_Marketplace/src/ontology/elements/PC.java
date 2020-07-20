
package ontology.elements;

//import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import jade.content.onto.annotations.AggregateSlot;
import jade.content.onto.annotations.Slot;

@SuppressWarnings("serial")
public class PC extends Item {
		
	@Override
	public String toString() {
		return cpu + " " + motherboard + " " + memory + " " + harddrive;
	}

	@Slot(mandatory = true)
	private HardDrive harddrive;
	@Slot(mandatory = true)
	private Memory memory;
	@Slot(mandatory = true)
	private Motherboard motherboard;
	@Slot(mandatory = true)
	private CPU cpu;
	

	public PC(){
		super();
	}
	
	public PC(HardDrive harddrive, Memory memory, Motherboard motherboard, CPU cpu){
		super();
		this.harddrive = harddrive;
		this.memory = memory;
		if(checkCompatiblity(motherboard,cpu)) {
			this.motherboard = motherboard;
			this.cpu = cpu;
		}
		else
			System.out.println("Invalid part types = requires same CPU and Mother board manucaturer");
	}
	
	
	public boolean checkCompatiblity(Motherboard motherboard, CPU cpu) {
		return motherboard.getManufacturer() == cpu.getManufacturer();
	}
	
	
	public HardDrive getHardDrive() {
		return harddrive;
	}

	public void setHardDrive(HardDrive harddrive) {
		this.harddrive = harddrive;
	}


	public Memory getMemory() {
		return memory;
	}


	public void setMemory(Memory memory) {
		this.memory = memory;
	}


	public Motherboard getMotherboard() {
		return motherboard;
	}


	public void setMotherboard(Motherboard motherboard) {
		this.motherboard = motherboard;
	}


	public CPU getCpu() {
		return cpu;
	}


	public void setCpu(CPU cpu) {
		this.cpu = cpu;
	}

	public List<Component> getComponents() {	
		return Arrays.asList(cpu, motherboard, memory, harddrive);
	}


	
}

