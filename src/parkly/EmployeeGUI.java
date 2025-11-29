package parkly;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import java.io.*;

public class EmployeeGUI {
	private static boolean allowLogin = false;
	private static ObjectOutputStream oos;
	private static ObjectInputStream ois;
	private static JTextArea messageArea;
	private static JFrame dashboardFrame;
	private static boolean connected = false;
	private static EmployeeConnection socket;
	
	
	public static void main(String[] args) {
		
		loginGUI();
	}
	
	private static void loginGUI() {
		String title = "Parkly Employee GUI";
		final String[] validUsernames = {"employee", "manager"};
		final char[] validPassword = {'p', 'a', 's', 's'};
		
		
		
		JFrame mainFrame = new JFrame(title);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Login GUI
		JDialog loginScreen = new JDialog((JFrame)null, "Employee Login", true); // (JFrame)null means no parent
		loginScreen.setLayout(new GridLayout(4, 2, 5, 5));
		
		JTextField usernameField = new JTextField(15);
		JPasswordField passwordField = new JPasswordField(15);

		JButton loginButton = new JButton("Login");
		JButton cancelButton = new JButton("Close");
		
		
		loginScreen.add(new JLabel("Username: ", JLabel.RIGHT));
		loginScreen.add(usernameField);
		loginScreen.add(new JLabel("Password: ", JLabel.RIGHT));
		loginScreen.add(passwordField);
		loginScreen.add(loginButton);
		loginScreen.add(cancelButton);
		
		// Action listeners for Login Screen
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = usernameField.getText();
				char[] password = passwordField.getPassword();
				// Authenticate
				boolean authUsername = Arrays.stream(validUsernames).anyMatch(element -> element.equals(username));
				boolean authPassword = Arrays.equals(password, validPassword);
				if (/*authUsername && authPassword*/ true) {
					allowLogin = true;
					loginScreen.dispose();
					initializeNetworkConnection();
					if (connected) {
						createEmployeeDashboard();
					} else {
						JOptionPane.showMessageDialog(null, "Could not connect to server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
					}
//					initializeNetworkConnection();
//					createEmployeeDashboard(); // Then create GUI
				} else {
					JOptionPane.showMessageDialog(loginScreen, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
				}
				Arrays.fill(password, ' ');
			}
		});
	
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(mainFrame, "Goodbye!");
				System.exit(0);
			}
		});
		loginScreen.pack();
		loginScreen.setLocationRelativeTo(null);
		loginScreen.setVisible(true);
	}
	
	private static void createEmployeeDashboard() {
		// Main container
		JFrame frame = new JFrame("EMPLOYEE DASHBOARD");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 400);
		frame.setPreferredSize(new Dimension(1200, 300));
		frame.setLocationRelativeTo(null);
		// Main panel to hold sub panels
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		// Sub Panel to hold title, user login, or quit
		JPanel displayServerPanel = new JPanel(new GridLayout(2, 1)); // panel to show text
		JLabel serverTextLabel = new JLabel("Server: ", JLabel.LEFT);
		String response = socket.getMessage().getText();
		JTextArea serverResponse = new JTextArea(response, 20, 30);
		displayServerPanel.add(serverTextLabel);
		displayServerPanel.add(serverResponse);
		
		JPanel enterTextPanel = new JPanel(new GridLayout(2, 1)); // panel to send messages
		JLabel inputTextLabel = new JLabel("Enter text: ", JLabel.RIGHT);
		JTextArea inputText = new JTextArea();
		String input = inputText.getText();
		enterTextPanel.add(inputTextLabel);
		enterTextPanel.add(inputText);
		
		
		JPanel imagePanel = new JPanel(); // hold image icon label
		ImageIcon parklyImageIcon = new ImageIcon("images/Parkly_Icon.png"); // hold software icon
		Image scaledIcon = parklyImageIcon.getImage().getScaledInstance(256, 256, Image.SCALE_SMOOTH); // re-scale image icon
		parklyImageIcon = new ImageIcon(scaledIcon); // reset image to re-scaled size
		JLabel imageIconLabel = new JLabel(parklyImageIcon);
		
		imagePanel.add(imageIconLabel);
		panel.add(imagePanel);
		panel.add(displayServerPanel);
		panel.add(enterTextPanel);
		frame.add(panel);
		frame.setVisible(true);
	}
	
	private static void initializeNetworkConnection() { 
		try {
			socket = new EmployeeConnection("localhost", 1235);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
	
