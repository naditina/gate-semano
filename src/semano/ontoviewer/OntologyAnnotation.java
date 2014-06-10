/*
 *  AnnotationImpl.java
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, Jan/00
 *
 *  $Id: AnnotationImpl.java 10555 2009-06-17 15:30:12Z ian_roberts $
 */

package semano.ontoviewer;

import gate.Annotation;
import gate.FeatureMap;
import gate.Node;
import gate.annotation.AnnotationImpl;
import gate.creole.ontology.AnnotationProperty;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OResource;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.OntologyUtilities;
import gate.creole.ontology.RDFProperty;
import gate.creole.ontology.URI;
import gate.gui.MainFrame;

import java.awt.HeadlessException;
import java.util.List;

import javax.swing.JOptionPane;

import semano.ontologyowl.AnnotationValue;
import semano.util.OntologyUtil;
import semano.util.Settings;


/**
 * Provides an implementation for the interface gate.Annotation
 * 
 */
public class OntologyAnnotation extends AnnotationImpl {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  //
  // public enum Feature{
  // INSTANCE,
  // SYNONYM,
  // ACRONYM,
  // EXPRESSION,
  // PASSAGE,
  // FORMULA,
  // NEWENTITY,
  // EXPRESSIONLESSRELATION
  // }

  public OntologyAnnotation(Integer id, Node start, Node end, String type,
          FeatureMap features) {
    super(id, start, end, type, features);
  }

  public OntologyAnnotation(Annotation a) {
    // if the features are set without copying
    super(a.getId(), a.getStartNode(), a.getEndNode(), a.getType(), a
            .getFeatures());
  }

  public static semano.ontoviewer.AnnotationMetaData getPropertyForTypeEnum(
          String annotationFeatureName) {
    for(semano.ontoviewer.AnnotationMetaData ap : Settings.annotationProperties) {
      if(ap.getEnumName().equals(annotationFeatureName)) {
        return ap;
      }
    }
    return null;
  }

  public static semano.ontoviewer.AnnotationMetaData getPropertyForTypeLabel(
          String annotationFeatureName) {
    for(semano.ontoviewer.AnnotationMetaData ap : Settings.annotationProperties) {
      if(ap.getGuiLabel().equals(annotationFeatureName)) {
        return ap;
      }
    }
    return null;
  }

  public static semano.ontoviewer.AnnotationMetaData getPropertyForTypeUri(
          String uri) {
    for(semano.ontoviewer.AnnotationMetaData ap : Settings.annotationProperties) {
      if(AnnotationValue.extractSimpleUri(uri).equals(OntologyAnnotation.getSimplePropertyNameForAnnotationType(ap))) {
        return ap;
      }
    }
    return null;
  }

  public static String getSimplePropertyNameForAnnotationType(
          semano.ontoviewer.AnnotationMetaData annotationFeature) {
    return AnnotationValue.extractSimpleUri(annotationFeature.getUri());
  }

  /**
   * @return value of the annotation
   */
  public String getValue() {
    if(getFeatures().containsKey(Settings.ANNOTATION_VALUE)) {
      return (String)getFeatures().get(Settings.ANNOTATION_VALUE);
    }
    return null;
  }

  @SuppressWarnings("deprecation")
  public OResource getResource(Ontology o) {
    Object node = (getFeatures()
            .get(Settings.DEFAULT_INSTANCE_URI_FEATURE_NAME));
    if(node == null) {
      node = (getFeatures().get(Settings.DEFAULT_CLASS_URI_FEATURE_NAME));
    }
    if(node == null) {
      node = (getFeatures().get(Settings.DEFAULT_PROPERTY_URI_FEATURE_NAME));
    }
    if(node instanceof OResource) {
      return (OResource)node;
    }
    else {

      List<OResource> res = o.getOResourcesByName((String)node);
      
      if(res != null && !res.isEmpty()) {
        if(res.size() == 1)
          return res.get(0);
        else {
          String ontologyURI = (String)getFeatures().get(
                  gate.creole.ANNIEConstants.LOOKUP_ONTOLOGY_FEATURE_NAME);
          if(ontologyURI != null && !ontologyURI.isEmpty()) {
            for(OResource r : res) {
              if(ontologyURI.equals(getOntologyUri(r))) {
                return r;
              }
            }
          }
        }
      }else{
        OResource r = o.getOResourceFromMap(((String)node));
        return r;
        
      }
      return null;
    }

  }

