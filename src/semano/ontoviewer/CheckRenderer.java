/*
 *  CheckRenderer.java
 *
 *  Niraj Aswani, 12/March/07
 *
 *  $Id: CheckRenderer.html,v 1.0 2007/03/12 16:13:01 niraj Exp $
 */
package semano.ontoviewer;

import gate.creole.ontology.OClass;
import gate.creole.ontology.OInstance;
import gate.gui.MainFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import semano.rulebaseeditor.TreePanel;

import com.ontotext.gate.vr.ClassNode;
import com.ontotext.gate.vr.IFolder;

/**
 * Description: This class provides the renderer for the Ontology Tree Nodes.
 * 
 * @author Niraj Aswani
 * @version 1.0
 */
public class CheckRenderer extends JPanel implements TreeCellRenderer,ListCellRenderer<String> {

  /**
   * Serial Version ID
   */
  private static final long serialVersionUID = 3257004371551204912L;

  /**
   * Allows user to select/deselect class in the ontology Tree
   */
  private JCheckBox check;

  /**
   * Class label is shown using this label
   */
  private JLabel label;

  /**
   * ICon label
   */
  private JLabel iconLabel;

  /**
   * Label Panel
   */
  private JPanel iconPanel, labelPanel;

  /**
   * The instance of ontologyTreePanel
   */
  private TreePanel ontologyTreePanel;

  /**
   * Constructor
   * 
   * @param owner
   */
  public CheckRenderer(TreePanel owner) {
    this.ontologyTreePanel = owner;
    check = new JCheckBox();
    check.setBackground(Color.WHITE);
    label = new JLabel();
    iconLabel = new JLabel();

    iconPanel = new JPanel(new BorderLayout(5, 5));
    setBackground(Color.WHITE);
    iconPanel.setBackground(Color.WHITE);
    ((BorderLayout)iconPanel.getLayout()).setHgap(0);
    iconPanel.setOpaque(true);
    iconPanel.add(check, BorderLayout.WEST);
    iconPanel.add(iconLabel, BorderLayout.EAST);

    labelPanel = new JPanel(new BorderLayout(5, 10));
    ((BorderLayout)labelPanel.getLayout()).setHgap(0);
    // labelPanel.setOpaque(true);
    labelPanel.add(label, BorderLayout.WEST);
    labelPanel.setBackground(Color.WHITE);

    setLayout(new FlowLayout(FlowLayout.LEFT,1,1));
    add(iconPanel);
    add(labelPanel);
  }

  /**
   * Renderer method
   */
  public Component getTreeCellRendererComponent(JTree tree, Object value,
    boolean isSelected, boolean expanded, boolean leaf, int row,
    boolean hasFocus) {

    Object userObject = value;
    if(!(userObject instanceof IFolder)) {
      label.setBackground(Color.white);
      return this;
    }

    javax.swing.Icon icon = null;
    ClassNode node = (ClassNode)userObject;
    String conceptName = node.toString();

    if(row == 0) {
      // this is the ontology name
      check.setVisible(false);
      iconLabel.setVisible(false);
      label.setText(conceptName);
      labelPanel.setBackground(Color.white);
      iconPanel.setBackground(Color.WHITE);
      return this;
    }
    else {
      check.setVisible(true);
      iconLabel.setVisible(true);
      }

    boolean selected = getSelection(conceptName);
    check.setSelected(selected);
    if(node.getSource() instanceof OClass) {
      iconLabel.setIcon(MainFrame.getIcon("ontology-class"));
    }
    else if(node.getSource() instanceof OInstance) {
      iconLabel.setIcon(MainFrame.getIcon("ontology-instance"));
    }
    else {
      iconLabel.setIcon(null);
    }

    label.setText(conceptName);
    label.setFont(tree.getFont());

    // We assign the automatically generated random colors to the
    // concept,
    // but randomly generation of colors for different classes takes
    // place
    // only once when that ontology is loaded for the first time
    if(ontologyTreePanel.getCurrentOResource2ColorMap().containsKey(conceptName)) {
      Color color =
        (Color)ontologyTreePanel.getCurrentOResource2ColorMap().get(conceptName);
      labelPanel.setBackground(color);
      iconPanel.setBackground(Color.WHITE);
    }
    check.setEnabled(true);
    label.setEnabled(true);
    
    return this;
  }

  public boolean getSelection(String element) {
    Boolean selected = false;
    HashMap<String, Boolean> currentOResource2IsSelectedMap = ontologyTreePanel.getCurrentOResource2IsSelectedMap();
    if(currentOResource2IsSelectedMap != null  &&  currentOResource2IsSelectedMap.containsKey(element)) {
      selected=currentOResource2IsSelectedMap.get(element);
    }
    if(selected == null) {
      selected = new Boolean(false);
//      ontologyTreePanel.getCurrentOResource2IsSelectedMap().put(element, bValue);
    }
    return selected;
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends String> list,
          String value, int index, boolean isSelected, boolean cellHasFocus) {

    boolean selected = getSelection(value);
    check.setSelected(selected);
    labelPanel.setBackground(Color.WHITE);
    label.setFont(list.getFont());
    check.setVisible(true);
    iconLabel.setIcon(MainFrame.getIcon("ontology-class"));
    iconLabel.setVisible(false);
    label.setText(value);
    check.setEnabled(true);
    label.setEnabled(true);    
    return this;

  }
}
