package semano.jape;

import gate.creole.ontology.ONodeID;

import java.io.FileNotFoundException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import semano.ontologyowl.AnnotationValue;
import semano.ontologyowl.OntologyParser;
import semano.ontologyowl.impl.Property;
import semano.ontologyowl.impl.ResourceInfo;
import semano.ontoviewer.AnnotationMetaData;
import semano.ontoviewer.OntologyAnnotation;
import semano.rulestore.AnnotationRule;
import semano.rulestore.Japelate;
import semano.rulestore.RuleStore;
import semano.rulestore.RuleStore.Type;
import semano.util.FileAndDownloadUtil;
import semano.util.OntologyUtil;

public class Ontology2JPRuleGenerator {

  static final int CRITICAL_LENGTH = 1;
    private static final String ONTOLOGYFILENAME = "plugins/Semano/data/mergedNanOnOntologyRDF.owl";
    private static String JPRULESDIRECTORY = "plugins/Semano/data/jprules/";
    private static String JAPELATEDIRECTORY = "plugins/Semano/data/japelates/";



    /**
     * @param args
     */
    public static void main(String[] args) {
        Ontology2JPRuleGenerator.generateInitialRuleBase(ONTOLOGYFILENAME,JPRULESDIRECTORY,JAPELATEDIRECTORY);
    }


