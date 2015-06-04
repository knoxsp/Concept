package scatterbox.simulator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import scatterbox.annotations.AnnotatedResult;
import scatterbox.classifier.Classification;
import scatterbox.classifier.ClassificationSortedSet;
import scatterbox.classifier.Classifier;
import cbml.CBMLException;
import cbml.CBMLReader;
import cbml.CaseReader;
import cbml.cbr.CaseStruct;
import cbml.cbr.Feature;
import cbml.cbr.FeatureStruct;
import cbml.cbr.IncompatableFeatureException;
import cbml.cbr.SimilarityProfile;
import cbml.cbr.feature.StringFeature;
import fionn.cbr.nn.Casenode;
import fionn.cbr.nn.NN;

public class CBRHandler {

   Class caseClass;

   int numberOfComparisonsMade = 0;

   /**
    * String buffer where the enarest neighbours are stored before printing.
    */
   StringBuffer message;

   /**
    * Logger for the class
    */
   private Logger my_logger = Logger.getLogger(getClass().getName());

   /**
    * Locations of the approproate structure files
    */
   final String xmlLocation = "cbml" + File.separator;

   /**
    * Structure location, specified in the properties file.
    * Must be in a cbml directory, which is located in the root.
    */
   String caseStructLocation;

   /**
    * Similarity file, specified in the properties file.
    * Must be in a cbml directory, which is located in the root
    */
   String similarityProfileLocation;

   /**
    * Casebase file, specified in the properties file.
    * Must be in a cbml directory, which is located in the root
    */
   String casebaseLocation;

   private List casebase;

   /**
    * Structure of the case base
    */
   public static CaseStruct my_caseStruct;

   CBMLReader cbmlReader;

   CaseReader caseReader;

   SimilarityProfile simMeasure;

   /**
    * Defines whether the casebase is allowed to accept new cases.
    * Retrieved from properties file. 
    * Default is true -- it allows new cases by default. 
    */
   boolean addNewCases = true;


   /**
    * The list of training files being used.
    */
   String[] my_trainingFiles;

   /**
    * A list of paired annotations [proposition, annotation], where false positives have been found.
    */
   List<String[]> falsePositives;
   /**
    * THe list of positive matches found in one iteration
    */
   List<String> truePositives;

   /**
    * This requires any training data to be specified in the properties file
    */
   public CBRHandler() {
      getProperties();
      getSimilarityMeasure();
   }

   /**
    * Training data is passed in, meaning it came from an excel file, used for testing
    * @param trainingData
    */
   public CBRHandler(String[] trainingData) {
      //Get properties may instantiate my_trainingFiles but
      //this constructor replaces anything used in the properties file
      //with the specified training data.
      getProperties();
      getSimilarityMeasure();
      my_trainingFiles = trainingData;
   }

   /**
    * Set up the cbr engine by reading in the casebase(and then deleting the file), structure and similarity files.
    */
   public NN setUpCbrEngine() {
      NN cbrEngine;
      try {
         SimilarityProfile simMeasure = getSimilarityMeasure();
         //Load the case base.
         loadTrainingData();
         casebase = caseReader.readCasebase(new InputSource(
               new BufferedInputStream(new FileInputStream(casebaseLocation))));
         //Create the cbr engine with the casebase, the structure and the similarity measure as inputs.
         cbrEngine = new NN(casebase, my_caseStruct, simMeasure);
         //Adds the appropriate training data to the cbr engine
         return cbrEngine;
      } catch (Exception e) {
         my_logger.severe("Problem reading the case base" + e.getStackTrace());
      }
      //Return null if the code above has not succeeded in returning a valid cbr engine.
      return null;
   }

