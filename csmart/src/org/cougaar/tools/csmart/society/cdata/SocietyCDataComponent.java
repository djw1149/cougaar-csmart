/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society.cdata;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyBase;

import java.util.ArrayList;

/**
 * The class for creating a configurable component that represents
 * a society from component data.
 */
public class SocietyCDataComponent extends SocietyBase {
  ComponentData cdata;

  protected static final String DESCRIPTION_RESOURCE_NAME = "/org/cougaar/tools/csmart/society/society-base-description.html";
  protected static final String BACKUP_DESCRIPTION =
    "A Society: Agents, Binders, Plugins, etc.";

  public SocietyCDataComponent(ComponentData cdata, String assemblyId) {
    super(cdata.getName());
    this.cdata = cdata;
    this.oldAssemblyId = assemblyId;
  }

  public void initProperties() {
    // create society properties from cdata
    // create agents from cdata
    ArrayList agentData = new ArrayList();
    ArrayList alldata = new ArrayList();
    if (cdata != null)
      alldata.add(cdata);
    
    // FIXME: It'd be nice to deal with binders of Agents in here!!!

    // Find all the agents
    for (int i = 0; i < alldata.size(); i++) {
      ComponentData someData = (ComponentData)alldata.get(i);
//       if (log.isDebugEnabled())
// 	log.debug("initProps: alldata(" + i + ")= " + someData);
      String type = someData.getType();
//       if (log.isDebugEnabled())
// 	log.debug("type = " + type);
      if (type.equals(ComponentData.AGENT)) {
// 	if (log.isDebugEnabled())
// 	  log.debug("adding to list of agents if not already there");

	// Do not add the same Agent to this society twice.
	// This should not be necessary if the incoming
	// CDATA is correctly created - usu from 
	// Experiment.getSocietyComponentData()
	// If we dont do this, ConfigurableComponent.addChild, when
	// we create an AgentCDataComponent for this and
	// add it to this society, will rename the Agent
	// for us quietly
	if (! agentData.contains(someData))
	  agentData.add(someData);

      } else if (type.equals(ComponentData.NODE) || type.equals(ComponentData.HOST) || type.equals(ComponentData.SOCIETY)) {
        ComponentData[] moreData = someData.getChildren();
        for (int j = 0; j < moreData.length; j++) 
          alldata.add(moreData[j]);
      }
    }

    if (log.isDebugEnabled())
      log.debug("initProps found " + agentData.size() + " agents to add");

    // For each agent, create a component and add it as a child
    for (int i = 0; i < agentData.size(); i++) {
//       if (log.isDebugEnabled())
// 	log.debug("initProps had CDATA(" + i + "): " + (ComponentData)agentData.get(i));
      AgentComponent agentComponent = 
        new AgentCDataComponent((ComponentData)agentData.get(i));
      agentComponent.initProperties();
//       if (log.isDebugEnabled())
// 	log.debug("... which after initProps produced AgentCDataComponent " + agentComponent.getFullName().toString());
      addChild(agentComponent);
//       if (log.isDebugEnabled())
// 	log.debug("... and which after addChild is " + agentComponent.getFullName().toString());
    }
  }

}
