package scatterbox.event;

import org.joda.time.DateTime;

import scatterbox.classifier.Classification;

public class KasterenEvent extends Event{
   
   String my_eventName;
   DateTime my_startTime;
   DateTime my_endTime;
   
   public KasterenEvent(DateTime a_startTime, DateTime an_endTime, String id){
      super(a_startTime);
      my_startTime = a_startTime;
      my_endTime = an_endTime;
      my_eventName = id;
   }
   
   public boolean isActive(DateTime now){
      if((now.getMillis() <= my_endTime.getMillis()) && (now.getMillis() >= my_startTime.getMillis())){
         return true;
      }else{
         return false;
      }
   }
   
   public DateTime getStartTime(){
      return my_startTime;
   }
   
   public DateTime getEndTime(){
      return my_endTime;
   }
   
   public String getEventName(){
      return my_eventName;
   }
   
   @Override
   public String getFeature(){
      return "/"+my_eventName;
   }
   
   @Override
   public String toString(){
      return my_eventName + " FROM " + my_startTime +" TO "+ my_endTime;
   }
   
   @Override
   public Classification classify(){
      Classification c = new Classification("/"+my_eventName, my_eventName, 100, 10);
      return c;
   }
   
}
