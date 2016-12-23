/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness.predictor;

import fileawareness.FlAre;
import java.io.File;

/**
 *
 * @author tintinmcleod
 */
public class FileExtPredictor {
    File __file;
    
    public FileExtPredictor(File file) {
        this.__file = file;
    }
    
    public String checkFileExtension() {
        String fileName = __file.getName();
        String retval = new String();
        if (fileName.contains(".")) {
            retval = fileName.substring(fileName.lastIndexOf("."),fileName.length());
        }
        if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#checkFileExtension: File "+fileName+" has extension "+retval);
        return retval;
    }
    
}
