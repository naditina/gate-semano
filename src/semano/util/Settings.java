package semano.util;

import org.apache.log4j.Logger;
import semano.ontoviewer.AnnotationMetaData;

import gate.Gate;

import java.io.*;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class Settings {
    private static Logger logger = Logger.getLogger(Settings.class);
    private static String PROPERTIES_FILE = "ontologyBasedAnnotation.properties";
    private static final String ANNOTATION_PROPERTIES_FILE = "annotationproperties.properties";


    public static HashSet<AnnotationMetaData> annotationProperties = new HashSet<AnnotationMetaData>();


    public static boolean WRITE_QUADS = true;
    public static final String FILENAME_QUADS = "quads.nq";
    public static boolean MATERIALIZE = true;
    public static boolean PERDOCUMENT = false;
    public static final String URI_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    public static final String PREFIX_COMMENT = "http://www.test.de/";
    //Annotation Manager
    public static String Y_PLURAL = "e";
    public static String SIMPLE_PLURAL = "s";
    public static boolean USE_PLURAL = true;
    public static final String[] STOPCHARS = {"_", "-"};
    public static final Object NEWENTITY = "NEWENTITY";
    public static final String SYNONYM = "SYNONYM";

    public static final String EXPRESSIONLESSRELATIONNNNN = "EXPRESSIONLESSRELATIONNNNN";
    public static final String EXPRESSIONLESSRELATIONJJNN = "EXPRESSIONLESSRELATIONJJNN";
    public static final String EXPRESSIONLESSRELATIONNNVBG = "EXPRESSIONLESSRELATIONNNVBG";
    public static final Set<String> EXPRESSIONLESSRELATIONWITHPOS = new HashSet<String>() {
        {
            add(EXPRESSIONLESSRELATIONNNNN);
            add(EXPRESSIONLESSRELATIONJJNN);
            add(EXPRESSIONLESSRELATIONNNVBG);
        }
    };
    public static final String EXPRESSIONLESSRELATION = "EXPRESSIONLESSRELATION";
    public static final String EXPRESSION = "EXPRESSION";
    public static final String PASSAGE = "PASSAGE";
    public static final String ANNO_TYPE_EXPRESSIONLESS_ANNOS = "EXPRESSION";
    public static final String NO_AUTOANNOTATION = "NOAUTOANNOTATION";
    public static String URI_LABEL = "label";
    //threshold for domain/range annotations
    public static int MATCHING_WINDOW = 50;
    //constants for generation candidate relation annotations
    public static boolean CALCULATE_CANDIDATE_RELATION_ANNOTATIONS = false;
    public static boolean CREATE_ANNOTATION_FOR_CANDIDATES = false;
    public static int OFFSET_DISTANCE = 60;
    //output for expressionless relation annotation
    public static boolean DEBUG_EXPRESSIONLESS_RELATIONS = false;
    public static boolean DEBUG_ALL_EXPRESSIONLESS_CANDIDATES = false;
    public static int OFFSET_FACTOR = 5;
    public static int MINIMAL_LETTER_NUMBER_FORMULA = 2;
    public static boolean ANNOTATE_WITH_NAMES = true;
    public static boolean ANNOTATE_DATA_PROPERTIES = false;
    public static boolean ANNOTATE_RELATIONS = false;
    /**
     * If true, the result of the autoannotation and the
     * ontology classes with their superclasses are written to file.
     */
    public static boolean EVALUATION = false;

    //Annotation Value
    public static String AUTOANNOTATION = "autoannotation";
    public static String ANNOTATION_VALUE = "value";
    public static String ANNOTATION_TYPE = "type";
    //Options dialog: feature labels

    /**
     * Instead of a null value, we specify the defaultAnnotationSetName with some
     * strange string
     */
    public static String DEFAULT_ANNOTATION_SET = "00#Default#00";
    public static String DEFAULT_ANNOTATION_TYPE = "Mention";
    public static String DEFAULT_CLASS_URI_FEATURE_NAME = "class";
    public static String DEFAULT_INSTANCE_URI_FEATURE_NAME = "inst";
    public static String DEFAULT_PROPERTY_URI_FEATURE_NAME = "property";
    public static String DEFAULT_DOMAIN_URI_FEATURE_NAME = "domain";
    public static String DEFAULT_RANGE_URI_FEATURE_NAME = "range";


    //GUI
    public static String DEFAULT_DOMAIN_ANNOTATION_URI_FEATURE_NAME = "domainAnnotation";
    public static String DEFAULT_RANGE_ANNOTATION_URI_FEATURE_NAME = "rangeAnnotation";
    public static boolean DEFAULT_APPLYTOALL = true;
    public static String PROPERTIES_GUI_LABEL = "Properties";
    public static String CLASSES_GUI_LABEL = "Classes";
    public static String COMBOTEXTCLASSES_GUI_LABEL = "Select here the annotated class. If you are creating a new class specify here the superclass of the new class.";
    public static String COMBOTEXTPROPERTIES_GUI_LABEL = "Select here the annotated entity. For a new property select here a property to indicate the namespace of the new entity. specify the property name in the table below in the row \"value\".";
    public static String COMBOTEXTDOMAIN_GUI_LABEL = "Domain concept";
    public static String COMBOTEXTRANGE_GUI_LABEL = "Range concept";
    public static String COMBOTEXTDOMAINANNOATTION_GUI_LABEL = "Domain annotation";
    public static String COMBOTEXTRANGEANNOATTION_GUI_LABEL = "Range annotation";
    public static int MAXWIDTH_FEATURES_DIALOG_GUI = 500;
    public static String VALUE_FEATURES_DIALOG_GUI_LABEL = "value";


    //Annotation Parser

    public static String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    public static String OFFSET = "offset";
    public static String SOURCEDOC = "source";
    public static String LANGUAGE = "language";
    public static String AUTHOR = "author";
    public static String TIME = "time";
    public static String ANTIPATTERN = "antipattern";
    public static String SUPERCONCEPT = "superconcept";

    public static String SOURCE_TAG = "@_source(%1$s)";
    public static String OFFSET_TAG = "@_offset(%1$s)";
    public static String TIME_TAG = "@_time(%1$s)";
    public static String AUTHOR_TAG = "@_author(%1$s)";
    public static String LANGUAGE_TAG = "@%1$s";
    public static String ANTIPATTERN_TAG = "@_antipattern(%1$s)";
    public static String SUPERCONCEPT_TAG = "@_superconcept(%1$s)";
    public static String DOMAIN_TAG = "@_domain(%1$s)";
    public static String RANGE_TAG = "@_range(%1$s)";
    public static String FORMAT_ANNOTATION_TAG = "\\(([^\\)]*)\\)";
    public static String LANGUAGE_REGEXP = "([a-zA-Z]+)";
    public static String ANNOTATION_DELIMITER = "\"";


    public static String DEFAULTLANGUAGE = "en";
    public static String DEFAULTAUTHOR = "nadejda";
    public static String DEFAULT_AUTHOR_CLASSNAME_ANNOTATIONS = "author of the ontology concept";
    public static String DEFAULT_OFFSET = "";
    public static String DEFALUT_TIME = "";
    public static String DEFAULT_SOURCE = "";
    public static String DEFAULT_LANGUAGE = "en";
    /**
     * If true, the debug information is printed.
     */
    public static boolean DEBUG = true;


    static {

        try {
            logger.info("Reading properties");
            String pluginPath="";
              File pluginDir = new File(Gate.getGateHome().toString()+"/plugins/Semano/");
              pluginPath = pluginDir.getAbsoluteFile().toURI().toURL().toString();      
              if(pluginPath!=null ){
                pluginPath=pluginPath.substring(5, pluginPath.length());
                System.out.println("plugin directory: "+pluginPath);       
              }
            
            FileInputStream inStream = new FileInputStream(pluginPath+PROPERTIES_FILE);
            Properties properties = new Properties();
            properties.load(inStream);


            Y_PLURAL = properties.getProperty("Y_PLURAL", Y_PLURAL);
            SIMPLE_PLURAL = properties.getProperty("SIMPLE_PLURAL", SIMPLE_PLURAL);
            USE_PLURAL = Boolean.parseBoolean(properties.getProperty("USE_PLURAL", Boolean.toString(USE_PLURAL)));
            URI_LABEL = properties.getProperty("URI_LABEL", URI_LABEL);
            MATCHING_WINDOW = Integer.parseInt(properties.getProperty("MATCHING_WINDOW", Integer.toString(MATCHING_WINDOW)));
            CALCULATE_CANDIDATE_RELATION_ANNOTATIONS = Boolean.parseBoolean(properties.getProperty("CALCULATE_CANDIDATE_RELATION_ANNOTATIONS", Boolean.toString(CALCULATE_CANDIDATE_RELATION_ANNOTATIONS)));
            CREATE_ANNOTATION_FOR_CANDIDATES = Boolean.parseBoolean(properties.getProperty("CREATE_ANNOTATION_FOR_CANDIDATES", Boolean.toString(CREATE_ANNOTATION_FOR_CANDIDATES)));
            OFFSET_DISTANCE = Integer.parseInt(properties.getProperty("OFFSET_DISTANCE", Integer.toString(OFFSET_DISTANCE)));
            DEBUG_EXPRESSIONLESS_RELATIONS = Boolean.parseBoolean(properties.getProperty("DEBUG_EXPRESSIONLESS_RELATIONS", Boolean.toString(DEBUG_EXPRESSIONLESS_RELATIONS)));
            DEBUG_ALL_EXPRESSIONLESS_CANDIDATES = Boolean.parseBoolean(properties.getProperty("DEBUG_ALL_EXPRESSIONLESS_CANDIDATES", Boolean.toString(DEBUG_ALL_EXPRESSIONLESS_CANDIDATES)));
            OFFSET_FACTOR = Integer.parseInt(properties.getProperty("OFFSET_FACTOR", Integer.toString(OFFSET_FACTOR)));
            MINIMAL_LETTER_NUMBER_FORMULA = Integer.parseInt(properties.getProperty("MINIMAL_LETTER_NUMBER_FORMULA", Integer.toString(MINIMAL_LETTER_NUMBER_FORMULA)));
            ANNOTATE_WITH_NAMES = Boolean.parseBoolean(properties.getProperty("ANNOTATE_WITH_NAMES", Boolean.toString(ANNOTATE_WITH_NAMES)));
            MATERIALIZE = Boolean.parseBoolean(properties.getProperty("MATERIALIZE", Boolean.toString(MATERIALIZE)));
            WRITE_QUADS = Boolean.parseBoolean(properties.getProperty("WRITE_QUADS", Boolean.toString(WRITE_QUADS)));
            ANNOTATE_DATA_PROPERTIES = Boolean.parseBoolean(properties.getProperty("ANNOTATE_DATA_PROPERTIES", Boolean.toString(ANNOTATE_DATA_PROPERTIES)));
            ANNOTATE_RELATIONS = Boolean.parseBoolean(properties.getProperty("ANNOTATE_RELATIONS", Boolean.toString(ANNOTATE_RELATIONS)));
            AUTOANNOTATION = properties.getProperty("AUTOANNOTATION", AUTOANNOTATION);
            ANNOTATION_VALUE = properties.getProperty("ANNOTATION_VALUE", ANNOTATION_VALUE);
            ANNOTATION_TYPE = properties.getProperty("ANNOTATION_TYPE", ANNOTATION_TYPE);
            DEFAULT_ANNOTATION_SET = properties.getProperty("DEFAULT_ANNOTATION_SET", DEFAULT_ANNOTATION_SET);
            DEFAULT_ANNOTATION_TYPE = properties.getProperty("DEFAULT_ANNOTATION_TYPE", DEFAULT_ANNOTATION_TYPE);
            DEFAULT_CLASS_URI_FEATURE_NAME = properties.getProperty("DEFAULT_CLASS_URI_FEATURE_NAME", DEFAULT_CLASS_URI_FEATURE_NAME);
            DEFAULT_INSTANCE_URI_FEATURE_NAME = properties.getProperty("DEFAULT_INSTANCE_URI_FEATURE_NAME", DEFAULT_INSTANCE_URI_FEATURE_NAME);
            DEFAULT_PROPERTY_URI_FEATURE_NAME = properties.getProperty("DEFAULT_PROPERTY_URI_FEATURE_NAME", DEFAULT_PROPERTY_URI_FEATURE_NAME);
            DEFAULT_DOMAIN_URI_FEATURE_NAME = properties.getProperty("DEFAULT_DOMAIN_URI_FEATURE_NAME", DEFAULT_DOMAIN_URI_FEATURE_NAME);
            DEFAULT_RANGE_URI_FEATURE_NAME = properties.getProperty("DEFAULT_RANGE_URI_FEATURE_NAME", DEFAULT_RANGE_URI_FEATURE_NAME);
            DEFAULT_DOMAIN_ANNOTATION_URI_FEATURE_NAME = properties.getProperty("DEFAULT_DOMAIN_ANNOTATION_URI_FEATURE_NAME", DEFAULT_DOMAIN_ANNOTATION_URI_FEATURE_NAME);
            DEFAULT_RANGE_ANNOTATION_URI_FEATURE_NAME = properties.getProperty("DEFAULT_RANGE_ANNOTATION_URI_FEATURE_NAME", DEFAULT_RANGE_ANNOTATION_URI_FEATURE_NAME);
            DEFAULT_APPLYTOALL = Boolean.parseBoolean(properties.getProperty("DEFAULT_APPLYTOALL", Boolean.toString(DEFAULT_APPLYTOALL)));
            PROPERTIES_GUI_LABEL = properties.getProperty("PROPERTIES_GUI_LABEL", PROPERTIES_GUI_LABEL);
            CLASSES_GUI_LABEL = properties.getProperty("CLASSES_GUI_LABEL", CLASSES_GUI_LABEL);
            COMBOTEXTCLASSES_GUI_LABEL = properties.getProperty("COMBOTEXTCLASSES_GUI_LABEL", COMBOTEXTCLASSES_GUI_LABEL);
            COMBOTEXTPROPERTIES_GUI_LABEL = properties.getProperty("COMBOTEXTPROPERTIES_GUI_LABEL", COMBOTEXTPROPERTIES_GUI_LABEL);
            COMBOTEXTDOMAIN_GUI_LABEL = properties.getProperty("COMBOTEXTDOMAIN_GUI_LABEL", COMBOTEXTDOMAIN_GUI_LABEL);
            COMBOTEXTRANGE_GUI_LABEL = properties.getProperty("COMBOTEXTRANGE_GUI_LABEL", COMBOTEXTRANGE_GUI_LABEL);
            COMBOTEXTDOMAINANNOATTION_GUI_LABEL = properties.getProperty("COMBOTEXTDOMAINANNOATTION_GUI_LABEL", COMBOTEXTDOMAINANNOATTION_GUI_LABEL);
            COMBOTEXTRANGEANNOATTION_GUI_LABEL = properties.getProperty("COMBOTEXTRANGEANNOATTION_GUI_LABEL", COMBOTEXTRANGEANNOATTION_GUI_LABEL);
            MAXWIDTH_FEATURES_DIALOG_GUI = Integer.parseInt(properties.getProperty("MAXWIDTH_FEATURES_DIALOG_GUI", Integer.toString(MAXWIDTH_FEATURES_DIALOG_GUI)));
            VALUE_FEATURES_DIALOG_GUI_LABEL = properties.getProperty("VALUE_FEATURES_DIALOG_GUI_LABEL", VALUE_FEATURES_DIALOG_GUI_LABEL);
            DATE_FORMAT_NOW = properties.getProperty("DATE_FORMAT_NOW", DATE_FORMAT_NOW);
            OFFSET = properties.getProperty("OFFSET", OFFSET);
            SOURCEDOC = properties.getProperty("SOURCEDOC", SOURCEDOC);
            LANGUAGE = properties.getProperty("LANGUAGE", LANGUAGE);
            AUTHOR = properties.getProperty("AUTHOR", AUTHOR);
            TIME = properties.getProperty("TIME", TIME);
            ANTIPATTERN = properties.getProperty("ANTIPATTERN", ANTIPATTERN);
            SUPERCONCEPT = properties.getProperty("SUPERCONCEPT", SUPERCONCEPT);
            SOURCE_TAG = properties.getProperty("SOURCE_TAG", SOURCE_TAG);
            OFFSET_TAG = properties.getProperty("OFFSET_TAG", OFFSET_TAG);
            TIME_TAG = properties.getProperty("TIME_TAG", TIME_TAG);
            AUTHOR_TAG = properties.getProperty("AUTHOR_TAG", AUTHOR_TAG);
            LANGUAGE_TAG = properties.getProperty("LANGUAGE_TAG", LANGUAGE_TAG);
            ANTIPATTERN_TAG = properties.getProperty("ANTIPATTERN_TAG", ANTIPATTERN_TAG);
            SUPERCONCEPT_TAG = properties.getProperty("SUPERCONCEPT_TAG", SUPERCONCEPT_TAG);
            DOMAIN_TAG = properties.getProperty("DOMAIN_TAG", DOMAIN_TAG);
            RANGE_TAG = properties.getProperty("RANGE_TAG", RANGE_TAG);
            FORMAT_ANNOTATION_TAG = properties.getProperty("FORMAT_ANNOTATION_TAG", FORMAT_ANNOTATION_TAG);
            LANGUAGE_REGEXP = properties.getProperty("LANGUAGE_REGEXP", LANGUAGE_REGEXP);
            ANNOTATION_DELIMITER = properties.getProperty("ANNOTATION_DELIMITER", ANNOTATION_DELIMITER);
            DEFAULTLANGUAGE = properties.getProperty("DEFAULTLANGUAGE", DEFAULTLANGUAGE);
            DEFAULTAUTHOR = properties.getProperty("DEFAULTAUTHOR", DEFAULTAUTHOR);
            DEFAULT_AUTHOR_CLASSNAME_ANNOTATIONS = properties.getProperty("DEFAULT_AUTHOR_CLASSNAME_ANNOTATIONS", DEFAULT_AUTHOR_CLASSNAME_ANNOTATIONS);
            DEFAULT_OFFSET = properties.getProperty("DEFAULT_OFFSET", DEFAULT_OFFSET);
            DEFALUT_TIME = properties.getProperty("DEFALUT_TIME", DEFALUT_TIME);
            DEFAULT_SOURCE = properties.getProperty("DEFAULT_SOURCE", DEFAULT_SOURCE);
            DEFAULT_LANGUAGE = properties.getProperty("DEFAULT_LANGUAGE", DEFAULT_LANGUAGE);
            DEBUG = Boolean.parseBoolean(properties.getProperty("DEBUG", Boolean.toString(DEBUG)));
            EVALUATION = Boolean.parseBoolean(properties.getProperty("EVALUATION", Boolean.toString(EVALUATION)));

            FileInputStream fstream = new FileInputStream(ANNOTATION_PROPERTIES_FILE);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                if (!strLine.startsWith("//")) {
                    AnnotationMetaData ap = AnnotationMetaData.readAnnotationPropertyFromString(strLine);
                    if (ap != null) {
                        annotationProperties.add(ap);
                    }
                }
            }
            //Close the input stream
            in.close();
        } catch (IOException e) {
            logger.error("Error reading properties file", e);
        }
    }


    public static final int COMBOBOX_H = 30;
    public static final int COMBOBOX_W = 40;
    public static final boolean WRITE_RELATIONS_ONLY = false;
    public static final boolean CONSIDER_SENTENCE_BOUNDS = true;
    public static final String SENTENCE_ANNO_TYPE = "Sentence";
    public static final String PLURAL_INDICATION = "y";
    public static final Object POS_IRRELEVANT_TYPE = "all";
    public static final boolean ENCODE = true;
    public static final int COMMENT_WINDOW = 80;
    public static final boolean USE_FULL_DOC_NAME = true;
    public static final Object RULE_FEATURE_NAME = "rule";
}
