package hw3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class Inventory {
	HashMap<String, Integer> stock;
	
	public Inventory() {
		stock = new HashMap<String, Integer>();
	}
	
	public synchronized void populate(String inFile) throws IOException {
		String currentLine;
		BufferedReader bf = new BufferedReader(new FileReader(inFile));
		while ((currentLine = bf.readLine()) != null) {
			String[] splitted = currentLine.split(" ");
			stock.put(splitted[0], Integer.parseInt(splitted[1]));
		}
		bf.close();
	}
	
	public synchronized int contains(String productName, int quantity) {
		if(!stock.containsKey(productName)) {
			return 0; // 0 means product not sold
		}
		if(stock.get(productName) < quantity) { 
			return -1; // -1 means we have product, but not the requested amount 
		}
		return 1; // we have product AND requested quantity
	}
	
	public synchronized void update(Order order) {
		String productName = order.productName;
		int quantity = order.quantity;
		int currentQuantity = stock.get(productName);
		currentQuantity -= quantity;
		stock.put(productName, currentQuantity);
	}
	
	@Override
	public String toString() {
		return stock.toString();
	}
	
	public synchronized String visualize() {
		Set<String> keys = stock.keySet();
		String s = "";
        for(String key: keys){
        	s += key + " " + stock.get(key) + "\n";
        }
        return s;
	}
	
	public synchronized String tcpVisualize() {
		Set<String> keys = stock.keySet();
		String s = "";
        for(String key: keys){
        	s += key + " " + stock.get(key) + "~";
        }
        return s;
	}
}
