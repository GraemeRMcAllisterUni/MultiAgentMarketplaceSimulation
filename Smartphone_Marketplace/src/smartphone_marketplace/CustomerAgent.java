package smartphone_marketplace;

import java.util.ArrayList;
import java.util.HashMap;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ontology.MarketplaceOntology;
import ontology.elements.*;


public class CustomerAgent extends Agent {
	private AID manufacturerAID;
	private Codec codec = new SLCodec();
	private Ontology ontology = MarketplaceOntology.getInstance();
	//stock list, with serial number as the key
	
	protected void setup(){
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		manufacturerAID = new AID("Manufacturer",AID.ISLOCALNAME);	
		//addBehaviour(new QueryBehaviour());
		//addBehaviour(new SellBehaviour());
		
	}
	
/*
	private class QueryBehaviour extends CyclicBehaviour{
		@Override
		public void action() {
			//This behaviour should only respond to QUERY_IF messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				try {
					ContentElement ce = null;
					System.out.println(msg.getContent()); //print out the message content in SL

					// Let JADE convert from String to Java objects
					// Output will be a ContentElement
					ce = getContentManager().extractContent(msg);
					if (ce instanceof Owns) {
						Owns owns = (Owns) ce;
						Item it = owns.getItem();
						// Extract the CD name and print it to demonstrate use of the ontology
						CD cd = (CD)it;
						System.out.println("The CD name is " + cd.getName());
						
						//check if seller has it in stock
						if(itemsForSale.containsKey(cd.getSerialNumber())) {
							System.out.println("I have the CD in stock!");
						}
						else {
							System.out.println("CD out of stock");
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
			else{
				block();
			}
		}
		
	}
	
	private class SellBehaviour extends CyclicBehaviour{
		@Override
		public void action() {
			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				try {
					ContentElement ce = null;
					System.out.println(msg.getContent()); //print out the message content in SL

					// Let JADE convert from String to Java objects
					// Output will be a ContentElement
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof Sell) {
							Sell order = (Sell)action;
							Item it = order.getItem();
							// Extract the CD name and print it to demonstrate use of the ontology
							if(it instanceof CD){
								CD cd = (CD)it;
								//check if seller has it in stock
								if(itemsForSale.containsKey(cd.getSerialNumber())) {
									System.out.println("Selling CD " + cd.getName() + " to " + order.getBuyer().getName());
								}
								else {
									System.out.println("You tried to order something out of stock!!!! Check first!");
								}

							}
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
			else{
				block();
			}
		}

	}
*/
	private class RequestOrder extends CyclicBehaviour{

		@Override
		public void action() {
			
			Device device = new Device();
			
			double[] components = {Math.random(),Math.random(),Math.random()};
			
			for(int i = 0; i<device.getComponentsNo();i++)
			{
			}
	

			if(Math.random() < 0.5) {
				device.setName("Phone");
				//Screen = 5"
				//Battery - 2000mAh
			}
			else {
				device.setName("Phablet");
				//Screen = 7"
				//Battery - 3000mAh
			}
			
			if(Math.random() < 0.5) {
				
				//RAM = 4Gb
			}
			else {
				
				//RAM = 8Gb
			}
			
			if(Math.random() < 0.5) {
				
				//Storage = 64Gb
			}
			else {
				
				//Storage = 256Gb
			}
			
			double quantity = Math.floor(1 + 50 * Math.random());
			double price = Math.floor(100 + 500 * Math.random());
			double fee = quantity * Math.floor(1 + 50 * Math.random());
			
			Order order = new Order(device, quantity, price, fee);
			
			
			
		}

		
	}
	}
}
