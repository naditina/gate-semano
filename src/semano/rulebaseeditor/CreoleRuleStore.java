package semano.rulebaseeditor;

import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.ontology.Ontology;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import semano.jape.JAPECompiler;
import semano.jape.RuleStoreAnnotator;
import semano.rulestore.AnnotationRule;
import semano.rulestore.Japelate;
import semano.rulestore.RuleStore;
import semano.rulestore.RuleStore.Type;

@CreoleResource(name = "Semano Rule Store", interfaceName = "gate.ProcessingResource", comment = "Rule base store created as a temporary in-memory repository", helpURL = "http://gate.ac.uk/userguide/sec:ontologies:ontoplugin:owlim")
public class CreoleRuleStore implements ProcessingResource {

  /**
   * 
   */
  private static final long serialVersionUID = 17598867348967L;

  // GUI viewer reference:
  public RuleBaseViewer ruleViewer;

  // path attributes:
  public static final String JPPARAM = "jprulesDir";

  public static final String JAPELATEPARAM = "japelatesDir";

  public static final String JAPE_JPRULES_ROOT = "data/jprules/";

  public static final String JAPE_JAPELATES_ROOT = "data/japelates/";  

  private static final String TEMP_JAPE_FILE = "/data/temp" + JAPECompiler.JAPE;

  private static final Object ONTOLOGY = "ontology";

  // gate-specific attribures:
  private FeatureMap featureMap;

  private String name;

  Logger logger = Logger.getLogger(this.getClass().getName());

  RuleStore ruleStore;

  private Ontology ontology;

  private Collection<StoreModificationListener> listeners=new  HashSet<>();

  private String pluginPath;

  public CreoleRuleStore() {
    super();
    featureMap = Factory.newFeatureMap();
    File pluginDir = new File(Gate.getGateHome().toString()+"/plugins/Semano/");
    try {
      pluginPath= pluginDir.getAbsoluteFile().toURI().toURL().toString();
    } catch(MalformedURLException e) {
      e.printStackTrace();
    }
    if(pluginPath!=null ){
      pluginPath=pluginPath.substring(5, pluginPath.length());
      System.out.println("using plugin directory: "+pluginPath);
      if(pluginDir.exists()){
        ruleStore = new RuleStore(pluginPath.toString()+JAPE_JPRULES_ROOT, pluginPath.toString()+JAPE_JAPELATES_ROOT);
        return;
      }      
    }
    ruleStore = new RuleStore(JAPE_JPRULES_ROOT, JAPE_JAPELATES_ROOT);
  }

  public CreoleRuleStore(String jprules, String japelates) {
    super();
    featureMap = Factory.newFeatureMap();
    ruleStore = new RuleStore(jprules, japelates);
  }

  // //////////////////
  // getters setters and CREOLE methods
  public RuleBaseViewer getRuleViewer() {
    return ruleViewer;
  }

  public void setRuleViewer(RuleBaseViewer ruleViewer) {
    this.ruleViewer = ruleViewer;
  }
  
  public LinkedHashMap<String, Japelate> getJapelates() {
    return ruleStore.getJapelateMap();
  } 

  @CreoleParameter(comment = "The URL of a directory containing a concepts and a relations directories with rule files")
  public void setJprulesDir(URL jprules) {
    ruleStore.setJprulesDir(jprules.getFile());
  }

