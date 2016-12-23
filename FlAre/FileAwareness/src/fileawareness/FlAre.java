/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fileawareness;

import fileawareness.common.GramDump;
import fileawareness.learner.NGramLearner;
import fileawareness.populator.Populator;

/**
 *
 * @author tintinmcleod
 */
public class FlAre {

    public static final boolean DEBUG = false;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        /*
        String fileType = "doc";
//        String filePath = "."; // /home/tintinmcleod/NetBeansProjects/FileAwareness/
//        String filePath = "/home/tintinmcleod/Desktop/corpus/Office2003/doc/textIcon/004545.doc";
        String filePath = "/home/tintinmcleod/Desktop/corpus/Office2003/doc/textOnly";
        */
        int MODE = 4;
        String mode = "";
        String filePath = "";
        String fileType = "";
        long start = 0;
        long end = 0;
        
        if (MODE==1) {
            mode = "learn";
//            filePath = "/home/tintinmcleod/Desktop/corpus/learnphase/radical/bmp";
//            filePath = "/home/tintinmcleod/Desktop/corpus/learnphase/radical/jpg";
//            filePath = "/home/tintinmcleod/Desktop/corpus/learnphase/radical/gif";
//            filePath = "/home/tintinmcleod/Desktop/corpus/learnphase/radical/png";
//            filePath = "/home/tintinmcleod/Desktop/corpus/learnphase/radical/pdf";
//            filePath = "/home/tintinmcleod/Desktop/corpus/learnphase/radical/doc";
//            filePath = "/home/tintinmcleod/Desktop/corpus/learnphase/radical/xls";
//            filePath = "/home/tintinmcleod/Desktop/corpus/learnphase/radical/docx";
//            filePath = "/home/tintinmcleod/Desktop/corpus/learnphase/radical/xlsx";
//            filePath = "/home/tintinmcleod/Desktop/corpus/learnphase/radical/ppt";
            filePath = "/home/tintinmcleod/Desktop/corpus/learnphase/radical/pptx";

            //        String filePath = "/home/tintinmcleod/Desktop/corpus/Office2003/doc/textOnly";
            fileType = "ppt";
        } else if (MODE==2) {
            mode = "predict";
//            filePath = "/home/tintinmcleod/Desktop/corpus/NEW-EXP-0/250560.ppt";
            filePath = "/home/tintinmcleod/Desktop/corpus/NEW-EXP-0";
        } else if (MODE==3) {
            mode = "dump";
        } else if (MODE==4) {
            mode = "predict";
//            filePath = "/home/tintinmcleod/Desktop/corpus/NEW-EXP-0/948335.ppt";
//            filePath = "/home/tintinmcleod/Desktop/corpus/doctored/FileExtensionJPGtoDOCX.docx";
//            filePath = "/home/tintinmcleod/Desktop/corpus/doctored/FileHeader_PNGtoJPG.png";
            filePath = "/home/tintinmcleod/Desktop/corpus/doctored/RandomData.doc";
        }
        
        if (mode.equalsIgnoreCase("learn")) {
            Learner learner = new Learner();
            learner.setFileType(fileType);
            learner.setFilePath(filePath);
             start = System.currentTimeMillis();
            learner.doLearn();
             end = System.currentTimeMillis();
            System.out.println("Learning process done in "+(end-start)+"ms.");
            Populator populator = new Populator();
             start = System.currentTimeMillis();
            populator.doNormalized();
             end = System.currentTimeMillis();
            System.out.println("Normalization process done in "+(end-start)+"ms.");
        } else if (mode.equalsIgnoreCase("predict")) {
            Predictor predictor = new Predictor(filePath);
            predictor.doPredict();
        } else if (mode.equalsIgnoreCase("dump")) {
            GramDump dump = new GramDump();
            dump.doDumpAsAscii();
        } else if (mode.equalsIgnoreCase("completePredict")) {
            
        } else {
            System.err.println("Unknown mode.");
            System.exit(2);
        }
        
        
    }
    
}
