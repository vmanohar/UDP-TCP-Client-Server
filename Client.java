package hw3;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	
	String hostName;
    int tcpPort;
    int udpPort; // get from user input
    int len = 1024; 
    
    public Client(String[] args){
        hostName = args[0];
        tcpPort = Integer.parseInt(args[1]);
        udpPort = Integer.parseInt(args[2]);
    }
    
  public static void main (String[] args) throws IOException {
   
    Client client = new Client(args);
    
    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <hostAddress>: the address of the server");
      System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(3) <udpPort>: the port number for UDP connection");
      System.exit(-1);
    }
    while(true) {
	    Scanner sc = new Scanner(System.in);
	    while(sc.hasNextLine()) {
	      String cmd = sc.nextLine();
	      String[] tokens = cmd.split(" ");
	      if(tokens[tokens.length - 1].equals("U")) {
	          doUDP(client, cmd, tokens);
	      }
	      else {
	    	  // TCP goes here
	    	  doTCP(client, cmd, tokens);
	      }
	    }
    }
  }

	private static void doTCP(Client client, String cmd, String[] tokens) throws IOException {
		  Socket clientSocket = new Socket("localhost", client.tcpPort);
		  DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		  outToServer.writeBytes(cmd + "\n");
		  System.out.println("Sending . . .");
		  String retstring = inFromServer.readLine();
		  retstring = retstring.replace('~', '\n');
		  System.out.println("Received from Server: \n" + retstring);
		  clientSocket.close();
	}

	private static void doUDP(Client client, String cmd, String[] tokens) throws UnknownHostException, SocketException, IOException {
		  DatagramPacket sPacket, rPacket;
		  InetAddress ia = InetAddress.getByName(client.hostName);
		  DatagramSocket datasocket = new DatagramSocket();
		  byte[] buffer = new byte[cmd.length()];
		  buffer = cmd.getBytes();
		  sPacket = new DatagramPacket(buffer, buffer.length, ia, client.udpPort);
		  datasocket.send(sPacket);
		  System.out.println("Sending . . .");
		  
		  // Receive from server
		  	byte[] rbuffer = new byte[client.len];
		  	rPacket = new DatagramPacket(rbuffer, rbuffer.length);
		  	datasocket.receive(rPacket);
		  	String retstring = new String(rPacket.getData(), 0,
		  			rPacket.getLength());
		  	System.out.println("Received from Server: \n" + retstring);
	}
}