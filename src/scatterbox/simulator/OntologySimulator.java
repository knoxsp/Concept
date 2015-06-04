package scatterbox.simulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import scatterbox.annotations.PlacelabAnnotationProcessor;
import scatterbox.event.Event;
import scatterbox.event.PlacelabEvent;
import scatterbox.event.RFIDEvent;
import scatterbox.semantic.CreateClassification;
import scatterbox.semantic.SituationReasoner;
import scatterbox.utils.CounterThread;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

import fionn.cbr.nn.Casenode;

public class OntologySimulator extends Simulator {   


   /**
    * Handler for creating the engine, nodes and getting neighbours
    */
   public static CBRHandler my_cbrHandler;
   
   InfModel ontology = ModelFactory.createOntologyModel();
   SituationReasoner sitReasoner = new SituationReasoner();
   Reasoner pellettReasoner = PelletReasonerFactory.theInstance().create();
   List<Statement> statements = new LinkedList<Statement>();
   public static void main(String[] args) throws SQLException, JSONException,
   IOException {
      Simulator sim = new OntologySimulator();
      sim.simulate();

   }

   public OntologySimulator() {
      propertiesFile = "ontologysimulator.properties";
      //Get start time, end time, K and threshold values from the properties file
      getSimulatorDetails(propertiesFile);
      //Initialise the cbr handler, this retrieves training data from the properties file.
      my_cbrHandler = new CBRHandler();
      my_databaseHandler = new DatabaseHandler();
      /**
       * Retrieves the annotation for a given time   
       */
      my_annotationProcessor = new PlacelabAnnotationProcessor();
      FileInputStream placelabFis;
      FileInputStream classificationsFis;
      try{
         placelabFis = new FileInputStream("placelab.owl");
         classificationsFis = new FileInputStream("../ontonym/SampleOntologies/classifications.owl");
         Model emptyModel = ModelFactory.createDefaultModel();
         ontology = ModelFactory.createInfModel(pellettReasoner, emptyModel);
         ontology.read(placelabFis, "");
         ontology.read(classificationsFis, "");
      }catch(Exception e){
         my_logger.severe("Could not read placelab ontology");
      }
   }

   public OntologySimulator(DateTime startTime, DateTime endTime, int k,
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
      Builtin c = new CreateClassification();
      com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry.theRegistry.register(c);
      
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
                     addCurrentEventToModel();
                     //addCurrentEventToClasificationStore();
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
                * Every ten seconds, reason about the state of the system. 
                * This is where alternative reasoning systems may be accessed.
                */
               if (count % 10000 == 0 && count > whenToStart) {
                  System.out.println("Ontology has: " + ontology.size() + " triples");
                  System.err.println("Applying rule base...");
                  CounterThread counter = new CounterThread();
                  counter.start();
                  reasonWithRules();
                  removeStatements();
                  System.out.println("That took: " + counter.getCount() + " Milliseconds");
                  counter.requestStop();
                  
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

   private void addCurrentEventToModel(){
      Property type = ontology.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
      Property observedBy = ontology.createProperty("http://ontonym.org/0.8/sensor#observedBy");
      Property readingValue = ontology.createProperty("http://ontonym.org/0.8/classifications.owl#readingValue");
      Resource placelabResource = ontology.createResource("http://ontonym.org/0.8/placelab.owl#");
      Resource instantEventResource = ontology.createResource("http://ontonym.org/0.8/event#InstantEvent");


      Event currentEvent = my_eventQueue.get(0);
      String sensorID;
      DateTime readingTime;
      double reading = 0;
      if(currentEvent.getEventType().equalsIgnoreCase("PlacelabEvent")){
         PlacelabEvent placelabEvent = (PlacelabEvent) currentEvent;
         sensorID = placelabEvent.getSensorID();
         readingTime = placelabEvent.getTime();
         reading = (double) placelabEvent.getSensorReading();
      }else{
         RFIDEvent rfidEvent = (RFIDEvent) currentEvent;
         sensorID = rfidEvent.getSensorID();
         readingTime = rfidEvent.getTime();
      }
      Resource sensorResource = ontology.createResource(placelabResource+sensorID);
      Resource instantEventIndividual = ontology.createResource("http://ontonym.org/0.8/placelab.owl#"+sensorID+readingTime.getMillis());

      Statement observation1 = ontology.createStatement(instantEventIndividual, type, instantEventResource);
      Statement observation2 = ontology.createStatement(instantEventIndividual, observedBy, sensorResource);
      Statement observation3 = ontology.createLiteralStatement(instantEventIndividual, readingValue, reading);

      
      ontology.add(observation1);
      statements.add(observation1);
      ontology.add(observation2);
      statements.add(observation2);
      if(currentEvent.getEventType().equalsIgnoreCase("PlacelabEvent")){
         ontology.add(observation3);
         statements.add(observation3);
      }

   }
   
   private void removeStatements(){
      ontology.remove(statements);
      Property type = ontology.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
      RDFNode classif = ontology.createResource("http://ontonym.org/0.8/classifications.owl#classification");
      ResIterator classifications = ontology.listSubjectsWithProperty(type, classif);
      while(classifications.hasNext()){
         ontology.removeAll(classifications.nextResource(), null, null);
      }
   }
  
   /**
    * Using a rule base, find the features of a situation which are most likely to 
    * represent that situation.
    */
   public void reasonWithRules() {

      List rules = Rule.rulesFromURL("file:concept.rules");
      Reasoner ruleReasoner = new GenericRuleReasoner(rules);

      InfModel ruleModel = ModelFactory.createInfModel(ruleReasoner, ontology);
      ruleModel.getDeductionsModel();         

   }
}
