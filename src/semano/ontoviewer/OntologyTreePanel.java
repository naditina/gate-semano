/*
 *  OntologyTreePanel.java
 *
 *  Niraj Aswani, 12/March/07
 *
 *  $Id: OntologyTreePanel.html,v 1.0 2007/03/12 16:13:01 niraj Exp $
 */
package semano.ontoviewer;

import gate.Annotation;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.Ontology;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreeSelectionModel;

import semano.rulebaseeditor.TreePanel;

import com.ontotext.gate.vr.ClassNode;
import com.ontotext.gate.vr.IFolder;
import com.ontotext.gate.vr.OntoTreeModel;

/**
 * GUI component showing a tree with properties or concepts of an
 * ontology
 * 
 * @author Nadejda Nikitina
 */
public class OntologyTreePanel extends JPanel implements TreePanel {

  /**
   * Serial version ID
   */
  private static final long serialVersionUID = 3618419328190592304L;

  // data fields

  /**
   * Current Annotation Map that stores the annotation in arraylist for
   * each concept
   */
  protected LinkedHashMap<String, LinkedHashSet<Annotation>> currentOResourceName2AnnotationsListMap;

  /** The current currentOntologyTreeModel */
  private OntoTreeModel currentOntologyTreeModel;

  /** Current ontologyColorScheme */
  private HashMap<String, Color> currentOResource2ColorMap;

  /** Class Selection map for the current ontology */
  private HashMap<String, Boolean> currentOResource2IsSelectedMap;

  // GUI fields

  /** Instance of JTree used to store information about ontology items */
  protected JTree ontologyTree;

  /** ToolBars that displays the different options */
  private JToolBar leftToolBar;

  /**
   * OntologyTreeListener that listens to the selection of ontology
   * classes
   */
  protected MouseAdapter ontoTreeListener;

  /** Instance of ontology Viewer */
  private Viewer ontoViewer;

  /**
   * Indicates whether the annotation window is being shown or not
   */
  protected boolean showingAnnotationWindow = false;

  /**
   * Indicates whether the properties instead of classes should be
   * displayed
   */
  private boolean isPropertiesView = false;

  /** Constructor */
  public OntologyTreePanel(Viewer ontoViewer, boolean isPropertiesView) {
    this.isPropertiesView = isPropertiesView;
    this.ontoViewer = ontoViewer;
    setCurrentOResource2ColorMap(new HashMap<String, Color>());
    setCurrentOResource2IsSelectedMap(new HashMap<String, Boolean>());
    initGUI();
  }

  public void setListener(MouseAdapter ontoTreeListener) {
    ontologyTree.addMouseListener(ontoTreeListener);
  }

  public Viewer getViewer() {
    return ontoViewer;
  }

  /**
   * This method finds out the ClassNode node in the ontology Tree for
   * given class
   * 
   * @param value
   * @return
   */
  public ClassNode getFirstNode(String value) {
    if(value == null) return null;
    // lets first convert this classValue into the className
    int index = value.lastIndexOf("#");
    if(index < 0) index = value.lastIndexOf("/");
    if(index < 0) index = value.lastIndexOf(":");
    if(index >= 0) {
      value = value.substring(index + 1, value.length());
    }

    ClassNode currentNode = (ClassNode)ontologyTree.getModel().getRoot();
    return getFirstClassNode(currentNode, value);
  }

  /**
   * Internal recursive method to find out the Node for given class
   * Value under the hierarchy of given node
   * 
   * @param node
   * @param classValue
   * @return
   */
  private ClassNode getFirstClassNode(ClassNode node, String classValue) {
    if(node.toString().equals(classValue)) {
      return node;
    }

    Iterator children = node.getChildren();
    while(children.hasNext()) {
      ClassNode tempNode = (ClassNode)children.next();
      ClassNode returnedNode = getFirstClassNode(tempNode, classValue);
      if(returnedNode != null) {
        return returnedNode;
      }
    }
    return null;
  }

  /**
   * This method finds out the ClassNode node in the ontology Tree for
   * given class
   * 
   * @param classValue
   * @return
   */
  public List<ClassNode> getNode(String classValue) {
    // lets first convert this classValue into the className
    int index = classValue.lastIndexOf("#");
    if(index < 0) index = classValue.lastIndexOf("/");
    if(index < 0) index = classValue.lastIndexOf(":");
    if(index >= 0) {
      classValue = classValue.substring(index + 1, classValue.length());
    }

    ClassNode currentNode = (ClassNode)ontologyTree.getModel().getRoot();
    return getClassNode(currentNode, classValue);
  }

  /**
   * Internal recursive method to find out the Node for given class
   * Value under the heirarchy of given node
   * 
   * @param node
   * @param classValue
   * @return
   */
  private List<ClassNode> getClassNode(ClassNode node, String classValue) {
    List<ClassNode> cNodes = new ArrayList<ClassNode>();
    if(node.toString().equalsIgnoreCase(classValue)) {
      cNodes.add(node);
      return cNodes;
    }

    Iterator children = node.getChildren();
    while(children.hasNext()) {
      ClassNode tempNode = (ClassNode)children.next();
      List<ClassNode> returnedNodes = getClassNode(tempNode, classValue);
      if(returnedNodes != null) {
        cNodes.addAll(returnedNodes);
      }
    }

    return cNodes;
  }

  /** Returns the current ontology */
  public Ontology getCurrentOntology() {
    return ontoViewer.getCurrentOntology();
  }