   /**
    * Return the similarity measures which are used in creating the case base
    * @return
    */
   public SimilarityProfile getSimilarityMeasure() {
      try {
         caseClass = Class.forName("fionn.cbr.nn.Casenode");
         InputSource source = new InputSource(new BufferedInputStream(
               new FileInputStream(caseStructLocation)));
         cbmlReader = new CBMLReader();
         if (cbmlReader.readCBMLDocument(source)) {

            my_caseStruct = cbmlReader.getCaseStruct();

            caseReader = new CaseReader(caseClass, my_caseStruct, Boolean.TRUE);

            //the source is now made to be the similarity xml file. This contains the link to the location similarity measure

            source = new InputSource(new BufferedInputStream(
                  new FileInputStream(similarityProfileLocation)));
            //Using the source document that was just loaded, the cbmlreader can get the similarity.
            if (cbmlReader.readCBMLDocument(source)) {
               //Turn the similarity results into a hashtable
               Hashtable profiles = cbmlReader.getSimilarities();
               //Cast the hash table as a SimilarityProfile object.
               simMeasure = (SimilarityProfile) profiles.get("default");
            }
         }
      } catch (ClassNotFoundException e) {
         my_logger
         .warning("The case has not been found when setting up the cbr engine"
               + e.getStackTrace());
      } catch (FileNotFoundException e) {
         my_logger
         .warning("The file has not been found when setting up the cbr engine"
               + e.getStackTrace());
      } catch (CBMLException e) {
         my_logger
         .warning("A cbml error has occurred while setting up cbr engine"
               + e.getStackTrace());
      }
      return simMeasure;
   }

   /**
    * Return a data structure which contains all possible classifications.
    * This data structure is an array of sorted sets
    * @param a_caseStrcture
    * @return
    */
   public ClassificationSortedSet[] setUpClassificationStore() {
      /*
       * Get the structure file and store it as a 2d array of classification sorted sets
       */
      //Get the features
      List featurePaths = my_caseStruct.getFeaturePaths();
      //Create a store of with the number of features in the path
      ClassificationSortedSet[] css = new ClassificationSortedSet[featurePaths
                                                                  .size()];
      for (int i = 0; i < css.length; i++) {
         css[i] = new ClassificationSortedSet();
      }
      //Fill each element in the array with the appropriate values.
      for (int i = 0; i < featurePaths.size(); i++) {
         String path = (String) featurePaths.get(i);
         FeatureStruct featureStruct = my_caseStruct.getFeatureStruct(path);
         List features;
         try {
            if (featureStruct.getType() == FeatureStruct.STRING) {
               //Provide a default classification value of OFF
               //When classified, this will become the name of the sensor
               String feature = featureStruct.getFeaturePath().substring(featureStruct.getFeaturePath().indexOf('/'));
               Classification c = new Classification(path, feature
                     .substring(feature.lastIndexOf('/') + 1), 0, 0);
               css[i].add(c);
            } else {
               features = featureStruct.getValues();
               for (int j = 0; j < features.size(); j++) {
                  String feature = (String) features.get(j);
                  Classification c = new Classification(path, feature
                        .substring(feature.lastIndexOf('/') + 1), 0, 0);
                  css[i].add(c);
               }
            }
         } catch (IncompatableFeatureException e) {
            my_logger.warning("Error getting values from feature struct"
                  + e.getMessage());
         }
      }
      return css;
   }

   /**
    * Every time we need to check a situation, a case must be created, into which a set of features
    * are put. 
    * @param caseName
    * @param caseClass
    * @return
    */
   public Casenode createCase(ClassificationSortedSet[] a_css,
         Classifier a_Classifier, DateTime a_time) {
      Casenode newCase = null;
      try {
         //Create the case and name it..
         final Constructor construct = caseClass.getConstructor(null);
         newCase = (Casenode) construct.newInstance(null);
         newCase.setCreationTime(a_time);
         newCase.setName("Case at: " + a_time.toString());
         //Add all features to the case
         StringFeature currentFeature = null;
         for (int i = 0; i < a_css.length; i++) {
            ClassificationSortedSet currentSet = a_css[i];
            Classification firstFeature = currentSet.get(0);
            //If this feature has been classified.
            if (firstFeature.getConfidence() > 0) {
               currentFeature = new StringFeature(firstFeature.getType(),
                     firstFeature.getValue());
               newCase.addFeature(currentFeature);
            }
         }
         //Add the time classification
         StringFeature timeFeature = a_Classifier.getTimeframe(a_time);
         newCase.addFeature(timeFeature);
      } catch (SecurityException e) {
         System.out.println("Security Exception!");
      } catch (IllegalArgumentException e) {
         System.out.println("Illegal Argument Exception!");
      } catch (NoSuchMethodException e) {
         System.out.println("No such method exception!");
      } catch (InstantiationException e) {
         System.out.println("Instantiation Exception!");
      } catch (IllegalAccessException e) {
         System.out.println("Illegal Access Exception!");
      } catch (InvocationTargetException e) {
         System.out.println("Invocation Target Exception!");
      }
      return newCase;
   }

