/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vel_epg.lib.parser;

import vel_epg.orm.dao.Event;
import vel_epg.orm.dao.Program;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import vel_epg.orm.dao.Config;
import static vel_epg.VelEpg.config;
import static vel_epg.VelEpg.channels;



/**
 *
 * @author Himel
 */
public class XmlParser extends DefaultHandler{

    Program channel;
    Event programme;
    
    boolean channelDisplayName = false;
    boolean programmeTitle = false;
    boolean programmeDesc = false;
    boolean programmeSubTitle = false;
    boolean hostname = false;
    boolean port = false;
    boolean username = false;
    boolean password = false;
    boolean dbname = false;
    

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
        
        if(qName.equalsIgnoreCase("channel")){
            channel = new Program();
            channel.setId(attr.getValue("id"));    
        }
        
        if(qName.equalsIgnoreCase("programme")){
            programme = new Event();
            programme.setChannel(attr.getValue("channel"));
            programme.setStart(attr.getValue("start"));
            programme.setStop(attr.getValue("stop"));
        }
        
        if(qName.equalsIgnoreCase("title")){
            
            programme.setLang(attr.getValue("lang"));
            programmeTitle = true;
            
        }
        
        if(qName.equalsIgnoreCase("desc")){
            programmeDesc = true;
        }
        
        if(qName.equalsIgnoreCase("sub-title")){
            programmeSubTitle = true;
        }
        
        if(qName.equalsIgnoreCase("display-name")){
            channel.setLang(attr.getValue("lang"));
            channelDisplayName = true;
        }
        
        if(qName.equalsIgnoreCase("icon")){
            channel.setIcon(attr.getValue("src"));
        }
        
        if(qName.equalsIgnoreCase("configure")){
            config = new Config();
        }
        
        if(qName.equalsIgnoreCase("hostname")){
            hostname = true;
        }
        
        if(qName.equalsIgnoreCase("port")){
            port = true;
        }
        
        if(qName.equalsIgnoreCase("username")){
            username = true;
        }
        
        if(qName.equalsIgnoreCase("password")){
            password = true;
        }
        
        if(qName.equalsIgnoreCase("dbname")){
            dbname = true;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(channelDisplayName){
            channel.setDisplayName(new String(ch,start,length));
            channelDisplayName = false;
        }
        
        if(programmeTitle){
            programme.setTitle(new String(ch,start,length));
            programmeTitle = false;
        }
        
        if(programmeDesc){
            programme.setDesc(new String(ch,start,length));
            programmeDesc = false;
        }
        
        if(programmeSubTitle){
            programme.setDesc(new String(ch,start,length));
            programmeSubTitle = false;
        }
        
        if(hostname){
            config.setHostname(new String(ch,start,length));
            hostname = false;
        }
        
        if(port){
            config.setPort(new String(ch,start,length));
            port = false;
        }
        
        if(username){
            config.setUsername(new String(ch,start,length));
            username = false;
        }
        
        if(password){
            config.setPassword(new String(ch,start,length));
            password = false;
        }
        
        if(dbname){
            config.setDbname(new String(ch,start,length));
            dbname = false;
        }
        
        
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equalsIgnoreCase("channel")){
            //System.out.println(channel);
            if(channels != null){
                channels.add(channel);
            }
            
        }
        
        if(qName.equalsIgnoreCase("programme")){
            int i = 0;
            for(Program c : channels){
                if(c.getId().equals(programme.getChannel())){
                    ((Program)channels.get(i)).setProgrammes(programme);
                }
                i++;
            }
        }
        
        
    }
    
    
    
    
    

    
    
}
