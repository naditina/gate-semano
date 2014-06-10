/*
 *  OntologyTreeListener.java
 *
 *  Niraj Aswani, 12/March/07
 *
 *  $Id: OntologyTreeListener.html,v 1.0 2007/03/12 16:13:01 niraj Exp $
 */
package semano.ontoviewer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import com.ontotext.gate.vr.ClassNode;
import com.ontotext.gate.vr.IFolder;

/**
 * Description: This class is responsible for events on the tree
 * @author Nadejda Nikitina
 * @version 1.0
 */
public class OntologyTreeListenerBasic extends MouseAdapter {

  /**
   * Instance of OntologyTreePanel
   */
  private OntologyTreePanel ontologyTreePanel;

  private Viewer viewer;

  /** Constructor */
  public OntologyTreeListenerBasic(OntologyTreePanel owner, Viewer viewer) {
    this.setOntologyTreePanel(owner);
    this.setViewer(viewer);
  }

  /**
   * This method is invoked whenever user clicks on one of the classes
   * in the ontology tree
   */
  public void mouseClicked(MouseEvent me) {
    // ok now find out the currently selected node
    int x = me.getX();
    int y = me.getY();
    JTree tree = getOntologyTreePanel().ontologyTree;
    int row = tree.getRowForLocation(x, y);
    if(row == 0) {
      // do nothing
      return;
    }
    TreePath path = tree.getPathForRow(row);

    if(path != null) {
      final ClassNode node = (ClassNode)path.getLastPathComponent();
      // right click
      if(SwingUtilities.isRightMouseButton(me)) {
        handleRightClick(x, y, node);
      } else {

        boolean isSelected =
                !getOntologyTreePanel().getCurrentOResource2IsSelectedMap()
                        .get(node.toString()).booleanValue();
        updateSelectionRecursively(node, isSelected);
        getOntologyTreePanel().ontologyTree.repaint();
        getViewer().refreshHighlights();
      }
    }
  }

  private void updateSelectionRecursively(ClassNode node, boolean isSelected) {
    getOntologyTreePanel().setSelected(node.toString(), isSelected);
    for(Object c:node.children()){
      updateSelectionRecursively((ClassNode)c,isSelected);
    }
    
  }

  protected void handleRightClick(int x, int y, final ClassNode node) { }

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

  /**
   * @return the ontologyTreePanel
   */
  protected OntologyTreePanel getOntologyTreePanel() {
    return ontologyTreePanel;
  }

  /**
   * @param ontologyTreePanel the ontologyTreePanel to set
   */
  protected void setOntologyTreePanel(OntologyTreePanel ontologyTreePanel) {
    this.ontologyTreePanel = ontologyTreePanel;
  }

  /**
   * @return the viewer
   */
  protected Viewer getViewer() {
    return viewer;
  }

  /**
   * @param viewer the viewer to set
   */
  protected void setViewer(Viewer viewer) {
    this.viewer = viewer;
  }

}