   /**
    * Get the k nearest neighbours to the test case and print them out.
    * The feature count represents the number of features in the testcase.
    * -1 = a match within the threshold has been found
    * -2 = a match outsied the threshold has been found, and a case is being added
    * > 0 = the number of levels that 2 situations have in common 
    * @param a_testCase
    * @param a_featureCount
    */
   public double getNearestNeighbours(Casenode a_testCase, int k,
         double threshold) {
      /*
       * The number of features a case has. Used for comparison
       * to test case
       */
      double featureCount;
      /*
       * The similarity between cases
       */
      double activation;

      /*
       * The best activation of the k results(the top activation)
       */
      //TODO make sure that this value always goes above 0 when updated.
      double bestActivation = 0;

      /*
       * Retrieves the annotated situation for the current time
       */
      DateTime creationTime = a_testCase.getCreationTime();

      String annotatedSituation = Simulator.my_annotationProcessor
      .getSituation(creationTime);

      //This occurrs when there is no annotation for the specified time period.
      //In such a sutuation, a correct answer cannot be determined, so there is no
      //point in checking.
      if (annotatedSituation.equalsIgnoreCase("unknown")) {
         return -3;
      } else {
         /*
          * Get the neighbours
          */
         int casebaseSize = Simulator.my_cbrEngine.getCasebaseSize();
         if (casebaseSize < k) {
            k = casebaseSize;
         }

         //Get the nearest cases to the test case, ranked by how often 
         //each has appeared.
         //We are assuming here that the more something has appeared in the past,
         //the more likely it is to appear in the future.
         //List topCases = getTopCases(a_testCase, k);
         List topCases = Simulator.my_cbrEngine.getNeighbors(a_testCase,k);

         /*
          * Set up the list which stores all the false positive results
          */
         falsePositives = new LinkedList<String[]>();
         /*
          * Set up the list which stores all the true positive results
          */
         truePositives = new LinkedList<String>();
         /*
          * This array stores the solution for each of the k proposed cases
          */
         List<Double> solutions = new LinkedList<Double>();

         /*
          * Solution of the current case being compared
          */
         String proposedSituation;
         StringFeature proposedSolutionFeature;
         message = new StringBuffer();
         message.append("\nHere is your target case:\n");
         message.append(a_testCase.toString());
         message.append("\nHere are your top cases:\n");

         for (int i = 0; i < topCases.size(); i++) {
            Casenode casenode = (Casenode) topCases.get(i);
            featureCount = countFeatures(a_testCase);
            //The activation of a case is the sum of the features which are *also* in the proposed case
            //The activation is not a reflection on the size of the case, but the number of common elements
            //The maximum activation is the sum of the features in the proposed case.
            activation = casenode.getActivation();

            proposedSituation = (String) casenode.getSolution().getValue();
            proposedSolutionFeature = new StringFeature("/situation",
                  proposedSituation);

            //Get the top activation
            if (i == 0) {
               bestActivation = activation;
            }
            
            //System.out.println(featureCount + " " + activation);
            
            /**
             * If the top case(i==1) has an activation value that is the threshold more 
             * less than the feature count, then add the testcase.
             * There is also a check to see if the addition of new cases is allowed in the properties file. 
             * There is no need to process the rest of the k values as the best proposed case is not 
             * good enough, hence it returns.
             */
            if ((i == 0) && (featureCount - activation) > threshold && addNewCases == true){
               numberOfComparisonsMade++;
               // Defines the standard input stream
               System.out
               .println("(" + activation + ") " + casenode.toString());
               System.out.println(a_testCase.toString());

               /**
                * Here, instead of asking the user, we load the annotated 
                * situation for the given time and set that
                * as the new case situation. 
                */

               try {

                  System.err.println("Adding new case");
                  //If multiple situations are occurring, split the case up into the appropriate
                  //features
                  getCases(a_testCase);
                  //Create a new feature for each of the new situations
                  StringFeature solnFeature = new StringFeature("/situation",
                        annotatedSituation);
                  a_testCase.setSolution(solnFeature);
                  Casenode newCase = (Casenode) a_testCase.clone();
                  //To add multiple features, they must have unique names, hence they have to be
                  //changed here, since a different solution is being given to the same case multiple 
                  //times. This is done by appending the situation to the name.
                  String new_name = newCase.getName() + "("
                  + annotatedSituation + ")";
                  newCase.setName(new_name);
                  //Set the solution of the proposed case
                  newCase.setSolution(solnFeature);
                  System.out.println("Test Case: " + newCase.toString());
                  //Add the newly annotated casenode to the casebase.
                  Simulator.my_cbrEngine.addCase(newCase);
                  //Store the case in training file
                  try {
                     Simulator.my_fileWriter.append(newCase.toString()
                           + System.getProperty("line.separator"));
                  } catch (IOException e) {
                     my_logger.severe("Cannot add case to training file"
                           + e.getLocalizedMessage());
                  }
               } catch (CloneNotSupportedException e1) {
                  // TODO Auto-generated catch block
                  e1.printStackTrace();
               }
               /**
                * -2 is used to identify itself the addition
                * of a new case.
                */
               return -2;

            } else if (activation == bestActivation && activation > 0) { // 
               message.append("\n(" + activation + ")" + casenode.toString()
                     + "\n");
               //System.err.println(featureCount + " " + activation);
               numberOfComparisonsMade++;

               double situationSimilarity = getSimilarity(proposedSituation, annotatedSituation);
               if (situationSimilarity == 1) {
                  //Increment the count of correct results for this situation
                  //Simulator.my_annotationProcessor
                  //.incrementCorrectSituation(proposedSituation);

                  message
                  .append("We think: "
                        + proposedSituation
                        + "\n"
                        + "When in fact: "
                        + annotatedSituation
                        + "\n"
                        + "|----------------------------------------------------|\n\n");
                   //System.out.println(message.toString());
                  /**
                   * A minus number represents a correct match
                   */
                  solutions.add(-1.0);
                  //This ELSE part checks for a partial match when the solutions do not match exactly, 
               } else {
                  //Increment the count of incorrect results for this situation
                  //Simulator.my_annotationProcessor
                  //.incrementIncorrectSituation(annotatedSituation, proposedSituation);

                  //Make a feature out of the annotation and compare it to the proposed situation. 
                  StringFeature annotatedSituationFeature = new StringFeature(
                        "/situation", annotatedSituation);
                  double similarity = simMeasure.calculateSimilarity(
                        annotatedSituationFeature, proposedSolutionFeature);
                  message
                  .append("Incorrect Situation Determined...\n"
                        + "We thought: "
                        + proposedSituation
                        + "\n"
                        + "When in fact: "
                        + annotatedSituation
                        + "\n"
                        + "Which have a similarity of"
                        + similarity
                        + "\n"
                        + "|----------------------------------------------------|\n\n");
                   //System.err.println(message.toString());
                  /**
                   * This number should 0 or greater, depending on how many levels apart two situations are.
                   */
                  solutions.add(similarity);
               }
            }
         }
         return getBestSolution(solutions);
      }
   }

