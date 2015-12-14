/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author nunol
 */
public class FTPService {
    
    public static void SendToClient(String user, String file) throws IOException
    {

        File myFile = new File ("Data/" + user + "/" + file);
        
        ServerSocket servsock = null;
        Socket sock = null;
        servsock = new ServerSocket(13267);
        
        int count;
        byte[] buffer = new byte[1024];

        sock = servsock.accept();
        
        OutputStream out = sock.getOutputStream();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(myFile));
        
        while ((count = in.read(buffer)) >= 0)
        {
            out.write(buffer, 0, count);
        }
        out.close();
        in.close();
        servsock.close();
        sock.close();
    }
    
    public static void ReceiveFileFromServer(String userDir, String filename) throws IOException
    {
        int SOCKET_PORT = 13267;      // you may change this
        String SERVER = "192.168.1.73";  // localhost
        
        Socket socket = new Socket(SERVER, 13267);
        FileOutputStream fos = new FileOutputStream(userDir + "/" + filename);
        BufferedOutputStream out = new BufferedOutputStream(fos);
        byte[] buffer = new byte[1024];
        int count;
        InputStream in = socket.getInputStream();
        while((count=in.read(buffer)) >= 0){
            fos.write(buffer, 0, count);
        }
        fos.close();
        in.close();
        socket.close();
    }
    
}
