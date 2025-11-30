package parkly;

import java.io.IOException;

import javax.swing.SwingUtilities;

public class main {
	public static void main(String[] args) throws IOException {
		SwingUtilities.invokeLater(() -> {
			// Create Login window instance
			LoginGUI login = new LoginGUI();
			// Display window
			login.show();
			// Once window is disposed (successful login or exit out of window) we execute accordingly
			if(login.isAuthenticated()) {
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
