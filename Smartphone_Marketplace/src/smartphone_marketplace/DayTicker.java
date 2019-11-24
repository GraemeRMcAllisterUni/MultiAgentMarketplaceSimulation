/**
 * 
 */
package smartphone_marketplace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class DayTicker extends Agent {
	public static final int NUM_DAYS = 30;
	@Override
	protected void setup() {
		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("ticker-agent");
		sd.setName(getLocalName() + "-ticker-agent");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
		//wait for the other agents to start
		doWait(5000);
		addBehaviour(new SynchAgentsBehaviour(this));
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

	public class SynchAgentsBehaviour extends Behaviour {

		private int step = 0;
		private int numFinReceived = 0; //finished messages from other agents
		private int day = 0;
		private ArrayList<AID> marketplaceAgents = new ArrayList<>();
		/**
		 * @param a	the agent executing the behaviour
		 */
		public SynchAgentsBehaviour(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			switch(step) {
			case 0:
				System.out.println("Setting up day " + day);
				//doWait(9000);
				//find all agents using directory service
				List<String> agents =  Arrays.asList("customer", "manufacturer", "supplier", "postman");
				for(String a : agents)
				{
					DFAgentDescription agentDesc = new DFAgentDescription();
					ServiceDescription serviceDesc = new ServiceDescription();
					serviceDesc.setType(a);
					agentDesc.addServices(serviceDesc);
					try{
						DFAgentDescription[] agentsFound  = DFService.search(myAgent,agentDesc); 
						
						for(DFAgentDescription aF : agentsFound) {
							marketplaceAgents.add(aF.getName()); // this is the AID						
						}
					}
					catch(FIPAException e) {
						e.printStackTrace();
					}
				}
		

				//send new day message to each agent
				ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
				tick.setContent("new day");
				for(AID id : marketplaceAgents) {
					tick.addReceiver(id);
				}
				myAgent.send(tick);
				step++;
				day++;
				break;
			case 1:
				//wait to receive a "done" message from all agents
				MessageTemplate mt = MessageTemplate.MatchContent("done");
				ACLMessage msg = myAgent.receive(mt);
				if(msg != null) {
					numFinReceived++;
					if(numFinReceived >= marketplaceAgents.size()) {
						step++;
					}
				}
				else {
					block();
				}

			}
		}

		@Override
		public boolean done() {
			return step == 2;
		}

		
		@Override
		public void reset() {
			super.reset();
			step = 0;
			marketplaceAgents.clear();
			numFinReceived = 0;
		}

		
		@Override
		public int onEnd() {
			System.out.println("End of day " + day);
			if(day == NUM_DAYS) {
				//send termination message to each agent
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setContent("terminate");
				for(AID agent : marketplaceAgents) {
					msg.addReceiver(agent);
				}
				myAgent.send(msg);
				myAgent.doDelete();
			}
			else {
				reset();
				myAgent.addBehaviour(this);
			}
			
			return 0;
		}



	}

}
