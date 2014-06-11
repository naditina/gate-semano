/**
 *
 */
package semano.jape;

import gate.*;
import gate.creole.ExecutionException;
import gate.creole.Transducer;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;

/**
 * @author nadeschda
 */
public class RuleStoreAnnotator {


    String JAPEfile;
    Transducer transducer;


    public RuleStoreAnnotator(String jAPEfile) {
        super();
        this.JAPEfile = jAPEfile;
        try {
            transducer = initializeTransducer(JAPEfile);
        } catch (GateException|IOException e) {
            e.printStackTrace();
        }
    }


    public void annotateDoc(Document doc, String currentASName) {
        try {
            transducer.setDocument(doc);
            transducer.setInputASName(currentASName);
            transducer.setOutputASName(currentASName);
            transducer.execute();
            Factory.deleteResource(transducer);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    public CorpusController initializeAnnie() throws GateException, IOException {

        File pluginsHome = Gate.getPluginsHome();
        File annieGapp = new File(pluginsHome + "/ANNIE", "ANNIE_with_defaults.gapp");

        return (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);
    }

    public Transducer initializeTransducer(String multiphaseJAPEFilename) throws GateException, IOException {
//    initializeAnnie();
        FeatureMap japeProperties = Factory.newFeatureMap();
        japeProperties.put("grammarURL", new File(multiphaseJAPEFilename).toURI().toURL());
        japeProperties.put("encoding", "UTF-8");
        return (Transducer) Factory.createResource(
                "gate.creole.Transducer", japeProperties);

    }
}
