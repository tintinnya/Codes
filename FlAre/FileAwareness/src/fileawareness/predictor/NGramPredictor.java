/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness.predictor;

import fileawareness.FlAre;
import fileawareness.common.Consts;
import fileawareness.common.DBHandler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tintinmcleod
 */
public class NGramPredictor {
    
    File __file;


    public NGramPredictor(File file) {
        this.__file = file;
    }

    public String checkNGram() {
        String retval = "";
        byte[] currData;
        String currDataStrHex;
        try {
            byte[] fileData = Files.readAllBytes(__file.toPath());
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#checkNGram: Copying "+fileData.length+" bytes to Array");

            DBHandler dbHandler = new DBHandler();
            HashMap uniqueGrams;
            HashMap<String, HashMap> _fileTypeStats = new HashMap(); // container for each fileTypes
            HashMap<String, Integer> numOfUniqueGramPerFileType = new HashMap();

            for (int nSize=1;nSize<=Consts.MAX_NGRAM_SIZE;nSize++) {
//            for (int nSize=3;nSize<=3;nSize++) {
                uniqueGrams = new HashMap();
                //traverse fileData;
                // result: key String gram, value String fileExt
                uniqueGrams = dbHandler.getAllUniqueNGrams(nSize);

                // result: key String fileExt, value Integer count(file_ext)
                numOfUniqueGramPerFileType = dbHandler.getStatsPerNGram(nSize);

                HashMap<Integer, StatsEntity> _mapStatsE = new HashMap();       // container for each ngram, with nSize as key

                // traverse each fileType
                for (Map.Entry<String, Integer> entry : numOfUniqueGramPerFileType.entrySet()) {
                    String ext = entry.getKey();
                    int count = numOfUniqueGramPerFileType.get(ext); // how many 
                    if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#checkNGram: FileExt "+ext+" has "+count+" unique "+nSize+"-gram");
                    
                    
                    if (_fileTypeStats.containsKey(ext)) {
                        // if in _fileTypeStats already exist file_ext statistics, just get its hashmap, put new entity
                        _mapStatsE = _fileTypeStats.get(ext);
                    } else {
                        _mapStatsE = new HashMap();
                    }

                    StatsEntity e = new StatsEntity();
                    e.setFileExt(ext);
                    e.setN(nSize);
                    e.setNumAllGram(count);
                    e.setNumMatch(0);
                    _mapStatsE.put(nSize, e);
                    _fileTypeStats.put(ext, _mapStatsE);
                } // end of for-each numOfUniqueGramPerFileType
                
                
                StatsEntity statsE = new StatsEntity();                     // statistics entity for all current nSize-gram checked
                
                int byteBoundary = fileData.length + 1 - nSize;
                currData = new byte[nSize]; 
                for (int nthSlidingWindow=0;nthSlidingWindow<byteBoundary; nthSlidingWindow++) {
                //create gram
                    
                // TODO: bugs on the first sliding window. 
                // if the data is D0, the representation hexString for this is D0000000000000000000...
                    for (int z=0;z<nSize;z++) {
                        currData[z]=fileData[nthSlidingWindow+z];
                    }
                    currDataStrHex = Consts.byteToHex(currData);
//                    System.out.println("nSize = "+nSize+"; nthSlidingWindow = "+nthSlidingWindow+"; currData = "+currDataStrHex);
//                    if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#checkMetadata: Unique Hashmap has "+uniqueGrams.size()+" entries.");
                    if (uniqueGrams.size() > 0) {
                        if (uniqueGrams.containsKey(currDataStrHex)) {
                            String fileExt = (String)uniqueGrams.get(currDataStrHex);
//                            if (FlAre.DEBUG) System.out.println(this.getClass().getName()+"#checkMetadata: currData "+ currDataStrHex +" has a match fileExt "+fileExt);

                            if (_fileTypeStats.containsKey(fileExt)) { 
                                // if hashmap of all fileTypeStats already contain fileExt, then check whether this size of n gram already registered
                                _mapStatsE = (HashMap<Integer, StatsEntity>)_fileTypeStats.get(fileExt);
                                if (_mapStatsE.containsKey(Integer.valueOf(nSize))) {
                                    statsE = _mapStatsE.get(Integer.valueOf(nSize));
                                    statsE.setNumMatch(statsE.getNumMatch() + 1);
                                }
                                
                            }
                            uniqueGrams.remove(currDataStrHex);
                        }
                    } else {
                        break;
                    } // end of checking uniqueGram size
                } // end of for sliding window
            } // end of n-size iteration

            if (FlAre.DEBUG) System.err.println("[ fileExt [\t key \t[ fExt,\tn,\tnumMatch+\tnumAllGram\t] ] ]\tpctg% match");
            for (Map.Entry<String, HashMap> e : _fileTypeStats.entrySet()) {
                String fileExt = e.getKey();
                HashMap<Integer, StatsEntity> h = (HashMap)e.getValue();
                int thisFileExtNumMatch = 0;
                int thisFileExtNumAllGram = 0;
                DecimalFormat df = new DecimalFormat("0.0000");
                for (Map.Entry<Integer, StatsEntity> g : h.entrySet()) {
                    Integer key = g.getKey();
                    StatsEntity value = g.getValue();
                    String fExt = value.getFileExt();
                    int n = value.getN();
                    int numMatch = value.getNumMatch();
                    thisFileExtNumMatch += numMatch;
                    int numAllGram = value.getNumAllGram();
                    thisFileExtNumAllGram += numAllGram;
                    double pctg = ((double)numMatch / (double)numAllGram) * 100;
                    
                    if (FlAre.DEBUG) System.err.println("[ "+fileExt+" [\t"+ key + "\t[ " +fExt+",\t"+n+",\t"+numMatch+",\t"+numAllGram+"\t] ] ]\t"+df.format(pctg)+"% match");
                }
                double pctgMatchFileExt = ((double)thisFileExtNumMatch / (double)thisFileExtNumAllGram) * 100;
/*  http://stackoverflow.com/questions/8819842/best-way-to-format-a-double-value-to-2-decimal-places */
                System.out.println("--- file "+__file.getAbsolutePath() +" is "+df.format(pctgMatchFileExt)+"% match with "+fileExt+" extension. ("+thisFileExtNumMatch+" out of "+thisFileExtNumAllGram+")");
                if (pctgMatchFileExt >= 95) { retval = retval + " " +fileExt +" ";}
            }
            
        } catch (OutOfMemoryError ex) {
            Logger.getLogger(NGramPredictor.class.getName()).log(Level.SEVERE, null, ex);
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#checkNGram: Unable to allocate memory. Perhaps file size bigger than 2GB.");
            System.exit(Consts.ERROR_EXIT_CODE_FILEISGREATERTHAN2GB);
        } catch (IOException ex) {
            Logger.getLogger(NGramPredictor.class.getName()).log(Level.SEVERE, null, ex);
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#checkNGram: IOError ."+ex.getMessage());
        }
        
        
        return retval;
                
    }
    
}
