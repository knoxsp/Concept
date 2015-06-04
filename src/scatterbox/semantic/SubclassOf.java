package scatterbox.semantic;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.builtins.BaseBuiltin;
import com.hp.hpl.jena.sparql.util.NodeFactory;

public class SubclassOf extends BaseBuiltin{

   public String getName() {
      // TODO Auto-generated method stub
      return "SubclassOf";
   }

   public void headAction(Node[] args, int length, RuleContext context) {
      Node subSituationOf = NodeFactory.create("<http://ontonym.org/0.8/classifications.owl#subSituationOf>");
     
      context.getGraph().add(new Triple(args[0], subSituationOf, args[1]));

   }
}