   /**
    * Get the top k nearest neighbours of a test case from the casebase
    * Return the ranked list based on order of occurrance..
    * @param a_testCase
    * @param k
    * @return
    */
   private List getTopCases(Casenode a_testCase, int k) {
      //Get the k nearest neighbours
      List unsortedNeighbours = Simulator.my_cbrEngine.getNeighbors(a_testCase,
            k);
      List sortedNeighbours = new LinkedList();
      Casenode currentCasenode;
      String currentSituation;
      //Turn the list of previously seen events into a blocking queue, which sorts them based on number
      //of occurrances.
      PriorityBlockingQueue<AnnotatedResult> sortedResults = new PriorityBlockingQueue<AnnotatedResult>(
            Simulator.my_annotationProcessor.annotations);
      //Go through the blocking queue and pick out the ones which correspond to the
      //k nearest neightbours. When a corresponding case is found, add it to the sorted set
      //of casenodes. This will ensure that the top cases are ordered...hopefully..
      AnnotatedResult ar;
      while ((ar = sortedResults.poll()) != null) {
         for (int i = 0; i < unsortedNeighbours.size(); i++) {
            currentCasenode = (Casenode) unsortedNeighbours.get(i);
            currentSituation = (String) currentCasenode.getSolution()
            .getValue();
            if (currentSituation.equalsIgnoreCase(ar.getAnnotation())) {
               sortedNeighbours.add(currentCasenode);
            }
         }
      }
      //If the annotated situation is not present in the casebase, it has not been seen yet, so just
      //return the unsorted neighbours -- this should only cause problems occur at the beginning.
      if(sortedNeighbours.size() == 0){
         return unsortedNeighbours;
      }
      return sortedNeighbours;
   }

