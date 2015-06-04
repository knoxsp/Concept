package scatterbox.event;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.joda.time.DateTime;

/**
 * The event for an instant messenger.
 * @author stephenknox
 *
 */
public class IMEvent extends Event {

   private final String my_email;
   private final String my_availability;
   private final String my_message;

   /**
    * Create a new event with the given parameters. An event either makes a
    * link between two nodes, or breaks a link between two nodes.
    * 
    * @param time
    *            The time the event takes place.
    */
   public IMEvent(DateTime time, String email, String availability, String message) {
      super(time);
      my_email = email;
      my_availability = availability;
      my_message = message;
   }
   
   @Override
   public String toString() {
      return super.getTime().toString() + " " 
      + my_email + " " 
      + my_availability + " " 
      + my_message;
   }
   
   @Override
   public String getEventType(){
      return "IM";
   }
   
   public String getEmail(){
      return my_email;
   }
   
   public String getAvailability(){
      return my_availability;
   }
   
   public String getMessage(){
      return my_message;
   }
   
   
   
   /**
    * This method takes a string and returns the MD5 hash of that string. 
    * The purpose of this method is to create a uniquie identifier for a person 
    * by hashing their email address.
    */
   public String getMD5(String data){
      StringBuffer md5 = new StringBuffer();

      MessageDigest digest;
      try {
         digest = java.security.MessageDigest.getInstance("MD5");
         digest.update(data.getBytes());
         byte[] hash = digest.digest();
         for (int i=0;i<hash.length;i++) {
            md5.append(Integer.toHexString(0xFF & hash[i]));
         }

      } catch (NoSuchAlgorithmException e) {
         System.err.println("Error with md5 transformation"+e.getLocalizedMessage());
      }

      return md5.toString();
   }

}