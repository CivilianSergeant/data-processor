/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vel_epg.lib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static vel_epg.VelEpg.config;


/**
 *
 * @author Himel
 */
public class Database {
    
    protected static Connection conn = null;
    private static String Host;
    private static String Port;
    private static String User;
    private static String Pass;
    private static String DB;
    private static final String CONFIG = "CONFIG";
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    
  
    private static void init(){
        try {
            
            // Read Configuration File
            String configFile = AppConfig.getConfigFile();
            //System.out.println(configFile);
            ParserProcessor parser = new ParserProcessor(configFile);
            parser.setSourceType(CONFIG);
            parser.start();
            parser.join();
            
            Host = config.getHostname().trim();
            Port = config.getPort().trim();
            User = config.getUsername().trim();
            Pass = config.getPassword().trim();
            DB   = config.getDbname().trim();
            
            Class.forName(DRIVER);
            String DNS = "jdbc:mysql://"+Host+":"+Port+"/"+DB;
            //System.out.println(DNS);
            conn = DriverManager.getConnection(DNS,User,Pass);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Connection getConnection(){
        if(conn == null){
            Database.init();
        }
        return conn;
    }
    
    public static void closeConnection(){
        if(conn != null){
            conn = null;
        }
    }
    
    
}
