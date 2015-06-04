package scatterbox.simulator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import scatterbox.annotations.AnnotationProcessor;
import scatterbox.classifier.Classification;
import scatterbox.classifier.ClassificationSortedSet;
import scatterbox.classifier.Classifier;
import scatterbox.event.Event;
import scatterbox.properties.Properties;
import cbml.cbr.feature.StringFeature;
import fionn.cbr.nn.Casenode;
import fionn.cbr.nn.NN;

public class Simulator{

   protected String my_databaseUserName;
   protected String my_databasePassword;
   protected String my_databaseUrl;
   protected String[] my_databaseQueries;

   /**
    * A store for the classifications. 
    * This is an array of sorted sets, the highest value of each element
    * will get added to the testcase.
    */
   public ClassificationSortedSet[] my_classificationStore;
   /**
    * The logger for this class
    */
   public Logger my_logger;
   /**
    * Adding events to this automatically sorts them
    */
   protected EventQueue my_eventQueue;

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
   protected DateTime my_simulationStartTime;

   /**
    * This is the time in the database that you want to end. 
    */
   protected DateTime my_simulationEndTime;

   /**
    * How long the simulation should run for
    */
   public int my_simulationLength;

   /**
    * How long between each iteration.
    * default = 10 milliseconds 
    * Real-time  = 1000
    */
   public int my_increment = 1;

   /**
    * If a case does not change, then do not ask for neighbours all the time.
    */
   Casenode my_tempCase;

   /**
    * Date time which identifies the time a case is being created
    */
   public DateTime my_caseCreationTime;

   /**
    * The cbr engine
    */
   public static NN my_cbrEngine;

   /**
    * The classifier used to classify all sensor readings
    */
   protected Classifier my_Classifier;

   /**
    * The measure of how close the activation values of two cases may be to be considered equal.
    * 0 means the cases must have identical activation values
    */
   public double my_Threshold = 0;

   /**
    * The number of cases that the getNearestNeighbours method should return
    */
   public int my_KValue = 0;

   /**
    * The names of the training files used to supply training data.
    */
   public String[] my_trainingFiles;

   /**
    * File and file writer for storing all cases added to the casebase
    * in this simulation. This file will be used as training data in future simulations
    */
   public File my_newCases;
   /**
    * This is a static filewriter as it is updated in the CBR handler
    */
   public static FileWriter  my_fileWriter;

   /**
    * Defines whether neighbours should be retrieved, or whether a new case should 
    * be added regardless.
    * Default is false -- true is only used during the learning process.
    */
   public boolean learningOnly;

   public boolean my_finishedFlag = false;


   /**
    * Indicates the classification which is next in the queue
    */
   public Classification my_currentClassification = null;

   /**
    * Index of the sorted set which corresponds to the current classification type
    */
   public int my_featureIndex;

   protected int my_original_casebase_size = 0;

   public static String propertiesFile;

   public static AnnotationProcessor my_annotationProcessor;

   public static JSONObject properties;

   public Simulator(){
      
   }

   public Simulator(DateTime startTime, DateTime endTime, int k, double threshold, String trainingFiles){
      my_simulationStartTime = startTime;
      my_simulationEndTime = endTime;
      my_KValue = k;
      my_Threshold = threshold;
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
      getSimulatorDetails("boo.properties");

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

            //Reduce the confidences of the current
            updateConfidences();

            //If the next element exists
            if (!my_eventQueue.isEmpty()) {
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
                     addCurrentEventToClasificationStore();
                     //Add one for each feature being added.
                     featureCount++;
                     my_eventQueue.remove(0);
                     previousEventTime = nextEventTime;
                     //If we have reached the last event, break and classify the current situation.
                     if (my_eventQueue.get(0) != null) {
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

                  for(int i=0;i<my_classificationStore.length;i++){
                     System.out.println(my_classificationStore[i].get(0));
                  }

               }
            } else {

               my_finishedFlag = true;
               //If there are no more events, stop the timer
               constructTimer.cancel();
            }
         }
      }, new Date(), my_increment);
   }
   /**
    * Reduces the confidence of each classification in the array of sorted sets.
    */
   public void updateConfidences(){
      for(int i =0;i<my_classificationStore.length;i++){
         my_classificationStore[i].update();
      }
   }
   /**
    * Returns the index of the particular feature (location, activity etc) in question
    * returns -1 if none are found
    * @param a_Feature
    * @return index of feature or -1 if none are found
    */
   public int getIndexOfFeature(String a_Feature){
      for(int i=0;i<my_classificationStore.length;i++){
         String feature = my_classificationStore[i].get(0).getType(); 
         if(feature.equalsIgnoreCase(a_Feature)){
            return i;
         }
      }
      return -1;
   }

   public void getSimulatorDetails(String fileName){
      propertiesFile = "properties/"+fileName;
      try {         
         Properties.load(propertiesFile);

         properties = Properties.getProperties();

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
         my_Threshold = simulatorProperties.getDouble("threshold");
         my_KValue = simulatorProperties.getInt("k");
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
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }


   public void addCurrentEventToClasificationStore() {
      /*
       * Check which activity has occurred and classify it. 
       */
      Event currentEvent = my_eventQueue.get(0);

      my_currentClassification = currentEvent.classify();
      String currentFeatureType = currentEvent.getFeature();
      my_featureIndex = getIndexOfFeature(currentFeatureType);
      //Some sensors occur in the database but have no documentation, so they are ignored.
      if (my_featureIndex != -1) {
         //super.my_logger.info("No documentation for this sensor, so it is ignored.");
         my_classificationStore[my_featureIndex].add(my_currentClassification);
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
         +"-"+my_simulationEndTime.getDayOfMonth()
         +" T:"+my_Threshold
         +" K:"+my_KValue;

      if(my_trainingFiles.length > 0){
         for(String trainingName:my_trainingFiles){
            fileName = fileName + " " + trainingName;
         }
      }

      my_newCases = new File(fileName);
      try {
         my_fileWriter = new FileWriter(my_newCases);
      } catch (IOException e) {
         my_logger.severe("File for new cases has caused a problem." + e.getMessage());
      }
   }

   public boolean isDone(){
      return my_finishedFlag;
   }

   public void learnCase(Casenode a_case){
      String annotatedSituation =my_annotationProcessor.getSituation(a_case.getCreationTime());
      if(!annotatedSituation.equalsIgnoreCase("unknown")){
         StringFeature solnFeature = new StringFeature("/situation",
               annotatedSituation);
         a_case.setSolution(solnFeature);
         //System.out.println(a_case);
         try {
            Simulator.my_fileWriter.append(a_case.toString()
                  + System.getProperty("line.separator"));
         } catch (IOException e) {
            my_logger.severe("Cannot add case to training file"
                  + e.getLocalizedMessage());
         }
      }
   }

   public static void main(String[] args) throws SQLException, JSONException,
   IOException {
      Simulator sim = new Simulator();
      sim.simulate();
   }
}
