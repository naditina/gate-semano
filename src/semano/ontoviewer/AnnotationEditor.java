package semano.ontoviewer;

import gate.Annotation;
import gate.AnnotationSet;
import gate.FeatureMap;
import gate.Main;
import gate.creole.AnnotationSchema;
import gate.creole.FeatureSchema;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.OResource;
import gate.creole.ontology.RDFProperty;
import gate.gui.MainFrame;
import gate.util.GateException;
import gate.util.GateRuntimeException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;

import org.xhtmlrenderer.util.GeneralUtil;

import semano.rulebaseeditor.AnnotationRuleEditor;
import semano.rulebaseeditor.TreePanel;
import semano.rulestore.AnnotationRule;
import semano.util.FileAndDatastructureUtil;
import semano.util.OntologyUtil;
import semano.util.Settings;

import com.ontotext.gate.vr.ClassNode;
import com.ontotext.gate.vr.IFolder;
import com.ontotext.gate.vr.OntoTreeModel;

/**
 * @author Nadejda Nikitina
 * 
 */
public class AnnotationEditor extends AbstractAction {

  private static final boolean ADD_ALL_CASE_SENSITIVE = true;

  protected JDialog annotationWindow;

  protected JPanel pane;

  protected JComboBox classCombo;

  protected JComboBox domainAnnotationCombo;

  protected JComboBox rangeAnnotationCombo;

  protected JComboBox propertiesCombo;

  protected JCheckBox antipatternCkb;
  
  protected JScrollPane scroller;

  protected FeaturesEditor featuresEditor;

  protected JPanel scrollerPanel;

  private OntologyViewer viewer;

  protected OntologyTreePanel ontologyTreePanelClasses;

  protected OntologyTreePanel ontologyTreePanelProperties;

  protected DeleteAnnotationAction deleteAnnotationAction;

  protected MakeAntiPatternAction antipatternAcion;
  

  protected ReannotateAction reannotateAction;

  protected JButton cancel, deleteBtn, editRuleBtn, reannotateBtn, OKBtn;

  private int textLocation;

  private Point mousePoint;


  private AddChangeAnnotationAction addChangeAnnotationAction;

  private int selectedAnnotationIndex = 0;

  private int iconWidth = 0;

  // ComboBoxModel annoTypeModel;

  ComboBoxModel classmodel;

  ComboBoxModel propertymodel;

  ComboBoxModel domainAnnotationModel;

  ComboBoxModel rangeAnnotationModel;

  boolean explicitCall = false;

  private JTabbedPane comboboxpane;

  private FeatureMap tempFeatureMap;

  private JPanel propertiesTab;

  // private ButtonGroup radioButtonsGroup;

  private JPanel classesTab;

  private Collection<String> annotationStrings;

  private Annotation currentAnnotation;

  /**
   * Constructor
   * 
   * @param viewer
   * 
   * @param ontoTreePanel
   */
  public AnnotationEditor(OntologyTreePanel ontoTreePanelClasses,
          OntologyTreePanel ontologyTreePanelProperties, OntologyViewer viewer) {
    this.ontologyTreePanelClasses = ontoTreePanelClasses;
    this.ontologyTreePanelProperties = ontologyTreePanelProperties;
    this.viewer = viewer;
    initGUI();
  }

