/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness.common;

import fileawareness.FlAre;
import fileawareness.learner.NGramLearner;
import fileawareness.learner.entity.FileTypeCounter;
import fileawareness.learner.entity.NGramTableEntity;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author tintinmcleod
 */
public class DBHandler {
    
    private static String url = "jdbc:postgresql://localhost/thesisdb";
    private static String user = "thesisuser";
    private static String password = "thesis2013!";

    public DBHandler() {
        super();
    }
    
    public Connection getConnection() throws SQLException {
        Connection conn = null;
        conn = DriverManager.getConnection(url, user, password);
        return conn;
    }
    
    public boolean isFileCurrentlyLearned(String fileMd5hash) {
        boolean retval = false;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            String selectSql = "SELECT file_name FROM files_temp WHERE file_md5hash = ?";
            ps = conn.prepareStatement(selectSql);
            ps.setString(1, fileMd5hash.toUpperCase());
            rs = ps.executeQuery();
            if (rs.next()) {
                // the same identic file with same md5sum already registered.
                retval = true;
            }
            rs.close();
            ps.close();
            conn.close();            
        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);
        } finally {
        }
        return retval;
    }
    
    
    public boolean isFileAlreadyLearned(String fileMd5hash) {
        boolean retval = false;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            String selectSql = "SELECT file_name FROM files_learned WHERE file_md5hash = ?";
            ps = conn.prepareStatement(selectSql);
            ps.setString(1, fileMd5hash.toUpperCase());
            rs = ps.executeQuery();
            if (rs.next()) {
                // the same identic file with same md5sum already registered.
                retval = true;
            }
            rs.close();
            ps.close();
            conn.close();            
        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);
        } finally {
        }
        return retval;
    }

     public ArrayList<FileTypeCounter> getFileTypeCounter() {
        ArrayList<FileTypeCounter> retval = new ArrayList();
        FileTypeCounter e = null;
        
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            String selectSql =  "select file_ext, count(file_ext) as numLearnedFiles " +
                                "from files_temp " +
                                "group by file_ext; ";
            ps = conn.prepareStatement(selectSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                e = new FileTypeCounter();
                e.setFileExt(rs.getString("file_ext").toLowerCase());
                e.setCount(rs.getInt("numLearnedFiles"));
                retval.add(e);
                if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#getFileTypeCounter: "+e.getFileExt()+" has "+e.getCount()+" file(s).");
            }
            rs.close();
            ps.close();
            conn.close();            
        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);

        } finally {
        }
        return retval;
    }
   
    
    /*
        retval int as file_id
    */
    public int addFile(String fileName, String fileExt, long fileSize, String fileMd5hash) {
        // check whether long in java is compatible with int in PostgreSQL
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int seqNumFileId = -1;
        fileMd5hash = fileMd5hash.toUpperCase();
        try {
            conn = getConnection();
            /* 
                insert into learned_files (file_name, file_ext, file_size, file_md5hash)
                values ('466466.doc','.doc',598016,'F55ADC5B165FD8C894B45668FC52EC0F');

            Processing 62 of 73 file /home/tintinmcleod/Desktop/corpus/BMP/.DS_Store
Nov 29, 2013 6:52:07 AM fileawareness.common.DBHandler addFile
SEVERE: null
org.postgresql.util.PSQLException: ERROR: value too long for type character varying(5)            
            
            
            
            
            */      
            
            String insertSql = "INSERT INTO files_temp "+
                                "(file_name, file_ext, file_size, file_md5hash) VALUES "+
                                "(?,?,?,?)";
                                
            ps = conn.prepareStatement(insertSql);
            conn.setAutoCommit(false);
            ps.setString(1, fileName);
            ps.setString(2, fileExt.toLowerCase());
            ps.setLong(3, fileSize);
            ps.setString(4, fileMd5hash);
            ps.execute();
            conn.commit();
            
            String checkSql = "SELECT file_id FROM files_temp WHERE file_md5hash = ?";
            ps = conn.prepareStatement(checkSql);
            ps.setString(1, fileMd5hash);
            rs = ps.executeQuery();
            
            while (rs.next()) {
               seqNumFileId = rs.getInt("file_id");
            }
            
            rs.close();
            ps.close();
            conn.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex1);
            }
            System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);

        }
        return seqNumFileId;
    }
    
    public void addGrams(int fileId, int nSize, HashMap hm) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            
            String selectSQL = "SELECT file_ext FROM files_temp WHERE file_id = ?";
            ps = conn.prepareCall(selectSQL);
            ps.setInt(1, fileId);
            rs = ps.executeQuery();
            
            String fileExt = "";
            while (rs.next()) {
                fileExt = rs.getString("file_ext").toLowerCase();
            }
            rs.close();
            ps.close();
                    
        /*
        insert into ngram (gram, n, frequency, file_id, file_type)
        values ('00',1,25,2,'doc');
        */     
            
            String insertSql = "INSERT INTO ngram "+
                                "(gram, n, frequency, file_id, file_ext) VALUES "+
                                "(?,?,?,?,?)";
                                
            ps = conn.prepareStatement(insertSql);
            conn.setAutoCommit(false);
            // inconsistency style of code: use for-each statement!
            Iterator iter = hm.keySet().iterator();
            int n=0;
            while(iter.hasNext()) {
                String gramStr = (String)iter.next();
                int freq = (int)hm.get(gramStr);
                ps.setString(1, gramStr);
                ps.setInt(2, nSize);
                ps.setInt(3, freq);
                ps.setInt(4, fileId);
                ps.setString(5, fileExt.toLowerCase());
                ps.addBatch();
                n++;
                if (n==1000) {
                    ps.executeBatch();
                    n=0;
                }
            }
            ps.executeBatch();
            conn.commit();
            
            rs.close();
            ps.close();
            conn.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            ex.getNextException().printStackTrace();
            try {
                conn.rollback();
                System.err.println("Transaction rolled back.");
                        
            } catch (SQLException ex1) {
                Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex1);
            }
            System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);

        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {};
            if (ps != null) try { rs.close(); } catch (SQLException e) {};
            if (conn != null) try { rs.close(); } catch (SQLException e) {};
            
        }


        
    }

    public void setCommonGramsFromExtraction(String fileExt, int count) {
        
        NGramTableEntity e = null;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement psSelect = null;
        PreparedStatement psInsert = null;
        try {
            conn = getConnection();
            String selectSql =  
                                "select gram, n, count(gram) as xsist, file_ext " +
                                "from ngram " +
                                "where n=? " +
                                "and file_ext=? " +
                                "group by gram, n, file_ext " +
                                "having count(gram)=?";
            String insertSql = 
                                "insert into ngram_common_temp (tempgram, n, xsist, total_files_learned, file_ext) " +
                                "values (?, ?, ?, ?, ?)";                    
                    ;
            psSelect = conn.prepareStatement(selectSql);
            psInsert = conn.prepareStatement(insertSql);
            
            conn.setAutoCommit(false);
            for (int x=1;x<=Consts.MAX_NGRAM_SIZE;x++) {
                int numCommonGram = 0;
                psSelect.setInt(1, x);
                psSelect.setString(2, fileExt);
                psSelect.setInt(3, count);
                rs = psSelect.executeQuery();
                while (rs.next()) {
                    e = new NGramTableEntity();
                    e.setGram(rs.getString("gram"));
                    e.setN(rs.getInt("n"));
                    e.setXsistInFiles(rs.getInt("xsist"));
                    e.setTotalFilesLearned(count);
                    e.setFileExt(rs.getString("file_ext").toLowerCase());

                    psInsert.setString(1, e.getGram());
                    psInsert.setInt(2, e.getN());
                    psInsert.setInt(3, e.getXsistInFiles());
                    psInsert.setInt(4, e.getTotalFilesLearned());
                    psInsert.setString(5, e.getFileExt().toLowerCase());
                    psInsert.addBatch();
                    numCommonGram++;
                    if (numCommonGram == 10000) {
                        psInsert.executeBatch();
                        conn.commit();
                    }
                }
                psInsert.executeBatch();
                conn.commit();

                if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#setCommonGramsFromExtraction: From "+count+" files of "+fileExt+": extracting gram from ngrams; wrote "+numCommonGram+" common "+x+"-gram to ngram_common_temp...");
            }
            rs.close();
            psInsert.close();
            psSelect.close();
            conn.close();            
        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);

        } finally {
        }
    }

    public HashMap<String, NGramTableEntity> getTempUniqueGram(int n) {
        
        HashMap<String, NGramTableEntity> retval = new HashMap();
        NGramTableEntity e = null;
        
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            String selectSql =  
                                "select tempgram, n, xsist, total_files_learned, file_ext " +
                                "from ngram_common_temp " +
                                "where tempgram in ( " +
                                " select tempgram from ngram_common_temp " +
                                " where n=? " +
                                " and tempgram not in (" +
                                "   select historygram from ngram_unique_history " +
                                " )" +
                                " group by tempgram " +
                                " having count(tempgram) = 1 " +
                                ")";
            ps = conn.prepareStatement(selectSql);
            ps.setInt(1, n);
            rs = ps.executeQuery();
            while (rs.next()) {
                e = new NGramTableEntity();
                e.setGram(rs.getString("tempgram"));
                e.setN(rs.getInt("n"));
                e.setXsistInFiles(rs.getInt("xsist"));
                e.setTotalFilesLearned(rs.getInt("total_files_learned"));
                e.setFileExt(rs.getString("file_ext").toLowerCase());
                retval.put(e.getGram(), e);
            }
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#getTempUniqueGram: copied "+retval.size()+" common "+n+"-gram from table ngram_common_temp that is not exists in ngram_unique_history to HashMap.");
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);

        } finally {
        }
        return retval;
        
        
        
    }

    public HashMap<String, NGramTableEntity> getFinalUniqueGramNeedToUpdatedInTemp(HashMap<String, NGramTableEntity> tempNgram) {
        HashMap<String, NGramTableEntity> retval = new HashMap();
        NGramTableEntity e = null;
        
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            StringBuilder sb = new StringBuilder();
            
            for (Map.Entry<String, NGramTableEntity> entry : tempNgram.entrySet()) {
                sb.append("'");
                sb.append(entry.getKey());
                sb.append("'");
                sb.append(",");
            }
            String str = sb.toString();
            int lastpos = str.lastIndexOf(",");
            if (lastpos > 0) {
                String tempgrams = str.substring(0,lastpos);
                String selectSql =  
                                    "select uniquegram, n, xsist, total_files_learned, file_ext " +
                                    "from ngram_unique_final " +
                                    "where uniquegram in ( " +
                                    tempgrams +
                                    ")";
                ps = conn.prepareStatement(selectSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    e = new NGramTableEntity();
                    e.setGram(rs.getString("uniquegram"));
                    e.setN(rs.getInt("n"));
                    e.setXsistInFiles(rs.getInt("xsist"));
                    e.setTotalFilesLearned(rs.getInt("total_files_learned"));
                    e.setFileExt(rs.getString("file_ext").toLowerCase());
                    retval.put(e.getGram(), e);
                }
                if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#getFinalUniqueGramNeedToUpdatedInTemp: returning "+retval.size()+" finalGrams that matches with common gram...");
                rs.close();
                ps.close();
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);

        } finally {
        }
        return retval;
    }

    public void deleteFinalGramForEfficientUpdate(HashMap<String, NGramTableEntity> finalNgram) {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        if ((finalNgram == null) || (finalNgram.size() == 0)) {
            try {
                conn = getConnection();
                StringBuilder sb = new StringBuilder();
                int numFileDeleted = 0;

                for (Map.Entry<String, NGramTableEntity> entry : finalNgram.entrySet()) {
                    numFileDeleted++;
                    sb.append("'");
                    sb.append(entry.getKey());
                    sb.append("'");
                    sb.append(",");
                }
                String str = sb.toString();
                int lastpos = str.lastIndexOf(",");
                if (lastpos > 0) {
                    String tempgrams = str.substring(0,lastpos);
                    String deleteSql =  
                                        "delete from ngram_unique_final " +
                                        "where uniquegram in ( " +
                                        tempgrams +
                                        ")";
                    ps = conn.prepareStatement(deleteSql);
                    ps.executeUpdate();
                }
                if (ps != null) ps.close();
                if (conn != null) conn.close();
                if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#deleteFinalGramForEfficientUpdate: Deleting "+ numFileDeleted +" final gram records before updating...");

            } catch (SQLException ex) {
                Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
                ex.getNextException().printStackTrace();
                System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);

            } finally {
            }            
        } else {
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#deleteFinalGramForEfficientUpdate: This hashmap is empty. No unique grams has been deleted from database.");
        } // end of checking finalNgram nullity or its size

    }

    public void insertFinalGramForEfficientUpdate(HashMap<String, NGramTableEntity> tempNgram) {
        Connection conn = null;
        PreparedStatement psInsert = null;
        
        if ( (tempNgram != null) && (tempNgram.size() > 0) ) {
            try {
                conn = getConnection();
                String insertSql = 
                                    "insert into ngram_unique_final (uniquegram, n, xsist, total_files_learned, file_ext) " +
                                    "values (?, ?, ?, ?, ?)";                    
                        ;
                psInsert = conn.prepareStatement(insertSql);

                int numNewGram = 0;
                conn.setAutoCommit(false);
                int n=0;

                for (Map.Entry<String, NGramTableEntity> entry : tempNgram.entrySet()) {
                    n = entry.getValue().getN();
                    psInsert.setString(1, entry.getValue().getGram());
                    psInsert.setInt(2, entry.getValue().getN());
                    psInsert.setInt(3, entry.getValue().getXsistInFiles());
                    psInsert.setInt(4, entry.getValue().getTotalFilesLearned());
                    psInsert.setString(5, entry.getValue().getFileExt().toLowerCase()); // error when pushing file_ext more than 4 chars
                    psInsert.addBatch();
                    numNewGram++;
                    if (numNewGram % 1000 == 0) {
                       psInsert.executeBatch();
                    }
                }
                psInsert.executeBatch();
                if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#insertFinalGramForEfficientUpdate: "+numNewGram+" unique "+n+"-grams has been inserted to database.");
                conn.commit();
                psInsert.close();
                conn.close();            
            } catch (SQLException ex) {
                Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
                ex.getNextException().printStackTrace();
                System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);

            } finally {
            }    
            
        } else {
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#insertFinalGramForEfficientUpdate: tempNgram is empty. No unique grams has been inserted to database.");
        } // end of checking tempNgram nullity or its size
        
    
    
    
    
    }

    public void cleanUpTempData() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            

            String truncateNGramCommonTempSQL = 
                "TRUNCATE ngram_common_temp;";
            ps = conn.prepareStatement(truncateNGramCommonTempSQL);
            ps.executeUpdate();
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#cleanUpTempData: Truncating data in ngram_common_temp.");

            String truncateNGramSQL = 
                "TRUNCATE ngram;";
            ps = conn.prepareStatement(truncateNGramSQL);
            ps.executeUpdate();
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#cleanUpTempData: Truncating data in ngram.");


            // moving data from files_temp to files_learned
            String insertIntoFileLearnedSQL = 
                "INSERT INTO files_learned "+
                    "(file_name, file_ext, file_size, file_md5hash) " +
                "(SELECT "+
                    " file_name, file_ext, file_size, file_md5hash "+
                "FROM files_temp);";                    
            ps = conn.prepareStatement(insertIntoFileLearnedSQL);
            ps.executeUpdate();
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#cleanUpTempData: Copying data from files_temp into files_learned.");

            String truncateFileTempSQL = 
                "TRUNCATE files_temp CASCADE";
            ps = conn.prepareStatement(truncateFileTempSQL);
            ps.executeUpdate();
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#cleanUpTempData: Truncating data in files_temp.");

            
            if (ps != null) ps.close();
            if (conn != null) conn.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            ex.getNextException().printStackTrace();
            System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);

        } finally {
        }    }

    public HashMap<String, String> getAllUniqueNGrams(int n) {
        HashMap<String, String> retval = new HashMap();
        
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            String selectSql =  
                                "select uniquegram, file_ext "+
                                "from ngram_unique_final "+
                                "where n=? "+
                                "order by uniquegram;";
            ps = conn.prepareStatement(selectSql);
            ps.setInt(1, n);
            rs = ps.executeQuery();
            while (rs.next()) {
                retval.put(rs.getString("uniquegram"), rs.getString("file_ext").toLowerCase());
            }
            rs.close();
            ps.close();
            conn.close(); 
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#getAllUniqueNGrams: Returning "+retval.size()+" unique "+n+"-gram.");
            
        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);

        } finally {
        }        
        
        
        return retval;
    }

    /*

    select file_ext, count(file_ext)
    from ngram_unique_final
    where n=2
    group by file_ext
    ;
    file_ext | count 
    ----------+-------
    .ppt     |   547
    .doc     |   132
    (2 rows)

    */
    public HashMap<String, Integer> getStatsPerNGram(int nSize) {
        HashMap<String, Integer> retval = new HashMap();
        
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            String selectSql =  
                                "select file_ext, count(file_ext) as x " +
                                "from ngram_unique_final " +
                                "where n=? " +
                                "group by file_ext";
            ps = conn.prepareStatement(selectSql);
            ps.setInt(1, nSize);
            rs = ps.executeQuery();
            while (rs.next()) {
                retval.put(rs.getString("file_ext").toLowerCase(), rs.getInt("x"));
            }
            rs.close();
            ps.close();
            conn.close(); 
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#getStatsPerNGram: Returning "+retval.size()+" records of <fileTypes, count(fileTypes)> based on "+nSize+"-gram.");
            
        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);

        } finally {
        }         
        
        return retval;
    }

    public void moveFinalGramNotSoUniqueAnymoreToHistory(HashMap<String, NGramTableEntity> toHistoryNgram) {
        Connection conn = null;
        PreparedStatement psInsert = null;
        PreparedStatement psDelete = null;
        
        try {
            conn = getConnection();
            String insertSql = 
                                "insert into ngram_unique_history (historygram, n, xsist, total_files_learned, file_ext) " +
                                "values (?, ?, ?, ?, ?)";                    
                    ;
                    
                    // moved or just copied???
            psInsert = conn.prepareStatement(insertSql);
            
            int numNewGram = 0;
            conn.setAutoCommit(false);
            int n=0;
            
            for (Map.Entry<String, NGramTableEntity> entry : toHistoryNgram.entrySet()) {
                n = entry.getValue().getN();
                psInsert.setString(1, entry.getValue().getGram());
                psInsert.setInt(2, entry.getValue().getN());
                psInsert.setInt(3, entry.getValue().getXsistInFiles());
                psInsert.setInt(4, entry.getValue().getTotalFilesLearned());
                psInsert.setString(5, entry.getValue().getFileExt().toLowerCase()); // error when pushing file_ext more than 4 chars
                psInsert.addBatch();
                numNewGram++;
                if (numNewGram % 1000 == 0) {
                   psInsert.executeBatch();
                }
            }
            psInsert.executeBatch();
            
            StringBuilder sb = new StringBuilder();
            
            for (Map.Entry<String, NGramTableEntity> entry : toHistoryNgram.entrySet()) {
                sb.append("'");
                sb.append(entry.getKey());
                sb.append("'");
                sb.append(",");
            }
            String str = sb.toString();
            int lastpos = str.lastIndexOf(",");
            if (lastpos > 0) {
                String historyGrams = str.substring(0,lastpos);
                String deleteSql =  
                                    "DELETE FROM ngram_unique_final " +
                                    "where uniquegram in ( " +
                                    historyGrams +
                                    ")";
                psDelete = conn.prepareStatement(deleteSql);
                psDelete.executeUpdate();
            }
            
            if (FlAre.DEBUG) System.err.println(this.getClass().getName()+"#moveFinalGramNotSoUniqueAnymoreToHistory: "+numNewGram+" unique "+n+"-grams has been moved to history table.");
            conn.commit();
            psInsert.close();
            psDelete.close();
            conn.close();            
        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            ex.getNextException().printStackTrace();
            System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);

        } finally {
        }    
    
    
    
        }

    public boolean isFileTypeOnceLearned(String fileExt) {
        boolean retval = false;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            String selectSql = "SELECT count(file_ext) FROM files_learned WHERE file_ext = ? GROUP BY file_ext";
            ps = conn.prepareStatement(selectSql);
            ps.setString(1, fileExt.toLowerCase());
            rs = ps.executeQuery();
            if (rs.next()) {
                // the same identic file with same md5sum already registered.
                retval = true;
            }
            rs.close();
            ps.close();
            conn.close();            
        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(Consts.ERROR_EXIT_CODE_SQLERROR);
        } finally {
        }
        return retval;
    }
    


    
    
} // end of class DBHandler
