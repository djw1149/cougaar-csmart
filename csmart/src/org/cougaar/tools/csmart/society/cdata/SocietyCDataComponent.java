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
package org.cougaar.tools.csmart.society.cdata;

import java.util.ArrayList;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyBase;

/**
 * The class for creating a configurable component that represents
 * a society from component data.
 */
public class SocietyCDataComponent extends SocietyBase {
  ComponentData cdata;

  public SocietyCDataComponent(ComponentData cdata) {
    super(cdata.getName());
    this.cdata = cdata;
  }

  public void initProperties() {
    // create society properties from cdata
    // create agents from cdata
    ArrayList agentData = new ArrayList();
    ArrayList alldata = new ArrayList();
    alldata.add(cdata);
    for (int i = 0; i < alldata.size(); i++) {
      ComponentData someData = (ComponentData)alldata.get(i);
      if (someData.getType().equals(ComponentData.AGENT)) {
        agentData.add(someData);
      } else {
        ComponentData[] moreData = someData.getChildren();
        for (int j = 0; j < moreData.length; j++) 
          alldata.add(moreData[j]);
      }
    }
    for (int i = 0; i < agentData.size(); i++) {
      AgentComponent agentComponent = 
        new AgentCDataComponent((ComponentData)agentData.get(i));
      agentComponent.initProperties();
      addChild(agentComponent);
    }
  }

}