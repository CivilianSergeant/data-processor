/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vel_epg.orm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Himel
 */
public class EpgGraberDataSource {
    
    private static final String TABLE = "epg_graber_data_sources";
    
    public ResultSet getAllActiveSources(Connection c,int srcTypeId) throws SQLException{
        Statement stmt = c.createStatement();
        String sqlCommand = "SELECT "+TABLE+".*,graber_source_type FROM "+TABLE+" JOIN graber_source_types ON"
                + " graber_source_types.id="+TABLE+".source_type_id WHERE is_active=1";
        
        if(srcTypeId > 0){
            sqlCommand = sqlCommand.concat(" AND "+TABLE+".id="+srcTypeId);
        }
        //System.out.println(sqlCommand);
        ResultSet rs = stmt.executeQuery(sqlCommand);
        return rs;
    }
}
