package hw3;

public class Order {

	static int currentOrderIndex = 1; // global increment
	
	String user;
	int orderID;
	String productName;
	int quantity;
	
	public Order (String user, String productName, int quantity){
		this.user = user;
		this.productName = productName;
		this.quantity = quantity;
		this.orderID = currentOrderIndex;
		incrementCurrentOrderIndex();
	}

	public Order(int orderID) {
		this.orderID = orderID;
	}
	
	private static synchronized void incrementCurrentOrderIndex() {
		currentOrderIndex += 1;
	}

	
	@Override
	public synchronized boolean equals(Object other) {
		return (((Order)other).orderID == this.orderID);
	}
	
	/*
	public static void decrementOrderNumber() {
		currentOrderIndex -= 1;
	}
	*/
}
