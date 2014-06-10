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
 *  $Id: AbstractOntologyImpl.java 11760 2009-10-24 22:20:05Z johann_p $
 */
package semano.ontologyowl.impl;

import gate.Gate;
import gate.creole.AbstractLanguageResource;
import gate.creole.ResourceData;
import gate.creole.ontology.AllValuesFromRestriction;
import gate.creole.ontology.AnnotationProperty;
import gate.creole.ontology.AnonymousClass;
import gate.creole.ontology.CardinalityRestriction;
import gate.creole.ontology.DataType;
import gate.creole.ontology.DatatypeProperty;
import gate.creole.ontology.GateOntologyException;
import gate.creole.ontology.HasValueRestriction;
import gate.creole.ontology.InvalidValueException;
import gate.creole.ontology.Literal;
import gate.creole.ontology.MaxCardinalityRestriction;
import gate.creole.ontology.MinCardinalityRestriction;
import gate.creole.ontology.OBNodeID;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OConstants.Closure;
import gate.creole.ontology.OConstants.OntologyFormat;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.ONodeID;
import gate.creole.ontology.OResource;
import gate.creole.ontology.OURI;
import gate.creole.ontology.ObjectProperty;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.OntologyModificationListener;
import gate.creole.ontology.RDFProperty;
import gate.creole.ontology.SomeValuesFromRestriction;
import gate.creole.ontology.SymmetricProperty;
import gate.creole.ontology.TransitiveProperty;
import gate.creole.ontology.URI;
import gate.util.ClosableIterator;
import gate.util.GateRuntimeException;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


import org.apache.log4j.Logger;

import semano.ontologyowl.AnnotationValue;
import semano.ontologyowl.OURIImpl;
import semano.ontologyowl.OWLIMOntology;
import semano.util.Settings;

/**
 * This class provides implementation of most of the methods of Ontology
 * interface. This implementation is based on the OntologyService (a SAIL) that stores
 * data in repository using SESAME.
 *
 * @author Niraj Aswani
 * @author Johann Petrak
 */
