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

import junit.framework.*;
import java.util.Iterator;
import java.util.Collection;

import org.cougaar.tools.csmart.core.data.ComponentData;
import org.cougaar.tools.csmart.core.data.LeafComponentData;
import org.cougaar.tools.csmart.core.data.AgentComponentData;
import org.cougaar.tools.csmart.core.data.RelationshipData;
import org.cougaar.tools.csmart.core.data.GenericComponentData;

import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;

public class ABCAgentTest extends TestCase {
  
  private GenericComponent gc = null;
  private ABCAgent agent = null;

  public ABCAgentTest(String name) {
    super(name);
  }

  protected void setUp() {
    gc = new GenericComponent();
    gc.initProperties();
    agent = (ABCAgent)gc.getChild(0);
  }

  public void testNothing() {
    assertEquals(1,1);
  }

//   public void testComponentData() {
//     ComponentData data = (ComponentData)agent.addComponentData(new GenericComponentData());

// //     assertEquals("Test Type", ComponentData.AGENT, data.getType());
// //     assertEquals("Test Name", agent.getFullName().toString(), data.getName());
// //     assertEquals("Test Class", "org.cougaar.core.agent.ClusterImpl", data.getClassName());
// //     assertEquals("Test Number of PlugIns", 6, data.childCount());
//   }

//   public void testComponentDataChildren() {
//     ComponentData data = (ComponentData)agent.addComponentData(new GenericComponentData());

//     Object[] children = data.getChildren();

//     ComponentData gc = (ComponentData)children[0];

//     assertEquals("Test AssetData", "org.cougaar.planning.plugin.AssetDataPlugIn", gc.getName());

//     gc = (ComponentData)children[1];
//     assertEquals("Test AssetReport", "org.cougaar.planning.plugin.AssetReportPlugIn", gc.getName());

//     gc = (ComponentData)children[2];
//     assertEquals("Test CustomerPlugIn", "org.cougaar.tools.csmart.plugin.CustomerPlugIn" , gc.getName());

//     gc = (ComponentData)children[3];
//     assertEquals("Test Metrics", "org.cougaar.tools.csmart.plugin.MetricsPlugin", gc.getName());

//     gc = (ComponentData)children[4];
//     assertEquals("Test Allocator", "org.cougaar.tools.csmart.plugin.AllocatorPlugIn", gc.getName());

//     gc = (ComponentData)children[5];
//     assertEquals("Test PlanServer", "org.cougaar.lib.planserver.PlanServerPlugIn", gc.getName());
//   }


//   public void testComponentDataLeaves() {
//     AgentComponentData data = (AgentComponentData)agent.addComponentData(new GenericComponentData());

//     LeafComponentData[] leaves = data.getLeafComponents();
//     LeafComponentData leaf = leaves[0];
//     assertEquals("Test Task File Name", "Generic.Type0.Tasks.dat", leaf.getName());
//     assertEquals("Test Task File Type", LeafComponentData.FILE, leaf.getType());
    
//     StringBuffer t1 = new StringBuffer();
//     t1.append("# <WorldState>, <TaskVerb>, <Rate>, <Chaos>, <Vitality>, <Duration> \n");
//     t1.append("PEACE, verb, 1, 4, 2.0, 3\n");
//     t1.append("PEACE, verb2, 2, 5, 3.0, 4\n");
//     assertEquals("Test Task File Value", t1.toString(), ((StringBuffer)leaf.getValue()).toString());

//     leaf = leaves[1];
//     assertEquals("Test Asset File Name", "Generic.Type0.LocalAssets.dat", leaf.getName());
//     assertEquals("Test Asset File Type", LeafComponentData.FILE, leaf.getType());
//     t1 = new StringBuffer();
//     t1.append("# [name], [dec amt], [avg time], [inv_stdev], [time_stdev], [roles]\n");
//     t1.append("Asset, 1, 2, 3, 4, Role1, Role2\n");
//     assertEquals("Test Asset File Value", t1.toString(), ((StringBuffer)leaf.getValue()).toString());

//     leaf = leaves[2];
//     assertEquals("Test Allocation File Name", "Generic.Type0.Allocations.dat", leaf.getName());
//     assertEquals("Test Allocation File Type", LeafComponentData.FILE, leaf.getType());
//     t1 = new StringBuffer();
//     t1.append("# [config, <fSuccess>, <tResp> (, <tAlloc>, <tTrans>, <tTry>)]\n");
//     t1.append("# [[rule, ] <task>, <role> (, <role>)*]\n");
//     t1.append("# <more \"rule\" lines as necessary>\n");
//     t1.append("config, 0.5, 50, 60, 150, 1100\n");
//     t1.append("rule, Task, Role1, Role2\n");
//     assertEquals("Test Allocation File Value", t1.toString(), ((StringBuffer)leaf.getValue()).toString());
//   }

//   public void testComponentDataTimePhased() {
//     String[] supplies = {"Supplies.Agent"};
//     agent.getProperty(ABCAgent.PROP_SUPPLIES).setValue(supplies);
//     agent.getProperty(ABCAgent.PROP_INITIALIZER).setValue("Initializer");

//     ComponentData data = (ComponentData)agent.addComponentData(new GenericComponentData());
    
//     RelationshipData[] tpd = data.RelationShipData();

//     if(tpd[1] instanceof RelationshipData) {
//       RelationshipData rel = (RelationshipData)tpd[1];
//       assertEquals("Test Relationship Role", "Role1", rel.getRole());
//       assertEquals("Test Item", "Supplies.Agent", rel.getItem());
//       assertEquals("Test Type", "Agent" ,rel.getType());
//       assertEquals("Test Cluster", "Supplies.Agent", rel.getCluster());
//     } else {
//       fail("Expected RelationshipData");
//     }

//     if(tpd[2] instanceof RelationshipData) {
//       RelationshipData rel = (RelationshipData)tpd[2];
//       assertEquals("Test Relationship Role", "Role2", rel.getRole());
//       assertEquals("Test Item", "Supplies.Agent", rel.getItem());
//       assertEquals("Test Type", "Agent" ,rel.getType());
//       assertEquals("Test Cluster", "Supplies.Agent", rel.getCluster());
//     } else {
//       fail("Expected RelationshipData");
//     }

//   }

