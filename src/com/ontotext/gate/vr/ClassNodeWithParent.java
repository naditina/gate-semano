/**
 * 
 */
package com.ontotext.gate.vr;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import gate.creole.gazetteer.MappingNode;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.OResource;
import gate.creole.ontology.Ontology;

/**
 * @author naddi
 *
 */
public class ClassNodeWithParent extends ClassNode {

  ClassNodeWithParent parent;
  /**
   * @param oResource
   */
  public ClassNodeWithParent(OResource oResource) {
    super(oResource);
  }
  
  public ClassNodeWithParent(Ontology o, ClassNodeWithParent p) {
    super(o);
    parent=p;
  }

  /**
   * @param clas
   */
  public ClassNodeWithParent(OClass clas, ClassNodeWithParent p) {
    super(clas);
    parent=p;  
  }
  
  
  /**
   * @param clas
   */
  public ClassNodeWithParent(OClass clas) {
    super(clas);
  }
  
  /**
   * @param instance
   */
  public ClassNodeWithParent(OInstance instance) {
    super(instance);
  }

  /**
   * @param instance
   */
  public ClassNodeWithParent(OInstance instance, ClassNodeWithParent p) {
    super(instance);
    parent=p;  
  }

  /**
   * @param mapNode
   */
  public ClassNodeWithParent(MappingNode mapNode) {
    super(mapNode);
  }

  /**
   * @param mapNode
   */
  public ClassNodeWithParent(MappingNode mapNode, ClassNodeWithParent p) {
    super(mapNode);
    parent=p;  
  }
  
  
  public ClassNodeWithParent getParent(){
    return parent;
  }
  
  public void setParent(ClassNodeWithParent p){
    parent=p;
  }

  public void setChildrenWithParent(Vector<ClassNodeWithParent> kids) {
    Vector<ClassNode> cn = new Vector<ClassNode>();
    for(ClassNodeWithParent n:kids){
      cn.add(n);
    }
    super.setChildren(cn);    
  }
  
  public static Set<String> getParentUris(ClassNodeWithParent node) {
    Set<String> uris = new HashSet<String>();
    if(node.getParent()!=null && node.getParent().getSource() instanceof OClass){
      OClass parent = (OClass) node.getParent().getSource();
      uris.add(parent.getURI().toString());
      uris.addAll(getParentUris(node.getParent()));
    }
    return uris;
  }

}
