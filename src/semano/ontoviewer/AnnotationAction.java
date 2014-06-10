/*
 *  AnnotationAction.java
 *
 *  Niraj Aswani, 12/March/07
 *
 *  $Id: AnnotationAction.html,v 1.0 2007/03/12 16:13:01 niraj Exp $
 */
package semano.ontoviewer;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

/**
 * This class provides the GUI implementation for creating/changing/deleting
 * annotations from the text. It uses OntologyTreePanel to display the list of
 * available classes in the ontology.
 * 
 * @author niraj
 */
public class AnnotationAction extends MouseInputAdapter {

  /**
   * Reference to the main OntologyTreePanel object
   */
  private OntologyTreePanel ontologyTreePanel;
  
  private OntologyViewer viewer;

  /**
   * Timer object
   */
  private javax.swing.Timer annotationWindowTimer;

  /**
   * How long we should wait before showing a new annotation/change annotation
   * window.
   */
  private final int DELAY = 0;

  /**
   * Action that is performed when user decides to create a new annotation.
   */
  private AnnotationEditor annotationEditor;

  /**
   * Constructor
   * @param ontologyViewer 
   * 
   * @param ontoViewer
   *          the instance this instance uses to obtain the information about
   *          ontology
   */
  public AnnotationAction(OntologyTreePanel ontoTreePanel,OntologyTreePanel ontoTreePanelProperties, OntologyViewer ontologyViewer) {
    this.ontologyTreePanel = ontoTreePanel;
    annotationEditor = new AnnotationEditor(ontoTreePanel,ontoTreePanelProperties, ontologyViewer);
    annotationWindowTimer = new javax.swing.Timer(DELAY, annotationEditor);
    annotationWindowTimer.setRepeats(false);
    viewer=ontologyViewer;
  }

  /**
   * Grabs the current location of mouse pointers
   * 
   * @param e
   */
  public void mousePressed(MouseEvent e) {
    // if mouse is pressed anywhere, we simply hide all the windows
    hideAllWindows();
  }

  /**
   * This method to hide all the popup windows
   */
  public void hideAllWindows() {
    if(ontologyTreePanel.showingAnnotationWindow) {
      ontologyTreePanel.showingAnnotationWindow = false;
      annotationEditor.hideWindow();
    }
  }


  /**
   * @param e
   */
  private void fireAnnotationEvent(MouseEvent e) {
    if(ontologyTreePanel.ontologyTree == null
      || ontologyTreePanel.showingAnnotationWindow) return;
    // mouse is moved so simply activate the timer
    annotationEditor
      .setTextLocation(viewer.documentTextArea
        .viewToModel(e.getPoint()));
    annotationWindowTimer.restart();
    annotationEditor.setMousePoint(e.getPoint());
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(MouseEvent e) {
      fireAnnotationEvent(e);
  }
  
  
}
