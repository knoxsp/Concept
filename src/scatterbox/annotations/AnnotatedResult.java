package scatterbox.annotations;

import java.util.HashMap;

public class AnnotatedResult implements Comparable<AnnotatedResult> {

   /**
    * This class represents a situation, as described in the CBML structure file.
    * There are a set of properties, these being the number of times this situation 
    * has occurred thus far in the simulation, the number of times this was identified correctly
    * by the cbr engine and the number of times this was incorrectly determined by the cbr engine.
    */
   private String annotation;
   private int count = 0;
   private int numCorrectResults = 0;
   private int numIncorrectResults = 0;
   private int indexOfResult;
   private double recall = 0;
   private double precision = -1;
   private int falsePositive = 0;
   HashMap<String, Integer> otherSituations = new HashMap<String, Integer>();
   
   public AnnotatedResult(String an_annotation, int an_index){
      annotation = an_annotation;
      indexOfResult = an_index;
   }
   
   public AnnotatedResult(String an_annotation, int a_count, int an_index){
      annotation = an_annotation;
      count = a_count;
      indexOfResult = an_index;
   }
   
   
   public double getRecall(){
      if(count>0 && numCorrectResults>0){
      recall = (double)numCorrectResults/(double)count;
      }
      if(count==0){
         return -1;
      }
      return recall;
   }
   
   public double getPrecision(){
      double denomenator = (double)numCorrectResults+(double)falsePositive;
      if(denomenator>0){
         precision = ((double)numCorrectResults)/denomenator;
      }
      return precision;
   }
   
   public double getFMeasure(){
      if(precision == 0 && recall == 0){
         return 0;
      }
      double fMeasure = ( 2 * precision * recall ) / ( precision + recall );
      return fMeasure;
   }
   
   public int getIndex(){
      return indexOfResult;
   }
   
   public String getAnnotation(){
      return annotation;
   }
   
   public int getNumOccurrances(){
      return count;
   }
   
   public void setNumOccurrances(int a_count){
      count = a_count;
   }
   /**
    * Add 1 to the number of times this annotation has been seen 
    * in the simulation so far.
    */
   public void incrementOccurrenceCount(){
      count = count + 1;
   }
   
   public int getNumCorrectResults(){
      return numCorrectResults;
   }
   
   public int getNumIncorrectResults(){
      return numIncorrectResults;
   }
   
   public int getFalsePositive(){
      return falsePositive;
   }
   
   public void incrementFalsePositive(){
      falsePositive = falsePositive + 1;
   }
   /**
    * Add 1 to the number of correct identifications of this situation
    */
   public void incrementNumCorrectResults(){
      numCorrectResults = numCorrectResults + 1;
   }
   /**
    * Add 1 to the number of times a false positive has occurred.
    */
   public void incrementNumIncorrectResults(){
      numIncorrectResults = numIncorrectResults + 1;
   }
   
   /**
    * Updates the count of the situation which you classified incorrectly
    * @param the_wrongSituation
    */
   public void updateArray(String the_wrongSituation){
      Integer fpCount = otherSituations.get(the_wrongSituation);
      if(fpCount == null){
         otherSituations.put(the_wrongSituation, 1);
      }else{
         fpCount = fpCount + 1;
         otherSituations.put(the_wrongSituation, fpCount);
      }
   }

   /**
    * Return 0 if num occurrances are equal
    * Return 1 if this has less occurrances
    * Return -1 if this has more occurrances.
    * NOTE: the results are reversed because we want the results to go high->low, not low->high
    */
   public int compareTo(AnnotatedResult otherAnnotatedResult) {
      int comparison = 0;
      if(count > otherAnnotatedResult.getNumOccurrances()){
         comparison = -1;
      }else if(count < otherAnnotatedResult.getNumOccurrances()){
         comparison = 1;
      }
      return comparison;
   }
}
