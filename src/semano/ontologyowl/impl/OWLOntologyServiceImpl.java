package semano.ontologyowl.impl;

import gate.creole.ontology.*;
import gate.creole.ontology.OConstants.Closure;
import gate.creole.ontology.OConstants.OntologyFormat;
import gate.util.ClosableIterator;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import semano.ontologyowl.*;
import semano.ontologyowl.Reasoning.Reasoner;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * OWLAPI based Implementation of the  Ontology Service.
 *
 * @author Nadejda Nikitina
 */
public class OWLOntologyServiceImpl implements OWLOntologyService {


    private Logger logger;

    public final AbstractOWLOntology ontology;
    private OntologyParser op = new OntologyParser();


    /**
     * Constructor
     */
    public OWLOntologyServiceImpl(AbstractOWLOntology o) {
        super();
        ontology = o;
        logger = Logger.getLogger(this.getClass().getName());
    }

    // ***************************************************************************
    // *** METHODS RELATED TO THE ONTOLOGY AS A WHOLE
    // ***************************************************************************

    // ********** CREATION, INITIALIZATION, SHUTDOWN

    /**
     * Create an unmanaged repository in the given data directory from the
     * given configuration data string.
     *
     * @param dataDir
     * @param configData
     */
    public void createRepository(File dataDir, String configData) {
        init();
    }

    /**
     * Create a managed repository at the given repository location, which could
     * either be a directory or a sesame server, with the given repository ID
     * from theconfiguration file at the given URL. The configuration fiel should
     * be a configuration file template with the template variable "id" which
     * will be replaced with the repository ID.
     *
     * @param repoLoc
     * @param repositoryID
     */
    public void createManagedRepository(URL repoLoc, String repositoryID) {

        init();
    }

    /**
     * Conect to the repository with the given repository ID
     * at the given repository URL location, which could
     * be either a directory or a sesame server.
     *
     * @param repositoryURL
     * @param repositoryID
     */
    void connectToRepository(URL repositoryURL, String repositoryID) {

        logger.debug("Service: calling connectToRepository for id " + repositoryID);

        init();
    }

    /**
     * Initialize the ontology service. This methods prepares the ontology service
     * for use. It must be called right after the repository has been created
     * or has been connected to.
     */
    private void init() {

    }

    /**
     * Shutdown the repository. This must be done before the the object of this
     * class is destroyed!
     */
    public void shutdown() {
    }

    // *************** IMPORT / EXPORT *******************************************

    public void readOntologyData(File selectedFile, String baseURI,
                                 OntologyFormat ontologyFormat, boolean asImport) throws OWLOntologyCreationException, FileNotFoundException {
        op.loadOntology(selectedFile.getAbsolutePath());

    }

    public String readOntologyData(Set<String> filenames, String dirName) throws OWLOntologyCreationException, FileNotFoundException {
        return op.loadOntologies(filenames, dirName);

    }


    public void readOntologyData(InputStream is, String baseURI,
                                 OntologyFormat ontologyFormat, boolean asImport) {
        throw new GateOntologyException("method readOntologyData(InputStream is,...) not implemented");

    }

    public void readOntologyData(Reader ir, String baseURI,
                                 OntologyFormat ontologyFormat, boolean asImport) {
        throw new GateOntologyException("method readOntologyData(Reader ir,...) not implemented");
    }

    public void writeOntologyData(Writer out, OntologyFormat ontologyFormat,
                                  boolean includeImports) {

        throw new GateOntologyException("Could not write ontology data");

    }

    public void writeOntologyData(OutputStream out, OntologyFormat ontologyFormat,
                                  boolean includeImports) {

        throw new GateOntologyException("Could not write ontology data");

    }

    public void loadSystemImport(File selectedFile,
                                 String baseURI, OntologyFormat ontologyFormat) {
//    op.loadOntology(selectedFile.getAbsolutePath());
    }


    // *************** OTHER METHODS RELATED TO THE ONTOLOGY AS A WHOLE **********


    /**
     * The method removes all data from the ontology, including imports and
     * the system imports.
     */
    public void cleanOntology() throws GateOntologyException {
        op = new OntologyParser();
    }

