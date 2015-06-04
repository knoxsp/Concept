package scatterbox.semantic;

import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.builtins.BaseBuiltin;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class IdentifyOutlyingLocations extends BaseBuiltin {

   public String getName() {
      return "IdentifyOutlyingLocations";
   }

   public void headAction(Node[] args, int length, RuleContext context) {
      if (!args[0].toString().contains("AND")) {
         Node identifiedBy = NodeFactory
         .create("<http://ontonym.org/0.8/classifications.owl#identifiedBy>");
         Node feature = NodeFactory
         .create("<http://ontonym.org/0.8/classifications.owl#feature>");
         Node derivedFrom = NodeFactory
         .create("<http://ontonym.org/0.8/provenance#derivedFrom>");
         Node location = NodeFactory
         .create("<http://ontonym.org/0.8/location#locatedIn>");
         Node type = NodeFactory
         .create("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");

         QueryHandler queryHandler = context.getGraph().queryHandler();

         ExtendedIterator i = queryHandler.objectsFor(args[0], identifiedBy);
         //The list of locations associated with the current situation
         List<String> locations = new LinkedList<String>();

         List<String[]> locationAndType = new LinkedList<String[]>();

         while (i.hasNext()) {
            //Get the current FeatureProbability 
            Node_URI featureProbNode = (Node_URI) i.next();
            //Find the feature for this feature probability node
            ExtendedIterator featureIterator = queryHandler.objectsFor(
                  featureProbNode, feature);
            Node contextNode = (Node) featureIterator.next();

            if (!contextNode.getLocalName().equalsIgnoreCase("time")) {
               //Get the sensor from which this feature was derived.
               ExtendedIterator sensorIterator = queryHandler.objectsFor(
                     contextNode, derivedFrom);
               Node sensorNode = (Node) sensorIterator.next();

               //Get the sensor type associated with the current feature probability
               String sensorType = "";
               ExtendedIterator sensorTypeIterator = queryHandler.objectsFor(
                     sensorNode, type);
               while (sensorTypeIterator.hasNext()) {
                  Node_URI sensorT = (Node_URI) sensorTypeIterator.next();
                  //Identify the correct type, in this case of "placelab" type
                  if (sensorT.toString().contains("placelab")) {
                     sensorType = sensorT.getLocalName();
                  }
               }

               //Get the location of this sensor and add it to the list of locations
               ExtendedIterator locationIterator = queryHandler.objectsFor(
                     sensorNode, location);
               Node_URI locationNode = (Node_URI) locationIterator.next();
               String sensorLocation = locationNode.toString();

               //Only add a situation once.
               boolean seenBefore = false;
               for (String loc : locations) {
                  if (loc.equalsIgnoreCase(sensorLocation)) {
                     seenBefore = true;
                  }
               }
               //Add a link between the current location and the sensor in that location 
               locationAndType.add(new String[] { sensorLocation, sensorType });

               if (seenBefore != true) {
                  locations.add(sensorLocation);
               }
            }
         }//end main while

         //For each location, check the sensor types associated with it. If 
         //the sensor types are ONLY current and/or light, then mark this location as an outlier.
         for (String loc : locations) {
            String types = "";
            for (String[] locTyp : locationAndType) {
               if (locTyp[0].equalsIgnoreCase(loc)) {
                  if(!types.contains(locTyp[1])){
                     types = types + locTyp[1];
                  }
               }
            }

            //If there is only current, only light, or only a combination of the two, then mark the location as a "nonRepresentativeLocation"
            if (types.equalsIgnoreCase("CURRENT")
                  || types.equalsIgnoreCase("LIGHT")
                  || (types.contains("CURRENT") && types.contains("LIGHT") && types
                        .length() == 12)) {
               Node outlier = NodeFactory.create("<http://ontonym.org/0.8/classifications.owl#nonRepresentativeLocation>");
               Node outlyingLocation = NodeFactory.create("<" + loc + ">");
               Triple outlyingNodeTriple = new Triple(args[0], outlier, outlyingLocation);
               context.add(outlyingNodeTriple);
               //TODO make sure this works
                   //           System.out.println(args[0].getLocalName());
                   //           System.out.println("Outlying Location: " + outlyingLocation);
            }
         }
      }
   }
}