package scatterbox.semantic;

import java.util.ArrayList;

public class FeatureSorter extends ArrayList<FeatureCount>{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private String my_activity;
   private int my_activityCount;

   public FeatureSorter(String an_activity){
      my_activity = an_activity;
   }


   public String getActivity(){
      return my_activity;
   }

   public int getActivityCount(){
      return my_activityCount;
   }

   public void incrementActivityCount(){
      my_activityCount = my_activityCount + 1;
   }

   public int getAverage(){
      int average = 0;
      FeatureCount tempFC;
      for(int i=0;i<super.size()-1;i++){
            tempFC = super.get(i);
            average = average + tempFC.getCount();
      }
      average = average/super.size();
      return average;
   }

   /**
    * Checks whether a feature is contained in the list already
    * @param a_feature
    * @return
    */
   public boolean contains(String a_feature){
      boolean contains = false;
      FeatureCount tempFC;
      for(int i=0;i<super.size();i++){
         tempFC = super.get(i);
         if(tempFC.getFeature().equalsIgnoreCase(a_feature)){
            contains = true;
         }
      }
      return contains;
   }

   public void addFeature(String a_feature){
      FeatureCount tempFC;
      for(int i=0;i<super.size();i++){
         tempFC = super.get(i);
         if(tempFC.getFeature().equalsIgnoreCase(a_feature)){
            tempFC.incrementCount();
         }
      }
   }
}
