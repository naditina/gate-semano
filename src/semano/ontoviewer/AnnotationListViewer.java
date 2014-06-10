package semano.ontoviewer;

import gate.creole.ontology.OResource;
import gate.creole.ontology.Ontology;
import gate.gui.MainFrame;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

import semano.ontologyowl.AnnotationValue;
import semano.ontologyowl.impl.OWLOntology;


import com.ontotext.gate.vr.ClassNode;

/**
 * @author Nadejda Nikitina
 * 
 */
public class AnnotationListViewer extends AbstractAction {

  protected JDialog annotationWindow;

  protected JScrollPane scroller;

  protected JPanel pane;

  protected OntologyTreePanel ontologyTreePanel;

  protected CancelAction dissmissAction;

  protected JButton dissmissBtn;

  ClassNode selectedNode;

  JTable table;

  private OntologyViewer viewer;

  /**
   * Constructor
   * 
   * @param ontoTreePanel
   * @param node
   */
  public AnnotationListViewer(OntologyTreePanel ontoTreePanel, ClassNode node, OntologyViewer viewer) {
    this.ontologyTreePanel = ontoTreePanel;
    this.viewer=viewer;
    selectedNode = node;
    initGUI();
  }

  private void initGUI() {
    this.annotationWindow = new JDialog(
            SwingUtilities
                    .getWindowAncestor(viewer.documentTextualDocumentView
                            .getGUI()));

    // /* Set a nice border - try to use the internal frame border
    // * (used inside JDesktopPane) which should mimic the native
    // * frame border when using the system look and feel. */
    Border border = UIManager.getBorder("InternalFrame.border");
    if(border == null) {
      /* Fallback to this border. */
      border = BorderFactory.createTitledBorder("Annotation: "+selectedNode);

      int w = UIManager.getInt("InternalFrame.borderWidth");
      w = Math.max(w, 3) - 2;
      border = BorderFactory.createCompoundBorder(border, BorderFactory
              .createEmptyBorder(w, w, w, w));
    }

    /* Register the resize listener. */
    ResizeListener resizeListener = new ResizeListener();
    annotationWindow.addMouseListener(resizeListener);
    annotationWindow.addMouseMotionListener(resizeListener);
//    
//    DragListener dragListener=new DragListener();
//    annotationWindow.addMouseListener(dragListener);
//    annotationWindow.addMouseMotionListener(dragListener);

    DefaultTableModel tableModel = null;

    table = new JTable();
    table.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    if(selectedNode != null && selectedNode.getSource() != null
            && selectedNode.getSource() instanceof OResource) {
      final OResource resource = ((OResource)selectedNode.getSource());
      Ontology o = viewer.getCurrentOntology();
      AnnotationValue[] annotationValues = ((OWLOntology)o)
              .getAnnotationValues(resource.getURI().toString());
      tableModel = createTableModel(annotationValues); 
      
      table.setModel(tableModel);
      
      // table.setAutoCreateColumnsFromModel(true);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      table.setAutoscrolls(true);
//      annotationWindow.setSize(new Dimension(table.getHeight(), table
//              .getWidth()));

    }

    pane = new JPanel();
    // pane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    pane.setBorder(border);
    pane.setLayout(new GridBagLayout());
    pane.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    annotationWindow.setContentPane(pane);
//    pane.setSize(new Dimension(table.getHeight(), table.getWidth()));

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

    Icon icon = UIManager.getIcon("InternalFrame.closeIcon");
    if(icon == null) icon = MainFrame.getIcon("exit");
    dissmissAction = new CancelAction(icon);
    dissmissBtn = new JButton(dissmissAction);

    constraints.insets = new Insets(0, 10, 0, 0);
    constraints.anchor = GridBagConstraints.NORTHEAST;
    constraints.weightx = 1;
    dissmissBtn.setBorder(null);
    pane.add(dissmissBtn, constraints);

    constraints.anchor = GridBagConstraints.CENTER;
    constraints.insets = insets0;

    // featuresEditor.setBackground(UIManager.getLookAndFeelDefaults().getColor(
    // "ToolTip.background"));
    // featuresEditor.init();

    // scrollerPanel = new JPanel(new BorderLayout());
    // // scrollerPanel.add(featuresEditor.getTable(),
    // // BorderLayout.CENTER);
    // scrollerPanel.add(table.getTableHeader(),
    // BorderLayout.PAGE_START);
    // scrollerPanel.add(table, BorderLayout.CENTER);
    // scrollerPanel.setMinimumSize(new Dimension(table
    // .getHeight(), table.getWidth()));


    
    

    JPanel tableButtonPanel = new JPanel();
    JButton addButton = new JButton("Add New Row");
    JButton deleteButton = new JButton("Delete Selected Row(s)");
    tableButtonPanel.add(addButton);
    
    tableButtonPanel.add(deleteButton);
    final DefaultTableModel tm = tableModel;
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JWindow addNewToList = new JWindow();
        tm.addRow(new Object[]{});
        //add to ontology...
      }
    });
    
    deleteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int[] selectedRows = table.getSelectedRows();
        
        boolean rowsSelected = selectedRows.length != 0;
        if(rowsSelected) {
            if(selectedRows.length > 1)
                Arrays.sort(selectedRows);
          //need to sort in order to remove last first, so indices are not moved by the deletion...
            for (int i = selectedRows.length - 1; i >= 0; i--) {
              ((DefaultTableModel)table.getModel()).removeRow(selectedRows[i]);
              
              //delete from ontology ...
              //ontology parser ??? uses OWLAPI
              
            }
            
        }
      }
    });
