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
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.core.db.ExperimentDB;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.viewer.GUIUtils;
import org.cougaar.tools.csmart.core.property.ModificationListener;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
import org.cougaar.tools.csmart.core.db.PopulateDb;

public class ExperimentBuilder extends JFrame implements ModificationListener {
  private static final String FILE_MENU = "File";
  private static final String SAVE_MENU_ITEM = "Save";
  private static final String SAVE_AS_MENU_ITEM = "Save As...";
  private static final String DUMP_INI_ITEM = "Debug: Dump .ini files";
  private static final String EXIT_MENU_ITEM = "Close";
  private static final String FIND_MENU = "Find";
  private static final String FIND_HOST_MENU_ITEM = "Find Host...";
  private static final String FIND_NODE_MENU_ITEM = "Find Node...";
  private static final String FIND_AGENT_MENU_ITEM = "Find Agent...";
  private static final String HELP_MENU = "Help";
  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "About Experiment Builder";
  private Experiment experiment;
  private CSMART csmart;
  private JTabbedPane tabbedPane;
  private UnboundPropertyBuilder propertyBuilder;
  private HostConfigurationBuilder hcb;
  private TrialBuilder trialBuilder;
  private ThreadBuilder threadBuilder;
  private boolean modified = false;
  private JMenu findMenu;
  private PopulateDb.ConflictHandler saveToDbConflictHandler =
    GUIUtils.createSaveToDbConflictHandler(this);
  // items in file menu specific to selected node in HostConfigurationBuilder
  private JMenuItem globalCommandLineMenuItem;
  private JMenuItem newHostMenuItem;
  private JMenu newNodeMenu;
  private JMenuItem newUnassignedNodeMenuItem;
  private JMenuItem newAssignedNodeMenuItem;
  private JMenuItem commandLineMenuItem;
  private JMenuItem describeHostMenuItem;
  private JMenuItem describeNodeMenuItem;
  private JMenuItem hostTypeMenuItem;
  private JMenuItem hostLocationMenuItem;
  private JMenuItem deleteHostMenuItem;
  private JMenuItem deleteNodeMenuItem;

