/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * � Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.community;

import java.util.*;

import org.cougaar.core.society.UID;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.monitor.generic.NodeObject;
import org.cougaar.util.PropertyTree;

/**
 * Creates a NodeObject, i.e. an object that can be graphed, from
 * a PropertyTree based on an AssetEvent.
 * These objects are used to populate a Community graph.
 */

public class ULCommunityNode implements NodeObject {
  public static final String MEMBERS = "Members";
  boolean visible = true;
  String UID; // use the UID of the asset that's used to create this
  String label;
  String toolTip = "";
  String color;
  String shape = "roundedbox";
  String sides = "0";
  String distortion = "0";
  String orientation = "0";
  Vector members; // cluster names
  Vector outgoingLinks;
  PropertyTree properties;

  /**
   * Create a NodeObject from a PropertyTree based on an AssetEvent.
   * @param properties    properties from an AssetEvent; supplied by the PSP
   */

  public ULCommunityNode(PropertyTree properties) {
    UID = (String)properties.get(PropertyNames.UID_ATTR);
    label = (String)properties.get(PropertyNames.AGENT_LABEL);
    // use community name as color map
    color = (String)properties.get(PropertyNames.AGENT_COMMUNITY_NAME);
    properties.put(PropertyNames.TABLE_TITLE,
		   "Community <" + label + ">");
    members = new Vector();
    outgoingLinks = new Vector();
    this.properties = properties;
  }

  public String getUID() {
    return UID;
  }

  public String getLabel() {
    return label;
  }
    
  public String getToolTip() {
    return toolTip;
  }

  public String getColor() {
    return color;
  }

  public String getBorderColor() {
    return null;
  }

  public String getFontStyle() {
    return "normal";
  }

  public String getShape() {
    return shape;
  }

  public String getSides() {
    return sides;
  }

  public String getDistortion() {
    return distortion;
  }

  public String getOrientation() {
    return orientation;
  }

  public PropertyTree getProperties() {
    String memberList = (String)properties.get(MEMBERS);
    if (memberList == null) {
      StringBuffer buffer = new StringBuffer(200);
      Iterator e = members.iterator();
      int maxIndex = members.size() - 1;
      for (int i = 0; i <= maxIndex; i++) {
	buffer.append(String.valueOf(e.next()));
	if (i < maxIndex)
	  buffer.append(", ");
      }
      properties.put(MEMBERS, buffer.toString());
    }
    return properties;
  }

  public Vector getIncomingLinks() {
    return null;
  }

  public Vector getOutgoingLinks() {
    return outgoingLinks;
  }

  public Vector getBidirectionalLinks() {
    return null;
  }

  /**
   * Make all nodes ellipses for now.
   */

  private void setNodeShapeParameters() {
    shape = "ellipse";
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  /**
   * Add the names of the agents in the community to the information
   * associated with this community.
   * @param agentNames vector of agent names (Strings)
   */

  public void addMembers(Vector agentNames) {
    members.addAll(agentNames);
  }

  public void addOutgoingLink(String UID) {
    outgoingLinks.add(UID);
  }

  public String getWidth() {
    return "0"; // default, ignored
  }

  public String getHeight() {
    return "0"; // default, ignored
  }
}




