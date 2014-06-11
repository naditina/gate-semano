/**
 * 
 */
package semano.ontoviewer;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.creole.ANNIEConstants;
import gate.creole.ontology.OResource;
import gate.creole.ontology.ObjectProperty;
import gate.creole.ontology.Ontology;
import gate.util.GateRuntimeException;

import java.awt.HeadlessException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import semano.ontologyowl.AbstractOWLOntology;
import semano.ontologyowl.AnnotationValue;
import semano.util.OntologyUtil;
import semano.util.Settings;

/**
 * 
 * Dont match and look, refactor this out ...
 * AnnotationRegistry - collection of the annotations
 * PatternMatcherClassses... , : related to patern matching into these classes
 * 
 * @author naddi
 * 
 */
public class AnnotationStore{


  Logger logger = Logger.getLogger(this.getClass().getName());

  public OntologyViewer ontoViewer;

  /**
   * AnnotationMap which stores all the relevant annotations (i.e.
   * annotation with class feature) in the following way className -->
   * Set (which contains all the annotations) </p>
   */
  private LinkedHashMap<String, LinkedHashSet<Annotation>> EntityName2AnnotationList = new LinkedHashMap<String, LinkedHashSet<Annotation>>();

  /**
   * Each Annotation Set is given a unique ID, which is used later in
   * the annotationsID2ASID map.
   */
  LinkedHashMap<Integer, String> asID2ASName = new LinkedHashMap<Integer, String>();

  /**
   * The highest Annotation Set ID
   */
  private int highestASID = 0;

  /**
   * We store the ID of each annotation and the ID of annotation set the
   * annotation belongs to.
   */
  Map<Integer, Integer> annotationsID2ASID = new LinkedHashMap<Integer, Integer>();

  
  public Map<String, ? extends Set<Annotation>> getEntityNameToAnnotationsMap(){
    return getEntityName2AnnotationList();
  }
  
  public AnnotationStore(OntologyViewer ontoViewer) {
    this.ontoViewer = ontoViewer;
  }

  /** ReIntializes all Maps used for local uses */
  protected void initLocalData() {
    getEntityName2AnnotationList().clear();
    asID2ASName.clear();
    Map<String, LinkedHashSet<Annotation>> temp = getAnnotationsWithOResourceFeature(
            ontoViewer.getDocument().getAnnotations(), null);
    if(temp != null) {
      getEntityName2AnnotationList().putAll(temp);
    }

    Map<String, AnnotationSet> annotSetMap = ontoViewer.getDocument()
            .getNamedAnnotationSets();
    if(annotSetMap != null) {
      java.util.List<String> setNames = new ArrayList<String>(
              annotSetMap.keySet());
      Collections.sort(setNames);
      Iterator<String> setsIter = setNames.iterator();
      while(setsIter.hasNext()) {
        temp = getAnnotationsWithOResourceFeature(ontoViewer.getDocument()
                .getAnnotations(setsIter.next()), null);
        if(temp != null) {
          Iterator<String> keyIter = temp.keySet().iterator();
          while(keyIter.hasNext()) {
            String key = keyIter.next();
            if(getEntityName2AnnotationList().containsKey(key)
                    && temp.get(key) != null) {
              getEntityName2AnnotationList().get(key).addAll(temp.get(key));
              continue;
            }
            else {
              getEntityName2AnnotationList().put(key, temp.get(key));
            }
          }
        }
      }
    }

  }

