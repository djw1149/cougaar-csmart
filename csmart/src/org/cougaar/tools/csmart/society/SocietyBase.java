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
package org.cougaar.tools.csmart.society;

import java.io.FileFilter;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.PropertiesListener;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.Property;
import java.util.List;
import java.util.ArrayList;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * SocietyBase.java
 *
 * Implements generic classes required by all societies.
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public abstract class SocietyBase 
  extends ModifiableConfigurableComponent
  implements SocietyComponent, PropertiesListener {

  protected static final String DESCRIPTION_RESOURCE_NAME = "description.html";
  protected static final String BACKUP_DESCRIPTION =
    "Description not available";

  protected boolean isRunning = false;
  protected boolean isSelfTerminating = false;

  protected List nodes = new ArrayList();
  protected List hosts = new ArrayList();

  protected transient Logger log;

  private String assemblyId;

  /**
   * Constructs a <code>SocietyBase</code> object
   * with the given name.
   * @param name Name for this component
   */
  public SocietyBase(String name){
    super(name);
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Returns the name of this Society
   *
   * @return Society Name
   */
  public String getSocietyName() {
    return getShortName();
  }

  /**
   * Returns the agents in this Society
   * @return an array of <code>AgentComponent</code> objects
   */
  public AgentComponent[] getAgents() {
    ArrayList agents = 
      new ArrayList(getDescendentsOfClass(AgentComponent.class));
    return (AgentComponent[])agents.toArray(new AgentComponent[agents.size()]);
  }

  /**
   * Set the assembly id for this Society.
   * @param assemblyId the assembly id for this Society
   */
  public void setAssemblyId(String assemblyId) {
    this.assemblyId = assemblyId;
  }

  /**
   * Get the assembly id for this Society.
   * @return a <code>String</code> which is the assembly id for this Society
   */
  public String getAssemblyId() {
    return this.assemblyId;
  }

  /**
   * Set by the experiment controller to indicate that the
   * society is running.
   * The society is running from the moment that any node
   * is successfully created 
   * (via the app-server's "create" method)
   * until all nodes are terminated (aborted, self terminated, or
   * manually terminated).
   * @param flag indicating whether or not the society is running
   */
   public void setRunning(boolean isRunning) {
    this.isRunning = isRunning;
  }

  /**
   * Returns whether or not the society is running, 
   * i.e. can be dynamically monitored. 
   * Running societies are not editable, but they can be copied,
   * and the copy can be edited. 
   * @return true if society is running and false otherwise
   */
  public boolean isRunning() {
    return isRunning;
  }

  /**
   * Return a file filter which can be used to fetch
   * the metrics files for this experiment. 
   * @return <code>FileFilter</code> to get metrics files for this experiment
   */
  public FileFilter getResultFileFilter() {
    return null;
  }

  /**
   * Return a file filter which can be used to delete
   * the files generated by this experiment.
   * @return <code>FileFilter</code> for cleanup
   */
  public FileFilter getCleanupFileFilter() {
    return null;
  }

  /**
   * Returns whether the society is self terminating or must
   * be manually terminated.
   * Self terminating nodes cause the app-server to send back
   * a "process-destroyed" message when the node terminates.
   * @return true if society is self terminating
   */
  public boolean isSelfTerminating() {
    return this.isSelfTerminating;
  }

  /**
   * Sets if the society is self terminating or not.
   * Self terminating nodes cause the app-server to send back
   * a "process-destroyed" message when the node terminates.
   *
   * @param isSelfTerminating true if society is self terminating
   */
  protected void setSelfTerminating(boolean isSelfTerminating) {
    this.isSelfTerminating = isSelfTerminating;
  }

  /**
   * Returns the description of this society
   *
   * @return an <code>URL</code> value
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);

  }

  /**
   * Modifies any part of the ComponentData Structure.
   *
   * @param data Completed ComponentData structure for the society
   * @return a <code>ComponentData</code> value
   */
  public ComponentData modifyComponentData(ComponentData data) {
    return data;
  }

  /**
   * Returns all the Nodes in this society
   *
   * @return a <code>NodeComponent[]</code> value
   */
  public NodeComponent[] getNodes() {
    return (NodeComponent[]) nodes.toArray(new NodeComponent[nodes.size()]);
  }

  /**
   * Returns all the Hosts in this society
   *
   * @return a <code>HostComponent[]</code> value
   */
  public HostComponent[] getHosts() {
    return (HostComponent[]) hosts.toArray(new HostComponent[hosts.size()]);
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
   * @param e The <code>PropertyEvent</code> describing the removed property.
   */
  public void propertyRemoved(PropertyEvent e) {}

  /**
   * Adds any relevent <code>ComponentData</code> for this component.
   * This method does not modify any existing <code>ComponentData</code>
   *
   * @see ComponentData
   * @param data Pointer to the global <code>ComponentData</code>
   * @return an updated <code>ComponentData</code> object
   */
  public ComponentData addComponentData(ComponentData data) {
    ComponentData[] children = data.getChildren();
    for(int i=0; i < children.length; i++) {
      ComponentData child = children[i];
      // for each child component data, if it's an agent's component data
      if (child.getType() == ComponentData.AGENT) {
        // get all my agent components
	Iterator iter = 
          ((Collection)getDescendentsOfClass(AgentComponent.class)).iterator();
	while(iter.hasNext()) {
	  AgentComponent agent = (AgentComponent)iter.next();
          // if the component data name matches the agent name
	  if (child.getName().equals(agent.getShortName().toString())) {
            // then set me as the owner of the component data
	    child.setOwner(this);
            // and add the component data
	    agent.addComponentData(child);
	  }
	}		
      } else {
	// Process children of component data
	addComponentData(child);
      }      
    }
    return data;
  }

  /**
   * Save this society to the database.
   */
  public void saveToDatabase() {
    System.out.println("SocietyBase: WARNING: save to database not implemented");
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}// SocietyBase
