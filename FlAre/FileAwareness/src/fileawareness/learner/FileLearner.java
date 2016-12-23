/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness.learner;

import fileawareness.common.DBHandler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tintinmcleod
 */
public class FileLearner {
    
    private String __filePath;
    private byte[] __fileData;
    private String __md5Value;
    private String __fileExt;
    private long __fileSize;
    
    public FileLearner() {
        super();
    }
    
    public FileLearner(String filePath) {
        super();
        setFilePath(filePath);
    }
    
    public String getMd5() {
        return __md5Value;
    }
    
    public String getFileExt() {
        return __fileExt;
    }

    /**
     * @return the __filePath
     */
    public String getFilePath() {
        return __filePath;
    }

    /**
     * @param __filePath the __filePath to set
     */
    public void setFilePath(String __filePath) {
        this.__filePath = __filePath;
    }
    
    private String getFileExtension(File file) {
        /*
        http://java-demos.blogspot.com/2012/11/get-extension-of-file-in-java.html
        */
        String fileName = file.getName();
        String retval = new String();
        if (fileName.contains(".")) {
            retval = fileName.substring(fileName.lastIndexOf("."),fileName.length());
        }
        // no ".", means empty string returned
        return retval;
        
    }
    
    private String byteToHex(byte[] b) {
        // http://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
        
        String hexStr = "";
        for (int i=0; i < b.length; i++) {
            hexStr += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
//            hexStr += " ";
        }
        return hexStr.toUpperCase();
        
    }
    
    public void doFileLearning() {
        
        File file = new File(__filePath);
        
        // is it directory of file?
        if (file.isFile()) {
            // this is file, then process it
            // TODO: check maximum file size that can be processed.
            __fileSize = file.length();
//            System.err.println(Long.MAX_VALUE);
            // 1. get file extension and fileSize
            __fileExt = new String(getFileExtension(file));
            try {
                // 2. get md5 and return array of bytes[]
                MessageDigest md = MessageDigest.getInstance("MD5");
                __fileData = Files.readAllBytes(file.toPath());
                byte[] b = md.digest(getFileData());
                __md5Value = byteToHex(b);
                
                // write this file identity to database
                DBHandler dbhandler = new DBHandler();
                
                boolean isLearned = dbhandler.isFileAlreadyLearned(__md5Value);
                if (isLearned) {
                    System.err.println("File identic with "+file.getName()+" has been registered previously. Skip Learning process for this file. ");
                } else {
                    isLearned = dbhandler.isFileCurrentlyLearned(__md5Value);
                    if (isLearned) {
                        System.err.println("File identic with "+file.getName()+" has been learned, but interrupted or identic with another file in same fileset. Skip Learning process for this file. ");
                    } else {
                        int fileId = dbhandler.addFile(file.getName(), __fileExt, __fileSize, __md5Value);
                        // 3. extract n-gram in this file
                        NGramLearner ngram = new NGramLearner(__fileData, fileId, dbhandler);
                        ngram.doNGramExtraction();
//                        ngram.doNormalized();
                        
                    }
                }
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(FileLearner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FileLearner.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } else { // not a file? quit!
            System.err.println("Only file can be processed in this method.");
        }
    }

    /**
     * @return the __fileSize
     */
    public long getFileSize() {
        return __fileSize;
    }

    /**
     * @return the __fileData
     */
    public byte[] getFileData() {
        return __fileData;
    }
        
    
} // end of FileLearner
