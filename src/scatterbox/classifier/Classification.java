package scatterbox.classifier;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;


public class Classification implements Comparable<Classification> {

   /**
    * A classified event must contain a type(location, ical, activity), a value("3rd floor","meeting" "active")
    * and a confidence in that classification.
    */
   private String my_type;

   private String my_value;

   private int my_confidence;

   /**
    * Decay refers to the percentage drop in the confidence value at every time cycle.
    */
   private int my_decay = 0;
   
   /**
    * The set of decay values associated with every classification. This is retrieved from the decay.properties file.
    */
   JSONObject decayCategories;

   /**
    * Logger for the class
    */
   private Logger my_logger = Logger.getLogger(getClass().getName());
   
   /**
    * Constructor without the decay value included. The decay gets loaded from the properties file. 
    * @param a_type
    * @param a_value
    * @param a_confidence
    */
   public Classification(String a_type, String a_value, int a_confidence) {
      my_type = a_type;
      my_value = a_value;
      my_confidence = a_confidence;
      
      /**
       * Retrieve decay categories.
       * Then retrieve the classification/decay pairs
       * Then retrieve the appropriate decay value.
       */
      try {
         JSONObject values = (JSONObject) Classifier.decayProperties.get(a_type.substring(1));
         my_decay = (Integer) values.get(a_value);
      } catch (JSONException e) {
         my_logger.severe("Problem with loading JSON object: " + e.getMessage());
      }
   }

   /**
    * Constructor which allows direct setting of the decay value
    * @param a_type
    * @param a_value
    * @param a_confidence
    * @param a_decay
    */
   public Classification(String a_type, String a_value, int a_confidence, int a_decay) {
      my_type = a_type;
      my_value = a_value;
      my_confidence = a_confidence;
      my_decay = a_decay;
   }
   
   /**
    * Compares two classification objects using their confidence values.
    */
   public int compareTo(Classification c) {
      if (this.getConfidence() < c.getConfidence()) {
         return -1;
      } else if (this.getConfidence() > c.getConfidence()) {
         return 1;
      } else {
         return 0;
      }
   }

   @Override
   public int hashCode(){
      int hashcode = 0;
      for(int i = 0;i<my_type.length();i++){
         hashcode += Character.getNumericValue(my_type.charAt(i));
      }
      for(int i = 0;i<my_value.length();i++){
         hashcode += Character.getNumericValue(my_value.charAt(i));
      }
      return hashcode;
   }
   @Override
   public boolean equals(Object obj) {
      System.out.println("Checking");
      boolean isEqual = false;
      Classification a_classification = (Classification) obj;
      if (a_classification.getType().equalsIgnoreCase(my_type)
            && a_classification
            .getValue().equalsIgnoreCase(my_value)) {
         isEqual = true;
      }

      return isEqual;
   }

   /**
    * Update the confidence value of this classification
    * @param a_confidence
    */
   public void setConfidence(int a_confidence) {
      my_confidence = a_confidence;
   }

   public int getConfidence() {
      return my_confidence;
   }

   public String getType() {
      return my_type;
   }

   public String getValue() {
      return my_value;
   }

   public void setDecay(int a_decay) {
      my_decay = a_decay;
   }

   public int getDecay() {
      return my_decay;
   }

   public String toString() {
      String classificationString = this.my_type + "_" + this.my_value + "_"
      + this.my_confidence;
      return classificationString;
   }

}
