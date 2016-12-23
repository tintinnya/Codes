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
public class LibmagicPredictor {
    File __file;
    
    public LibmagicPredictor(File file) {
        this.__file = file;
    }
    //  http://stackoverflow.com/questions/18240944/cannot-launch-shell-script-with-arguments-using-java-processbuilder
    public String checkMagic() {
        String[] cmd = {"/usr/bin/file",__file.getAbsolutePath()};
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
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#checkMagic: "+sb.toString());
            retval = interpretOutput(sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(LibmagicPredictor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retval;
    }
    
    public String interpretOutput(String line) {
/*
        987041.png: JPEG image data, JFIF standard 1.01
        1.jpg: JPEG image data, JFIF standard 1.02
        2.bmp: PC bitmap, Windows 3.x format, 1920 x 1080 x 24
        3.png: PNG image data, 1440 x 900, 8-bit/color RGB, non-interlaced
        4.gif: GIF image data, version 89a, 788 x 446
        880064.doc: Composite Document File V2 Document, Little Endian, Os: MacOS, Version 10.3, Code page: 10000, Title: a. Approval (2/8/94), Author: S. Peter Gary, Template: Normal, Last Saved By: Laurie Hixson, Revision Number: 2, Name of Creating Application: Microsoft Word 8.0, Last Printed: Wed Jul 15 16:23:00 1998, Create Time/Date: Wed Jul  7 17:40:00 1999, Last Saved Time/Date: Wed Jul  7 17:40:00 1999, Number of Pages: 1, Number of Words: 260, Number of Characters: 1487, Security: 0        
        615326.docx: Microsoft Word 2007+        
        901341.xls: Composite Document File V2 Document, Little Endian, Os: Windows, Version 5.0, Code page: 1252, Author: LocalAdmin, Last Saved By: trenta, Name of Creating Application: Microsoft Excel, Create Time/Date: Tue Dec  4 13:12:20 2001, Last Saved Time/Date: Wed Nov 26 15:47:04 2003, Security: 0
        848990.xlsx: Microsoft Excel 2007+
        948335.ppt: Composite Document File V2 Document, Little Endian, Os: Windows, Version 4.10, Code page: 1252, Title: Corn stover\243 what do we know about what makes a good feedstock? , Author: John Sheehan, Last Saved By: Anne Ehrenshaft, Revision Number: 8, Name of Creating Application: Microsoft PowerPoint, Total Editing Time: 13:17:45, Last Printed: Sun Jun 24 16:58:01 2001, Create Time/Date: Thu Nov  8 00:16:05 2001, Last Saved Time/Date: Tue Nov 13 19:22:15 2001, Number of Words: 2510
        996017.pptx: Microsoft PowerPoint 2007+
*/        
        String retval = "";
        String input = line;
        if (input.contains("JPEG image data")) {
            retval = ".jpg";
        } else if (input.contains("PC bitmap")) {
            retval = ".bmp";
        } else if (input.contains("PNG image data")) {
            retval = ".png";
        } else if (input.contains("GIF image data")) {
            retval = ".gif";
        } else if (input.contains("Name of Creating Application: Microsoft Word")) {
            retval = ".doc";
        } else if (input.contains("Microsoft Word 2007+")) {
            retval = ".docx";
        } else if (input.contains("Name of Creating Application: Microsoft Excel")) {
            retval = ".xls";
        } else if (input.contains("Microsoft Excel 2007+")) {
            retval = ".xlsx";
        } else if (input.contains("Name of Creating Application: Microsoft PowerPoint")) {
            retval = ".ppt";
        } else if (input.contains("Microsoft PowerPoint 2007+")) {
            retval = ".pptx";
        } else if (input.contains("PDF document, version")) {
            retval = ".pdf";
        } else {
            retval = ".unkn";
        }
        
        
        return retval;
    }
}
