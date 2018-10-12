/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.logging.Level;
import java.util.logging.Logger;
import vel_epg.lib.parser.Helper;
import vel_epg.orm.dao.Program;
import vel_epg.orm.dao.Event;

/**
 *
 * @author Himel
 */
public class ProgramService {
    private static final String TABLE = "programs";
    
    public static long isProgramExistByProgramNameTag(Connection c,String name, String tag) throws SQLException{
        
        ResultSet rs = null;
        Statement stmt = c.createStatement();
        String sqlCommand = "SELECT * FROM "+ TABLE + 
                " WHERE program_name = '"+name+"' AND program_tag='"+tag+"' LIMIT 1";
        
        
        rs = stmt.executeQuery(sqlCommand);
        if(rs.next()){
            return rs.getLong(1);
        }
        return 0;
        
    }
    
    public static long saveChannel(Connection c, Program ch){
        Event p;
        String[] start;
        
        if(ch== null){
            return -1;
        }
        
        
            try {
                
                
                String timeOffset = null;
                if(ch.getProgrammes().size() > 0){
                    p = ch.getProgrammes().get(0);
                    start = p.getStart().split(" ");
                    timeOffset = start[1];
                }
                
                String sqlCommand = "INSERT INTO "+TABLE+" (program_name,program_tag,lang,logo_url,insert_update_timestamp,source_id,source_type_id,token) "
                        + " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement stmt = c.prepareStatement(sqlCommand,PreparedStatement.RETURN_GENERATED_KEYS);
                stmt.setString(1,ch.getDisplayName());
                stmt.setString(2,ch.getId());
                stmt.setString(3,ch.getLang());
                stmt.setString(4,ch.getIcon());
                stmt.setString(5, getSystemTime());
                stmt.setInt(6,ch.getSourceId());
                stmt.setLong(7, ch.getSourceTypeId());
                stmt.setString(8, Helper.getToken());
                
                //System.out.println(stmt);
                if(stmt.executeUpdate()>0){
                    ResultSet rs = stmt.getGeneratedKeys();
                    if(rs.first()){
                        return rs.getLong(1);
                    }
                }
                
            } catch (SQLException ex) {
                Logger.getLogger(ProgramService.class.getName()).log(Level.SEVERE, null, ex);
            }
        
       
        return 0;
    }
    
    public static long updateChannel(Connection c, Program p) throws SQLException{
        String updateCommand = "UPDATE "+TABLE+" SET insert_update_timestamp=? WHERE id=?";
        
        PreparedStatement pstmt = c.prepareStatement(updateCommand, PreparedStatement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, getSystemTime());
        pstmt.setLong(2, p.getAutoId());
        int res = pstmt.executeUpdate();
        if(res > 0){
            return res;
        }
        
        return 0;
    }
    
    
    public static long updateLastEventDateTime(Connection c, Event lastEvent){
        try {
            
            String updateDateTime = getLocalTimeinBDT(lastEvent.getStart());
            
            String updateSqlCommand = "UPDATE "+TABLE+" SET last_event_datetime=? WHERE id=?";
            PreparedStatement stmt = c.prepareStatement(updateSqlCommand,PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1,updateDateTime);
            stmt.setLong(2, Long.parseLong(lastEvent.getProgramId()));
            
            int res = stmt.executeUpdate(); 
            if(res > 0){
                return res;
            }
            
            
        } catch (SQLException ex) {
            Logger.getLogger(ProgramService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) { 
            Logger.getLogger(ProgramService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return 0;
    }
    
    public static long numberOfEventUpdate(Connection c, Program p) throws SQLException{
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        
        long todayTime = date.getTime();
        long tomorrowTime = todayTime + ((24*3600)*1000);
        long dayAfterTomorrowTime = todayTime + ((48*3600)*1000);
        
        String todayDate = dateFormat.format(date);
        
        date.setTime(tomorrowTime);
        String tomorrowDate = dateFormat.format(date);
        
        date.setTime(dayAfterTomorrowTime);
        String dayAfterTomorrowDate = dateFormat.format(date);
        
        //System.out.println(todayDate+" "+tomorrowDate+" "+dayAfterTomorrowDate);
        String pId = String.valueOf(p.getAutoId());
        
        String sqlCommand = "select * from  (SELECT count(program_id) as no_of_event from program_events WHERE start_time like '"+todayDate+"%' AND program_id = "+pId+") a,";
        sqlCommand += "(SELECT count(program_id) as no_of_event_tomorrow from program_events WHERE start_time like '"+tomorrowDate+"%' AND program_id = "+pId+") b,";
        sqlCommand += "(SELECT count(program_id) as no_of_event_dftomorrow from program_events WHERE start_time like '"+dayAfterTomorrowDate+"%' AND program_id = "+pId+") c";

        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(sqlCommand);
        
        String noOfEvent,noOfEventTomorrow,noOfEventDfTomorrow;
        
        if(rs.next()){
            noOfEvent = rs.getString("no_of_event");
            noOfEventTomorrow = rs.getString("no_of_event_tomorrow");
            noOfEventDfTomorrow = rs.getString("no_of_event_dftomorrow");
            
            String updateCommand = "UPDATE "+TABLE+" SET number_of_event_today=? , number_of_event_tomorrow=? , number_of_event_dftomorrow=?";
            updateCommand += " WHERE id=?";
            PreparedStatement pstmt = c.prepareStatement(updateCommand,PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, noOfEvent);
            pstmt.setString(2, noOfEventTomorrow);
            pstmt.setString(3, noOfEventDfTomorrow);
            pstmt.setLong(4, p.getAutoId());
            
            int res = pstmt.executeUpdate();
            
            if(res > 0){
                return res;
            }
        }
        
        return 0;
        
    }
    
    private static String getLocalTimeinBDT(String utcDateTime) throws ParseException{
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateObj = dateFormat.parse(utcDateTime.trim());
        long time = dateObj.getTime(); // time in miliseconds
        long adj = ((6*3600)*1000); // 1000 for miliseconds
        time = (time+adj);

        dateObj.setTime(time);
        
        return dateFormat.format(dateObj);
        
    }
    
    private static String getSystemTime(){
        
        Calendar c = Calendar.getInstance();
        Date dateObj = c.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(dateObj);
    }
    
    
}
