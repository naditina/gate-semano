package semano.jape;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import semano.rulestore.AnnotationRule;
import semano.rulestore.Japelate;
import semano.rulestore.RuleStore;
import semano.rulestore.RuleStore.Type;
import semano.util.FileAndDownloadUtil;

public class JAPECompiler {

  // //// parameters for file paths and generating jape format:
  public static final String conceptTemplateHeader = "Phase: $phasename$\n"
          + "Input:  Token Mention Sentence\n"
          + "Options: control = all debug = true\n";

  private static final String MULTIPHASECODE =
          "MultiPhase: SemanoRules\nPhases:";

  static final String JAPE = ".jape";

  public static final String DIRNAME = "JAPE/japefiles/";

  public static final String MULTIPHASEFILENAME = DIRNAME + "1multiphase"
          + JAPE;

  // // parameters for the ruleStore when called via main:
  public static final String JAPE_JPRULES_ROOT = "JAPE/jprules/";

  private static final String JAPE_JAPELATES_DIR = "JAPE/japelates/";

  private static final String TEMP_JAPE_FILE = "JAPE/temp" + JAPE;

  public static String convertRuleToJAPEFile(AnnotationRule rule) {
    String phasename = RuleStore.getPhasenameForConcept(rule.getClas());
    FileAndDownloadUtil.writeStringToFile(TEMP_JAPE_FILE,
            generateHeader(phasename), true);
    FileAndDownloadUtil.appendStringToFile(createdJAPERule(rule),
            TEMP_JAPE_FILE);
    return TEMP_JAPE_FILE;
  }

  public static void convertJP2JAPE(RuleStore rs, Type ruleType) {
    System.out.println("compiling rules to jape");
    FileAndDownloadUtil.createDirectory(DIRNAME, true);
    FileAndDownloadUtil.writeStringToFile(MULTIPHASEFILENAME, MULTIPHASECODE,
            true);
    compile(DIRNAME, MULTIPHASEFILENAME, rs, ruleType);
    System.out.println("done!");
  }

  public static void convertJP2JAPE(RuleStore rs) {
    System.out.println("compiling rules to jape");
    FileAndDownloadUtil.createDirectory(DIRNAME, true);
    FileAndDownloadUtil.writeStringToFile(MULTIPHASEFILENAME, MULTIPHASECODE,
            true);
    compile(DIRNAME, MULTIPHASEFILENAME, rs, Type.CONCEPT);
    compile(DIRNAME, MULTIPHASEFILENAME, rs, Type.RELATION);
    System.out.println("done!");
  }

  public static void compile(String japeTargetDirName,
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
    FileAndDownloadUtil.appendStringToFile(phasename, multiphaseFilename);
    FileAndDownloadUtil.writeStringToFile(fname, generateHeader(phasename),
            true);
    FileAndDownloadUtil.appendStringsToFile(generateJAPE(rules), fname);

  }

  private static Collection<String> generateJAPE(Set<AnnotationRule> rules) {

    LinkedHashSet<String> japeRules = new LinkedHashSet<String>();
    Iterator<AnnotationRule> ruleIterator = rules.iterator();
    while(ruleIterator.hasNext()) {
      japeRules.add(createdJAPERule(ruleIterator.next()));
    }
    return japeRules;
  }

  private static String createdJAPERule(AnnotationRule rule) {
    Japelate japelate = rule.getJapelate();
    String japeRule = japelate.getJapelateBody();
    int lastParameterPosition = japelate.getParamList().size() - 1;

    for(int i = 0; i < lastParameterPosition; i++) {
      japeRule =
              japeRule.replaceAll("\\$" + i + "\\$", rule.getParameters()
                      .get(i));
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

  static String generateHeader(String phasename) {
    return conceptTemplateHeader.replaceAll("\\$phasename\\$", phasename);
  }

  public static void main(String[] args) {
    RuleStore rs = new RuleStore(JAPE_JPRULES_ROOT, JAPE_JAPELATES_DIR);
    rs.init();
    convertJP2JAPE(rs);
  }

}
