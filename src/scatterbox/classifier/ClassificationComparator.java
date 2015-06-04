package scatterbox.classifier;

import java.util.Comparator;

public class ClassificationComparator implements Comparator<Classification>{

   /**
    * Compares two classification objects using their confidence values.
    */
   public int compare(Classification c0, Classification c1) {
      if(c0.getConfidence() < c1.getConfidence()){
         return -1;
      }else if(c0.getConfidence() > c1.getConfidence()){
         return 1;
      }else{
         return 0;
      }
   }
}
