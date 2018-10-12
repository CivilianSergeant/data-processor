/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vel_epg;

import vel_epg.orm.dao.Event;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Himel
 */
public class BatchExecutionResult {

    private List<Event> lastProgramme = new ArrayList<>();
    private int[] batchExecution;

//    public BatchExecutionResult() {
//        lastProgramme = new ArrayList<>();
//        batchExecution = null;
//    }
    
    public List<Event> getLastProgrammes(){
        return lastProgramme;
    }
    
    public Event getLastProgramme(int i) {
        return lastProgramme.get(i);
    }

    public void setLastProgramme(Event lp) {
        this.lastProgramme.add(lp);
    }

    public int[] getBatchExecution() {
        return batchExecution;
    }

    public void setBatchExecution(int[] batchExecution) {
        this.batchExecution = batchExecution;
    }
    
    
    
}
