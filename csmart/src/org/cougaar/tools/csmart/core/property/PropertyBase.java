/* 
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

import org.cougaar.tools.csmart.core.property.name.CompositeName;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Base implementation of the <code>Property</code> Interface
 * @see Property for Documentation.
 */
public abstract class PropertyBase implements Property {
  private transient List listeners = null;
  private ConfigurableComponent component;
  private String tooltip;
  private URL help;
  private boolean visible = true;

  /**
   * Creates a new <code>PropertyBase</code> instance.
   *
   * @param c 
   */
  protected PropertyBase(ConfigurableComponent c) {
    component = c;
  }

  public final ConfigurableComponent getConfigurableComponent() {
    return component;
  }
  public abstract CompositeName getName();
  public abstract Class getPropertyClass();
  public abstract void setPropertyClass(Class c);
  public abstract String getLabel();
  public abstract void setLabel(String label);
  public abstract Object getDefaultValue();
  public abstract void setDefaultValue(Object defaultValue);
  public abstract Object getValue();
  public abstract void setValue(Object value);
  public abstract Set getAllowedValues();
  public abstract void setAllowedValues(Set allowedValues);
  public abstract boolean isValueSet();
  public String getToolTip() {
    return tooltip;
  }

  public Property setToolTip(String tt) {
    tooltip = tt;
    return this;
  }

  public URL getHelp() {
    return help;
  }

  public Property setHelp(URL url) {
    help = url;
    return this;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean isVisible() {
    return visible;
  }

  public void addPropertyListener(PropertyListener l) {
    // MIK: most have 1 or two listeners, and most are duplicates.
    // it would be better to have the listener slot be an Object
    // of either PropertyLister or LinkedList rather than waste 
    // our space with an ArrayList.
    if (listeners == null) listeners = new ArrayList(1);
    listeners.add(l);
  }

  public void removePropertyListener(PropertyListener l) {
    if (listeners == null) return;
    listeners.remove(l);
  }

  public Iterator getPropertyListeners() {
    if (listeners == null) return Collections.EMPTY_SET.iterator();
    return listeners.iterator();
  }

  protected boolean haveListeners() {
    return listeners != null && listeners.size() > 0;
  }

  protected void fireValueChanged(Object oldValue) {
    if (listeners != null) {
      PropertyEvent ev = new PropertyEvent(this, PropertyEvent.VALUE_CHANGED, oldValue);
      // Use array in case listeners remove themselves
      PropertyListener[] ls =
        (PropertyListener[]) listeners.toArray(new PropertyListener[listeners.size()]);
      for (int i = 0; i < ls.length; i++) {
        ls[i].propertyValueChanged(ev);
      }
    }
  }

  protected void fireOtherChanged(Object old, int whatChanged) {
    if (listeners != null) {
      PropertyEvent ev = new PropertyEvent(this, whatChanged, old);
      // Use array in case listeners remove themselves
      PropertyListener[] ls =
        (PropertyListener[]) listeners.toArray(new PropertyListener[listeners.size()]);
      for (int i = 0; i < ls.length; i++) {
        ls[i].propertyOtherChanged(ev);
      }
    }
  }

  private void writeObject(ObjectOutputStream stream)
    throws IOException
  {
    stream.defaultWriteObject();
    stream.writeObject(getSerializableListeners(listeners));
  }

  private List getSerializableListeners(List listeners) {
    List result = null;
    if (listeners != null) {
      result = new ArrayList(listeners.size());
      for (int i = 0, n = listeners.size(); i < n; i++) {
        Object o = listeners.get(i);
        if (o instanceof ConfigurableComponentListener) result.add(o);
      }
    }
    return result;
  }

  private void readObject(ObjectInputStream stream)
    throws IOException, ClassNotFoundException
  {
    stream.defaultReadObject();
    listeners = (List) stream.readObject();
  }

  public void printProperty(PrintStream out) {
    printProperty(out, "");
  }

  public void printProperty(PrintStream out, String indent) {
    out.println(indent + "Name: " + getName()); 
    out.println(indent + "Label: " + getLabel());
    out.println(indent + "Class: " + getPropertyClass());
    out.println(indent + "Value: " + getValue());
    out.println(indent + "Default: " + getDefaultValue());
    out.println(indent + "Allowed Values: " + getAllowedValues());
  }

}
