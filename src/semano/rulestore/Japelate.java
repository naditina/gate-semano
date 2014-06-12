package semano.rulestore;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import semano.util.FileAndDatastructureUtil;


/**
 * This class represents  japelates and abstract japelate instantiations.
 * @author nadeschda
 *
 */
public class Japelate {

    protected String name;
    protected List<Parameter> paramList = new ArrayList<Parameter>();
    private boolean isAbstract = false;

    protected String japelateBody;
    protected File japelateFile;

    //list of numbers
    
    /**
     * @return the parameter list
     */
    public List<Parameter> getParamList() {
        return paramList;
    }

    /**
     * @return the name of the japelate
     */
    public String getName() {
        return name;
    }

    /**
     * @return the body of the japelate used for generating JAPE rules from semano rules
     */
    public String getJapelateBody() {
        return japelateBody;
    }

    /**
     * constructor used by the rules store to load japelates from files
     * @param name japelate name
     * @param japelateFile the name of the file
     */
    public Japelate(String name, File japelateFile) {
        this.name = name;
        this.japelateFile = japelateFile;
    }


    /**
     * used by the rule store to load japelate from file
     */
    protected void parseJapelate() {
        ArrayList<String> linesOfJapelate = FileAndDatastructureUtil.readStringsFromFile(japelateFile);
        Iterator<String> it = linesOfJapelate.iterator();
        while (it.hasNext()) {
            String line = it.next();
            if (!line.contains(RuleStore.JAPELATE_HEADER_TAG) && !line.contains(RuleStore.JAPELATE_BODY_TAG)) {
                parseParameter(line);
            }
            if (line.contains(RuleStore.JAPELATE_BODY_TAG)) {
                if(line.contains(RuleStore.ABSTRACT_JAPELATE_BODY_TAG)){
                    isAbstract = true;
                }
                it.remove();
                break;
            }
            it.remove();
        }

        StringBuilder parsedJapelate = new StringBuilder();
        for (String line : linesOfJapelate) {
            parsedJapelate.append(line + "\n");
        }

        japelateBody = "\n" + parsedJapelate.toString();
    }


    private void parseParameter(String line) {
        if (line != null && !line.isEmpty()) {
            if (line.contains(":") && line.contains(",")) {
                String position = line.substring(0, line.indexOf(":"));
                String type = line.substring(line.indexOf(":") + 1, line.indexOf(",")).trim();
                String description = line.substring(line.indexOf(",") + 1, line.length()).trim();
                Parameter p = new Parameter(position, type, description);
                this.paramList.add(p);

            } else {
                System.err.println("Found damaged japelate header:" + line + "in japelate " + name);
            }
        }

    }

    /**
     * tests whether this is an abstract japelate instantiation
     * @return true if this is an abstract japelate instantiation
     */
    public boolean isAbstract(){
        return this.isAbstract;
    }


}
