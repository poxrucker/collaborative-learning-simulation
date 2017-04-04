package de.dfki.visualization;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class CheckBoxList extends JList<JCheckBox> {

  /**
  * 
  */
  private static final long serialVersionUID = -2521716126203391415L;

  protected static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

  protected int previousIndex;
  
  public CheckBoxList(AbstractListModel<JCheckBox> model) {
    super(model);
    previousIndex = 0;
    setCellRenderer(new CellRenderer());
    
    addMouseListener(new MouseAdapter() {

      public void mousePressed(MouseEvent e) {
        int index = locationToIndex(e.getPoint());

        if (index != previousIndex) {
          previousIndex = index;
          setSelectedIndex(index);
          repaint();
          return;
        }
        
        if (index != -1) {
          JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
          checkbox.setSelected(!checkbox.isSelected());
          repaint();
        }
      }
    });
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  }

  protected class CellRenderer implements ListCellRenderer<JCheckBox> {

    public Component getListCellRendererComponent(JList< ? extends JCheckBox> list, JCheckBox value, int index, boolean isSelected, boolean cellHasFocus) {
      JCheckBox checkbox = (JCheckBox) value;
      checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
      checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
      checkbox.setEnabled(isEnabled());
      checkbox.setFont(getFont());
      checkbox.setFocusPainted(false);
      checkbox.setBorderPainted(true);
      checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
      return checkbox;
    }

    
  }
}
