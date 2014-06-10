/*
 *  AnnonymousClassImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: AnonymousClassImpl.java 11600 2009-10-13 17:13:22Z johann_p $
 */
package semano.ontologyowl.impl;

import gate.creole.ontology.AnonymousClass;
import gate.creole.ontology.ONodeID;
import gate.creole.ontology.Ontology;

/**
 * Implementation of the AnonymousClass
 *
 * @author niraj
 */
public class AnonymousClassImpl extends OClassImpl implements AnonymousClass {
    /**
     * Constructor
     *
     * @param aURI
     * @param ontology
     * @param repositoryID
     * @param owlimPort
     */
    public AnonymousClassImpl(ONodeID aURI, Ontology ontology,
                              OWLOntologyService owlimPort) {
        super(aURI, ontology, owlimPort);
    }
}
