/**
 *
 */
package semano.ontologyowl;

import gate.Factory;
import gate.FeatureMap;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import semano.ontologyowl.impl.PropertyValue;
import semano.ontoviewer.OntologyAnnotation;
import semano.util.Settings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nadejda Nikitina
 */
public class AnnotationValue extends PropertyValue {

    static Logger logger = Logger.getLogger(AnnotationValue.class);


    private String anntoationProperty = Settings.DEFAULT_SOURCE;
    private String annotatedEntity = Settings.DEFAULT_SOURCE;
    private String time = Settings.DEFAULT_SOURCE;
    private String offset = Settings.DEFAULT_SOURCE;
    private String source = Settings.DEFAULT_SOURCE;
    private String author = Settings.DEFAULTAUTHOR;
    private String language = Settings.DEFAULTLANGUAGE;
    private String antipattern = String.valueOf(false);

    private String superconcept = Settings.DEFAULT_SOURCE;
    private String domain = Settings.DEFAULT_SOURCE;
    private String range = Settings.DEFAULT_SOURCE;


    /**
     * headers for the annotation table
     *
     * @return
     */
    public static String[] getFieldNames() {
        return new String[]{"Annotation type", "Value", "Time", "Author", "Language", "Source", "Offset", "Antipattern", "Superconcept", "Domain", "Range"};
    }


    /**
     * @param anntoationProperty
     * @param annotatedEntity
     * @param time
     * @param offset
     * @param source
     * @param author
     * @param language
     * @param antipattern
     */
    public AnnotationValue(String anntoationProperty, String annotatedEntity,
                           String time, String offset, String source, String author,
                           String language, String antipattern, String value) {
        super();
        this.anntoationProperty = anntoationProperty;
        this.annotatedEntity = annotatedEntity;
        this.time = time;
        this.offset = offset;
        this.source = source;
        this.author = author;
        this.language = language;
        this.antipattern = antipattern;
        setValue(value);
    }


    /**
     * @param anntoationProperty
     * @param annotatedEntity
     * @param time
     * @param author
     * @param language
     */
    public AnnotationValue(String anntoationProperty, String annotatedEntity,
                           String time, String author, String language, String value) {
        super();
        this.anntoationProperty = anntoationProperty;
        this.annotatedEntity = annotatedEntity;
        this.time = time;
        this.author = author;
        this.language = language;
        setValue(value);
    }


    /**
     * @param anntoationProperty
     * @param annotatedEntity
     * @param time
     * @param offset
     * @param source
     * @param author
     * @param language
     */
    public AnnotationValue(String anntoationProperty, String annotatedEntity,
                           String time, String offset, String source, String author,
                           String language) {
        super();
        this.anntoationProperty = anntoationProperty;
        this.annotatedEntity = annotatedEntity;
        this.time = time;
        this.offset = offset;
        this.source = source;
        this.author = author;
        this.language = language;
    }


    /**
     * the main constructor used by Ontology parser
     *
     * @param anntotionProperty
     * @param annotatedEntity
     * @param value
     */
    public AnnotationValue(String anntotionProperty, String annotatedEntity, String value) {
        super();
        this.anntoationProperty = anntotionProperty;
        this.annotatedEntity = annotatedEntity;
        String completeValue = value;
        setValue(extractValue(value));
        if (getValue().isEmpty() || getValue().equals("^^xsd:string")) {
            logger.error("annotation was not parsed correctly: " + completeValue + " became " + getValue());
        }
        //now metadata
        value = extractMetadata(value);
        setTime(extractOtherMetadata(value, Settings.TIME_TAG));
        setAuthor(extractOtherMetadata(value, Settings.AUTHOR_TAG));
        setSource(extractOtherMetadata(value, Settings.SOURCE_TAG));
        setOffset(extractOtherMetadata(value, Settings.OFFSET_TAG));
        setAntipattern(extractOtherMetadata(value, Settings.ANTIPATTERN_TAG));
        setLanguage(extractLanguage(value));
        //this data is only available for annotations of type newEntity
        setSuperconcept(extractOtherMetadata(value, Settings.SUPERCONCEPT_TAG));
        setDomain(extractOtherMetadata(value, Settings.DOMAIN_TAG));
        setRange(extractOtherMetadata(value, Settings.RANGE_TAG));

    }

