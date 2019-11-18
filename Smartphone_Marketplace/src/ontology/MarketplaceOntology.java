package ontology;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class MarketplaceOntology extends BeanOntology{
	
	private static Ontology theInstance = new MarketplaceOntology("my_ontology");
	
	public static Ontology getInstance(){
		return theInstance;
	}
	//singleton pattern
	private MarketplaceOntology(String name) {
		super(name);
		try {
			add("ontology.elements");
		} catch (BeanOntologyException e) {
			e.printStackTrace();
		}
	}
}