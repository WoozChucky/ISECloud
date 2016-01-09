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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import servers.DirectoryService;
import servers.FTPService;
import servers.messages.MessageSerializer;
import servers.messages.PDMessage;
import servers.messages.ResponseType;

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
    
    public void handleMessage(PDMessage msg, boolean running, ConnectionInfo tcpConn, String dir, UDPService auxService) throws IOException
    {
        switch(msg.ResponseCODE)
        {
            case EXIT:
                socket.close();
                System.exit(-1);
                //running = false;
                
                break;
                
            case NONE:
                
                break;
                
            case DOWNLOAD:
                //Receives the File
                FTPService.ReceiveFileFromServer(dir, msg.Commands[1]);
                //Receives Confimation Message
                msg = receive();
                System.out.println("[DirectoryServer"+ serverPID() +"] " + msg.Command);
                msg.ResponseCODE = ResponseType.NONE;
                
                break;
                
            case CHECK_FILE_EXISTS:
                
                break;
            case DOWNLOAD_READ:
                //Receives the File
                FTPService.ReceiveFileFromServer(dir, msg.Commands[0]);
                
                //Test for .txt
                if(!"txt".equals(DirectoryService.getFileExtension(dir + "/" + msg.Commands[0])))
                {
                    System.out.println("[DirectoryServer"+ serverPID() +"] Sorry but this functionality in only available to .txt files.");
                    return;
                }
                
                BufferedReader in = new BufferedReader(new FileReader(dir + "/" + msg.Commands[0]));
                
                String output, line;
                output = "\n\tFile Content\n-----";
                while((line = in.readLine()) != null)
                {
                    output += "\n" + line;
                }
                output += "\n-----";
                
                System.out.println("[DirectoryServer"+ serverPID() +"] " + output);

                break;
                
            case REMOVE_LOCAL:
                if(new File(dir + "/" + msg.Commands[0]).exists())
                {
                    new File(dir + "/" + msg.Commands[0]).delete();
                }
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
