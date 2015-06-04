package scatterbox.event;

import java.util.Map;

import org.joda.time.DateTime;

/**
 * A ubisense event 
 */
public class UbisenseEvent extends Event {
   private final double my_xCoordinate;

   private final double my_yCoordinate;

   private final double my_zCoordinate;

   private final String my_tagID;

   private final String my_username;

   /**
    * Create a new event with the given parameters. An event either makes a
    * link between two nodes, or breaks a link between two nodes.
    * 
    * @param time
    *            The time the event takes place.
    */
   public UbisenseEvent(DateTime time, double x, double y, double z, String id,
         String name) {
      super(time);
      my_xCoordinate = x;
      my_yCoordinate = y;
      my_zCoordinate = z;
      my_tagID = id;
      my_username = name;
   }

   /**
    * Map based constructor to support instantiation via reflection.
    * @param eventDetails the object properties in map form.
    */
   public UbisenseEvent(Map<String, String> eventDetails) {
      super(eventDetails.get("sensed_at"));
      my_xCoordinate = Double.valueOf(eventDetails.get("x"));
      my_yCoordinate = Double.valueOf(eventDetails.get("y"));
      my_zCoordinate = Double.valueOf(eventDetails.get("z"));
      my_tagID = eventDetails.get("tag_id");
      my_username = "No Name";
   }

   public double getX() {
      return my_xCoordinate;
   }

   public double getY() {
      return my_yCoordinate;
   }

   public double getZ() {
      return my_zCoordinate;
   }

   public String getTagID() {
      return my_tagID;
   }

   public String getUserName() {
      return my_username;
   }

   @Override
   public String getEventType() {
      return "ubisense";
   }

   @Override
   public String toString() {
      return "UbisenseEvent: " + super.getTime().toString() + " "
            + my_xCoordinate + " " + my_yCoordinate + " " + my_zCoordinate
            + " " + my_tagID + " " + my_username;
   }

}