  private void initGUI() {
    annotationWindow =
            new JDialog(
                    SwingUtilities
                            .getWindowAncestor(viewer.documentTextualDocumentView
                                    .getGUI()));
    DragListener listener = new DragListener();
    annotationWindow.addMouseListener(listener);
    annotationWindow.addMouseMotionListener(listener);

    pane = new JPanel();
    pane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    pane.setLayout(new GridBagLayout());
    pane.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    annotationWindow.setContentPane(pane);

//    JPanel buttonPanel = new JPanel(/* new FlowLayout(FlowLayout.LEFT) */);
//    buttonPanel.setBackground(UIManager.getLookAndFeelDefaults().getColor(
//            "ToolTip.background"));

//    pane.add(buttonPanel, constraints);
    //
    // soelAction = new StartOffsetExtendLeftAction(MainFrame
    // .getIcon("extend-left"));
    // soelBtn = new JButton(soelAction);
    // soelBtn.setBorderPainted(false);
    // soelBtn.setContentAreaFilled(false);
    // soelBtn.setMargin(new Insets(0, 0, 0, 0));
    // soelBtn.setToolTipText("Extend StartOffset");
    // buttonPanel.add(soelBtn);
    //
    // soerAction = new StartOffsetExtendRightAction(MainFrame
    // .getIcon("extend-right"));
    // soerBtn = new JButton(soerAction);
    // soerBtn.setBorderPainted(false);
    // soerBtn.setContentAreaFilled(false);
    // soerBtn.setMargin(new Insets(0, 0, 0, 0));
    // soerBtn.setToolTipText("Shrink StartOffset");
    // buttonPanel.add(soerBtn);

  
    //
    // eoelAction = new
    // EndOffsetExtendLeftAction(MainFrame.getIcon("extend-left"));
    // eoelBtn = new JButton(eoelAction);
    // eoelBtn.setBorderPainted(false);
    // eoelBtn.setContentAreaFilled(false);
    // eoelBtn.setMargin(new Insets(0, 0, 0, 0));
    // eoelBtn.setToolTipText("Shrink EndOffset");
    // buttonPanel.add(eoelBtn);
    //
    // eoerAction = new EndOffsetExtendRightAction(MainFrame
    // .getIcon("extend-right"));
    // eoerBtn = new JButton(eoerAction);
    // eoerBtn.setBorderPainted(false);
    // eoerBtn.setContentAreaFilled(false);
    // eoerBtn.setMargin(new Insets(0, 0, 0, 0));
    // eoerBtn.setToolTipText("Extend EndOffset");
    // buttonPanel.add(eoerBtn);

    // applyToAll = new JCheckBox("Apply To All");
    // applyToAll.setBorderPainted(false);
    // applyToAll.setContentAreaFilled(false);
    // applyToAll.setMargin(new Insets(0, 0, 0, 0));
    // applyToAll.setToolTipText("Apply to All");
    // applyToAll.setSelected(Settings.DEFAULT_APPLYTOALL);
    // buttonPanel.add(applyToAll);
    
    // radioButtonsGroup = new ButtonGroup();
    // synonym = createRadioButton("Synonym", buttonPanel,
    // radioButtonsGroup);
    // acronym = createRadioButton("Acronym", buttonPanel,
    // radioButtonsGroup);
    // expression = createRadioButton(Settings.EXPRESSION_GUI_LABEL,
    // buttonPanel, radioButtonsGroup);
    // formula = createRadioButton("Chemical Formula", buttonPanel,
    // radioButtonsGroup);
    // passage = createRadioButton("Text passage", buttonPanel,
    // radioButtonsGroup);
    // newclass =
    // createRadioButton(Settings.NEW_CLASS_PROPERTY_GUI_LABEL,
    // buttonPanel, radioButtonsGroup);
    // expression.setSelected(true);
    // createInstance = createRadioButton("Instance", buttonPanel,
    // radioButtonsGroup);
    // createInstance.setVisible(false);
    // synonym.addItemListener(new RadioButtonListener());
    // acronym.addItemListener(new RadioButtonListener());
    // expression.addItemListener(new RadioButtonListener());
    // formula.addItemListener(new RadioButtonListener());
    // passage.addItemListener(new RadioButtonListener());
    // newclass.addItemListener(new RadioButtonListener());
    // annoTypeCombo = new JComboBox();
    // for(AnnotationMetaData ap:Settings.annotationProperties){
    // annoTypeCombo.addItem(ap.getGuiLabel());
    // }
    // annoTypeCombo.setEditable(false);
    // annoTypeCombo.setBackground(UIManager.getLookAndFeelDefaults().getColor(
    // "ToolTip.background"));
    // buttonPanel.add(annoTypeCombo);
    // annoTypeCombo.addItemListener(new AnnoTypeListener());

    // Icon icon = UIManager.getIcon("InternalFrame.closeIcon");
    // if(icon == null) icon = MainFrame.getIcon("exit");
    // dissmissAction = new CancelAction(icon);
    // dissmissBtn = new JButton(dissmissAction);
    // constraints.insets = new Insets(0, 10, 0, 0);
    // constraints.anchor = GridBagConstraints.NORTHEAST;
    // constraints.weightx = 1;
    // dissmissBtn.setBorder(null);
    // pane.add(dissmissBtn, constraints);
    Insets insets0 = new Insets(0, 0, 0, 0);
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.gridwidth = 1;
    constraints.gridy = 0;
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.insets = insets0;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.insets = insets0;

    // comboboxes
    classCombo = new JComboBox();
    // classCombo.addActionListener(addChangeAnnotationAction);
    classCombo.setRenderer(new ComboRenderer(ontologyTreePanelClasses));
    classCombo.setEditable(true);
    classCombo.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    classCombo
            .getEditor()
            .getEditorComponent()
            .addKeyListener(
                    new ComboKeyListener(classCombo, ontologyTreePanelClasses));
    classCombo.setPreferredSize(new Dimension(Settings.COMBOBOX_W,
            Settings.COMBOBOX_H));

    // domainAnnotationCombo combo
    domainAnnotationCombo = new JComboBox();
    // domainAnnotationCombo.setRenderer(new
    // ComboRenderer(ontologyTreePanelClasses));
    domainAnnotationCombo.setEditable(true);
    domainAnnotationCombo.setBackground(UIManager.getLookAndFeelDefaults()
            .getColor("ToolTip.background"));
    domainAnnotationCombo.setPreferredSize(new Dimension(Settings.COMBOBOX_W,
            Settings.COMBOBOX_H));
    JPanel domainAnnotationPane = new JPanel(new BorderLayout());
    domainAnnotationPane.setBackground(UIManager.getLookAndFeelDefaults()
            .getColor("ToolTip.background"));
    domainAnnotationPane.add(new JLabel(
            Settings.COMBOTEXTDOMAINANNOATTION_GUI_LABEL), BorderLayout.NORTH);
    domainAnnotationPane.add(domainAnnotationCombo, BorderLayout.SOUTH);

    // rangeAnnotationCombo combo
    rangeAnnotationCombo = new JComboBox();
    // rangeAnnotationCombo.setRenderer(new
    // ComboRenderer(ontologyTreePanelClasses));
    rangeAnnotationCombo.setEditable(true);
    rangeAnnotationCombo.setBackground(UIManager.getLookAndFeelDefaults()
            .getColor("ToolTip.background"));
    rangeAnnotationCombo.setPreferredSize(new Dimension(Settings.COMBOBOX_W,
            Settings.COMBOBOX_H));
    JPanel rangeAnnotationPane = new JPanel(new BorderLayout());
    rangeAnnotationPane.setBackground(UIManager.getLookAndFeelDefaults()
            .getColor("ToolTip.background"));
    rangeAnnotationPane.add(new JLabel(
            Settings.COMBOTEXTRANGEANNOATTION_GUI_LABEL), BorderLayout.NORTH);
    rangeAnnotationPane.add(rangeAnnotationCombo, BorderLayout.SOUTH);

    // properties combo
    propertiesCombo = new JComboBox();
    // propertiesCombo.addActionListener(addChangeAnnotationAction);
    propertiesCombo.setRenderer(new ComboRenderer(ontologyTreePanelProperties));
    propertiesCombo.setEditable(true);
    propertiesCombo.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    propertiesCombo
            .getEditor()
            .getEditorComponent()
            .addKeyListener(
                    new ComboKeyListener(propertiesCombo,
                            ontologyTreePanelProperties));
    propertiesCombo.setPreferredSize(new Dimension(Settings.COMBOBOX_W,
            Settings.COMBOBOX_H));
    // /////////////
    // /// combobox structures
    //
    comboboxpane = new JTabbedPane();
    comboboxpane.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));

    // //////
    // classes tab

    classesTab = new JPanel(new BorderLayout());
    classesTab.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    // we use this additional Pane to let the combo be always in the top
    JPanel constantPartClasses = new JPanel(new BorderLayout());
    // constantPartClasses.setSize(20,20);
    constantPartClasses.setBackground(UIManager.getLookAndFeelDefaults()
            .getColor("ToolTip.background"));
    JLabel usageInstructions = new JLabel(Settings.COMBOTEXTCLASSES_GUI_LABEL);
    usageInstructions.setBackground(UIManager.getLookAndFeelDefaults()
            .getColor("ToolTip.background"));
    constantPartClasses.add(usageInstructions, BorderLayout.NORTH);
    constantPartClasses.add(classCombo, BorderLayout.SOUTH);
    classesTab.add(constantPartClasses, BorderLayout.NORTH);

    // ///////
    // properties tab

    propertiesTab = new JPanel(new BorderLayout());
    propertiesTab.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    JPanel constantPart = new JPanel(new BorderLayout());
    constantPart.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    JLabel usageInstructions2 =
            new JLabel(Settings.COMBOTEXTPROPERTIES_GUI_LABEL);
    usageInstructions2.setBackground(UIManager.getLookAndFeelDefaults()
            .getColor("ToolTip.background"));
    constantPart.add(usageInstructions2, BorderLayout.NORTH);
    constantPart.add(propertiesCombo, BorderLayout.SOUTH);
    propertiesTab.add(constantPart, BorderLayout.NORTH);

    JPanel domainrangeAnnotations = new JPanel(new BorderLayout());
    domainrangeAnnotations.setBackground(UIManager.getLookAndFeelDefaults()
            .getColor("ToolTip.background"));
    domainrangeAnnotations.add(domainAnnotationPane, BorderLayout.NORTH);
    domainrangeAnnotations.add(rangeAnnotationPane, BorderLayout.SOUTH);
    propertiesTab.add(domainrangeAnnotations, BorderLayout.SOUTH);

    comboboxpane.add(classesTab, Settings.CLASSES_GUI_LABEL);
    comboboxpane.add(propertiesTab, Settings.PROPERTIES_GUI_LABEL);

    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridy = 1;
    constraints.gridwidth = 1;
    constraints.weightx = 1;
    constraints.insets = new Insets(3, 2, 2, 2);
    pane.add(comboboxpane, constraints);
    // pane.add(classCombo, constraints);
    // constraints.gridx=2;
    // constraints.gridy = 2;
    // pane.add(propertiesCombo, constraints);

    featuresEditor = new FeaturesEditor();
    featuresEditor.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    featuresEditor.init();

    scrollerPanel = new JPanel(new BorderLayout());
    scrollerPanel.add(featuresEditor.getTable(), BorderLayout.CENTER);
    scroller = new CustomScroller(scrollerPanel);
    iconWidth = MainFrame.getIcon("delete").getIconWidth();
    constraints.gridy = 2;
    constraints.weighty = 1;
    constraints.fill = GridBagConstraints.BOTH;
    pane.add(scroller, constraints);

    //buttons
    constraints.gridy = 3;
    constraints.weighty = 1;
    constraints.fill = GridBagConstraints.BOTH;
    JPanel buttonPanel = new JPanel(new FlowLayout());
    buttonPanel.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    
    // OK Button
    addChangeAnnotationAction = new AddChangeAnnotationAction();
    OKBtn = new JButton("OK");
    OKBtn.setAction(addChangeAnnotationAction);
    OKBtn.setBorderPainted(true);
