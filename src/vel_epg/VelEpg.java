/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vel_epg;

import java.io.BufferedWriter;
import java.io.File;
import vel_epg.orm.dao.Event;
import vel_epg.orm.dao.Program;
import vel_epg.lib.ParserProcessor;
import vel_epg.lib.Downloader;
import vel_epg.lib.AppConfig;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import vel_epg.lib.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import vel_epg.orm.EpgGraberDataSource;
import vel_epg.orm.EpgGraberLogService;
import vel_epg.orm.ManualProcessLogService;
import vel_epg.orm.ProgramService;
import vel_epg.orm.ProgramEventService;
import vel_epg.orm.dao.Config;
import vel_epg.orm.dao.EpgGraberLog;
import vel_epg.orm.dao.ManualProcessLog;

/**
 *
 * @author Himel
 */
public class VelEpg {

    public static Config config = null;
    public static List<Program> channels = null;
    
    protected static Connection conn = null;
    protected static Statement stmt = null;
    protected static ResultSet rs = null;
    
    private static int sourceId;
    private static int sourceTypeId;
    private static int sourceUrlType;
    private static String sourceUrl = null;
    private static String sourceType = null;
    
    
    private static final String XMLTV = "XMLTV";
    private static final String EXCEL = "EXCEL";
    private static final String CSV   = "CSV";
    private static final String FILENAME = "channel";
    
