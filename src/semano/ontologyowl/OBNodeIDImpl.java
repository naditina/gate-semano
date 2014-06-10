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
 *  $Id: OBNodeIDImpl.java 11598 2009-10-13 13:44:17Z johann_p $
 */

package semano.ontologyowl;

import gate.creole.ontology.OBNodeID;

/**
 * @author johann
 */
public class OBNodeIDImpl extends ONodeIDImpl implements OBNodeID {
    public OBNodeIDImpl(String uri) {
        super(uri, true);
    }
}
