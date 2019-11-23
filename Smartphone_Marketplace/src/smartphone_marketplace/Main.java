package smartphone_marketplace;

import jade.core.*;
import jade.core.Runtime;
import jade.tools.sniffer.Sniffer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Main {
	
	static int c = 3;
	
	static int w = 5;

	public static void main(String[] args) {

		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		try{
			ContainerController myContainer = myRuntime.createMainContainer(myProfile);	
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			
			
			for(int i=0 ; i<c ;i++)
			{
				AgentController custAgent = myContainer.createNewAgent("Customer" + Integer.toString(i), CustomerAgent.class.getCanonicalName(), null);
				custAgent.start();
			}
			
			
			
			AgentController SupplierAgent1 = myContainer.createNewAgent("Supplier 1", SupplierAgent.class.getCanonicalName(),
					null);
			SupplierAgent1.start();
			
			AgentController SupplierAgent2 = myContainer.createNewAgent("Supplier 2", SupplierAgent.class.getCanonicalName(),
					null);
			SupplierAgent2.start();
			
			
			AgentController snifferDog = myContainer.createNewAgent("Sniffer Dog", Sniffer.class.getCanonicalName(),
					null);
			snifferDog.start();
			

			AgentController manuAgent = myContainer.createNewAgent("Manufacturer", ManufacturerAgent.class.getCanonicalName(),
					null);
			manuAgent.start();
			
			AgentController tickerAgent = myContainer.createNewAgent("ticker", DayTicker.class.getCanonicalName(),
					null);
			tickerAgent.start();
			
			

					
		}
		catch(Exception e){
			System.out.println("Exception starting agent: " + e.toString());
		}

	}

}
