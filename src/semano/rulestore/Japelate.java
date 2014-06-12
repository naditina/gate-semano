package semano.rulestore;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import semano.util.FileAndDownloadUtil;


public class Japelate {

    protected String name;
    protected List<Parameter> paramList = new ArrayList<Parameter>();
    private boolean isAbstract = false;

    protected String japelateBody;
    protected File japelateFile;

    //list of numbers
    public List<Parameter> getParamList() {
        return paramList;
    }

    public String getName() {
        return name;
    }

    public String getJapelateBody() {
        return japelateBody;
    }

    public Japelate(String name, File japelateFile) {
        this.name = name;
        this.japelateFile = japelateFile;
    }


    public void parseJapelate() {
        ArrayList<String> linesOfJapelate = FileAndDownloadUtil.readStringsFromFile(japelateFile);
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

    public boolean isAbstract(){
        return this.isAbstract;
    }


}