   /**
    * Checks whether an exact solution is found. If not, checks
    * whether an incorrect solution was found. Otherwise checks whether
    * no solution was found.
    * @param a_solution_set
    * @return
    */
   private double getBestSolution(List<Double> a_solution_set) {

      //Check for an exact match
      for (double solution : a_solution_set) {
         if (solution == -1) {
            return solution;
         }
      }
      //Checks whether a bad solution was found
      for (double solution : a_solution_set) {
         if (solution >= 0) {
            return solution;
         }
      }

      return 0;
   }

   /**
    * Compares two features
    * @param a_feature
    * @param b_feature
    * @return
    */
   public double compare(Feature a_feature, Feature b_feature) {
      simMeasure = getSimilarityMeasure();
      double similarity = simMeasure.calculateSimilarity(a_feature, b_feature);
      return similarity;
   }

   /**
    * Counts the number of features in a case.
    * @param a_case
    * @return
    */
   public double countFeatures(Casenode a_case) {
      final List<Feature> features = a_case.getFeatures();
      double featureSum = 0;
      String currentFeatureType;
      for (Feature f : features) {
         currentFeatureType = f.getName();
         featureSum += simMeasure.getFeatureWeight("/" + currentFeatureType);
      }
      return featureSum;
   }

   /**
    * If there are specified files containing training data in the properties file, load them into the casebase.
    */
   private void loadTrainingData() {
      try {
         File cases = new File(casebaseLocation);
         cases.createNewFile();
         //Ensure that this file is deleted when the virtual machine terminates.
         cases.deleteOnExit();

         FileWriter fw = new FileWriter(cases);
         fw.append("<?xml version=\"1.0\"?>" + "\n"
               + "<casebase domain=\"scatterbox\">");
         fw
         .append("<case name=\"Situation1\"><time>900-930</time><situation>default</situation></case>");
         File f;
         FileReader fr;
         BufferedReader br;
         //Check whether any training data is used
         if (my_trainingFiles.length != 0) {
            if (!my_trainingFiles[0].equals("")) {
               for (String s : my_trainingFiles) {
                  f = new File("training/" + s);
                  fr = new FileReader(f);
                  br = new BufferedReader(fr);
                  String currentLine;
                  do {
                     currentLine = br.readLine();
                     fw.append(currentLine);
                  } while (currentLine != null);
               }
            }
         }
         fw.append("</casebase>");
         fw.close();
      } catch (Exception e) {
         my_logger.warning("No training file found");
         e.printStackTrace();
      }
   }

