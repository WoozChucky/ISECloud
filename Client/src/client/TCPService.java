/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import servers.message.PDMessage;

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
            clientSocket = new Socket(InetAddress.getByName(host), port);
            clientSocket.setSoTimeout(5000);
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
                PDMessage msg = (PDMessage) in.readObject();

                if(msg instanceof PDMessage)
                    return msg;
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
    
    public void handleMessage(PDMessage msg)
    {
        
    }
    
//    public void shutdown()
//    {
//        try {
//            oos.close();
//            in.close();
//            clientSocket.close();
//        } catch (IOException ex) {
//            Logger.getLogger(TCPService.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    
    public String serverPID()
    {
        return clientSocket.getRemoteSocketAddress().toString();
    }
}
