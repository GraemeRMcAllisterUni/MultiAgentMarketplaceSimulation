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
		String itemString = this.getClass().getSimpleName();
		
		if(this.id != 0)
			itemString = itemString.concat(". ID: " + this.getId());

		return itemString;
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
