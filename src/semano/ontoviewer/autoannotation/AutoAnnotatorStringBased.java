package semano.ontoviewer.autoannotation;

import gate.Annotation;
import gate.FeatureMap;
import gate.creole.ontology.OResource;
import gate.creole.ontology.ObjectProperty;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.RDFProperty;
import gate.util.GateRuntimeException;

import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.log4j.Logger;

import semano.ontologyowl.AnnotationValue;
import semano.ontologyowl.impl.OWLOntology;
import semano.ontoviewer.AnnotationMetaData;
import semano.ontoviewer.AnnotationStore;
import semano.ontoviewer.OntologyAnnotation;
import semano.util.OntologyUtil;
import semano.util.Settings;

public class AutoAnnotatorStringBased  implements JobFinishedListener, Annotator {


  private static final boolean EXPORT_CONCEPT_PATTERNS_TO_JAPE = true;

  private Logger logger=Logger.getLogger(this.getClass().getName());

  private boolean jobsFinished = false;
 
  String text;
  
  public AnnotationStore m;

  public AutoAnnotatorStringBased(AnnotationStore aStore, String text) {
    m=aStore;
    this.text=text;
  }
  

  @Override
  public void notifyJobFinished() {
    jobsFinished = true;
  }

  /**
   * @return the jobsFinished
   */
  private boolean isJobsFinished() {
    return jobsFinished;
  }


  // //////////////////////////////////////
  //
  //
  //
  // AUTO ANNOTATION
  //
  // //////////////////////////////////////

