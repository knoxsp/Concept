//package scatterbox.simulator;
//
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.Date;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.logging.Logger;
//
//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.sensestar.service.ObjectEventHandler;
//import org.sensestar.util.Properties;
//
//import scatterbox.classifier.Classification;
//import scatterbox.classifier.ClassificationSortedSet;
//import scatterbox.classifier.Classifier;
//import scatterbox.event.ActivityEvent;
//import scatterbox.event.BluetoothEvent;
//import scatterbox.event.CalendarEvent;
//import scatterbox.event.Event;
//import scatterbox.event.IMEvent;
//import scatterbox.event.UbisenseEvent;
//import fionn.cbr.nn.Casenode;
//import fionn.cbr.nn.NN;
//
//public class SimulatorUsingSenseStar extends ObjectEventHandler{
//
//   /**
//    * A store for the classifications. 
//    * This is an array of sorted sets, the highest value of each element
//    * will get added to the testcase.
//    */
//   private ClassificationSortedSet[] my_classificationStore;
//   /**
//    * The logger for this class
//    */
//   private Logger my_logger;
//   /**
//    * Adding events to this automatically sorts them
//    */
//   EventQueue my_eventQueue;
//
//   /**
//    * Handler for creating the engine, nodes and getting neighbours
//    */
//   CBRHandler my_cbrHandler = new CBRHandler();
//   
//   /**
//    * Handler for all database related processing
//    */
//   public final static DatabaseHandler my_databaseHandler = new DatabaseHandler();
//
//   /**
//    * Format of the sql timestamp. Allows easy conversion to date format
//    */
//   final DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
//   /**
//    * This is the time in the database that you want to start. 
//    */
//   DateTime my_simulationStartTime;
//
//   /**
//    * How long the simulation should run for
//    */
//   private int my_simulationLength;
//   
//   /**
//    * If a case does not change, then do not ask for neighbours all the time.
//    */
//   Casenode my_tempCase;
//
//   /**
//    * Date time which identifies the time a case is being created
//    */
//   DateTime caseCreationTime;
//
//   NN my_cbrEngine;
//
//   /**
//    * Indicates how long since a user was active at their computer
//    */
//   long timeSinceLastActivityEvent = 0;
//
//   /**
//    * The classifier used to classify all sensor readings
//    */
//   Classifier my_Classifier = new Classifier();
//   
//
//   /**
//    * Index of the sorted set which corresponds to the current classification type
//    */
//   int featureIndex;
//   /**
//    * Index in the store of the location classification set.
//    */
//   int locationIndex;
//
//   /**
//    * Indicates the classification which is next in the queue
//    */
//   Classification currentClassification = null;
//   
//   /**
//    * How long between each iteration. 
//    * Real-time  = 1000
//    */
//   private int my_increment;
//   /**
//    * The measure of how close the activation values of two cases may be to be considered equal.
//    * 0 means the cases must have identical activation values
//    */
//   private double my_Threshold = 0;
//   
//   /**
//    * The number of cases that the getNearestNeighbours method should return
//    */
//   private int my_KValue = 0;
//   
//   public static void main(String[] args) throws SQLException, JSONException, IOException{
//
//      //Properties.load("simulator.properties");
//      //new SenseStar();
//      Simulator sim = new Simulator();
//      sim.getSimulatorDetails();
//      sim.simulate();
//   }
//
//   //   @Override
//      public void handleEvent(Object an_object) {
//         try{
//         System.out.println("Handling event in simulator" + an_object);
//         if(an_object instanceof ActivityEvent){
//            ActivityEvent activityEvent = (ActivityEvent)an_object;
//            //Classify the next event
//            currentClassification = my_Classifier.classifyActivity(timeSinceLastActivityEvent);
//            //Find the index of the set of activity features in the array of possible features
//            featureIndex = getIndexOfFeature("/activity");
//            locationIndex = getIndexOfFeature("/location");
//            timeSinceLastActivityEvent = 0;
//         }else if(an_object instanceof BluetoothEvent){
//            BluetoothEvent bluetoothEvent = (BluetoothEvent)an_object;    
//            currentClassification = my_Classifier.classifyBluetooth(bluetoothEvent);
//            //Bluetooth classification returns a location type
//            featureIndex = getIndexOfFeature("/location");  
//         }else if(an_object instanceof IMEvent){
//            IMEvent imEvent = (IMEvent)an_object;         
//            currentClassification = my_Classifier.classifyIM(imEvent);
//            featureIndex = getIndexOfFeature("/IM");   
//         }else if(an_object instanceof UbisenseEvent){
//            UbisenseEvent ubisenseEvent = (UbisenseEvent)an_object;
//            currentClassification = my_Classifier.classifyUbisense(ubisenseEvent);
//            featureIndex = getIndexOfFeature("/location"); 
//         }else if(an_object instanceof CalendarEvent){
//            CalendarEvent calendarEvent = (CalendarEvent)an_object;
//            currentClassification = my_Classifier.classifyCalendar(calendarEvent);
//            featureIndex = getIndexOfFeature("/calendar");
//         }
//         }catch(Exception e){
//            e.printStackTrace();
//         }
//      }
//   
//   /**
//    * Get all information for the same time period from the database. 
//    * Must provide a start time and a duration
//    * 
//    * Sort all the information by time ascending. 
//    * 
//    * For every line, create appropriate rdf and enter into construct.
//    */
//   public void simulate(){     
//      my_cbrEngine = my_cbrHandler.setUpCbrEngine();
//      my_classificationStore = my_cbrHandler.setUpClassificationStore();
//      my_eventQueue = my_databaseHandler.getSensorDataAsQueue(my_simulationStartTime, my_simulationLength);
//
//      final Timer constructTimer = new Timer();
//
//      /**
//       * The timer. 
//       */
//      constructTimer.scheduleAtFixedRate(new TimerTask(){
//         int numberOfCorrectMatches = 0;
//         int numberOfComparisonsMade = 0;
//         int numberOfPartialMatches = 0;
//         double partialMatchAccumulator = 0;
//         double situationMatch;
//         int featureCount = 0;
//         DateTime nextEventTime = null;
//         long count = 0;
//         long difference = 0;
//
//         public void run(){
//            //Counts every second that has passed since the start
//            count = count + 1000;
//            timeSinceLastActivityEvent+=1000;
//            //Reduce the confidences of the current
//            updateConfidences();               
//
//            //If the next element exists
//            if(my_eventQueue.peek() != null){
//               //get the time of next event
//               nextEventTime = my_eventQueue.peek().getTime();
//               featureCount = 0;
//
//               //The difference between the next event time and the start time of the simulation
//               //measured in milliseconds
//               difference = nextEventTime.getMillis() - my_simulationStartTime.getMillis();
//               //When the count reaches that of the difference 
//               //This allows to wait for the appropriate number of milliseconds before processing
//               //the next event.
//               if(count >= difference){
//                  String sensorType = my_eventQueue.peek().getEventType();
//                  Event event = null;
//
//                  /*
//                   * Check which activity has occurred and classify it. 
//                   */
//                  if(sensorType.equalsIgnoreCase("activity")){
//                     //Classify the next event
//                     currentClassification = my_Classifier.classifyActivity(timeSinceLastActivityEvent);
//                     //Find the index of the set of activity features in the array of possible features
//                     featureIndex = getIndexOfFeature("/activity");
//                     locationIndex = getIndexOfFeature("/location");
//                     timeSinceLastActivityEvent = 0;
//                  }else if(sensorType.equalsIgnoreCase("bluetooth")){
//                     BluetoothEvent bluetoothEvent = (BluetoothEvent) my_eventQueue.peek();
//                     currentClassification = my_Classifier.classifyBluetooth(bluetoothEvent);
//                     //Bluetooth classification returns a location type
//                     featureIndex = getIndexOfFeature("/location");           
//                  }else if(sensorType.equalsIgnoreCase("IM")){
//                     IMEvent imEvent = (IMEvent) my_eventQueue.peek();
//                     currentClassification = my_Classifier.classifyIM(imEvent);
//                     featureIndex = getIndexOfFeature("/IM");             
//                  }else if(sensorType.equalsIgnoreCase("ubisense")){
//                     UbisenseEvent ubisenseEvent = (UbisenseEvent) my_eventQueue.peek();
//                     currentClassification = my_Classifier.classifyUbisense(ubisenseEvent);
//                     featureIndex = getIndexOfFeature("/location"); 
//                  }else if(sensorType.equalsIgnoreCase("calendar")){
//                     CalendarEvent calendarEvent = (CalendarEvent) my_eventQueue.peek();
//                     currentClassification = my_Classifier.classifyCalendar(calendarEvent);
//                     featureIndex = getIndexOfFeature("/calendar");
//                  }
//
//                  my_classificationStore[featureIndex].add(currentClassification);
//                  //Add one for each feature being added.
//                  featureCount++;
//                  my_eventQueue.poll();
//               }
//
//               /*
//                * Evety ten seconds, reason about the state of the system. 
//                * This is where alternative reasoning systems may be accessed.
//                */
//               if(count%10000 == 0){
//                  /*
//                   * Classify Location
//                   */
//                  my_Classifier.classifyLocation(my_classificationStore[locationIndex]);;
//
//                  /*
//                   * Classify Activity
//                   */
//                  currentClassification = my_Classifier.classifyActivity(timeSinceLastActivityEvent);
//                  //Find the index of the set of activity features in the array of possible features
//                  featureIndex = getIndexOfFeature("/activity");
//                  //Add the classification to the activity list. 
//                  my_classificationStore[featureIndex].add(currentClassification);
//                  //Create test case and add the features
//
//                  /*
//                   * Create the case which represents the current state of the system
//                   */
//                  caseCreationTime = new DateTime(my_simulationStartTime.getMillis()+count);
//                  Casenode testCase = my_cbrHandler.createCase(my_classificationStore, my_Classifier, caseCreationTime);
//
//                  /*
//                   * This checks to see if the current case is the same as the previous one. 
//                   * If it is, don't get the nearest neighbours, as they are already retrieved.
//                   */
//                  if(my_tempCase == null || !testCase.equals(my_tempCase)){
//                     //If the current case is not equal to the previous one, replace the
//                     //temp case and get the nearest neighbours.
//                     my_tempCase = testCase;
//                     situationMatch = my_cbrHandler.getNearestNeighbours(testCase, my_KValue, my_Threshold);
//                     numberOfComparisonsMade++;
//                     /*
//                      * If the two cases are identical
//                      */
//                     if(situationMatch == 1){
//                        numberOfCorrectMatches++;
//                        
//                     }else if (situationMatch > 0){
//                        numberOfPartialMatches++;
//                        partialMatchAccumulator+= situationMatch;
//                     }
//                  }
//               }
//            }else{
//               System.err.println(numberOfCorrectMatches+" correctly identified out of "+numberOfComparisonsMade+" attempts");
//               //If there are no more events, stop the timer
//               constructTimer.cancel();
//            }
//         }
//      }
//      , new Date(), 50);
//   }
//   /**
//    * Reduces the confidence of each classification in the array of sorted sets.
//    */
//   private void updateConfidences(){
//      for(int i =0;i<my_classificationStore.length;i++){
//         my_classificationStore[i].update();
//      }
//   }
//   /**
//    * Returns the index of the particular feature (location, activity etc) in question
//    * returns -1 if none are found
//    * @param a_Feature
//    * @return index of feature or -1 if none are found
//    */
//   private int getIndexOfFeature(String a_Feature){
//      for(int i=0;i<my_classificationStore.length;i++){
//         String feature = my_classificationStore[i].get(0).getType(); 
//         if(feature.equalsIgnoreCase(a_Feature)){
//            return i;
//         }
//      }
//      return -1;
//   }
//   
//   public void getSimulatorDetails(){
//      try {
//         Properties.load("scatterbox.properties");   
//         JSONObject properties = Properties.getProperties();
//         JSONObject simulatorProperties = (JSONObject) properties.get("simulator");
//         System.out.println(simulatorProperties.get("start"));
//         my_simulationStartTime = f.parseDateTime(simulatorProperties.getString("start"));
//         my_simulationLength = Integer.parseInt(simulatorProperties.getString("length"));
//         my_Threshold = simulatorProperties.getDouble("threshold");
//         my_KValue = simulatorProperties.getInt("k");
//         my_increment = (int) simulatorProperties.getInt("increment");
//      } catch (JSONException e) {
//         my_logger.severe("JSON error when loading properties" + e.getLocalizedMessage());
//      } catch (IOException e) {
//         my_logger.severe("IO error when loading properties" + e.getLocalizedMessage());
//      }
//   }
//
//
//
//}