  public static String getSourceFeatureValue(Ontology o, Annotation oa) {
    if(!(oa.getFeatures().containsKey(Settings.DEFAULT_CLASS_URI_FEATURE_NAME)))
      return null;
    
    
    String value = (String)oa.getFeatures().get(Settings.DEFAULT_CLASS_URI_FEATURE_NAME);
    
    return value;
//    String aName = null;
//    OResource node = ((OntologyAnnotation)oa).getResource(o);
//    if(node != null && node.getURI() != null) {
//      aName = node.getURI().toString();
//      if(aName == null || aName.isEmpty())
//        aName = node.getONodeID().toString();
//    }
//    if(aName == null) return null;
//    int index = aName.lastIndexOf("#");
//    if(index < 0) index = aName.lastIndexOf("/");
//    if(index < 0) index = aName.lastIndexOf(":");
//    if(index >= 0) {
//      aName = aName.substring(index + 1, aName.length());
//    }
//    return aName;
  }

  public boolean isSourceAClass() {
    return getFeatures().containsKey(Settings.DEFAULT_CLASS_URI_FEATURE_NAME);
  }

  public static boolean isSourceAClass(gate.Annotation annot) {
    return annot.getFeatures().containsKey(
            Settings.DEFAULT_CLASS_URI_FEATURE_NAME);
  }
  /**
   * required when initializing annotationManager and its Lists
   * 
   * @param currentAnnot
   * @return
   */
  public static String getSourceFeatureValue(Annotation currentAnnot) {
    if(currentAnnot != null && currentAnnot.getFeatures() != null) {
      String node = (String)(currentAnnot.getFeatures()
              .get(Settings.DEFAULT_INSTANCE_URI_FEATURE_NAME));
      if(node == null) {
        node =  (String)(currentAnnot.getFeatures()
                .get(Settings.DEFAULT_CLASS_URI_FEATURE_NAME));
      }
      if(node == null) {
        node =  (String)(currentAnnot.getFeatures()
                .get(Settings.DEFAULT_PROPERTY_URI_FEATURE_NAME));
      }
      if(node == null) {
        // System.err.println("/nCannot find a class or property or instance name for annotation with features: "+currentAnnot.getFeatures().toString());
        return "";
      }
      return OntologyUtil.extractEntityName(node);
    }
    return "";
  }

  /**
   * Given an Annotation this method gets the value of
   * gate.creole.ANNIEConstants.LOOKUP_INSTANCE_FEATURE_NAME feature.
   * 
   * @param annot
   * @return
   */
  public static String getInstanceFeatureValue(gate.Annotation annot, Ontology o) {

    return getSourceFeatureValue(o,annot);
  }

  /**
   * Given an Annotation this method gets the value of
   * gate.creole.ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME feature.
   * 
   * @param annot
   * @return
   */
  public static String getClassFeatureValue(gate.Annotation annot, Ontology o) {

    return getSourceFeatureValue(o,annot);
  }

  public boolean isManuallySetAnnotation() {
    if(getFeatures().containsKey(Settings.AUTOANNOTATION)) {
      return !getFeatures().get(Settings.AUTOANNOTATION).equals(
              String.valueOf(true));
    }
    return true;
  }

  /**
   * extracts the ontology uri of the resource
   * 
   * @param node
   * @return
   */
  public static String getOntologyUri(OResource node) {
    String dns = null;
    String uri = node.getURI().getNameSpace();
    if(uri == null || uri.isEmpty()) {
      uri = node.getONodeID().getNameSpace();
    }
    if(uri.contains(".owl") && uri.length() > 5)
      dns = uri.substring(0, uri.lastIndexOf(".owl") + 4);
    else if(uri.contains("/") && uri.length() > 2) {
      dns = uri.substring(0, uri.lastIndexOf("/") + 1);
    }
    return dns;
  }

