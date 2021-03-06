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

package org.cougaar.tools.csmart.ui.monitor.society;

import org.cougaar.core.util.UID;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.monitor.generic.NodeObject;
import org.cougaar.util.PropertyTree;

import java.util.Vector;

/**
 * Creates a NodeObject, i.e. an object that can be graphed, from
 * a PropertyTree object derived from Organization assets.
 * These objects are used to populate Society graphs.
 */
public class ULSocietyNode implements NodeObject {
  boolean visible;
  String UID;
  String label;
  String toolTip;
  String color;
  String shape;
  String sides = "0";
  String distortion = "0";
  String orientation = "0";
  PropertyTree properties;
  Vector incomingLinks;
  Vector outgoingLinks;

  /**
   * Create a NodeObject from a PropertyTree based on an AssetEvent.
   * @param properties  properties from an AssetEvent; supplied by the Servlet
   */
  public ULSocietyNode(PropertyTree properties) {
    UID = (String)properties.get(PropertyNames.UID_ATTR);
    color = (String)properties.get(PropertyNames.ORGANIZATION_NAME);
    label = (String)properties.get(PropertyNames.ORGANIZATION_NAME);
    toolTip = "";
    setNodeShapeParameters();
    visible = true;
    properties.put(PropertyNames.TABLE_TITLE,
		   "Agent " + 
		   (String)properties.get(PropertyNames.ORGANIZATION_NAME));
    this.properties = properties;
    incomingLinks = new Vector();
    outgoingLinks = new Vector();
  }

  public String getUID() {
    return UID;
  }

  /**
   * Force quotations at start and end of label, otherwise dot
   * mis-interprets labels that start with a digit.
   */

  public String getLabel() {
    //    return "\"" + label + "\"";
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
    return properties;
  }

  public Vector getIncomingLinks() {
    return incomingLinks;
  }

  public void addIncomingLink(String UID) {
    if (!incomingLinks.contains(UID))
      incomingLinks.addElement(UID);
  }

  public Vector getOutgoingLinks() {
    return outgoingLinks;
  }

  public void addOutgoingLink(String UID) {
    if (!outgoingLinks.contains(UID))
      outgoingLinks.addElement(UID);
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

  public String getWidth() {
    return "0"; // default, ignored
  }

  public String getHeight() {
    return "0"; // default, ignored
  }
}




