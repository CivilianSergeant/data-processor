package vel_epg.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vel_epg.BatchExecutionResult;
import vel_epg.lib.parser.Helper;
import vel_epg.orm.dao.Program;
import vel_epg.orm.dao.Event;

/**
 *
 * @author Himel
 */
public class ProgramEventService {
    
    private static final String TABLE = "program_events";
    private static BatchExecutionResult ber = null;
    
    public static long isPEventExistByPidStartStop(Connection c,long pid, String start, String stop) throws SQLException{
        
        ResultSet rs = null;
        Statement stmt = c.createStatement();
        String sqlCommand = "SELECT * FROM "+ TABLE + 
                " WHERE program_id = '"+pid+"' AND start_time='"+start+"' AND stop_time='"+stop+"'  LIMIT 1";
        
        
        rs = stmt.executeQuery(sqlCommand);
        if(rs.next()){
            return rs.getLong(1);
        }
        return 0;
        
    }
    
    public static BatchExecutionResult saveEvent(Connection c, Program ch) throws SQLException{

        
        //BatchExecutionResult ber = null;
        if(ch == null){
            return ber;
        }
        
        String insertSqlCommand = "INSERT INTO "+TABLE+" (program_id,start_time,stop_time,title,`desc`,lang,duration,insert_datetime,update_datetime,source_type_id,token)"
                + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        String updateSqlCommand = "UPDATE "+TABLE+" SET stop_time=?, title=?, `desc`=?, lang=?, duration=?, update_datetime=?, source_type_id=? WHERE id=?";

        
        PreparedStatement insertStmt = c.prepareStatement(insertSqlCommand,PreparedStatement.RETURN_GENERATED_KEYS);
        PreparedStatement updateStmt = c.prepareStatement(updateSqlCommand,PreparedStatement.RETURN_GENERATED_KEYS);
        
        long insertBatchCount = 0;
        long updateBatchCount = 0;
        
        if(ch.getProgrammes() != null && ch.getProgrammes().size() > 0){
            int i = 0;
            int len = ch.getProgrammes().size();
            ber = new BatchExecutionResult();
            for(Event p : ch.getProgrammes()){
                
                i++;
                if(i==len){
                    
                        ber.setLastProgramme(p);
                        //System.out.println("tHere:");
                    
                }
                // turn on default database commit for batch execution
                c.setAutoCommit(true);
                
                // for start time
                String timestamp = p.getStart();
                String[] t = timestamp.split(" ");
                String startTime = t[0];
                String tz = t[1];

                // for stop time
                timestamp = p.getStop();
                t = timestamp.split(" ");

                String stopTime = t[0];
                
                p.setOriginalStart(startTime);
                p.setOriginalStop(stopTime);

                startTime = ProgramEventService.getUTCTime(startTime,tz);
                stopTime  = ProgramEventService.getUTCTime(stopTime, tz);
                
                long isProgramEventExist = ProgramEventService.isPEventExistByPidStartStop(c, ch.getAutoId(), startTime, stopTime);
                
                
                
                p.setProgramId(ch.getAutoId());
                
                if(isProgramEventExist == 0){

                    p.setStart(startTime);
                    p.setStop(stopTime);
                    p.setDuration();
                    p.setInsertDateTime(getSystemTime());
                    p.setUpdateDateTime(getSystemTime());
                    
                    insertStmt.setInt(1,Integer.parseInt(p.getProgramId()));
                    insertStmt.setString(2,p.getStart());
                    insertStmt.setString(3,p.getStop());
                    insertStmt.setString(4,p.getTitle(0).trim());
                    String desc = p.getDesc(0);
                    if(desc != null){
                        desc = desc.trim();
                    }
                    insertStmt.setString(5,desc);
                    
                    List<String> langs = p.getLang();
                    String lang = null;
                    for(String s : langs){
                        if(s.contains("en") || s.contains("eng")){
                            lang = s;
                        }else if(s.toLowerCase().contains("bengali") || s.toLowerCase().contains("bn")){
                            lang = s;
                        }
                    }
                    //System.out.println("Name "+p.getTitle(0).trim()+" Start "+p.getStart()+" Stop "+p.getStop()+" Dur "+p.getDuration());
                    insertStmt.setString(6,lang);
                    insertStmt.setString(7,p.getDuration());
                    insertStmt.setString(8,p.getInsertDateTime());
                    insertStmt.setString(9,p.getUpdateDateTime());
                    insertStmt.setLong(10,ch.getSourceTypeId());
                    insertStmt.setString(11, Helper.getToken());
                    insertStmt.addBatch();
                    
                    insertBatchCount++;

                }else{
                    p.setStart(startTime);
                    p.setStop(stopTime);
                    p.setDuration();
                    p.setUpdateDateTime(getSystemTime());
                    
                    updateStmt.setString(1, p.getStop());
                    updateStmt.setString(2, p.getTitle(0).trim());
                    updateStmt.setString(3, p.getDesc(0));
                    
                    List<String> langs = p.getLang();
                    String lang = null;
                    for(String s : langs){
                        if(s.contains("en") || s.contains("eng")){
                            lang = s;
                        }else if(s.toLowerCase().contains("bengali") || s.toLowerCase().contains("bn")){
                            lang = s;
                        }
                    }
                    //System.out.println("Name "+p.getTitle(0).trim()+" Start "+p.getStart()+" Stop "+p.getStop()+" Dur "+p.getDuration());
                    updateStmt.setString(4, lang);
                    updateStmt.setString(5, p.getDuration());
                    updateStmt.setString(6, p.getUpdateDateTime());
                    updateStmt.setLong(7,ch.getSourceTypeId());
                    updateStmt.setString(8, String.valueOf(isProgramEventExist));
                    updateStmt.addBatch();
                    
                    updateBatchCount++;
                }
            }
            
            // turn off default database commit for batch execution
            c.setAutoCommit(false);
            
            int[] batchExecution;
            
            if(insertBatchCount > 0){
                batchExecution = insertStmt.executeBatch();
                ber.setBatchExecution(batchExecution);
            }
            
            if(updateBatchCount > 0){
                batchExecution = updateStmt.executeBatch();
                ber.setBatchExecution(batchExecution);
            }
            
            c.commit();
            return ber;
        }

        return ber;
    }
    