  public static String getAnnotationType(
          Annotation currentAnnotation) {
    if(currentAnnotation.getFeatures().containsKey(Settings.ANNOTATION_TYPE)) {
      if(currentAnnotation.getFeatures().get(Settings.ANNOTATION_TYPE) instanceof String) {
        return ((String)currentAnnotation.getFeatures().get(
                Settings.ANNOTATION_TYPE));
      }
    }
    return null;
  }

  public static String extractAnnotationType(Annotation currentAnnotation) {
    FeatureMap currentAnnotationFeatures = currentAnnotation.getFeatures();
    if(currentAnnotationFeatures.containsKey(Settings.ANNOTATION_TYPE)) {
      if(currentAnnotationFeatures.get(Settings.ANNOTATION_TYPE) instanceof String)
        return (String)currentAnnotation.getFeatures().get(
                Settings.ANNOTATION_TYPE);
      else {
        String result = null;
        try {
          result = ((String)currentAnnotationFeatures.get(
                  Settings.ANNOTATION_TYPE));
          return result;
        }
        catch(Exception e) {
          // here the String value is not a valid annotation type value
          // any more
          System.err
                  .println("the annotation type value "
                          + (String)currentAnnotationFeatures.get(
                                  Settings.ANNOTATION_TYPE)
                          + " is not a valid annotation type value. This can occur if an old annotated document is opeden with a newer version of the tool. Please ask the developers.");
        }
      }
    }
    return null;
  }

  public static boolean isManuallySetAnnotation(Annotation annotation) {
    FeatureMap annotationFeatures = annotation.getFeatures(); 
    if(annotationFeatures.containsKey(Settings.AUTOANNOTATION)) {
      return !annotationFeatures.get(Settings.AUTOANNOTATION)
              .equals(String.valueOf(true));
    }
    return true;
  }

  public static boolean isAntipattern(Annotation annotation) {
    FeatureMap annotationFeatures = annotation.getFeatures(); 
    if(annotationFeatures.containsKey(Settings.ANTIPATTERN)) {
      return annotationFeatures.get(Settings.ANTIPATTERN)
              .equals(String.valueOf(true));
    }
    return false;
  }

  public void setType(String newAnnotationType) {
    getFeatures().put(Settings.ANNOTATION_TYPE, newAnnotationType);
  }

  public void setResource(OResource newEntity) {
    if(getFeatures().get(Settings.DEFAULT_INSTANCE_URI_FEATURE_NAME) != null) {
      getFeatures().put(Settings.DEFAULT_INSTANCE_URI_FEATURE_NAME,
              newEntity.toString());
    }
    else if(getFeatures().get(Settings.DEFAULT_CLASS_URI_FEATURE_NAME) != null) {
      getFeatures().put(Settings.DEFAULT_CLASS_URI_FEATURE_NAME,
              newEntity.toString());
    }
    if(getFeatures().get(Settings.DEFAULT_PROPERTY_URI_FEATURE_NAME) != null) {
      getFeatures().put(Settings.DEFAULT_PROPERTY_URI_FEATURE_NAME,
              newEntity.toString());
    }

  }

  //
  // public AnnotationProperty getAnnotationProperty(Ontology o){
  // AnnotationProperty aProp = null;
  // String propName
  // =OntologyAnnotation.getPropertyNameForAnnotationType(getAnnotationType().getName());
  // if(propName != null){
  // OResource prop=o.getOResourceByName(propName);
  // if(prop != null && prop instanceof AnnotationProperty) {
  // return (AnnotationProperty)prop;
  // }
  // }
  // return null;
  // }

  /**
   * @param newMap
   */
  public static void removeSource(FeatureMap newMap) {
    newMap.remove(getClassTag());
    newMap.remove(getInstanceTag());
    newMap.remove(getPropertyTag());
  }

  private static String getTag(Object object) {
    // create Instance for a class
    if(object instanceof OClass) {
      return getClassTag();
      // or for a property
    }
    else if(object instanceof RDFProperty) {
      return getPropertyTag();
    }
    return null;
  }

  /**
   * @return
   */
  private static String getClassTag() {
    return Settings.DEFAULT_CLASS_URI_FEATURE_NAME;
  }

  /**
   * @return
   */
  private static String getInstanceTag() {
    return Settings.DEFAULT_INSTANCE_URI_FEATURE_NAME;
  }

