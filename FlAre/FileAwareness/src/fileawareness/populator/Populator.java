/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness.populator;

import fileawareness.FlAre;
import fileawareness.common.Consts;
import fileawareness.common.DBHandler;
import fileawareness.learner.entity.FileTypeCounter;
import fileawareness.learner.entity.NGramTableEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tintinmcleod
 */
public class Populator {
    
     DBHandler __handler;
    
    public void doNormalized() {
        
       __handler = new DBHandler();
        
        
        /* 
            # get all of each learned file_ext
                select file_ext, count(file_ext) as numLearnedFiles 
                from temp_files 
                group by file_ext;
        */
            ArrayList<FileTypeCounter> counter = __handler.getFileTypeCounter();
            HashMap<String, NGramTableEntity> tempNgram = new HashMap();
            HashMap<String, NGramTableEntity> finalNgram = new HashMap();

            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#doNormalized: "+counter.size()+" file types in ngram table.");
            
            /*
        
            # for each [file_ext]:
                * get all of the grams that exists in ALL same learned file_ext
                    @ for each n-gram [1..20], get all gram that only exists in all learnedFile 
                            select gram, n, count(gram) as xsist, file_ext
                            from ngram
                            where n=[1..20]
                            and file_ext=[file_ext]
                            group by gram, file_ext
                            having count(gram)=[numLearnedFiles];
                        $ save it into ArrayList<javaobject> ngram  EGram(String gram, int n, int xsist, double xsist_pctg, String file_type
                        $ for each ArrayList<javaobject>, write it to db with xsist_pct calculation field
                            insert into temp_common_grams (gram, n, xsist, xsist_pct, file_type)
                            values (javaobject.gram, n, javaobject.xsist, (javaobject.xsist/numLearnedFiles), file_type)
                        $ end result for this operation: database filled with all gram with percentage of existance
            # find the uniq gram for each file_ext, that is count(tempgram) = 1
                select tempgram, n, xsist, xsist_pct, file_ext 
                from temp_common_gram
                where tempgram in (
                    select tempgram from temp_common_gram 
                    group by tempgram 
                    having count(tempgram) = 1
                );
            # save it into HashMap(String tempgram, <javaobject>) temp
            # compare it with the final_common_gram, get all finalgram that are occured in temp_common_gram
                select finalgram, n, xsist, xsist_pct, file_ext 
                from final_common_gram
                where finalgram in (
                    select tempgram from temp_common_gram
                );
            # save it into ArrayList<javaobject> finalToUpdate
*/
            
            
            for (FileTypeCounter c : counter) {
                long start = System.currentTimeMillis();
                __handler.setCommonGramsFromExtraction(c.getFileExt(), c.getCount()); // extract common grams from ngrams table for each nSize
                long finish = System.currentTimeMillis();
                if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#doNormalized: "+c.getFileExt()+" has "+c.getCount()+" file(s). Extraction process done in "+(finish-start)+"ms.");
            }

            for (int i=1; i<=Consts.MAX_NGRAM_SIZE; i++) {
                tempNgram = __handler.getTempUniqueGram(i); // key = gram, value = entity
                /*
                    This tempNgram should be unique enough so that it only exists in ngram_common_temp, 
                    but not exists in ngram_unique_history 
                */
                int initialTempNGramSize = tempNgram.size();
                if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#doNormalized: Processing only "+initialTempNGramSize+" unique "+i+"-grams that has not recognized as unique gram (i.e. on history table) from hashmap...");
                
                if ((tempNgram != null)&&(tempNgram.size() > 0)) {
                    // get all final unique gram that similar with gram in tempgram, 
                    // either to update its xsist value or to delete it from table since it is not unique anymore 
                    finalNgram = __handler.getFinalUniqueGramNeedToUpdatedInTemp(tempNgram); // key = gram, value = entity

                    HashMap<String, NGramTableEntity> toHistoryNgram = new HashMap();
                    HashMap<String, NGramTableEntity> updatedTempNGram = new HashMap();
                    HashMap<String, NGramTableEntity> newUniqueTempNGram = new HashMap();
                    
                    if ((finalNgram != null)&&(finalNgram.size() > 0)) {
                        int numUpdatedGrams = 0;
                        int numNonSoUniqueAnymoreGrams = 0;
                        for (Map.Entry<String, NGramTableEntity> entry : finalNgram.entrySet()) {
                            String gramAsKey = entry.getKey();
                            NGramTableEntity tempNgramToUpdate = tempNgram.get(gramAsKey);
                            NGramTableEntity finalNgramFromDatabase = finalNgram.get(gramAsKey);
                            if (finalNgramFromDatabase.getFileExt().equalsIgnoreCase(tempNgramToUpdate.getFileExt())) {
                                // if file extension of common gram is the same, then update the xsist value
                                tempNgramToUpdate.setXsistInFiles(tempNgramToUpdate.getXsistInFiles() + entry.getValue().getXsistInFiles());
                                tempNgramToUpdate.setTotalFilesLearned(tempNgramToUpdate.getTotalFilesLearned() + entry.getValue().getTotalFilesLearned());
                                if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#doNormalized: Final Unique Gram "+entry.getKey()+" for "+finalNgramFromDatabase.getFileExt()+" needs to be updated its xsist value...");
                                tempNgram.remove(gramAsKey);
                                updatedTempNGram.put(gramAsKey, tempNgramToUpdate);
                                numUpdatedGrams++;
                            } else {
                                if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#doNormalized: Final Unique Gram "+entry.getKey()+" for "+finalNgramFromDatabase.getFileExt()+" is not unique anymore since it also exists in different file extension "+tempNgramToUpdate.getFileExt()+". Moving it to ngram_history.");
                                // different file extensions share the same gram, so this gram is not unique. 
                                // delete it from temp_grams
                                tempNgram.remove(gramAsKey);
                                toHistoryNgram.put(gramAsKey, finalNgramFromDatabase);
                                numNonSoUniqueAnymoreGrams++;
                            }
                        } // end of finalNGram iteration for-each loop
                        if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#doNormalized: From "+initialTempNGramSize+" common "+i+"-gram: "+numUpdatedGrams+" updated its xsist value, "+numNonSoUniqueAnymoreGrams+" not so unique anymore, "+(initialTempNGramSize-numUpdatedGrams-numNonSoUniqueAnymoreGrams)+" new unique gram.");
                        if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#doNormalized: * From "+initialTempNGramSize+" common "+i+"-gram: "+updatedTempNGram.size()+" updated its xsist value, "+toHistoryNgram.size()+" not so unique anymore, "+tempNgram.size()+" new unique gram.");
                        
                        __handler.deleteFinalGramForEfficientUpdate(finalNgram); // all of final grams that were exists in tempGram were processed: either exists or become not so unique anymore
                        __handler.moveFinalGramNotSoUniqueAnymoreToHistory(toHistoryNgram);
                    } // end of checking finalNGram nullity
                    if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#doNormalized: Inserting "+updatedTempNGram.size()+" updated unique gram..");
                    __handler.insertFinalGramForEfficientUpdate(updatedTempNGram);
                    
                    int numOfNewUniqueGram = tempNgram.size();
                    
                    if (numOfNewUniqueGram > 0) {
                        for (Map.Entry<String, NGramTableEntity> entry : tempNgram.entrySet()) {
                            String currentKey = entry.getKey();
                            NGramTableEntity currentEntity = tempNgram.get(currentKey);
                            String currentFileExt = currentEntity.getFileExt();
                            if (__handler.isFileTypeOnceLearned(currentFileExt)) {
                                // bugs: how to guarantee that these new unique n-grams is new unique? not have been eliminated by selection SQL query since it was non common enough?

                                // this filetype has been learned before, but not in the ngram_unique_final nor ngram_unique_history (how come?)
                                // delete it 
                                //tempNgram.remove(currentKey);
                                if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#doNormalized: In "+numOfNewUniqueGram+" new unique gram, fileExt "+currentFileExt+" has been learned before but this "+currentKey+" is not be able to considered as common gram or even unique gram (i.e. this gram deleted even before inserted to database).");
                                // what if those grams is common? something in process deleted them???
                                // in finding common gram, all of gram that is not 100% will not be picked up
                                // probably, this gram shown up from small set of learning file.
                                // --> Do I have to store them in ngram_uncommon?
                                // if this file was added, and other file was learned, this gram will be removed. So, that's unnecessarily added
                            } else {
                                // only copy the new gram that has not been learned before 
                                newUniqueTempNGram.put(currentKey, currentEntity);
                            }  
                        } // end of new unique gram iteration
                    } // end of checking tempNgram size
                    
                    if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#doNormalized: Inserting "+newUniqueTempNGram.size()+" new unique gram..");
                    __handler.insertFinalGramForEfficientUpdate(newUniqueTempNGram); // this is the unique new gram
                    
                    
                } // end of checking tempNGram nullity
                if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#doNormalized: --- done processing "+i+"-gram ---");
            } // end of n-gram size iterator
            

            /*            
            # for each finalToUpdate.tempgram
                @ update the number of xsist with final + temp
            # delete gram from final, since they are already updated and saved on HashMap
                delete from final_common_gram
                where finalgram in (
                    select tempgram from temp_common_gram
                );
            # for each tempGram
                insert into final_common_gram (gram, n, xsist, xsists_pctg
            */
            
            /*
                truncating, and move files_temp to files_learned
            */

            __handler.cleanUpTempData();
            
    }
} // end of class
