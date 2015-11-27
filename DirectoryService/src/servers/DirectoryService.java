package servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;
import servers.message.*;
import servers.security.UserLogins;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Nuno
 */
public class DirectoryService {
        
    public static final int MAX_SIZE = 2048;
    public static final String LOGINS_FILE = "logins.txt";
    
    private byte[] receiveData = new byte[MAX_SIZE];
    
    private int port;
    private DatagramSocket socket;
    private DatagramPacket packet; //para receber os pedidos e enviar as respostas
    
    private PDMessage messageToReceive;
    private PDMessage messageToSend;
    
    private UserLogins logins;
    
    private HashMap<String, Runnable> commandsMap;
    

    public DirectoryService(int port) throws SocketException
    {        
        if(!available(port)) {
            System.err.println("Directory Service already running at port " + port + ".");
            System.exit(-1);
        }
        
        this.socket = null;
        packet = new DatagramPacket(receiveData, receiveData.length);
        socket = new DatagramSocket(port);
        messageToReceive = null;
        messageToSend = new PDMessage();
        commandsMap = new HashMap<>();
        commandsMap.put("exit", this::exit);
        commandsMap.put("login", this::login);
        commandsMap.put("logout", this::logout);
        commandsMap.put("get", this::help);
        commandsMap.put("help", this::help);
        commandsMap.put("tcp", this::tcp);
        logins = new UserLogins(LOGINS_FILE);
    }
    
    private void login()
    {
        
        if(messageToReceive.Commands.length != 3)
        {
            messageToSend.createMessage("Invalid Command!\nArguments must be <login username password>");
            messageToSend.ClientStatus = 0;
        }
        else
        {            
            if(messageToReceive.ClientStatus == 1)
            {
                messageToSend.createMessage("Already logged in.");
                return;
            }
            
            for (int i = 0; i <= logins.Records; i++) 
            {
                if(messageToReceive.Commands[1].equals(logins.Usernames[i]) &&
                        messageToReceive.Commands[2].equals(logins.Passwords[i]))
                {
                    messageToSend.createMessage(logins.Usernames[i] + " logged in successfully!");
                    messageToSend.ClientStatus = 1;

                    return;
                }   
            }
            
            messageToSend.createMessage("Invalid login.");
            messageToSend.ClientStatus = 0;
        }
    }
    private void logout()
    {
        if(messageToReceive.ClientStatus == 1)
        {
            messageToSend.ClientStatus = 0;
            messageToSend.createMessage("Logged out successfully.");
        }
        else
            messageToSend.createMessage("Not logged in.");
    }
    private void exit()
    {
        messageToSend.ClientStatus = -1;
        messageToSend.createMessage("Exiting...");
    }
    
    private void tcp()
    {
        if(messageToReceive.ClientStatus == 0) //TODO : Colocar 1 para login obrigatorio
        {
            messageToSend.ClientStatus = 2;
            messageToSend.createMessage("Connecting to Storage Server.. Bye");
        }
        else
        {
            messageToSend.createMessage("Not logged in.");
        }
    }
    
    private void help()
    {
        messageToSend.createMessage("Avaible commands:\n\n"
                + "exit                       -> Exits the App\n"
                + "login (username, password) -> Tries to login the current user\n"
                + "logout                     -> Logs out the current logged in user\n"
                + "tcp                        -> Connects to Storage Server (DEBUG)\n"
                + "get (filename)             -> Tries to download the file\n");
    }
    
    private PDMessage waitDatagram() throws IOException
    {
        PDMessage request;
        
        if(socket == null){
            return null;
        }
        
        packet = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(packet);
        
        byte[] myObject = new byte[packet.getLength()];
            
        //System.arraycopy(receiveData, 0, myObject, 0, packet.getLength());
        
        for (int i = 0; i < packet.getLength(); i++) {
            myObject[i] = receiveData[i];
        }
            
        request = MessageSerializer.deserializeMessageReceived(myObject);
            
        return request;
        
        //System.out.println("Recebido \"" + request.Command + "\" de " + 
                //packet.getAddress().getHostAddress() + ":" + packet.getPort());
    }
    
    public void processRequests() throws IOException
    {
        
        if(socket == null){
            return;
        }
        
        System.out.println("Server has started... waiting for connections.");
        
        while(true){
            messageToReceive = null;
            messageToReceive = waitDatagram();
            
            System.out.println("Message received -> '" + messageToReceive.Command + "'");
            
            if(messageToReceive == null){
                continue; //send error message
            }
            
            handleMessage(messageToReceive);
            
            //System.out.println("PDMessage to send -> " + messageToSend.Command);

            packet.setData(MessageSerializer.serializeMessageToSend(messageToSend));
            
            //O ip e porto de destino jÃ¡ se encontram definidos em packet
            socket.send(packet);
            
        }
    }   
    
    public static void main(String[] args) throws IOException
    {
        int port;
        
        DirectoryService storageSv = null;
        
        if(args.length != 1)
        {
            System.out.println("Sintaxe: java StorageServer listeningPort");
            return;
        }
        
        try {
            port = Integer.parseInt(args[0]); //obtem porta dos argumentos
            
            storageSv = new DirectoryService(port);
            storageSv.processRequests();
            
        } catch (NumberFormatException e)
        {
            
        }
    }

    private void handleMessage(PDMessage msg) {   
        if (commandsMap.containsKey(msg.Commands[0]))
        {
            commandsMap.get(msg.Commands[0]).run();
        }
        else
        {
            messageToSend.createMessage("Invalid command! Maybe try 'help' ?");
            messageToSend.ClientStatus = messageToReceive.ClientStatus;
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
    
}