  private Action helpAction = new AbstractAction(HELP_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
	URL help = (URL)this.getClass().getResource(HELP_DOC);
	if (help != null)
	  Browser.setPage(help);
      }
    };
  private Action aboutAction = new AbstractAction(ABOUT_CSMART_ITEM) {
      public void actionPerformed(ActionEvent e) {
	URL help = (URL)this.getClass().getResource(ABOUT_DOC);
	if (help != null)
	  Browser.setPage(help);
      }
    };
  private Action[] fileActions = {
    new AbstractAction(SAVE_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
        save();
      }
    },
    new AbstractAction(SAVE_AS_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
        saveAs();
      }
    },
    new AbstractAction(DUMP_INI_ITEM) {
	public void actionPerformed(ActionEvent e) {
	  dumpINIs();
	}
      },
    new AbstractAction(EXIT_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
	exit();
        NamedFrame.getNamedFrame().removeFrame(ExperimentBuilder.this);
	dispose();
      }
    }
  };
  private Action[] findActions = {
    new AbstractAction(FIND_HOST_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
        hcb.findHost();
      }
    },
    new AbstractAction(FIND_NODE_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
        hcb.findNode();
      }
    },
    new AbstractAction(FIND_AGENT_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
        hcb.findAgent();
      }
    }
  };

  private Action[] helpActions = {
    helpAction,
    aboutAction
  };

  public ExperimentBuilder(CSMART csmart, Experiment experiment) {
    this.csmart = csmart;
    setExperiment(experiment);
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.setToolTipText("Configure hosts and nodes, save, or quit");
    globalCommandLineMenuItem = 
      new JMenuItem(HostConfigurationBuilder.GLOBAL_COMMAND_LINE_MENU_ITEM);
    globalCommandLineMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          hcb.setGlobalCommandLine();
        }
      });
    fileMenu.add(globalCommandLineMenuItem);
    newHostMenuItem = 
      new JMenuItem(HostConfigurationBuilder.NEW_HOST_MENU_ITEM);
    newHostMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          hcb.createHost();
        }
      });
    fileMenu.add(newHostMenuItem);
    newNodeMenu = new JMenu(HostConfigurationBuilder.NEW_NODE_MENU_ITEM);
    newUnassignedNodeMenuItem = new JMenuItem("Unassigned");
    newUnassignedNodeMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          hcb.createUnassignedNode();
        }
      });
    newNodeMenu.add(newUnassignedNodeMenuItem);
    newAssignedNodeMenuItem = new JMenuItem("On Host");
    newAssignedNodeMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          hcb.createAssignedNode();
        }
      });
    newNodeMenu.add(newAssignedNodeMenuItem);
    fileMenu.add(newNodeMenu);
    commandLineMenuItem = 
      new JMenuItem(HostConfigurationBuilder.NODE_COMMAND_LINE_MENU_ITEM);
    commandLineMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          hcb.setNodeCommandLine();
        }
      });
    fileMenu.add(commandLineMenuItem);
    describeHostMenuItem = 
      new JMenuItem(HostConfigurationBuilder.DESCRIBE_HOST_MENU_ITEM);
    describeHostMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          hcb.setHostDescription();
        }
      });
    fileMenu.add(describeHostMenuItem);
    describeNodeMenuItem = 
      new JMenuItem(HostConfigurationBuilder.DESCRIBE_NODE_MENU_ITEM);
    describeNodeMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          hcb.setNodeDescription();
        }
      });
    fileMenu.add(describeNodeMenuItem);
    hostTypeMenuItem = 
      new JMenuItem(HostConfigurationBuilder.HOST_TYPE_MENU_ITEM);
    hostTypeMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          hcb.setHostType();
        }
      });
    fileMenu.add(hostTypeMenuItem);
    hostLocationMenuItem = 
      new JMenuItem(HostConfigurationBuilder.HOST_LOCATION_MENU_ITEM);
    hostLocationMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          hcb.setHostLocation();
        }
      });
    fileMenu.add(hostLocationMenuItem);
    deleteHostMenuItem = 
      new JMenuItem(HostConfigurationBuilder.DELETE_HOST_MENU_ITEM);
    deleteHostMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          hcb.deleteHost();
        }
      });
    fileMenu.add(deleteHostMenuItem);
    deleteNodeMenuItem = 
      new JMenuItem(HostConfigurationBuilder.DELETE_NODE_MENU_ITEM);
    deleteNodeMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          hcb.deleteNode();
        }
      });
    fileMenu.add(deleteNodeMenuItem);
    fileMenu.addSeparator();
    for (int i = 0; i < fileActions.length; i++) {
      fileMenu.add(fileActions[i]);
    }
    fileMenu.addMenuListener(myMenuListener);
    findMenu = new JMenu(FIND_MENU);
    findMenu.setToolTipText("Find a host, node, or agent.");
    for (int i = 0; i < findActions.length; i++) {
      findMenu.add(findActions[i]);
    }
    JMenu helpMenu = new JMenu(HELP_MENU);
    helpMenu.setToolTipText("Display documentation.");
    for (int i = 0; i < helpActions.length; i++) {
      helpMenu.add(helpActions[i]);
    }
    menuBar.add(fileMenu);
    menuBar.add(findMenu);
    menuBar.add(helpMenu);
    setJMenuBar(menuBar);

    tabbedPane = new JTabbedPane();
    tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (tabbedPane.getSelectedComponent().equals(hcb))
          findMenu.setEnabled(true);
        else
          findMenu.setEnabled(false);
      }
    });
    propertyBuilder = new UnboundPropertyBuilder(experiment, this);
    tabbedPane.add("Properties", propertyBuilder);
    hcb = 
      new HostConfigurationBuilder(experiment, this);
    tabbedPane.add("Configurations", hcb);
    // only display trial builder for non-database experiments
    if (experiment.isInDatabase()) {
      threadBuilder = new ThreadBuilder(experiment);
      tabbedPane.add("Threads", threadBuilder);
    } else {
      trialBuilder = new TrialBuilder(experiment);
      tabbedPane.add("Trials", trialBuilder);
    }
    // after starting all the editors, set experiment editability to false
    experiment.setEditInProgress(true);
    getContentPane().add(tabbedPane);
    pack();
    setSize(650, 400);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	exit();
      }
    });
    show();
  }

  private void setExperiment(Experiment newExperiment) {
    if (experiment != null) {
      experiment.removeModificationListener(this);
    }
    experiment = newExperiment;
    if (experiment != null) {
      experiment.addModificationListener(this);
    }
  }

  public void modified(ModificationEvent e) {
    setModified(true);
  }

  private void exit() {
    //    saveSilently(); // if experiment from database was modified, save it
    if (modified)
      saveAs();
    // before exiting, restore experiment's and society's editability
    // FIXME Restore on society!!!!
    experiment.setEditInProgress(false);
    // If the experiment now has a society and is otherwise runnable, say so
    if (experiment.getSocietyComponentCount() > 0)
      experiment.setRunnable(true);
  }

  /**
   * Set experiment to edit; used to re-use a running editor
   * to edit a different experiment.  Set the new experiment in all
   * the user interfaces (tabbed panes).
   * Silently save the previous experiment if necessary.
   */

  public void reinit(Experiment newExperiment) {
    //    saveSilently();
    if (modified)
      saveAs();
    // restore editable flag on previous experiment
    experiment.setEditInProgress(false);
    experiment = newExperiment;
    propertyBuilder.reinit(experiment);
    hcb.reinit(experiment);
    // only display trial builder for non-database experiments
    // only display thread builder for database experiments
    if (experiment.isInDatabase()) {
      if (trialBuilder != null) {
        tabbedPane.remove(trialBuilder);
        trialBuilder = null;
      }
      if (threadBuilder == null) {
        threadBuilder = new ThreadBuilder(experiment);
        tabbedPane.add("Threads", threadBuilder);
      } else
        threadBuilder.reinit(experiment);
    } else {
      if (trialBuilder == null) {
        trialBuilder = new TrialBuilder(experiment);
        tabbedPane.add("Trials", trialBuilder);
      } else
        trialBuilder.reinit(experiment);
      if (threadBuilder != null) {
        tabbedPane.remove(threadBuilder);
        threadBuilder = null;
      }
    }
    experiment.setEditInProgress(true);
  }

  /**
   * Called by HostConfigurationBuilder or UnboundPropertyBuilder
   * if they modify an experiment.
   */

  public void setModified(boolean modified) {
    this.modified = modified;
  }

  /**
   * If the experiment was from the database and 
   * components were either added or removed or
   * the host-node-agent mapping was modified, then save it,
   * otherwise display a dialog indicating that no modifications were made.
   */

  private void save() {
    if (!experiment.isInDatabase())
      return;
    if (!modified) {
      String[] msg = {
        "No modifications were made.",
        "Do you want to save this experiment anyway?"
      };
      int answer =
        JOptionPane.showConfirmDialog(this, msg,
                                      "No Modifications",
                                      JOptionPane.YES_NO_OPTION,
                                      JOptionPane.WARNING_MESSAGE);
      if (answer != JOptionPane.YES_OPTION) return;
      setModified(true);
    }
    saveHelper();
  }

  /**
   * Silently save experiment from database if modified.
   */