//  
   
    constraints.gridy = 1;
    constraints.weighty = 3;
    constraints.fill = GridBagConstraints.BOTH;
    scroller = new JScrollPane(table);

    scroller.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    pane.add(scroller, constraints);

    constraints.gridx = 2;
    pane.add(tableButtonPanel);
  }

  private void enableDisableComponents() {

    scroller.setEnabled(true);
  }

  /**
   * is called every time a mouse is rolled over any text
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent ae) {

    if(selectedNode != null && selectedNode.getSource() != null
            && selectedNode.getSource() instanceof OResource) {

      // AnnotationSchema annSchema = new AnnotationSchema();
      // Set<FeatureSchema> fsSet = new HashSet<FeatureSchema>();
      // annSchema.setFeatureSchemaSet(fsSet);
      // featuresEditor.setSchema(annSchema);
      // featuresEditor.setTargetFeatures(constructFeatures(annotationValues));
      enableDisableComponents();
      
      showWindow();
    }
  }

  private DefaultTableModel createTableModel(AnnotationValue[] annotationValues) {

    String[] columnNames = AnnotationValue.getFieldNames();
    ArrayList<Object[]> data = new ArrayList<Object[]>();
    for(AnnotationValue av : annotationValues) {
      Object[] row = av.toFieldArray();
      data.add(row);
    }
    return new DefaultTableModel(data.toArray(new Object[][] {}), columnNames);
  }



  private void showWindow() {

    // and lets show it
    final JScrollPane textComp =(JScrollPane)viewer.documentTextualDocumentView.getGUI();
//    final JTextArea textComp = ontologyTreePanel.getOntologyViewer().documentTextArea;
    int x1 = textComp.getX();
    enableDisableComponents();

    final int xx = x1;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
//        Rectangle startRect = null;
        Point topLeft = null;
        int charHeight = 0;

//        try {
//          startRect = textComp.modelToView(xx);
          topLeft = textComp.getLocationOnScreen();

          FontMetrics fm = textComp.getFontMetrics(textComp.getFont());
          charHeight = fm.getAscent() + fm.getDescent();
//        }
//        catch(BadLocationException ble) {
//          throw new GateRuntimeException("Can't show the window ", ble);
//        }

        final int x = topLeft.x;

        final int y = topLeft.y ;
        //+ startRect.y + charHeight;
        
        annotationWindow.setLocation(x, y);
        annotationWindow.setSize(textComp.getSize());
        annotationWindow.pack();
        annotationWindow.setVisible(true);
      }
    });
  }

  protected class CancelAction extends AbstractAction {
    public CancelAction(Icon icon) {
      super("", icon);
    }

    public void actionPerformed(ActionEvent ae) {
      annotationWindow.setVisible(false);
      viewer.documentTextArea.requestFocus();
      viewer.documentTextArea
              .setSelectionStart(viewer.documentTextArea
                      .getSelectionStart());
      viewer.documentTextArea
              .setSelectionEnd(viewer.documentTextArea
                      .getSelectionStart());
      viewer.documentTextArea.requestFocus();
      ontologyTreePanel.showingAnnotationWindow = false;
    }
  }

  public void hideWindow() {
    if(annotationWindow != null) annotationWindow.setVisible(false);
    ontologyTreePanel.showingAnnotationWindow = false;
  }
  
  
  

  /**
   * Mouse Motion Listener for dragging the window
   * @author naddi
   *
   */
  class ResizeListener extends MouseAdapter implements MouseMotionListener {

    private static final short RESIZE_E = 1;

    private static final short RESIZE_W = 2;

    private static final short RESIZE_N = 4;

    private static final short RESIZE_S = 8;

    int resizing = 0;

    private Rectangle tempBounds = new Rectangle();

    public void mousePressed(MouseEvent evt) {
      resizing = getResizeDirection(evt);
    }

    public void mouseReleased(MouseEvent evt) {
      resizing = 0;
    }

    private short getResizeDirection(MouseEvent evt) {
      short direction = 0;
      Component c = evt.getComponent();
      int width = c.getWidth();
      int height = c.getHeight();
      Insets insets = pane.getInsets();
      int mouseX = evt.getX();
      int mouseY = evt.getY();

      if(mouseX < insets.left) {
        direction |= RESIZE_W;
      }
      else if(mouseX > width - insets.right) {
        direction |= RESIZE_E;
      }

      if(mouseY < insets.top) {
        direction |= RESIZE_N;
      }
      else if(mouseY > height - insets.bottom) {
        direction |= RESIZE_S;
      }

      return direction;
    }

    public void mouseMoved(MouseEvent evt) {
      short direction = getResizeDirection(evt);
      int cursorType = getCursorType(direction);
      evt.getComponent().setCursor(Cursor.getPredefinedCursor(cursorType));
    }

    private int getCursorType(int direction) {
      switch(direction) {
        case RESIZE_S:
          return Cursor.S_RESIZE_CURSOR;
        case RESIZE_E:
          return Cursor.E_RESIZE_CURSOR;
        case RESIZE_N:
          return Cursor.N_RESIZE_CURSOR;
        case RESIZE_W:
          return Cursor.W_RESIZE_CURSOR;
        case RESIZE_S | RESIZE_E:
          return Cursor.SE_RESIZE_CURSOR;
        case RESIZE_N | RESIZE_W:
          return Cursor.NW_RESIZE_CURSOR;
        case RESIZE_N | RESIZE_E:
          return Cursor.NE_RESIZE_CURSOR;
        case RESIZE_S | RESIZE_W:
          return Cursor.SW_RESIZE_CURSOR;
        default:
          return Cursor.DEFAULT_CURSOR;
      }
    }

    public void mouseDragged(MouseEvent evt) {

      Component c = evt.getComponent();
      resizeComponent(c,evt.getPoint(), evt.getX(), evt.getY());
      resizeComponent(pane,evt.getPoint(), evt.getX(), evt.getY());
      pane.validate();
      c.validate();
      c.repaint();
    }

    private void resizeComponent(Component c, Point mouse, int x, int y) {
      Rectangle bounds = c.getBounds(tempBounds);
      SwingUtilities.convertPointToScreen(mouse, c);
      if((resizing & RESIZE_E) != 0) {
        bounds.width = x;
      }
      else if((resizing & RESIZE_W) != 0) {
        bounds.width += bounds.x - mouse.x;
        bounds.x = mouse.x;
      }

      if((resizing & RESIZE_S) != 0) {
        bounds.height = y;
      }
      else if((resizing & RESIZE_N) != 0) {
        bounds.height += bounds.y - mouse.y;
        bounds.y = mouse.y;
      }
      c.setBounds(bounds);
    }

  }
  
  

  
  /**
   * Frame mit einer eigenen Move / Drag Implementation, die
   * an den Bildschirmrand "schnappt". Nachteil: Keine herk�mmliche
   * Titelleiste. Aber mit Titelleiste bekommt der Frame dort keine
   * Drag Ereignisse, die Titelleiste managed das System.
   */
  class DragListener implements MouseMotionListener, MouseListener
  {
    
     // relativer Startpunkt des Drags zum 0,0 Punkt der Komponente
     Point dragStart;


     // relativen Startpunkt des Drags merken
     public void mousePressed(MouseEvent e)
     {
        dragStart = e.getPoint();
     }

     public void mouseDragged(MouseEvent e)
     {
        //Drag Position abfragen
        Point drag = e.getPoint();
        // Komponente holen
        Component c = e.getComponent();
        // Gr��e der Komponente abfragen um rechts und unten "schnappen" berehcnen zu k�nnen
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
        if(x < 50)
           x = 0;
        if(y < 50)
           y = 0;
        // Bildschirmaufl�sung holen f�r rechten und unteren Rand
        DisplayMode dm = c.getGraphicsConfiguration().getDevice().getDisplayMode();
        // Breite und H�he in Maximalwerte einrechnen
        int maxX = dm.getWidth() - w;
        int maxY = dm.getHeight() - h;
        // Innerhalb von 50 Pixeln zum Rand nach unten und rechts schnappen
        if(x > maxX - 50)
           x = maxX;
        if(y > maxY - 50)
           y = maxY;
        // Zielposition setzen
        c.setLocation(x, y);
     }

     // unbenutzte Listener Methoden
     public void mouseReleased(MouseEvent e)
     {
     }

     public void mouseMoved(MouseEvent e)
     {
     }

     public void mouseClicked(MouseEvent e)
     {
     }

     public void mouseEntered(MouseEvent e)
     {
       pane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
     }

     public void mouseExited(MouseEvent e)
     {
       pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
     }
     
     

  } 

}
