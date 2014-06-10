/**
 * 
 */
package semano.ontoviewer;


/**
 * @author naddi
 *
 */
public class OntologyAnnotator {
  
  OntologyViewer oviewer;
  String text="";

  public OntologyAnnotator(OntologyViewer ov) {
    oviewer=ov;
    text =oviewer.getDocument().getContent().toString();
  }

  public void autoAnnotate() {
    String text =oviewer.getDocument().getContent().toString();
    
  }


  public void annotateWithEntityNames(){
//    for(OClass cl:ontologyTreePanel.getCurrentOntology().getOClasses(false)){
//      cl.getName();
//    }
  }
  
}