//    private void saveSilently() {
//      if (modified && experiment.isInDatabase())
//        saveHelper();
//    }

  private void saveAs() {
    if (!experiment.isInDatabase())
      return;
    if (!modified) 
      setModified(true);
    // get unique name in both database and CSMART or
    // reuse existing name
    if (ExperimentDB.isExperimentNameInDatabase(experiment.getShortName())) {
      String name = csmart.getUniqueExperimentName(experiment.getShortName(),
                                                   true);
      if (name == null)
        return;
      experiment.setName(name);
    }
    saveHelper();
  }

  // Dump out the ini files for the first trial to the local results directory
  private void dumpINIs() {
    experiment.dumpINIFiles();
  }
  
  private void saveHelper() {
    modified = false;
    final Component c = this;
    GUIUtils.timeConsumingTaskStart(c);
    GUIUtils.timeConsumingTaskStart(csmart);
    try {
      new Thread("Save") {
        public void run() {
          //          doSave();
          experiment.saveToDb(saveToDbConflictHandler);
          GUIUtils.timeConsumingTaskEnd(c);
          GUIUtils.timeConsumingTaskEnd(csmart);
        }
      }.start();
    } catch (RuntimeException re) {
      System.out.println("Error saving experiment: " + re);
      GUIUtils.timeConsumingTaskEnd(c);
    }
  }

  /**
   * Enable/disable entries in the File menu dependent on what
   * is selected in the organizer.
   */

  private MenuListener myMenuListener =
    new MenuListener() {
      public void menuCanceled(MenuEvent e) {
      }
      public void menuDeselected(MenuEvent e) {
      }
      public void menuSelected(MenuEvent e) {
        // if host configuration builder not selected
        // disable all the menu commands for it
        if (!tabbedPane.getSelectedComponent().equals(hcb)) {
          globalCommandLineMenuItem.setEnabled(false);
          newHostMenuItem.setEnabled(false);
          newNodeMenu.setEnabled(false);
          commandLineMenuItem.setEnabled(false);
          describeHostMenuItem.setEnabled(false);
          describeNodeMenuItem.setEnabled(false);
          hostTypeMenuItem.setEnabled(false);
          hostLocationMenuItem.setEnabled(false);
          deleteHostMenuItem.setEnabled(false);
          deleteNodeMenuItem.setEnabled(false);
          return;
        }
        // enable "global command line" command 
        // if either root, or any set of hosts or nodes is selected
        DefaultMutableTreeNode[] hostsInHostTree = 
          hcb.getSelectedHostsInHostTree();
        DefaultMutableTreeNode[] nodesInHostTree = 
          hcb.getSelectedNodesInHostTree();
        DefaultMutableTreeNode[] nodesInNodeTree = 
          hcb.getSelectedNodesInNodeTree();
        boolean isHostRootSelected = hcb.isHostTreeRootSelected();
        boolean isNodeRootSelected = hcb.isNodeTreeRootSelected();
        newHostMenuItem.setEnabled(isHostRootSelected);
        if (isHostRootSelected || isNodeRootSelected ||
            (hostsInHostTree != null) ||
            (nodesInHostTree != null) ||
            (nodesInNodeTree != null))
          globalCommandLineMenuItem.setEnabled(true);
        else
          globalCommandLineMenuItem.setEnabled(false);
        // enable "new node" command if unassigned nodes root is selected
        // or one host is selected in the host tree
        newUnassignedNodeMenuItem.setEnabled(isNodeRootSelected);
        if (hostsInHostTree != null && hostsInHostTree.length == 1) {
          newAssignedNodeMenuItem.setText("On " + 
                                          hostsInHostTree[0].toString());
          newAssignedNodeMenuItem.setEnabled(true);
        } else 
          newAssignedNodeMenuItem.setEnabled(false);
        newNodeMenu.setEnabled(newAssignedNodeMenuItem.isEnabled() ||
                               newUnassignedNodeMenuItem.isEnabled());
        // enable "new host" command if host tree root is selected
        newHostMenuItem.setEnabled(isHostRootSelected);
        // if a single node is selected
        // enable "command line arguments"
        int nodeCount = 0;
        if (nodesInHostTree != null)
          nodeCount = nodesInHostTree.length;
        if (nodesInNodeTree != null)
          nodeCount += nodesInNodeTree.length;
        commandLineMenuItem.setEnabled(nodeCount == 1);
        describeHostMenuItem.setEnabled(hostsInHostTree != null);
        describeNodeMenuItem.setEnabled(nodesInHostTree != null ||
                                        nodesInNodeTree != null);
        hostTypeMenuItem.setEnabled(hostsInHostTree != null);
        hostLocationMenuItem.setEnabled(hostsInHostTree != null);
        deleteHostMenuItem.setEnabled(hostsInHostTree != null);
        deleteNodeMenuItem.setEnabled(nodesInHostTree != null ||
                                      nodesInNodeTree != null);
      }
    };

}