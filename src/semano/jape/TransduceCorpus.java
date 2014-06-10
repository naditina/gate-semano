package semano.jape;

import gate.*;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.Transducer;
import gate.util.GateException;
import gate.util.Out;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class TransduceCorpus {

    private static final String JAPE_FILE = "JAPE/japefiles/1multiphase.jape";

    public static CorpusController initializeAnnie() throws GateException, IOException {
        File pluginsHome = Gate.getPluginsHome();
        File annieGapp = new File(pluginsHome + "/ANNIE", "ANNIE_with_defaults.gapp");

        return (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);
    }

    public static void main(String[] args) {

        try {
            Gate.init();
            initializeAnnie();
        } catch (GateException|IOException e1) {
            e1.printStackTrace();
            System.exit(-1);
        }
        Corpus corpus;
        try {
            corpus = Factory.newCorpus("JAPE corpus");
            FeatureMap japeProperties = Factory.newFeatureMap();
            japeProperties.put("grammarURL", new File(JAPE_FILE).toURI().toURL());
            japeProperties.put("encoding", "UTF-8");
            CorpusController jape = (CorpusController) Factory.createResource(
                    "gate.creole.Transducer", japeProperties);
            jape.setCorpus(corpus);
            jape.execute();
        } catch (ResourceInstantiationException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (MalformedURLException|ExecutionException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }


    private static Document loadDocument(File f)
            throws MalformedURLException, ResourceInstantiationException {
        URL documentFileAsURL = new URL(getFileURL(f));
        FeatureMap params = Factory.newFeatureMap();
        params.put("sourceUrl", documentFileAsURL);
        params.put("preserveOriginalContent", new Boolean(true));
        params.put("collectRepositioningInfo", new Boolean(true));
        Out.prln("Creating document : " + documentFileAsURL);
        Document document = (Document)
                Factory.createResource("gate.corpora.DocumentImpl", params);
        return document;
    }

    private static Transducer addCorpusToTransucer(File mainJape, Corpus c) throws ResourceInstantiationException, MalformedURLException {
        FeatureMap transducerParams = Factory.newFeatureMap();
        transducerParams.put(Transducer.TRANSD_GRAMMAR_URL_PARAMETER_NAME, new URL(getFileURL(mainJape)));
        Transducer t = (Transducer) Factory.createResource("gate.creole.Transducer", transducerParams);
        t.setCorpus(c);
        return t;
    }

    private static String getFileURL(File f) {
        return "file:/" + f.getAbsolutePath();
    }
}
