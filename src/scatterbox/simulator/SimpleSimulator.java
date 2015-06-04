package scatterbox.simulator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import scatterbox.annotations.PlacelabAnnotationProcessor;
import scatterbox.classifier.Classification;
import scatterbox.classifier.Classifier;
import scatterbox.event.Event;
import scatterbox.properties.Properties;

public class SimpleSimulator{   

   String my_databaseUserName;
   String my_databasePassword;
   String my_databaseUrl;
   String[] my_databaseQueries;
   
   public List<Classification> my_classificationStore;
   
   /**
    * The logger for this class
    */
   public Logger my_logger;
   /**
    * Adding events to this automatically sorts them
    */
   EventQueue my_eventQueue;

   /**
    * Handler for all database related processing
    */
   public static DatabaseHandler my_databaseHandler;

   /**
    * Format of the sql timestamp. Allows easy conversion to date format
    */
   final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
   /**
    * This is the time in the database that you want to start. 
    */
   DateTime my_simulationStartTime;

   /**
    * This is the time in the database that you want to end. 
    */
   DateTime my_simulationEndTime;
   
   /**
    * How long the simulation should run for
    */
   public int my_simulationLength;
   
   /**
    * How long between each iteration.
    * default = 10 milliseconds 
    * Real-time  = 1000
    */
   public int my_increment = 10;

   /**
    * Date time which identifies the time a case is being created
    */
   DateTime my_caseCreationTime;

   /**
    * Indicates how long since a user was active at their computer
    */
   long my_timeSinceLastActivityEvent = -1;

   /**
    * The classifier used to classify all sensor readings
    */
   Classifier my_Classifier;
   
   /**
    * The names of the training files used to supply training data.
    */
   public String[] my_trainingFiles;
   
   /**
    * Defines whether neighbours should be retrieved, or whether a new case should 
    * be added regardless.
    * Default is false -- true is only used during the learning process.
    */
   boolean learningOnly;
   
   public boolean my_finishedFlag = false;
   
   
   /**
    * Indicates the classification which is next in the queue
    */
   Classification my_currentClassification = null;
   
   /**
    * Index of the sorted set which corresponds to the current classification type
    */
   int my_featureIndex;
   
   int my_original_casebase_size = 0;
   
   public static String propertiesFile = "scatterbox.properties";
   
   public static PlacelabAnnotationProcessor my_annotationProcessor;
   
   public static JSONObject properties;
   
