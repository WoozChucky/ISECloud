/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servers.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nuno
 */
public class UserLogins {
    public String[] Usernames;
    public String[] Passwords;
    public int Records;
    private String path;
    
    public UserLogins(String path)
    {
        Usernames = new String[20];
        Passwords = new String[20];
        Records = 0;
        this.path = path;
        
        File f = new File(path);
        
        if(!f.exists()){
            System.out.print("Aborting. Cant find logins file..");
            System.exit(-1);
        }
            
        int i = 0;
                
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
               String[] split = line.split("\\s");
               Usernames[i] = split[0];
               Passwords[i] = split[1];
               Records = i;
               i++;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UserLogins.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UserLogins.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void dispLogins()
    {
        for (int i = 0; i <= Records; i++) {
            System.out.println("Username: " + Usernames[i]);
            System.out.println("Password: " + Passwords[i]);
            System.out.println("ID: " + i);
        }
    }
        
}
