/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness.learner;

/**
 *
 * @author tintinmcleod
 */
public class LearnerTestClass {
    public static void main(String[] args) {
        String filePath = "/home/tintinmcleod/Desktop/corpus/Office2003/doc/466466.doc";
        
        String folderPath = "/home/tintinmcleod/Desktop/corpus/Office2003/doc/textOnly";
        FileLearner learner = new FileLearner(filePath);
        learner.doFileLearning();
        System.out.println("File: "+filePath);
        System.out.println("File extension: "+learner.getFileExt());
        System.out.println("File size: "+learner.getFileSize());
        System.out.println("File md5sum: "+learner.getMd5());
        
    }
}
