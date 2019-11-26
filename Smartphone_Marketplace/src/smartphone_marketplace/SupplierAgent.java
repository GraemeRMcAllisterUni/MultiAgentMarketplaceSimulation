package smartphone_marketplace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ontology.MarketplaceOntology;
import ontology.elements.*;


public class SupplierAgent extends Agent {

	private AID tickerAgent;
	private AID postman;
	private Codec codec = new SLCodec();
	private Ontology ontology = MarketplaceOntology.getInstance();

	protected void setup(){

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("supplier");
		sd.setName(getLocalName() + "-supplier-agent");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}

		postman = new AID("Postman",AID.ISLOCALNAME);

		///Component c = new Componenet();	

		HashMap<Component, Double> stock = new HashMap<Component, Double>();


		System.out.println(this.getName());

		if(this.getName().contains("Supplier 1"))
		{
			stock.put(new Component("Screen","5"),(double)100);
			stock.put(new Component("Screen","7"),(double)150);		
			stock.put(new Component("Storage","64"),(double)25);
			stock.put(new Component("Storage","256"),(double)50);		
			stock.put(new Component("RAM","4"),(double)30);
			stock.put(new Component("RAM","8"),(double)60);
			stock.put(new Component("Battery","2000"),(double)70);
			stock.put(new Component("Battery","3000"),(double)100);
			//System.out.println("Supplier 1 stock: " + stock);
		}
		else if (this.getName().contains("Supplier 2")) 
		{

			stock.put(new Component("Storage","64"),(double)15);
			stock.put(new Component("Storage","256"),(double)40);		
			stock.put(new Component("RAM","4"),(double)20);
			stock.put(new Component("RAM","8"),(double)35);
			//System.out.println("Supplier 2 stock: " + stock);
		}
		else
			System.out.println("Invalid Supplier");

		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		addBehaviour(new TickerWaiter(this));
	}

	public class TickerWaiter extends CyclicBehaviour {

		//behaviour to wait for a new day
		public TickerWaiter(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("new day"),
					MessageTemplate.MatchContent("terminate"));
			ACLMessage msg = myAgent.receive(mt); 
			if(msg != null) {
				if(tickerAgent == null) {
					tickerAgent = msg.getSender();
				}
				if(msg.getContent().equals("new day")) {
					ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();
					OrderRequest or = new OrderRequest();
					cyclicBehaviours.add(or);
					myAgent.addBehaviour(or);
					myAgent.addBehaviour(new EndDayListener(myAgent, cyclicBehaviours));
				}
				else {
					//termination message to end simulation
					myAgent.doDelete();
				}
			}
			else{
				block();
			}
		}

	}

	private class OrderRequest extends CyclicBehaviour{



		public void action() {			

			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); //this is messege (component and quantity)
			ACLMessage msg = receive(mt);
			if(msg != null){
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof Order) {						
							
							
							ACLMessage postComponent = new ACLMessage(ACLMessage.REQUEST); // sellerAID is the AID of the Seller agent
							postComponent.addReceiver(postman);
							postComponent.setLanguage(codec.getName());
							postComponent.setOntology(ontology.getName()); 
							
							Order order = new Order();
							order = (Order)action;
							order.setCustomer(myAgent.getAID());
							order.setItem(order.getItem());
							
							Action requestOrder = new Action();							
							requestOrder.setAction(order);
							requestOrder.setActor(postman);
							
							try {
								// Let JADE convert from Java objects to string
								getContentManager().fillContent(postComponent, requestOrder); //send the wrapper object
								send(postComponent);
							}
							catch (CodecException cex) {
								cex.printStackTrace();
							}
							catch (OntologyException oe) {
								oe.printStackTrace();
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
		}


	}



	public class EndDayListener extends CyclicBehaviour {
		private List<Behaviour> toRemove;

		public EndDayListener(Agent a, List<Behaviour> toRemove) {
			super(a);
			this.toRemove = toRemove;
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchContent("done");
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null) {
				if(msg.getContent().equals("done")) {
					ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
					tick.setContent("done");
					tick.addReceiver(tickerAgent);
					tick.addReceiver(postman);
					myAgent.send(tick);
					//remove behaviours
					for(Behaviour b : toRemove) {
						myAgent.removeBehaviour(b);
					}
					myAgent.removeBehaviour(this);
				}				
			}
			else {
				block();
			}

		}
	}

}
