package pc_marketplace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

	// private List<ComponentSupplier> supplierAgents = new ArrayList<>();
	private List<ComponentSupplier> supplierAgents = new ArrayList<>();
	private AID tickerAgent;
	private List<AID> customerAgents = new ArrayList<>();
	private Codec codec = new SLCodec();
	private Ontology ontology = PCOntology.getInstance();
	
	int noOfCustomers;
	int w;
	int p;
	int a;
	
	//double acceptableProfitMargin = 1000;

	double Budget = 0;
	double Profit = 0;

	private static HashMap<Component, Double> warehouse = new HashMap<>();
//	private static HashMap<Component, Double> suppler1Stock = new HashMap<>();
//	private static HashMap<Component, Double> suppler2Stock = new HashMap<>();
	private static List<Order> orderBacklog = new ArrayList<Order>();
	private static HashMap<Order, Double> dailyOrders = new HashMap<>();

	public void setup() {

		Object[] args = getArguments();
		try {
			if (args.length != 0) {
				if (args[0].toString() != null)
					noOfCustomers = Integer.parseInt(args[0].toString());
				if (args[1].toString() != null)
					w = Integer.parseInt(args[1].toString());
				if (args[2].toString() != null)
					p = Integer.parseInt(args[2].toString());
				if (args[3].toString() != null)
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
					FindSuppliers fs = new FindSuppliers(myAgent);
					OrderRequest or = new OrderRequest();
					ReceiveSupplies rs = new ReceiveSupplies();
					AssemblePC as = new AssemblePC(myAgent);
					PayFees pf = new PayFees(myAgent);
					GetPayment gp = new GetPayment(myAgent);

					myAgent.addBehaviour(gp);

					cyclicBehaviours.add(rs);
					myAgent.addBehaviour(rs);

					dailyActivity.addSubBehaviour(ds);
					dailyActivity.addSubBehaviour(fs);
					dailyActivity.addSubBehaviour(or);
					dailyActivity.addSubBehaviour(as);
					dailyActivity.addSubBehaviour(pf);
					myAgent.addBehaviour(dailyActivity);
					myAgent.addBehaviour(new EndDayListener(myAgent, cyclicBehaviours));

				} else {
					System.out.println("\n\n\n\n\n\n\n\n\nProfit on final day:£" + Profit);
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
			for (Order o : orderBacklog) {
				if(o.getDueDate()!=1)
					o.setDueDate(o.getDueDate() - 1);
				if(o.getDueDate()<1)
					o.setDueDate(1);
			}
			if (warehouse != null) {
				// warehouse.values().removeIf(v -> v == 0);
				warehouse.values().removeIf(f -> f == 0);
				System.out.println("Manufacturer Warehouse:" + warehouse);
			}
			for (int i = 0; i < orderBacklog.size(); i++)
				if (orderBacklog.get(i).isFulfilled())
					orderBacklog.remove(i);
		}

	}

	public class GetPayment extends CyclicBehaviour {

		Order completedOrder;
		boolean paid;

		public GetPayment(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchConversationId("payment"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				try {
					double payment = Double.parseDouble(msg.getContent());
					Profit = Profit + payment;
					System.out.println("Manufacturer received payment of " + payment + " from "
							+ msg.getSender().getLocalName() + " and profit now £" + Profit);
				} catch (NumberFormatException ne) {
					ne.printStackTrace();
				}
			} else
				block();

		}

	}

	public class AssemblePC extends OneShotBehaviour {

		int assembled = 0;
		int step = 0;

		public AssemblePC(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			switch (step) {
			case 0:
				for (Order o : orderBacklog) {
					if (canBuild(o) && o.getQuantity() + assembled <= a) {
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

						assembled = assembled + (int) o.getQuantity();
//						GetPayment gp = new GetPayment(myAgent, o);
//						myAgent.addBehaviour(gp);
						msg.setConversationId("delivery");
						try {
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
				break;
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
				if (o.getDueDate() < 1 && !o.isFulfilled()) {
					Profit = Profit - p;
					System.out.println("Paid £" + p + " in late fees");
				}
			}
			double runningFee = Profit;
			warehouse.forEach((k, v) -> {
				Profit = Profit - (v * w);
			});
			runningFee = runningFee - Profit;
			if (runningFee != 0)
				System.out.println("Paid £" + runningFee + " in storage fees");

			System.out.println("Running profit = £" + Profit);
		}
	}

	public class FindSuppliers extends Behaviour {

		int step = 0;
		int suppliersFound = 0;

		public FindSuppliers(Agent a) {
			super(a);
			step = 0;
		}

		public void action() {
			switch (step) {
			case 0:

				String[] agents = { "supplier" };

				if (supplierAgents != null)
					supplierAgents.clear();

				for (String a : agents) {

					DFAgentDescription agentDesc = new DFAgentDescription();
					ServiceDescription serviceDesc = new ServiceDescription();
					serviceDesc.setType(a);
					agentDesc.addServices(serviceDesc);
					try {
						ACLMessage stock = new ACLMessage(ACLMessage.REQUEST);
						stock.setConversationId("stocklist");
						DFAgentDescription[] agentsFound = DFService.search(myAgent, agentDesc);
						suppliersFound = agentsFound.length;
						for (DFAgentDescription aF : agentsFound) {
							// supplierAgents.add(new ComponentSupplier(aF.getName())); // this is the AID
							stock.addReceiver(aF.getName());
						}
						myAgent.send(stock);

					} catch (FIPAException e) {
						e.printStackTrace();
					}
				}
				step++;
				break;
			case 1:
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId("stocklist"));
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					try {
						ContentElement ce = null;
						ce = getContentManager().extractContent(msg);
						if (ce instanceof ComponentSupplier) {
							ComponentSupplier seller = (ComponentSupplier) ce;
							//System.out.println(seller);
							supplierAgents.add(seller);
						}

					} catch (CodecException e) {
						e.printStackTrace();
					} catch (OntologyException e) {
						e.printStackTrace();
					}
					suppliersFound--;
				}
				if (suppliersFound == 0)
					step++;
				break;
			}

		}

		@Override
		public boolean done() {
			return step == 2;
		}
	}

	public class EndDayListener extends CyclicBehaviour {
		private int customersDone = 0;
		private List<Behaviour> toRemove;
		boolean allDelivered = false;

		public EndDayListener(Agent a, List<Behaviour> toRemove) {
			super(a);
			this.toRemove = toRemove;
			this.customersDone = 0;
			allDelivered = false;
		}

		@Override
		public void action() {

			MessageTemplate mt = MessageTemplate.MatchContent("done");
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (msg.getSender().getName().contains("Customer")) {
					customersDone++;
					if (customersDone == noOfCustomers) {
						ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
						supplierAgents.forEach(sa -> tick.addReceiver(sa.getSupplier()));
						tick.setContent("done");
						myAgent.send(tick); // telling suppliers no more orders for components
					}
				} else if (msg.getSender().getName().contains("Postman")) {
					allDelivered = true;
				}
				if (customersDone == noOfCustomers && allDelivered) {
					ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
					// we are finished
					customerAgents.forEach(ca -> tick.addReceiver(ca));
					tick.addReceiver(tickerAgent);
					tick.setContent("done");
					myAgent.send(tick);
					// remove behaviours
					for (Behaviour b : toRemove) {
						myAgent.removeBehaviour(b);
					}
					myAgent.removeBehaviour(this);
					customersDone = 0;
					allDelivered = false;
				}
			} else {
				block();
			}
		}
	}

	private class ReceiveSupplies extends CyclicBehaviour {

		@Override
		public void action() {

			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("delivery"));
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
										stock = stock + warehouse.get(c);
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

		int ordersReceived = 0;

		PC pc = new PC();
		Order order = new Order();
		

		@Override
		public void action() {

			// This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("order"));
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
									if (!customerAgents.contains(msg.getSender()))
										customerAgents.add(msg.getSender());
									pc = (PC) it;
									ACLMessage orderMsg;
									if (strategy()) {
										orderMsg = new ACLMessage(ACLMessage.AGREE);
										orderSupplies();
										orderBacklog.add(order);
										Collections.sort(orderBacklog, new Comparator<Order>() {
											public int compare(Order c1, Order c2) {
												double c1Fee = ((p/(c1.getDueDate()))+(w*c1.getQuantity()));
												double c2Fee = ((p/(c2.getDueDate()))+(w*c2.getQuantity()));
												if (c1Fee < c2Fee)
													return 1;
												if (c1Fee> c2Fee)
													return -1;
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

				double bestPrice = Double.MAX_VALUE;
				int count = 0;

				for (int i = 0; i < supplierAgents.size(); i++) {
					if (supplierAgents.get(i).getStock().getComponentPrice(c) < bestPrice
							&& supplierAgents.get(i).getDeliveryTime() < order.getDueDate()) {
						count = i;
						bestPrice = supplierAgents.get(i).getStock().getComponentPrice(c);
					}
				}

				msg.addReceiver(supplierAgents.get(count).getSupplier());
				requestOrder.setActor(supplierAgents.get(count).getSupplier());
				supplierOrder.setPrice(supplierAgents.get(count).getStock().getComponentPrice(c));
				msg.setConversationId("suppliesorder");

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
			
			double lateFeeBias = 0.2;
			double warehouseFeeBias = 0.65;
			double dailyQuotaBias = 2;

			// Boolean accepted = true;
			double price = order.getPrice();
			double quantity = order.getQuantity();
			double dueDate = order.getDueDate();
			double componentsPrice = 0;
			double cP;
			double expectedProfit = 0;
			PC pc = (PC) order.getItem();
			// for (Component comp : pc.getComponents()) // this returns and double value in
			// the stock list(HashMap)

			double bestPrice = Double.MAX_VALUE;
			int count = 0;

			for (int i = 0; i < supplierAgents.size(); i++) {
				double quote = supplierAgents.get(i).quote(pc);
				if (quote < bestPrice && supplierAgents.get(i).getDeliveryTime() < dueDate) {
					count = i;
					bestPrice = quote;
				}
			}
			
			double dailyQuota = (( quantity/a ) * noOfCustomers);
			
			if(dailyQuota>noOfCustomers)
				return false;
			
			double riskFactor = (( noOfCustomers * ( p * lateFeeBias ) ) + ( noOfCustomers * ( (1 + (w * (quantity * pc.getComponents().size() ))) * warehouseFeeBias ))) * (dailyQuota * dailyQuotaBias);
				
			//acceptableProfitMargin = acceptableProfitMargin + (riskFactor*dailyQuota);

			componentsPrice = supplierAgents.get(count).quote(pc) * quantity;

			expectedProfit = (price * quantity) - componentsPrice;
			

//			System.out.println(expectedProfit + " total price = " + price +" * "+ quantity +" - " + componentsPrice);
//			System.out.println("expected = totalPrice:"+ expectedProfit +" - " + "Expected days fee applied:" + dd + " * fee:" + fee);

			// expectedProfit = expectedProfit - (dd * fee);

			if (expectedProfit < riskFactor)
				return false;

			dailyOrders.put(order, expectedProfit);
			return true;
		}

		@Override
		public boolean done() {
			return ordersReceived >= noOfCustomers;
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
