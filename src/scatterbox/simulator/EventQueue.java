package scatterbox.simulator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import scatterbox.event.ActivityEvent;
import scatterbox.event.BluetoothEvent;
import scatterbox.event.CalendarEvent;
import scatterbox.event.Event;
import scatterbox.event.IMEvent;
import scatterbox.event.KasterenEvent;
import scatterbox.event.PlacelabEvent;
import scatterbox.event.RFIDEvent;
import scatterbox.event.UbisenseEvent;

public class EventQueue extends ArrayList<Event>{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   Logger my_logger = Logger.getLogger(getClass().getName());

   public void addData(ResultSet[] some_resultSets){
      System.out.println(some_resultSets.length);
      String sensorType = "";
      try{
         for(ResultSet r:some_resultSets){
            if(r.next()){
               sensorType = r.getString("java_type");
               r.first();
               if(sensorType.equalsIgnoreCase("scatterbox.event.ActivityEvent")){
                  addActivityData(r);
               }else if(sensorType.equalsIgnoreCase("scatterbox.event.BluetoothEvent")){
                  addBluetoothData(r);
               }else if(sensorType.equalsIgnoreCase("scatterbox.event.CalendarEvent")){
                  addCalendarData(r);
               }else if(sensorType.equalsIgnoreCase("scatterbox.event.IMEvent")){
                  addMessengerData(r);
               }else if(sensorType.equalsIgnoreCase("scatterbox.event.UbisenseEvent")){
                  addUbisenseData(r);
               }else if(sensorType.equalsIgnoreCase("scatterbox.event.PlacelabEvent")){
                  addPlacelabData(r);
               }else if(sensorType.equalsIgnoreCase("scatterbox.event.RFIDEvent")){
                  addRFIDData(r);
               }else if(sensorType.equalsIgnoreCase("scatterbox.event.KasterenEvent")){
                  addTVKEvent(r);
               }
            }else{
               my_logger.warning("There appears to be nothing of type "+ sensorType +" in the specified time period");
            }
         }
      }catch(SQLException e){
         my_logger.severe(e.getMessage());
      }
   }

   /**
    * 
    * @param a_kasterenResult
    */
   public void addTVKEvent(ResultSet TVKEvents){
      boolean firstElement = false;
      try {
         //CHeck if there is anything in the result set
         firstElement = TVKEvents.next();
         //GO back to the start
         TVKEvents.beforeFirst();
         //If there is something there, iterate through, adding each reading to a list
         //of events to be sorted.
         if(firstElement){
            while(TVKEvents.next()){
               Timestamp t1 = TVKEvents.getTimestamp(1);
               Timestamp t2 = TVKEvents.getTimestamp(2);
               DateTime properFormatStart = new DateTime(t1.getTime());
               DateTime properFormatEnd = new DateTime(t2.getTime());
               KasterenEvent tvkEvent =  new KasterenEvent(properFormatStart, properFormatEnd,TVKEvents.getString(3));
               super.add(tvkEvent);
            }
         }
      } catch (SQLException e) {
         my_logger.warning("Error reading result set!");
      }
   }
   
   /**
    * 
    * @param a_placelabResult
    */
   public void addRFIDData(ResultSet an_RFIDResult){
      boolean firstElement = false;
      try {
         //CHeck if there is anything in the result set
         firstElement = an_RFIDResult.next();
         //GO back to the start
         an_RFIDResult.beforeFirst();
         //If there is something there, iterate through, adding each reading to a list
         //of events to be sorted.
         if(firstElement){
            while(an_RFIDResult.next()){
               Timestamp t = an_RFIDResult.getTimestamp(1);
               DateTime properFormatDate = new DateTime(t.getTime());
               RFIDEvent rfidEvent =  new RFIDEvent(properFormatDate,an_RFIDResult.getString(2), 
                     an_RFIDResult.getString(3));

               super.add(rfidEvent);
            }
         }
      } catch (SQLException e) {
         my_logger.warning("Error reading result set!");
      }
   }
   
   /**
    * 
    * @param a_placelabResult
    */
   public void addPlacelabData(ResultSet a_placelabResult){
      boolean firstElement = false;
      try {
         //CHeck if there is anything in the result set
         firstElement = a_placelabResult.next();
         //GO back to the start
         a_placelabResult.beforeFirst();
         //If there is something there, iterate through, adding each reading to a list
         //of events to be sorted.
         if(firstElement){
            while(a_placelabResult.next()){
               Timestamp t = a_placelabResult.getTimestamp(1);
               DateTime properFormatDate = new DateTime(t.getTime());
               PlacelabEvent placelabEvent =  new PlacelabEvent(properFormatDate,a_placelabResult.getInt("sensor_type"), 
                     a_placelabResult.getString("sensor_id"), 
                     a_placelabResult.getInt("sensor_reading"));

               super.add(placelabEvent);
            }
         }
      } catch (SQLException e) {
         my_logger.warning("Error reading result set!");
      }
   }
   
