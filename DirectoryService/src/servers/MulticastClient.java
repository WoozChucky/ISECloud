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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import servers.message.Heartbeat;
import servers.message.MessageSerializer;

/**
 *
 * @author nunol
 */
public class MulticastClient extends Thread {

    final static String INET_ADDR = "225.15.15.15";
    final static int PORT = 7000;
    
    private ArrayList<Server> Servers;
    
    public MulticastClient()
    {
        Timer timer = new Timer();
        timer.schedule(new CheckServers(), 0, 5000);
    }
    
    public ArrayList<Server> getServers()
    {
        return Servers;
    }
    
    @Override
    public void run()
    {
        Servers = new ArrayList<>();
        
        try {
            InetAddress address = InetAddress.getByName(INET_ADDR);
            
            byte[] buf = new byte[DirectoryService.MAX_SIZE];
            
            
            try (MulticastSocket clientSocket = new MulticastSocket(PORT)){
                
                clientSocket.joinGroup(address);
                
                while (true) {
                    
                    DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                    clientSocket.receive(msgPacket);
                    
                    Heartbeat hb = MessageSerializer.deserializeHeartbeat(buf);
                    
                    //Handle new Connection
                    handleHeartbeat(hb);
                    
                    System.out.println(hb.getMsg());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(MulticastClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean serverExists(Server s)
    {
        return Servers.stream().anyMatch((sv) -> (sv.getPort() == s.getPort()));
    }
    
    public void handleHeartbeat(Heartbeat hb)
    {
        Server sv = new Server(hb.getHost(), hb.getPort());

        if(serverExists(sv))
            for (Server s : Servers)
            {
                if(s.getPort() == sv.getPort())
                {
                    if(sv.getSeconds() - s.getSeconds() < 5)
                        s.setSeconds(sv.getSeconds());
                }
            }
        else
            if(Servers.isEmpty())
            {
                sv.setMaster(true);
                Servers.add(sv);
            }
            else
            {
                sv.setMaster(false);
                Servers.add(sv);
            }
    }
    
    private final class CheckServers extends TimerTask {
        
        @Override
        public void run() {
            int currentSecs = Calendar.getInstance().get(Calendar.HOUR_OF_DAY ) * 60 * 60 + 
                Calendar.getInstance().get(Calendar.MINUTE) * 60 +
                Calendar.getInstance().get(Calendar.SECOND);
            
            for (Server s : Servers) {
                
                if(s.getNullheartbeats() >= 3)
                {
                    System.out.println("Lost Connection to StorageServer("+ s.getHost() + ":" + s.getPort() + ") after 15 seconds. Removed..");
                    Servers.remove(s);
                    Server temp = Servers.get(0);
                    System.out.println(temp.isMaster());
                    temp.setMaster(true);
                    break;
                }
                
                if(currentSecs - s.getSeconds() > 5)
                    s.setNullheartbeats(s.getNullheartbeats() + 1);
            }
        }
        
    }
    
}
