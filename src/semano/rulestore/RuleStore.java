/**
 *
 */
package semano.rulestore;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import semano.ontologyowl.ONodeIDImpl;
import semano.rulestore.AnnotationRule.RuleParseException;
import semano.util.FileAndDatastructureUtil;

/**
 * This is the key class to be used as part of the Java API.
 * 
 * It represents s rule store that can load a rule base and provides
 * various access methods for rules and japelates.
 * 
 * @author nadeschda
 * 
 */
public class RuleStore {

  /**
   * This is the type of an entity annotated by the  rule (concept or relation). This
   * is important as concept annotations will always be carried out
   * first.
   * 
   * @author nadeschda
   * 
   */
  public enum Type {
    CONCEPT, RELATION
  };

  // //default file parsing options:

  public static final String JP_FILE_EXTENSION = ".jprule";

  public static final String JAPELATE_FILE_EXTENSION = ".japelate";

  public static final String JAPELATE_HEADER_TAG = "JAPELATE HEADER:";

  public static final String JAPELATE_BODY_TAG = "JAPELATE BODY:";

  public static final String ABSTRACT_JAPELATE_BODY_TAG =
          "ABSTRACT JAPELATE BODY:";

  // //default paths to JPRules:
  private static final String CONCEPTS_SUBFOLDER = "concepts/";

  private static final String RELATIONS_SUBFOLDER = "relations/";

  Logger logger = Logger.getLogger(this.getClass().getName());

  // path attributes and GUI viewer reference:
  String jprulesDir, japelatesDir;

  // actual data:
  private HashMap<String, Set<AnnotationRule>> rulesConcepts =
          new HashMap<String, Set<AnnotationRule>>();

  private HashMap<String, Set<AnnotationRule>> rulesRelations =
          new HashMap<String, Set<AnnotationRule>>();

  protected LinkedHashMap<String, Japelate> japelateMap = new LinkedHashMap<>();

  private HashMap<String, AnnotationRule> ruleID2Rule = new LinkedHashMap<>();

  /**
   * main constructor. loads rules and japelates frm the provided
   * directory paths
   * 
   * @param jprulesDir directory that contains subfolders for rules of
   *          type CONCEPT and RELATION which in tern contain JP files
   * @param japelatesDir directory that contains japelate files
   */
  public RuleStore(String jprulesDir, String japelatesDir) {
    this.jprulesDir = jprulesDir;
    this.japelatesDir = japelatesDir;
  }

  //
  //
  // JAPELATE OPERATIONS
  //
  //

