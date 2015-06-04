package scatterbox.annotations;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import scatterbox.properties.Properties;
import scatterbox.simulator.Simulator;

public class CASLAnnotationProcessor extends AnnotationProcessor {

   /**
    * Logger for the class
    */
   private Logger my_logger = Logger.getLogger(getClass().getName());

   /**
    * Retrieves the situation which corresponds to the annotation at
    * the given time. 
    * @param a timestamp
    * @return the situation
    */
   public String getSituation(DateTime a_timestamp) {

      String situation = "";

      String[] beforeAndAfter = getSurroundingAnnotations(a_timestamp);

      String before = beforeAndAfter[0];
      String after = beforeAndAfter[1];

      /*
       * "Back" means the person has returned to their desk.
       */
      if (before.equalsIgnoreCase("back")
            || before.toLowerCase().contains("arrive")) {
         situation = "atdesk";
      } else if (after.equalsIgnoreCase("back")
            || after.toLowerCase().contains("arrive")) {
         situation = processAnnotation(before);
      }else{
         situation = processAnnotation(before);
      }
      
      addAnnotation(situation);
      
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
      
      return situation;
   }
   /**
    * Find the situation annotation before and after the current time.
    * The appropriate annotation should then be processed.
    * @param a_timestamp
    * @return
    */
   public String[] getSurroundingAnnotations(DateTime a_timestamp) {
      Timestamp sqlFormatTimestamp = new Timestamp(a_timestamp.getMillis());
      String[] beforeAndAfter = new String[2];
      String username = getUsername();
      String previousAnnotationQuery = "select annotation from annotations where username=\""+username+"\" and timestamp < \""
      + sqlFormatTimestamp + "\" order by timestamp desc limit 1;";
      String nextAnnotationQuery = "select annotation from annotations where username=\""+username+"\" and timestamp > \""
      + sqlFormatTimestamp + "\" order by timestamp limit 1;";

      ResultSet previousAnnotation = Simulator.my_databaseHandler.QueryDatabase(previousAnnotationQuery);
      ResultSet nextAnnotation = Simulator.my_databaseHandler.QueryDatabase(nextAnnotationQuery);

      try {
         if(previousAnnotation.next()){
            previousAnnotation.first();
            beforeAndAfter[0] = previousAnnotation.getString(1);
         }else{
            beforeAndAfter[0] = "unknown";
         }
         if(nextAnnotation.next()){
            nextAnnotation.first();
            beforeAndAfter[1] = nextAnnotation.getString(1);
         }else{
            beforeAndAfter[1] = "unknown";
         }
         if(beforeAndAfter[0].equalsIgnoreCase("unknown")){
            beforeAndAfter[0] = beforeAndAfter[1];
         }else if(beforeAndAfter[1].equalsIgnoreCase("unknown")){
            beforeAndAfter[1] = beforeAndAfter[0];
         }

      } catch (SQLException e) {
         my_logger
         .warning("Your annotation queries were wrong, or the database does not contain the appropriate entry: "
               + e.getMessage());
      }
      return beforeAndAfter;
   }


   /**
    * Takes an annotation and figures out what the corresponding situation is.
    * @param an_annotation
    * @return
    */
   public String processAnnotation(String an_annotation) {
      String situation = "unknown";
      /*
       * If the annotation contains an @ sign, it means that the annotator
       * included the location by saying "lunch @ blah" or said "@ lunch"
       * In the former case, the annotation before the @ is what we want.
       */
      if(an_annotation.contains("@") && an_annotation.charAt(0)!='@'){
         String[] sectionedAnnotation = an_annotation.split("@");
         an_annotation = sectionedAnnotation[0];
      }
      if (an_annotation.contains("tea") || an_annotation.contains("break")
            || an_annotation.contains("coffee")) {
         situation = "break";
      } else if (an_annotation.contains("lunch")) {
         situation = "lunch";
      } else if (an_annotation.contains("talking") || an_annotation.contains(" desk")) {
         situation = "meeting";
      } else if (an_annotation.contains("shower")
            || an_annotation.contains("toilet")) {
         situation = "toilet";
      }
      return situation;
   }

   public static void main(String[] args) {
      CASLAnnotationProcessor a = new CASLAnnotationProcessor();
      String sit = a.getSituation(new DateTime(2008, 12, 11, 10, 45, 10, 0));
      System.out.println(sit);
   }

   /**
    * Get the username of the user whose annotation is being processed.
    * @return
    */
   public String getUsername(){
      String username = "NOUSERNAME";

      try {
         Properties.load(Simulator.propertiesFile);
         JSONObject properties = Properties.getProperties();
         username = properties.getString("annotationusername");
      } catch (JSONException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }          
      return username;
   }

}
