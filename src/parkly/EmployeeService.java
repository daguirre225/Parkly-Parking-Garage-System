package parkly;
import java.io.IOException;
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
			System.err.println("EmployeeService: Not connected.");
		}
	}
	
	public static void loginWindow() {
		
	}
	public static void disconnect() {
		if (socket != null) {
			System.out.println("EmployeeService: Initiating client disconnection.");
			
			socket.logout();
			socket = null;
			System.out.println("EmployeeService: Connection fully closed.");
		}
	}
}
