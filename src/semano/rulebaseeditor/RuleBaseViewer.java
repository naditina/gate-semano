/*
 *  OntologyViewer.java
 *
 *  Niraj Aswani, 12/March/07
 *
 *  $Id: OntologyViewer.html,v 1.0 2007/03/12 16:13:01 niraj Exp $
 */
package semano.rulebaseeditor;

import gate.Annotation;
import gate.Gate;
import gate.Resource;
import gate.creole.AbstractVisualResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.ontology.OResource;
import gate.creole.ontology.Ontology;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;
import gate.swing.ColorGenerator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import semano.ontologyowl.ONodeIDImpl;
import semano.ontoviewer.OntologyTreeListenerBasic;
import semano.ontoviewer.OntologyTreePanel;
import semano.ontoviewer.Viewer;
import semano.rulestore.AnnotationRule;
import semano.rulestore.Japelate;
import semano.rulestore.Parameter;

import com.ontotext.gate.vr.ClassNode;
import com.ontotext.gate.vr.ClassNodeWithParent;
import com.ontotext.gate.vr.IFolder;
import com.ontotext.gate.vr.OntoTreeModel;

/**
 * the basic structure of the ontology viewer manages the selection of
 * the ontology for annotation if there several ontology sets
 * 
 * @author Nadejda Nikitina
 */
