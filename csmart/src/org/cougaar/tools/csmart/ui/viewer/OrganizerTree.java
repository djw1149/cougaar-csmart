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

package org.cougaar.tools.csmart.ui.viewer;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.tree.CSMARTDataFlavor;
import org.cougaar.tools.csmart.ui.tree.DNDTree;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

public class OrganizerTree extends DNDTree {
  private transient Logger log;

  public static final DataFlavor folderFlavor =
    new CSMARTDataFlavor(DefaultMutableTreeNode.class,
                         null,
                         OrganizerTree.class,
                         "Folder");
  public static final CSMARTDataFlavor societyFlavor =
    new CSMARTDataFlavor(SocietyComponent.class,
                         null,
                         OrganizerTree.class,
                         "Society");
  public static final CSMARTDataFlavor recipeFlavor =
    new CSMARTDataFlavor(RecipeComponent.class,
                         null,
                         OrganizerTree.class,
                         "Recipe");
  public static final CSMARTDataFlavor experimentFlavor =
    new CSMARTDataFlavor(Experiment.class,
                         null,
                         OrganizerTree.class,
                         "Experiment");
  public static final CSMARTDataFlavor componentFlavor =
    new CSMARTDataFlavor(ModifiableComponent.class,
                         null,
                         OrganizerTree.class,
                         "Modifiable Component");

  private DefaultTreeModel model;

  /**
   * MyTransferable encapsulates the DefaultMutableTreeNode
   * being transferred.
   */
  private static class MyTransferable implements Transferable {
    Object theData;
    DataFlavor[] flavors;
    public MyTransferable(DefaultMutableTreeNode aNode) {
      theData = aNode;
      Object theUserData = aNode.getUserObject();
      if (theUserData instanceof String)
        flavors = new DataFlavor[] {folderFlavor};
      else if (theUserData instanceof SocietyComponent)
        flavors = new DataFlavor[] {societyFlavor};
      else if (theUserData instanceof RecipeComponent)
        flavors = new DataFlavor[] {recipeFlavor};
      else if (theUserData instanceof Experiment)
        flavors = new DataFlavor[] {experimentFlavor};
      else if (theUserData instanceof ModifiableComponent)
        flavors = new DataFlavor[] {componentFlavor};
      else
        throw new IllegalArgumentException("Unknown node");
    }
    /**
     * If the flavor allows children, then return
     * the DefaultMutableTreeNode, so that the children can
     * be transferred; otherwise just return the user object.
     */
    public Object getTransferData(DataFlavor flavor) {
      if (!flavor.equals(flavors[0]))
        throw new IllegalArgumentException("Illegal DataFlavor");
      if (flavor.equals(folderFlavor) || flavor.equals(experimentFlavor))
        return theData;
      else
        return ((DefaultMutableTreeNode)theData).getUserObject();
    }
    public DataFlavor[] getTransferDataFlavors() {
      return flavors;
    }
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return flavors[0].equals(flavor);
    }
  }

  public OrganizerTree(DefaultTreeModel model) {
    super(model);
    this.model = model;
    setExpandsSelectedPaths(true);
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void setSelection(TreeNode treeNode) {
    TreeNode[] nodes = model.getPathToRoot(treeNode);
    TreePath path = new TreePath(nodes);
    setSelectionPath(path);
  }

  public Transferable makeDraggableObject(Object o) {
    Transferable tran = null;
    if (o instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
      return new MyTransferable(node);
    }
    throw new IllegalArgumentException("Not a DefaultMutableTreeNode");
  }

  /**
   * Allows drop if target is either root or a folder;
   * allows any source as long as it's from this tree.
   * @param possibleFlavors possible flavors of the source
   * @param target where the source will be dropped
   */
  public int isDroppable(DataFlavor[] possibleFlavors, 
                         DefaultMutableTreeNode target) {
    Object userObject = target.getUserObject();
    // if dropping on root or a folder
    if (userObject == null || userObject instanceof String) {
      for (int i = 0; i < possibleFlavors.length; i++) {
        DataFlavor flavor = possibleFlavors[i];
        if (flavor instanceof CSMARTDataFlavor) {
          CSMARTDataFlavor cflavor = (CSMARTDataFlavor) flavor;
          // if dragging & dropping in this tree, then it's ok
          if (getClass().getName().equals(cflavor.getSourceClassName()))
            return DnDConstants.ACTION_MOVE;
        }
      }
    }
    // dropping from another tree is not allowed
    return DnDConstants.ACTION_NONE;
  }

  /**
   * The object is draggable if it's not root and it's not
   * inside an experiment.
   */
  public boolean isDraggable(Object o) {
    if (o instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
      if (node == getModel().getRoot()) 
        return false; // not draggable if it's the root node
      // not draggable if it's inside an experiment
      DefaultMutableTreeNode parentNode = 
        (DefaultMutableTreeNode)node.getParent();
      Object userObject = parentNode.getUserObject();
      if (userObject != null && userObject instanceof Experiment)
        return false;
      return true;
    }
    return false;
  }

  /**
   * Add the dragged element to the drop site.
   * If the dragged element has children, they are moved as well.
   */
  public int addElement(Transferable t,
                        DefaultMutableTreeNode target,
                        DefaultMutableTreeNode before) {
    DataFlavor[] flavors = t.getTransferDataFlavors();
    int action = isDroppable(flavors, target);
    if (action == DnDConstants.ACTION_NONE) 
      return action; // do nothing
    Object data = null;
    try {
      data = t.getTransferData(flavors[0]);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception adding dropped element:", e);
        return DnDConstants.ACTION_NONE;
      }
    }
    // the data being added
    Object userData = data;
    boolean allowsChildren = false;
    // if the data being added is a tree node, then extract the user data
    if (data instanceof DefaultMutableTreeNode) {
      userData = ((DefaultMutableTreeNode)data).getUserObject();
      if (userData instanceof String || userData instanceof Experiment)
        allowsChildren = true;
    }
    // create a new tree node for the transferred data
    DefaultMutableTreeNode newNode =
      new DefaultMutableTreeNode(userData, allowsChildren);
    int ix = target.getChildCount();
    if (before != null) {
      ix = model.getIndexOfChild(target, before);
    }
    if(log.isDebugEnabled()) {
      log.debug("Insert into " + target + " at " + ix + " before " + before);
    }
    // put the new tree node into the target
    model.insertNodeInto(newNode, target, ix);
    // if the data being transferred is a tree node, then add its children
    if (data instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode source = (DefaultMutableTreeNode)data;
      int n = source.getChildCount();
      DefaultMutableTreeNode[] children = new DefaultMutableTreeNode[n];
      for (int i = 0; i < n; i++) 
        children[i] = (DefaultMutableTreeNode) source.getChildAt(i);
      for (int i = 0; i < n; i++) 
        model.insertNodeInto(children[i], newNode, i);
    }
    selectNode(newNode);
    return action;  // Always MOVE
  }
}
