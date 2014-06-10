package semano.ontoviewer;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

public class WriteInstancesAction implements Action {

  InstancesWriter ow;
  
  public WriteInstancesAction(OntologyViewer ontoViewer) {
    ow = new InstancesWriter(ontoViewer);
  }

  public void addPropertyChangeListener(PropertyChangeListener arg0) {
    
  }

  public Object getValue(String arg0) {
    return null;
  }

  public boolean isEnabled() {
    return true;
  }

  public void putValue(String arg0, Object arg1) {

  }

  public void removePropertyChangeListener(PropertyChangeListener arg0) {

  }

  public void setEnabled(boolean arg0) {

  }

  public void actionPerformed(ActionEvent arg0) {
    ow.writeInstacnesToFile();

  }

}
