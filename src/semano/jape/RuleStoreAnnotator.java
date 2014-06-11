/**
 *
 */
package semano.jape;

import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.creole.ExecutionException;
import gate.creole.Transducer;
import gate.creole.ontology.Ontology;
import gate.util.GateException;

import java.io.File;
import java.io.IOException;

/**
 * @author nadeschda
 */
public class RuleStoreAnnotator {


    String JAPEfile;
    Transducer transducer;


    public RuleStoreAnnotator(String jAPEfile, Ontology o) {
        super();
        this.JAPEfile = jAPEfile;
        try {
            transducer = initializeTransducer(JAPEfile,o);
        } catch (GateException|IOException e) {
            e.printStackTrace();
        }
    }


    public void annotateDoc(Document doc, String currentASName) {
      if(transducer==null){
        System.err.println("transducer could not be initialized!");
        return;
      }
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



    public Transducer initializeTransducer(String multiphaseJAPEFilename, Ontology o) throws GateException, IOException {
//    initializeAnnie();
        FeatureMap japeProperties = Factory.newFeatureMap();
        japeProperties.put("grammarURL", new File(multiphaseJAPEFilename).toURI().toURL());
        japeProperties.put("encoding", "UTF-8");
        japeProperties.put("ontology", o);
        return (Transducer) Factory.createResource(
                "gate.creole.Transducer", japeProperties);

    }
}