  /**
   * This method iterates through annotations and find out the ones with
   * ontology and class features and returns the map<String,
   * ArrayList<Annotation>>
   */
  private LinkedHashMap<String, LinkedHashSet<Annotation>> getAnnotationsWithOResourceFeature(
          AnnotationSet set, String aClassName) {
    Integer setId = null;
    if(asID2ASName.values() != null
            && asID2ASName.values().contains(set.getName())) {
      Iterator<Integer> iter = asID2ASName.keySet().iterator();
      while(iter.hasNext()) {
        Integer tempId = iter.next();
        String setString = asID2ASName.get(tempId);
        if(setString != null && setString.equals(set.getName())) {
          setId = tempId;
          break;
        }
      }
    }
    else {
      setId = new Integer(highestASID);
      highestASID++;
      asID2ASName.put(setId, set.getName());
    }

    LinkedHashMap<String, LinkedHashSet<Annotation>> subMap = new LinkedHashMap<String, LinkedHashSet<Annotation>>();
    // now lets find out all the annotations which have ontology as a
    // feature
    Iterator<Annotation> setIter = set.iterator();

    while(setIter.hasNext()) {
      Annotation currentAnnot = setIter.next();
      currentAnnot.removeAnnotationListener(ontoViewer);
      currentAnnot.addAnnotationListener(ontoViewer);
      String aName = OntologyAnnotation.getSourceFeatureValue(currentAnnot);
      if(aName == null || aName.isEmpty()) {
        continue;
      }

      if(aClassName == null || aName.equals(aClassName)) {
        if(subMap.containsKey(aName)) {
          LinkedHashSet<Annotation> annotList = subMap.get(aName);
          annotList.add(currentAnnot);
          subMap.put(aName, annotList);
        }
        else {
          LinkedHashSet<Annotation> annotList = new LinkedHashSet<Annotation>();
          annotList.add(currentAnnot);
          subMap.put(aName, annotList);
        }
      }
      annotationsID2ASID.put(currentAnnot.getId(), setId);
    }
    return subMap;
  }

  /**
   * @param set annotation set to search in
   * @param wantedName name of the required annotated entity
   * @return list of annotations
   */
  public HashSet<Annotation> getAnnotationsWithResource(AnnotationSet set,
          String wantedName) {
    HashSet<Annotation> result = new HashSet<Annotation>();
    if(wantedName != null) {
      Iterator<Annotation> setIter = set.iterator();
      while(setIter.hasNext()) {
        Annotation currentAnnot = setIter.next();
        String annotatedEntityName = OntologyAnnotation
                .getSourceFeatureValue(currentAnnot);
        if(annotatedEntityName != null
                && annotatedEntityName.equals(wantedName)) {
          result.add(currentAnnot);
        }
      }
    }
    return result;
  }

  /**
   * Given an annotation, this method tells which
   * selectedAnnotationSetName it belongs to.
   * 
   * @param ann
   * @return the name of the annotation set.
   */
  public String getAnnotationSet(Annotation ann) {
    Integer setId = annotationsID2ASID.get(ann.getId());
    return asID2ASName.get(setId);
  }

  public ArrayList<Integer> getAnnotationIDs(int textposition) {
    ArrayList<Integer> result = new ArrayList<Integer>();
    for(HashSet<Annotation> annotationlist : getEntityName2AnnotationList().values()) {
      for(Annotation a : annotationlist) {
        if(a.getStartNode().getOffset() < textposition
                && a.getEndNode().getOffset() > textposition) {
          result.add(a.getId());
        }
      }
    }
    return result;

  }

  public ArrayList<Annotation> getHighlightedAnnotations(int textposition) {
    ArrayList<Annotation> result = new ArrayList<Annotation>();
    // TODO synchronized
    for(HashSet<Annotation> annotationlist : getEntityName2AnnotationList().values()) {
      for(Annotation a : annotationlist) {
        if(ontoViewer.highlightedAnnotations.contains(a)) {
          if(a.getStartNode().getOffset() <= textposition
                  && a.getEndNode().getOffset() >= textposition) {
            result.add(a);
          }
        }
      }
    }
    return result;

  }

  public Annotation getAnnotation(int id) {
    for(Annotation a : getAnnotationSet()) {
      if(a.getId() == id) {
        return a;
      }

    }
    return null;

  }

  public Ontology getCurrentOntology() {
    return ontoViewer.getCurrentOntology();
  }

  /**
   * @return
   */
  public AnnotationSet getAnnotationSet() {
      return ontoViewer.getDocument().getAnnotations();
  }

