package smartphone_marketplace;

import java.util.ArrayList;
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


	private List<AID> supplierAgents = new ArrayList<>();
	private AID tickerAgent;
	private List<AID> customerAgents = new ArrayList<>();
	private Codec codec = new SLCodec();
	private Ontology ontology = MarketplaceOntology.getInstance();
	int noOfCustomers = 3;

	HashMap<String, Double> stock1 = new HashMap<String, Double>();
	HashMap<String, Double> stock2 = new HashMap<String, Double>();

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

		stock1.put("5",(double)100);
		stock1.put("7",(double)150);		
		stock1.put("64",(double)25);
		stock1.put("256",(double)50);		
		stock1.put("4",(double)30);
		stock1.put("8",(double)60);
		stock1.put("2000",(double)70);
		stock1.put("3000",(double)100);

		stock2.put("64",(double)15);
		stock2.put("256",(double)40);		
		stock2.put("4",(double)20);
		stock2.put("8",(double)35);

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
					CyclicBehaviour or = new OrderRequest();
					myAgent.addBehaviour(or);
					ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();
					cyclicBehaviours.add(or);
					CyclicBehaviour fa = new FindAgents();
					cyclicBehaviours.add(fa);
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

	public class FindAgents extends CyclicBehaviour {


		public 	void action() {

			String [] agents = { "customer" , "supplier"};

			for(String a : agents)
			{

				DFAgentDescription agentDesc = new DFAgentDescription();
				ServiceDescription serviceDesc = new ServiceDescription();
				serviceDesc.setType(a);
				agentDesc.addServices(serviceDesc);
				try{
					DFAgentDescription[] agentsFound  = DFService.search(myAgent,agentDesc); 

					for(DFAgentDescription aF : agentsFound)
						supplierAgents.add(aF.getName()); // this is the AID
				}
				catch(FIPAException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public class EndDayListener extends CyclicBehaviour {
		private int customersDone = 0;
		private List<Behaviour> toRemove;

		public EndDayListener(Agent a, List<Behaviour> toRemove) {
			super(a);
			this.toRemove = toRemove;
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
				for(Behaviour b : toRemove) {
					myAgent.removeBehaviour(b);
				}
				myAgent.removeBehaviour(this);
				System.out.println("manufactorer done");
			}

		}
	}

	private class OrderRequest extends CyclicBehaviour{

		double acceptableProfitMargin = 1;

		Boolean strategy(OrderDetails od) {

			List<Component> components = od.getComponents();
			Boolean accepted = true;															
			double price = od.getPrice();
			double fee = od.getFee();
			double quantity = od.getQuantity();
			double dueDate = od.getDueDate();
			Boolean preferLower = (dueDate>4);
			double componentsPrice = 0;
			double cP = 0;
			double expectedProfit =0;

			for(Component c : components)
			{
				cP = stock1.get(c.getSpec());
				if(preferLower && stock2.get(c.getSpec()) != null)
					cP = stock2.get(c.getSpec());

				System.out.println(c.toString() +" Price: " + cP);

				componentsPrice += cP;
			}

			od.totalPrice = price * quantity - componentsPrice;

			double dd = dueDate;
			if(dd>4)
				dd = 0;

			expectedProfit = od.totalPrice - (dd * fee);

			if(expectedProfit<acceptableProfitMargin || (dueDate<4&&(4-dueDate*fee)>od.totalPrice))
				accepted = false;

			System.out.println(od.toString());
			System.out.println(accepted);
			System.out.println("Expected Profit = " + expectedProfit);
			
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
								OrderDetails od = (OrderDetails)it;	
								System.out.println("order read");
								ACLMessage orderMsg = new ACLMessage(ACLMessage.INFORM);
								if(strategy(od))
								{
									orderMsg.setContent("accept");
								}
								else
								{
									orderMsg.setContent("reject");
								}
								orderMsg.addReceiver(msg.getSender());
								//myAgent.send(msg);							
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
