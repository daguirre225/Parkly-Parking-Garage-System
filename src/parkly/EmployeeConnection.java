package parkly;

import java.io.*;
import java.net.*;
import java.util.Scanner;


public class EmployeeConnection implements Runnable {
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private Message msg;
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
				this.msg = (Message) ois.readObject();
				String receivedText = this.msg.getText();
				
				System.out.println("Returned message: " + receivedText);
				EmployeeGUI.appendServerMessage("Server: " + receivedText + "\n");
//				if (type.equalsIgnoreCase("text") && !input.equalsIgnoreCase("logout")) {
//					oos.writeObject(new Message(type, status, input));
//					msg = (Message) ois.readObject();
//					status = msg.getStatus();
//				} else if (input.equalsIgnoreCase("logout") || text.equalsIgnoreCase("logout")) {
//					oos.writeObject(new Message("logout", status, "logout"));
//					msg = (Message) ois.readObject();
//					status = msg.getStatus();
//					break;
//				} else {
//					System.out.println("Protocol error: Unexpected message type...");
//					break;
//				}
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


