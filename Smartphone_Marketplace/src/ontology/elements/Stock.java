package ontology.elements;

import java.util.ArrayList;
import java.util.List;

import jade.content.Concept;

@SuppressWarnings("serial")
public class Stock implements Concept {

	private List<Component> stock = new ArrayList<Component>();
	private List<Double> price = new ArrayList<Double>();

	public List<Component> getStock() {
		return stock;
	}

	public void setStock(List<Component> stock) {
		this.stock = stock;
	}

	public List<Double> getPrice() {
		return price;
	}

	@Override
	public String toString() {
		return "Stock [stock=" + stock + ", price=" + price + "]";
	}

	public void setPrice(List<Double> price) {
		this.price = price;
	}

	public void addPart(Component c, Double p) {
		this.stock.add(c);
		this.price.add(p);
	}

	public double getComponentPrice(Component c) {
		for (int i = 0; i < stock.size(); i++)
			if (stock.get(i).equals(c))
				return price.get(i);
		System.out.println("No stock found for " + c);
		return Double.MAX_VALUE;
	}

}
