package semano.ontoviewer.autoannotation;

import gate.util.GateRuntimeException;
import semano.ontoviewer.OntologyViewer;
import semano.util.Settings;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


public class AnnotationRunner implements Runnable {

    boolean animate = true;
    OntologyViewer ontologyViewer;
    AutoAnnotatorStringBased annotator;

    Set<JobFinishedListener> listeners = new HashSet<JobFinishedListener>();

    protected void registerListener(JobFinishedListener l) {
        listeners.add(l);
    }

    /**
     * @param animate        true if should show real time annotation.
     * @param ontologyViewer
     * @param annotator
     */
    public AnnotationRunner(boolean animate, OntologyViewer ontologyViewer, AutoAnnotatorStringBased annotator) {
        super();
        this.animate = animate;
        this.ontologyViewer = ontologyViewer;
        this.annotator = annotator;
    }


    /**
     * @deprecated  actually doesn't do anything, maybe this should change?
     * @param text
     */
    private void setStatusMessage(String text) {
//    sListener = new StatusListener(){
//      @Override
//      public void statusChanged(String text){
//        fireStatusChanged(text);
//      }
//    };
//    gate.event.StatusListener sListener = (gate.event.StatusListener)MainFrame.
//            .getListeners().get("gate.event.StatusListener");
//    if(sListener != null) {
//      sListener.statusChanged(text);
//    }
    }

    /**
     * @param progress
     * @return
     */
    private void setProgress(int progress) {
//    gate.event.ProgressListener pListener = (gate.event.ProgressListener)MainFrame
//            .getListeners().get("gate.event.ProgressListener");
//    if(pListener != null) {
//      if(progress == 100) {
//        pListener.processFinished();
//      }
//      else {
//        pListener.progressChanged(progress);
//      }
//    }
    }


    public void run() {
        setStatusMessage("Annotating text...");
        ontologyViewer.setAutoannotation(true);
        // time
        Calendar startingTime = Calendar.getInstance();
        setProgress(0);
        try {
            // MainFrame.lockGUI("Annotating/restoring ... ");

            if (Settings.ANNOTATE_WITH_NAMES) {
                annotator.annotateClassesWithLabels();
            }
            setProgress(30);
            // ontoViewer.refreshHighlights();

            int tasks = Settings.annotationProperties.size() + 1;
            int done = 0;
            for (semano.ontoviewer.AnnotationMetaData ap : Settings.annotationProperties) {
                if (ap.isAutoannotate()) {
                    annotator.annotateClassesWithAnnoProp(ap);
                    setProgress(30 + 30 * ++done / tasks);
                }
            }

            if (Settings.ANNOTATE_RELATIONS) {
                annotator.annotateProperties();

            }
            setProgress(100);


            ontologyViewer.setAutoannotation(false);
            if (ontologyViewer.hasGUI()) {
                ontologyViewer.refreshHighlights();
            }
            for (JobFinishedListener l : this.listeners)
                l.notifyJobFinished();
        } catch (Exception ex) {
            throw new GateRuntimeException("Problem annotating", ex);
        } finally {
            // MainFrame.unlockGUI();
            Calendar endTime = Calendar.getInstance();
            endTime.setTimeInMillis(endTime.getTimeInMillis()
                    - startingTime.getTimeInMillis());
            setStatusMessage("Finished annotation in " + endTime.getTimeInMillis()
                    / 1000 + " seconds!");
        }
    }


}