public abstract class OWLOntology
        extends AbstractLanguageResource
        implements Ontology {

    /**
     * The ontology import URIs that were already processed for this ontology.
     * NOTE: this contains both the URIs from imports and the ontology URIs
     * from loading ontology data in order to prevent that an ontology that
     * was loaded gets again loaded as an import. Having an ontology loaded
     * both normally and as an import will cause things to break in absurd
     * and unpredictable ways!!!
     */
    protected Set<String> knownImportURIs = new HashSet<String>();

    protected List<OURI> loadedOntologyURIs = new ArrayList<OURI>();

    /**
     * instance of the OntologyService
     */
    protected OWLOntologyService ontologyService;

    /**
     * Main URL of the ontology. This is the URL that was used to load
     * the first ontology data. If not known, this is null.
     */
    protected URL ontologyURL;


    /**
     * Default Namespace
     */
    protected String defaultNameSpace;

    /**
     * Parameter that keeps track of if the ontology is modified
     */
    protected boolean isModified;

    /**
     * A List of ontology modification listeners
     */
    protected transient List<OntologyModificationListener> modificationListeners;


    /**
     * Map where the key is a resource name and value is a list of resources with
     * that name.
     */
    //protected Map<String, List<OResource>> resourceNamesToOResourcesMap;

    protected static int anonymousNodeCounter = 0;

    protected static String restrictionPrefix = "Restriction";

    /**
     * This field controls whether new ontology entities will have their
     * label set to the entity resource name automatically.
     * The behavior has changed from the old to the new implementation: the
     * old implementation always set the label, the new implementation does
     * not do that for the new LRs. However, to provide better temporary backwards
     * compatibility, the backwards compatibility LR sets this to true and
     * does create the labels automatically.
     */
    protected boolean doSetAutoLabel = false;

    private Logger logger;

    protected Random randomGenerator;

    /**
     * Constructor
     */
    public OWLOntology() {
        logger = Logger.getLogger(this.getClass().getName());
        //TODO: get rid of this!
        //urisToOResouceMap = new HashMap<String, OResource>();
        //resourceNamesToOResourcesMap = new HashMap<String, List<OResource>>();
        knownImportURIs.add("http://www.w3.org/2000/01/rdf-schema");
        randomGenerator = new Random();
    }


    // TODO: why does this look if the map for the generated name is
    // empty? If a global counter gets incremented, should it not always be
    // empty since that name has never been used before?
    public String getAutoGeneratedRestrictionName() {
        anonymousNodeCounter++;
        String toReturn = restrictionPrefix + anonymousNodeCounter;
    /* OLD CODE:
    String toReturn = null;
    while(true) {
      anonymousNodeCounter++;
      toReturn = restrictionPrefix + anonymousNodeCounter;
      List<OResource> resources = resourceNamesToOResourcesMap.get(toReturn);
      if(resources == null || resources.isEmpty()) break;
    }
     * */
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#cleanOntology()
     */
    public void cleanOntology() {
        ontologyService.cleanOntology();
        // TODO: REMOVE
        //urisToOResouceMap.clear();
        //resourceNamesToOResourcesMap.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getOntologyData(byte)
     */
    public String getOntologyData(byte format) {
        throw new UnsupportedOperationException("Method not supported in this implementation");
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#writeOntologyData(java.io.OutputStream,
     * byte)
     */
    public void writeOntologyData(OutputStream out, byte format) {
        throw new UnsupportedOperationException("Method not supported in this implementation");
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#writeOntologyData(java.io.Writer, byte)
     */
    public void writeOntologyData(Writer out, byte format) {
        throw new UnsupportedOperationException("Not supported any more in this implementation");
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getURL()
     */
    public URL getURL() {
        return ontologyURL;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#setURL(java.net.URL)
     */
    public void setURL(URL aUrl) {
        this.ontologyURL = aUrl;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#setDefaultNameSpace(gate.creole.ontology.URI)
     */
    public void setDefaultNameSpace(String theURI) {
        defaultNameSpace = theURI;
        if (defaultNameSpace != null &&
                !defaultNameSpace.endsWith("#") &&
                !defaultNameSpace.endsWith("/")) {
            throw new GateOntologyException(
                    "The default name space (base URI) must end with '#' or '/': " +
                            theURI);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getDefaultNameSpace()
     */
    public String getDefaultNameSpace() {
        return this.defaultNameSpace;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#setVersion(java.lang.String)
     */
    public void setVersion(String theVersion) {
        ontologyService.setVersion(theVersion);
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getVersion()
     */
    public String getVersion() {
        return ontologyService.getVersion();
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#addOClass(gate.creole.ontology.URI)
     */
    public OClass addOClass(OURI aURI, String superClassURI, byte classType) {
        OClass existing = this.getOClass(aURI);
        if (existing != null) {
            Utils.warning(aURI + " already exists");
            return existing;
        }
        ((OWLOntologyServiceImpl) ontologyService).addClass(aURI.toString(), superClassURI);
        OClass oClass =
                Utils.createOClass(this, ontologyService, aURI.toString(),
                        classType);
        return oClass;
    }


    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#addOClass(gate.creole.ontology.URI)
     */
    public OClass addOClass(OURI aURI, byte classType) {
        OClass existing = this.getOClass(aURI);
        if (existing != null) {
            Utils.warning(aURI + " already exists");
            return existing;
        }
        ((OWLOntologyServiceImpl) ontologyService).addClass(aURI.toString(), classType);
        OClass oClass =
                Utils.createOClass(this, ontologyService, aURI.toString(),
                        classType);
        return oClass;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#addOClass(gate.creole.ontology.URI,
     * byte)
     */
    public OClass addOClass(OURI aURI) {
        return addOClass(aURI, OConstants.OWL_CLASS);
    }
  
  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getOClass(gate.creole.ontology.URI)
   */
//  public OClass getOClass(OURI theClassURI) {
//    if(ontologyService.hasClass(theClassURI.toString())) {
//      byte classType =
//        ontologyService.getClassType(theClassURI.toString());
//      return Utils.createOClass(this, ontologyService,
//        theClassURI.toString(), classType);
//    }
//    return null;
//
//  }

    public OClass getOClass(ONodeID theClassURI) {
        //OResource resource = getOResourceFromMap(theClassURI.toString());
        //if(resource != null) { return (OClass)resource; }
        if (ontologyService.hasClass(theClassURI.toString())) {
            byte classType =
                    ontologyService.getClassType(theClassURI.toString());
            return Utils.createOClass(this, ontologyService,
                    theClassURI.toString(), classType);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#removeOClass(gate.creole.ontology.OClass)
     */
    public void removeOClass(OClass theClass) {

        if (!containsOClass(theClass.getONodeID())) {
            Utils.warning("Cannot remove the class " + theClass.getONodeID().toString() + " - does not exist");
            return;
        }

        String[] deletedResources =
                ontologyService.removeClass(theClass.getONodeID().toString(),
                        true);
        fireOntologyResourcesRemoved(deletedResources);
    }

    /**
     * @param theURI
     * @return
     */
    //public boolean containsOClass(OURI theURI) {
    //  return ontologyService.hasClass(theURI.toString());
    //}
    public boolean containsOClass(ONodeID theURI) {
        return ontologyService.hasClass(theURI.toString());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#containsOClass(gate.creole.ontology.OClass)
     */
    public boolean containsOClass(OClass theClass) {
        return ontologyService.hasClass(theClass.getONodeID().toString());
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getOClasses(boolean)
     */
    public Set<OClass> getOClasses(boolean top) {
        return ontologyService.getClasses(top);
    }

    public ClosableIterator<OClass> getOClassesIterator(boolean top) {
        return ontologyService.getClassesIterator(top);
    }


    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getDistance(gate.creole.ontology.OClass,
     * gate.creole.ontology.OClass)
     */
    public int getDistance(OClass class1, OClass class2) {

        if (!containsOClass(class1.getONodeID())) {
            Utils.warning(class1.getONodeID().toString() + " does not exist");
            return -1;
        }

        if (!containsOClass(class2.getONodeID())) {
            Utils.warning(class2.getONodeID() + " does not exist");
            return -1;
        }

        int result = 0;
        OClass c;
        ArrayList<Set<OClass>> supers1 = class1.getSuperClassesVSDistance();
        ArrayList<Set<OClass>> supers2 = class2.getSuperClassesVSDistance();
        for (int i1 = 0; i1 < supers1.size(); i1++) {
            if (supers1.get(i1).contains(class2)) {
                result = i1 + 1;
                break;
            }
        }
        for (int i2 = 0; i2 < supers2.size(); i2++) {
            if (supers2.get(i2).contains(class1)) {
                result = i2 + 1;
                break;
            }
        }
        if (0 == result) {
            for (int i1 = 0; i1 < supers1.size(); i1++) {
                for (int i2 = 0; i2 < supers2.size(); i2++) {
                    Set<OClass> s1 = supers1.get(i1);
                    Set<OClass> s2 = supers2.get(i2);
                    Iterator<OClass> i3 = s1.iterator();
                    while (i3.hasNext()) {
                        c = i3.next();
                        if (s2.contains(c)) {
                            result = i1 + i2 + 2;
                            i1 = supers1.size();
                            i2 = supers2.size();
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#addOInstance(gate.creole.ontology.URI,
     * gate.creole.ontology.OClass)
     */
    public OInstance addOInstance(OURI theInstanceURI, OClass theClass) {
//    if(!containsOClass(theClass.getONodeID())) {
//      Utils.error(theClass.getONodeID() + " does not exist");
//      return null;
//    }

        // TODO: how to properly not use the map here?
        OResource anInst = null; //getOResourceFromMap(theInstanceURI.toString());
        // if(anInst != null && !(anInst instanceof OInstance)) {
        //   Utils.error(anInst.getURI().toString() + " already exists but "
        //    + " is not an ontology instance!");
        //  return null;
        //}

    /*
    if(anInst != null &&
      ((OInstance)anInst).getOClasses(OConstants.TRANSITIVE_CLOSURE)
        .contains(theClass)) {
      Utils.warning(theInstanceURI.toString()
        + " is already registered as an instanceof "
        + theClass.getURI().toString());
      return (OInstance)anInst;
    }
     * */

        OInstance existing = getOInstance(theInstanceURI);
        if (existing != null) {
            Utils.warning("instance " + theInstanceURI + " already exists");
            return existing;
        }

        ontologyService.addIndividual(theClass.getONodeID().toString(),
                theInstanceURI.toString());
        OInstance oInst =
                Utils.createOInstance(this, ontologyService,
                        theInstanceURI.toString());
//    fireOntologyResourceAdded(oInst);
        if (doSetAutoLabel) {
            oInst.setLabel(theInstanceURI.getResourceName(), null);
        }
        return oInst;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#removeOInstance(gate.creole.ontology.OInstance
     * )
     */
    public void removeOInstance(OInstance theInstance) {
        if (!containsOInstance((OURI) theInstance.getOURI())) {
            Utils.warning(theInstance.getOURI() + " does not exist");
            return;
        }

        String[] deletedResources =
                ontologyService.removeIndividual(theInstance.getOURI().toString());
        System.out.println("Deleted for " + theInstance + ": " + deletedResources);
        fireOntologyResourcesRemoved(deletedResources);
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getOInstances()
     */
    public Set<OInstance> getOInstances() {
//    String[] oInsts = ontologyService.getIndividuals();
//    Set<OInstance> set = new HashSet<OInstance>();
//    for(int i = 0; i < oInsts.length; i++) {
//      set.add(Utils.createOInstance(this, this.ontologyService,
//        oInsts[i]));
//    }
//    return set;
        Set<OInstance> theInstances = new HashSet<OInstance>();
        ClosableIterator<OInstance> ii =
                ontologyService.getInstancesIterator(null, null);
        while (ii.hasNext()) {
            OInstance i = ii.next();
            //System.out.println("Adding to result: "+i);
            theInstances.add(i);
        }
        return theInstances;
    }

    public ClosableIterator<OInstance> getOInstancesIterator() {
        return ontologyService.getInstancesIterator(null, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#getOInstances(gate.creole.ontology.OClass,
     * boolean)
     */
    public Set<OInstance> getOInstances(OClass theClass, byte closure) {
        //throw new UnsupportedOperationException("Method not supported any more with these parameters");
        Closure theClosure = closure == OConstants.DIRECT_CLOSURE ?
                Closure.DIRECT_CLOSURE : Closure.TRANSITIVE_CLOSURE;
        return getOInstances(theClass, theClosure);
    }

    public Set<OInstance> getOInstances(OClass theClass, Closure closure) {
//    String[] oInsts =
//      ontologyService.getIndividuals(theClass.getONodeID()
//        .toString(), closure);
//    Set<OInstance> set = new HashSet<OInstance>();
//
//    if(!containsOClass(theClass.getONodeID())) {
//      Utils.warning("GetOInstances: "+theClass.getONodeID() + " does not exist");
//      return set;
//    }
//
//    for(int i = 0; i < oInsts.length; i++) {
//      set.add(Utils.createOInstance(this, this.ontologyService,
//        oInsts[i]));
//    }
//    return set;
        Set<OInstance> theInstances = new HashSet<OInstance>();
        ClosableIterator<OInstance> ii =
                ontologyService.getInstancesIterator(theClass.getONodeID(), closure);
        while (ii.hasNext()) {
            OInstance i = ii.next();
            //System.out.println("Adding to result: "+i);
            theInstances.add(i);
        }
        return theInstances;
    }

    public ClosableIterator<OInstance>
    getOInstancesIterator(OClass theClass, Closure closure) {
        return ontologyService.getInstancesIterator(theClass.getONodeID(), closure);
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getOInstance(gate.creole.ontology.URI)
     */
    // TODO: extremely bad performance, do this differnetly!
    public OInstance getOInstance(OURI theInstanceURI) {
//    // TODO: properly remove map
//    OResource resource = null; //= getOResourceFromMap(theInstanceURI.toString());
//    //if(resource != null) return (OInstance)resource;
//    List<String> individuals =
//      Arrays.asList(ontologyService.getIndividuals());
//    if(individuals.contains(theInstanceURI.toString())) { return Utils
//      .createOInstance(this, ontologyService, theInstanceURI
//        .toString()); }
//    return null;
        if (ontologyService.hasInstance(theInstanceURI, null, null)) {
            return Utils.createOInstance(this, ontologyService, theInstanceURI.toString());
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#containsOInstance(gate.creole.ontology.OInstance
     * )
     */
    public boolean containsOInstance(OInstance theInstance) {
        return containsOInstance(theInstance.getOURI());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#containsOInstance(gate.creole.ontology.URI)
     */
    // TODO: !!!! extremely bad performance, do this differently!
    public boolean containsOInstance(OURI theInstanceURI) {
//    List<String> individuals =
//      Arrays.asList(ontologyService.getIndividuals());
//    return individuals.contains(theInstanceURI.toString());
        return ontologyService.hasInstance(theInstanceURI, null, null);
    }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#addRDFProperty(gate.creole.ontology.URI,
   * java.util.Set, java.util.Set)
   */

    // TODO: this should really take a set of OClasses instead of a set of
    // OResources ...
    public RDFProperty addRDFProperty(OURI aPropertyURI, Set<OResource> domain,
                                      Set<OResource> range) {
        // TODO: properly remove map
        // OResource res = null; //= getOResourceFromMap(aPropertyURI.toString());
    /*
    if(res != null) {
      if(res instanceof RDFProperty) {
        Utils.warning(aPropertyURI.toString() + " already exists");
        return (RDFProperty)res;
      }
      else {
        Utils.error(aPropertyURI.toString()
          + " already exists but it is not an RDFProperty");
        return null;
      }
    }
     * */
        if (domain == null) {
            domain = new HashSet<OResource>();
        }
        if (range == null) {
            range = new HashSet<OResource>();
        }
        String[] domainURIs = new String[domain.size()];
        String[] rangeURIs = new String[range.size()];
        Iterator<OResource> iter = domain.iterator();
        int counter = 0;
        while (iter.hasNext()) {
            domainURIs[counter] = iter.next().getONodeID().toString();
        }
        iter = range.iterator();
        counter = 0;
        while (iter.hasNext()) {
            rangeURIs[counter] = iter.next().getONodeID().toString();
        }
        ontologyService.addRDFProperty(aPropertyURI.toString(),
                domainURIs, rangeURIs);
        RDFProperty rp =
                Utils.createOProperty(this, ontologyService, aPropertyURI
                        .toString(), OConstants.RDF_PROPERTY);
        fireOntologyResourceAdded(rp);

        return rp;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getRDFProperties()
     */
    public Set<RDFProperty> getRDFProperties() {
        Property[] properties = ontologyService.getRDFProperties();
        Set<RDFProperty> set = new HashSet<RDFProperty>();
        for (int i = 0; i < properties.length; i++) {
            set.add(Utils.createOProperty(
                    this, ontologyService, properties[i].getUri(), properties[i].getType()));
        }
        return set;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#isRDFProperty(gate.creole.ontology.URI)
     */
    public boolean isRDFProperty(OURI thePropertyURI) {
        return ontologyService.isRDFProperty(thePropertyURI
                .toString());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#addAnnotationProperty(gate.creole.ontology
     * .URI)
     */
    public AnnotationProperty addAnnotationProperty(OURI aPropertyURI) {
        RDFProperty exists = getProperty(aPropertyURI);
        if (exists != null) {
            if (exists instanceof DatatypeProperty) {
                Utils.warning(aPropertyURI.toString() + " already exists");
                return (AnnotationProperty) exists;
            }
            Utils.warning(aPropertyURI.toString() + " already exists but is not an annotation property");
            return null;
        }


        ontologyService.addAnnotationProperty(aPropertyURI
                .toString());
        AnnotationProperty ap =
                (AnnotationProperty) Utils.createOProperty(this,
                        ontologyService, aPropertyURI.toString(), OConstants.ANNOTATION_PROPERTY);
        fireOntologyResourceAdded(ap);
        if (doSetAutoLabel) {
            ap.setLabel(aPropertyURI.getResourceName(), null);
        }
        return ap;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getAnnotationProperties()
     */
    public Set<AnnotationProperty> getAnnotationProperties() {
        Property[] properties =
                ontologyService.getAnnotationProperties();
        Set<AnnotationProperty> set = new HashSet<AnnotationProperty>();
        for (int i = 0; i < properties.length; i++) {
            set.add((AnnotationProperty) Utils.createOProperty(
                    this, ontologyService, properties[i].getUri(),
                    properties[i].getType()));
        }
        return set;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#isAnnotationProperty(gate.creole.ontology
     * .URI)
     */
    public boolean isAnnotationProperty(OURI thePropertyURI) {
        return ontologyService.isAnnotationProperty(thePropertyURI
                .toString());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#addDatatypeProperty(gate.creole.ontology.URI,
     * java.util.Set, gate.creole.ontology.DataType)
     */
    public DatatypeProperty addDatatypeProperty(OURI aPropertyURI,
                                                Set<OClass> domain, DataType aDatatype) {
        if (domain == null) {
            domain = new HashSet<OClass>();
        }
        RDFProperty exists = getProperty(aPropertyURI);
        if (exists != null) {
            if (exists instanceof DatatypeProperty) {
                Utils.warning(aPropertyURI.toString() + " already exists");
                return (DatatypeProperty) exists;
            }
            Utils.warning(aPropertyURI.toString() + " already exists but is not a datatype property");
            return null;
        }

        String[] domainURIs = new String[domain.size()];
        Iterator<OClass> iter = domain.iterator();
        int counter = 0;
        while (iter.hasNext()) {
            domainURIs[counter] = iter.next().getONodeID().toString();
        }
        ontologyService.addDataTypeProperty(aPropertyURI.toString(),
                domainURIs, aDatatype.getXmlSchemaURIString());
        DatatypeProperty dp =
                (DatatypeProperty) Utils.createOProperty(this,
                        ontologyService, aPropertyURI.toString(), OConstants.DATATYPE_PROPERTY);
        fireOntologyResourceAdded(dp);

        if (doSetAutoLabel) {
            dp.setLabel(aPropertyURI.getResourceName(), null);
        }

        return dp;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getDatatypeProperties()
     */
    public Set<DatatypeProperty> getDatatypeProperties() {
        Property[] properties =
                ontologyService.getDatatypeProperties();
        Set<DatatypeProperty> set = new HashSet<DatatypeProperty>();
        for (int i = 0; i < properties.length; i++) {
            set.add((DatatypeProperty) Utils.createOProperty(
                    this, ontologyService, properties[i].getUri(), properties[i].getType()));
        }
        return set;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#isDatatypeProperty(gate.creole.ontology.URI)
     */
    public boolean isDatatypeProperty(OURI thePropertyURI) {
        return ontologyService.isDatatypeProperty(thePropertyURI
                .toString());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#addObjectProperty(gate.creole.ontology.URI,
     * java.util.Set, java.util.Set)
     */
    public ObjectProperty addObjectProperty(OURI aPropertyURI, Set<OClass> domain,
                                            Set<OClass> range) {

        if (domain == null) {
            domain = new HashSet<OClass>();
        }
        if (range == null) {
            range = new HashSet<OClass>();
        }
        RDFProperty exists = getProperty(aPropertyURI);
        if (exists != null) {
            if (exists instanceof ObjectProperty) {
                Utils.warning(aPropertyURI.toString() + " already exists");
                return (ObjectProperty) exists;
            }
            Utils.warning(aPropertyURI.toString() + " already exists but is not an object property");
            return null;
        }

        String[] domainURIs = new String[domain.size()];
        String[] rangeURIs = new String[range.size()];
        Iterator<OClass> iter = domain.iterator();
        int counter = 0;
        while (iter.hasNext()) {
            domainURIs[counter] = iter.next().getONodeID().toString();
            counter++;
        }
        iter = range.iterator();
        counter = 0;
        while (iter.hasNext()) {
            rangeURIs[counter] = iter.next().getONodeID().toString();
            counter++;
        }
        ontologyService.addObjectProperty(aPropertyURI.toString(),
                domainURIs, rangeURIs);
        ObjectProperty op =
                (ObjectProperty) Utils.createOProperty(this,
                        ontologyService, aPropertyURI.toString(), OConstants.OBJECT_PROPERTY);
//    fireOntologyResourceAdded(op);
//
//    if(doSetAutoLabel) {
//      op.setLabel(aPropertyURI.getResourceName(), null);
//    }

        return op;
    }


    public ObjectProperty addObjectProperty(OURI aPropertyURI, OURI propertyFromTheSameOntologySet, Set<OClass> domain,
                                            Set<OClass> range) {

        if (domain == null) {
            domain = new HashSet<OClass>();
        }
        if (range == null) {
            range = new HashSet<OClass>();
        }
        RDFProperty exists = getProperty(aPropertyURI);
        if (exists != null) {
            if (exists instanceof ObjectProperty) {
                Utils.warning(aPropertyURI.toString() + " already exists");
                return (ObjectProperty) exists;
            }
            Utils.warning(aPropertyURI.toString() + " already exists but is not an object property");
            return null;
        }

        String[] domainURIs = new String[domain.size()];
        String[] rangeURIs = new String[range.size()];
        Iterator<OClass> iter = domain.iterator();
        int counter = 0;
        while (iter.hasNext()) {
            domainURIs[counter] = iter.next().getONodeID().toString();
            counter++;
        }
        iter = range.iterator();
        counter = 0;
        while (iter.hasNext()) {
            rangeURIs[counter] = iter.next().getONodeID().toString();
            counter++;
        }
        ((OWLOntologyServiceImpl) ontologyService).addObjectProperty(aPropertyURI.toString(), propertyFromTheSameOntologySet.toString(),
                domainURIs, rangeURIs);
        ObjectProperty op =
                (ObjectProperty) Utils.createOProperty(this,
                        ontologyService, aPropertyURI.toString(), OConstants.OBJECT_PROPERTY);
        return op;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getObjectProperties()
     */
    public Set<ObjectProperty> getObjectProperties() {
        Property[] properties = ontologyService.getObjectProperties();
        Set<ObjectProperty> set = new HashSet<ObjectProperty>();
        for (int i = 0; i < properties.length; i++) {
            set.add((ObjectProperty) Utils.createOProperty(
                    this, ontologyService, properties[i].getUri(), properties[i].getType()));
        }
        return set;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#isObjectProperty(gate.creole.ontology.URI)
     */
    public boolean isObjectProperty(OURI thePropertyURI) {
        return ontologyService.isObjectProperty(thePropertyURI
                .toString());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#addSymmetricProperty(gate.creole.ontology
     * .URI, java.util.Set)
     */
    public SymmetricProperty addSymmetricProperty(OURI aPropertyURI,
                                                  Set<OClass> domainAndRange) {
        // TODO: properly not use map
    /*
    OResource res = getOResourceFromMap(aPropertyURI.toString());
    if(res != null) {
      if(res instanceof SymmetricProperty) {
        Utils.warning(aPropertyURI.toString() + " already exists");
        return (SymmetricProperty)res;
      }
      else {
        Utils.error(aPropertyURI.toString()
          + " already exists but it is not an SymmetricProperty");
        return null;
      }
    }
    */
        if (domainAndRange == null) {
            domainAndRange = new HashSet<OClass>();
        }

        RDFProperty exists = getProperty(aPropertyURI);
        if (exists != null) {
            if (exists instanceof DatatypeProperty) {
                Utils.warning(aPropertyURI.toString() + " already exists");
                return (SymmetricProperty) exists;
            }
            Utils.warning(aPropertyURI.toString() + " already exists but is not as ymmetric property");
            return null;
        }

        String[] domainURIs = new String[domainAndRange.size()];
        Iterator<OClass> iter = domainAndRange.iterator();
        int counter = 0;
        while (iter.hasNext()) {
            domainURIs[counter] = iter.next().getONodeID().toString();
            counter++;
        }
        ontologyService.addSymmetricProperty(
                aPropertyURI.toString(), domainURIs);
        SymmetricProperty sp =
                (SymmetricProperty) Utils.createOProperty(this,
                        ontologyService, aPropertyURI.toString(), OConstants.SYMMETRIC_PROPERTY);
        fireOntologyResourceAdded(sp);

        if (doSetAutoLabel) {
            sp.setLabel(aPropertyURI.getResourceName(), null);
        }

        return sp;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getSymmetricProperties()
     */
    public Set<SymmetricProperty> getSymmetricProperties() {
        Property[] properties =
                ontologyService.getSymmetricProperties();
        Set<SymmetricProperty> set = new HashSet<SymmetricProperty>();
        for (int i = 0; i < properties.length; i++) {
            set.add((SymmetricProperty) Utils.createOProperty(
                    this, ontologyService, properties[i].getUri(), properties[i].getType()));
        }
        return set;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#isSymmetricProperty(gate.creole.ontology.URI)
     */
    public boolean isSymmetricProperty(OURI thePropertyURI) {
        return ontologyService.isSymmetricProperty(thePropertyURI
                .toString());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#addTransitiveProperty(gate.creole.ontology
     * .URI, java.util.Set, java.util.Set)
     */
    public TransitiveProperty addTransitiveProperty(OURI aPropertyURI,
                                                    Set<OClass> domain, Set<OClass> range) {
        // TODO: properly remove map
    /*
    OResource res = getOResourceFromMap(aPropertyURI.toString());
    if(res != null) {
      if(res instanceof TransitiveProperty) {
        Utils.warning(aPropertyURI.toString() + " already exists");
        return (TransitiveProperty)res;
      }
      else {
        Utils.error(aPropertyURI.toString()
          + " already exists but it is not a TransitiveProperty");
        return null;
      }
    }
     * */
        if (domain == null) {
            domain = new HashSet<OClass>();
        }
        if (range == null) {
            range = new HashSet<OClass>();
        }
        RDFProperty exists = getProperty(aPropertyURI);
        if (exists != null) {
            if (exists instanceof DatatypeProperty) {
                Utils.warning(aPropertyURI.toString() + " already exists");
                return (TransitiveProperty) exists;
            }
            Utils.warning(aPropertyURI.toString() + " already exists but is not as transitive property");
            return null;
        }


        String[] domainURIs = new String[domain.size()];
        String[] rangeURIs = new String[range.size()];
        Iterator<OClass> iter = domain.iterator();
        int counter = 0;
        while (iter.hasNext()) {
            domainURIs[counter] = iter.next().getONodeID().toString();
            counter++;
        }
        iter = range.iterator();
        counter = 0;
        while (iter.hasNext()) {
            rangeURIs[counter] = iter.next().getONodeID().toString();
            counter++;
        }
        ontologyService.addTransitiveProperty(aPropertyURI
                .toString(), domainURIs, rangeURIs);
        TransitiveProperty tp =
                (TransitiveProperty) Utils.createOProperty(this,
                        ontologyService, aPropertyURI.toString(), OConstants.TRANSITIVE_PROPERTY);
        fireOntologyResourceAdded(tp);

        if (doSetAutoLabel) {
            tp.setLabel(aPropertyURI.getResourceName(), null);
        }

        return tp;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getTransitiveProperties()
     */
    public Set<TransitiveProperty> getTransitiveProperties() {
        Property[] properties =
                ontologyService.getTransitiveProperties();
        Set<TransitiveProperty> set = new HashSet<TransitiveProperty>();
        for (int i = 0; i < properties.length; i++) {

            set.add((TransitiveProperty) Utils.createOProperty(
                    this, ontologyService, properties[i].getUri(),
                    properties[i].getType()));
        }
        return set;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#isTransitiveProperty(gate.creole.ontology
     * .URI)
     */
    public boolean isTransitiveProperty(OURI thePropertyURI) {
        return ontologyService.isTransitiveProperty(thePropertyURI
                .toString());
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getPropertyDefinitions()
     */
    public Set<RDFProperty> getPropertyDefinitions() {
        Set<RDFProperty> set = new HashSet<RDFProperty>();
        set.addAll(getAnnotationProperties());
        set.addAll(getDatatypeProperties());
        set.addAll(getObjectProperties());
        set.addAll(getRDFProperties());
        return set;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getProperty(gate.creole.ontology.URI)
     */
    public RDFProperty getProperty(OURI thePropertyURI) {
        Property property =
                ontologyService.getRDFProperty(thePropertyURI
                        .toString());
        if (property == null) return null;
        return Utils.createOProperty(this, ontologyService,
                thePropertyURI.toString(), property.getType());
    }

    public AnnotationProperty getAnnotationProperty(OURI theURI) {
        Property[] ps = ontologyService.getAnnotationProperties(theURI.toString());
        if (ps != null) {
            for (Property p : ps) {
                AnnotationProperty ap = new AnnotationPropertyImpl(new OURIImpl(p.getUri()), this, ontologyService);
                //we just need one, so lets return one property
                if (ap != null) {
                    return ap;
                }
            }
        }
        return null;
    }

    public DatatypeProperty getDatatypeProperty(OURI theURI) {
        if (ontologyService.isDatatypeProperty(theURI.toString())) {
            return (DatatypeProperty) Utils.createOProperty(this, ontologyService,
                    theURI.toString(), OConstants.DATATYPE_PROPERTY);
        } else {
            return null;
        }
    }

    public ObjectProperty getObjectProperty(OURI theURI) {
        if (ontologyService.isObjectProperty(theURI.toString())) {
            return (ObjectProperty) Utils.createOProperty(this, ontologyService,
                    theURI.toString(), OConstants.OBJECT_PROPERTY);
        } else {
            return null;
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#removeProperty(gate.creole.ontology.RDFProperty
     * )
     */
    public void removeProperty(RDFProperty theProperty) {
        // TODO: properly remove map
    /*
    OResource res = getOResourceFromMap(theProperty.getURI().toString());
    if(res == null) {
      Utils.warning(theProperty.getURI().toString() + " does not exist");
      return;
    }
     * */

        String[] deletedResources =
                ontologyService.removePropertyFromOntology(theProperty
                        .getOURI().toString(), true);
        fireOntologyResourcesRemoved(deletedResources);
    }

    /**
     * Adds a new MinCardinality Restriction to the ontology. It automatically
     * creates a randon anonymous class, which it uses to denote the restriction.
     * The default datatype is set to NonNegativeIntegerNumber
     *
     * @param onProperty          - Specifies the property for which the restriction is being set.
     * @param minCardinalityValue - generally a numeric number.
     * @return
     * @throws InvalidValueException - if a value is not compatible with the nonNegativeIntegerNumber
     *                               datatype.
     */
    public MinCardinalityRestriction addMinCardinalityRestriction(
            RDFProperty onProperty, String minCardinalityValue)
            throws InvalidValueException {
        String restId = getAutoGeneratedRestrictionName();
        DataType datatype =
                OntologyUtilities.getDataType(OntologyUtilities.NON_NEGATIVE_INTEGER);

        ontologyService.addClass(restId,
                OConstants.MIN_CARDINALITY_RESTRICTION);

        ontologyService.setOnPropertyValue(restId, onProperty
                .getOURI().toString());

        if (!datatype.isValidValue(minCardinalityValue)) {
            throw new InvalidValueException(minCardinalityValue
                    + " is not valid for datatype " + datatype.getXmlSchemaURIString());
        }

        ontologyService.setPropertyValue(restId,
                OConstants.MIN_CARDINALITY_RESTRICTION, minCardinalityValue, datatype
                .getXmlSchemaURIString());

        MinCardinalityRestriction mcr =
                (MinCardinalityRestriction) Utils.createOClass(
                        this, ontologyService, restId, OConstants.MIN_CARDINALITY_RESTRICTION);

        fireOntologyResourceAdded(mcr);
        return mcr;
    }

    /**
     * Adds a new MaxCardinality Restriction to the ontology. It automatically
     * creates a randon anonymous class, which it uses to denote the restriction.
     * The default datatype is set to NonNegativeIntegerNumber
     *
     * @param onProperty          - Specifies the property for which the restriction is being set.
     * @param maxCardinalityValue - generally a numeric number.
     * @return
     * @throws InvalidValueException - if a value is not compatible with the nonNegativeIntegerNumber
     *                               datatype.
     */
    public MaxCardinalityRestriction addMaxCardinalityRestriction(
            RDFProperty onProperty, String maxCardinalityValue)
            throws InvalidValueException {
        String restId = getAutoGeneratedRestrictionName();
        DataType datatype =
                OntologyUtilities.getDataType(OntologyUtilities.NON_NEGATIVE_INTEGER);

        ontologyService.addClass(restId,
                OConstants.MAX_CARDINALITY_RESTRICTION);

        ontologyService.setOnPropertyValue(restId, onProperty
                .getOURI().toString());

        if (!datatype.isValidValue(maxCardinalityValue)) {
            throw new InvalidValueException(maxCardinalityValue
                    + " is not valid for datatype " + datatype.getXmlSchemaURIString());
        }

        ontologyService.setPropertyValue(restId,
                OConstants.MAX_CARDINALITY_RESTRICTION, maxCardinalityValue, datatype
                .getXmlSchemaURIString());

        MaxCardinalityRestriction mcr =
                (MaxCardinalityRestriction) Utils.createOClass(
                        this, ontologyService, restId, OConstants.MAX_CARDINALITY_RESTRICTION);
        fireOntologyResourceAdded(mcr);
        return mcr;
    }

    /**
     * Adds a new Cardinality Restriction to the ontology. It automatically
     * creates a randon anonymous class, which it uses to denote the restriction.
     * The default datatype is set to NonNegativeIntegerNumber
     *
     * @param onProperty       - Specifies the property for which the restriction is being set.
     * @param cardinalityValue - generally a numeric number.
     * @return
     * @throws InvalidValueException - if a value is not compatible with the nonNegativeIntegerNumber
     *                               datatype.
     */
    public CardinalityRestriction addCardinalityRestriction(
            RDFProperty onProperty, String cardinalityValue)
            throws InvalidValueException {
        String restId = getAutoGeneratedRestrictionName();
        DataType datatype =
                OntologyUtilities.getDataType(OntologyUtilities.NON_NEGATIVE_INTEGER);

        ontologyService.addClass(restId,
                OConstants.CARDINALITY_RESTRICTION);

        ontologyService.setOnPropertyValue(restId, onProperty
                .getOURI().toString());

        if (!datatype.isValidValue(cardinalityValue))
            throw new InvalidValueException(cardinalityValue
                    + " is not valid for datatype " + datatype.getXmlSchemaURIString());

        ontologyService.setPropertyValue(restId,
                OConstants.CARDINALITY_RESTRICTION, cardinalityValue, datatype
                .getXmlSchemaURIString());

        CardinalityRestriction cr =
                (CardinalityRestriction) Utils.createOClass(this,
                        ontologyService, restId, OConstants.CARDINALITY_RESTRICTION);

        fireOntologyResourceAdded(cr);
        return cr;
    }

    /**
     * Adds a new HasValue Restriction to the ontology. It automatically creates a
     * randon anonymous class, which it uses to denote the restriction.
     *
     * @param onProperty - Specifies the property for which the restriction is being set.
     * @param hasValue   - a resource or a literal used as a value for hasValue element of
     *                   the restriction.
     * @return
     */
    public HasValueRestriction addHasValueRestriction(RDFProperty onProperty,
                                                      OResource hasValue) {

        String restId = getAutoGeneratedRestrictionName();

        ontologyService.addClass(restId,
                OConstants.HAS_VALUE_RESTRICTION);

        ontologyService.setOnPropertyValue(restId, onProperty
                .getOURI().toString());

        String valueString =
                hasValue instanceof Literal
                        ? ((Literal) hasValue).getValue()
                        : ((OResource) hasValue).getONodeID().toString();
        ontologyService.setRestrictionValue(restId,
                OConstants.HAS_VALUE_RESTRICTION, valueString);

        HasValueRestriction hvr =
                (HasValueRestriction) Utils.createOClass(this,
                        ontologyService, restId, OConstants.HAS_VALUE_RESTRICTION);
        fireOntologyResourceAdded(hvr);
        return hvr;
    }

    /**
     * Adds a new AllValuesFrom Restriction to the ontology. It automatically
     * creates a randon anonymous class, which it uses to denote the restriction.
     *
     * @param onProperty - Specifies the property for which the restriction is being set.
     * @param hasValue   - a resource used as a value for hasValue element of the
     *                   restriction.
     * @return
     */
    public AllValuesFromRestriction addAllValuesFromRestriction(
            RDFProperty onProperty, OResource hasValue) {
        String restId = getAutoGeneratedRestrictionName();

        ontologyService.addClass(restId,
                OConstants.ALL_VALUES_FROM_RESTRICTION);

        ontologyService.setOnPropertyValue(restId, onProperty
                .getOURI().toString());

        ontologyService.setRestrictionValue(restId,
                OConstants.ALL_VALUES_FROM_RESTRICTION, hasValue.getONodeID().toString());

        AllValuesFromRestriction avfr =
                (AllValuesFromRestriction) Utils.createOClass(
                        this, ontologyService, restId, OConstants.ALL_VALUES_FROM_RESTRICTION);
        fireOntologyResourceAdded(avfr);
        return avfr;
    }

    public AllValuesFromRestriction addAllValuesFromRestriction(
            ObjectProperty onProperty, OClass hasValue) {
        String restId = getAutoGeneratedRestrictionName();

        ontologyService.addClass(restId,
                OConstants.ALL_VALUES_FROM_RESTRICTION);

        ontologyService.setOnPropertyValue(restId, onProperty
                .getOURI().toString());

        ontologyService.setRestrictionValue(restId,
                OConstants.ALL_VALUES_FROM_RESTRICTION, hasValue.getONodeID().toString());

        AllValuesFromRestriction avfr =
                (AllValuesFromRestriction) Utils.createOClass(
                        this, ontologyService, restId, OConstants.ALL_VALUES_FROM_RESTRICTION);
        fireOntologyResourceAdded(avfr);
        return avfr;
    }

    /**
     * Adds a new AllValuesFrom Restriction to the ontology. It automatically
     * creates a randon anonymous class, which it uses to denote the restriction.
     *
     * @param onProperty - Specifies the property for which the restriction is being set.
     * @param hasValue   - a resource used as a value for hasValue element of the
     *                   restriction.
     * @return
     */
    public SomeValuesFromRestriction addSomeValuesFromRestriction(
            RDFProperty onProperty, OResource hasValue) {
        String restId = getAutoGeneratedRestrictionName();

        ontologyService.addClass(restId,
                OConstants.SOME_VALUES_FROM_RESTRICTION);

        ontologyService.setOnPropertyValue(restId, onProperty
                .getOURI().toString());

        ontologyService.setRestrictionValue(restId,
                OConstants.SOME_VALUES_FROM_RESTRICTION, hasValue.getONodeID().toString());

        SomeValuesFromRestriction svfr =
                (SomeValuesFromRestriction) Utils.createOClass(
                        this, ontologyService, restId, OConstants.SOME_VALUES_FROM_RESTRICTION);
        fireOntologyResourceAdded(svfr);
        return svfr;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#setModified(boolean)
     */
    public void setModified(boolean isModified) {
        this.isModified = isModified;
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#isModified()
     */
    @Override
    public boolean isModified() {
        return this.isModified;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#addOntologyModificationListener(gate.creole
     * .ontology.OntologyModificationListener)
     */
    public synchronized void addOntologyModificationListener(
            OntologyModificationListener oml) {
        List<OntologyModificationListener> newListeners =
                new ArrayList<OntologyModificationListener>();
        if (this.modificationListeners != null) {
            newListeners.addAll(this.modificationListeners);
        }
        newListeners.add(oml);
        this.modificationListeners = newListeners;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gate.creole.ontology.Ontology#removeOntologyModificationListener(gate.creole
     * .ontology.OntologyModificationListener)
     */
    public synchronized void removeOntologyModificationListener(
            OntologyModificationListener oml) {
        if (this.modificationListeners == null
                || !this.modificationListeners.contains(oml)) {
            return;
        } else {
            List<OntologyModificationListener> newListeners =
                    new ArrayList<OntologyModificationListener>();
            for (OntologyModificationListener l : this.modificationListeners) {
                if (l != oml) {
                    newListeners.add(l);
                }
            }
            this.modificationListeners = newListeners;
        }
    }

    /**
     * A method to invoke when a resource's property value is changed
     *
     * @param resource
     * @param eventType
     */
    public void fireResourcePropertyValueChanged(OResource resource,
                                                 RDFProperty property, Object value, int eventType) {
        List<OntologyModificationListener> listeners = this.modificationListeners;
        if (listeners != null) {
            for (OntologyModificationListener l : listeners) {
                l.resourcePropertyValueChanged(this, resource, property, value,
                        eventType);
            }
        }
    }

    /**
     * A method to invoke when a resource's property value is changed
     *
     * @param resource
     * @param eventType
     */
    public void fireResourceRelationChanged(OResource resource1,
                                            OResource resource2, int eventType) {
        List<OntologyModificationListener> listeners = this.modificationListeners;
        if (listeners != null) {
            for (OntologyModificationListener l : listeners) {
                l.resourceRelationChanged(this, resource1, resource2, eventType);
            }
        }
    }

    public void fireOntologyReset() {
        List<OntologyModificationListener> listeners = this.modificationListeners;
        if (listeners != null) {
            for (OntologyModificationListener l : listeners) {
                l.ontologyReset(this);
            }
        }
    }

    /**
     * A Method to invoke an event for newly added ontology resource
     *
     * @param resource
     */
    public void fireOntologyResourceAdded(OResource resource) {
        List<OntologyModificationListener> listeners = this.modificationListeners;
        if (listeners != null) {
            for (OntologyModificationListener l : listeners) {
                l.resourceAdded(this, resource);
            }
        }
    }

    /**
     * A Method to invoke an event for a removed ontology resource
     *
     * @param resource
     */
    public void fireOntologyResourcesRemoved(String[] resources) {
        // we need to delete this resource from our maps
        //for(int i = 0; i < resources.length; i++) {
        //  removeOResourceFromMap(resources[i]);
        //}

        List<OntologyModificationListener> listeners = this.modificationListeners;
        if (listeners != null) {
            for (OntologyModificationListener l : listeners) {
                l.resourcesRemoved(this, resources);
            }
        }
    }

    public boolean transationStarted() {
        throw new UnsupportedOperationException("Not supported any more in this implementation");
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getSesameRepository()
     */
    public Object getSesameRepository() {
        throw new UnsupportedOperationException("Not supported any more in this implementation");
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getSesameRepositoryID()
     */
    public String getSesameRepositoryID() {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#getOResourceFromMap(java.lang.String)
     */
    public OResource getOResourceFromMap(String uri) {
        //Utils.warnDeprecation("getOResourceFromMap");
        // TODO: this tries to provide a replacement without the map for
        // backwards compatibility but this should really be dropped entirely!
        OURI ouri = null;
        try {
            ouri = this.createOURI(uri);
        } catch (Exception e) {
            ouri = this.createOURI(getDefaultNameSpace() + uri);
        }
        if (ouri != null) {
            List<OResource> toReturn = new ArrayList<OResource>();
            OClass c = getOClass(ouri);
            if (c != null) {
                return c;
            }
            OInstance i = getOInstance(ouri);
            if (i != null) {
                return i;
            }
            RDFProperty r = getProperty(ouri);
            if (r != null) {
                return r;
            }
        }
        return null;

        //throw new UnsupportedOperationException("getResourceFromMap not supported any more");
    }

    /*
     * (non-Javadoc)
     *
     * @see gate.creole.ontology.Ontology#addOResourceToMap(java.lang.String,
     * gate.creole.ontology.OResource)
     */
    public void addOResourceToMap(String uri, OResource resource) {
        throw new UnsupportedOperationException("addOResourceToMap not supported any more");
    }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#removeOResourceFromMap(java.lang.String)
   */

    public void removeOResourceFromMap(String uri) {
        throw new UnsupportedOperationException("rremoveOResourceFromMap not supported any more");
    }


    public void cleanup() {
    }


    /**
     * This method checks in its cache find out the URI for the given resource
     * name. However, doesn't guranttee that it will be able to return the URI. It
     * is also possible for two resources to have a same name but different name
     * spaces. This method returns a List containing all such URIs.
     *
     * @param resourceName
     * @return
     */
    public OResource getOResourceByName(String resourceName) {
        //System.err.println("getOResourceByName called");
        //new GateOntologyException("NO USE").printStackTrace();
        List<OResource> resources = getOResourcesByName(resourceName);
        if (resources != null) {
            if (resources.size() > 1) {
                System.err
                        .print("Warning : there are more than one resources matching with the name "
                                + resourceName);
            }
            if (resources.size() == 1) {
                return resources.get(0);
            }
        }
        return null;
    }

    /**
     * This method checks in its cache to find out the OResources for the given
     * resource name. It is possible for two resources to have a same name but
     * different name spaces. This method returns a list of resources with the
     * common name. Please note that deleting an instance from this list (e.g.
     * list.remove(int/Object)) does not delete the resource from an ontology. One
     * must use appropriate method from the Ontology interface to delete such
     * resources.
     *
     * @param resourceName
     * @return
     */
    // TODO: replace by actual lookup!
    public List<OResource> getOResourcesByName(String resourceName) {
        //Utils.warnDeprecation("getOResourcesByName");
        //System.err.println("getOResourcesByName called");
        //new GateOntologyException("NO USE").printStackTrace();
        List<OResource> toReturn = new ArrayList<OResource>();
        Set<OClass> classes = getOClassesByName(resourceName);
        if (classes != null) {
            toReturn.addAll(classes);
        }
        classes = null;
        Set<OInstance> instances = getOInstancesByName(resourceName);
        if (instances != null) {
            toReturn.addAll(instances);
        }
        instances = null;
        Set<RDFProperty> properties = getPropertiesByName(resourceName);
        if (properties != null) {
            toReturn.addAll(properties);
        }
        properties = null;
        try {
            AnnotationProperty aproperty = getAnnotationProperty(new OURIImpl(resourceName));
            if (aproperty != null) {
                toReturn.add(aproperty);
            }
            aproperty = null;
        } catch (Exception e) {
            //here we ignore the exception if the name is not a uri
        }
        //List<OResource> resources = resourceNamesToOResourcesMap.get(resourceName);
        //if(resources == null) { return null; }
        //List<OResource> toReturn = new ArrayList<OResource>();
        //toReturn.addAll(resources);
        return toReturn;
    }

    public Set<OClass> getOClassesByName(String resourceName) {
        // TODO: normalize/check resourceName: quotes, spaces etc must be escaped!
        return ontologyService.getClassesByName(encodeResourceName(resourceName));
    }

    public Set<OInstance> getOInstancesByName(String resourceName) {
        // TODO: normalize/check resourceName: quotes, spaces etc must be escaped!
        return ontologyService.getInstancesByName(encodeResourceName(resourceName));
    }

    public Set<RDFProperty> getPropertiesByName(String resourceName) {
        // TODO: normalize/check resourceName: quotes, spaces etc must be escaped!
        return ontologyService.getPropertiesByName(encodeResourceName(resourceName));
    }

    protected String encodeResourceName(String resourceName) {
        // TODO: replace blanks by %20, quotes, <, >, #(?), ampersand?
        return resourceName;
    }

    /**
     * This method returns a list of OResources from the ontology. Please note
     * that deleting an instance from this list (e.g. list.remove(int/Object))
     * does not delete the resource from an ontology. One must use appropriate
     * method from the Ontology interface to delete such resources.
     *
     * @return
     */
    public List<OResource> getAllResources() {
        //Utils.warnDeprecation("getAllResources");
        // TODO: would love to make this Unsupported but at the moment
        // it is still used by the ontology editor.
        Set<OClass> cs = getOClasses(false);
        Set<OInstance> is = getOInstances();
        Set<RDFProperty> rs = getPropertyDefinitions();
        List<OResource> toReturn = new ArrayList<OResource>();
        for (OClass c : cs) {
            toReturn.add(c);
        }
        for (OInstance i : is) {
            toReturn.add(i);
        }
        for (RDFProperty r : rs) {
            toReturn.add(r);
        }
        //Iterator<String> keys = resourceNamesToOResourcesMap.keySet().iterator();
        //while(keys.hasNext()) {
        //  toReturn.addAll(resourceNamesToOResourcesMap.get(keys.next()));
        //}
        return toReturn;
        //throw new UnsupportedOperationException("getAllResources not supported any more");
    }

    /**
     * Tries to save the ontology at the provided File
     */
    public void store(File newOntology) throws IOException {
        throw new UnsupportedOperationException("Method not supported in this implementation");
    }

    /**
     * This method given a property (either an annotation or datatype), retrieves
     * a list of resources which have the provided literal set as a value.
     *
     * @param aProperty
     * @param aValue
     * @return
     */
    public List<OResource> getOResourcesWith(RDFProperty aProperty, Literal aValue) {
        List<OResource> toReturn = new ArrayList<OResource>();

        int propType = 1;

        if (aProperty instanceof AnnotationProperty) {
            propType = 1;
        } else if (aProperty instanceof DatatypeProperty) {
            propType = 2;
        } else {
            return toReturn;
        }

        // here the first thing is to obtain all the resources
        List<OResource> resources = getAllResources();

        // and on each resource we need to check if it has the above
        // property set on it
        for (OResource aResource : resources) {
            switch (propType) {
                case 1:
                    if (aResource.hasAnnotationPropertyWithValue(
                            (AnnotationProperty) aProperty, aValue)) {
                        toReturn.add(aResource);
                    }
                    break;
                case 2:
                    if (aResource instanceof OInstance
                            && ((OInstance) aResource).hasDatatypePropertyWithValue(
                            (DatatypeProperty) aProperty, aValue)) {
                        toReturn.add(aResource);
                    }
                    break;
            }
        }
        return toReturn;
    }

    /**
     * This method given a property (either object, transitive, symmetric or rdf),
     * retrieves a list of resources which have the provided resource set as a
     * value.
     *
     * @param aProperty
     * @param aValue
     * @return
     */
    public List<OResource> getOResourcesWith(RDFProperty aProperty,
                                             OResource aValue) {
        List<OResource> toReturn = new ArrayList<OResource>();

        int propType = 1;

        if (aProperty instanceof ObjectProperty) {
            propType = 1;
        } else if (!(aProperty instanceof DatatypeProperty)) {
            propType = 2;
        } else {
            return toReturn;
        }

        // here the first thing is to obtain all the resources
        List<OResource> resources = getAllResources();

        // and on each resource we need to check if it has the above
        // property set on it
        for (OResource aResource : resources) {
            switch (propType) {
                case 1:
                    if (aResource instanceof OInstance
                            && aValue instanceof OInstance
                            && ((OInstance) aResource).hasObjectPropertyWithValue(
                            (ObjectProperty) aProperty, (OInstance) aValue)) {
                        toReturn.add(aResource);
                    }
                    break;
                case 2:
                    if (aResource instanceof OInstance
                            && ((OInstance) aResource)
                            .hasRDFPropertyWithValue(aProperty, aValue)) {
                        toReturn.add(aResource);
                    }
                    break;
            }
        }
        return toReturn;
    }

    // NOTE: we provide an implementation for this in the sesame implementation
    // package that overwrites this method
    @Deprecated
    public String executeQuery(String serqlQuery) {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    public void resolveImports(Map<String, String> importMappings) {
        boolean haveUnresolvedImports = true;
        // make sure we have a map, even if empty
        if (importMappings == null) {
            importMappings = new HashMap<String, String>();
        }
        // get the global substitution patterns from the map: everything that
        // starts with "*" will be replaced in any import URI that starts with
        // this.
        List<String> patterns = new ArrayList<String>();
        for (String from : importMappings.keySet()) {
            if (from.startsWith("*")) {
                //System.out.println("Adding pattern: " + from);
                patterns.add(from.substring(1));
            }
        }
        // now sort by decreasing length of the replacement patter
        Collections.sort(patterns,
                new Comparator<String>() {

                    public int compare(String s1, String s2) {
                        if (s1.length() == s2.length()) {
                            return s1.compareTo(s2);
                        } else if (s1.length() < s2.length()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
        // Go through all the import URIs and if we have not seen the URI yet,
        // load or ignore according to the map. Repeat until no more unseen
        // import URIs are left
        while (haveUnresolvedImports) {
            // get all the imports mentioned in the ontology repository
            // this includes the ones we already have processed
            Set<OURI> importURIs = getImportURIs();
            // lets see if we find anything new to load in this iteration
            int loaded = 0;
            OUTER:
            for (OURI anURI : importURIs) {
                String URIString = anURI.toString();
                // if we find an URI that we have not processed yet
                if (!this.knownImportURIs.contains(URIString)) {
                    // lets see if we have a specific mapping for it
                    if (importMappings.containsKey(URIString)) {
                        String mapped = importMappings.get(URIString);
                        // if the mapping contains something, try to interpret that
                        // as an URL and load from there, otherwise do not actually
                        // import anything
                        if (mapped != null &&
                                // !mapped.isEmpty()  - does not work in Java 1.5
                                (mapped.length() != 0)) {
                            URL location;
                            try {
                                location = new URL(mapped);
                            } catch (MalformedURLException ex) {
                                throw new GateOntologyException(
                                        "Problem creating an URL from the mapping " + mapped +
                                                " for ontology import " + anURI, ex);

                            }
                            InputStream is;
                            try {
                                is = location.openStream();
                            } catch (IOException ex) {
                                throw new GateOntologyException(
                                        "Problem opening the URL " + location + " from the mapping " + mapped +
                                                " for ontology import " + anURI, ex);

                            }
                            try {
                                System.out.println("Loading import for " + URIString + " from " + location);
                                readOntologyData(is, anURI.toString(), OntologyFormat.RDFXML, true);
                            } catch (Exception ex) {
                                throw new GateOntologyException(
                                        "Problem loading ontology from URL " + location, ex);
                            }
                            try {
                                is.close();
                            } catch (IOException ex) {
                                throw new GateOntologyException("Problem closing the stream for URL " +
                                        location + " from the mapping " + mapped +
                                        " for ontology import " + anURI, ex);
                            }
                            loaded++;
                        } else {
                            System.out.println("Ignoring import for " + URIString);
                        }
                        knownImportURIs.add(anURI.toString());
                        continue OUTER;
                    }

                    // we get here only if we did not find a specific mapping
                    // lets see if we have a pattern that matches
                    for (String pattern : patterns) {
                        if (URIString.startsWith(pattern)) {
                            //System.out.println("Processing URI: " + URIString);
                            //System.out.println("Found matching pattern: " + pattern);
                            // substitute the pattern in the uri with the mapping, or
                            // ignore the URI if the mapping is empty
                            String subst = importMappings.get("*" + pattern);
                            //System.out.println("Found replacement " + subst);
                            if (subst.length() == 0) {
                                knownImportURIs.add(anURI.toString());
                                continue OUTER;
                            } else {
                                // substitute the part of the URI that matches with
                                // the replacement part
                                knownImportURIs.add(anURI.toString());
                                URIString = URIString.replaceFirst("\\Q" + pattern + "\\E", subst);
                                //System.out.println("URI String after replacement: " + URIString);
                                anURI = createOURI(URIString);
                                //System.out.println("Replaced, new URI is: " + anURI.toString());
                            }
                        }
                    }
                    // no process either the original URI or the one we got after
                    // making the substitution
                    URL location;
                    try {
                        location = new URL(anURI.toString());
                    } catch (MalformedURLException ex) {
                        throw new GateOntologyException(
                                "Problem creating an URL from the ontology import URI " + anURI, ex);

                    }
                    InputStream is;
                    try {
                        is = location.openStream();
                    } catch (IOException ex) {
                        throw new GateOntologyException(
                                "Problem opening the URL " + location +
                                        " from the ontology import URI " + anURI, ex);

                    }
                    try {
                        System.out.println("Loading import for " + URIString + " from " + location);
                        readOntologyData(is, anURI.toString(), OntologyFormat.RDFXML, true);
                    } catch (Exception ex) {
                        throw new GateOntologyException(
                                "Problem loading ontology from URL " + location, ex);
                    }
                    try {
                        is.close();
                    } catch (IOException ex) {
                        throw new GateOntologyException(
                                "Problem closing the stream for URL " +
                                        location +
                                        " for ontology import URI " + anURI, ex);
                    }


                    loaded++;
                    // remember that we have already processed this URI
                    knownImportURIs.add(anURI.toString());
                }
            }
            // if nothing was loaded, there will be no more import URIs to process
            if (loaded == 0) {
                haveUnresolvedImports = false;
            }
        }
    }

    public Set<OURI> getImportURIs() {
        Set<OURI> set = new HashSet<OURI>();
        Set<String> ss = ontologyService.getImportURIStrings();
        for (String s : ss) {
            OURI theOURI = createOURI(s);
            set.add(theOURI);
            //System.out.println("Converted import URI string "+s+" to OURI "+theOURI.toString());
        }
        return set;
    }

    // TODO: maybe we implement this at a later time if it is needed and
    // useful, at the moment, only the first URL loaded is returned by getURL
    //public List<URL> getURLs(boolean includeImports) {
    //  throw new UnsupportedOperationException("Needs to get implemented in AbstractOntologyImpl.getURLs");
    //}


    public void startTransaction() {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    public void commitTransaction() {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    /**
     * Return an URL that is related to the source of the ontology.
     * This needs to be implemented by all implementing LRs so the factory
     * can determine a name based on the source of the ontology. If the ontology
     * is empty or the source otherwise unknown, null can be returned.
     * This is not part of the public ontology API but used internally by
     * the GATE Factory.
     *
     * @return a URL of the source of the ontology
     */
    public abstract java.net.URL getSourceURL();


    protected File pluginDir = null;

    public File getPluginDir() {
        if (pluginDir == null) {
            ResourceData myResourceData =
                    Gate.getCreoleRegister().get(OWLIMOntology.class.getName());
            java.net.URL creoleXml = myResourceData.getXmlFileUrl();
            try {
                pluginDir = new File(creoleXml.toURI()).getParentFile();
            } catch (URISyntaxException ex) {
                throw new GateRuntimeException("Problem finding plugin location", ex);
            }

        }
        return pluginDir;
    }


    public Map<String, String> loadImportMappingsFile(java.net.URL mappingsURL) {
        Map<String, String> mappings = new HashMap<String, String>();
        try {
            // open file and read line by line, each line should have
            // two tab-separated strings, which will be trimmed and added
            // to the map
            InputStream is = mappingsURL.openStream();
            DataInputStream dis = new DataInputStream(is);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (strLine.matches("^\\s*#.*") ||
                        strLine.matches("^\\s*$")) {
                    continue;
                }
                String[] s = strLine.split("[\\t\\s]+", 2);
                if (s.length == 2) {
                    String theMapping = s[1];
                    if (!theMapping.startsWith("http:") &&
                            !theMapping.startsWith("file:") &&
                            !theMapping.startsWith("/")) {
                        String mappingsDir = mappingsURL.toString();
                        // remove everything after the last "/" and append the relative name
                        mappingsDir = mappingsDir.substring(0, mappingsDir.lastIndexOf("/"));
                        theMapping = mappingsDir + "/" + theMapping;
                    } else if (theMapping.startsWith("/")) {
                        String mappingsURIString = "file://" + theMapping;
                    }
                    mappings.put(s[0].trim(), theMapping);
                } else {
                    mappings.put(strLine.trim(), "");
                }
            }
            is.close();
        } catch (IOException ex) {
            throw new GateRuntimeException("Error reading mappings file ", ex);
        }
        return mappings;
    }

    // ************************* helper methods and utilities

    // add the ontology URI of an ontology just loaded to the known imports
    // this should get called by any method that LOADS an ontology and
    // it should find exactly one new ontology URI
    // If more than one URI has been found they will all get added to the known
    // import URIs and to the loaded ontology URIs for now ...
    // the method returns the new URIs actually found.
    protected Set<OURI> addOntologyURIs() {
        Set<OURI> ouris = ontologyService.getOntologyURIs();
        Set<OURI> newuris = new HashSet<OURI>();
        for (OURI u : ouris) {
            if (!loadedOntologyURIs.contains(u)) {
                loadedOntologyURIs.add(u);
                knownImportURIs.add(u.toString());
                newuris.add(u);
            }
        }
        return newuris;
    }

    public List<OURI> getOntologyURIs() {
        return loadedOntologyURIs;
    }

    public AnonymousClass addAnonymousClass() {
        throw new UnsupportedOperationException("Still to be implemented!");
    }

    public abstract OBNodeID createOBNodeID(String id);

    public List<Literal> getOntologyAnnotationValues(AnnotationProperty ann) {
        Set<OURI> ontouris = ontologyService.getOntologyURIs();
        if (ontouris.size() != 1) {
            throw new GateOntologyException(
                    "Can only get ontology annotation values if there is a single ontology uri but there are " +
                            ontouris.size() + ": " + ontouris);
        }
        OURI ouri = ontouris.iterator().next();
        PropertyValue[] propValues = ontologyService.getAnnotationPropertyValues(
                ouri.toString(), ann
                .getOURI().toString());
        List<Literal> list = new ArrayList<Literal>();
        for (int i = 0; i < propValues.length; i++) {
            Literal l = new Literal(propValues[i].getValue(), OntologyUtilities
                    .getLocale(propValues[i].getDatatype()));
            list.add(l);
        }
        return list;
    }

    public void setOntologyAnnotation(AnnotationProperty ann, Literal val) {
        Set<OURI> ontouris = ontologyService.getOntologyURIs();
        if (ontouris.size() != 1) {
            throw new GateOntologyException(
                    "Can only set ontology annotation values if there is a single ontology uri but there are " +
                            ontouris.size() + ": " + ontouris);
        }
        OURI ouri = ontouris.iterator().next();
        RDFProperty res = getProperty(ann.getOURI());
        if (res == null) {
            Utils
                    .error(ann.getOURI().toTurtle()
                            + " does not exist");
            return;
        }

        if (!(res instanceof AnnotationProperty)) {
            Utils.error(ann.getOURI().toTurtle()
                    + " is not an annotation property");
            return;
        }

        ontologyService.addAnnotationPropertyValue(ouri.toString(),
                ann.getOURI().toString(), val.getValue(),
                val.getLanguage() != null
                        ? val.getLanguage().getLanguage()
                        : null);
    }

    public void setOntologyURI(OURI theURI) {
        // TODO: check if already have one or more URIs, if yes, remove them(?)
        Set<OURI> uris = ontologyService.getOntologyURIs();
        if (uris.size() == 0) {
            ontologyService.setOntologyURI(theURI);
        } else if (uris.size() == 1) {
            OURI existing = uris.iterator().next();
            if (existing.equals(theURI)) {
                throw new GateOntologyException("Ontology URI already set to " + existing.toTurtle());
            }
        } else {
            throw new GateOntologyException("Ontology has already several URIS: " + uris);
        }
    }

    public OURI getOntologyURI() {
        Set<OURI> uris = ontologyService.getOntologyURIs();
        if (uris.size() == 0) {
            return null;
        } else if (uris.size() == 1) {
            return uris.iterator().next();
        } else {
            throw new GateOntologyException("More than one ontology URI found: " + uris);
        }
    }


    public AnnotationValue[] getAnnotationValues(String cl,
                                                 String annotationProperty) {
        return ontologyService.getAnnotationPropertyValues(cl, annotationProperty);

    }


    public AnnotationValue[] getAnnotationValues(String annotatedResource) {
        return ((OWLOntologyService) ontologyService).getAnnotationValues(annotatedResource);
    }


    public OClass addOClass(OURI aURI, String superclass) {
        return addOClass(aURI, superclass, OConstants.OWL_CLASS);

    }


    public ObjectProperty getObjectPropery(String entity) {
        Property[] res = ((OWLOntologyServiceImpl) ontologyService).getObjectProperties(entity);
        if (res.length > 0) {
            return (ObjectProperty) Utils.createOProperty(this, ontologyService,
                    res[0].getUri(), OConstants.OBJECT_PROPERTY);
        }
        return null;
    }


    public void writeInstanceData(String filename) {
        if (Settings.WRITE_QUADS) {
            if (Settings.PERDOCUMENT) {
                ((OWLOntologyService) ontologyService).writeQuads(filename + "." + Settings.FILENAME_QUADS);
            } else {
                ((OWLOntologyService) ontologyService).writeQuads(Settings.FILENAME_QUADS);
            }
        } else {
            ((OWLOntologyService) ontologyService).writeInstanceData(filename);
        }

    }


    public void addInstanceRelation(OInstance i1, OInstance i2, URI uri) {
        ((OWLOntologyService) ontologyService).addInstanceRelation(i1, i2, uri);

    }


    public void addQuad(String[] quad) {
        ((OWLOntologyService) ontologyService).addQuad(quad);


    }


    public void writeQuads(String filename) {
        ((OWLOntologyService) ontologyService).writeQuads(filename);


    }


    public Property[] getInverses(String aPropertyURI) {
        return ((OWLOntologyService) ontologyService).getInverseProperties(aPropertyURI);
    }

}
