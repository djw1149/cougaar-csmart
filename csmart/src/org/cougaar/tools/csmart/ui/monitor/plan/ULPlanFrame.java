/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.monitor.plan;

import att.grappa.*;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTFrame;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTGraph;
import org.cougaar.tools.csmart.ui.monitor.generic.LegendComboBoxModel;
import org.cougaar.tools.csmart.ui.monitor.generic.LegendRenderer;
import org.cougaar.tools.csmart.ui.monitor.generic.UIProperties;
import org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

public class ULPlanFrame extends CSMARTFrame {
  private static final String THREAD_UP_MENU_ITEM = "Ancestor Thread";
  private static final String THREAD_DOWN_MENU_ITEM = "Descendant Thread";
  private static final String LEGEND_MENU_ITEM = "Legend";
  private static final String PARENTS_MENU_ITEM = "Parents";
  private static final String CHILDREN_MENU_ITEM = "Children";
  private static final String COLOR_PARENTS = "colorParents";
  private static final String COLOR_CHILDREN = "colorChildren";
  // MEK
  private static final String FIND_PLAN_OBJECTS_MENU_ITEM = "Find Plan Objects";
  private Hashtable communityToAgents = null;
  private ULPlanFinder finder;
  // MEK
  private JDialog legendDialog;
  private ULPlanFilter filter;
  private static UIProperties properties = null;
  private JMenuItem threadUpMenuItem;
  private JMenuItem threadDownMenuItem;

  private transient Logger log;

