/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.tools.csmart.core.property;

import org.cougaar.tools.csmart.core.property.name.ComponentName;
import org.cougaar.tools.csmart.core.property.name.CompositeName;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Set;

/**
 * A ConfigurableComponentProperty is a property within a ConfigurableComponent.
 * Usually these properties are displayed on the GUI, however, they don't always
 * have to be displayed.
 * Every property has these attributes:
 * name, propertyClass, label, defaultValue, value, allowedValues, 
 * These values should be get/set through the ComponentProperties interface.
 */
public class ConfigurableComponentProperty extends PropertyBase implements Property {
  static final long serialVersionUID = -1591739578094464420L;

  private ComponentName name;
  private Class propertyClass;
  private String label;
  private Object defaultValue;
  private Object value;
  private Set allowedValues;

  /**
   * Construct a Property with default values based on the name and value
   **/
  public ConfigurableComponentProperty(ConfigurableComponent c, String name, Object value) {
    super(c);
    this.name = new ComponentName(c, name);
    setValue(value);
    setDefaultValue(value);
    setLabel(name);
  }

  /**
   * Copy constructor creates a new ConfigurableComponentProperty from
   * an existing Property
   **/
  public ConfigurableComponentProperty(ConfigurableComponent c, String name, Property prop) {
    super(c);
    this.name = new ComponentName(c, name);
    setValue(prop.getValue());
    setPropertyClass(prop.getPropertyClass());
    setLabel(prop.getLabel());
    setDefaultValue(prop.getDefaultValue());
    setAllowedValues(prop.getAllowedValues());
  }

  /**
   * Gets the name of this Property
   *
   * @return a <code>CompositeName</code> value
   */
  public CompositeName getName() {
    return name;
  }

  /**
   * Gets the class of this Property.  The Class of the Property 
   * is the Class Type of values that are accepted by this property.
   *
   * @return a <code>Class</code> value
   */
  public Class getPropertyClass() {
    return propertyClass;
  }

  /**
   * Sets the class of this Property.  The Class indicates what type
   * of values are excepted in this Property.
   *
   * @param c Class of this property
   */
  public void setPropertyClass(Class c) {
    Class old = propertyClass;
    propertyClass = c;
    try {
      fireOtherChanged(old, PropertyEvent.CLASS_CHANGED);
    } catch (RuntimeException re) {
      propertyClass = old;
      throw re;
    }
  }

  /**
   * Gets the Label of this Property.  The Label is a text string
   * describing this property.
   *
   * @return a <code>String</code> value
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets the label of this Property.  The label is a text string
   * describing this property.
   *
   * @param label of this property
   */
  public void setLabel(String label) {
    String old = this.label;
    this.label = label;
    try {
      fireOtherChanged(old, PropertyEvent.LABEL_CHANGED);
    } catch (RuntimeException re) {
      this.label = old;
      throw re;
    }
  }

  /**
   * Gets the default value of this Property.  All Properties
   * should contain a default value.
   *
   * @return an <code>Object</code> value
   */
  public Object getDefaultValue() {
    return defaultValue;
  }

  /**
   * Sets the default value for this Property.  All Properties
   * should contain a default value.
   *
   * @param defaultValue of the Property
   */
  public void setDefaultValue(Object defaultValue) {
    Object old = this.defaultValue;
    this.defaultValue = defaultValue;
    try {
      fireOtherChanged(old, PropertyEvent.DEFAULTVALUE_CHANGED);
      if (value == null) fireValueChanged(old);
    } catch (RuntimeException re) {
      this.defaultValue = old;
      throw re;
    }
  }

  /**
   * Gets the value of this Property
   *
   * @return an <code>Object</code> value
   */
  public Object getValue() {
    if (value == null) return defaultValue;
    return value;
  }

  /**
   * Sets the value of this Property
   *
   * @param value of the property
   */
  public void setValue(Object value) {
    // MIK: should this be .equals?
    if ((value == null && this.value != null) || (value != null && ! value.equals(this.value))) {
      //    if(value != this.value) {
      Object old = this.value;

      // intern short (< 16 chars) strings
      if (value instanceof String) {
        String v = (String) value;
        if (v.length() < 16) {
          value = v.intern();
        }
      }

      this.value = value;
      try {
	fireValueChanged(old);
      } catch (RuntimeException re) {
	this.value = old;
	throw re;
      }
    }
  }

  /**
   * Gets a Set of all allowed values.
   * Properties allow ranges of allowable values,
   * If defined, they method returns that list.
   *
   * @return a <code>Set</code> value
   */
  public Set getAllowedValues() {
    return allowedValues;
  }

  /**
   * Sets a range of allowed values.
   * Properties allow ranges of allowable values,
   *
   * @param allowedValues 
   */
  public void setAllowedValues(Set allowedValues) {
    Set old = allowedValues;
    this.allowedValues = allowedValues;
    try {
      fireOtherChanged(old, PropertyEvent.ALLOWEDVALUES_CHANGED);
    } catch (RuntimeException re) {
      this.allowedValues = old;
      throw re;
    }
  }

  /**
   * Indicates if a value has been set for this Property.
   *
   * @return a <code>boolean</code> value
   */
  public boolean isValueSet() {
    return value != null;
  }

  private void readObject(ObjectInputStream stream)
    throws IOException, ClassNotFoundException
  {
    stream.defaultReadObject();
    // intern short (< 16 chars) strings
    if (value instanceof String) {
      String v = (String) value;
      if (v.length() < 16) {
        value = v.intern();
      }
    }
  }

}

