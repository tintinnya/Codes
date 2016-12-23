/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness;

import fileawareness.common.Consts;
import fileawareness.common.DBHandler;
import fileawareness.learner.FileLearner;
import fileawareness.learner.NGramLearner;
import java.io.File;

/**
 *
 * @author tintinmcleod
 */
public class Learner {
    
    private String __fileType;
    private String __filePath;
    private int __fileNum = 0;
    private int __dirNum = 0;
    
    public Learner() {
        super();
    }
    
    public Learner(String fileType, String filePath) {
        setFileType(fileType);
        setFilePath(filePath);
    }

    /**
     * @return the fileType
     */
    public String getFileType() {
        return __fileType;
    }

    /**
     * @param fileType the fileType to set
     */
    public void setFileType(String fileType) {
        this.__fileType = fileType;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return __filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.__filePath = filePath;
    }
    
    public void doLearn() {
        // this method should determine whether file or directory,
        // --> if it is directory, traverse it one by one, but not recursively
        //      to handle recursive learn, try to avoid StackOverflowError on JVM
        // --> if it is file, then process it with 
        //      1. get file extension, get filename, get filepath
        //      2. extract md5
        //      3. extract n-gram up to 20-gram
        // 
        File filePath = new File(__filePath);
        if (!filePath.exists()) {
            System.err.println("File not found: "+filePath.getAbsolutePath());
            System.exit(Consts.ERROR_EXIT_CODE_FILENOTFOUND);
        }

        FileLearner learner = new FileLearner();
        if (filePath.isFile()) {
            __fileNum++;
            System.out.println("Processing one single file "+filePath.getAbsolutePath());
            learner.setFilePath(filePath.getAbsolutePath());
            learner.doFileLearning();
        } else if (filePath.isDirectory()) {
            File[] files = filePath.listFiles();
            int totalFiles = files.length;
            for (File file : files) {
                if (file.isDirectory()) {
                    __dirNum++;
                    totalFiles--;
                    System.out.println("Skipping directory "+file.getAbsolutePath());
                } else {
                    __fileNum++;
                    System.out.println("Processing "+__fileNum+" of "+totalFiles+" file "+file.getAbsolutePath());
                    learner.setFilePath(file.getAbsolutePath());
                    learner.doFileLearning();
                }
            } // end of for files;

        }
        System.out.println("Processing "+__fileNum+" files and skipping "+__dirNum+" directories");

    
    } // end of doLearn()
    
    
} // end of class