  /* (non-Javadoc)
   * @see kit.aifb.ontologyannotation.autoannotation.Annotator#autoAnnotate(boolean)
   */
  @Override
  public void autoAnnotate(boolean animate) {
    if(animate) {
      AnnotationRunner runableAction = new AnnotationRunner(animate,m.ontoViewer,this);
      runableAction.registerListener(this);
      Thread thread = new Thread(runableAction, "");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
    else {
      autoAnnnotateNoProgress();
    }
  }


  /**
   * annotates the text with label-like ids of entities, The type of
   * annotation is Synonym
   * 
   * @param classes
   * @param text
   * @param isProperty
   * @param o
   */
  private void autoAnnotateWithNames(Set<? extends OResource> classes,
          String text, String textLowerCase, boolean isProperty, Ontology o) {
    if(isProperty) {

      for(OResource cl : classes) {
        if(!hasToBeAnnotated(o, cl)) {
          return;
        }
        String name = OntologyUtil.convertToValidLabel(cl.getName()).toLowerCase();
        semano.ontoviewer.AnnotationMetaData type = OntologyAnnotation
                .getPropertyForTypeEnum(Settings.SYNONYM);
        HashMap<Integer, Integer> offsets = search(true, 
                type, name);
        m.addAnnotationsWithOffsets(text, cl,
                AnnotationValue.getDefaultNameFeatureMap(name), type,
                isProperty, offsets, null, null, true);
      }
    }

  }

  /**
   * calls annotateWithString with the same settings for each annotation
   * 
   * @param classes
   * @param text
   * @param isProperty
   * @param feature
   * @param o
   * @param caseSensitive
   * @param wholeWord
   */
  private void autoAnnotateWithFeature(Set<? extends OResource> classes,
          String text, String textLowerCase, boolean isProperty,
          semano.ontoviewer.AnnotationMetaData type, Ontology o) {

    for(OResource cl : classes) {
      if(!hasToBeAnnotated(o, cl)) {
        return;
      }
      AnnotationValue[] annotationValues = ((OWLOntology)o)
              .getAnnotationValues(cl.getURI().toString(), type.getEnumName());

      for(AnnotationValue annotationValue : annotationValues) {
        if(OntologyUtil.iriEquals(type, annotationValue)) {
          if(!annotationValue.isAntiPattern()) {
            // make sure the formula is long enough to be a formula
            String searchString = annotationValue.getValue();
            if(annotationValue.getAnnotationPropertyObject()
                    .getMinLettersNumber() <= searchString
                    .length()) {

              // annotateWithString(isProperty, text, cl,
              // annotationValue.getValue(), type,
              // annotationValue.createFeatureMap(), true);
              HashMap<Integer, Integer> offsets = search(
                      true,type, searchString);
              ArrayList<Annotation> s = m.addAnnotationsWithOffsets(text, cl,
                      annotationValue.createFeatureMap(), type, isProperty,
                      offsets, null, null, true);
//              //TODO NN DB CODE for JAPEGenerator:
//              if(EXPORT_CONCEPT_PATTERNS_TO_JAPE){
//                if(s!=null && s.size()!=0){
//                  JAPEGenerator.generateJAPE(s.get(0),searchString);
//                }
//              }
            }
          }

        }
      }

    }
  }

  /**
   * @param o
   * @param cl
   * @return
   */
  private boolean hasToBeAnnotated(Ontology o, OResource cl) {
    // Check if the entity has to be annotated
    @SuppressWarnings("deprecation")
    AnnotationValue[] annotationValuesNoAnnotate = ((OWLOntology)o)
            .getAnnotationValues(cl.getURI().toString(), OntologyAnnotation
                    .getPropertyForTypeEnum(Settings.NO_AUTOANNOTATION)
                    .getUri());
    if(annotationValuesNoAnnotate == null
            || annotationValuesNoAnnotate.length == 0) {
      return true;
    }
    return false;
  }

  


  private void expressionlesslyAnnotateRelation(ObjectProperty prop,
          String text, Ontology o, Set<OResource> domains, Set<OResource> ranges, semano.ontoviewer.AnnotationMetaData annoType) {
    String simplePropertyNameForExpAnno = OntologyAnnotation
            .getSimplePropertyNameForAnnotationType(annoType);
    AnnotationValue[] annotationValues = ((OWLOntology)o).getAnnotationValues(
            prop.getURI().toString(), simplePropertyNameForExpAnno);
    // each relation containing two annotations can be annotated without
    // expressions
    if(annotationValues != null && annotationValues.length > 1) {

      int maxDistance = getMaxAnnotationDistance(annotationValues);
      // apply the FACTOR
      maxDistance = maxDistance / Settings.OFFSET_FACTOR;
      annotatePropsWithDistance(prop, text, domains, ranges, annotationValues,
              maxDistance, true, " ", annoType);
    }
    if(Settings.CALCULATE_CANDIDATE_RELATION_ANNOTATIONS) {
      annotatePropsWithDistance(prop, text, domains, ranges, annotationValues,
              Settings.OFFSET_DISTANCE, false, "",annoType);
    }
  }

  /**
   * @param prop
   * @param text
   * @param domains
   * @param ranges
   * @param annotationValues
   * @param distance
   * @throws HeadlessException
   * @throws GateRuntimeException
   */
  private void annotatePropsWithDistance(ObjectProperty prop, String text,
          Set<OResource> domains, Set<OResource> ranges,
          AnnotationValue[] annotationValues, int distance,
          boolean separatorOnly, String possibleSeparator, semano.ontoviewer.AnnotationMetaData annoType)
          throws HeadlessException, GateRuntimeException {
    boolean oneWay = true;
    Map<Annotation, Set<Annotation>> selectedDomainsRanges = gatherPossibleDomainsRanges(
            text, prop, oneWay, distance, domains, ranges);

    for(Annotation domain : selectedDomainsRanges.keySet()) {
      // select one most suitable domain from the possible ones
      for(Annotation range : selectedDomainsRanges.get(domain)) {
        int firstEnd = domain.getEndNode().getOffset().intValue();
        int secondEnd = range.getStartNode().getOffset().intValue();
        if(firstEnd > secondEnd) {
//          System.out.println("first not before second");
          continue;
        }
        if(separatorOnly) {
          String sep = text.substring(firstEnd, secondEnd);
          if(!sep.equals(possibleSeparator)) {
            continue;
          }
        }
        //check POS for domain and range annotations:
        if(annoType.isExpressionlessWithPOS()){
            Set<String> matchedDomainPOS = new HashSet<String>();
            for(String posTagD:annoType.getDomainPOS()){
              matchedDomainPOS.addAll(m.getPOS(domain.getStartNode().getOffset(), domain.getEndNode().getOffset(), posTagD));
            }
            Set<String> matchedRangePOS = new HashSet<String>();
            for(String posTagR:annoType.getRangePOS()){
              matchedRangePOS.addAll(m.getPOS(range.getStartNode().getOffset(), range.getEndNode().getOffset(), posTagR));
            } 
            if(matchedDomainPOS!=null && !matchedDomainPOS.isEmpty() &&
                    matchedRangePOS!=null && !matchedRangePOS.isEmpty()){
                // add annotation
                finishExpressionlessAnnotation(prop,
                    text, annotationValues, annoType, domain, range, domain.getStartNode().getOffset().intValue(),
                    range.getEndNode().getOffset().intValue());
            }
        }else{    
        // add annotation
        finishExpressionlessAnnotation(prop,
            text, annotationValues, annoType, domain, range, domain.getStartNode().getOffset().intValue(),
            range.getEndNode().getOffset().intValue());

      }}
    }
  }

  /**
   * @param prop
   * @param text
   * @param annotationValues
   * @param annoType
   * @param domain
   * @param range
   * @param startOffset
   * @param endOffset
   * @return
   * @throws HeadlessException
   * @throws GateRuntimeException
   */
  private FeatureMap finishExpressionlessAnnotation(ObjectProperty prop,
          String text, AnnotationValue[] annotationValues,
          semano.ontoviewer.AnnotationMetaData annoType,
          Annotation domain, Annotation range, int startOffset, int endOffset)
          throws HeadlessException, GateRuntimeException {
    // add anotations:
    FeatureMap initializedFeatureMap = annotationValues[0]
            .createFeatureMap();
    if(m.setDomainRangeAnnotsInMap(initializedFeatureMap, domain, range)) {
      Annotation relationAnnotation = m.addAnnotation((OResource)prop,
              initializedFeatureMap, true, startOffset,
              endOffset, true);
      if(relationAnnotation != null) {
        if(Settings.DEBUG_EXPRESSIONLESS_RELATIONS) {
          OntologyAnnotation.setRangeAnnotation(range,
                  relationAnnotation.getFeatures());
          boolean domainFirst = startOffset == domain.getStartNode()
                  .getOffset().intValue();
          String relationString = domainFirst ? text.substring(domain
                  .getEndNode().getOffset().intValue(), range
                  .getStartNode().getOffset().intValue()) : text.substring(
                  range.getEndNode().getOffset().intValue(), domain
                          .getStartNode().getOffset().intValue());
          System.out
                  .println("\n\n\n\n EXPRESSIONLESS RELATION ANNOTATION: "
                          + prop.getName()
                          + "\n"
                          + (domainFirst
                                  ? OntologyAnnotation.getSummary(domain)
                                          + " (domain)"
                                  : OntologyAnnotation.getSummary(range)
                                          + " (range)")
                          + "\n"
                          + relationString
                          + "("
                          + relationString.length()
                          + ")"
                          + "\n"
                          + (domainFirst
                                  ? OntologyAnnotation.getSummary(range)
                                          + " (range)"
                                  : OntologyAnnotation.getSummary(domain)
                                          + " (domain)"));
        }

      }
    }
    return initializedFeatureMap;
  }

  /**
   * @param annotationValues
   * @return
   */
  private int getMaxAnnotationDistance(AnnotationValue[] annotationValues) {
    try {
      return Integer.parseInt(annotationValues[0].getValue());
    }
    catch(NumberFormatException e) {
      try {
        return Integer.parseInt(annotationValues[1].getValue());
      }
      catch(Exception e2) {
        return 0;
      }
    }

  }

  /**
   * @param text
   * @param prop
   * @param oneWay
   * @param ranges
   * @param domains
   * @return
   */
  private Map<Annotation, Set<Annotation>> gatherPossibleDomainsRanges(
          String text, ObjectProperty prop, boolean oneWay, int distance,
          Set<OResource> domains, Set<OResource> ranges) {
    Map<Annotation, Set<Annotation>> selectedRangesToDomains = new HashMap<Annotation, Set<Annotation>>();

    Set<Annotation> domainAnnots = new HashSet<Annotation>();
    for(OResource dom : domains) {
      domainAnnots.addAll(m.getAnnotationsWithResource(m.getAnnotationSet(),
              dom.getName()));
    }
    for(Annotation domainAnnot : domainAnnots) {
      Long startOffset = domainAnnot.getEndNode().getOffset();
      // gather all possible candidates
      Set<Annotation> rangeCandidantes = m.getRangeAnnotations(text, ranges,
              startOffset);
      if(!rangeCandidantes.isEmpty()) {
        Annotation range = m.selectNextAnnotation(rangeCandidantes, startOffset,
                false);
        // add domainAnnotation into the list
        if(selectedRangesToDomains.get(domainAnnot) == null) {
          selectedRangesToDomains.put(domainAnnot, new HashSet<Annotation>());
        }
        selectedRangesToDomains.get(domainAnnot).add(range);
      }
    }
    return selectedRangesToDomains;
  }
   

  private void autoAnnnotateNoProgress() {
    m.ontoViewer.setAutoannotation(true);
    try {
      String text = m.ontoViewer.getDocument().getContent().toString();
      String textLowerCase= text.toLowerCase();
      // MainFrame.lockGUI("Annotating/restoring ... ");
      Ontology o = m.getCurrentOntology();
      Set<? extends OResource> classes = o.getOClasses(false);
      int partsOfProgress = 2;
      if(!Settings.ANNOTATE_RELATIONS) {
        partsOfProgress = 1;
      }
      annotateEntitiesNoProgress(classes, text, textLowerCase, 0, partsOfProgress, false);

      if(Settings.ANNOTATE_RELATIONS) {

        Set<? extends RDFProperty> properties;
        if(Settings.ANNOTATE_DATA_PROPERTIES) {
          properties = o.getRDFProperties();
        }
        else {
          properties = o.getObjectProperties();
        }
        annotateEntitiesNoProgress(properties, text, textLowerCase, 50, partsOfProgress, true);
      }
      m.ontoViewer.setAutoannotation(false);
      if(m.ontoViewer.hasGUI()) {
        m.ontoViewer.refreshHighlights();
      }
    }
    catch(Exception ex) {
      throw new GateRuntimeException("Problem annotating", ex);
    }
  }
  
  
  
  
  
  
  
  
  
  
  
  
  

  private void annotateEntitiesNoProgress(Set<? extends OResource> entities,
          String text, String textLowerCase, int initialProgress, int partsOfProgress,
          boolean isProperty) {
    if(isProperty) {

      for(OResource entity : entities) {
        if(entity instanceof ObjectProperty) {
          ObjectProperty prop = (ObjectProperty)entity;
          Set<OResource> ranges = OntologyUtil.getTransitiveClosureOfSubclasses(prop
                  .getRange());
          Set<OResource> domains = OntologyUtil.getTransitiveClosureOfSubclasses(prop
                  .getDomain());
          semano.ontoviewer.AnnotationMetaData ap1 = OntologyAnnotation
                  .getPropertyForTypeEnum(Settings.EXPRESSION);
          if(ap1.isAutoannotate()) {
            autoAnnotatePropertyWithFeature(prop, text, textLowerCase, isProperty, ap1,
                    m.getCurrentOntology(), domains, ranges);
          }
//          ap1 = OntologyAnnotation.getPropertyForTypeEnum(Settings.PASSAGE);
//          if(ap1.isAutoannotate()) {
//            autoAnnotatePropertyWithFeature(prop, text, isProperty, ap1,
//                    getCurrentOntology(), domains, ranges);
//          }
          semano.ontoviewer.AnnotationMetaData ap2 = OntologyAnnotation
                  .getPropertyForTypeEnum(Settings.EXPRESSIONLESSRELATION);
          if(ap2.isAutoannotate()) {
            expressionlesslyAnnotateRelation(prop, text, m.getCurrentOntology(),
                    domains, ranges, ap2);
            // autoAnnotatePropertyWithFeature(entity, text, isProperty,
            // ap2, getCurrentOntology(),domains, ranges);
          }
          
          for(String ap3String: Settings.EXPRESSIONLESSRELATIONWITHPOS){
            semano.ontoviewer.AnnotationMetaData ap3 = OntologyAnnotation
            .getPropertyForTypeEnum(ap3String);
          if(ap3.isAutoannotate()) {
            expressionlesslyAnnotateRelation(prop, text, m.getCurrentOntology(),
                    domains, ranges, ap3);
            // autoAnnotatePropertyWithFeature(entity, text, isProperty,
            // ap2, getCurrentOntology(),domains, ranges);
          }
          }
        }
      }
    }
    else {
      if(Settings.ANNOTATE_WITH_NAMES) {
        // ids
        autoAnnotateWithNames(entities, text, textLowerCase, isProperty, m.getCurrentOntology());
      }
      for(semano.ontoviewer.AnnotationMetaData ap : Settings.annotationProperties) {
        if(ap.isAutoannotate()) {
          autoAnnotateWithFeature(entities, text, textLowerCase, isProperty, ap,
                  m.getCurrentOntology());
        }
      }
    }

  }

  private void autoAnnotatePropertyWithFeature(OResource cl, String text,String textLowerCase, 
          boolean isProperty,
          semano.ontoviewer.AnnotationMetaData type, Ontology o,
          Set<OResource> domains, Set<OResource> ranges) {
    AnnotationValue[] annotationValues = ((OWLOntology)o).getAnnotationValues(
            cl.getURI().toString(), type.getEnumName());
    for(AnnotationValue annotationValue : annotationValues) {
      if(OntologyUtil.iriEquals(type, annotationValue)) {
        if(!annotationValue.isAntiPattern()) {
          // make sure the formula is long enough to be a formula
          if(annotationValue.getAnnotationPropertyObject()
                  .getMinLettersNumber() <= annotationValue.getValue().length()) {
            HashMap<Integer, Integer> offsets = search(true,
                    type, annotationValue.getValue());
            m.addAnnotationsWithOffsets(text, cl,
                    annotationValue.createFeatureMap(), type, isProperty,
                    offsets, domains, ranges, true);
            // searchAndAnnotate(isProperty, text, cl,
            // annotationValue.getValue(), type,
            // annotationValue.createFeatureMap(), true, false, domains,
            // ranges);

          }
        }

      }

    }

  }

  /* (non-Javadoc)
   * @see kit.aifb.ontologyannotation.autoannotation.Annotator#search(boolean, kit.aifb.ontologyannotation.AnnotationMetaData, java.lang.String)
   */
  @Override
  public HashMap<Integer, Integer> search(
          boolean plural,
          semano.ontoviewer.AnnotationMetaData selectedAnnotationMetaData,
          String searchString) {
    HashMap<Integer, Integer> offsets;
    
    if(searchString.toLowerCase().endsWith(Settings.PLURAL_INDICATION)
            && Settings.USE_PLURAL) {
      // also search for plural
      String pluralForm = searchString.substring(0, searchString.length() - 1)
              + "i";
      offsets = search(pluralForm, selectedAnnotationMetaData, true);
    }
    offsets = search(searchString, selectedAnnotationMetaData, false);
    return offsets;

  }
  
  
  

  /**
   * @param text
   * @param searchstring
   * @param annotationType
   * @param isPlural
   * @param result
   * @return
   */
  private HashMap<Integer, Integer>  search(String searchstring,
          semano.ontoviewer.AnnotationMetaData annotationType,
          boolean isPlural) {
    String textLowerCase = text.toLowerCase();
    HashMap<Integer, Integer> offsets = new HashMap<Integer, Integer>();
    if(searchstring == null || searchstring.isEmpty()) {
      return offsets;
    }
    int fromIndex = 0;
    boolean notFound = false;
    while(fromIndex < text.length() && !notFound) {
      // search
      int start = -1;
      if(annotationType.isCaseSensitive())
        start=text.indexOf(searchstring, fromIndex);
      else
        start=textLowerCase.indexOf(searchstring.toLowerCase(), fromIndex);
      // found?
      if(start == -1) {
        notFound = true;
      }
      // found
      else {
        fromIndex = start + searchstring.length();
        // Set<String> pos = getPOS(start);
        String annotationValue = searchstring;
        // for plural
        if(isPlural) {
          String possiblyPluralForm = getPluralWord(start, searchstring, text);
          annotationValue = possiblyPluralForm;
          if(fromIndex < start + possiblyPluralForm.length()) {
            fromIndex = start + possiblyPluralForm.length();
          }
        }else{
          if(text.substring(fromIndex).startsWith(Settings.SIMPLE_PLURAL)){
            fromIndex++;
            annotationValue+=Settings.SIMPLE_PLURAL;
          }
        }
        // else
        if(!annotationType.isWholeWordsOnly()
                || isSingleWord(start, annotationValue, text)) {
          offsets.put(start, fromIndex);
        }
      }
    }
//    JAPEGenerator.generateJAPE(searchstring, annotationType);
    return offsets;
  }

  /**
   * gets the plural form if found in text
   * 
   * @param start
   * @param searchstring
   * @param text
   * @return
   */
  private static String getPluralWord(int start, String searchstring, String text) {
    if(text.length() > start + searchstring.length()) {
      String letterAfter = "" + text.charAt(start + searchstring.length());
      if(letterAfter.equals(Settings.SIMPLE_PLURAL)) {
        return searchstring + letterAfter;
      }
      else if(letterAfter.equals(Settings.Y_PLURAL)) {
        if(text.length() > start + searchstring.length() + 1) {
          String letterAfterAfter = ""
                  + text.charAt(start + searchstring.length() + 1);
          if(letterAfterAfter.equals(Settings.SIMPLE_PLURAL)) {
            return searchstring + letterAfter + letterAfterAfter;
          }
        }
      }
    }
    return searchstring;
  }

  /**
   * verifies that the substring is a wohle word, not just a subword
   * 
   * @param fromIndex
   * @param name
   * @param text
   * @return
   */
  private static boolean isSingleWord(int fromIndex, String name, String text) {
    Pattern p = Pattern.compile("^[a-zA-Z]$");
    String letterBefore = "", letterAfter = "";
    if(fromIndex > 0) {
      letterBefore = "" + text.charAt(fromIndex - 1);
      Matcher m = p.matcher(letterBefore);
      if(m.find() || letterBefore.equals("-")) return false;
    }
    if(fromIndex < text.length() - name.length()) {
      letterAfter = "" + text.charAt(fromIndex + name.length());
      Matcher m = p.matcher(letterAfter);
      if(m.find() || letterAfter.equals("-")) return false;
    }
    return true;
  }
//
//  // TODO NN reg expr
//  private static String getSearchableStringAsRegExpr(String name) {
//    String result = OntologyUtil.convertClassnameToLabel(name).toLowerCase();
//    result = result.replace(" ", "\\s+");
//    result = result.replace("  ", " ");
//    return result;
//  }
//  
  
  
  
  

  
///////////////////////////////////////////////////////////////////////////////////  

///////////////////////////////////////////////////////////////////////////////////  

///////////////////////////////////////////////////////////////////////////////////  
/**
* THIS three methods is called only from the runnable annotator
* 
* @param entities
* @param textLowerCase
* @param initialProgress
* @param partsOfProgress
* @param classEntities
*/
protected void annotateClassesWithLabels() {
String textLowerCase = text.toLowerCase();
Ontology o = m.getCurrentOntology();
Set<? extends OResource> entities = o.getOClasses(false);
autoAnnotateWithNames(entities, text, textLowerCase, false,
m.getCurrentOntology());
}

protected void annotateClassesWithAnnoProp(AnnotationMetaData ap) {
String textLowerCase = text.toLowerCase();
Ontology o = m.getCurrentOntology();
Set<? extends OResource> entities = o.getOClasses(false);
autoAnnotateWithFeature(entities, text, textLowerCase, false, ap,
m.getCurrentOntology());
}

protected void annotateProperties() {
if(!Settings.ANNOTATE_DATA_PROPERTIES) {
for(ObjectProperty prop : m.getCurrentOntology().getObjectProperties()) {
Set<OResource> ranges = OntologyUtil
.getTransitiveClosureOfSubclasses(prop.getRange());
Set<OResource> domains = OntologyUtil
.getTransitiveClosureOfSubclasses(prop.getDomain());
semano.ontoviewer.AnnotationMetaData ap1 = OntologyAnnotation
.getPropertyForTypeEnum(Settings.EXPRESSION);
if(ap1.isAutoannotate()) {
autoAnnotatePropertyWithFeature(prop, text, text.toLowerCase(), true,
ap1, m.getCurrentOntology(), domains, ranges);
}
semano.ontoviewer.AnnotationMetaData ap2 = OntologyAnnotation
.getPropertyForTypeEnum(Settings.EXPRESSIONLESSRELATION);
if(ap2.isAutoannotate()) {
expressionlesslyAnnotateRelation(prop, text, m.getCurrentOntology(),
domains, ranges, ap2);
}
}
}

}


///////////////////////////////////////////////////////////////////////////////////  

///////////////////////////////////////////////////////////////////////////////////  

///////////////////////////////////////////////////////////////////////////////////  


  }