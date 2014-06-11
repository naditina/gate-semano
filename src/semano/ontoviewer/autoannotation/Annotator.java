package semano.ontoviewer.autoannotation;

import java.util.HashMap;

public interface Annotator {

  public abstract void autoAnnotate(boolean animate);

  /**
   * @param selectedAnnotationMetaData
   * @param searchString
   *
   */
  public abstract HashMap<Integer, Integer> search(
          boolean plural,
          semano.ontoviewer.AnnotationMetaData selectedAnnotationMetaData,
          String searchString);

}
