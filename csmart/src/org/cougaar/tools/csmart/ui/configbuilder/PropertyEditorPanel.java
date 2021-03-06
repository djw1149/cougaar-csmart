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

package org.cougaar.tools.csmart.ui.configbuilder;



import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.PropertiesListener;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.PropertyHelper;
import org.cougaar.tools.csmart.core.property.PropertyListener;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.recipe.CompleteAgentRecipe;
import org.cougaar.tools.csmart.recipe.ComplexRecipeBase;
import org.cougaar.tools.csmart.recipe.ComplexRecipeComponent;
import org.cougaar.tools.csmart.recipe.ComponentCollectionRecipe;
import org.cougaar.tools.csmart.recipe.RecipeQueryProperty;
import org.cougaar.tools.csmart.society.*;
import org.cougaar.tools.csmart.society.ui.AgentUIComponent;
import org.cougaar.tools.csmart.society.ui.AssetUIComponent;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.experiment.PropTableModelBase;
import org.cougaar.tools.csmart.ui.util.ComboDialog;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Panel that holds the PropertyEditor for editing the properties of a <code>ModifiableConfigurableComponent</code>. <br>
 * It will both view a non-editable components, and edit an editable one.<br>
 * Note that you may in fact view/edit multiple components at once.
 */
public class PropertyEditorPanel extends JPanel 
  implements PropertiesListener, PropertyListener,
             TreeSelectionListener, HyperlinkListener {
  JSplitPane configCompPanel;
  JTree tree;
  JPopupMenu societyMenu;
  JPopupMenu bindersMenu;
  JPopupMenu componentsMenu;
  JPopupMenu pluginsMenu;
  //JPopupMenu assetMenu;
  JPopupMenu propertyGroupsMenu;
  JPopupMenu relationshipsMenu;
  JPopupMenu baseComponentMenu;
  JPopupMenu baseRecipeComponentMenu;
  JPopupMenu agentMenu;
  JPopupMenu completeAgentMenu;
  JPopupMenu componentCollectionRecipeMenu;

  DefaultMutableTreeNode root;
  PropertyTable propertyTable = null;
  JScrollPane tableScrollPane;
  // correspondence between configurable component and tree node
  private Hashtable componentToNode = new Hashtable();
  private Hashtable nodeToComponent = new Hashtable();
  Hashtable propertyToLabel = new Hashtable();
  boolean isEditable;
  ModifiableComponent componentToConfigure = null;
  private transient Logger log;
  // all property groups, including those created by user
  Vector propertyGroups = null;
  // properties we're listening on, for cleanup
  ArrayList listeningOnProperties = new ArrayList();
  // well known property groups
  Object [] wellKnownPropertyGroups = { 
    PropGroupData.ITEM_IDENTIFICATION,
    PropGroupData.TYPE_IDENTIFICATION,
    PropGroupData.CLUSTER,
    PropGroupData.ORGANIZATION,
    PropGroupData.MILITARYORG,
    PropGroupData.ENTITY,
    PropGroupData.COMMUNITY,
    PropGroupData.ASSIGNMENT,
    PropGroupData.MAINTENANCE,
    PropGroupData.CSSCAPABILITY
  };
  // indices for above
  static final int ITEM_IDENTIFICATION = 0;
  static final int TYPE_IDENTIFICATION = 1;
  static final int CLUSTER = 2;
  static final int ENTITY = 3;
  static final int COMMUNITY = 4;
  static final int MILITARYORG = 5;
  static final int ASSIGNMENT = 6;
  static final int ORGANIZATION = 7;
  static final int MAINTENANCE = 8;
  static final int CSSCAPABILITY = 9;

  // actions for use on popup menus
  private AbstractAction addAgentAction =
    new AbstractAction("Add Agent") {
        public void actionPerformed(ActionEvent e) {
          addAgent();
        }
      };
  private AbstractAction addBinderAction =
    new AbstractAction("Add Binder") {
        public void actionPerformed(ActionEvent e) {
          addBinder();
        }
      };
  private AbstractAction addComponentAction =
    new AbstractAction("Add Other Component") {
        public void actionPerformed(ActionEvent e) {
          addComponent();
        }
      };
  private AbstractAction addPluginAction =
    new AbstractAction("Add Plugin") {
        public void actionPerformed(ActionEvent e) {
          addPlugin();
        }
      };
  private AbstractAction addPropertyGroupAction =
    new AbstractAction("Add Property Group") {
        public void actionPerformed(ActionEvent e) {
          addPropertyGroup();
        }
      };
  private AbstractAction addRelationshipAction =
    new AbstractAction("Add Relationship") {
        public void actionPerformed(ActionEvent e) {
          addRelationship();
        }
      };
  private AbstractAction addAssetParameterAction =
    new AbstractAction("Add Parameter") {
        public void actionPerformed(ActionEvent e) {
          addParameter();
        }
      };
  // Used for PropertyGroups, Relationships, Binder, Plugins, Other Components
  // For the last three, we really want the argument to be false,
  // but within the method it resets it
  // if the component is a MiscComponent anyhow
  private AbstractAction addParameterAction =
    new AbstractAction("Add Parameter") {
        public void actionPerformed(ActionEvent e) {
          addParameter(true);
        }
      };
  private AbstractAction deleteAction =
    new AbstractAction("Delete") {
        public void actionPerformed(ActionEvent e) {
          removeBaseComponent();
        }
      };
  private AbstractAction addTargetAction =
    new AbstractAction("Override Parent Target") {
      public void actionPerformed(ActionEvent e) {
        addTarget(true);
      }
    };

  private AbstractAction addRecipeParameterAction =
    new AbstractAction("Add Parameter") {
      public void actionPerformed(ActionEvent e) {
        addPropGroupParameter(true);
      }
    };

  private Object[] societyMenuItems = {
    addAgentAction
  };
  private Object[] completeAgentMenuItems = {
    addAgentAction
  };
  private Object[] componentCollectionRecipeMenuItems = {
    addAgentAction,
//     addBinderAction,
    addPluginAction
    // FIXME: Maybe do addComponent here?
//     addRelationshipAction,
//     addPropertyGroupAction
  };

  private Object[] bindersMenuItems = {
    addBinderAction
  };
  private Object[] componentsMenuItems = {
    addComponentAction
  };
  private Object[] pluginsMenuItems = {
    addPluginAction
  };
//   private Object[] assetMenuItems = {
//     addAssetParameterAction
//   };
  private Object[] propertyGroupsMenuItems = {
    addPropertyGroupAction
  };
  private Object[] relationshipsMenuItems = {
    addRelationshipAction
  };
  private Object[] baseComponentMenuItems = {
    addParameterAction,
    deleteAction
  };
  private Object[] baseRecipeComponentMenuItems = {
    addTargetAction,
    addParameterAction,
    deleteAction
  };

  private Object[] agentMenuItems = {
    deleteAction
  };

  /**
   * Create a <code>PropertyEditorPanel</code> which is
   * used to edit or view properties of societies and recipes.
   * @param c <code>ModifiableComponent</code> to edit or view
   * @param isEditable true if component can be edited
   */
  public PropertyEditorPanel(ModifiableComponent c, boolean isEditable) {
    // caller decides if this panel should allow editing
    this.isEditable = isEditable;
    // create the panel
    configCompPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    setLayout(new BorderLayout());
    add(configCompPanel, BorderLayout.CENTER);
    componentToConfigure = c;
    init();
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Set component to edit or view; used to re-use a running editor
   * to edit or view a different component.
   * @param c <code>ModifiableComponent</code> to edit or view
   */
  public void reinit(ModifiableComponent c) {
    componentToConfigure = c;
    init();
  }

  /**
   * Update the page being displayed in the browser.
   * @param e <code>HyperlinkEvent</code> containing the URL
   */
  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      Browser.setPage(e.getURL());
    }
  }

  private void init() {
    if (tree != null)
      configCompPanel.remove(tree);
    // create tree and model before adding to it
    // as the add methods reference the tree and model
    root = new DefaultMutableTreeNode("Properties");
    tree = new JTree(new DefaultTreeModel(root));
    if (!isEditable) {
      DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
      renderer.setTextNonSelectionColor(Color.gray);
      renderer.setTextSelectionColor(Color.gray);
      tree.setCellRenderer(renderer);
    }
    makeTree();
    tree.expandPath(new TreePath(root));
    tree.setEditable(isEditable);
    // don't allow cell editing
    tree.setCellEditor(new DefaultCellEditor(new JTextField()) {
        public boolean isCellEditable(EventObject e) {
          return false;
        }
      });
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addTreeSelectionListener(this);
    MouseListener myMouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) displayMenu(e);
      }
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) displayMenu(e);
      }
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) displayMenu(e);
      }
    };
    tree.addMouseListener(myMouseListener);
    // create popup menus to be displayed in tree
    societyMenu = new JPopupMenu();
    completeAgentMenu = new JPopupMenu();
    componentCollectionRecipeMenu = new JPopupMenu();
    baseComponentMenu = new JPopupMenu();
    baseRecipeComponentMenu = new JPopupMenu();
    agentMenu = new JPopupMenu();
    bindersMenu = new JPopupMenu();
    componentsMenu = new JPopupMenu();
    pluginsMenu = new JPopupMenu();
    //    assetMenu = new JPopupMenu();
    propertyGroupsMenu = new JPopupMenu();
    relationshipsMenu = new JPopupMenu();
    for (int i = 0; i < societyMenuItems.length; i++)
      societyMenu.add((Action)societyMenuItems[i]);
    for (int i = 0; i < completeAgentMenuItems.length; i++)
      completeAgentMenu.add((Action)completeAgentMenuItems[i]);
    for (int i = 0; i < componentCollectionRecipeMenuItems.length; i++)
      componentCollectionRecipeMenu.add((Action)componentCollectionRecipeMenuItems[i]);
    for (int i = 0; i < baseComponentMenuItems.length; i++)
      baseComponentMenu.add((Action)baseComponentMenuItems[i]);
    for (int i = 0; i < baseRecipeComponentMenuItems.length; i++)
      baseRecipeComponentMenu.add((Action)baseRecipeComponentMenuItems[i]);
    for (int i = 0; i < agentMenuItems.length; i++)
      agentMenu.add((Action)agentMenuItems[i]);
    for (int i = 0; i < bindersMenuItems.length; i++)
      bindersMenu.add((Action)bindersMenuItems[i]);
    for (int i = 0; i < componentsMenuItems.length; i++)
      componentsMenu.add((Action)componentsMenuItems[i]);
    for (int i = 0; i < pluginsMenuItems.length; i++)
      pluginsMenu.add((Action)pluginsMenuItems[i]);
