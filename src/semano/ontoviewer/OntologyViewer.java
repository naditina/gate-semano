package semano.ontoviewer;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Gate;
import gate.LanguageResource;
import gate.ProcessingResource;
import gate.Resource;
import gate.SimpleAnnotationSet;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.OResource;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.OntologyModificationListener;
import gate.creole.ontology.RDFProperty;
import gate.event.AnnotationEvent;
import gate.event.AnnotationListener;
import gate.event.AnnotationSetEvent;
import gate.event.AnnotationSetListener;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;
import gate.gui.docview.AbstractDocumentView;
import gate.gui.docview.DocumentView;
import gate.gui.docview.TextualDocumentView;
import gate.swing.ColorGenerator;
import gate.util.GateRuntimeException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import semano.ontologyowl.OWLIMOntology;
import semano.rulebaseeditor.CreoleRuleStore;
import semano.util.Settings;

import com.ontotext.gate.vr.ClassNode;
import com.ontotext.gate.vr.ClassNodeWithParent;
import com.ontotext.gate.vr.IFolder;
import com.ontotext.gate.vr.OntoTreeModel;

/**
 * the basic structure of the ontology viewer 
 * manages the selection of the ontology for annotation if there several ontology sets 
 * 
 * @author Nadejda Nikitina
 */
