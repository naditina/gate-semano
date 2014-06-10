/**
 *
 */
package semano.ontologyowl.impl;

import gate.creole.ontology.HasValueRestriction;
import gate.creole.ontology.InvalidValueException;
import gate.creole.ontology.Literal;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.ONodeID;
import gate.creole.ontology.OResource;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.RDFProperty;
import gate.util.GateRuntimeException;

/**
 * @author niraj
 */
public class HasValueRestrictionImpl extends OClassImpl implements
        HasValueRestriction {

    /**
     * @param aURI
     * @param ontology
     * @param owlimPort
     */
    public HasValueRestrictionImpl(ONodeID aURI, Ontology ontology,
                                   OWLOntologyService owlimPort) {
        super(aURI, ontology, owlimPort);
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.HasValuesFromRestriction#getHasValue()
     */
    public Object getHasValue() {
        ResourceInfo resource = ontologyService.getRestrictionValue(
                this.nodeId.toString(), OConstants.HAS_VALUE_RESTRICTION);
        RDFProperty prop = getOnPropertyValue();
        if (prop instanceof DatatypePropertyImpl) {
            try {
                return new Literal(resource.getUri(), ((DatatypePropertyImpl) prop).getDataType());
            } catch (InvalidValueException ive) {
                throw new GateRuntimeException(ive);
            }
        }

        if (resource.getClassType() == OConstants.INSTANCE) {
            return Utils.createOInstance(this.ontology,
                    this.ontologyService, resource.getUri());
        }

        return Utils.createOClass(this.ontology, this.ontologyService,
                resource.getUri(), resource.getClassType());
    }

    /**
     * Sets the resource as a restricted value.
     *
     * @param resource
     */
    public void setHasValue(OResource resource) {
        ontologyService.setRestrictionValue(this.nodeId.toString(),
                OConstants.HAS_VALUE_RESTRICTION, resource.getONodeID()
                .toString());
    }


    /**
     * Sets the resource as a restricted value.
     *
     * @param resource
     */
    public void setHasValue(Literal literal) {
        ontologyService.setRestrictionValue(this.nodeId.toString(),
                OConstants.HAS_VALUE_RESTRICTION, literal.getValue());
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Restriction#getOnPropertyValue()
     */
    public RDFProperty getOnPropertyValue() {
        Property property = ontologyService.getOnPropertyValue(this.nodeId
                .toString());
        return Utils.createOProperty(ontology, ontologyService, property
                .getUri(), property.getType());
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Restriction#setOnPropertyValue(gate.creole.ontology.RDFProperty)
     */
    public void setOnPropertyValue(RDFProperty property) {
        ontologyService.setOnPropertyValue(this.nodeId.toString(), property
                .getOURI().toString());
        ontology.fireResourceRelationChanged(this,
                property, OConstants.RESTRICTION_ON_PROPERTY_VALUE_CHANGED);
    }
}
