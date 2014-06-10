package semano.rulebaseeditor;

import gate.creole.AbstractVisualResource;

import java.awt.Color;
import java.util.HashMap;

public interface TreePanel {


  public abstract HashMap<String, Boolean>  getCurrentOResource2IsSelectedMap();

  public abstract HashMap<String, Color> getCurrentOResource2ColorMap();

}
