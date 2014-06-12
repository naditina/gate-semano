package semano.rulestore;

/**
 * This class stores parameter information for japelates (each entry in
 * the japelate header is a parameter).
 * 
 * @author nadeschda
 */
public class Parameter {

  /**
   * Type of japelate parameter (ontology entity or literal)
   * @author nadeschda
   *
   */
  public enum ParameterType {
    ONTOLOGY_ENTITY, LITERAL
  }

  /**
   * number of the rule.
   */
  private String name;

  private String description;

  private ParameterType type;

  public Parameter(String name, String type, String description) {
    this.name = name;

    switch(type) {
      case "LITERAL":
        this.type = ParameterType.LITERAL;
        break;
      case "ONTOLOGY_ENTITY":
        this.type = ParameterType.ONTOLOGY_ENTITY;
        break;
      default:
        this.type = null;

    }
    this.description = description;
  }

  public ParameterType getType() {
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
