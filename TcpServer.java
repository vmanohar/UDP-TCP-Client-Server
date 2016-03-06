package hw3;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class TcpServer implements Runnable {
	InetAddress ia;
	String hostName = "localhost";
	int len = 1024;
	int tcpPort;
	ArrayList<Order> orders;
	Inventory inventory;
	
	public TcpServer(String[] args, Inventory inventory, ArrayList<Order> orders) throws UnknownHostException {
		this.tcpPort = Integer.parseInt(args[0]);
		this.ia = InetAddress.getByName(this.hostName);
		this.inventory = inventory;
		this.orders = orders;
	}
	
	public void run() {
        ServerSocket serverSocket = null;
		try { serverSocket = new ServerSocket(this.tcpPort); }
		catch (IOException e1) {}

        while(true) {
	        Socket connectionSocket = null;
			try { connectionSocket = serverSocket.accept(); }
			catch (IOException e1) {}
	        BufferedReader inFromClient = null;
			try { inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));}
			catch (IOException e1) {}
	        DataOutputStream outToClient = null;
			try { outToClient = new DataOutputStream(connectionSocket.getOutputStream()); } 
			catch (IOException e1) {}
	        String cmd = null;
			try { cmd = inFromClient.readLine(); System.out.println("Receiving . . .");}
			catch (IOException e1) {}
	        String result = this.parseMessage(cmd);
	        try { outToClient.writeBytes(result + "\n"); } 
	        catch (IOException e) {}
        }
	}
	
	private String parseMessage(String command){
		String[] tokens = command.split(" ");		
		String responseStr = "";
		System.out.println("command is: " + command);
		if (tokens[0].equals("purchase")) {
			responseStr = tcpPurchase(tokens);	
		} else if (tokens[0].equals("cancel")) {
			responseStr = tcpCancel(tokens);
		} else if (tokens[0].equals("search")) {
			responseStr = tcpSearch(tokens);
		} else if (tokens[0].equals("list")) {
			responseStr = tcpList();
		} else {
		  System.out.println("ERROR: No such command");
		}
		
		return responseStr;
	}

	private String tcpList() { 
		return inventory.tcpVisualize(); 
	}

	private String tcpSearch(String[] tokens) {
		String responseStr = "";
		for (Order o : orders){
			if (o.user.equals(tokens[1])){
				responseStr += o.orderID + ", " + o.productName + ", " + o.quantity + "~";
			}
		}
		// if no order found in orders for that person
		if (responseStr.equals("")){
			responseStr = "No order found for " + tokens[1];
		}
		return responseStr;
	}

	private String tcpCancel(String[] tokens) {
		String responseStr;
		Order dummy = new Order(Integer.parseInt(tokens[1]));
		if(!orders.contains(dummy)) {
			responseStr = "Order " + Integer.parseInt(tokens[1]) + " not found, no such order";
		}
		else {
			// find order from order number
			int i = orders.indexOf(dummy);
			
			// modify order with negative quantity
			Order temp = orders.get(i);
			temp.quantity *= -1;
			
			// update inventory 
			inventory.update(temp);
			
			// delete order from orders
			orders.remove(i);
			
			// build message
			responseStr = "Order " + Integer.parseInt(tokens[1]) + " is canceled";
		}
		return responseStr;
	}

	private String tcpPurchase(String[] tokens) {
		String responseStr;
		// if product not sold
		if(inventory.contains(tokens[2], Integer.parseInt(tokens[3])) == 0) {
			responseStr = "Not Available - We do not sell this product";
		}
		// if quantity not available
		else if(inventory.contains(tokens[2], Integer.parseInt(tokens[3])) == -1) {
			responseStr = "Not Available - Not enough items";
		}
		// we're good!
		else {
			Order order = new Order(tokens[1], tokens[2], Integer.parseInt(tokens[3]));
			orders.add(order);
			inventory.update(order);
			responseStr = "Your order has been placed, " 
						+ order.orderID + " "
						+ order.user + " "
						+ order.productName + " "
						+ order.quantity;
		}
		return responseStr;
	}
}