public class OntologyViewer extends AbstractDocumentView implements
                                                        Viewer,
                                                        CreoleListener,
                                                        AnnotationSetListener,
                                                        AnnotationListener,
                                                        OntologyModificationListener {
  /**
   * Serial version ID
   */
  private static final long serialVersionUID = 3977303230621759543L;   
  

  /** Current Ontology */
  private Ontology currentOntology;
  
  /**
   * Stores all the various classes ontology2OntoTreeModels for different ontologies
   */
  protected HashMap<Ontology, OntoTreeModel> ontology2OntoTreeModelsClasses;
  
  /**
   * Stores all the various annotations ontology2Annotation for different ontologies
   */
  protected HashMap<Ontology, Set<Annotation>> ontology2OAnnotation;
  
  /**
   * Stores all the various property ontology2OntoTreeModels for different ontologies
   */
  protected HashMap<Ontology, OntoTreeModel> ontology2OntoTreeModelsProperties;
  
  /** Stores various color schemes for different ontology classes */
  protected HashMap<Ontology, HashMap<String, Color>> ontology2ColorSchemesMap;

  /** This stores Class selection map for each individual loaded ontology */
  protected HashMap<Ontology, HashMap<String, Boolean>> ontology2OResourceSelectionMap;
  
  /** This stores all the available instances of Ontologies in GATE */
  private ArrayList<Ontology> ontologies;

  public AnnotationStore annotationManager;
  
  public boolean showAutoAnnotations=true;
  public boolean showWriteOnlyNewEntities=false;
  public boolean showPropertyAnnotations=false;
 
  private LAYOUT_TYPE layoutType = LAYOUT_TYPE.VERTICLE;
 
  public enum LAYOUT_TYPE{
    VERTICLE, 
    HORIZONTAL;
  }

  /**
   * List of highlighted annotations
   */
  protected ArrayList<Annotation> highlightedAnnotations;

  private List<String> entityNames = new ArrayList<String>();
  /**
   * This is where we specify the start/end offsets of the highlighted
   * annotations which is then used for quick lookup to see if the mouse is on
   * one of them
   */
  protected int[] annotationRange;
  
  

  /**
   * List of highlighted TAGS, which are used for removing highlights
   */
  private ArrayList<Object> highlightedTags;

  protected final static ColorGenerator colourGenerator = new ColorGenerator();
  
  /////  GUI fields

  
  /**
   * Highlighter that is used for highlighting
   */
  protected Highlighter highlighter;


  /** Main panel which holds all different components */
  private JPanel mainPanel,
                 mainPanelClasses,
                 mainPanelProperties;


  /** ComboBox used to display the ontology instances */
  private JComboBox ontologyCB, ruleStoreCB;

  /** Panel to display ComboBox */
  private JPanel ontologyCBPanel;

  protected JButton wirteToFileBtn, autoAnnotateBtn, wirteInstancesBtn;

  protected JCheckBox showAutoAnnotationsCB;
//  protected JCheckBox showWriteOnlyNewEntitiesCB;
  protected JCheckBox showPropertyAnnotationsCB;
  /**
   * Instance of JTabbedPane to show the ontology Viewer and the
   * OntologyViewerOptions
   */
  private JTabbedPane tabbedPane;

  /** Instance of OntologyTreePanel */
  private OntologyTreePanel ontologyTreePanelClasses;
  
  /** Instance of OntologyTreePanel for properties*/
  private OntologyTreePanel ontologyTreePanelProperties;

  /** TextualDocument View instance */
  protected TextualDocumentView documentTextualDocumentView;

  /**
   * This is where the actual document text is being displayed.
   */
  protected JTextArea documentTextArea;

  /**
   * This object provides a functionality to add/remove/change the annotations
   */
  protected AnnotationAction annotationAction;


  private boolean autoannotation=false;


  private boolean m_GUI=false;


  protected OntologyTreeListener  ontoTreeListenerClasses;
  protected OntologyTreeListener  ontoTreeListenerProperties;


  private ArrayList<CreoleRuleStore> ruleStores;




  
  
  /**
   * @return the autoannotation
   */
  public boolean isAutoannotation() {
    return autoannotation;
  }


  /**
   * @param autoannotation the autoannotation to set
   */
  public synchronized void setAutoannotation(boolean autoannotation) {
    this.autoannotation = autoannotation;
  }

  public void initData() {
    ontology2ColorSchemesMap = new HashMap<Ontology, HashMap<String, Color>>();    
    ontology2OntoTreeModelsClasses = new HashMap<Ontology, OntoTreeModel>();
    ontology2OntoTreeModelsProperties = new HashMap<Ontology, OntoTreeModel>();
    ontology2OResourceSelectionMap =
      new HashMap<Ontology, HashMap<String, Boolean>>();
    ontology2OAnnotation= new HashMap<Ontology, Set<Annotation>>();
    annotationManager = new AnnotationStore(this);
    annotationManager.initLocalData();

  }
  
  

  public void reloadDocument() {
    ontology2OAnnotation= new HashMap<Ontology, Set<Annotation>>();
    annotationManager = new AnnotationStore(this);
    annotationManager.initLocalData();
    refreshHighlights();    
    
  }
  
  
  /** Initialises the GUI */
  public void initGUI() { 
    initData();

    m_GUI=true;
    // get a pointer to the textual view used for highlights
    Iterator centralViewsIter = owner.getCentralViews().iterator();
    while(documentTextualDocumentView == null && centralViewsIter.hasNext()) {
      DocumentView aView = (DocumentView)centralViewsIter.next();
      if(aView instanceof TextualDocumentView)
        documentTextualDocumentView = (TextualDocumentView)aView;
    }
    documentTextArea =
      (JTextArea)((JScrollPane)documentTextualDocumentView.getGUI())
        .getViewport().getView();
    

    
    // first lets find out all the annotations
    // which have ontology and a class feature in it
    document.getAnnotations().addAnnotationSetListener(this);
    
    // first creating ontologyCB GUI
    ontologyCB = new JComboBox();
    ruleStoreCB = new JComboBox();
    ontologyCBPanel = new JPanel();
    ontologyCBPanel.setLayout(new FlowLayout());
    ontologyCBPanel.add(ontologyCB);
    ontologyCBPanel.add(ruleStoreCB);
    

    // load ontologies from the Gate environment
    loadOntologies();
    loadRuleStores();
    // lets create the OntologyTree Component
    ontologyTreePanelClasses = new OntologyTreePanel(this, false);  
    ontoTreeListenerClasses = new OntologyTreeListener(ontologyTreePanelClasses,this);
    ontologyTreePanelClasses.setListener(ontoTreeListenerClasses);
    ontologyTreePanelProperties = new OntologyTreePanel(this, true);
    ontoTreeListenerProperties = new OntologyTreeListener(ontologyTreePanelProperties,this);
    ontologyTreePanelProperties.setListener(ontoTreeListenerProperties);
    
    this.highlighter = getHighlighter();

    JPanel southPanel = new JPanel(new BorderLayout());
//    buttonPanel.setBackground(UIManager.getLookAndFeelDefaults().getColor(
//      "ToolTip.background"));    

    JPanel buttonPanel = new JPanel(new FlowLayout());
    autoAnnotateBtn = new JButton("Annotate");
    autoAnnotateBtn.setBorderPainted(true);
    autoAnnotateBtn.setContentAreaFilled(false);
    autoAnnotateBtn.setMargin(new Insets(0, 0, 0, 0));
    autoAnnotateBtn.setToolTipText("Annotate With Ontology and Rule Store");
    buttonPanel.add(autoAnnotateBtn);
    
    Action writeInstancesAction = new WriteInstancesAction(this);
    wirteInstancesBtn = new JButton(writeInstancesAction);
    wirteInstancesBtn.setBorderPainted(true);
    wirteInstancesBtn.setContentAreaFilled(false);
    wirteInstancesBtn.setMargin(new Insets(0, 0, 0, 0));
    wirteInstancesBtn.setText("Instance Export");
    wirteInstancesBtn.setToolTipText("Instance Export");
    buttonPanel.add(wirteInstancesBtn);
    
//
//    Action writeAction = new WriteAnnotationsToFileAction(this);
//    wirteToFileBtn = new JButton(writeAction);
//    wirteToFileBtn.setBorderPainted(true);
//    wirteToFileBtn.setContentAreaFilled(true);
//    wirteToFileBtn.setMargin(new Insets(0, 0, 0, 0));
//    wirteToFileBtn.setText("Rule Export");
//    wirteToFileBtn.setToolTipText("Save Rules");
//    buttonPanel.add(wirteToFileBtn, BorderLayout.EAST);
    southPanel.add(buttonPanel,BorderLayout.SOUTH);
    
    JPanel filterPanel = new JPanel(new BorderLayout());
    showAutoAnnotationsCB = new JCheckBox(new AbstractAction("Show Auto-Annotations") {      
      public void actionPerformed(ActionEvent e) {
        showAutoAnnotations=showAutoAnnotationsCB.isSelected();
        refreshHighlights();        
      }
    });
    showAutoAnnotationsCB.setSelected(showAutoAnnotations);
    filterPanel.add(showAutoAnnotationsCB,BorderLayout.WEST);    
    
//    
//    showWriteOnlyNewEntitiesCB=new JCheckBox(new AbstractAction("Show and write only New-Entity-Annotations") {      
//      public void actionPerformed(ActionEvent e) {
//        showWriteOnlyNewEntities=showWriteOnlyNewEntitiesCB.isSelected();
//        refreshHighlights();        
//      }
//    });
//    showWriteOnlyNewEntitiesCB.setSelected(showWriteOnlyNewEntities);
//    filterPanel.add(showWriteOnlyNewEntitiesCB, BorderLayout.EAST);    
    
    showPropertyAnnotationsCB=new JCheckBox(new AbstractAction("Show Relation Annotations") {      
      public void actionPerformed(ActionEvent e) {
        showPropertyAnnotations=showPropertyAnnotationsCB.isSelected();
        refreshHighlights();        
      }
    });
    showPropertyAnnotationsCB.setSelected(showPropertyAnnotations);
    filterPanel.add(showPropertyAnnotationsCB, BorderLayout.CENTER);    
    southPanel.add(filterPanel,BorderLayout.NORTH);
    
    // create panels
//    int width =
//      ontologyTreePanelProperties.getWidth() > ontologyTreePanelClasses.getWidth() ? ontologyTreePanelProperties
//        .getWidth() : ontologyTreePanelClasses.getWidth();
//   int width=100;
        
    mainPanelClasses= createPannel(ontologyTreePanelClasses);
    mainPanelProperties= createPannel(ontologyTreePanelProperties);
    tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Classes", mainPanelClasses);
    tabbedPane.addTab("Properties", mainPanelProperties);
    //NN no options pane!
//    tabbedPane.addTab("Options", ontologyTreePanel.ontoViewer.ontologyViewerOptions
//      .getGUI());
    
    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(ontologyCBPanel, BorderLayout.NORTH);
    mainPanel.add(tabbedPane, BorderLayout.CENTER);
    mainPanel.add(southPanel, BorderLayout.SOUTH);
//    mainPanel.setBackground(Color.white);
//    mainPanel.setMaximumSize(new Dimension(width, mainPanel.getHeight()));
 
    
    addListeners();
    if(ontologies != null && ontologies.size() > 0) {
      currentOntology=ontologies.get(0);
      this.ontologyCB.setSelectedItem(currentOntology);
      updateOntologyData();
    }
    else {
      ontologyTreePanelProperties.showEmptyOntologyTree();
      ontologyTreePanelClasses.showEmptyOntologyTree();
    }
    refreshHighlights();
    if(ruleStores!=null && !ruleStores.isEmpty()){
      this.ruleStoreCB.setSelectedIndex(0);
    }
    
    autoAnnotateBtn.setAction(new AbstractAction("Annotate") {      
      @Override
      public void actionPerformed(ActionEvent e) {
        getRuleStore().annotate(document);
        
      }
    });
    
    mainPanel.setMaximumSize(new Dimension(ruleStoreCB.getWidth()+ontologyCB.getWidth()+30, mainPanel.getHeight()));
  }

 
  /**
   * @param ontologyTreePanel which contains the data model
   * @param width of the panel
   * @return
   */
  private JPanel createPannel(OntologyTreePanel ontologyTreePanel) {    
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(ontologyTreePanel.getGUI(), BorderLayout.CENTER);
    panel.setBackground(Color.white);
//    panel.setMaximumSize(new Dimension(width, panel.getHeight()));
    return panel;
  }



  /**
   * This method is called by respective selectedAnnotationSetName from which an
   * annotation is removed
   */
  public void annotationRemoved(AnnotationSetEvent ase) {
    if(getCurrentOntology() == null) return;
    Annotation currentAnnot = ase.getAnnotation();
    if(ontologies.contains(getCurrentOntology()) &&
       ontology2OAnnotation.get(getCurrentOntology())!=null &&
       ontology2OAnnotation.get(getCurrentOntology()).contains(currentAnnot)){
      
      ontology2OAnnotation.get(getCurrentOntology()).remove(currentAnnot);
    }
    currentAnnot.removeAnnotationListener(this);
    String annotatedResource = OntologyAnnotation
            .getSourceFeatureValue(getCurrentOntology(),currentAnnot);
    if(annotatedResource != null) {
      //remove annotation from map
      LinkedHashSet<Annotation> annots = annotationManager.getEntityName2AnnotationList()
              .get(annotatedResource);
      if(annots != null) {
        //to prevent concurrent modifications we first gather all removed Annotations and then remove them
        HashSet<Annotation> toRemove=new HashSet<Annotation>();
        for(Annotation annot:annots) {
          if(currentAnnot.getId().equals(annot.getId())) {
            toRemove.add(currentAnnot);
          }
        }
        for(Annotation annot:toRemove) {          
            annots.remove(annot);
        }
        annotationManager.getEntityName2AnnotationList().put(
                annotatedResource, annots);
        // remove this from annotationsID2ASID
        annotationManager.annotationsID2ASID.remove(currentAnnot.getId());
        // highlight annotations
        refreshHighlights();
      }
    }

  }

  /* (non-Javadoc)
   * @see gate.event.AnnotationListener#annotationUpdated(gate.event.AnnotationEvent)
   */
  public void annotationUpdated(AnnotationEvent ae) {
    if(getCurrentOntology() == null) return;
    Annotation currentAnnot = (Annotation)ae.getSource();    
    
    annotationManager.updateAnnotationLists(currentAnnot);
    AnnotationSet set = null;
    // find out the AnnotationSet Name that contains this
    // annotation
    if(document.getAnnotations().get(currentAnnot.getId()) != null) {
      set = document.getAnnotations();
    }
    else {
      Collection sets = document.getNamedAnnotationSets().values();
      Iterator iter = sets.iterator();
      while(iter.hasNext()) {
        AnnotationSet set1 = (AnnotationSet)iter.next();
        if(set1.get(currentAnnot.getId()) != null) {
          set = set1;
          break;
        }
      }
    }
    
    //register the set if it was not registered
    annotationManager.registerAnnotationSet(set,currentAnnot.getId());
    updateOntologyData();    
    refreshHighlights();
  }

  
  /**
   * This method is invoked whenever a new annotation is added
   */
  public void annotationAdded(AnnotationSetEvent ase) {
    if(getCurrentOntology() == null) return;
    Annotation currentAnnot = ase.getAnnotation();
    currentAnnot.addAnnotationListener(this);
    if(!ontology2OAnnotation.containsKey(getCurrentOntology()) && ontology2OAnnotation.get(getCurrentOntology())==null){
      ontology2OAnnotation.put(getCurrentOntology(), new HashSet<Annotation>());
    }
    ontology2OAnnotation.get(getCurrentOntology()).add(currentAnnot);
    annotationManager.updateAnnotationLists(currentAnnot);
    AnnotationSet set = (AnnotationSet)ase.getSource();
    //register the current annotation set
   annotationManager.registerAnnotationSet(set, currentAnnot.getId());
   updateOntologyData();
   if(!autoannotation){
     refreshHighlights();
   }
  }


  public void registerHooks() {
    documentTextArea.addMouseListener(annotationAction);
    documentTextArea.addMouseMotionListener(annotationAction);
    refreshHighlights();
  }

  public void unregisterHooks() {
    documentTextArea.removeMouseListener(annotationAction);
    documentTextArea.removeMouseMotionListener(annotationAction);
    removeHighlights();
  }

  public int getType() {
    return VERTICAL;
  }

  /**
   * gets the main tabbed pane that will be displayed as the OCAT component
   */
  public Component getGUI() {
    return mainPanel;
  }

  /**
   * Adds various listeners to the different components
   */
  private void addListeners() {
    Gate.getCreoleRegister().addCreoleListener(this);
    ontologyCB.addActionListener(new OntologySelectionChangeAction());
    annotationAction = new AnnotationAction(ontologyTreePanelClasses, ontologyTreePanelProperties, this);
  }

  /**
   * Releases all resources and listeners
   */
  public void cleanup() {
    Gate.getCreoleRegister().removeCreoleListener(this);
    //remove the annotationSetListener as well
    AnnotationSet set = document.getAnnotations();
    set.removeAnnotationSetListener(this);
    Map annotSetMap = document.getNamedAnnotationSets();
    if(annotSetMap != null) {
      java.util.List<String> setNames =
        new ArrayList<String>(annotSetMap.keySet());
      Collections.sort(setNames);
      Iterator<String> setsIter = setNames.iterator();
      while(setsIter.hasNext()) {
        set = document.getAnnotations(setsIter.next());
        set.removeAnnotationSetListener(this);
      }
    }
  }
  
  
  private void updateOntologyData() {
    
    if(!ontology2ColorSchemesMap.containsKey(getCurrentOntology()) ||
            !ontology2OResourceSelectionMap.containsKey(getCurrentOntology()) ||
            !ontology2OntoTreeModelsClasses.containsKey(getCurrentOntology()) ||
            !ontology2OntoTreeModelsProperties.containsKey(getCurrentOntology())){
    //TODO NN create a color scheme for properties and topclasses together  
    //create color scheme and update data in trees and init selection map
    HashMap<String, Color> newColorScheme = new HashMap<String, Color>();
    HashMap<String, Boolean> newClassSelection =
      new HashMap<String, Boolean>();
    updateOntologyData(ontology2OntoTreeModelsClasses, false, newColorScheme, newClassSelection);
    updateOntologyData(ontology2OntoTreeModelsProperties, true, newColorScheme, newClassSelection);    
    ontology2ColorSchemesMap.put(getCurrentOntology(), newColorScheme);
    ontology2OResourceSelectionMap.put(getCurrentOntology(), newClassSelection);    
    }
    ontologyTreePanelClasses.showOntologyInOntologyTreeGUI(getCurrentOntology(), annotationManager.getEntityName2AnnotationList());
    ontologyTreePanelProperties.showOntologyInOntologyTreeGUI(getCurrentOntology(), annotationManager.getEntityName2AnnotationList());
    
  }
  
  private void updateOntologyData(
          HashMap<Ontology, OntoTreeModel> ontology2OntoTreeModels,
          boolean isPropertyView, HashMap<String, Color> newColorScheme,
          HashMap<String, Boolean> newClassSelection) {
    Ontology currentOntology = getCurrentOntology();
    ClassNode rootClasses = ClassNode.createRootNode(currentOntology, true,
            false, isPropertyView);
    initColorScheme(rootClasses, newColorScheme);
    if(!isPropertyView) {
      initSelectionRec(rootClasses, newClassSelection, true);
    }
    else {
      initSelectionRecForChildren(rootClasses, newClassSelection, true);
    }
    OntoTreeModel newOntologyTreeModel = new OntoTreeModel(rootClasses);
    ontology2OntoTreeModels.put(currentOntology, newOntologyTreeModel);
    loadOntologyEntities();
  }
  
  

  /**
   * For every ontology it generates the colors only once at the begining which
   * should remain same throughout the programe
   * 
   * 
   * NN changes the color alignment: only upper nodes have different colors, but the childner inherit the color scheme.
   * 
   * @param root
   *          - the root (top class) of the ontology
   * @param colorScheme
   *          - and the colorScheme hashmap Key=conceptName, Value:associated
   *          color map. if provided as a new fresh instance of hashmap with
   *          size zero, it parses through the whole ontology and generate the
   *          random color instances for all the classes and stores them in the
   *          provided colorScheme hashmap
   */
  public static void initColorScheme(IFolder root, HashMap<String, Color> colorScheme) {
    if(root instanceof ClassNodeWithParent && ((ClassNodeWithParent)root).getParent() != null && ((ClassNodeWithParent)root).getParent() instanceof ClassNodeWithParent) {
      //this is a child node, so put the color of the parent
        ClassNodeWithParent parent = ((ClassNodeWithParent)root).getParent();
        colorScheme.put(root.toString(), colorScheme.get((parent.toString())));
    }
    else {
      //this is NOT a child node
      if(!colorScheme.containsKey(root.toString())) {
        float components[] = colourGenerator.getNextColor().getComponents(null);
        Color color = new Color(components[0], components[1], components[2], 0.5f);
        colorScheme.put(root.toString(), color);
      }
    }
    Iterator children = root.getChildren();
    while(children.hasNext()) {
      initColorScheme((IFolder)children.next(), colorScheme);
    }
  }

  /**
   * initializes the classSelection as true for all subclasses of root
   * 
   * @param root
   * @param classSelection
   */
  private void initSelectionRec(IFolder root,
    HashMap<String, Boolean> classSelection, boolean defaultSelectionValue) {
    if(!classSelection.containsKey(root.toString())) {
      classSelection.put(root.toString(), defaultSelectionValue);
      Iterator children = root.getChildren();
      while(children.hasNext()) {
        initSelectionRec((IFolder)children.next(), classSelection,defaultSelectionValue);
      }
    }
  }

  /**
   * initializes the classSelection as true for all subclasses of root
   * 
   * @param root
   * @param classSelection
   */
  private void initSelectionRecForChildren(IFolder root,
          HashMap<String, Boolean> classSelection, boolean defaultSelectionValue) {
    Iterator children = root.getChildren();
    while(children.hasNext()) {
      initSelectionRec((IFolder)children.next(), classSelection,
              defaultSelectionValue);
    }
  }
  
  

  /** Returns the instance of highlighter */
  public javax.swing.text.Highlighter getHighlighter() {
    return ((JTextArea)((JScrollPane)documentTextualDocumentView.getGUI()).getViewport().getView())
      .getHighlighter();
  }

  /** Returns the associated color for the given class */
  public Color getHighlightColor(String classVal) {
    Ontology curentOntology = getCurrentOntology();
    HashMap<String,Color> colorMap = this.ontology2ColorSchemesMap.get(getCurrentOntology());
    return (Color)colorMap.get(classVal);
  }


  public void removeAnnotationsWithSource(String nodeId) {
    if(nodeId != null && !nodeId.isEmpty()) {
      Set<Annotation> annotationsToRemove = ontology2OAnnotation
              .get(getCurrentOntology());
      if(annotationsToRemove != null) {
        HashSet<Annotation> deleteAnnotations = new HashSet<Annotation>();
        for(Annotation a : annotationsToRemove) {
          if(nodeId.endsWith(OntologyAnnotation.getSourceFeatureValue(a))) {
            deleteAnnotations.add(a);
          }
        }
        for(Annotation a:deleteAnnotations){
          annotationManager.deleteAnnotation(a);
        }
      }
    }
  }

  /**
   * Searches within the GATE system to find out all the loaded ontologies
   */
  private void loadOntologies() {
    if(ontologies == null) {
      ontologies = new ArrayList<Ontology>();
      java.util.List lrs = gate.Gate.getCreoleRegister().getPublicLrInstances();
      Iterator iter1 = lrs.iterator();
      while(iter1.hasNext()) {
        gate.LanguageResource lr = (LanguageResource)iter1.next();
        if(!(lr instanceof Ontology)) continue;
        ((Ontology)lr).addOntologyModificationListener(this);
        ontologies.add((Ontology)lr);
        if(null!=ontologyCB)
        ontologyCB.addItem(((Ontology)lr).getName());
      }
      
    }
  }
  
  

  /**
   * Searches within the GATE system to find out all the loaded ontologies
   */
  private void loadRuleStores() {
    if(ruleStores == null) {
      ruleStores = new ArrayList<CreoleRuleStore>();
      java.util.List prs = gate.Gate.getCreoleRegister().getPublicPrInstances();
      Iterator iter1 = prs.iterator();
      while(iter1.hasNext()) {
        gate.ProcessingResource lr = (ProcessingResource)iter1.next();
        if(!(lr instanceof CreoleRuleStore)) continue;
//        ((CreoleRuleStore)lr).addOntologyModificationListener(this);
        ruleStores.add((CreoleRuleStore)lr);
        if(null!=ruleStoreCB)
          ruleStoreCB.addItem(((CreoleRuleStore)lr).getName());
      }
      ruleStoreCB.setSelectedIndex(0);
      
    }
  }
  
  /**
   * Searches within the GATE system to find out all the loaded ontologies
   */
  private void unloadOntologies() {
    for(Ontology o:ontologies){
        o.removeOntologyModificationListener(this);
    }
  }
  
  /**
   * Refresh the comboBox.. in case new ontology is loaded or one is removed
   * 
   * @param ontology
   *          this could be either newly added ontology or that removed one
   * @param removed
   *          if true instance is removed otherwise added
   */
  private void refreshOntologyCB(Ontology ontology, boolean removed) {
    refreshOntologyCB(ontology, removed, false);
  }

  /**
   * Refresh the comboBox.. in case new ontology is loaded or one is removed
   * 
   * @param ontology
   *          this could be either newly added ontology or that removed one
   * @param removed
   *          if true instance is removed otherwise added
   */
  private void refreshOntologyCB(Ontology ontology, boolean removed, boolean removeAnnotations) {
    if(removed) {
      int index = ontologies.indexOf(ontology);
      if(index >= 0) {
        ontologies.remove(ontology);
        ontologyCB.removeItemAt(index);
        ontologyCB.invalidate();
        if(removeAnnotations){
          Set<Annotation> annotationsToRemove =ontology2OAnnotation.get(ontology);
          if(annotationsToRemove!=null){
            for(Annotation a:annotationsToRemove){
              annotationManager.deleteAnnotation(a);
            }
          }
        }
        boolean wasCurrentlySelected = false;
        if(getCurrentOntology() == ontology)
          wasCurrentlySelected = true;
        removeOntologyTreeModels(ontology,
          wasCurrentlySelected);
        if(wasCurrentlySelected) if(ontologyCB.getItemCount() > 0) {
          ontologyCB.setSelectedIndex(0);
        }
      }
    }
    else {
      ontologies.add(ontology);
      ontologyCB.addItem(ontology.getName());
      if(ontologyCB.getItemCount() == 1) {
//        ontologyTreePanelClasses.ontologyTree.updateUI();
        ontologyTreePanelClasses.ontologyTree.setVisible(true);
//        ontologyTreePanelProperties.ontologyTree.updateUI();
        ontologyTreePanelProperties.ontologyTree.setVisible(true);
        ontologyCB.setSelectedIndex(0);
      }
    }
  }

  /**
   * Description: an internal OntologySelectionChangeAction used to handle the
   * ontology selection changes
   * 
   * @author Niraj Aswani
   * @version 1.0
   */
  private class OntologySelectionChangeAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      /** gets executed when user changes his/her selection of ontology */
      if(e.getSource() == ontologyCB) {
        int index = ontologyCB.getSelectedIndex();
        if(index >= 0) {
          currentOntology=ontologies.get(index);
          updateOntologyData();
        }
        else {
          ontologyTreePanelProperties.showEmptyOntologyTree();
          ontologyTreePanelClasses.showEmptyOntologyTree();
        }
        refreshHighlights();
      }
    }
  }

  // ***************************
  /** CreoleListener methods */
  // ****************************
  /**
   * Gets executed whenever new resource is loaded in the GATE system
   * 
   * @param creoleEvent
   */
  public void resourceLoaded(CreoleEvent creoleEvent) {
    Resource rs = creoleEvent.getResource();
    if(rs instanceof Ontology) {
      ((Ontology)rs).addOntologyModificationListener(this);
      refreshOntologyCB((Ontology)rs, false);
    }
  }

  /**
   * Gets executed whenever existing resource is unloaded in the GATE system
   * 
   * @param creoleEvent
   */
  public void resourceUnloaded(CreoleEvent creoleEvent) {
    Resource rs = creoleEvent.getResource();
    if(rs instanceof Ontology) {
      // we are interested
      ((Ontology)rs).removeOntologyModificationListener(this);
      refreshOntologyCB((Ontology)rs, true);
    }
  }

  public void datastoreOpened(CreoleEvent creoleEvent) {
  }

  public void datastoreCreated(CreoleEvent creoleEvent) {
  }

  public void datastoreClosed(CreoleEvent creoleEvent) {
  }

  /**
   * Gets executed whenever resource name is changed
   * 
   * @param resource
   * @param string
   * @param string2
   */
  public void resourceRenamed(Resource resource, String string, String string2) {
    if(resource instanceof Ontology) {
      int index = ontologies.indexOf((Ontology)resource);
      ontologyCB.remove(index);
      ontologyCB.insertItemAt(((Ontology)resource).getName(), index);
      ontologyCB.invalidate();
    }
  }

  public void resourcesRemoved(Ontology ontology, String[] deletedResources) {
    boolean shouldSelectAgain = false;
    int index = ontologies.indexOf(ontology);
    if(index < 0) return;

    if(ontologyCB.getSelectedIndex() == index) {
      shouldSelectAgain = true;
    }
    HashMap<String, Boolean> selectionMap =
      ontology2OResourceSelectionMap.get(ontology);

    refreshOntologyCB(ontology, true);
    refreshOntologyCB(ontology, false);
    if(shouldSelectAgain) ontologyCB.setSelectedIndex(index);
    HashMap<String, Boolean> newMap =
      ontology2OResourceSelectionMap.get(ontology);
    for(String key : selectionMap.keySet()) {
      Boolean val = selectionMap.get(key);
      if(newMap.containsKey(key)) {
        newMap.put(key, val);
      }
    }

    documentTextArea.requestFocus();
  }

  public void resourceRelationChanged(Ontology ontology, OResource resource1,
    OResource resouce2, int eventType) {
    this.ontologyModified(ontology, resource1, eventType);
  }

  public void resourcePropertyValueChanged(Ontology ontology,
    OResource resource, RDFProperty property, Object value, int eventType) {
    this.ontologyModified(ontology, resource, eventType);
  }

  public void ontologyModified(Ontology ontology, OResource resource,
    int eventType) {
    if(eventType == OConstants.SUB_CLASS_ADDED_EVENT
      || eventType == OConstants.SUB_CLASS_REMOVED_EVENT) {
      ontologyReset(ontology);
    }
  }

  public void ontologyReset(Ontology ontology) {
    boolean shouldSelectAgain = false;
    int index = ontologies.indexOf(ontology);
    if(index < 0) return;

    annotationManager.initLocalData();
    if(ontologyCB.getSelectedIndex() == index) {
      shouldSelectAgain = true;
    }

    // lets traverse through the ontology and find out the classes which
    // are selected
    HashMap<String, Boolean> selectionMap =
      ontology2OResourceSelectionMap.get(ontology);
    refreshOntologyCB(ontology, true);
    refreshOntologyCB(ontology, false);
    if(shouldSelectAgain) ontologyCB.setSelectedIndex(index);
    HashMap<String, Boolean> newMap =
      ontology2OResourceSelectionMap.get(ontology);
    for(String key : selectionMap.keySet()) {
      Boolean val = selectionMap.get(key);
      if(newMap.containsKey(key)) {
        newMap.put(key, val);
      }
    }

    documentTextArea.requestFocus();

  }

  public void resourceAdded(Ontology ontology, OResource resource) {
    boolean shouldSelectAgain = false;
    int index = ontologies.indexOf(ontology);
    if(index < 0) return;
    if(ontologyCB.getSelectedIndex() == index) {
      shouldSelectAgain = true;
    }

    if(resource instanceof OInstance) {
      OInstance inst1 = (OInstance)resource;
      Set<OClass> oClasses = inst1.getOClasses(OConstants.DIRECT_CLOSURE);
      // for each class find out its class nodes
      for(OClass aClass : oClasses) {
        List<ClassNode> cnodes = ontologyTreePanelClasses.getNode(aClass.getName());
        if(cnodes.isEmpty()) {
          continue;
        }

        for(ClassNode anode : cnodes) {
          ClassNode instNode = new ClassNode(inst1);
          anode.addSubNode(instNode);
          // here we need to set a color for this new instance
          initColorScheme(instNode,
            (HashMap<String, Color>)ontology2ColorSchemesMap
              .get(ontology));
          
          initSelectionRec(
              instNode,
              (HashMap<String, Boolean>)ontology2OResourceSelectionMap
                .get(ontology),true);
        }
      }
    }
    else {

      // traverse through the ontology and find out selected classes
      HashMap<String, Boolean> selectionMap =
        ontology2OResourceSelectionMap.get(ontology);
      refreshOntologyCB(ontology, true, false);
      refreshOntologyCB(ontology, false);
      if(shouldSelectAgain) ontologyCB.setSelectedIndex(index);
      HashMap<String, Boolean> newMap =
        ontology2OResourceSelectionMap.get(ontology);
      if(selectionMap!=null){
      for(String key : selectionMap.keySet()) {
        Boolean val = selectionMap.get(key);
        if(newMap.containsKey(key)) {
          newMap.put(key, val);
        }
      }
      }else
        System.err.println("selectionMap was null, ontology:"+ontology.getName());
    }

    if(shouldSelectAgain) ontologyCB.setSelectedIndex(index);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        ontologyTreePanelClasses.ontologyTree.updateUI();
        documentTextArea.requestFocus();
      }
    });
  }
  
  

  /**
  * Method to remove all highlights
  */
  public void removeHighlights() {
  annotationRange = new int[0];
  // everytime we hightlight first we remove all the highlights
  if(highlightedTags != null) {
    for(int i = 0; i < highlightedTags.size(); i++) {
      highlighter.removeHighlight(highlightedTags.get(i));
    }
  }
  highlightedAnnotations = new ArrayList<Annotation>();
  highlightedTags = new ArrayList<Object>();
  }

  /**
  * Method to highlight the annotations
  */
  public void refreshHighlights() {

  annotationRange = new int[0];
  // everytime we hightlight first we remove all the highlights
  if(highlightedTags != null) {
    for(int i = 0; i < highlightedTags.size(); i++) {
      highlighter.removeHighlight(highlightedTags.get(i));
    }
  }
  highlightedTags = new ArrayList<Object>();
  highlightedAnnotations = new ArrayList<Annotation>();

  LinkedHashMap<String, LinkedHashSet<Annotation>> currentClassName2AnnotationsListMap =
    annotationManager.getEntityName2AnnotationList();
  if(currentClassName2AnnotationsListMap == null) return;

  HashMap<String, Boolean> currentClass2IsSelectedMap =
    ontology2OResourceSelectionMap.get(getCurrentOntology());

  // if there is no class selected, no need to highlight anything
  if(currentClass2IsSelectedMap == null
    || currentClass2IsSelectedMap.isEmpty()) { return; }

  Iterator<String> iter = currentClass2IsSelectedMap.keySet().iterator();
  while(iter.hasNext()) {
    String className = iter.next();
    
    if(!currentClass2IsSelectedMap.get(className).booleanValue()) {
      continue;
    }

    String key = className;
    if(className.startsWith("[") && className.endsWith("]"))
      key = className.substring(1, className.length()-1);
    
    HashSet<Annotation> annotationsList =
      currentClassName2AnnotationsListMap.get(key);
    
    

    if(annotationsList == null || annotationsList.isEmpty()) {
      continue;
    }
    else {
      // see which annotation types should only be listed
      String typeToMatch =Settings.DEFAULT_ANNOTATION_TYPE;
      String setToMatch =Settings.DEFAULT_ANNOTATION_SET;
      for(Annotation ann:annotationsList) {
        if(ann.getType().equals(typeToMatch)) {
          String set = annotationManager.getAnnotationSet(ann);
          if(set == null
            && setToMatch
              .equals(Settings.DEFAULT_ANNOTATION_SET)) {
            // do nothing
          }
          else if(set != null && set.equals(setToMatch)) {
            // so nothing
          }
          else {
            continue;
          }
        }
        else {
          continue;
        }
        //check if we should show autoannotations:
        if(showAutoAnnotations || OntologyAnnotation.isManuallySetAnnotation(ann)){
          
          //check if only new entities should be shown and written:
          if(!showWriteOnlyNewEntities || Settings.NEWENTITY.toString().equals(OntologyAnnotation.getAnnotationType(ann))){
              //check if only class annotations should be shown and written:
              if(showPropertyAnnotations || !OntologyAnnotation.isSourceAProperty(ann)){
              
            
                  //finally highlight!
                  try {
                    Color color = getHighlightColor(className);
                    Object tag =
                      highlighter.addHighlight(ann.getStartNode().getOffset()
                        .intValue(), ann.getEndNode().getOffset().intValue(),
                        new DefaultHighlighter.DefaultHighlightPainter(color));
                    highlightedAnnotations.add(ann);
                    highlightedTags.add(tag);
                  }
                  catch(javax.swing.text.BadLocationException e) {
                    throw new GateRuntimeException(e);
                  }
              }
          }
        }
      }
    }
  }

  // This is to make process faster.. instead of accessing each
  // annotation
  // and its offset, we create an array with its annotation offsets to
  // search
  // faster
  ArrayList<Annotation> highAnns = highlightedAnnotations;
  Collections.sort(highAnns, new gate.util.OffsetComparator());
  annotationRange = new int[highAnns.size() * 2];
  for(int i = 0, j = 0; j < highAnns.size(); i += 2, j++) {
    Annotation ann = (Annotation)highAnns.get(j);
    annotationRange[i] = ann.getStartNode().getOffset().intValue();
    annotationRange[i + 1] = ann.getEndNode().getOffset().intValue();
  }
  }

  public Ontology getCurrentOntology() {
//    ontologyCB.getSelectedIndex();
    return currentOntology;
  }
  
  
  /**
   * This method is called to remove the stored ontology model and free up the
   * memory with other resources occupied by the removed ontology
   */
  public void removeOntologyTreeModels(Ontology ontology,
    boolean wasCurrentlySelected) {
    this.ontology2OntoTreeModelsClasses.remove(ontology);
    this.ontology2OntoTreeModelsProperties.remove(ontology);
    this.ontology2ColorSchemesMap.remove(ontology);
    this.ontology2OResourceSelectionMap.remove(ontology);
    if(ontology2OntoTreeModelsClasses == null || ontology2OntoTreeModelsClasses.size() == 0) {
      ontologyTreePanelClasses.showEmptyOntologyTree();
    }
    if(ontology2OntoTreeModelsProperties == null || ontology2OntoTreeModelsProperties.size() == 0) {
      ontologyTreePanelProperties.showEmptyOntologyTree();
    }
  }


 
  public AnnotationSet getAnnotationSet() {
      return getDocument().getAnnotations();
  }

  public static InstancesWriter getWirter(Document doc, OWLIMOntology o) {
    OntologyViewer ov = new OntologyViewer();
    ov.currentOntology=o;
    ov.document=doc;
    ov.initData();
    InstancesWriter iw = new InstancesWriter(ov);
    return iw;
  }
    