  public void updateAnnotationLists(Annotation currentAnnot) {
    String aName = OntologyAnnotation.getSourceFeatureValue(
            getCurrentOntology(),currentAnnot);
    if(getEntityName2AnnotationList().containsKey(aName)) {
      LinkedHashSet<Annotation> annotList = getEntityName2AnnotationList().get(aName);
      annotList.add(currentAnnot);
      getEntityName2AnnotationList().put(aName, annotList);
    }
    else {
      LinkedHashSet<Annotation> annotList = new LinkedHashSet<Annotation>();
      annotList.add(currentAnnot);
      getEntityName2AnnotationList().put(aName, annotList);
    }

  }

  /** Deletes the Annotations from the document */
  public void deleteAnnotation(Annotation annotation) {
    // and now removing from the actual document
    AnnotationSet set = ontoViewer.getDocument().getAnnotations();
    if(!(set.remove(annotation))) {
      Map annotSetMap = ontoViewer.getDocument().getNamedAnnotationSets();
      if(annotSetMap != null) {
        java.util.List<String> setNames = new ArrayList<String>(
                annotSetMap.keySet());
        Collections.sort(setNames);
        Iterator<String> setsIter = setNames.iterator();
        while(setsIter.hasNext()) {
          set = ontoViewer.getDocument().getAnnotations(setsIter.next());
          if(set.remove(annotation)) {
            return;
          }
        }
      }
    }
  }

  public Integer registerAnnotationSet(AnnotationSet set, Integer annotationId) {
    Integer setId = null;
    if(asID2ASName.values() != null && asID2ASName.values().contains(set)) {
      Iterator<Integer> iter = asID2ASName.keySet().iterator();
      while(iter.hasNext()) {
        Integer tempId = iter.next();
        String setString = asID2ASName.get(tempId);
        if(setString.equals(set.getName())) {
          setId = tempId;
          break;
        }
      }
    }
    else {
      setId = new Integer(highestASID);
      highestASID++;
      asID2ASName.put(setId, set.getName());
    }
    annotationsID2ASID.put(annotationId, setId);
    return setId;
  }

  // ////////////////////////////
  // //
  // // Adding new annotations
  //
  // ////////////////////////////
//
//  /**
//   * Method to add a new annotation
//   * 
//   * @param classValue
//   * @param all
//   */
//  public ArrayList<Annotation> addNewAnnotation(
//          OResource entity,
//          boolean all,
//          FeatureMap map,
//          semano.ontoviewer.AnnotationMetaData selectedAnnotationType,
//          boolean isProperty) {
//    // get first selection from text area
//    int start = ontoViewer.documentTextArea.getSelectionStart();
//    int end = ontoViewer.documentTextArea.getSelectionEnd();
//    String text = ontoViewer.documentTextArea.getText();
//
//    String searchstring = ontoViewer.documentTextArea.getText().substring(
//            start, end);
//    HashMap<Integer, Integer> offsets = new HashMap<Integer, Integer>();
//    if(all && !isProperty) {
//      Annotator annotator = new AutoAnnotatorStringBased(this, text);
//      offsets = annotator.search(true,selectedAnnotationType,
//              searchstring);
//    }
//    else {
//      offsets.put(start, end);
//    }
//    return addAnnotationsWithOffsets(text, entity, map, selectedAnnotationType,
//            isProperty, offsets, false);
//
//  }

  /**
   * @param node
   * @param map
   * @param selectedAnnotationType
   * @param isProperty
   * @param offsets
   * @param ranges
   * @param domains
   * @return
   * @throws HeadlessException
   * @throws GateRuntimeException
   */
  public ArrayList<Annotation> addAnnotationsWithOffsets(String text,
          OResource entity, FeatureMap map,
          semano.ontoviewer.AnnotationMetaData annotationProperty,
          boolean isProperty, HashMap<Integer, Integer> offsets,
          boolean autoannotation) throws HeadlessException,
          GateRuntimeException {
    ArrayList<Annotation> result = new ArrayList<Annotation>();
    if(isProperty && annotationProperty.isShowDomainRangeAnno()) {
      if(entity instanceof ObjectProperty) {
        ObjectProperty prop = (ObjectProperty)entity;
        Set<OResource> ranges = OntologyUtil.getTransitiveClosureOfSubclasses(prop.getRange());
        Set<OResource> domains = OntologyUtil.getTransitiveClosureOfSubclasses(prop
                .getDomain());
        return addAnnotationsWithOffsets(text, prop, map, annotationProperty,
                isProperty, offsets, domains, ranges, autoannotation);
      }
    }
    else {
      return addAnnotationsWithOffsets(text, entity, map, annotationProperty,
              isProperty, offsets, null, null, autoannotation);

    }
    return result;
  }

