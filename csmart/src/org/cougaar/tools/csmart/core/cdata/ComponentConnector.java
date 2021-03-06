/**
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.core.cdata;



import org.cougaar.core.agent.AgentManager;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.core.node.INIParser;
import org.cougaar.core.plugin.PluginManager;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.viewer.SocietyFinder;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

/**
 * ComponentConnector.java
 *
 * ComponentConnector converts a <code>ComponentDescription</code> to a
 * <code>ComponentData</code> object.  When INI files are parsed, they
 * are parsed into a <code>ComponentDescription</code> object.  To use the
 * parsed data within CSMART, it must be of type <code>ComponentData</code>.
 *
 * Created: Wed Feb 20 09:40:54 2002
 *
 * @see ComponentData
 * @see org.cougaar.core.component.ComponentDescription
 */

public class ComponentConnector {
  
  public static ComponentData createComponentData(String filename) {
    ComponentConnector cc= new ComponentConnector();
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.cdata.ComponentConnector");
    ComponentData data = new GenericComponentData();
    ComponentDescription[] desc = null;

    // Parse the Node Level file.
    desc = cc.parseFile(filename);

    for(int i=0; i < desc.length; i++) {
      ComponentData agent = cc.createComponentData(desc[i]);
      //Parse the Agent File.
      ComponentDescription[] agentDesc = cc.parseFile(agent.getName());
      for(int j=0; j < agentDesc.length; j++) {
        ComponentData plugin = cc.createComponentData(agentDesc[j]);
        plugin.setParent(agent);
        if(log.isDebugEnabled()) {
          log.debug("Creating: " + plugin.getName() + " with parent: " + agent.getName());
        }
        agent.addChild(plugin);
      }

      data.addChild(agent);
    }

    return data;
  }

  public static ComponentDescription[] parseFile(String filename) {
    ComponentDescription[] desc = null;

    Logger log = 
     CSMART.createLogger("org.cougaar.tools.csmart.core.cdata.ComponentConnector");

    if(!filename.endsWith(".ini")) {
      filename = filename + ".ini";
    }

    if(log.isDebugEnabled()) {
      log.debug("Parsing File: " + filename);
    }

    try {
      InputStream in = SocietyFinder.getInstance().open(filename);
      //InputStream in = ConfigFinder.getInstance().open(filename);
      try { 
        desc = INIParser.parse(in);
      } finally {
        in.close();
      }
    } catch( Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Caught an error reading file", e);
      }
    }
    return desc;
  }

  private ComponentData createComponentData(ComponentDescription desc) {
    ComponentData data = null;

    Logger log = 
     CSMART.createLogger("org.cougaar.tools.csmart.core.cdata.ComponentConnector");

    String type = desc.getInsertionPoint();
    String shorttype = type.substring(type.lastIndexOf(".")+1);
    if(log.isDebugEnabled()) {
      log.debug("Component Type is: " + shorttype);
    }

    if(shorttype.equalsIgnoreCase("agent")) {
      if(log.isDebugEnabled()) {
        log.debug("Creating AgentComponentData");
      }

      data = new AgentComponentData();
      data.setType(ComponentData.AGENT);
      Vector v = (Vector)desc.getParameter();
      data.setName((String)v.get(0));

    } else {
      if(log.isDebugEnabled()) {
        log.debug("Creating GenericComponentData");
      }

      data = new GenericComponentData();
      data.setType(type);
      data.setName(desc.getName());
      data.setPriority(ComponentDescription.priorityToString(desc.getPriority()));
      Vector v = (Vector)desc.getParameter();
      for(int i=0; i < v.size(); i++) {
        data.addParameter(v.get(i));
      }
      if(shorttype.equalsIgnoreCase("plugin")) {
        data.setType(ComponentData.PLUGIN);
      } else {
	if (shorttype.equalsIgnoreCase("binder")) {
	  // Node or Agent?
	  if (type.equalsIgnoreCase(AgentManager.INSERTION_POINT + ".Binder")) 
	    data.setType(ComponentData.NODEBINDER);
	  else if (type.equalsIgnoreCase(PluginManager.INSERTION_POINT + ".Binder")) 
	    data.setType(ComponentData.NODEBINDER);
	}
        if(log.isWarnEnabled()) {
          log.warn("Un-Supported Type: " + shorttype);
        }
        // TODO: Add all other types.
      }
    }
    if(log.isDebugEnabled()) {
      log.debug("Name: " + data.getName());
    }

    data.setClassName(desc.getClassname());
    return data;
  }

  public static String getAgentName(ComponentDescription desc) {
    if(desc.getParameter() != null) {
      Vector v = (Vector)desc.getParameter();
      return (String)v.get(0);
    } else {
      return null;
    }
  }

  public static Iterator getPluginProps(ComponentDescription desc) {
    if(desc.getParameter() != null) {
      Vector v = (Vector)desc.getParameter();
      return v.iterator();
    } else {
      return Collections.EMPTY_LIST.iterator();
    }
  }

//   public static ComponentData createComponentData(String compName, 
//                                                   ComponentDescription desc) {
//     ComponentData data = null;

//     Logger log = 
//      CSMART.createLogger("org.cougaar.tools.csmart.core.cdata.ComponentConnector");

//     String type = desc.getInsertionPoint();
//     type = type.substring(type.lastIndexOf(".")+1);
//     if(log.isDebugEnabled()) {
//       log.debug("Component Type is: " + type);
//     }

//     if(type.equalsIgnoreCase("agent")) {
//       if(log.isDebugEnabled()) {
//         log.debug("Creating AgentComponentData");
//       }

//       data = new AgentComponentData();
//       data.setType(ComponentData.AGENT);
//       Vector v = (Vector)desc.getParameter();
//       data.setName((String)v.get(0));

//     } else {
//       if(log.isDebugEnabled()) {
//         log.debug("Creating GenericComponentData");
//       }

//       data = new GenericComponentData();
//       if(type.equalsIgnoreCase("plugin")) {
//         data.setType(ComponentData.PLUGIN);
//       } else {
//         if(log.isWarnEnabled()) {
//           log.warn("Un-Supported Type: " + type);
//         }
//         // TODO: Add all other types.
//       }
//       data.setName(desc.getName());
//       Vector v = (Vector)desc.getParameter();
//       for(int i=0; i < v.size(); i++) {
//         data.addParameter(v.get(i));
//       }
//     }

//     data.setClassName(desc.getClassname());
//     return data;
//   }

    
}// ComponentConnector
