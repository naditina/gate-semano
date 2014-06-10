/*
 *  Copyright (c) 1998-2009, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Johann Petrak 2009-08-20
 *
 *  $Id: LiteralOrONodeIDImpl.java 11600 2009-10-13 17:13:22Z johann_p $
 */
package semano.ontologyowl.impl;

import gate.creole.ontology.Literal;
import gate.creole.ontology.LiteralOrONodeID;
import gate.creole.ontology.ONodeID;
import gate.util.GateRuntimeException;

/**
 * Wrap either a Literal or a ONodeID object.
 * <p/>
 * TODO: should we implement comparable and equals/hashcode for this?
 *
 * @author Johann Petrak
 */
public class LiteralOrONodeIDImpl implements LiteralOrONodeID {
    protected Object theObject;
    protected boolean isLiteral;

    public LiteralOrONodeIDImpl(Literal aLiteral) {
        theObject = aLiteral;
        isLiteral = true;
    }

    public LiteralOrONodeIDImpl(ONodeID aONodeID) {
        theObject = aONodeID;
        isLiteral = false;
    }

    public boolean isLiteral() {
        return isLiteral;
    }

    public boolean isONodeID() {
        return !isLiteral;
    }

    public ONodeID getONodeID() {
        if (isLiteral) {
            throw new GateRuntimeException(
                    "Cannot return an ONodeID, have a Literal: " + ((Literal) theObject));
        }
        return (ONodeID) theObject;
    }

    public Literal getLiteral() {
        if (!isLiteral) {
            throw new GateRuntimeException(
                    "Cannot return a Literal, have an ONodeID: " + ((ONodeID) theObject));
        }
        return (Literal) theObject;
    }

    @Override
    public String toString() {
        if (isLiteral) {
            return ((Literal) theObject).toString();
        } else {
            return ((ONodeID) theObject).toString();
        }
    }

    public String toTurtle() {
        if (isLiteral) {
            return ((Literal) theObject).toTurtle();
        } else {
            return ((ONodeID) theObject).toTurtle();
        }
    }
}
