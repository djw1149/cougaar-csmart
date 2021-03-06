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

package org.cougaar.tools.csmart.ui.experiment;

import org.cougaar.core.agent.AgentManager;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.core.plugin.PluginManager;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.experiment.DBExperiment;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Window to show the detailed contents of an Agent
 **/
public class AgentInfoPanel extends JPanel {
  private transient Logger log;

  public AgentInfoPanel(Experiment experiment, String agentName) {
    super();
    log = CSMART.createLogger(this.getClass().getName());

    // Warning: This next line may save the whole experiment
    ComponentData societyComponentData = experiment.getSocietyComponentData();

    if (societyComponentData == null) {
      JOptionPane.showMessageDialog(this, "No information available",
                                    "No Information",
                                    JOptionPane.PLAIN_MESSAGE);
      if (log.isDebugEnabled()) {
	log.debug("Experiment returned no component data: " + experiment.getExperimentName());
      }
      return;
    }
    ComponentData agentComponentData = null;
    ComponentData[] children = societyComponentData.getChildren();

    // Yuck this is ugly.....
    if (societyComponentData.getType().equals(ComponentData.NODE) || societyComponentData.getType().equals(ComponentData.AGENT)) {
      if (societyComponentData.getName().equals(agentName)) {
	agentComponentData = societyComponentData;
      }
    }

    if (agentComponentData == null) {

      // Loop to find the Agent we want
      for (int i = 0; i < children.length; i++) {
	if (agentComponentData != null)
	  break;
	if (children[i].getType().equals(ComponentData.NODE)) {
	  // Let it possibly be the Node itself
	  if (agentName.equals(children[i].getName())) {
	    agentComponentData = children[i];
	    break;
	  }

	  ComponentData[] agents = children[i].getChildren();
	  for (int k = 0; k < agents.length; k++) {
	    if (agentComponentData != null)
	      break;
	    if (agents[k].getName().equals(agentName)) {
	      agentComponentData = agents[k];
	      break;
	    }
	  }
	} else if (children[i].getType().equals(ComponentData.AGENT)) {
	  // Let it possibly be the Node itself
	  if (agentName.equals(children[i].getName())) {
	    agentComponentData = children[i];
	    break;
	  }
	} else if (children[i].getType().equals(ComponentData.HOST)) {
	  ComponentData[] nodes = children[i].getChildren();

	  for (int j = 0; j < nodes.length; j++) {
	    if (agentComponentData != null)
	      break;
	    // Let it possibly be the Node itself
	    if (agentName.equals(nodes[j].getName())) {
	      agentComponentData = nodes[j];
	      break;
	    }

	    ComponentData[] agents = nodes[j].getChildren();
	    for (int k = 0; k < agents.length; k++) {
	      if (agentComponentData != null)
		break;
	      if (agents[k].getName().equals(agentName)) {
		agentComponentData = agents[k];
		break;
	      }
	    }
	  }
	}
      }
    }

    if (agentComponentData == null) {
      JOptionPane.showMessageDialog(this, "No information available",
                                    "No Information",
                                    JOptionPane.PLAIN_MESSAGE);
      if (log.isDebugEnabled()) {
	log.debug("Got null agentComponentData after search?");
      }
      return;
    }

    ComponentData[] agentChildren = agentComponentData.getChildren();
    ArrayList entries = new ArrayList(agentChildren.length);
    for (int i = 0; i < agentChildren.length; i++) {
      StringBuffer sb = new StringBuffer();
      if (agentChildren[i].getType().equals(ComponentData.AGENTBINDER)) {
	sb.append(PluginManager.INSERTION_POINT + ".Binder");
      } else if (agentChildren[i].getType().equals(ComponentData.NODEBINDER)) {
	sb.append(AgentManager.INSERTION_POINT + ".Binder");
      } else {
	sb.append(agentChildren[i].getType());
      }
      if(ComponentDescription.parsePriority(agentChildren[i].getPriority()) !=
	 ComponentDescription.PRIORITY_COMPONENT) {
	sb.append("(" + agentChildren[i].getPriority() + ")");
      }
      sb.append(" = ");
      sb.append(agentChildren[i].getClassName());
      if (agentChildren[i].parameterCount() != 0) {
        sb.append("(");
        Object[] params = agentChildren[i].getParameters();
        sb.append(params[0].toString());
        for (int j = 1; j < agentChildren[i].parameterCount(); j++) {
          sb.append(",");
          sb.append(params[j].toString());
        }
        sb.append(")");
      }
      entries.add(sb.toString());
    }
    JList plugInsList = new JList(entries.toArray());
    JScrollPane jsp = new JScrollPane(plugInsList);
    jsp.setPreferredSize(new Dimension(550, 200));
    setLayout(new GridBagLayout());
    plugInsList.setBackground(getBackground());
    int x = 0;
    int y = 0;
    add(new JLabel("SubComponents:"),
        new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                               GridBagConstraints.WEST,
                               GridBagConstraints.NONE,
                               new Insets(10, 0, 5, 5),
                               0, 0));
    add(jsp,
        new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                               GridBagConstraints.WEST,
                               GridBagConstraints.NONE,
                               new Insets(0, 0, 5, 0),
                               0, 0));
    setSize(400, 400);
  }
}
