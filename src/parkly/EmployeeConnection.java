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
	private Ticket ticket = null;
	private Scanner sc;
	private String input;
	private String type;
	private String status;
	private String text;
	private volatile boolean running = true;
	
	
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
						if (type.equalsIgnoreCase("text") && !input.equalsIgnoreCase("logout")) {
							oos.writeObject(new Message(type, status, input)); // create new message object to send to server
							msg = (Message) ois.readObject(); // retrieve servers response in caps
							status = msg.getStatus(); // synch status of messages
							System.out.println("Server: " + msg.getText()); // display new message in console
							System.out.print("Enter message: ");
						} else if (input.equalsIgnoreCase("logout") || text.equalsIgnoreCase("logout")) { // if logout is sent to server
							oos.writeObject(new Message("logout", status, "logout")); // send new logout message to server
							msg = (Message) ois.readObject(); // receive logout message to end connection
							status = msg.getStatus(); // synch status of messages
							break; // end loop and close socket
						} else { 
							System.out.println("Protocol error: Unexpected message type...");
							break;
						}
						break;
						
					case "TICKET":
						this.ticket = (Ticket) taggedObject;
						System.out.println("Received Ticket ID: " + this.ticket.getTicketID());
						EmployeeGUI.appendServerMessage("Server: new ticket created with ID: " + this.ticket.getTicketID());
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
				System.out.println("EmployeeConnection.sendMessage(): Sending msg: " + msg.getType() + " | " + msg.getStatus() + " | " + msg.getText());
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
		while (this.ticket == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
		Ticket returnTicket =  this.ticket;
		this.ticket = null;
		return returnTicket;
	}
	
	
	public void logout() {
		this.running = false;
		if (socket != null && !socket.isClosed()) {
			try {
				oos.writeObject(new Message("logout", null, "logout"));
				socket.close();
				System.out.println("Socket closed properly.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Warning: Error closing socket.");
			}
			
		}
	}
}


