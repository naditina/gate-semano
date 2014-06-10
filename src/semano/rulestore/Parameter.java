package semano.rulestore;

/**
 * Store parameter information.
 */
public class Parameter {

    public enum TYPE {
        ONTOLOGY_ENTITY,
        LITERAL
    }

    /**
     * number of the rule.
     */
    private String name;
    private String description;
    private TYPE type;


    public Parameter(String name, String type, String description) {
        this.name = name;

        switch(type){
            case "LITERAL" :
                this.type = TYPE.LITERAL;
                break;
            case "ONTOLOGY_ENTITY" :
                this.type = TYPE.ONTOLOGY_ENTITY;
                break;
            default :
                this.type = null;

        }
        this.description = description;
    }

    public TYPE getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
