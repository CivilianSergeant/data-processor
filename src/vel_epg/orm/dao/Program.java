/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vel_epg.orm.dao;

import java.util.ArrayList;
import java.util.List;

/**
 * Program Entity
 * @author Himel
 */
public class Program {
    
    private long _id;
    private String id;
    private String displayName;
    private String icon;
    private String lang;
    private int sourceId;
    private int sourceTypeId;
    private List<Event> programmes = new ArrayList<Event>();

    public void setAutoId(long i){
        this._id = i;
    }
    
    public long getAutoId(){
        return this._id;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getIcon(){
        return icon;
    }
    
    public void setIcon(String icon){
        this.icon = icon;
    }
    
    public String getLang(){
        return lang;
    }
    
    public void setLang(String lang){
        this.lang = lang;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public int getSourceTypeId() {
        return sourceTypeId;
    }

    public void setSourceTypeId(int sourceTypeId) {
        this.sourceTypeId = sourceTypeId;
    }
    
    public List<Event> getProgrammes() {
        return programmes;
    }

    public void setProgrammes(Event programme) {
        this.programmes.add(programme);
    }

    @Override
    public String toString() {
        return "Channel{" + "id=" + id + ", displayName=" + displayName + '}';
    }
    
    
    
    
}
