package scatterbox.event;

import org.joda.time.DateTime;

/**
 * Computer activity event. This occurs every X seconds, and reports the number of mouse and keyboard 
 * presses in that time.
 * @author stephenknox
 *
 */
public class ActivityEvent extends Event {
   
   //private static final String OBS = "rdf/activity_observation.owl";
   //public static String my_ObservationNTriple = getOntology(OBS, "activity");
   
   public final int my_keystrokes;
   public final int my_mousePresses;
   public final String my_user;

   /**
    * Create a new event with the given parameters. An event either makes a
    * link between two nodes, or breaks a link between two nodes.
    * 
    * @param time
    *            The time the event takes place.
    */
   public ActivityEvent(DateTime time, int keystrokes, int mousepresses, String user) {
      super(time);
      my_keystrokes = keystrokes;
      my_mousePresses = mousepresses;
      my_user = user;
   }
   
   @Override
   public String toString() {
      return super.getTime().toString() + " " 
      + String.valueOf(my_keystrokes) + " " 
      + String.valueOf(my_mousePresses) + " " 
      + my_user;
   }
   @Override
   public String getEventType(){
      return "activity";
   }
   
   public String convertToRDF(){
      String RDF = "";
      
      return RDF;
   }

}