  /**
   * @param entity
   * @param map
   * @param selectedAnnotationType
   * @param isProperty
   * @param offsets
   * @param ranges
   * @param domains
   * @return
   * @throws HeadlessException
   * @throws GateRuntimeException
   */
  public ArrayList<Annotation> addAnnotationsWithOffsets(String text,
          OResource entity, FeatureMap map1,
          semano.ontoviewer.AnnotationMetaData annotationMetaData,
          boolean isProperty, HashMap<Integer, Integer> offsets,
          Set<OResource> domains, Set<OResource> ranges, boolean autoannotation)
          throws HeadlessException, GateRuntimeException {
    ArrayList<Annotation> result = new ArrayList<Annotation>();
    for(Entry<Integer, Integer> entry : offsets.entrySet()) {
      FeatureMap map = Factory.newFeatureMap();
      map.putAll(map1);
      if(isProperty && annotationMetaData.isShowDomainRangeAnno()) {
        setDomainRangeAnnotationsInMap(text, map, domains, ranges, entry);
        // if domain and range are not there we have to remove the
        // annotation?
        Annotation domainAnnotationID = OntologyAnnotation.getDomainAnnotation(
                map, this);
        Annotation rangeAnnotationID = OntologyAnnotation.getRangeAnnotation(
                map, this);

        // logger.debug(domainAnnotationID + " and " +
        // rangeAnnotationID);
        if(domainAnnotationID != null && rangeAnnotationID != null) {
          Annotation annotation = addAnnotation(entity, map,
                  isProperty, entry.getKey(), entry.getValue(), autoannotation);
//          logger.debug(anno.toString());
          result.add(annotation);
        }
      }
      else {
        // annotation concepts
        // test whether POS is required!
        if(annotationMetaData.isPOSrelevant()) {
          Set<String> posValues = getPOS(entry.getKey(), entry.getValue(),
                  annotationMetaData.getPosAnnotationType());
          if(posValues != null && !posValues.isEmpty()) {
            Annotation annotation = addAnnotation(entity, map,
                    isProperty, entry.getKey(), entry.getValue(),
                    autoannotation);
            result.add(annotation);
          }
        }
        else {
          Annotation anno = addAnnotation(entity, map,
                  isProperty, entry.getKey(), entry.getValue(), autoannotation);
          result.add(anno);
        }
      }
    }
    return result;
  }

  /**
   * @param map
   * @param domains
   * @param ranges
   * @param entry
   */
  public void setDomainRangeAnnotationsInMap(String text, FeatureMap map,
          Set<OResource> domains, Set<OResource> ranges,
          Entry<Integer, Integer> entry) {
    // add domain and range annotations for property annotations
    // of type expression
    Set<Annotation> domainsAnnotations = getDomainAnnotations(text, domains,
            entry.getKey());
    Annotation domain = selectNextAnnotation(domainsAnnotations,
            new Long(entry.getKey()), true);
    Set<Annotation> rangeAnnotations = getRangeAnnotations(text, ranges,
            entry.getKey());
    Annotation range = selectNextAnnotation(rangeAnnotations,
            new Long(entry.getKey()), false);
    if(domain != null && range != null) {
      if(Settings.CONSIDER_SENTENCE_BOUNDS) {
        Set<Annotation> enclosingSentenceAnn = findEnclosingAnnotations(
                new Long(entry.getKey()), new Long(entry.getValue()),
                Settings.SENTENCE_ANNO_TYPE);
        // only one sentence is allowed, otherwise we do not continue...
        if(!enclosingSentenceAnn.isEmpty() && enclosingSentenceAnn.size() == 1) {
          Annotation sentenceAnnotation = enclosingSentenceAnn.iterator()
                  .next();
          if(sentenceAnnotation.getStartNode().getOffset().intValue() <= domain
                  .getStartNode().getOffset().intValue()
                  && sentenceAnnotation.getEndNode().getOffset().intValue() >= range
                          .getEndNode().getOffset().intValue()) {
            setDomainRangeAnnotsInMap(map, domain, range);
          }
        }
      }
      else {
        setDomainRangeAnnotsInMap(map, domain, range);
      }
    }
  }

