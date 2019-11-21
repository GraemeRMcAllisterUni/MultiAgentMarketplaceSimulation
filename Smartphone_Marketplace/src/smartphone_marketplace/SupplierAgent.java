package smartphone_marketplace;

import java.util.HashMap;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ontology.MarketplaceOntology;
import ontology.elements.*;


public class SupplierAgent extends Agent {

	private AID tickerAgent;
	private Codec codec = new SLCodec();
	private Ontology ontology = MarketplaceOntology.getInstance();

	protected void setup(){
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("supplier");
		///Component c = new Componenet();	
		
		HashMap<Component, Double> stock = new HashMap<Component, Double>();
		
		

		System.out.println(this.getName());
		

		if(this.getName().contains("Supplier 1"))
		{
			stock.put(new Component("Screen","5"),(double)100);
			stock.put(new Component("Screen","7"),(double)150);		
			stock.put(new Component("Storage","64"),(double)25);
			stock.put(new Component("Storage","256"),(double)50);		
			stock.put(new Component("RAM","4"),(double)30);
			stock.put(new Component("RAM","8"),(double)60);
			stock.put(new Component("Battery","2000"),(double)70);
			stock.put(new Component("Battery","3000"),(double)100);
			System.out.println("Supplier 1 stock: " + stock);
		}
		else if (this.getName().contains("Supplier 2")) 
		{
			
			stock.put(new Component("Storage","64"),(double)15);
			stock.put(new Component("Storage","256"),(double)40);		
			stock.put(new Component("RAM","4"),(double)20);
			stock.put(new Component("RAM","8"),(double)35);
			System.out.println("Supplier 2 stock: " + stock);
		}
		else
			System.out.println("Invalid Supplier");

		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);


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
					System.out.println("new day");
					/*
					myAgent.addBehaviour(new BookGenerator());
					myAgent.addBehaviour(new FindBuyers(myAgent));
					CyclicBehaviour os = new OffersServer(myAgent);
					myAgent.addBehaviour(os);
					ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();
					cyclicBehaviours.add(os);
					myAgent.addBehaviour(new EndDayListener(myAgent,cyclicBehaviours));
					 */
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

}
