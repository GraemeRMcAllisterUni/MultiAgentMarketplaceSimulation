package pc_marketplace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import Manufacturer.CPUManufacturer;
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
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ontology.PCOntology;
import ontology.elements.*;

@SuppressWarnings("serial")
public class ManufacturerAgent extends MarketPlaceAgent {

	double acceptableProfitMargin = -100000;
	private AID supplier1AID;
	private AID supplier2AID;
	private List<AID> supplierAgents = new ArrayList<>();
	private List<AID> customerAgents = new ArrayList<>();
	private AID tickerAgent;
	// private List<AID> customerAgents = new ArrayList<>();
	private Codec codec = new SLCodec();
	private Ontology ontology = PCOntology.getInstance();

	int noOfCustomers = 3; // c
	int w = 1; // the cost of warehouse storage per-day per-component is £1
	int p = 50; // the per-day late delivery penalty is £50
	int a = 50; // maximum number of PCs that can be assembled per day is 50

	double Budget = 0;
	double Profit = 0;

	private static HashMap<Component, Double> warehouse = new HashMap<>();
	private static HashMap<Component, Double> suppler1Stock = new HashMap<>();
	private static HashMap<Component, Double> suppler2Stock = new HashMap<>();
	private static List<Order> orderBacklog = new ArrayList<Order>();
	private static HashMap<Order, Double> dailyOrders = new HashMap<>();

