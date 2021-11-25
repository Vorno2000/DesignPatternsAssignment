package processapplication;
import java.io.BufferedReader;
import java.util.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Clients {
	
    private static final String HOSTNAME = "localhost";
    private static final int HOSTPORT = 2020; // Assigned port number for any client which joins
    private int userID; // unique int ID for a client server adds the id
    private Socket socket;
    private BufferedReader br; // For input stream
    private PrintWriter pw; // For output stream
    private Receive r;
    private Send s;
    private static final Object lock = new Object();
	
    public Clients() {}
	
    public Socket getSocket() {
        return socket;
    }
    /* Gets User int ID
    This ensures that the correct answer 
    is given back to the correct client 
    */
    public int getUser(){
        return userID;
    }

    /* Sets User int ID
    This ensures that the correct answer 
    is given back to the correct client 
    */
    public void setUser(int userID){
        this.userID = userID;
    }

    /**
    * Method to prompt user for input, try connect with the server, and 
    * sends user input through
    */  
    public void startClient() {
        Scanner key = new Scanner(System.in);
        
        String userInput = "";
        boolean valid = false;
        System.out.println("What Shape would you like to measure (Square, Triangle, Pentagon): ");
        
        while(!valid) {
            userInput = key.nextLine().toLowerCase();
            
            switch(userInput) {
                case "square":
                    valid = true;
                    break;
                case "triangle":
                    valid = true;
                    break;
                case "pentagon":
                    valid = true;
                    break;
                default:
                    System.out.println("Please select between Square, Triangle or Pentagon: ");
            }
        }
        
        userInput += ",";
        System.out.println("Please enter the length of one side (cm): ");
        double tempInput;
        
        while(true) {
            try {
                tempInput = key.nextDouble();
                break;
            } catch (InputMismatchException e) {
                System.out.println("Please enter a number (decimals optional): ");
                key.nextLine();
            }
        }
        
        userInput += tempInput;
        
        key.close();
        
        try {
                socket = new Socket(HOSTNAME, HOSTPORT);
                System.out.println("Connected to Server successfully:");
                pw = new PrintWriter(socket.getOutputStream(), true);
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
                System.err.println("Cannot connect to socket: "+e);
        }
        
        r = new Receive(socket, this, lock);
        Thread t = new Thread(r);
        t.start();
        
        s = new Send(socket);
        Thread t2 = new Thread(s);
        t2.start();
        
        s.sendInput(userInput);
    }
    
    /* A method to end the client's connection with the server*/
    public void endConnection() {
        try {
            r.endThread();
            s.endThread();
            socket.close();
            pw.close();
            br.close();
            System.exit(0);
        } catch (IOException ex) {}
    }
    /**
     * A method to display the results of the servers calculations from the input 
     * 
     * @param task	the Task object with answers to the calculations to be displayed
     */
    public void displayProcessedTask(Task task) {
        System.out.println("Shape One Side: "+task.getShapeOneSide());
        System.out.println("Calculated Perimeter: "+task.getShapePerimeter()+"(cm)");
        System.out.println("Calculated Area: "+task.getShapeArea()+"(cm^2)");
    }

	 /* Runs client class for user interaction */
    public static void main(String[] args) {
        Clients client = new Clients();
        client.startClient();
    }
	
	/* Client/server connection */
    public class Receive implements Runnable {
        private static final int SOCKETPORT = 2021;
        private ServerSocket serverSocket;
        private Socket socket;
        private ObjectInputStream ois;
        private boolean finished;
        private Clients client;
        private Task processedTask;
        private final Object lock;
        
        /**
         * Constructor for Receive which will keep reference to the socket and client
         * 
         * @param socket	The socket object for the client's connection to the server
         * @param client	 
         */
        public Receive( Socket socket, Clients client, Object lock) {
                this.socket = socket;
                this.client = client;
                finished = false;
                this.lock = lock;
        }

        public void endThread() {
            try {
                finished = true;
                serverSocket.close();
                socket.close();
            } catch (IOException ex) {}
        }
        
        public void closeClient() {
            client.endConnection();
        }

        
        @Override
        public void run() {
            while(true) {
                try {
                    synchronized(lock) {
                        serverSocket = new ServerSocket(SOCKETPORT);
                        System.out.println("Client-Side Server has started at "+InetAddress.getLocalHost()+" on port "+SOCKETPORT);
                        break;
                    }
                } catch (IOException ex) {}
            }
            
            while(!finished) {
                try {
                    socket = serverSocket.accept();
                    System.out.println("Server successfully connected to client");
                    ois = new ObjectInputStream(socket.getInputStream());
                    processedTask = (Task)ois.readObject();
                    System.out.println("Successfully received processed information:\n//////////////////");
                    client.displayProcessedTask(processedTask);
                    closeClient();
                } catch (IOException | ClassNotFoundException ex) {
                    System.err.println("Could not read object from server: "+ex);
                }
            }
        }

    }
    
    /*Method which is responsible for sending information fron client to server */
    public class Send implements Runnable {
        private PrintWriter pw;
        private boolean finished = false;
        
        /**
         * Constructor for Send to keep reference to a socket object
         * 
         * @param socket
         */
        public Send(Socket socket) {
            try {
                pw = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException ex) {}
        }
        @Override
        public void run() {
            while(!finished) {
                
            }
        }
        
        /**
         * Method to send input to the server
         * 
         * @param input	A String of input from the client to be worked on by the server
         */
        public void sendInput(String input) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {}
            
            System.out.println("Sending: "+input);
            pw.println(input);
        }
        
        public void endThread() {
            finished = true;
        }
    }
}

