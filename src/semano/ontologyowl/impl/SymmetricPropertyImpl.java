/*
 *  SymmetricPropertyImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: SymmetricPropertyImpl.java 11600 2009-10-13 17:13:22Z johann_p $
 */
package semano.ontologyowl.impl;

import gate.creole.ontology.OURI;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.SymmetricProperty;

/**
 * Implementation of the SymmetricProperty
 *
 * @author niraj
 */
public class SymmetricPropertyImpl extends ObjectPropertyImpl implements
        SymmetricProperty {
    /**
     * Constructor
     *
     * @param aURI
     * @param ontology
     * @param repositoryID
     * @param owlimPort
     */
    public SymmetricPropertyImpl(OURI aURI, Ontology ontology,
                                 OWLOntologyService owlimPort) {
        super(aURI, ontology, owlimPort);
    }
}
