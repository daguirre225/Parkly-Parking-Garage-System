package parkly;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.JDialog;
import javax.swing.JFrame; // Use JFrame for the main app window if needed

public class ConnectTask extends SwingWorker<EmployeeConnection, Void> {
	private JFrame mainFrame;
	
	public ConnectTask(JFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	// Called as task.execute from main
	@Override 
	protected EmployeeConnection doInBackground() throws Exception {
		// Run socket creation in background thread
		try {
			// Create socket connection to server
			EmployeeConnection connection = EmployeeService.connect("localhost", 1235);
			return connection;
		} catch (IOException e) {
			throw e;
		}
	}
	
	@Override
	protected void done() {
		// Runs back on Event Dispatch Thread (EDT)!
		try {
			EmployeeConnection socket = get(); // Get result from doInBackground
			new Thread(socket).start(); // Start listener thread for incoming messages from server (this is client side, similar to server side of implementing the run function)
			EmployeeGUI.createEmployeeDashboard(); // Create employee dashboard GUI
		} catch (Exception e) {
			String errorMessage = "Failed to connect to the serve.";
			if (e.getCause() != null) {
				errorMessage += "\nError: " + e.getCause().getMessage();
			} else {
				errorMessage += "\nError: " + e.getMessage();
			}
			
			JOptionPane.showMessageDialog(null,  errorMessage, "Connection Error", JOptionPane.ERROR_MESSAGE);
			
			System.exit(1);
		}
	}
}
