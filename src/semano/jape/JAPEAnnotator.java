package semano.jape;

import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.corpora.DocumentStaxUtils;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.Transducer;
import gate.creole.ontology.Ontology;
import gate.util.GateException;
import gate.util.Out;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.stream.XMLStreamException;

import semano.util.FileAndDownloadUtil;


public class JAPEAnnotator {

    private static final String logFileName = "plugins/Semano/data/annotationlog.txt";


    public CorpusController initializeAnnie() throws GateException, IOException {

        File pluginsHome = Gate.getPluginsHome();
        File annieGapp = new File(pluginsHome + "/ANNIE", "ANNIE_with_defaults.gapp");

        return (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);
    }

    public Transducer initilalizeTransducer(String multiphaseJAPEFilename, Ontology o) throws GateException, IOException {
        initializeAnnie();
        FeatureMap japeProperties = Factory.newFeatureMap();
        japeProperties.put("grammarURL", new File(multiphaseJAPEFilename).toURI().toURL());
        japeProperties.put("encoding", "UTF-8");
        japeProperties.put("ontology", o);
        return (Transducer) Factory.createResource(
                "gate.creole.Transducer", japeProperties);

    }


    /**
     *  Annotate a single file, using supplied <tt>JAPETemplate</tt> file
     *
     * @param runAnnie
     * @param documentToAnnotate
     * @param saveLocation
     * @param controller
     * @param multiphase
     * @param threadNumber
     * @throws GateException
     * @throws IOException
     * @throws XMLStreamException
     */
    public void annotate(boolean runAnnie, final File documentToAnnotate, String saveLocation, CorpusController controller, String multiphase, int threadNumber) throws GateException, IOException, XMLStreamException {

        boolean isDirectory = documentToAnnotate.isDirectory();
        if (isDirectory)
            annotateAllDocuments(runAnnie, documentToAnnotate, saveLocation, multiphase, threadNumber);
        else
            annotateDocumentWithCorpusController(documentToAnnotate, saveLocation, controller);
    }

    public void annotateDocumentWithCorpusController(final File documentToAnnotate, String saveLocation, CorpusController controller)
            throws GateException, IOException, XMLStreamException {
        Document d = loadDocument(documentToAnnotate);
        Corpus corpus = annotateDocument(controller, d);
        String pathname = saveLocation + "/" + documentToAnnotate.getName() + ".xml";
        DocumentStaxUtils.writeDocument(d, new File(pathname));
        corpus.cleanup();
        d.cleanup();
        controller.cleanup();
    }

    /**
     *
     * @param controller
     * @param document document to add to corpus and annotate.
     * @return annotated corpus
     * @throws ResourceInstantiationException
     * @throws ExecutionException
     */
    private Corpus annotateDocument(CorpusController controller, Document document)
            throws ResourceInstantiationException, ExecutionException {
        Corpus corpus = Factory.newCorpus("JAPE corpus");
        controller.setCorpus(corpus);
        corpus.add(document);
        controller.execute();
        return corpus;
    }


    /**
     *
     * @param documentToAnnotate the document to be annotated
     * @param saveLocation the location the annotated document will be saved.
     * @param transducer the transducer that will be used.
     * @throws GateException
     * @throws IOException
     * @throws XMLStreamException
     */
    public void annotateDocument(final File documentToAnnotate, String saveLocation, Transducer transducer)
            throws GateException, IOException, XMLStreamException {
        Document d = loadDocument(documentToAnnotate);

        Corpus corpus = Factory.newCorpus("JAPE corpus");
        transducer.setCorpus(corpus);
        transducer.setDocument(d);
        corpus.add(d);
        transducer.execute();
        String pathname = saveLocation + "/" + documentToAnnotate.getName() + ".xml";
        DocumentStaxUtils.writeDocument(d, new File(pathname));
        corpus.cleanup();
        d.cleanup();
        transducer.cleanup();
    }

    private String getFileURL(File f) {
        String ret = "file:" + f.getPath().trim();
        Out.prln(ret);
        return ret;
    }

