package semano.rulebaseeditor;

import gate.creole.ontology.InvalidURIException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

import semano.ontologyowl.ONodeIDImpl;
import semano.ontologyowl.OURIImpl;
import semano.ontoviewer.Viewer;
import semano.rulestore.AnnotationRule;
import semano.rulestore.Japelate;
import semano.rulestore.RuleStore.Type;

/**
 * @author Nadejda Nikitina
 * 
 */
public class AnnotationRuleEditor extends AbstractAction {

  protected JDialog annotationWindow;

  protected JPanel pane;

  private JComboBox<String> japelatesCB = new JComboBox<>();

  protected JButton dissmissBtn, OKBtn;

  private ParameterTable parametersEditor;

  private boolean newAnnotationMode = true;

  private Viewer viewer;
  private CreoleRuleStore ruleStore;

  private Japelate selectedJapelate;

  private AnnotationRule rule;

  /**
   * Constructor
   * 
   * @param viewer
   * @param store
   */
  public AnnotationRuleEditor(Viewer viewer, CreoleRuleStore store) {
    this.viewer = viewer;
    this.ruleStore=store;
    this.parametersEditor = new ParameterTable(viewer);
  }

  private void initGUI() {
    annotationWindow =
            new JDialog(SwingUtilities.getWindowAncestor(viewer
                    .getParent()));
    Border border = UIManager.getBorder("InternalFrame.border");
    if(border == null) {
      border = BorderFactory.createTitledBorder("Rule Editor");
      int w = UIManager.getInt("InternalFrame.borderWidth");
      w = Math.max(w, 3) - 2;
      border =
              BorderFactory.createCompoundBorder(border,
                      BorderFactory.createEmptyBorder(w, w, w, w));
    }
    DragListener listener = new DragListener();
    annotationWindow.addMouseListener(listener);
    annotationWindow.addMouseMotionListener(listener);

    pane = new JPanel();
    pane.setBorder(border);
    pane.setLayout(new BorderLayout());
    pane.setBackground(UIManager.getLookAndFeelDefaults().getColor(Color.WHITE));
    annotationWindow.setContentPane(pane);

    // top of the window:
    JPanel top = new JPanel();
    top.setLayout(new BorderLayout());
//    Icon icon = UIManager.getIcon("InternalFrame.closeIcon");
//    if(icon == null) icon = MainFrame.getIcon("exit");
    
//    top.add(dissmissBtn, BorderLayout.EAST);
    // //japelate
    JPanel japelateP = new JPanel();
    JTextField jap = new JTextField("Japelate: ");
    jap.setEditable(false);
    jap.setBackground(top.getBackground());
    jap.setBorder(null);
    japelateP.add(jap);

    getJapelatesCB().addActionListener(new JapelateSelectionAction());
    // getJapelatesCB().setSelectedIndex(1);
    getJapelatesCB().setEditable(true);
    japelateP.add(getJapelatesCB());
    top.add(japelateP, BorderLayout.CENTER);
    pane.add(top, BorderLayout.NORTH);

    // parameter table
    parametersEditor.setBackground(Color.GRAY);
    parametersEditor.initGUI();
    pane.add(new JScrollPane(parametersEditor.getTable()), BorderLayout.CENTER);

    // OK Button
    OKBtn = new JButton("OK");
    OKBtn.setAction(new AddChangeAnnotationAction());
    OKBtn.setBorderPainted(true);
//    OKBtn.setContentAreaFilled(true);
    OKBtn.setMargin(new Insets(0, 0, 0, 0));
    OKBtn.setText("OK");
    OKBtn.setToolTipText("Add to rule base");
    JPanel buttons = new JPanel();
    buttons.add(OKBtn);
    dissmissBtn = new JButton("Cancel");
    dissmissBtn.setAction(new CancelAction());
    buttons.add(dissmissBtn);
    pane.add(buttons, BorderLayout.SOUTH);

  }