    private static int commandSrcId=0;
    private static int commandProcessId=0;
    private static boolean mProcess = false;
    private static boolean numOfEvent = false;
    private static boolean exportCsv = false;
    private static String exportDateTime = null;
    private static int operatorId = 0;
    
    
    public static void main(String[] args) {
        
        getCommand(args);
        long start_time = System.currentTimeMillis();
        
        //System.exit(0);
        try {
            AppConfig.initConfig();
            
            conn = Database.getConnection();
            
            //Generate CSV For Operator
            generateCSV();
            
            //Update no of events
            UpdateNoOfEvents();
            
            // Mannual Epg Process
            processManualEpg(conn);
            
            rs = new EpgGraberDataSource().getAllActiveSources(conn,commandSrcId);
            
            if(rs == null){
                return;
            }
            
            EpgGraberLog epgGraberLog = null;
            long epgGraberLogSaved = 0;
            String processId = null;
            
            rs.last();
            System.out.println("Total Source Fetched: "+rs.getRow());
            rs.beforeFirst();
            
            while(rs.next()){
                ParserProcessor parser;
                channels = new ArrayList<>();
                sourceUrl = rs.getString("source_url");
                sourceUrlType = rs.getInt("source_url_type");
                sourceType = rs.getString("graber_source_type");
                sourceId = rs.getInt("id");
                sourceTypeId = rs.getInt("source_type_id");
                epgGraberLog = new EpgGraberLog();
                
                if(commandSrcId >= 0 && processId == null && commandProcessId > 0){
                    epgGraberLog = EpgGraberLogService.findLog(conn, commandProcessId);
                    if(epgGraberLog != null)
                        processId = epgGraberLog.getPid();
                }
                
                if(processId != null && epgGraberLog.getId() == 0){
                    epgGraberLog.setPid(processId);
                }
                
                if(processId == null && epgGraberLog == null){
                    epgGraberLog = new EpgGraberLog();
                }
                
                epgGraberLog.setDataSourceId(sourceId);
                epgGraberLog.setDlStartDatetime(VelEpg.getSystemTime());
                epgGraberLogSaved = EpgGraberLogService.Save(conn, epgGraberLog);
                
                
                
                if(epgGraberLogSaved > 0){
                    String s;
                    if(sourceUrlType == 1){
                        s = "\r\nDownloading Source ";
                    }else{
                        s = "\r\nCopying Source ";
                    }
                    System.out.println(s+sourceUrl);
                    epgGraberLog.setId(epgGraberLogSaved);
                }

                // downloading xml from sourceUrl
                String filename = getFileName(FILENAME, sourceType);
                
                Downloader d = new Downloader(sourceUrl,filename);
                
                if(sourceUrlType == 1){
                    d.runDownload();
                }else{
                    d.runCopy();
                }
                
                if(d.isDownloaded()){
                    epgGraberLog.setDlStatus(true);
                    epgGraberLog.setDlEndDatetime(VelEpg.getSystemTime());
                    epgGraberLogSaved = EpgGraberLogService.Save(conn, epgGraberLog);
                
                    System.out.println("Parsing --- ");
                    
                    parser = new ParserProcessor(filename);
                    parser.setSourceType(sourceType);
                    parser.start();
                    parser.join();
                    System.out.println("\r\nParsing Completed");
                    
                    epgGraberLog.setExStartDatetime(VelEpg.getSystemTime());
                    epgGraberLogSaved = EpgGraberLogService.Save(conn, epgGraberLog);
                    System.out.println("\r\nDatabase Operation Started");
                }else{
                    
                    epgGraberLog.setDlStatus(false);
                    epgGraberLog.setDlEndDatetime(VelEpg.getSystemTime());
                    epgGraberLogSaved = EpgGraberLogService.Save(conn, epgGraberLog);
                    continue;
                }

                if((parser.isParseCompleted()==false) || (channels.size() <= 0)){
                    epgGraberLog.setExEndDatetime(VelEpg.getSystemTime());
                    epgGraberLogSaved = EpgGraberLogService.Save(conn, epgGraberLog);
                    return;
                }
                
                try{
                    BatchExecutionResult ber = null;
                    for(Program c : channels){
                        System.out.println("--------------------------------------");
                        conn.setAutoCommit(true);

                        long lastChannelInsertId;
                        long isChannelExist = ProgramService.isProgramExistByProgramNameTag(conn, c.getDisplayName(), c.getId());
                        
                        c.setSourceId(sourceId);
                        c.setSourceTypeId(sourceTypeId);
                        if(isChannelExist == 0){ // if no channel exist in db
                            lastChannelInsertId = ProgramService.saveChannel(conn, c);
                            c.setAutoId(lastChannelInsertId);
                            System.out.println("Channel "+c.getDisplayName()+"-"+c.getId()+" was saved [#"+lastChannelInsertId+"]");
                        }else{
                            lastChannelInsertId = isChannelExist;
                            c.setAutoId(lastChannelInsertId);
                            ProgramService.updateChannel(conn, c);
                        }

                        if(lastChannelInsertId > 0){

                            
                            ber = ProgramEventService.saveEvent(conn, c);

                            if(ber != null){
                                int totalEventsChanged = ber.getBatchExecution().length;
                                System.out.println("Total "+totalEventsChanged+" events was saved/updated for channel [#"+lastChannelInsertId+" "+c.getDisplayName()+"]");
                            }
                        }

                        // last event datetime update 
                        if((ber != null) && (ber.getLastProgrammes().size()>0)){
                            Event lastEvent = ber.getLastProgrammes().get(0);
                            long affectedRows = ProgramService.updateLastEventDateTime(conn,lastEvent);  
                            if(affectedRows > 0){
                                System.out.println("Program #"+c.getAutoId()+" last event infomration  was updated ");
                            }
                        }
                        
                        // number of event update
                        long affectedRows = ProgramService.numberOfEventUpdate(conn, c);
                        if(affectedRows>0){
                            System.out.println("Program #"+c.getAutoId()+" no of event infomration  was updated ");
                        }
                    }
                    
                    epgGraberLog.setExStatus(true);
                    epgGraberLog.setExEndDatetime(VelEpg.getSystemTime());
                    epgGraberLogSaved = EpgGraberLogService.Save(conn, epgGraberLog);
                
                }catch(Exception e){
                    
                    epgGraberLog.setExStatus(false);
                    epgGraberLog.setExEndDatetime(VelEpg.getSystemTime());
                    epgGraberLogSaved = EpgGraberLogService.Save(conn, epgGraberLog);
                }

                

            } // epg graber source loop end
            
            
            
        } catch (SQLException ex) {
            Logger.getLogger(VelEpg.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(VelEpg.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VelEpg.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLStreamException ex) {
            Logger.getLogger(VelEpg.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(VelEpg.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        
        
        long stop_time = System.currentTimeMillis();
       
        long diff_time = (stop_time - start_time);
        long diffSeconds = diff_time / 1000 % 60;
        long diffMinutes = diff_time / (60 * 1000) % 60;
        long diffHours = diff_time / (60 * 60 * 1000);

        String execStr="";
        System.out.println();

        execStr = "Total execution time: "+diffHours+" hours "+diffMinutes+" minutes "+diffSeconds+" seconds";


        System.out.println(execStr);
        System.out.println();
    }
    
    private static String getSystemTime(){
        
        Calendar c = Calendar.getInstance();
        Date dateObj = c.getTime();
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        return dateFormat.format(dateObj);
    }
    
    private static String getFileName(String f,String srcType){
        
        String filename = "./dist/"+f;
        if(srcType.equalsIgnoreCase(XMLTV)){
            filename += ".xml";
        }
        
        if(srcType.equalsIgnoreCase(EXCEL)){
            filename += ".xls";
        }
        
        if(srcType.equalsIgnoreCase(CSV)){
            filename += ".csv";
        }
        
        return filename;
    }
    
    
    private static void getCommand(String[] args){
        
        boolean srcId = false;
        boolean process = false;
        boolean manualProcess = false;
        boolean numberOfEvent = false;
        boolean exportCSVFlag = false;
        boolean exportDateTimeFlag = false;
        boolean operatorIdFlag = false;
        
        for(String a:args){
            if(a.equalsIgnoreCase("--src-id")){
                srcId = true;
                continue;
            }
            
            if(a.equalsIgnoreCase("-pid")){
                process = true;
                continue;
            }
            
            if(a.equalsIgnoreCase("-mprocess")){
                manualProcess = true;
                continue;
            }
            if(a.equalsIgnoreCase("-no-of-event")){
                numberOfEvent = true;
                continue;
            }
            
            if(a.equalsIgnoreCase("-export-csv")){
                exportCSVFlag = true;
                continue;
            }
            
            if(a.equalsIgnoreCase("-export-date")){
                exportDateTimeFlag = true;
                continue;
            }
            
            if(a.equalsIgnoreCase("-operator-id")){
                operatorIdFlag = true;
                continue;
            }

            if(process){
                commandProcessId = Integer.parseInt(a.trim());
                process = false;
            }
            
            if(srcId){
                commandSrcId = Integer.parseInt(a.trim());
                srcId = false;
            }
            
            if(manualProcess){
                mProcess = Boolean.parseBoolean(a.trim());
                manualProcess = false;
            }
            
            if(numberOfEvent){
                numOfEvent = Boolean.parseBoolean(a.trim());
                numberOfEvent = false;
            }
            
            if(exportCSVFlag){
                exportCsv = Boolean.parseBoolean(a.trim());
                exportCSVFlag = false;
            }
            
            if(exportDateTimeFlag){
                exportDateTime = a.trim();
                exportDateTimeFlag = false;
            }
            
            if(operatorIdFlag){
                operatorId = Integer.parseInt(a.trim());
                operatorIdFlag = false;
            }
        }
    }

    private static void processManualEpg(Connection conn) throws SQLException {
        if((mProcess == true) && commandProcessId > 0){
            channels = new ArrayList<>();
            
            
            ManualProcessLog manualProcessLog = null;
            manualProcessLog = ManualProcessLogService.findLog(conn, commandProcessId);
            
            if(manualProcessLog != null){
                manualProcessLog.setStartDateTime(getSystemTime());
                manualProcessLog.setProcessStartStatus(true);
                ManualProcessLogService.Save(conn, manualProcessLog);
            }
            
            Statement processLogStmt = conn.createStatement();
            String processLogSqlCommand = "SELECT * FROM epg_manual_process_logs WHERE id="+commandProcessId+" LIMIT 1";
            ResultSet rsProcessLogs = processLogStmt.executeQuery(processLogSqlCommand);
            //System.out.println(processLogSqlCommand);
            if(rsProcessLogs != null){
                int programId=0;
                
                while(rsProcessLogs.next()){
                    String _programId = rsProcessLogs.getString("program_id");
                    if(_programId != null || (Integer.parseInt(_programId) > 0)){
                        programId  = Integer.parseInt(_programId);
                    }
                    Statement getProgramStmt = conn.createStatement();
                    String getProgramSqlCommand = "SELECT * FROM programs WHERE is_manual=1 ";
                    if(programId>0){
                        getProgramSqlCommand += " AND id="+programId;
                    }
                    //System.out.println(getProgramSqlCommand);
                    ResultSet rsProgram = getProgramStmt.executeQuery(getProgramSqlCommand);
                    rsProgram.last();
                    System.out.println("Total Program Found: "+rsProgram.getRow());
                    rsProgram.beforeFirst();
                    Program program = null;
                    while(rsProgram.next()){
                        
                        program = new Program();
                        program.setAutoId(rsProgram.getInt("id"));
                        program.setDisplayName(rsProgram.getString("program_name"));
                        program.setId(rsProgram.getString("program_tag"));
                        program.setLang(rsProgram.getString("lang"));
                    
                        
                        Statement selectEpgManualStmt = conn.createStatement();
                        String selectEpgManualsSqlCommand = "SELECT epg_manual.*,if(epg_manual.epg_type = 'FIXED', " 
                        + " GROUP_CONCAT(epg_manual_repeat_times.repeat_date)," 
                        + " GROUP_CONCAT(epg_manual_repeat_times.week_days)) repeat_days," 
                        + " GROUP_CONCAT(repeat_time) repeat_time,GROUP_CONCAT(epg_manual_repeat_times.duration_in_sec) as repeat_duration FROM epg_manual LEFT JOIN epg_manual_repeat_times ON epg_manual_repeat_times.epg_id = epg_manual.id "
                        + " WHERE epg_manual.program_id=" + program.getAutoId() + " AND epg_manual.is_processed=0 GROUP BY epg_manual.id";
                        //System.out.println(selectEpgManualsSqlCommand);
                        ResultSet rsEpgManual = selectEpgManualStmt.executeQuery(selectEpgManualsSqlCommand);
                        rsEpgManual.last();
                        System.out.println("Total Manual Epg Found: "+rsEpgManual.getRow());
                        rsEpgManual.beforeFirst();
                        //System.exit(0);
                        while(rsEpgManual.next()){
                     //       System.out.println(rsEpgManual.getString("program_name"));
                     //       System.out.println(rsEpgManual.getString("epg_type"));
                            //System.exit(0);
                            if(rsEpgManual.getString("epg_type").contains("FIXED")){
                                
                                String startTime = rsEpgManual.getString("show_date")+" "+rsEpgManual.getString("start_time");
                                String endTime   = rsEpgManual.getString("show_date")+" "+rsEpgManual.getString("end_time");
                                startTime = startTime.replaceAll("\\W+", "");
                                endTime   = endTime.replaceAll("\\W+", "");
                                
                                Event event = new Event();
                                event.setStart(startTime+" +0600");
                                event.setStop(endTime+" +0600");
                                event.setTitle(rsEpgManual.getString("program_name").trim());
                                event.setDesc(rsEpgManual.getString("program_description").trim());
                                event.setSourceTypeId(4);
                                event.setProgramId(programId);
                                event.setLang(program.getLang());
                                program.setProgrammes(event);
                                
                                // EPG Repeat By Fixed
                                String repeatDays = rsEpgManual.getString("repeat_days");
                                String repeatEpgTimes = rsEpgManual.getString("repeat_time");
                                String repeatDuration = rsEpgManual.getString("repeat_duration");
                                
                                if(repeatDays != null && repeatEpgTimes != null){
                                    String repeatDates[] = repeatDays.split(",");
                                    String repeatTimes[] = repeatEpgTimes.split(",");
                                    String repeatDurations[] = repeatDuration.split(",");
                                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                                    Date dateObj = null;
                                    if(repeatDates.length == repeatTimes.length){
                                        int i=0;
                                        for(String repeatDate : repeatDates){
                                            String repeatTime = repeatTimes[i];
                                            String startRepeatDateTime = repeatDate+" "+repeatTime;
                                            try {

                                                dateObj = df.parse(startRepeatDateTime);
                                                int repeatDurationInMinute = (repeatDurations[i] != null)? Integer.parseInt(repeatDurations[i]) : 0;
                                                long duration = repeatDurationInMinute;
                                                long d_duration = dateObj.getTime();
                                                dateObj.setTime(d_duration + duration);
                                                String endRepeatDateTime = df.format(dateObj);

                                                startRepeatDateTime = startRepeatDateTime.replaceAll("\\W+","");
                                                endRepeatDateTime   = endRepeatDateTime.replaceAll("\\W+", "");

                                                Event repeatEvent = new Event();
                                                repeatEvent.setStart(startRepeatDateTime+" +0600");
                                                repeatEvent.setStop(endRepeatDateTime+" +0600");
                                                repeatEvent.setTitle(rsEpgManual.getString("program_name").trim());
                                                repeatEvent.setDesc(rsEpgManual.getString("program_description").trim());
                                                repeatEvent.setSourceTypeId(4);
                                                repeatEvent.setProgramId(programId);
                                                repeatEvent.setLang(program.getLang());
                                                program.setProgrammes(repeatEvent);

                                            } catch (ParseException ex) {
                                                Logger.getLogger(VelEpg.class.getName()).log(Level.SEVERE, null, ex);
                                            }

                                            i++;
                                        }
                                    }
                                }
                                
                            }else{
                                
                                String[] weekDays = rsEpgManual.getString("week_days").split(",");
                                if(weekDays.length>0){
                                    
                                    Calendar calendar = Calendar.getInstance();
                                    int currentDayNum = calendar.get(Calendar.DAY_OF_WEEK);
                                    for(String weekDay : weekDays){
                                            int dayNum = getDayNum(weekDay);
                                        
                                            int dayDiff = (dayNum - currentDayNum);
                                            
                                            Date dateObj = calendar.getTime();
                                            long longSeconds = 0;
                                            if(dayDiff < 0){
                                                longSeconds = (((dayDiff+7)*(24*60*60))*1000);
                                            }else{
                                                longSeconds = ((dayDiff*(24*60*60))*1000);
                                            }
                                            long timestamp = dateObj.getTime() + longSeconds;
                                            dateObj.setTime(timestamp);
                                            Event event = new Event();

                                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                                            String startTime = df.format(dateObj)+" "+rsEpgManual.getString("start_time");
                                            String endTime   = df.format(dateObj)+" "+rsEpgManual.getString("end_time");

                                            startTime = startTime.replaceAll("\\W+", "");
                                            endTime   = endTime.replaceAll("\\W+", "");

                                            event.setStart(startTime+" +0600");
                                            event.setStop(endTime+" +0600");
                                            event.setTitle(rsEpgManual.getString("program_name").trim());
                                            event.setDesc(rsEpgManual.getString("program_description").trim());
                                            event.setSourceTypeId(4);
                                            event.setProgramId(programId);
                                            event.setLang(program.getLang());
                                            program.setProgrammes(event);    
                                            
                                        
                                    }
                                    
                                }
                                
                                // Epg Repeat By Recurring
                                String repeatEpgWeekDays = rsEpgManual.getString("repeat_days");
                                String repeatEpgWeekTimes = rsEpgManual.getString("repeat_time");
                                String repeatDuration = rsEpgManual.getString("repeat_duration");
                                
                                if(repeatEpgWeekDays != null && repeatEpgWeekTimes != null){
                                    
                                    String repeatWeekDays[] = repeatEpgWeekDays.split(",");
                                    String repeatTimes[] = repeatEpgWeekTimes.split(",");
                                    String repeatDurations[] = repeatDuration.split(",");
                                    
                                    if(repeatWeekDays.length == repeatTimes.length){
                                        
                                        Calendar rCalendar = Calendar.getInstance();
                                        int rCurrentDayNum = rCalendar.get(Calendar.DAY_OF_WEEK);
                                        int i=0;
                                        for(String rWeekDay : repeatWeekDays){
                                            int rDayNum = getDayNum(rWeekDay);
                                            int rDayDiff = (rDayNum - rCurrentDayNum);
                                            Date rDateObj = rCalendar.getTime();
                                            long rLongSeconds = 0;
                                            if(rDayDiff < 0){
                                                rLongSeconds = (((rDayDiff+7)*(24*60*60))*1000);
                                            }else{
                                                rLongSeconds = ((rDayDiff*(24*60*60))*1000);
                                            }
                                            long rTimestamp = rDateObj.getTime() + rLongSeconds;
                                            rDateObj.setTime(rTimestamp);
                                            
                                            Event rEvent = new Event();

                                            DateFormat rDf = new SimpleDateFormat("yyyy-MM-dd");
                                            DateFormat rEdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            
                                            String rStartTime = rDf.format(rDateObj)+" "+repeatTimes[i];
                                            int repeatDurationInMinute = (repeatDurations[i] != null)? Integer.parseInt(repeatDurations[i]) : 0;
                                            long rDuration = repeatDurationInMinute;
                                            
                                            try { 
                                                Date rEndDateObj = rEdf.parse(rStartTime);
                                                
                                                long rEndSeconds = rEndDateObj.getTime();
                                                rEndSeconds = rEndSeconds+(rDuration*1000);
                                                rEndDateObj.setTime(rEndSeconds);
                                                String rEndTime = rEdf.format(rEndDateObj);
                                                //System.out.println("REPEAT DURATION "+rStartTime+" "+rEdf.format(rEndDateObj));
                                                rStartTime = rStartTime.replaceAll("\\W+", "");
                                                rEndTime = rEndTime.replaceAll("\\W+","");
                                                
                                                rEvent.setStart(rStartTime+" +0600");
                                                rEvent.setStop(rEndTime+" +0600");
                                                rEvent.setTitle(rsEpgManual.getString("program_name").trim());
                                                rEvent.setDesc(rsEpgManual.getString("program_description").trim());
                                                rEvent.setSourceTypeId(4);
                                                rEvent.setProgramId(programId);
                                                rEvent.setLang(program.getLang());
                                                program.setProgrammes(rEvent);   
                                                
                                            } catch (ParseException ex) {
                                                Logger.getLogger(VelEpg.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                           
                                            
                                            i++;
                                        }
                                    }
                                }
                                
                                
                            }
                        }
                        
                        channels.add(program);
                    } // end while loop
                    
                    
                }
                
                try{
                    
                    BatchExecutionResult ber = null;
                    for(Program c : channels){
                        ber = ProgramEventService.saveEvent(conn, c);
    
                        if(ber != null){
                            int totalEventsChanged = ber.getBatchExecution().length;
                            System.out.println("Total "+totalEventsChanged+" events was saved/updated for channel [#"+c.getAutoId()+" "+c.getDisplayName()+"]");
                        }
                        
                        // last event datetime update 
                        if((ber != null) && (ber.getLastProgrammes().size()>0)){
                            Event lastEvent = ber.getLastProgrammes().get(0);
                            long affectedRows = ProgramService.updateLastEventDateTime(conn,lastEvent);  
                            if(affectedRows > 0){
                                System.out.println("Program #"+c.getAutoId()+" last event infomration  was updated ");
                            }
                        }
                        
                        // number of event update
                        long affectedRows = ProgramService.numberOfEventUpdate(conn, c);
                        if(affectedRows>0){
                            System.out.println("Program #"+c.getAutoId()+" no of event infomration  was updated ");
                        }
                    }
                }catch(Exception e){
                    
                }
                
            }
            
            if(manualProcessLog != null){
                manualProcessLog.setEndDateTime(getSystemTime());
                manualProcessLog.setProcessEndStatus(true);
                ManualProcessLogService.Save(conn, manualProcessLog);
            }
            System.exit(0);
        }
    }
    
    private static void UpdateNoOfEvents(){
        if(numOfEvent){
            try {
                String getAllProgramsSql = "SELECT id from programs";
                Statement getAllProgramsStmt = conn.createStatement();
                ResultSet allProgramsResultSet = getAllProgramsStmt.executeQuery(getAllProgramsSql);
                while(allProgramsResultSet.next()){
                    Program program = new Program();
                    program.setAutoId(allProgramsResultSet.getInt("id"));
                    long affectedRows = ProgramService.numberOfEventUpdate(conn, program);
                    if(affectedRows>0){
                        System.out.println("Program #"+program.getAutoId()+" no of event infomration  was updated ");
                    }
                }

            } catch (SQLException ex) {
                Logger.getLogger(VelEpg.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.exit(0);
        }
        
    }
    
    
    
    private static void generateCSV(){
        if(exportCsv){
            
            if(exportDateTime == null){
                System.out.println("400 | Export date time not found");
                System.exit(0);
                
            }            
            
            try {
                // Get operator last sync
                String getOperatorSql = "SELECT id,userid,operator_last_sync FROM dvb_operators WHERE id = "+operatorId;
                Statement getOperatorStmt = conn.createStatement();
                ResultSet rsOperator = getOperatorStmt.executeQuery(getOperatorSql);
                int lastEventId = 0;
                String operatorUserId = "";
                
                if(rsOperator.first()){
                    lastEventId = rsOperator.getInt("operator_last_sync");
                    operatorUserId = rsOperator.getString("userid").trim().replaceAll("\\W+","_");
                }
                
                
                String getOperatorProgramMapSql = "SELECT programs.program_time_offset,program_events.id as event_id, program_events.title,program_events.desc,program_events.start_time,program_events.stop_time,program_events.duration, program_operator_maps.* ";
                       getOperatorProgramMapSql += " FROM program_operator_maps ";
                       getOperatorProgramMapSql += " JOIN programs ON programs.id = program_operator_maps.program_id";
                       getOperatorProgramMapSql += " JOIN program_events ON program_events.program_id = program_operator_maps.program_id";
                       getOperatorProgramMapSql += " WHERE operator_id = "+operatorId+" AND program_events.id > "+lastEventId;
                       getOperatorProgramMapSql += " AND program_events.start_time Like '"+exportDateTime+"%'";
                       getOperatorProgramMapSql += " ORDER BY program_events.program_id ASC,program_events.start_time asc;";
                Statement getOperatorProgramMapStmt = conn.createStatement();
                ResultSet operatorProgramMapRs = getOperatorProgramMapStmt.executeQuery(getOperatorProgramMapSql);
                operatorProgramMapRs.last();
                int itemCount = operatorProgramMapRs.getRow();
                operatorProgramMapRs.beforeFirst();
                //System.out.println(getOperatorProgramMapSql);
                if(itemCount > 0){
                    operatorUserId = ((operatorUserId.length()<3)? ((Math.random()*1000)+1)+"" : operatorUserId);
                    String dirPath = "./export-operator-csv/";
                    File fw = new File(dirPath+operatorUserId.toLowerCase()+".csv");
                    File dir = new File(dirPath);
                    if(!dir.isDirectory()){
                        dir.mkdirs();
                        dir.setWritable(true);
                    }

                    PrintWriter pw = new PrintWriter(fw);
                    DateFormat epgDf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    DateFormat parseEpgDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    while(operatorProgramMapRs.next()){

                        String title = operatorProgramMapRs.getString("title")
                                .replace("@NEXAMP@", "&amp;").replace("@NEXPOS@", "'").replace("\"", ""); 
                        String desc  = operatorProgramMapRs.getString("desc");
                        if(desc != null){        
                            desc  = desc.replace("@NEXAMP@", "&amp;").replace("@NEXPOS@", "'").replace("\"", ""); 
                        }
                        String startTime = operatorProgramMapRs.getString("start_time").trim();
                        String timeOffset = operatorProgramMapRs.getString("program_time_offset").trim();

                        String formatedStartTime = startTime.replaceAll("\\W+","");

                        String convertedStartTime = ProgramEventService.getConvertedTime(formatedStartTime, timeOffset);
                        Date epgDate = parseEpgDf.parse(convertedStartTime);
                        convertedStartTime = epgDf.format(epgDate);

                        String row = "";
                        row += '"'+operatorProgramMapRs.getString("service_id").trim()+'"';
                        row += ","+'"'+convertedStartTime+'"';
                        row += ","+operatorProgramMapRs.getString("duration").trim();
                        row += ","+'"'+title+'"';
                        row += ","+'"'+desc+'"'+"\r\n";
                        pw.write(row);
                        lastEventId = operatorProgramMapRs.getInt("event_id");
                    }

                    pw.close();

                    if(lastEventId > 0){
                        String updateLastSyncSql = "UPDATE dvb_operators SET operator_last_sync = ? WHERE id = ?";
                        PreparedStatement updateLastEventStmt = conn.prepareStatement(updateLastSyncSql,PreparedStatement.RETURN_GENERATED_KEYS);
                        updateLastEventStmt.setInt(1, lastEventId);
                        updateLastEventStmt.setInt(2, operatorId);
                        int updated = updateLastEventStmt.executeUpdate();
                        if(updated > 0){
                            System.out.println("Operator #"+operatorId+" information updated");
                        }
                    }
                
                }
                
                
                
            } catch (SQLException ex) {
                Logger.getLogger(VelEpg.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(VelEpg.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(VelEpg.class.getName()).log(Level.SEVERE, null, ex);
            }
        
            System.exit(0);
        }
    }
    
    private static int getDayNum(String day){
        switch(day.toLowerCase()){
            case "sun":
                return 1;
            case "mon":
                return 2;
            case "tue":
                return 3;
            case "wed":
                return 4;
            case "thu":
                return 5;
            case "fri":
                return 6;
            case "sat":
                return 7;
            default:
                return 0;
        }
    }
    
}
