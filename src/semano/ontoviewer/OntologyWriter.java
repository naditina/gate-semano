package semano.ontoviewer;

import gate.Annotation;
import gate.AnnotationSet;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OResource;
import gate.creole.ontology.OURI;
import gate.creole.ontology.ObjectProperty;
import gate.creole.ontology.Ontology;

import java.util.HashSet;
import java.util.Set;

import semano.ontologyowl.AbstractOWLOntology;
import semano.ontologyowl.OURIImpl;
import semano.ontologyowl.impl.OWLOntology;
import semano.util.Settings;


public class OntologyWriter {

  private static boolean createNewAnnotation = true;

  OntologyViewer ontoViewer;

  public OntologyWriter(OntologyViewer ov) {
    this.ontoViewer = ov;
  }

  /**
   * @return the OntologyViewer
   */
  public OntologyViewer getOntologyViewer() {
    return ontoViewer;
  }

  /**
   * @param OntologyViewer the OntologyViewer to set
   */
  public void setOntologyViewer(OntologyViewer ontologyTreePanel) {
    this.ontoViewer = ontologyTreePanel;
  }

  /**
   * @return the currentOntology
   */
  public Ontology getCurrentOntology() {
    return ontoViewer.getCurrentOntology();
  }

  /**
   * NN: writing instance data to ontology
   */

  @SuppressWarnings("deprecation")
  public void writeOntologyAnnotationsToFile() {
    
    // ArrayList<String> lines = new ArrayList<String>();
    // // and so add it to the annotationSetName
    AnnotationSet annotationSet = ontoViewer.getAnnotationSet();
    // in order to refresh the views later we gather all new data
    HashSet<OntologyAnnotation> addedAnnotations = new HashSet<OntologyAnnotation>();
    HashSet<OResource> addedResources = new HashSet<OResource>();
    for(Annotation aannotation : annotationSet) {

      if(aannotation == null || aannotation.getType() == null) {
      }
      else if(aannotation.getType().equals(Settings.DEFAULT_ANNOTATION_TYPE)) {
        OntologyAnnotation ontologyAnnotation= new OntologyAnnotation(aannotation);
        if(ontologyAnnotation.isManuallySetAnnotation()) {
          if(Settings.NEWENTITY.toString().equals(OntologyAnnotation.getAnnotationType(ontologyAnnotation)
                 )) {
            // create a new class or property:

            // 1.convert the string into an acceptable entity name
            String entityName = convertToProperEntityName(ontologyAnnotation.getValue());
            OResource source = ontologyAnnotation.getResource(getCurrentOntology());
            OURI superResource = source.getURI();
            if(source == null || superResource == null) {
              System.err
                      .println("aparrently there is a new Entity annotation without an existing superconcept or property. Note that currently you cannot create hierarchies of new Entities, just sub-entities. Concerned new entity: "
                              + entityName
                              + " try to write to ontology one more time, then this entity will also be written");
              continue;
            }
            String ontologyUri = OntologyAnnotation.getOntologyUri(source);
            if(!ontologyUri.endsWith("/")) ontologyUri += "#";
            OURI aURI = new OURIImpl(ontologyUri + entityName);
            OResource newEntity = null;
            
            // is it a class or a property?
            if(ontologyAnnotation.isSourceAClass()) {
              // 2. add class to ontology
              OClass newClas = ((OWLOntology)ontoViewer.getCurrentOntology())
                      .addOClass(aURI, superResource.toString());
              // 3.get the right class and add the new class with a
              // subclass axiom
              // ((OClass)oa.getResource(getCurrentOntology())).addSubClass(newClas);
              // subclass axiom will now be created automatically
              newEntity = newClas;
            }
            else {
              // 2. add property to ontology
              String domainClString = OntologyAnnotation.getDomainClass(ontologyAnnotation);
              Set<OClass> domains = getOClasses(domainClString);
              String rangeClString = OntologyAnnotation.getRangeClass(ontologyAnnotation);
              Set<OClass> ranges = getOClasses(rangeClString);
              ObjectProperty newProperty = ((OWLOntology)ontoViewer
                      .getCurrentOntology()).addObjectProperty(aURI,
                      superResource, domains, ranges);
              newEntity = newProperty;
            }
            if(newEntity != null) {
              addedResources.add(newEntity);
              // create an annotation of type synonym
              if(createNewAnnotation) {
                // we write the annotation into the new entity, but we
                // dont permanently change the annotation:
                aannotation.removeAnnotationListener(ontoViewer);
                ontologyAnnotation.setResource(newEntity);
                ontoViewer.annotationManager.addAnnotationToOntology(ontologyAnnotation);
                ontologyAnnotation.setResource(source);
                aannotation.addAnnotationListener(ontoViewer);
              }
            }
          }
          else {
            // newEntity annotations are processed first. if it is not a
            // new class annotation, then save it for later
            addedAnnotations.add(ontologyAnnotation);

          }

        }
      }
    }
    for(OResource resource : addedResources) {
      ontoViewer.getCurrentOntology().fireOntologyResourceAdded(resource);
    }
    // now all usual annotations (not new entity)
    if(!ontoViewer.showWriteOnlyNewEntities) {
      for(OntologyAnnotation a : addedAnnotations) {
        ontoViewer.annotationManager.addAnnotationToOntology(a);
      }
    }
    ((AbstractOWLOntology)getCurrentOntology()).writeOntologyData();

    // refresh views
    // for(OntologyAnnotation oa:addedAnnotations){
    // AnnotationProperty annotationProperty =
    // oa.getAnnotationProperty(getCurrentOntology());
    // if(annotationProperty!=null){
    // ontoViewer.getCurrentOntology().fireResourcePropertyValueChanged(oa.getResource(getCurrentOntology()),
    // annotationProperty, new Literal(oa.getValue()),
    // OConstants.ANNOTATION_PROPERTY_VALUE_ADDED_EVENT);
    // } else{
    // System.err.println(" annotationProperty of the annotation was not found! "+oa.getAnnotationType());
    // }
    // }
  }
  /**
   * @param classString
   * @return
   */
  private Set<OClass> getOClasses(String classString) {
    Set<OClass> oclasses = ((OWLOntology)ontoViewer.getCurrentOntology())
            .getOClassesByName(classString);
    return oclasses;
  }

  // TODO NN only a-zA-Z_ should be possible
  private String convertToProperEntityName(String text) {
    return text.replaceAll("-", "_")
            .replaceAll(" ", "_")
            .replaceAll("!", "")
            .replaceAll("\"", "")
            .replaceAll("'", "")
            .replaceAll("+", "")
            .replaceAll(":", "")
            .replaceAll(";", "")
            .replaceAll(".", "")
            .replaceAll(",", "")
            .replaceAll("*", "")
            .replaceAll("#", "")
            .replaceAll("�", "")
            .replaceAll("$", "")
            .replaceAll("%", "")
            .replaceAll("&", "")
            .replaceAll("/", "")
            .replaceAll("(", "")
            .replaceAll(")", "")
            .replaceAll("=", "")
            .replaceAll("?", "")
            .replaceAll("<", "")
            .replaceAll(">", "");
  }
}
