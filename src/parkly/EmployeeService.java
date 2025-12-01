package parkly;
import java.io.IOException;
import java.time.LocalDateTime;
public class EmployeeService {
	private static EmployeeConnection socket = null;
	
	// Connection logic encapsulation
	public static EmployeeConnection connect(String host, int port) throws IOException {
		System.out.println("EmployeeService: Attempting to connect...");
		// Constructor call to create connection
		socket = new EmployeeConnection(host, port);
		System.out.println("EmployeeService: Connection successful.");
		return socket;
	}
	
	// GUI uses this to send data
	public static void sendMessage(Message msg) {
		if (socket != null) {
			System.out.println("EmployeeService.sendMessage: Sending msg: " + msg.getType() + " | " + msg.getStatus() + " | " + msg.getText());
			socket.sendMessage(msg);
		} else {
			System.err.println("EmployeeService.sendMessage: Not connected.");
		}
	}
	
	public static Message getMessage() {
		if (socket != null) {
			System.out.println("EmployeeService.getMessage: Requesting message");
			return socket.getMessage();
		}
		return null;
	}
	public static Ticket generateTicket() {
		if (socket != null) {
			System.out.println("EmployeeService.generateTicket: Creating new ticket...");
			return socket.generateTicket();
		} else {
			System.err.println("EmployeeService.generateTicket: Not connected.");
		}
		return null;
	}
	
	public static Ticket findTicket(String ticketID) {
		if (socket != null) {
			System.out.println("EmployeeService.findTicket: Finding ticket...");
			Ticket returnedTicket = socket.findTicket(ticketID);
			if (returnedTicket != null) {
				System.out.println("EmployeeService.findTicket: Returning ticket: " + returnedTicket.getTicketID());				
			} else {
				System.out.println("EmployeeService.findTicket: Ticket not found (returning null).");
			}
			return returnedTicket;
		} else {
			System.err.println("EmployeeService.findTicket: Not connected.");
		}
		return null;
	}
	
	public static boolean payTicket(Ticket paidTicket) {
		if (socket != null) {
			System.out.println("EmployeeService.payTicket: Updating ticket to paid.");
			Ticket returnedTicket = socket.payTicket(paidTicket);
			if (returnedTicket != null) {
				System.out.println("EmployeeService.payTicket: Returned ticket: " + returnedTicket.getTicketID());
			} else {
				System.out.println("EmployeeService.payTicket: Ticket not found (returning false).");
			}
			return true;
		} else {
			System.err.println("EmployeeService.payTicket: Not connected.");
			return false;
		}
	}
	public static String openEntryGate() {
		if (socket != null) {
			System.out.println("EmployeeService.openEntryGate: Sending open entry gate message...");
			return socket.openEntryGate();
		} else {
			System.err.println("EmployeeService.openEntryGate: Not connected.");
			return "EmployeeService: Not connected.";
		}
	}
	
	public static void disconnect() {
		if (socket != null) {
			System.out.println("EmployeeService.disconnect: Initiating client disconnection.");
			
			socket.logout();
			socket = null;
			System.out.println("EmployeeService.disconnect: Connection fully closed.");
		}
	}
}
