package ontology;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class PCOntology extends BeanOntology{
	
	private static Ontology theInstance = new PCOntology("PCOntology");
	
	public static Ontology getInstance(){
		return theInstance;
	}
	//singleton pattern
	private PCOntology(String name) {
		super(name);
		try {
			add("ontology.elements");
		} catch (BeanOntologyException e) {
			e.printStackTrace();
		}
	}
}