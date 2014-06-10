package semano;


import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import semano.ontologyowl.OntologyParser;

import java.io.FileNotFoundException;


/**
 * @author nadeschda nikitina
 */
public class Semano {


    Reasoning.Reasoner reasoner;
    OntologyParser op;


    public Semano(Reasoning.Reasoner r, OntologyParser op) {
        super();
        this.op = op;
        this.reasoner = r;
    }

    /**
     * @param filename the ontology file name
     */
    public boolean loadOntology(String filename) {
        try{
            op.loadOntology(filename);
        } catch (OWLOntologyCreationException |FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    public boolean loadLexicon(String filename) {
        return false;
    }

    public boolean annotate(String documentfilename) {
        return false;
    }


}
