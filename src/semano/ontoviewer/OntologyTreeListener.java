/*
 *  OntologyTreeListener.java
 *
 *  Niraj Aswani, 12/March/07
 *
 *  $Id: OntologyTreeListener.html,v 1.0 2007/03/12 16:13:01 niraj Exp $
 */
package semano.ontoviewer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.tree.TreePath;

import com.ontotext.gate.vr.ClassNode;
import com.ontotext.gate.vr.IFolder;

/**
 * Description: This class is responsible for events on the tree
 * 
 * including right click
 * 
 * @author Nadejda Nikitina
 * @version 1.0
 */
public class OntologyTreeListener extends OntologyTreeListenerBasic {


  /** Constructor */
  public OntologyTreeListener(OntologyTreePanel owner, OntologyViewer viewer) {
    super(owner,viewer);
  }

  /**
   * This method is invoked whenever user clicks on one of the classes
   * in the ontology tree
   */
  public void mouseClicked(MouseEvent me) {
    super.mouseClicked(me);
        getViewer().documentTextArea.requestFocus();
      
    
  }

  protected void handleRightClick(int x, int y, final ClassNode node) {
    final Color color =
            getOntologyTreePanel().getCurrentOResource2ColorMap().get(
                    node.toString());
    final JPopupMenu popup = new JPopupMenu();
    JMenuItem cancel = new JMenuItem("Close");
    cancel.setToolTipText("Closes this popup");
    JMenuItem changeColor = new JMenuItem("Change Color");
    changeColor.setToolTipText("Changes Color");
    JMenuItem showAnnotations = new JMenuItem("Show Annotations");
    showAnnotations
            .setToolTipText("Shows all annotations of this element already contained in the ontology");

    changeColor.setToolTipText("Changes Color of this list element");

    ToolTipManager.sharedInstance().registerComponent(cancel);
    ToolTipManager.sharedInstance().registerComponent(changeColor);

    popup.add(new JLabel(node.toString()));
    popup.addSeparator();
    popup.add(changeColor);
    popup.add(showAnnotations);
    popup.add(cancel);
    popup.setOpaque(true);
    popup.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));

    changeColor.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        Color col =
                JColorChooser.showDialog(getOntologyTreePanel(),
                        "Select colour for \"" + node.toString() + "\"", color);
        if(col != null) {
          Color colAlpha =
                  new Color(col.getRed(), col.getGreen(), col.getBlue(), 128);
          getOntologyTreePanel().setColor(node.toString(), colAlpha);
          // so let us update our tree
          // and rehighlight
          getOntologyTreePanel().repaint();
          getViewer().refreshHighlights();
          popup.setVisible(false);
        }
      }
    });

    showAnnotations.addActionListener(new AnnotationListViewer(
            getOntologyTreePanel(), node, getViewer()));

    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        popup.setVisible(false);
      }
    });
    popup.setVisible(true);
    popup.show(getOntologyTreePanel(), x, y);
    getViewer().documentTextArea.requestFocus();
    return;
  }

  /**
   * This is to expand all the paths
   * 
   * @param paths => This should be the all ancestor in proper sequence
   *          including the current node at end for e.g. root,
   *          greatgrandfather, grandfather, father, currentnode
   * @param node => it is currentnode.. providing this would help
   *          interate through all this node's children and expand them
   *          into the ontotree
   */
  private void expandChildren(Object[] paths, IFolder node) {
    Object[] newPath = new Object[paths.length + 1];
    System.arraycopy(paths, 0, newPath, 0, paths.length);

    if(node.getChildCount() > 0) {
      Iterator iter = node.getChildren();
      while(iter.hasNext()) {
        IFolder node1 = (IFolder)iter.next();
        newPath[newPath.length - 1] = node1;
        expandChildren(newPath, node1);
      }
    } else {
      Object[] myArray = new Object[paths.length - 1];
      System.arraycopy(paths, 0, myArray, 0, paths.length - 1);
      TreePath temp = new TreePath(myArray);

      if(!getOntologyTreePanel().ontologyTree.isVisible(temp)) {
        getOntologyTreePanel().ontologyTree.expandPath(new TreePath(myArray));
      }
    }
  }

  /**
   * This method is to find out the path for this node from the root of
   * the tree
   * 
   * @param node
   * @return
   */
  public TreePath getTreePath(IFolder node) {
    IFolder root = (IFolder)getOntologyTreePanel().ontologyTree.getModel().getRoot();
    Object[] path = new Object[0];
    path = traverseThroughPath(root, node, path);
    return new TreePath(path);
  }

  /**
   * find out the tree path
   * 
   * @param currentNode
   * @param nodeToFind
   * @param path
   * @return updated path
   */
  private Object[] traverseThroughPath(IFolder currentNode, IFolder nodeToFind,
          Object[] path) {
    if(currentNode.equals(nodeToFind)) {
      return path;
    }
    if(currentNode.getChildCount() > 0) {
      Object[] tempPath = new Object[path.length + 1];
      System.arraycopy(path, 0, tempPath, 0, path.length);
      tempPath[tempPath.length - 1] = currentNode;
      Iterator children = currentNode.getChildren();
      while(children.hasNext()) {
        IFolder node = (IFolder)children.next();
        Object[] returnedPath = traverseThroughPath(node, nodeToFind, tempPath);
        if(returnedPath != null) {
          return returnedPath;
        }
      }
    }
    return null;
  }

  /**
   * Method to select the children nodes as well
   * 
   * @param node parent node
   * @param value isSelected
   */
  void setChildrenSelection(IFolder node, boolean value) {
    getOntologyTreePanel().setSelected(node.toString(), value);
    if(node.getChildCount() > 0) {
      Iterator iter = node.getChildren();
      while(iter.hasNext()) {
        setChildrenSelection((IFolder)iter.next(), value);
      }
    }
  }

  /**
   * @param node
   */
  public void updateOntologyTreePanelSelection(ClassNode node) {
    if(node != null) {
      getOntologyTreePanel().setSelected(node.toString(), true);
      // expand the path
      TreePath path = getTreePath(node);
      getOntologyTreePanel().ontologyTree.expandPath(path);
      getOntologyTreePanel().ontologyTree.scrollPathToVisible(path);
    }
  }

  protected OntologyViewer getViewer(){
    return (OntologyViewer)super.getViewer();
  }
}
