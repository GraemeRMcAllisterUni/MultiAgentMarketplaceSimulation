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


	private AID supplier1AID;
	private AID supplier2AID;
	private List <AID> supplierAgents = new ArrayList<>();
	private AID tickerAgent;
	private List<AID> customerAgents = new ArrayList<>();
	private Codec codec = new SLCodec();
	private Ontology ontology = MarketplaceOntology.getInstance();
	int noOfCustomers = 3;
	int w = 5;

//	HashMap<String, Double> stock1 = new HashMap<String, Double>();
//	HashMap<String, Double> stock2 = new HashMap<String, Double>();
	
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

//		stock1.put("5",(double)100);
//		stock1.put("7",(double)150);		
//		stock1.put("64",(double)25);
//		stock1.put("256",(double)50);		
//		stock1.put("4",(double)30);
//		stock1.put("8",(double)60);
//		stock1.put("2000",(double)70);
//		stock1.put("3000",(double)100);
//
//		stock2.put("64",(double)15);
//		stock2.put("256",(double)40);		
//		stock2.put("4",(double)20);
//		stock2.put("8",(double)35);
		

		stock1.put(new Component("Screen","5"), (double) 100);
		stock1.put(new Component("Screen","7"), (double) 150);		
		stock1.put(new Component("Storage","64"), (double) 25);
		stock1.put(new Component("Storage","256"), (double) 50);		
		stock1.put(new Component("RAM","4"), (double) 30);
		stock1.put(new Component("RAM","8"), (double) 60);
		stock1.put(new Component("Battery","2000"), (double) 70);
		stock1.put(new Component("Battery","3000"), (double) 100);
		
		System.out.println(stock1);

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
					
					dailyActivity.addSubBehaviour(new FindSuppliers());
					dailyActivity.addSubBehaviour(or);
					cyclicBehaviours.add(or);
					dailyActivity.addSubBehaviour(new EndDayListener(myAgent,cyclicBehaviours));
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

	public class FindSuppliers extends OneShotBehaviour {


		public 	void action() {
			String [] agents = { "supplier"};

			for(String a : agents)
			{

				supplier1AID = new AID("Supplier1",AID.ISLOCALNAME);
				supplier2AID = new AID("Supplier2",AID.ISLOCALNAME);	
				
				supplierAgents.add(supplier1AID);
				supplierAgents.add(supplier2AID);
				
//				DFAgentDescription agentDesc = new DFAgentDescription();
//				ServiceDescription serviceDesc = new ServiceDescription();
//				serviceDesc.setType(a);
//				agentDesc.addServices(serviceDesc);
//				try{
//					DFAgentDescription[] agentsFound  = DFService.search(myAgent,agentDesc); 
//
//					for(DFAgentDescription aF : agentsFound)
//						supplierAgents.add(aF.getName()); // this is the AID
//				}
//				catch(FIPAException e) {
//					e.printStackTrace();
//				}
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
			if(msg != null)
			{
				customersDone++;
			}
			else
			{
				block();
			}
			if(customersDone >= noOfCustomers)
			{
				ACLMessage msgSupplier = new ACLMessage(ACLMessage.INFORM);

				for(AID agent : supplierAgents) {
					msgSupplier.addReceiver(agent);
				}
				myAgent.send(msgSupplier);
				//we are finished
				ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
				tick.setContent("done");
				tick.addReceiver(tickerAgent);
				myAgent.send(tick);
				//remove behaviours
				toRemove.add(this);
				for(Behaviour b : toRemove) {
					myAgent.removeBehaviour(b);
				}
				//myAgent.removeBehaviour(this);
				System.out.println("manufacturer done");
			}

		}

		@Override
		public boolean done() {
			return customersDone >= noOfCustomers;
		}
	}
	
	private class ReceiveSupplies extends CyclicBehaviour{

		@Override
		public void action() {
			// TODO Auto-generated method stub
			
		}

	}
	

	private class OrderRequest extends Behaviour{

		double acceptableProfitMargin = 1000;
		Boolean preferLower;
		int ordersReceived = 0;
		
		OrderDetails od = new OrderDetails();
		
		
		
		void orderSupplies() {
			ACLMessage messege = new ACLMessage(ACLMessage.REQUEST); // sellerAID is the AID of the Seller agent
			messege.setLanguage(codec.getName());
			messege.setOntology(ontology.getName()); 
			for(Component c : od.getComponents())
			{
				ACLMessage msg = (ACLMessage)messege.clone();
				PlaceOrder order = new PlaceOrder();			
				order.setCustomer(myAgent.getAID());
				c.setQuantity(od.getQuantity());
				order.setItem(c);
				Action requestOrder = new Action();
				requestOrder.setAction(order);
				
				
				
				//if(supplierAgents.get(1).getLocalName().contains("1"))
					
				
				if(preferLower && stock2.containsKey(c))
				{
					
					requestOrder.setActor(supplier2AID);
				}
				else					
					requestOrder.setActor(supplier1AID);

				try {
					// Let JADE convert from Java objects to string
					getContentManager().fillContent(msg, requestOrder); //send the wrapper object
					send(msg);
					System.out.println("ordered " + c.getQuantity() +" "+ c + "(s) from " + requestOrder.getActor().getLocalName());
				}
				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				} 
			}
		}

		Boolean strategy() {

			Boolean accepted = true;															
			double price = od.getPrice();
			double fee = od.getFee();
			double quantity = od.getQuantity();
			double dueDate = od.getDueDate();
			preferLower = (dueDate>4);
			double componentsPrice = 0;
			double cP = 0;
			double expectedProfit =0;

//			for(Component c : od.getComponents())
//			{
//				cP = stock1.get(c.getSpec());								// this returns and double value in the stock list(HashMap) where the specification of component is the key, and price is the value.
//				if(preferLower && stock2.get(c.getSpec()) != null)
//					cP = stock2.get(c.getSpec());
//
//				componentsPrice += cP;
//			}
			
			System.out.println(stock1);
			for(Component comp : od.getComponents())
			{
				System.out.println(comp);
				cP = stock1.get(comp);								// this returns and double value in the stock list(HashMap) where the specification of component is the key, and price is the value.
				if(preferLower && stock2.containsKey(comp))
					cP = stock2.get(comp);
				componentsPrice += cP;				
			}
			
			double dd = dueDate;
			if(dd>4) {
				dd = 0;
			}
			else if(dd<4)
			{
				dd = 4 - dd;
			}

			
			od.setOrderPrice(price * quantity - componentsPrice);
			
			System.out.println(od.toString());
			System.out.println(od.getOrderPrice() + " total price = " + price +" * "+ quantity +" - " + componentsPrice);
			System.out.println("expected = totalPrice:"+ od.getOrderPrice() +" - " + "dd:" +dd + " * fee:" + fee);
			
			expectedProfit = od.getOrderPrice() - (dd * fee);
			
			if(expectedProfit<acceptableProfitMargin || (dueDate<4&&(4-dueDate*fee)>od.getOrderPrice()))
				accepted = false;
			
			System.out.println("Expected Profit = " + expectedProfit);						
			od.setOrderPrice(expectedProfit);
			System.out.println(accepted);			
			return accepted;
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
						if (action instanceof PlaceOrder) {
							PlaceOrder order = (PlaceOrder)action;
							Item it = order.getItem();
							if(it instanceof OrderDetails){
								od = (OrderDetails)it;	
								System.out.println("order read");
								ACLMessage orderMsg = new ACLMessage(ACLMessage.INFORM);
								if(strategy())
								{
									orderMsg.setContent("accept");									
									orderSupplies();
								}
								else
								{
									orderMsg.setContent("reject");
									
								}
								orderMsg.addReceiver(msg.getSender());
								System.out.println(msg.getSender());
								myAgent.send(orderMsg);
								ordersReceived++;
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
			if(ordersReceived == 3)
				return true;
			else	
				return false;
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
