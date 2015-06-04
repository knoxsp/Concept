package scatterbox.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.xml.sax.InputSource;

import scatterbox.classifier.Classification;
import scatterbox.classifier.ClassificationSortedSet;
import cbml.CBMLReader;
import cbml.CaseReader;
import cbml.cbr.CaseStruct;
import cbml.cbr.FeatureStruct;
import cbml.cbr.SimilarityProfile;
import cbml.cbr.feature.StringFeature;
import fionn.cbr.nn.Casenode;
import fionn.cbr.nn.NN;


public class SimpleCaseBaseTest {

   static final private Logger logger = Logger
   .getLogger(SimpleCaseBaseTest.class);
   
   Random generator = new Random();

   public SimpleCaseBaseTest() {

   }

   /**
    * @param args
    */
   public static void main(String[] args) {
      DOMConfigurator.configure("logger.xml");

      logger.info("\nEntering application.");
      SimpleCaseBaseTest test = new SimpleCaseBaseTest();
      test.testCasebase();
//      test.testClassifiationSet();
      logger.info("Finished application.");
   }

   public void testClassifiationSet(){      
      ClassificationSortedSet set  = new ClassificationSortedSet();
      Random generator = new Random();
      
      Classification c = new Classification("A0", "B0", 299, 10);
      Classification c1 = new Classification("A1", "B1", 300, 10);
      c.setDecay(1);
      c1.setDecay(10);
      set.add(c);
      set.add(c1);
      
      set.update();
      
      
   }
   

   /*
    * A query to get all activity for user knox between time x and time y
    * SELECT * FROM activity where user="knox" and keystrokes>0 and mousepresses>0 and timestamp between "2008-12-11 12:00:00" and "2008-12-11 16:00:00" order by timestamp
    */
   private Casenode createScatterboxCase(String caseName, Class caseClass) {
      Casenode newCase = null;
      try {
         final Constructor construct = caseClass.getConstructor(null);
         newCase = (Casenode) construct.newInstance(null);
         newCase.setName(caseName);
         newCase.addFeature(new StringFeature("/activity", "hour"));
         newCase.addFeature(new StringFeature("/location", "CASL"));
         newCase.addFeature(new StringFeature("/time", "worktime"));
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


   public void testCasebase(){
      final String xmlLocation = "cbml" + File.separator;
      final String caseStructLocation = xmlLocation + "structure.xml";
      final String similarityProfileLocation = xmlLocation + "similarity.xml";
      final String casebaseLocation = xmlLocation + "casebase.xml";

      try {
         Class caseClass = Class.forName("fionn.cbr.nn.Casenode");
         CBMLReader cbmlReader = new CBMLReader();
         InputSource source = new InputSource(new BufferedInputStream(
               new FileInputStream(caseStructLocation)));
         logger.info("Reading case structure.");
         if (cbmlReader.readCBMLDocument(source)) {
            SimilarityProfile simMeasure = null;
            CaseStruct caseStruct = cbmlReader.getCaseStruct();          
            
            /*
             * Get the structure file and store it as a 2d array of classification sorted sets
             */
          
            //There are 7 clasification types, each divided into a set of features which will be saved in
            //this data structure.
            ClassificationSortedSet[] css = new ClassificationSortedSet[7];
            for (int i = 0; i < css.length; i++)
            {
               css[i] = new ClassificationSortedSet();
            }
            List featurePaths = caseStruct.getFeaturePaths();
            for(int i=0;i<featurePaths.size();i++){
               String path = (String) featurePaths.get(i);
               System.out.println("Path: "+path);
               FeatureStruct featureStruct = caseStruct.getFeatureStruct(path);
               List features = featureStruct.getValues();
               for(int j=0;j<features.size();j++){
                  String feature = (String) features.get(j);
                  System.out.println(feature.substring(feature.lastIndexOf('/')+1));
                  Classification c = new Classification(path, feature.substring(feature.lastIndexOf('/')+1), generator.nextInt(100), 10);
                  css[i].add(c);
                  //System.out.println(css[i].get(j));
               }
            }            
            
            CaseReader caseReader = new CaseReader(caseClass, caseStruct,
                  Boolean.TRUE);
            //the source is now made to be the similarity xml file. This contains the link to the location similarity measure

            source = new InputSource(new BufferedInputStream(
                  new FileInputStream(similarityProfileLocation)));
            logger.info("Reading similarity measures.");
            //Using the source document that was just loaded, the cbmlreader can get the similarity.
            if (cbmlReader.readCBMLDocument(source)) {
               //Turn the similarity results into a hashtable
               Hashtable profiles = cbmlReader.getSimilarities();
              // System.out.println(profiles.toString());
               
               //Cast the hash table as a SimilarityProfile object.
               simMeasure = (SimilarityProfile) profiles.get("default");
               
               logger.info("Reading case base.");
               //Load the case base.
               //TODO ask lorcan why this is loaded here and not above.
               List casebase = caseReader.readCasebase(new InputSource(
                     new BufferedInputStream(new FileInputStream(
                           casebaseLocation))));
               //Create the cbr engine with the casebase, the structure and the similarity measure as inputs.
               NN cbrEngine = new NN(casebase, caseStruct, simMeasure);

               StringFeature s1 = new StringFeature("/location", "CASLCommonRoomComfySeatingArea");
               StringFeature s2 = new StringFeature("/location", "FourthFloorFoyer");
               double d = simMeasure.calculateSimilarity(s1, s2);
               System.out.println("Similarity of EatingArea = " + d);

               
               
               //Get the test case
               Casenode testcase = createScatterboxCase("testcase", caseClass);

               //Get the closest cases to the test case from the cbr engine

               List topCases = cbrEngine.getNeighbors(testcase, 10);
               cbrEngine.addCase(testcase);
               StringBuffer message = new StringBuffer();

               
               message.append("\nHere is your target case:\n");
               message.append(testcase.toString());
               message.append("\nHere are your top cases:\n");
               for (int i = 0; i < topCases.size(); i++) {
                  Casenode casenode = (Casenode) topCases.get(i);
                  message.append(i);
                  message.append(": (");
                  message.append(casenode.getActivation());
                  //message.append(casenode.toString());
                  message.append(casenode.getSolution().getValue());
                  message.append("\n");
               }
               //logger.info(message);

               //Get the closest cases to the test case from the cbr engine 
               Casenode testcase1 = createScatterboxCase("testcase1", caseClass);
               List topCases1 = cbrEngine.getNeighbors(testcase1, 10);
               message = new StringBuffer();

               message.append("\nHere is your target case:\n");
               message.append(testcase.toString());
               message.append("\nHere are your top cases:\n");
               for (int i = 0; i < topCases1.size(); i++) {
                  Casenode casenode = (Casenode) topCases1.get(i);
                  List features = casenode.getFeatures();
                  message.append(i);
                  message.append(": (");
                  message.append(casenode.getActivation());
                  message.append(topCases1.get(i).toString());
                  message.append("Has a size of: " + features.size());
                  message.append("\n");
               }
               //logger.info(message);
            }
         }
      } catch (Exception e) {
         System.out.println("An error has occurred creating the test" + e);
      }
   }
}
