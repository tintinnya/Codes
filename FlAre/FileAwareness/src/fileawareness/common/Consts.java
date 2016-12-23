/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness.common;

/**
 *
 * @author tintinmcleod
 */
public class Consts {
    public static final int ERROR_EXIT_CODE_FILENOTFOUND = 3;
    public static final int ERROR_EXIT_CODE_SQLERROR = 4;
    public static final int ERROR_EXIT_CODE_NOTAFILE = 5;
    
    public static final int ERROR_EXIT_CODE_FILEISGREATERTHAN2GB = 6;
    
    public static int MAX_NGRAM_SIZE = 20;
    
    public static String byteToHex(byte[] b) {
        // http://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
        
        String hexStr = "";
        for (int i=0; i < b.length; i++) {
            hexStr += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return hexStr.toUpperCase();
        
    }    
}
