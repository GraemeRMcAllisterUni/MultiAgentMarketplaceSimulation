package smartphone_marketplace;

import java.util.ArrayList;
import java.util.Collections;
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
import ontology.MarketplaceOntology;
import ontology.elements.*;

public class ManufacturerAgent extends Agent{

	double acceptableProfitMargin = 1000;
	private AID supplier1AID;
	private AID supplier2AID;
	private List <AID> supplierAgents = new ArrayList<>();
	private AID tickerAgent;
	private List<AID> customerAgents = new ArrayList<>();
	private Codec codec = new SLCodec();
	private Ontology ontology = MarketplaceOntology.getInstance();
	int noOfCustomers = 3;
	int w = 5;
	private List<OrderManifest> orderCatalogue= new ArrayList<>();
	private List<OrderManifest> dailyOrders= new ArrayList<>();

	double Budget = 0;
	double Profit = 0;

	private static HashMap<Component, Double> warehouse = new HashMap<>();
	private static HashMap<Component, Double> stock1 = new HashMap<>();
	private static HashMap<Component, Double> stock2 = new HashMap<>();


	public void setup() {

		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("manufacturer");
		sd.setName(getLocalName() + "-manufacturer-agent");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();

		}

		stock1.put(new Component("Screen","5"), (double) 100);
		stock1.put(new Component("Screen","7"), (double) 150);		
		stock1.put(new Component("Storage","64"), (double) 25);
		stock1.put(new Component("Storage","256"), (double) 50);		
		stock1.put(new Component("RAM","4"), (double) 30);
		stock1.put(new Component("RAM","8"), (double) 60);
		stock1.put(new Component("Battery","2000"), (double) 70);
		stock1.put(new Component("Battery","3000"), (double) 100);

		stock2.put(new Component("Storage","64"),(double)15);
		stock2.put(new Component("Storage","256"),(double)40);		
		stock2.put(new Component("RAM","4"),(double)20);
		stock2.put(new Component("RAM","8"),(double)35);

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
					//spawn new sequential behaviour for day's activities

					ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();

					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					OrderRequest or = new OrderRequest();
					AcceptOrders ao = new AcceptOrders();
					OrderSupplies os = new OrderSupplies();
					ReceiveSupplies rs = new ReceiveSupplies();
					AssemblePhones as = new AssemblePhones();
					PayFees pf = new PayFees();
					dailyActivity.addSubBehaviour(or);
					dailyActivity.addSubBehaviour(rs);					
					cyclicBehaviours.add(or);


					myAgent.addBehaviour(dailyActivity);
					myAgent.addBehaviour(new EndDayListener(myAgent,cyclicBehaviours));

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

	public class AcceptOrders extends OneShotBehaviour{

		double bestProfit = 0;
		@Override
		public void action() {
			for(OrderManifest om : dailyOrders){
				if(om.getExpectedProfit()>bestProfit)
					bestProfit = om.getExpectedProfit();
			}
			double runningProfit;
			for(OrderManifest om : dailyOrders){
				if(om.getExpectedProfit()==bestProfit)
					if(om.getExpectedProfit()<dailyOrders.forEach((order) -> ))
					bestProfit = om.getExpectedProfit();
			}

		}

	}

	public class AssemblePhones extends OneShotBehaviour{

		@Override
		public void action() {

		}

	}	

	public class PayFees extends OneShotBehaviour{

