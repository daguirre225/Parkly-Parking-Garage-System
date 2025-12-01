package parkly;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.JDialog;
import javax.swing.JFrame; // Use JFrame for the main app window if needed


// Allows us to create tasks that invoke the creation of the network connection, create a Thread to handle listening of incoming messages from the server.
// We extend SwingWorker to be able to update the Event Dispatch Thread / GUI connection to the server
public class ConnectTask extends SwingWorker<EmployeeConnection, Void> {
	private JFrame mainFrame;
	
	public ConnectTask(JFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	// Called as task.execute from main
	@Override 
	protected EmployeeConnection doInBackground() throws Exception {
		// Executes on a separate Worker Thread, NOT the EDT.
		// Run socket creation in background thread to prevent freezing the GUI.
		try {
			// Create socket connection to server via connected network
			EmployeeConnection connection = EmployeeService.connect("10.0.0.114", 1235); // create an EmployeeConnection object that will hold Socket + Socket to server operations
			System.out.println("ConnectTask.doInBackground:\n\tSuccess creating EmployeeConnection connection.");
			return connection; // Send established connection to done()
		} catch (IOException e) {
			throw e;
		}
	}
	// After doInBackground() is ran, done() runs automatically afterwards
	@Override
	protected void done() {
		// Executes back on the EDT, All GUI updates must happen here
		try {
			// Get result from doInBackground (EmployeeConnection object) from doInBackground(). 
			// This call will re-throw any exceptions caught during the background task.
			EmployeeConnection socket = get();
			// Start listener thread for incoming messages from server
			// a continuous, blocking operation that must NOT run on the EDT or the SwingWorder thread.
			new Thread(socket).start(); 
			EmployeeGUI.createEmployeeDashboard(); // Create employee dashboard GUI on the safe EDT
			System.out.println("ConnectTask.done:\n\tSuccess creating listening thread.");
		} catch (Exception e) {
			String errorMessage = "Failed to connect to the serve.";
			if (e.getCause() != null) {
				errorMessage += "\nError: " + e.getCause().getMessage();
			} else {
				errorMessage += "\nError: " + e.getMessage();
			}
			// The JOptionPane call is safe here because done() runs on the EDT.
			JOptionPane.showMessageDialog(null,  errorMessage, "Connection Error", JOptionPane.ERROR_MESSAGE);
			
			System.exit(1);
		}
	}
}
