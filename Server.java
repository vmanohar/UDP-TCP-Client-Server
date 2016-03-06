package hw3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Server {

	int tcpPort;
	static String fileName;
	static ArrayList<Order> orders = new ArrayList<Order>();
    static Inventory inventory = new Inventory();
	
//    public Server(String[] args){
//        tcpPort = Integer.parseInt(args[0]);
//        fileName = args[2];
//    }
	
	  public static void main (String[] args) throws IOException {

	    if (args.length != 3) {
	      System.out.println("ERROR: Provide 3 arguments");
	      System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
	      System.out.println("\t(2) <udpPort>: the port number for UDP connection");
	      System.out.println("\t(3) <file>: the file of inventory");
	      System.exit(-1);
	    }
	    
	    // parse the inventory file
	    inventory.populate(args[2]);
		UdpServer udpserver = new UdpServer(args, inventory, orders);
		TcpServer tcpserver = new TcpServer(args, inventory, orders);
		Thread t1 = new Thread(udpserver);
		Thread t2 = new Thread(tcpserver);
		t1.start();
		t2.start();
	  }
}