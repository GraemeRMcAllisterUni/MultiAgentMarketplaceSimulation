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

	private HashMap<Component, Double> transit = new HashMap<Component, Double>();
	HashMap<String, Double> stock1 = new HashMap<String, Double>();
	HashMap<String, Double> stock2 = new HashMap<String, Double>();

	private List<AID> supplierAgents = new ArrayList<>();
	private AID manufacturerAID;
	private AID tickerAgent;
	private Codec codec = new SLCodec();
	private Ontology ontology = MarketplaceOntology.getInstance();
	int noOfCustomers = 3;

	protected void setup() {
		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("postman-agent");
		sd.setName(getLocalName() + "-ticker-agent");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
		//wait for the other agents to start
		manufacturerAID = new AID("Manufacturer",AID.ISLOCALNAME);	

		String [] agents = {"supplier"};

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
	}

	private class deliver extends OneShotBehaviour{


		public deliver(Agent myAgent) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(manufacturerAID); // sellerAID is the AID of the Seller agent
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName()); 

			List<Component> compSent = new ArrayList<>();
			for(Map.Entry<Component, Double> entry : transit.entrySet())
			{
				if(entry.getValue()==1)
				{
					compSent.add(entry.getKey());
					((HashMap<Component,Double>) entry).remove(entry.getKey());
				}
			}

			compSent.forEach((comp) ->{
				PlaceOrder order = new PlaceOrder();			
				order.setCustomer(myAgent.getAID());
				order.setItem(comp);

				Action requestOrder = new Action();
				requestOrder.setAction(order);
				requestOrder.setActor(manufacturerAID);

				try {
					// Let JADE convert from Java objects to string
					getContentManager().fillContent(msg, requestOrder); //send the wrapper object
					send(msg);

				}
				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				} 

			});


		}
	}



private class PostalOrder extends OneShotBehaviour{


	public PostalOrder(Agent a) {
		super(a);
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
						if(it instanceof Component){
							Component c = (Component)it;

							if(msg.getSender().getName().contains("1"))
							{
								transit.put(c, (double)1);
							}
							else
							{
								transit.put(c, (double)4);
							}


							System.out.println("order read");
							ACLMessage orderMsg = new ACLMessage(ACLMessage.INFORM);
							orderMsg.addReceiver(msg.getSender());
							myAgent.send(orderMsg);							
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
				SequentialBehaviour dailyActivity = new SequentialBehaviour();
				//sub-behaviours will execute in the order they are added
				dailyActivity.addSubBehaviour(new PostalOrder(myAgent));
				dailyActivity.addSubBehaviour(new deliver(myAgent));
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


		ACLMessage msgSupplier = new ACLMessage(ACLMessage.INFORM);

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