    /**
     * From all the data and imports so far loaded, gather the set of
     * all ontology import URIs.
     *
     * @return
     */
    public Set<String> getImportURIStrings() {
        Set<String> uris = op.getImportURIS();
        return uris;
    }

    public Set<OURI> getOntologyURIs() {
        // apparently, this can return several ontology URIs, of which only
        // the one that is not object of an owl:priorVersion property is the
        // the one we want?

        // TODO: this just checks if the URI found is equal to one of the
        // import uri strings as present in the ontology, but does not check
        // against the actual import URI as it will be created from those
        // improt URI strings by replacing relative URI references.
        // Not sure how to really deal with this for now .... JP
        Set<OURI> theURIs =
                new LinkedHashSet<OURI>();
        Set<String> uris = op.getLoadedOntologyUris();
        for (String uri : uris) {
            OURI u = new OURIImpl(uri);
            theURIs.add(u);
        }
        return theURIs;
    }

    /**
     * The method allows adding version information to the repository.
     *
     * @param versionInfo
     */
    public void setVersion(String versionInfo) {
//    addUULStatement(this.ontologyUrl, OWL.VERSIONINFO.toString(), versionInfo, null);
    }

    /**
     * The method returns the version information of the repository.
     *
     * @return
     */
    public String getVersion() throws GateOntologyException {
//    try {
//      RepositoryResult<Statement> iter =
//          repositoryConnection.getStatements(getResource(this.ontologyUrl),
//          makeSesameURI(OWL.VERSIONINFO.toString()), null, true);
//      while (iter.hasNext()) {
//        Statement stmt = iter.next();
//        return stmt.getObject().toString();
//      }
//    } catch (Exception e) {
//      throw new GateOntologyException("Problem getting the ontology version: ", e);
//    }
        return null;
    }


    public void setOntologyURI(OURI theURI) {
//    addUUUStatement(theURI.toString(), RDF.TYPE.toString(), OWL.ONTOLOGY.toString());
    }


    // ***************************************************************************
    // **** CLASS RELATED METHODS
    // ***************************************************************************

    /**
     * The method allows adding a class to repository.
     *
     * @param classURI
     */
    public void addClass(String classURI, byte classType) {
        op.addClass(classURI);
//    switch (classType) {
//      case OConstants.OWL_CLASS:
//        addUUUStatement(classURI, RDF.TYPE.toString(), OWL.CLASS.toString());
//        return;
//      default:
//        addUUUStatement(classURI, RDF.TYPE.toString(), OWL.RESTRICTION.toString());
//        return;
//    }
    }


    public void startTransaction(String repositoryID) {
    }

    public void endTransaction(String repositoryID) throws GateOntologyException {
    }


    private Property[] listToPropertyArray(List<Property> list) {
        if (list == null) {
            return null;
        }
        ArrayList<Property> subList = new ArrayList<Property>();
        for (int i = 0; i < list.size(); i++) {
            if (hasSystemNameSpace(list.get(i).getUri())) {
                continue;
            }
            subList.add(list.get(i));
        }
        Property[] props = new Property[subList.size()];
        for (int i = 0; i < subList.size(); i++) {
            props[i] = subList.get(i);
        }
        return props;
    }

    private PropertyValue[] listToPropertyValueArray(List<PropertyValue> subList) {
        if (subList == null) {
            return null;
        }
        PropertyValue[] props = new PropertyValue[subList.size()];
        for (int i = 0; i < subList.size(); i++) {
            props[i] = subList.get(i);
        }
        return props;
    }

    private ResourceInfo[] listToResourceInfoArray(List<String> list) {
        if (list == null) {
            return null;
        }
        ArrayList<ResourceInfo> subList = new ArrayList<ResourceInfo>();
        for (int i = 0; i < list.size(); i++) {
            String resourceURI = list.get(i);
            if (hasSystemNameSpace(resourceURI)) {
                continue;
            }
            byte classType = getClassType(resourceURI);
            if (classType == OConstants.ANNONYMOUS_CLASS) {
                continue;
            }
            subList.add(new ResourceInfo(list.get(i).toString(), classType));
        }

        ResourceInfo[] strings = new ResourceInfo[subList.size()];
        for (int i = 0; i < subList.size(); i++) {
            strings[i] = subList.get(i);
        }
        return strings;
    }


