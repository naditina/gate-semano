package semano.rulebaseeditor;

//: c14:JTableDemo.java
//Simple demonstration of JTable.
//<applet code=Table width=350 height=200></applet>
//From 'Thinking in Java, 3rd ed.' (c) Bruce Eckel 2002
//www.BruceEckel.com. See copyright notice in CopyRight.txt.

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JWindow;

public class JTableDemo  {

public static void main(String[] args) {
 JFrame frameq = new JFrame();
 frameq.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 JDialog frame= new JDialog(frameq);
 JComboBox table = new JComboBox(new Object[] { "one", "two", "three", "four" });
 frame.getContentPane().add(table);
 frame.setSize(350, 200);
 frame.setVisible(true);
}
} ///:~

