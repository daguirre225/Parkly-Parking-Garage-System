package parkly;
import javax.swing.*;
import javax.swing.JDialog;
import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FeeGUI extends JDialog {
	private static JPanel mainPanel;
	private static JLabel searchTicketLabel;
	private static JTextField searchTicketText;
	private static JButton searchTicketButton;
	private Ticket ticket;
	
	public FeeGUI(JFrame owner) {
		super(owner, "Pay Parking Fees", ModalityType.APPLICATION_MODAL);
		this.setSize(400, 300);
		this.setLocationRelativeTo(owner);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		
		searchTicketLabel = new JLabel("Enter ticket ID: ");
		searchTicketText = new JTextField(10);
		searchTicketButton = new JButton("Search Ticket");
		
		mainPanel.add(searchTicketLabel);
		mainPanel.add(searchTicketText);
		mainPanel.add(searchTicketButton);
		
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		this.pack();
		
		searchTicketButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String searchTicketInput = searchTicketText.getText();
				if (searchTicketInput != null) {
					searchTicket(searchTicketInput);
				}
			}
		});
	}
	
	private Ticket searchTicket(String input) {
		return ticket;
	}
	
	
	
}