    // TODO: get rid of this and use ontology objects directly!
    private Property createPropertyObject(String uri)
            throws GateOntologyException {
        byte type = OConstants.ANNOTATION_PROPERTY;
        if (isAnnotationProperty(uri)) {
            type = OConstants.ANNOTATION_PROPERTY;
        } else if (isObjectProperty(uri)) {
            type = OConstants.OBJECT_PROPERTY;
        } else if (isDatatypeProperty(uri)) {
            type = OConstants.DATATYPE_PROPERTY;
        } else if (isTransitiveProperty(uri)) {
            type = OConstants.TRANSITIVE_PROPERTY;
        } else if (isSymmetricProperty(uri)) {
            type = OConstants.SYMMETRIC_PROPERTY;
        } else if (isRDFProperty(uri)) {
            type = OConstants.RDF_PROPERTY;
        } else {
            return null;
        }
        return new Property(type, uri);
    }


    private byte getPropertyType(String aPropertyURI)
            throws GateOntologyException {
        if (isDatatypeProperty(aPropertyURI)) {
            return OConstants.DATATYPE_PROPERTY;
        } else if (isTransitiveProperty(aPropertyURI)) {
            return OConstants.TRANSITIVE_PROPERTY;
        } else if (isSymmetricProperty(aPropertyURI)) {
            return OConstants.SYMMETRIC_PROPERTY;
        } else if (isObjectProperty(aPropertyURI)) {
            return OConstants.OBJECT_PROPERTY;
        } else if (isAnnotationProperty(aPropertyURI)) {
            return OConstants.ANNOTATION_PROPERTY;
        } else {
            return OConstants.RDF_PROPERTY;
        }
    }


    // ***************************************************************************
    // *** UTILITY FUNCTIONS
    // ***************************************************************************


    // TODO: is returnSystemStatements still relevant?
    // if yes, check how often and where actually used
    // This should probably become part of the query anyways.
    // Try to get rid and move entirely to UtilConvert
    private boolean hasSystemNameSpace(String uri) {
        if (returnSystemStatements) {
            return false;
        }
        Boolean val = new Boolean(Utils.hasSystemNameSpace(uri));
        return val.booleanValue();
    }


    // ***************************************************************************
    // **** STUFF TO GET RID OF EVENTUALLY
    // ***************************************************************************


    /**
     * Debug parameter, if set to true, shows various messages when different
     * methods are invoked
     */
    private boolean debug = true;

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean getDebug() {
        return debug;
    }

    // TODO: STUFF TO GET RID OF
    // TODO: get rid of this or make semantics available to API?
    // If it is just internal, make this local to whatever method and
    // context where it is relevant
    private boolean returnSystemStatements = false;


    public void addAnnotationProperty(String aPropertyURI)
            throws GateOntologyException {
        op.addAnnotationProperty(aPropertyURI);

    }

    public void addAnnotationPropertyValue(String theResourceURI,
                                           String theAnnotationPropertyURI, String value, String language)
            throws GateOntologyException {
        op.addAnnotationPropertyValue(theResourceURI, theAnnotationPropertyURI, value, language);

    }

    public void addAnnotationPropertyValue(AnnotationValue av)
            throws GateOntologyException {
        op.addAnnotationPropertyValue(av);

    }

