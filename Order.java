package processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Order implements Runnable {
	public Map<String, Item> listOfItems = new TreeMap<>();
	private Map<String, Double> itemData = new HashMap<>();
	private Map<Integer, Order> orders;
	private int clientID;
	private String baseFile, itemFile;

	//Constructor initializes all instances variables
	public Order(String baseFileName, String itemsDataFile, Map<Integer, Order> orders) {
		this.baseFile = baseFileName;
		this.itemFile = itemsDataFile; 
		this.orders = orders; 
		
	}

	//Proccesses order based on inputed basefile
	public void processOrder() throws FileNotFoundException {
		File orderFile = new File(baseFile + ".txt"); //creates the file object
		loadItemData(itemFile); //loads all possible items and their prices
		Scanner orderReader = new Scanner(orderFile); 
		orderReader.next(); //Skips first white space to white space
		clientID = orderReader.nextInt(); //Saves the clientID to the instance variable
		while (orderReader.hasNextLine()) { //While loop that reads entire txt file
			String itemName = orderReader.next(); //Captures item name
			orderReader.next(); // Ignores date purchased
			if (listOfItems.containsKey(itemName)) { //if the treemap of items already contains this item 
				listOfItems.get(itemName).changeQuantity(1); //then just change quantity by 1
			} else { //if it doesn't, then put that item into the treemap
				listOfItems.put(itemName, new Item(itemName, itemData.get(itemName), 1));
			}

		}
		orderReader.close(); 
	}
	
	//Reads the itemData based on user's inputted itemData file name 
	public void loadItemData(String itemDataName) throws FileNotFoundException {
		File itemDataFile = new File(itemDataName);
		Scanner scan;
		scan = new Scanner(itemDataFile); 
		while (scan.hasNextLine()) {
			itemData.put(scan.next(), scan.nextDouble()); //Reads the item name and price 
			//Adds that to the itemData map with key as the name and value as the price
		}
		scan.close();

	}
	
	public int getClientId() {
		return clientID; //returns client ID
	}
	
 	public Order() { //Summary order constructor
		orders = null;
	}

 	//Iterates through the parameter order's list of items 
	public void addOrderItems(Order o) {
		for (Map.Entry<String, Item> entry : o.listOfItems.entrySet()) {
			//if the summary already has this item in it's list than it will add the quantity from the parameter order's
			//to its own quantity of that specific item 
			if(this.listOfItems.containsKey(entry.getKey())) {
				this.listOfItems.get(entry.getKey()).changeQuantity(entry.getValue().getQuantity());
			} else { //if this is the first time adding this item, than it will add the same thing as the parameter's using item copy constructor
				this.listOfItems.put(entry.getKey(), new Item(entry.getValue()));
			}
		}
	}



	//Simple toString method that formats correctly and iterates through
	//the list of items and adds each item's toString and keeps track of total cost
	@Override
	public String toString() {
		String answer = "----- Order details for client with Id: ";
		answer += clientID + " -----\n";
		double totalCost = 0;

		for (Map.Entry<String, Item> entry : listOfItems.entrySet()) {
			answer += entry.getValue().toString() + "\n";
			totalCost += entry.getValue().totalCost;
		}

		answer += "Order Total: " + NumberFormat.getCurrencyInstance().format(totalCost) + "\n";

		return answer;
	}

	//Custom toString method only for summaryMethods that does the same thing as the regular toString, but with different formatting
	public String summaryString() {
		String answer = "***** Summary of all orders *****\n";
		double totalCost = 0;

		for (Map.Entry<String, Item> entry : this.listOfItems.entrySet()) {
			answer += "Summary - " + entry.getValue().summaryString() + "\n";
			totalCost += entry.getValue().totalCost;
		}

		answer += "Summary Grand Total: " + NumberFormat.getCurrencyInstance().format(totalCost) + "\n";

		return answer;
	}
	
	//For threads, the lock object will be the parameter map of orders
	@Override
	public void run() {
		synchronized(orders) {
			try {
				//Will process the order
				this.processOrder();
				System.out.println("Reading order for client with id: " + this.getClientId()); //prints only when this item is being processed
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			//Than add this order to the map with the clientID as the key to keep it ordered
			orders.put(clientID, this);

		}
	}

	//Inner item class
	class Item implements Comparable<Item> {
		private double price;
		private String name;
		private int quantity;
		private double totalCost;

		//Instantiates all instances variables and  calculates total cost
		public Item(String name, double price, int quantity) {
			this.price = price;
			this.name = name;
			this.quantity = quantity;
			totalCost = price * quantity;

		}
		
		//Copy constructor
		public Item(Item item) {
			this.price = item.price;
			this.name = item.name;
			this.quantity = item.quantity;
			this.totalCost = item.totalCost;
		}

		
		public void changeQuantity(int delta) {
			this.quantity += delta;
			this.totalCost += (price * delta);
		}

		public int getQuantity() {
			return quantity;
		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		//To String that gives name, cost per item, and quantity, and total cost
		@Override
		public String toString() {
			String formattedPrice = NumberFormat.getCurrencyInstance().format(this.price);
			String totalCost = NumberFormat.getCurrencyInstance().format(this.totalCost);

			return "Item's name: " + name + ", Cost per item: " + formattedPrice + ", Quantity: " + quantity
					+ ", Cost: " + totalCost;
		}
		
		//To String that gives name, cost per item, and quantity, and total cost, but in different format for the summary order!
		public String summaryString() {
			String formattedPrice = NumberFormat.getCurrencyInstance().format(this.price);
			String totalCost = NumberFormat.getCurrencyInstance().format(this.totalCost);

			return "Item's name: " + name + ", Cost per item: " + formattedPrice + ", Number sold: " + quantity
					+ ", Item's Total: " + totalCost;
		}

		@Override
		public int compareTo(Item o) {
			return this.name.compareTo(o.name);
		}

	}

}
