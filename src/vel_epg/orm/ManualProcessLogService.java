package vel_epg.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import vel_epg.orm.dao.ManualProcessLog;

/**
 *
 * @author Himel
 */
public class ManualProcessLogService {
    
    private static final String TABLE = "epg_manual_process_logs";
    
    public static long Save(Connection c, ManualProcessLog manualProcessLog){
        try {
            
            c.setAutoCommit(true);
             
            String sql = "";
            if(manualProcessLog.getId() > 0){
                sql += "UPDATE "+TABLE;
                sql += " SET program_id=?,pid=?,start_datetime=?,end_datetime=?,process_start_status=?,process_end_status=?";
                sql += " WHERE id=?";
            }else{
                sql += "INSERT INTO "+TABLE;
                sql += " (program_id,pid,start_datetime,end_datetime,process_start_status,process_end_status)";
                sql += " VALUES(?, ?, ?, ?, ?, ?)";
            }
            
            
            
            PreparedStatement stmt = c.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
            
            stmt.setInt(1, manualProcessLog.getProgramId());
            stmt.setString(2,manualProcessLog.getPid());
            stmt.setString(3, manualProcessLog.getStartDateTime());
            stmt.setString(4, manualProcessLog.getEndDateTime());
            stmt.setInt(5, (manualProcessLog.isProcessStarted())? 1 : 0);
            stmt.setInt(6, (manualProcessLog.isProcessEnded())? 1 : 0);
            
            if(manualProcessLog.getId() > 0){
                stmt.setLong(7, manualProcessLog.getId());
            }
            
            if(stmt.executeUpdate()>0){
                //System.out.println("\r\n"+epgGraberLog.toString());
                ResultSet rs = stmt.getGeneratedKeys();
                if(rs.first()){
                    return rs.getLong(1);
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(EpgGraberLogService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return 0;
    }
    
    
    public static ManualProcessLog findLog(Connection conn, int id) throws SQLException{
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM "+TABLE+" WHERE id="+id;
        ResultSet rsManualProcessLog = stmt.executeQuery(sql);
        ManualProcessLog manualProcessLog = null;
        if(rsManualProcessLog.first()){
            manualProcessLog = new ManualProcessLog();
            manualProcessLog.setId(rsManualProcessLog.getInt("id"));
            manualProcessLog.setStartDateTime(rsManualProcessLog.getString("start_datetime"));
            manualProcessLog.setEndDateTime(rsManualProcessLog.getString("end_datetime"));
            manualProcessLog.setProcessStartStatus(Boolean.parseBoolean(rsManualProcessLog.getString("process_start_status")));
            manualProcessLog.setProcessEndStatus(Boolean.parseBoolean(rsManualProcessLog.getString("process_end_status")));
            manualProcessLog.setPid(rsManualProcessLog.getString("pid"));
            manualProcessLog.setProgramId(rsManualProcessLog.getInt("program_id"));
        }
        
        return manualProcessLog;
    }
    
}
