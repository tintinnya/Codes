/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness.learner;

import fileawareness.common.Consts;
import fileawareness.common.DBHandler;
import fileawareness.learner.entity.FileTypeCounter;
import fileawareness.learner.entity.NGramTableEntity;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author tintinmcleod
 */
public class NGramLearner {
    
    private byte[] __fileData;
    private DBHandler __handler;
    private int __fileId;
    
    public NGramLearner(byte[] data, int fileId, DBHandler dbhandler) {
        this.__fileData = data;
        this.__handler = dbhandler;
        this.__fileId = fileId;
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
    
    private void updateFrequency(HashMap map, String strHex) {
        
        int frequency;
        if (map.containsKey(strHex)) {
            frequency = (int)map.get(strHex);
//            System.err.println(strHex +" with freq "+frequency);
            map.remove(strHex);
            map.put(strHex, frequency+1);
        } else {
            map.put(strHex, 1);
//            System.err.println(strHex + " is new in hashmap.");
        }
    }
    
    public void doNGramExtraction() {
        
        for (int x=1;x<=Consts.MAX_NGRAM_SIZE;x++) {
            HashMap hm = new HashMap();
            //traverse __fileData;
            int byteBoundary = __fileData.length + 1 - x;
            byte[] currData = new byte[x];
            long startTime = System.currentTimeMillis();
            for (int y=0;y<byteBoundary; y++) {
                //create gram
                for (int z=0;z<x;z++) {
                    currData[z]=__fileData[y+z];
                }
//                System.err.println(x+"-gram["+y+"] = "+byteToHex(currData));
                updateFrequency(hm, byteToHex(currData));
            }
            long endTime = System.currentTimeMillis();
            System.out.print(hm.size()+" unique "+x+"-gram extracted in "+(endTime - startTime)+"ms...");
            // write the content of this hashmap to database
            __handler.addGrams(__fileId, x, hm);
            long endTime2 = System.currentTimeMillis();
            System.out.println(".."+(endTime2 - startTime)+"ms. (Total with db operation)");
        }
        
    } // end of method doNGramExtraction()
    

} // end of class NGramLearner