  public URL getJprulesDir() {
    try {
      return new URL(ruleStore.getJprulesDir());
    } catch(MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  @CreoleParameter(comment = "The URL of a directory containing japelates")
  public void setJapelatesDir(URL japelates) {
    ruleStore.setJapelatesDir(japelates.getFile());
  }

  public URL getJapelatesDir() {
    try {
      return new URL(ruleStore.getJapelatesDir());
    } catch(MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  @CreoleParameter(comment = "Ontology to be used for annotation")
  public void setOntology(Ontology o) {
    ontology=o;
  }

  public Ontology getOntology() {
    return ontology;
  }

  @Override
  public Resource init() {
    ruleStore.init();
    return this;
  }

  @Override
  public void cleanup() {
    // TODO Auto-generated method stub

  }

  @Override
  public Object getParameterValue(String paramaterName)
          throws ResourceInstantiationException {
    if(featureMap != null) {
      return featureMap.get(paramaterName);
    }
    return null;
  }

  @Override
  public void setParameterValue(String paramaterName, Object parameterValue)
          throws ResourceInstantiationException {
    if(featureMap == null) {
      featureMap = Factory.newFeatureMap();
    }
    this.featureMap.put(paramaterName, parameterValue);
  }

  @Override
  public void setParameterValues(FeatureMap parameters)
          throws ResourceInstantiationException {
    if(featureMap == null) {
      featureMap = Factory.newFeatureMap();
    }
    if(parameters != null) {
      for(Object key : parameters.keySet()) {
        Object value = parameters.get(key);
        if(key != null && value != null) {
          this.featureMap.put(key, value);
          if(key.equals(JAPELATEPARAM)) {
            // remove file: prefix from the string
            ruleStore.setJapelatesDir(((String)value).substring(5));
          } else if(key.equals(JPPARAM)) {
            // remove file: prefix from the string
            ruleStore.setJprulesDir(((String)value).substring(5));
          } else if(key.equals(ONTOLOGY)) {
            setOntology((Ontology)value);
          }
        }
      }
    }
  }

  @Override
  public FeatureMap getFeatures() {
    return this.featureMap;
  }

  @Override
  public void setFeatures(FeatureMap features) {
    this.featureMap = features;

  }

  @Override
  public void setName(String name) {
    this.name = name;

  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void execute() throws ExecutionException {
    // TODO Auto-generated method stub

  }

  @Override
  public void interrupt() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isInterrupted() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void reInit() throws ResourceInstantiationException {
    // TODO Auto-generated method stub

  }

  
  
  
  
  
  
  
  //////////
  
  //  forwarded methods for rulestore
  
  ////////
  
  
  public Collection<AnnotationRule> getRules() {
    return ruleStore.getRules();
  }

  public void saveRules() {
    ruleStore.saveRules();    
  }

  public void deleteRule(String ruleID) {
    ruleStore.deleteRule(ruleID);    
  }

  public void addRule(Japelate japelate, List<String> params, Type type) {
    AnnotationRule rule = AnnotationRule.createFreshAnnotationRule(japelate, params.get(1), params.get(2),type);
    for(int i=AnnotationRule.MINIMUM_PARAMETER_NUMBER;i<params.size();i++){
      rule.addParameter(params.get(i));
    }
    ruleStore.addRule(rule,type);    
  }

  public void updateRule(AnnotationRule rule) {
    ruleStore.updateRule(rule);
    
  }

  public AnnotationRule getRule(String ruleID) {
    return ruleStore.getRule(ruleID);
  }
  
  
  //////////
  // events
  //////////

  public void fireStoreUpdatedEvent() {
   for(StoreModificationListener listener:listeners){
     listener.storeChanged();
   }
    
  }
  
  public void registerStoreModificationListener (StoreModificationListener listener){
    listeners.add(listener);
  }
  
  public void removeStoreModificationListener (StoreModificationListener listener){
    listeners.remove(listener);
  }

  
  
  /////////
  ///  annotation
  ////////
  
  
  
  public void annotateWithRule(String ruleID, Document doc) {
    AnnotationRule rule = getRule(ruleID);
    if(rule!=null){
      String JAPEfilename=JAPECompiler.convertRuleToJAPEFile(rule, pluginPath+TEMP_JAPE_FILE);
      RuleStoreAnnotator annotator = new RuleStoreAnnotator(JAPEfilename,this.ontology);
      annotator.annotateDoc(doc, doc.getAnnotations().getName());
    }
    
  }
  

  public void annotate(Document doc) {
    String japefile=JAPECompiler.convertJP2JAPE(ruleStore, Type.RELATION, pluginPath); 
    RuleStoreAnnotator annotator = new RuleStoreAnnotator(japefile,this.ontology);
    annotator.annotateDoc(doc, doc.getAnnotations().getName());
  }
  

  public void annotate(Document doc, Type ruleType) {
    String japefile=JAPECompiler.convertJP2JAPE(ruleStore, ruleType, pluginPath); 
    RuleStoreAnnotator annotator = new RuleStoreAnnotator(japefile,this.ontology);
    annotator.annotateDoc(doc, doc.getAnnotations().getName());
  }

}
