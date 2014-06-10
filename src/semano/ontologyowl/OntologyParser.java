package semano.ontologyowl;

import gate.creole.ontology.OConstants;
import gate.creole.ontology.OConstants.Closure;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.ONodeID;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import semano.ontologyowl.Reasoning.Reasoner;
import semano.ontologyowl.impl.Property;
import semano.ontologyowl.impl.ResourceInfo;
import semano.util.OntologyUtil;

public class OntologyParser {

    //TODO make configurable
    public static final String DEFAULT_NAMESPACE = "http://www.fiz-karlsruhe.de/";

    Logger logger = Logger.getLogger(this.getClass().getName());

    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    Set<OWLOntology> ontologies = new HashSet<OWLOntology>();

    HashSet<String[]> quads = new HashSet<String[]>();

    HashSet<OWLOntology> modifiedOntologies = new HashSet<OWLOntology>();

    HashMap<OWLEntity, OWLOntology> entities2Ontologies = new HashMap<OWLEntity, OWLOntology>();

    HashMap<OWLOntology, String> filenames = new HashMap<OWLOntology, String>();

    HashMap<ONodeID, Set<ONodeID>> subsumptions = new HashMap<ONodeID, Set<ONodeID>>();

    private HashMap<OWLClassExpression, ONodeID> owl2oclass = new HashMap<OWLClassExpression, ONodeID>();

    private HashMap<OWLAnnotationProperty, Property> annotationProperties = new HashMap<OWLAnnotationProperty, Property>();

    private HashMap<OWLDataProperty, Property> dataProperties = new HashMap<OWLDataProperty, Property>();

    private HashMap<OWLObjectProperty, Property> objectProperties = new HashMap<OWLObjectProperty, Property>();

    private String instanceOntologyFilename = "instances";
    private static String OWL_EXTENSION = ".owl";
    private String dirName = "";
    private HashMap<String, OWLIndividual> indMap = new HashMap<String, OWLIndividual>();
    private Set<OWLAxiom> assertionAxioms = new HashSet<OWLAxiom>();

    private Reasoner reasoner=Reasoner.hermit;
    HashMap<OWLOntology, OWLReasoner> OWLreasoners = new HashMap<OWLOntology, OWLReasoner>();

    private boolean classify=false;

    private static final boolean RENDER_CONCEPT_NAMES = false;

    public String loadOntologies(Set<String> fileNames, String directoryName) throws OWLOntologyCreationException, FileNotFoundException {
        this.dirName = directoryName;
        String defaultUri = DEFAULT_NAMESPACE;
        for (String fileName : fileNames) {
            logger.debug("starting loading ontology " + fileName);
            OWLOntology o = OntologyUtil.loadOntology(manager, fileName);

            System.out.println("Loaded Ontology " + fileName);
            if (o != null && o.getOntologyID().getOntologyIRI() != null) {
                ontologies.add(o);
                this.filenames.put(o, fileName);
                if(classify){
                  System.out.println("classifying ontology");
                  OWLReasoner OWLreasoner=Reasoning.getReasoner(o, reasoner);
                  OWLreasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
                  OWLreasoners.put(o, OWLreasoner);
                }
                logger.debug("loaded ontology " + fileName);
            } else {
                System.err.println("could not load ontology: " + fileName);
            }
        }
        loadAnnotationProperties();
        loadEntities();
        loadSubclassAxioms();
        loadDatatypeProperties();
        loadObjectProperties();
        return defaultUri;
    }

    public void loadOntology(String filename) throws OWLOntologyCreationException, FileNotFoundException {
        logger.debug("starting loading ontology " + filename);
        OWLOntology o = OntologyUtil.loadOntology(manager, filename);
        if (o != null && o.getOntologyID().getOntologyIRI() != null) {
            ontologies.add(o);
            this.filenames.put(o, filename);
            loadAnnotationProperties();
            loadEntities();
            loadSubclassAxioms(o);
            loadObjectProperties();
            loadDatatypeProperties();
            if(classify){
              System.out.println("classifying ontology");
              OWLReasoner OWLreasoner=Reasoning.getReasoner(o, reasoner);
              OWLreasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
              OWLreasoners.put(o, OWLreasoner);
            }
            logger.debug("loaded ontology " + filename);
        } else {
            System.err.println("could not load ontology: " + filename);
        }

    }

    private void loadEntities() {
        for (OWLOntology ox : ontologies) {
            for (OWLEntity e : ox.getSignature()) {
                if (e instanceof OWLClass) {
                    registerNode((OWLClass) e);
                }
                entities2Ontologies.put(e, ox);
                // loadAnnotations(e,ox);
            }
        }

    }

    // private void loadEntities(OWLOntology o) {
    // for(OWLEntity e : o.getSignature()) {
    // entities2Ontologies.put(e, o);
    // loadAnnotations(e,o);
    // }
    // }

    // private void loadAnnotations(OWLEntity e, OWLOntology o) {
    // for(OWLAnnotation a:e.getAnnotations(o)){
    // OWLAnnotationProperty ap= a.getProperty();
    // if(!annotationProperties2Annotations .containsKey(ap)){
    // annotationProperties2Annotations.put(ap, new
    // HashSet<OWLAnnotation>());
    // }
    // annotationProperties2Annotations.get(ap).add(a);
    // }
    // }

    private void loadSubclassAxioms(OWLOntology o) {
        logger.debug("starting loading subclass axioms for "
                + o.getOntologyID().getOntologyIRI().toString());
        // first subclass axioms
        Set<OWLSubClassOfAxiom> subclassAxioms = o.getAxioms(AxiomType.SUBCLASS_OF);
        for (OWLSubClassOfAxiom subaxiom : subclassAxioms) {
            loadAxiom(subaxiom);
        }
        // then equivalent axioms
        // loadEquivAxioms();
        logger.debug("finished loading subclass axioms for "
                + o.getOntologyID().getOntologyIRI().toString());
    }

