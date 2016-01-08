/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storageserver;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import servers.MulticastServer;
import servers.messages.PDMessage;

/**
 *ASsimasososososos
 * @author Nuno
 */
public class StorageServer {

    public static final int MAX_USERS = 250;
    private int port;
    ServerSocket myServerSocket;
    boolean serverOn = true;
    boolean isMaster = false;
    File workingDir;

    public StorageServer(int _port, File dir) {
        port = _port;
        if(available(port))
        {
            isMaster = true;
            
            try {
            InetAddress addr = InetAddress.getByName("192.168.1.73");
            
            myServerSocket = new ServerSocket(port, MAX_USERS, addr);
            workingDir = dir;
            if(!workingDir.exists())
                workingDir.mkdir();
            
            System.out.println("Master StorageServer created successfully at port " + port + ".");
            
            } catch (IOException ex) {
                Logger.getLogger(StorageServer.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Could not create storage server on port " + port +". Exiting."); 
                System.exit(-1); 
            }
        }
        else
        {
            port++;
            isMaster = false;
            
            try {
            InetAddress addr = InetAddress.getByName("192.168.1.73");
            
            myServerSocket = new ServerSocket(port, MAX_USERS, addr);
            workingDir = dir;
            if(!workingDir.exists())
                workingDir.mkdir();
            
            System.out.println("Secundary StorageServer created successfully at port " + port + ".");
            
            } catch (IOException ex) {
                Logger.getLogger(StorageServer.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Could not create storage server on port " + port +". Exiting."); 
                System.exit(-1); 
            }
        }
        
        
        
    }
    
    public void listen()
    {
        System.out.println("Heartbeat System has started");
        new MulticastServer(port, isMaster).start();
        System.out.println("Listening for connections...");
        
        while(serverOn)
        {
            try {
                //accepts incoming TCP connection
                Socket clientSocket = myServerSocket.accept();

                //starts new service thread to handle client requests in background
                new ClientHandleThread(clientSocket).start();
            }
            catch (IOException e)
            {
                System.out.println("Exception encountered on accept. Ignoring. Stack Trace :"); 
                e.printStackTrace();
            }
        }
        
        try {
            myServerSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(StorageServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    class ClientHandleThread extends Thread {
        
        private Socket myClientSocket;
        private HashMap<String, Runnable> commandsMap;
        private PDMessage messageToSend, messageToReceive;
        private boolean connected = true;
        
        public ClientHandleThread() {
            super();
        }

        public ClientHandleThread(Socket myClientSocket) {
            this.myClientSocket = myClientSocket;
            this.messageToSend = new PDMessage();
            this.messageToReceive = new PDMessage();
            this.commandsMap = new HashMap<>();
            this.commandsMap.put("stop", this::help);
            this.commandsMap.put("ls", this::help);
            this.commandsMap.put("get", this::help);
            this.commandsMap.put("exit", this::help);
        }
        
        private void help()
        {
            messageToSend.createMessage("Avaible commands:\n\n"
                + "exit                       -> Exits the App\n"
                + "stop                       -> Stops the server\n"
                + "dir                        -> Gets personal file's info in current server\n");
            messageToSend.ClientStatus = 0;
        }
        
        private void handleMessage()
        {
            if (commandsMap.containsKey(messageToReceive.Commands[0]))
            {
                commandsMap.get(messageToReceive.Commands[0]).run();
            }
            else
            {
                messageToSend.createMessage("Invalid command! Maybe try 'help' ?");
                messageToSend.ClientStatus = messageToReceive.ClientStatus;
            }
        }
        
        @Override
        public void run()
        {
            ObjectInputStream in = null; 
            ObjectOutputStream out = null;
            
            System.out.println("Accepted connection from "
                        +  myClientSocket.getInetAddress().getHostAddress()+ ":" + myClientSocket.getPort());
            
            try
            {        
                
                in = new ObjectInputStream(myClientSocket.getInputStream());
                out = new ObjectOutputStream(myClientSocket.getOutputStream()); 
                
                while(connected)
                {
                    messageToReceive = (PDMessage) in.readObject();
                    System.out.println("Client Says :" + messageToReceive.Command);

//                    if(!serverOn) 
//                    { 
//                        // Special command. Quit this thread 
//                        System.out.print("Server has already stopped"); 
//                        PDMessage bye = new PDMessage();
//                        bye.createMessage("Server has stopped");
//
//                        out.writeObject(bye); 
//                        out.flush(); 
//                    }

                    //handleMessage();

                    out.writeObject(messageToSend);
                    System.out.println("Server Sent :" + messageToSend.Command);
                    out.flush();
                } 

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                Logger.getLogger(StorageServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally
            {
                try
                {                    
                    in.close(); 
                    out.close(); 
                    myClientSocket.close(); 
                    System.out.println("...Stopped"); 
                } 
                catch(IOException e) 
                { 
                    e.printStackTrace(); 
                }
            }
        }
        
    }
    
    /**
    * Checks to see if a specific port is available.
    *
    * @param port the port to check for availability
    */
    private static boolean available(int port) {
        if (port < 5000 || port > 30000) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }
    
    public static void main(String[] args) {
        
        if(args.length != 1)
        {
            System.out.println("Sintaxe: java StorageServer workingDirectory");
            return;
        }
        
        StorageServer server = new StorageServer(7000, new File(args[0]));
        server.listen();
    }
    
}

