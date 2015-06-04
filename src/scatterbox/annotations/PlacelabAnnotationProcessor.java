package scatterbox.annotations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.joda.time.DateTime;

import scatterbox.simulator.CBRHandler;
import scatterbox.simulator.Simulator;

import cbml.cbr.FeatureStruct;
import cbml.cbr.IncompatableFeatureException;

public class PlacelabAnnotationProcessor extends AnnotationProcessor{

   /**
    * Retrieves the situation which corresponds to the annotation at
    * the given time. 
    * @param a timestamp
    * @return the situation
    */
   @Override
   public String getSituation(DateTime a_timestamp) {

      String situation = "";
      try {
      Timestamp sqlFormatTimestamp = new Timestamp(a_timestamp.getMillis());
      
      String annotationQuery = "select annotation from annotations where start_time < \""
         + sqlFormatTimestamp + "\" and end_time > \""+sqlFormatTimestamp+"\"" +
         		"and category=\"activity\"";

      ResultSet annotation = Simulator.my_databaseHandler.QueryDatabase(annotationQuery);
      
         //There may be multiple annotations for the same time.
         //So we append the new situation to the the string, together with AND tags.
         while(annotation.next()){
            String s = getCategory(annotation.getString(1));
            //This checks whether the current category has already been seen in this cycle.
            if(!situation.contains(s)){
               addAnnotation(s);
               situation += s+"AND";
            }
         }
         
         //If the resultset is empty -- i.e. there is no annotation for this time.
         if(situation.equalsIgnoreCase("")){
            situation = "unknown";
            return situation;
         }
         
         //Remove trailing AND -- TODO this is a hack, change it.
         situation = situation.substring(0, situation.length()-3);
         
         /**
          * If multiple situations have been seen before, but in a different order, 
          * revert to the order that has already been seen Ex,
          * X and Y and Z has been seen. If Z and X and Y is seen, revert to X, Y, Z
          */
         int seenBefore = seenSituationBefore(situation); 
         if(seenBefore != -1){
            situation = annotationRecord.get(seenBefore);
         }else{
            annotationRecord.add(situation);
         }
         
      } catch (SQLException e) {
         my_logger
         .warning("Your annotation queries were wrong, or the database does not contain the appropriate entry: "
               + e.getMessage());
      }
      //return the string minus the trailing AND
      return situation;
   }
   
   public String getCategory(String s){
      String category = "";
      FeatureStruct featureStruct = CBRHandler.my_caseStruct.getFeatureStruct("/situation");
      try {
         List l = featureStruct.getValues();
         for(int i=0;i<l.size();i++){
            String feature = (String) l.get(i);
            String[] features = feature.split("/");
            if(features[features.length-1].equalsIgnoreCase(s)){
            //The feature looks like: "/blah", and is turned into [][blah]
            //or it looks like: /blah/boo, which turns to [][boo][blah]
            //Either way, take the feature from [1]
            category = features[1];
            }
         }
      } catch (IncompatableFeatureException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
//         String leaf = path.substring(path.lastIndexOf('/'));
//         if(s.equalsIgnoreCase(leaf)){
//            
//         }
      return category;
      
   }
}
