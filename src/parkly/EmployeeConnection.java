package parkly;

import java.io.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class EmployeeConnection implements Runnable {
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private Message msg;
	private volatile Ticket generateTicket = null;
	private volatile Ticket incomingTicket = null;
	private Scanner sc;
	private String input;
	private String type;
	private String status;
	private String text;
	private volatile boolean running = true;
	private volatile Report lastReport = null;

	
	
	// consider adding host and port number to parameters for constructor, not implemented for testing ATM.
	EmployeeConnection(String host, int port) throws IOException {
		System.out.println("Creating EmployeeConnection.");
		try {
			this.socket = new Socket(host, port);
			if (socket != null) {
				System.out.println("Created successful socket connection.");
			}
			this.sc = new Scanner(System.in);
			this.oos = new ObjectOutputStream(this.socket.getOutputStream());
			this.oos.flush();
			this.ois = new ObjectInputStream(this.socket.getInputStream());
			this.msg = new Message("login", null, "login"); // Initialize the msg object to begin login handshake 
			oos.writeObject(this.msg); // Send login message to server
			this.msg = (Message) ois.readObject(); // Read servers response: login | success | CONNECTION ESTABLISHED!
			// verify handshake is complete
			if ((!msg.getType().equalsIgnoreCase("login") && msg.getStatus().equalsIgnoreCase("success"))) {
				throw new IOException("Login failed: " + msg.getText());
			}
			this.type = "text";
			System.out.println("Successful creation of EmployeeConnection Object.");
		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
			throw new IOException("Server response invalid.", e);
		}
	}
	
	// Listens to incoming messages from the server here
	@Override
	public void run() {
		try {
			// Listens for incoming messages
			while (running) {
				// Check what object we receive first
				Object receivedObject = ois.readObject();
				if (!(receivedObject instanceof ObjectTag)) {
					System.err.println("Protocol error: Received object cannot be identified.");
					continue;
				}
				ObjectTag taggedObject = (ObjectTag) receivedObject;
				String tag = taggedObject.getObjectTag();
				switch (tag) {
					case "MESSAGE": 
						this.msg = (Message) taggedObject;
						EmployeeGUI.appendServerMessage("Server: " + this.msg.getText() + "\n");
//						if (type.equalsIgnoreCase("text") && !input.equalsIgnoreCase("logout")) {
////							oos.writeObject(new Message(type, status, input)); // create new message object to send to server
////							msg = (Message) ois.readObject(); // retrieve servers response in caps
////							status = msg.getStatus(); // synch status of messages
////							EmployeeGUI.appendServerMessage("Server: " + this.msg.getText());
//						} else if (input.equalsIgnoreCase("logout") || text.equalsIgnoreCase("logout")) { // if logout is sent to server
////							oos.writeObject(new Message("logout", status, "logout")); // send new logout message to server
////							msg = (Message) ois.readObject(); // receive logout message to end connection
////							status = msg.getStatus(); // synch status of messages
//							break; // end loop and close socket
//						} else { 
////							System.out.println("Protocol error: Unexpected message type...");
//							break;
//						}
						break;
						
					case "TICKET":
						Ticket receivedTicket = (Ticket) taggedObject;
						this.generateTicket = receivedTicket;
						this.incomingTicket = receivedTicket;
						System.out.println("EmployeeConnection.run: TICKET OBJECT DETECTED: " + receivedTicket.getTicketID());
						System.out.println("\tFOUND TICKET: " + receivedTicket.getTicketID());
						System.out.println("\tTicket Data:\n\tENTRY TIME: " + receivedTicket.getEntryTime() + "\n\tEXIT TIME: " + 
											receivedTicket.getExitTime() + "\n\tFEES DUE: " + receivedTicket.getTotalFees() + "\n\tPAID: " + receivedTicket.isPaid());
//						EmployeeGUI.appendServerMessage("Server: new ticket with ID: " + receivedTicket.getTicketID());

						break;
						
					case "REPORT":
					    this.lastReport = (Report) taggedObject;
					    System.out.println("EmployeeConnection.run: REPORT received for date "
					                       + lastReport.getDate() + " tickets=" + lastReport.getTickets().size());
					    break;

					
					default: 
						System.err.println("Unknown object tag received: " + tag);
						break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
					System.out.println("Socket closed properly, Goodbye!");
				} catch (IOException e) {
					System.out.println("Warning: Error closing socket.");
				}
			}
		}
	}
	
	public Message getMessage() {
		return this.msg;
	}
	
	public void sendMessage(Message msg) {
		if (oos == null || socket.isClosed()) {
			System.err.println("Connection is closed. Cannot send message.");
			return;
		}
		try {
			// Send message through function
			if (msg != null && !msg.getText().isEmpty()) {
				System.out.println("EmployeeConnection.sendMessage:\n\tSending msg: " + msg.getType() + " | " + msg.getStatus() + " | " + msg.getText());
				this.oos.writeObject(msg);
				this.oos.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Error sending message: " + e.getMessage());
		}
	}
	
	public Ticket generateTicket() {
		sendMessage(new Message("TICKET", "SUCCESS", "GENERATE NEW TICKET"));
		long startTime = System.currentTimeMillis();
		while (this.generateTicket == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
		Ticket returnTicket =  this.generateTicket;
		this.generateTicket = null;
		return returnTicket;
	}
	
	public Ticket findTicket(String ticketID) {
		this.incomingTicket = null;
		sendMessage(new Message("FIND TICKET", "SUCCESS", ticketID));
		long startTime = System.currentTimeMillis();
		final long TIMEOUT_MS = 5000; // 5 seconds
		// wait until ticket is received OR timeout expires
		while (this.incomingTicket == null && (System.currentTimeMillis() - startTime < TIMEOUT_MS)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
		// if we exit the lopp and incomingTicket is still null
		if (this.incomingTicket == null) {
			System.out.println("EmployeeConnection.incomingTicket:\n\tTimed out waiting for server response.");
		}
		
		Ticket returnTicket = this.incomingTicket;
		this.incomingTicket = null;
		System.out.println("EmployeeConnection.findTicket: \n\tFOUND TICKET: " + returnTicket.getTicketID());
		System.out.println("\n\tTicket Data:\n\tENTRY TIME: " + returnTicket.getEntryTime() + "\n\tEXIT TIME: " + returnTicket.getExitTime() + "\n\tFEES DUE: " + returnTicket.getTotalFees());
		return returnTicket;
	}
	
	public Ticket payTicket(Ticket paidTicket) {
		sendMessage(new Message("PAY TICKET", "SUCCESS", paidTicket.getTicketID()));
		long startTime = System.currentTimeMillis();
		final long TIMEOUT_MS = 5000;
		while (this.incomingTicket == null && (System.currentTimeMillis() - startTime < TIMEOUT_MS)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
		if (this.incomingTicket == null) {
			System.out.println("EmployeeConnection.payTicket: \n\tTimed out waiting for server response.");
		}
		Ticket returnTicket = this.incomingTicket;
		this.incomingTicket = null;
		System.out.println("Employeeconnection.payTicket: \n\tPaidTicket: " + returnTicket.getTicketID());
		System.out.println("\n\tTicket Paid: " + returnTicket.isPaid());
		return returnTicket;
	}
	
	public String openEntryGate() {
		sendMessage(new Message("GATE", "SUCCESS", "OPEN ENTRY GATE"));
		return "Could not open gate";
	}
	
	public Report requestReport(String date) {
	    this.lastReport = null;
	    sendMessage(new Message("REPORT", "SUCCESS", date));

	    long startTime = System.currentTimeMillis();
	    final long TIMEOUT_MS = 5000;

	    while (this.lastReport == null &&
	           (System.currentTimeMillis() - startTime < TIMEOUT_MS)) {
	        try {
	            Thread.sleep(50);
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	            return null;
	        }
	    }

	    if (this.lastReport == null) {
	        System.out.println("EmployeeConnection.requestReport: timed out waiting for report.");
	    }

	    Report result = this.lastReport;
	    this.lastReport = null;
	    return result;
	}

	
	// Inside EmployeeConnection.java
	public void logout() {
	    // 1. Send logout request
	    sendMessage(new Message("logout", null, "logout"));
	    
	    // 2. Add a synchronous read to catch the server's confirmation message
	    try {
	        // Wait for the server to confirm the status is "logout" before closing
	        Object obj = ois.readObject(); // This is safe here because the thread is ending
	        if (obj instanceof Message) {
	            Message logoutConfirm = (Message) obj;
	            System.out.println("Logout confirmed by server: " + logoutConfirm.getText());
	        }
	    } catch (Exception e) {
	        System.out.println("Warning: Error during logout confirmation.");
	    }
	    
	    // 3. Close connection and end run() loop
	    this.running = false;
	    if (socket != null && !socket.isClosed()) {
	        try {
	            socket.close();
	            System.out.println("Socket closed properly.");
	        } catch (IOException e) {
	            e.printStackTrace();
	            System.out.println("Warning: Error closing socket.");
	        }
	    }
	}
}


