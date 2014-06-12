package semano.rulebaseeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import semano.ontoviewer.Viewer;
import semano.rulestore.Parameter;

/**
 * A GUI component showing the table of japelate parameters with values.
 * @author nadeschda
 *
 */
public class ParameterTable extends JPanel {

  JTable mainTable = new JTable();

  private DefaultTableModel dataModel = new DefaultTableModel();

  private List<Parameter> japelateParams = new ArrayList<>();

  private List<String> paramValues = new ArrayList<>();


  private JScrollPane scroller;

  private Viewer viewer;

  

  public ParameterTable(Viewer viewer) {
    super();
    this.viewer=viewer;
  }

  public List<Parameter> getJapelateParams() {
    return japelateParams;
  }

  public void setJapelateParams(List<Parameter> japelateParams) {
    this.japelateParams = japelateParams;
  }

  public List<String> getParamValues() {
    return paramValues;
  }

  public void setParamValues(List<String> paramValues) {
    this.paramValues = paramValues;
  }

  public Component getTable() {
    return mainTable;
  }

  public void initGUI() {
    // this.removeAll();
    mainTable.setTableHeader(null);
    mainTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    mainTable.setShowVerticalLines(false);
    mainTable.setBackground(Color.WHITE);
    mainTable.setIntercellSpacing(new Dimension(2, 2));
    mainTable.setRowHeight(23);
    mainTable.setPreferredSize(new Dimension(650,
            (mainTable.getRowHeight() + 2) * mainTable.getRowCount()));
    scroller = new JScrollPane(mainTable);
    scroller.setBackground(getBackground());
    scroller.getViewport().setBackground(getBackground());
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(scroller);
  }

  private void setRendererEditor(JTable t) {
    FeatureEditorRenderer editor = new FeatureEditorRenderer();
    FeatureEditorRendererJTF editorJTF = new FeatureEditorRendererJTF();
    TableColumnModel columnModel = t.getColumnModel();
    columnModel.getColumn(0).setCellRenderer(editorJTF);
    columnModel.getColumn(0).setCellEditor(editorJTF);
    columnModel.getColumn(1).setCellRenderer(editor);
    columnModel.getColumn(1).setCellEditor(editor);
  }

  public void loadDataModel(List<Parameter> paramsJapelate,
          List<String> paramsRule) {
    this.japelateParams = paramsJapelate;
    if(paramsRule != null) {
      this.paramValues = paramsRule;
    } else {
      this.paramValues = new ArrayList<>();
    }
    ArrayList<Object[]> data = new ArrayList<Object[]>();
    for(int i = 0; i < paramsJapelate.size(); i++) {
      // init description
      Parameter paramJ = paramsJapelate.get(i);
      String paramName = paramJ.getDescription();
      // init value
      String paramValue = "";
      if(paramValues.size() > i) {
        paramValue = paramValues.get(i);
      } else {
        paramValues.add(paramValue);
      }

      Object[] row = {paramName, paramValue};
      data.add(row);
    }
    dataModel =
            new DefaultTableModel(data.toArray(new Object[][] {}),
                    new String[] {"", ""});
    mainTable.setModel(dataModel);
    setRendererEditor(mainTable);
    mainTable.setPreferredSize(new Dimension(650,
            (mainTable.getRowHeight() + 2) * mainTable.getRowCount()));
    if(scroller != null)
      scroller.setPreferredSize(mainTable.getPreferredScrollableViewportSize());
    dataModel.fireTableDataChanged();
  }

  protected class FeatureEditorRenderer extends DefaultCellEditor implements
                                                                 TableCellRenderer {

    JComboBox<String> editorCombo;

    JComboBox<String> rendererCombo;

    public FeatureEditorRenderer() {
      super(new JComboBox<String>());
      editorCombo = (JComboBox<String>)editorComponent;
      editorCombo.setBackground(mainTable.getBackground());
      editorCombo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          int row = mainTable.getEditingRow();
          int col = mainTable.getEditingColumn();
          if(row >= 0 && editorCombo.getSelectedItem() != null) {
            String newValue = editorCombo.getSelectedItem().toString();
            if(japelateParams.get(row).getType()
                    .equals(Parameter.ParameterType.ONTOLOGY_ENTITY)) {
              newValue = viewer.getOntologyEntities().get(editorCombo.getSelectedIndex());
            }
            paramValues.set(row, newValue);
            mainTable.setValueAt(newValue, row, 1);
          }
          stopCellEditing();
        }
      });

      rendererCombo = new JComboBox<String>();
      rendererCombo.setBackground(mainTable.getBackground());
      rendererCombo.setOpaque(false);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
      prepareCombo(rendererCombo, row);
      // rendererCombo.setPreferredSize(new Dimension(250, rendererCombo
      // .getPreferredSize().height));
      return rendererCombo;
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
      prepareCombo(editorCombo, row);
      return editorCombo;
    }

    protected void prepareCombo(JComboBox<String> combo, int row) {
      DefaultComboBoxModel<String> comboModel =
              (DefaultComboBoxModel<String>)combo.getModel();
      comboModel.removeAllElements();
      if(row == 0 || row == 1) {
        // we have the rule ID
        comboModel.addElement(paramValues.get(row));
        combo.setSelectedItem(paramValues.get(row));
        combo.setEditable(false);
        combo.setEnabled(false);
        return;
      } else if(japelateParams.get(row).getType()
              .equals(Parameter.ParameterType.ONTOLOGY_ENTITY)) {
        combo.setEditable(false);

        if(!viewer.getOntologyEntities().contains(paramValues.get(row))
                && !paramValues.get(row).isEmpty()) {
          viewer.getOntologyEntities().add(paramValues.get(row));
        }
        for(String val : viewer.getOntologyEntities()) {
//          comboModel.addElement(new ONodeIDImpl(val, false).getResourceName());
          comboModel.addElement(val);
        }
        if(paramValues.get(row).isEmpty()) {
          paramValues.set(row, viewer.getOntologyEntities().get(0));
        }
        String val =paramValues.get(row);
//                (new ONodeIDImpl(paramValues.get(row), false))
//                        .getResourceName();
        comboModel.addElement(val);
        combo.setSelectedItem(val);
      } else {
        String val = paramValues.get(row);
        comboModel.addElement(val);
        combo.setSelectedItem(val);
        combo.setEditable(true);
      }
      combo.setEnabled(true);
    }
    // combo.getEditor().getEditorComponent()
    // .setBackground(mainTable.getBackground());
  }

  protected class FeatureEditorRendererJTF extends DefaultCellEditor implements
                                                                    TableCellRenderer {
    JTextField listItems;

    JPanel listPanel;

    public FeatureEditorRendererJTF() {
      super(new JTextField());
      listItems = new JTextField(10);
      listItems.setMargin(new Insets(0, 0, 0, 0));
      listItems.setEditable(false);
      listItems.setEnabled(false);
      listItems.setBackground(mainTable.getBackground());
      listPanel = new JPanel(new BorderLayout(0, 0));
      listPanel.add(listItems, BorderLayout.CENTER);
      listPanel.setBackground(mainTable.getBackground());
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

      listItems.setText((String)mainTable.getValueAt(row, column));
      return listPanel;
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {

      listItems.setText((String)mainTable.getValueAt(row, column));
      return listPanel;

    }

  }
}
