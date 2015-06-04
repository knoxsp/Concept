package scatterbox.semantic;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.builtins.BaseBuiltin;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class SubsetOf extends BaseBuiltin{

   Node type = NodeFactory.create("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
   Node identified_by = NodeFactory.create("<http://ontonym.org/0.8/classifications.owl#identifiedBy>");
   Node associated_with = NodeFactory.create("<http://ontonym.org/0.8/classifications.owl#associatedWith>");

   public String getName() {
      // TODO Auto-generated method stub
      return "SubsetOf";
   }

   public boolean bodyCall(Node[] args, int length, RuleContext context) {
      System.out.println("subset of");
      //Allows for data to be extracted from the ontology
      QueryHandler handler = context.getGraph().queryHandler();
      //Get everything which this element directly depends on
      ExtendedIterator identifiersA = handler.objectsFor(args[0], identified_by);
      ExtendedIterator identifiersB = handler.objectsFor(args[1], identified_by);

      int sizeOfA = 0;
      int sizeOfB = 0;
      ExtendedIterator Acopy = identifiersA;
      ExtendedIterator Bcopy = identifiersB;
      while(Acopy.hasNext()){
         sizeOfA++;
         Acopy.next();
      }
      while(Bcopy.hasNext()){
         sizeOfB++;
         Bcopy.next();
      }

      if(sizeOfA>0 && sizeOfB>0 && sizeOfA<sizeOfB){
         //Get each situation in turn
         while(identifiersB.hasNext()){
            Node_URI situationB = (Node_URI) identifiersB.next();
            //Makes sure that this is reset each iteration.
            ExtendedIterator tempA = identifiersA;
            Node_URI situationA;
            //For each situation, check to see if the identifiers are the same.
            while(tempA.hasNext()){
               situationA = (Node_URI) identifiersA.next();
               if(!situationA.matches(situationB)){
                  return false;
               }
            }
         }
      }else{
         //If one or both situations have no identifiers associated with them.
         return false;
      }

      if(sizeOfA == sizeOfB){
         return false;
      }
      

//      for(int i=0;i<args.length;i++){
//         System.out.println("ARGS["+i+"]: "+args[i]);
//      }      
      return true;
   }

}
