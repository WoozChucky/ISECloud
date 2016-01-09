/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storageserver.servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import servers.DirectoryService;

/**
 *
 * @author nunol
 */
public class StorageSearcher {
    private final static String INET_ADDR = "225.15.15.15";
    private final static int PORT = 7000;
    private final static int TIMEOUT = 15000;
    
    public boolean masterAvailable()
    {
        try {
            InetAddress address = InetAddress.getByName(INET_ADDR);
            
            byte[] buf = new byte[DirectoryService.MAX_SIZE];
            
            try (MulticastSocket clientSocket = new MulticastSocket(PORT)){
                
                clientSocket.joinGroup(address);
                clientSocket.setSoTimeout(TIMEOUT);

                while (true) {
                    
                    DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                    clientSocket.receive(msgPacket);
                    
                    return buf != null;
                }
                
            } catch (IOException ex) {
            }
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(StorageSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
}
