package ontology.elements;

import java.util.ArrayList;
import java.util.List;


import jade.content.Predicate;

import jade.core.AID;

@SuppressWarnings("serial")
public class ComponentSupplier implements Predicate{


	private AID supplier;

	private List<Component> stock = new ArrayList<Component>();

	public ComponentSupplier(AID name) {
		this.supplier = name;
	}

	public AID getSupplier() {
		return supplier;
	}

	public void setSupplier(AID supplier) {
		this.supplier = supplier;
	}

	public List<Component> getStock() {
		return stock;
	}

	public void setStock(List<Component> stock) {
		this.stock = stock;
	}

	
	

//	public HashMap getStock() {
//		HashMap<Component, Double> stockList = new HashMap<>();
//		for(int i=0; i<stock.size();i++) {
//			stockList.put(stock.get(i), prices.get(i));
//		}			
//		return stockList;
//	}

//	public void setStock(HashMap<Component, Double> stockList) {
//		stockList.forEach((k,v) ->{ 
//			stock.add(k); 
//			prices.add(v);
//			});
//	}

}