   /**
    * 
    * @param a_gTalkResult
    */
   public void addMessengerData(ResultSet a_gTalkResult){
      boolean firstElement = false;
      try {
         //CHeck if there is anything in the result set
         firstElement = a_gTalkResult.next();
         //GO back to the start
         a_gTalkResult.beforeFirst();
         //If there is something there, iterate through, adding each reading to a list
         //of events to be sorted.
         if(firstElement){
            while(a_gTalkResult.next()){
               Timestamp t = a_gTalkResult.getTimestamp(1);
               DateTime properFormatDate = new DateTime(t.getTime());

               IMEvent IMevent =  new IMEvent(properFormatDate,a_gTalkResult.getString(2), 
                     a_gTalkResult.getString(3), 
                     a_gTalkResult.getString(4));

               super.add(IMevent);
            }
         }
      } catch (SQLException e) {
         my_logger.warning("Error reading result set!");
      }
   }

   /**
    * 
    * @param a_CalendarResult
    */
   public void addCalendarData(ResultSet a_CalendarResult){
      boolean firstElement;
      try {
         firstElement = a_CalendarResult.first();
         a_CalendarResult.beforeFirst();
         if(firstElement)
            while(a_CalendarResult.next()){
               String eventID = a_CalendarResult.getString(1);
               DateTime readingTime = new DateTime(a_CalendarResult.getTimestamp(2).getTime());
               String title = a_CalendarResult.getString(3);
               String[] participants = a_CalendarResult.getString(4).split(",");
               DateTime eventStartTime = new DateTime(a_CalendarResult.getTimestamp(5).getTime());
               DateTime eventEndTime = new DateTime(a_CalendarResult.getTimestamp(6).getTime());
               String location = a_CalendarResult.getString(7);
               String description = a_CalendarResult.getString(8);
               String username = a_CalendarResult.getString(9);
               CalendarEvent calendarEvent = 
                  new CalendarEvent(username, 
                        readingTime, 
                        eventID, 
                        title, 
                        participants, 
                        eventStartTime, 
                        eventEndTime, 
                        location, 
                        description);
               System.out.println(calendarEvent.toString());
               super.add(calendarEvent);
            }
      } catch (SQLException e) {
         my_logger.warning("Error processing calendar data" + e.getMessage());
      }
   }

   /**
    * Adds all activity data for a person into a sorted list.
    * @param an_ActivityResult
    */
   public void addActivityData(ResultSet an_ActivityResult){
      boolean firstElement = false;
      try {
         //Check if there is anything in the result set
         firstElement = an_ActivityResult.first();
         //GO back to the start
         an_ActivityResult.beforeFirst();
         //If there is something there, iterate through, adding each reading to a list
         //of events to be sorted.
         if(firstElement){
            while(an_ActivityResult.next()){
               Timestamp t = an_ActivityResult.getTimestamp(1);
               DateTime properFormatDate = new DateTime(t.getTime());

               ActivityEvent activityEvent =  new ActivityEvent(properFormatDate,an_ActivityResult.getInt(2), 
                     an_ActivityResult.getInt(3), 
                     an_ActivityResult.getString(4));
               super.add(activityEvent);
            }
         }
      } catch (SQLException e) {
         my_logger.warning("Error reading result set!" + e.getLocalizedMessage());
      }
   }

   /**
    * Add any bluetooth readings to the list
    * @param a_bluetoothResult
    */
   public void addBluetoothData(ResultSet a_bluetoothResult){
      boolean firstElement = false;
      try {
         //CHeck if there is anything in the result set
         firstElement = a_bluetoothResult.next();
         //GO back to the start
         a_bluetoothResult.beforeFirst();
         //If there is something there, iterate through, adding each reading to a list
         //of events to be sorted.
         if(firstElement){
            while(a_bluetoothResult.next()){

               Timestamp t = a_bluetoothResult.getTimestamp(1);
               DateTime properFormatDate = new DateTime(t.getTime());
               BluetoothEvent btevent =  new BluetoothEvent(properFormatDate,
                     a_bluetoothResult.getString(2), 
                     a_bluetoothResult.getString(3), 
                     a_bluetoothResult.getString(4));
               super.add(btevent);
            }
         }
      } catch (SQLException e) {
         my_logger.warning("Error reading result set!");
      }
   }

   /**
    * Add ubisense data to the event queue
    */
   public void addUbisenseData(ResultSet a_ubisenseResult){
      boolean firstElement = false;
      try {
         //CHeck if there is anything in the result set
         firstElement = a_ubisenseResult.next();
         //GO back to the start
         a_ubisenseResult.beforeFirst();
         //If there is something there, iterate through, adding each reading to a list
         //of events to be sorted.
         if(firstElement){
            while(a_ubisenseResult.next()){
               Timestamp t = a_ubisenseResult.getTimestamp(1);
               DateTime properFormatDate = new DateTime(t.getTime());
               UbisenseEvent ubivent =  new UbisenseEvent(properFormatDate,
                     a_ubisenseResult.getDouble(2), 
                     a_ubisenseResult.getDouble(3), 
                     a_ubisenseResult.getDouble(4),
                     a_ubisenseResult.getString(5), 
                     a_ubisenseResult.getString(6));

               super.add(ubivent);
            }
         }
      } catch (SQLException e) {
         my_logger.warning("Error reading result set!");
      }
   }
}
