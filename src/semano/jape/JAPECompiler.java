package semano.jape;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import semano.rulestore.AnnotationRule;
import semano.rulestore.Japelate;
import semano.rulestore.Parameter;
import semano.rulestore.RuleStore;
import semano.rulestore.RuleStore.Type;
import semano.util.FileAndDatastructureUtil;

/**
 * This class compiles a semano rule store into a set of JAPE files that
 * can be loaded by a GATE transducer.
 * 
 * @author nadeschda
 * 
 */
public class JAPECompiler {

  // //// parameters for file paths and generating jape format:
  private static final String conceptTemplateHeader = "Phase: $phasename$\n"
          + "Input:  Token Mention Sentence\n"
          + "Options: control = all debug = true\n";

  private static final String MULTIPHASECODE =
          "MultiPhase: SemanoRules\nPhases:";

  public static final String JAPE = ".jape";

  public static final String JAPE_DIRNAME = "plugins/Semano/data/japefiles/";

  public static final String MULTIPHASEFILENAME = JAPE_DIRNAME + "1multiphase"
          + JAPE;

  // // parameters for the ruleStore when called via main:
  public static final String JAPE_JPRULES_ROOT = "plugins/Semano/data/jprules/";

  private static final String JAPE_JAPELATES_DIR =
          "plugins/Semano/data/japelates/";

  /**
   * JAVA API method for compiling the entire rule base into JAPE
   * 
   * @param rs ruleStore to be used for compilation
   * @param japeDirectoryName directory to store jape files
   * @param multiPhaseFilename filename of the multiphase jape file
   */
  public static void convertJP2JAPE(RuleStore rs, String japeDirectoryName,
          String multiPhaseFilename) {
    System.out.println("compiling rules to jape");
    FileAndDatastructureUtil.createDirectory(japeDirectoryName, true);
    FileAndDatastructureUtil.writeStringToFile(multiPhaseFilename,
            MULTIPHASECODE, true);
    compile(japeDirectoryName, multiPhaseFilename, rs, Type.CONCEPT);
    compile(japeDirectoryName, multiPhaseFilename, rs, Type.RELATION);
    System.out.println("done!");
  }

  /**
   * method used from the CreoleRuleStore to annotate documents
   * 
   * @param rs ruleStore to be used
   * @param ruleType type of rules (concept, relation)
   * @param pluginPath path to the plugin (ending with semano?)
   * @return filename of the main jape file
   */
  public static String convertJP2JAPE(RuleStore rs, Type ruleType,
          String pluginPath) {
    System.out.println("compiling rules to jape");
    String directoryName = pluginPath + JAPE_DIRNAME;
    FileAndDatastructureUtil.createDirectory(directoryName, true);
    String filenameJAPE = pluginPath + MULTIPHASEFILENAME;
    FileAndDatastructureUtil.writeStringToFile(filenameJAPE, MULTIPHASECODE,
            true);
    compile(directoryName, filenameJAPE, rs, ruleType);
    System.out.println("done!");
    return filenameJAPE;
  }

  /**
   * method used from the CreoleRuleStore to annotate documents with a
   * single rule
   * 
   * @param rule rule to be compiled
   * @param filename of the JAPE file to be generated
   * @return filename of the generated JAPE file
   */
  public static String convertRuleToJAPEFile(AnnotationRule rule,
          String filename) {
    String phasename = RuleStore.getPhasenameForConcept(rule.getEntityIRI());
    FileAndDatastructureUtil.writeStringToFile(filename,
            generateHeader(phasename), true);
    FileAndDatastructureUtil
            .appendStringToFile(createdJAPERule(rule), filename);
    return filename;
  }

  private static void compile(String japeTargetDirName,
          String multiphaseFilename, RuleStore rs, Type ruleType) {
    System.out.println(ruleType.name() + "...");
    HashMap<String, Set<AnnotationRule>> ruleMap = rs.getRules(ruleType);
    for(String phaseName : ruleMap.keySet()) {

      writeJapeToFile(RuleStore.getPhasenameForConcept(phaseName),
              ruleMap.get(phaseName), japeTargetDirName, multiphaseFilename);
    }
  }

  private static void writeJapeToFile(String phasename,
          Set<AnnotationRule> rules, String japeLocation,
          String multiphaseFilename) {
    String fname = japeLocation + phasename + JAPE;
    FileAndDatastructureUtil.appendStringToFile(phasename, multiphaseFilename);
    FileAndDatastructureUtil.writeStringToFile(fname,
            generateHeader(phasename), true);
    FileAndDatastructureUtil.appendStringsToFile(generateJAPE(rules), fname);

  }

  private static Collection<String> generateJAPE(Set<AnnotationRule> rules) {

    LinkedHashSet<String> japeRules = new LinkedHashSet<String>();
    Iterator<AnnotationRule> ruleIterator = rules.iterator();
    while(ruleIterator.hasNext()) {
      japeRules.add(createdJAPERule(ruleIterator.next()));
    }
    return japeRules;
  }

  /**
   * this method replaces parameter placeholders within japelates by
   * actual values from rules
   * 
   * @param JP rule
   * @return JAPE rule as String
   */
  private static String createdJAPERule(AnnotationRule rule) {
    Japelate japelate = rule.getJapelate();
    String japeRule = japelate.getJapelateBody();
    int lastParameterPosition = japelate.getParamList().size() - 1;

    for(int i = 0; i < lastParameterPosition; i++) {
      String replacement = rule.getParameters().get(i);
      // if the parameter is an ontology entity, we need to remove all
      // spaces around the URI
      // otherwise it wont annotate relations
      if(japelate.getParamList().get(i).getType()
              .equals(Parameter.ParameterType.ONTOLOGY_ENTITY))
        replacement = replacement.trim();
      japeRule = japeRule.replaceAll("\\$" + i + "\\$", replacement);
    }
    int start = japeRule.indexOf("${") + 1;
    int end = japeRule.indexOf("}$") + 1;
    if(start != 0 && end != 0) {
      String repeatingString = japeRule.substring(start, end);
      StringBuilder entireString =
              new StringBuilder(japeRule.substring(0, start - 1));
      for(int i = lastParameterPosition; i < rule.getParameters().size(); i++) {
        entireString.append(repeatingString.replaceAll("\\$"
                + lastParameterPosition + "\\$", rule.getParameters().get(i)));
        entireString.append("\n");
      }
      entireString.delete(entireString.length() - 1, entireString.length());
      entireString.append(japeRule.substring(end + 1));
      japeRule = entireString.toString();
    } else {
      japeRule =
              japeRule.replaceAll("\\$" + lastParameterPosition + "\\$", rule
                      .getParameters().get(lastParameterPosition));
    }
    return japeRule;
  }

  private static String generateHeader(String phasename) {
    return conceptTemplateHeader.replaceAll("\\$phasename\\$", phasename);
  }

  public static void main(String[] args) {
    RuleStore rs = new RuleStore(JAPE_JPRULES_ROOT, JAPE_JAPELATES_DIR);
    rs.init();
    convertJP2JAPE(rs, JAPE_DIRNAME, MULTIPHASEFILENAME);
  }

}
