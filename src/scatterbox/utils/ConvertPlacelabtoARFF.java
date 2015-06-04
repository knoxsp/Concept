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

import scatterbox.annotations.CASLAnnotationProcessor;
import scatterbox.annotations.PlacelabAnnotationProcessor;
import scatterbox.classifier.Classification;
import scatterbox.classifier.Classifier;
import scatterbox.event.BluetoothEvent;
import scatterbox.event.CalendarEvent;
import scatterbox.event.IMEvent;
import scatterbox.event.SimpleEvent;
import scatterbox.event.UbisenseEvent;
import scatterbox.simulator.CBRHandler;
import scatterbox.simulator.DatabaseHandler;
import scatterbox.simulator.Simulator;

public class ConvertPlacelabtoARFF extends Simulator{

   List<SimpleEvent> sensorvalues = new LinkedList<SimpleEvent>();

   FileWriter arffWriter;
   File arffFile;
   String outputFile;
   
   List<SimpleEvent> simpleEvents = new LinkedList<SimpleEvent>();

   /**
    * Handler for creating the engine, nodes and getting neighbours
    */
   CBRHandler my_cbrHandler;

   public static void main(String[] args) throws SQLException, JSONException, IOException{
      Simulator sim = new ConvertPlacelabtoARFF();
      sim.simulate();
   }

   public ConvertPlacelabtoARFF(){
      propertiesFile = "placelab.properties";
      getSimulatorDetails(propertiesFile);
      my_Classifier = new Classifier();
      /**
       * Retrieves the annotation for a given time   
       */
      my_annotationProcessor = new PlacelabAnnotationProcessor();

      outputFile  = "arff/"+my_simulationStartTime.toString() + ".arff";
      my_simulationLength = (int) my_simulationEndTime.getMillis() - (int) my_simulationStartTime.getMillis();
      //Initialise the handler with the appropriate training files included
      my_cbrHandler = new CBRHandler();
      my_databaseHandler = new DatabaseHandler();

   }

   public void simulate(DateTime startTime, DateTime endTime){
      my_simulationStartTime = startTime;
      my_simulationEndTime = endTime;
      simulate();
   }

   /**
    * Get all information for the same time period from the database. 
    * Must provide a start time and a duration
    * 
    * Sort all the information by time ascending. 
    * 
    * For every line, create appropriate rdf and enter into construct.
    */
   @Override
   public void simulate(){     
      my_cbrEngine = my_cbrHandler.setUpCbrEngine();
      my_classificationStore = my_cbrHandler.setUpClassificationStore();
      my_simulationLength = (int) my_simulationEndTime.getMillis() - (int) my_simulationStartTime.getMillis();
      my_eventQueue = my_databaseHandler.getSensorDataAsQueue(my_databaseQueries, my_simulationStartTime, my_simulationLength);
      my_Classifier = new Classifier();
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

      constructTimer.scheduleAtFixedRate(new TimerTask(){
         /*
          * This lets the simulator know how long there is between the start time of the simulator 
          * and the time the first event occurs. This is done to avoid doing processing before
          * the first event. When count goes above this variable, processing begins every 10 seconds.
          */
         long whenToStart =  my_eventQueue.get(0).getTime().getMillis() - my_simulationStartTime.getMillis();

         /**
          * Indicates how long since a user was active at their computer
          */
         public long my_timeSinceLastActivityEvent = -1;

         DateTime previousEventTime = my_simulationStartTime;
         
         int featureCount = 0;
         DateTime nextEventTime = null;
         long count = 0;
         long difference = 0;
         /**
          * Indicates the classification which is next in the queue
          */
         Classification currentClassification = null;

         /**
          * Index of the sorted set which corresponds to the current classification type
          */
         int featureIndex;
         /**
          * Index in the store of the location classification set.
          */
         int locationIndex;
         public void run(){
            //Counts every second that has passed since the start
            count = count + 1000;
            if(my_timeSinceLastActivityEvent != -1){
               my_timeSinceLastActivityEvent+=1000;
            }
            //Reduce the confidences of the current
            updateConfidences();               

            //If the next element exists
            if(!my_eventQueue.isEmpty()){
               //get the time of next event
               nextEventTime = my_eventQueue.get(0).getTime();
               featureCount = 0;

               //The difference between the next event time and the start time of the simulation
               //measured in milliseconds
               difference = nextEventTime.getMillis() - my_simulationStartTime.getMillis();
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
               if(count%10000 == 0 && count>whenToStart){
                  DateTime currentTime = new DateTime(my_simulationStartTime.getMillis()+count);
                  featureIndex = getIndexOfFeature("/time");
                  my_classificationStore[featureIndex].add(my_Classifier.classifyTime(currentTime));
                  writeClassificationsToFile(currentTime);
                  System.out.println(currentTime);
               }
            }else{
               my_finishedFlag = true;
               //If there are no more events, stop the timer
               constructTimer.cancel();
            }
         }
         }
      , new Date(), 5);
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

   private void writeClassificationsToFile(DateTime now){
      String currentAnnotation = my_annotationProcessor.getSituation(now).replace(" ", "");
      if(!currentAnnotation.equalsIgnoreCase("unknown")){
         try{
            for(int i=0;i<my_classificationStore.length;i++){
               for(Classification classification:my_classificationStore[i])
                  if(!classification.getType().contains("situation")){
                     if(classification.getConfidence() > 0){
                        for(SimpleEvent se:simpleEvents){
                           if(se.sensor_id.equalsIgnoreCase(classification.getValue())){
                              se.setStatus(1);
                           }
                        }
                     }
                  }
            }
            for(SimpleEvent se:simpleEvents){
               arffWriter.append(se.getStatus()+",");
               se.setStatus(0);
            }
            
            arffWriter.append(currentAnnotation);
            arffWriter.append("\n");
            arffWriter.flush();
         }catch(IOException e){
            System.err.println("Could not add a CASL classification to the arff file."+e.getLocalizedMessage());
         }
      }
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
               if(!a_classification.getType().contains("situation") && !a_classification.getType().contains("location")){
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
}