		@Override
		public void action() {
			warehouse.forEach((k, v) -> warehouse.replace(k, v, v-1));

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
			if(msg != null)
			{
				customersDone++;
				if(customersDone == noOfCustomers)
				{
					ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
					supplierAgents.forEach(sa -> tick.addReceiver(sa));
					tick.setContent("done"); 
					myAgent.send(tick); // telling suppliers no more orders for components
				}
				else if(customersDone >= noOfCustomers + 1)
				{
					System.out.println("");
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
			return customersDone >= noOfCustomers + 1;
		}
	}

	private class ReceiveSupplies extends OneShotBehaviour{

		@Override
		public void action() {

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
								Component c = new Component();
								c = (Component)it;

								double stock = c.getQuantity();
								if(warehouse.containsKey(c))
								{								
									stock =+ warehouse.get(c);
									warehouse.replace(c, warehouse.get(c), stock);
								}
								else
									warehouse.put(c, stock);

								System.out.println("Manufacturer Warehouse:" + warehouse);


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

	private class OrderSupplies extends OneShotBehaviour{

		@Override
		public void action() {
			ACLMessage messege = new ACLMessage(ACLMessage.REQUEST); // sellerAID is the AID of the Seller agent
			messege.setLanguage(codec.getName());
			messege.setOntology(ontology.getName()); 
			for(OrderManifest om : dailyOrders)
			{
				for(Component c : od.getComponents())
				{
					Boolean preferLower = (dueDate>4);
					ACLMessage msg = (ACLMessage)messege.clone();
					Order order = new Order();			
					order.setCustomer(myAgent.getAID());
					c.setQuantity(od.getQuantity());
					order.setItem(c);
					Action requestOrder = new Action();
					requestOrder.setAction(order);


					if(preferLower && stock2.containsKey(c))
					{
						msg.addReceiver(supplier2AID);
						requestOrder.setActor(supplier2AID);
						c.setPrice(stock2.get(c));

					}
					else				
					{
						requestOrder.setActor(supplier1AID);
						msg.addReceiver(supplier1AID);
						c.setPrice(stock1.get(c));
					}
					try {
						// Let JADE convert from Java objects to string
						getContentManager().fillContent(msg, requestOrder); //send the wrapper object
						send(msg);
						System.out.println("ordered " + c.getQuantity() +" "+ c + "(s) from " + requestOrder.getActor().getLocalName() + " for £" + c.getPrice() + " per component");
						Profit = Profit - c.getPrice();
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




	}


	private class OrderRequest extends Behaviour{


		int ordersReceived = 0;
		AID customer;

		OrderDetails od = new OrderDetails();			

		double expectedProfit() {

			Boolean accepted = true;															
			double price = od.getPrice();
			double fee = od.getFee();
			double quantity = od.getQuantity();
			double dueDate = od.getDueDate();
			Boolean preferLower = (dueDate>4);
			double componentsPrice = 0;
			double cP = 0;
			double expectedProfit = 0;

			for(Component comp : od.getComponents())
			{
				System.out.println(comp);
				cP = stock1.get(comp);								// this returns and double value in the stock list(HashMap) where the specification of component is the key, and price is the value.
				if(preferLower && stock2.containsKey(comp))
					cP = stock2.get(comp);
				componentsPrice += cP;				
			}

			double feecounter = dueDate;
			if(feecounter>=4) 
				feecounter = 0;
			else if(feecounter<4)
				feecounter = 4 - feecounter;

			expectedProfit = price * quantity - componentsPrice;

			System.out.println(od.toString());
			System.out.println(expectedProfit + " total price = " + price +" * "+ quantity +" - " + componentsPrice);
			System.out.println("expected = totalPrice:"+ expectedProfit +" - " + "dd:" + feecounter + " * fee:" + fee);

			expectedProfit = expectedProfit - feecounter * fee;



			System.out.println("Expected Profit = " + expectedProfit);
			System.out.println(accepted);
			return expectedProfit;
		}

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
							if(msg.getSender().getName().contains("Customer"))
							{
								if(it instanceof OrderDetails){
									od = (OrderDetails)it;	
									System.out.println("order read");
									ACLMessage orderMsg;
									if(expectedProfit()>=acceptableProfitMargin)
									{
										
										customer = msg.getSender();
										orderMsg = new ACLMessage(ACLMessage.AGREE);
										OrderManifest om = new OrderManifest(od, customer,expectedProfit());
										dailyOrders.add(om);
									}
									else
									{
										orderMsg = new ACLMessage(ACLMessage.REFUSE);
									}
									orderMsg.addReceiver(msg.getSender());
									System.out.println(msg.getSender());
									myAgent.send(orderMsg);
									ordersReceived++;
								}
							}
							else if(msg.getSender().getName().contains("Postman"))
							{
								if(it instanceof Component){
									Component c = new Component();
									c = (Component)it;
									double stock = c.getQuantity();
									if(warehouse.containsKey(c))
									{								
										stock =  stock + warehouse.get(c);
										warehouse.replace(c, warehouse.get(c), stock);
									}
									else
										warehouse.put(c, stock);

									System.out.println("Manufacturer Warehouse:" + warehouse);
								}
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

		public void reset() {
			ordersReceived = 0;
			super.reset();
		}



		@Override
		public boolean done() {
			if(ordersReceived >= noOfCustomers)
			{
				completeOrder();
				return true;
			}
			else	
				return false;
		}

		void completeOrder() {


		}

		public int onEnd(){		
			reset();
			return 0;

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