   /**
    * Get the properties from the properties file.
    * Properties include structure, similarity, casebase and training files. 
    */
   private void getProperties() {
      try {
         /**
          * Get the casebase, structure and similarity files from the properties file
          */
         JSONObject cbml = (JSONObject) Simulator.properties.get("cbml");
         caseStructLocation = xmlLocation + cbml.getString("structure");
         similarityProfileLocation = xmlLocation + cbml.getString("similarity");
         casebaseLocation = xmlLocation + cbml.getString("casebase");
         addNewCases = cbml.getBoolean("addnewcases");

//         /**
//          * Get the training files.
//          */
         JSONObject using = (JSONObject) Simulator.properties.get("training_files");
         int numTrainingFiles = using.length();
         my_trainingFiles = new String[numTrainingFiles];
         if (numTrainingFiles != 0) {
            for (int i = 0; i < numTrainingFiles; i++) {
               my_trainingFiles[i] = using.getString("" + i + "");
            }
         }

      } catch (JSONException e) {
         my_logger.severe("A JSON error occurred while getting neighbours");
      }
   }

   /**
    * If multiple situations are currently happening, split the current case into 
    * separate cases which represent the constituent situations.
    * @param c
    * @return
    */
   public List<Casenode> getCases(Casenode c){
      List<Casenode> annotatedSituations = new LinkedList<Casenode>();



      return annotatedSituations;
   }

   /**
    * Compares an annotation to a proposed situation. Both arguments can contain multiple solutions
    * separated by AND. If a match is not found for a proposed solution, a false positive is added to each 
    * of the annotated solutions. If a match is found, no false positives are noted. 
    * @param proposedSolution
    * @param annotatedSolution
    * @return
    */
   public double getSimilarity(String proposedSolution, String annotatedSolution){
      String[] proposedArray = proposedSolution.split("AND");
      String[] annotatedArray = annotatedSolution.split("AND");
      double numMatches = 0;
      //For each proposition
      for(String proposition:proposedArray){
         //Check against each annotation for a match
         for(String annotation:annotatedArray){
            //If a match has been found
            if(proposition.equalsIgnoreCase(annotation) && !truePositives.contains(annotation)){
               //Check in the list of false positives for times when this annotation
               //was flagged as a false positive. 
               for(int i=0;i<falsePositives.size();i++){
                  if(falsePositives.get(i)[0].equalsIgnoreCase(proposition)){
                     //remove false positives, since a match has been found. 
                     falsePositives.remove(i);
                  }
               }
               //add the "true positive" match to the lit of false positives. 
               //THis is done to stop future false positives occurring.
               truePositives.add(proposition);
               Simulator.my_annotationProcessor.incrementCorrectSituation(proposition);
               numMatches++;
               break;
            }else{
               //Flag checks whether a true positive for the current annotation has already been seen.
               boolean trueMatchAlreadyFound = false;
               //Iterate through the true positives to find a positive match.
               for(int i=0;i<truePositives.size();i++){
                  if(truePositives.get(i).equalsIgnoreCase(proposition)){
                     trueMatchAlreadyFound = true;
                  }
               }
               //If a match has not been seen before, then this is a false positive
               if(trueMatchAlreadyFound == false){
                  falsePositives.add(new String[]{proposition, annotation});
               }
            }
         }
      }

      for(int i=0;i<falsePositives.size();i++){
         String[] currentFalsePositive = falsePositives.get(i);
         //the string arrays are in the form[proposition, annotation], so get added to this method backwards.
         Simulator.my_annotationProcessor.incrementIncorrectSituation(currentFalsePositive[1], currentFalsePositive[0]);
      }

      if(numMatches == proposedArray.length){
         return 1;
      }
      return 0;
   }

}