  private Set<Annotation> findEnclosingAnnotations(Long startOffset,
          Long endOffset, String annotationType) {
    Set<Annotation> enclosingAnnots = getAnnotationSet().get(startOffset,endOffset);
    Set<Annotation> enclosingSentenceAnn = new HashSet<Annotation>();
    for(Annotation enclosing : enclosingAnnots) {
      if(enclosing.getType().equals(annotationType)) {
        enclosingSentenceAnn.add(enclosing);
      }
    }
    return enclosingSentenceAnn;
  }

  /**
   * @param map
   * @param domain
   * @param range
   */
  public boolean setDomainRangeAnnotsInMap(FeatureMap map, Annotation domain,
          Annotation range) {
    if(domain != null && range != null && domain.getId() != range.getId()
            && OntologyAnnotation.isSourceAClass(range)
            && OntologyAnnotation.isSourceAClass(domain)) {
      OntologyAnnotation.setDomainAnnotation(domain, map);
      OntologyAnnotation.setRangeAnnotation(range, map);
      return true;
    }
    else {

      OntologyAnnotation.resetDomainAnnotation(map);
      OntologyAnnotation.resetRangeAnnotation(map);
      return false;
    }
  }

 
  /**
   * Method to add a new annotation
   * 
   * @param classValue
   * @param all
   */
  public Annotation addAnnotation(
          OResource node,
          FeatureMap map,
          boolean isProperty, int start, int end, boolean autoannotation)
          throws HeadlessException, GateRuntimeException {

    // deselect text
    if(null != ontoViewer.documentTextArea && !autoannotation) {
      ontoViewer.documentTextArea.setSelectionStart(start);
      ontoViewer.documentTextArea.setSelectionEnd(start);
    }

    // set annotationValue if it is not set yet
    String selectedText = (String)map.get(Settings.ANNOTATION_VALUE);
    if(selectedText == null || selectedText.isEmpty()) {
      try {
        selectedText = ontoViewer.getDocument().getContent().toString()
                .substring(start, end);
      }
      catch(Exception e) {
        System.err.println(e.getMessage());
        return null;
      }
    }

    if(node == null) {
      System.err.println("class node was null!");
      return null;
    }
    FeatureMap newMap = initializeFeatureMap(node, map, 
            selectedText, autoannotation, start);

    // insert the annotation into the set:
    AnnotationSet set = getAnnotationSet();
    Integer id = createAnnotation(start, end, set,
            newMap);
    return set.get(id);
  }

  /**
   * initializes a new Map for a new Annotation
   * 
   * @param selectedText
   * @param Firstoffset
   * @return
   * @throws HeadlessException
   */
  public FeatureMap initializeFeatureMap(String selectedText, int Firstoffset)
          throws HeadlessException {
    FeatureMap newMap = Factory.newFeatureMap();
    newMap = updateFeatureMap(newMap, Firstoffset);
    newMap.put(Settings.ANNOTATION_VALUE, selectedText);
    return newMap;
  }

