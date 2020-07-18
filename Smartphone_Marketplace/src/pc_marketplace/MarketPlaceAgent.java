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

		private List<AID> marketPlaceAgents = new ArrayList<>();

		public EndDay(Agent a, List<AID> marketPlaceAgents) {
			super(a);
			this.marketPlaceAgents = marketPlaceAgents;
		}

		public EndDay(Agent a, AID marketPlaceAgent) {
			super(a);
			this.marketPlaceAgents.add(marketPlaceAgent);
		}

		@Override
		public void action() {
			doWait(5000);
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			for (AID a : marketPlaceAgents) {
				msg.addReceiver(a);
				msg.setContent("done");
			}
			myAgent.send(msg);
		}

	}

}
