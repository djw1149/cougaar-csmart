/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * � Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.generic;

import java.awt.Color;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import java.util.*;

/**
 * Used as the model for JComboBoxes in legends.
 */

public class LegendComboBoxModel extends AbstractListModel implements ComboBoxModel {
    Object currentValue;
    Vector values; // vector of JLabel
    JLabel blankLabel;
    int nElements; // number of elements in the list

  /** Called with a vector which has alternating entries:
   * a String (for example, agent name)
   * a Color (java.awt.Color) for that String.
   * @param nodeColors strings and colors
   */

  public LegendComboBoxModel(Vector nodeColors) {
    nElements = nodeColors.size()/2;
    values = new Vector(nElements);
    for (int i = 0; i < nElements; i++) {
      JLabel label = new JLabel((String)nodeColors.elementAt(i*2),
				new ColoredSquare((Color)nodeColors.elementAt(i*2+1), 100, 12),
				SwingConstants.LEFT);
      label.setForeground(Color.black);
      values.addElement(label);
    }
    // sort labels alphabetically
    Collections.sort(values, new MyComparator());
    blankLabel = new JLabel();
    blankLabel.setText("");
    blankLabel.setIcon(null);
  }
  
  public void setSelectedItem(Object anObject) {
    currentValue = anObject;
    fireContentsChanged(this,-1,-1);
  }
      
  public Object getSelectedItem() {
    return currentValue;
  }

  public int getSize() {
    return nElements;
  }

  public Object getElementAt(int index) {
    if (index >= 0 && index < nElements)
      return values.elementAt(index);
    else 
      return blankLabel;
  }

  class MyComparator implements Comparator {

    public int compare(Object o1, Object o2) {
      String s1 = ((JLabel)o1).getText();
      return s1.compareTo(((JLabel)o2).getText());
    }

    public boolean equals(Object o) {
      return this.equals(o);
    }

  }
}

