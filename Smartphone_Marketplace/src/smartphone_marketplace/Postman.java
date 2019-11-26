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

import java.util.List;
import java.util.Map;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;



public class Postman extends Agent  {

	private List<AID> supplierAgents = new ArrayList<>();
	private AID manufacturerAID;
	private AID tickerAgent;
	private Codec codec = new SLCodec();
	private Ontology ontology = MarketplaceOntology.getInstance();
	int noOfCustomers = 3;
	private HashMap<Component, Double> transit = new HashMap<Component, Double>();
	private int orderNumber = 1;

	protected void setup() {
		//add this agent to the yellow pages
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("postman");
		sd.setName(getLocalName() + "-postman-agent");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
		//wait for the other agents to start
		manufacturerAID = new AID("Manufacturer",AID.ISLOCALNAME);

		String [] agents = { "supplier" };

		for(String a : agents)
		{

			DFAgentDescription agentDesc = new DFAgentDescription();
			ServiceDescription serviceDesc = new ServiceDescription();
			serviceDesc.setType(a);
			agentDesc.addServices(serviceDesc);
			try{
				DFAgentDescription[] agentsFound  = DFService.search(this, agentDesc); 
				for(DFAgentDescription aF : agentsFound)
					supplierAgents.add(aF.getName()); // this is the AID
			}
			catch(FIPAException e) {
				e.printStackTrace();
			}
		}

		//generateStock();
		
		addBehaviour(new TickerWaiter(this));
				
	}

	
	private class Deliver extends OneShotBehaviour{ // sends mail to manufacturer after set days in "transit"
		
		@Override
		public void action() {

			List<Component> compSent = new ArrayList<>();
			System.out.println("Out for Delivery: " + transit);
			transit.forEach((k, v) -> {	
					if(v == (double) 1) 
					{
						compSent.add(k);
						//transit.remove(k);
					}			
			});
					
			

			if(!compSent.isEmpty())				
			{
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(manufacturerAID); // sellerAID is the AID of the Seller agent
				msg.setLanguage(codec.getName());
				msg.setOntology(ontology.getName()); 
				System.out.println("Arriving today: " + compSent);
				compSent.forEach((comp) ->{
					comp.setId(0);
					
					Order order = new Order();			
					order.setCustomer(myAgent.getAID());
					order.setItem(comp);

					Action deliver = new Action();
					deliver.setAction(order);
					deliver.setActor(manufacturerAID);

					try {
						// Let JADE convert from Java objects to string
						getContentManager().fillContent(msg, deliver); //send the wrapper object
						send(msg);
					}
					catch (CodecException ce) {
						ce.printStackTrace();
					}
					catch (OntologyException oe) {
						oe.printStackTrace();
					} 

				});
				System.out.println("Delivered: " + compSent);
				compSent.forEach(c -> transit.remove(c));
				compSent.clear();
			}
			transit.forEach((k, v) -> transit.replace(k, v, v-1));
			
		}

	}



	private class PostalOrder extends CyclicBehaviour{ //adds any orders from suppliers to transit

		@Override
		public void action() {			

			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof Order) {
							Order order = (Order)action;
							Item it = order.getItem();
							if(it instanceof Component){
								Component c = (Component)it;
								c.setId(orderNumber);
								if(msg.getSender().getName().contains("Supplier 1"))
								{
									transit.put(c, (double)1);
								}
								else
								{
									transit.put(c, (double)4);									
								}
								System.out.println(transit);
								orderNumber++;
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


	public class TickerWaiter extends CyclicBehaviour { // receives new day notice

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
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();	
					
					Deliver d = new Deliver();	
					
					CyclicBehaviour po = new PostalOrder();		
					
					cyclicBehaviours.add(po);										
					dailyActivity.addSubBehaviour(d);
					dailyActivity.addSubBehaviour(po);
					
					myAgent.addBehaviour(dailyActivity);
					
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
			if(msg!=null)
			{
				ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
				tick.setContent("done");
				tick.addReceiver(tickerAgent);
				tick.addReceiver(manufacturerAID);
				myAgent.send(tick);
				//remove behaviours
				for(Behaviour b : toRemove) {
					myAgent.removeBehaviour(b);
				}
				myAgent.removeBehaviour(this);
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
