/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vel_epg.lib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Himel
 */
public class Downloader{
    private HttpURLConnection httpConn;
    private int contentLength;
    private FileOutputStream fw = null;
    private String filename;
    private String srcUrl;
    private String os;
    
    private boolean downloadStatus = false;
    
    public Downloader(String sourceUrl,String file) {
        
        try {
            contentLength = 0;
            filename = file;
            os = System.getProperty("os.name");
            srcUrl = sourceUrl; 
            if(srcUrl.contains("http")|| srcUrl.contains("https")){
                URL url = new URL(sourceUrl);
                httpConn =  (HttpURLConnection)url.openConnection();
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    
    public void runDownload() {
        try {
            int d;
            int contentDownloaded = 0;
            int bufferLen = 1024;
            contentLength = httpConn.getContentLength();
            InputStream is = httpConn.getInputStream();
            BufferedInputStream br = new BufferedInputStream(is);
            
            
            System.out.println("Content Length:"+ httpConn.getContentLength());
            
            File f;
            f = new File(filename);
            
            fw = new FileOutputStream(f);
            byte[] readBuffer = new byte[bufferLen];
            while((d = br.read(readBuffer, 0, bufferLen)) > 0){
                
                if(contentDownloaded <= 0)
                   System.out.println("Download started");
                
                String str = new String(readBuffer,0,d);

                String replace = str.replace("&amp;","@NEXAMP@");
                replace        = replace.replace("&pos;","@NEXPOS@");
                replace        = replace.replace("&quot;","");
                
                fw.write(replace.getBytes(), 0, replace.getBytes().length);
                contentDownloaded += d;
                
                if(os.startsWith("Unix") || os.startsWith("Linux")){
                    System.out.print("\33[1A\33[2k");
                    System.out.println("Bytes written "+contentDownloaded+"/"+ contentLength);
                    
                }else{
                    System.out.print("bytes written "+contentDownloaded+"/"+ contentLength+"\r");
                }
            }

            if(fw != null){
                fw = null;
                System.out.println("Download Completed");
                System.out.println();
            }
            
            //filterXML();
            downloadStatus = true;
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        
    }
    
    public void runCopy(){
        
        try {
            int d;
            int bufferLen = 1024;
            File sourceFile = new File(srcUrl);
            File distFile = new File(filename);
            
            fw = new FileOutputStream(distFile);
            FileInputStream fis = new FileInputStream(sourceFile);
          
            BufferedInputStream br = new BufferedInputStream(fis);
            byte[] readBuffer = new byte[bufferLen];
            while((d = br.read(readBuffer, 0, bufferLen)) > 0){
                
                
                String str = new String(readBuffer,0,d);

                String replace = str.replace("&amp;","@NEXAMP@");
                replace        = replace.replace("&pos;","@NEXPOS@");
                replace        = replace.replace("&quot;","");
                
                fw.write(replace.getBytes(), 0, replace.getBytes().length);
                
            }
            
            if(fw != null){
                fw = null;
                downloadStatus = true;
                System.out.println("File Copy Completed");
                System.out.println();
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
       
    
    public boolean isDownloaded(){
        return downloadStatus;
    }
    
    
    
    
    
    
}
