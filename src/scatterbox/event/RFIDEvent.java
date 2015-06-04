package scatterbox.event;

import java.util.Map;

import org.joda.time.DateTime;

import scatterbox.classifier.Classification;

/**
 * Placelab RFID event. Occurrs when a user comes into contact with an RFID tag.
 * @author seamusknox
 *
 */
public class RFIDEvent extends Event {

   private final String my_sensorID;

   private final String my_computerID;

   /**
    * Create a new event with the given parameters. An event either makes a
    * link between two nodes, or breaks a link between two nodes.
    * 
    * @param time
    *            The time the event takes place.
    */
   public RFIDEvent(DateTime time, String a_sensorID, String a_computerID) {
      super(time);
      my_sensorID = a_sensorID;
      my_computerID = a_computerID;
   }

   /**
    * Map based constructor to support instantiation via reflection.
    * @param eventDetails the object properties in map form.
    */
   public RFIDEvent(Map<String, String> eventDetails) {
      super(eventDetails.get("sensed_at"));
      my_sensorID = eventDetails.get("spotter_id");
      my_computerID = eventDetails.get("computer_id");
   }

   public String getSensorID() {
      return my_sensorID;
   }

   public String getComputerID() {
      return my_computerID;
   }

   @Override
   public String toString() {
      return "RFID Event: " +  super.getTime().toString() + " " + my_sensorID + " "
            + my_computerID;
   }

   @Override
   public String getFeature(){
      return "/PL_"+my_sensorID;
   }
   
   @Override
   public String getEventType() {
      return "RFID";
   }
   
   @Override 
   public Classification classify(){
      Classification rfidClassification = new Classification("/PL_"+my_sensorID, my_sensorID, 100, 10);
      return rfidClassification;
   }
}