//    OKBtn.setContentAreaFilled(true);
    OKBtn.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    // OKBtn.setMargin(new Insets(0, 0, 0, 0));
    OKBtn.setText("OK");
    OKBtn.setToolTipText("Create or cahnge annotation");
    buttonPanel.add(OKBtn);
    cancel = new JButton("Cancel");
    cancel.setAction(new CancelAction());
    cancel.setBorderPainted(true);
    buttonPanel.add(cancel);
    deleteAnnotationAction =
            new DeleteAnnotationAction();
    deleteBtn = new JButton(deleteAnnotationAction);
    deleteBtn.setBorderPainted(true);
//    deleteBtn.setContentAreaFilled(true);
    deleteBtn.setText("Delete");
    deleteBtn.setToolTipText("Delete Annotation");
    buttonPanel.add(deleteBtn);
    

    EditRuleAction editRuleAction =
            new EditRuleAction("Edit Rule");
    editRuleBtn = new JButton(editRuleAction);
    editRuleBtn.setBorderPainted(true);
//    editRuleBtn.setContentAreaFilled(true);
    editRuleBtn.setText("Edit Rule");
    editRuleBtn.setToolTipText("Edit Rule");
    buttonPanel.add(editRuleBtn);

    reannotateAction =
            new ReannotateAction("Re-run Annotation");
    reannotateBtn = new JButton(reannotateAction);
    reannotateBtn.setBorderPainted(true);