  public ULPlanFrame(String title, CSMARTGraph graph, ULPlanFilter filter) {
    super(title, graph);
    setProperties();
    this.filter = filter;
    this.graph = graph;
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void customize() {
    insertMenuItem(CSMARTFrame.VIEW_MENU, LEGEND_MENU_ITEM, 2, this);
    threadUpMenuItem = 
      insertMenuItem(CSMARTFrame.VIEW_MENU, THREAD_UP_MENU_ITEM, 3, this);
    threadUpMenuItem.setEnabled(false);
    threadDownMenuItem =
      insertMenuItem(CSMARTFrame.VIEW_MENU, THREAD_DOWN_MENU_ITEM, 4, this);
    threadDownMenuItem.setEnabled(false);
    insertMenuItem(CSMARTFrame.SHOW_MENU, PARENTS_MENU_ITEM, 0, this);
    insertMenuItem(CSMARTFrame.SHOW_MENU, CHILDREN_MENU_ITEM, 1, this);
    insertMenuItem(CSMARTFrame.SELECT_MENU, FIND_PLAN_OBJECTS_MENU_ITEM, 1, this);
  }

  private void setProperties() {
    if (properties != null)
      return; // only need to do this once
    properties = CSMARTGraph.getProperties();
    GrappaColor.addColor(COLOR_PARENTS, properties.getColorCauses());
    GrappaColor.addColor(COLOR_CHILDREN, properties.getColorEffects());
  }

  /**
   * Enable/disable menu items that are used when the event graph
   * has selected nodes.
   * Overrides CSMARTFrame to enable the show menu only
   * when a task node is selected.
   * @param enable true to enable menu items; false to disable
   */

  protected void enableSelectedMenus(boolean enable) {
    super.enableSelectedMenus(enable);
    boolean haveSelectedNode = false;
    if (enable) {
      Vector elements = graph.getSelectedElements();
      if (elements == null)
	return;
      for (int i = 0; i < elements.size(); i++) {
	Element element = (Element)elements.elementAt(i);
	if (element instanceof Node) {
	  haveSelectedNode = true;
	  String objectType =
	    (String)element.getAttributeValue(PropertyNames.OBJECT_TYPE);
	  if (objectType != null && 
	      objectType.equals(PropertyNames.TASK_OBJECT)) {
	    threadUpMenuItem.setEnabled(true);
	    threadDownMenuItem.setEnabled(true);
	    return; // a task element was selected, leave show menu enabled
	  }
	}
      }
      if (threadUpMenuItem != null) { // may not have been initted
	threadUpMenuItem.setEnabled(haveSelectedNode);
	threadDownMenuItem.setEnabled(haveSelectedNode);
      }
      showMenu.setEnabled(false);
    } else {
      // disabling menu items
      if (threadUpMenuItem != null) { // may not have been initted
	threadUpMenuItem.setEnabled(false);
	threadDownMenuItem.setEnabled(false);
      }
    }
  }

  public TableModel getAttributeTableModel(Node node) {
    return new ULPlanTableModel(node);
  }

  /**
   * Process menu commands that are specific to plan objects,
   * pass other menu commands to CSMARTFrame.
   * @param evt the event received
   */

  public void actionPerformed(ActionEvent evt) {
    String command = ((JMenuItem)evt.getSource()).getText();

    if (command.equals(NEW_WITH_SELECTION_MENU_ITEM)) {
      // check that at least one node is selected, else do nothing
      if (!graph.isNodeSelected())
	return;
      CSMARTGraph newGraph = graph.newGraphFromSelection();
      new ULPlanFrame(NamedFrame.PLAN, newGraph, filter.copy());
      return;
    }

    if (command.equals(LEGEND_MENU_ITEM)) {
      createLegend();
      return;
    }

    if (command.equals(THREAD_UP_MENU_ITEM) ||
	 command.equals(THREAD_DOWN_MENU_ITEM)) {
      // will gather these parameters:
      String UID;
      String agentName;
      int limit = 0;
      boolean isDown;

      // get the parameter values
      Vector selected = graph.getSelectedElements();
      if (selected == null || selected.size() == 0) 
        return;
      Element element = (Element)selected.elementAt(0);
      if (!(element.isNode()))
        return;
      UID = element.getName();
      if (UID == null)
        return;
      agentName = 
        (String)element.getAttributeValue(PropertyNames.AGENT_ATTR);
      if (agentName == null)
        return;
      isDown = (command.equals(THREAD_DOWN_MENU_ITEM));

      // Get the thread size limit (non-0)
      while (limit == 0) {
	// popup for limit
	String sLimit = JOptionPane.showInputDialog("Trace limit (e.g. 200 objects, negative for all): ", "200");
	
	if (sLimit == null || sLimit.trim().equals("") || sLimit.trim().equals("0")) {
	  if (log.isDebugEnabled())
	    log.debug("User canceled trace by entering limit of " + sLimit);
	  return;
	}
	
	try {
	  limit = Integer.parseInt(sLimit);
	} catch (NumberFormatException nfe) {
	  if(log.isErrorEnabled()) {
	    log.error("Illegal limit size number: "+sLimit);
	  }
	  // Pop up message, re-query
	  JOptionPane.showMessageDialog(null, "Invalid limit. Must be valid number: " + sLimit, "Invalid limit", JOptionPane.WARNING_MESSAGE);
	}
      } // end of while loop

      // create the thread graph
      CSMARTUL.makeThreadGraph(UID, agentName, isDown, limit);
      return;
    }

    // filter menu item is declared in base CSMARTFrame class
    // by copying the filter here, if the user
    // creates a new graph from the filter, then the new graph
    // correctly gets the new filter, and this graph retains
    // the unaltered filter
    if (command.equals(FILTER_MENU_ITEM)) {
      ULPlanFilter filterCopy = filter.copy();
      filterCopy.postFilter(this, graph);
      return;
    }

    if (command.equals(PARENTS_MENU_ITEM)) {
      Vector elements = graph.getSelectedElements();
      if (elements == null)
	return;
      graph.resetColors();
      for (int i = 0; i < elements.size(); i++) {
	Element element = (Element)elements.elementAt(i);
	if (element instanceof Node)
	  graph.markTails((Node)element, COLOR_PARENTS);
      }
      return;
    }

    if (command.equals(CHILDREN_MENU_ITEM)) {
      Vector elements = graph.getSelectedElements();
      if (elements == null)
	return;
      graph.resetColors();
      for (int i = 0; i < elements.size(); i++) {
	Element element = (Element)elements.elementAt(i);
	if (element instanceof Node)
	  graph.markHeads((Node)element, COLOR_CHILDREN);
      }
      return;
    }

    if (command.equals(FIND_PLAN_OBJECTS_MENU_ITEM)) {
      // create hashtable mapping community to agents for the finder
      if (communityToAgents == null) {
	communityToAgents = new Hashtable();
	Vector nodes = graph.vectorOfElements(GrappaConstants.NODE);
	for (int i = 0; i < nodes.size(); i++) {
	  Node node = (Node)nodes.get(i);
	  String agent = 
	    (String)node.getAttributeValue(PropertyNames.AGENT_ATTR);
	  String community =
	    (String)node.getAttributeValue(PropertyNames.PLAN_OBJECT_COMMUNITY_NAME);
	  if (agent != null && community != null) {
	    Vector agents = (Vector)communityToAgents.get(community);
	    if (agents == null) {
	      agents = new Vector();
	      agents.addElement(agent);
	      communityToAgents.put(community, agents);
	    } else if (!agents.contains(agent))
	      agents.addElement(agent);
	  }
	}
      }
      if (finder == null)
        finder = new ULPlanFinder(this, graph, communityToAgents);
      finder.displayFinder();
      return;
    }

    super.actionPerformed(evt); // let CSMARTFrame handle the rest
  }

  /**
   * Create a legend; which is a non-modal dialog.
   * Called in response to selecting "display legend" from a menu.
   */

  private void createLegend() {
    if (legendDialog != null) {
      legendDialog.setVisible(true);
      return;
    }
    JPanel legendPanel = new JPanel(new BorderLayout());
    JPanel agentPanel = new JPanel();
    agentPanel.setBorder(new TitledBorder("Agents"));
    agentPanel.setLayout(new BoxLayout(agentPanel, BoxLayout.Y_AXIS));

    JComboBox cb = 
      new JComboBox(new LegendComboBoxModel(graph.getNodeColors()));
    cb.setRenderer(new LegendRenderer());
    cb.setSelectedIndex(0);
    agentPanel.add(cb);

    JPanel planPanel = new JPanel(new BorderLayout());
    planPanel.setBorder(new TitledBorder("Plan Objects"));
    planPanel.add(getNodeShapeLegend(), BorderLayout.CENTER);
    legendPanel.add(agentPanel, BorderLayout.NORTH);
    legendPanel.add(planPanel, BorderLayout.CENTER);
    legendDialog = new JDialog(this, "Legend", false);
    legendDialog.getContentPane().setLayout(new BorderLayout());
    legendDialog.getContentPane().add(legendPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    JButton OKButton = new JButton("OK");
    OKButton.setFocusPainted(false);
    OKButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	legendDialog.setVisible(false);
      }
    });
    buttonPanel.add(OKButton);
    legendDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    //    legendDialog.setSize(400, 700);
    legendDialog.setSize(300, 400);
    legendDialog.show();
  }

  /**
   * Get node shapes for plan objects for legend.
   */

  private JPanel getNodeShapeLegend() {
    CSMARTGraph legendGraph = new CSMARTGraph(); // just to make nodes
    legendGraph.setAttribute(GrappaConstants.RANKDIR_ATTR, "TB");
    legendGraph.setNodeAttribute(GrappaConstants.COLOR_ATTR, "goldenrod2");
    GrappaStyle gs = 
      new GrappaStyle(GrappaConstants.NODE,
		      "filled(true),line_color(goldenrod2)");
    legendGraph.setNodeAttribute(GrappaConstants.STYLE_ATTR, gs);
    Subgraph subg1 = new Subgraph(legendGraph);
    Subgraph subg2 = new Subgraph(legendGraph);

    GrappaPanel legendPanel = new GrappaPanel(legendGraph);
    legendPanel.setToolTipText("");
    GrappaColor.addColor("myGray", legendPanel.getBackground());
    legendPanel.setMinimumSize(new Dimension(370, 520));
    legendPanel.setMaximumSize(new Dimension(370, 520));
    legendPanel.setPreferredSize(new Dimension(370, 520));
    legendPanel.setScaleToFit(true);

    // column 1
    Node taskNode = new Node(legendGraph);
    taskNode.setAttribute(GrappaConstants.LABEL_ATTR, "Task");
    taskNode.setAttribute(GrappaConstants.SHAPE_ATTR, "ellipse");
    taskNode.setAttribute(GrappaConstants.WIDTH_ATTR, "1.0");

    Node allocationNode = new Node(legendGraph);
    allocationNode.setAttribute(GrappaConstants.LABEL_ATTR, "Allocation");
    allocationNode.setAttribute(GrappaConstants.SHAPE_ATTR, "triangle");
    allocationNode.setAttribute(GrappaConstants.ORIENTATION_ATTR, "-90");
    Edge edge = new Edge(legendGraph, taskNode, allocationNode);
    edge.setAttribute(CSMARTGraph.INVISIBLE_ATTRIBUTE, "true");

    Node expansionNode = new Node(legendGraph);
    expansionNode.setAttribute(GrappaConstants.LABEL_ATTR, 
			       "Expansion");
    expansionNode.setAttribute(GrappaConstants.SHAPE_ATTR, "trapezium");
    expansionNode.setAttribute(GrappaConstants.ORIENTATION_ATTR, "90");
    edge = new Edge(legendGraph, allocationNode, expansionNode);
    edge.setAttribute(CSMARTGraph.INVISIBLE_ATTRIBUTE, "true");

    Node aggregationNode = new Node(legendGraph);
    aggregationNode.setAttribute(GrappaConstants.LABEL_ATTR, "Aggregation");
    aggregationNode.setAttribute(GrappaConstants.SHAPE_ATTR, "trapezium");
    aggregationNode.setAttribute(GrappaConstants.ORIENTATION_ATTR, "-90");
    edge = new Edge(legendGraph, expansionNode, aggregationNode);
    edge.setAttribute(CSMARTGraph.INVISIBLE_ATTRIBUTE, "true");

    // column 2
    Node assetNode = new Node(legendGraph);
    assetNode.setAttribute(GrappaConstants.SHAPE_ATTR, "box");
    assetNode.setAttribute(GrappaConstants.LABEL_ATTR, "Asset");

    Node assetTransferNode = new Node(legendGraph);
    assetTransferNode.setAttribute(GrappaConstants.LABEL_ATTR, 
				   "AssetTransfer");
    assetTransferNode.setAttribute(GrappaConstants.SHAPE_ATTR, "hexagon");
    edge = new Edge(legendGraph, assetNode, assetTransferNode);
    edge.setAttribute(CSMARTGraph.INVISIBLE_ATTRIBUTE, "true");

    Node workflowNode = new Node(legendGraph);
    workflowNode.setAttribute(GrappaConstants.LABEL_ATTR, "Workflow");
    workflowNode.setAttribute(GrappaConstants.SHAPE_ATTR, "triangle");
    edge = new Edge(legendGraph, assetTransferNode, workflowNode);
    edge.setAttribute(CSMARTGraph.INVISIBLE_ATTRIBUTE, "true");

    // create two columns
    legendGraph.addNodeToSubgraph(taskNode, subg1);
    legendGraph.addNodeToSubgraph(allocationNode, subg1);
    legendGraph.addNodeToSubgraph(expansionNode, subg1);
    legendGraph.addNodeToSubgraph(aggregationNode, subg1);

    legendGraph.addNodeToSubgraph(assetNode, subg2);
    legendGraph.addNodeToSubgraph(assetTransferNode, subg2);
    legendGraph.addNodeToSubgraph(workflowNode, subg2);

    legendGraph.doLayout();
    // TODO: setting background doesn't work
    legendGraph.setGrappaAttribute(GrappaConstants.GRAPPA_BACKGROUND_COLOR_ATTR,
				   "lightgray");
    return legendPanel;
  }


}


