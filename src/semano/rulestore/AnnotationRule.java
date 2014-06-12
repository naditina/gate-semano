package semano.rulestore;

import java.util.ArrayList;
import java.util.List;

import semano.rulestore.RuleStore.Type;


/**
 * This class represents an annotation rule.
 * @author nadeschda
 *
 */
public class AnnotationRule {


    public class RuleParseException extends Exception {
        public RuleParseException(String string) {
            super(string);
        }
    }

    private static final String RULE_PREFIX = "rule";
    private static final int RULE_NAME = 0;
    private static final int ONTOLOGY = 1;
    private static final int CLASS = 2;
    /**
     * the  number of parameters that are standard and are not included into a rule value list
     */
    public static final int MINIMUM_PARAMETER_NUMBER = 3;
    private ArrayList<String> parameters = new ArrayList<>();
    private Japelate template;
    private String japelateName;
    private Type type = Type.CONCEPT;
    
    /**
     * the currently maximal ID
     */
    public static int RULE_ID = 0;

    /**
     * add a parameter value at the end of the parameter list
     * @param parameterValue
     */
    public void addParameter(String parameterValue) {
        getParameters().add(parameterValue);
    }


    /**
     * @return type of rule (concept, relation)
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type the new type of rule
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @param japeplate the new japelate
     */
    public void setJapelate(Japelate japeplate) {
        this.template = japeplate;
        this.japelateName = japeplate.getName();
    }

    /**
     * @return the japelate of the rule
     */
    public Japelate getJapelate() {
        return template;
    }

    /**
     * @return the name of the rule japelate
     */
    public String getJapelateName() {
        return japelateName;
    }

    /**
     * @return the IRI of the ontoloty entity annotated by this rule
     */
    public String getEntityIRI() {
        return parameters.get(CLASS);
    }

    /**
     * @param entityIRI the new IRI of the ontoloty entity annotated by this rule
     */
    public void setEntityIRIClas(String entityIRI) {
        this.parameters.set(CLASS, entityIRI);
    }

    /**
     * @return the ontology IRI
     */
    public String getOntology() {
        return parameters.get(ONTOLOGY);
    }

    /**
     * @param ontology the new ontology IRI
     */
    public void setOntology(String ontology) {
        this.parameters.set(ONTOLOGY, ontology);
    }

    /**
     * @return the ID of the rule
     */
    public String getName() {
        return this.parameters.get(RULE_NAME);
    }

    /**
     * @param name the new ID of the rule
     */
    public void setName(String name) {
        this.parameters.set(RULE_NAME, name);
    }

    /**
     * @return the parameter list
     */
    public List<String> getParameters() {
        return this.parameters;
    }


    /**
     * constructor used for loading rules from a file
     * @param ruleLine rule body (parameter list)
     * @param ontologyURI ontology IRI
     * @param className entity IRI
     * @param type type of rule
     * @throws RuleParseException
     */
    public AnnotationRule(String ruleLine, String ontologyURI, String className, Type type) throws RuleParseException {
        if (ruleLine == null || ruleLine.isEmpty() && ruleLine.indexOf(":") == -1) {
            throw new RuleParseException("Could not parse rule: " + className + " ," + ruleLine);
        }
        int separatorPosition = ruleLine.indexOf(":");
        String[] ruleParts = {ruleLine.substring(0, separatorPosition), ruleLine.substring(separatorPosition + 1, ruleLine.length())};
        String ruleName = ruleParts[0].trim();
        initParamArraySize();
        setName(ruleName);
        setOntology(ontologyURI.trim());
        setEntityIRIClas(className.trim());
        parseCode(ruleParts[1]);
        setType(type);
        syncRuleID(getID());

    }


    private void initParamArraySize() {
        for (int i = 0; i < MINIMUM_PARAMETER_NUMBER; i++) {
            parameters.add("");
        }

    }

    private AnnotationRule(Japelate japelate, ArrayList<String> params, Type type) {
        super();
        setJapelate(japelate);
        this.parameters = params;
        setType(type);
    }

    private void parseCode(String instantiation) throws RuleParseException {
        int templateNameEnd = instantiation.indexOf("(");
        this.japelateName = instantiation.substring(0, templateNameEnd).trim();
//    template = new Japelate(instantiation.substring(0, templateNameEnd));
        if (instantiation.length() < templateNameEnd || instantiation.lastIndexOf(")") < 0)
            throw new RuleParseException("Could not parse rule: " + getEntityIRI() + " ," + instantiation);
        String parametersString = instantiation.substring(templateNameEnd + 1, instantiation.lastIndexOf(")"));
        String[] params = parametersString.split(",");
        int numberOfParams = params.length;
        for (int j = 0; j < numberOfParams; j++) {
            this.parameters.add(params[j].trim());
        }
    }


    @Override
    public String toString() {
        return "AnnotationRule [name=" + getName() + ", ontology=" + getOntology() + ", japelate=" + getJapelate() + ", type=" + getType().name() + ", parameters=" + parameters.toString() + "]";
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (int index = 1; index < parameters.size(); index++) {
            result = prime * result + ((parameters.get(index) == null) ? 0 : parameters.get(index).hashCode());
        }
        result =
                prime * result
                        + ((template == null) ? 0 : template.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AnnotationRule other = (AnnotationRule) obj;
        if (other.parameters.size() != this.parameters.size())
            return false;
        for (int index = 1; index < parameters.size(); index++) {
            if (parameters.get(index) == null) {
                if (other.parameters.get(index) != null) return false;
            } else if (!parameters.get(index).equals(other.parameters.get(index))) return false;
        }
        if (template == null) {
            if (other.template != null) return false;
        } else if (!template.equals(other.template)) return false;
        return true;
    }

    /**
     * used by the rule store to write rules to file
     * @return formatted parameter list as it is written to file
     */
    public String getCode() {

        String ruleCode = this.getName() + ": " + this.japelateName + "(";
        for (int i = MINIMUM_PARAMETER_NUMBER; i < parameters.size(); i++) {
            ruleCode += parameters.get(i) + ",";
        }
        ruleCode = (ruleCode.substring(0, ruleCode.length() - 1) + ")").trim();
        return ruleCode;
    }

    /**
     * generates a unique rule ID
     * @return new rule ID
     */
    private static String generateFreshRuleID() {
        return RULE_PREFIX + RULE_ID++;
    }

    /**
     * method used to create new rules that have not been part of a rule base before
     * 
     * @param japelate the rule japelate
     * @param ontology the IRI of the ontology
     * @param entityName the IRI of the ontology entity
     * @param type the type of the rule (concept, relation)
     * @return new rule
     */
    public static AnnotationRule createFreshAnnotationRule(Japelate japelate, String ontology, String entityName, Type type) {
        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(RULE_NAME, generateFreshRuleID());
        parameters.add(ONTOLOGY, ontology);
        parameters.add(CLASS, entityName);
        return new AnnotationRule(japelate, parameters, type);
    }

    /**
     * @return the integer value of the rule ID
     */
    public int getID() {
        String stringID = getName().substring(RULE_PREFIX.length(), getName().length());
        return Integer.parseInt(stringID);
    }


    /**
     * this method should be called when rules are loaded into the rule store that have an ID assigned in a previous session.
     * This will ensure that no new IDs will coinside with the IDs from previous sessions.
     * @param id 
     */
    private void syncRuleID(int id) {
        if (RULE_ID < id) {
            RULE_ID = id;
        }
    }
}