  /** Initialize the GUI */
  private void initGUI() {
    ontologyTree = new JTree();

    ToolTipManager.sharedInstance().registerComponent(ontologyTree);
    ontologyTree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
    this.setLayout(new BorderLayout());
    this.add(new JScrollPane(ontologyTree), BorderLayout.CENTER);

    leftToolBar = new JToolBar(JToolBar.VERTICAL);
    leftToolBar.setFloatable(false);
    // this.add(leftToolBar, BorderLayout.WEST);

    CheckRenderer cellRenderer = new CheckRenderer(this);
    ontologyTree.setCellRenderer(cellRenderer);
  }

  /** A method to show an empty ontology tree */
  public void showEmptyOntologyTree() {
    setCurrentOntologyTreeModel(null);
    setCurrentOResource2ColorMap(null);
    setCurrentOResource2IsSelectedMap(null);
    ontologyTree.setVisible(false);
    ontoViewer.removeHighlights();
  }

  /**
   * This method sets the data to be displayed
   * 
   * @param ontology - the ontology to be ploted
   * @param currentOResourceName2AnnotationsListMap - the annotationMap
   *          which contains Key=concept(String)
   *          Value=annotations(ArrayList)
   */
  public void showOntologyInOntologyTreeGUI(Ontology ontology,
          LinkedHashMap<String, LinkedHashSet<Annotation>> annotMap) {

    this.currentOResourceName2AnnotationsListMap = annotMap;
    if(getOntoTreeModel() != null) {
      setCurrentOntologyTreeModel(getOntoTreeModel());
      setCurrentOResource2ColorMap(ontoViewer.getCurrentOntology2ColorScheme());
      setCurrentOResource2IsSelectedMap(ontoViewer
              .getCurrentOntology2OResourceSelection());
    }
    ontologyTree.setModel(getCurrentOntologyTreeModel());
    // update the GUI part of the Tree
    ontologyTree.invalidate();
  }

  private OntoTreeModel getOntoTreeModel() {
    if(isPropertiesView) {
      return ontoViewer.getCurrentOntoTreeModelProperties();
    } else {
      return ontoViewer.getCurrentOntoTreeModelClasses();
    }
  }

  /**
   * returns the currentOntologyTree Panel
   * 
   * @return
   */
  public Component getGUI() {
    return this;
  }

  /**
   * This method select/deselect the classes in the classSelectionMap
   * 
   * @param className
   * @param value
   */
  public void setSelected(String className, boolean value) {
    getCurrentOResource2IsSelectedMap().put(className, new Boolean(value));
  }

  public void setColor(String className, Color col) {
    updateColorScheme(getFirstNode(className), getCurrentOResource2ColorMap(),
            className, false, col);
    getCurrentOResource2ColorMap().put(className, col);
  }

  /**
   * 
   * @param root - the root (top class) of the ontology
   * @param colorScheme - and the colorScheme hashmap Key=conceptName,
   *          Value:associated color map. if provided as a new fresh
   *          instance of hashmap with size zero, it parses through the
   *          whole ontology and generate the random color instances for
   *          all the classes and stores them in the provided
   *          colorScheme hashmap
   */
  private static void updateColorScheme(IFolder root,
          HashMap<String, Color> colorScheme, String classname,
          boolean searchForNameMatch, Color col) {
    Iterator childenIt = root.getChildren();
    while(childenIt.hasNext()) {
      Object childO = childenIt.next();
      if(!searchForNameMatch || childO.toString().equals(classname)) {
        if(!colorScheme.get(classname).equals(col)) {
          if(childO instanceof ClassNode) {
            ClassNode child = (ClassNode)childO;
            colorScheme.put(child.toString(), col);
            updateColorScheme(child, colorScheme, classname, false, col);
          }
        }
      }
    }
  }

  public Set<String> getAllClassNames() {
    Set<String> toReturn = new HashSet<String>();
    for(String aResource : getCurrentOResource2IsSelectedMap().keySet()) {
      if(getCurrentOntology().getOResourceByName(aResource) instanceof OClass) {
        toReturn.add(aResource);
      }
    }
    return toReturn;
  }

  public Set<String> getAllInstanceNames() {
    Set<String> toReturn = new HashSet<String>();
    for(String aResource : getCurrentOResource2IsSelectedMap().keySet()) {
      if(getCurrentOntology().getOResourceByName(aResource) instanceof OInstance) {
        toReturn.add(aResource);
      }
    }
    return toReturn;
  }

  private OntoTreeModel getCurrentOntologyTreeModel() {
    return currentOntologyTreeModel;
  }

  private void setCurrentOntologyTreeModel(
          OntoTreeModel currentOntologyTreeModel) {
    this.currentOntologyTreeModel = currentOntologyTreeModel;
  }

  public HashMap<String, Color> getCurrentOResource2ColorMap() {
    return currentOResource2ColorMap;
  }

  protected void setCurrentOResource2ColorMap(
          HashMap<String, Color> currentOResource2ColorMap) {
    this.currentOResource2ColorMap = currentOResource2ColorMap;
  }

  public HashMap<String, Boolean> getCurrentOResource2IsSelectedMap() {
    return currentOResource2IsSelectedMap;
  }

  protected void setCurrentOResource2IsSelectedMap(
          HashMap<String, Boolean> currentOResource2IsSelectedMap) {
    this.currentOResource2IsSelectedMap = currentOResource2IsSelectedMap;
  }
}
