package parkly;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
	private static int employeeCount; // keep track of how many employee 
	private static List<Ticket> activeTickets = new ArrayList<>();
	
	private static synchronized void increment() {employeeCount++;};
	public static void main(String[] args) {
		
		ServerSocket server = null;
		employeeCount = 0;
		try {
			// server is listening on port 1235
			server = new ServerSocket(1235);
			server.setReuseAddress(true);
			
			// run infinite loop for getting client request
			while (true) { 
				System.out.println("Server started. Waiting for incoming connections...");
				Socket employee = server.accept(); // accept incoming requests from employee computer
				increment(); // increment employee count
				System.out.println("New employee connected: ID(" + employeeCount + ") " + employee.getInetAddress().getHostAddress()); // for testing
				
				EmployeeHandler employeeSocket = new EmployeeHandler(employeeCount, employee); // create handler for newly connected employee
				
				new Thread(employeeSocket).start(); // hand off employee handler to new thread for multi-threading
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (server != null) {
				try {
					System.out.println("Server shutting down.");
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static class EmployeeHandler implements Runnable {
		private final int employeeID; // keep track of this EmployeHandler's employee id
		private final Socket employeeSocket; // for socket manipulation
		private Message msg;
		private String type;
		private String status;
		private String text;
		private Ticket ticket;
		// constructor
		public EmployeeHandler(int employeeCount, Socket socket) {
			this.employeeID = employeeCount;
			this.employeeSocket = socket;
		}
		
		public void run() {
			try {
				// Start object output and input streams
				ObjectOutputStream oos = new ObjectOutputStream(this.employeeSocket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(this.employeeSocket.getInputStream());
				System.out.println("Socket connected.");
				// Test if a message can be sent from employee computer
				this.msg = (Message) ois.readObject();
				this.type = msg.getType();
				this.status = msg.getStatus();
				this.text = msg.getText();
				if (!(this.type.equalsIgnoreCase("login"))) {
					throw new IOException();
				}
				this.type = "login";
				this.status = "success";
				this.text = "CONNECTION ESTABLISHED!";
				// Create new success object to send to client
				oos.writeObject(new Message(this.type, this.status, this.text));
				
				// Loop to listen for incoming messages from client
				while (true) {
					// Listening while loop for any new incoming objects
					Object receivedObject = ois.readObject();
					if(!(receivedObject instanceof ObjectTag)) {
						System.err.println("Protocol error: Received object cannot be identified.");
						oos.writeObject(new Message("error", "protocol", "Unknown object type received."));
						continue;
					}
					
					ObjectTag taggedObject = (ObjectTag) receivedObject;
					String tag = taggedObject.getObjectTag();
					switch (tag) {
						case "MESSAGE":
							this.msg = (Message) taggedObject;
							System.out.println("Successfully received message: " + this.msg.getText());
							if (this.msg.getType().equalsIgnoreCase("text") && this.msg.getStatus().equalsIgnoreCase("success")) {
								oos.writeObject(new Message(this.msg.getType(), this.msg.getStatus(), this.msg.getText().toUpperCase().trim()));
							} else if (this.msg.getType().equalsIgnoreCase("TICKET") && this.msg.getText().equalsIgnoreCase("GENERATE NEW TICKET")) {
								Ticket ticket = new Ticket();
								System.out.println("New ticket generate.\nTicketID: " + ticket.getTicketID());
								oos.writeObject(ticket);
							} else if (this.msg.getType().equalsIgnoreCase("logout") && this.msg.getText().equalsIgnoreCase("logout")){
								this.status = "logout";
								System.out.println("Logout received: " + msg.getText());
								oos.writeObject(new Message("success", this.status, "CONNECTION CLOSED!"));
							} else {
								System.out.println("Could not capitolize message...");
							}
							break;
							
							
						case "TICKET":
							this.ticket = (Ticket) taggedObject;
							System.out.println("Successfully received ticket request.");
							break;
					}
				}
			} catch (EOFException e) {
				System.out.println("Employee " + employeeID + " disconnected abruptly (EOF).");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Employee " + employeeID + " disconnected: " + e.getMessage());
			} catch (ClassNotFoundException c) {
				c.printStackTrace();
			} finally {
				try {
					System.out.println("Closing socket for client: ID(" + this.employeeID + ") ");
					employeeSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
