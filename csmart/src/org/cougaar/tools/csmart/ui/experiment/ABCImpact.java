/* 
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
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.experiment;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFileChooser;
import java.util.Collection;

import org.cougaar.tools.server.ConfigurationWriter;
import org.cougaar.util.EmptyIterator;

import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.viewer.Organizer;
import org.cougaar.tools.csmart.configgen.abcsociety.ABCAgent;

/**
 * Impact for ABC XML specified RealWorldEvents. 
 * Includes static method for getting an XML file.<br>
 * <br>
 * getAgents will then return a Generator and Transducer Agent.
 * And the ConfigurationWriter will write them out, and also
 * be able to modify the Node.ini files as necessary.
 * 
 * @see org.cougaar.tools.csmart.ldm.event.RealWorldEvent
 * @see org.cougaar.tools.csmart.plugin.ScriptedEventPlugIn
 */
public class ABCImpact 
  extends ModifiableConfigurableComponent
  implements PropertiesListener, Serializable, ImpactComponent {
  private String name;

  // These should be removed when we switch to ComponentData method
  private String xmlFileContents;
  private String societyFileContents;
  private String rweFile;
  private String societyFile;

  private static final String DESCRIPTION_RESOURCE_NAME = "description.html";
  private static final String BACKUP_DESCRIPTION = 
    "Impact description not available";

  /** Property Definitions for Real World Events **/
  public static final String  PROP_CYBERCOUNT      = "Cyber Events";
  public static final Integer PROP_CYBERCOUNT_DFLT = new Integer(0);
  public static final String  PROP_CYBERCOUNT_DESC = "Number of Cyber Events in this experiment";

  /** Property Definitions for Kinetic Events **/
  public static final String  PROP_KINETICCOUNT  = "Kinetic Events";
  public static final Integer PROP_KINETICCOUNT_DFLT = new Integer(0);
  public static final String  PROP_KINETICCOUNT_DESC = "Number of Kinetic Events in this experiment";

  public static final String PROP_SAMPLESPERSECOND = "Samples Per Second";
  public static final String PROP_SAMPLESPERSECOND_DFLT = "2";
  public static final String PROP_SAMPLESPERSECOND_DESC = "How many times per second messages should be released from the queues";

  public static final String PROP_IN_MSGSPERSECOND = "In Messages Per Second";
  public static final String PROP_IN_MSGSPERSECOND_DFLT = "10";
  public static final String PROP_IN_MSGSPERSECOND_DESC = "How many messages can be received per second by an agent";

  public static final String PROP_OUT_MSGSPERSECOND = "Out Messages Per Second";
  public static final String PROP_OUT_MSGSPERSECOND_DFLT = "10";
  public static final String PROP_OUT_MSGSPERSECOND_DESC = "How many messages can be sent per second by an agent";

  public static final String BinderClass_name = "org.cougaar.tools.csmart.binder.SlowMessageTransportServiceFilter";

  private static final String tAgentName = "Transducer";
  private static final String gAgentName = "Generator";
  private static final String socFileName = "Society.dat";

  private Property propCyberCount;
  private Property propKineticCount;
  private Property propSamplesPerSecond;
  private Property propInMsgsPerSecond;
  private Property propOutMsgsPerSecond;

  private ImpactAgentComponent transducerAgent;
  private ImpactAgentComponent generatorAgent;

  private boolean editable = true;
  private ArrayList agentNames = null;

  public ABCImpact() {
    this("ABC Impact");
  }

  public ABCImpact (String name) {
    super(name);
    this.name = name;
    agentNames = new ArrayList();
  }

  public void initProperties() {
    propCyberCount = addProperty(PROP_CYBERCOUNT, PROP_CYBERCOUNT_DFLT, 
			       new ConfigurableComponentPropertyAdapter() {
				 public void propertyValueChanged(PropertyEvent e) {
				   int newVal = ((Integer)e.getProperty().getValue()).intValue();
				   int prevVal = ((Integer)e.getPreviousValue()).intValue();
				   if(newVal < 1) {
				     propCyberCount.setValue(new Integer(prevVal));
				   } else if( newVal == prevVal ) {
				     // Ignore it.
				   } else {
				     changeCyberImpactCount(newVal);
				   }
				 }
			       });
    propCyberCount.setToolTip(PROP_CYBERCOUNT_DESC);

    propKineticCount = addProperty(PROP_KINETICCOUNT, PROP_KINETICCOUNT_DFLT, 
			       new ConfigurableComponentPropertyAdapter() {
				 public void propertyValueChanged(PropertyEvent e) {
				   int newVal = ((Integer)e.getProperty().getValue()).intValue();
				   int prevVal = ((Integer)e.getPreviousValue()).intValue();
				   if(newVal < 1) {
				     propCyberCount.setValue(new Integer(prevVal));
				   } else if( newVal == prevVal ) {
				     // Ignore it.
				   } else {
				     changeKineticImpactCount(newVal);
				   }
				 }
			       });
    propKineticCount.setToolTip(PROP_KINETICCOUNT_DESC);

    propSamplesPerSecond = addProperty(PROP_SAMPLESPERSECOND, PROP_SAMPLESPERSECOND_DFLT, 
			       new ConfigurableComponentPropertyAdapter() {
				 public void propertyValueChanged(PropertyEvent e) {
				   if(((String)e.getProperty().getValue()).equals("")) {
				     propSamplesPerSecond.setValue(e.getPreviousValue());
				   }
				 }
			       });
    propSamplesPerSecond.setToolTip(PROP_SAMPLESPERSECOND_DESC);

    propInMsgsPerSecond = addProperty(PROP_IN_MSGSPERSECOND, PROP_IN_MSGSPERSECOND_DFLT, 
			       new ConfigurableComponentPropertyAdapter() {
				 public void propertyValueChanged(PropertyEvent e) {
				   if(((String)e.getProperty().getValue()).equals("")) {
				     propInMsgsPerSecond.setValue(e.getPreviousValue());
				   }
				 }
			       });
    propInMsgsPerSecond.setToolTip(PROP_IN_MSGSPERSECOND_DESC);

    propOutMsgsPerSecond = addProperty(PROP_OUT_MSGSPERSECOND, PROP_OUT_MSGSPERSECOND_DFLT, 
			       new ConfigurableComponentPropertyAdapter() {
				 public void propertyValueChanged(PropertyEvent e) {
				   if(((String)e.getProperty().getValue()).equals("")) {
				     propOutMsgsPerSecond.setValue(e.getPreviousValue());
				   }
				 }
			       });
    propOutMsgsPerSecond.setToolTip(PROP_OUT_MSGSPERSECOND_DESC);

    // Create our two agents here.
    transducerAgent = new ImpactAgentComponent("Transducer", false);
    transducerAgent.initProperties();
    addChild(transducerAgent);

    generatorAgent = new ImpactAgentComponent("Generator", true);
    generatorAgent.initProperties();
    addChild(generatorAgent);

    // Remove this hack when we switch to ComponentData
    rweFile = (String) getFullName().toString() + "_Impacts.xml";
    societyFile = (String) getFullName().toString() + "_Society.dat";

  }

  public void setName(String newName) {
    this.name = newName;
  }

  public String getImpactName() {
    return name;
  }
  
  /**
   * Get the agents, both assigned and unassigned.
   * @return array of agent components
   */
  public AgentComponent[] getAgents() {
    AgentComponent[] agents = new AgentComponent[2];
    agents[0] = transducerAgent;
    agents[1] = generatorAgent;
    
    return agents;
  }

  public void changeCyberImpactCount(int newCount) {
    Collection c = getDescendentsOfClass(ABCCyberImpact.class);
    int index = c.size();
    if(index > newCount) {
      // Remove some      
    } else {
      // Add some
      for(int i=index; i < newCount; i++) {
      ABCCyberImpact cyber = new ABCCyberImpact("CyberImpact" + i);
      cyber.initProperties();
      addChild(cyber);
      } 
    }
  }

  public void changeKineticImpactCount(int newCount) {
    Collection c = getDescendentsOfClass(ABCKineticImpact.class);
    int index = c.size();
    if(index > newCount) {
      // Remove some
    } else {
      // Add some
      for(int i=index; i < newCount; i++) {
	ABCKineticImpact kinetic = new ABCKineticImpact("Kinetic Impact" + i);
	kinetic.initProperties();
	addChild(kinetic);
      } 
    }
  }
  
  /**
   * Get a configuration writer for this Impact.
   * Warning: This Impact assumes that it has been given
   * All of the Nodes in the Society
   */
  public ConfigurationWriter getConfigurationWriter(NodeComponent[] nodes) {
    return new ABCImpactCWriter(nodes);
  }

  class ABCImpactCWriter implements ConfigurationWriter {
    AgentComponent[] allagents;
    
    public ABCImpactCWriter(NodeComponent[] nodes) {
      ArrayList tmp = new ArrayList();
      for (int i = 0; i < nodes.length; i++) {
	NodeComponent node = nodes[i];
	AgentComponent[] ags = node.getAgents();
	for (int j = 0; j < ags.length; j++) {
	  tmp.add(ags[j]);
	}
      }
      this.allagents = (AgentComponent[])tmp.toArray(new AgentComponent[tmp.size()]);
    }

    /**
     * Just writes out the XML File and the Society File.
     * The Agent files get written out by the societies themselves,
     * as things are currently written.<br>
     * WARNING: This assumes it has gotten all of the Nodes
     * in the Society!
     *
     * @param configDir a <code>File</code> path
     * @exception IOException if an error occurs
     */
    public void writeConfigFiles(File configDir) throws IOException {
      writeFile(configDir, xmlFileContents, (String)rweFile);
      writeFile(configDir, societyFileContents, societyFile);
    }
  }
  
  /**
   * This is the opportunity for an impact to specify additional
   * components to load into non-Impact Agents
   *
   * @return a <code>String</code> Node file addition, possibly null
   */
  public String getNodeFileAddition() {
    StringBuffer buf = new StringBuffer();
    ComponentData data = createBinderComponentData();

    buf.append(data.getName());
    buf.append(" = ");
    buf.append(data.getClassName());
    buf.append("(");
    Object[] params = data.getParameters();
    for(int i=0; i < params.length; i++) {
      if((i+1) == params.length) {
	buf.append(params[i] + ")\n");
      } else {
	buf.append(params[i] + ", ");
      }
    }
    return buf.toString();
  }

  private ComponentData createBinderComponentData() {
    ComponentData data = new GenericComponentData();

    data.setType(ComponentData.BINDER);
    data.setName("Node.Agent.Binder");
    data.setClassName(BinderClass_name);
    data.addParameter(propSamplesPerSecond.getValue());
    data.addParameter(propInMsgsPerSecond.getValue());
    data.addParameter(propOutMsgsPerSecond.getValue());

    return data;
  }

  private void writeFile(File configDir, String contents, String fname) {
     PrintWriter writer = null;
    if (configDir == null)
      configDir = new File(System.getProperty("org.cougaar.install.path"));
    try {
      writer = new PrintWriter(new FileWriter(new File(configDir, fname)));
      writer.print(contents);
    } catch (IOException e) {
    } finally {
      try {
	writer.close();
      } catch (NullPointerException e) {
      }
    }
  }

  private LeafComponentData createSocietyLeaf() {
    GenericLeafComponentData lcd = new GenericLeafComponentData();
    StringBuffer sb = new StringBuffer();

    lcd.setType(LeafComponentData.FILE);
    lcd.setName(societyFile);

    Iterator iter = agentNames.iterator();
    while(iter.hasNext()) {      
      String name = (String)iter.next();
      sb.append(name);
      sb.append(", ");
      sb.append("0.0");  // Latitude
      sb.append(", ");
      sb.append("0.0");  // Longitude
      sb.append("\n");
    }

    lcd.setValue(sb);

    // Remove when we switch to componentData.
    societyFileContents = sb.toString();

    return lcd;
  }

  private LeafComponentData createRWELeaf() {
    GenericLeafComponentData lcd = new GenericLeafComponentData();
    StringBuffer sb = new StringBuffer();

    lcd.setType(LeafComponentData.FILE);
    lcd.setName(rweFile);

    sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
    sb.append("<!DOCTYPE EVENTLIST [\n");
    sb.append("\n");
    sb.append("<!ELEMENT EVENTLIST ((CYBER|KINETIC)*)>\n");
    sb.append("\n");
    sb.append("<!ELEMENT CYBER (PARAM*)>\n");
    sb.append("<!ATTLIST CYBER  TARGET CDATA #REQUIRED\n");
    sb.append("		        TIME CDATA #REQUIRED\n");
    sb.append("		        TYPE CDATA #REQUIRED>\n");
    sb.append("\n");
    sb.append("<!ELEMENT KINETIC (PARAM*)>\n");
    sb.append("<!ATTLIST KINETIC LATITUDE CDATA #REQUIRED\n");
    sb.append(" 		 LONGITUDE CDATA #REQUIRED\n");
    sb.append("		         TIME CDATA #REQUIRED\n");
    sb.append("		         TYPE CDATA #REQUIRED>\n");
    sb.append("\n");
    sb.append("<!ELEMENT PARAM EMPTY>\n");
    sb.append("<!ATTLIST PARAM NAME CDATA #REQUIRED\n");
    sb.append("                VALUE CDATA #REQUIRED>\n");
    sb.append("\n");
    sb.append("]>\n");
    sb.append("\n");
    sb.append("<EVENTLIST>\n");

    // Add all events in here.
    Iterator iter = ((Collection)getDescendentsOfClass(ABCCyberImpact.class)).iterator();
    while(iter.hasNext()) {
      ABCCyberImpact impact = (ABCCyberImpact)iter.next();
      sb.append(impact.getXML());
    }

    iter = ((Collection)getDescendentsOfClass(ABCKineticImpact.class)).iterator();
    while(iter.hasNext()) {
      ABCKineticImpact impact = (ABCKineticImpact)iter.next();
      sb.append(impact.getXML());
    }

    sb.append("</EVENTLIST>");
    
    lcd.setValue(sb);

    // This will eventually get removed, used now to
    // support the old method of file writing.
    xmlFileContents = sb.toString();

    return lcd;
  }

  private void writeSocietyFile(File configDir, AgentComponent[] agents) {
    writeFile(configDir, societyFileContents, socFileName);
  }

  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  public boolean isEditable() {
    return this.editable;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  public void addModificationListener(ModificationListener l) {
    getEventListenerList().add(ModificationListener.class, l);
  }

  public void removeModificationListener(ModificationListener l) {
    getEventListenerList().remove(ModificationListener.class, l);
  }

  public void fireModification() {
    fireModification(new ModificationEvent(this, ModificationEvent.SOMETHING_CHANGED));
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


  public ComponentData addComponentData(ComponentData data) {

    processData(data);

    data.addLeafComponent(createRWELeaf());
    data.addLeafComponent(createSocietyLeaf());

    return data;
  }

  public void processData(ComponentData data) {
    ComponentData[] children = data.getChildren();
    
    for(int i=0; i < children.length; i++) {
      ComponentData child = children[i];
      if(child.getType() == ComponentData.NODE) {
	child.addChild(0, createBinderComponentData());
      } else if(child.getType() == ComponentData.AGENT) {
	// Process it.
	if(child.getName().equals(generatorAgent.getFullName().toString())) {
	  // Found Generator
	  child.setOwner(this);
	  child = generatorAgent.getComponentData(child);
	} else if(child.getName().equals(transducerAgent.getFullName().toString())) {
	  // Found Transducer
	  child.setOwner(this);
	  child = transducerAgent.getComponentData(child);
	} else {
	  // Add to list for Society
	  agentNames.add(child.getName());
	}
      } else {
	// Process it's children.
	processData(child);
      }      
    }
  }

  public ComponentData modifyComponentData(ComponentData data) {
    return data;
  }

  // Simple ImpactAgent Component.
  class ImpactAgentComponent extends ConfigurableComponent 
    implements AgentComponent, Serializable {

    private boolean generator = false;

    private String agentClass_name = "org.cougaar.core.cluster.ClusterImpl";
    private String planServer_name = "org.cougaar.lib.planserver.PlanServerPlugIn";
    private String scriptedEvent_name = "org.cougaar.tools.csmart.plugin.ScriptedEventPlugIn";
    private String transducer_name = "org.cougaar.tools.csmart.plugin.TransducerPlugIn";

    public ImpactAgentComponent() {
      this("ImpactAgent", false);
    }

    public ImpactAgentComponent(boolean generator) {
      this("ImpactAgent", generator);
    }

    public ImpactAgentComponent(String name, boolean generator) {
      super(name);
      this.generator = generator;
    }

    public void initProperties() {
      // No props to init.
    }

    public String getConfigLine() {
      return null;
    }

    public boolean isGenerator() {
      return generator;
    }

    public void writeIniFile(File configDir) throws IOException {
      File iniFile = new File(configDir, this.getFullName() + ".ini");
      PrintWriter writer = new PrintWriter(new FileWriter(iniFile));

      try {
	writer.println("# $id$");
	writer.println("[ Cluster ]");
	writer.println("class = " + agentClass_name);
	writer.println("uic = \"" + this.getFullName().toString() + "\"");
	writer.println("cloned = false");
	writer.println();
	writer.println("[ PlugIns ]");
	if(generator) {
	  writer.print("plugin = " + scriptedEvent_name);
	  writer.print("(" + rweFile + ", ");
	  Iterator iter = 
	    ((Collection)this.getParent().getDescendentsOfClass(ImpactAgentComponent.class)).iterator();
	  while(iter.hasNext()) {
	    ImpactAgentComponent iac = (ImpactAgentComponent)iter.next();
	    if(!iac.isGenerator()) {
	      writer.print(iac.getFullName().toString() + ")\n");
	    }
	  }
	} else {
	  writer.print("plugin = " + transducer_name);
	  writer.println("(" + societyFile + ")");
	}
	writer.println("plugin = " + planServer_name);
	writer.println();
	writer.println("[ Policies ]");
	writer.println();
	writer.println("[ Permission ]");
	writer.println();
	writer.println("[ AuthorizedOperation ]");
      }
      finally {
	writer.close();
      }
    }

    public ComponentData getComponentData(ComponentData child) {
      StringBuffer sb = new StringBuffer();
      ABCImpact parent = (ABCImpact)this.getParent();

      // Remove this hack when we switch to ComponentData
      rweFile = (String) parent.getFullName().toString() + "_Impacts.xml";
      societyFile = (String) parent.getFullName().toString() + "_Society.dat";

      GenericComponentData plugin = new GenericComponentData();
      plugin.setType(ComponentData.PLUGIN);
      if(generator) {
	plugin.setName(scriptedEvent_name);
	plugin.addParameter(rweFile);
	Iterator iter = ((Collection)parent.getDescendentsOfClass(ImpactAgentComponent.class)).iterator();
	while(iter.hasNext()) {
	  ImpactAgentComponent iac = (ImpactAgentComponent)iter.next();
	  if(!iac.isGenerator()) {
	    plugin.addParameter(iac.getFullName().toString());
	  }
	}
      } else {
	plugin.setName(transducer_name);
	plugin.addParameter(societyFile);
      }
      child.addChild(plugin);

      plugin = new GenericComponentData();
      plugin.setType(ComponentData.PLUGIN);
      plugin.setName(planServer_name);
      child.addChild(plugin);

      return child;
    }
  }

}// ABCImpact