  private void loadJapelates() {
    FileFilter fileFilter = new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(JAPELATE_FILE_EXTENSION);
      }
    };
    File dir = new File(this.japelatesDir);
    if(dir != null && dir.exists()) {
      System.out.println("japelates...");
      for(File f : dir.listFiles(fileFilter)) {
        loadJaplate(f);
      }
    } else {
      System.err.println("directory " + dir == null ? this.japelatesDir : dir
              .getAbsolutePath() + " does not exist!");
    }

  }

  private void loadJaplate(File japelateFile) {
    String name =
            japelateFile.getName()
                    .substring(0, japelateFile.getName().lastIndexOf("."))
                    .trim();
    Japelate japelate = new Japelate(name, japelateFile);
    japelate.parseJapelate();
    this.japelateMap.put(name, japelate);
  }

  public Japelate getJapelate(String japelateName) {
    return this.japelateMap.get(japelateName);
  }

  public LinkedHashMap<String, Japelate> getJapelateMap() {
    return this.japelateMap;
  }

  //
  //
  // RULE OPERATIONS
  //
  //

  // loading rules from file

  private void loadRuleStore() {
    FileFilter fileFilter = new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(JP_FILE_EXTENSION);
      }
    };

    File dir = new File(this.getDirPath(Type.CONCEPT));
    if(dir != null && dir.exists()) {
      System.out.println("loading rules for concepts...");
      for(File f : dir.listFiles(fileFilter)) {
        parseRules(f, Type.CONCEPT);
      }
    } else {
      System.err.println("directory " + dir == null ? this
              .getDirPath(Type.CONCEPT) : dir.getAbsolutePath()
              + " does not exist!");
    }

    dir = new File(this.getDirPath(Type.RELATION));
    if(dir != null && dir.exists()) {
      System.out.println("loading rules for relations...");
      for(File f : dir.listFiles(fileFilter)) {
        parseRules(f, Type.RELATION);
      }
    } else {
      System.err.println("directory " + dir == null ? this
              .getDirPath(Type.RELATION) : dir.getAbsolutePath()
              + " does not exist!");
    }
  }

  private void parseRules(File file, Type type) {
    HashMap<String, Set<AnnotationRule>> rules = getRules(type);
    List<String> lines = FileAndDatastructureUtil.readStringsFromFile(file);
    String jpFileName =
            file.getName().substring(0,
                    file.getName().length() - JP_FILE_EXTENSION.length());
    String[] ontologyAndName = lines.get(0).split(",");
    // ontologyURI
    String ontologyURI = ontologyAndName[0].replaceAll("\"", "").trim();
    String className = ontologyAndName[1].replaceAll("\"", "").trim();
    if(!jpFileName.equals(getPhasenameForConcept(className))) {
      System.err.println("Warning: filename of JP file " + jpFileName
              + " does not reflect the entity name " + className);
    }
    // overwriting current rules in memory with those from file:
    rules.put(className, new LinkedHashSet<AnnotationRule>());
    lines.remove(0);
    Iterator<String> ruleIterator = lines.iterator();
    while(ruleIterator.hasNext()) {
      String next = ruleIterator.next();
      if(!next.isEmpty()) {
        try {
          AnnotationRule rule =
                  new AnnotationRule(next, ontologyURI, className, type);
          ruleID2Rule.put(rule.getName(), rule);
          rule.setJapelate(this.japelateMap.get(rule.getJapelateName()));
          rules.get(className).add(rule);
        } catch(RuleParseException e) {
          System.err.println(e.getMessage());
        }
      }
    }
  }

  // writing rules to file

  /**
   * writes all rules back to the harddrive
   */
  public void saveRules() {
    saveRules(Type.CONCEPT);
    saveRules(Type.RELATION);
  }

  /**
   * writes rules of the given type (concept, relation) back to the
   * harddrive
   * 
   * @param type the type (concept, relation) of rules to be saved
   */
  public void saveRules(Type type) {
    HashMap<String, Set<AnnotationRule>> rules = getRules(type);
    String dirPath = getDirPath(type);
    FileAndDatastructureUtil.createDirectory(dirPath, true);
    int totalRules = 0;
    for(String phasename : rules.keySet()) {
      if(!rules.get(phasename).isEmpty()) {
        String fname =
                dirPath + getPhasenameForConcept(phasename) + JP_FILE_EXTENSION;
        // writing header:
        AnnotationRule firstRule = rules.get(phasename).iterator().next();
        FileAndDatastructureUtil.writeStringToFile(
                fname,
                RuleStore.generateHeader(firstRule.getEntityIRI(),
                        firstRule.getOntology()), true);
        // writing all rules:
        for(AnnotationRule rule : rules.get(phasename)) {
          FileAndDatastructureUtil.appendStringToFile(rule.getCode(), fname);
          totalRules++;
        }
      }
    }
    System.out.println("total rules saved: " + totalRules);

  }

  /**
   * writes rules of the given entity back to the harddrive
   * 
   * @param entityIRI the ontology entity for which the rules need to be
   *          saved
   */
  public void saveRules(String entityIRI) {
    Set<AnnotationRule> set;
    Type type;
    if(getRules(Type.CONCEPT).containsKey(entityIRI)) {
      set = getRules(entityIRI, Type.CONCEPT);
      type = Type.CONCEPT;
    } else {
      set = getRules(entityIRI, Type.RELATION);
      type = Type.RELATION;
    }
    if(set != null) {
      String dirPath = getDirPath(type);
      int totalRules = 0;
      if(!set.isEmpty()) {
        String phasename = getPhasenameForConcept(entityIRI);
        String fname = dirPath + phasename + JP_FILE_EXTENSION;
        // writing header:
        AnnotationRule firstRule = set.iterator().next();
        FileAndDatastructureUtil.writeStringToFile(
                fname,
                RuleStore.generateHeader(firstRule.getEntityIRI(),
                        firstRule.getOntology()), true);
        // writing all rules:
        for(AnnotationRule rule : set) {
          FileAndDatastructureUtil.appendStringToFile(rule.getCode(), fname);
          totalRules++;
        }
        System.out.println("total rules saved for " + phasename + ": "
                + totalRules);
      }
    }
  }

  /**
   * writes all rules of the entity containing the rule with the given
   * rule ID back to the harddrive
   * 
   * @param ruleID that belongs to the ontology entity for which the
   *          rules need to be saved
   */
  public void saveRule(String ruleID) {
    AnnotationRule annotationRule = ruleID2Rule.get(ruleID);
    if(annotationRule != null) {
      saveRules(annotationRule.getEntityIRI());
    }
  }

  // // getting rules

  /**
   * retrieves loaded rules for the given type sorted into sets
   * according to ontology entities
   * 
   * @param type the type (concept, relation) of rules to be retrieved
   * @return an associative array containing a set of rules per ontology
   *         entities
   */
  public HashMap<String, Set<AnnotationRule>> getRules(Type type) {
    if(Type.CONCEPT.equals(type)) {
      return rulesConcepts;
    }
    return rulesRelations;
  }

  /**
   * 
   * retrieves all loaded rules sorted into sets according to ontology
   * entities
   * 
   * @return an associative array containing a set of rules per ontology
   *         entities
   */
  public Collection<AnnotationRule> getRules() {
    LinkedHashSet<AnnotationRule> result = new LinkedHashSet<>();
    HashMap<String, Set<AnnotationRule>> ruleMap = this.getRules(Type.CONCEPT);
    for(String phaseName : ruleMap.keySet()) {
      result.addAll(ruleMap.get(phaseName));
    }
    ruleMap = this.getRules(Type.RELATION);
    for(String phaseName : ruleMap.keySet()) {
      result.addAll(ruleMap.get(phaseName));
    }
    return result;
  }

  /**
   * retrieves loaded rules for the given ontology entity
   * 
   * @param entityIRI IRI of the entity for which to retrieve rules
   * @param type the type of the entity for which to retrieve rules
   * @return set of rules for the given ontology entity
   */
  public Set<AnnotationRule> getRules(String entityIRI, Type type) {
    HashMap<String, Set<AnnotationRule>> rules = getRules(type);
    return rules.get(entityIRI);
  }

  /**
   * retrieves the loaded rule with the given ID
   * 
   * @param ruleID for which to retrieve the rule
   * @return rule with the given ID
   */
  public AnnotationRule getRule(String ruleID) {
    return ruleID2Rule.get(ruleID);
  }

  // / adding rules

  /**
   * adds a rule of the given type to the rule base (does not
   * automatically save the base to the harddrive)
   * 
   * @param rule the ruke to be added
   * @param type the type (concept, relation) of the new rule
   */
  public void addRule(AnnotationRule rule, Type type) {
    if(rule == null) return;
    HashMap<String, Set<AnnotationRule>> rules = getRules(type);
    Set<AnnotationRule> rulesForName = rules.get(rule.getEntityIRI());
    if(rulesForName == null) {
      rulesForName = new HashSet<AnnotationRule>();
    }
    rulesForName.add(rule);
    rules.put(rule.getEntityIRI(), rulesForName);
    ruleID2Rule.put(rule.getName(), rule);
  }

  // deleting rules

  /**
   * removes the loaded rule with the given ID from the rule base
   * 
   * @param ruleID for the rule to be deleted
   */
  public void deleteRule(String ruleID) {
    AnnotationRule rule = ruleID2Rule.get(ruleID);
    if(rule != null) {
      ruleID2Rule.remove(ruleID);
      Set<AnnotationRule> set;
      String entityIRI = rule.getEntityIRI();
      if(getRules(Type.CONCEPT).containsKey(entityIRI)) {
        set = getRules(entityIRI, Type.CONCEPT);
      } else {
        set = getRules(entityIRI, Type.RELATION);
      }
      if(set != null) {
        set.remove(rule);
      }
    }

  }

  /**
   * clears the entire rule base
   */
  public void deleteRules() {
    getRules(Type.CONCEPT).clear();
    getRules(Type.RELATION).clear();
    ruleID2Rule.clear();
  }

  /**
   * removes the loaded rule of the given ontology entity from the rule
   * base
   * 
   * @param entityIRI the IRI of the ontology entity for which to remove
   *          the rules
   */
  public void deleteRules(String entityIRI) {
    Set<AnnotationRule> set;
    if(getRules(Type.CONCEPT).containsKey(entityIRI)) {
      set = getRules(entityIRI, Type.CONCEPT);
    } else {
      set = getRules(entityIRI, Type.RELATION);
    }
    if(set != null) {
      for(AnnotationRule r : set) {
        ruleID2Rule.remove(r);
      }
      set.clear();
    }
  }

  // ////////////////
  // updating rules

  // ////////////////

  /**
   * updates the rule within the rule base. While rules are not
   * immutable, the index structures of the rule base need to be updated
   * when a rules changes.
   * 
   * @param rule to be updated within the rule base
   */
  public void updateRule(AnnotationRule rule) {
    if(rule != null) {
      deleteRule(rule.getName());
      addRule(rule, rule.getType());
    }

  }

  // ////////////////

  // / getters setters helpers

  // //////////////

  /**
   * returns the directory that contains subfolders for rules of
   *          type CONCEPT and RELATION which in tern contain JP files
   * @return the currently used path to the JP Rules directory
   */
  public String getJprulesDir() {
    return jprulesDir;
  }

  /**
   * sets the directory that contains subfolders for rules of
   *          type CONCEPT and RELATION which in tern contain JP files
   * @param jprules the new path to the JP Rules directory
   */
  public void setJprulesDir(String jprules) {
    this.jprulesDir = jprules;
  }

  /**
   * @return the used path to the directory that contains japelate files
   */
  public String getJapelatesDir() {
    return japelatesDir;
  }

  /**
   * @param japelates the new path to the directory that contains japelate files
   */
  public void setJapelatesDir(String japelates) {
    this.japelatesDir = japelates;
  }

  /**
     * initializes a rule base and loads the data from the harddrive
     */
  public void init() {
    loadJapelates();
    loadRuleStore();
  }

  /**
   * initializes an empty rule base
   */
  public void initFreshRuleStore() {
    loadJapelates();
  }

  private static String generateHeader(String entityURI, String ontologyURI) {
    return ontologyURI + ", " + entityURI;
  }

  private String getDirPath(Type type) {
    if(Type.CONCEPT.equals(type)) {
      return this.jprulesDir + CONCEPTS_SUBFOLDER;
    }
    return this.jprulesDir + RELATIONS_SUBFOLDER;
  }

  /**
   * a procedure translating an ontology entity name into a filename of a JP file
   * @param entityIRI the IRI of the entity to be translated into a filename
   * @return a filename of a JP file for the given ontology entity
   */
  public static String getPhasenameForConcept(String entityIRI) {
    String result = new ONodeIDImpl(entityIRI, false).getResourceName();
    return result.replaceAll("[^a-zA-Z0-9]", "");
  }

}