  /**
   * is called every time a mouse is rolled over any text
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent ae) {
    newAnnotationMode = true;
    if(setupData()) {
      showWindow();
    }
  }

  public void editAnnotationRule(AnnotationRule rule) {
    newAnnotationMode = false;
    if(setupData(rule)) showWindow();
  }

  private boolean setupData() {
    LinkedHashMap<String, Japelate> japelateMap =
            ruleStore.getJapelates();
    if(japelateMap == null && japelateMap.isEmpty()) return false;
    Japelate defaultJ = japelateMap.values().iterator().next();
    if(defaultJ == null) return false;
    this.selectedJapelate = defaultJ;
    initCombo(japelateMap.keySet(), selectedJapelate);
    // rule is null
    return initParameterFields();
  }

  private boolean setupData(AnnotationRule rule) {
    LinkedHashMap<String, Japelate> japelateMap =
            ruleStore.getJapelates();
    if(japelateMap == null && japelateMap.isEmpty()) return false;
    Japelate defaultJ = rule.getJapelate();
    if(defaultJ == null) return false;
    this.selectedJapelate = defaultJ;
    initCombo(japelateMap.keySet(), selectedJapelate);
    this.rule = rule;
    return initParameterFields();
  }

  private boolean initParameterFields() {
    if(rule == null) {
      parametersEditor.loadDataModel(selectedJapelate.getParamList(), null);
    } else if(selectedJapelate.equals(rule.getJapelate())) {
      parametersEditor.loadDataModel(selectedJapelate.getParamList(),
              rule.getParameters());
    } else {// japelate has been changed, so we need to reset
            // parameters:
      List<String> parametersRule = new ArrayList<>();
      for(int i = 0; i < AnnotationRule.MINIMUM_PARAMETER_NUMBER; i++)
        parametersRule.add(rule.getParameters().get(i));
      parametersEditor.loadDataModel(selectedJapelate.getParamList(),
              parametersRule);
    }
    return true;
  }

  private void initCombo(Collection<String> entries, Japelate selectedJapelate) {
    DefaultComboBoxModel<String> model =
            new DefaultComboBoxModel<String>(entries.toArray(new String[] {}));
    getJapelatesCB().setModel(model);
    getJapelatesCB().setSelectedItem(selectedJapelate.getName());
  }

  private void showWindow() {
    initGUI();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Point topLeft = null;
        int charHeight = 0;
        topLeft = viewer.getLocationOnScreen();
        FontMetrics fm =
                viewer.getFontMetrics(viewer.getFont());
        charHeight = fm.getAscent() + fm.getDescent();
        final int x = topLeft.x;
        final int y = topLeft.y;
        annotationWindow.setLocation(x, y);
        annotationWindow.setSize(new Dimension(parametersEditor.getTable()
                .getPreferredSize().width + 20, 500));
        // annotationWindow.pack();
        annotationWindow.setVisible(true);
      }
    });

  }

  protected class CancelAction extends AbstractAction {
    public CancelAction() {
      super("Cancel");
    }

    public void actionPerformed(ActionEvent ae) {
      annotationWindow.setVisible(false);
    }
  }

  protected class JapelateSelectionAction extends AbstractAction {
    public JapelateSelectionAction() {
      super("");
    }

    public void actionPerformed(ActionEvent ae) {
      String jn = (String)getJapelatesCB().getSelectedItem();
      if(jn != null) {
        Japelate j = ruleStore.getJapelates().get(jn);
        if(j != null) {
          selectedJapelate = j;
          // japelatesCB.setSelectedItem(jn);
          initParameterFields();
          annotationWindow.setPreferredSize(new Dimension(parametersEditor
                  .getTable().getPreferredSize().width, annotationWindow
                  .getHeight()));
        }
      }
      // System.out.println("japelate selected");
    }
  }

    /**
     * Add and change annotations action...
     */
  protected class AddChangeAnnotationAction extends AbstractAction {
    public void actionPerformed(ActionEvent ie) {
      List<String> params = parametersEditor.getParamValues();
      try {
        ONodeIDImpl entityIRI = new ONodeIDImpl(params.get(2), false);
        String ontology = entityIRI.getNameSpace();
        params.set(1, ontology);
        if(newAnnotationMode) {
          // System.out.println("adding new rule");
          Type type = Type.CONCEPT;
          if(viewer.getCurrentOntology().isObjectProperty(
                  new OURIImpl(params.get(2)))) type = Type.RELATION;
          ruleStore.addRule(
                  AnnotationRuleEditor.this.selectedJapelate, params, type);
          newAnnotationMode = false;
        } else {
          System.out.println("updating rule");
          rule.setJapelate(selectedJapelate);
          rule.setClas(params.get(2));
          rule.setOntology(ontology);
          for(int i = AnnotationRule.MINIMUM_PARAMETER_NUMBER; i < params.size(); i++) {
            if(rule.getParameters().size() <= i)
              rule.addParameter(params.get(i));
            else rule.getParameters().set(i, params.get(i));
          }
          ruleStore.updateRule(rule);

        }
      } catch(InvalidURIException e) {
        System.err.println(e.getMessage());
      }
      hideWindow();
      ruleStore.fireStoreUpdatedEvent();
    }

  }

  public void hideWindow() {
    if(annotationWindow != null) annotationWindow.setVisible(false);
  }

  /**
   * @return the japelatesCB
   */
  protected JComboBox<String> getJapelatesCB() {
    return japelatesCB;
  }

  /**
   * @param japelatesCB the japelatesCB to set
   */
  protected void setJapelatesCB(JComboBox<String> japelatesCB) {
    this.japelatesCB = japelatesCB;
  }

  class DragListener extends MouseAdapter implements MouseMotionListener {

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

    public void mouseEntered(MouseEvent e) {
      pane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    public void mouseExited(MouseEvent e) {
      pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

  }

}
