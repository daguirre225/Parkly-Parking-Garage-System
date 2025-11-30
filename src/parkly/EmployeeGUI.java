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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.*;

public class EmployeeGUI {
	private static boolean allowLogin = false;
//	private static ObjectOutputStream oos;
//	private static ObjectInputStream ois;
	private static JTextArea messageArea;
	private static JFrame dashboardFrame;
	private static boolean connected = false;
	private static EmployeeConnection socket;
	private static JTextArea serverTextBox;
	private static Map<String, String> VALID_USERS = null;
	private static String RESOURCE_PATH = null;

	
	static void createEmployeeDashboard() {
		// Main container
		JFrame frame = new JFrame("EMPLOYEE DASHBOARD");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 400);
		frame.setPreferredSize(new Dimension(800, 600));
		frame.setLocationRelativeTo(null);
		// Main panel to hold sub panels
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		
		JPanel displayServerPanel = new JPanel(new GridLayout(1, 2)); // panel to show text
		JLabel serverTextLabel = new JLabel("Server: ", JLabel.LEFT);
//		String response = socket.getMessage().getText();
		String response = "Connection established. Ready to receive server messages...\n";
		serverTextBox = new JTextArea(response, 10, 10);
		serverTextBox.setEditable(false);
		displayServerPanel.add(serverTextLabel);
		displayServerPanel.add(new JScrollPane(serverTextBox));
		
		JPanel enterTextPanel = new JPanel(new GridLayout(3, 1)); // panel to send messages
		JLabel inputTextLabel = new JLabel("Enter text: ", JLabel.LEFT);
		JTextArea inputText = new JTextArea();
		String input = inputText.getText();
		
		// Send button
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String inputToSend = inputText.getText();
				if (!inputToSend.isEmpty()) {
//				socket.sendMessage(new Message("text", "success", inputToSend));
				Message msgToSend = new Message("text", "success", inputToSend);
				System.out.println("Message being sent: " + msgToSend.getType() + " | " + msgToSend.getStatus() + " | " + msgToSend.getText());
//				socket.sendMessage(msgToSend);
				appendServerMessage("You: " + inputToSend + "\n");
				EmployeeService.sendMessage(msgToSend);
				inputText.setText("");
				}
			}
		});
		
		// Logout button
		JButton logoutButton = new JButton("Logout");
		logoutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to logout of dashboard?", "Logout of dashboard?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					EmployeeService.disconnect();
					frame.dispose();
					EmployeeGUI.startLoginAndConnectFlow();
				}
			}
		});
		
		// Close window logs out / disconnects correctly 
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to close the dashboard and disconnect?", "Close Application?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					EmployeeService.disconnect();
					System.exit(0);
				}
			}
		});
		
		enterTextPanel.add(inputTextLabel);
		enterTextPanel.add(inputText);
		enterTextPanel.add(sendButton);
		
		JPanel imagePanel = new JPanel(); // hold image icon label
		ImageIcon parklyImageIcon = new ImageIcon("images/Parkly_Icon.png"); // hold software icon
		Image scaledIcon = parklyImageIcon.getImage().getScaledInstance(256, 256, Image.SCALE_SMOOTH); // re-scale image icon
		parklyImageIcon = new ImageIcon(scaledIcon); // reset image to re-scaled size
		JLabel imageIconLabel = new JLabel(parklyImageIcon);
		
		imagePanel.add(imageIconLabel);
		panel.add(imagePanel);
		panel.add(displayServerPanel);
		panel.add(enterTextPanel);
		panel.add(logoutButton);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}

	public static void appendServerMessage(String message) {
        // Ensure this update runs on the EDT
        SwingUtilities.invokeLater(() -> {
            if (serverTextBox != null) {
                serverTextBox.append(message + "\n");
            }
        });
    }
	
	public static void startLoginAndConnectFlow() {
		LoginGUI newLogin = new LoginGUI();
		newLogin.show();
		if (newLogin.isAuthenticated()) {
			System.out.println("Login successful after logout. Attempting reconnection...");
			ConnectTask newTask = new ConnectTask(null);
			newTask.execute();
		} else {
			System.out.println("Second login failed or cancelled. Exiting application.");
			System.exit(0);
		}
	}
}
	
