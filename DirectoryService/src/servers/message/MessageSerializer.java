/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servers.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import servers.DirectoryService;

/**
 *
 * @author Nuno
 */
public final class MessageSerializer {
    
    public static final byte[] serializeMessageToSend(PDMessage msg)
    {
        try {
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream(DirectoryService.MAX_SIZE);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            
            oos.writeObject(msg);
            oos.close();
            
            byte[] obj = baos.toByteArray();
            baos.close();
            return obj;
        
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public static final PDMessage deserializeMessageReceived(byte[] data)
    {
        try {
            ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(data));
            try {
                PDMessage msg = (PDMessage) iStream.readObject();
                iStream.close();
            
                return msg;
            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            
        } 
        catch (IOException e)
        {
            System.out.println("iStream -> " + e.toString());
        }
        
        return null;
    }
}
