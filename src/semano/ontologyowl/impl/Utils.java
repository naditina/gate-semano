/**
 *
 */
package semano.ontologyowl.impl;


import gate.creole.ontology.AllValuesFromRestriction;
import gate.creole.ontology.CardinalityRestriction;
import gate.creole.ontology.GateOntologyException;
import gate.creole.ontology.HasValueRestriction;
import gate.creole.ontology.MaxCardinalityRestriction;
import gate.creole.ontology.MinCardinalityRestriction;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.OResource;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.RDFProperty;
import gate.creole.ontology.Restriction;
import gate.creole.ontology.SomeValuesFromRestriction;
import gate.util.GateRuntimeException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import semano.ontologyowl.OBNodeIDImpl;
import semano.ontologyowl.OURIImpl;


/**
 * @author niraj
 */
public class Utils {


    static Set<String> seenWarningMessages = new HashSet<String>();

    /**
     * Given required parameters, this method, based on the provided type,
     * returns an appropriate object of a property.
     *
     * @param repositoryID
     * @param ontology
     * @param ontologyService
     * @param uri
     * @param type
     * @return
     */
    public static RDFProperty createOProperty(
            Ontology ontology, OWLOntologyService ontologyService, String uri, byte type) {
        // TODO: get rid of getOResourceFromMap here!
        // Test simply commenting out for now
        RDFProperty prop = null; // = (RDFProperty)ontology.getOResourceFromMap(uri);
        //if(prop != null) return prop;
        switch (type) {
            case OConstants.ANNOTATION_PROPERTY:
                return new AnnotationPropertyImpl(new OURIImpl(uri), ontology,
                        ontologyService);

            case OConstants.RDF_PROPERTY:
                return new RDFPropertyImpl(new OURIImpl(uri), ontology,
                        ontologyService);

            case OConstants.OBJECT_PROPERTY:
                return new ObjectPropertyImpl(new OURIImpl(uri), ontology,
                        ontologyService);

            case OConstants.SYMMETRIC_PROPERTY:
                return new SymmetricPropertyImpl(new OURIImpl(uri), ontology,
                        ontologyService);

            case OConstants.TRANSITIVE_PROPERTY:
                return new TransitivePropertyImpl(new OURIImpl(uri), ontology,
                        ontologyService);

            case OConstants.DATATYPE_PROPERTY:
                return new DatatypePropertyImpl(new OURIImpl(uri), ontology,
                        ontologyService);

            default:
                return null;

        }

    }

    /**
     * Creates a new instance of Ontology Class
     *
     * @param ontology
     * @param owlim
     * @param uri
     * @param classType
     * @return
     */
    public static OClass createOClass(Ontology ontology,
                                      OWLOntologyService owlim, String uri, byte classType) {
        OClass aClass = null; //(OClass)ontology.getOResourceFromMap(uri);
        //if(aClass != null) {
        //return aClass;
        //}
        switch (classType) {
            case OConstants.HAS_VALUE_RESTRICTION:
                return new HasValueRestrictionImpl(new OBNodeIDImpl(uri), ontology,
                        owlim);

            case OConstants.ALL_VALUES_FROM_RESTRICTION:
                return new AllValuesFromRestrictionImpl(new OBNodeIDImpl(uri), ontology,
                        owlim);

            case OConstants.SOME_VALUES_FROM_RESTRICTION:
                return new SomeValuesFromRestrictionImpl(new OBNodeIDImpl(uri),
                        ontology, owlim);

            case OConstants.CARDINALITY_RESTRICTION:
                return new CardinalityRestrictionImpl(new OBNodeIDImpl(uri), ontology,
                        owlim);

            case OConstants.MIN_CARDINALITY_RESTRICTION:
                return new MinCardinalityRestrictionImpl(new OBNodeIDImpl(uri),
                        ontology, owlim);

            case OConstants.MAX_CARDINALITY_RESTRICTION:
                return new MaxCardinalityRestrictionImpl(new OBNodeIDImpl(uri),
                        ontology, owlim);

            case OConstants.ANNONYMOUS_CLASS:
                return new AnonymousClassImpl(new OBNodeIDImpl(uri),
                        ontology, owlim);

            default:
                return new OClassImpl(new OURIImpl(uri), ontology,
                        owlim);

        }

    }

