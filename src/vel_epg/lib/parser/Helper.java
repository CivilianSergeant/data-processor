/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vel_epg.lib.parser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import vel_epg.orm.ProgramEventService;

/**
 *
 * @author Himel
 */
public class Helper {
    
    public static String getToken(){
        
       String nanoTime = String.valueOf(System.nanoTime());
       StringBuffer sb = new StringBuffer();
       try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(nanoTime.getBytes());
            byte[] hash = md.digest();
            for(int i=0; i<hash.length; i++){
                sb.append(Integer.toString((hash[i] & 0xff)  + 0x100,16).substring(1));
            }
            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ProgramEventService.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        return sb.toString();
    }
}
