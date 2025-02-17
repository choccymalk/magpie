package apcsa.lab3;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class UI extends JPanel implements ActionListener {
    static String html = "<html><body style='width: %1spx'>%1s";

    // JTextField
    static JTextField textField;

    // JFrame
    static JFrame frame;

    // JButton
    static JButton button;
    //.JPanel
    static JPanel p;
    static JPanel messagePanel;  // Panel to hold all message labels
    static JScrollPane scrollPane;
    static int yPos = 0;

    // default constructor
    UI() {
    }

    public void main(String[] args) {
        frame = new JFrame("Magpie");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create panels
        p = new JPanel();
        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));

        // Create components
        button = new JButton("submit");
        textField = new JTextField(16);

        // Setup scroll pane
        scrollPane = new JScrollPane(messagePanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(600, 500));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        p.setLayout(new BorderLayout());
        p.add(textField, BorderLayout.NORTH);
        p.add(button, BorderLayout.NORTH);
        p.add(scrollPane, BorderLayout.CENTER);

        // Set preferred size for better initial display
        scrollPane.setPreferredSize(new Dimension(500, 300));
        messagePanel.setOpaque(true);
        // Add components to main panel (p)
        p.setLayout(new BorderLayout());
        p.add(textField, BorderLayout.NORTH);
        p.add(button, BorderLayout.NORTH);
        p.add(scrollPane, BorderLayout.CENTER);
        textField.setPreferredSize(new Dimension(200, 30));
        button.setPreferredSize(new Dimension(100, 30));
        // Add action listener to button
        button.addActionListener(this);
        p.setLayout(new FlowLayout());
        scrollPane.setPreferredSize(new Dimension(500, 500));
        // Set frame properties
        frame.setContentPane(p);
        p.setPreferredSize(new Dimension(600, 600));
        frame.pack();
        frame.setVisible(true);

        // Initial setup - add empty label to ensure proper scrolling behavior
        JLabel emptyLabel = new JLabel("");
        messagePanel.add(emptyLabel);
    }

    public static void appendTextToLabel(String text) {
        String textFormatted = String.format(html, 600, text);
        JLabel newLabel = new JLabel(textFormatted);
        newLabel.setOpaque(true);
        messagePanel.add(newLabel);
        messagePanel.updateUI();
    }

    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("submit")) {
            String message = textField.getText().trim();
            if (!message.isEmpty()) {
                Magpie.history.add("{\"role\":\"user\",\"content\":\"" + message + "\"}");
                try {
                    appendTextToLabel("You: " + message);
                    Magpie.sendMessage();
                    textField.setText("");
                } catch (Exception ex) {
                    System.out.println("Error communicating with the server: " + ex.getMessage());
                }
            }
        }
    }
}