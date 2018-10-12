/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vel_epg.orm.dao;

/**
 *
 * @author Himel
 */
public class EpgGraberLog {
    
    private long id=0;
    private int dataSourceId;
    private String dlStartDatetime;
    private boolean dlStatus;
    private String dlEndDatetime;
    private String exStartDatetime;
    private boolean exStatus;
    private String exEndDatetime;
    private String pid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public int getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(int dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getDlStartDatetime() {
        return dlStartDatetime;
    }

    public void setDlStartDatetime(String dlStartDatetime) {
        this.dlStartDatetime = dlStartDatetime;
    }

    public boolean isDownloaded() {
        return dlStatus;
    }

    public void setDlStatus(boolean dlStatus) {
        this.dlStatus = dlStatus;
    }

    public String getDlEndDatetime() {
        return dlEndDatetime;
    }

    public void setDlEndDatetime(String dlEndDatetime) {
        this.dlEndDatetime = dlEndDatetime;
    }

    public String getExStartDatetime() {
        return exStartDatetime;
    }

    public void setExStartDatetime(String exStartDatetime) {
        this.exStartDatetime = exStartDatetime;
    }

    public boolean isExecuted() {
        return exStatus;
    }

    public void setExStatus(boolean exStatus) {
        this.exStatus = exStatus;
    }

    public String getExEndDatetime() {
        return exEndDatetime;
    }

    public void setExEndDatetime(String exEndDatetime) {
        this.exEndDatetime = exEndDatetime;
    }

    @Override
    public String toString() {
        return "EpgGraberLog{" + "id=" + id + ", dataSourceId=" + dataSourceId + ", dlStartDatetime=" + dlStartDatetime + ", dlStatus=" + dlStatus + ", dlEndDatetime=" + dlEndDatetime + ", exStartDatetime=" + exStartDatetime + ", exStatus=" + exStatus + ", exEndDatetime=" + exEndDatetime + '}';
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
    
    public String getPid(){
        return this.pid;
    }
    
    
    
    
    
    
    
}
