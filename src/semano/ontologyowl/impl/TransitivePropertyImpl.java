/*
 *  TransitivePropertyImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: TransitivePropertyImpl.java 11600 2009-10-13 17:13:22Z johann_p $
 */
package semano.ontologyowl.impl;


import gate.creole.ontology.OURI;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.TransitiveProperty;

/**
 * Implementation of the TransitiveProperty
 *
 * @author niraj
 */
public class TransitivePropertyImpl extends ObjectPropertyImpl implements
        TransitiveProperty {
    /**
     * Constructor
     *
     * @param aURI
     * @param ontology
     * @param owlimPort
     */
    public TransitivePropertyImpl(OURI aURI, Ontology ontology,
                                  OWLOntologyService owlimPort) {
        super(aURI, ontology, owlimPort);
    }
}
