package scatterbox.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class SimilarityProcessor {

   String sensorType = "";
   String sensorID = ""; 
   String device = "";

   String filename = "cbml/similarityfromontology_high.xml";
   File outputFile = new File(filename);
   FileWriter writer;
   
   String newClassification = "<rdf:Description rdf:about=\"http://ontonym.org/0.8/classifications.owl#PL_SENSORID\">"
        +"<provenance:derivedFrom rdf:resource=\"http://ontonym.org/0.8/placelab.owl#SENSORID\"/>"
        +"<rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>"
        +"<rdf:type rdf:resource=\"http://ontonym.org/0.8/classifications.owl#classification\"/>"
        +"</rdf:Description>";


   InfModel placelabOntology;

   public static void main(String argv[]) throws IOException {
      SimilarityProcessor x = new SimilarityProcessor();
      x.loadOntology();
      x.processPlacelabXMLFile();
      //x.writer.close();
   }

   private void processPlacelabXMLFile(){
      try {
         QueryHandler queryHandler = placelabOntology.queryHandler();

         Node identifier = NodeFactory.create("<http://ontonym.org/0.8/device#identifier>");
         Node weight = NodeFactory.create("<http://ontonym.org/0.8/placelab.owl#weight>");
         Node type = NodeFactory.create("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
         Node sensor = NodeFactory.create("<http://ontonym.org/0.8/sensor#Sensor>");
         Node derivedFrom = NodeFactory.create("<http://ontonym.org/0.8/provenance#derivedFrom>");


         //       writer = new FileWriter(outputFile);
         //       
         //       File file = new File("cbml/similarity.xml");
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db;
         //
         db = dbf.newDocumentBuilder();
         Document document = db.newDocument();
         
         Element rootElement = document.createElement("case");
         rootElement.setAttribute("domain", "scatterbox");
         rootElement.setAttribute("xsi:noNamespaceSchemaLocation", "/home/pta/xml/cbmlv3.xsd");
         rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
         
         Element similarityElement = document.createElement("similarity");
         similarityElement.setAttribute("username", "default");
         document.appendChild(rootElement);

         rootElement.appendChild(similarityElement);
         
         Element situationElement = document.createElement("feature");
         situationElement.setAttribute("name", "situation");
         situationElement.setAttribute("weight", ""+1);
         Element measure = document.createElement("measure");
         measure.setAttribute("name", "scatterbox.similarity.SituationSimilarityMeasure");
         situationElement.appendChild(measure);
         
         Element locationElement = document.createElement("feature");
         locationElement.setAttribute("name", "location");
         locationElement.setAttribute("weight", ""+1);
         Element locationMeasure = document.createElement("measure");
         locationMeasure.setAttribute("name", "scatterbox.similarity.LocationSimilarityMeasure");
         locationElement.appendChild(locationMeasure);
         
         Element timeElement = document.createElement("feature");
         timeElement.setAttribute("name", "time");
         timeElement.setAttribute("weight", ""+1);
         Element exact1 = document.createElement("exact"); 
         timeElement.appendChild(exact1);
         
         similarityElement.appendChild(situationElement);
         similarityElement.appendChild(locationElement);
         similarityElement.appendChild(timeElement);
         
         
         ExtendedIterator sensorIterator = queryHandler.subjectsFor(type, sensor);
         while(sensorIterator.hasNext()){
            Node sensorNode = (Node) sensorIterator.next();
            Node identifierNode = (Node) queryHandler.objectsFor(sensorNode, identifier).next();
            String identifierString = (String) identifierNode.getLiteralValue(); 
            try{
               Node weightNode = (Node) queryHandler.objectsFor(sensorNode, weight).next();
               Node classifier = (Node) queryHandler.subjectsFor(derivedFrom, sensorNode).next();
               double weightValue = (Double) weightNode.getLiteralValue();
               String feature = classifier.getLocalName();
               
               Element featureElement = document.createElement("feature");
               featureElement.setAttribute("name", feature);
               featureElement.setAttribute("weight", ""+weightValue);
               Element exact = document.createElement("exact"); 
               featureElement.appendChild(exact);
               similarityElement.appendChild(featureElement);
            }catch(NoSuchElementException nse){
               System.err.println(newClassification.replace("SENSORID", identifierString));
            }
         }

         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         Transformer transformer = transformerFactory.newTransformer();
         DOMSource source = new DOMSource(document);
         StreamResult result =  new StreamResult(new FileOutputStream(new File(filename)));
         transformer.transform(source, result);

      } catch (ParserConfigurationException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      catch (TransformerConfigurationException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (TransformerException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (FileNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   /**
    * Load the ontology.
    */
   private void loadOntology(){
      try {    
         Reasoner pellettReasoner = PelletReasonerFactory.theInstance().create();
         FileInputStream fis = new FileInputStream("ontologies/placelab.owl");
         FileInputStream fis2 = new FileInputStream("ontologies/ammendedClassifications.owl");
         Model emptyModel = ModelFactory.createDefaultModel();
         placelabOntology = ModelFactory.createInfModel(pellettReasoner, emptyModel);
         placelabOntology.read(fis, "");
         placelabOntology.read(fis2, "");
         placelabOntology.getDeductionsModel();
      } catch (FileNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

}