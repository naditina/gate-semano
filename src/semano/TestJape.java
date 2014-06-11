package semano;

import gate.Annotation;
import gate.Factory;
import gate.jape.JapeException;
import gate.util.InvalidOffsetException;

public class TestJape {
  // private gate.jape.ActionContext ctx;
  public java.lang.String ruleName() {
    return "myrule";
  }

  public java.lang.String phaseName() {
    return "mpo";
  }

  // public void setActionContext(gate.jape.ActionContext ac) { ctx =
  // ac; }
  // public gate.jape.ActionContext getActionContext() { return ctx; }
  public void doit(gate.Document doc,
          java.util.Map<java.lang.String, gate.AnnotationSet> bindings,
          gate.AnnotationSet inputAS, gate.AnnotationSet outputAS,
          gate.creole.ontology.Ontology ontology)
          throws gate.jape.JapeException {

    Annotation domainAnnotation = null, rangeAnnotation = null;
    gate.AnnotationSet annotsDomain = (gate.AnnotationSet)bindings.get("domain");
    if(annotsDomain != null && !annotsDomain.isEmpty()) {
      if(annotsDomain.size() > 1) System.exit(1);
      domainAnnotation = annotsDomain.iterator().next();
      System.out.println("Domain annotation: " + domainAnnotation);
    }
    gate.AnnotationSet annotsRange = (gate.AnnotationSet)bindings.get("range");
    if(annotsRange != null && !annotsRange.isEmpty()) {
      if(annotsRange.size() > 1) System.exit(1);
      rangeAnnotation = annotsRange.iterator().next();
      System.out.println("Range annotation: " + rangeAnnotation);
    }

    if(domainAnnotation != null && rangeAnnotation != null) {
      gate.AnnotationSet relationAnnots = (gate.AnnotationSet)bindings
              .get("binding");

      gate.FeatureMap features = Factory.newFeatureMap();
      features.put("ontology", "sdd");
      features.put("autoannotation", "true");
      features.put("domain", domainAnnotation.getId());
      features.put("range", rangeAnnotation.getId());
      features.put("class", "class");
      features.put("language", "en");
      features.put("type", "test");

      // create the new annotation
      try {
        outputAS.add(relationAnnots.firstNode().getOffset(), relationAnnots
                .lastNode().getOffset(), "Mention", features);
      }
      catch(InvalidOffsetException e) {
        throw new JapeException(e);
      }
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
