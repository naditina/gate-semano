/*
 *  CheckRenderer.java
 *
 *  Niraj Aswani, 12/March/07
 *
 *  $Id: ComboRenderer.java 10618 2009-07-14 17:52:47Z nirajaswani $
 */
package semano.ontoviewer;

import gate.creole.ontology.OClass;
import gate.creole.ontology.OInstance;
import gate.gui.MainFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import semano.rulebaseeditor.TreePanel;

import com.ontotext.gate.vr.ClassNode;
import com.ontotext.gate.vr.IFolder;

/**
 * Description: This class provides the renderer for the Ontology Tree Nodes.
 * 
 * @author Niraj Aswani
 * @version 1.0
 */
public class ComboRenderer extends JPanel implements ListCellRenderer {

  /**
   * Serial Version ID
   */
  private static final long serialVersionUID = 3257004371551204912L;

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
  private JPanel labelPanel;

  /**
   * The instance of ontologyTreePanel
   */
  private TreePanel treePanel;

  /**
   * Constructor
   * 
   * @param owner
   */
  public ComboRenderer(TreePanel owner) {
    this.treePanel = owner;
    label = new JLabel();
    iconLabel = new JLabel();

    labelPanel = new JPanel(new BorderLayout(5, 10));
    ((BorderLayout)labelPanel.getLayout()).setHgap(0);
    labelPanel.add(label);

    setLayout(new BorderLayout(5, 10));
    ((BorderLayout)getLayout()).setHgap(1);
    add(iconLabel, BorderLayout.WEST);
    add(labelPanel, BorderLayout.CENTER);
    this.setOpaque(true);
  }

  /**
   * Renderer method
   */
  public Component getListCellRendererComponent(JList list, Object value,
    int row, boolean isSelected, boolean hasFocus) {

    Object userObject = value;
    ClassNode item = (ClassNode)userObject;

    if(!(item instanceof IFolder)) {
      label.setBackground(Color.white);
      return this;
    }

    String conceptName = item.getSource().toString();
    iconLabel.setVisible(true);

    if(item.getSource() instanceof OClass) {
      iconLabel.setIcon(MainFrame.getIcon("ontology-class"));
    }
    else if(item.getSource() instanceof OInstance) {
      iconLabel.setIcon(MainFrame.getIcon("ontology-instance"));
    }
    else {
      iconLabel.setIcon(null);
    }

    label.setText(conceptName);
    label.setFont(list.getFont());

    // We assign the automatically generated random colors to the concept,
    // but randomly generation of colors for different classes takes place
    // only once when that ontology is loaded for the first time
    if(treePanel.getCurrentOResource2ColorMap().containsKey(conceptName)) {
      Color color =
        (Color)treePanel.getCurrentOResource2ColorMap().get(conceptName);
      labelPanel.setBackground(color);
      iconLabel.setBackground(Color.WHITE);
    }
    return this;
  }
}