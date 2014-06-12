package semano.rulebaseeditor;

import java.awt.Color;
import java.util.HashMap;

/**
 * a general interface that is implemented by different tree panels
 * (ontology entities and japelates) used in the GUI for filtering
 * 
 * @author nadeschda
 * 
 */
public interface TreePanel {

  public abstract HashMap<String, Boolean> getCurrentOResource2IsSelectedMap();

  public abstract HashMap<String, Color> getCurrentOResource2ColorMap();

}
