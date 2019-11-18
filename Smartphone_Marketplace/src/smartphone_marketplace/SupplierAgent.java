package smartphone_marketplace;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.Agent;
import ontology.MarketplaceOntology;

public class SupplierAgent extends Agent {
	private Codec codec = new SLCodec();
	private Ontology ontology = MarketplaceOntology.getInstance();

	public void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
	}


}
