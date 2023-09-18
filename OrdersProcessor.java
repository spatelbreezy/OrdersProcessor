package processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class OrdersProcessor {
	public static void main(String[] args) throws FileNotFoundException {
		
		
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter item's data file name: ");
		String itemsData = scan.next();

		System.out.print("\nEnter 'y' for multiple threads, any other character otherwise: ");
		String multiChar = scan.next();
		boolean multithread;
		if (multiChar.equals("y")) {
			multithread = true;
		} else {
			multithread = false;
		}

		System.out.print("\nEnter number of orders to process: ");
		int numOrders = scan.nextInt();

		System.out.print("\nEnter order's base filename: ");
		String baseFile = scan.next();

		System.out.print("\nEnter result's filename:");
		String result = scan.next();
		System.out.println();
		scan.close();
		//Everything above this is basic scanner output and input
		
		long startTime = System.currentTimeMillis(); //Starts timer

		Map<Integer, Order> orders = new TreeMap<>(); //Makes a tree map of orders ordered based on clientID
		ArrayList<Thread> threads = new ArrayList<>(); //ArrayList of thread addresses so we can access them later
		Order summary = new Order(); //Summary order to add all the items of other orders
		
		
		//Creates a certain amount of orders based on input from earlier scanner
		for(int i = 1; i < numOrders + 1; i++) {
			Order order = new Order(baseFile + i, itemsData, orders);
			//if the user chose to multithread than it will create threads and add it to the thread arraylist
			if(multithread) {
				Thread thread = new Thread(order);
				threads.add(thread);
			} else { //if not multithreading than the order will be processed and added to the summary order so it can total all the items
				order.processOrder();
				summary.addOrderItems(order);
				System.out.println("Reading order for client with id: " + order.getClientId()); //Print this so the user knows which one is being read
				orders.put(order.getClientId(), order); //Puts in the tree map of orders so it stays sorted based on clientID
			}
		}
		
		if(multithread) {
			//Starts all threads and joins them 
			for(Thread thread : threads) {
				thread.start();
			}
			
			for(Thread thread : threads) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					System.out.println(e.getMessage());
				}
			}
			//Iterates through the orders treemap and adds them all to the summary order 
			for(Map.Entry<Integer, Order> entry : orders.entrySet()) {
				summary.addOrderItems(entry.getValue());
			}
		}
		
		
		//Writes to the file 
		try {
			FileWriter resultFile = new FileWriter(new File(result));
			//Iterates through all orders from treeset and appends the toString for each order to the file
			for(Map.Entry<Integer, Order> entry : orders.entrySet()) {
				resultFile.append(entry.getValue().toString());
			}
			//Adds the summary order's custom toString method at the end 
			resultFile.append(summary.summaryString());
			resultFile.close(); //finishes editing file 
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		long endTime = System.currentTimeMillis(); //Stops timer
		System.out.println("Processing time (msec): " + (endTime - startTime)); //Prints how long in ms it took to process everything
		System.out.println("Results can be found in the file: " + result);

	}



	
}