package hw3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class UdpServer implements Runnable {
	
	InetAddress ia;
	String hostName = "localhost";
	int len = 1024;
	int udpPort;
	ArrayList<Order> orders;
	Inventory inventory;
	
	public UdpServer(String[] args, Inventory inventory, ArrayList<Order> orders) throws UnknownHostException{
        this.udpPort = Integer.parseInt(args[1]);
		this.ia = InetAddress.getByName(this.hostName);
		this.inventory = inventory;
		this.orders = orders;
	}
	
	public void run(){
		DatagramPacket datapacket, returnpacket;
		DatagramSocket datasocket = null;
		try {
			datasocket = new DatagramSocket(this.udpPort);
		} catch (SocketException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		byte[] buf = new byte[this.len];
		while (true) {
			datapacket = new DatagramPacket(buf, buf.length);
			try {
				datasocket.receive(datapacket);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			byte[] someMsg = datapacket.getData();
			DatagramPacket responsePacket = this.parseMessage(someMsg);
			
			// send a reply to client
			try { this.sendResponse(responsePacket, datapacket, datasocket); }
			catch (IOException e) {}
		}
	}
	
	private void sendResponse(DatagramPacket responsePacket, DatagramPacket datapacket, DatagramSocket datasocket) throws IOException {
		DatagramPacket returnpacket;
		returnpacket = new DatagramPacket(
				responsePacket.getData(),
				responsePacket.getLength(),
				datapacket.getAddress(),
				datapacket.getPort());
		datasocket.send(returnpacket);
	}
	
	private DatagramPacket parseMessage(byte[] msg){
		String command = new String(msg);
		String[] tokens = command.split(" ");
		DatagramPacket resp = null;
		String responseStr;
		byte[] buffer;

		System.out.println("command is: " + command);
		if (tokens[0].equals("purchase")) {
			resp = udpPurchase(tokens);	
		} else if (tokens[0].equals("cancel")) {
			resp = udpCancel(tokens);
		} else if (tokens[0].equals("search")) {
			resp = udpSearch(tokens);
		} else if (tokens[0].equals("list")) {
			resp = udpList();
		} else {
		  System.out.println("ERROR: No such command");
		}
		
		return resp;
	}

	private DatagramPacket udpList() {
		DatagramPacket resp;
		byte[] buffer;
		String responseStr = inventory.visualize();
		buffer = new byte[responseStr.length()];
		buffer = responseStr.getBytes();
		resp = new DatagramPacket(buffer, buffer.length, this.ia, this.udpPort);
		return resp;
	}

	private DatagramPacket udpSearch(String[] tokens) {
		DatagramPacket resp;
		String responseStr = "";
		byte[] buffer;
		for (Order o : orders){
			if (o.user.equals(tokens[1])){
				responseStr += o.orderID + ", " + o.productName + ", " + o.quantity + "\n";
			}
		}
		// if no order found in orders for that person
		if (responseStr.equals("")){
			responseStr = "No order found for " + tokens[1];
		}
		buffer = new byte[responseStr.length()];
		buffer = responseStr.getBytes();
		resp = new DatagramPacket(buffer, buffer.length, this.ia, this.udpPort);
		return resp;
	}

	private DatagramPacket udpCancel(String[] tokens) {
		DatagramPacket resp;
		String responseStr;
		byte[] buffer;
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
		buffer = new byte[responseStr.length()];
		buffer = responseStr.getBytes();
		resp = new DatagramPacket(buffer, buffer.length, this.ia, this.udpPort);
		return resp;
	}

	private DatagramPacket udpPurchase(String[] tokens) {
		DatagramPacket resp;
		String responseStr;
		byte[] buffer;
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
		buffer = new byte[responseStr.length()];
		buffer = responseStr.getBytes();
		resp = new DatagramPacket(buffer, buffer.length, this.ia, this.udpPort);
		return resp;
	}
}