//
//  public static void autoannotate(Document doc, OWLIMOntology o) {
//    OntologyViewer ov = new OntologyViewer();
//    ov.currentOntology=o;
//    ov.document=doc;
//    ov.initData();
//    boolean asynchronous=false;
//    ov.annotationManager.autoAnnotate(asynchronous);
////    while(!ov.annotationManager.isJobsFinished()){
////      try {
////        Thread.sleep(3000);
////      }
////      catch(InterruptedException e) {
////        // TODO Auto-generated catch block
////        e.printStackTrace();
////      }
////    }
//    int size = doc.getAnnotations().size();
//    System.out.println("annotation of document "+doc.getName()+" finished; created "+size+" annotations");
////    for(Annotation a: doc.getAnnotations().size())
//    ov.unloadOntologies();
//  }


  /**
   * @param ov
   */
  public static void writeInstances(Object instanceWriter) {
    if(instanceWriter instanceof InstancesWriter){
      InstancesWriter iw =(InstancesWriter)instanceWriter;
      iw.writeInstacnesToFile();
      ((InstancesWriter)instanceWriter).ontoViewer.unloadOntologies();
    }else{
      System.err.println("InstancesWriter object has a wrong type");
    }
  }
  
  public CreoleRuleStore getRuleStore(){
    return ruleStores.get(ruleStoreCB.getSelectedIndex());
  }


  public boolean hasGUI() {
    return m_GUI;
  }


  public HashMap<String, Color> getCurrentOntology2ColorScheme() {
    return ontology2ColorSchemesMap.get(getCurrentOntology());
  }


  public HashMap<String, Boolean> getCurrentOntology2OResourceSelection() {
    return ontology2OResourceSelectionMap.get(getCurrentOntology());
  }


  public OntoTreeModel getCurrentOntoTreeModelProperties() {
    return ontology2OntoTreeModelsProperties.get(getCurrentOntology());
  }


  public OntoTreeModel getCurrentOntoTreeModelClasses() {
    return ontology2OntoTreeModelsClasses.get(getCurrentOntology());
  }


  @Override
  public Component getParent() {
    return this.getGUI();
  }


  @Override
  public Point getLocationOnScreen() {
    return getGUI().getLocationOnScreen();
  }


  @Override
  public Font getFont() {
    return getGUI().getFont();
  }


  @Override
  public FontMetrics getFontMetrics(Font font) {
    return getGUI().getFontMetrics(font);
  }


  @Override
  public List<String> getOntologyEntities() {
    return entityNames;
  }
  

  private void loadOntologyEntities() {
    entityNames= new ArrayList<>();
    for(OResource r : getCurrentOntology().getAllResources()) {
      entityNames.add(r.getURI().toString());
    }

  }





}
