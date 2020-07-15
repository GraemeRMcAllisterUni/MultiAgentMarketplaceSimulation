package smartphone_marketplace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jade.core.*;
import jade.core.Runtime;
import jade.tools.sniffer.Sniffer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import ontology.elements.Component;

public class Main {

	static int c = 3; // customers

	static int w = 1; // storage per component per day

	static int p = 50; // per day late fee

	static int a = 50;

	public static void main(String[] args) {
		Object[] agentArgs = new Object[4];
		
		try {
			if (args.length != 0) {
				agentArgs[0] = args[0];
				agentArgs[1] = args[1];
				agentArgs[2] = args[2];
				agentArgs[3] = args[3];
			}
				else {
					agentArgs[0] = c;
					agentArgs[1] = w;
					agentArgs[2] = p;
					agentArgs[3] = a;
			}
		} 
		catch(Exception ex) {
			System.out.println(ex.getMessage());			
		}
		finally {
			System.out.println(Arrays.toString(agentArgs));
		}
		
		


		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		try {
			ContainerController myContainer = myRuntime.createMainContainer(myProfile);
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();

			for (int i = 0; i < c; i++) {
				AgentController custAgent = myContainer.createNewAgent("Customer" + Integer.toString(i),
						CustomerAgent.class.getCanonicalName(), null);
				custAgent.start();
			}

			AgentController tickerAgent = myContainer.createNewAgent("Ticker", DayTicker.class.getCanonicalName(),
					null);
			tickerAgent.start();

			AgentController SupplierAgent1 = myContainer.createNewAgent("Supplier 1",
					SupplierAgent.class.getCanonicalName(), null);
			SupplierAgent1.start();

			AgentController SupplierAgent2 = myContainer.createNewAgent("Supplier 2",
					SupplierAgent.class.getCanonicalName(), null);
			SupplierAgent2.start();

			AgentController snifferDog = myContainer.createNewAgent("Sniffer Dog", Sniffer.class.getCanonicalName(),
					null);
			snifferDog.start();

			AgentController manuAgent = myContainer.createNewAgent("Manufacturer",
					ManufacturerAgent.class.getCanonicalName(), agentArgs);
			manuAgent.start();

			AgentController postmanAgent = myContainer.createNewAgent("Postman", Postman.class.getCanonicalName(),
					null);
			postmanAgent.start();

		} catch (Exception e) {
			System.out.println("Exception starting agent: " + e.toString());
		}

	}

}
