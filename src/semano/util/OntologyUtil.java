package semano.util;

import gate.Annotation;
import gate.FeatureMap;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants.Closure;
import gate.creole.ontology.OResource;
import gate.creole.ontology.ObjectProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import semano.ontologyowl.AnnotationValue;
import semano.ontoviewer.OntologyAnnotation;


public class OntologyUtil {


    public static void addAxiom(OWLOntology ontology, OWLAxiom undecidedA,
                                OWLOntologyManager manager) {
        insertAxiomIntoOntology(manager,
                ontology, undecidedA);

    }


    /**
     * @param manager
     * @param ontology
     * @param newOwlAxiom
     */
    public static void insertAxiomIntoOntology(
            OWLOntologyManager manager,
            OWLOntology ontology, OWLAxiom newOwlAxiom) {
        try {
            if (newOwlAxiom != null) {
                AddAxiom addAxiom = new AddAxiom(ontology,
                        newOwlAxiom);
                if (addAxiom != null) {
                    manager.applyChange(addAxiom);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param owlOntology
     * @param entity
     * @return
     */
    public static OWLEntity extractEntityWithSubstring(OWLOntology owlOntology, String entity) {
        if (owlOntology != null) {
            Set<OWLEntity> entities = owlOntology.getSignature();
            Iterator<OWLEntity> it = entities.iterator();
            while (it.hasNext()) {
                OWLEntity next = it.next();
                if (next.toString().toLowerCase().replaceAll("_", "").contains(
                        entity.toLowerCase().replaceAll("_", ""))) {
                    return next;
                }
                Set<OWLAnnotation> annotations = next.getAnnotations(owlOntology);
                Iterator<OWLAnnotation> itAnno = annotations.iterator();
                while (itAnno.hasNext()) {
                    OWLAnnotation axiom = itAnno.next();
                    if (axiom.getValue().toString().toLowerCase().replaceAll("_", "")
                            .contains(entity.toLowerCase().replaceAll("_", ""))) {
                        if (axiom.getProperty().isLabel()) {
                            return next;
                        }
                    }
                }
            }
        }
        return null;
    }


    /**
     * Given an <tt>entityName</tt> and a collection of <tt>axioms</tt> returns the type
     * of the axiom with the name matching entity name.
     * @param entityName the name to get the type for.
     * @param axioms the collection of axioms to search in.
     * @return
     */
    public static String getType(String entityName, Collection<OWLAxiom> axioms) {
        for (OWLAxiom axiom : axioms) {
            Set<OWLEntity> entities = axiom.getSignature();
            for (OWLEntity entity : entities) {
                if (entity.toString().equals(entityName)) {
                    return entity.getClass().getSimpleName();
                }
            }
        }
        return "";
    }

    /**
     * @param uri
     * @param dataDir
     * @param urlFormat
     * @param manager
     * @return
     */
    public static OWLOntology loadOntology(String uri, String dataDir,
                                           boolean urlFormat, OWLOntologyManager manager) throws OWLOntologyCreationException, FileNotFoundException {
        if (!urlFormat) {
            return OntologyUtil.loadOntology(uri, dataDir);
        }
        return loadOntology(manager, dataDir + "/" + uri);
    }

    /**
     * @param manager
     * @param filename
     * @return
     */
    public static OWLOntology loadOntology(OWLOntologyManager manager, String filename) throws OWLOntologyCreationException, FileNotFoundException {
        OWLOntology ontology = null;
        File sourceFile = new File(filename);
        if (sourceFile.exists()) {
            FileInputStream source = new FileInputStream(sourceFile);
            ontology = manager.loadOntologyFromOntologyDocument(source);
        }
        return ontology;
    }

    /**
     * @param uri
     * @param dataDir
     * @return
     */
    public static OWLOntology loadOntology(String uri, String dataDir) throws OWLOntologyCreationException, FileNotFoundException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = loadOntology(uri, dataDir, true, manager);
        return ontology;
    }



    public static void saveOntology(OWLOntologyManager manager, OWLOntology o, String filename) {
        File f = new File(filename);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            manager.saveOntology(o, new FileOutputStream(f));
        } catch (OWLOntologyStorageException|IOException e) {
            e.printStackTrace();
        }

    }

    public static OWLOntology createOntology(Collection<OWLAxiom> axioms,
                                             OWLOntologyManager manager, String physicalUri) {
        IRI newBaseUri = IRI
                .create(physicalUri);

        IRI physicalURI = IRI.create(physicalUri);
        SimpleIRIMapper mapper = new SimpleIRIMapper(newBaseUri, physicalURI);
        manager.addIRIMapper(mapper);
        OWLOntology ontology = null;
        try {
            ontology = manager.createOntology(newBaseUri);
            for (OWLAxiom a : axioms) {
                insertAxiomIntoOntology(manager,
                        ontology, a);
            }
        } catch (OWLOntologyAlreadyExistsException e) {
            ontology = manager.getOntology(newBaseUri);
            manager.removeAxioms(ontology, ontology.getAxioms());
            for (OWLAxiom a : axioms) {
                insertAxiomIntoOntology(manager,
                        ontology, a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ontology;
    }

    public static String convertToProperUriString(String text) {
        String result = convertToValidLabel(text);
        result = result.replace(" ", "_");
        return result;
    }

    /**
     * replaces and removes all characters that are unlikely to be found
     * in text with the remaining string
     *
     * @param name
     * @return
     */
    public static String convertToValidLabel(String name) {

        String result = name.replaceAll("\\|\"", "");
        result = splitCamelCase(result);
        result = result.replaceAll("\\\\", "");
        result = result.replaceAll("[^a-zA-Z0-9]", " ");
        result = result.replace("  ", " ");
        return result;
    }
    
    static String splitCamelCase(String s) {
      return s.replaceAll(
         String.format("%s|%s|%s",
            "(?<=[A-Z])(?=[A-Z][a-z])",
            "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])"
         ),
         " "
      );
   }

    /**
     * @param classes TODO
     */
    public static Set<OResource> getTransitiveClosureOfSubclasses(Set<OResource> classes) {
        Set<OResource> classesAll = new HashSet<OResource>();
        for (OResource clas : classes) {
            if (clas instanceof OClass) {
                classesAll.add(clas);
                OClass cl = (OClass) clas;
                classesAll.addAll(cl.getSubClasses(Closure.TRANSITIVE_CLOSURE));
            }
        }
        return classesAll;
    }

    /**
     * @param entity
     * @param entities TODO
     */
    public static boolean isContainedInTransitiveClosure(String entity, Set<OResource> entities) {
        Set<OResource> entitiesTC = getTransitiveClosureOfSubclasses(entities);
        for (OResource e : entitiesTC) {
            if (e.toString().equals(entity)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkDomain(OResource entity,
                                      Annotation annotation) {
        if (annotation != null && OntologyAnnotation.isSourceAClass(annotation)) {
            String r = OntologyAnnotation.getSourceFeatureValue(annotation);

            if (entity instanceof ObjectProperty) {
                ObjectProperty prop = (ObjectProperty) entity;
                Set<OResource> domain = prop.getDomain();
                return isContainedInTransitiveClosure(r, domain);
            }
        }
        return false;
    }

    public static boolean checkRange(OResource entity, FeatureMap featureMap,
                                     Annotation da) {
        if (da != null && OntologyAnnotation.isSourceAClass(da)) {
            String r = OntologyAnnotation.getSourceFeatureValue(da);
            if (entity instanceof ObjectProperty) {
                ObjectProperty prop = (ObjectProperty) entity;
                Set<OResource> ranges = prop.getRange();
                return isContainedInTransitiveClosure(r, ranges);
            }
        }
        return false;
    }

    /**
     * @param feature
     * @param annotationValue
     * @return
     */
    public static boolean iriEquals(
            semano.ontoviewer.AnnotationMetaData feature,
            AnnotationValue annotationValue) {
        String annotationProperty1 = annotationValue.getAnnotationPropertyObject()
                .getUri();
        String annotationProperty2 = OntologyAnnotation
                .getSimplePropertyNameForAnnotationType(feature);
        return (annotationProperty1.endsWith(annotationProperty2)
                || annotationProperty1.equals(annotationProperty2)
                || annotationProperty2.endsWith(annotationProperty1) || annotationProperty2
                .equals(annotationProperty1));
    }


    public static String extractEntityName(String uri) {
        int lastIndexOf = separatorPosition(uri);

        String clas = uri.substring(lastIndexOf + 1);
        return clas;
    }

    private static int separatorPosition(String uri) {
        char replacementChar = '/';
        if (uri.contains("#"))
            replacementChar = '#';
        int lastIndexOf = uri.lastIndexOf(replacementChar);
        return lastIndexOf;
    }

    public static String extractOntology(String uri) {
        int lastIndexOf = separatorPosition(uri);
        String ontology = uri.substring(0, lastIndexOf + 1);
        return ontology;
    }

    public static void main(String[] args){
      String[] tests = {
              "lowercase as sdsd",        // [lowercase]
              "Class",            // [Class]
              "MyClass sda",          // [My Class]
              "HTML",             // [HTML]
              "PDFLoader",        // [PDF Loader]
              "AString",          // [A String]
              "SimpleXMLParser dsfsd mSmSm asfdasfda sDGSgsSsSsSsSsSs",  // [Simple XML Parser]
              "GL11Version",      // [GL 11 Version]
              "99Bottles",        // [99 Bottles]
              "May5",             // [May 5]
              "BFG9000",          // [BFG 9000]
          };
          for (String test : tests) {
              System.out.println("[" + splitCamelCase(test) + "]");
          }
    }
}
