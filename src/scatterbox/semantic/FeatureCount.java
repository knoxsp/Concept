package scatterbox.semantic;

public class FeatureCount implements Comparable<FeatureCount>{

   //The number of times this feature has been seen
   private int my_count;
   //The name of the feature
   private String my_feature;
   
   /**
    * The relevance of this feature to a situation. 
    */
   private double igain = 0;
   
   public FeatureCount(String a_feature, int a_count){
      my_count = a_count;
      my_feature = a_feature;
   }
   
   /**
    * The the relevance of this feature to a given situation
    * @return
    */
   public double getIGain(){
      return igain;
   }
   /**
    * Set the relevance of this feature to a given situation. 
    * @param a_relevance
    */
   public void setIGain(double an_igain){
      igain = an_igain;
   }
   
   public int getCount(){
      return my_count;
   }
   
   public String getFeature(){
      return my_feature;
   }
   
   public int incrementCount(){
      my_count = my_count + 1;
      return my_count;
   }
   
   //compared the number of occurrances of features for ordering
   //Returns results in the opposite way you would expect, because
   //we want the ordering to be high->low, not low->high
//   public int compareTo(FeatureCount a_featureCount) {
//      int comparison = 0;
//      if(my_count > a_featureCount.getCount()){
//         comparison =  -1;
//      }else if(igain < a_featureCount.getCount()){
//         comparison =  1;
//      }      
//      return comparison; 
//   }
   
   public int compareTo(FeatureCount a_featureCount) {
      int comparison = 0;
      if(igain > a_featureCount.getIGain()){
         comparison =  -1;
      }else if(igain < a_featureCount.getIGain()){
         comparison =  1;
      }      
      return comparison; 
   }

}
