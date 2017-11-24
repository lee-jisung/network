package myPackage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 * 
 * 
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.  The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all
 * chatters connected to the server.  When the server sends a
 * line beginning with "MESSAGE " then all characters following
 * this string should be displayed in its message area.
 */
public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter & Whisper"); 
    JTextField textField = new JTextField(40);  
    JTextArea messageArea = new JTextArea(8, 40);  
    JTextArea userlist = new JTextArea(10, 10);     // Current users are displayed

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    public ChatClient() {

        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        userlist.setEditable(false);
        frame.getContentPane().add(textField, "North"); 
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");  
        frame.getContentPane().add(new JScrollPane(userlist), "East");     // for user list 
        frame.pack(); 

        // Add Listeners
        textField.addActionListener(new ActionListener() { 
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server.    Then clear
             * the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {  
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(  
            frame,
            "Enter IP Address of the Server:",   
            "Welcome to the Chatter",      
            JOptionPane.QUESTION_MESSAGE);  
    }

    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        while (true) {
            String line = in.readLine(); 
            if (line.startsWith("SUBMITNAME")) {   
                out.println(getName());   
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);  
            } else if (line.startsWith("MESSAGE")) { 
               messageArea.append(line.substring(8) + "\n"); 
            } 
              else if(line.startsWith("USER")){                // if message starts with user, then 
            	userlist.append(line.substring(5) + "\n");     // add user name to the user list
            } 
              else if(line.startsWith("EXIT")){                // if message starts with exit, then user list 
            	userlist.setText("");                          // reset except client that terminated
            	messageArea.append(line.substring(5) + "\n");  // inform that client is exit
            } 
              else if(line.startsWith("ENTER")){               // inform that client is enter
            	messageArea.append(line.substring(6) + "\n");
            } 
        }
    }
    
    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 메인프레임 닫으면 프로그램 안정적 종료
        client.frame.setVisible(true);	
        client.run();
        
    }
}