    /**
     * gets the value after the first and before the second "
     * otherwise the original value
     *
     * @param value
     * @return
     */
    public static String extractValue(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue != null && !normalizedValue.isEmpty() && normalizedValue.contains(Settings.ANNOTATION_DELIMITER)) {
            String[] parts = normalizedValue.split(Settings.ANNOTATION_DELIMITER);
            return parts[0];
        }
        return value;
    }


    private static String normalize(String value) {
        String result = value;
//    if(result.contains("Au") && !result.contains("Aug"))
//      logger.debug(value);      
        while (result.startsWith(Settings.ANNOTATION_DELIMITER)) {
            result = result.substring(1);
        }
        if (result.equals("^^xsd:string")) {
            logger.error("annotation was not parsed correctly: " + value + " became " + result);
        }
        return result;
    }


    /**
     * gets the value after the the second "
     * otherwise empty string
     *
     * @param value
     * @return
     */
    public static String extractMetadata(String value) {
        String normalizedValue = normalize(value);
        String[] parts = normalizedValue.split(Settings.ANNOTATION_DELIMITER);
        if (normalizedValue != null && !normalizedValue.isEmpty() && normalizedValue.contains(Settings.ANNOTATION_DELIMITER)) {
            return parts[1];
        }
        return value;
    }


    /**
     * is primarily for internal use but is also used by the OntologyParser to implement the GATE Ontology API
     *
     * @param value
     * @return language tag, e.g. en
     */
    public static String extractLanguage(String value) {
        String pattern = String.format(Settings.LANGUAGE_TAG, Settings.LANGUAGE_REGEXP);
        Matcher matcher = Pattern.compile(pattern).matcher(value);
        if (matcher.find()) {
            String language = matcher.group(1);
            return language;
        }
        return "";
    }

    private static String extractOtherMetadata(String value, String tag) {
        String pattern = String.format(tag, Settings.FORMAT_ANNOTATION_TAG);
        Matcher matcher = Pattern.compile(pattern).matcher(value);
        if (matcher.find()) {
            String found = matcher.group(2);
            return found;
        }
        return "";
    }

    public static String extractTime(String value) {
        String pattern = String.format(Settings.TIME_TAG, Settings.FORMAT_ANNOTATION_TAG);
        Matcher matcher = Pattern.compile(pattern).matcher(value);
        if (matcher.find()) {
            String time = matcher.group(2);
            return time;
        }

        return "";
    }


    /**
     * @return the superconcept of this
     */
    public String getSuperConcept() {
        return superconcept;
    }


    /**
     * @param superconcept the superconcept to set
     */
    public void setSuperconcept(String superconcept) {
        this.superconcept = superconcept;
    }


    /**
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }


    /**
     * @param domain the domain to set
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }


    /**
     * @return the range
     */
    public String getRange() {
        return range;
    }


    /**
     * @param range the range to set
     */
    public void setRange(String range) {
        this.range = range;
    }


    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the anntoationProperty
     */
    public String getAnnotationProperty() {
        return anntoationProperty;
    }

    /**
     * @param anntoationProperty the anntoationProperty to set
     */
    public void setAnntoationProperty(String anntoationProperty) {
        this.anntoationProperty = anntoationProperty;
    }

    /**
     * @return the annotatedEntity
     */
    public String getAnnotatedEntity() {
        return annotatedEntity;
    }

    /**
     * @param annotatedEntity the annotatedEntity to set
     */
    public void setAnnotatedEntity(String annotatedEntity) {
        this.annotatedEntity = annotatedEntity;
    }


    /**
     * @return the offset
     */
    public String getOffset() {
        return offset;
    }


    /**
     * @param offset the offset to set
     */
    public void setOffset(String offset) {
        this.offset = offset;
    }


    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }


    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }


    /**
     * @return the antipattern
     */
    public String getAntipattern() {
        return antipattern;
    }


    /**
     * @param antipattern the antipattern to set
     */
    public void setAntipattern(String antipattern) {
        this.antipattern = antipattern;
    }


    /**
     * data for the annotation table
     *
     * @return
     */
    public Object[] toFieldArray() {
        String annoPP = extractSimpleUri(getAnnotationProperty());
        return new String[]{annoPP, getValue(), getTime(), getAuthor(), getLanguage(), getSource(), getOffset(), getAntipattern(), getSuperConcept(), getDomain(), getRange()};
    }


    /**
     * @return
     */
    public static String extractSimpleUri(String fullUri) {
        IRI ap = IRI.create(fullUri);
        String annoPP = ap.getFragment();
        if (ap.getFragment() == null || ap.getFragment().isEmpty())
            annoPP = fullUri.substring(fullUri.lastIndexOf("/") + 1, fullUri.length());
        if (annoPP == null || annoPP.isEmpty())
            annoPP = fullUri;
        return annoPP;
    }


    /**
     * used to create a map from already existing annotation value to autonnotate text
     *
     * @return
     */
    public FeatureMap createFeatureMap() {

        FeatureMap features = Factory.newFeatureMap();
        //time
        features.put(Settings.TIME, getTime());
        //author
        features.put(Settings.AUTHOR, getAuthor());
        //language
        features.put(Settings.LANGUAGE, getLanguage());

        features.put(Settings.SOURCEDOC, getSource());

        features.put(Settings.OFFSET, getOffset());

        if (getAntipattern().equals(String.valueOf(true)))
            features.put(Settings.ANTIPATTERN, getAntipattern());

        if (isNewEntityAnnotation()) {
            features.put(Settings.SUPERCONCEPT, getSuperConcept());
            features.put(Settings.DEFAULT_DOMAIN_URI_FEATURE_NAME, getDomain());
            features.put(Settings.DEFAULT_RANGE_URI_FEATURE_NAME, getRange());
        }
        return features;
    }


    private boolean isNewEntityAnnotation() {
//    OntologyAnnotation.
        return false;
    }


    /**
     * used only to implement the generic API and by the plugin itself
     *
     * @param value1
     * @param language2
     * @return
     */
    public static String formatWithLanguageOnly(String value1, String language2) {
        if (language2 != null && value1 != null) {
            return value1 + String.format(Settings.LANGUAGE_TAG, language2);
        }
        return null;
    }


    /**
     * compares the relevant fields of the annotation but ignores metadata
     *
     * @param av another annotationValue
     * @return
     */
    public boolean contentEquals(AnnotationValue av) {
        if (av != null) {
            if (av.getAnnotatedEntity() != null &&
                    av.getAnnotatedEntity().equals(getAnnotatedEntity()) &&
                    av.getAnnotationProperty() != null &&
                    av.getAnnotationProperty().equals(getAnnotationProperty()) &&
                    av.getLanguage() != null &&
                    av.getLanguage().equals(getLanguage()) &&
                    av.getValue() != null &&
                    getValue() != null &&
                    av.getValue().trim().equals(getValue().trim()) &&
                    ((av.getAntipattern() == null &&
                            this.getAntipattern() == null) ||
                            (av.getAntipattern().isEmpty() &&
                                    this.getAntipattern().isEmpty()) ||
                            (av.getAntipattern().equals(getAntipattern()) &&
                                    //if it is an antipattern, the offset must also be unique
                                    (av.getAntipattern().equals(String.valueOf(false)) ||
                                            (av.getOffset() != null && av.getOffset().equals(getOffset()))))))
                return true;
        }
        return false;
    }


    public String formatMetaData() {
        String result = getValue() + Settings.ANNOTATION_DELIMITER + String.format(Settings.TIME_TAG, time) + String.format(Settings.AUTHOR_TAG, author) + String.format(Settings.LANGUAGE_TAG, language);
        if (isNewEntityAnnotation()) {
            result += String.format(Settings.SUPERCONCEPT_TAG, getSuperConcept()) + String.format(Settings.DOMAIN_TAG, getDomain()) + String.format(Settings.RANGE_TAG, getRange());
        }
        return result;
    }

    /**
     * used to format the metadata into one string with the value to write it into the ontology
     *
     * @param value
     * @param ontologyAnnotation
     * @return
     */
    public static String formatMetaData(String value, OntologyAnnotation ontologyAnnotation) {
        FeatureMap features = ontologyAnnotation.getFeatures();
        String source = (String) features.get(Settings.SOURCEDOC);
        if (source == null)
            source = "";
        Object offsetO = features.get(Settings.OFFSET);
        String offset = "";
        if (offsetO != null)
            offset = offsetO.toString();
        String time = (String) features.get(Settings.TIME);
        if (time == null)
            time = "";
        String author = (String) features.get(Settings.AUTHOR);
        if (author == null)
            author = "";
        String language = (String) features.get(Settings.LANGUAGE);
        if (language == null)
            language = "";
        //antipattern
        String antipattern = (String) features.get(Settings.ANTIPATTERN);
        if (isAntiPattern(features))
            antipattern = String.format(Settings.ANTIPATTERN_TAG, antipattern);
        else
            antipattern = "";

        String superconcept = "";
        if (features.containsKey(Settings.SUPERCONCEPT)) {
            superconcept = (String) features.get(Settings.SUPERCONCEPT);
            if (superconcept != null)
                superconcept = String.format(Settings.SUPERCONCEPT_TAG, superconcept);
            else
                superconcept = "";
        }

        String domainRange = "";
        if (features.containsKey(Settings.DEFAULT_DOMAIN_URI_FEATURE_NAME)) {
            String domain = OntologyAnnotation.getDomainClass(ontologyAnnotation);
            if (domain != null) {
                domain = String.format(Settings.DOMAIN_TAG, domain);
                domainRange += domain;
            }
            String range = OntologyAnnotation.getRangeClass(ontologyAnnotation);
            if (range != null) {
                range = String.format(Settings.RANGE_TAG, range);
                domainRange += range;
            }
        }

        String domainRangeAnnotations = "";
        if (features.containsKey(Settings.DEFAULT_DOMAIN_ANNOTATION_URI_FEATURE_NAME)) {
            String domain = OntologyAnnotation.getDomainAnnotation(ontologyAnnotation);
            if (domain != null) {
                domain = String.format(Settings.DOMAIN_TAG, domain);
                domainRangeAnnotations += domain;
            }
            String range = OntologyAnnotation.getRangeAnnotation(ontologyAnnotation);
            if (range != null) {
                range = String.format(Settings.RANGE_TAG, range);
                domainRangeAnnotations += range;
            }
        }

        //total result
        return value + Settings.ANNOTATION_DELIMITER + String.format(Settings.TIME_TAG, time) + String.format(Settings.AUTHOR_TAG, author) + String.format(Settings.SOURCE_TAG, source) + String.format(Settings.OFFSET_TAG, offset) + antipattern + superconcept + domainRange + domainRangeAnnotations;
    }


    /**
     * used to create a map for a very first time when an annotation is created by a user
     *
     * @return
     */
    public static FeatureMap createDefaultFeatureMap(String author, String language, String source, String offset) {

        FeatureMap features = Factory.newFeatureMap();
        //time
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(Settings.DATE_FORMAT_NOW);
        String time = sdf.format(cal.getTime());
        features.put(Settings.TIME, time);

        //author
        features.put(Settings.AUTHOR, author);
        //language
        features.put(Settings.LANGUAGE, language);

        features.put(Settings.SOURCEDOC, source);

        features.put(Settings.OFFSET, offset);
        return features;

    }


    public static boolean isAntiPattern(FeatureMap features) {
        String getAntiPattern = (String) features.get(Settings.ANTIPATTERN);
        return getAntiPattern != null && !getAntiPattern.isEmpty() && getAntiPattern.equals(String.valueOf(true));
    }

    public boolean isAntiPattern() {
        return getAntipattern() != null && !getAntipattern().isEmpty() && getAntipattern().equals(String.valueOf(true));
    }


    public static FeatureMap getDefaultNameFeatureMap(String name) {
        FeatureMap result = createDefaultFeatureMap(Settings.DEFAULT_AUTHOR_CLASSNAME_ANNOTATIONS, Settings.DEFAULT_LANGUAGE, Settings.DEFAULT_SOURCE, Settings.DEFAULT_OFFSET);
        result.put(Settings.TIME, Settings.DEFALUT_TIME);
        return result;
    }


    public semano.ontoviewer.AnnotationMetaData getAnnotationPropertyObject() {
        return OntologyAnnotation.getPropertyForTypeUri(anntoationProperty);
    }


    public static boolean noAutoannotationIncluded(
            AnnotationValue[] annotationValues) {
        for (AnnotationValue av : annotationValues) {
            if (Settings.NO_AUTOANNOTATION.equals(OntologyAnnotation.getPropertyForTypeUri(av.anntoationProperty).getEnumName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return "AnnotationValue [anntoationProperty=" + anntoationProperty
                + ", annotatedEntity=" + annotatedEntity + ", time=" + time
                + ", offset=" + offset + ", source=" + source + ", author="
                + author + ", language=" + language + ", antipattern="
                + antipattern + ", superconcept=" + superconcept + ", domain="
                + domain + ", value=" + value + ", range=" + range + "]";
    }


}
