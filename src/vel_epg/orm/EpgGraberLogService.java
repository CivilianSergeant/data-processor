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
import java.util.logging.Level;
import java.util.logging.Logger;
import vel_epg.orm.dao.EpgGraberLog;

/**
 *
 * @author Himel
 */
public class EpgGraberLogService {
    
    private static final String TABLE = "epg_graber_logs";
        
    public static long Save(Connection c, EpgGraberLog epgGraberLog){
        try {
            
            c.setAutoCommit(true);
             
            String sql = "";
            if(epgGraberLog.getId() > 0){
                sql += "UPDATE "+TABLE;
                sql += " SET data_source_id=?,dl_start_datetime=?,dl_status=?,dl_end_datetime=?,ex_start_datetime=?,ex_status=?,ex_end_datetime=?";
                sql += " WHERE id=?";
            }else{
                sql += "INSERT INTO "+TABLE;
                sql += " (data_source_id,dl_start_datetime,dl_status,dl_end_datetime,ex_start_datetime,ex_status,ex_end_datetime,pid)";
                sql += " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
            }
            
            
            
            PreparedStatement stmt = c.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
            
            stmt.setInt(1, epgGraberLog.getDataSourceId());
            stmt.setString(2,epgGraberLog.getDlStartDatetime());
            stmt.setInt(3, (epgGraberLog.isDownloaded())? 1 : 0);
            stmt.setString(4,epgGraberLog.getDlEndDatetime());
            stmt.setString(5, epgGraberLog.getExStartDatetime());
            stmt.setInt(6, (epgGraberLog.isExecuted())? 1 : 0);
            stmt.setString(7, epgGraberLog.getExEndDatetime());
            stmt.setString(8, epgGraberLog.getPid());
            
            if(epgGraberLog.getId() > 0){
                stmt.setLong(8, epgGraberLog.getId());
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
    
    
    public static EpgGraberLog findLog(Connection conn, int id) throws SQLException{
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM "+TABLE+" WHERE id="+id;
        ResultSet rsEpgGraberLog = stmt.executeQuery(sql);
        EpgGraberLog epgGraberLog = null;
        if(rsEpgGraberLog.first()){
            epgGraberLog = new EpgGraberLog();
            epgGraberLog.setId(rsEpgGraberLog.getInt("id"));
            epgGraberLog.setDataSourceId(rsEpgGraberLog.getInt("data_source_id"));
            epgGraberLog.setDlStartDatetime(rsEpgGraberLog.getString("dl_start_datetime"));
            epgGraberLog.setDlEndDatetime(rsEpgGraberLog.getString("dl_end_datetime"));
            epgGraberLog.setDlStatus(Boolean.parseBoolean(rsEpgGraberLog.getString("dl_status")));
            epgGraberLog.setExStartDatetime(rsEpgGraberLog.getString("ex_start_datetime"));
            epgGraberLog.setExEndDatetime(rsEpgGraberLog.getString("ex_end_datetime"));
            epgGraberLog.setExStatus(Boolean.parseBoolean(rsEpgGraberLog.getString("ex_status")));
            epgGraberLog.setPid(rsEpgGraberLog.getString("pid"));
        }
        
        return epgGraberLog;
    }
    
}
