package semano.ontoviewer;

import com.ontotext.gate.vr.OntoTreeModel;
import gate.creole.ontology.Ontology;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public interface Viewer {

  Ontology getCurrentOntology();

  void removeHighlights();

  HashMap<String, Color> getCurrentOntology2ColorScheme();

  HashMap<String, Boolean> getCurrentOntology2OResourceSelection();

  OntoTreeModel getCurrentOntoTreeModelProperties();

  OntoTreeModel getCurrentOntoTreeModelClasses();

  void refreshHighlights();

  Component getParent();

  Point getLocationOnScreen();

  Font getFont();

  FontMetrics getFontMetrics(Font font);

  List<String> getOntologyEntities();

}
