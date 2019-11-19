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
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ontology.MarketplaceOntology;
import ontology.elements.*;

import java.util.List;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;



public class CustomerAgent extends Agent  {
	private AID manufacturerAID;
	private AID tickerAgent;
	private Codec codec = new SLCodec();
	private Ontology ontology = MarketplaceOntology.getInstance();
	//stock list, with serial number as the key

	protected void setup(){
		
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		
		sd.setType("customer");
		sd.setName(getLocalName() + "-customer-agent");
		
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
		
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		manufacturerAID = new AID("Manufacturer",AID.ISLOCALNAME);	
		addBehaviour(new RequestOrder());
		addBehaviour(new TickerWaiter(this));
		//addBehaviour(new SellBehaviour());

	}
	
	/*
	public class EndDayListener extends CyclicBehaviour {
		private int buyersFinished = 0;
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
				buyersFinished++;
			}
			else {
				block();
			}
			if(buyersFinished == buyers.size()) {
				//we are finished
				ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
				tick.setContent("done");
				tick.addReceiver(tickerAgent);
				myAgent.send(tick);
				//remove behaviours
				for(Behaviour b : toRemove) {
					myAgent.removeBehaviour(b);
				}
				myAgent.removeBehaviour(this);
			}
		}
		
	}
	*/


	private class RequestOrder extends OneShotBehaviour{

		@Override
		public void action() {
			// Prepare the action request message
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(manufacturerAID); // sellerAID is the AID of the Seller agent
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName()); 



			Device device = new Device();
			//double[] components = {Math.random(),Math.random(),Math.random()};
			{
				if(Math.random() < 0.5) {
					device.setName("Phone");

					device.setComponent(new Component("Screen","5"));
					device.setComponent(new Component("Battery","2000"));
					//Screen = 5"
					//Battery - 2000mAh
				}
				else {
					device.setName("Phablet");

					device.setComponent(new Component("Screen","7"));
					device.setComponent(new Component("Battery","3000"));
					//Screen = 7"
					//Battery - 3000mAh
				}

				if(Math.random() < 0.5) {
					device.setComponent(new Component("RAM","4"));
					//RAM = 4Gb
				}
				else {
					device.setComponent(new Component("RAM","8"));
					//RAM = 8Gb
				}

				if(Math.random() < 0.5) {
					device.setComponent(new Component("Storage","64"));
					//Storage = 64Gb
				}
				else {
					device.setComponent(new Component("Storage","256"));
					//Storage = 256Gb
				}
			}
			
			double quantity = Math.floor(1 + 50 * Math.random());
			double price = Math.floor(100 + 500 * Math.random());
			double fee = quantity * Math.floor(1 + 50 * Math.random());

			OrderDetails orderDetails = new OrderDetails(device, quantity, price, fee);
			
			PlaceOrder order = new PlaceOrder();
			
			order.setCustomer(myAgent.getAID());
			order.setItem(orderDetails);

			Action requestOrder = new Action();
			requestOrder.setAction(order);
			requestOrder.setActor(manufacturerAID);
			try {
				// Let JADE convert from Java objects to string
				getContentManager().fillContent(msg, requestOrder); //send the wrapper object
				send(msg);
			}
			catch (CodecException ce) {
				ce.printStackTrace();
			}
			catch (OntologyException oe) {
				oe.printStackTrace();
			} 

		}




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
					System.out.println("new day");
					/*myAgent.addBehaviour(new BookGenerator());
					myAgent.addBehaviour(new FindBuyers(myAgent));
					CyclicBehaviour os = new OffersServer(myAgent);
					myAgent.addBehaviour(os);
					ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();
					cyclicBehaviours.add(os);
					myAgent.addBehaviour(new EndDayListener(myAgent,cyclicBehaviours));
					*/
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
}