    /**
     * method generating a simple rule base from an ontology
     * @param ontologyFileName  name of the ontology file
     * @param jpRulesDirectory name of the directory for storing JP rules
     * @param japelateDirectory name of the directory containing japelates
     */
    public static void generateInitialRuleBase(String ontologyFileName, String jpRulesDirectory, String japelateDirectory) {
        OntologyParser op = new OntologyParser();
        try{
            op.loadOntology(ontologyFileName);
        } catch (OWLOntologyCreationException |FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        FileAndDownloadUtil.createDirectory(jpRulesDirectory, true);
        RuleStore ruleStore = new RuleStore(jpRulesDirectory, japelateDirectory);
        ruleStore.initFreshRuleStore();
        exportConcepts(op, ruleStore);
        System.out.println("writing concept rules to " + jpRulesDirectory);
        ruleStore.saveRules(Type.CONCEPT);
        exportRelations(op, ruleStore);
        System.out.println("writing relation rules to " + jpRulesDirectory);
        ruleStore.saveRules(Type.RELATION);
    }

    private static void exportConcepts(OntologyParser op, RuleStore ruleStore) {
        Japelate j = ruleStore.getJapelate("label");
        JPRulesGeneratorConcept japeGenerator = new JPRulesGeneratorConcept();
        for (ONodeID cl : op.getClasses()) {
            if (!cl.isAnonymousResource()) {
                String clas = cl.getResourceName();
                String ontology = cl.getNameSpace();

                // generating rule from concept name:
                String searchString =
                        OntologyUtil.convertToValidLabel(cl.getResourceName());
                
                // call the generator
                AnnotationRule rule =
                        japeGenerator.createRule(ontology, clas,
                                cl.getNameSpace() + cl.getResourceName(),
                                "SYNONYM", searchString, j);
                ruleStore.addRule(rule, Type.CONCEPT);


                // generating rule from annotations:
                for (AnnotationValue annotationValue : op.getAnnotationPropertyValues(cl
                        .toString())) {
                    searchString = annotationValue.getValue();
                    AnnotationMetaData type = OntologyAnnotation.getPropertyForTypeUri(annotationValue
                            .getAnnotationProperty());
                    if (!annotationValue.isAntiPattern() && type != null
                            && type.isAutoannotate() && searchString != null
                            && searchString.length() > CRITICAL_LENGTH) {
                        // call the generator
                        rule =
                                japeGenerator.createRule(ontology, clas,
                                        cl.getNameSpace() + cl.getResourceName(),
                                        type.getEnumName(), searchString, j);

                        ruleStore.addRule(rule, Type.CONCEPT);
                    }

                }
            }
        }


    }

    private static void exportRelations(OntologyParser op, RuleStore ruleStore) {
        for (Property property : op.getObjectProperties()) {
            for (AnnotationValue annotationValue : op
                    .getAnnotationPropertyValues(property.getUri())) {
                String searchString = annotationValue.getValue();
                String ontology = OntologyUtil.extractOntology(property.getUri());
                String propertyName = OntologyUtil.extractEntityName(property.getUri());
                AnnotationMetaData type =
                        OntologyAnnotation.getPropertyForTypeUri(annotationValue
                                .getAnnotationProperty());
                if (!propertyName.isEmpty() && !annotationValue.isAntiPattern()
                        && type != null && type.isAutoannotate()
                        && searchString != null
                        && searchString.length() > CRITICAL_LENGTH) {

                    ResourceInfo[] domains = op.getDomains(property.getUri());
                    ResourceInfo[] ranges = op.getRangess(property.getUri());


                    String regExpr = searchString.replaceAll(" ", ",");
                    regExpr = regExpr.replaceAll("[^a-zA-Z,0-9]", "");

                    for (ResourceInfo domain : domains) {
                        for (ResourceInfo range : ranges) {
                            if (type.getEnumName().equals("EXPRESSIONLESSRELATION")) {
                                Japelate j = ruleStore.getJapelate("unlabeledRelation");
                                AnnotationRule rule = AnnotationRule.createFreshAnnotationRule(j, ontology, property.getUri(), Type.RELATION);
                                rule.getParameters().add(domain.getUri());
                                rule.getParameters().add(range.getUri());
                                ruleStore.addRule(rule, Type.RELATION);
                            } else if (type.getEnumName().equals("EXPRESSION")) {
                                Japelate j = ruleStore.getJapelate("labeledRelation");
                                AnnotationRule rule = AnnotationRule.createFreshAnnotationRule(j, ontology, property.getUri(), Type.RELATION);
                                rule.getParameters().add(domain.getUri());
                                rule.getParameters().add(range.getUri());
                                for (String p : regExpr.split(",")) {
                                    rule.getParameters().add(p);
                                }
                                ruleStore.addRule(rule, Type.RELATION);
                            } else if (type.getEnumName().equals("EXPRESSIONLESSRELATIONNNNN")) {
                                Japelate j =
                                        ruleStore.getJapelate("unlabeledRelationWithFixedPOS");
                                AnnotationRule rule = AnnotationRule.createFreshAnnotationRule(j, ontology, property.getUri(), Type.RELATION);
                                rule.getParameters().add(domain.getUri());
                                rule.getParameters().add("NN");
                                rule.getParameters().add(range.getUri());
                                rule.getParameters().add("NN");
                                ruleStore.addRule(rule, Type.RELATION);
                            } else if (type.getEnumName().equals("EXPRESSIONLESSRELATIONJJNN")) {
                                Japelate j =
                                        ruleStore.getJapelate("unlabeledRelationWithFixedPOS");
                                AnnotationRule rule = AnnotationRule.createFreshAnnotationRule(j, ontology, property.getUri(), Type.RELATION);
                                rule.getParameters().add(domain.getUri());
                                rule.getParameters().add("JJ");
                                rule.getParameters().add(range.getUri());
                                rule.getParameters().add("NN");
                                ruleStore.addRule(rule, Type.RELATION);
                            } else if (type.getEnumName()
                                    .equals("EXPRESSIONLESSRELATIONNNVBG")) {
                                Japelate j =
                                        ruleStore.getJapelate("unlabeledRelationWithFixedPOS");
                                AnnotationRule rule = AnnotationRule.createFreshAnnotationRule(j, ontology, property.getUri(), Type.RELATION);
                                rule.getParameters().add(domain.getUri());
                                rule.getParameters().add("NN");
                                rule.getParameters().add(range.getUri());
                                rule.getParameters().add("VBG");
                                ruleStore.addRule(rule, Type.RELATION);
                            }

                        }

                    }

                }
            }

        }

    }
}

