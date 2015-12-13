/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servers.message;

import java.io.Serializable;

/**
 *
 * @author Nuno
 */
public class PDMessage implements Serializable {
    
    private static final long serialVersionUID = 27015L;
    
    public int ClientStatus; // 0 - NotLogged :::  1 - Logged
    public String Command;
    public String[] Commands;  
    
    public String Username;
    
    public PDMessage()
    {
        this.ClientStatus = 0;
        this.Command = null;
        this.Commands = null;
        this.Username = "";
    }   
    
    public void createMessage(String msg)
    {
        this.Command = msg;
        this.Commands = msg.split("\\s+");
    }
}
