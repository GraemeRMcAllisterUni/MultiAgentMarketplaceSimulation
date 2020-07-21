package pc_marketplace;

import java.util.ArrayList;
import java.util.Arrays;

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

import Manufacturer.CPUManufacturer;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

@SuppressWarnings("serial")
public class CustomerAgent extends MarketPlaceAgent {
	private AID manufacturerAID;
	private AID tickerAgent;
	private Codec codec = new SLCodec();
	private Ontology ontology = PCOntology.getInstance();
	// stock list, with serial number as the key
	private Order dailyOrder;

	protected void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("customer");
		sd.setName(getLocalName() + "-customer-agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		manufacturerAID = new AID("Manufacturer", AID.ISLOCALNAME);
		addBehaviour(new TickerWaiter(this));
	}

	public class OrderResponse extends Behaviour {
		
		boolean messageReceived = false;

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE),
					MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.AGREE) {
					System.out.println(myAgent.getLocalName() + dailyOrder + ": offer accepted");
					messageReceived = true;
				} else if (msg.getPerformative() == ACLMessage.REFUSE) {
					System.out.println(myAgent.getLocalName() + dailyOrder + ": offer rejected");
					messageReceived = true;
				} else
					block();
			} else {
				block();
			}

		}

		@Override
		public boolean done() {
			return messageReceived;
		}
	}

	public class TickerWaiter extends CyclicBehaviour {

		// behaviour to wait for a new day
		public TickerWaiter(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("new day"),
					MessageTemplate.MatchContent("terminate"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (tickerAgent == null) {
					tickerAgent = msg.getSender();
				}
				if (msg.getContent().equals("new day")) {
					doWait(500);

					List<AID> marketPlaceAgents = Arrays.asList(manufacturerAID);// ,tickerAgent);
					ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();
					SequentialBehaviour dailyActivity = new SequentialBehaviour();

					CheckDeliveries cd = new CheckDeliveries(myAgent);
					cyclicBehaviours.add(cd);
					RequestOrder ro = new RequestOrder();
					OrderResponse or = new OrderResponse();

					myAgent.addBehaviour(cd);

					dailyActivity.addSubBehaviour(ro);
					dailyActivity.addSubBehaviour(or);
					dailyActivity.addSubBehaviour(new EndDay(myAgent, marketPlaceAgents));
					myAgent.addBehaviour(dailyActivity);
					myAgent.addBehaviour(new EndDayListener(myAgent, cyclicBehaviours));

				} else {
					// termination message to end simulation
					myAgent.doDelete();
				}
			} else {
				block();
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
			if (msg != null) {
				if (msg.getContent().equals("done")) {
					ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
					tick.setContent("done");
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

	private class CheckDeliveries extends CyclicBehaviour {

		public CheckDeliveries(Agent a) {
			super(a);
		}

		@Override
		public void action() {

			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchConversationId("delivery"));
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
							PC pc = (PC) it;
							ACLMessage payment = new ACLMessage(ACLMessage.INFORM);
							payment.addReceiver(manufacturerAID);
							payment.setConversationId("payment");
							String content = Double.toString(order.getPrice() * order.getQuantity());
							payment.setContent(content);
							send(payment);
							System.out.println(myAgent.getName()+ " Received " + pc + " and paid " + content);
						}
					}
				} catch (CodecException ce) {
					ce.printStackTrace();
				} catch (OntologyException oe) {
					oe.printStackTrace();
				}

			}
		}

	}

	private class RequestOrder extends OneShotBehaviour {

		@Override
		public void action() {
			// Prepare the action request message
			PC pc = new PC();
			if (Math.random() < 0.5) {
				pc.setCpu(new CPU(CPUManufacturer.Mintel));
				pc.setMotherboard(new Motherboard(CPUManufacturer.Mintel));
			} else {
				pc.setCpu(new CPU(CPUManufacturer.IMD));
				pc.setMotherboard(new Motherboard(CPUManufacturer.IMD));
			}

			if (Math.random() < 0.5) {
				pc.setMemory(new Memory(4));
				// RAM = 4Gb
			} else {
				pc.setMemory(new Memory(8));
				// RAM = 8Gb
			}

			if (Math.random() < 0.5) {
				pc.setHardDrive(new HardDrive(1024));
				// Storage = 64Gb
			} else {
				pc.setHardDrive(new HardDrive(2048));
				// Storage = 256Gb
			}

			double quantity = Math.floor(1 + 50 * Math.random());
			double price = Math.floor((200 + 500)* Math.random());
			double dueDate = Math.floor(1 + 10 * Math.random());
			double fee = quantity * Math.floor(1 + 50 * Math.random());

			dailyOrder = new Order(pc, quantity, price, dueDate);
			dailyOrder.setCustomer(myAgent.getAID());

			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(manufacturerAID);
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName());
			msg.setConversationId("order");

			Action requestOrder = new Action();
			requestOrder.setAction(dailyOrder);
			requestOrder.setActor(manufacturerAID);

			try {
				// Let JADE convert from Java objects to string
				getContentManager().fillContent(msg, requestOrder); // send the wrapper object
				send(msg);
			} catch (CodecException ce) {
				ce.printStackTrace();
			} catch (OntologyException oe) {
				oe.printStackTrace();
			}

			doWait(2000);
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
