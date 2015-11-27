/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nuno
 */
public class Heartbeat extends Thread {
    
    public Heartbeat(Socket sock, InetAddress addr, int port)
    {
        this.Socket = sock;
        this.Address = addr;
        this.Port = port;
    }
    
    public static Socket Socket;
    public static InetAddress Address;
    public static int Port;
    
    private DatagramPacket packet;
    private static final long frequency = 5000; //hearbeat frequency in miliseconds
    
    @Override
    public void run()
    {
        String line = "ping";
        
        packet = new DatagramPacket(line.getBytes(), line.length(), Address, Port);
        
        while(true)
        {
            //try {
                //Socket.send(packet);
              //  sleep(frequency);
            //} catch (InterruptedException | IOException ex) {
            //    Logger.getLogger(Heartbeat.class.getName()).log(Level.SEVERE, null, ex);
            //}
        }

    }
    
}
