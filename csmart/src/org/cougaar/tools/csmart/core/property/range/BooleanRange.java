/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.tools.csmart.core.property.range;

/**
 * Implementation of Range for Booleans.  Note that a BooleanRange
 * always has a single Boolean value.
 */

public class BooleanRange extends RangeBase implements Range {
  static final long serialVersionUID = -7995183168378633722L;

  private boolean value;
  private Boolean valueObject;

  public BooleanRange(boolean value) {
    this.value = value;
    valueObject = new Boolean(value);
  }

  /**
   * Get the minimum value of the range. Values are allowed to be
   * equal to the minimum value
   * @return an object having the minimum value
   **/

  public Object getMinimumValue() {
    return valueObject;
  }

  /**
   * Get the maximum value of the range. Values are allowed to
   * be equal to the maximum value
   * @return an object having the maximum value
   **/

  public Object getMaximumValue() {
    return valueObject;
  }

  /**
   * Test if an Object is in this Range
   * @param o the Object to test
   **/
  
  public boolean isInRange(Object o) {
    if (o instanceof Boolean)
      return ((Boolean)o).booleanValue() == value;
    else 
      return false;
  }

  public String toString() {
    return valueObject.toString();
  }
}
