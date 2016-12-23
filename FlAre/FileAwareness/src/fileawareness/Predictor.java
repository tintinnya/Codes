/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness;

import fileawareness.common.Consts;
import fileawareness.predictor.FileExtPredictor;
import fileawareness.predictor.LibmagicPredictor;
import fileawareness.predictor.MetadataPredictor;
import fileawareness.predictor.NGramPredictor;
import java.io.File;

/**
 *
 * @author tintinmcleod
 */
public class Predictor {
    String __filePath;
    
    public Predictor(String filePath) {
        this.__filePath = filePath;
    }
    
    public void doPredict() {
        File file = new File(__filePath);
        if (!file.exists()) {
            System.err.println("File not found: "+file.getAbsolutePath());
            System.exit(Consts.ERROR_EXIT_CODE_FILENOTFOUND);
        }
        if (file.isFile()) {
            /* check with file extension. 
                Map it into a filetype database, 
                print it on the screen
                return of this process is a mapping number (e.g. 1 for DOC, 2 for XLS, etc)
            */
            System.out.println("Predicting file "+file.getAbsolutePath()+" using its extension.");
            FileExtPredictor fileExtPredictor = new FileExtPredictor(file);
            String fileExtRetval = fileExtPredictor.checkFileExtension();
            System.out.println("*** Result: "+fileExtRetval);
            
            /*
                check with command 'file'
                grep the some string that indicate the file description
                Map it into a file description database
                Print it on the screen
                return of this process is a mapping number (e.g 1 for Microsoft Word, 2 for Microsoft Excel, etc)

            */
            System.out.println("Predicting file "+file.getAbsolutePath()+" using libmagic 'file' command...");
            LibmagicPredictor libmagicPredictor = new LibmagicPredictor(file);
            String libmagicRetval = libmagicPredictor.checkMagic();
            System.out.println("*** Result: "+libmagicRetval);
            
            /*
                check with command 'exiftool'
                grep the some string that indicate the file description
                    File Type                       : DOC
                    MIME Type                       : application/msword
                    [DOC]
                        Software                        : Microsoft Word 9.0
                        Comp Obj User Type              : Microsoft Word Document
                    [JPG]
                        Software                        : Adobe Photoshop CS2 Windows
                        Image Size                      : 260x293
                        JFIF Version                    : 1.01
            
                Map it into a file description database
                Print it on the screen
                return of this process is a mapping number (e.g 1 for Microsoft Word, 2 for Microsoft Excel, etc)
            */
            System.out.println("Predicting file "+file.getAbsolutePath()+" using metadata viewer 'exiftool' command...");
            MetadataPredictor metadataPredictor = new MetadataPredictor(file);
            String metadataRetval = metadataPredictor.checkMetadata();
            System.out.println("*** Result: "+metadataRetval);
            /*
                NGram analysis
            
            */
            System.out.println("Predicting file "+file.getAbsolutePath()+" using n-gram Analysis...");
            NGramPredictor ngramPredictor = new NGramPredictor(file);
            String metadataNgram = ngramPredictor.checkNGram();
            System.out.println("*** Result: "+metadataNgram);
            
            boolean c1 = (fileExtRetval.intern() == libmagicRetval.intern());
            boolean c2 = (fileExtRetval.intern() == metadataRetval.intern());
            boolean c3 = (metadataRetval.intern() == libmagicRetval.intern());
            
            boolean consistent = (c1 && c2 && c3);
            
            if (consistent) {
                if (metadataNgram.contains(fileExtRetval)) {
                    consistent = true;
                } else {
                    consistent = false;
                }
            }
            
            String consistency = "";
            if (consistent) {
                consistency = "Consistent filetype of "+fileExtRetval;
            } else {
                consistency = "Inconsistency detected!";
            }
            
            
            System.out.println("*** Final Result: "+consistency);
            
            
        } else {
            System.err.println("Parameter input: "+file.getAbsolutePath()+" is not a file.");
//            System.exit(Consts.ERROR_EXIT_CODE_NOTAFILE);
            File[] files = file.listFiles();
            int fileNum = 0;
            int totalFiles = files.length;
            NGramPredictor ngramPredictor = null;
            for (File f : files) {
                if (f.isDirectory()) {
                    System.out.println("Skipping directory "+f.getAbsolutePath());
                } else {
                    fileNum++;
                    System.out.println("Processing "+fileNum+" of "+totalFiles+" file "+f.getAbsolutePath());
                    ngramPredictor = new NGramPredictor(f);
                    String metadataNgram = ngramPredictor.checkNGram();
                }
            } // end of for files;

        
        }
        System.out.println("Done.");

        
    }

    private void checkMetadata() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
