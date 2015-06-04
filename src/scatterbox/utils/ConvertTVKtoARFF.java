package scatterbox.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.json.JSONException;

import scatterbox.annotations.KasterenAnnotationProcessor;
import scatterbox.classifier.Classification;
import scatterbox.classifier.Classifier;
import scatterbox.event.KasterenEvent;
import scatterbox.event.SimpleEvent;
import scatterbox.simulator.CBRHandler;
import scatterbox.simulator.DatabaseHandler;
import scatterbox.simulator.Simulator;

public class ConvertTVKtoARFF extends Simulator {

   List<SimpleEvent> sensorvalues = new LinkedList<SimpleEvent>();

   FileWriter arffWriter;

   File arffFile;

   String outputFile;

   List<SimpleEvent> simpleEvents = new LinkedList<SimpleEvent>();

   /**
    * Handler for creating the engine, nodes and getting neighbours
    */
   public static CBRHandler my_cbrHandler;

   List<KasterenEvent> my_currentEvents = new LinkedList<KasterenEvent>();

   public static void main(String[] args) throws SQLException, JSONException,
   IOException {
      Simulator sim = new ConvertTVKtoARFF();
      //sim.getSimulatorDetails();
      sim.simulate();
   }

   public ConvertTVKtoARFF() {
      propertiesFile = "kasteren.properties";
      getSimulatorDetails(propertiesFile);
      outputFile = "arff/" + my_simulationStartTime.toString() + ".arff";
      //Get start time, end time, K and threshold values from the properties file
      //getSimulatorDetails("scatterbox.properties");
      //Initialise the cbr handler, this retrieves training data from the properties file.
      my_cbrHandler = new CBRHandler();
      my_Classifier = new Classifier();
      /**
       * Retrieves the annotation for a given time   
       */
      my_annotationProcessor = new KasterenAnnotationProcessor();

   }

   public ConvertTVKtoARFF(DateTime startTime, DateTime endTime, int k,
         double threshold, String trainingFiles) {
      my_simulationStartTime = startTime;
      my_simulationEndTime = endTime;
      my_KValue = k;
      my_Threshold = threshold;
      //Initialise the handler with the appropriate training files included
      my_cbrHandler = new CBRHandler(trainingFiles.split(","));
      my_databaseHandler = new DatabaseHandler();
   }

