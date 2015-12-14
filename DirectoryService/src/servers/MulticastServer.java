/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import servers.messages.Heartbeat;
import servers.messages.MessageSerializer;

/**
 *
 * @author nunol
 */
public class MulticastServer extends Thread {

    final static String INET_ADDR = "225.15.15.15";
    final static int PORT = 7000;
    final static boolean debug = true;
    private boolean _isMaster;
    private int svPort;
    
    public MulticastServer(int port, boolean master)
    {
        svPort = port;
        _isMaster = master;
    }
    
    @Override
    public void run()
    {
        try{
            InetAddress addr = InetAddress.getByName(INET_ADDR);
            
            try(DatagramSocket serverSocket = new DatagramSocket())
            {
                while(true)
                {
                    Heartbeat hb = new Heartbeat(InetAddress.getLocalHost().getHostAddress(), svPort, _isMaster);
                    
                    byte[] toSend = MessageSerializer.serializeHeartbeat(hb);
                    
                    DatagramPacket packet = new DatagramPacket(toSend, toSend.length, addr, PORT);
                    
                    serverSocket.send(packet);
                    
                    if(debug)
                        System.out.println("Pinging DirectoryService..");
                    
                    Thread.sleep(5000);
                }
            } catch (SocketException ex) {
                Logger.getLogger(MulticastServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MulticastServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(MulticastServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(MulticastServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the _isMaster
     */
    public boolean isIsMaster() {
        return _isMaster;
    }

    /**
     * @param _isMaster the _isMaster to set
     */
    public void setIsMaster(boolean _isMaster) {
        this._isMaster = _isMaster;
    }
    
}
