/**
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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

import org.cougaar.tools.csmart.ui.component.*;
import java.io.Serializable;
import java.sql.SQLException;
import java.net.URL;
import java.util.Set;

public class ParameterInsertionRecipe extends ModifiableConfigurableComponent
  implements RecipeComponent, PropertiesListener, Serializable
{
  
  private static final String DESCRIPTION_RESOURCE_NAME = "parameter-insertion-recipe-description.html";
  private static final String BACKUP_DESCRIPTION = 
    "ParameterInsertionRecipe provides a method for inserting new Parameters into a PSP";

  private static final String PROP_QUERY = "Agent Query";
  private static final String PROP_QUERY_DFLT = "";
  private static final String PROP_QUERY_DESC = "Query for agents to update PSP parameters";

  private static final String PROP_PLUGINNAME = "Plugin Name";
  private static final String PROP_PLUGINNAME_DFLT = "";
  private static final String PROP_PLUGINNAME_DESC = "Name of the Plugin to add Parameter to";

  private static final String PROP_PARAMETER = "Parameter";
  private static final String PROP_PARAMETER_DFLT = "";
  private static final String PROP_PARAMETER_DESC = "The new Parameter";

  private Property propQuery;
  private Property propPluginName;
  private Property propParameter;

  private boolean editable = true;

  public ParameterInsertionRecipe() {
    super("Paramter Insertion Recipe");
  }

  public ParameterInsertionRecipe(String name) {
    super(name);
  }

  public void initProperties() {
    propQuery = addRecipeQueryProperty(PROP_QUERY, PROP_QUERY_DFLT);
    propQuery.setToolTip(PROP_QUERY_DESC);

    propPluginName = addProperty(PROP_PLUGINNAME, PROP_PLUGINNAME_DFLT);
    propPluginName.setToolTip(PROP_PLUGINNAME_DESC);

    propParameter = addProperty(PROP_PARAMETER, PROP_PARAMETER_DFLT);
    propParameter.setToolTip(PROP_PARAMETER_DESC);

  }

  private Property addRecipeQueryProperty(String name, String dflt) {
    Property prop = addProperty(new RecipeQueryProperty(this, name, dflt));
    prop.setPropertyClass(String.class);
    return prop;
  }

  public String getRecipeName() {
    return getShortName();
  }

  public AgentComponent[] getAgents() {
    return null;
  }

  public ComponentData addComponentData(ComponentData data) {
    return data;
  }

  public ComponentData modifyComponentData(ComponentData data, PopulateDb pdb) {
    try {
      Set targets = pdb.executeQuery(propQuery.getValue().toString());
      modifyComponentData(data, pdb, targets);
    } catch (SQLException sqle) {
      sqle.printStackTrace();
    }
    return data;
  }

  private void modifyComponentData(ComponentData data, PopulateDb pdb, Set targets)
    throws SQLException
  {
    String pluginAlib = data.getName() + "|" + propPluginName.getValue().toString();

    if (targets.contains(pdb.getComponentAlibId(data))) {
      ComponentData[] children = data.getChildren();
      for (int i=0; i < children.length; i++ ) {        
        if (children[i].getName().equals(pluginAlib)) {
	  // FIXME: Make sure this parameter isnt already there?
          children[i].addParameter(propParameter.getValue().toString());
          break;
        }        
      }
    }
    if (data.childCount() > 0) {
      // for each child, call this same method.
      ComponentData[] children = data.getChildren();
      for (int i = 0; i < children.length; i++) {
	modifyComponentData(children[i], pdb, targets);
      }
    }      
  }

  ///////////////////////////////////////////
  // Boilerplate stuff added below... Necessary?
  
  // Implement PropertyListener
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
  public void propertyRemoved(PropertyEvent e) {
    // FIXME - do something?
  }
  // end of PropertyListener implementation

  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  /**
   * Returns whether or not the component can be edited.
   * @return true if component can be edited and false otherwise
   */
  public boolean isEditable() {
    //    return !isRunning;
    return editable;
  }

  /**
   * Set whether or not the component can be edited.
   * @param editable true if component is editable and false otherwise
   */
  public void setEditable(boolean editable) {
    this.editable = editable;
  }

}// ParameterInsertionRecipe