  public static junit.framework.Test suite() {
    return new TestSuite(ABCAgentTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  // Inner class needed for creating the test env.

  class GenericComponent
    extends ConfigurableComponent
  {
    /** Properties defined in other files **/
    public static final String PROP_TASKVERB = ABCTask.PROP_TASKVERB;
    public static final String PROP_STARTTIME = ABCAgent.PROP_STARTTIME;
    public static final String PROP_STOPTIME = ABCAgent.PROP_STOPTIME;

    private Property propStartTime;
    private Property propStopTime;
    private Property propTaskVerb;

    public GenericComponent() {
      super("Generic");
    }

    public void initProperties() {
      propStartTime = addProperty(PROP_STARTTIME, new Integer(0));
      propStopTime = addProperty(PROP_STOPTIME, new Integer(10));
      propTaskVerb = addProperty(PROP_TASKVERB, "Test");      

      ABCAgent ag = new ABCAgent("Community", "Type", 0);
      addChild(ag);      
      addPropertyAlias(ag, propTaskVerb, PROP_TASKVERB);
      addPropertyAlias(ag, propStartTime, PROP_STARTTIME);
      addPropertyAlias(ag, propStopTime, PROP_STOPTIME);

      ABCTaskFile taskfile = new ABCTaskFile("Tasks");
      taskfile.initProperties();
      ABCTask task = new ABCTask("Task1");
      task.initProperties();
      task.getProperty(ABCTask.PROP_TASKVERB).setValue("verb");
      task.getProperty(ABCTask.PROP_RATE).setValue(new Long(1));
      task.getProperty(ABCTask.PROP_VITAL).setValue(new Double(2.0));
      task.getProperty(ABCTask.PROP_DURATION).setValue(new Long(3));
      task.getProperty(ABCTask.PROP_CHAOS).setValue(new Integer(4));
      taskfile.addChild(task);

      task = new ABCTask("Task2");
      task.initProperties();
      task.getProperty(ABCTask.PROP_TASKVERB).setValue("verb2");
      task.getProperty(ABCTask.PROP_RATE).setValue(new Long(2));
      task.getProperty(ABCTask.PROP_VITAL).setValue(new Double(3.0));
      task.getProperty(ABCTask.PROP_DURATION).setValue(new Long(4));
      task.getProperty(ABCTask.PROP_CHAOS).setValue(new Integer(5));
      taskfile.addChild(task);

      ag.addChild(taskfile);

      ABCLocalAsset asset = new ABCLocalAsset();
      asset.initProperties();
      asset.getProperty(ABCLocalAsset.PROP_NAME).setValue("Asset");
      String[] roles = {"Role1", "Role2"};
      asset.getProperty(ABCLocalAsset.PROP_ROLES).setValue(roles);
      asset.getProperty(ABCLocalAsset.PROP_DECAMOUNT).setValue(new Long(1));
      asset.getProperty(ABCLocalAsset.PROP_AVGTIME).setValue(new Long(2));
      asset.getProperty(ABCLocalAsset.PROP_INVDEV).setValue(new Long(3));
      asset.getProperty(ABCLocalAsset.PROP_TIMEDEV).setValue(new Long(4));
      ag.addChild(asset);
      Iterator iter = ((Collection)getDescendentsOfClass(ABCLocalAsset.class)).iterator();
      while(iter.hasNext()) {
	ABCLocalAsset ala = (ABCLocalAsset)iter.next();
	Property p = ala.addProperty(ABCLocalAsset.PROP_ASSETFILENAME, ala.getFullName().toString() + ".dat");
      }

      ABCAllocation alloc = new ABCAllocation();
      alloc.initProperties();
      ABCAllocationRule rule = new ABCAllocationRule("Task");
      rule.initProperties();
      rule.getProperty(ABCAllocationRule.PROP_TASKVERB).setValue("Task");
      rule.getProperty(ABCAllocationRule.PROP_ROLES).setValue(roles);
      alloc.addChild(rule);
      ag.addChild(alloc);

      ag.initProperties();

    }   

    private Property addPropertyAlias(ABCAgent c, Property prop, String name) {
      Property childProp = c.addAliasProperty(prop, name);
      setPropertyVisible(childProp, false);
      return childProp;
    }
 
  }

}