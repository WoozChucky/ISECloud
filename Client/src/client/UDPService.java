/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import servers.DirectoryService;
import servers.messages.MessageSerializer;
import servers.messages.PDMessage;

/**
 *
 * @author Nuno
 */
public class UDPService {
    
    public static final int TIMEOUT = 2; //segundos
    
    private InetAddress serverAddr;
    private int serverPort;
    private DatagramSocket socket;
    private DatagramPacket packet;

    public UDPService(String host, int port)
    {
        try {
            serverAddr = InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            Logger.getLogger(UserClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        serverPort = port;   
        try {
            socket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(UserClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socket.setSoTimeout(TIMEOUT*1000);
        } catch (SocketException ex) {
            Logger.getLogger(UserClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String serverPID()
    {
        return packet.getAddress().toString() + ":" + packet.getPort();
    }
    
    public void send(PDMessage msg)
    {
        byte[] data = MessageSerializer.serializePDMessage(msg);
        
        packet = new DatagramPacket(data, data.length, serverAddr,
                    serverPort);
        try {
            socket.send(packet);
        } catch (IOException ex) {
            Logger.getLogger(UserClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public PDMessage receive()
    {
        try {
            byte[] receiveData = new byte[DirectoryService.MAX_SIZE];
            
            //receive a packet
            packet = new DatagramPacket(receiveData, receiveData.length);
            
            socket.receive(packet);
            
            byte[] myObject = new byte[packet.getLength()];
            
            System.arraycopy(receiveData, 0, myObject, 0, packet.getLength());
            
            PDMessage msg = MessageSerializer.deserializePDMessage(myObject);
            
            return msg;
            
        } catch (IOException ex) {
            System.err.println("Error: " + ex);
            System.exit(-1);
        }
        
        return null;  
    }
    
    public void handleUDPResponseMessage(PDMessage msg, boolean running, ConnectionInfo tcpConn, String dir, UDPService auxService) throws IOException
    {
        switch(msg.ResponseCODE)
        {
            case EXIT:
                socket.close();
                TCPService.removeUserFiles(dir);
                System.exit(-1);
            break;
                 
            case CONNECT_TCP:
                //Wait for Server Info, Then Connect                
                msg = auxService.receive();
                tcpConn.Host = msg.Commands[0];
                tcpConn.Port = Integer.parseInt(msg.Commands[1]);
                socket.close();
                
                running = false;
            break;
        }
    }
}
