/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness.predictor;

/**
 *
 * @author tintinmcleod
 */
public class StatsEntity {
    private String __gram;      // hexString gram representation
    private int __n;            // size of gram
    private String __fileExt;   // file extension that has this gram
    private int __numMatch;     // how many grams from predicted file match with total uniq gram from db
    private int __numAllGram;   // how many grams extracted from db

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

    /**
     * @return the __numMatch
     */
    public int getNumMatch() {
        return __numMatch;
    }

    /**
     * @param __numMatch the __numMatch to set
     */
    public void setNumMatch(int __numMatch) {
        this.__numMatch = __numMatch;
    }

    /**
     * @return the __numAllGram
     */
    public int getNumAllGram() {
        return __numAllGram;
    }

    /**
     * @param __numAllGram the __numAllGram to set
     */
    public void setNumAllGram(int __numAllGram) {
        this.__numAllGram = __numAllGram;
    }
}
