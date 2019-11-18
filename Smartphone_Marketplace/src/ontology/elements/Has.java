/**
 * 
 */
package ontology.elements;


import jade.content.Predicate;
import jade.core.AID;

public class Has implements Predicate {
	private AID owner;
	private Item item;
	
	public AID getOwner() {
		return owner;
	}
	
	public void setOwner(AID owner) {
		this.owner = owner;
	}
	
	public Item getItem() {
		return item;
	}
	
	public void setItem(Item item) {
		this.item = item;
	}
	
}
