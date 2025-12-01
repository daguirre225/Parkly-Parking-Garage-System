package parkly;

import java.io.IOException;

import javax.swing.SwingUtilities;
/*
 * Main driver for a client to use the application. Start of Parkly operation. 
 */
public class main {
	public static void main(String[] args) throws IOException {
		// Use SwingUtilities.invokeLater() to ensure that the GUI creation and manipulation 
		// occurs on the Event Dispatch Thread (EDT), which is mandatory for Swing applications.
		SwingUtilities.invokeLater(() -> {
			// Create Login window instance
//			LoginGUI login = new LoginGUI();
			// Display window
//			login.show();
			// After the login window is closed/disposed, we check the outcome. 
			// If login was successful (isAuthenticated() returns true), proceed with application setup.
//			if(login.isAuthenticated()) {
			if (true) {
				System.out.println("Login successful. Starting network connection...");
				// Begin background task of creating a network connection
				ConnectTask task = new ConnectTask(null);
				task.execute();
			} else {
				System.out.println("Login canceled. Exiting.");
				System.exit(0);
			}
		}); 
	}
}
