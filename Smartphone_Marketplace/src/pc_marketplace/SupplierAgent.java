package pc_marketplace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Manufacturer.CPUManufacturer;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.Predicate;
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
import ontology.PCOntology;
import ontology.elements.*;


@SuppressWarnings("serial")
public class SupplierAgent extends MarketPlaceAgent {

	private AID tickerAgent;
	private AID postman;
	private Codec codec = new SLCodec();
	private Ontology ontology = PCOntology.getInstance();
	
	HashMap<Component, Double> stock = new HashMap<Component, Double>();
	
	ComponentSupplier compSeller = new ComponentSupplier(this.getAID());
	Stock myStock = new Stock();

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


		if(this.getName().contains("Supplier 1"))
		{

			myStock.addPart(new CPU(CPUManufacturer.Mintel),(double)200);
			myStock.addPart(new CPU(CPUManufacturer.IMD),(double)150);		
			myStock.addPart(new Motherboard(CPUManufacturer.Mintel),(double)125);
			myStock.addPart(new Motherboard(CPUManufacturer.IMD),(double)75);		
			myStock.addPart(new Memory(4),(double)50);
			myStock.addPart(new Memory(8),(double)90);
			myStock.addPart(new HardDrive(1024),(double)50);
			myStock.addPart(new HardDrive(2048),(double)75);
			compSeller.setStock(myStock);
			compSeller.setDeliveryTime(1);
			compSeller.setSupplier(this.getAID());
			//System.out.println("Supplier 1 stock: " + stock);
		}
		else if (this.getName().contains("Supplier 2")) 
		{			
			myStock.addPart(new CPU(CPUManufacturer.Mintel),(double)175);
			myStock.addPart(new CPU(CPUManufacturer.IMD),(double)130);		
			myStock.addPart(new Motherboard(CPUManufacturer.Mintel),(double)115);
			myStock.addPart(new Motherboard(CPUManufacturer.IMD),(double)60);		
			myStock.addPart(new Memory(4),(double)40);
			myStock.addPart(new Memory(8),(double)80);
			myStock.addPart(new HardDrive(1024),(double)45);
			myStock.addPart(new HardDrive(2048),(double)65);
			compSeller.setStock(myStock);
			compSeller.setDeliveryTime(4);
			compSeller.setSupplier(this.getAID());
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
					SuppliesOrder so = new SuppliesOrder();
					StockRequest sr = new StockRequest(myAgent);
					cyclicBehaviours.add(so);
					cyclicBehaviours.add(sr);					
					myAgent.addBehaviour(so);
					myAgent.addBehaviour(sr);
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
	
	private class StockRequest extends CyclicBehaviour {

		public StockRequest(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("stocklist"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				ACLMessage stockReply = new ACLMessage(ACLMessage.INFORM);
				stockReply.addReceiver(msg.getSender());
				stockReply.setLanguage(codec.getName());
				stockReply.setOntology(ontology.getName());
				stockReply.setConversationId("stocklist");
				
				try {
					getContentManager().fillContent(stockReply, compSeller);
					send(stockReply);
				} catch (CodecException ce) {
					ce.printStackTrace();
				} catch (OntologyException oe) {
					oe.printStackTrace();
				}
			}
		}

	}

	private class SuppliesOrder extends CyclicBehaviour{



		public void action() {			

			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("suppliesorder")); //this is messege (component and quantity)
			ACLMessage msg = receive(mt);
			if(msg != null){
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof Order) {						
										
							ACLMessage postComponent = new ACLMessage(ACLMessage.REQUEST);
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



	public class EndDayListener extends CyclicBehaviour {// listens to hear no more post orders
		private List<Behaviour> toRemove;

		public EndDayListener(Agent a, List<Behaviour> toRemove) {
			super(a);
			this.toRemove = toRemove;
		}

		@Override
		public void action() {

			MessageTemplate mt = MessageTemplate.MatchContent("done");
			ACLMessage msg = myAgent.receive(mt);
			if(msg!=null) {					
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
	}

}
