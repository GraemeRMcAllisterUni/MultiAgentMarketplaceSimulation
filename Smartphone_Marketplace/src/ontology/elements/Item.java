package ontology.elements;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;


public class Item implements Concept {
	
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	//public int id;
	//private double Price = 0;

//	public int getId() {
//		return id;
//	}
//
//	public void setId(int id) {
//		this.id = id;
//	}

}
