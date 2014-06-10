/**
 * 
 */
package semano.ontoviewer;

import java.util.HashSet;
import java.util.Set;

import semano.util.Settings;


//GUI_LABEL,URI,AUTOANNOTATE,WHOLEWORDONLY,CASESENSITIVE,MINIMAL_LETTER_NUMBER


/**
 * @author naddi
 *
 */
public class AnnotationMetaData {

  private String guiLabel;
  private String uri;
  private boolean autoannotate;
  private boolean wholeWordsOnly;
  private boolean caseSensitive;
  private boolean showDomainRange;
  private boolean showDomainRangeAnno;
  private int minLettersNumber=0;
  private String enumName;
  private String posAnnotationType;
  
  
  public AnnotationMetaData(){}
  
  /**
   * @param guiLabel
   * @param uri
   * @param autoannotate
   * @param wholeWordsOnly
   * @param caseSensitive
   * @param minLettersNumber
   */
  public AnnotationMetaData(String guiLabel, String uri, boolean autoannotate,
          boolean wholeWordsOnly, boolean caseSensitive, int minLettersNumber, String enumName, boolean showDomainRange,boolean showDomainRangeAnno,String posAnnotationType) {
    this.guiLabel = guiLabel;
    this.uri = uri;
    this.autoannotate = autoannotate;
    this.wholeWordsOnly = wholeWordsOnly;
    this.caseSensitive = caseSensitive;
    this.minLettersNumber = minLettersNumber;
    this.enumName=enumName;
    this.showDomainRange=showDomainRange;
    this.showDomainRangeAnno=showDomainRangeAnno;
    this.posAnnotationType=posAnnotationType;
  }
  /**
   * @return the guiLabel
   */
  public String getGuiLabel() {
    return guiLabel;
  }
  /**
   * @param guiLabel the guiLabel to set
   */
  public void setGuiLabel(String guiLabel) {
    this.guiLabel = guiLabel;
  }
  /**
   * @return the uri
   */
  public String getUri() {
    return uri;
  }
  /**
   * @param uri the uri to set
   */
  public void setUri(String uri) {
    this.uri = uri;
  }
  /**
   * @return the autoannotate
   */
  public boolean isAutoannotate() {
    return autoannotate;
  }
  /**
   * @param autoannotate the autoannotate to set
   */
  public void setAutoannotate(boolean autoannotate) {
    this.autoannotate = autoannotate;
  }
  /**
   * @return the wholeWordsOnly
   */
  public boolean isWholeWordsOnly() {
    return wholeWordsOnly;
  }
  /**
   * @param wholeWordsOnly the wholeWordsOnly to set
   */
  public void setWholeWordsOnly(boolean wholeWordsOnly) {
    this.wholeWordsOnly = wholeWordsOnly;
  }
  /**
   * @return the caseSensitive
   */
  public boolean isCaseSensitive() {
    return caseSensitive;
  }
  /**
   * @param caseSensitive the caseSensitive to set
   */
  public void setCaseSensitive(boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
  }
  /**
   * @return the minLettersNumber
   */
  public int getMinLettersNumber() {
    return minLettersNumber;
  }
  /**
   * @param minLettersNumber the minLettersNumber to set
   */
  public void setMinLettersNumber(int minLettersNumber) {
    this.minLettersNumber = minLettersNumber;
  }

  /**
   * @return the enumName
   */
  public String getEnumName() {
    return enumName;
  }

  /**
   * @param enumName the enumName to set
   */
  public void setEnumName(String enumName) {
    this.enumName = enumName;
  }
  
  
  
  /**
   * @return the showDomainRange
   */
  public boolean isShowDomainRange() {
    return showDomainRange;
  }

  /**
   * @param showDomainRange the showDomainRange to set
   */
  public void setShowDomainRange(boolean showDomainRange) {
    this.showDomainRange = showDomainRange;
  }

