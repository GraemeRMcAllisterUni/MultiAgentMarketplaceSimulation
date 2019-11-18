package smartphone_marketplace;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ontology.MarketplaceOntology;
import ontology.elements.*;


public class ManufacturerAgent extends Agent{

	private Codec codec = new SLCodec();
	private Ontology ontology = MarketplaceOntology.getInstance();

	public void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
	}

	private class QueryBehaviour extends CyclicBehaviour{

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF); 
			ACLMessage msg = receive(mt);			
		}


	}

	private class OrderRequest extends CyclicBehaviour{
		@Override
		public void action() {
			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				try {
					ContentElement ce = null;
					System.out.println(msg.getContent());
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof Order) {							
							Order order = (Order)action;
							Device device = order.getDevice();
							
							
							
							
						}
					}
				}
				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				}


			}
		}
	}
}