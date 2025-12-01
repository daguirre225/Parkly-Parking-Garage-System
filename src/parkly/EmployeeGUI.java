package parkly;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.*;

public class EmployeeGUI {
	private static JFrame mainFrame;
	private static JTextArea serverTextBox;
	private static JPanel panel;
	private static JPanel displayServerPanel;
	private static JLabel serverTextLabel;
	private static JPanel inputTextPanel;
	private static JLabel inputTextLabel;
	private static JTextArea inputText;
	private static JButton sendButton;
	private static JButton logoutButton;
	private static JPanel imagePanel;
	private static ImageIcon parklyImageIcon;
	private static Image scaledIcon;
	private static JLabel imageIconLabel;
	private static JLabel dateTimeLabel;
	private static Timer timer;
	private static JButton openEntryGateButton;
	private static JButton payFeesButton;
	private static JButton openExitGateButton;
	
	
	static void createEmployeeDashboard() {
		// Main container
		mainFrame = new JFrame("EMPLOYEE DASHBOARD");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(800, 400);
		mainFrame.setPreferredSize(new Dimension(800, 600));
		mainFrame.setLocationRelativeTo(null);
		// Main panel to hold sub panels
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		
//		displayServerPanel = new JPanel(new GridLayout(1, 2)); // panel to show text
		displayServerPanel = new JPanel();
		displayServerPanel.setLayout(new BoxLayout(displayServerPanel, BoxLayout.X_AXIS));
		serverTextLabel = new JLabel("Server: ", JLabel.LEFT);

		String response = "Connection established. Ready to receive server messages...\n";
		serverTextBox = new JTextArea(response, 10, 10);
		serverTextBox.setEditable(false);
		displayServerPanel.add(serverTextLabel);
		displayServerPanel.add(new JScrollPane(serverTextBox));
		
		// User input text panel
//		inputTextPanel = new JPanel(new GridLayout(3, 1)); // panel to send messages
		inputTextPanel = new JPanel();
		inputTextPanel.setLayout(new BoxLayout(inputTextPanel, BoxLayout.X_AXIS));
		inputTextLabel = new JLabel("Enter text: ", JLabel.LEFT);
		inputText = new JTextArea();
		
		// Send button
		sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String inputToSend = inputText.getText();
				if (!inputToSend.isEmpty()) {
				Message msgToSend = new Message("text", "success", inputToSend);
				System.out.println("Message being sent: " + msgToSend.getType() + " | " + msgToSend.getStatus() + " | " + msgToSend.getText());
//				socket.sendMessage(msgToSend);
				appendServerMessage("You: " + inputToSend + "\n");
				EmployeeService.sendMessage(msgToSend);
				inputText.setText("");
				}
			}
		});
		
		// Open entry gate button
		openEntryGateButton = new JButton("Open Entrance Gate");
		openEntryGateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Ticket newTicket = EmployeeService.generateTicket();
				EmployeeService.openEntryGate();
			}
		});
		
		// Calculate fees
		payFeesButton = new JButton("Pay Fees");
		payFeesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FeeGUI feePaymentWindow = new FeeGUI(mainFrame);
				feePaymentWindow.setVisible(true);
			}
		});
		
		// Open exit gate button
		openExitGateButton = new JButton("Open Exit Gate");
		// Logout button
		logoutButton = new JButton("Logout");
		logoutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to logout of dashboard?", "Logout of dashboard?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					EmployeeService.disconnect();
					mainFrame.dispose();
					EmployeeGUI.startLoginAndConnectFlow();
				}
			}
		});
		
		// Close window logs out / disconnects correctly 
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to close the dashboard and disconnect?", "Close Application?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					EmployeeService.disconnect();
					System.exit(0);
				}
			}
		});
		
		
		// display date and time
		dateTimeLabel = new JLabel("Loading Time...", JLabel.CENTER);
		dateTimeLabel.setFont(new Font("Arial", Font.BOLD, 16));
		
		inputTextPanel.add(inputTextLabel);
		inputTextPanel.add(inputText);
		inputTextPanel.add(sendButton);
		
		imagePanel = new JPanel(); // hold image icon label
		parklyImageIcon = new ImageIcon("images/Parkly_Icon.png"); // hold software icon
		scaledIcon = parklyImageIcon.getImage().getScaledInstance(256, 256, Image.SCALE_SMOOTH); // re-scale image icon
		parklyImageIcon = new ImageIcon(scaledIcon); // reset image to re-scaled size
		imageIconLabel = new JLabel(parklyImageIcon);
		
		imagePanel.add(imageIconLabel);
		panel.add(imagePanel);
		panel.add(displayServerPanel);
		panel.add(inputTextPanel);
		panel.add(openEntryGateButton);
		panel.add(payFeesButton);
		panel.add(logoutButton);
		panel.add(dateTimeLabel);
		mainFrame.add(panel);
		mainFrame.pack();
		mainFrame.setVisible(true);
		
		// Action listener to update label
		ActionListener updateTimeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LocalDateTime now = java.time.LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
				String formattedDateTime = now.format(formatter);
				dateTimeLabel.setText(formattedDateTime);
			}
		};
		// Timer to keep track of time
		timer = new Timer(1000, updateTimeAction);
		timer.start();
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
	