    private void loadSubclassAxioms() {
        logger.debug("starting loading subclass axioms for all ontologies "
                + ontologies.size());
        // first subclass axioms
        Set<OWLSubClassOfAxiom> subclassAxioms = new HashSet<OWLSubClassOfAxiom>();
        for (OWLOntology ox : ontologies) {
            subclassAxioms.addAll(ox.getAxioms(AxiomType.SUBCLASS_OF));
        }
        for (OWLSubClassOfAxiom subaxiom : subclassAxioms) {
            loadAxiom(subaxiom);
        }
        // then equivalent axioms
        // loadEquivAxioms();
        logger.debug("finished loading subclass axioms ");
    }

    // no need
    // /**
    // *
    // */
    // private void loadEquivAxioms() {
    // Set<OWLEquivalentClassesAxiom> eqclassAxioms = new
    // HashSet<OWLEquivalentClassesAxiom>();
    // for(OWLOntology ox : ontologies) {
    // eqclassAxioms.addAll(ox.getAxioms(AxiomType.EQUIVALENT_CLASSES));
    // }
    // for(OWLEquivalentClassesAxiom eqaxiom : eqclassAxioms) {
    // Set<OWLClassExpression> descriptions =
    // eqaxiom.getClassExpressions();
    // Set<OWLClass> eqclasses = new HashSet<OWLClass>();
    // for(OWLClassExpression d : descriptions) {
    // if(d instanceof OWLClass) {
    // eqclasses.add((OWLClass)d);
    // }
    // }
    // if(eqclasses.size() > 1) {
    // for(OWLClass c1 : eqclasses) {
    // for(OWLClass c2 : eqclasses) {
    // if(c2 != c1) {
    //
    // // get the children of the equivalent class and add them
    // addSubConceptsOf(c1, c2);
    // addSubConceptsOf(c2, c1);
    // }
    // }
    // }
    // }
    // }
    // }

    /**
     * @param c1
     * @param c2
     */
    private void addSubConceptsOf(OWLClass c1, OWLClass c2) {
        Set<OWLSubClassOfAxiom> subOfSuper = new HashSet<OWLSubClassOfAxiom>();
        for (OWLOntology ox : ontologies) {
            subOfSuper.addAll(ox.getSubClassAxiomsForSuperClass(c1));
        }
        for (OWLSubClassOfAxiom a : subOfSuper) {
            OWLClassExpression d = a.getSubClass();
            if (d instanceof OWLClass) {
                OWLSubClassOfAxiom a1 = manager.getOWLDataFactory()
                        .getOWLSubClassOfAxiom(c2, d);
                loadAxiom(a1);
            }
        }
    }

    /**
     * @param subaxiom
     */
    private void loadAxiom(OWLSubClassOfAxiom subaxiom) {
        if (subaxiom.getSuperClass() instanceof OWLClass) {
            if (!subaxiom.getSuperClass().isOWLThing()) {
                OWLClassExpression subCls = subaxiom.getSubClass();
                if (subCls instanceof OWLClass) {
                    if (!subCls.isOWLNothing()) {
                        registerSubsumption(subaxiom, subCls);
                    }

                }
            }
        }
    }

    /**
     * @param subaxiom
     * @param subCls
     */
    private void registerSubsumption(OWLSubClassOfAxiom subaxiom,
                                     OWLClassExpression subCls) {
        ONodeID superNode = registerNode((OWLClass) subaxiom.getSuperClass());
        if (isNumericID(superNode.getResourceName())) {
//      System.out.println("node "+superNode.getResourceName()+" has no label!");
        }
        ONodeID subNode = registerNode((OWLClass) subCls);
        if (isNumericID(subNode.getResourceName())) {
//      System.out.println("node "+subNode.getResourceName()+" has no label!");
        }
        // if the opposite is not yet included
        if (!subsumptions.containsKey(subNode)
                || !subsumptions.get(subNode).contains(superNode)) {
            Set<ONodeID> subs = getStoredSubConcepts(superNode);
            subs.add(subNode);
            subsumptions.put(superNode, subs);
//      logger.debug("added subclass axiom");

        }
    }

