package vel_epg.orm.dao;

/**
 *
 * @author Himel
 */
public class ManualProcessLog {
    
    private int id;
    private int programId;
    private String pid;
    private String startDateTime;
    private String endDateTime;
    private boolean processStartStatus;
    private boolean processEndStatus;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    
    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public boolean isProcessStarted() {
        return processStartStatus;
    }

    public void setProcessStartStatus(boolean processStartStatus) {
        this.processStartStatus = processStartStatus;
    }

    public boolean isProcessEnded() {
        return processEndStatus;
    }

    public void setProcessEndStatus(boolean processEndStatus) {
        this.processEndStatus = processEndStatus;
    }
    
    
    
}
