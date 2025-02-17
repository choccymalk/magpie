package apcsa.lab3;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import java.awt.Container;
import java.awt.BorderLayout;
import javax.swing.JButton; 
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout; 
import javax.swing.Box; 
import javax.swing.border.EmptyBorder; 
import java.awt.Insets; 
import java.awt.Dimension; 
public class UI extends JPanel implements ActionListener {
    static String html = "<html><body style='width: %1spx'>%1s";
    
    // JTextField
    static JTextField textField;
 
    // JFrame
    static JFrame frame;
 
    // JButton
    static JButton button;
    // JPanel
    static JPanel p;
    static int yPos = 0;
    // default constructor
    UI()
    {
    }
 
    // main class
    public void main(String[] args)
    {
        
        // create a new frame to store text field and button
        frame = new JFrame("Magpie");
        JScrollBar scrollbar = new JScrollBar();
        scrollbar.setPreferredSize(new Dimension(5, 400));
        frame.add(scrollbar);
        frame.pack();

        // create a new button
        button = new JButton("submit");
 
        // create a object of the text class
        UI te = new UI();
 
        // addActionListener to button
        button.addActionListener(te);
 
        // create a object of JTextField with 16 columns
        textField = new JTextField(16);

        // create a panel to add buttons and textfield
        p = new JPanel();

        //BoxLayout boxlayout = new BoxLayout(p, BoxLayout.Y_AXIS); 
  
        // to set the box layout 
        //p.setLayout(boxlayout); 
        
        p.setBorder(new EmptyBorder(new Insets(0, 150, 600, 150))); 
        textField.setMaximumSize(new Dimension(500, 50));
        setLayout(new BorderLayout());
        // add buttons and textfield to panel
        //label.setLocation(100,100);
        p.add(textField, BorderLayout.NORTH);
        p.add(button, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // add panel to frame
        frame.add(p);
 
        // set the size of frame
        frame.setSize(600, 600);
 
        frame.show();
    }
    public static void appendTextToLabel(String text) {
        //label.setText("<html><br>" + label.getText() + "<br>" + text + "</html>");
        //JPanel newPanel = new JPanel();
        String textFormatted = String.format(html, 400, text);
        JLabel newLabel = new JLabel(textFormatted);
        newLabel.setMaximumSize(new Dimension(150, 50));
        yPos += 20;
        p.setLocation(0, yPos);
        p.add(newLabel);
        frame.revalidate();
    }
    // if the button is pressed
    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();
        if (s.equals("submit")) {
            /*// set the text of the label to the text of the field
            label.setText(textField.getText());
            */
            // set the text of field to blank
            //textField.setText("");
            String message = textField.getText();
            if (message.equalsIgnoreCase("exit"))
                System.exit(0);
                Magpie.history.add("{\"role\":\"user\",\"content\":\"" + message + "\"}");
            try {
                appendTextToLabel("You: " + message);
                Magpie.sendMessage();
            } catch (Exception except) {
                System.out.println("Error communicating with the server: " + except.getMessage());
            }
        }
    }
}