  /**
   * @return
   */
  private static String getPropertyTag() {
    return Settings.DEFAULT_PROPERTY_URI_FEATURE_NAME;
  }

  /**
   * @param node
   * @param newMap
   */
  public static void setSource(OResource node, FeatureMap newMap) {
    String tag = getTag(node);
    if(tag != null) {
      newMap.put(tag, node.toString());
    }
  }

  /**
   * @param annotationProperty
   * @throws HeadlessException
   */
  public static AnnotationProperty getAnnotationProperty(
          semano.ontoviewer.AnnotationMetaData annotationProperty,
          Ontology o) throws HeadlessException {
    AnnotationProperty aProp = null;
    String propName = annotationProperty.getUri();
    if(propName != null) {
      OResource prop = o.getOResourceByName(propName);
      if(prop != null) {
        if(!(prop instanceof AnnotationProperty)) {
          JOptionPane.showMessageDialog(MainFrame.getInstance(), propName
                  + " is not a valid annotation property");
        }
        else {
          aProp = (AnnotationProperty)prop;
        }
      }
      else {
        // lets add this property in ontology
        URI annPropURI = OntologyUtilities.createURI(o, propName, false);
        aProp = o.addAnnotationProperty(annPropURI);
      }
    }
    return aProp;
  }

  public static boolean hasAnnotationType(Annotation currentAnnotation,
          String annoType) {
    return (annoType.equals(extractAnnotationType(currentAnnotation)));
  }

  public static boolean isSourceAProperty(Annotation currentAnnotation) {
    return currentAnnotation.getFeatures().containsKey(
            Settings.DEFAULT_PROPERTY_URI_FEATURE_NAME);
  }

  private static Object getFeatureMapValue(Annotation currentAnnot, String key) {
    Object node= null;
    if(currentAnnot != null && currentAnnot.getFeatures() != null) {
      node = (currentAnnot.getFeatures().get(key));
      if(node != null) {
      return  node;
//      if(node == null) {
////        System.out.println("Cannot find the value of " + key
////                + " for annotation with features: "
////                + currentAnnot.getFeatures().toString());
//        return "";
//      }
//      else {
//        return node.toString();
      }
    }
    System.err.println("Annotation is null or its features ");
    return "";
  }

  public static String getDomainClass(Annotation currentAnnot) {
    return getFeatureMapValue(currentAnnot,
            Settings.DEFAULT_DOMAIN_URI_FEATURE_NAME).toString();
  }

  public static String getRangeClass(Annotation currentAnnot) {
    return getFeatureMapValue(currentAnnot,
            Settings.DEFAULT_RANGE_URI_FEATURE_NAME).toString();
  }

  public static String getSummary(Annotation a) {
    if(a == null) 
      return "NULL";
    StringBuilder result = new StringBuilder();
    OntologyAnnotation ontologyAnnotation = new OntologyAnnotation(a);
    result.append(ontologyAnnotation.getValue() + "[");
    result.append(getSourceFeatureValue(ontologyAnnotation) + ", ");
    result.append(OntologyAnnotation.getAnnotationType(ontologyAnnotation) + ", ");
    result.append(ontologyAnnotation.getStartNode().getOffset() + ", ");
    result.append(ontologyAnnotation.getId() + "]");
    return result.toString();
  }
  

  public static Integer getAnnotationIdFromSummary(String summary){
    if(summary != null) {
      String id = summary.split(",")[3];
      if(id != null) {
        String idtr = id.substring(0, id.length()-1).trim();
        return Integer.parseInt(idtr);
      }
    }
    return null;
  }

  public static void setDomainAnnotation(Annotation domain,
          FeatureMap featureMap) {
    if(featureMap != null) {
      featureMap.put(Settings.DEFAULT_DOMAIN_ANNOTATION_URI_FEATURE_NAME,
              domain);
    }

  }
  
  public static void resetDomainAnnotation(
          FeatureMap featureMap) {
    featureMap.remove(Settings.DEFAULT_DOMAIN_ANNOTATION_URI_FEATURE_NAME);
  }
  
