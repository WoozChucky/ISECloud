/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import servers.DirectoryService;
import servers.FTPService;
import servers.messages.PDMessage;
import servers.messages.ResponseType;

/**
 *
 * @author Nuno
 */
public class TCPService {
    
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream oos;
    
    
    public TCPService(String host, int port)
    {
        try {
            clientSocket = new Socket(host, port);
            
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            
            
        } catch (IOException ex) {
            System.err.println("Cant connect to Storage Server.\n" + ex);
            System.exit(-1);
        }

    }
    
    public Socket sock()
    {
        return clientSocket;
    }
    
    public PDMessage receive()
    {
        try { 
            
            try {
                return (PDMessage) in.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }      

        }   catch (IOException ex) {
            Logger.getLogger(TCPService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void send(PDMessage msg)
    {
        try {
            
            oos.writeObject(msg);
            oos.flush();
            
        } catch (IOException ex) {
            Logger.getLogger(TCPService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void handleTCPResponseMessage(PDMessage msg, String dir) throws IOException
    {
        switch(msg.ResponseCODE)
        {
            case VIEW_CHECK_FILE:

                String filenameCheck = msg.Commands[1];
                
                if(!new File(dir + "/" + filenameCheck).exists())
                {
                    //Dlit
                    msg = new PDMessage();
                    msg.ResponseCODE = ResponseType.DOWNLOAD;
                    send(msg);
                    
                    //Receives the File
                    FTPService.ReceiveFileFromServer(sock().getInetAddress().getHostAddress(), dir, filenameCheck);
                    
                    msg = new PDMessage();
                    msg = receive();
                    System.out.println("[StorageServer] " + msg.Command);
                    
                    
                }
                else
                {
                    msg = new PDMessage();
                    msg.ResponseCODE = ResponseType.OK;
                    send(msg);
                    
                    msg = new PDMessage();
                    msg = receive();
                    System.out.println("[StorageServer#2] " + msg.Command);
                }
                
                //Test for .txt
                if(!"txt".equals(DirectoryService.getFileExtension(dir + "/" + filenameCheck)))
                {
                    System.out.println("[StorageServer] Sorry but this functionality in only available to .txt files.");
                    return;
                }
                
                BufferedReader inReader = new BufferedReader(new FileReader(dir + "/" + filenameCheck));
                
                String output, line;
                output = "\n\tFile Content\n-----";
                while((line = inReader.readLine()) != null)
                {
                    output += "\n" + line;
                }
                output += "\n-----";
                inReader.close();
                
                System.out.println("[StorageServer] " + output);
                
            break;
            case UPLOAD:              
                
                msg = receive();
                
                if(msg.ResponseCODE == ResponseType.ALREADY_EXISTS)
                {
                    System.out.println("[StorageServer] " + msg.Command);
                    return;
                }
                
                String filenameUP = msg.Command;
        
                if(!new File(dir + "/" + filenameUP).exists())
                {
                    msg = new PDMessage();
                    msg.ResponseCODE = ResponseType.STOP_UPLOAD;
                    send(msg);
                    
                    msg = new PDMessage();
                    msg = receive();
                    System.out.println("[StorageServer] " + msg.Command);
                    return;
                }
                else
                {
                    msg = new PDMessage();
                    msg.ResponseCODE = ResponseType.OK;
                    send(msg);
                }
                
                FTPService.SendFileToServer(dir, filenameUP);
                
                msg = new PDMessage();
                msg = receive();
                System.out.println("[StorageServer] " + msg.Command);
                
            break;
            case DOWNLOAD:
                
                String filename = msg.Commands[2];
                
                if(new File(dir + "/" + filename).exists())
                {
                    msg = new PDMessage();
                    msg.ResponseCODE = ResponseType.ALREADY_EXISTS;
                    send(msg);
                    msg = new PDMessage();
                    msg = receive();
                    System.out.println("[StorageServer] " + msg.Command);
                    return;
                }
                else
                {
                    msg = new PDMessage();
                    msg.ResponseCODE = ResponseType.OK;
                    send(msg);
                }
                
                //Receives the File
                FTPService.ReceiveFileFromServer(sock().getInetAddress().getHostAddress(), dir, filename);
                //Receives Confimation Message
                msg = new PDMessage();
                msg = receive();
                System.out.println("[DirectoryServer] " + msg.Command);
                msg.ResponseCODE = ResponseType.NONE;
            break;
            
            case REMOVE_LOCAL:
                if(new File(dir + "/" + msg.Commands[0]).exists())
                {
                    new File(dir + "/" + msg.Commands[0]).delete();
                }
            break;
                
            case EXIT:
                clientSocket.close();
                oos.close();
                in.close();
                removeUserFiles(dir);
                System.exit(-1);
            break;
        }
        
    }
    
    public static void removeUserFiles(String dir)
    {
        for(File file: new File(dir).listFiles()) file.delete();
    }
    
    public String serverPID()
    {
        return clientSocket.getRemoteSocketAddress().toString();
    }
}
