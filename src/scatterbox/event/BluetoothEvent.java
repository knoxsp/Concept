package scatterbox.event;

import java.util.Map;

import org.joda.time.DateTime;

/*
 * * An Event models either the creation or the destruction of a Link between
 * two nodes.
 * 
 * @author Graham Williamson (graham.williamson@ucd.ie).
 */
public class BluetoothEvent extends Event {

   private final String my_spottedMac;

   private final String my_deviceType;

   private final String my_spotterID;

   /**
    * Create a new event with the given parameters. An event either makes a
    * link between two nodes, or breaks a link between two nodes.
    * 
    * @param time
    *            The time the event takes place.
    */
   public BluetoothEvent(DateTime time, String mac, String type, String id) {
      super(time);
      my_spottedMac = mac;
      my_deviceType = type;
      my_spotterID = id;
   }

   /**
    * Map based constructor to support instantiation via reflection.
    * @param eventDetails the object properties in map form.
    */
   public BluetoothEvent(Map<String, String> eventDetails) {
      super(eventDetails.get("sensed_at"));
      my_spottedMac = eventDetails.get("mac_address");
      my_deviceType = eventDetails.get("device_type");
      my_spotterID = eventDetails.get("spotter_address");
   }

   public String getSpotterID() {
      return my_spotterID;
   }

   public String getSpottedMac() {
      return my_spottedMac;
   }

   public String getDeviceType() {
      return my_deviceType;
   }

   @Override
   public String toString() {
      return "BluetoothEvent: " +  super.getTime().toString() + " " + my_spottedMac + " "
            + my_deviceType + " " + my_spotterID;
   }

   @Override
   public String getEventType() {
      return "bluetooth";
   }
}