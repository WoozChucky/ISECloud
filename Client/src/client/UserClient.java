package client;


import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import servers.DirectoryService;
import servers.messages.PDMessage;
import servers.messages.ResponseType;

/*
 * Consegue obter Host e Porta do Storage
 * Visualização de Conteúdo ?? Imagens = LOL (É Local e se nao existir, faz o download automaticamente)
 * 
 */

/**
 *
 * @author Nuno
 */
public class UserClient {
    
    private static final String PID = ManagementFactory.getRuntimeMXBean().getName();
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
        String username = "";
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
        
        if(DirectoryService.available(Integer.parseInt(args[1])))
        {
            System.err.println("["+ PID +"] DirectoryService Unavailable. Exiting app...");
            System.exit(-1);
        }
        
        workingDir = new File(args[2]);
        if(!workingDir.exists())
            workingDir.mkdir();
        
        //TODO REMOVE COMENT HERE!!!!
        //TCPService.removeUserFiles(workingDir.getAbsolutePath());

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
            
            udpService.handleUDPResponseMessage(msg, isUDPRunning, tcpConn, workingDir.getAbsolutePath(), udpService);
            
            if(msg.ResponseCODE == ResponseType.LOGIN)
                username = msg.Username;
            
            if(msg.ResponseCODE == ResponseType.CONNECT_TCP)
                break;
        }
        
        tcpService = new TCPService(tcpConn.Host, tcpConn.Port);
        isTCPRunning = true;
        
        // While-Loop for TCP Service 
        while (isTCPRunning)
        {
            msg = new PDMessage();
            msg.Username = username;
            
            System.out.print("["+ PID +"] Insert command: ");
            buf = scanIn.nextLine();
            
            msg.createMessage(buf);
            
            scanIn.reset();
 
            tcpService.send(msg);
            
            msg = tcpService.receive();
            
            System.out.println("[StorageServer] " + msg.Command);
            
            tcpService.handleTCPResponseMessage(msg, workingDir.getAbsolutePath());
        }
        
   }
    
}
