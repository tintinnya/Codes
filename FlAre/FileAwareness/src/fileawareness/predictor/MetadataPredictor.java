/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness.predictor;

import fileawareness.FlAre;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tintinmcleod
 */
public class MetadataPredictor {

    File __file;
    
    public MetadataPredictor(File file) {
        this.__file = file;
    }

    public String checkMetadata() {
        String[] cmd = {"/usr/bin/exiftool",__file.getAbsolutePath()};
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p;
        String retval = "";
        try {
            p = pb.start();
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            StringBuffer sb = new StringBuffer();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            } //
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#checkMetadata: "+sb.toString());
            retval = interpretOutput(sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(LibmagicPredictor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retval;
    }

    public String interpretOutput(String line) {
/*
MIME Type                       : image/jpeg
MIME Type                       : image/bmp
MIME Type                       : image/png
MIME Type                       : image/gif
MIME Type                       : application/msword
MIME Type                       : application/vnd.openxmlformats-officedocument.wordprocessingml.document
MIME Type                       : application/vnd.ms-excel
MIME Type                       : application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
MIME Type                       : application/vnd.ms-powerpoint
MIME Type                       : application/vnd.openxmlformats-officedocument.presentationml.presentation
        */        
        String retval = "";
        String input = line;
        if (input.contains("image/jpeg")) {
            retval = ".jpg";
        } else if (input.contains("image/bmp")) {
            retval = ".bmp";
        } else if (input.contains("image/png")) {
            retval = ".png";
        } else if (input.contains("image/gif")) {
            retval = ".gif";
        } else if (input.contains("application/msword")) {
            retval = ".doc";
        } else if (input.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            retval = ".docx";
        } else if (input.contains("application/vnd.ms-excel")) {
            retval = ".xls";
        } else if (input.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            retval = ".xlsx";
        } else if (input.contains("application/vnd.ms-powerpoint")) {
            retval = ".ppt";
        } else if (input.contains("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
            retval = ".pptx";
        } else if (input.contains("application/pdf")) {
            retval = ".pdf";
        } else {
            retval = ".unkn";
        }
        
        
        return retval;
    }
    
    
    
}
