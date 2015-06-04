package scatterbox.annotations;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;

public abstract class AnnotationProcessor {
   
   /**
    * Logger for the class
    */
   public Logger my_logger = Logger.getLogger(getClass().getName());
   public List<AnnotatedResult> annotations = new LinkedList<AnnotatedResult>();
   List<String> annotationRecord  = new LinkedList<String>();
   
   public String getSituation(DateTime a_timestamp){
      return null;
   }
   
   public void incrementCorrectSituation(String annotation){
      for(AnnotatedResult ar:annotations){
         if(ar.getAnnotation().equalsIgnoreCase(annotation)){
            ar.incrementNumCorrectResults();
            return;
         }
      }
   }
   
   /**
    * @param annotation
    * @param incorrectSolution
    */
   public void incrementIncorrectSituation(String annotation, String incorrectSolution){
      //If a training set is being used, some situations may never have been seen before, so they
      //need to be added.
      int annotationIndex = getIndexOfAnnotation(incorrectSolution);
      if(annotationIndex==-1){
         annotations.add(new AnnotatedResult(incorrectSolution, 1));
      }
      
      for(AnnotatedResult ar:annotations){
         if(ar.getAnnotation().equalsIgnoreCase(annotation)){
            ar.incrementNumIncorrectResults();
            ar.updateArray(incorrectSolution);
         }
      }
      
      for(AnnotatedResult ar:annotations){
         if(ar.getAnnotation().equalsIgnoreCase(incorrectSolution)){
            ar.incrementFalsePositive();
         }
      }
      
      
   }
   
   /**
    * Returns the position in the list of this annotation
    * returns -1 if the annotation is not found.
    * @param annotation
    * @return
    */
   public int getIndexOfAnnotation(String annotation){
      String currentAnnotation;
      for(int i=0;i<annotations.size();i++){
         currentAnnotation = annotations.get(i).getAnnotation();
         if(currentAnnotation.equalsIgnoreCase(annotation)){
            return i;
         }
      }
      //If this annotation is not present in the current list, return -1
      return -1;
   }
   
   public void addAnnotation(String a_situation){
      int annotationIndex = getIndexOfAnnotation(a_situation);
      //If a new situation has been created by the merging of two situations,
      //add it to the hashtable, otherwise update the situation.
      if(annotationIndex==-1){
         System.out.println("First addition of " + a_situation);
         annotations.add(new AnnotatedResult(a_situation, 1, 1));
      }else{
         annotations.get(annotationIndex).incrementOccurrenceCount();
      }
   }
   
   /**
    * Generate a confusion matrix from each of the recorded annotations.
    * @return
    */
   public int[][] generateConfusionMatrix(){
      int matrixSize = annotations.size();
      System.out.println(matrixSize);
      int[][] confusionMatrix = new int[matrixSize][matrixSize];
      //Initialise each element to the default value of 0
      for(int i=0;i<matrixSize;i++){
         for(int j=0;j<matrixSize;j++){
            confusionMatrix[i][j] = 0;
         }
      }
      int index;
      //for each classification, put correct results into the classification
      //for each incorrect classification, add the appropriate result to that element
      for(AnnotatedResult ar:annotations){
         index = getIndexOfAnnotation(ar.getAnnotation());
         confusionMatrix[index][index] = ar.getNumCorrectResults();
         
         
         int indexOfOther;
         for(String incorrectClassification : ar.otherSituations.keySet()){
            //System.out.println(incorrectClassification);
            indexOfOther = getIndexOfAnnotation(incorrectClassification);
            int numIncorrectClassifications = ar.otherSituations.get(incorrectClassification);
            confusionMatrix[index][indexOfOther] = numIncorrectClassifications; 
         }
      }
      
      return confusionMatrix;
   }
   
   /**
    * Checks to see if a situation has been processed before.
    * @param a_situation
    * @return
    */
   public int seenSituationBefore(String a_situation){
      //If this is the first check.
      if(annotationRecord.size()==0){
         return -1;
      }
      
      for(int i=0;i<annotationRecord.size(); i++){
         String sit = annotationRecord.get(i);
            if(sit.equalsIgnoreCase(a_situation)){
               return i;
            }
      }
         String[] situationArray = a_situation.split("AND");
         for(int i=0;i<annotationRecord.size(); i++){
            String sit = annotationRecord.get(i);
            if(sit.contains("AND")){
               String[] testSituationArray = sit.split("AND");
               if(testSituationArray.length == situationArray.length){
                  int numMatches = 0;
                  for(String s : situationArray){
                     for(String s1:testSituationArray){
                        if(s.equalsIgnoreCase(s1)){
                           numMatches++;
                           break;
                        }
                     }
                  }
                  if(numMatches == situationArray.length){
                     //The combination has been seen before, at index i
                     return i;
                  }
               }else{
                  return -1;
               }
            }
         }
      return -1;
   }
   
   /**
    * Designed to be overwritten, this is used should there be a number of categories 
    * of situation
    * @return
    */
   public String getCategory(String annotation){
      return null;
   }
   
}
