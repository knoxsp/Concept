package scatterbox.event;
import org.joda.time.DateTime;

public class CalendarEvent extends Event {
   
   public final String my_gcalid;
   public final String my_location;
   public final String[] my_participants;
   public final String my_description;
   public final String my_title;
   public final DateTime my_startTime;
   public final DateTime my_endTime;
   public final DateTime my_readingTime;
   public final String my_username;
   
   /**
    * Create a new event with the given parameters. An event either makes a
    * link between two nodes, or breaks a link between two nodes.
    * 
    * @param time
    *            The time the event takes place.
    */
   public CalendarEvent(String username, DateTime readingTime, String gcalid, String title, String[] participants, DateTime starttime, DateTime endtime, String location, String description ) {
      super(endtime);
      my_readingTime = readingTime;
      my_gcalid = gcalid;
      my_title = title;
      my_participants = participants;
      my_startTime = starttime;
      my_endTime = endtime;
      my_location = location;
      my_description = description;
      my_username = username;
   }
   
   @Override
   public String toString() {
      return super.getTime().toString() + " " 
      + my_gcalid + " " 
      + my_title + " " 
      + my_startTime.toString()+ " "
      + my_endTime.toString()+ " "
      + super.getTime().toString()+ " "
      + "http://example.com/myLocations#"+my_location;
   }

   public String getEventType(){
      return "calendar";
   }
}