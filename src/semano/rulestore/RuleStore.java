/**
 *
 */
package semano.rulestore;

import org.apache.log4j.Logger;
import semano.ontologyowl.ONodeIDImpl;
import semano.rulestore.AnnotationRule.RuleParseException;
import semano.util.FileAndDownloadUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.*;


public class RuleStore {

    public enum Type {CONCEPT, RELATION};

    ////default file parsing options:
    public static final String JP = ".jprule";
    public static final String JAPELATE = ".japelate";
    public static final String JAPELATE_HEADER_TAG = "JAPELATE HEADER:";
    public static final String JAPELATE_BODY_TAG = "JAPELATE BODY:";
    public static final String ABSTRACT_JAPELATE_BODY_TAG = "ABSTRACT JAPELATE BODY:";
    ////default paths to JPRules:
    private static final String CONCEPTS = "concepts/";
    private static final String RELATIONS = "relations/";
    Logger logger = Logger.getLogger(this.getClass().getName());
    // path attributes and GUI viewer reference:
    String jprulesDir, japelatesDir;
    // actual data:
    private HashMap<String, Set<AnnotationRule>> rulesConcepts = new HashMap<String, Set<AnnotationRule>>();
    private HashMap<String, Set<AnnotationRule>> rulesRelations = new HashMap<String, Set<AnnotationRule>>();
    protected LinkedHashMap<String, Japelate> japelateMap = new LinkedHashMap<>();
    private HashMap<String, AnnotationRule> ruleID2Rule = new LinkedHashMap<>();


    public RuleStore(String jprulesDir, String japelatesDir) {
        this.jprulesDir = jprulesDir;
        this.japelatesDir = japelatesDir;
    }


    //
    //
    //    JAPELATE OPERATIONS
    //
    //