public class RuleBaseViewer extends AbstractVisualResource implements
                                                          CreoleListener, Viewer, StoreModificationListener {
  /**
   * Serial version ID
   */
  private static final long serialVersionUID = 3977303230621759543L;

//  public static final String JAPE_JPRULES_ROOT = "data/jprules/";
//
//  public static final String JPRULE_RELATIONS_DIR = JAPE_JPRULES_ROOT
//          + "relations/";
//
//  public static final String JPRULE_CONCEPTS_DIR = JAPE_JPRULES_ROOT
//          + "concepts/";

  public CreoleRuleStore ruleStore;

  private JapelateTreePanel japelatesTreePanel;

  /** Instance of OntologyTreePanel */
  private OntologyTreePanel ontologyTreePanelClasses;
  
  /** Instance of OntologyTreePanel for properties*/
  private OntologyTreePanel ontologyTreePanelProperties;
  
  private OntoTreeModel ontoTreeModelsClasses;
  private OntoTreeModel ontoTreeModelsRelations;
  private HashMap<String, Color> ontologyColorScheme = new HashMap<>();
  protected HashMap<String, Boolean> entitySelectionMap = new HashMap<>();
  protected HashMap<String, Boolean> japelateSelectionMap = new HashMap<>();
  // protected JButton dissmissBtn;

  /** Main panel which holds all different components */
  private JPanel mainPanel;

  /**
   * RESOURCES...
   */

  java.net.URL editURL;

  java.net.URL deleteURL;

  /**
   * Instance of JTabbedPane to show the ontology Viewer and the
   * OntologyViewerOptions
   */
  private JTabbedPane rulesPane;

  private JTable table;

  private DefaultTableModel ruleTableModel;


  private JScrollPane scrollP;

  private List<String> entityNames = new ArrayList<String>();
  
  

  protected final static ColorGenerator colourGenerator = new ColorGenerator();
  
  
  

  public RuleBaseViewer() {
    super();
    String pluginPath="";
    try {
      File pluginDir = new File(Gate.getGateHome().toString()+"/plugins/Semano/");
      pluginPath = pluginDir.getAbsoluteFile().toURI().toURL().toString();      
      if(pluginPath!=null ){
        pluginPath=pluginPath.substring(5, pluginPath.length());
        System.out.println("plugin directory: "+pluginPath);       
      }
      editURL= new File(pluginPath+"pencil.gif").toURI().toURL();
      deleteURL= new File(pluginPath+"delete.gif").toURI().toURL();
      System.out.println("loading icons from "+editURL.toString());
    } catch(MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public int getX() {
    return table.getX();

  }

  private void loadTableModel(Collection<AnnotationRule> rules) {
    String[] columnNames =
            {"", "", "Rule Name", "Japelate", "Parameters", "Entity Name",
                "Ontology"};
    ArrayList<Object[]> data = new ArrayList<Object[]>();
    for(AnnotationRule annotationRule : rules) {
      Japelate japelate = annotationRule.getJapelate();
      if(japelate != null) {
        String japelateName = japelate.getName();
        String entity = new ONodeIDImpl(annotationRule.getClas(), false)
        .getResourceName();
        if(japelateSelectionMap != null
                && japelateSelectionMap.containsKey(japelateName)
                && japelateSelectionMap.get(japelateName)) {
          if(entitySelectionMap!=null && entitySelectionMap.containsKey(entity) && 
                  entitySelectionMap.get(entity)){
          // JButton editB = new JButton("edit");
          String paramListHTML = formatParameterList(annotationRule);

          ImageIcon editIcon = null;
          if(editURL != null) {
            editIcon = new ImageIcon(editURL);
          }
          JButton editB =
                  (editIcon != null) ? new JButton(editIcon) : new JButton(
                          "edit");
          editB.setBorder(null);
          editB.addActionListener(new EditAction());

          ImageIcon deleteIcon = null;
          if(deleteURL != null) {
            deleteIcon = new ImageIcon(deleteURL);
          }
          JButton deleteB =
                  (deleteIcon != null) ? new JButton(deleteIcon) : new JButton(
                          "delete");
          deleteB.setBorder(null);
          deleteB.addActionListener(new DeleteAction());
          Object[] row =
                  {
                      editB,
                      deleteB,
                      annotationRule.getName(),
                      japelateName,
                      paramListHTML,
                      entity, annotationRule.getOntology()};

          data.add(row);
        }
        } 
      }
    }

    ruleTableModel =
            new DefaultTableModel(data.toArray(new Object[][] {}), columnNames) {

              private static final long serialVersionUID = 1L;

              @Override
              public boolean isCellEditable(int row, int column) {
                return false;
              }
            };

    table.setModel(ruleTableModel);
    table.getColumnModel().getColumn(0).setMaxWidth(40);
    table.getColumnModel().getColumn(1).setMaxWidth(40);
    table.getColumnModel().getColumn(2).setMaxWidth(100);
    table.getColumnModel().getColumn(2).setPreferredWidth(100);
    table.getColumnModel().getColumn(3).setMaxWidth(200);
    table.getColumnModel().getColumn(3).setPreferredWidth(200);
    table.getColumnModel().getColumn(4).setMaxWidth(500);
    table.getColumnModel().getColumn(4).setPreferredWidth(500);
    table.getColumnModel().getColumn(5).setMaxWidth(300);
    table.getColumnModel().getColumn(5).setPreferredWidth(250);
    table.getColumnModel().getColumn(6).setPreferredWidth(50);
    TableRowSorter<TableModel> rowSorter =
            new TableRowSorter<TableModel>(ruleTableModel);
    // rowSorter.setComparator(2,new AlphaNumericComparator(true));
    table.setRowSorter(rowSorter);
  }

  private String formatParameterList(AnnotationRule annotationRule) {
    StringBuilder paramListHTML = new StringBuilder();
    paramListHTML.append("<html>");
    List<Parameter> paramsJapelate =
            annotationRule.getJapelate().getParamList();
    List<String> paramsRule = annotationRule.getParameters();
    if(paramsRule.size() >= paramsJapelate.size()) {
      // start at 2 to avoid ontology and rule name printing
      // again....
      for(int i = AnnotationRule.MINIMUM_PARAMETER_NUMBER; i < paramsJapelate.size(); i++) {
        String separator = "";
        if(i != paramsRule.size() - 1) separator = ", &nbsp;&nbsp;&nbsp;";
        Parameter paramJ = paramsJapelate.get(i);
        String paramR = paramsRule.get(i);
        formatParameterPair(paramListHTML, separator, paramJ, paramR);
      }
      if(paramsRule.size() > paramsJapelate.size()){
        for(int i = paramsJapelate.size(); i < paramsRule.size(); i++) {
          String separator = "";
          if(i != paramsRule.size() - 1) separator = ", &nbsp;&nbsp;&nbsp;";
          Parameter paramJ = paramsJapelate.get(paramsJapelate.size()-1);
          String paramR = paramsRule.get(i);
          formatParameterPair(paramListHTML, separator, paramJ, paramR);
        }
      }
    } else {
      System.err.println("too few parameters for japelate "
              + annotationRule.getJapelate().getName() + " in rule "
              + annotationRule.getName());
    }
    paramListHTML.append("</html>");
    return paramListHTML.toString();

  }

  public void formatParameterPair(StringBuilder paramListHTML,
          String separator, Parameter paramJ, String paramR) {
    if(paramR.trim().equals("")) paramR = "EMPTY";
    switch(paramJ.getType()) {
      case LITERAL:
        paramListHTML.append("<font color='blue'>" + paramR + "</font>"
                + separator);
        break;
      case ONTOLOGY_ENTITY:
        paramListHTML.append("<font color='red'>"
                + (new ONodeIDImpl(paramR, false)).getResourceName()
                + "</font>" + separator);
        break;
    }
  }

  /** Initialises the GUI */
  public void initGUI() {
    mainPanel = new JPanel();
    JPanel southPanel = new JPanel();
    // buttonPanel.setBackground(UIManager.getLookAndFeelDefaults().getColor(
    // "ToolTip.background"));

    JPanel buttonPanel = new JPanel(new FlowLayout());
    southPanel.add(buttonPanel, BorderLayout.SOUTH);

    table = new JTable();
    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int column = table.getColumnModel().getColumnIndexAtX(e.getX()); // get
                                                                         // the
                                                                         // coloum
                                                                         // of
                                                                         // the
                                                                         // button
        int row = e.getY() / table.getRowHeight(); // get the row of the
                                                   // button

        /* Checking the row or column is valid or not */
        if(row < table.getRowCount() && row >= 0
                && column < table.getColumnCount() && column >= 0) {
          Object value = table.getValueAt(row, column);
          if(value instanceof JButton) {
            /* perform a click event */
            ((JButton)value).doClick();
          }
        }

      }
    });
    loadTableModel(new HashSet<AnnotationRule>());
    table.setDefaultRenderer(Object.class, new MyCellRenderer());
    table.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 16));
    // table.setPreferredSize(new Dimension(1200,1200));
    // table.setAutoCreateColumnsFromModel(true);
    // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    // table.setPreferredScrollableViewportSize(new Dimension(1200,
    // 70));
    // table.setFillsViewportHeight(true);
    // table.setAutoscrolls(true);

    JButton addButton = new JButton("Add Rule");
    buttonPanel.add(addButton);

    JButton saveButton = new JButton("Save");
    buttonPanel.add(saveButton);
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ruleStore.saveRules();
      }
    });

    southPanel.add(buttonPanel);
    JTabbedPane rightTabbedPane = new JTabbedPane();
    rightTabbedPane.setMinimumSize(new Dimension(250, 200));
    rightTabbedPane.setPreferredSize(new Dimension(250, 200));
    this.japelatesTreePanel = new JapelateTreePanel(this);

    // lets create the OntologyTree Component
    ontologyTreePanelClasses = new OntologyTreePanel(this, false);
    OntologyTreeListenerBasic ontoTreeListenerClasses = new OntologyTreeListenerBasic(ontologyTreePanelClasses, this);
    ontologyTreePanelClasses.setListener(ontoTreeListenerClasses );
    ontologyTreePanelProperties = new OntologyTreePanel(this, true);
    OntologyTreeListenerBasic ontoTreeListenerProperties = new OntologyTreeListenerBasic(ontologyTreePanelProperties, this);
    ontologyTreePanelProperties.setListener(ontoTreeListenerProperties);

    rightTabbedPane.addTab("Japelates", japelatesTreePanel);
    rightTabbedPane.addTab("Ontology Classes", ontologyTreePanelClasses);
    rightTabbedPane.addTab("Ontology Relations", ontologyTreePanelProperties);

    mainPanel.setLayout(new BorderLayout());
    scrollP = new JScrollPane(table);
    mainPanel.add(scrollP, BorderLayout.CENTER);
    mainPanel.add(rightTabbedPane, BorderLayout.EAST);
    mainPanel.add(southPanel, BorderLayout.SOUTH);
    mainPanel.setBackground(Color.white);
    this.setLayout(new java.awt.BorderLayout());
    this.add(mainPanel, java.awt.BorderLayout.CENTER);
    // it is important to call this last as it needs getGUI()
    addButton.addActionListener(new AnnotationRuleEditor(RuleBaseViewer.this, ruleStore));
  }

  public Component getGUI() {
    return table;
  }

  /**
   * Releases all resources and listeners
   */
  public void cleanup() {
    Gate.getCreoleRegister().removeCreoleListener(this);
    // remove the annotationSetListener as well
  }

  public void setTarget(Object target) {
    this.ruleStore = (CreoleRuleStore)target;
    ruleStore.registerStoreModificationListener(this);
    updateJapelateData();
    initOntologyModels();
    loadOntologyEntities();
    refleshRuleTable();

  }

  private void initOntologyModels() {
   ontologyColorScheme = new HashMap<String, Color>();
    entitySelectionMap =
      new HashMap<String, Boolean>();
    ontoTreeModelsClasses=updateOntologyData(false, ontologyColorScheme, entitySelectionMap);
    ontoTreeModelsRelations=updateOntologyData( true, ontologyColorScheme, entitySelectionMap);       
    ontologyTreePanelClasses.showOntologyInOntologyTreeGUI(getCurrentOntology(), new LinkedHashMap<String, LinkedHashSet<Annotation>>());
    ontologyTreePanelProperties.showOntologyInOntologyTreeGUI(getCurrentOntology(), new LinkedHashMap<String, LinkedHashSet<Annotation>>());
    
  }

  private OntoTreeModel updateOntologyData(
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
    return new OntoTreeModel(rootClasses);
  }
  
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
  
  

  private void loadOntologyEntities() {
    for(OResource r : ruleStore.getOntology().getAllResources()) {
      entityNames.add(r.getURI().toString());
    }

  }

  static class MyCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
      Component tableCellRendererComponent =
              super.getTableCellRendererComponent(table, value, isSelected,
                      hasFocus, row, column);
      if(value instanceof JButton) {
        JButton button = (JButton)value;
        return button;
      }
      if(value instanceof JTextField) {
        JTextField button = (JTextField)value;
        return button;
      }
      if(value instanceof JComboBox) {
        JComboBox button = (JComboBox)value;
        return button;
      }
      return tableCellRendererComponent;
    }

  }

  @Override
  public void resourceLoaded(CreoleEvent e) {
  }

  @Override
  public void resourceUnloaded(CreoleEvent e) {
  }

  @Override
  public void datastoreOpened(CreoleEvent e) {
  }

  @Override
  public void datastoreCreated(CreoleEvent e) {

  }

  @Override
  public void datastoreClosed(CreoleEvent e) {
  }

  @Override
  public void resourceRenamed(Resource resource, String oldName, String newName) {
  }

  public Resource init() throws ResourceInstantiationException {
    this.initGUI();
    return this;
  }// init()

  /**
   * Deletes row from the table.... TODO: implement confirm delete?
   * 
   * @author davidberry
   * 
   */
  class DeleteAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      int[] selectedRows = table.getSelectedRows();

      boolean rowsSelected = selectedRows.length != 0;
      if(rowsSelected) {
        if(selectedRows.length > 1) Arrays.sort(selectedRows);
        // need to sort in order to remove last first, so indices are
        // not moved by the deletion...
        for(int i = selectedRows.length - 1; i >= 0; i--) {
          DefaultTableModel defaultTableModel =
                  (DefaultTableModel)table.getModel();
          String ruleID =
                  (String)defaultTableModel.getValueAt(selectedRows[i], 2);
          ruleStore.deleteRule(ruleID);
          defaultTableModel.removeRow(selectedRows[i]);

        }

      }
    }
  }

  class EditAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      int[] selectedRows = table.getSelectedRows();

      boolean rowsSelected = selectedRows.length != 0;
      if(rowsSelected) {
        if(selectedRows.length > 1) {
          // warn that only one row can be edited at a time.
          return;
        }        
        String ruleID =
                (String)table.getValueAt(selectedRows[0], 2);
        AnnotationRuleEditor ae = new AnnotationRuleEditor(RuleBaseViewer.this, ruleStore);
        ae.editAnnotationRule(ruleStore.getRule(ruleID));
      }

    }
  }

  private void updateJapelateData() {
    if(ruleStore == null) {
      return;
    }
    // HashMap<String, Color> newColorScheme = new HashMap<String,
    // Color>();
    this.japelateSelectionMap = new HashMap<String, Boolean>();

    for(String jname : ruleStore.getJapelates().keySet()) {
      japelateSelectionMap.put(jname, true);
    }
    japelatesTreePanel.showJapelates(ruleStore.getJapelates().keySet()
            .toArray(new String[] {}));
  }

  public void refleshRuleTable() {
    loadTableModel(ruleStore.getRules());
  }

  public List<String> getOntologyEntities() {
    return entityNames;
  }

  @Override
  public Ontology getCurrentOntology() {
    return ruleStore.getOntology();
  }

  @Override
  public void removeHighlights() {
    refleshRuleTable(); 
    
  }

  @Override
  public HashMap<String, Color> getCurrentOntology2ColorScheme() {
    return ontologyColorScheme;
  }

  @Override
  public HashMap<String, Boolean> getCurrentOntology2OResourceSelection() {
    return entitySelectionMap;
  }

  @Override
  public OntoTreeModel getCurrentOntoTreeModelProperties() {
    return ontoTreeModelsRelations;
  }

  @Override
  public OntoTreeModel getCurrentOntoTreeModelClasses() {
    return ontoTreeModelsClasses;
  }

  @Override
  public void refreshHighlights() {
    refleshRuleTable();     
  }

  @Override
  public void storeChanged() {
    refleshRuleTable();   
    
  }

}
