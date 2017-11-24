package myPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * A multithreaded chat room server.  When a client connects the
 * server requests a screen name by sending the client the
 * text "SUBMITNAME", and keeps requesting a name until
 * a unique one is received.  After a client submits a unique
 * name, the server acknowledges with "NAMEACCEPTED".  Then
 * all messages from that client will be broadcast to all other
 * clients that have submitted a unique screen name.  The
 * broadcast messages are prefixed with "MESSAGE ".
 *
 * Because this is just a teaching example to illustrate a simple
 * chat server, there are a few features that have been left out.
 * Two are very useful and belong in production code:
 *
 *     1. The protocol should be enhanced so that the client can
 *        send clean disconnect messages to the server.
 *
 *     2. The server should do some logging.
 */
public class ChatServer {

    /**
     * The port that the server listens on.
     */
    private static final int PORT = 9001;

    /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    /**
     * The map of all the print writers and names. 
     * This map makes easier to find target of whisper
     */
    private static HashMap<String, PrintWriter> whispermap = new HashMap<String, PrintWriter>();
    
    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
        
    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages. 
     */
    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
		public void run() {
            try {

                // Create character streams for the socket. 
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITNAME"); 
                    name = in.readLine();       
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {     
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }
                
                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED"); 
                writers.add(out); 
                
                // Put the name and print writer to the map 
                // so this map used for find target of whisper
                whispermap.put(name,out);
                
                // If new client enter the chat room, server send to this client all user list
                // and other client notice that new client entrance by message
                // Add new client's name to user list in other client's list
               for(PrintWriter writer: writers){
            	   if(writer.equals((PrintWriter)this.out)){
            		   for(String user : names){
            			   writer.println("USER " + user);
            		   }
            	   } else{
            		   writer.println("USER " + name);
            		   writer.println("ENTER " + "< " + name + " > 님이 입장하셨습니다.");
            	   } 
               }

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                //  If message starts with /w, then this means that client want to whisper to other.
                // And then, finding target in the map and send message to the target client
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    else if(input.startsWith("/w")) {
                    	input = input.substring(3);
                    	String ID = input.substring(0, input.indexOf(" "));
                    	String msg = input.substring(input.indexOf(" ")+1);
                    	PrintWriter whisper;
                    	this.out.println("MESSAGE " + ID + "에게 귓말: " + msg);
                    	whisper = whispermap.get(ID);
                        whisper.println("MESSAGE " + name +"로부터 귓말: " + msg);
                    }
                    else {
                    	for (PrintWriter writer : writers) {   
                    	writer.println("MESSAGE " + name + ": " + input);
                      }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {  
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
            	// Notice to other clients by message that this client is going down
            	// Remove its name and print writer from map. 
                if (name != null) {
                	
                	names.remove(name);
                	for(PrintWriter writer : writers){
                		writer.println("EXIT " + "< " + name + " > 님이 퇴장하셨습니다.");
                		for(String user : names){
                			writer.println("USER " + user);
                		}
                	}
                }
                if (out != null) {
                    writers.remove(out);
                    whispermap.remove(name,out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