  /**
   * initializes a new Map for a new Annotation
   * 
   * @param selectedText
   * @param Firstoffset
   * @return
   * @throws HeadlessException
   */
  public FeatureMap updateFeatureMap(FeatureMap map, int Firstoffset)
          throws HeadlessException {
    String docTitle = ontoViewer.getDocument().getName().replaceAll(" ", "_");
    FeatureMap newMap = AnnotationValue.createDefaultFeatureMap(
            ((AbstractOWLOntology)ontoViewer.getCurrentOntology()).getAuthor(),
            ((AbstractOWLOntology)ontoViewer.getCurrentOntology())
                    .getLanguage(), docTitle, String.valueOf(Firstoffset));
    newMap.put(Settings.AUTOANNOTATION, String.valueOf(false));
    return newMap;
  }

  /**
   * is called when an auto-annotation was changes. it will be stored as
   * a new annotation then
   * 
   * @param selectedAnnotation
   */
  public FeatureMap updateAnnotationMetadata(FeatureMap features) {

    FeatureMap map = null;
    if(features == null || features.isEmpty()) {
      map = initializeFeatureMap(ontoViewer.documentTextArea.getSelectedText(),
              ontoViewer.documentTextArea.getSelectionStart());
    }
    else {
      map = features;
      String docTitle = ontoViewer.getDocument().getName().replaceAll(" ", "_");
      map.put(Settings.SOURCEDOC, docTitle);
      map.put(Settings.AUTHOR, ((AbstractOWLOntology)ontoViewer
              .getCurrentOntology()).getAuthor());
      map.put(Settings.LANGUAGE, ((AbstractOWLOntology)ontoViewer
              .getCurrentOntology()).getLanguage());
      map.put(Settings.OFFSET, ontoViewer.documentTextArea.getSelectionStart());
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat(Settings.DATE_FORMAT_NOW);
      String time = sdf.format(cal.getTime());
      map.put(Settings.TIME, time);

      if(ontoViewer.documentTextArea.getSelectedText() != null) {
        map.put(Settings.ANNOTATION_VALUE,
                ontoViewer.documentTextArea.getSelectedText());
      }
      map.put(Settings.AUTOANNOTATION, String.valueOf(false));
    }
    return map;
  }

  /**
   * @param node
   * @param map
   * @param annotationType
   * @param shouldCreateInstance
   * @param selectedText
   * @return
   * @throws HeadlessException
   */
  private FeatureMap initializeFeatureMap(OResource node, FeatureMap map,
          String selectedText, boolean autoannotation, int Firstoffset)
          throws HeadlessException {
    FeatureMap newMap = initializeFeatureMap(selectedText, Firstoffset);
    if(node == null) return newMap;
    if(map != null) {
      newMap.putAll(map);
      if(!autoannotation) {
        newMap = updateAnnotationMetadata(newMap);
      }
    }
    else {
      String docTitle = ontoViewer.getDocument().getName().replaceAll(" ", "_");
      newMap = AnnotationValue.createDefaultFeatureMap(
              ((AbstractOWLOntology)ontoViewer.getCurrentOntology())
                      .getAuthor(), ((AbstractOWLOntology)ontoViewer
                      .getCurrentOntology()).getLanguage(), docTitle, String
                      .valueOf(Firstoffset));
    }
    OntologyAnnotation.removeSource(newMap);
    OntologyAnnotation.setSource(node, newMap);

    String dns = OntologyAnnotation.getOntologyUri(node);
    if(dns == null) {
      dns = getCurrentOntology().getDefaultNameSpace();
    }
    newMap.put(gate.creole.ANNIEConstants.LOOKUP_ONTOLOGY_FEATURE_NAME, dns);
    if(!newMap.containsKey(Settings.ANNOTATION_VALUE) && selectedText != null) {
      newMap.put(Settings.ANNOTATION_VALUE, selectedText);
    }

    newMap.put(Settings.OFFSET, Firstoffset);
    newMap.put(Settings.AUTOANNOTATION, String.valueOf(autoannotation));
    return newMap;
  }

  public void addAnnotationToOntology(OntologyAnnotation oa) {
    OResource source = oa.getResource(getCurrentOntology());
    if(source != null) {
      addAnnotationToOntology(source, OntologyAnnotation.getAnnotationType(oa),
              AnnotationValue.formatMetaData(oa.getValue(), oa));
    }
    else {
      System.err
              .println("source of an annotation was NULL! Annotation with value "
                      + oa.getValue());
    }
  }

