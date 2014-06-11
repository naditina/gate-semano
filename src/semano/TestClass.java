package semano;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;

public class TestClass extends JFrame 
  implements ActionListener {
  private JComboBox box;

  public static void main(String[] args) {
      new TestClass().setVisible(true);
  }

  public TestClass() {
      super("Text DEMO");
      setSize(300, 300);
      setLayout(new FlowLayout());
      Container cont = getContentPane();
      box = new JComboBox(new String[] { "First", "Second", "..." });
      box.setEditable(true);
      box.addActionListener(this);
      cont.add(box);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
      box.removeActionListener(this);
      box.insertItemAt(box.getSelectedItem(), 0);
      box.addActionListener(this);
  }

}
