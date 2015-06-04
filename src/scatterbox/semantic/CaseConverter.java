package scatterbox.semantic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class CaseConverter {

   OntModel placelabOntology;
   OntModel classificationOntology;
   //   String placelab = "http://ontonym.org/0.8/placelab.owl#";
   //   String location = "http://ontonym.org/0.8/location#";
   String classification = "http://ontonym.org/0.8/classifications.owl#";
   String placelab = "http://ontonym.org/0.8/placelab.owl#";
   List<SituationFeatureMap> situationList = new LinkedList<SituationFeatureMap>();
   List<FeatureSorter> featureSorterList = new LinkedList<FeatureSorter>();
   List<FeatureSorter> secondaryFeatureSorterList = new LinkedList<FeatureSorter>();
   HashMap<String, Integer> activityCount = new HashMap<String, Integer>(); 
   HashMap<String, Double> activityProbability = new HashMap<String, Double>();
   List<String> annotationRecord  = new LinkedList<String>();

   /**
    * Keep track of all the features contained in the whole file, one entry per unique feature
    */
   List<String> overallFeatureList = new LinkedList<String>();
   /**
    * The number of activity instances in the whole training document.
    */
   int numberOfActivityInstances = 0;
   /**
    * Entropy of each 
    */
   HashMap<String, Double> overallFeatureEntropy = new HashMap<String, Double>();

   /**
    * Overall feature count
    * @param args
    * @throws SAXException
    */
   HashMap<String, Integer> overallFeatureCount = new HashMap<String, Integer>();

   public static void main(String[] args) throws SAXException{
      CaseConverter cc = new CaseConverter();
      cc.loadOntology();
      cc.convertToOntology();

//      FileOutputStream fos;
//      try {
//         fos = new FileOutputStream("ontologies/ammendedClassifications.owl");
//         cc.classificationOntology.write(fos);
//      } catch (IOException e) {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
//
//      SituationReasoner r = new SituationReasoner();
//      r.reasonWithRules();
   }

   /**
    * From the training case base, process each case and add the appropriate feature-probability pairs
    * to the Ontology.
    */
   private void convertToOntology(){
      try {
         File file = new File("training/training2");
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db;

         db = dbf.newDocumentBuilder();

         Document doc = db.parse(file);
         doc.getDocumentElement().normalize();
         NodeList nodeLst = doc.getElementsByTagName("case");

         numberOfActivityInstances = nodeLst.getLength();

         for(int i=0;i<nodeLst.getLength();i++){
            // The case
            Node n = nodeLst.item(i);
            //The case features, each one is a node
            NodeList features = n.getChildNodes();
            //The node containing the situation
            Node situationNode = features.item(features.getLength()-1);
            //The situation string.
            String activity = situationNode.getTextContent().replace(" ", "_");

            /**
             * If multiple situations have been seen before, but in a different order, 
             * revert to the order that has already been seen Ex,
             * X and Y and Z has been seen. If Z and X and Y is seen, revert to X, Y, Z
             */
            int seenBefore = seenSituationBefore(activity); 
            if(seenBefore != -1){
               activity = annotationRecord.get(seenBefore);
            }else{
               annotationRecord.add(activity);
            }


            if(!seenActivityBefore(activity)){
               situationList.add(new SituationFeatureMap(activity));
               activityCount.put(activity, 1);
               addActivityToOntology(activity);
            }else{
               int count = activityCount.get(activity);
               count = count + 1;
               activityCount.put(activity, count);
            }
            //For each feature of the case, besides the situation case
            for(int j=0;j<features.getLength()-1;j++){
               Node feature = features.item(j);
               addFeature(activity, feature.getNodeName());
            }
         }

         //Get rid of all "unimportant" features
         //In other words, features which occur a below average number of times.
         //         for(SituationFeatureMap sfm: situationList){
         //            sfm.filterMap();
         //         }

         //By adding all the maps to a priority blocking queue, they can be sorted.
         sortAllFeatureMaps();
         processRelevantFeatures();
      } catch (ParserConfigurationException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (SAXException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   /**
    * For each activity, iterate through the features and add them to the ontology
    */
   private void processRelevantFeatures(){
      String[] queryResult;

      for(FeatureSorter sorter: featureSorterList){
         //Get activity name
         String activity = sorter.getActivity(); 
         
         System.out.println(activity + activityCount.get(activity));

         for(int i=0;i<sorter.size();i++){
            FeatureCount tempFC = sorter.get(i);

            String tempFeature = tempFC.getFeature();
            double tempValue = tempFC.getCount();
            //Get the type, location and associated object to this feature.
            queryResult = queryOntology(tempFeature);
            double igain = tempFC.getIGain();

            //if(igain > 0 && !activity.equalsIgnoreCase("unknown") && !activity.contains("AND")){
            if(!activity.equalsIgnoreCase("unknown")){
               System.out.println(activity + 
                     "  " + tempFeature + 
                     "  " + queryResult[0] +
                     "  " + queryResult[1] +
                     "  " + queryResult[2] + 
                     "  " + tempValue +
                     "  " + igain + "--igain");
               addFeatureProb(activity, tempFeature, igain);
            }
         }
      }
   }


   /**
    * Load the ontology.
    */
   private void loadOntology(){
      try {    

         FileInputStream fis = new FileInputStream("../ontonym/SampleOntologies/placelab.owl");
         FileInputStream fis2 = new FileInputStream("../ontonym/SampleOntologies/classifications.owl");
         placelabOntology = ModelFactory.createOntologyModel();
         classificationOntology = ModelFactory.createOntologyModel();
         placelabOntology.read(fis, "");
         classificationOntology.read(fis2, "");
         //Property p = classificationOntology.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
         //RDFNode r = classificationOntology.createResource("http://ontonym.org/0.8/classifications.owl#testFeatureProbability");
         //printIterator(classificationOntology.listStatements(), "");
      } catch (FileNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   /**
    * Query the ontology for the attached object, location and type of a sensor.
    * @param sensor
    * @return
    */
   private String[] queryOntology(String classification){
      String[] objectAndLocation = new String[3];
      String sensorQuery = 
         "PREFIX placelab: <http://ontonym.org/0.8/placelab.owl#> "
         + "PREFIX classification: <http://ontonym.org/0.8/classifications.owl#> "
         + "PREFIX location: <http://ontonym.org/0.8/location#> "
         + "PREFIX provenance: <http://ontonym.org/0.8/provenance#> "
         + "PREFIX rdf: <http://www.w3.org/2000/01/rdf-schema#> "
         + "PREFIX rdfns: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
         + "SELECT ?sensortype ?object ?location ?coverage WHERE{"
         + "classification:"+classification+" provenance:derivedFrom ?sensor ."
         + "?sensor rdfns:type ?sensortype . "
         + "?sensor placelab:attachedTo ?object . "
         + "?object location:locatedIn ?location . "
         + "OPTIONAL { ?sensor placelab:locatedIn ?coverage . } "
         + "}";

      QueryExecution objectLocationQuery = QueryExecutionFactory.create(sensorQuery, classificationOntology);
      ResultSet resultSet = objectLocationQuery.execSelect();

      while(resultSet.hasNext()){
         QuerySolution s = resultSet.nextSolution();
         String object = s.get("object").toString();
         String location = s.get("location").toString();
         String sensorType = s.get("sensortype").toString();

         if(location.equalsIgnoreCase("null")){
            location = s.get("coverage").toString();
         }
         objectAndLocation[0] = object.substring(object.indexOf('#')+1);
         objectAndLocation[1] = location.substring(location.indexOf('#')+1);
         if(sensorType.contains("placelab")){
            objectAndLocation[2] = sensorType.substring(sensorType.indexOf('#')+1);
         }
      }

      for(int i=0;i<objectAndLocation.length;i++){
         if(objectAndLocation[i] == null){
            objectAndLocation[i] = "null";
         }
      }
      return objectAndLocation;
   }

   /**
    * Adds an previously unseen activity to an ontology
    * @param activity
    */
   private void addActivityToOntology(String activity){
      Resource activityResource = classificationOntology.createResource(classification+activity.replace(" ", "_"));
      RDFNode activityType = classificationOntology.createResource("http://ontonym.org/0.8/classifications.owl#activity");
      Property type = classificationOntology.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
      Statement newActivity = classificationOntology.createStatement(activityResource, type, activityType);
      classificationOntology.add(newActivity);
   }

   /**
    * Adds a feature-probability pair to the ontology, relating to an activity.
    * @param an_activity
    * @param a_feature
    * @param prob
    */
   private void addFeatureProb(String an_activity, String a_feature, double prob){
      Resource newFeatureProb = classificationOntology.createResource(classification+an_activity+"_"+a_feature);
      RDFNode FeatureProb = classificationOntology.createResource(classification+"FeatureProbability");
      Resource theActivity = classificationOntology.createResource(classification+an_activity);
      Property hasFeature = classificationOntology.createProperty(classification+"feature");
      Property hasProbability = classificationOntology.createProperty(classification+"probability");
      Property identifiedBy = classificationOntology.createProperty(classification+"identifiedBy");
      Resource featureResource = classificationOntology.createResource(classification+a_feature);
      Property type = classificationOntology.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

      Statement[] newFeatureProbStatements = new Statement[4];
      newFeatureProbStatements[0] = classificationOntology.createStatement(theActivity, identifiedBy, newFeatureProb);
      newFeatureProbStatements[1] = classificationOntology.createStatement(newFeatureProb, type, FeatureProb);
      newFeatureProbStatements[2] = classificationOntology.createLiteralStatement(newFeatureProb, hasProbability, prob);
      newFeatureProbStatements[3] = classificationOntology.createStatement(newFeatureProb, hasFeature, featureResource);

      classificationOntology.add(newFeatureProbStatements);
   }


   /**
    * Checks whether the feature map has seen this activity before.
    * @param activity
    * @return
    */
   public boolean seenActivityBefore(String activity){
      for(SituationFeatureMap sfm: situationList){
         if(sfm.getActivity().equalsIgnoreCase(activity)){
            return true;
         }
      }
      return false;
   }

   /**
    * Adds a feature and a count of 1 to a given feature map
    * @param activity
    * @param feature
    */
   public void addFeature(String activity, String feature){
      for(SituationFeatureMap sfm: situationList){
         if(sfm.getActivity().equalsIgnoreCase(activity)){
            sfm.add(feature);
         }
      }
   }

   /**
    * 
    */
   public void sortAllFeatureMaps(){
      List<String> featureList;
      List<Integer> valueList;

      for(SituationFeatureMap sfm: situationList){
         //Get activity name
         String activity = sfm.activity;
         FeatureSorter sorter = new FeatureSorter(activity);
         //for this activity, get the features and values 
         featureList = sfm.getFeatures();
         valueList = sfm.getValues();

         //for each feature value pair, create a feature count object and add it to the 
         //current sorter
         for(int i=0;i<featureList.size();i++){
            String currentFeature = featureList.get(i);
            int currentValue = valueList.get(i);

            //keep track of all features by adding them to this hash map
            if(overallFeatureCount.containsKey(currentFeature)){
               int oldValue = overallFeatureCount.get(currentFeature);
               overallFeatureCount.put(currentFeature, oldValue+currentValue);
            }else{
               overallFeatureList.add(currentFeature);
               overallFeatureCount.put(currentFeature, currentValue);
            }

            FeatureCount cf = new FeatureCount(currentFeature, currentValue);
            sorter.add(cf);
         }
         featureSorterList.add(sorter);
      }
      getInformationGain();

      for(FeatureSorter fs : featureSorterList){
         Collections.sort(fs);
      }

   }

   private void getActivityProbabilities(){
      Set<String> activitySet = activityCount.keySet();
      Iterator<String> activityIterator = activitySet.iterator();
      while(activityIterator.hasNext()){
         String act = activityIterator.next();
         int currentActCount = activityCount.get(act);
         double activityProp = (double) currentActCount/ (double) numberOfActivityInstances;
         activityProbability.put(act, activityProp);
      }
   }

   /**
    * Get my version of information gain for each feature in the feature sorter.
    */
   private void getMyInformationGain(){
      secondaryFeatureSorterList = featureSorterList;
      //Get the probability of each activity ocurring
      getActivityProbabilities();
      //Get the overall entropy of each feature
      //getOverallFeatureEntropy();

      //Go through each feature sorter and assign an igain value
      //to each featurecount object. 
      for(FeatureSorter igainsorter: featureSorterList){
         String activity = igainsorter.getActivity();
         int numberOtherActivityInstances= numberOfActivityInstances - activityCount.get(activity);
         //Go through each feature count in the feature sorter.
         for(FeatureCount tempFC:igainsorter){
            //The entropy of this feature with respect to all activities but this one.
            double probWRTotheractivities = 0;
            //Go through the feature sorters of all activities except for this one and find the 
            //entropy of each feature contained within them.
            for(FeatureSorter tempsorter: secondaryFeatureSorterList){
               String internalSorterActivity = tempsorter.getActivity();
               double tempFeatureProb = 0;
               if(!tempsorter.getActivity().equalsIgnoreCase(activity)){

                  for(FeatureCount testFC:tempsorter){
                     if(tempFC.getFeature().equalsIgnoreCase(testFC.getFeature())){

                        double tempCount = (double) testFC.getCount();
                        double totalFeatureCount = activityCount.get(internalSorterActivity);
                        tempFeatureProb = (tempCount/totalFeatureCount);
                        double currentProb = ((totalFeatureCount/(double)numberOtherActivityInstances) * tempFeatureProb);
                        probWRTotheractivities += currentProb;
                        //double nonFeatureProb = ((totalFeatureCount - tempCount)/totalFeatureCount);
                        //double featureEntropy = getEntropy(featureProb);       
                        //double nonFeatureEntropy = getEntropy(nonFeatureProb);
                     }
                  }
               }
            }
            double igain = 1 - probWRTotheractivities;
            tempFC.setIGain(igain);
         }
      }
   }
   /**
    * Get the information gain for each feature in the feature sorter.
    */
   private void getInformationGain(){
      //Get the probability of each activity ocurring
      getActivityProbabilities();
      //Get the overall entropy of each feature
      getOverallFeatureEntropy();

      for(String testFeature:overallFeatureList){
         //Go through each feature sorter and assign an igain value
         //to each featurecount object. 
         double totalFeatureCount = overallFeatureCount.get(testFeature);
         double featureProb = totalFeatureCount/numberOfActivityInstances;
         double nonFeatureProb = 1 - featureProb;
         //Go through the features representing each activity
         for(FeatureSorter igainsorter: featureSorterList){
            String activity = igainsorter.getActivity();
            //Get the probability of this activity occurring
            double currentActivityCount = activityCount.get(activity);
            double currentActivityProb = currentActivityCount/numberOfActivityInstances;
            double currentNonActivityProb = 1-currentActivityProb;
            double currentActivityEntropy = getEntropy(currentActivityProb) + getEntropy(currentNonActivityProb);
            double probActivityGivenFeature = 0;
            double probActivityGivenNoFeature = 0;
            double tempCount = 0;
            String featureName = "";
            //Go through each feature count in the feature sorter.
            for(FeatureCount tempFC:igainsorter){
               //Every time a matching feature set is found, add the count of those features
               if(tempFC.getFeature().equalsIgnoreCase(testFeature)){
                  tempCount = (double) tempFC.getCount();
                  featureName = tempFC.getFeature();
               }
            }

            probActivityGivenFeature = (tempCount/totalFeatureCount);
            //The number of cases for which this was not a feature.
            double totalNonOccurrences = numberOfActivityInstances - totalFeatureCount;

            double totalNonOccurrencesWRTThisActivity = activityCount.get(activity) - tempCount;
            
            if(totalNonOccurrences != 0){
               probActivityGivenNoFeature = (totalNonOccurrencesWRTThisActivity/totalNonOccurrences);
            }
            
            double entropyActivityGivenFeature = getEntropy(probActivityGivenFeature) 
                                                   +
                                                 getEntropy(1-probActivityGivenFeature);

            double entropyActivityGivenNoFeature = getEntropy(probActivityGivenNoFeature) 
                                                   +
                                                 getEntropy(1-probActivityGivenNoFeature);
            
            //System.out.println(entropyActivityGivenNoFeature);
            
            double averageEntropy = featureProb*entropyActivityGivenFeature + nonFeatureProb*entropyActivityGivenNoFeature;
            
            //System.out.println(activity + " " + currentActivityEntropy + " -" + featureName + " " + averageEntropy);
            
            double igain = currentActivityEntropy - averageEntropy;

            //Go through each feature count in the feature sorter.
            for(FeatureCount tempFC:igainsorter){
               //Every time a matching feature set is found, add the count of those features
               if(tempFC.getFeature().equalsIgnoreCase(testFeature)){
                  tempFC.setIGain(igain);
               }
            }
         }
      }
   }

/**
 * Get the overall entropy of each feature.
 */
private void getOverallFeatureEntropy(){
   for(String currentFeature: overallFeatureList){

      int currentFeatureCount = overallFeatureCount.get(currentFeature);
      //The probability of a feature is its count divided by the number of features
      double featureProbability = (double) currentFeatureCount / (double) numberOfActivityInstances;

      int nonOccurrances = numberOfActivityInstances - currentFeatureCount;
      double nonFeatureProbability = (double) nonOccurrances / (double) numberOfActivityInstances;

      double featureEntropy = getEntropy(featureProbability);
      double nonFeatureEntropy = getEntropy(nonFeatureProbability);

      double entropy = featureEntropy;// + nonFeatureEntropy;
      overallFeatureEntropy.put(currentFeature, entropy);
   }
}

/**
 * Entropy is minus the sum of the probability multiplied by the log to the base 2 of
 * the probability
 * @param probability
 * @return
 */
public double getEntropy(double probability){
   if(probability == 0){
      return 0;
   }else{
      double logProb = Math.log(probability)/Math.log(2);
      double entropy = logProb*probability*-1;
      return entropy;
   }
}

/**
 * Checks to see if a situation has been processed before.
 * @param a_situation
 * @return
 */
private int seenSituationBefore(String a_situation){
   //If this is the first check.
   if(annotationRecord.size()==0){
      return -1;
   }

   for(int i=0;i<annotationRecord.size(); i++){
      String sit = annotationRecord.get(i);
      if(sit.equalsIgnoreCase(a_situation)){
         return i;
      }
   }
   String[] situationArray = a_situation.split("AND");
   for(int i=0;i<annotationRecord.size(); i++){
      String sit = annotationRecord.get(i);
      if(sit.contains("AND")){
         String[] testSituationArray = sit.split("AND");
         if(testSituationArray.length == situationArray.length){
            int numMatches = 0;
            for(String s : situationArray){
               for(String s1:testSituationArray){
                  if(s.equalsIgnoreCase(s1)){
                     numMatches++;
                     break;
                  }
               }
            }
            if(numMatches == situationArray.length){
               //System.out.println(a_situation + "has been seen before!!!");
               return i;
            }
         }else{
            return -1;
         }
      }
   }
   return -1;
}

public void printIterator(Iterator<?> i, String header) {

   System.out.println(header);

   for (int c = 0; c < header.length(); c++)

      System.out.print("=");

   System.out.println();

   if (i.hasNext()) {

      while (i.hasNext()){
         String res = i.next().toString();
         System.out.println(res);
      }

   }

   else

      System.out.println("<EMPTY>");

   System.out.println();

}
}
