/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vel_epg.orm.dao;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Event Entity
 * @author Himel
 */
public class Event {
    
    private String channel;
    private String start;
    private String stop;
    private List<String> lang = new ArrayList<>();
    private List<String> title = new ArrayList<>();
    private List<String> desc = new ArrayList<>();
    private String programId;
    private int sourceTypeId;
    private String duration;
    private String insertDateTime;
    private String updateDateTime;
    private String originalStart;
    private String originalStop;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public String getLang(int index) {
        return lang.get(index);
    }
    public List<String> getLang(){
        return lang;
    }

    public void setLang(String lang) {
        this.lang.add(lang);
    }

    public String getTitle(int index) {
        return title.get(index);
    }

    public void setTitle(String title) {
        this.title.add(title);
    }

    public String getDesc(int index) {
        if(desc.size() > 0){
            return desc.get(index);
        }else{
            return null;
        }
    }

    public void setDesc(String desc) {
        this.desc.add(desc);
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(long programId) {
        this.programId = String.valueOf(programId);
    }

    public int getSourceTypeId() {
        return sourceTypeId;
    }

    public void setSourceTypeId(int sourceTypeId) {
        this.sourceTypeId = sourceTypeId;
    }
   
    public String getInsertDateTime() {
        return insertDateTime;
    }

    public void setInsertDateTime(String insertDateTime) {
        this.insertDateTime = insertDateTime;
    }

    public String getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(String updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public String getOriginalStart() {
        return originalStart;
    }

    public void setOriginalStart(String originalStart) {
        this.originalStart = originalStart;
    }

    public String getOriginalStop() {
        return originalStop;
    }

    public void setOriginalStop(String originalStop) {
        this.originalStop = originalStop;
    }
    
    
    
    
    

    @Override
    public String toString() {
        return "Programme{" + "channel=" + channel + ", programId="+programId+", start=" + start + ", stop=" + stop + ", lang=" + lang + ", title=" + title + ", desc=" + desc + ", duration="+ duration +'}';
    }
    
    public void setDuration(){
        
        if(this.start != null && this.stop != null){
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startDateObj = dateFormat.parse(this.start);
                Date stopDateObj = dateFormat.parse(this.stop);
                
                long diff = stopDateObj.getTime() - startDateObj.getTime();
                long diffSeconds = diff / 1000 % 60;
                long diffMinutes = diff / (60 * 1000) % 60;
                long diffHours = diff / (60 * 60 * 1000);
                //int diffInDays = (int) ((stopDateObj.getTime() - startDateObj.getTime()) / (1000 * 60 * 60 * 24));

                long hours = 0;
                long minutes  = 0;
                long seconds  = 0;
                
//                if (diffHours >= 1) {
//
//                    hours = diffHours;
//                    
//                } else if (diffMinutes >= 1) {
//                    minutes = (diffMinutes>9) ? String.valueOf(diffMinutes) : "0"+String.valueOf(diffMinutes);
//                    
//                }else if(diffSeconds > 1){
//                    seconds = (diffSeconds>9) ? String.valueOf(diffSeconds) : "0"+String.valueOf(diffSeconds);
//                }
                hours = (diffHours * 3600);
                minutes = (diffMinutes * 60);
                seconds = diffSeconds;
                
                this.duration = String.valueOf((hours+minutes+seconds));
                //System.out.println(this.toString());
            } catch (ParseException ex) {
                Logger.getLogger(Event.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    public String getDuration() {
        return this.duration;
    }
    
    
    
    
}
