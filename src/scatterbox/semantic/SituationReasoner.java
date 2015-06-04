package scatterbox.semantic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import cbml.cbr.feature.StringFeature;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class SituationReasoner {

   String classifications = "http://ontonym.org/0.8/classifications.owl#";

   String placelab = "http://ontonym.org/0.8/placelab.owl#";

   String location = "http://ontonym.org/0.8/location#";

   public static void main(String[] args) {
      SituationReasoner sr = new SituationReasoner();
      Builtin getPrimaryLocation = new GetPrimaryLocation();
      Builtin identifyOutlyingLocations = new IdentifyOutlyingLocations();
      com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry.theRegistry
      .register(getPrimaryLocation);
      com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry.theRegistry
      .register(identifyOutlyingLocations);
      sr.reasonWithRules();

   }

   public SituationReasoner() {
      Builtin getPrimaryLocation = new GetPrimaryLocation();
      Builtin identifyOutlyingLocations = new IdentifyOutlyingLocations();
      com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry.theRegistry
      .register(getPrimaryLocation);
      com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry.theRegistry
      .register(identifyOutlyingLocations);
   }

   /**
    * Using a rule base, find the features of a situation which are most likely to 
    * represent that situation.
    */
   public void reasonWithRules() {

      List rules = Rule.rulesFromURL("file:placelab.rules");
      Reasoner ruleReasoner = new GenericRuleReasoner(rules);

      Reasoner pellettReasoner = PelletReasonerFactory.theInstance().create();
      try {
         FileInputStream fis = new FileInputStream(
         "ontologies/ammendedClassifications.owl");
         FileInputStream fis1 = new FileInputStream(
         "ontologies/placelab.owl");
         Model emptyModel = ModelFactory.createDefaultModel();
         InfModel ontModel = ModelFactory.createInfModel(pellettReasoner,
               emptyModel);
         ontModel.read(fis, "");
         ontModel.read(fis1, "");
         InfModel ruleModel = ModelFactory.createInfModel(ruleReasoner,
               ontModel);
         ruleModel.getDeductionsModel();
         InfModel ontModel2 = ModelFactory.createInfModel(pellettReasoner,
               ruleModel);

         createCasebaseWithoutSemantic(ontModel2);
         System.out.println("Done with non semantic");

         createCasebaseWithSemantic(ontModel2);


         fis.close();
         fis1.close();
      } catch (FileNotFoundException e) {
         System.err.println("Ontology not found.");
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

      public void createCasebaseWithSemantic(Model a_model) {
         FileWriter fw;
         try {
            fw = new FileWriter("with_semantic");
   
   
            QueryHandler queryHandler = a_model.queryHandler();
            Node identifier = NodeFactory
            .create("<http://ontonym.org/0.8/device#identifier>");
            Node derivedFrom = NodeFactory
            .create("<http://ontonym.org/0.8/provenance#derivedFrom>");
            Node identifiedBy = NodeFactory.create("<" + classifications
                  + "identifiedBy>");
            Node locatedIn = NodeFactory.create("<" + location
                  + "locatedIn>");
            Node nonRepresentativeLocation = NodeFactory.create("<" + classifications
                  + "nonRepresentativeLocation>");
            Node featureNode = NodeFactory.create("<" + classifications
                  + "feature>");
            Node situation = NodeFactory.create("<" + classifications + "Situation>");
            Node type = NodeFactory
            .create("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
   
            String caseTemplate = "<case name=\"CASENAME\">";
            ExtendedIterator situationIterator = queryHandler.subjectsFor(type,
                  situation);
            boolean addToCases = false;
            System.err.println("WITH SEMANTIC!!!");
            while (situationIterator.hasNext()) {
               Node currentSituation = (Node) situationIterator.next();
               if(!currentSituation.getLocalName().contains("AND")){
                  String currentCase = caseTemplate.replace("CASENAME", currentSituation
                        + "_case");
                  currentCase = currentCase
                  + new StringFeature("/situation", (String) currentSituation
                        .getLocalName().replace("_", " "));
   
                  //Put all relevant locations into a list.
                  ExtendedIterator nonRepLoc = queryHandler.objectsFor(currentSituation, nonRepresentativeLocation);
                  List<String> nonRepLocs = new LinkedList<String>();
                  while(nonRepLoc.hasNext()){
                     Node nonNode = (Node) nonRepLoc.next();
                     nonRepLocs.add(nonNode.getLocalName());
                  }
   
                  ExtendedIterator identifierIterator = queryHandler.objectsFor(currentSituation, identifiedBy);
   
                  while (identifierIterator.hasNext()) {
                     Node primaryFeature = (Node) identifierIterator.next();
                     addToCases = true;
                     ExtendedIterator featureIterator = queryHandler.objectsFor(
                           primaryFeature, featureNode);
                     Node f = (Node) featureIterator.next();
                     if(!f.getLocalName().equalsIgnoreCase("time")){
                        Node sensor = (Node) queryHandler.objectsFor(f,derivedFrom).next();
                        Node location = (Node) queryHandler.objectsFor(sensor, locatedIn).next();
                        boolean ignoreFeature = false;
                        for(String l:nonRepLocs){
                           if(location.getLocalName().equalsIgnoreCase(l)){
                              ignoreFeature = true;
                           }
                        }
                        if(ignoreFeature == false){
                           Node ident = (Node) queryHandler.objectsFor(sensor, identifier).next();
                           currentCase = currentCase + new StringFeature(f.getLocalName(),
                                 (String) ident.getLiteralValue());
                        }
                     }
                  }
   
                  if (addToCases == true) {
                     fw.append(currentCase + "</case>\n");
                     System.out.println(currentCase + "</case>");
                     addToCases = false;
                  }
               }
            }
            fw.flush();
            fw.close();
         } catch (IOException e1) {
            e1.printStackTrace();
         }
      }

//      public void createCasebaseWithSemantic(Model a_model) {
//         FileWriter fw;
//         try {
//            fw = new FileWriter("with_semantic");
//   
//   
//            QueryHandler queryHandler = a_model.queryHandler();
//            Node identifier = NodeFactory
//            .create("<http://ontonym.org/0.8/device#identifier>");
//            Node derivedFrom = NodeFactory
//            .create("<http://ontonym.org/0.8/provenance#derivedFrom>");
//            Node identifiedBy = NodeFactory.create("<" + classifications
//                  + "identifiedBy>");
//            Node attachedTo = NodeFactory.create("<" + placelab
//                  + "attachedTo>");
//            Node locatedIn = NodeFactory.create("<" + location
//                  + "locatedIn>");
//            Node nonRepresentativeLocation = NodeFactory.create("<" + classifications
//                  + "nonRepresentativeLocation>");
//            Node featureNode = NodeFactory.create("<" + classifications
//                  + "feature>");
//            Node situation = NodeFactory.create("<" + classifications + "Situation>");
//            Node type = NodeFactory
//            .create("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
//   
//            String caseTemplate = "<case name=\"CASENAME\">";
//            ExtendedIterator situationIterator = queryHandler.subjectsFor(type,
//                  situation);
//            boolean addToCases = false;
//            System.err.println("WITH SEMANTIC!!!");
//            
//            
//            ExtendedIterator objectIterator = queryHandler.subjectsFor(locatedIn, NodeFactory.create("<" + placelab + "diningroom>"));
//            
//            while(objectIterator.hasNext()){
//                  Node sensor = (Node) objectIterator.next();
//                  ExtendedIterator objectTypeIterator = queryHandler.objectsFor(sensor, type);
//                  while(objectTypeIterator.hasNext()){
//                     Node objectType = (Node) objectTypeIterator.next();
//                     if(objectType.getLocalName().equalsIgnoreCase("Sensor")){
//                        Node identifierNode = (Node) queryHandler.objectsFor(sensor, identifier).next();
//                        Node attachedObject = (Node) queryHandler.objectsFor(sensor, attachedTo).next();
//                           if(!attachedObject.getLocalName().contains("book")){
//                              System.out.print("<PL_"+identifierNode.getLiteralValue()+">"+identifierNode.getLiteralValue()+"</PL_"+identifierNode.getLiteralValue()+">");
//                           }
//                        }
//                  }
//            }
//            
//            while (situationIterator.hasNext()) {
//               Node currentSituation = (Node) situationIterator.next();
//               if(!currentSituation.getLocalName().contains("AND")){
//                  String currentCase = caseTemplate.replace("CASENAME", currentSituation
//                        + "_case");
//                  currentCase = currentCase
//                  + new StringFeature("/situation", (String) currentSituation
//                        .getLocalName().replace("_", " "));
//   
//                  //Put all relevant locations into a list.
//                  ExtendedIterator repLoc = queryHandler.objectsFor(currentSituation, locatedIn);
//                  List<String> nonRepLocs = new LinkedList<String>();
//                  while(repLoc.hasNext()){
//                     Node nonNode = (Node) repLoc.next();
//                     nonRepLocs.add(nonNode.getLocalName());
//                  }
//   
//                  ExtendedIterator identifierIterator = queryHandler.objectsFor(currentSituation, identifiedBy);
//   
//                  while (identifierIterator.hasNext()) {
//                     Node primaryFeature = (Node) identifierIterator.next();
//                     addToCases = true;
//                     ExtendedIterator featureIterator = queryHandler.objectsFor(
//                           primaryFeature, featureNode);
//                     Node f = (Node) featureIterator.next();
//                     if(!f.getLocalName().equalsIgnoreCase("time")){
//                        Node sensor = (Node) queryHandler.objectsFor(f,derivedFrom).next();
//                        Node location = (Node) queryHandler.objectsFor(sensor, locatedIn).next();
//                        for(String l:nonRepLocs){
//                           boolean ignoreFeature = false;
//                           if(!location.getLocalName().equalsIgnoreCase(l)){
//                              ignoreFeature = true;
//                           }
//                           if(ignoreFeature == false){
//                              Node ident = (Node) queryHandler.objectsFor(sensor, identifier).next();
//                              currentCase = currentCase + new StringFeature(f.getLocalName(),
//                                    (String) ident.getLiteralValue());
//                           }
//                        }
//                     }
//                  }
//   
//                  if (addToCases == true) {
//                     fw.append(currentCase + "</case>\n");
//                     System.out.println(currentCase + "</case>");
//                     addToCases = false;
//                  }
//               }
//            }
//            fw.flush();
//            fw.close();
//         } catch (IOException e1) {
//            e1.printStackTrace();
//         }
//      }

   public void createCasebaseWithoutSemantic(Model a_model) {
      FileWriter fw;
      try {
         fw = new FileWriter("without_semantic");
         QueryHandler queryHandler = a_model.queryHandler();
         Node identifier = NodeFactory
         .create("<http://ontonym.org/0.8/device#identifier>");
         Node derivedFrom = NodeFactory
         .create("<http://ontonym.org/0.8/provenance#derivedFrom>");
         Node identifiedBy = NodeFactory.create("<" + classifications
               + "identifiedBy>");
         Node featureNode = NodeFactory.create("<" + classifications
               + "feature>");
         Node situation = NodeFactory.create("<" + classifications + "Situation>");
         Node type = NodeFactory
         .create("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");

         String caseTemplate = "<case name=\"CASENAME\">";
         ExtendedIterator situationIterator = queryHandler.subjectsFor(type,
               situation);
         boolean addToCases = false;
         System.err.println("WITHOUT SEMANTIC!!!");
         while (situationIterator.hasNext()) {
            Node currentSituation = (Node) situationIterator.next();
            String currentCase = caseTemplate.replace("CASENAME", currentSituation
                  + "_case");
            currentCase = currentCase
            + new StringFeature("/situation", (String) currentSituation
                  .getLocalName().replace("_", " "));
            ExtendedIterator identifierIterator = queryHandler.objectsFor(
                  currentSituation, identifiedBy);

            while (identifierIterator.hasNext()) {
               Node featureValue = (Node) identifierIterator.next();
               //If this feature is a primary feature
               addToCases = true;
               ExtendedIterator featureIterator = queryHandler.objectsFor(
                     featureValue, featureNode);
               Node f = (Node) featureIterator.next();
               try {
                  Node resource = (Node) queryHandler.objectsFor(f,
                        derivedFrom).next();
                  Node ident = (Node) queryHandler.objectsFor(resource, identifier)
                  .next();
                  currentCase = currentCase
                  + new StringFeature(f.getLocalName(),
                        (String) ident.getLiteralValue());
               } catch (Exception e) {
                  addToCases = false;
               }
               //                  //System.out.println(currentSituation.getLocalName() + ": " + ident.getLiteralValue());
            }
            if (addToCases == true) {
               fw.append(currentCase + "</case>\n");
               System.out.println(currentCase + "</case>");
               addToCases = false;
            }
         }
         fw.flush();
         fw.close();
      } catch (IOException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
   }

   public static void printIterator(Iterator<?> i, String header) {

      System.out.println(header);

      for (int c = 0; c < header.length(); c++)

         System.out.print("=");

      System.out.println();

      if (i.hasNext()) {

         while (i.hasNext())

            System.out.println(i.next());

      }

      else

         System.out.println("<EMPTY>");

      System.out.println();

   }

}
