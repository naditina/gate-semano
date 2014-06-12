package semano.ontoviewer;

import gate.creole.ontology.Ontology;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;

import com.ontotext.gate.vr.OntoTreeModel;

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
