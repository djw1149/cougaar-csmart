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
package org.cougaar.tools.csmart.society.abc;

import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;

/**
 * Defines an ABCCommunity
 *
 * An ABC Community defaults to 4 agents consisting to 2 customer agents
 * and 2 provider agents.  The customer agents submit tasks to the provider
 * agents.  The provider agents contain local assets to satisify the task
 * requests from the customers.
 *
 * @see ConfigurableComponent
 * @see Serializable
 */

public class ABCCommunity
    extends ConfigurableComponent
    implements Serializable
{
  /** Property Definitions for Demand **/
  public static final String PROP_DEMAND = "Demand";
  public static final Integer PROP_DEMAND_DFLT = new Integer(5);
  public static final String PROP_DEMAND_DESC = "Amount of Demand for this community";

  /** Property Definitions for Production **/
  public static final String PROP_PRODUCTION = "Production";
  public static final Integer PROP_PRODUCTION_DFLT = new Integer(10);
  public static final String PROP_PRODUCTION_DESC = "Production Capacity for this community";

  /** Property Definitions for Latitude **/
  public static final String PROP_LATITUDE = "Latitude";
  public static final Float  PROP_LATITUDE_DFLT = new Float(42.22);
  public static final String PROP_LATITUDE_DESC = "Latitude for the center of the community";

  /** Property Definitions for Longitude **/
  public static final String PROP_LONGITUDE = "Longitude";
  public static final Float PROP_LONGITUDE_DFLT = new Float(71.2);
  public static final String PROP_LONGITUDE_DESC = "Longitude for the center of the community";

  /** Property Definitions for Level **/
  public static final String PROP_LEVEL = "Level";
  public static final String PROP_LEVEL_DESC = "Which Level this community is on";

  /** Properties used here but defined elsewhere **/
  public static final String PROP_TASKVERB = ABCTask.PROP_TASKVERB;
  public static final String PROP_TASKVERB_DESC = ABCTask.PROP_TASKVERB_DESC;

  public static final String PROP_ROLES = ABCLocalAsset.PROP_ROLES;
  public static final String PROP_ROLES_DESC = ABCLocalAsset.PROP_ROLES_DESC;

  public static final String PROP_DIRECTION = ABCAgent.PROP_DIRECTION;
  public static final String PROP_DIRECTION_DESC = ABCAgent.PROP_DIRECTION_DESC;

  public static final String PROP_DISTANCE = ABCAgent.PROP_DISTANCE;
  public static final String PROP_DISTANCE_DESC= ABCAgent.PROP_DISTANCE_DESC;

  public static final String PROP_STARTTIME = ABCAgent.PROP_STARTTIME;
  public static final String PROP_STOPTIME = ABCAgent.PROP_STOPTIME;

  /** Name of the Customer Agent **/
  public static final String DEFAULT_CUSTOMER_NAME = "Customer";
  
  /** Name of the Provider Agent **/
  public static final String DEFAULT_PROVIDER_NAME = "Provider";
  
  /** Name of the Local Asset **/
  public static final String DEFAULT_ASSETNAME = "ClassOneDepot";

  /** Agent specific defaults **/

  /** Customer 1 **/

  /** Distance from Center of Community **/
  public static final int C1_DFLT_DISTANCE  = 6400;
  /** Direction from Center of Community **/
  public static final int C1_DFLT_DIRECTION = 90;
  /** Factor **/
  public static final int C1_DFLT_FACTOR    = 550;
  /** Duration of Task **/
  public static final int C1_DFLT_DURATION  = 12000;
  /** Vitalness of Task **/
  public static final double C1_DFLT_VITAL  = 0.6;
  /** Chaos to apply to task Rate **/
  public static final int C1_DFLT_CHAOS     = 0;
  

  /** Customer 2 **/

  /** Distance from Center of Community **/
  public static final int C2_DFLT_DISTANCE  = 3640;
  /** Direction from Center of Community **/
  public static final int C2_DFLT_DIRECTION = 75;
  /** Factor **/
  public static final int C2_DFLT_FACTOR    = 600;
  /** Duration of Task **/
  public static final int C2_DFLT_DURATION  = 12000;
  /** Vitalness of Task **/
  public static final double C2_DFLT_VITAL  = 0.5;
  /** Chaos to apply to task Rate **/
  public static final int C2_DFLT_CHAOS     = 0;

  /** Provider 1 **/

  /** Distance from Center of Community **/
  public static final int P1_DFLT_DISTANCE   = 3200;
  /** Direction from Center of Community **/
  public static final int P1_DFLT_DIRECTION  = 0;
  /** Amount to deplete inventory **/
  public static final long P1_DFLT_DEPLETE   = 1200;
  /** Inventory Level Chaos **/
  public static final long P1_DFLT_INVCHAOS  = 0;
  /** Completion Time Chaos **/
  public static final long P1_DFLT_TIMECHAOS = 0;
  /** Average time to satisify request **/
  public static final long P1_DFLT_AVGTIME   = 12000; 

  /** Provider 2 **/

  /** Distance from Center of Community **/
  public static final int P2_DFLT_DISTANCE   = 1600;
  /** Direction from Center of Community **/
  public static final int P2_DFLT_DIRECTION  = 180;
  /** Amount to deplete inventory **/
  public static final long P2_DFLT_DEPLETE   = 900;
  /** Inventory Level Chaos **/
  public static final long P2_DFLT_INVCHAOS  = 0;
  /** Completion Time Chaos **/
  public static final long P2_DFLT_TIMECHAOS = 0;
  /** Average time to satisify request **/
  public static final long P2_DFLT_AVGTIME   = 12000; 

  private Property propDemand;
  private Property propProduction;
  private Property propLatitude;
  private Property propLongitude;
  private Property propLevel;

  private int level;
  private int community;

  private Property addPropertyAlias(ABCAgent a, Property prop, String name) {
    Property childProp = a.addAliasProperty(prop, name);
    setPropertyVisible(childProp, false);
    return childProp;
  }

  public ABCCommunity(int level, int comm) {
    super("Community" + comm);
    this.level = level;
    this.community = comm;
  }

  /**
   * Initializes all local properties
   */
  public void initProperties() {
    propLevel = addProperty(PROP_LEVEL, new Integer(level), Integer.class);
    propLevel.setToolTip(PROP_LEVEL_DESC);
    setPropertyVisible(propLevel, false);

    propDemand = addProperty(PROP_DEMAND, PROP_DEMAND_DFLT);
    propDemand.setToolTip(PROP_DEMAND_DESC);
    propDemand.setAllowedValues(Collections.singleton(new IntegerRange(0, Integer.MAX_VALUE)));

    propProduction = addProperty(PROP_PRODUCTION, PROP_PRODUCTION_DFLT);
    propProduction.setToolTip(PROP_PRODUCTION_DESC);
    propProduction.setAllowedValues(Collections.singleton(new IntegerRange(0, Integer.MAX_VALUE)));

    propLatitude = addProperty(PROP_LATITUDE, PROP_LATITUDE_DFLT);
    propLatitude.setToolTip(PROP_LATITUDE_DESC);
    propLatitude.setAllowedValues(Collections.singleton(new FloatRange(-90.0F, 90.0F)));

    propLongitude = addProperty(PROP_LONGITUDE, PROP_LONGITUDE_DFLT);
    propLongitude.setToolTip(PROP_LONGITUDE_DESC);
    propLongitude.setAllowedValues(Collections.singleton(new FloatRange(-180.0F, 180.0F)));
  }

  /**
   * Indicates that a new property was added to the community
   * 
   * @param PropertyEvent Event for new property
   */
  public void propertyAdded(PropertyEvent e) {
    Property addedProperty = e.getProperty();
    Property myProperty = getProperty(addedProperty.getName().last().toString());
    if (myProperty != null) {
      setPropertyVisible(addedProperty, false);
    }
  }
  
  /** 
   * Returns the level for this community.
   *
   * @return Level number
   */
  public int getLevel() {
    return ((Integer) propLevel.getValue()).intValue();
  }

  /**
   * Returns the community number for this community
   *
   * @return community number
   */
  public int getCommunity() {
    return this.community;
  }

  /**
   * Adds all our default agents for a community that contains
   * Four Agents: 2 <code>Customer</code> Agents and 2 <code>Provider</code> Agents.
   * All Tasks are produced by <code>Customer</code> Agents and all
   * assets are owned by <code>Provider</code> agents.
   */
  public void addAgents() {

    String role = "SubsistenceProvider";

    String[] roles = new String[1];
    roles[0] = role;

    String c1 = addCustomerAgent(DEFAULT_CUSTOMER_NAME, 1, C1_DFLT_FACTOR, 
				 C1_DFLT_DURATION, C1_DFLT_VITAL, C1_DFLT_CHAOS,
				 C1_DFLT_DISTANCE, C1_DFLT_DIRECTION, roles);
    
    String c2 = addCustomerAgent(DEFAULT_CUSTOMER_NAME, 2, C2_DFLT_FACTOR,
				 C2_DFLT_DURATION, C2_DFLT_VITAL, C2_DFLT_CHAOS,
				 C2_DFLT_DISTANCE, C2_DFLT_DIRECTION, roles);

    String[] supplies = new String[2];
    supplies[0] = c1;
    supplies[1] = c2;
    String p1 = addProviderAgent(DEFAULT_PROVIDER_NAME, 1, supplies, P1_DFLT_DISTANCE,
				 P1_DFLT_DIRECTION, DEFAULT_ASSETNAME, roles, P1_DFLT_DEPLETE,
				 P1_DFLT_INVCHAOS, P1_DFLT_TIMECHAOS, P1_DFLT_AVGTIME, roles);
    supplies = new String[1];
    supplies[0] = p1;
    
    addProviderAgent(DEFAULT_PROVIDER_NAME, 2, supplies, P2_DFLT_DISTANCE, 
		     P2_DFLT_DIRECTION, DEFAULT_ASSETNAME, roles, P2_DFLT_DEPLETE, 
		     P2_DFLT_INVCHAOS, P2_DFLT_TIMECHAOS, P2_DFLT_AVGTIME, roles);
  }

  /**
   * Returns all External Providers to this community.
   * Each community can provide local assets to another
   * community.  This link is usually through Provider 2.
   * This method returns all agents from other communities that
   * can talk to this communities provider 2.
   *
   * @return Agent Names
   */
  public String getExternalProviderName() {
    String retVal = null;

    for(int i =0; i < getChildCount(); i ++) {
      ABCAgent agent = (ABCAgent)getChild(i);
      String name = agent.getFullName().get(2).toString();
      if(name.equals(DEFAULT_PROVIDER_NAME + "2")) {
	retVal=  agent.getFullName().toString();
	break;
      }
    }
    return retVal;
  }

  /**
   * Adds a new Provider agent to this community.
   *
   * @param name Name of the Provider Agent
   * @param index Unique index for this agent
   * @param supplies String array of all agents this providers supplies assets for
   * @param distance Distance of this agent from the center of the community
   * @param direction Direction of this agent from the center of the community
   * @param assetName Name of the Local Asset
   * @param roles All Roles that the local asset can satisify
   * @param deplete used with community production to calc inventory delpetion rate
   * @param invChaos How much chaos to apply to the inventory level
   * @param timeChaos How much chaos to apply to completion time
   * @param avgTime Average completion time
   * @param allocRoles All roles for the local allocation
   * @return Agents name
   */
  private String addProviderAgent(String name, int index, String[] supplies, int distance,
				  int direction, String assetName, String[] roles,
				  long deplete, long invChaos, long timeChaos, long avgTime,
				  String[] allocRoles) {

    Property p = null;

    ABCAgent agent = new ABCAgent(getFullName().toString(), name, index);
    addChild(agent);
    ABCLocalAsset la = new ABCLocalAsset("LocalAsset");
    la.initProperties();
    la.getProperty(ABCLocalAsset.PROP_NAME).setValue(assetName);
    la.getProperty(ABCLocalAsset.PROP_ROLES).setValue(roles);
    int production = ((Integer)getProperty(PROP_PRODUCTION).getValue()).intValue();
    long newDeplete = ((deplete / production) > 100) ? 100 : (deplete / production);
    la.getProperty(ABCLocalAsset.PROP_DECAMOUNT).setValue(new Long(newDeplete));
    la.getProperty(ABCLocalAsset.PROP_AVGTIME).setValue(new Long(avgTime));
    la.getProperty(ABCLocalAsset.PROP_INVDEV).setValue(new Long(invChaos));
    la.getProperty(ABCLocalAsset.PROP_TIMEDEV).setValue(new Long(timeChaos));
    agent.addChild(la);

    // Create Allocations
    ABCAllocation alloc = new ABCAllocation();
    alloc.initProperties();
    String[] tasks = (String[])getProperty(PROP_TASKVERB).getValue();

    for(int i=0; i < tasks.length; i++) {
      ABCAllocationRule rule = new ABCAllocationRule(tasks[i]);
      rule.initProperties();
      Property verb = rule.getProperty(PROP_TASKVERB);
      verb.setValue(tasks[i]);
      verb.setToolTip(PROP_TASKVERB_DESC);
      Property role = rule.getProperty(PROP_ROLES);
      role.setValue(allocRoles);
      role.setToolTip(PROP_ROLES_DESC);
      alloc.addChild(rule);
    }
    agent.addChild(alloc);

    agent.initProperties();
    agent.getProperty(PROP_DIRECTION).setValue(new Integer(direction));
    agent.getProperty(PROP_DISTANCE).setValue(new Integer(distance));

    // Create Supplies
    if(supplies != null) {
      Property supp = agent.getProperty(ABCAgent.PROP_SUPPLIES);
      supp.setValue(supplies);
    }

    return agent.getFullName().toString();
  }



  /**
   * Adds a new Customer agent to the society
   *
   * @param name Name of the new agent
   * @param index Index of the new agent
   * @param factor used with community demand to generate task rate
   * @param duration Length of time for task completion
   * @param vital How vital each task is for overall happiness
   * @param chaos Amount of chaos to apply to task distribution
   * @param distance How far this agent is from the center of the community
   * @param direction Which direction this agent is from the center of community
   * @param roles All Roles for the customers tasks
   */
  private String addCustomerAgent(String name, int index, int factor, 
				  int duration, double vital, int chaos,
				  int distance, int direction, String[] roles) {

    Property p = null;

    ABCAgent agent = new ABCAgent(getFullName().toString(), name, index);
    addChild(agent);

    ABCTaskFile taskFile = new ABCTaskFile("Tasks");
    taskFile.initProperties();
     String[] tasks = (String[])getProperty(PROP_TASKVERB).getValue();
    //String taskverb = (String)getProperty(PROP_TASKVERB).getValue();
    int count = 0;
    for(int i=0; i < tasks.length; i++) {
      String tt = tasks[i];
      ABCTask task = new ABCTask(tt);
      task.initProperties();
      task.getProperty(PROP_TASKVERB).setValue(tt);
      int demand = ((Integer)getProperty(PROP_DEMAND).getValue()).intValue();
      task.getProperty(ABCTask.PROP_RATE).setValue(new Long(factor * demand));
      task.getProperty(ABCTask.PROP_VITAL).setValue(new Double(vital));
      task.getProperty(ABCTask.PROP_DURATION).setValue(new Long(duration));
      task.getProperty(ABCTask.PROP_CHAOS).setValue(new Integer(chaos));
      taskFile.addChild(task);
    }
    agent.addChild(taskFile);

    // Create Allocations
    ABCAllocation alloc = new ABCAllocation();
    alloc.initProperties();
    for(int i=0; i < tasks.length; i++) {
      ABCAllocationRule rule = new ABCAllocationRule(tasks[i]);
      rule.initProperties();
      Property type = rule.getProperty(PROP_TASKVERB);
      type.setValue(tasks[i]);
      type.setToolTip(PROP_TASKVERB_DESC);
      Property role = rule.getProperty(PROP_ROLES);
      role.setValue(roles);
      role.setToolTip(PROP_ROLES_DESC);
      alloc.addChild(rule);
    }
    agent.addChild(alloc);

    addPropertyAlias(agent, getProperty(PROP_STARTTIME), PROP_STARTTIME);
    addPropertyAlias(agent, getProperty(PROP_STOPTIME), PROP_STOPTIME);

    agent.initProperties();
    agent.getProperty(PROP_DISTANCE).setValue(new Integer(distance));
    agent.getProperty(PROP_DIRECTION).setValue(new Integer(direction));

    return agent.getFullName().toString();
  }



  /**
   * Adds an alias property
   */
  public Property addAliasProperty(Property prop, String name) {
    return addProperty(new PropertyAlias(this, name, prop));
  }

  /**
   * Generates all ini files for agents
   *
   * @param File Directory to write files to
   */
  public void generateIniFiles(File configDir) {
  }
}