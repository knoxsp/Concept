package scatterbox.annotations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.joda.time.DateTime;

import scatterbox.simulator.Simulator;

public class KasterenAnnotationProcessor extends AnnotationProcessor {

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
               + sqlFormatTimestamp
               + "\" and end_time > \""
               + sqlFormatTimestamp + "\"";

         ResultSet annotation = Simulator.my_databaseHandler
               .QueryDatabase(annotationQuery);
         //There may be multiple annotations for the same time.
         //So we append the new situation to the the string, together with AND tags.
         while (annotation.next()) {
            String s = annotation.getString("annotation");
            //This checks whether the current category has already been seen in this cycle.
               addAnnotation(s);
               situation += s + "AND";
         }

         //If the resultset is empty -- i.e. there is no annotation for this time.
         if (situation.equalsIgnoreCase("")) {
            situation = "unknown";
            return situation;
         }

         //Remove trailing AND -- TODO this is a hack, change it.
         situation = situation.substring(0, situation.length() - 3);

         /**
          * If multiple situations have been seen before, but in a different order, 
          * revert to the order that has already been seen Ex,
          * X and Y and Z has been seen. If Z and X and Y is seen, revert to X, Y, Z
          */
         int seenBefore = seenSituationBefore(situation);
         if (seenBefore != -1) {
            situation = annotationRecord.get(seenBefore);
         } else {
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
}
