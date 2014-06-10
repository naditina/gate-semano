package semano.jape;

import java.util.ArrayList;

import semano.rulestore.AnnotationRule;
import semano.rulestore.Japelate;
import semano.rulestore.RuleStore.Type;
import semano.util.OntologyUtil;

public class JPRulesGeneratorConcept {

    private static final int CRITICAL_LENGTH_PLURAL = 2;

    public AnnotationRule createRule(String ontology, String readableClassName,
                                     String entityURI, String type, String searchString, Japelate japelate) {

        // some initial checks:
        searchString = OntologyUtil.convertToValidLabel(searchString);
        if (searchString.isEmpty() || readableClassName.isEmpty()) return null;
        // heuristic to detecting ACRONYMS:
        if (searchString.equals(searchString.toUpperCase())) {
            type = "ACRONYM";
        }

        AnnotationRule ar = AnnotationRule.createFreshAnnotationRule(japelate, ontology, entityURI, Type.CONCEPT);
        // init params:
        boolean caseSensitive = type.equals("ACRONYM") && !type.equals("FORMULA");
        String cs = caseSensitive ? "" : "(?i)";
        ar.getParameters().add(cs);
        ar.getParameters().addAll(generateParameters(searchString, type, readableClassName,
                caseSensitive));

        return ar;
    }

    private static ArrayList<String> generateParameters(String concept,
                                                        String type, String clas, boolean caseSensitive) {
        ArrayList<String> result = new ArrayList<>();
        if (!caseSensitive) {// case

            concept = concept.toLowerCase();
        }
        String[] concepts = concept.split(" ");
        // boolean prural=Settings.USE_PLURAL;
        boolean pluralFormOn = !type.equals("FORMULA");
        // can be many words, need to work for variable args
        for (int i = 0; i < concepts.length; i++) {
            if (!concepts[i].isEmpty()) {// avoid empty words in patterns
                if (i == concepts.length - 1) {
                    result.add(computeLastWord(clas, concepts, pluralFormOn));
                } else {
                    result.add(concepts[i]);
                }
            }
        }
        return result;
    }

    public static String computeLastWord(String clas, String[] concepts,
                                         boolean pluralFormOn) {
        // is concept in the ontology plural form:
        boolean isClasPlural = generatePluralForm(clas).equals(clas);
        // is searchstring a plural form:
        boolean isLastWordPlural =
                generatePluralForm(concepts[concepts.length - 1]).equals(
                        concepts[concepts.length - 1]);
        // do we need singular|plural regexpr or just searchstring:
        boolean needsSingularPluralAlternatives = !isClasPlural && pluralFormOn;
        String lastExpression = concepts[concepts.length - 1];
        if (needsSingularPluralAlternatives) {
            String singularForm = concepts[concepts.length - 1];
            if (isLastWordPlural) {
                String singularFormNew =
                        generateSingularFormFromPlural(concepts[concepts.length - 1],
                                clas);
                if (singularForm.length() > CRITICAL_LENGTH_PLURAL) {
                    singularForm = singularFormNew;
                }
            }
            String pluralForm = generatePluralForm(concepts[concepts.length - 1]);
            lastExpression = "(" + singularForm + "|" + pluralForm + ")";
        }
        return lastExpression;
    }

    public static String generateSingularFormFromPlural(String word, String clas) {
        if (word.endsWith("ies")) return word.substring(0, word.length() - 3) + "y";
        if (word.endsWith("sses")) return word.substring(0, word.length() - 2);

        return word.substring(0, word.length() - 1);
    }

    private static String generatePluralForm(String initialString) {
        String pluralForm = initialString + "s";
        if (initialString.endsWith("y")) {
            pluralForm =
                    initialString.substring(0, initialString.length() - 1) + "ies";
        } else if (initialString.endsWith("ss")) {
            pluralForm = initialString + "es";
        } else if (initialString.endsWith("s")) {
            pluralForm = initialString;
        }
        // System.out.println(pluralForm);
        return pluralForm;
    }

}