  /**
   * @param annotationFeature
   * @param start
   * @param end
   * @param set
   * @param newMap
   * @return
   * @throws GateRuntimeException
   */
  private Integer createAnnotation(
          int start, int end, AnnotationSet set, FeatureMap newMap)
          throws GateRuntimeException {
    Integer id;
    try {
      // String selectedAnnotationType =
      // Settings.DEFAULT_ANNOTATION_TYPE;
      // if(null!=ontoViewer.ontologyViewerOptions){
      // selectedAnnotationType = ontoViewer.ontologyViewerOptions
      // .getSelectedAnnotationType(annotationFeature);
      // }

      id = set.add(new Long(start), new Long(end),
              Settings.DEFAULT_ANNOTATION_TYPE,
              newMap);

    }
    catch(gate.util.InvalidOffsetException ioe) {
      throw new GateRuntimeException(ioe);
    }
    return id;
  }

  // /**
  // * general method called by other methods passing different pattern
  // * matching settings
  // *
  // * @param isProperty if the annotated entity is a property
  // * @param text
  // * @param cl annotated entity
  // * @param searchstring
  // * @param annotationType
  // * @param features pre-initialized features map
  // * @param singleWord whether the searchstring has to be a word as a
  // * whole
  // * @param caseSensitive
  // */
  // private ArrayList<Annotation> annotateWithString(boolean
  // isProperty,
  // String text, OResource cl, String searchstring,
  // kit.aifb.ontologyannotation.AnnotationProperty annotationType,
  // FeatureMap features, boolean autoannotation) {
  //
  //
  // if(searchstring.toLowerCase().endsWith(PLURAL_INDICATION) &&
  // Settings.USE_PLURAL){
  // //also search for plural
  // String pluralForm =
  // searchstring.substring(0,searchstring.length()-1)+"i";
  // return searchAndAnnotate(isProperty, text, cl, pluralForm,
  // annotationType,
  // features, autoannotation,true);
  // }else{
  // return searchAndAnnotate(isProperty, text, cl, searchstring,
  // annotationType,
  // features, autoannotation,false);
  // }
  // }

  // /**
  // * @param isProperty
  // * @param text
  // * @param cl
  // * @param searchstring
  // * @param annotationType
  // * @param features
  // * @param annotationType2
  // * @param autoannotation
  // * @param ranges
  // * @param domains
  // * @param result
  // */
  // private ArrayList<Annotation> searchAndAnnotate(boolean isProperty,
  // String text, OResource cl,
  // String searchstring, kit.aifb.ontologyannotation.AnnotationProperty
  // annotationType, FeatureMap features,
  // boolean autoannotation, boolean isPlural, Set<OResource> domains,
  // Set<OResource> ranges) {
  //
  //
  //
  // }


  // domain and range

  public Annotation selectNextAnnotation(Set<Annotation> domainsAnnotations,
          Long startOffset, boolean before) {
    long leastDistance = Settings.MATCHING_WINDOW;
    Annotation leastDistanceAnnotation = null;
    for(Annotation a : domainsAnnotations) {
      long end = leastDistance;
      long start = 0;
      if(before) {
        end = startOffset;
        start = a.getEndNode().getOffset().intValue();
      }
      else {
        start = startOffset;
        end = a.getStartNode().getOffset().intValue();
      }
      long distance = end - start;
      if(distance < leastDistance) {
        leastDistance = distance;
        leastDistanceAnnotation = a;
      }
    }
    return leastDistanceAnnotation;
  }

