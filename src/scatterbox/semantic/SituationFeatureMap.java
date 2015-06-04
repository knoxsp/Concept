package scatterbox.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SituationFeatureMap {

   String activity;

   private HashMap<String, Integer> valueMap = new HashMap<String, Integer>();
   
   public SituationFeatureMap(String an_activity){
      activity = an_activity;
   }

   /**
    * Add a new feature to the feature map. 
    * The index of the feature and value are the same in the lists.
    * @param feature
    */
   public void add(String feature){
      if(valueMap.containsKey(feature)){
         int v = valueMap.get(feature);
         int v1 = v + 1;
         valueMap.put(feature, v1);
      }else{
         valueMap.put(feature, 1);
      }
   }

   public String getActivity(){
      return activity;
   }
   /**
    * Get the list containing all the features associated with this situation
    * @return
    */
   public List<String> getFeatures(){
      Set<String> s = valueMap.keySet();
      Object[] keyArray = s.toArray();
      List<String> features = new ArrayList<String>();
      for(int i=0;i<keyArray.length;i++){
         features.add((String)keyArray[i]);
      }
      return features;
   }
   /**
    * Get the list containing all the count of features.
    * @return
    */
   public List<Integer> getValues(){
      Collection<Integer> vals = valueMap.values();
      Iterator<Integer> valIterator = vals.iterator();
      List<Integer> values = new ArrayList<Integer>();
      while(valIterator.hasNext()){
         values.add(valIterator.next());
      }
      
      return values;
   }
   
   /**
    * Print the mapping between features and their counts.
    */
   public void printfeatureMap(){
      Set<String> keys = valueMap.keySet();
      Collection<Integer> vals = valueMap.values();
      
      Iterator<String> ki = keys.iterator();
      Iterator<Integer> vi = vals.iterator();
      while(ki.hasNext()){
         System.out.println(ki.next() + ", " + vi.next());
      }
   }

   /**
    * Remove the features which are not regularly occurring.
    */
   public void filterMap(){
      int average = getAverage();
      Set<String> s = valueMap.keySet();
      Object[] keyArray = s.toArray();
     
      for(int i=0;i<keyArray.length;i++){
         String keyString = (String)keyArray[i];
         int value = valueMap.get(keyString);
         if(value < average){
            valueMap.remove(keyString);
         }
      }
   }

   /**
    * Get the average of all values in the hash map
    * @return
    */
   private int getAverage(){
      Collection<Integer> c = valueMap.values();
      Iterator<Integer> i = c.iterator();
      //Get the average of the feature counts
      int averageReading = 0;
      while(i.hasNext()){
         averageReading = averageReading + i.next();
      }
      averageReading = averageReading/valueMap.size();

      return averageReading;
   }
}
