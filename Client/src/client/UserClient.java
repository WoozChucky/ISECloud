package client;


import servers.messages.PDMessage;
import servers.messages.ResponseType;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nuno
 */
public class UserClient {
    
    private static String PID = ManagementFactory.getRuntimeMXBean().getName();
    public static boolean isUDPRunning;
    public static boolean isTCPRunning;

    public static void main(String[] args) throws IOException
    {        
        /* Service Variable Declaration */
        UDPService udpService;
        TCPService tcpService;
        
        /* Other Variables */
        String buf = "";
        Scanner scanIn = new Scanner(System.in);
        ConnectionInfo tcpConn = new ConnectionInfo();
        File workingDir;
        PDMessage msg = new PDMessage();

        
        if(args.length != 3){
            System.out.println("["+ PID +"] Missing server address, port and working directory.\n\tEx: 192.168.1.73 27015 Downloads");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(UserClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.exit(-1);
        }
        
        
        workingDir = new File(args[2]);
        if(!workingDir.exists())
            workingDir.mkdir();

        udpService = new UDPService(args[0], Integer.parseInt(args[1]));
        
        isTCPRunning = false;
        isUDPRunning = true;

        /* While-Loop for UDP Service */
        while (isUDPRunning) 
        {       
            if(msg.ResponseCODE == ResponseType.EXIT) break;
            
            System.out.print("["+ PID +"] Insert command: ");
            buf = scanIn.nextLine();

            msg.createMessage(buf);
            
            scanIn.reset();
            
            udpService.send(msg);
            
            msg = udpService.receive();
            
            System.out.println("[DirectoryServer"+ udpService.serverPID() +"] " + msg.Command);
            
            udpService.handleMessage(msg, isUDPRunning, tcpConn, workingDir.getAbsolutePath());
        }
        
        //tcpService = new TCPService(tcpConn.Host, tcpConn.Port);
        //isTCPRunning = true;
        
        //ObjectInputStream in = new ObjectInputStream(tcpService.sock().getInputStream());
        //ObjectOutputStream oos = new ObjectOutputStream(tcpService.sock().getOutputStream());
        
        /* While-Loop for TCP Service 
        while (isTCPRunning)
        {
            System.out.print("["+ PID +"] Insert command: ");
            buf = scanIn.nextLine();

            msg.createMessage(buf);
            
            scanIn.reset();
            
            tcpService.send(msg);
            System.out.println("Sent -> " + msg.Command);

            msg = tcpService.receive();
            System.out.println("Received -> " + msg.Command);
            
            System.out.println("[StorageServer"+ tcpService.serverPID() +"] " + msg.Command);
            
            tcpService.handleMessage(msg);
        }
        */
   }
    
}