    public void addDataTypeProperty(String aPropertyURI,
                                    String[] domainClassesURIs, String dataTypeURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void addDatatypePropertyValue(String anInstanceURI,
                                         String aDatatypePropertyURI, String datatypeURI, String value)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void addIndividual(String superClassURI, String individualURI)
            throws GateOntologyException {
        op.addIndividual(superClassURI, individualURI);

    }

    public void addObjectProperty(String aPropertyURI,
                                  String[] domainClassesURIs, String[] rangeClassesTypes)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void addObjectProperty(String aPropertyURI, String propertyFromTheSameOntologySet,
                                  String[] domainClassesURIs, String[] rangeClassesTypes)
            throws GateOntologyException {
        op.addProperty(aPropertyURI, propertyFromTheSameOntologySet, domainClassesURIs, rangeClassesTypes);
        // TODO Auto-generated method stub

    }

    public void addObjectPropertyValue(String sourceInstanceURI,
                                       String anObjectPropertyURI, String theValueInstanceURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void addRDFProperty(String aPropertyURI, String[] domainClassesURIs,
                               String[] rangeClassesTypes) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void addRDFPropertyValue(String anInstanceURI,
                                    String anRDFPropertyURI, String aResourceURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void addStatement(String subjectURI, String predicateURI,
                             String objectURI) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void addStatement(String subject, String predicate, String object,
                             String datatype) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void addSubClass(String superClassURI, String subClassURI)
            throws GateOntologyException {
        op.addSubclassAxiom(superClassURI, subClassURI);
    }

    public void addSubProperty(String superPropertyURI, String subPropertyURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void addSuperClass(String superClassURI, String subClassURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void addSuperProperty(String superPropertyURI, String subPropertyURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void addSymmetricProperty(String aPropertyURI,
                                     String[] domainAndRangeClassesURIs) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void addTransitiveProperty(String aPropertyURI,
                                      String[] domainClassesURIs, String[] rangeClassesTypes)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }


    /////////////////////////////////////////////////////////

    public Property[] getAnnotationProperties(String theResourceURI)
            throws GateOntologyException {
        return op.getAnnotationProperties(theResourceURI).toArray(new Property[]{});
    }

    public Property[] getAnnotationProperties() throws GateOntologyException {
        Set<Property> props = op.getAnnotationProperties();
        return props.toArray(new Property[]{});
    }

    public String getAnnotationPropertyValue(String theResourceURI,
                                             String theAnnotationPropertyURI, String language)
            throws GateOntologyException {
        return op.getAnnotationPropertyValue(theResourceURI, theAnnotationPropertyURI, language);
    }

    public AnnotationValue[] getAnnotationPropertyValues(String theResourceURI,
                                                         String theAnnotationPropertyURI) throws GateOntologyException {
        return op.getAnnotationPropertyValues(theResourceURI, theAnnotationPropertyURI);
    }

    public byte getClassType(String restrictionURI) throws GateOntologyException {
        return 0;
    }

    public Set<OClass> getClasses(boolean top) throws GateOntologyException {
        Set<OClass> result = new HashSet<OClass>();
        Collection<ONodeID> concepts = new HashSet<ONodeID>();
        if (top) {
            concepts = op.getTopConcepts();
        } else {
            concepts = op.getClasses();
        }
        for (ONodeID clas : concepts) {
            if (clas != null) {
                OClass oclass = new OClassImpl(clas, this.ontology, this);
                result.add(oclass);
            }
        }
        return result;
    }

    public Set<OClass> getClassesByName(String name) {
        Set<ONodeID> classes = op.getClasses(name);
        Set<OClass> result = packClasses(classes);
        return result;
    }

    /**
     * @param classes
     * @return
     */
    private Set<OClass> packClasses(Set<ONodeID> classes) {
        Set<OClass> result = new HashSet<OClass>();
        for (ONodeID clas : classes) {
            if (clas != null) {
                OClass oclass = new OClassImpl(clas, this.ontology, this);
                result.add(oclass);
            }
        }
        return result;
    }

    public ClosableIterator<OClass> getClassesIterator(boolean top)
            throws GateOntologyException {
        Set<ONodeID> classes = new HashSet<ONodeID>();
        if (top) {
            classes.addAll(op.getTopConcepts());
        } else {
            classes.addAll(op.getClasses());
        }
        ClosableIterator<OClass> ii = new ClosableIteratorImpl<OClass>(packClasses(classes).iterator());
        return ii;
    }

    public ResourceInfo[] getClassesOfIndividual(String individualURI,
                                                 Closure direct) throws GateOntologyException {
        return op.getClassesOfIndividual(individualURI, direct);
    }

    public String getDatatype(String theDatatypePropertyURI)
            throws GateOntologyException {
        return theDatatypePropertyURI;
    }

    public Property[] getDatatypeProperties(String theResourceURI)
            throws GateOntologyException {
        Set<Property> props = op.getDatatypeProperties(theResourceURI);
        return props.toArray(new Property[]{});
    }

    public Property[] getDatatypeProperties() throws GateOntologyException {
        Set<Property> props = op.getDatatypeProperties();
        return props.toArray(new Property[]{});
    }

    public PropertyValue[] getDatatypePropertyValues(String anInstanceURI,
                                                     String aDatatypePropertyURI) {
        // TODO Auto-generated method stub
        return new PropertyValue[]{};
    }

    public String[] getDifferentIndividualFrom(String individualURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return new String[]{};
    }

    public String[] getDisjointClasses(String classURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return new String[]{};
    }

    public ResourceInfo[] getDomain(String aPropertyURI)
            throws GateOntologyException {
        ResourceInfo[] domains = op.getDomains(aPropertyURI);
        return domains;
    }

    public ResourceInfo[] getEquivalentClasses(String aClassURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return new ResourceInfo[]{};
    }

    public Property[] getEquivalentPropertyAs(String aPropertyURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return new Property[]{};
    }

    public Set<OInstance> getInstancesByName(String name) {
        // TODO Auto-generated method stub
        return new HashSet<OInstance>();
    }

    public ClosableIterator<OInstance> getInstancesIterator(ONodeID aClass,
                                                            Closure closure) {
        Set<OInstance> instances = op.getInstances(aClass);
        ClosableIterator<OInstance> ii = new ClosableIteratorImpl<OInstance>(instances.iterator());
        return ii;
    }

    public Property[] getInverseProperties(String aPropertyURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return op.getInverses(aPropertyURI);
    }

    public Property[] getObjectProperties(String theResourceURI)
            throws GateOntologyException {
        Set<Property> props = op.getObjectProperties(theResourceURI);
        return props.toArray(new Property[]{});
    }

    public Property[] getObjectProperties() throws GateOntologyException {
        Set<Property> props = op.getObjectProperties();
        return props.toArray(new Property[]{});
    }

    public String[] getObjectPropertyValues(String sourceInstanceURI,
                                            String anObjectPropertyURI) throws GateOntologyException {
        // TODO Auto-generated method stub
        return new String[]{};
    }

    public Property getOnPropertyValue(String restrictionURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<RDFProperty> getPropertiesByName(String name) {
        Set<RDFProperty> rdfProperties = new HashSet<RDFProperty>();
        for (Property pr : op.getRDFProperties(name)) {
            RDFPropertyImpl p = new RDFPropertyImpl(new OURIImpl(pr.getUri()), ontology, this);
            rdfProperties.add(p);
        }
        return rdfProperties;
    }

    public Property[] getPropertiesWithResourceAsDomain(String theResourceURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return new Property[]{};
    }

    public Property[] getPropertiesWithResourceAsRange(String theResourceURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return new Property[]{};
    }

    public Property getRDFProperty(String thePropertyURI)
            throws GateOntologyException {
        Set<Property> pps = op.getAllProperties(thePropertyURI);
        if (pps != null && !pps.isEmpty()) {
            return pps.iterator().next();
        }
        return null;
    }

    public PropertyValue getPropertyValue(String restrictionURI,
                                          byte restrictionType) throws GateOntologyException {
        // TODO Auto-generated method stub
        return null;
    }

    public Property[] getRDFProperties(String theResourceURI)
            throws GateOntologyException {
        Set<Property> props = op.getRDFProperties(theResourceURI);
        return props.toArray(new Property[]{});
    }

    public Property[] getRDFProperties() throws GateOntologyException {
        Set<Property> props = op.getRDFProperties();
        return props.toArray(new Property[]{});
    }

    public ResourceInfo[] getRDFPropertyValues(String anInstanceURI,
                                               String anRDFPropertyURI) throws GateOntologyException {
        // TODO Auto-generated method stub
        return new ResourceInfo[]{};
    }

    public ResourceInfo[] getRange(String aPropertyURI)
            throws GateOntologyException {
        ResourceInfo[] ranges = op.getRangess(aPropertyURI);
        return ranges;
    }

    public Property[] getInverse(String aPropertyURI) throws GateOntologyException {
        return op.getInverses(aPropertyURI);

    }

    public ResourceInfo getRestrictionValue(String restrictionURI,
                                            byte restrictionType) throws GateOntologyException {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getSameIndividualAs(String individualURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return new String[]{};
    }

    public Set<OClass> getSubClasses(ONodeID superClassURI, Closure direct) {
        Set<OClass> classes = new HashSet<OClass>();
        for (ONodeID nodeid : op.getStoredSubConcepts(superClassURI, direct)) {
            OClass cl = new OClassImpl(nodeid, ontology, this);
            classes.add(cl);
        }
        return classes;
    }

    public ClosableIterator<OClass> getSubClassesIterator(ONodeID forClass,
                                                          Closure closure) {
        // TODO Auto-generated method stub
        return new ClosableIteratorImpl<OClass>(new HashSet<OClass>().iterator());
    }

    public Property[] getSubProperties(String aPropertyURI, Closure direct)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return new Property[]{};
    }

    public Property[] getSubProperties(String aPropertyURI, boolean direct)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return new Property[]{};
    }

    public ResourceInfo[] getSuperClasses(String subClassURI, Closure direct)
            throws GateOntologyException {
        ArrayList<ResourceInfo> result = new ArrayList<ResourceInfo>();
        // nn implement
        for (ONodeID node : op.getSuperClasses(subClassURI, direct)) {
            result.add(new ResourceInfo(node.toString(), OConstants.OWL_CLASS));
        }
        return result.toArray(new ResourceInfo[]{});
    }

    public Property[] getSuperProperties(String aPropertyURI, Closure direct)
            throws GateOntologyException {
        return new Property[]{};
    }

    public Property[] getSuperProperties(String aPropertyURI, boolean direct)
            throws GateOntologyException {
        return new Property[]{};
    }

    public Property[] getSymmetricProperties(String theResourceURI)
            throws GateOntologyException {
        return new Property[]{};
    }

    public Property[] getSymmetricProperties() throws GateOntologyException {
        return new Property[]{};
    }

    public Property[] getTransitiveProperties(String theResourceURI)
            throws GateOntologyException {
        return new Property[]{};
    }

    public Property[] getTransitiveProperties() throws GateOntologyException {
        return new Property[]{};
    }

    public boolean hasClass(String classURI) throws GateOntologyException {
        return !op.getClasses(classURI).isEmpty();
    }

    public boolean hasInstance(OURI theURI, ONodeID theClass, Closure closure) {
        return false;
    }

    public boolean isAnnotationProperty(String aPropertyURI)
            throws GateOntologyException {
        return false;
    }

    public boolean isDatatypeProperty(String aPropertyURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isDifferentIndividualFrom(String theInstanceURI1,
                                             String theInstanceURI2) throws GateOntologyException {
        return false;
    }

    public boolean isEquivalentClassAs(String theClassURI1, String theClassURI2)
            throws GateOntologyException {
        return false;
    }

    public boolean isEquivalentPropertyAs(String aPropertyURI1,
                                          String aPropertyURI2) throws GateOntologyException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isFunctional(String aPropertyURI) throws GateOntologyException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isInverseFunctional(String aPropertyURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isObjectProperty(String aPropertyURI)
            throws GateOntologyException {
        return !this.op.getObjectProperties(aPropertyURI).isEmpty();
    }

    public boolean isRDFProperty(String aPropertyURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSameIndividualAs(String theInstanceURI1,
                                      String theInstanceURI2) throws GateOntologyException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSubClassOf(String theSuperClassURI, String theSubClassURI,
                                Closure direct) throws GateOntologyException {
        return op.isSubClassOf(theSuperClassURI,theSubClassURI,direct);
    }

    public boolean isSubPropertyOf(String aSuperPropertyURI,
                                   String aSubPropertyURI, Closure direct) throws GateOntologyException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSuperClassOf(String theSuperClassURI, String theSubClassURI,
                                  Closure direct) throws GateOntologyException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSuperPropertyOf(String aSuperPropertyURI,
                                     String aSubPropertyURI, Closure direct) throws GateOntologyException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSymmetricProperty(String aPropertyURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isTopClass(String classURI) throws GateOntologyException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isTransitiveProperty(String aPropertyURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return false;
    }

    public void removeAnnotationPropertyValue(String theResourceURI,
                                              String theAnnotationPropertyURI, String value, String language)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void removeAnnotationPropertyValues(String theResourceURI,
                                               String theAnnotationPropertyURI) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public String[] removeClass(String classURI, boolean deleteSubTree)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return new String[]{};
    }

    public void removeDatatypePropertyValue(String anInstanceURI,
                                            String aDatatypePropertyURI, String datatypeURI, String value) {
        // TODO Auto-generated method stub

    }

    public void removeDatatypePropertyValues(String anInstanceURI,
                                             String aDatatypePropertyURI) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public String[] removeIndividual(String individualURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return new String[]{};
    }

    public void removeObjectPropertyValue(String sourceInstanceURI,
                                          String anObjectPropertyURI, String theValueInstanceURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void removeObjectPropertyValues(String sourceInstanceURI,
                                           String anObjectPropertyURI) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public String[] removePropertyFromOntology(String aPropertyURI,
                                               boolean removeSubTree) throws GateOntologyException {
        // TODO Auto-generated method stub
        return new String[]{};
    }

    public void removeRDFPropertyValue(String anInstanceURI,
                                       String anRDFPropertyURI, String aResourceURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void removeRDFPropertyValues(String anInstanceURI,
                                        String anRDFPropertyURI) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void removeStatement(String subjectURI, String predicateURI,
                                String objectURI) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void removeStatement(String subject, String predicate, String object,
                                String datatype) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void removeSubClass(String superClassURI, String subClassURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void removeSubProperty(String superPropertyURI, String subPropertyURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void removeSuperClass(String superClassURI, String subClassURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void removeSuperProperty(String superPropertyURI, String subPropertyURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void setDifferentIndividualFrom(String individual1uri,
                                           String individual2uri) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void setDisjointClassWith(String class1uri, String class2uri)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void setEquivalentClassAs(String class1uri, String class2uri)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void setEquivalentPropertyAs(String property1uri, String property2uri)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void setFunctional(String aPropertyURI, boolean isFunctional)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void setInverseFunctional(String aPropertyURI,
                                     boolean isInverseFunctional) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void setInverseOf(String propertyURI1, String propertyURI2)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void setOnPropertyValue(String restrictionURI, String propertyURI)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void setPropertyValue(String restrictionURI, byte restrictionType,
                                 String value, String datatypeURI) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void setRestrictionValue(String restrictionURI, byte restrictionType,
                                    String value) throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public void setSameIndividualAs(String individual1uri, String individual2uri)
            throws GateOntologyException {
        // TODO Auto-generated method stub

    }

    public boolean isImplicitResource(String resourceID)
            throws GateOntologyException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean containsURI(OURI uri) {
        // TODO Auto-generated method stub
        return false;
    }

    public void writeOntologyData() {
        op.writeOntologyData();

    }

    public AnnotationValue[] getAnnotationValues(String annotatedResource) {
        return op.getAnnotationPropertyValues(annotatedResource);
    }


    String author = "";
    String language = "";

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    public void addClass(String classURI, String superClassURI) {
        op.addClass(classURI, superClassURI);

    }

    public void writeInstanceData(String filename) {
        op.writeInstanceData(filename);

    }

    public void addInstanceRelation(OInstance i1, OInstance i2, URI uri) {
        op.addIndividualRelation(i1.getOURI().toString(), i2.getOURI().toString(), uri.toString());

    }

    public void addQuad(String[] quad) {
        if (quad.length == 4) {
            op.addQuad(quad[0], quad[1], quad[2], quad[3]);
        }

    }

    public void writeQuads(String filename) {
        op.writeQuads(filename);

    }

    public void setClassify(boolean classify) {
      op.setClassify(classify);
      
    }

    public void setReasoner(Reasoner reasoner) {
      op.setReasoner(reasoner);
      
    }


}
