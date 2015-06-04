package scatterbox.event;

public class SimpleEvent {

   public String sensor_id;
   
   public int status;
   
   public SimpleEvent(String a_sensorID, int a_status){
      sensor_id = a_sensorID;
      status = a_status;
   }
   
   public int getStatus(){
      return status;
   }
   public void setStatus(int a_status){
      status = a_status;
   }
   
   public String getSensorID(){
      return sensor_id;
   }
   public void setSensorID(String a_sensorID){
      sensor_id = a_sensorID;
   }
   
}
