package smartphone_marketplace;

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

public class ManufacturerAgent extends Agent {

	double acceptableProfitMargin = 1000;
	private AID supplier1AID;
	private AID supplier2AID;
	private List<AID> supplierAgents = new ArrayList<>();
	private AID tickerAgent;
	private List<AID> customerAgents = new ArrayList<>();
	private Codec codec = new SLCodec();
	private Ontology ontology = PCOntology.getInstance();
	int noOfCustomers = 3;
	int w = 1;
	int p = 50;
	int a = 50;

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
					// spawn new sequential behaviour for day's activities

					ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();

					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					DoStock ds = new DoStock();
					AcceptOrders ao = new AcceptOrders();
					FindSuppliers fs = new FindSuppliers();
					OrderRequest or = new OrderRequest();
					ReceiveSupplies rs = new ReceiveSupplies();
					AssemblePC as = new AssemblePC();
					PayFees pf = new PayFees();
					dailyActivity.addSubBehaviour(fs);
					dailyActivity.addSubBehaviour(or);
					dailyActivity.addSubBehaviour(rs);
					dailyActivity.addSubBehaviour(ao);
					dailyActivity.addSubBehaviour(as);					
					dailyActivity.addSubBehaviour(pf);

					cyclicBehaviours.add(or);
					cyclicBehaviours.add(fs);

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

	public class DoStock extends OneShotBehaviour {

		@Override
		public void action() {
			for (Order o : orderBacklog)
				o.setDueDate(o.getDueDate() - 1);
		}

	}

	public class AssemblePC extends OneShotBehaviour {

		int assembled = 0;

		@Override
		public void action() {
			for (Order o : orderBacklog) {
				if (canBuild(o)) {

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

					try {
						// Let JADE convert from Java objects to string
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

		@Override
		public void action() {
			for(Order o : orderBacklog){
				if(o.getDueDate()<1)
					Profit = Profit - w;
			}
		}

	}

	public class FindSuppliers extends OneShotBehaviour {

		public void action() {
			supplier1AID = new AID("Supplier 1", AID.ISLOCALNAME);
			supplier2AID = new AID("Supplier 2", AID.ISLOCALNAME);
			supplierAgents.add(supplier1AID);
			supplierAgents.add(supplier2AID);
		}
	}

	public class EndDayListener extends Behaviour {
		private int customersDone;
		private List<Behaviour> toRemove;
		boolean allDelivered = false;

		public EndDayListener(Agent a, List<Behaviour> toRemove) {
			super(a);
			this.toRemove = toRemove;
			this.customersDone = 0;
		}

		@Override
		public void action() {

			MessageTemplate mt = MessageTemplate.MatchContent("done");
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null){
				if(msg.getSender().getName().contains("Customer")) {
				customersDone++;
				if(customersDone == noOfCustomers)
				{
					ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
					supplierAgents.forEach(sa -> tick.addReceiver(sa));
					tick.setContent("done"); 
					myAgent.send(tick); // telling suppliers no more orders for components
				}
				}
				else if(msg.getSender().getName().contains("Postman"))
				{
					allDelivered = true;
				}
				if(done()) {					
					ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
					//we are finished				
					tick.addReceiver(tickerAgent);
					tick.setContent("done");
					myAgent.send(tick);
					//remove behaviours
					for(Behaviour b : toRemove) {
						myAgent.removeBehaviour(b);
					}
					myAgent.removeBehaviour(this);
					System.out.println("manufacturer done");
				}
			}
			else
			{
				block();
			}
		}

		@Override
		public boolean done() {
			return customersDone >= noOfCustomers && allDelivered;
		}
	}

	private class ReceiveSupplies extends OneShotBehaviour {

		@Override
		public void action() {

			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = receive(mt);
			if (msg != null) {
				System.out.println("I don;t think we hit this. Message from " + msg.getSender().getName());
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

									System.out.println("Manufacturer Warehouse:" + warehouse);
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

								}
								System.out.println("Manufacturer Warehouse:" + warehouse);
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
					msg.addReceiver(supplier2AID);
					requestOrder.setActor(supplier2AID);
					supplierOrder.setPrice(suppler2Stock.get(c));
				} else {
					requestOrder.setActor(supplier1AID);
					msg.addReceiver(supplier1AID);
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

				componentsPrice += cP * quantity;
			}

			double dd = dueDate;
			if (dueDate > 4) {
				dd = 0;
			} else if (dueDate < 4) {
				dd = 4 - dueDate;
			}

			expectedProfit = price * quantity - componentsPrice;

//			System.out.println(expectedProfit + " total price = " + price +" * "+ quantity +" - " + componentsPrice);
//			System.out.println("expected = totalPrice:"+ expectedProfit +" - " + "Expected days fee applied:" + dd + " * fee:" + fee);

			expectedProfit = expectedProfit - dd * fee;

			if (expectedProfit < acceptableProfitMargin || (dueDate < 4 && (4 - dueDate * fee) > expectedProfit))
				return false;

			dailyOrders.put(order, expectedProfit);
			return true;
		}

		public void reset() {
			ordersReceived = 0;
			super.reset();
		}

		@Override
		public boolean done() {
			if (ordersReceived >= noOfCustomers) {
				return true;
			} else
				return false;
		}

		public int onEnd() {
			reset();
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

	private class AcceptOrders extends OneShotBehaviour {

		@Override
		public void action() {

		}

	}
}