    private void loadJapelates() {
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(JAPELATE);
            }
        };
        File dir = new File(this.japelatesDir);
        if (dir != null && dir.exists()) {
            System.out.println("japelates...");
            for (File f : dir.listFiles(fileFilter)) {
                loadJaplate(f);
            }
        } else {
            System.err.println("directory " + dir == null ? this.japelatesDir : dir.getAbsolutePath() + " does not exist!");
        }

    }

    private void loadJaplate(File japelateFile) {
        String name = japelateFile.getName().substring(0, japelateFile.getName().lastIndexOf(".")).trim();
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
//    RULE OPERATIONS
//
//


// loading rules from file


    private void loadRuleStore() {
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(JP);
            }
        };

        File dir = new File(this.getDirPath(Type.CONCEPT));
        if (dir != null && dir.exists()) {
            System.out.println("loading rules for concepts...");
            for (File f : dir.listFiles(fileFilter)) {
                parseRules(f, Type.CONCEPT);
            }
        } else {
            System.err.println("directory " + dir == null ? this.getDirPath(Type.CONCEPT) : dir.getAbsolutePath() + " does not exist!");
        }

        dir = new File(this.getDirPath(Type.RELATION));
        if (dir != null && dir.exists()) {
            System.out.println("loading rules for relations...");
            for (File f : dir.listFiles(fileFilter)) {
                parseRules(f, Type.RELATION);
            }
        } else {
            System.err.println("directory " + dir == null ? this.getDirPath(Type.RELATION) : dir.getAbsolutePath() + " does not exist!");
        }
    }


    private void parseRules(File file, Type type) {
        HashMap<String, Set<AnnotationRule>> rules = getRules(type);
        List<String> lines = FileAndDownloadUtil.readStringsFromFile(file);
        String jpFileName = file.getName().substring(0, file.getName().length() - JP.length());
        String[] ontologyAndName = lines.get(0).split(",");
        //ontologyURI
        String ontologyURI = ontologyAndName[0].replaceAll("\"", "").trim();
        String className = ontologyAndName[1].replaceAll("\"", "").trim();
        if (!jpFileName.equals(getPhasenameForConcept(className))) {
            System.err.println("Warning: filename of JP file " + jpFileName + " does not reflect the entity name " + className);
        }
        //overwriting current rules in memory with those from file:
        rules.put(className, new LinkedHashSet<AnnotationRule>());
        lines.remove(0);
        Iterator<String> ruleIterator = lines.iterator();
        while (ruleIterator.hasNext()) {
            String next = ruleIterator.next();
            if (!next.isEmpty()) {
                try {
                    AnnotationRule rule = new AnnotationRule(next, ontologyURI, className, type);
                    ruleID2Rule.put(rule.getName(), rule);
                    rule.setJapelate(this.japelateMap.get(rule.getJapelateName()));
                    rules.get(className).add(rule);
                } catch (RuleParseException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }


//writing rules to file

    public void saveRules() {
        saveRules(Type.CONCEPT);
        saveRules(Type.RELATION);
    }

    public void saveRules(Type type) {
        HashMap<String, Set<AnnotationRule>> rules = getRules(type);
        String dirPath = getDirPath(type);
        FileAndDownloadUtil.createDirectory(dirPath, true);
        int totalRules = 0;
        for (String phasename : rules.keySet()) {
            if (!rules.get(phasename).isEmpty()) {
                String fname = dirPath + getPhasenameForConcept(phasename) + JP;
                // writing header:
                AnnotationRule firstRule = rules.get(phasename).iterator().next();
                FileAndDownloadUtil.writeStringToFile(fname, RuleStore.generateHeader(firstRule.getClas(), firstRule.getOntology()),
                        true);
                //writing all rules:
                for (AnnotationRule rule : rules.get(phasename)) {
                    FileAndDownloadUtil.appendStringToFile(rule.getCode(), fname);
                    totalRules++;
                }
            }
        }
        System.out.println("total rules saved: " + totalRules);

    }

    public void saveRules(String entityIRI) {
        Set<AnnotationRule> set;
        Type type;
        if (getRules(Type.CONCEPT).containsKey(entityIRI)) {
            set = getRules(entityIRI, Type.CONCEPT);
            type = Type.CONCEPT;
        } else {
            set = getRules(entityIRI, Type.RELATION);
            type = Type.RELATION;
        }
        if (set != null) {
            String dirPath = getDirPath(type);
            int totalRules = 0;
            if (!set.isEmpty()) {
                String phasename = getPhasenameForConcept(entityIRI);
                String fname = dirPath + phasename + JP;
                // writing header:
                AnnotationRule firstRule = set.iterator().next();
                FileAndDownloadUtil.writeStringToFile(
                        fname,
                        RuleStore.generateHeader(firstRule.getClas(),
                                firstRule.getOntology()), true);
                // writing all rules:
                for (AnnotationRule rule : set) {
                    FileAndDownloadUtil.appendStringToFile(rule.getCode(), fname);
                    totalRules++;
                }
                System.out
                        .println("total rules saved for " + phasename + ": " + totalRules);
            }
        }
    }

    public void saveRule(String ruleID) {
        AnnotationRule annotationRule = ruleID2Rule.get(ruleID);
        if (annotationRule != null) {
            saveRules(annotationRule.getClas());
        }
    }


//// getting rules

    public HashMap<String, Set<AnnotationRule>> getRules(Type type) {
        if (Type.CONCEPT.equals(type)) {
            return rulesConcepts;
        }
        return rulesRelations;
    }

    public Collection<AnnotationRule> getRules() {
        LinkedHashSet<AnnotationRule> result = new LinkedHashSet<>();
        HashMap<String, Set<AnnotationRule>> ruleMap = this.getRules(Type.CONCEPT);
        for (String phaseName : ruleMap.keySet()) {
            result.addAll(ruleMap.get(phaseName));
        }
        ruleMap = this.getRules(Type.RELATION);
        for (String phaseName : ruleMap.keySet()) {
            result.addAll(ruleMap.get(phaseName));
        }
        return result;
    }

    public Set<AnnotationRule> getRules(String entityIRI, Type type) {
        HashMap<String, Set<AnnotationRule>> rules = getRules(type);
        return rules.get(entityIRI);
    }

    public AnnotationRule getRule(String ruleID) {
        return ruleID2Rule.get(ruleID);
    }


    /// adding rules

    public void addRule(AnnotationRule rule, Type type) {
        if (rule == null)
            return;
        HashMap<String, Set<AnnotationRule>> rules = getRules(type);
        Set<AnnotationRule> rulesForName = rules.get(rule.getClas());
        if (rulesForName == null) {
            rulesForName = new HashSet<AnnotationRule>();
        }
        rulesForName.add(rule);
        rules.put(rule.getClas(), rulesForName);
        ruleID2Rule.put(rule.getName(), rule);
    }


    // deleting rules


    public void deleteRule(String ruleID) {
        AnnotationRule rule = ruleID2Rule.get(ruleID);
        if (rule != null) {
            ruleID2Rule.remove(ruleID);
            Set<AnnotationRule> set;
            String entityIRI = rule.getClas();
            if (getRules(Type.CONCEPT).containsKey(entityIRI)) {
                set = getRules(entityIRI, Type.CONCEPT);
            } else {
                set = getRules(entityIRI, Type.RELATION);
            }
            if (set != null) {
                set.remove(rule);
            }
        }

    }

//  public void deleteRule(String ruleID, Type type) {
//    AnnotationRule rule = ruleID2Rule.get(ruleID);
//    if(rule!=null){
//      ruleID2Rule.remove(ruleID);
//      Set<AnnotationRule> set=getRules(rule.getClas(),type);
//      if(set!=null){
//        set.remove(rule);
//      }
//    }
//    
//  }


    public void deleteRules() {
        getRules(Type.CONCEPT).clear();
        getRules(Type.RELATION).clear();
        ruleID2Rule.clear();
    }


//  public void deleteRules(String entityIRI, Type type) {
//    Set<AnnotationRule> set=getRules(entityIRI,type);
//    if(set!=null){
//      for(AnnotationRule r:set){
//        ruleID2Rule.remove(r);
//      }
//      set.clear();
//    }
//  }

    public void deleteRules(String entityIRI) {
        Set<AnnotationRule> set;
        if (getRules(Type.CONCEPT).containsKey(entityIRI)) {
            set = getRules(entityIRI, Type.CONCEPT);
        } else {
            set = getRules(entityIRI, Type.RELATION);
        }
        if (set != null) {
            for (AnnotationRule r : set) {
                ruleID2Rule.remove(r);
            }
            set.clear();
        }
    }


    public void updateRule(AnnotationRule rule) {
        if (rule != null) {
            deleteRule(rule.getName());
            addRule(rule, rule.getType());
        }

    }


    //////////////////

    ///   getters setters helpers

    ////////////////


    public String getJprulesDir() {
        return jprulesDir;
    }

    public void setJprulesDir(String jprules) {
        this.jprulesDir = jprules;
    }

    public String getJapelatesDir() {
        return japelatesDir;
    }

    public void setJapelatesDir(String japelates) {
        this.japelatesDir = japelates;
    }

    /**
     *
     */
    public void init() {
        loadJapelates();
        loadRuleStore();
    }


    public void initFreshRuleStore() {
        loadJapelates();
    }


    private static String generateHeader(String entityURI, String ontologyURI) {
        return ontologyURI + ", " + entityURI;
    }

    private String getDirPath(Type type) {
        if (Type.CONCEPT.equals(type)) {
            return this.jprulesDir + CONCEPTS;
        }
        return this.jprulesDir + RELATIONS;
    }

    public static String getPhasenameForConcept(String entityIRI) {
        String result = new ONodeIDImpl(entityIRI, false).getResourceName();
        return result.replaceAll("[^a-zA-Z0-9]", "");
    }


}
