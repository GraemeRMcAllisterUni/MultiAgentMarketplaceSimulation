package pc_marketplace;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class MarketPlaceAgent extends Agent {

	public class EndDay extends OneShotBehaviour {

		private List<AID> Agents = new ArrayList<>();

		public EndDay(Agent a, List<AID> agents) {
			super(a);
			this.Agents = agents;
		}

		public EndDay(Agent a, AID agent) {
			super(a);
			this.Agents.add(agent);
		}

		@Override
		public void action() {
			doWait(2000);
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			for (AID a : Agents) {
				msg.addReceiver(a);
			}
			msg.setContent("done");
			myAgent.send(msg);
		}

	}

}