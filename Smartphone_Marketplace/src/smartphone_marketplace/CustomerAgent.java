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
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ontology.MarketplaceOntology;
import ontology.elements.*;
import smartphone_marketplace.ManufacturerAgent.EndDayListener;

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
		try{DFService.register(this, dfd);}
		catch(FIPAException e){e.printStackTrace();}
		manufacturerAID = new AID("Manufacturer",AID.ISLOCALNAME);	
		addBehaviour(new TickerWaiter(this));
	}

	public class OrderResponse extends OneShotBehaviour {

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("accept"),
					MessageTemplate.MatchContent("reject"));			
			ACLMessage msg = myAgent.receive(mt); 
			if(msg != null) {
			
				if(msg.getContent().equals("accept")) {
					System.out.println(myAgent.getLocalName() + ": offer accepted");
				}
				else if (msg.getContent().equals("reject")){
					System.out.println(myAgent.getLocalName() + ": offcer rejected");
				}
			}
			else{
				block();
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
					System.out.println("Customer heard new day");
					doWait(1000);
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					dailyActivity.addSubBehaviour(new RequestOrder());
					dailyActivity.addSubBehaviour(new OrderResponse());
					dailyActivity.addSubBehaviour(new EndDay(myAgent));
					myAgent.addBehaviour(dailyActivity);

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

	public class EndDay extends OneShotBehaviour {

		public EndDay(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			//doWait(1000);
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(tickerAgent);
			msg.setContent("done");
			myAgent.send(msg);

			//send a message to manufacturer that we have finished
			ACLMessage custDone = new ACLMessage(ACLMessage.INFORM);
			custDone.addReceiver(manufacturerAID);
			custDone.setContent("done");
			myAgent.send(custDone);
		}

	}



	private class RequestOrder extends OneShotBehaviour{

		@Override
		public void action() {
			boolean sent = false;
			// Prepare the action request message
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(manufacturerAID); // sellerAID is the AID of the Seller agent
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName()); 

			Device device = new Device();
			List<Component> c = new ArrayList<Component>();
			{
				if(Math.random() < 0.5) {
					device.setName("Phone");
					c.add(new Component("Screen","5"));
					c.add(new Component("Battery","2000"));
					//Screen = 5"
					//Battery - 2000mAh
				}
				else {
					device.setName("Phablet");
					c.add(new Component("Screen","7"));
					c.add(new Component("Battery","3000"));
					//Screen = 7"
					//Battery - 3000mAh
				}

				if(Math.random() < 0.5) {
					c.add(new Component("RAM","4"));
					//RAM = 4Gb
				}
				else {
					c.add(new Component("RAM","8"));
					//RAM = 8Gb
				}

				if(Math.random() < 0.5) {
					c.add(new Component("Storage","64"));
					//Storage = 64Gb
				}
				else {
					c.add(new Component("Storage","256"));
					//Storage = 256Gb
				}
			}

			//device.setComponents(c);

			double quantity = Math.floor(1 + 50 * Math.random());
			double price = Math.floor(100 + 500 * Math.random());
			double dueDate = Math.floor(1 + 10 * Math.random());
			double fee = quantity * Math.floor(1 + 50 * Math.random());



			OrderDetails orderDetails = new OrderDetails(device, quantity, price, fee, dueDate, c);


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
				System.out.println("order sent");
			}
			catch (CodecException ce) {
				ce.printStackTrace();
			}
			catch (OntologyException oe) {
				oe.printStackTrace();
			} 
		}

	}


@Override
protected void takeDown() {
	//Deregister from the yellow pages
	try{
		DFService.deregister(this);
	}
	catch(FIPAException e){
		e.printStackTrace();
	}

}

}


