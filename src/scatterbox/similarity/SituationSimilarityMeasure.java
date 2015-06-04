   package scatterbox.similarity;

import java.util.ArrayList;
import java.util.List;

import cbml.cbr.Feature;
//import cbml.cbr.SimilarityMeasure;
/**
 * Insert the type's description here.
 * Creation date: (10/07/2002 19:24:17)
 * @author: 
 */
public class SituationSimilarityMeasure implements cbml.cbr.SimilarityMeasure {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private List possibleValues;
   
   /**
    * OriginSimilarityMeasure constructor comment.
    */
   public SituationSimilarityMeasure(List possibleValues) {
      this.possibleValues = possibleValues;
   }
   /**
    * calculateSimilarity method comment.
    */
   public double calculateSimilarity(Feature feature1, Feature feature2) {
      double similarity = 0;
      String value1 = (String) feature1.getValue();
      String value2 = (String) feature2.getValue();
      String address1 = null;
      String address2 = null;
      // find the paths of these values in the tree and calculate similarity from that

      for (int i = 0; i < possibleValues.size(); i++) {
         String testValue = (String) possibleValues.get(i);
         int pos = testValue.lastIndexOf("/");
         if (pos != -1) {
            testValue = testValue.substring(pos + 1);
            if (testValue.equals(value1))
               address1 = (String) possibleValues.get(i);
            if (testValue.equals(value2))
               address2 = (String) possibleValues.get(i);
         }
      }

      if (address1 == null || address2 == null)
         return 0;

      int commonLevels = 0;
      //The number of levels by which 2 situations differ
      //Ex: /atwork/atdesk and /atwork/atdesk/workingatcomputer differ by 1
      while (true) {
         int pos1 = address1.indexOf("/", 1);
         if (pos1 == -1) {
            pos1 = address1.length();
         }

         int pos2 = address2.indexOf("/", 1);
         // The candidate situation is not specific enough
         if (pos2 == -1)
            pos2 = address2.length();

         String begin1 = address1.substring(0, pos1);
         String begin2 = address2.substring(0, pos2);
         
         if (begin1.equalsIgnoreCase(begin2)) {
            commonLevels++;
            address1 = address1.substring(pos1);
            address2 = address2.substring(pos2);
            if (address1.length() == 0){
               similarity = getNumLevels(address2);
               break;
            }else if (address2.length() == 0){
               similarity = getNumLevels(address1);
               break;
            }
         } else {
            //Same level but not the same, hence it is wrong
            similarity = commonLevels;
            break;
         }
      }
      return similarity;
   }
   
   
   /**
    * Takes an address string and returns the number of levels in a location
    * string. Each hop is has a cost of 0.05
    * @param a_testString
    * @return
    */
   private double getNumLevels(String a_testString){
      double distance = 0;
      
      for(int i=0;i<a_testString.length();i++){
         if(a_testString.charAt(i) == '/'){
            distance++;
         }
      }
      return distance;
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   public Object clone() throws CloneNotSupportedException {
      List clonePossibleValues = new ArrayList();

      for (int i = 0; i < possibleValues.size(); i++) {
         String value = (String) possibleValues.get(i);
         clonePossibleValues.add(value);
      }
      Object clone = new SituationSimilarityMeasure(clonePossibleValues);
      return clone;
   }
}
