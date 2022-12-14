package chatserver;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A simple Swing-based client for the chat server. Graphically it is a frame
 * with a text field for entering messages and a textarea to see the whole
 * dialog.
 *
 * The client follows the Chat Protocol which is as follows. When the server
 * sends "SUBMITNAME" the client replies with the desired screen name. The
 * server will keep sending "SUBMITNAME" requests as long as the client submits
 * screen names that are already in use. When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start sending the server
 * arbitrary strings to be broadcast to all chatters connected to the server.
 * When the server sends a line beginning with "MESSAGE " then all characters
 * following this string should be displayed in its message area.
 */
public class ChatClient {

	BufferedReader in;
	PrintWriter out;
	JFrame frame = new JFrame("Chatter");
	JTextField textField = new JTextField(20);
	JTextArea messageArea = new JTextArea(8, 30);
	
	// TODO: Add a list box
	DefaultListModel userListModel = new DefaultListModel();
	JList userList = new JList(userListModel);

	JCheckBox doBroadcast = new JCheckBox("Broadcast");

	/**
	 * Constructs the client by laying out the GUI and registering a listener with
	 * the textfield so that pressing Return in the listener sends the textfield
	 * contents to the server. Note however that the textfield is initially NOT
	 * editable, and only becomes editable AFTER the client receives the
	 * NAMEACCEPTED message from the server.
	 */
	public ChatClient() {

		// Layout GUI
		textField.setEditable(false);
		messageArea.setEditable(false);
		  // TODO: You may have to edit this event handler to handle point to point messaging,
        // where one client can send a message to a specific client. You can add some header to 
        // the message to identify the recipient. You can get the receipient name from the listbox.
		frame.getContentPane().setLayout(new FlowLayout());
		// Add the user list
		frame.getContentPane().add(userList);
		// Add a broadcast check box
		frame.getContentPane().add(doBroadcast);
		frame.getContentPane().add(textField);

		frame.getContentPane().add(new JScrollPane(messageArea), "Center");
		frame.pack();
		frame.setBounds(0, 0, 400, 400);

		textField.addActionListener(new ActionListener() {
			/**
			 * Responds to pressing the enter key in the textfield by sending the contents
			 * of the text field to the server. Then clear the text area in preparation for
			 * the next message.
			 */
			public void actionPerformed(ActionEvent e) {

				// Send message to everyone if broadcast check else send to point to point
				if (doBroadcast.isSelected()) {
					out.println(textField.getText());
				} else {

					for (Object username : userList.getSelectedValuesList()) {
						out.println(username.toString() + ">>" + textField.getText());
					}
				}
				textField.setText("");
			}
		});

	}

	/**
	 * Prompt for and return the address of the server.
	 */
	private String getServerAddress() {
		return JOptionPane.showInputDialog(frame, "Enter IP Address of the Server:", "Welcome to the Chatter",
				JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * Prompt for and return the desired screen name.
	 */
	private String getName() {
		return JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection",
				JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Connects to the server then enters the processing loop.
	 */
	private void run() throws IOException {

		// Make connection and initialize streams
		String serverAddress = "localhost";// getServerAddress();
		Socket socket = new Socket(serverAddress, 9001);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		// Process all messages from server, according to the protocol.

		// TODO: You may have to extend this protocol to achieve task 9 in the lab sheet
		while (true) {
			String line = in.readLine();

			System.out.println(line);

			if (line.startsWith("SUBMITNAME")) {
				String username = getName();
				frame.setTitle(username);
				out.println(username);
			} else if (line.startsWith("NAMEACCEPTED")) {
				textField.setEditable(true);
			} else if (line.startsWith("USERS")) {
				//In the initial connection get the user list
				String[] users = line.substring(6).split(",");
				for (String user : users) {
					userListModel.addElement(user.trim());
				}

			} else if (line.startsWith("USERCONNECTED")) {
				//Adding new users to list when new one connected
				userListModel.addElement(line.substring(14));
			} else if (line.startsWith("MESSAGE")) {
				messageArea.append(line.substring(8) + "\n");
			}
		}
	}

	/**
	 * Runs the client as an application with a closeable frame.
	 */
	public static void main(String[] args) throws Exception {
		ChatClient client = new ChatClient();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.setVisible(true);
		client.run();
	}
}