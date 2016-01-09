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
import servers.DirectoryService;
import servers.FTPService;
import servers.MulticastServer;
import servers.messages.PDMessage;
import servers.messages.ResponseType;
import storageserver.servers.StorageSearcher;

/**
 *ASsimasososososos
 * @author Nuno
 */
public class StorageServer {

    public static final int MAX_USERS = 1;
    int port;
    ServerSocket myServerSocket;
    MulticastServer multiCastServer;
    boolean serverOn = true;
    boolean isMaster = false;
    File workingDir;
    HashMap<String, Runnable> commandsMap;
    
    ObjectInputStream in = null; 
    ObjectOutputStream out = null;
    
    PDMessage messageToReceive = null;
    PDMessage messageToSend;

    public StorageServer(int _port, File dir) {
        port = _port;
        
        StorageSearcher s = new StorageSearcher();
        
        //if(s.masterAvailable())
        //{
            //Secondary Server StartUp
        //}
        //else
        //{
            
        //}
        
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
        
        messageToSend = new PDMessage();
        commandsMap = new HashMap<>();
        commandsMap.put("exit", this::exit);
        commandsMap.put("get", this::help);
        commandsMap.put("help", this::help);
        commandsMap.put("show", this::show);
        commandsMap.put("download", this::download);
        commandsMap.put("remove", this::remove);
        commandsMap.put("view", this::view);
        
    }
    
    private void exit()
    {
        messageToSend.ResponseCODE = ResponseType.EXIT;
        messageToSend.createMessage("Exiting...");
    }
    
    private void help()
    {
        messageToSend.createMessage("Avaible commands:\n\n"
                + "exit                       -> Exits the App\n"
                + "show                       -> Shows files available for current user\n"
                + "download (filename)        -> Downloads the give filename from the server\n"
                + "upload (filename)          -> Uploads the given filename to the server\n"
                + "remove (filename)          -> Deletes the given filename from the server");
    }
    
