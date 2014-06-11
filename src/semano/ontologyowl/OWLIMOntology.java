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
 *  $Id: OWLIMOntology.java 11600 2009-10-13 17:13:22Z johann_p $
 */
package semano.ontologyowl;

import gate.Gate;
import gate.Resource;
import gate.creole.ResourceData;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OConstants.OntologyFormat;
import gate.creole.ontology.OntologyTripleStore;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import semano.Reasoning;
import semano.ontologyowl.impl.OWLOntologyServiceImpl;

import java.io.File;
import java.io.IOException;
import java.net.URL;


/**
 * This ontology resource allows the creation of an ontology. The ontology can be optionally loaded with
 * an initial ontology at creation time and can be created as either
 * persistent or not persistent. If the ontology is created with the
 * persistent parameter set to true, the directory created to
 * hold all the ontology data will be kept after the ontology is closed.
 *
 * @author Nadejda Nikitina
 */
@CreoleResource(
        name = "OWL API Ontology",
        interfaceName = "gate.creole.ontology.Ontology",
        comment = "OWL API Ontology",
        icon = "ontology",
        helpURL = "http://gate.ac.uk/userguide/sec:ontologies:ontoplugin:owlim")
public class OWLIMOntology
        extends OntologyResource {

    private static final long serialVersionUID = 1L;


//    @CreoleParameter(comment = "", defaultValue = "nadejda")
//    public void setAuthor(String newauthor) {
//        author = newauthor;
//    }
//
//    public String getAuthor() {
//        return author;
//    }
//
//    protected String author = "";
//
//
//    @CreoleParameter(comment = "", defaultValue = "en")
//    public void setLanguage(String newlanguage) {
//        language = newlanguage;
//    }
//
//    public String getLanguage() {
//        return language;
//    }
//
//    protected String language = "en";

    @CreoleParameter(comment = "", disjunction = "url")
    public void setRdfXmlURL(URL theURL) {
        rdfXmlURL = theURL;
    }
    public URL getRdfXmlURL() {
        return rdfXmlURL;
    }
    protected URL rdfXmlURL;

    
    
    @CreoleParameter(comment = "reasoner", defaultValue = "hermit")
    public void setReasoner(Reasoning.Reasoner reasoner) {
      this.reasoner = reasoner;
    }
    public Reasoning.Reasoner getReasoner() {
        return reasoner;
    }
    protected Reasoning.Reasoner reasoner;


    @CreoleParameter(comment = "classify ontology on loading?", defaultValue = "true")
    public void setClassifyOnLoad(boolean classify) {
      this.classify = classify;
    }
    public boolean getClassifyOnLoad() {
        return classify;
    }
    protected boolean classify;

//
//  @CreoleParameter(comment="")
//  public void setN3URL(CreoleRuleStore ruleStore) {
//    this.ruleStore = ruleStore;
//  }
//  public CreoleRuleStore getN3URL() {
//    return ruleStore;
//  }
//  protected CreoleRuleStore ruleStore;

    //  @Optional
//  @CreoleParameter(comment="",disjunction="url")
//  public void setNtriplesURL(URL theURL) {
//    ntriplesURL = theURL;
//  }
//  public URL getNtriplesURL() {
//    return ntriplesURL;
//  }
//  protected URL ntriplesURL;
//
//  @Optional
//  @CreoleParameter(comment="",disjunction="url")
//  public void setTurtleURL(URL theURL) {
//    turtleURL = theURL;
//  }
//  public URL getTurtleURL() {
//    return turtleURL;
//  }
//  protected URL turtleURL;
//
//  @Optional
//  @CreoleParameter(comment="Directory that should contain the repository director")
//  /**
//   * Set the name of the directory in which the directory "storage-folder"
//   * which contains the ontology repository data will be created.
//   * If the directory does not exist but its parent exists, it will be 
//   * created.
//   */
//  public void setDataDirectoryURL(URL dataDirectoryURL) {
//    this.dataDirectoryURL = dataDirectoryURL;
//  }
//  public URL getDataDirectoryURL() {
//    return dataDirectoryURL;
//  }
//  protected URL dataDirectoryURL;
//
    @Optional
    @CreoleParameter(
            comment = "Ontology base URI, default is http://gate.ac.uk/dummybaseuri#"
    )
    public void setBaseURI(String theURI) {
        baseURI = theURI;
    }

    public String getBaseURI() {
        return baseURI;
    }

    protected String baseURI;
//
//  @Optional
//  @CreoleParameter(
//      comment="The URL of a file containing mappings between ontology import URIs and URLs or blank"
//      )
//  public void setMappingsURL(URL theMappings) {
//    mappingsURL = theMappings;
//  }
//  public URL getMappingsURL() {
//    return mappingsURL;
//  }
//  protected URL mappingsURL;
//
//  @CreoleParameter(
//      comment="If the ontology imports specified in the ontology should get automatically loaded",
//      defaultValue = "true")
//  public void setLoadImports(Boolean doit) {
//    loadImports = doit;
//  }
//  public Boolean getLoadImports() {
//    return loadImports;
//  }
//  protected Boolean loadImports;

  /* this does not seem to work?
  @CreoleParameter(
      comment="The format of the ontology file to load",
      defaultValue="rdfxml")
  public void setOntologyFileFormat(OntologyFormat theFormat) {
    ontologyFileFormat = theFormat;
  }
  public OntologyFormat getOntologyFileFormat() {
    return ontologyFileFormat;
  }
  private OntologyFormat ontologyFileFormat;
   * */

    private File dataDirectory;
    private File storageFolderDir;

    protected Logger logger;

    /**
     * Constructor
     */
    public OWLIMOntology() {
        super();
        logger = Logger.getLogger(this.getClass().getName());
    }

    /**
     * Initialises this resource, and returns it.
     *
     * @return
     * @throws ResourceInstantiationException
     */
    @Override
    public Resource init() throws ResourceInstantiationException {
        load();
        Gate.getCreoleRegister().addCreoleListener(this);
        return this;
    }

    /**
     * Loads this ontology.
     *
     * @throws ResourceInstantiationException
     */
    public void load() throws ResourceInstantiationException {
        try {
            logger.debug("Running load");


            // determine ontology file and format to load, if any.
            OntologyFormat ontologyFormat = OConstants.OntologyFormat.RDFXML;
            if (rdfXmlURL != null && rdfXmlURL.toString().trim().length() > 0) {
                ontologyURL = rdfXmlURL;
            } else {
                ontologyURL = null;
                ontologyFormat = OConstants.OntologyFormat.RDFXML;
            }
            logger.debug("creating ontology resource");
            String ontoURLString = ontologyURL == null ? "" : ontologyURL
                    .toExternalForm();

            logger.debug("Determined url and format: " + ontoURLString + "/" + ontologyFormat);
            // determine the URL to the plugin directory
            ResourceData myResourceData =
                    Gate.getCreoleRegister().get(this.getClass().getName());
            URL creoleXml = myResourceData.getXmlFileUrl();
            logger.debug("creoleXML is " + creoleXml);
            getPluginDir();

            URL dataDirectoryURL = null;
            // determine where to store the repository data
            if (dataDirectoryURL == null) {
                // use the system tmp
                String tmplocation = System.getProperty("run.java.io.tmpdir");
                logger.debug("run.java.io.tmpdir is " + tmplocation);
                if (tmplocation == null) {
                    tmplocation = System.getProperty("java.io.tmpdir");
                    logger.debug("java.io.tmpdir is " + tmplocation);
                }
                if (tmplocation != null) {
                    dataDirectoryURL = new File(tmplocation).toURI().toURL();
                }
            }
            if (dataDirectoryURL == null) {
                throw new ResourceInstantiationException(
                        "Could not determine location for the data directory");
            }
            logger.debug("dataDirectoryURL is now " + dataDirectoryURL);
            if (!dataDirectoryURL.getProtocol().equals("file")) {
                throw new ResourceInstantiationException("dataDirectoryURL must be a local file");
            }
            dataDirectory = new File(dataDirectoryURL.toURI());
            if (!dataDirectory.exists()) {
                if (!dataDirectory.mkdir()) {
                    throw new ResourceInstantiationException(
                            "Could not create data directory " + dataDirectoryURL);
                }
            } else {
                if (!dataDirectory.isDirectory()) {
                    throw new ResourceInstantiationException(
                            "Not a directory: " + dataDirectory.getAbsolutePath());
                }
            }
            String storageFolderName = "GATE_OWLOntology_" +
                    Long.toString(System.currentTimeMillis(), 36);
            storageFolderDir = new File(dataDirectory, storageFolderName);
            storageFolderDir.mkdir();
            // TODO: replace by logger.info
            System.out.println("Storing data in folder: " + storageFolderDir.getAbsolutePath());

            // get the configuration file , check if the system import files
            // are there
            File configDir = new File(pluginDir, "config");
            File repoConfig;

            // This was how it was done with the unmanaged repository: use a
            // persist configuration when the persist parameter is true.
            //if(getPersistent()) {
            //  repoConfig = new File(configDir,"owlim-max-nopartial-persist.ttl");
            //} else {
            //  repoConfig = new File(configDir,"owlim-max-nopartial.ttl");
            //}

            // with the managed repository always use the same config (not decided
            // yet wheter to use the persist variation
            repoConfig = new File(configDir, "owlim-max-nopartial.ttl");

            logger.debug("Using config " + repoConfig.getAbsolutePath());
            System.out.println("Using config file: " + repoConfig.getAbsolutePath());

            if (!repoConfig.exists()) {
                throw new ResourceInstantiationException(
                        "Repository config file not found " + repoConfig.getAbsolutePath());
            }
            File owlDefFile = new File(configDir, "owl.rdfs");
            if (!owlDefFile.exists()) {
                throw new ResourceInstantiationException(
                        "OWL definition file not found " + owlDefFile.getAbsolutePath());
            }
            File rdfsDefFile = new File(configDir, "rdf-schema.rdf");
            if (!rdfsDefFile.exists()) {
                throw new ResourceInstantiationException(
                        "RDFS definition file not found " + rdfsDefFile.getAbsolutePath());
            }


            OWLOntologyServiceImpl oService = new OWLOntologyServiceImpl(this);
            oService.setReasoner(reasoner);
            oService.setClassify(classify);

            // create a managed repository
            oService.createManagedRepository(
                    storageFolderDir.toURI().toURL(),
                    "owlim3", repoConfig.toURI().toURL());
            ontologyService = oService;

            logger.debug("Repository created");

            loadSystemImports();

            logger.debug("System imports done");

            if (ontologyURL != null) {
                logger.debug("Loading ontology data from " + ontologyURL +
                        " using format " + ontologyFormat + " and base URI " + getBaseURI());
                readOntologyData(ontologyURL, getBaseURI(), ontologyFormat, false);
                logger.debug("default name space after loading: " + getDefaultNameSpace());
                System.out.println("Default name space is " + getDefaultNameSpace());
                logger.debug("Ontology data loaded");
//        if(loadImports) {
//          Map<String,String> mappings = null;
//          if(mappingsURL != null &&  // !mappingsURL.toString().isEmpty()
//             (mappingsURL.toString().length() != 0)
//            ) {
//            mappings = loadImportMappingsFile(mappingsURL);
//            logger.debug("mappings loaded: "+mappings);
//          }
//          logger.debug("Resolving imports");
//          resolveImports(mappings);
//          logger.debug("Import resolving done");
//        }
            }

        } catch (Exception ioe) {
            throw new ResourceInstantiationException(ioe);
        }

        setURL(ontologyURL);

    }

    public void cleanup() {
        super.cleanup();
        if (dataDirectory != null) {
            try {
                FileUtils.deleteDirectory(storageFolderDir);
                logger.info("Directory " + storageFolderDir.getAbsolutePath() + " removed");
            } catch (IOException ex) {
                logger.error("Could not remove the storage-folder in " + dataDirectory.getAbsolutePath());
            }
        }
    }

    public java.net.URL getSourceURL() {
        return getRdfXmlURL();
    }

    @Override
    public OntologyTripleStore getOntologyTripleStore() {
        // TODO Auto-generated method stub
        return null;
    }
}
