package semano.rulebaseeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreeSelectionModel;

import semano.ontoviewer.CheckRenderer;

/**
 * A GUI component showing a list of japelates used on the right-hand side within the rule base editor.
 * @author nadeschda
 *
 */
public class JapelateTreePanel extends JPanel implements TreePanel {

  private HashMap<String, Boolean> currentJapelateSelection = new HashMap<>();

  /** Instance of JTree used to store information about ontology items */
  protected JList<String> ontologyTree;

  /** ToolBars that displays the different options */
  private JToolBar leftToolBar;

  // /**
  // * OntologyTreeListener that listens to the selection of ontology
  // classes
  // */
  // protected OntologyTreeListener ontoTreeListener;

  /** Instance of ontology Viewer */
  private RuleBaseViewer ruleBaseViewer;

  /**
   * Indicates whether the annotation window is being shown or not
   */
  protected boolean showingAnnotationWindow = false;

  /** Constructor */
  public JapelateTreePanel(RuleBaseViewer ruleBaseViewer) {
    super();
    this.ruleBaseViewer = ruleBaseViewer;
    initGUI();
  }

  /** Initialize the GUI */
  private void initGUI() {
    ontologyTree = new JList<String>();
    ToolTipManager.sharedInstance().registerComponent(ontologyTree);
    ontologyTree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
    this.setLayout(new BorderLayout());
    this.add(new JScrollPane(ontologyTree), BorderLayout.CENTER);

    MouseListener mouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent mouseEvent) {
        if(SwingUtilities.isLeftMouseButton(mouseEvent)) {
          int index = ontologyTree.locationToIndex(mouseEvent.getPoint());
          if(index >= 0) {
            String o = ontologyTree.getModel().getElementAt(index);
            if(getCurrentOResource2IsSelectedMap()!=null && getCurrentOResource2IsSelectedMap().get(o)!=null){
            boolean isSelected =
                    !getCurrentOResource2IsSelectedMap().get(o);
            setSelected(o, isSelected);
            ontologyTree.repaint();
            ruleBaseViewer.refleshRuleTable();
            }
          }
        }
      }
    };
    ontologyTree.addMouseListener(mouseListener);

    leftToolBar = new JToolBar(JToolBar.VERTICAL);
    leftToolBar.setFloatable(false);
    CheckRenderer cellRenderer = new CheckRenderer(this);
    ontologyTree.setCellRenderer(cellRenderer);
  }

  /** A method to show an empty tree */
  public void showEmptyOntologyTree() {
    setCurrentJapelateSelection(null);
    ontologyTree.setVisible(false);

  }

  public Component getGUI() {
    return this;
  }

  public void showJapelates(String[] data) {
    setCurrentJapelateSelection(ruleBaseViewer.japelateSelectionMap);
    ontologyTree.setListData(data);
    // ((CheckRenderer)ontologyTree.getCellRenderer()).selectAll();
    ontologyTree.invalidate();

  }

  public void setSelected(String row, boolean value) {
    getCurrentJapelateSelection().put(row, new Boolean(value));
  }

  @Override
  public HashMap<String, Boolean> getCurrentOResource2IsSelectedMap() {
    return getCurrentJapelateSelection();
  }

  @Override
  public HashMap<String, Color> getCurrentOResource2ColorMap() {
    return new HashMap<>();
  }

  /**
   * @return the currentJapelateSelection
   */
  private HashMap<String, Boolean> getCurrentJapelateSelection() {
    return currentJapelateSelection;
  }

  /**
   * @param currentJapelateSelection the currentJapelateSelection to set
   */
  private void setCurrentJapelateSelection(HashMap<String, Boolean> currentJapelateSelection) {
    this.currentJapelateSelection = currentJapelateSelection;
  }

}