    private ONodeID registerNode(OWLClassExpression cl) {
        if (owl2oclass.containsKey(cl)) return owl2oclass.get(cl);
        String uri = "";
        if (cl.isAnonymous()) {
            uri = cl.toString();
        } else {
            uri = cl.asOWLClass().getIRI().toString();
            OWLDataFactory f = manager.getOWLDataFactory();
            if (isNumericID(cl.toString())) {
                for (OWLOntology o : ontologies) {
                    Set<OWLAnnotation> labels = cl.asOWLClass()
                            .getAnnotations(o, f
                                    .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL
                                            .getIRI()));
                    if (RENDER_CONCEPT_NAMES && labels != null && labels.size() > 0) {
                        uri = uri.replace("#", "/");
                        uri += "#" + labels.toArray(new OWLAnnotation[]{})[0].getValue();
                        uri = uri.replace("\"", "");
                        uri = uri.replace(" ", "_");
                        uri = uri.split("@")[0];
//          logger.debug("setting label for " + cl);
                        break;
                    }
                }
            }
        }
        ONodeID nodeid = new ONodeIDImpl(uri, false);
        owl2oclass.put(cl, nodeid);
        return nodeid;
    }

    private boolean isNumericID(String string) {
        if (string.matches(".*>$"))
            return (string.matches(".*_[0-9]+>$"));
        else return (string.matches(".*_[0-9]+$"));
    }

    private Set<ONodeID> getStoredSubConcepts(ONodeID superClassURI) {
//    logger.debug("getting subconcepts for " + superClassURI.toString());
        Set<ONodeID> result = new HashSet<ONodeID>();
        if (subsumptions.containsKey(superClassURI)) {
            result = subsumptions.get(superClassURI);
        }
        result.remove(superClassURI);
        return result;
    }


    public Set<ONodeID> getStoredSubConcepts(ONodeID superClassURI, Closure direct) {
        if (Closure.DIRECT_CLOSURE.equals(direct))
            return getStoredSubConcepts(superClassURI);
        else {
            int depth = 0;
            Set<ONodeID> allSubClasses = new HashSet<ONodeID>();
            Set<ONodeID> newsubclasses = new HashSet<ONodeID>();
            newsubclasses = getStoredSubConcepts(superClassURI);
            while (!newsubclasses.isEmpty()) {
                Set<ONodeID> classesToRemove = new HashSet<ONodeID>();
                for (ONodeID cl : newsubclasses) {
                    if (!allSubClasses.contains(cl)) {
                        allSubClasses.add(cl);
                    } else {
                        classesToRemove.add(cl);
                    }
                }
                newsubclasses.removeAll(classesToRemove);
                newsubclasses = getNextLevelSubclasses(newsubclasses);
                depth++;
//        if(depth>707){
//          System.out.println("depth too large!");
//        }
            }
            return allSubClasses;
        }
    }

    private Set<ONodeID> getNextLevelSubclasses(Set<ONodeID> classes) {
        Set<ONodeID> allSubClasses = new HashSet<ONodeID>();
        for (ONodeID clas : classes) {
            allSubClasses.addAll(getStoredSubConcepts(clas));
        }
        return allSubClasses;
    }


    public Set<ONodeID> getTopConcepts() {
        logger.debug("getting topconcepts");

        Set<ONodeID> topconcepts = new HashSet<ONodeID>();
        for (OWLOntology o : ontologies) {
            for (OWLClass cls : o.getClassesInSignature()) {
                if (!cls.isOWLThing()) {

                    Set<OWLClassExpression> superclasses = new HashSet<OWLClassExpression>();
                    for (OWLOntology ox : ontologies) {
                        superclasses.addAll(cls.getSuperClasses(ox));
                    }
                    boolean top = true;
                    for (OWLClassExpression superCls : superclasses) {
                        if (superCls instanceof OWLClass) {
                            if (!superCls.isOWLThing()) {
                                top = false;
                            }
                        }
                    }
                    if (top && !cls.toString().endsWith("#>")) {
                        topconcepts.add(owl2oclass.get(cls));
                    }
                }
            }
        }
        logger.debug("finished getting topconcepts");
        return topconcepts;
    }

    public Set<String> getImportURIS() {
        // TODO Auto-generated method stub
        return new HashSet<String>();
    }

    public Set<String> getLoadedOntologyUris() {
        Set<String> uris = new HashSet<String>();
        for (OWLOntology o : ontologies) {
            if (o != null && o.getOntologyID() != null && o.getOntologyID().getOntologyIRI() != null) {
                uris.add(o.getOntologyID().getOntologyIRI().toString());
            }
        }
        return uris;
    }

    public Set<OInstance> getInstances(ONodeID aClass) {

        Set<OInstance> result = new HashSet<OInstance>();
        // TODO Auto-generated method stub

        return result;
    }

    private OWLClass getOWLClass(String theResourceURI) {
        Set<ONodeID> classes = getClasses(theResourceURI);
        if (classes != null && !classes.isEmpty()) {
            ONodeID node = classes.iterator().next();
            if (node != null) {
                for (OWLClassExpression cl : owl2oclass.keySet()) {
                    if (owl2oclass.get(cl).equals(node)) {
                        if (cl instanceof OWLClass) {
                            return (OWLClass) cl;
                        } else {
//            System.err.println("there is an anonymous class which mathces the specified resource name, but annotations are not allowed for it!");
                        }
                    }
                }
            }
        }

        return null;
    }


    private OWLClassExpression getOWLClassExpression(String theResourceURI) {
        Set<ONodeID> classes = getClasses(theResourceURI);
        if (classes != null && !classes.isEmpty()) {
            ONodeID node = classes.iterator().next();
            if (node != null) {
                for (OWLClassExpression cl : owl2oclass.keySet()) {
                    if (owl2oclass.get(cl).equals(node)) {
                        return cl;
                    }
                }
            }
        }

        return null;
    }

    private OWLEntity getEntity(String theResourceURI) {
        OWLEntity e = null;
        for (Entry<OWLClassExpression, ONodeID> nodeID : owl2oclass.entrySet()) {
            if ((nodeID.getValue().getNameSpace() + nodeID.getValue()
                    .getResourceName()).equals(theResourceURI)) {
                if (nodeID.getKey() instanceof OWLClass) {
                    return (OWLClass) nodeID.getKey();
                } else {
//          System.err.println("there is an anonymous class which mathces the specified resource name, but annotations are not allowed for it!");
                }
            }
        }
        for (Entry<OWLObjectProperty, Property> nodeID : objectProperties.entrySet()) {
            if (nodeID.getValue().getUri().equals(theResourceURI)) {
                return nodeID.getKey();
            }
        }
        for (Entry<OWLDataProperty, Property> nodeID : dataProperties.entrySet()) {
            if (nodeID.getValue().getUri().equals(theResourceURI)) {
                return nodeID.getKey();
            }
        }
        for (OWLOntology o : ontologies) {
            e = OntologyUtil.extractEntityWithSubstring(o, theResourceURI);
            if (e != null) return e;
        }
        return e;
    }

    // //////////////// ANNOTATION PROPERTIES//////////////////////////

    /**
     *
     */
    private void loadAnnotationProperties() {
        Set<OWLAnnotationProperty> auris = new HashSet<OWLAnnotationProperty>();
        for (OWLOntology o : ontologies) {
            auris.addAll(o.getAnnotationPropertiesInSignature());
        }
        for (OWLAnnotationProperty u : auris) {
            Property p = new Property(OConstants.ANNOTATION_PROPERTY, u.getIRI()
                    .toString());
            annotationProperties.put(u, p);
        }
    }

    public Set<Property> getAnnotationProperties() {
        return new HashSet<Property>(annotationProperties.values());
    }

    /**
     * @param name
     * @return
     */
    public HashSet<Property> getAnnotationProperties(String name) {
        HashSet<Property> result = new HashSet<Property>();
        for (Property p : annotationProperties.values()) {
            if (matchesEntity(p.getUri().toLowerCase(), name.toLowerCase())) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * @param name
     * @return
     */
    public HashSet<OWLAnnotationProperty> getAnnotationPropertyUris(String name) {
        HashSet<OWLAnnotationProperty> result = new HashSet<OWLAnnotationProperty>();
        for (OWLAnnotationProperty p : annotationProperties.keySet()) {
            String uri = p.toString().toLowerCase();
            if (matchesEntity(uri, name.toLowerCase())) {
                result.add(p);
            }
        }
        return result;
    }

    public void addAnnotationProperty(String aPropertyURI) {
        // TODO Auto-generated method stub
    }

    /**
     * returns all entity annotations annotated with the annotation
     * property given as uri-string
     *
     * @param theAnnotationPropertyURI
     * @return set of entity annotations
     */
    public Set<AnnotationValue> getAnnotations(String theAnnotationPropertyURI) {
        Set<AnnotationValue> result = new HashSet<AnnotationValue>();
        HashSet<OWLAnnotationProperty> aps = getAnnotationPropertyUris(theAnnotationPropertyURI);
        for (OWLAnnotationProperty ap : aps) {
            for (OWLEntity e : entities2Ontologies.keySet()) {
                Set<OWLAnnotation> allAnnotationsOfClas = new HashSet<OWLAnnotation>();
                for (OWLOntology ox : ontologies) {
                    allAnnotationsOfClas.addAll(e.getAnnotations(ox, ap));
                }
                for (OWLAnnotation a : allAnnotationsOfClas) {
                    AnnotationValue av = new AnnotationValue(a.getProperty().getIRI()
                            .toString(), e.getIRI().toString(), a.getValue().toString());
                    result.add(av);
                }
            }
        }
        return result;
    }


    public String getAnnotationPropertyValue(String theResourceURI,
                                             String theAnnotationPropertyURI, String language) {
        AnnotationValue[] res = getAnnotationPropertyValues(theResourceURI,
                theAnnotationPropertyURI);
        for (AnnotationValue av : res) {
            if (av.getLanguage().equals(language))
                return av.getValue();
        }
        return "";
    }

    /**
     * gets annotation values stored in the ontology set for the given
     * entity and annotation property
     *
     * @param theResourceURI
     * @param theAnnotationPropertyURI
     * @return
     */
    public AnnotationValue[] getAnnotationPropertyValues(String theResourceURI,
                                                         String theAnnotationPropertyURI) {
        Set<AnnotationValue> result = new HashSet<AnnotationValue>();
        OWLEntity e = getEntity(theResourceURI);
        HashSet<OWLAnnotationProperty> aps = getAnnotationPropertyUris(theAnnotationPropertyURI);

        for (OWLAnnotationProperty ap : aps) {
            Set<OWLAnnotation> allAnnotationsOfClas = new HashSet<OWLAnnotation>();
            for (OWLOntology ox : ontologies) {
                allAnnotationsOfClas.addAll(e.getAnnotations(ox, ap));
            }
            for (OWLAnnotation a : allAnnotationsOfClas) {
                AnnotationValue av = new AnnotationValue(a.getProperty().getIRI()
                        .toString(), e.getIRI().toString(), a.getValue().toString());
                result.add(av);
            }
        }
        return result.toArray(new AnnotationValue[]{});
    }


    /**
     * gets annotation values stored in the ontology set for the given
     * entity
     *
     * @param annotatedResource
     * @return
     */
    public AnnotationValue[] getAnnotationPropertyValues(String annotatedResource) {
        Set<AnnotationValue> result = new HashSet<AnnotationValue>();
        OWLEntity e = getEntity(annotatedResource);
        Set<OWLAnnotation> allAnnotationsOfClas = new HashSet<>();
        for (OWLOntology ox : ontologies) {
            allAnnotationsOfClas.addAll(e.getAnnotations(ox));
        }
        for (OWLAnnotation a : allAnnotationsOfClas) {

            AnnotationValue av = new AnnotationValue(a.getProperty().getIRI()
                    .toString(), e.getIRI().toString(), a.getValue().toString());
            result.add(av);

        }
        return result.toArray(new AnnotationValue[]{});
    }

    /**
     * used to implement the generic API and is called to write Data to Ontology
     * stores the annotation value in the ontology manager, so that the next time the ontology is flushed, the annotation value is written into the file.
     *
     * @param theResourceURI
     * @param theAnnotationPropertyURI
     * @param value1
     * @param language
     */
    public boolean addAnnotationPropertyValue(String theResourceURI,
                                              String theAnnotationPropertyURI, String value1, String language) {
        try {
            OWLEntity clas = getEntity(theResourceURI);
            OWLOntology o = entities2Ontologies.get(clas);
            if (o != null && clas != null) {
                Set<OWLAnnotationProperty> uris = getAnnotationPropertyUris(theAnnotationPropertyURI);
                OWLDataFactory f = manager.getOWLDataFactory();
                String value = AnnotationValue.formatWithLanguageOnly(value1, language);
                OWLLiteral litValue = f.getOWLTypedLiteral(value);
                for (OWLAnnotationProperty u : uris) {
                    Set<OWLAnnotation> allAnnotationsOfClas = new HashSet<OWLAnnotation>();
                    for (OWLOntology ox : ontologies) {
                        allAnnotationsOfClas.addAll(clas.getAnnotations(ox, u));
                    }
                    // do not store the same value several times:
                    boolean found = false;
                    for (OWLAnnotation a : allAnnotationsOfClas) {
                        //compare the annotation property, the entity, the language and the value
                        AnnotationValue av = new AnnotationValue(a.getProperty().getIRI()
                                .toString(), clas.getIRI().toString(), a.getValue().toString());
                        AnnotationValue newav = new AnnotationValue(theAnnotationPropertyURI, clas.getIRI().toString(), value);
                        if (av.contentEquals(newav)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        OWLAnnotation annotation = f.getOWLAnnotation(u, litValue);
                        OWLAnnotationAxiom ax = f.getOWLAnnotationAssertionAxiom(clas
                                .getIRI(), annotation);
                        manager.applyChange(new AddAxiom(o, ax));
                        modifiedOntologies.add(o);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * used by the plugin itself
     * stores the annotation value in the ontology manager, so that the next time the ontology is flushed, the annotation value is written into the file.
     *
     * @param av AnnotationValue
     */
    public void addAnnotationPropertyValue(AnnotationValue av) {
        try {
            OWLEntity entityClass = getEntity(av.getAnnotatedEntity());
            OWLOntology o = entities2Ontologies.get(entityClass);
            if (o != null && entityClass != null) {
                Set<OWLAnnotationProperty> uris = getAnnotationPropertyUris(av.getAnnotationProperty());
                OWLDataFactory f = manager.getOWLDataFactory();
                OWLLiteral litValue = f.getOWLTypedLiteral(av.formatMetaData());
                for (OWLAnnotationProperty u : uris) {
                    // do not store the same value several times:
                    boolean found = false;
                    for (OWLAnnotation a : entityClass.getAnnotations(o, u)) {

                        if ((new AnnotationValue(u.getIRI()
                                .toString(), entityClass.getIRI().toString(), a.getValue().toString())).contentEquals(av)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        OWLAnnotation annotation = f.getOWLAnnotation(u, litValue);
                        OWLAnnotationAxiom ax = f.getOWLAnnotationAssertionAxiom(entityClass
                                .getIRI(), annotation);
                        manager.applyChange(new AddAxiom(o, ax));
                        modifiedOntologies.add(o);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // //////////////// DATA PROPERTIES//////////////////////////

    public void loadDatatypeProperties() {
        Set<OWLDataProperty> auris = new HashSet<OWLDataProperty>();
        for (OWLOntology o : ontologies) {
            auris.addAll(o.getDataPropertiesInSignature());
        }
        for (OWLDataProperty u : auris) {
            Property p = new Property(OConstants.DATATYPE_PROPERTY, u.getIRI()
                    .toString());
            dataProperties.put(u, p);
        }
    }

    public Set<Property> getDatatypeProperties() {
        return new HashSet<Property>(dataProperties.values());
    }

    public Set<Property> getDatatypeProperties(String name) {
        HashSet<Property> result = new HashSet<Property>();
        if (name != null) {
            for (Property id : dataProperties.values()) {
                if (matchesEntity(id.getUri(), name)) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    // //////////////// OBJECT PROPERTIES//////////////////////////

    public void loadObjectProperties() {
        Set<OWLObjectProperty> auris = new HashSet<OWLObjectProperty>();
        for (OWLOntology o : ontologies) {
            auris.addAll(o.getObjectPropertiesInSignature());
        }
        for (OWLObjectProperty u : auris) {
            registerObjectProperty(u);
        }
    }

    /**
     * @param u
     */
    private void registerObjectProperty(OWLObjectProperty u) {
        Property p = new Property(OConstants.OBJECT_PROPERTY, u.getIRI().toString());
        objectProperties.put(u, p);
    }

    public Set<Property> getObjectProperties(String name) {
        HashSet<Property> result = new HashSet<Property>();
        if (name != null) {
            for (Property id : objectProperties.values()) {
                if (matchesEntity(id.getUri(), name)) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    public Set<Property> getObjectProperties() {
        return new HashSet<Property>(objectProperties.values());
    }

    // //////////////// COMBINED PROPERTIES
    // /////////////////////////////////


    private boolean matchesEntity(String uri, String name) {
        if (uri.startsWith("<") && uri.endsWith(">"))
            uri = uri.substring(1, uri.length() - 1);
        return uri.endsWith(name);
    }


    /**
     * no annotation properties included
     *
     * @param name
     * @return
     */
    public HashSet<Property> getRDFProperties(String name) {
        HashSet<Property> result = new HashSet<Property>();
        if (name != null) {
            result.addAll(getDatatypeProperties(name));
            result.addAll(getObjectProperties(name));
        } else {
            System.out.println("given name of the property to search is null");
        }
        return result;
    }

    /**
     * gets all properties incl. annotation properties
     *
     * @param name
     * @return
     */
    public Set<Property> getAllProperties(String name) {
        HashSet<Property> result = new HashSet<Property>();
        result.addAll(getDatatypeProperties(name));
        result.addAll(getObjectProperties(name));
        result.addAll(getAnnotationProperties(name));
        return result;
    }

    /**
     * no annotation properties included
     *
     * @return
     */
    public Set<Property> getRDFProperties() {
        HashSet<Property> result = new HashSet<Property>();
        result.addAll(getDatatypeProperties());
        result.addAll(getObjectProperties());
        return result;
    }

    // ////////////// other methods//////////////////////////

    public Set<ONodeID> getSuperClasses(String subClassURI, Closure direct) {
        OWLEntity e = getEntity(subClassURI);
        if (e != null) {
            if (e instanceof OWLClass) {
                OWLClass cls = (OWLClass) e;
                return getSuperClasses(cls, direct);
//        Set<OWLClassExpression> superclasses = cls.getSuperClasses(ontologies);
//        for(OWLClassExpression cl:superclasses){
//          if(cl instanceof OWLClass){
//          ONodeID nodeId = registerNode((OWLClass) cl);
//          if(nodeId != null && nodeId.toString()!= null){
//            sups.add(nodeId);
//          }
//          }
//        }

            }
        }
        return new HashSet<ONodeID>();
    }


    private Set<ONodeID> getSuperClassesAnyLattice(OWLClass cls, Closure closure) {
        Set<ONodeID> sups = new HashSet<ONodeID>();
        Set<OWLClassExpression> superclassesAll = new HashSet<OWLClassExpression>();
        superclassesAll.add(cls);
        if (Closure.TRANSITIVE_CLOSURE.equals(closure))
            while (!superclassesAll.isEmpty()) {
                boolean foundNew = false;
                Set<OWLClassExpression> superclasses = new HashSet<OWLClassExpression>();
                for (OWLClassExpression c : superclassesAll) {
                    if (c instanceof OWLClass)
                        superclasses.addAll(((OWLClass) c).getSuperClasses(ontologies));
                }
                superclassesAll = new HashSet<OWLClassExpression>();
                ONodeID nodeId = null;
                OWLClass c = null;
                for (OWLClassExpression cl : superclasses) {
                    if (cl instanceof OWLClass) {
                        if (!superclassesAll.contains(cl)) {
                            foundNew = true;
                            c = (OWLClass) cl;
                            nodeId = registerNode(c);
                        }
                    }
                }
                if (nodeId != null && nodeId.toString() != null) {
                    sups.add(nodeId);
                    superclassesAll.add(c);
                }
            }
        return sups;
    }


    private Set<ONodeID> getSuperClasses(OWLClass cls, Closure closure) {
        Set<ONodeID> sups = new HashSet<ONodeID>();
        Set<OWLClassExpression> superclassesAll = new HashSet<OWLClassExpression>();
        superclassesAll.add(cls);
        boolean proceed = true;
        while (proceed) {
            if (!Closure.TRANSITIVE_CLOSURE.equals(closure)) {
                proceed = false;
            }
            boolean foundNew = false;
            Set<OWLClassExpression> superclasses = new HashSet<OWLClassExpression>();
            for (OWLClassExpression c : superclassesAll) {
                if (c instanceof OWLClass)
                    superclasses.addAll(((OWLClass) c).getSuperClasses(ontologies));
            }
            for (OWLClassExpression cl : superclasses) {
                if (cl instanceof OWLClass) {
                    if (!superclassesAll.contains(cl)) {
                        foundNew = true;
                        ONodeID nodeId = registerNode((OWLClass) cl);
                        if (nodeId != null && nodeId.toString() != null) {
                            sups.add(nodeId);
                        }
                        superclassesAll.add(cl);
                    }
                }
            }
            if (!foundNew)
                proceed = false;
        }


//    if(sups.toString().contains("aterial")){
//      System.out.println();
//    }
        return sups;
    }

    public void addIndividual(String classURI, String individualURI) {
        OWLEntity clas = getEntity(classURI);
        if (clas instanceof OWLClassExpression) {
            OWLIndividual ind = manager.getOWLDataFactory().getOWLNamedIndividual(IRI.create(individualURI));
            OWLAxiom a = manager.getOWLDataFactory().getOWLClassAssertionAxiom((OWLClassExpression) clas, ind);
            indMap.put(individualURI, ind);
            assertionAxioms.add(a);
        }

    }

    public void addIndividualRelation(String individualURI1, String individualURI2, String propertyUri) {
        if (indMap.containsKey(individualURI1) && indMap.containsKey(individualURI2)) {
            OWLIndividual ind1 = indMap.get(individualURI1);
            OWLIndividual ind2 = indMap.get(individualURI2);
            OWLEntity prop = getEntity(propertyUri);
            if (prop instanceof OWLObjectProperty) {
                assertionAxioms.add(manager.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom((OWLObjectProperty) prop, ind1, ind2));
            }
        }
    }

    public ResourceInfo[] getClassesOfIndividual(String individualURI,
                                                 Closure direct) {
        // TODO Auto-generated method stub
        return new ResourceInfo[]{};
    }

    public void writeInstanceData(String filenameSuffix) {
        String directory = dirName + "/" + OntologyUtil.convertToProperUriString(instanceOntologyFilename);
        File f = new File(directory);
        if (!f.exists()) {
            f.mkdir();
        }
        String filename = directory + "/" + OntologyUtil.convertToProperUriString(filenameSuffix) + OWL_EXTENSION;
        if (filename != null) {
            OWLOntology instanceOntology = OntologyUtil.createOntology(assertionAxioms, manager, OntologyUtil.convertToProperUriString(filenameSuffix) + OWL_EXTENSION);
            OntologyUtil.saveOntology(manager, instanceOntology, filename);
        }
    }

    public void writeOntologyData() {
        try {
            for (OWLOntology o : modifiedOntologies) {
                String filename = filenames.get(o);
                if (filename != null) {
                    OntologyUtil.saveOntology(manager, o, filename);
                } else {
                    System.err.println("the filename of the ontology " + o.getOntologyID().getOntologyIRI().toString() + " is null");
                }
            }
            modifiedOntologies = new HashSet<OWLOntology>();
        } catch (UnknownOWLOntologyException e) {
            e.printStackTrace();
        }

    }

    private OWLClassExpression findRegisteredClass(String uriencoded) {
        String uri = uriencoded;
        OWLClassExpression classOWL = null;
        ONodeID nodeid = null;
        try {
            nodeid = new ONodeIDImpl(uri, false);
        } catch (Exception e) {
            // find

            nodeid = new ONodeIDImpl(uri, false);
        }
        if (nodeid != null && owl2oclass.containsValue(nodeid)) {

            for (Entry<OWLClassExpression, ONodeID> entry : owl2oclass.entrySet()) {
                if (entry.getValue().equals(nodeid)) {
                    classOWL = entry.getKey();
                }
            }

        }

        return classOWL;
    }

    private OWLProperty findRegisteredProperty(String uriencoded) {
        String uri = uriencoded;
        OWLProperty classOWL = null;
        ONodeID nodeid = null;
        try {
            nodeid = new ONodeIDImpl(uri, false);
        } catch (Exception e) {
            // find

            nodeid = new ONodeIDImpl(uri, false);
        }
        if (nodeid != null && this.annotationProperties.containsValue(nodeid)) {

            for (Entry<OWLDataProperty, Property> entry : dataProperties.entrySet()) {
                if (entry.getValue().equals(nodeid)) {
                    classOWL = entry.getKey();
                }
            }

        }

        return classOWL;
    }

    public Set<ONodeID> getClasses(String name) {
        HashSet<ONodeID> result = new HashSet<ONodeID>();
        if (name != null) {
            for (ONodeID nodeId : owl2oclass.values()) {
                if (nodeId != null && nodeId.toString() != null && nodeId.toString().contains(name)) {
                    if (nodeId.toString().equals(name)
                            || nodeId.toString().endsWith("/" + name)
                            || nodeId.toString().endsWith("#" + name)) {
                        result.add(nodeId);
                    } else {
                        // System.out.println();
                    }
                }
            }
        }
        return result;
    }


    public Collection<ONodeID> getClasses() {

        return owl2oclass.values();
    }

    public void addClass(String classURI) {
        OWLOntology o = getOntology(classURI);
        if (o != null) {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLDataFactory factory = manager.getOWLDataFactory();
            OWLClass owlClass = factory.getOWLClass(IRI.create(classURI));
            OWLAxiom a = factory.getOWLDeclarationAxiom(owlClass);
            OntologyUtil.addAxiom(o, a, manager);
            modifiedOntologies.add(o);
            //update structures
            this.entities2Ontologies.put(owlClass, o);
            registerNode(owlClass);
        } else {
            System.err.println("ontology was null");
        }
    }

    public void addClass(String classURI, String superClassURI) {
        OWLClass superClass = this.getOWLClass(superClassURI);
        OWLOntology o = entities2Ontologies.get(superClass);
        if (o != null) {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLDataFactory factory = manager.getOWLDataFactory();
            OWLClass subClass = factory.getOWLClass(IRI.create(classURI));
            OWLAxiom a = factory.getOWLDeclarationAxiom(subClass);
            OntologyUtil.addAxiom(o, a, manager);
            OWLSubClassOfAxiom subaxiom = factory.getOWLSubClassOfAxiom(subClass, superClass);
            OntologyUtil.addAxiom(o, subaxiom, manager);
            //update structures
            registerSubsumption(subaxiom, subClass);
            modifiedOntologies.add(o);
            this.entities2Ontologies.put(subClass, o);
        } else {
            System.err.println("ontology was null");
        }
    }

    private OWLOntology getOntology(String classURI) {
        String possibleURI = classURI;
        if (classURI.contains("/")) {
            URI iri = URI.create(classURI);
            possibleURI = iri.getHost();
//      int lastIndex=classURI.lastIndexOf("/");
//      possibleURI=classURI.substring(0, lastIndex);
        } else if (classURI.contains("#")) {
            possibleURI = classURI.split("#")[0];
        }
        String debugUris = "";
        for (OWLOntology o : ontologies) {
            String uri = o.getOntologyID().getOntologyIRI().toString();
            debugUris += uri + "\n";
            if (uri.contains(possibleURI))
                return o;
        }
        System.err.println("ontology for " + classURI + " not found. possible ontology was: " + possibleURI + ". all ontologies: " + debugUris);
        return null;
    }

    public void addSubclassAxiom(String superClassURI, String subClassURI) {
        OWLClass superClass = this.getOWLClass(superClassURI);
        OWLClass subClass = this.getOWLClass(subClassURI);
        if (subClass != null && superClass != null) {
            OWLOntology o = entities2Ontologies.get(superClass);
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLDataFactory factory = manager.getOWLDataFactory();
            OWLSubClassOfAxiom a = factory.getOWLSubClassOfAxiom(subClass, superClass);
            OntologyUtil.addAxiom(o, a, manager);
            registerSubsumption(a, subClass);
            modifiedOntologies.add(o);
        }
    }

    public void addProperty(String aPropertyURI,
                            String propertyFromTheSameOntologySet, String[] domainClassesURIs,
                            String[] rangeClassesTypes) {
        OWLEntity anotherProperty = this.getEntity(propertyFromTheSameOntologySet);
        OWLOntology o = entities2Ontologies.get(anotherProperty);
        if (o != null) {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLDataFactory factory = manager.getOWLDataFactory();
            OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(aPropertyURI));
            OWLAxiom a = factory.getOWLDeclarationAxiom(prop);
            OntologyUtil.addAxiom(o, a, manager);
//    OWLSubClassOfAxiom subaxiom = factory.getOWLSubClassOfAxiom(subClass,superClass);
//    OntologyUtil.addAxiom(o, subaxiom, manager);
            //update structures
            modifiedOntologies.add(o);
            this.entities2Ontologies.put(prop, o);
            registerObjectProperty(prop);

            //domain and range axioms:
            if (domainClassesURIs != null && domainClassesURIs.length > 0) {
                for (String domainClString : domainClassesURIs) {
                    OWLClassExpression domainClass = this.getOWLClassExpression(domainClString);
                    OWLAxiom domainA = factory.getOWLObjectPropertyDomainAxiom(prop, domainClass);
                    OntologyUtil.addAxiom(o, domainA, manager);
                }
            }

            if (rangeClassesTypes != null && rangeClassesTypes.length > 0) {
                for (String rangeClString : rangeClassesTypes) {
                    OWLClassExpression rangeClass = this.getOWLClassExpression(rangeClString);
                    OWLAxiom rangeA = factory.getOWLObjectPropertyRangeAxiom(prop, rangeClass);
                    OntologyUtil.addAxiom(o, rangeA, manager);
                }
            }


        } else {
            System.err.println("ontology was null");
        }

    }

    public ResourceInfo[] getDomains(String aPropertyURI) {
        ResourceInfo[] result = new ResourceInfo[]{};
        OWLObjectProperty objectProp = getObjectProperty(aPropertyURI);
        if (objectProp != null) {
            Set<OWLClassExpression> domains = objectProp.getDomains(ontologies);
            result = createResourceInfo(domains);
        }
        return result;
    }

    /**
     * @param aPropertyURI
     * @return
     */
    private OWLObjectProperty getObjectProperty(String aPropertyURI) {
        OWLObjectProperty objectProp = null;
        OWLEntity owlProp = this.getEntity(aPropertyURI);
        if (owlProp != null && owlProp instanceof OWLObjectProperty) {
            objectProp = (OWLObjectProperty) owlProp;
            //
        }
        return objectProp;
    }


    public Property[] getInverses(String aPropertyURI) {
        OWLObjectProperty objectProp = getObjectProperty(aPropertyURI);
        if (objectProp != null) {
            Set<OWLObjectPropertyExpression> inv = objectProp.getInverses(ontologies);
            Set<Property> result = new HashSet<Property>();
            for (OWLObjectPropertyExpression op : inv) {
                if (op instanceof OWLObjectProperty) {
                    String name = ((OWLObjectProperty) op).getIRI().toString();
                    if (name != null) {
                        for (Property id : objectProperties.values()) {
                            if (matchesEntity(id.getUri(), name)) {
                                result.add(id);
                            }
                        }
                    }
                }
            }
            return result.toArray(new Property[]{});
        }
        return null;
    }

    public ResourceInfo[] getRangess(String aPropertyURI) {
        ResourceInfo[] result = new ResourceInfo[]{};
        OWLObjectProperty objectProp = getObjectProperty(aPropertyURI);
        if (objectProp != null) {
            Set<OWLClassExpression> ranges = objectProp.getRanges(ontologies);
            result = createResourceInfo(ranges);
        }
        return result;
    }

//
//  /**
//   * @param objectProp
//   * @return
//   */
//  private String calculateName(OWLObjectProperty objectProp) {
//    String result="null";
//    if(objectProp!=null && objectProp.getIRI()!=null && objectProp.getIRI().toString()!=null){
//      result = objectProp.getIRI().toString();
//    }
//    result+="[D:";
//    Set<OWLClassExpression> domains =objectProp.getDomains(ontologies);
////    result+="[";
//    result += getONodeListAsString(domains);
////    result+="]";
//    result+=" R:";
//    Set<OWLClassExpression> ranges =objectProp.getRanges(ontologies);
////    result+="[";
//    result += getONodeListAsString(ranges);
////    result+="]";
//    result+="]";
//    return result;
//  }

    /**
     *
     * @param domains
     * @return
     */
    private ResourceInfo[] createResourceInfo(Set<OWLClassExpression> domains) {
        HashSet<ResourceInfo> result = new HashSet<ResourceInfo>();
        if (domains != null) {
            for (OWLClassExpression dom : domains) {
                if (!dom.isAnonymous()) {
                    result.add(createResourceInfo(dom, OConstants.OWL_CLASS));
                } else {
                    //we just go one step further, we gather all disjunctions in a single list, but leave conjunctions as they are
                    for (OWLClassExpression dom1 : dom.asDisjunctSet()) {
                        if (!dom1.isAnonymous()) {
                            result.add(createResourceInfo(dom1, OConstants.OWL_CLASS));
                        } else {
                            System.out.println("\n there is an annonymous class in domain or range which is a conjunction. it won't be shown properly. Sorry. " + dom1.toString());
                            result.add(createResourceInfo(dom1, OConstants.ANNONYMOUS_CLASS));
                        }
                    }
                }
            }
        }
        return result.toArray(new ResourceInfo[]{});
    }

    /**
     *
     * @param dom1
     * @param type
     * @return
     */
    private ResourceInfo createResourceInfo(
            OWLClassExpression dom1, byte type) {
        ONodeID nodeID = registerNode(dom1);
        ResourceInfo nodeR = new ResourceInfo(nodeID.toString(), type);
        return nodeR;
    }


    public void addQuad(String sub, String pred, String ob, String comment) {
        String quad[] = {sub, pred, ob, comment};
        quads.add(quad);
    }


    public void writeQuads(String filename) {
        String fn = dirName + "/" + filename;
        File f = new File(fn);
        try {
            if (!f.exists()) {
                f.createNewFile();

            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(fn, true));
            for (String[] quad : quads) {
                writer.write(StringUtils.join(quad, " ") + " .\n");
            }
            writer.close();
            quads.clear();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * TODO implement...
     */
    public void addAnnotationToOntology() {

    }

    /**
     * TODO implement...
     */
    public void deleteAnnotationToOntology() {

    }

    public void setClassify(boolean classify) {
      this.classify=classify;
      
    }

    public void setReasoner(Reasoner reasoner) {
      this.reasoner=reasoner;
      
    }

    public boolean isSubClassOf(String theSuperClassURI, String theSubClassURI,
            Closure closure) {
      OWLClass owlsuperClass = getOWLClass(theSuperClassURI);
      if(closure.equals(Closure.TRANSITIVE_CLOSURE) && classify){
       OWLOntology o = entities2Ontologies.get(owlsuperClass);
       return OWLreasoners.get(o).getSubClasses(owlsuperClass, false).containsEntity(getOWLClass(theSubClassURI));        
      }
      return getSuperClasses(getOWLClass(theSubClassURI),  closure).contains(owlsuperClass);
    }

}
