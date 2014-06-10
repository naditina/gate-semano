package semano.ontologyowl;

import org.apache.log4j.Logger;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.more.MOReReasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import uk.ac.manchester.cs.jfact.JFactFactory;
import au.csiro.snorocket.owlapi.SnorocketReasonerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class Reasoning {

  private static final Logger logger = Logger
      .getLogger(Reasoning.class);

public enum Reasoner {
  pellet, hermit, factPP, elk, more, snowroket;
}


public static OWLReasoner getReasoner(OWLOntology o, Reasoner reasoner) {
  if (reasoner.equals(Reasoner.pellet))
    return  PelletReasonerFactory.getInstance()
        .createReasoner(o);
  else if (reasoner.equals(Reasoner.hermit))
    return new org.semanticweb.HermiT.Reasoner.ReasonerFactory().createReasoner(o);
  else if (reasoner.equals(Reasoner.factPP))
    return new JFactFactory().createReasoner(o);

  else if (reasoner.equals(Reasoner.more))
    return new MOReReasoner(o);
  else if (reasoner.equals(Reasoner.elk))
    return new ElkReasonerFactory().createReasoner(o);
  else{
    return new SnorocketReasonerFactory().createReasoner(o);
    }
}



  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