  public Set<Annotation> getDomainAnnotations(String text,
          Set<OResource> domains, long offset) {
    Set<Annotation> domainAnnotations = new HashSet<Annotation>();
    Set<Annotation> domainAnnotationsCurrent = getAnnotationsWithinWindow(text,
            Settings.MATCHING_WINDOW, offset, true);
    for(Annotation a : domainAnnotationsCurrent) {
      String sourceFeatureValue = OntologyAnnotation.getSourceFeatureValue(a);
      if(sourceFeatureValue != null) {
        for(OResource clas : domains) {
          if(clas.toString().equals(sourceFeatureValue)) {
            domainAnnotations.add(a);
          }
        }
      }
    }
    return domainAnnotations;
  }

  public Set<Annotation> getRangeAnnotations(String text,
          Set<OResource> ranges, long offset) {
    Set<Annotation> rangeAnnotations = new HashSet<Annotation>();
    Set<Annotation> rangeAnnotationsCurrent = getAnnotationsWithinWindow(text,
            Settings.MATCHING_WINDOW, offset, false);
    for(Annotation a : rangeAnnotationsCurrent) {
      String sourceFeatureValue = OntologyAnnotation.getSourceFeatureValue(a);
      if(sourceFeatureValue != null) {
        for(OResource clas : ranges) {
          if(clas.toString().equals(sourceFeatureValue)) {
            rangeAnnotations.add(a);
          }
        }
      }
    }
    return rangeAnnotations;
  }

  private Set<Annotation> getAnnotationsWithinWindow(String text,
          int matchingWindow, long offset, boolean before) {
    HashSet<Annotation> result = new HashSet<Annotation>();

    Long startOffset = offset;
    Long endOffset = offset;
    if(before) {
      if(startOffset <= matchingWindow) {
        startOffset = new Long(0);
      }
      else {
        startOffset -= matchingWindow;
      }
    }
    else {
      Long documentEndOffset = new Long(text.length() - 1);
      if(endOffset >= documentEndOffset - matchingWindow) {
        endOffset = documentEndOffset;
      }
      else {
        endOffset += matchingWindow;
      }
    }
    result.addAll(getAnnotationSet().get(startOffset, endOffset));

    return result;
  }


  // ////////////////////////////////////
  //
  //
  // writing annotations to ontology
  // /////////////////////////////////////

  /**
   * @param node
   * @param string
   * @param selectedText
   * @throws HeadlessException
   */
  public void addAnnotationToOntology(OResource source,
          String string,
          String selectedText) throws HeadlessException {
    throw new HeadlessException("this doesnt work any more with generic annotation types!");
//    AnnotationProperty aProp = OntologyAnnotation.getAnnotationProperty(
//            string, getCurrentOntology());
//    if(aProp != null) {
//      source.addAnnotationPropertyValue(aProp, new Literal(selectedText));
//    }
//    else {
//      System.err
//              .println("could not determine the annotation type for the following annotation: "
//                      + selectedText + " ," + source != null ? source
//                      .toString() : "");
//    }
  }


  public Set<String> getPOS(long startOffset, long endOffset, String posTag) {
    Set<String> result = new HashSet<String>();
    for(Annotation a : getAnnotationSet().get(startOffset, endOffset)) {
      if(a.getFeatures()
              .containsKey(ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME)) {
        Object value = a.getFeatures().get(
                ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME);
  //        System.out.println(value);
        if(value.toString().contains(posTag)) {
          result.add(value.toString());
        }
      }
    }
    return result;
  }
//
//  public void autoAnnotate(boolean animate) {
//    new AutoAnnotatorStringBased(this, this.ontoViewer.documentTextArea.getText()).autoAnnotate(animate);
//    
//  }

  LinkedHashMap<String, LinkedHashSet<Annotation>> getEntityName2AnnotationList() {
    return EntityName2AnnotationList;
  }

  void setEntityName2AnnotationList(LinkedHashMap<String, LinkedHashSet<Annotation>> entityName2AnnotationList) {
    EntityName2AnnotationList = entityName2AnnotationList;
  }

  public void removeAnnotationsWithRuleID(String ruleID) {
    Iterator<Annotation> it = getAnnotationSet().iterator();
    while(it.hasNext()){
      Annotation a =it.next();
      if(ruleID.equals(OntologyAnnotation.getRuleID(a)))
        it.remove();
    }
    
  }

}
