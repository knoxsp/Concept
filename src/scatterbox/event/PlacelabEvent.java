package scatterbox.event;

import java.util.Map;

import org.joda.time.DateTime;

import scatterbox.classifier.Classification;

/**
 * This class covers water, gas, current and light events.
 * Each of these events have the same properties, hence one class can be used
 * to cover them all. They are distinguished by the event type and sensor type variables.
 * @author stephenknox
 *
 */
public class PlacelabEvent extends Event {

   private final String my_sensorID;

   private final int my_sensorType;
   
   private final int my_sensorReading;

   /**
    * Create a new event with the given parameters. An event either makes a
    * link between two nodes, or breaks a link between two nodes.
    * 
    * @param time
    *            The time the event takes place.
    */
   public PlacelabEvent(DateTime time, int a_sensorType, String a_sensorID, int a_sensorReading) {
      super(time);
      my_sensorID = a_sensorID;
      my_sensorType = a_sensorType;
      my_sensorReading = a_sensorReading;
   }

   /**
    * Map based constructor to support instantiation via reflection.
    * @param eventDetails the object properties in map form.
    */
   public PlacelabEvent(Map<String, String> eventDetails) {
      super(eventDetails.get("sensed_at"));
      my_sensorID = eventDetails.get("spotter_id");
      my_sensorType = Integer.parseInt(eventDetails.get("sensor_type"));
      my_sensorReading = Integer.parseInt(eventDetails.get("sensor_reading"));
   }

   public String getSensorID() {
      return my_sensorID;
   }

   public int getSensorType() {
      return my_sensorType;
   }

   public int getSensorReading(){
      return my_sensorReading;
   }
   @Override
   public String getEventType(){
      return "PlacelabEvent";
   }
   
   @Override
   public String getFeature(){
      return "/PL_"+my_sensorID;
   }
   
   @Override
   public String toString() {
      return "Placelab Event: " +  super.getTime().toString() + " " + my_sensorID + " " + my_sensorReading;
   }
   
   @Override 
   public Classification classify(){  
      Classification environmentClassification = new Classification("/PL_"+my_sensorID, my_sensorID, 100, 10);
      return environmentClassification;
   }
}