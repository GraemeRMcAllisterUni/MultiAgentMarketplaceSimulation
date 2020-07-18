package pc_marketplace;

import java.util.ArrayList;
import java.util.Arrays;
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
import ontology.PCOntology;
import ontology.elements.*;

import java.util.List;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

@SuppressWarnings("serial")
public class Postman extends MarketPlaceAgent {

	private List<AID> supplierAgents = new ArrayList<>();
	private AID manufacturerAID;
	private AID tickerAgent;
	private Codec codec = new SLCodec();
	private Ontology ontology = PCOntology.getInstance();
	int noOfCustomers = 3;
	private HashMap<Order, Double> transit = new HashMap<Order, Double>();
	private int orderNumber = 1;

	protected void setup() {
		// add this agent to the yellow pages
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("postman");
		sd.setName(getLocalName() + "-postman-agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		// wait for the other agents to start
		manufacturerAID = new AID("Manufacturer", AID.ISLOCALNAME);


		tickerAgent = new AID("Ticker", AID.ISLOCALNAME);
		
		String[] agents = { "supplier" };

		for (String a : agents) {

			DFAgentDescription agentDesc = new DFAgentDescription();
			ServiceDescription serviceDesc = new ServiceDescription();
			serviceDesc.setType(a);
			agentDesc.addServices(serviceDesc);
			try {
				DFAgentDescription[] agentsFound = DFService.search(this, agentDesc);
				for (DFAgentDescription aF : agentsFound)
					supplierAgents.add(aF.getName()); // this is the AID
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}

		// generateStock();

		addBehaviour(new TickerWaiter(this));

	}

	public class TickerWaiter extends CyclicBehaviour { // receives new day notice

		// behaviour to wait for a new day
		public TickerWaiter(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchSender(tickerAgent),
					MessageTemplate.MatchContent("terminate"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (msg.getContent().equals("new day")) {
					System.out.println(myAgent.getLocalName() + " heard " + msg.getContent());
					transit.forEach((k, v) -> transit.replace(k, v, v - 1));
					ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();
					CyclicBehaviour po = new PostalOrder(myAgent);
					cyclicBehaviours.add(po);
					myAgent.addBehaviour(po);
					myAgent.addBehaviour(new EndDayListener(myAgent, cyclicBehaviours));
					
				} else if (msg.getContent().equals("afternoon")) {
					
					System.out.println(myAgent.getLocalName() + " heard " + msg.getContent());	
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					Deliver d = new Deliver(myAgent);
					List<AID> marketPlaceAgents = Arrays.asList(tickerAgent, manufacturerAID);
					EndDay ed = new EndDay(myAgent, marketPlaceAgents);
					dailyActivity.addSubBehaviour(d);
					dailyActivity.addSubBehaviour(ed);
					myAgent.addBehaviour(dailyActivity);
					
					
				} else {
					// termination message to end simulation
					myAgent.doDelete();
				}
			} else {
				block();
			}
		}

	}

	private class Deliver extends OneShotBehaviour { // sends mail to manufacturer after set days in "transit"

		public Deliver(Agent a) {
			super(a);
		}

		@Override
		public void action() {

			List<Order> compSent = new ArrayList<>();
			System.out.println("Out for Delivery: " + transit);
			transit.forEach((c, v) -> {
				if (v == (double) 1) {
					compSent.add(c);
					// transit.remove(k);
				}
			});
			if (!compSent.isEmpty()) {
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(manufacturerAID); // sellerAID is the AID of the Seller agent
				msg.setLanguage(codec.getName());
				msg.setOntology(ontology.getName());
				msg.setConversationId("delivery");
				compSent.forEach((comp) -> {
					Component c = (Component) comp.getItem();
					c.setId(0);
					comp.setItem(c);

					Action deliver = new Action();
					// comp.setCustomer(myAgent.getAID());
					deliver.setAction(comp);
					deliver.setActor(manufacturerAID);

					try {
						// Let JADE convert from Java objects to string
						getContentManager().fillContent(msg, deliver); // send the wrapper object
						send(msg);
					} catch (CodecException ce) {
						ce.printStackTrace();
					} catch (OntologyException oe) {
						oe.printStackTrace();
					}

				});
				System.out.println("Delivered: " + compSent);
				compSent.forEach(c -> transit.remove(c));
				compSent.clear();
			}
			

		}

	}

	private class PostalOrder extends CyclicBehaviour { 
		
		
		public PostalOrder(Agent a) {
			super(a);
		}

	// adds any orders from suppliers to transit

		@Override
		public void action() {

			// This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = receive(mt);
			if (msg != null) {
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(msg);
					if (ce instanceof Action) {
						Concept action = ((Action) ce).getAction();
						if (action instanceof Order) {
							Order order = (Order) action;
							Item it = order.getItem();
							if (it instanceof Component) {
								Order compOrder = new Order();
								Component c = (Component) it;
								c.setId(orderNumber);
								compOrder.setItem(c);
								compOrder.setQuantity(order.getQuantity());
								if (msg.getSender().getName().contains("Supplier 1")) {
									transit.put(compOrder, (double) 1);
								} else {
									transit.put(compOrder, (double) 4);
								}
								// System.out.println(transit);
								orderNumber++;
							}
						}
					}
				} catch (CodecException ce) {
					ce.printStackTrace();
				} catch (OntologyException oe) {
					oe.printStackTrace();
				}

			}
			else {
				block();
			}
		}
	}

	public class EndDayListener extends CyclicBehaviour {// listens to hear no more post orders
		private List<Behaviour> toRemove;

		private int suppliersCount = 0;

		public EndDayListener(Agent a, List<Behaviour> toRemove) {
			super(a);
			this.toRemove = toRemove;
			suppliersCount = 0;
		}

		@Override
		public void action() {

			MessageTemplate mt = MessageTemplate.MatchContent("done");
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				suppliersCount++;
				if (suppliersCount == supplierAgents.size()) {
					ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
					tick.setContent("done");
					tick.addReceiver(manufacturerAID);
					tick.addReceiver(tickerAgent);
					myAgent.send(tick);
					// remove behaviours
					for (Behaviour b : toRemove) {
						myAgent.removeBehaviour(b);
					}
					myAgent.removeBehaviour(this);
				}
			} else {
				block();
			}

		}
	}

	@Override
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

	}

}
