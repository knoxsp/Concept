package scatterbox.simulator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import scatterbox.annotations.AnnotatedResult;
import scatterbox.annotations.CASLAnnotationProcessor;
import scatterbox.annotations.PlacelabAnnotationProcessor;
import scatterbox.classifier.Classifier;
import scatterbox.properties.Properties;
import scatterbox.utils.ResultsToXLS;
import fionn.cbr.nn.Casenode;

public class PlacelabSimulator extends Simulator {   

   /**
    * Handler for creating the engine, nodes and getting neighbours
    */
   public static CBRHandler my_cbrHandler;
   
   public static void main(String[] args) throws SQLException, JSONException,
   IOException {
      Simulator sim = new PlacelabSimulator();
      //sim.getSimulatorDetails();
      sim.simulate();
   }

   public PlacelabSimulator() {
      propertiesFile = "placelab.properties";
      getSimulatorDetails(propertiesFile);
      //Get start time, end time, K and threshold values from the properties file
      //getSimulatorDetails("scatterbox.properties");
      //Initialise the cbr handler, this retrieves training data from the properties file.
      my_databaseHandler = new DatabaseHandler(my_databaseUserName, my_databasePassword, my_databaseUrl);
      my_cbrHandler = new CBRHandler();
      my_Classifier = new Classifier();
      /**
       * Retrieves the annotation for a given time   
       */
      my_annotationProcessor = new PlacelabAnnotationProcessor();

   }

   public PlacelabSimulator(DateTime startTime, DateTime endTime, int k,
         double threshold, String trainingFiles) {
      propertiesFile = "placelab.properties";
      getSimulatorDetails(propertiesFile);
      my_simulationStartTime = startTime;
      my_simulationEndTime = endTime;
      my_KValue = k;
      my_Threshold = threshold;
      my_trainingFiles = trainingFiles.split(",");
      my_databaseHandler = new DatabaseHandler(my_databaseUserName, my_databasePassword, my_databaseUrl);
      //Initialise the handler with the appropriate training files included
      my_cbrHandler = new CBRHandler(my_trainingFiles);
      my_Classifier = new Classifier();
      /**
       * Retrieves the annotation for a given time   
       */
      my_annotationProcessor = new PlacelabAnnotationProcessor();
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

      final Timer constructTimer = new Timer();

      constructTimer.scheduleAtFixedRate(new TimerTask() {
         /*
          * This lets the simulator know how long there is between the start time of the simulator 
          * and the time the first event occurs. This is done to avoid doing processing before
          * the first event. When count goes above this variable, processing begins every 10 seconds.
          */
         long whenToStart = my_eventQueue.get(0).getTime().getMillis()
         - my_simulationStartTime.getMillis();

         int numberOfCorrectMatches = 0;

         int numberOfIncorrectMatches = 0;

         int numberOfUnAnnotatedOccurrances = 0;

         int numberOfPartialMatches = 0;

         double partialMatchAccumulator = 0;

         double situationMatch;

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
                  /*
                   * Create the case which represents the current state of the system
                   */
                  my_caseCreationTime = new DateTime(my_simulationStartTime
                        .getMillis()
                        + count);
                  Casenode testCase = my_cbrHandler.createCase(
                        my_classificationStore, my_Classifier,
                        my_caseCreationTime);

                  /*
                   * This checks to see if the current case is the same as the previous one. 
                   * If it is, don't get the nearest neighbours, as they are already retrieved.
                   */
                  if (my_tempCase == null || !testCase.equals(my_tempCase)) {
                     //If the current case is not equal to the previous one, replace the
                     //temp case and get the nearest neighbours.
                     my_tempCase = testCase;
                     /**
                      * If the simulator is in "learning phase" new cases are not reasoned about.
                      * they are just added directly to the casebase.
                      */
                     if(learningOnly){
                        learnCase(my_tempCase);

                     }else{

                        /**
                         * Get the nearest neighbour to the test case. Return -1 if a match is found, 
                         * -2 if a new case is added
                         * Otherwise return the similarity of the two situations, given a partial match if a partial match is found.  
                         */
                        situationMatch = my_cbrHandler.getNearestNeighbours(
                              testCase, my_KValue, my_Threshold);
                        /*
                         * If a match is found, -1 is returned
                         */
                        if(situationMatch == 0){
                           numberOfIncorrectMatches++;
                        }else if (situationMatch == -1) {
                           numberOfCorrectMatches++;
                        } else if (situationMatch > 0) {
                           numberOfPartialMatches++;
                           partialMatchAccumulator += situationMatch;
                        } else if (situationMatch == -3) {
                           numberOfUnAnnotatedOccurrances++;
                        }
                     }
                  }
               }
            } else {

               System.err.println(numberOfCorrectMatches
                     + " correctly identified out of "
                     + my_cbrHandler.numberOfComparisonsMade + " attempts");

               System.err.println(numberOfIncorrectMatches
                     + " incorrectly identified out of "
                     + my_cbrHandler.numberOfComparisonsMade + " attempts");

               System.err.println(numberOfPartialMatches
                     + " partially identified with an average similarity of "
                     + (partialMatchAccumulator / numberOfPartialMatches));

               System.err.println(numberOfUnAnnotatedOccurrances
                     + " situations occurred where no annotations were found");

               int numberOfNewCases = (my_cbrEngine.getCasebaseSize() - my_original_casebase_size);

               System.err.println("Number of cases added to the casebase = "
                     + numberOfNewCases);
               
               

               int[][] confusionMatrix = my_annotationProcessor.generateConfusionMatrix();
               int sizeOfMatrix = my_annotationProcessor.annotations.size();
               
               for(int i=0;i<sizeOfMatrix;i++){
                  for(int j=0;j<sizeOfMatrix;j++){
                     System.out.print(confusionMatrix[i][j] + " ");
                  }
                  System.out.println(my_annotationProcessor.annotations.get(i).getAnnotation());
               }
               ResultsToXLS resultWriter = new ResultsToXLS();
               for(int i=0;i<sizeOfMatrix;i++){
                  //ResultsToXLS resultWriter = new ResultsToXLS();
                  AnnotatedResult ar = my_annotationProcessor.annotations.get(i); 
//                  System.out.println(ar.getAnnotation() + 
//                        ": Precision: " + ar.getPrecision() + 
//                        " Recall" + ar.getRecall() + 
//                        " FMeasure" + ar.getFMeasure());
                  resultWriter.addResult(my_simulationStartTime,
                        my_simulationEndTime, my_trainingFiles, ar.getAnnotation(), my_KValue,
                        my_Threshold, ar.getNumOccurrances(),
                        ar.getRecall(), ar.getPrecision(),
                        ar.getFMeasure(), numberOfNewCases);
               }
               
               //Write the newly added cases to their file
               try {
                  resultWriter.closeXLS();
                  my_fileWriter.flush();
                  my_fileWriter.close();
               } catch (IOException e) {
                  my_logger.severe("Unable to flush or close my_fileWriter."
                        + e.getMessage());
               }

               my_finishedFlag = true;
               //If there are no more events, stop the timer
               constructTimer.cancel();
            }
         }
      }, new Date(), my_increment);
   }

}
