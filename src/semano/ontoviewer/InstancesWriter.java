package semano.ontoviewer;

import gate.Annotation;
import gate.AnnotationSet;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants.Closure;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.OResource;
import gate.creole.ontology.OURI;
import gate.creole.ontology.Ontology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semano.ontologyowl.OURIImpl;
import semano.ontologyowl.impl.Property;
import semano.util.OntologyUtil;
import semano.util.Settings;



public class InstancesWriter {

  OntologyViewer ontoViewer;

  public InstancesWriter(OntologyViewer ov) {
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

  public void writeInstacnesToFile() {

    // 1. create all class instances and remember the names
    // 2. set relations between instances
    // 3. wirte data 

    
    HashMap<Annotation, OInstance> annotation2Instance = new  HashMap<Annotation, OInstance>();

    if(!Settings.WRITE_RELATIONS_ONLY){
      annotation2Instance=addInstances();
    }
    addPropeties(annotation2Instance);
    ((semano.ontologyowl.impl.OWLOntology)getCurrentOntology())
            .writeInstanceData(ontoViewer.getDocument().getName());

  }

  private HashMap<Annotation, OInstance> addInstances() {
    HashMap<Annotation, OInstance> result = new HashMap<Annotation, OInstance>();
    AnnotationSet set = ontoViewer.getAnnotationSet();    
    for(Annotation a : set) {
      OInstance instance = writeInstance(a);
      if(instance!=null)
        result.put(a, instance);
    }
    return result;
  }

  /**
   * @param result
   * @param docName
   * @param a
   * @return 
   */
  public OInstance writeInstance(
          Annotation a) {
    OInstance result=null;
    String docName = ontoViewer.getDocument().getName();
    if(a == null || a.getType() == null) {
      System.err.println("annotation is null or has no type!");
    }
    else if(a.getType().equals(Settings.DEFAULT_ANNOTATION_TYPE)) {
      OntologyAnnotation oa = new OntologyAnnotation(a);
      if(!OntologyAnnotation.isAntipattern(oa)) {
        if(!Settings.NEWENTITY.toString().equals(OntologyAnnotation.getAnnotationType(oa))){
          // create an instance of the class
          // 1.convert the string into an acceptable entity name
          String docExtension = docName.substring(0,3);
          if(Settings.USE_FULL_DOC_NAME){
            docExtension=docName;
          }
          String entityName = OntologyUtil.convertToProperUriString(oa.getValue()+"_"+docExtension);
          OResource source = oa.getResource(getCurrentOntology());
          if(source == null || source.getURI() == null) {
            System.err
                    .println("aparrently there is a new Entity annotation without an existing concept or property. Note that currently you cannot create hierarchies of new Entities, just sub-entities. Concerned new entity: "
                            + entityName
                            + " try to write to ontology one more time, then this entity will also be written");
            return null;
          }
          OResource newEntity = null;
          if(oa.isSourceAClass()) {
            OClass classs = (OClass)source;
            Set<OClass> allClasses=new HashSet<OClass>();
            allClasses.add(classs);
            if(Settings.MATERIALIZE){
              allClasses.addAll(classs.getSuperClasses(Closure.TRANSITIVE_CLOSURE));                
            }
            // 2. add class to ontology
          String ontologyUri = OntologyAnnotation.getOntologyUri(source);
          if(!ontologyUri.endsWith("/")) ontologyUri += "#";
            OURI aURI = new OURIImpl(ontologyUri + entityName);
            for(OClass clas:allClasses){              
              if(Settings.WRITE_QUADS) {
                  String comment = constructComment(oa);
                addQuad(aURI.toString(),Settings.URI_TYPE,clas.getURI().toString(),comment);
              }
                OInstance newInstance = ((semano.ontologyowl.impl.OWLOntology)ontoViewer
                      .getCurrentOntology()).addOInstance(aURI, clas);
              result= newInstance;
              
            }
          }
        }
      }
    }
    return result;
  }

  private void addQuad(String sub, String predicate, String obj, String comment1) {
    String comment=comment1.replaceAll("[^a-zA-Z0-9\\s-;\\._<>/]", "");
    String quad[]=new String[4];
    quad[0]="<"+sub+">";
    quad[1]="<"+predicate+">";
    quad[2]="<"+obj+">";
    quad[3]=comment;
    if(Settings.ENCODE){
      String uri = comment;
      uri=java.net.URLEncoder.encode(comment);
    try{
//      System.out.println(java.net.URLDecoder.decode(uri));
    }catch(IllegalArgumentException e){
      comment.replace("%", "P");
      uri=java.net.URLEncoder.encode(comment);
    }

    quad[3]="<"+Settings.PREFIX_COMMENT+uri+">";
    }
    ((semano.ontologyowl.impl.OWLOntology)ontoViewer
            .getCurrentOntology()).addQuad(quad);
    
  }

  @SuppressWarnings("deprecation")
  private void addPropeties(HashMap<Annotation, OInstance> annotation2Instance) {
    AnnotationSet set = ontoViewer.getAnnotationSet();
    for(Annotation a : set) {

      if(a == null || a.getType() == null) {
        System.err.println("annotation is null or has no type!");
      }
      else if(a.getType().equals(Settings.DEFAULT_ANNOTATION_TYPE)) {
        OntologyAnnotation oa = new OntologyAnnotation(a);
        if(!OntologyAnnotation.isAntipattern(oa)) {
          if(!Settings.NEWENTITY.toString().equals(OntologyAnnotation.getAnnotationType(oa))) {
            // is it a class or a property?
            if(OntologyAnnotation.isSourceAProperty(oa)) {
              // //2. add property to ontology
              OResource source = oa.getResource(getCurrentOntology());
              if(source == null || source.getURI() == null) {
                System.err
                        .println("aparrently there is an Entity annotation without an existing concept or property.");
                continue;
              }
              

              Annotation dom = oa.getDomainAnnotation(ontoViewer.annotationManager);
              Annotation ran = oa.getRangeAnnotation(ontoViewer.annotationManager);
              if(dom==null ||ran==null) {
                  continue;
              }
              OInstance i1 = annotation2Instance.get(dom);
              OInstance i2 = annotation2Instance.get(ran);
              if( i1==null) {
                if(Settings.WRITE_RELATIONS_ONLY){
                  i1 = writeInstance(dom);
                  if(i1!=null)
                    annotation2Instance.put(dom, i1);
                }
                if( i1==null) {
                  continue;
                }
              }
              if( i2==null) {
                if(Settings.WRITE_RELATIONS_ONLY){
                  i2 = writeInstance(ran);
                  if(i2!=null)
                    annotation2Instance.put(ran, i2);
                }
                if( i2==null) {
                  continue;
                }
              }
              if(Settings.WRITE_QUADS){
                String comment = constructComment(oa, dom, ran);
                addQuad(i1.getOURI().toString(), source.getURI().toString(),i2.getOURI().toString(),comment);
                

                if(Settings.MATERIALIZE) {
                  Property[] sources = ((semano.ontologyowl.impl.OWLOntology)ontoViewer
                          .getCurrentOntology()).getInverses(source.getURI()
                          .toString());
                  if(sources != null) {
                    for(Property source2 : sources) {
                      if(source.getURI() != null) {
                        addQuad(i2.getOURI().toString(), source2.getUri()
                                .toString(), i1.getOURI().toString(), comment);
                      }
                    }
                  }

                }
                
              }else{
                ((semano.ontologyowl.impl.OWLOntology)ontoViewer
                        .getCurrentOntology()).addInstanceRelation(i1, i2,
                        source.getURI());
              }

                
              
            }
          }
        }
      }
    }
  }

  private String constructComment(OntologyAnnotation oa, Annotation dom,
          Annotation ran) {
    
    String docName = ontoViewer.getDocument().getName();
    String comment="<b>"+docName+"</b> "+oa.getStartNode().getOffset().intValue() +"<br/>";
    Annotation first,last;
    if(dom.getStartNode().getOffset()<ran.getStartNode().getOffset()){
      //domain is before range
      first=dom;last=ran;
    }else{
      first=ran;last=dom;
    }
    boolean overlapping=false;
    if(last.getStartNode().getOffset()<first.getEndNode().getOffset()){
      overlapping=true;
    }    
    
    
    int startOffset = first.getStartNode().getOffset().intValue()-Settings.MATCHING_WINDOW;
    if(startOffset<0){
      startOffset=0;
    }
    int endOffset = last.getEndNode().getOffset().intValue()+Settings.MATCHING_WINDOW;
    if(endOffset>ontoViewer.getDocument().getContent().toString().length()-1){
      endOffset=ontoViewer.getDocument().getContent().toString().length()-1;
    }
    comment+=ontoViewer.getDocument().getContent().toString().substring(startOffset, first.getStartNode().getOffset().intValue());
    if(!overlapping){
      comment+="<b>"+ontoViewer.getDocument().getContent().toString().substring(first.getStartNode().getOffset().intValue(), first.getEndNode().getOffset().intValue())+"</b>";
      comment+=ontoViewer.getDocument().getContent().toString().substring(first.getEndNode().getOffset().intValue(),last.getStartNode().getOffset().intValue());
      comment+="<b>"+ontoViewer.getDocument().getContent().toString().substring(last.getStartNode().getOffset().intValue(), last.getEndNode().getOffset().intValue())+"</b>";
    }else{
      comment+="<b>"+ontoViewer.getDocument().getContent().toString().substring(first.getStartNode().getOffset().intValue(), last.getEndNode().getOffset().intValue())+"</b>";
    }
    comment+=ontoViewer.getDocument().getContent().toString().substring(last.getEndNode().getOffset().intValue(),endOffset);
//    String text = ontoViewer.getDocument().getContent().toString().substring(startOffset, endOffset);
//    comment+=text;
    return comment;
  }

  /**
   * @param oa
   * @return
   */
  public String constructComment(OntologyAnnotation oa) {
    String docName = ontoViewer.getDocument().getName();
    String comment="<b>"+docName+"</b> "+oa.getStartNode().getOffset().intValue() +"<br/>";
    int startOffset = oa.getStartNode().getOffset().intValue()-Settings.COMMENT_WINDOW;
    if(startOffset<0){
      startOffset=0;
    }
    int endOffset = oa.getEndNode().getOffset().intValue()+Settings.COMMENT_WINDOW;
    if(endOffset>ontoViewer.getDocument().getContent().toString().length()-1){
      endOffset=ontoViewer.getDocument().getContent().toString().length()-1;
    }
    comment+=ontoViewer.getDocument().getContent().toString().substring(startOffset, oa.getStartNode().getOffset().intValue());
    comment+="<b>"+oa.getValue()+"</b>";                
    comment+=ontoViewer.getDocument().getContent().toString().substring(oa.getEndNode().getOffset().intValue(),endOffset);
    return comment;
  }





  public static void main(String value[]) {
    String res = "";
//    String val = "<b>the title of the text</b>\\<offset\\><br/>Hier kommt dann der Text mit Annotationen <b>fett</b> gekennzeichnet.";
//    res = Uri.escapeDisallowedChars(val);
    String var="<http://www.test.de/%3cb%3eAdv%20Mater%202007%2019%202224%20-%20ITO%20Synthesis-a.pdf_00016%3c/b%3e%20980%3cbr/%3eved.%20[2,10]%20When%20aiming%20at%20transparent%20electrodes%20on%20flexible%20or%20heat%20%20sensitive%20substrates%20(e.g.,%20pla";
    res = java.net.URLDecoder.decode(var);
    System.out.println(res);
    // return res;
  }



}