//    reannotateBtn.setContentAreaFilled(true);
    reannotateBtn.setText("Re-run Annotation");
    reannotateBtn.setToolTipText("Re-run annotation");
    buttonPanel.add(reannotateBtn);

    antipatternAcion = new MakeAntiPatternAction("Antipattern");
    antipatternCkb = new JCheckBox(antipatternAcion);
    antipatternCkb.setContentAreaFilled(false);
    antipatternCkb
            .setToolTipText("report a false pattern match");
    buttonPanel.add(antipatternCkb);
    antipatternCkb.setSelected(false);
    
    
    pane.add(buttonPanel, constraints);
  }

  // private JRadioButton createRadioButton(String name, JPanel
  // buttonPanel,
  // ButtonGroup group) {
  // JRadioButton resultButton = new JRadioButton(name);
  // resultButton.setBorderPainted(false);
  // resultButton.setContentAreaFilled(false);
  // resultButton.setMargin(new Insets(0, 0, 0, 0));
  // resultButton.setToolTipText(name);
  // buttonPanel.add(resultButton);
  // group.add(resultButton);
  // return resultButton;
  // }

  protected class CustomScroller extends JScrollPane {
    public CustomScroller(Component component) {
      super(component);
    }

    public Dimension getPreferredSize() {
      return new Dimension((int)scrollerPanel.getPreferredSize().getWidth()
              + iconWidth * 2, (int)scrollerPanel.getPreferredSize()
              .getHeight() * 2 / 3);
      // + iconWidth * 2, 130);

    }
  }

  private void enableDisableComponents(boolean isNewAnnotationMode,
          boolean manualannotation) {
    if(isNewAnnotationMode) {
      deleteBtn.setEnabled(false);
      scroller.setEnabled(true);
      antipatternCkb.setEnabled(false);
    } else {
      deleteBtn.setEnabled(true);
      scroller.setEnabled(true);
      if(manualannotation)
        antipatternCkb.setEnabled(false);
      else antipatternCkb.setEnabled(true);
    }
  }

  /**
   * is called every time a mouse is rolled over any text
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent ae) {
    JTextArea textPane = viewer.documentTextArea;
    // we check here if there is any text selected
    String selectedText = textPane.getSelectedText();
//    newAnnotationMode = false;
    
      // yes it is put on the highlighted annotation so show the
      // annotation window
      final ArrayList<Annotation> annotations =
              viewer.annotationManager.getHighlightedAnnotations(textLocation);
      if(annotations.size() > 0) {
        if(ontologyTreePanelClasses.showingAnnotationWindow) {
          gate.Annotation annotation = annotations.get(0);
          try {
            Rectangle startRect =
                    textPane.modelToView(annotation.getStartNode().getOffset()
                            .intValue());
            Point topLeft = textPane.getLocationOnScreen();
            FontMetrics fm = textPane.getFontMetrics(textPane.getFont());
            int charHeight = fm.getAscent() + fm.getDescent();

            int x = topLeft.x + startRect.x;
            int y = topLeft.y + startRect.y + charHeight;

            if(annotationWindow.getX() == x && annotationWindow.getY() == y) {
              // do nothing
              return;
            }
          } catch(BadLocationException e1) {
            throw new GateRuntimeException("Can't show the popup window", e1);
          }
        }

        // find out classes
        final ArrayList<String> classValues = new ArrayList<String>();
        final ArrayList<Boolean> isClass = new ArrayList<Boolean>();
        for(Annotation currentAnnotation : annotations) {
          isClass.add(OntologyAnnotation.isSourceAClass(currentAnnotation));
          classValues.add(OntologyAnnotation.getSourceFeatureValue(
                  ontologyTreePanelClasses.getCurrentOntology(),
                  currentAnnotation));
        }
        // exactly one annotation is in the area
        if(classValues.size() == 1) {
          selectedAnnotationIndex = annotations.get(0).getId();
          showWindow();
          return;
        }
        // else more than one annotation is in the area
        // so before showing window we need to list all the available
        // classes/properties of the annotations
        selectedAnnotationIndex = annotations.get(0).getId();
        final JPopupMenu classLists = new JPopupMenu();
        classLists.setLayout(new GridLayout(classValues.size(), 1));
        for(int i = 0; i < classValues.size(); i++) {
          Icon icon =
                  isClass.get(i).booleanValue() ? MainFrame
                          .getIcon("ontology-class") : MainFrame
                          .getIcon("ontology-property");
          JMenuItem button = new JMenuItem(classValues.get(i), icon);
          classLists.add(button);
          button.setActionCommand("" + i);
          button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              final int selectedClassValue =
                      Integer.parseInt(ae.getActionCommand());
              selectedAnnotationIndex =
                      annotations.get(selectedClassValue).getId();
              classLists.setVisible(false);
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  showWindow();
                }
              });
            }
          });
        }

        // and finally show it
        classLists.show(viewer.documentTextArea, (int)mousePoint.getX(),
                (int)mousePoint.getY());

        // else lets make it visible see below
      }
    
  }

  /**
   * THE ONE METHOD!
   */
  private void showWindow() {

    // and lets show it
    final JTextArea textComp = viewer.documentTextArea;
    int x1 = textComp.getSelectionStart();

    createComboboxModel(classCombo, ontologyTreePanelClasses, classmodel);
    createComboboxModel(propertiesCombo, ontologyTreePanelProperties,
            propertymodel);
    initModelsOfAnnotations(x1);

    
      currentAnnotation = getAnnotation(selectedAnnotationIndex);
      boolean manually =
              OntologyAnnotation.isManuallySetAnnotation(currentAnnotation);

      enableDisableComponents(false, manually);

      antipatternCkb.setSelected(OntologyAnnotation
              .isAntipattern(currentAnnotation));

      comboboxpane.setSelectedComponent(OntologyAnnotation
              .isSourceAClass(currentAnnotation) ? classesTab : propertiesTab);
      if(currentAnnotation != null) {
        x1 = currentAnnotation.getStartNode().getOffset().intValue();
        selectNode(currentAnnotation).getSource();
        // data for domain/range Annotations

        Annotation domainAnnotation =
                OntologyAnnotation.getDomainAnnotation(
                        currentAnnotation.getFeatures(),
                        viewer.annotationManager);
        domainAnnotationCombo.setSelectedItem(OntologyAnnotation
                .getSummary(domainAnnotation));
        Annotation rangeAnnotation =
                OntologyAnnotation.getRangeAnnotation(
                        currentAnnotation.getFeatures(),
                        viewer.annotationManager);
        rangeAnnotationCombo.setSelectedItem(OntologyAnnotation
                .getSummary(rangeAnnotation));
        // }

        AnnotationSchema annSchema = new AnnotationSchema();
        Set<FeatureSchema> fsSet = new HashSet<FeatureSchema>();
        annSchema.setFeatureSchemaSet(fsSet);
        featuresEditor.setSchema(annSchema);
        featuresEditor.setTargetFeatures(currentAnnotation.getFeatures());
      }
//    } else { // new Annotation mode
//      enableDisableComponents(newAnnotationMode, true);
//      AnnotationSchema annSchema = new AnnotationSchema();
//      featuresEditor.setSchema(annSchema);
//      tempFeatureMap =
//              viewer.annotationManager.initializeFeatureMap(
//                      viewer.documentTextArea.getSelectedText(), x1);
//      featuresEditor.setTargetFeatures(tempFeatureMap);
//      antipatternCkb.setSelected(false);
//    }

    final int xx = x1;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Rectangle startRect = null;
        Point topLeft = null;
        int charHeight = 0;

        try {
          startRect = textComp.modelToView(xx);
          topLeft = textComp.getLocationOnScreen();

          FontMetrics fm = textComp.getFontMetrics(textComp.getFont());
          charHeight = fm.getAscent() + fm.getDescent();
        } catch(BadLocationException ble) {
          throw new GateRuntimeException("Can't show the window ", ble);
        }

        // final int x = topLeft.x + startRect.x;
        final int x = topLeft.x;

        final int y = topLeft.y + startRect.y + charHeight;

        ontologyTreePanelClasses.showingAnnotationWindow = true;
        annotationWindow.setLocation(x, y);
        annotationWindow.pack();
        annotationWindow.setVisible(true);
      }
    });
  }

  private void initModelsOfAnnotations(int currentOffset) {
    AnnotationSet currentAnnotationSet = viewer.getAnnotationSet();
    // we need strings in the comboboxes! SORTED strings!
    annotationStrings =
            convertAnnotationsToSortedString(currentAnnotationSet,
                    currentOffset);
    // Set<Annotation> propertyDomainClass =
    // getDomainOrRangeAnnotations(currentAnnotation, domains);

    domainAnnotationModel =
            createAndSetAnnotationModel(domainAnnotationCombo,
                    annotationStrings);
    // Set<Annotation> propertyRangeClass =
    // getDomainOrRangeAnnotations(currentAnnotation, ranges);
    rangeAnnotationModel =
            createAndSetAnnotationModel(rangeAnnotationCombo, annotationStrings);

  }

  private Collection<String> convertAnnotationsToSortedString(
          AnnotationSet currentAnnotationSet, int currentOffset) {
    ArrayList<String> result = new ArrayList<String>();
    if(currentAnnotationSet != null) {
      HashMap<Long, ArrayList<String>> annotationStringsToSort =
              new HashMap<Long, ArrayList<String>>();
      for(Annotation a : currentAnnotationSet) {
        if(a.getType().equals(Settings.DEFAULT_ANNOTATION_TYPE)) {
          Long offsetDifference =
                  Math.abs(a.getStartNode().getOffset() - currentOffset);
          // now it can happen that different annotations have the same
          // difference value
          // so don't overwrite the already set values! use lists as
          // values!
          if(!annotationStringsToSort.containsKey(offsetDifference)) {
            annotationStringsToSort.put(offsetDifference,
                    new ArrayList<String>());
          }
          ArrayList<String> annots =
                  annotationStringsToSort.get(offsetDifference);
          annots.add(OntologyAnnotation.getSummary(a));
          // annotationStringsToSort.put(offsetDifference, annots);
        }
      }
      ArrayList<ArrayList<String>> sortedList =
              FileAndDatastructureUtil.sortHashMapByKeys(annotationStringsToSort, true);
      for(ArrayList<String> res : sortedList) {
        result.addAll(res);
      }
    }
    return result;
  }

  /**
   * @param combo
   * @param propertyDomainClass
   * @return
   */
  private ComboBoxModel createAndSetAnnotationModel(JComboBox combo,
          Collection<String> annotationSummaries) {
    String[] nodes = annotationSummaries.toArray(new String[] {});
    ComboBoxModel annotationModel = new DefaultComboBoxModel(nodes);
    combo.setModel(annotationModel);
    return annotationModel;
  }

  /**
   * is called when the antipattern checkbox is clicked
   * 
   * @author naddi
   * 
   */
  protected class MakeAntiPatternAction extends AbstractAction {
    public MakeAntiPatternAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent ie) {
      if(antipatternCkb.isEnabled()) {
        // if an antipattern was declared:
        gate.Annotation selectedAnnotation =
                getAnnotation(selectedAnnotationIndex);
        if(antipatternCkb.isSelected()) {
          selectedAnnotation.getFeatures().put(Settings.ANTIPATTERN,
                  String.valueOf(true));
        } else {
          selectedAnnotation.getFeatures().remove(Settings.ANTIPATTERN);
        }
        selectedAnnotation.setFeatures(viewer.annotationManager
                .updateAnnotationMetadata(selectedAnnotation.getFeatures()));
      }
    }
  }
  
  /**
   * is called when the antipattern checkbox is clicked
   * 
   * @author naddi
   * 
   */
  protected class ReannotateAction extends AbstractAction {
    public ReannotateAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent ie) {
      String ruleID=OntologyAnnotation.getRuleID(
              currentAnnotation);
      viewer.annotationManager.removeAnnotationsWithRuleID(ruleID);
      viewer.getRuleStore().annotateWithRule(ruleID, viewer.getDocument());
      hideWindow();
      viewer.reloadDocument();
    }
  }
  

  /**
   * is called when the antipattern checkbox is clicked
   * 
   * @author naddi
   * 
   */
  protected class EditRuleAction extends AbstractAction {
    public EditRuleAction(String name) {
      super(name);
    }
    public void actionPerformed(ActionEvent ie) {
      String ruleID=OntologyAnnotation.getRuleID(
              currentAnnotation);
      AnnotationRule rule = viewer.getRuleStore().getRule(ruleID);
      if(rule==null){
        Object[] options = new Object[] {"OK"};
        try {
          int confirm =
                  JOptionPane.showOptionDialog(Main.getMainFrame(),
                          "Rule with ID "+ruleID+" does not exist in this rule store.",
                          "Error",
                          JOptionPane.OK_OPTION,
                          JOptionPane.ERROR_MESSAGE, null, options,
                          options[0]);
        } catch(HeadlessException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch(GateException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }else{
      AnnotationRuleEditor ae = new AnnotationRuleEditor(viewer, viewer.getRuleStore());
      ae.editAnnotationRule(rule);
      }
    }
  }

  protected class CancelAction extends AbstractAction {
    public CancelAction() {
      super("Cancel");
    }

    public void actionPerformed(ActionEvent ae) {
      annotationWindow.setVisible(false);
    }
  }
  // what to do when user selects to remove the annotation
  protected class DeleteAnnotationAction extends AbstractAction {

    public DeleteAnnotationAction() {
      super("Delete");
    }

    public void actionPerformed(ActionEvent e) {
      try {
        boolean delete = true;
        
          Object[] options = new Object[] {"YES", "NO"};
          int confirm =
                  JOptionPane.showOptionDialog(Main.getMainFrame(),
                          "Delete Annotation : Are you sure?",
                          "Delete Annotation Confirmation",
                          JOptionPane.YES_NO_CANCEL_OPTION,
                          JOptionPane.QUESTION_MESSAGE, null, options,
                          options[0]);
          if(confirm != JOptionPane.YES_OPTION) {
            delete = false;
          }
        
        if(delete) {
          gate.Annotation annot = getAnnotation(selectedAnnotationIndex);

          if(annot != null) {

            if(OntologyAnnotation.getAnnotationType(annot).equals(
                    Settings.NEWENTITY.toString())) {
              options = new Object[] {"YES", "NO"};
               confirm =
                      JOptionPane.showOptionDialog(Main.getMainFrame(),
                              "Delete all annotations of this element?",
                              "Delete all annotations",
                              JOptionPane.YES_NO_CANCEL_OPTION,
                              JOptionPane.QUESTION_MESSAGE, null, options,
                              options[0]);
              if(confirm == JOptionPane.YES_OPTION) {
                String nodeId = OntologyAnnotation.getSourceFeatureValue(annot);
                viewer.removeAnnotationsWithSource(nodeId);
              }
            }

//            if(applyToAll.isSelected()) {
//              ArrayList<Annotation> annotations = getSimilarAnnotations(annot);
//              for(int i = 0; i < annotations.size(); i++) {
//                viewer.annotationManager.deleteAnnotation(annotations.get(i));
//              }
//            } else {
              viewer.annotationManager.deleteAnnotation(annot);

//            }
            viewer.refreshHighlights();
          }
          hideWindow();
        }

      } catch(Exception e1) {
        e1.printStackTrace();
      }
    }
  }



  protected class AddChangeAnnotationAction extends AbstractAction {
    public void actionPerformed(ActionEvent ie) {
      if(explicitCall) return;
      boolean isProperty = !isClassTabSelected();
//      if(newAnnotationMode) {
//        // ClassNode item = getNotHiddenItem();
//        // if(item == null) return;
//        // ArrayList<Annotation> addedAnnotations =
//        // addAnnotation(isProperty, item);
//        // if(addedAnnotations != null && !addedAnnotations.isEmpty()) {
//        // Annotation addedAnnotation = addedAnnotations.get(0);
//        // selectedAnnotationIndex =addedAnnotation.getId();
//        // }
//        // newAnnotationMode = false;
//        // hideWindow();
//        // return;
//
//      } else {// not new annotation mode
        ClassNode newItem = getNotHiddenItem();
        if(newItem == null) return;
        gate.Annotation selectedAnnotation =
                getAnnotation(selectedAnnotationIndex);

        // apply to all
        HashSet<Annotation> annotations = new HashSet<Annotation>();
        annotations.add(selectedAnnotation);
        

        // update Annotation
        for(Annotation annot : annotations) {
          updateAnnotation(annot, newItem);
        }
        // set text selection
        int cStartOffset =
                selectedAnnotation.getStartNode().getOffset().intValue();
        int cEndOffset = selectedAnnotation.getEndNode().getOffset().intValue();
        viewer.documentTextArea.setSelectionStart(cStartOffset);
        viewer.documentTextArea.setSelectionEnd(cEndOffset);
        viewer.refreshHighlights();
        hideWindow();
        return;
      }
//    }

  }

  //
  //
  //
  // NON-GUI methods
  //
  //

  //
  //
  // /**
  // * @param isProperty
  // * @param item
  // * @return
  // */
  // private ArrayList<Annotation> addAnnotation(boolean isProperty,
  // ClassNode item) {
  // ArrayList<Annotation> addedAnnotations=new ArrayList<Annotation>();
  // //get Data From GUI
  // if(item !=null && item.getSource() instanceof OResource){
  // OResource entity = (OResource)item.getSource();
  // if(isProperty){
  // updateDomainRangeParameters(selectedAnnotationType, entity,
  // tempFeatureMap);
  // }
  // addedAnnotations = viewer.annotationManager
  // .addNewAnnotation(entity, applyToAll.isSelected(), tempFeatureMap,
  // selectedAnnotationType, isProperty);
  // if(addedAnnotations != null && !addedAnnotations.isEmpty()) {
  // if(!isProperty) {
  // viewer.ontoTreeListenerClasses
  // .updateOntologyTreePanelSelection(item);
  // }
  // else {
  // viewer.ontoTreeListenerProperties
  // .updateOntologyTreePanelSelection(item);
  // }
  // }
  // }
  // return addedAnnotations;
  // }

  /**
   * @return
   */
  private boolean isClassTabSelected() {
    return comboboxpane.indexOfTab(Settings.CLASSES_GUI_LABEL) == comboboxpane
            .indexOfComponent(comboboxpane.getSelectedComponent());
  }

  /**
   * @param annotatedEntity
   * @param featureMap
   */
  private void updateDomainRangeParameters(OResource annotatedEntity,
          FeatureMap featureMap) {

    String selectedDA = (String)domainAnnotationCombo.getSelectedItem();
    Integer id1 = OntologyAnnotation.getAnnotationIdFromSummary(selectedDA);
    if(id1 != null) {
      Annotation annotation = getAnnotation(id1);
      if(OntologyUtil.checkDomain(annotatedEntity, annotation)) {
        OntologyAnnotation.setDomainAnnotation(annotation, featureMap);
      }

    }
    String selectedRA = (String)rangeAnnotationCombo.getSelectedItem();
    Integer id = OntologyAnnotation.getAnnotationIdFromSummary(selectedRA);
    if(id != null) {
      Annotation ra = getAnnotation(id);
      if(OntologyUtil.checkRange(annotatedEntity, featureMap, ra)) {
        OntologyAnnotation.setRangeAnnotation(ra, featureMap);
      }
    }
  }

  /**
   * Sets the textLocation
   * 
   * @param textLocation
   */
  public void setTextLocation(int textLocation) {
    this.textLocation = textLocation;
  }

  /**
   * Sets the mouse point
   * 
   * @param point
   */
  public void setMousePoint(Point point) {
    this.mousePoint = point;
  }

  public void hideWindow() {
    if(annotationWindow != null) annotationWindow.setVisible(false);
    ontologyTreePanelClasses.showingAnnotationWindow = false;
  }

  /**
   * Given the annotation, this method returns the annotation with same
   * text and same class feature.
   * 
   * @param annot
   * @return
   */
  private ArrayList<Annotation> getSimilarAnnotations(gate.Annotation annot) {
    ArrayList<Annotation> annotations = new ArrayList<Annotation>();

    String annotSource =
            OntologyAnnotation.getSourceFeatureValue(
                    ontologyTreePanelClasses.getCurrentOntology(), annot);
    String annotValue = getAnnotatedText(annot);

    for(Annotation a : getAnnotations(OntologyAnnotation.isSourceAClass(annot))) {
      String aSource =
              OntologyAnnotation.getSourceFeatureValue(
                      ontologyTreePanelClasses.getCurrentOntology(), a);

      String aValue = getAnnotatedText(a);

      if(ADD_ALL_CASE_SENSITIVE) {
        if(annotSource.equals(aSource) && annotValue.equals(aValue)) {
          annotations.add(a);
        }
      } else {
        if(annotSource.equalsIgnoreCase(aSource)
                && annotValue.equalsIgnoreCase(aValue)) {
          annotations.add(a);
        }
      }
    }
    return annotations;
  }

  /**
   * @return
   */
  private Annotation getAnnotation(int selectedAnnotationIndex) {
    return viewer.annotationManager.getAnnotation(selectedAnnotationIndex);
  }

  private Collection<Annotation> getAnnotations(boolean sourceAClass) {

    return viewer.highlightedAnnotations;

  }

  /**
   * Retrieves the underlying text of the annotation.
   * 
   * @param annot
   * @return
   */
  private String getAnnotatedText(gate.Annotation annot) {
    return viewer
            .getDocument()
            .getContent()
            .toString()
            .substring(annot.getStartNode().getOffset().intValue(),
                    annot.getEndNode().getOffset().intValue());
  }

  /**
   * @param currentAnnotation
   * @return
   */
  private ClassNode selectNode(Annotation currentAnnotation) {
    OntologyAnnotation oa = new OntologyAnnotation(currentAnnotation);
    String annotatedEntity =
            OntologyAnnotation.getSourceFeatureValue(
                    ontologyTreePanelClasses.getCurrentOntology(), oa);
    ClassNode annotatedNode;

    if(oa.isSourceAClass()) {
      annotatedNode =
              setSelectedElement(annotatedEntity, classCombo,
                      ontologyTreePanelClasses);
    } else {
      annotatedNode =
              setSelectedElement(annotatedEntity, propertiesCombo,
                      ontologyTreePanelProperties);
    }
    return annotatedNode;
  }

  /**
   * @param annotatedEntity
   * @param ontologyTreePanel
   * @param combo
   * @return
   */
  private ClassNode setSelectedElement(String annotatedEntity, JComboBox combo,
          OntologyTreePanel ontologyTreePanel) {
    ClassNode annotatedNode;
    annotatedNode = ontologyTreePanel.getFirstNode(annotatedEntity);
    explicitCall = true;
    combo.setSelectedItem(annotatedNode);
    explicitCall = false;
    return annotatedNode;
  }

  /**
   * @param combo
   * @param model
   * @return
   */
  private IFolder createComboboxModel(JComboBox combo,
          OntologyTreePanel ontologyTreePanel, ComboBoxModel model) {

    IFolder rootNode =
            (ClassNode)((OntoTreeModel)ontologyTreePanel.ontologyTree
                    .getModel()).getRoot();
    // iterate through nodes and obtain all classes and instances
    ArrayList<ClassNode> items =
            getClassesAndInstances(rootNode, ontologyTreePanel, "");
    if(items.isEmpty()) {
      return null;
    }

    // lets populate the typeCombo
    ClassNode[] nodes = new ClassNode[items.size()];
    for(int i = 0; i < items.size(); i++) {
      nodes[i] = items.get(i);
    }
    model = new DefaultComboBoxModel(nodes);
    combo.setModel(model);
    return rootNode;
  }

  private boolean isValidDomain(RDFProperty aProp, OInstance inst) {
    Set<OResource> domain = aProp.getDomain();
    if(domain == null || domain.isEmpty()) return true;
    ClassNode inode = ontologyTreePanelClasses.getFirstNode(inst.getName());
    for(OResource res : domain) {
      if(!(res instanceof OClass)) continue;
      ClassNode cnode = ontologyTreePanelClasses.getFirstNode(res.getName());
      if(cnode == null) continue;
      if(!hasChild(cnode, inode)) return false;
    }
    return true;
  }

  private boolean hasChild(ClassNode parent, ClassNode child) {
    if(parent == child) return true;
    if(parent.getChildCount() == 0) return false;
    Iterator iter = parent.getChildren();
    while(iter.hasNext()) {
      ClassNode aChild = (ClassNode)iter.next();
      if(hasChild(aChild, child)) return true;
    }
    return false;
  }

  private boolean isValidRange(RDFProperty aProp, OInstance inst) {
    Set<OResource> range = aProp.getRange();
    if(range == null || range.isEmpty()) return true;
    ClassNode inode = ontologyTreePanelClasses.getFirstNode(inst.getName());
    for(OResource res : range) {
      if(!(res instanceof OClass)) continue;

      ClassNode cnode = ontologyTreePanelClasses.getFirstNode(res.getName());
      if(cnode == null) continue;
      if(!hasChild(cnode, inode)) return false;
    }
    return true;
  }

  private Set<OInstance> getInstances(IFolder rootNode) {
    Set<OInstance> toReturn = new HashSet<OInstance>();
    if(rootNode instanceof ClassNode
            && ((ClassNode)rootNode).getSource() instanceof OInstance) {
      toReturn.add((OInstance)((ClassNode)rootNode).getSource());
    } else if(rootNode instanceof ClassNode) {
      // we also need to obtain all its children and iterate through all
      // of them
      Iterator childrenIterator = rootNode.getChildren();
      while(childrenIterator.hasNext()) {
        ClassNode aNode = (ClassNode)childrenIterator.next();
        toReturn.addAll(getInstances(aNode));
      }
    }

    return toReturn;
  }

  private ArrayList<ClassNode> getClassesAndInstances(IFolder rootNode,
          TreePanel ontologyTreePanel, String startWith) {

    ArrayList<ClassNode> toReturn = new ArrayList<ClassNode>();

    if(rootNode instanceof ClassNode
            && ((ClassNode)rootNode).getSource() instanceof OResource) {

      if(startWith.length() > 0) {
        if(((OResource)((ClassNode)rootNode).getSource()).getName()
                .toLowerCase().startsWith(startWith)) {
          toReturn.add((ClassNode)rootNode);
        }
      } else {
        toReturn.add((ClassNode)rootNode);
      }

    }

    // we also need to obtain all its children and iterate through all
    // of them
    Iterator childrenIterator = rootNode.getChildren();
    while(childrenIterator.hasNext()) {
      ClassNode aNode = (ClassNode)childrenIterator.next();
      toReturn.addAll(getClassesAndInstances(aNode, ontologyTreePanel,
              startWith));
    }
    return toReturn;
  }

  /**
   * @param node
   * @param isClass
   */
  private void updateSelection(Annotation currentAnnotation, boolean isClass) {

    ClassNode node = selectNode(currentAnnotation);
    OntologyTreePanel panel = null;
    TreePath path = null;
    if(isClass) {
      panel = ontologyTreePanelClasses;
      path = viewer.ontoTreeListenerClasses.getTreePath(node);
    } else {
      panel = ontologyTreePanelProperties;
      path = viewer.ontoTreeListenerProperties.getTreePath(node);
    }
    panel.setSelected(node.toString(), false);
    panel.ontologyTree.scrollPathToVisible(path);

    panel.ontologyTree.repaint();
    viewer.refreshHighlights();
  }

  private Annotation updateAnnotation(Annotation currentAnnotation,
          ClassNode item) {
    // if an antipattern was declared:
    FeatureMap features = currentAnnotation.getFeatures();
    Annotation addedAnnotation = null;
    if(item != null && item.getSource() instanceof OResource) {
      OResource entity = (OResource)item.getSource();
      if(antipatternCkb.isSelected()) {
        features.put(Settings.ANTIPATTERN, String.valueOf(true));
      }
      boolean isProperty =
              !OntologyAnnotation.isSourceAClass(currentAnnotation);
      boolean makeProperty = !isClassTabSelected();
      if(isProperty) {
        updateDomainRangeParameters(entity, features);
      }
      addedAnnotation =
              viewer.annotationManager.addAnnotation(entity, features,
                      makeProperty, currentAnnotation.getStartNode()
                              .getOffset().intValue(), currentAnnotation
                              .getEndNode().getOffset().intValue(), false);
      if(makeProperty) {
        viewer.ontoTreeListenerProperties
                .updateOntologyTreePanelSelection(item);
      } else {
        viewer.ontoTreeListenerClasses.updateOntologyTreePanelSelection(item);
      }
      viewer.annotationManager.deleteAnnotation(currentAnnotation);
    }
    return addedAnnotation;

  }

  // /**
  // * @param currentAnnotation
  // * @param features
  // * @return
  // */
  // private Annotation updateAnnotation(gate.Annotation
  // currentAnnotation) {
  // OntologyAnnotation oa = new OntologyAnnotation(currentAnnotation);
  // String value =
  // oa.getSourceFeatureValue(ontologyTreePanelClasses.getCurrentOntology());
  // Feature annotationFeature = getSelectedAnnotationType();
  // boolean isProperty =
  // !OntologyAnnotation.isSourceAClass(currentAnnotation);
  // ClassNode aNode;
  // Annotation addedAnnotation = null;
  // OntologyTreePanel treePanel;
  // if(isProperty) {
  // treePanel = ontologyTreePanelProperties;
  // }
  // else {
  // treePanel = ontologyTreePanelClasses;
  // }
  // aNode = treePanel.getFirstNode(value);
  // treePanel.getOntologyViewer().annotationManager.deleteAnnotation(currentAnnotation);
  // addedAnnotation = treePanel.getOntologyViewer().annotationManager
  // .addNewAnnotation((OResource)aNode.getSource(), false,
  // currentAnnotation.getFeatures(),
  // annotationFeature, isProperty).get(0);
  // if(addedAnnotation!=null){
  // treePanel.ontoTreeListener.updateOntologyTreePanelSelection(aNode);
  // }
  // return addedAnnotation;
  // }

  /**
   * @return
   * @throws HeadlessException
   */
  private ClassNode getNotHiddenItem() throws HeadlessException {
    Object selectedItem;
    ClassNode item = null;
    if(isClassTabSelected()) {
      selectedItem = classCombo.getSelectedItem();
      if(selectedItem instanceof String) {
        item = ontologyTreePanelClasses.getFirstNode((String)selectedItem);
      }
    } else {
      selectedItem = propertiesCombo.getSelectedItem();
      if(selectedItem instanceof String) {
        item = ontologyTreePanelProperties.getFirstNode((String)selectedItem);
      }
    }
    if(!(selectedItem instanceof String)) {
      item = (ClassNode)selectedItem;
    }

    if(item == null) {
      JOptionPane.showMessageDialog(MainFrame.getInstance(),
              "No resource found with value : " + selectedItem.toString());
//      newAnnotationMode = false;
      hideWindow();
      return null;
    }
    return item;
  }

  /**
   * A class to show a list of concepts or properties available for
   * annotations
   * 
   * @author naddi
   * 
   */
  public class ComboKeyListener extends KeyAdapter {

    OntologyTreePanel ontologyTreePanel;

    public ComboKeyListener() {
      super();
    }

    public ComboKeyListener(JComboBox combobox,
            OntologyTreePanel ontologyTreePanel) {
      super();
      combo = combobox;
      this.ontologyTreePanel = ontologyTreePanel;
    }

    JComboBox combo;

    public void keyReleased(KeyEvent keyevent) {
      String s =
              ((JTextComponent)combo.getEditor().getEditorComponent())
                      .getText();
      if(s != null) {
        if(keyevent.getKeyCode() != KeyEvent.VK_ENTER
                || keyevent.getKeyCode() != KeyEvent.VK_UP
                || keyevent.getKeyCode() != KeyEvent.VK_DOWN) {
          IFolder rootNode = getRootNode();
          ArrayList<ClassNode> items =
                  getClassesAndInstances(rootNode, ontologyTreePanel,
                          s.toLowerCase());
          ClassNode[] nodes = new ClassNode[items.size()];
          for(int i = 0; i < items.size(); i++) {
            nodes[i] = items.get(i);
          }
          DefaultComboBoxModel defaultcomboboxmodel =
                  new DefaultComboBoxModel(nodes);
          combo.setModel(defaultcomboboxmodel);

          try {
            if(!items.isEmpty()) combo.showPopup();
          } catch(Exception exception) {
          }
        }
        ((JTextComponent)combo.getEditor().getEditorComponent()).setText(s);
      }
    }

    /**
     * @return
     */
    private IFolder getRootNode() {
      IFolder rootNode =
              (ClassNode)((OntoTreeModel)ontologyTreePanel.ontologyTree
                      .getModel()).getRoot();
      return rootNode;
    }

  }




  /**
   * Frame mit einer eigenen Move / Drag Implementation, die an den
   * Bildschirmrand "schnappt". Nachteil: Keine herk�mmliche
   * Titelleiste. Aber mit Titelleiste bekommt der Frame dort keine Drag
   * Ereignisse, die Titelleiste managed das System.
   */
  class DragListener implements MouseMotionListener, MouseListener {

    // relativer Startpunkt des Drags zum 0,0 Punkt der Komponente
    Point dragStart;

    // relativen Startpunkt des Drags merken
    public void mousePressed(MouseEvent e) {
      dragStart = e.getPoint();
    }

    public void mouseDragged(MouseEvent e) {
      // Drag Position abfragen
      Point drag = e.getPoint();
      // Komponente holen
      Component c = e.getComponent();
      // Gr��e der Komponente abfragen um rechts und unten "schnappen"
      // berehcnen zu k�nnen
      Dimension size = c.getSize();
      // Bildschirmposition des Drags berechnen
      SwingUtilities.convertPointToScreen(drag, c);
      // Relativen Startpunkt des Drags benutzen
      drag.translate(-dragStart.x, -dragStart.y);
      // Aktuelle Zielposition x und y
      int x = drag.x;
      int y = drag.y;
      // Breite und H�he
      int w = size.width;
      int h = size.height;
      // Innerhalb von 50 Pixeln zum Rand nach links und oben schnappen
      if(x < 50) x = 0;
      if(y < 50) y = 0;
      // Bildschirmaufl�sung holen f�r rechten und unteren Rand
      DisplayMode dm =
              c.getGraphicsConfiguration().getDevice().getDisplayMode();
      // Breite und H�he in Maximalwerte einrechnen
      int maxX = dm.getWidth() - w;
      int maxY = dm.getHeight() - h;
      // Innerhalb von 50 Pixeln zum Rand nach unten und rechts
      // schnappen
      if(x > maxX - 50) x = maxX;
      if(y > maxY - 50) y = maxY;
      // Zielposition setzen
      c.setLocation(x, y);
    }

    // unbenutzte Listener Methoden
    public void mouseReleased(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
      pane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    public void mouseExited(MouseEvent e) {
      pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

  }

}
