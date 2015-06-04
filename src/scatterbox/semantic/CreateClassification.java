package scatterbox.semantic;

import org.joda.time.DateTime;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.builtins.BaseBuiltin;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class CreateClassification extends BaseBuiltin{

   Node type = NodeFactory.create("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
   Node classification = NodeFactory.create("<http://ontonym.org/0.8/classifications.owl#classification>");
   Node classifiedValue = NodeFactory.create("<http://ontonym.org/0.8/classifications.owl#classifiedValue>");
   Node createdAt = NodeFactory.create("<http://ontonym.org/0.8/classifications.owl#createdAt>");
   Node derivedFrom = NodeFactory.create("<http://ontonym.org/0.8/provenance#derivedFrom>");
   Node observedBy = NodeFactory.create("<http://ontonym.org/0.8/sensor#observedBy>");
   Node instantEvent = NodeFactory.create("<http://ontonym.org/0.8/event#InstantEvent>");
   Node readingValue = NodeFactory.create("<http://ontonym.org/0.8/classifications.owl#readingValue>");

   public String getName() {
      // TODO Auto-generated method stub
      return "createClassification";
   }

   public void headAction(Node[] args, int length, RuleContext context) {
      InfGraph ont = context.getGraph();
      Node newClassification = NodeFactory.create("<http://ontonym.org/0.8/classifications.owl#"+new DateTime().getMillis()+">");
      ont.add(new Triple(newClassification, derivedFrom, args[0]));
      ont.add(new Triple(newClassification, type, classification));
      ont.add(new Triple(newClassification, createdAt, NodeFactory.nowAsDateTime()));
      ont.add(new Triple(newClassification, classifiedValue, args[1]));
   }
}
