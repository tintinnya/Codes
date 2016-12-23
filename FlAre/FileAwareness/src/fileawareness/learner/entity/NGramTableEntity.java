package fileawareness.learner.entity;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author tintinmcleod
 */
public class NGramTableEntity {
    // gram, n, count(gram) as xsist, file_ext
    private String __gram;
    private int __n;
    private int __xsistInFiles;
    private int __totalFilesLearned;
    private String __fileExt;

    /**
     * @return the __gram
     */
    public String getGram() {
        return __gram;
    }

    /**
     * @param __gram the __gram to set
     */
    public void setGram(String __gram) {
        this.__gram = __gram;
    }

    /**
     * @return the __n
     */
    public int getN() {
        return __n;
    }

    /**
     * @param __n the __n to set
     */
    public void setN(int __n) {
        this.__n = __n;
    }

    /**
     * @return the __xsistInFiles
     */
    public int getXsistInFiles() {
        return __xsistInFiles;
    }

    /**
     * @param __xsistInFiles the __xsistInFiles to set
     */
    public void setXsistInFiles(int __xsistInFiles) {
        this.__xsistInFiles = __xsistInFiles;
    }

    /**
     * @return the __totalFilesLearned
     */
    public int getTotalFilesLearned() {
        return __totalFilesLearned;
    }

    /**
     * @param __totalFilesLearned the __totalFilesLearned to set
     */
    public void setTotalFilesLearned(int __totalFilesLearned) {
        this.__totalFilesLearned = __totalFilesLearned;
    }

    /**
     * @return the __fileExt
     */
    public String getFileExt() {
        return __fileExt;
    }

    /**
     * @param __fileExt the __fileExt to set
     */
    public void setFileExt(String __fileExt) {
        this.__fileExt = __fileExt;
    }
    
    
}
