/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vel_epg.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import vel_epg.lib.parser.XmlParser;

/**
 *
 * @author Himel
 */
public class ParserProcessor extends Thread{

    
    private String filename;
    private String srcType;
    private boolean parseStatus = false;
    
    private static final String XMLTV = "XMLTV";
    private static final String EXCEL = "EXCEL";
    private static final String CSV   = "CSV";
    private static final String CONFIG = "CONFIG";
    
    public ParserProcessor(String file){
        filename = file;
       
    }
    
    public boolean isParseCompleted(){
        return parseStatus;
    }
    
    public void setSourceType(String srcType){
        this.srcType = srcType;
    }
    
    
    @Override
    public void run() {
        try {
            
            File inputFile = new File(filename);
            
            
            if(srcType.equalsIgnoreCase(XMLTV)){
                System.out.print(filename);
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                SAXParser saxParser = factory.newSAXParser();
                XmlParser dh = new XmlParser();
                saxParser.parse(inputFile, dh);
                parseStatus = true;
            }
            
            if(srcType.equalsIgnoreCase(CONFIG)){
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                SAXParser saxParser = factory.newSAXParser();
                XmlParser dh = new XmlParser();
                saxParser.parse(inputFile, dh);
                
            }
            
            
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ParserProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ParserProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ParserProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    
}