  public Annotation getDomainAnnotation(AnnotationStore am) {
    return (Annotation)OntologyAnnotation.getDomainAnnotation(this.features,am);
  }

  


  public static void resetRangeAnnotation(
          FeatureMap featureMap) {    
      featureMap.remove(Settings.DEFAULT_RANGE_ANNOTATION_URI_FEATURE_NAME);
    }

  public static void setRangeAnnotation(Annotation ra,
          FeatureMap featureMap) {
    if(featureMap != null) {
      featureMap.put(Settings.DEFAULT_RANGE_ANNOTATION_URI_FEATURE_NAME,
              ra);
    }

  }
  
  public Annotation getRangeAnnotation(AnnotationStore am) {
    return (Annotation)OntologyAnnotation.getRangeAnnotation(this.features,am);
  }

  public static void setDomain(String domainString, FeatureMap featureMap) {
    if(featureMap != null) {
      featureMap.put(Settings.DEFAULT_DOMAIN_URI_FEATURE_NAME, domainString);
    }

  }

  public static void setRange(String rangeString, FeatureMap featureMap) {
    if(featureMap != null) {
      featureMap.put(Settings.DEFAULT_RANGE_URI_FEATURE_NAME, rangeString);
    }

  }

  public static String getTypeLabel(Annotation currentAnnotation) {
    return getAnnotationType(currentAnnotation);
  }
  

  public static Annotation getRangeAnnotation(FeatureMap map, AnnotationStore am) {
    if(map!=null){        
      Object annotObj = map.get(Settings.DEFAULT_RANGE_ANNOTATION_URI_FEATURE_NAME);
      if(annotObj!=null){
        if(annotObj.toString().startsWith(AnnotationImpl.class.getSimpleName())){
          String id=annotObj.toString().split("=")[1];
          String idd = id.split(";")[0];
          return am.getAnnotation(Integer.parseInt(idd.trim()));
        }else{
          if(annotObj instanceof Annotation){
            return (Annotation)annotObj;          
          }else{
            String summary = (String)annotObj;
            return am.getAnnotation(getAnnotationIdFromSummary(summary));
          }
        }
      }
    }
  return null;
  }


  public static Annotation getDomainAnnotation(FeatureMap map, AnnotationStore am) {
    if(map!=null){        
        Object annotObj = map.get(Settings.DEFAULT_DOMAIN_ANNOTATION_URI_FEATURE_NAME);
        if(annotObj!=null){
          if(annotObj.toString().startsWith(AnnotationImpl.class.getSimpleName())){
            String id=annotObj.toString().split("=")[1];
            String idd = id.split(";")[0];
            return am.getAnnotation(Integer.parseInt(idd.trim()));
          }else{
            if(annotObj instanceof Annotation){
              return (Annotation)annotObj;          
            }else{
              String summary = (String)annotObj;
              return am.getAnnotation(getAnnotationIdFromSummary(summary));
            }
          }
        }
      }
    return null;
  }
  
  public static String getRangeAnnotation(OntologyAnnotation oa) {
    if(oa != null && oa.getFeatures() != null) {
      Object o = oa.getFeatures().get(
              Settings.DEFAULT_RANGE_ANNOTATION_URI_FEATURE_NAME);
      if(null != o) {
        if(o instanceof String) {
          return (String)o;
        }
        else if(o instanceof Annotation) {
          return getSummary((Annotation)o);
        }
      }
    }
    return null;
  }

  public static String getDomainAnnotation(OntologyAnnotation oa) {
    if(oa != null && oa.getFeatures() != null) {
      Object o = oa.getFeatures().get(
              Settings.DEFAULT_DOMAIN_ANNOTATION_URI_FEATURE_NAME);
      if(null != o) {
        if(o instanceof String) {
          return (String)o;
        }
        else if(o instanceof Annotation) {
          return getSummary((Annotation)o);
        }
      }
    }
    return null;
  }

  public static String getRuleID(Annotation oa) {
    if(oa != null && oa.getFeatures() != null) {
      Object o = oa.getFeatures().get(
              Settings.RULE_FEATURE_NAME);
      if(null != o) {
        if(o instanceof String) {
          return (String)o;
        }
      }
    }
    return null;
  }

 // class AnnotationImpl
}
