/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vel_epg.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Himel
 */
public class AppConfig {
    
    private static final String configDir = "./dist/config";
    private static final String configFileName = configDir+"/config.xml";
    
    
    public static void initConfig() throws FileNotFoundException, XMLStreamException, UnsupportedEncodingException{
        
        File dir = new File(configDir);
        if(!dir.isDirectory()){
            dir.mkdirs();
        }
        
        File configFile = new File(configFileName);
        if(!configFile.isFile()){
            System.out.println("Application Configuration not found");
            OutputStream outputStream = new FileOutputStream(configFile);
            XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(new OutputStreamWriter(outputStream,"utf-8"));
            xmlStreamWriter.writeStartDocument();
            xmlStreamWriter.writeCharacters("\r\n");
            xmlStreamWriter.writeStartElement("application");
            
            xmlStreamWriter.writeCharacters("\r\n    ");
            xmlStreamWriter.writeStartElement("configure");

            xmlStreamWriter.writeCharacters("\r\n        ");
            xmlStreamWriter.writeStartElement("hostname");
            xmlStreamWriter.writeCharacters("localhost");
            xmlStreamWriter.writeEndElement();

            xmlStreamWriter.writeCharacters("\r\n        ");
            xmlStreamWriter.writeStartElement("port");
            xmlStreamWriter.writeCharacters("3306");
            xmlStreamWriter.writeEndElement();

            xmlStreamWriter.writeCharacters("\r\n        ");
            xmlStreamWriter.writeStartElement("username");
            xmlStreamWriter.writeCharacters("root");
            xmlStreamWriter.writeEndElement();

            xmlStreamWriter.writeCharacters("\r\n        ");
            xmlStreamWriter.writeStartElement("password");
            xmlStreamWriter.writeCharacters("");
            xmlStreamWriter.writeEndElement();

            xmlStreamWriter.writeCharacters("\r\n        ");
            xmlStreamWriter.writeStartElement("dbname");
            xmlStreamWriter.writeCharacters("vel_epg");
            xmlStreamWriter.writeEndElement();

            xmlStreamWriter.writeCharacters("\r\n    ");
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\r\n");
            xmlStreamWriter.writeEndElement();

            xmlStreamWriter.writeEndDocument();

            xmlStreamWriter.close();
            
            System.out.println("Configuration file created with default settings at ./dist/config/config.xml");
            System.out.println("Please change config.xml settings to run application properly");
            System.exit(0);
        }
    }

    public static String getConfigFile() {
        return configFileName;
    }
}