    /**
     * Recursively annotate documents in sub directories.
     *
     *
     *
     * @param runAnnie specifies whether to run annie.
     * @param threadNumber
     * @throws IOException
     * @throws GateException
     * @throws XMLStreamException
     */
    public void annotateAllDocuments(boolean runAnnie, File folder, String saveLocation, String multiphaseJape, int threadNumber) throws GateException, IOException, XMLStreamException {
        File[] filesInFolder = folder.listFiles(
            new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("xml");
                }
            }
        );
        if (filesInFolder != null && filesInFolder.length > 0) {

            FileAndDownloadUtil.appendStringToFile("Starting annotation with thread " + threadNumber +" "+ now(), logFileName);
            Gate.init();

            Transducer japeTransducer = null;
            CorpusController controller = null;
            if (runAnnie) {
                controller = initializeAnnie();
                ArrayList<ProcessingResource> prs = new ArrayList<>();
                for (ProcessingResource o : controller.getPRs()) {
                    if (o.toString().contains("ANNIETransducer") || o.toString().contains("Gazetteer")) {
                    } else if (o.toString().contains("AnnotationDeletePR"))
                        prs.add(0, o);
                    else if (o.toString().contains("DefaultTokeniser"))
                        prs.add(1, o);
                    else if (o.toString().contains("SentenceSplitter"))
                        prs.add(2, o);
                    else if (o.toString().contains("POSTagger"))
                        prs.add(3, o);
                    else if (o.toString().contains("OrthoMatcher"))
                        prs.add(4, o);
                }
                controller.setPRs(prs);
                System.out.println("used processing resources: ");
                for (Object o : controller.getPRs()) {
                    System.out.println(o);
                }
            } else {
                Ontology o = loadOntology(new File("plugins/Semano/data/mergedNanOnOntologyRDF.owl"));
                japeTransducer = initilalizeTransducer(multiphaseJape,o);

            }
            FileAndDownloadUtil.appendStringToFile("init finished " +
                    now(), logFileName);
            System.out.println("init finished " + now());

            for (File f : filesInFolder) {
                if (runAnnie)
                        annotateDocumentWithCorpusController(f, saveLocation, controller);
                    else
                        annotateDocument(f, saveLocation, japeTransducer);

                    FileAndDownloadUtil.appendStringToFile("finished document with thread " + threadNumber +
                            now(), logFileName);
                    System.out.println("finished document with thread " + threadNumber +
                            now());
                
//                f.delete();

            }
            FileAndDownloadUtil.appendStringToFile(" annotation with thread " + threadNumber + " finished. " + now(), logFileName);
        }
    }

    /**
     * Loads the given <tt>document</tt> using <tt>gate.corpora.DocumentImpl</tt>
     *
     * @param documentToLoad The file to be loaded.
     * @return
     * @throws MalformedURLException
     * @throws ResourceInstantiationException
     */
    private Document loadDocument(File documentToLoad)
            throws MalformedURLException, ResourceInstantiationException {
        URL documentFileAsURL = new URL(getFileURL(documentToLoad));
        FeatureMap params = Factory.newFeatureMap();
        params.put("sourceUrl", documentFileAsURL);
        params.put("preserveOriginalContent", new Boolean(true));
        params.put("collectRepositioningInfo", new Boolean(true));
        Out.prln("Creating document : " + documentFileAsURL);
        Document document = (Document)
                Factory.createResource("gate.corpora.DocumentImpl", params);
        return document;
    }
    

    
    private Ontology loadOntology(File documentToLoad)
            throws MalformedURLException, ResourceInstantiationException {
        URL documentFileAsURL = new URL(getFileURL(documentToLoad));
        FeatureMap params = Factory.newFeatureMap();
        params.put("rdfXmlURL", documentFileAsURL);
        Out.prln("Creating ontology : " + documentFileAsURL);
        Ontology o = (Ontology)
                Factory.createResource("gate.creole.ontology.Ontology", params);
        return o;
    }


    /**
     * @param args Expects either 5 or 1 arguments.
     *             If there is only only one argument we use default values for rootCorpusDirectory, saveLocation, multiphaseJapeFile and annie, user defines the number of threads.
     *             if there are 5 arguments user supplies values for 1. rootCorpusDirectory, 2. saveLocation, 3. multiphaseJapeFile, 4. useAnnie and 5. numberOfThreads.
     *
     */
    public static void main(String[] args) {
        if(args.length != 1 && args.length != 5){
            System.exit(-1);
        }
        annotate(args);
    }

    /**
     *
     * Runs AnnotateAllDocuments when numberOfThreads is 1, otherwise runs annotateAllDocumentsParallel
     * @param args
     */
    public static void annotate(String[] args) {
        JAPEAnnotator j = new JAPEAnnotator();
        String rootCorpusDirectory = "plugins/Semano/data/NEW/annotation";
        String saveLocation = "plugins/Semano/data/NEW";
        String multiphaseJapeFile = "plugins/Semano/data/japefiles/1multiphase.jape";
        boolean annie = false;
        int numberOfThreads = 1;
        int threadNumber = 0;
        if (args.length == 5) {
            rootCorpusDirectory = args[0];// "JAPE/2009fulltexts/annotation"
            saveLocation = args[1]; // "JAPE/NEW/annotation"
            multiphaseJapeFile = args[2]; // "JAPE/japefiles/1multiphase.jape"
            annie = Boolean.parseBoolean(args[3]); // "JAPE/japefiles/1multiphase.jape"
            numberOfThreads = Integer.parseInt(args[4]); // "JAPE/japefiles/1multiphase.jape"
        } else if (args.length == 1) {
            threadNumber = Integer.parseInt(args[0]);
        }
        try {
            if (numberOfThreads <= 1) {
                j.annotateAllDocuments(annie, new File(rootCorpusDirectory + "/" + threadNumber), saveLocation, multiphaseJapeFile, threadNumber);
            } else {
                j.annotateAllDocumentsParallel(annie, rootCorpusDirectory, saveLocation, multiphaseJapeFile, numberOfThreads);
            }
        } catch (GateException|IOException|XMLStreamException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recursively annotates documents in <tt>rootCorpusDirectory</tt>, thread is assigned to each subdirectory, asumes that
     * files are stored in following directory structure.
     *
     * - Root
     * -- 1
     * -- 2
     * -- 3
     * -- 4
     *
     * @param annie Should annie be used?
     * @param rootCorpusDirectory the root directory of the corpus.
     * @param saveLocation Where to save the annotated documents.
     * @param multiphaseJAPE The main JAPE filed to be used for the annotation.
     * @param numberOfThreads number of threads to annotate.
     * @throws GateException
     * @throws IOException
     * @throws XMLStreamException
     */
    private void annotateAllDocumentsParallel(final boolean annie,
                                              final String rootCorpusDirectory, final String saveLocation, final String multiphaseJAPE, int numberOfThreads) throws GateException, IOException, XMLStreamException {
        for (int i = 0; i < numberOfThreads; i++) {
            final String pathname = rootCorpusDirectory + "/" + i;
            final int iteration = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        annotateAllDocuments(annie, new File(pathname), saveLocation, multiphaseJAPE, iteration);
                    } catch (GateException|XMLStreamException|IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.run();
        }
    }

    /**
     * @return string representation of the current time, in format "yyyy/MM/d HH:mm:ss".
     */
    private String now() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }


}