   @Override
   public void simulate() {
      my_cbrEngine = my_cbrHandler.setUpCbrEngine();
      my_original_casebase_size = my_cbrEngine.getCasebaseSize();
      my_classificationStore = my_cbrHandler.setUpClassificationStore();
      my_simulationLength = (int) my_simulationEndTime.getMillis()
      - (int) my_simulationStartTime.getMillis();

      my_eventQueue = my_databaseHandler.getSensorDataAsQueue(my_databaseQueries,
            my_simulationStartTime, my_simulationLength);

      setTrainingDataFile();
      

      /**
       * Create the file and add the necessary information
       */
      try{
         arffFile = new File(outputFile);
         arffWriter = new FileWriter(arffFile);
         arffWriter.append("@relation situation\n");
         getClassifications();
         arffWriter.append("@data\n");
         arffWriter.flush();
      } catch (Exception e) {
         e.printStackTrace();
      }

      final Timer constructTimer = new Timer();

      constructTimer.scheduleAtFixedRate(new TimerTask() {
         /*
          * This lets the simulator know how long there is between the start time of the simulator 
          * and the time the first event occurs. This is done to avoid doing processing before
          * the first event. When count goes above this variable, processing begins every 10 seconds.
          */
         long whenToStart = my_eventQueue.get(0).getTime().getMillis()
         - my_simulationStartTime.getMillis();

         /**
          * Index of the sorted set which corresponds to the current classification type
          */
         int featureIndex;

         DateTime nextEventTime = null;

         DateTime previousEventTime = my_simulationStartTime;

         long count = 0;

         long difference = 0;

         public void run() {
            //Counts every second that has passed since the start
            count += 1000;
            updateConfidences();
            //If the next element exists
            if (!my_eventQueue.isEmpty()) {
               //get the time of next event
               nextEventTime = my_eventQueue.get(0).getTime();

               //The difference between the next event time and the start time of the simulation
               //measured in milliseconds
               difference = nextEventTime.getMillis()
               - my_simulationStartTime.getMillis();
               //When the count reaches that of the difference 
               //This allows to wait for the appropriate number of milliseconds before processing
               //the next event.
               if (count >= difference) {
                  do {
                     KasterenEvent ke = (KasterenEvent) my_eventQueue.get(0);
                     my_currentEvents.add(ke);
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
                * Every ten seconds, reason about the state of the system. 
                * This is where alternative reasoning systems may be accessed.
                */
               if (count % 10000 == 0 && count > whenToStart) {
                  DateTime currentTime = new DateTime(my_simulationStartTime
                        .getMillis()
                        + count);
                  updateCurrentEvents(currentTime);

                  featureIndex = getIndexOfFeature("/time");
                  my_classificationStore[featureIndex].add(my_Classifier
                        .classifyTime(currentTime));

                  writeClassificationsToFile(currentTime);

               }
            } else {
               my_finishedFlag = true;
               //If there are no more events, stop the timer
               constructTimer.cancel();
            }
         }
      }, new Date(), my_increment);
   }

   private void writeClassificationsToFile(DateTime now) {
      String currentAnnotation = my_annotationProcessor.getSituation(now);
      if (!currentAnnotation.equalsIgnoreCase("unknown")) {
         try {
            for (int i = 0; i < my_classificationStore.length; i++) {
               for (Classification classification : my_classificationStore[i])
                  if (!classification.getType().contains("situation")) {
                     if (classification.getConfidence() > 0) {
                        for (SimpleEvent se : simpleEvents) {
                           if (se.sensor_id.contains(classification
                                 .getValue())) {
                              se.setStatus(1);
                           }
                        }
                     }
                  }
            }
            for (SimpleEvent se : simpleEvents) {
               arffWriter.append(se.getStatus() + ",");
               se.setStatus(0);
            }

            arffWriter.append(currentAnnotation);
            arffWriter.append("\n");
            arffWriter.flush();
         } catch (IOException e) {
            System.err
            .println("Could not add a CASL classification to the arff file."
                  + e.getLocalizedMessage());
         }
      }
   }

   public void updateCurrentEvents(DateTime now) {
      List<KasterenEvent> expiredEvents = new LinkedList<KasterenEvent>();
      for (KasterenEvent ke : my_currentEvents) {
         //If the current event has expired (the current time is greater than the end time of the event)
         //OR if an event expired at some point in the last 10 seconds.
         if (ke.isActive(now)
               || (now.getMillis() - ke.getEndTime().getMillis()) < 10000) {
            my_currentClassification = ke.classify();
            String currentFeatureType = ke.getFeature();
            int index = getIndexOfFeature(currentFeatureType);
            my_classificationStore[index].add(my_currentClassification);
         } else {
            expiredEvents.add(ke);
         }
      }
      my_currentEvents.removeAll(expiredEvents);
   }
   /**
    * Get all possible classifications, so they may be be put into the arff
    * file. The easiest way to do this is to iterate through the classification
    * store.
    */
   public void getClassifications(){
      try {
         List<String> classifications = new LinkedList<String>();
         for(int i=0;i<my_classificationStore.length;i++){
            for(Classification a_classification : my_classificationStore[i]){
               if(!a_classification.getType().contains("situation")){
                  classifications.add(a_classification.getValue());
                  String uniqueID = a_classification.getValue();
                  simpleEvents.add(new SimpleEvent(uniqueID, 0));
                  arffWriter.append("@attribute "+uniqueID+" real\n");
               }
            }
         }
         //Now get all unique situations
         List<String> allSitations = getAllSituations();
         String attributeList = "@attribute situation {";
         arffWriter.append(attributeList);
         for(String s:allSitations){
            arffWriter.append(s.replace(" ", "")+", ");
         }
         arffWriter.append("}\n");
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   public List<String> getAllSituations() {
      List<String> allSituations = new LinkedList<String>();
      //Number of seconds
      for(int count=0;count < my_simulationLength;count+=10000){
         String situation = "";

         DateTime Timestamp = new DateTime(my_simulationStartTime.getMillis()+count);

         situation = my_annotationProcessor.getSituation(Timestamp);

         boolean seenItBefore = false;
         //If it is a new situation, mark it as such
         for(int i=0;i<allSituations.size();i++){
            if(allSituations.get(i).equalsIgnoreCase(situation)){
               seenItBefore = true;
            }
         }
         //Add the current situation to the list if it has not been seen before
         if(seenItBefore == false){
            allSituations.add(situation);
         }
      }
      //return the string minus the trailing AND
      return allSituations;
   }
}