    public static String getUTCTime(String datetime, String tz){
        String utcDateTime = null;
        try {
            String sign = tz.substring(0,1);
            int hour = Integer.parseInt(tz.substring(1,3));
            int min  = Integer.parseInt(tz.substring(3,5));
            String date = datetime.substring(0,4)+"-"+datetime.substring(4,6)+"-"+datetime.substring(6,8);
            String time = datetime.substring(8,10)+":"+datetime.substring(10,12)+":"+datetime.substring(12,14);
            
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateObj = dateFormat.parse(date+" "+time);
            long timestamp = dateObj.getTime();
            long adj = ((hour*3600) + (min*60))*1000;
            long utcTimestamp = 0;
            Date newDate;
            
            if(sign.equals("+")){
                // minus timestamp
                
                utcTimestamp = (timestamp-adj);
                newDate = new Date(utcTimestamp);
                
            }else{
                // plus timestamp
                
                utcTimestamp = (timestamp+adj);
                newDate = new Date(utcTimestamp);
                
            }
            
            utcDateTime = dateFormat.format(newDate);
            
        } catch (ParseException ex) {
            Logger.getLogger(ProgramEventService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return utcDateTime;
    }
    
    public static String getConvertedTime(String datetime, String tz){
        String utcDateTime = null;
        try {
            String sign = tz.substring(0,1);
            int hour = Integer.parseInt(tz.substring(1,3));
            int min  = Integer.parseInt(tz.substring(3,5));
            String date = datetime.substring(0,4)+"-"+datetime.substring(4,6)+"-"+datetime.substring(6,8);
            String time = datetime.substring(8,10)+":"+datetime.substring(10,12)+":"+datetime.substring(12,14);
            
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateObj = dateFormat.parse(date+" "+time);
            long timestamp = dateObj.getTime();
            long adj = ((hour*3600) + (min*60))*1000;
            long utcTimestamp = 0;
            Date newDate;
            
            if(sign.equals("+")){
                // minus timestamp
                
                utcTimestamp = (timestamp+adj);
                newDate = new Date(utcTimestamp);
                
            }else{
                // plus timestamp
                
                utcTimestamp = (timestamp-adj);
                newDate = new Date(utcTimestamp);
                
            }
            
            utcDateTime = dateFormat.format(newDate);
            
        } catch (ParseException ex) {
            Logger.getLogger(ProgramEventService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return utcDateTime;
    }
    
    private static String getSystemTime(){
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }
         
}