//     for (int i = 0; i < assetMenuItems.length; i++)
//       assetMenu.add((Action)assetMenuItems[i]);
    for (int i = 0; i < propertyGroupsMenuItems.length; i++)
      propertyGroupsMenu.add((Action)propertyGroupsMenuItems[i]);
    for (int i = 0; i < relationshipsMenuItems.length; i++)
      relationshipsMenu.add((Action)relationshipsMenuItems[i]);
    configCompPanel.setLeftComponent(new JScrollPane(tree));
    propertyTable = new PropertyTable(isEditable);
    // don't allow user to reorder columns
    propertyTable.getTableHeader().setReorderingAllowed(false);
    if (!isEditable) {
      propertyTable.setForeground(Color.gray);
    }
    tableScrollPane = new JScrollPane(propertyTable);
    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.add(tableScrollPane);
    // only add description if user can edit the properties
    if (isEditable) {
      URL url = componentToConfigure.getDescription();
      if (url != null) {
	JTextPane pane = new JTextPane();
	try {
	  pane.setEditable(false);
	  pane.addHyperlinkListener(this);
	  pane.setPage(url);
	  rightPanel.add(pane, BorderLayout.NORTH);
	} catch (IOException ioe) {
          if(log.isErrorEnabled()) {
            log.error("Exception", ioe);
          }
	}
      } else {
	JLabel pane = new JLabel("No description available");
	rightPanel.add(pane, BorderLayout.NORTH);
      }
    }
    configCompPanel.setRightComponent(rightPanel);
    componentToConfigure.addPropertiesListener(this);
    configCompPanel.validate();
    configCompPanel.setDividerLocation(200);
  }

  /**
   * Make a tree from the property names in the selected configurable
   * component.
   */

  private void makeTree() {
    // get the list of configurable components to represent in the tree
    List components = new ArrayList();
    components.add(componentToConfigure);
    // for each configurable component
    for (int i = 0; i < components.size(); i++) {
      ModifiableComponent component = 
        (ModifiableComponent)components.get(i);
      // determine where to put component in tree
      ModifiableComponent parentComponent = 
        (ModifiableComponent)component.getParent();
      DefaultMutableTreeNode parentNode = root;
      if (parentComponent != null) {
        parentNode = 
          (DefaultMutableTreeNode)componentToNode.get(parentComponent);
        if (parentNode == null)
          parentNode = root;
      }
      // add the component to the tree
      createTreeNode(component, parentNode);
      // add its children to the list of components to put in the tree
      int nChildren = component.getChildCount();
      for (int j = 0; j < nChildren; j++) 
        components.add(component.getChild(j));
    }
  }

  /**
   * Display user interface components for property names in a table.
   * Called when user makes a selection in the tree.
   */

  private void displayComponents(PropertyTreeNode node) {
    ModifiableComponent component = 
      (ModifiableComponent)nodeToComponent.get(node);
    for (Iterator i = component.getLocalPropertyNames(); i.hasNext(); ) {
      CompositeName propName = (CompositeName) i.next();
      Property property = component.getProperty(propName);
      if (property != null) {
        node.addPropertyName(propName);
        addTableEntryForProperty(property);
      }
    }
  }

  /**
   * Add table entry for a property.
   */
  private void addTableEntryForProperty(Property property) {
    propertyTable.addProperty(property);
    property.addPropertyListener(this);
    listeningOnProperties.add(property);
  }

  /**
   * PropertiesListener interface.
   */

  /**
   * A property was added by the configurable component.
   * This method is defined in the <code>PropertiesListener</code> interface.
   * Add the property name to the tree.
   * If the parent property name is selected in the tree,
   * then add an user interface component for this property
   * to the current display, otherwise the user interface component
   * is created when the parent property name is selected from the tree.
   * @param e <code>PropertyEvent</code> describing the property added
   */
  public void propertyAdded(PropertyEvent e) {
    Property prop = e.getProperty();
    // get the tree node for this property's configurable component
    ModifiableComponent cc = 
      (ModifiableComponent)prop.getConfigurableComponent();
    PropertyTreeNode node = (PropertyTreeNode)componentToNode.get(cc);
    CompositeName name = prop.getName();
    if (node == null) {
      if (log.isErrorEnabled()) {
        log.error("No node for component: " + cc);
      }
      return;
    }
    if (node.addPropertyName(name)) {
      TreePath path = tree.getSelectionPath();
      if (path == null) return;
      // if it's a property of the selected tree node, then display it now
      PropertyTreeNode selectedNode = 
        (PropertyTreeNode)path.getLastPathComponent();
      if (selectedNode.equals(node)) {
        if(log.isDebugEnabled()) {
          log.debug("PropertyBuilder: Property added: " + name);
        }
        addTableEntryForProperty(prop);
      }
    }
  }

  /**
   * A property was removed by the configurable component.
   * This method is defined in the <code>PropertiesListener</code> interface.
   * Remove the user interface component for the property and remove the
   * property from the tree, only if the property is found.
   * If we get notified because the property of a child node was removed,
   * this does the right thing -- i.e. it ignores the remove notification
   * because it doesn't find the specified property in the properties
   * for the tree node.
   * @param e <code>PropertyEvent</code> describing the property removed
   */
  public void propertyRemoved(PropertyEvent e) {
    Property prop = e.getProperty();
    ModifiableComponent cc = 
      (ModifiableComponent)prop.getConfigurableComponent();
    PropertyTreeNode node = (PropertyTreeNode)componentToNode.get(cc);
    CompositeName name = prop.getName();
    if (node.removePropertyName(name)) {
      TreePath path = tree.getSelectionPath();
      if (path == null)
        return;
      PropertyTreeNode selectedNode = 
        (PropertyTreeNode)path.getLastPathComponent();
      if (selectedNode.equals(node)) {
        if(log.isDebugEnabled()) {
          log.debug("PropertyBuilder: Property removed: " + prop.getName());
        }
        removeTableEntryForProperty(prop);
      }
    }
  }

  /**
   * End PropertiesListener interface.
   */

  /**
   * Remove table entry for a property, if it exists.
   */
  private void removeTableEntryForProperty(Property property) {
    propertyTable.removeProperty(property);
    property.removePropertyListener(this);
    listeningOnProperties.remove(property);
  }

  /**
   * The value of a property changed.
   * This method is defined in the <code>PropertyListener</code> interface.
   * If the configurable component (as opposed to the UI) initiates a change
   * then set the new value in the user inteface (table).
   * @param e <code>PropertyEvent</code> describing the property changed
   */
  public void propertyValueChanged(PropertyEvent e) {
    Property p = e.getProperty();
    p.removePropertyListener(this);
    listeningOnProperties.remove(p);
    int row = 
      ((PropTableModelBase)(propertyTable.getModel())).getRowForProperty(p);
    if (row != -1) {

      Object value = null;
      if (p.isValueSet())
	value = p.getValue();
      else
	value = "<not set>";
      Object valueInTable = propertyTable.getValueAt(row, 1);
      if (valueInTable.equals(value)) {
	p.addPropertyListener(this);
        listeningOnProperties.add(p);
	return; // don't need to update
      }
      propertyTable.setValueAt(p.getValue(), row, 1);
      propertyTable.revalidate();
      propertyTable.repaint();
    }
    p.addPropertyListener(this);
    listeningOnProperties.add(p);
  }

  /**
   * Some aspect of a property changed (i.e. new label, default value, 
   * class or allowed values).
   * This method is defined in the <code>PropertyListener</code> interface.
   * Currently not implemented.
   * @param e <code>PropertyEvent</code> describing the property changed
   */
  public void propertyOtherChanged(PropertyEvent e) {
    // TODO: handle DEFAULTVALUE_CHANGED, LABEL_CHANGED,
    // CLASS_CHANGED, ALLOWEDVALUES_CHANGED
  }

  /**
   * End PropertyListener interface.
   */

  /**
   * TreeSelectionListener interface.
   */

  /**
   * If tree selection changes, update list of user interface components
   * displayed. Part of the <code>TreeSelectionListener</code> interface.
   * @param event the tree selection event describing the selection change
   */
  public void valueChanged(TreeSelectionEvent event) {
    TreePath path = event.getPath();
    if (path == null) return;
    Object o = path.getLastPathComponent();
    if (o instanceof PropertyTreeNode) {
      // stop any editing in progress in table and accept the value
      stopEditing();
      propertyTable.removeAll(); // clear what was displayed
      if (event.isAddedPath())   // display parameters for new selection
        displayComponents((PropertyTreeNode)o);
    }
  }

  /**
   * End TreeSelectionListener interface.
   */

  /**
   * Display popup menu with actions appropriate for the selected tree node.
   */

  private void displayMenu(MouseEvent e) {
    // the path to the node the mouse is pointing at
    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
    if (selPath == null)
      return;
    tree.setSelectionPath(selPath);
    int x = e.getX();
    int y = e.getY();
    // show menu for type of node selected
    Object o = nodeToComponent.get(selPath.getLastPathComponent());
    if (o instanceof SocietyComponent)
      societyMenu.show(tree, x, y);
    else if (o instanceof ContainerComponent) {
      String name = ((ModifiableComponent)o).getShortName();
      if (name.equals("Binders"))
        bindersMenu.show(tree, x, y);
      else if (name.equals("Plugins"))
        pluginsMenu.show(tree, x, y);
      else if (name.equals("Other Components"))
        componentsMenu.show(tree, x, y);
      else if (name.equals("Relationships"))
        relationshipsMenu.show(tree, x, y);
      else if (name.equals("Property Groups"))
        propertyGroupsMenu.show(tree, x, y);
    } else if (o instanceof AssetComponent) {
      // Bug 1883: Don't allow deleting the AssetData, since you can't
      // add it back in again...
      //agentMenu.show(tree, x, y);
    } else if (o instanceof AgentComponent)
      agentMenu.show(tree, x, y);
    else if (o instanceof RelationshipComponent)
      agentMenu.show(tree, x, y);
    else if (o instanceof ComplexRecipeComponent) {
      if (o instanceof CompleteAgentRecipe) 
        completeAgentMenu.show(tree, x, y);
      else if (o instanceof ComponentCollectionRecipe)
        componentCollectionRecipeMenu.show(tree, x, y);
    } else if (o instanceof BaseComponent) {
      // Need to see if the Parent is a ComponentCollectionRecipe.
      if (nodeToComponent.get(selPath.getParentPath().getLastPathComponent()) instanceof ComponentCollectionRecipe) 
        baseRecipeComponentMenu.show(tree, x, y);
      else
        baseComponentMenu.show(tree, x, y);
    } else if (o == null) {
      if (log.isInfoEnabled()) {
	log.info("No menu item for null component (root?).");
      }
    } else if (log.isInfoEnabled())
      log.info("No menu for component of class: " + o);
  }

  /**
   * Actions invoked from popup menu.
   */

  /**
   * Query the user for the name of the agent.
   * Create a component for the agent, and a tree node
   * representing the agent, and add it to the society node in the tree.
   * Create components and add tree nodes for Binders, Plugins and AssetData
   * folders.
   */

  private void addAgent() {
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    ModifiableComponent society = 
      (ModifiableComponent)nodeToComponent.get(selNode);

    String name = 
      (String)JOptionPane.showInputDialog(this, "Enter Agent Name", 
                                          "Agent Name",
                                          JOptionPane.QUESTION_MESSAGE,
                                          null, null, "");
    if (name == null) return;

    name = name.trim();
    if (name.equals(""))
      return;

    if (society instanceof ComplexRecipeComponent) {
      while (name.equals(society.getFullName().toString())) {
	if (log.isInfoEnabled())
	  log.info("Cant add agent to Complex recipe with same name as recipe: " + name);
	name = 
	  (String)JOptionPane.showInputDialog(this, "Enter Agent Name different from Recipe name", 
                                          "Agent Name",
                                          JOptionPane.QUESTION_MESSAGE,
                                          null, null, name);
	if (name == null) return;
	
	name = name.trim();
	if (name.equals(""))
	  return;
      }
    }

    AgentComponent agentComponent = 
      (AgentComponent)new AgentUIComponent(name);
    // put the node in the tree before adding properties to it
    society.addChild(agentComponent);
    PropertyTreeNode agentNode = createTreeNode(agentComponent, selNode);
    agentComponent.initProperties();
    tree.setSelectionPath(tree.getSelectionPath().pathByAddingChild(agentNode));
    addContainerComponent(agentNode, agentComponent, "Other Components");
    addContainerComponent(agentNode, agentComponent, "Binders");
    addContainerComponent(agentNode, agentComponent, "Plugins");
    AssetComponent asset = (AssetComponent)new AssetUIComponent();
    agentComponent.addChild(asset);
    PropertyTreeNode assetNode = createTreeNode(asset, agentNode);
    asset.initProperties();
    addContainerComponent(assetNode, asset, "Property Groups");
    addContainerComponent(assetNode, asset, "Relationships");
  }

  private void addContainerComponent(DefaultMutableTreeNode parentNode,
                                     BaseComponent parent, String name) {
    ContainerComponent containerComponent = 
      (ContainerComponent)new ContainerBase(name);
    parent.addChild(containerComponent);
    createTreeNode(containerComponent, parentNode);
    containerComponent.initProperties();
  }

  /**
   * Remove base component from parent
   * and remove tree branch or node representing that base component.
   */

  private void removeBaseComponent() {
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    ModifiableComponent cc = 
      (ModifiableComponent)nodeToComponent.get(selNode);
    Object parent = cc.getParent();
    if (parent != null && parent instanceof ModifiableComponent)
      ((ModifiableComponent)parent).removeChild(cc);
    // remove agent and subnodes from the tree
    removeTreeNodes(selNode);
  }

  /**
   * Query the user for the name of the bindere.
   * Create a component for the binder, and a tree node
   * representing the binder, and add it to the agent node in the tree.
   */
  private void addBinder() {
    Enumeration components = componentToNode.keys();
    ArrayList binderNames = new ArrayList();
    ArrayList binderClasses = new ArrayList();
    while (components.hasMoreElements()) {
      Object o = components.nextElement();
      if (o instanceof BinderComponent) {
        BinderComponent binder = (BinderComponent)o;
        String name = ((BaseComponent)binder).getShortName();
	if (name == null)
	  continue;
	if (name.trim().equals(""))
	  continue;
        // if name is prefixed with an agentname|, strip it off
        int index = name.indexOf('|');
        if (index != -1)
          name = name.substring(index+1);
        if (!binderNames.contains(name)) {
          binderNames.add(name);
          binderClasses.add(binder.getComponentClassName());
        }
      }
    }
    // add in binder names and classes from the database
    Set dbBinderClasses = DBUtils.dbGetBinderClasses();
    for (Iterator i = dbBinderClasses.iterator(); i.hasNext(); ) {
      String dbBinderClass = (String)i.next();
      if (!binderClasses.contains(dbBinderClass)) {
        binderClasses.add(dbBinderClass);
      }
    }

    ArrayList sortedClasses = (ArrayList)binderClasses.clone();
    Collections.sort(sortedClasses);
    String className = 
      (String)ComboDialog.showDialog(this, "Select Binder or Enter New Class", new Vector(sortedClasses));
    if (className == null)
      return;
    className = className.trim();
    if (className.length() == 0)
      return;
    BinderComponent binder = new BinderBase(className, className, null);
    addBaseComponent(binder);
    binder.setComponentType(ComponentData.AGENTBINDER);
  }

  /**
   * Query the user for the name of the component.
   * Create a component for it, and a tree node
   * representing the component, and add it to the agent node in the tree.
   */
  private void addComponent() {
    // FIXME: Get the class from the user
    // and provide a drop-down, just like for plugins...

    // Also, get the component type from the user, using a drop
    // down that lists at least plugin, 2 binders

    String name = 
      (String)JOptionPane.showInputDialog(this, "Enter Component Name",
                                          "Component Name",
                                          JOptionPane.QUESTION_MESSAGE,
                                          null, null, "");
    if (name == null)
      return;
    name = name.trim();
    if (name.length() == 0)
      return;
    MiscComponent binder = new ComponentBase(name);
    addBaseComponent(binder);
  }

  /**
   * Query the user for the name of the plugin.
   * Create a component for the plugin, and a tree node
   * representing the plugin, and add it to the agent node in the tree.
   */

  private void addPlugin() {
    // get plugins from all existing agents
    Enumeration components = componentToNode.keys();
    ArrayList pluginNames = new ArrayList();
    ArrayList pluginClasses = new ArrayList();
    while (components.hasMoreElements()) {
      Object o = components.nextElement();
      if (o instanceof PluginComponent) {
        PluginComponent plugin = (PluginComponent)o;
        String name = ((BaseComponent)plugin).getShortName();
	if (name == null)
	  continue;
	name = name.trim();
	if (name.equals(""))
	  continue;
        // if name is prefixed with an agentname|, strip it off
        int index = name.indexOf('|');
        if (index != -1)
          name = name.substring(index+1);
        if (!pluginNames.contains(name)) {
          pluginNames.add(name);
          pluginClasses.add(plugin.getComponentClassName());
        }
      }
    }
    // add in plugin names and classes from database
    Set dbPluginClasses = DBUtils.dbGetPluginClasses();
    for (Iterator i = dbPluginClasses.iterator(); i.hasNext(); ) {
      String dbPluginClass = (String)i.next();
      if (!pluginClasses.contains(dbPluginClass)) {
        pluginClasses.add(dbPluginClass);
      }
    }

    ArrayList sortedClasses = (ArrayList)pluginClasses.clone();
    Collections.sort(sortedClasses);
    String className =
      (String)ComboDialog.showDialog(this, "Select Plugin or Enter New Class", new Vector(sortedClasses));
    if (className == null)
      return;
    className = className.trim();
    if (className.length() == 0)
      return;
    
    PluginComponent plugin = 
      (PluginComponent)new PluginBase(className, className, null);
    addBaseComponent(plugin);
  }

  /**
   * Add base component to the component in the selected node.
   * Init the properties of the base component.
   * Make the new component be the selected node.
   */
  private void addBaseComponent(BaseComponent bc) {
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    BaseComponent parentBC = (BaseComponent)nodeToComponent.get(selNode);
    if (parentBC.getChild(bc.getFullName()) != null) {
      if (log.isDebugEnabled()) {
        log.debug("Duplicate Names, adjusting....");
      }
      bc.setName(bc.getFullName() + String.valueOf(parentBC.getChildCount()));
    } 

    parentBC.addChild(bc);
    if (log.isDebugEnabled()) {
      log.debug("Adding Child: " + bc.getFullName());
    }
    DefaultMutableTreeNode newNode = createTreeNode(bc, selNode);
    bc.initProperties();
    tree.setSelectionPath(tree.getSelectionPath().pathByAddingChild(newNode));
  }

  /**
   * Create component and tree node for new property group.
   */
  private void addPropertyGroup() {
    if (propertyGroups == null) {
      propertyGroups = new Vector(wellKnownPropertyGroups.length);
      for (int i = 0; i < wellKnownPropertyGroups.length; i++)
        propertyGroups.add(wellKnownPropertyGroups[i]);
    }
    String pgName =
      (String)ComboDialog.showDialog(this, "Property Group", propertyGroups);
    if (pgName == null)
      return;
    int index = propertyGroups.indexOf(pgName);
    if (index == -1) {
      pgName = pgName.trim();
      if (pgName.length() == 0)
        return;
    }
    PropGroupData pgData = new PropGroupData(pgName);
    PropGroupComponent pg = (PropGroupComponent)new PropGroupBase(pgData);
    addBaseComponent(pg);
    // for each of the well defined properties
    // add the appropriate parameters
    if (index >= 0 && index < wellKnownPropertyGroups.length)
      setParameters(pg, index);
    else
      propertyGroups.add(pgName); // user defined property group
  }

  /**
   * Create component and tree node for new relationship.
   */
  private void addRelationship() {
    Vector relationships = new Vector();

//     DefaultMutableTreeNode selNode =
//       (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    // relationship configurable components are created from relationship data
    RelationshipData relData = new RelationshipData();

    if (componentToConfigure instanceof ComplexRecipeComponent) {
      Set agentNames = DBUtils.dbGetAgentNames();
      for (Iterator i = agentNames.iterator(); i.hasNext();) {
        relationships.add((String)i.next());
      }
    } else {

      AgentComponent[] agents = 
        ((SocietyComponent)componentToConfigure).getAgents();
      for (int i = 0; i < agents.length; i++)
        relationships.add(agents[i].getShortName());
    }
    String relName = 
      (String)ComboDialog.showDialog(this, "Relationship With Agent", relationships);
    if (relName == null)
      return;
    int index = relationships.indexOf(relName);
    if (index == -1) {
      relName = relName.trim();
      if (relName.length() == 0)
        return;
    }
    relData.setSupported(relName);
    relData.setItemId(relName);
    relData.setType("");
    relData.setRole("");
    
    RelationshipComponent relationship = new RelationshipBase(relData);
    addBaseComponent(relationship);
  }

  /**
   * Add parameters for well know properties.
   */
  private void setParameters(ModifiableComponent pg, int pgIndex) {
    switch (pgIndex) {
    case ITEM_IDENTIFICATION:
      pg.addProperty("ItemIdentification (String)", "");
      pg.addProperty("Nomenclature (String)", "");
      pg.addProperty("AlternateItemIdentification (String)", "");
      break;
    case TYPE_IDENTIFICATION:
      pg.addProperty("TypeIdentification (String)", "UTC/RTOrg");
      break;
    case CLUSTER:
      // type is enclosed in parens and appended to name
      pg.addProperty("MessageAddress (MessageAddress)", "");
      break;
    case ENTITY:
      break;
    case COMMUNITY:
//       pg.addProperty("TimeSpan (TimeSpan)", "");
//       pg.addProperty("Communities (??)", "");
      break;
    case MILITARYORG:
      break;
    case ASSIGNMENT:
      break;
    case ORGANIZATION:
      // Service (Service)
      pg.addProperty("Service (Service)", "");
      // Agency (Agency)
      pg.addProperty("Agency (Agency)", "");
      // Roles (Collection<Role>)
      pg.addProperty("Roles (Role)", "");
      break;
    case MAINTENANCE:
      break;
    case CSSCAPABILITY:
      break;
    default:
      break;
    }
  }

  /**
   * Add a parameter to a configurable component.  Invoked when the
   * user selects a tree node and invokes the "Add Parameter" option from
   * the pop-up menu.  Note that this simply adds the parameter to the
   * configurable component corresponding to the tree node.  It relies
   * on listeners that were previously set up on the configurable components
   * to update the user interface.
   */
  private void addParameter() {
    addParameter(true);
  }

  
  private void addParameter(boolean getName) {
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    ModifiableComponent cc = 
      (ModifiableComponent)nodeToComponent.get(selNode);

    if (cc instanceof MiscComponent)
      getName = false;

    String name = "";
    if (getName) {
      name =
	(String)JOptionPane.showInputDialog(this, "Enter Parameter Name",
					    "Parameter Name",
					    JOptionPane.QUESTION_MESSAGE,
					    null, null, name);
      if (name == null) return;
      name = name.trim(); // trim white space
      if (name.length() == 0) return;
    }
    
    String type = "";
    if (cc instanceof PropGroupComponent) {
      type =
        (String)JOptionPane.showInputDialog(this, "Enter Parameter Type",
                                            "Parameter Type",
                                            JOptionPane.QUESTION_MESSAGE,
                                            null, null, "String");
      type = type.trim();
      name = name + " (" + type + ")";
      // FIXME: How do we handle getting a PG MultiVal?
    }

    String value =
      (String)JOptionPane.showInputDialog(this, "Enter Parameter Value",
                                          "Parameter Value",
                                          JOptionPane.QUESTION_MESSAGE,
                                          null, null, "");
    if (value == null) return;
    value = value.trim(); // trim white space
    if (value.length() == 0) return;

    // Allow lists
    Object realValue = null;
    if (value.startsWith("[") && value.endsWith("]")) {
      realValue = PropertyHelper.convertStringToArrayList(value);
    } else if (value.startsWith("{") && value.endsWith("}")) {
      realValue = PropertyHelper.convertStringToArray(value);
    } else {
      realValue = value;
    }

    if (getName) {
      // FIXME: See if the name is unique. If not, then what?
      cc.addProperty(name, realValue);
    } else {
      cc.addProperty(generateName((ConfigurableComponent)cc), realValue);
    }
  }


  private void addPropGroupParameter() {
    addPropGroupParameter(true);
  }

  private void addPropGroupParameter(boolean getName) {
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    ModifiableComponent cc = 
      (ModifiableComponent)nodeToComponent.get(selNode);

    if (cc instanceof MiscComponent)
      getName = false;

    String name = "";
    if (getName) {
      name =
	(String)JOptionPane.showInputDialog(this, "Enter Parameter Name",
					    "Parameter Name",
					    JOptionPane.QUESTION_MESSAGE,
					    null, null, name);
      if (name == null) return;
      name = name.trim(); // trim white space
      if (name.length() == 0) return;
    }

    String type = "";
    type = 
      (String)JOptionPane.showInputDialog(this, "Enter Parameter Type",
                                          "Parameter Type",
                                          JOptionPane.QUESTION_MESSAGE,
                                          null, null, type);
    type = type.trim();
    name = name + "   (" + type + ")";

    String value =
      (String)JOptionPane.showInputDialog(this, "Enter Parameter Value",
                                          "Parameter Value",
                                          JOptionPane.QUESTION_MESSAGE,
                                          null, null, "");
    if (value == null) return;
    value = value.trim(); // trim white space
    if (value.length() == 0) return;

    // Allow lists
    Object realValue = null;
    if (value.startsWith("[") && value.endsWith("]")) {
      realValue = PropertyHelper.convertStringToArrayList(value);
    } else if (value.startsWith("{") && value.endsWith("}")) {
      realValue = PropertyHelper.convertStringToArray(value);
    } else {
      realValue = value;
    }

    if (getName) {
      cc.addProperty(name, realValue);
    } else {
      cc.addProperty(generateName((ConfigurableComponent)cc), realValue);
    }
  }

  private void addTarget() {
    addTarget(true);
  }

  private void addTarget(boolean getName) {
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    ModifiableConfigurableComponent cc = 
      (ModifiableConfigurableComponent)nodeToComponent.get(selNode);
    cc.addProperty(new RecipeQueryProperty(cc, ComplexRecipeBase.PROP_TARGET_COMPONENT_QUERY, "recipeQuerySelectNothing"));
    cc.getProperty(ComplexRecipeBase.PROP_TARGET_COMPONENT_QUERY).setPropertyClass(String.class);
  }


  private String generateName(ConfigurableComponent cc) {
    // get all properties
    Iterator i = cc.getSortedLocalPropertyNames();
    // find the highest numbered property of the form param-x
    int lastParamNumber = -1;
    while (i.hasNext()) {
      String name = ((CompositeName)i.next()).last().toString();
      int index = name.indexOf("param-");
      if (index == -1)
        continue;
      int curNum = -1;
      try {
        curNum = Integer.parseInt(name.substring(index+6));
      } catch (NumberFormatException nfe) {
        //System.out.println("NFE: " + nfe);
        // just ignore
      }
      if (curNum > lastParamNumber)
	lastParamNumber = curNum;
    }
    // generate next highest numbered name
    lastParamNumber++;
    String name = "param-" + String.valueOf(lastParamNumber);    
    return name;
  }

  /**
   * Create a tree node for the configurable component,
   * and add it to the parent tree node.
   * Also, put the component and the new node in hashtables to facilitate
   * matching tree nodes to components.
   * Return the new tree node.
   */
  private PropertyTreeNode createTreeNode(BaseComponent component,
                                          DefaultMutableTreeNode parentNode) {
    PropertyTreeNode newNode = new PropertyTreeNode(component.getFullName());
    if (component instanceof AssetComponent || 
        component instanceof ContainerComponent)
      newNode.setLeaf(false);
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();        
    model.insertNodeInto(newNode, parentNode, parentNode.getChildCount());
    componentToNode.put(component, newNode);
    nodeToComponent.put(newNode, component);
    return newNode;
  }

  /**
   * Remove node and its children from tree and
   * remove node and its children from the hashtables.
   */

  private void removeTreeNodes(DefaultMutableTreeNode nodeToRemove) {
    Enumeration nodesToRemove = nodeToRemove.depthFirstEnumeration();
    while (nodesToRemove.hasMoreElements()) {
      DefaultMutableTreeNode node = 
        (DefaultMutableTreeNode)nodesToRemove.nextElement();
      ModifiableComponent cc = 
        (ModifiableComponent)nodeToComponent.get(node);
      componentToNode.remove(cc);
      nodeToComponent.remove(node);
    }
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
    model.removeNodeFromParent(nodeToRemove);
  }

  /**
   * Stop editing in table and accept changes.
   */

  public void stopEditing() {
    int row = propertyTable.getEditingRow();
    int column = propertyTable.getEditingColumn();
    if (row != -1 && column != -1) 
      propertyTable.getCellEditor(row, column).stopCellEditing();
  }

  /**
   * Remove this object as a listener on the configurable component.
   */

  public void exit() {
    componentToConfigure.removePropertiesListener(this);
    for (int i = 0; i < listeningOnProperties.size(); i++)
      ((Property)listeningOnProperties.get(i)).removePropertyListener(this);
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}