    public static String getRestrictionName(byte classType) {
        switch (classType) {
            case OConstants.HAS_VALUE_RESTRICTION:
                return OntologyUtilities.HASVALUE;
            case OConstants.ALL_VALUES_FROM_RESTRICTION:
                return OntologyUtilities.ALLVALUESFROM;
            case OConstants.SOME_VALUES_FROM_RESTRICTION:
                return OntologyUtilities.SOMEVALUESFROM;
            case OConstants.CARDINALITY_RESTRICTION:
                return OntologyUtilities.CARDINALITY;
            case OConstants.MIN_CARDINALITY_RESTRICTION:
                return OntologyUtilities.MINCARDINALITY;
            case OConstants.MAX_CARDINALITY_RESTRICTION:
                return OntologyUtilities.MAXCARDINALITY;
            case OConstants.ANNONYMOUS_CLASS:
                return "Annonymous";
            default:
                return "Unknown";
        }

    }

    public static String getRestrictionName(Restriction res) {
        String className = "Unknown";
        if (res instanceof HasValueRestriction) {
            className = OntologyUtilities.HASVALUE;
        } else if (res instanceof AllValuesFromRestriction) {
            className = OntologyUtilities.ALLVALUESFROM;
        } else if (res instanceof SomeValuesFromRestriction) {
            className = OntologyUtilities.SOMEVALUESFROM;
        } else if (res instanceof CardinalityRestriction) {
            className = OntologyUtilities.CARDINALITY;
        } else if (res instanceof MinCardinalityRestriction) {
            className = OntologyUtilities.MINCARDINALITY;
        } else if (res instanceof MaxCardinalityRestriction) {
            className = OntologyUtilities.MAXCARDINALITY;
        } else if (res instanceof AnonymousClassImpl) {
            className = "Annonymous";
        }
        return className;
    }


    /**
     * Creates a new instance of Ontology Instance
     *
     * @param repositoryID
     * @param ontology
     * @param ontologyService
     * @param uri
     * @return
     */
    public static OInstance createOInstance(
            Ontology ontology, OWLOntologyService owlim, String uri) {
        OResource aResource = null; //= ontology.getOResourceFromMap(uri);
        if (aResource instanceof OInstance || aResource == null) {
            OInstance anInstance = (OInstance) aResource;
            if (anInstance != null) return anInstance;
            anInstance = new OInstanceImpl(ontology.createOURI(uri), ontology,
                    owlim);
            //ontology.addOResourceToMap(uri, anInstance);
            return anInstance;
        } else {
            throw new GateOntologyException("Expecting " + uri +
                    " to be an instance but it is a \"" +
                    aResource.getClass().getCanonicalName() + "\" instead!");
        }
    }


    public static boolean hasSystemNameSpace(String uri) {
        if (uri.startsWith("http://www.w3.org/2002/07/owl#"))
            return true;
        else if (uri.startsWith("http://www.w3.org/2001/XMLSchema#"))
            return true;
        else if (uri.startsWith("http://www.w3.org/2000/01/rdf-schema#"))
            return true;
        else if (uri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#"))
            return true;
        else return false;
    }

    /**
     * Utility method that shows warning to the user.
     *
     * @param warningMsg - message to be displayed to the user
     */
    public static void warning(String warningMsg) {
        System.err.println("WARNING :" + warningMsg);
    }

    public static void warnDeprecation(String methodName) {
        warnOnce("Method " + methodName + " is deprecated and should not be used any more!");
    }

    public static void warnOnce(String message) {
        if (!seenWarningMessages.contains(message)) {
            System.err.println(message);
            new GateOntologyException().printStackTrace();
            seenWarningMessages.add(message);
        }
    }

    /**
     * Utility method that throws a GateRuntimeException to the user.
     *
     * @param warningMsg - message to be displayed to the user
     */
    public static void error(String errorMsg) {
        throw new GateRuntimeException("ERROR :" + errorMsg);
    }


    /**
     * @param filename
     * @param strings
     * @param overwrite
     */
    public static void writeStringToFile(String filename, String string,
                                         boolean overwrite) {
        File destFile = new File(filename);
        try {
            if (destFile.exists()) {
                if (overwrite)
                    destFile.delete();
                else return;
            }
            destFile.createNewFile();
            Writer outputStream = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(destFile), "UTF-8"));

            outputStream.write(string);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFileAsString(String fileName, String charsetName)
            throws java.io.IOException {
        java.io.InputStream is = new java.io.FileInputStream(fileName);
        try {
            final int bufsize = 4096;
            int available = is.available();
            byte data[] = new byte[available < bufsize ? bufsize : available];
            int used = 0;
            while (true) {
                if (data.length - used < bufsize) {
                    byte newData[] = new byte[data.length << 1];
                    System.arraycopy(data, 0, newData, 0, used);
                    data = newData;
                }
                int got = is.read(data, used, data.length - used);
                if (got <= 0) break;
                used += got;
            }
            return charsetName != null
                    ? new String(data, 0, used, charsetName)
                    : new String(data, 0, used);
        } finally {
            is.close();
        }
    }


}