	public void setup() {

		Object[] args = getArguments();
		try {
			if (args.length != 0) {
				noOfCustomers = Integer.parseInt(args[0].toString());
				w = Integer.parseInt(args[1].toString());
				p = Integer.parseInt(args[2].toString());
				a = Integer.parseInt(args[3].toString());
			}
		} finally {

		}

		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("manufacturer");
		sd.setName(getLocalName() + "-manufacturer-agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		tickerAgent = new AID("Ticker", AID.ISLOCALNAME);

		suppler1Stock.put(new CPU(CPUManufacturer.Mintel), (double) 200);
		suppler1Stock.put(new CPU(CPUManufacturer.IMD), (double) 150);
		suppler1Stock.put(new Motherboard(CPUManufacturer.Mintel), (double) 125);
		suppler1Stock.put(new Motherboard(CPUManufacturer.IMD), (double) 75);
		suppler1Stock.put(new Memory(4), (double) 50);
		suppler1Stock.put(new Memory(8), (double) 90);
		suppler1Stock.put(new HardDrive(1024), (double) 50);
		suppler1Stock.put(new HardDrive(2048), (double) 75);

		suppler2Stock.put(new CPU(CPUManufacturer.Mintel), (double) 175);
		suppler2Stock.put(new CPU(CPUManufacturer.IMD), (double) 130);
		suppler2Stock.put(new Motherboard(CPUManufacturer.Mintel), (double) 115);
		suppler2Stock.put(new Motherboard(CPUManufacturer.IMD), (double) 60);
		suppler2Stock.put(new Memory(4), (double) 40);
		suppler2Stock.put(new Memory(8), (double) 80);
		suppler2Stock.put(new HardDrive(1024), (double) 45);
		suppler2Stock.put(new HardDrive(2048), (double) 65);

		addBehaviour(new TickerWaiter(this));
		addBehaviour(new FindCustomers(this));
		addBehaviour(new FindSuppliers(this));
	}

	public class TickerWaiter extends CyclicBehaviour {

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
				if (tickerAgent == null) {
					tickerAgent = msg.getSender();
				}
				if (msg.getContent().equals("new day")) {
					System.out.println(myAgent.getLocalName() + " heard " + msg.getContent());
					// spawn new sequential behaviour for day's activities
					System.out.println("Running Proft: " + Profit);
					ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();
					SequentialBehaviour dailyActivity = new SequentialBehaviour();

					DoStock ds = new DoStock(myAgent);
					OrderRequest or = new OrderRequest(myAgent);

					dailyActivity.addSubBehaviour(ds);
					cyclicBehaviours.add(or);
					dailyActivity.addSubBehaviour(or);
					dailyActivity.addSubBehaviour(new EndDayListener(myAgent, cyclicBehaviours));
					
					myAgent.addBehaviour(dailyActivity);

				} else if (msg.getContent().equals("afternoon")) {
					System.out.println(myAgent.getLocalName() + " heard " + msg.getContent());

					ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();
					SequentialBehaviour dailyActivity = new SequentialBehaviour();

					ReceiveSupplies rs = new ReceiveSupplies(myAgent);

					AssemblePC as = new AssemblePC(myAgent);
					PayFees pf = new PayFees(myAgent);

					dailyActivity.addSubBehaviour(as);
					dailyActivity.addSubBehaviour(pf);
					
					cyclicBehaviours.add(rs);
					myAgent.addBehaviour(rs);
					myAgent.addBehaviour(dailyActivity);
					dailyActivity.addSubBehaviour(new EndDayListener(myAgent, cyclicBehaviours));

					
				} else {
					// termination message to end simulation
					myAgent.doDelete();
				}
			} else {
				block();
			}
		}

	}

	public class DoStock extends OneShotBehaviour {

		public DoStock(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			for (Order o : orderBacklog)
				o.setDueDate(o.getDueDate() - 1);
		}

	}

	public class AssemblePC extends OneShotBehaviour {

		int assembled = 0;

		public AssemblePC(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			for (Order o : orderBacklog) {
				if (canBuild(o)) {
					try {

						PC pc = (PC) o.getItem();
						for (Component comp : pc.getComponents()) {
							// double stock = warehouse.get(comp);
							warehouse.replace(comp, warehouse.get(comp) - o.getQuantity());
						}

						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(o.getCustomer()); // sellerAID is the AID of the Seller agent
						msg.setLanguage(codec.getName());
						msg.setOntology(ontology.getName());

						Action requestOrder = new Action();
						requestOrder.setAction(o);
						requestOrder.setActor(myAgent.getAID());

						Profit += (o.getPrice() * o.getQuantity());

						getContentManager().fillContent(msg, requestOrder); // send the wrapper object
						send(msg);
						o.setFulfilled(true);
					} catch (CodecException ce) {
						ce.printStackTrace();
					} catch (OntologyException oe) {
						oe.printStackTrace();
					}
				}
			}
		}

		private boolean canBuild(Order o) {

			double quantity = o.getQuantity();
			PC pc = (PC) o.getItem();
			for (Component comp : pc.getComponents()) {
				if (warehouse.containsKey(comp)) {
					double stock = warehouse.get(comp);
					if (quantity > stock)
						return false;
				} else
					return false;
			}
			return true;
		}

	}

	public class PayFees extends OneShotBehaviour {

		public PayFees(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			for (Order o : orderBacklog) {
				if (o.getDueDate() < 1)
					Profit = Profit - p;
			}
			warehouse.forEach((k, v) -> Profit = Profit + (v * w));
		}

	}

	public class FindSuppliers extends OneShotBehaviour {

		public FindSuppliers(Agent a) {
			super(a);
		}

		public void action() {
			String[] agents = { "supplier" };

			for (String a : agents) {

				DFAgentDescription agentDesc = new DFAgentDescription();
				ServiceDescription serviceDesc = new ServiceDescription();
				serviceDesc.setType(a);
				agentDesc.addServices(serviceDesc);
				try {
					DFAgentDescription[] agentsFound = DFService.search(myAgent, agentDesc);
					for (DFAgentDescription aF : agentsFound)
						supplierAgents.add(aF.getName()); // this is the AID
				} catch (FIPAException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class FindCustomers extends OneShotBehaviour {

		public FindCustomers(Agent a) {
			super(a);
		}

		public void action() {
			String[] agents = { "customer" };

			for (String a : agents) {

				DFAgentDescription agentDesc = new DFAgentDescription();
				ServiceDescription serviceDesc = new ServiceDescription();
				serviceDesc.setType(a);
				agentDesc.addServices(serviceDesc);
				try {
					DFAgentDescription[] agentsFound = DFService.search(myAgent, agentDesc);
					for (DFAgentDescription aF : agentsFound)
						customerAgents.add(aF.getName()); // this is the AID
				} catch (FIPAException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class EndDayListener extends Behaviour {
		private int customersDone;
		private List<Behaviour> toRemove;

		public EndDayListener(Agent a, List<Behaviour> toRemove) {
			super(a);
			this.toRemove = toRemove;
			this.customersDone = 0;
		}

		@Override
		public void action() {

			MessageTemplate mt = MessageTemplate.MatchContent("done");
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (msg.getSender().getName().contains("Customer")) {
					customersDone++;
					if (customersDone == noOfCustomers-1) {
						ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
						supplierAgents.forEach(sa -> tick.addReceiver(sa));
						tick.setContent("done");
						myAgent.send(tick); // telling suppliers no more orders for components
					}

				} else if (msg.getSender().getName().contains("Postman")) {
					customersDone = noOfCustomers-1;
					ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
					customerAgents.forEach(sa -> tick.addReceiver(sa));
					tick.setContent("done");
					myAgent.send(tick); // telling suppliers no more orders for components
				}

				else
					block();
			}
			else {
				block();
			}
		}

		@Override
		public boolean done() {
			return customersDone >= noOfCustomers-1;
		}

		public int onEnd() {
			ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
			customersDone = 0;
			// we are finished
			tick.addReceiver(tickerAgent);
			tick.setContent("done");
			myAgent.send(tick);
			// remove behaviours
			for (Behaviour b : toRemove) {
				myAgent.removeBehaviour(b);
			}
			myAgent.removeBehaviour(this);
			System.out.println("manufacturer done");
			return 0;
		}
	}

	private class ReceiveSupplies extends CyclicBehaviour {

		public ReceiveSupplies(Agent a) {
			super(a);
		}

		@Override
		public void action() {

			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = receive(mt);
			if (msg != null) {
				if (msg.getSender().getName().contains("Postman"))
					try {
						ContentElement ce = null;
						ce = getContentManager().extractContent(msg);
						if (ce instanceof Action) {
							Concept action = ((Action) ce).getAction();
							if (action instanceof Order) {
								Order order = (Order) action;
								Item it = order.getItem();
								if (it instanceof Component) {
									Component c = new Component();
									c = (Component) it;
									double stock = order.getQuantity();
									if (warehouse.containsKey(c)) {
										stock = +warehouse.get(c);
										warehouse.replace(c, warehouse.get(c), stock);
									} else
										warehouse.put(c, stock);

									
								}
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

	private class OrderRequest extends Behaviour {

		Boolean preferLower;
		int ordersReceived = 0;

		PC pc = new PC();
		Order order = new Order();

		public OrderRequest(Agent a) {
			super(a);
		}

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
							order = (Order) action;
							Item it = order.getItem();
							if (msg.getSender().getName().contains("Customer")) {
								if (it instanceof PC) {
									pc = (PC) it;
									ACLMessage orderMsg;
									if (strategy()) {
										orderMsg = new ACLMessage(ACLMessage.AGREE);
										System.out.println("Accepted: " + order);
										orderSupplies();
										orderBacklog.add(order);
										Collections.sort(orderBacklog, new Comparator<Order>() {
											public int compare(Order c1, Order c2) {
												if (c1.getDueDate() < c2.getDueDate())
													return -1;
												if (c1.getDueDate() > c2.getDueDate())
													return 1;
												return 0;
											}
										});
									} else {
										orderMsg = new ACLMessage(ACLMessage.REFUSE);
									}
									orderMsg.addReceiver(msg.getSender());
									myAgent.send(orderMsg);
									ordersReceived++;
								}
							} else if (msg.getSender().getName().contains("Postman")) {
								if (it instanceof Component) {
									Component c = new Component();
									c = (Component) it;
									double numberofItems = order.getQuantity();
									if (warehouse.containsKey(c)) {
										double stock = warehouse.get(c);
										warehouse.replace(c, numberofItems + stock);
									} else
										warehouse.put(c, numberofItems);
									System.out.println("Manufacturer Warehouse:" + warehouse);
								}
							}
						}
					}
				} catch (CodecException ce) {
					ce.printStackTrace();
				} catch (OntologyException oe) {
					oe.printStackTrace();
				}

			}
		}

		void orderSupplies() {
			ACLMessage messege = new ACLMessage(ACLMessage.REQUEST); // sellerAID is the AID of the Seller agent
			messege.setLanguage(codec.getName());
			messege.setOntology(ontology.getName());
			for (Component c : pc.getComponents()) {
				ACLMessage msg = (ACLMessage) messege.clone();
				Order supplierOrder = new Order();
				supplierOrder.setCustomer(myAgent.getAID());
				supplierOrder.setItem(c);
				supplierOrder.setQuantity(order.getQuantity());
				Action requestOrder = new Action();
				if (preferLower && suppler2Stock.containsKey(c)) {
					msg.addReceiver(supplierAgents.get(1));
					requestOrder.setActor(supplierAgents.get(1));
					supplierOrder.setPrice(suppler2Stock.get(c));
				} else {
					requestOrder.setActor(supplierAgents.get(0));
					msg.addReceiver(supplierAgents.get(0));
					supplierOrder.setPrice(suppler1Stock.get(c));
				}
				try {
					requestOrder.setAction(supplierOrder);
					getContentManager().fillContent(msg, requestOrder);
					send(msg);
					System.out.println("ordered " + supplierOrder.getQuantity() + " " + c + "(s) from "
							+ requestOrder.getActor().getLocalName() + " for £" + supplierOrder.getPrice()
							+ " per component");
					Profit = Profit - (supplierOrder.getPrice() * supplierOrder.getQuantity());
				} catch (CodecException ce) {
					ce.printStackTrace();
				} catch (OntologyException oe) {
					oe.printStackTrace();
				}
			}
		}

		Boolean strategy() {

			// Boolean accepted = true;
			double price = order.getPrice();
			double fee = (double) p;
			double quantity = order.getQuantity();
			double dueDate = order.getDueDate();
			preferLower = (dueDate > 4);
			double componentsPrice = 0;
			double cP = 0;
			double expectedProfit = 0;
			PC pc = (PC) order.getItem();
			for (Component comp : pc.getComponents()) { // this returns and double value in the stock list(HashMap)
														// where the specification of component is the key, and price is
														// the value.
				if (preferLower && suppler2Stock.containsKey(comp))
					cP = suppler2Stock.get(comp);
				else
					cP = suppler1Stock.get(comp);

				componentsPrice = componentsPrice + (cP * quantity);
			}

			double dd = dueDate;
			if (dueDate > 4) {
				dd = 0;
			} else if (dueDate < 4) {
				dd = 4 - dueDate;
			}

			expectedProfit = (price * quantity - componentsPrice) - (dd * fee);

			System.out.println("expected:"+expectedProfit);


			if (expectedProfit < acceptableProfitMargin || (dueDate < 4 && (4 - dueDate * fee) > expectedProfit))
				return false;

			dailyOrders.put(order, expectedProfit);
			return true;
		}

		@Override
		public boolean done() {
			return ordersReceived >= noOfCustomers-1;
		}

		public int onEnd() {
			return 0;
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
