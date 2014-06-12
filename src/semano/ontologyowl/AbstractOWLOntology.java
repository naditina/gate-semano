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
 *  $Id: AbstractOntologyImplSesame.java 11598 2009-10-13 13:44:17Z johann_p $
 */
package semano.ontologyowl;

import gate.creole.ontology.GateOntologyException;
import gate.creole.ontology.OBNodeID;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OConstants.OntologyFormat;
import gate.creole.ontology.OConstants.QueryLanguage;
import gate.creole.ontology.OURI;
import gate.creole.ontology.OntologyBooleanQuery;
import gate.creole.ontology.OntologyTupleQuery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import semano.ontologyowl.impl.OWLOntology;
import semano.ontologyowl.impl.OWLOntologyServiceImpl;
import semano.ontologyowl.impl.Utils;


/**
 * Extension of <tt>OWLOntology</tt> specifying methods to read and write ontological data.
 *
 * @see
 * @author Nadejda Nikitina
 */
public abstract class AbstractOWLOntology extends OWLOntology {


    /**
     *
     * @param theURL the <tt>URL</tt> of the ontology to be read
     * @param baseURI
     * @param format The <tt>OntologyFormat</tt> of the ontology to be read.
     * @param asImport
     */
    public void readOntologyData(java.net.URL theURL, String baseURI,
                                 OConstants.OntologyFormat format, boolean asImport) throws OWLOntologyCreationException, FileNotFoundException {
        File file = null;
        try {
            file = new File(theURL.toURI());
        } catch (URISyntaxException e) {
            System.err.println(e.getMessage());
        }

        if (file != null) {
            if (!file.isDirectory()) {
                readOntologies(theURL, baseURI, format, asImport, file);
            } else {
                // read ontologies in this directory
                Set<String> filenames = new HashSet<>();
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile()) {
                            filenames.add(f.toString());
                        }
                    }
                }
                readOntologies(filenames, baseURI, asImport, file.getAbsolutePath());
            }
        }
    }

    private void readOntologies(Set<String> filenames, String baseURI,
                                boolean asImport, String dirName) throws OWLOntologyCreationException, FileNotFoundException {
        boolean isBaseURIset = true;
        if (baseURI == null || baseURI.length() == 0) {
            isBaseURIset = false;
            baseURI = OConstants.ONTOLOGY_DEFAULT_BASE_URI;
        }
        String defaultUri = ((OWLOntologyServiceImpl) ontologyService).readOntologyData(filenames, dirName);
        setDefaultNameSpace(defaultUri);
        if (!asImport) {
            if (isBaseURIset && getDefaultNameSpace() == null) {
                setDefaultNameSpace(baseURI);
            }
            Set<OURI> us = addOntologyURIs();
            int n = us.size();
            if (n != 1) {
                Utils.warning("Found 0 or more than 1 ontology in " + filenames.toString());
            } else {
                setDefaultNameSpaceFromOntologyURI();
            }
        }

    }

    /**
     * @param theURL
     * @param baseURI
     * @param format
     * @param asImport
     * @param file
     */
    private void readOntologies(java.net.URL theURL, String baseURI,
                                OConstants.OntologyFormat format, boolean asImport, File file) throws OWLOntologyCreationException, FileNotFoundException {
        boolean isBaseURIset = true;
        if (baseURI == null || baseURI.length() == 0) {
            isBaseURIset = false;
            baseURI = OConstants.ONTOLOGY_DEFAULT_BASE_URI;
        }
        ((OWLOntologyServiceImpl) ontologyService).readOntologyData(file,
                baseURI, format, asImport);
        if (!asImport) {
            if (isBaseURIset && getDefaultNameSpace() == null) {
                setDefaultNameSpace(baseURI);
            }
            Set<OURI> us = addOntologyURIs();
            int n = us.size();
            if (n == 0) {
                Utils.warning("No ontology URI found for ontology loaded from " + theURL);
            } else if (n > 1) {
//        Utils.warning("More than one("+n+") ontology URI found for ontology loaded from "+theURL+": "+us);
            } else {
                setDefaultNameSpaceFromOntologyURI();
            }
        }
    }

    public void readOntologyData(File selectedFile, String baseURI,
                                 OConstants.OntologyFormat format, boolean asImport) throws OWLOntologyCreationException, FileNotFoundException {
        boolean isBaseURIset = true;
        if (baseURI == null || baseURI.length() == 0) {
            isBaseURIset = false;
            baseURI = OConstants.ONTOLOGY_DEFAULT_BASE_URI;
        }
        ((OWLOntologyServiceImpl) ontologyService).readOntologyData(selectedFile,
                baseURI, format, asImport);
        if (!asImport) {
            if (isBaseURIset && getDefaultNameSpace() == null) {
                setDefaultNameSpace(baseURI);
            }
            Set<OURI> us = addOntologyURIs();
            int n = us.size();
            if (n == 0) {
                Utils.warning("No ontology URI found for ontology loaded from " + selectedFile.getAbsolutePath());
            } else if (n > 1) {
//        Utils.warning("More than one("+n+") ontology URI found for ontology loaded from "+selectedFile.getAbsolutePath()+": "+us);
            } else {
                setDefaultNameSpaceFromOntologyURI();
            }
        }
    }

    public void readOntologyData(Reader in, String baseURI, OntologyFormat format,
                                 boolean asImport) {
        boolean isBaseURIset = true;
        if (baseURI == null || baseURI.length() == 0) {
            isBaseURIset = false;
            baseURI = OConstants.ONTOLOGY_DEFAULT_BASE_URI;
        }
        ((OWLOntologyServiceImpl) ontologyService).readOntologyData(in,
                baseURI, format, asImport);
        if (!asImport) {
            if (isBaseURIset && getDefaultNameSpace() == null) {
                setDefaultNameSpace(baseURI);
            }
            Set<OURI> us = addOntologyURIs();
            int n = us.size();
            if (n == 0) {
                Utils.warning("No ontology URI found for ontology loaded");
            } else if (n > 1) {
//        Utils.warning("More than one("+n+") ontology URI found for ontology loaded: "+us);
            } else {
                setDefaultNameSpaceFromOntologyURI();
            }
        }
    }

    public void readOntologyData(InputStream in, String baseURI,
                                 OntologyFormat format, boolean asImport) {
        boolean isBaseURIset = true;
        if (baseURI == null || baseURI.length() == 0) {
            isBaseURIset = false;
            baseURI = OConstants.ONTOLOGY_DEFAULT_BASE_URI;
        }
        ((OWLOntologyServiceImpl) ontologyService).readOntologyData(in,
                baseURI, format, asImport);
        if (!asImport) {
            if (isBaseURIset && getDefaultNameSpace() == null) {
                setDefaultNameSpace(baseURI);
            }
            Set<OURI> us = addOntologyURIs();
            int n = us.size();
            if (n == 0) {
                Utils.warning("No ontology URI found for ontology loaded");
            } else if (n > 1) {
//        Utils.warning("More than one("+n+") ontology URI found for ontology loaded: "+us);
            } else {
                setDefaultNameSpaceFromOntologyURI();
            }
        }
    }

    private void setDefaultNameSpaceFromOntologyURI() {
        if (getDefaultNameSpace() == null && getOntologyURIs().size() == 1) {
            String uri = getOntologyURIs().get(0).toString();
            if (!uri.endsWith("#")) {
                uri = uri + "#";
            }
            setDefaultNameSpace(uri);
        }
    }

    public void writeOntologyData(File selectedFile,
                                  OConstants.OntologyFormat format, boolean includeImports) {
        FileWriter fw;
        try {
            fw = new FileWriter(selectedFile);
        } catch (IOException ex) {
            throw new GateOntologyException("Could not open writer for file " +
                    selectedFile.getAbsolutePath(), ex);
        }
        ((OWLOntologyServiceImpl) ontologyService).writeOntologyData(fw,
                format, includeImports);
    }

    public void writeOntologyData(OutputStream out, OntologyFormat format,
                                  boolean includeImports) {
        ((OWLOntologyServiceImpl) ontologyService).writeOntologyData(out,
                format, includeImports);
    }

    public void writeOntologyData(Writer out, OntologyFormat format,
                                  boolean includeImports) {
        ((OWLOntologyServiceImpl) ontologyService).writeOntologyData(out,
                format, includeImports);
    }

    /**
     * Load the system imports into a repository that does not have them
     * loaded yet.
     */
    public void loadSystemImports() {
        File pluginDir = getPluginDir();
        File owlFile = new File(new File(pluginDir, "config"), "owl.rdfs");
        ((OWLOntologyServiceImpl) ontologyService).loadSystemImport(owlFile,
                "http://www.w3.org/2002/07/owl#", OConstants.OntologyFormat.RDFXML);
        File rdfsFile = new File(new File(pluginDir, "config"), "rdf-schema.rdf");
        ((OWLOntologyServiceImpl) ontologyService).loadSystemImport(rdfsFile,
                "http://www.w3.org/2000/01/rdf-schema#", OConstants.OntologyFormat.RDFXML);
    }

    @Override
    public void cleanOntology() {
        super.cleanOntology();
        loadSystemImports();
    }

    public OURI createOURI(String uriString) {
        return new OURIImpl(uriString);
    }

    public OURI createOURIForName(String resourceName) {
        // TODO: check and normalize resourceName
        String baseURI = getDefaultNameSpace();
        if (baseURI == null) {
            throw new GateOntologyException("Cannot create OURI, no system name space (base URI) set");
        }
        return new OURIImpl(baseURI + resourceName);
    }

    public OURI createOURIForName(String resourceName, String baseURI) {
        // TODO: check and normalize resource name, maybe also URI, or do the
        // latter in the OURI constructor?
        return new OURIImpl(baseURI + resourceName);
    }

    public OURI generateOURI(String resourceName) {
        String baseURI = getDefaultNameSpace();
        if (baseURI == null) {
            throw new GateOntologyException("No default name space set, cannot generate OURI");
        }
        return generateOURI(resourceName, baseURI);
    }

    public OURI generateOURI(String resourceName, String baseURI) {
        if (resourceName == null) {
            resourceName = "";
        }
        // TODO: check and normalize resource name so it is a valid part of an IRI

        // now append our generated suffix
        resourceName =
                resourceName +
                        Long.toString(System.currentTimeMillis(), 36) +
                        Integer.toString(Math.abs(randomGenerator.nextInt(1296)), 36);
        OURI uri = null;
        while (true) {
            uri = createOURIForName(resourceName);
            if (!((OWLOntologyServiceImpl) ontologyService).containsURI(uri)) {
                break;
            }
        }
        return uri;
    }

    public OBNodeID createOBNodeID(String id) {
        return new OBNodeIDImpl(id);
    }


    public OntologyBooleanQuery createBooleanQuery(String theQuery,
                                                   QueryLanguage queryLanguage) {
        // TODO Auto-generated method stub
        return null;
    }

    public OntologyTupleQuery createTupleQuery(String theQuery,
                                               QueryLanguage queryLanguage) {
        // TODO Auto-generated method stub
        return null;
    }

    public void createAnnotation(String decodedClassName, String annotationproperty, String value) {
        ((OWLOntologyServiceImpl) ontologyService).addAnnotationPropertyValue(decodedClassName, annotationproperty, value, "en");

    }

    public void writeOntologyData() {
        ((OWLOntologyServiceImpl) ontologyService).writeOntologyData();

    }


    /**
     * @return the author
     */
    public String getAuthor() {
        return ((OWLOntologyServiceImpl) ontologyService).getAuthor();
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return ((OWLOntologyServiceImpl) ontologyService).getLanguage();
    }


}
