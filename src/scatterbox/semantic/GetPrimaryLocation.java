package scatterbox.semantic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.builtins.BaseBuiltin;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class GetPrimaryLocation extends BaseBuiltin{

   public String getName() {
      return "GetPrimaryLocation";
   }

   public void headAction(Node[] args, int length, RuleContext context) {
      if(!args[0].toString().contains("AND")){
         Node identifiedBy = NodeFactory.create("<http://ontonym.org/0.8/classifications.owl#identifiedBy>");
         Node feature = NodeFactory.create("<http://ontonym.org/0.8/classifications.owl#feature>");
         Node probability = NodeFactory.create("<http://ontonym.org/0.8/classifications.owl#probability>");
         Node derivedFrom = NodeFactory.create("<http://ontonym.org/0.8/provenance#derivedFrom>");
         Node location = NodeFactory.create("<http://ontonym.org/0.8/location#locatedIn>");
         Node type = NodeFactory.create("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");

         QueryHandler queryHandler = context.getGraph().queryHandler();

         ExtendedIterator i = queryHandler.objectsFor(args[0], identifiedBy);
         HashMap<String, Double> situationProbabilities = new HashMap<String, Double>();
         while(i.hasNext()){
            try{
               System.out.println("Gets in here");
               Node_URI n = (Node_URI) i.next();
               //Find the feature for this situation
               ExtendedIterator featureIterator = queryHandler.objectsFor(n, feature);
               Node contextNode = (Node) featureIterator.next();
               //Get the sensor from which this feature was derived.
               ExtendedIterator sensorIterator = queryHandler.objectsFor(contextNode, derivedFrom);
               Node sensorNode = (Node) sensorIterator.next();

               ExtendedIterator probabilityIterator = queryHandler.objectsFor(n, probability);
               Node nod = (Node) probabilityIterator.next();
               double probabilityValue = (Integer) nod.getLiteralValue();
               
               //Get the sensor type of this feature
               String sensorType = "";
               ExtendedIterator sensorTypeIterator = queryHandler.objectsFor(sensorNode, type);
               while(sensorTypeIterator.hasNext()){
                  //This can be either "thing" or the specified placelab type, so we must ensure
                  //that the placelab type is found each time.
                  Node_URI sensorT = (Node_URI) sensorTypeIterator.next();
                  if(sensorT.toString().contains("placelab")){
                     sensorType = sensorT.toString();
                  }
               }
               String sensorLocation = "";
               //If the sensor type is motion or OM, then find the location of this feature.
               if(sensorType.contains("MOTION") || sensorType.contains("OM") || sensorType.contains("RFID")){
                  ExtendedIterator locationIterator = queryHandler.objectsFor(sensorNode, location);
                  Node_URI locationNode = (Node_URI) locationIterator.next();
                  sensorLocation = locationNode.toString();
                  
                  if(situationProbabilities.containsKey(sensorLocation)){
                     double probCount = situationProbabilities.get(sensorLocation);
                     probCount = probCount + probabilityValue;
                     situationProbabilities.put(sensorLocation, probCount);
                  }else{
                     situationProbabilities.put(sensorLocation, probabilityValue);
                  }
               }

               ExtendedIterator locationIterator = queryHandler.objectsFor(sensorNode, location);
               Node_URI locationNode = (Node_URI) locationIterator.next();
            
            }catch(Exception e){
               System.err.println("COULD NOT FIND ELEMENT");
            }
         }

         Set<String> keys = situationProbabilities.keySet();
         Iterator<String> keyIterator = keys.iterator();
         String topLocation = "";
         double locationCount = 0;
         while(keyIterator.hasNext()){
            String currentKey = keyIterator.next(); 
            double currentCount = situationProbabilities.get(currentKey);
            if(currentCount > locationCount){
               topLocation = currentKey;
               locationCount = currentCount;
            }
         }
         Node primaryLocationProperty = NodeFactory.create("<http://ontonym.org/0.8/classifications.owl#primaryLocation>");
         Node primaryLocationNode = NodeFactory.create("<"+topLocation+">"); 
         if(locationCount>0){
            Triple primaryLocationTriple = new Triple(args[0], primaryLocationProperty, primaryLocationNode);
            context.add(primaryLocationTriple);
            System.out.println(args[0] + ": " + topLocation + ", " + locationCount);
         }
      }
   }
}