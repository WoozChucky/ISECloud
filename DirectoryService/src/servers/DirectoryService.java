package servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import servers.messages.MessageSerializer;
import servers.messages.PDMessage;
import servers.messages.ResponseType;
import servers.security.UserLogins;

/**
 *
 * @author Nuno
 */
public class DirectoryService {
        
    public static final int MAX_SIZE = 2*1024;
    public static final String LOGINS_FILE = "logins.txt";
    
    private byte[] receiveData = new byte[MAX_SIZE];
    private int port;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private PDMessage messageToReceive;
    private PDMessage messageToSend;
    private UserLogins logins;
    private HashMap<String, Runnable> commandsMap;
    private MulticastClient multiCastClient;
    

    public DirectoryService(int port) throws SocketException
    {        
        if(!available(port)) {
            System.err.println("Directory Service already running at port " + port + ".");
            System.exit(-1);
        }
        
        socket = new DatagramSocket(port);
        packet = new DatagramPacket(receiveData, receiveData.length);
        
        messageToReceive = null;
        messageToSend = new PDMessage();
        
        commandsMap = new HashMap<>();
        commandsMap.put("exit", this::exit);
        commandsMap.put("login", this::login);
        commandsMap.put("logout", this::logout);
        commandsMap.put("help", this::help);
        commandsMap.put("connect", this::tcp);
        
        logins = new UserLogins(LOGINS_FILE);
        multiCastClient = new MulticastClient();
    }
    
    
    
    
    private void login()
    {
        if(messageToReceive.Commands.length != 3)
        {
            messageToSend.createMessage("Invalid Command!\nArguments must be <login username password>");
            messageToSend.ClientStatus = 0;
            return;
        }
         
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
                messageToSend.Username = logins.Usernames[i];
                messageToSend.ResponseCODE = ResponseType.LOGIN;

                /*
                File userFolder = new File("Data/" + logins.Usernames[i]);
                if(!userFolder.exists() && !userFolder.isDirectory())
                {
                    userFolder.mkdir();
                }
                */
                return;                    
            }   
        }

        messageToSend.createMessage("Invalid login.");
        messageToSend.ClientStatus = 0;
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
        messageToSend.ResponseCODE = ResponseType.EXIT;
        messageToSend.createMessage("Exiting...");
    }
    private void tcp()
    {
        if(messageToReceive.ClientStatus == 1) //TODO : Colocar 1 para login obrigatorio
        {
            //Check if there are any servers available for user
            if(multiCastClient.serversAvailable() > 0)
                    try {
                        messageToSend.ResponseCODE = ResponseType.CONNECT_TCP;
                        messageToSend.createMessage("Connecting to Storage Server.. Bye");

                        packet.setData(MessageSerializer.serializePDMessage(messageToSend));
                        socket.send(packet);

                        messageToSend.createMessage(multiCastClient.getFreeStorageServer());
                        packet.setData(MessageSerializer.serializePDMessage(messageToSend));
                        socket.send(packet);

                    } catch (IOException ex) {
                        Logger.getLogger(DirectoryService.class.getName()).log(Level.SEVERE, null, ex);
                    }     
            else
            {
                messageToSend.createMessage("No servers are available at this time. Please try again later.");
            }
        }
        else
        {
            messageToSend.createMessage("Not logged in.");
        }
    }
    private void help()
    {
        if (messageToReceive.ClientStatus == 0)
        {
            messageToSend.createMessage("Avaible commands:\n\n"
                + "exit                       -> Exits the App\n"
                + "login (username, password) -> Tries to login the current user\n");
            /*
            messageToSend.createMessage("Avaible commands:\n\n"
                + "exit                       -> Exits the App\n"
                + "login (username, password) -> Tries to login the current user\n"
                + "logout                     -> Logs out the current logged in user\n"
                + "show                       -> Shows file available for current user\n"
                + "download (filename)        -> Downloads the give filename from the server\n"
                + "upload (filename)          -> Uploads the given filename to the server"
                + "remove (filename)          -> ");

            */
        }
        else
        {
            messageToSend.createMessage("Avaible commands:\n\n"
                + "exit                       -> Exits the App\n"
                + "logout                     -> Logs out the current logged in user\n"
                + "connect                    -> Tries to connect user to StorageServer ");
        }
        
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
        
        for (int i = 0; i < packet.getLength(); i++) {
            myObject[i] = receiveData[i];
        }
            
        request = MessageSerializer.deserializePDMessage(myObject);
            
        return request;
    }
    
    public void processRequests() throws IOException
    {
        
        multiCastClient.start();
        
        if(socket == null){
            return;
        }
        
        System.out.println("Server has started... waiting for connections.");
        
        while(true){
            
            messageToReceive = null;
            messageToReceive = waitDatagram();
            
            System.out.println("Message received -> '" + messageToReceive.Command + "'");
            
            handleMessage(messageToReceive);

            if(messageToSend.ResponseCODE != ResponseType.FINISH_DOWNLOAD)
            {
                packet.setData(MessageSerializer.serializePDMessage(messageToSend));

                socket.send(packet);
            }
            messageToSend.ResponseCODE = ResponseType.NONE;
            
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
            messageToSend.ResponseCODE = ResponseType.NONE;
        }
    }
    
    public static boolean available(int port) {
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
                }
            }
        }

        return false;
    }
    
    public static String getFileExtension(String path)
    {
        int i = path.lastIndexOf('.');
        if(i >= 0)
            return path.substring(i+1);
        return null;
    }
    
}
