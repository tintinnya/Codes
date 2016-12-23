/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness.learner.entity;

/**
 *
 * @author tintinmcleod
 */
public class FileTypeCounter {
    private String __fileExt;
    private int __count;

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
     * @return the __count
     */
    public int getCount() {
        return __count;
    }

    /**
     * @param __count the __count to set
     */
    public void setCount(int __count) {
        this.__count = __count;
    }
    
}
