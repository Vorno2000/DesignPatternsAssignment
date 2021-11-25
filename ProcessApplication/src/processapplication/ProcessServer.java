/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.AbstractQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

//the server class
public class ProcessServer {
    private ServerSocket serverSocket;
    private static final int PORT = 2020;
    private boolean stopRequested = false;
    private Map<Integer, ProcessConnections> connections;
    private int IDCounter;
    private NotificationQueue startQueue;
    private NotificationQueue middleQueue;
    private NotificationQueue endQueue;
    private AbstractQueue<Task> taskQueue;
    private PerimeterWorker perimeterWorker;
    private AreaWorker areaWorker;

    public ProcessServer() {
        connections = new HashMap<>();
        IDCounter = 1;
    }
    
    //creates the notification queues and worker pools with worker threads 
    //and aligns them to work together
    public void startServer() {
        serverSocket = null;
        
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server has started at "+InetAddress.getLocalHost()+" on port "+PORT);
            
            taskQueue = new LinkedBlockingQueue<>();
            startQueue = new NotificationQueue(taskQueue);
            
            middleQueue = new NotificationQueue();
            endQueue = new NotificationQueue(this);
            
            perimeterWorker = new PerimeterWorker(5, middleQueue);
            startQueue.addListener(perimeterWorker);
            
            areaWorker = new AreaWorker(2, endQueue);
            middleQueue.addListener(areaWorker);
        } catch(IOException e) {
            System.err.println("Server Can't listen on port "+PORT);
            System.err.println("Server Error: "+e);
            System.exit(-1);
        }
        
        try {
            while(!stopRequested) {
                Socket socket = serverSocket.accept();
                ProcessConnections newConnection = new ProcessConnections(socket, this, IDCounter);
                connections.put(IDCounter, newConnection);
                
                System.out.println("Client Successfully connected with ID:"+IDCounter);
                IDCounter++;
                
                Thread thread = new Thread(newConnection);
                thread.start();
            }
            
        } catch(IOException ex) {
            System.err.println("Server could not accept connection with client: "+ex);
        }
    }
    
    public void addTask(Task task) {
        startQueue.add(task);
    }
    
    public void sendTask(Task task) {
        ProcessConnections conn = connections.get(task.getIdentifier());
        conn.sendTask(task);
    }
    
    public void requestStop() {
        stopRequested = true;
        
        for(Map.Entry<Integer, ProcessConnections> connection : connections.entrySet()) {
            connection.getValue().stop();
        }
        
        try {
            serverSocket.close();
            perimeterWorker.stopWorkers();
            areaWorker.stopWorkers();
            
            startQueue.stop();
            middleQueue.stop();
            endQueue.stop();
            
            System.exit(-1);
        } catch (IOException ex) {}
    }
    
    public static void main(String[] args) {
        ProcessServer serverApp = new ProcessServer();
        serverApp.startServer();
    }
    
    //ProcessConnections Class
    //A class that handles each clients connection
    public class ProcessConnections implements Runnable{
        private Socket socket;
        private Socket clientSocket;
        private static final String CLIENT_HOSTNAME = "localhost";
        private static final int CLIENT_PORT = 2021;
        private ProcessServer server;
        private PrintWriter pw;
        private BufferedReader br;
        private int clientID;
        private String clientRequest;
        private Task clientTask;
        private ObjectOutputStream oos;
        private Object lock = new Object();
        
        public ProcessConnections() {}
        
        public ProcessConnections(Socket socket, ProcessServer server, int clientID) {
            this.socket = socket;
            this.server = server;
            this.clientID = clientID;
        }
        
        //sends the processed task to the client
        public void sendTask(Task task) {
            try {
                Task sendTask = null;
                try {
                    sendTask = (Task)task.clone();
                } catch (CloneNotSupportedException ex) {
                    System.err.println("Could not clone task to send");
                }
                synchronized(lock) {
                    clientSocket = new Socket(CLIENT_HOSTNAME, CLIENT_PORT);
                    System.out.println("Successfully connected to Client");

                    System.out.println("Sending the task using ID:"+task.getIdentifier());
                    oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    oos.writeObject(sendTask);
                    clientSocket.close();
                }
            } catch(IOException | NullPointerException e) {
                System.err.println("Error sending task to client: "+e);
            }
        }

        //creates the task to be processed
        @Override
        public void run() {
            try {
                pw = new PrintWriter(socket.getOutputStream(), true);
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                clientRequest = br.readLine();
                String[] info = clientRequest.split(",", 2);

                clientTask = new Task(info[0], Double.parseDouble(info[1]));
                clientTask.setIdentifier(clientID);

                server.addTask(clientTask);
            } catch (IOException | NumberFormatException ex) {
                System.err.println("Error creating client Task: "+ex);
            }
        }
        
        public void stop() {
            try {
                socket.close();
                clientSocket.close();
                pw.close();
                br.close();
                oos.close();
            } catch (IOException ex) {
                System.err.println("Error closing client connection: "+ex);
            }
        }
    }
}
