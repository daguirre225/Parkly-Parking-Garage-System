package parkly;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
	private static int employeeCount; // keep track of how many employee 
	private static List<Ticket> activeTickets = new ArrayList<>();
	private static SpaceTracker spaceTracker = new SpaceTracker(50); 
	 
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
		private Gate gate;
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
							System.out.println("Server.run (MESSAGE): Successfully received message. \n\tTYPE: " + this.msg.getType() + "\n\tSTATUS: " + this.msg.getStatus() + "\n\tTEXT: " + this.msg.getText());
							if (this.msg.getType().equalsIgnoreCase("text") && this.msg.getStatus().equalsIgnoreCase("success")) {
								oos.writeObject(new Message(this.msg.getType(), this.msg.getStatus(), this.msg.getText().toUpperCase().trim()));
								oos.flush();
							
								// NEW TICKET
							} else if (this.msg.getType().equalsIgnoreCase("TICKET") && this.msg.getText().equalsIgnoreCase("GENERATE NEW TICKET")) {
								
								// update occupancy
								spaceTracker.increment(); 
								if (spaceTracker.isFull()) {
								    System.out.println("Server.run (SPACE): LOT IS FULL!");
								}

								this.ticket = new Ticket();
								System.out.println("Server.run (NEW TICKET):\n\tNew ticketID created: " + this.ticket.getTicketID());
								activeTickets.add(this.ticket);
								
								System.out.println("Server.run (SPACE): Current spaces used: "
							            + spaceTracker.getCurrentCount() + " / " + spaceTracker.getCapacity()
							            + " (available: " + spaceTracker.getAvailable() + ")");
								oos.reset();
								oos.writeObject(this.ticket);
								oos.flush();
								// FIND TICKET
							} else if (this.msg.getType().equalsIgnoreCase("FIND TICKET")) {
								System.out.println("Server.run (FIND TICKET): Searching for ticket: " + this.msg.getText());
								String ticketID = this.msg.getText();
								this.ticket = findTicket(ticketID);
								if (this.ticket != null) { 
									if (this.ticket.getTicketID().equalsIgnoreCase(this.msg.getText())) {
										this.ticket.setExitStamp(); // close ticket timing, set exit timing, calc total time + fees due;
										oos.reset();
										System.out.println("\tFOUND TICKET: " + this.ticket.getTicketID());
										System.out.println("\tTicket Data:\n\tENTRY TIME: " + this.ticket.getEntryTime() + "\n\tEXIT TIME: " + this.ticket.getExitTime() + "\n\tFEES DUE: " + this.ticket.getTotalFees());
										oos.writeObject(this.ticket);
										oos.flush();
									}
								}
							} else if (this.msg.getType().equalsIgnoreCase("PAY TICKET")) {
								this.ticket = findTicket(this.msg.getText());
								if (this.ticket != null) {
									if (this.ticket.getTicketID().equalsIgnoreCase(this.msg.getText())) {
										this.ticket.markPaid();
										spaceTracker.decrement();
										oos.reset();
										System.out.println("Server.run (PAY TICKET): Ticket " + this.ticket.getTicketID() + " has been paid: " + this.ticket.isPaid());
										System.out.println("Server.run (SPACE): Current spaces used: "
								                    + spaceTracker.getCurrentCount() + " / " + spaceTracker.getCapacity()
								                    + " (available: " + spaceTracker.getAvailable() + ")");
										oos.reset();
										oos.writeObject(this.ticket);
										oos.flush();
									}
								}
								
							} else if (this.msg.getType().equalsIgnoreCase("GATE") && this.msg.getText().equalsIgnoreCase("OPEN ENTRY GATE")) {
								System.out.println("Server.run (OPEN ENTRY GATE):\n\tOpening front gate...");
								
							 // Generate Report 
							} else if (this.msg.getType().equalsIgnoreCase("REPORT")) {

						        String reportDate = this.msg.getText(); // e.g. "12/1/2025"
						        System.out.println("Server.run (REPORT): Generating report for " + reportDate);

						        java.util.List<Ticket> reportList = new java.util.ArrayList<>();
						        for (Ticket t : activeTickets) {
						            if (t.getExitDate() != null
						                    && !t.getExitDate().isEmpty()
						                    && t.getExitDate().equals(reportDate)) {
						                reportList.add(t);
						            }
						        }

						        Report report = new Report(reportDate, reportList);
						       
						        
						        oos.reset();
						        oos.writeObject(report);
						        oos.flush();
						    }
							else if (this.msg.getType().equalsIgnoreCase("logout") && this.msg.getText().equalsIgnoreCase("logout")){
								this.status = "logout";
								System.out.println("Server.run (LOGOUT):\n\tLogout received: " + msg.getText());
								oos.writeObject(new Message("success", this.status, "CONNECTION CLOSED!"));
								oos.flush();
						        
						    } else {
								System.out.println("Could not capitolize message...");
								oos.writeObject(new Message("text", "success", "Failed."));
								oos.flush();
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
		
		public Ticket findTicket(String ticketID) {
			for (Ticket ticket : activeTickets) {
				if (ticket instanceof Ticket) {
					Ticket foundTicket = (Ticket) ticket;
					if (foundTicket.getTicketID().equals(ticketID)) {
						return foundTicket;
					}
				}
			}
			System.out.println("Could not find ticket.");
			return null;
		}
	}
}
