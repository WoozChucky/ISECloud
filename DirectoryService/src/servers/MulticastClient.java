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
import java.util.logging.Level;
import java.util.logging.Logger;
import servers.messages.Heartbeat;
import servers.messages.MessageSerializer;

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
                
                Timer timer = new Timer();
                timer.schedule(new CheckServers(), 0, 5000);
                
                while (true) {
                    
                    DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                    clientSocket.receive(msgPacket);
                    
                    Heartbeat hb = MessageSerializer.deserializeHeartbeat(buf);
                                        
                    //Handle new Connection
                    handleHeartbeat(hb);
                    
                    //System.out.println(hb.getMsg());
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
    
    public String getFreeStorageServer()
    {
        for (Server sv : Servers) {
            if(sv.isAvailable())
            {
                return sv.getHost() + " " + sv.getPort();
            }
        }
        return null;
    }
    
    public void handleHeartbeat(Heartbeat hb)
    {
        Server sv = new Server(hb.getHost(), hb.getPort(), hb.IsMaster(), hb.IsAvailable());


        if(serverExists(sv))
            for (Server s : Servers)
            {
                if(s.getPort() == sv.getPort())
                {
                   s.setSeconds(sv.getSeconds());
                }
            }
        else
            Servers.add(sv);
    }
    
    private final class CheckServers extends TimerTask {
        
        @Override
        public void run() {
            int currentSecs = Calendar.getInstance().get(Calendar.HOUR_OF_DAY ) * 60 * 60 + 
                Calendar.getInstance().get(Calendar.MINUTE) * 60 +
                Calendar.getInstance().get(Calendar.SECOND);
            
            for(int i = 0; i < Servers.size(); i++)
            {
                if(Servers.get(i).getNullheartbeats() >= 3)
                {
                    System.out.println("Lost Connection to StorageServer("+ Servers.get(i).getHost() + ":" + Servers.get(i).getPort() + ") after 15 seconds. Removed..");
                    if(Servers.get(i).isMaster() && Servers.size() > 1)
                    {
                        Servers.remove(i);
                        Server temp = Servers.get(0);
                        temp.setMaster(true);
                        Servers.set(0, temp);
                    }
                    else
                    {
                        Servers.remove(i);
                        continue;
                    }
                }

                if(currentSecs - Servers.get(i).getSeconds() > 5)
                    Servers.get(i).setNullheartbeats(Servers.get(i).getNullheartbeats() + 1);
            }
        }
        
    }
    
}
