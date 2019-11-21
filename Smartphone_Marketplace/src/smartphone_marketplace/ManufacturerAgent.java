package smartphone_marketplace;

import java.util.ArrayList;
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
	private AID customerAID;
	private Codec codec = new SLCodec();
	private Ontology ontology = MarketplaceOntology.getInstance();

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
					System.out.println("Manufacturer heard new day from ticker agent");
					//spawn new sequential behaviour for day's activities
					CyclicBehaviour or = new OrderRequest();
					myAgent.addBehaviour(or);
					ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();
					cyclicBehaviours.add(or);
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

	public class EndDayListener extends CyclicBehaviour {
		private int buyersFinished = 0;
		private List<Behaviour> toRemove;

		public EndDayListener(Agent a, List<Behaviour> toRemove) {
			super(a);
			this.toRemove = toRemove;
		}

		@Override
		public void action() {

			MessageTemplate mt = MessageTemplate.MatchContent("done");
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null) {
				ACLMessage msgSupplier = new ACLMessage(ACLMessage.INFORM);
				DFAgentDescription agentDesc = new DFAgentDescription();
				ServiceDescription serviceDesc = new ServiceDescription();
				serviceDesc.setType("supplier");
				agentDesc.addServices(serviceDesc);
				try{
					DFAgentDescription[] agentsFound  = DFService.search(myAgent,agentDesc); 

					for(DFAgentDescription aF : agentsFound)
						supplierAgents.add(aF.getName()); // this is the AID
				}
				catch(Exception e)
				{}

				for(AID agent : supplierAgents) {
					msgSupplier.addReceiver(agent);
				}
				myAgent.send(msgSupplier);

			}
			else {
				block();
			}
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

		}

	}

	private class OrderRequest extends CyclicBehaviour{
		@Override
		public void action() {

			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				try {
					ContentElement ce = null;
					System.out.println(msg.getContent());
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof PlaceOrder) {
							PlaceOrder order = (PlaceOrder)action;
							Item it = order.getItem();
							if(it instanceof OrderDetails){
								OrderDetails od = (OrderDetails)it;
								//List<Component> cs = od.getComponents();
								Device d = od.getDevice();
								d.setComponents(d.getComponents());
								System.out.println("Order received: " + d.toString());
								int i = 0;								
								System.out.println("Components size: "+Integer.toString(d.getComponents().size()));
							

									
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
}