/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * � Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.component;

/**
 * An implementation of the Range interface for integers.
 */

public class IntegerRange implements Range {
  static final long serialVersionUID = 6546059774126493583L;

  int minValue;
  int maxValue;
  Integer minValueObject;
  Integer maxValueObject;

  public IntegerRange(int minValue, int maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
    minValueObject = new Integer(minValue);
    maxValueObject = new Integer(maxValue);
  }

  /**
   * Get the minimum value of the range. Values are allowed to be
   * equal to the minimum value
   * @return an object having the minimum value
   **/

  public Object getMinimumValue() {
    return minValueObject;
  }

  /**
   * Get the maximum value of the range. Values are allowed to
   * be equal to the maximum value
   * @return an object having the maximum value
   **/

  public Object getMaximumValue() {
    return maxValueObject;
  }

  /**
   * Test if an Object is in this Range
   * @param o the Object to test
   **/
  
  public boolean isInRange(Object o) {
    if (!(o instanceof Integer))
      return false;
    int i = ((Integer)o).intValue();
    if (i >= minValue && i <= maxValue)
      return true;
    return false;
  }

  public String toString() {
    return minValue + ":" + maxValue;
  }

}