    private void show()
    {
        if(messageToReceive.Commands.length != 1)
        {
            messageToSend.createMessage("Invalid command!\n Arguments are only 'show'");
            messageToSend.ClientStatus = messageToReceive.ClientStatus;
            return;
        }

        File userDir = new File("Data/" + messageToReceive.Username + "/");

        System.out.println(userDir.getAbsolutePath());

        File[] lista = userDir.listFiles();

        String msg = new String();
        msg += "\n\tFiles Available\n";
        for(File f : lista)
        {
            if(f.isFile())
            {
                float Bs = f.length();
                float KBs = Bs / 1024; 
                float MBs = KBs / 1024;

                if(Bs < 1024)
                    msg += ("\n- " + f.getName() + "\t" + Math.round(Bs * 100d) / 100d + " Bytes");
                else if (Bs < 1048576)
                    msg += ("\n- " + f.getName() + "\t" + Math.round(KBs* 100d) / 100d + " Kilobytes");
                else 
                    msg += ("\n- " + f.getName() + "\t" + Math.round(MBs* 100d) / 100d + " Megabytes");
            }
        }
        msg+="\n";

        messageToSend.createMessage(msg);
    }
    private void download()
    {
        try {           
            //Arguments Verification
            if(messageToReceive.Commands.length != 2)
            {
                messageToSend.createMessage("Invalid Command!\nArguments must be <download filename>.");
                return;
            }
            
            //File Exists Verification
            if(new File("Data/" + messageToReceive.Username + "/" + messageToReceive.Commands[1]).exists() == false)
            {
                messageToSend.createMessage("The file you requested does not exist. Please use <show> command.");
                return;
            }
            
            messageToSend.createMessage("Downloading " + messageToReceive.Commands[1]);
            messageToSend.ResponseCODE = ResponseType.DOWNLOAD;
            out.writeObject(messageToSend);
            out.flush();
            
            FTPService.SendToClient(messageToReceive.Username, messageToReceive.Commands[1]);
            
            messageToSend.createMessage("File transfer complete!");
            messageToSend.ResponseCODE = ResponseType.FINISH_DOWNLOAD;
            out.writeObject(messageToSend);
            out.flush();
            
        } catch (IOException ex) {
            Logger.getLogger(DirectoryService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void remove()
    {
        //Arguments Verification
        if(messageToReceive.Commands.length != 2)
        {
            messageToSend.createMessage("Invalid Command!\nArguments must be <remove filename>.");
            return;
        }

        //Send to the client the code to also delete file locally
        messageToSend.ResponseCODE = ResponseType.REMOVE_LOCAL;

        //File Exists Verification
        if(new File("Data/" + messageToReceive.Username + "/" + messageToReceive.Commands[1]).exists() == false)
        {
            messageToSend.createMessage("The file you requested does not exist. Please use <show> command.");
            return;
        }

        //Deletes The Requested File
        new File("Data/" + messageToReceive.Username + "/" + messageToReceive.Commands[1]).delete();

        messageToSend.createMessage(messageToReceive.Commands[1] + " deleted successfully from server."); 
    }
    
    private void view()
    {
        //Login Verification
        if(messageToReceive.ClientStatus != 1)
        {
            messageToSend.createMessage("You must be logged in to view files.");
            messageToSend.ClientStatus = messageToReceive.ClientStatus;
            return;
        }   

        //Arguments Verification
        if(messageToReceive.Commands.length != 2)
        {
            messageToSend.createMessage("Invalid Command!\nArguments must be <view filename>.");
            return;
        }


        //Checks if file exists in server
        if(new File("Data/" + messageToReceive.Username + "/" + messageToReceive.Commands[1]).exists() == false)
        {
           messageToSend.createMessage("The file you requested does not exist.");
           messageToSend.ResponseCODE = ResponseType.NONE;
        }
        else
        {
            if(messageToReceive.ResponseCODE != ResponseType.OK)
            {
                //File Exists @ Client
                //Do Nothing
            }
            else
            {
                //Check if Files Exists @ Client
                if(true)
                //File Does Not Exists @ Client, Send Command to DL
                messageToSend.ResponseCODE = ResponseType.CHECK_FILE_EXISTS;

            }
        }

        messageToSend.createMessage("");
    }
    
    public void listen()
    {
        System.out.println("Heartbeat System has started");
        
        multiCastServer = new MulticastServer(port, isMaster);
        multiCastServer.start();
        
        System.out.println("Listening for connections...");
        
        while(serverOn)
        {
            try {
                //accepts incoming TCP connection
                Socket clientSocket = myServerSocket.accept();
                
                if(clientSocket != null)
                {
                    multiCastServer.setAvailable(false);
                    System.err.println("Got a client. Not accepting anymore clients.");
                }
                
                handleClient(clientSocket, multiCastServer);
                
            }
            catch(IOException e)
            {
            }
        }
        
        try {
            myServerSocket.close();
        } catch (IOException ex) {

        }
    }
    
    private void handleClient(Socket clientSock, MulticastServer mainServer)
    {
        System.out.println("Accepted connection from "
                        +  clientSock.getInetAddress().getHostAddress()+ ":" + clientSock.getPort());
        
        try
            {        
                in = new ObjectInputStream(clientSock.getInputStream());
                out = new ObjectOutputStream(clientSock.getOutputStream()); 
                
                while(true)
                {
                    messageToReceive = (PDMessage) in.readObject();
                    System.out.println("Client Says: " + messageToReceive.Command);
                    
                    handleMessage();
                    
                    out.writeObject(messageToSend);
                    System.out.println("Server Sent :" + messageToSend.Command);
                    out.flush();
                } 

            } catch (IOException | ClassNotFoundException ex) {
            }
            finally
            {
                try
                {                    
                    in.close(); 
                    out.close(); 
                    clientSock.close(); 
                    System.out.println("Connection dropped by user. Server available again to answer users."); 
                    mainServer.setAvailable(true);
                } 
                catch(IOException e) 
                { 
                }
            }  
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
            messageToSend.ResponseCODE = ResponseType.NONE;
        }
    }
    
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
        
        StorageServer server = new StorageServer(9000, new File(args[0]));
        server.listen();
    }
    
}

