package scatterbox.event;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import scatterbox.classifier.Classification;
import scatterbox.utils.SingleLineNTripleWriter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;

/**
 * An Event models either the creation or the destruction of a Link between two nodes.
 * 
 * @author Graham Williamson (graham.williamson@ucd.ie).
 */
public abstract class Event implements Comparable<Event> {

   public static long id_count = 0;
   public final long my_id; 
   
   private DateTime my_time;

   /**
    * Format of the sql timestamp.
    */
   final DateTimeFormatter my_sqlDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
   
   /**
    * Create a new event with the given parameters. An event either makes a
    * link between two nodes, or breaks a link between two nodes.
    * 
    * @param time
    *            The time the event takes place.
    */
   public Event(DateTime time){
      my_id = id_count++;
		my_time = time;
   }
   
   public Event(String time){
      my_id = id_count++;
      my_time = my_sqlDateFormat.parseDateTime(time);
   }
   
   public int compareTo(Event other) {
      int comparison = my_time.compareTo(other.my_time);
      if (comparison == 0) {
         comparison = my_id < other.my_id ? -1 : 1;
      }
      return comparison;
   }
   
   public String getEventType(){
      return null;
   }

   public Classification classify(){
      return null;
   }
   
   public DateTime getTime(){
      return my_time;
   }
   
   public String toString() {
      return my_time.toString();
   }
   
   public String getFeature(){
      return null;
   }
   
   public static String getOntology(String a_URL, String a_sensorType){
      final Model model = ModelFactory.createDefaultModel();
      // Get the observation Ontology from a local file.
      FileInputStream fis;
      try {
         fis = new FileInputStream(a_URL);
         model.read(fis, "http://ontonym.org/0.8/"+a_sensorType);
         fis.close();
      } catch (FileNotFoundException e1) {
         System.err.println("Could not find observation ontology!" + e1.getLocalizedMessage());
      } catch (IOException e) {
         System.err.println("IO Error reading observation ontology" + e.getLocalizedMessage());
      }
      return modelToNtriples(model);
   }
   
   /**
    * Converts a model to N-TRIPLES.
    * @param a_model the model to be converted to N-TRIPLEs.
    * @return the N-TRIPLE representation of the model
    */
   private static String modelToNtriples(final Model a_model) {
      if (a_model == null) {
         throw new IllegalArgumentException("Model is null");
      }
      String data = "";
      try {
         a_model.enterCriticalSection(Lock.READ);
         final ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
         new SingleLineNTripleWriter().write(a_model, byteOutStream, null);
         data = byteOutStream.toString();
      } finally {
         a_model.leaveCriticalSection();
      }
      return data;
   }
      

}