/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness.common;

import fileawareness.learner.entity.NGramTableEntity;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tintinmcleod
 */
public class GramDump {
    
    public GramDump() {
        
    } // end of constructor
    
    //  http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
    public static String gramStringToAscii(String s) {
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
            if ((data[i/2] < 32) || (data[i/2] > 127)) {
                data[i/2] = 46;
            }
            sb.append((char)data[i/2]);
        }
        
        return sb.toString();
    }
    
    public void doDumpAsAscii() {
        
        DBHandler dbHandler = new DBHandler();
        HashMap<String, String> uniqueGrams;
        
//        System.out.println(gramStringToAscii("00000028000000"));


        for (int nSize=1;nSize<=Consts.MAX_NGRAM_SIZE;nSize++) {
            uniqueGrams = new HashMap();
            // result: key String gram, value String fileExt
            uniqueGrams = dbHandler.getAllUniqueNGrams(nSize);
            for (Map.Entry<String, String> entry : uniqueGrams.entrySet()) {
                String gram = entry.getKey();
                String fileExt = entry.getValue();
                System.out.println(fileExt+"\t"+nSize+"\t"+gram+"\t"+gramStringToAscii(gram));
            }            
            
        }
        
    }
    
} // end of class
