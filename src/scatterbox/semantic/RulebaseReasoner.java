package scatterbox.semantic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;


public class RulebaseReasoner {

   public static void main(String[] args) {	   
      //usageWithOntModel();
      Builtin a = new SubsetOf();
      Builtin b = new SubclassOf();
      Builtin c = new CreateClassification();
      Builtin d = new GetPrimaryLocation();
      com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry.theRegistry.register(a);
      com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry.theRegistry.register(b);
      com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry.theRegistry.register(c);
      com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry.theRegistry.register(d);

      //reasonWithOWL();
      //reasonPlacelabWithRules();
      reasonWithRules();

      // usageWithDefaultModel();

   }

   public static void reasonWithOWL() {

      FileInputStream fis;
      try {
         fis = new FileInputStream("placelab.owl");
         OntModel ontModel = ModelFactory.createOntologyModel();
         ontModel.read(fis, "");
         //Reasoner OWLReasoner = ReasonerRegistry.getOWLReasoner();
         //InfModel ontModel = ModelFactory.createInfModel(OWLReasoner, model); 
         //ontModel.getDeductionsModel();
         Property p = ontModel.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
         Property loc = ontModel.createProperty("http://ontonym.org/0.8/location#locatedIn");
         RDFNode n = ontModel.createResource("http://ontonym.org/0.8/placelab.owl#Sensor");
         Resource r = ontModel.createResource("http://ontonym.org/0.8/placelab.owl#E00700001E1F7297");
         printIterator(ontModel.listObjectsOfProperty(loc), "");
         //printIterator(ontModel.listStatements(), "");
         //printSituationStructure(m);
      } catch (FileNotFoundException e) {
         System.err.println("Ontology not found.");
      }
   }
   
   public static void reasonPlacelabWithRules() {
      System.out.println("Results for placelab");
      System.out.println("----------------------------");
      System.out.println();
      //String demoURI = "http://www.dataduct.com/person.owl#";
      //String ruleString = "[rule1: (?P http://www.dataduct.com/person.owl#marriedTo ?Q),(?P http://www.dataduct.com/person.owl#hasAddress ?R) -> (?Q http://www.dataduct.com/person.owl#marriedTo http://www.dataduct.com/person.owl#Mary)]";

      List rules = Rule.rulesFromURL("file:concept.rules");
      Reasoner ruleReasoner = new GenericRuleReasoner(rules);
      Reasoner pellettReasoner = PelletReasonerFactory.theInstance().create();

      FileInputStream fis;
      try {
         //fis = new FileInputStream("../ontonym/0.8/location.owl");
         fis = new FileInputStream("../ontonym/SampleOntologies/placelab.owl");
         //OntModel ontModel = ModelFactory.createOntologyModel();
         Model emptyModel = ModelFactory.createDefaultModel();
         InfModel model = ModelFactory.createInfModel(pellettReasoner, emptyModel);
         model.read(fis, "");
         Property p = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
         RDFNode n = model.createResource("http://ontonym.org/0.8/placelab.owl#Sensor");
         Resource r = model.createResource("http://ontonym.org/0.8/placelab.owl#E00700001E1F7297");
         //printIterator(model.listObjectsOfProperty(r, p), "");
      } catch (FileNotFoundException e) {
         System.err.println("Ontology not found.");
      }
   }

   public static void reasonWithRules() {
      System.out.println("Results with Rule Reasoner");
      System.out.println("----------------------------");
      System.out.println();
      //String demoURI = "http://www.dataduct.com/person.owl#";
      //String ruleString = "[rule1: (?P http://www.dataduct.com/person.owl#marriedTo ?Q),(?P http://www.dataduct.com/person.owl#hasAddress ?R) -> (?Q http://www.dataduct.com/person.owl#marriedTo http://www.dataduct.com/person.owl#Mary)]";

      List rules = Rule.rulesFromURL("file:concept.rules");
      Reasoner ruleReasoner = new GenericRuleReasoner(rules);

      FileInputStream fis;
      FileInputStream fis1;
      Reasoner pellettReasoner = PelletReasonerFactory.theInstance().create();
      try {
         fis = new FileInputStream("ammendedClassifications.owl");
         fis1 = new FileInputStream("placelab.owl");
         Model emptyModel = ModelFactory.createDefaultModel();
         InfModel ontModel = ModelFactory.createInfModel(pellettReasoner, emptyModel);
         ontModel.read(fis, "");   
         ontModel.read(fis1,"");
         InfModel ruleModel = ModelFactory.createInfModel(ruleReasoner, ontModel);
         ruleModel.getDeductionsModel();         
//         printIterator(ruleModel.listStatements(), "http://ontonym.org/0.8/");

         
         //printSituationStructure(ruleModel);
         
         Property p1 = ontModel.createProperty("http://ontonym.org/0.8/classifications.owl#primaryFeature");
         RDFNode n = ontModel.createResource("http://ontonym.org/0.8/placelab.owl#Sensor");
         Resource r = ontModel.createResource("http://ontonym.org/0.8/classifications.owl#primaryLocation");
         printIterator(ruleModel.listSubjectsWithProperty(p1) ,"");
      } catch (FileNotFoundException e) {
         System.err.println("Ontology not found.");
      }
   }

   public static void testOntology(Model a_model){
      String SUBCLASS_QUERY = "PREFIX placelab: <http://ontonym.org/0.8/placelab.owl#> "
         + "PREFIX location: <http://ontonym.org/0.8/location#> "
         + "PREFIX rdf: <http://www.w3.org/2000/01/rdf-schema#> "
         + "PREFIX rdfns: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
         + "SELECT ?sensor WHERE{"
         + "?sensor placelab:locatedIn location:kitchen . " +
         "}";
   
      System.out.println(SUBCLASS_QUERY);
      QueryExecution customerQuery = QueryExecutionFactory.create(SUBCLASS_QUERY, a_model);
      final ResultSet resultSet = customerQuery.execSelect();

      while(resultSet.hasNext()){
         QuerySolution s = resultSet.nextSolution();
         System.out.println(s);
      }
   }
   
   public static void printSituationStructure(Model a_model){
      String SUBCLASS_QUERY = "PREFIX ontonym: <http://ontonym.org/0.8/classifications.owl#> "
         + "PREFIX rdf: <http://www.w3.org/2000/01/rdf-schema#> "
         + "PREFIX rdfns: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
         + "SELECT ?situation WHERE{"
         + "?situation ontonym:subSituation ?anotherSituation . " +
         "}";
   
      System.out.println(SUBCLASS_QUERY);
      QueryExecution customerQuery = QueryExecutionFactory.create(SUBCLASS_QUERY, a_model);
      final ResultSet resultSet = customerQuery.execSelect();

      while(resultSet.hasNext()){
         QuerySolution s = resultSet.nextSolution();
         System.out.println(s);
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