   public SimpleSimulator(){
      try {
         Properties.load(propertiesFile);
      } catch (JSONException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      properties = Properties.getProperties();
      //Get start time, end time, K and threshold values from the properties file
      getSimulatorDetails();

      my_databaseHandler = new DatabaseHandler();
   }
   
   public SimpleSimulator(DateTime startTime, DateTime endTime, String trainingFiles){
      my_simulationStartTime = startTime;
      my_simulationEndTime = endTime;
      my_databaseHandler = new DatabaseHandler();
   }
   
   /**
    * Get all information for the same time period from the database. 
    * Must provide a start time and a duration
    * 
    * Sort all the information by time ascending. 
    * 
    * For every line, create appropriate rdf and enter into construct.
    */
   public void simulate() {
      my_classificationStore = new LinkedList<Classification>();
      
      my_simulationLength = (int) my_simulationEndTime.getMillis()
      - (int) my_simulationStartTime.getMillis();

      my_eventQueue = my_databaseHandler.getSensorDataAsQueue(my_databaseQueries,
            my_simulationStartTime, my_simulationLength);


      setTrainingDataFile();

      final Timer constructTimer = new Timer();

      constructTimer.scheduleAtFixedRate(new TimerTask() {
         /*
          * This lets the simulator know how long there is between the start time of the simulator 
          * and the time the first event occurs. This is done to avoid doing processing before
          * the first event. When count goes above this variable, processing begins every 10 seconds.
          */
         long whenToStart = my_eventQueue.get(0).getTime().getMillis()
         - my_simulationStartTime.getMillis();

         int featureCount = 0;

         DateTime nextEventTime = null;

         DateTime previousEventTime = my_simulationStartTime;

         long count = 0;

         long difference = 0;

         public void run() {
            //Counts every second that has passed since the start
            count += 1000;

            //If the next element exists
            if (my_eventQueue.get(0) != null) {
               //get the time of next event
               nextEventTime = my_eventQueue.get(0).getTime();

               featureCount = 0;

               //The difference between the next event time and the start time of the simulation
               //measured in milliseconds
               difference = nextEventTime.getMillis()
               - my_simulationStartTime.getMillis();
               //When the count reaches that of the difference 
               //This allows to wait for the appropriate number of milliseconds before processing
               //the next event.
               if (count >= difference) {
                  do {
                     Event currentEvent = my_eventQueue.get(0);
                     my_currentClassification = currentEvent.classify();
                     
                     my_classificationStore.add(my_currentClassification);
                     
                     //Add one for each feature being added.
                     featureCount++;
                     my_eventQueue.remove(0);
                     previousEventTime = nextEventTime;
                     //If we have reached the last event, break and classify the current situation.
                     if (!my_eventQueue.isEmpty()) {
                        nextEventTime = my_eventQueue.get(0).getTime();
                     } else {
                        break;
                     }
                  } while (nextEventTime.getMillis() == previousEventTime
                        .getMillis());
               }

               /*
                * Every X seconds, reason about the state of the system. 
                * This is where alternative reasoning systems may be accessed.
                */
               if (count % 1000 == 0 && count > whenToStart) {
                  
                  for(int i=0;i<my_classificationStore.size();i++){
                     System.out.println(my_classificationStore.get(i));
                  }
                  
                  my_classificationStore = new LinkedList<Classification>();
                  
               }
            } else {

               my_finishedFlag = true;
               //If there are no more events, stop the timer
               constructTimer.cancel();
            }
         }
      }, new Date(), my_increment);
   }

   
   public void getSimulatorDetails(){
      
      try {         

         JSONObject databaseProperties = (JSONObject) properties.get("database");
         my_databaseUserName = (String) databaseProperties.get("username");
         my_databasePassword = (String) databaseProperties.get("password");
         my_databaseUrl = (String) databaseProperties.get("url");
         
         JSONObject queries = (JSONObject) properties.get("queries");
         int numQueries = queries.length();
         my_databaseQueries = new String[numQueries];
         for(int i=0;i<numQueries; i++){
            my_databaseQueries[i] = (String) queries.get(String.valueOf(i));
         }
         
         JSONObject simulatorProperties = (JSONObject) properties.get("simulator");
         my_simulationStartTime = dateTimeFormatter.parseDateTime(simulatorProperties.getString("start"));
         //my_simulationLength = Integer.parseInt(simulatorProperties.getString("length"));
         my_simulationEndTime = dateTimeFormatter.parseDateTime(simulatorProperties.getString("end"));
         my_increment = (int) simulatorProperties.getInt("increment");
         
         JSONObject cbml = (JSONObject) properties.get("cbml");
         learningOnly = cbml.getBoolean("learning");
         
         JSONObject using = (JSONObject) properties.get("training_files");
         int numTrainingFiles = using.length();
         my_trainingFiles = new String[numTrainingFiles];
         if(numTrainingFiles != 0){
            for(int i=0; i<numTrainingFiles; i++){
               my_trainingFiles[i] = using.getString(""+i+"");
            }
         }
      } catch (JSONException e) {
         my_logger.severe("JSON error when loading properties" + e.getLocalizedMessage());
      }
   }
   
   /**
    * Set the name of the file containing all new cases created in this simulation
    * Also sets the filewriter.
    * The training file name looks like 
    * YY-MM-DD T:0 K:0 Using:
    */
   public void setTrainingDataFile(){
      String fileName = "training/"
         + my_simulationEndTime.getYear()
         +"-"+my_simulationEndTime.getMonthOfYear()
         +"-"+my_simulationEndTime.getDayOfMonth();
      
         if(my_trainingFiles.length > 0){
            for(String trainingName:my_trainingFiles){
               fileName = fileName + " " + trainingName;
            }
         }
         
   }
   
   public boolean isDone(){
      return my_finishedFlag;
   }
   
   public static void main(String[] args) throws SQLException, JSONException,
   IOException {
      SimpleSimulator sim = new SimpleSimulator();
      //sim.getSimulatorDetails();
      sim.simulate();
   }
   
}
