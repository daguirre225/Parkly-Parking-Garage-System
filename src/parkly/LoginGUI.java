package parkly;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


public class LoginGUI {
	private final static String title = "Parkly Employee GUI";
	private final JFrame mainFrame;
	private final JDialog loginScreen;
	private final JTextField usernameField;
	private final JPasswordField passwordField;
	private final JButton loginButton;
	private final JButton cancelButton;
	private static final Map<String, char[]> VALID_USERS = new HashMap<>();
	private static final String RESOURCE_PATH = "valid_users.txt";
	private boolean validUser = false;
	
	// Initiate 1 instance of file reading
	static {
		try {
			// Create InputStream to read file
			InputStream inputStream = LoginGUI.class.getClassLoader().getResourceAsStream(RESOURCE_PATH);
			if (inputStream == null) {
				throw new FileNotFoundException("Resource not found: " + RESOURCE_PATH);
			}
			// Attempt to read the file
			try (Scanner scanner = new Scanner(inputStream)) {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					if (line.trim().isEmpty() || line.trim().startsWith("#")) {
						continue;
					}
					String[] parts = line.split(",",2);
					if (parts.length == 2) {
						String username = parts[0].trim();
						String password = parts[1].trim();
						VALID_USERS.put(username, password.toCharArray());
					}
				}
				System.out.println("Loaded " + VALID_USERS.size() + " users for authentication.");
			}
		} catch (Exception e) {
			System.err.println("FATAL: Failed to load user data for authentication.");
			e.printStackTrace();
		}
	}
	
	// Constructor
	LoginGUI() {
		this.mainFrame = new JFrame(title);
		this.loginScreen = new JDialog((JFrame)null, "Employee Login", true);
		this.loginScreen.setLayout(new GridLayout(4, 2, 5, 5));
		this.usernameField = new JTextField(15);
		this.passwordField = new JPasswordField(15);
		this.loginButton = new JButton("Login");
		this.cancelButton = new JButton("Close");
		this.loginScreen.add(new JLabel("Username: ", JLabel.RIGHT));
		this.loginScreen.add(this.usernameField);
		this.loginScreen.add(new JLabel("Password: ", JLabel.RIGHT));
		this.loginScreen.add(this.passwordField);
		this.loginScreen.add(this.loginButton);
		this.loginScreen.add(this.cancelButton);
		
		this.validUser = false;
		// Use button to run user input through validation
		this.loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = usernameField.getText();
				char[] password = passwordField.getPassword();
				validUser = validateUsers(username, password);
				if (validUser) {	
					loginScreen.dispose();
				} else {
					JOptionPane.showMessageDialog(loginScreen, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
				}
				Arrays.fill(password, ' ');
			}
		});
		
		this.cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(mainFrame, "Goodbye!");
				System.exit(0);
			}
		});	
		
	}
	
	// Validation function to approve users
	private boolean validateUsers(String username, char[] password) {
		if (VALID_USERS.containsKey(username)) {
			char[] storedPassword = VALID_USERS.get(username);
			boolean match = Arrays.equals(storedPassword, password);
			Arrays.fill(password, ' ');
			return match;
		}
		Arrays.fill(password, ' ');
		return false;
	}
	
	// Return boolean value for inputs
	public boolean isAuthenticated() {
		return this.validUser;
	}
	
	// Display login window to user upon running application code
	public void show() {
		this.loginScreen.pack();
		this.loginScreen.setLocationRelativeTo(null);
		this.loginScreen.setVisible(true);
	}
}