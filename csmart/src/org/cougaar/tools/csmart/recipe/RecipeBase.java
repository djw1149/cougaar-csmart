/**
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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
 *  </copyright>
 */
package org.cougaar.tools.csmart.recipe;

import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import java.util.Iterator;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.core.property.PropertiesListener;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import java.net.URL;
import java.util.List;
import org.cougaar.tools.csmart.core.property.ModificationListener;
import org.cougaar.tools.csmart.core.property.PropertyEvent;


/**
 * RecipeBase.java *
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public abstract class RecipeBase 
  extends ModifiableConfigurableComponent 
  implements RecipeComponent, PropertiesListener  {

  protected static final String DESCRIPTION_RESOURCE_NAME = 
    "No Description Available";

  protected boolean editable = true;
  protected String name = null;

  public RecipeBase (String name){
    super(name);
  }

  /**
   *
   * @return <description>
   */
  public URL getDescription()
  {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  /**
   *
   * @param param1 <description>
   */
  public void addModificationListener(ModificationListener l)
  {
    getEventListenerList().add(ModificationListener.class, l);
  }

  /**
   *
   * @param param1 <description>
   */
  public void removeModificationListener(ModificationListener l)
  {
    getEventListenerList().remove(ModificationListener.class, l);
  }

  /**
   *
   * @return <description>
   */
  public boolean isEditable()
  {
    return this.editable;
  }

  /**
   *
   * @param param1 <description>
   */
  public void setEditable(boolean param1)
  {
    this.editable = editable;
  }

  /**
   *
   * @param name Name of this component.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   *
   * @return <description>
   */
  public String getRecipeName()
  {    
    return this.name;
  }

  /**
   * Called when a new property has been added to the
   * society. 
   *
   * @param PropertyEvent Event for the new property
   */
  public void propertyAdded(PropertyEvent e) {
    Property addedProperty = e.getProperty();
    Property myProperty = getProperty(addedProperty.getName().last().toString());
    if (myProperty != null) {
      setPropertyVisible(addedProperty, true);
    }
  }
  /**
   * Called when a property has been removed from the society
   */
  public void propertyRemoved(PropertyEvent e) {}

  /**
   * Most recipes do not contain agents.
   *
   * @return an <code>AgentComponent[]</code> value
   */
  public AgentComponent[] getAgents() {
    return null;
  }

  /**
   * Most recipes do not add ComponentData, they modify it.
   * This is a helper impl.
   *
   * @param data 
   * @return a <code>ComponentData</code> value
   */
  public ComponentData addComponentData(ComponentData data) {
    return data;
  }

}// RecipeBase