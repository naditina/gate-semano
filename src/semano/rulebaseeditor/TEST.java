package semano.rulebaseeditor;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.table.DefaultTableModel;

public class TEST {

  public static void main(String[] a) {
    JWindow window = new JWindow(new JFrame("test"));
    window.setSize(500, 500);
    DefaultTableModel m = new DefaultTableModel(new String[][] {{"a", "b"}}, new String[]{"A", "B"}){
      @Override
      public boolean isCellEditable(int row, int column) {
        return true;
      }
    };
    
    JTable table = new JTable(m);
    table.setFocusable(true);
    window.getContentPane().add(new JScrollPane(table));
    window.getOwner().setVisible(true);
    window.setVisible(true);
  }
}
