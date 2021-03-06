/*
 *  Copyright (c) 1998-2009, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Johann Petrak 2009-08-13
 *
 *  $Id: Ontology.java 11598 2009-10-13 13:44:17Z johann_p $
 */
package semano.ontologyowl;

import gate.Gate;
import gate.Resource;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;
import gate.gui.ActionsPublisher;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import semano.ontologyowl.impl.OWLOntologyServiceImpl;

/**
 * The common abstract base class for all ontology LRs in this implementation.
 *
 * @author Johann Petrak
 */
public abstract class OntologyResource
        extends AbstractOWLOntology
        implements ActionsPublisher, CreoleListener {

    /**
     * Actions for the remote ontology LR.
     */
    protected List<AbstractAction> actionsList;

    private Logger logger;

    /**
     * Constructor
     */
    public OntologyResource() {
        super();
        logger = Logger.getLogger(this.getClass().getName());
        actionsList = new ArrayList<AbstractAction>();
        actionsList.add(new ActionLoadOntology("Load", this));
//    actionsList.add(new ActionSaveOntology("Save as ...",this));
//    actionsList.add(new ActionCleanOntology("Delete ontology data",this));
    }


    public List getActions() {
        return actionsList;
    }

    // ************ Creole Listener Methods ************
    public void resourceLoaded(CreoleEvent ce) {
        // do nothing
    }

    public void datastoreClosed(CreoleEvent ce) {
    }

    public void resourceRenamed(Resource resource, String oldname, String newname) {
    }

    public void datastoreOpened(CreoleEvent ce) {
    }

    public void datastoreCreated(CreoleEvent ce) {
    }

    /**
     * This method removes the repository, provided that user has decided
     * not to persist this, when this resource is unloaded
     */
    // TODO: when does this get executed?
    public void resourceUnloaded(CreoleEvent ce) {
        logger.debug("============= Running resourceUnloaded!");
        if (ce.getResource() == this) {
            //unload();
            Gate.getCreoleRegister().removeCreoleListener(this);
        }
    }

    /**
     * This method deletes the repository from memory and releases all
     * resources occupied by the ontology. Please note that after calling
     * this method, any call to any method of the ontology will throw an
     * exception complaining that the repository does not exist. So this
     * should be the last call to this ontology when you decide to discard
     * this instance of ontology.
     */
    public void cleanup() {
        super.cleanup();
        logger.debug("Running cleanup ...");
        if (ontologyService != null) {
            ontologyService.shutdown();
            ontologyService = null;
        } else {
            logger.debug("ongologyService is already null in cleanup!");
        }
    }

    // just for testing
    public OWLOntologyServiceImpl getService() {
        return (OWLOntologyServiceImpl) ontologyService;
    }

}