  /**
   * @return the showDomainRangeAnno
   */
  public boolean isShowDomainRangeAnno() {
    return showDomainRangeAnno;
  }

  /**
   * @param showDomainRangeAnno the showDomainRangeAnno to set
   */
  public void setShowDomainRangeAnno(boolean showDomainRangeAnno) {
    this.showDomainRangeAnno = showDomainRangeAnno;
  }
  
  

  /**
   * @return the posAnnotationType
   */
  public String getPosAnnotationType() {
    return posAnnotationType;
  }

  /**
   * @param posAnnotationType the posAnnotationType to set
   */
  public void setPosAnnotationType(String posAnnotationType) {
    this.posAnnotationType = posAnnotationType;
  }

  public static AnnotationMetaData readAnnotationPropertyFromString(String annotationProperty){    
    String[] fields = annotationProperty.split(",");
    if(fields.length==10){
      AnnotationMetaData result = new AnnotationMetaData();    
      result.setEnumName(fields[0]);
      result.setGuiLabel(fields[1]);
      result.setUri(fields[2]);
      result.setAutoannotate(Boolean.parseBoolean(fields[3]));
      result.setWholeWordsOnly(Boolean.parseBoolean(fields[4]));
      result.setCaseSensitive(Boolean.parseBoolean(fields[5]));
      result.setMinLettersNumber(Integer.parseInt(fields[6]));
      result.setShowDomainRange(Boolean.parseBoolean(fields[7]));
      result.setShowDomainRangeAnno(Boolean.parseBoolean(fields[8]));
      result.setPosAnnotationType(fields[9]);
      return result;
    }
    return null;
  }

  public boolean isRelationAnnotationProerty() {
    if(this.getEnumName().equals(Settings.EXPRESSION)
            || this.getEnumName().equals(Settings.EXPRESSIONLESSRELATION)
            ||this.isExpressionlessWithPOS())
      return true;
    return false;
  }
  
  
  public Set<String> getDomainPOS() {
    Set<String> result = new HashSet<String>();
    if(isExpressionlessWithPOS()){
      String posExp=getPosAnnotationType();
      if(posExp.contains(":")){
        String domainPOSExp = posExp.split(":")[0];
        for(String pos:domainPOSExp.split("|")){
          result.add(pos);
        }
      }
    }
    return result;
  }

  /**
   * @return
   */
  public boolean isExpressionlessWithPOS() {
    for(String annotationType : Settings.EXPRESSIONLESSRELATIONWITHPOS){
      if(this.getEnumName().equals(annotationType))
        return true;
    }
    return false;
  }
  
  /**
   * getRangePartOfSpeech : determines the part of speech for the range annotation.
   * @return
   */
  public Set<String> getRangePOS() {
    Set<String> result = new HashSet<String>();
    if(isExpressionlessWithPOS()){
      String posExp=getPosAnnotationType();
      if(posExp.contains(":")){
        String rangePORExp = posExp.split(":")[1];
        for(String pos:rangePORExp.split("|")){
          result.add(pos);
        }
      }
    }
    return result;
  }
  
  /**
   * Determine whether this annotation takes POS into consideration. 
   *  
   * @return true if POS is relevant for the annotation, false otherwise.
   */
  public boolean isPOSrelevant(){
    return getPosAnnotationType().equals(Settings.POS_IRRELEVANT_TYPE) ? false : true;
  }

  @Override
  public String toString() {
    return "AnnotationMetaData [guiLabel=" + guiLabel + ", uri=" + uri
            + ", autoannotate=" + autoannotate + ", wholeWordsOnly="
            + wholeWordsOnly + ", caseSensitive=" + caseSensitive
            + ", showDomainRange=" + showDomainRange + ", showDomainRangeAnno="
            + showDomainRangeAnno + ", minLettersNumber=" + minLettersNumber
            + ", enumName=" + enumName + ", posAnnotationType="
            + posAnnotationType + "]";
  }



}
