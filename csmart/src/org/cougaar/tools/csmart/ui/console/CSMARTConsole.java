/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.console;

import org.cougaar.core.agent.AgentManager;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.core.plugin.PluginManager;
import org.cougaar.mlm.ui.glsinit.GLSClient;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.experiment.Trial;
import org.cougaar.tools.csmart.experiment.TrialResult;
import org.cougaar.tools.csmart.recipe.MetricComponent;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.experiment.HostConfigurationBuilder;
import org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL;
import org.cougaar.tools.csmart.ui.tree.ConsoleTreeObject;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.util.ResultsFileFilter;
import org.cougaar.tools.server.OutputListener;
import org.cougaar.tools.server.OutputPolicy;
import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteFileSystem;
import org.cougaar.tools.server.RemoteHost;
import org.cougaar.tools.server.RemoteListenable;
import org.cougaar.tools.server.RemoteListenableConfig;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.util.Parameters;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The Console or Experiment Controller is the tool for
 * starting, stopping, and monitoring a running Cougaar society.
 *
 * @property org.cougaar.tools.csmart.startdelay is the CSMART startup argument indicating the time
 *        to pause between Node creation invocations (defaults to none).
 **/
public class CSMARTConsole extends JFrame {
  private static final String DEFAULT_BOOTSTRAP_CLASS = "org.cougaar.bootstrap.BootStrapper";
  private static final String DEFAULT_NODE_CLASS = "org.cougaar.core.node.Node";
  public static final String COMMAND_ARGUMENTS = "Command$Arguments";
  private static final String[] emptyStringArray = {};

  // Need window titles to find the windows later
  private static final String configWindowTitle = "Configuration";
  private static final String glsWindowTitle = "GLS";

  // number of characters displayed in the node output window
  private static final int DEFAULT_VIEW_SIZE = 300000; // 60 pages of text or 300K

  private String GLS_PROTOCOL = "http"; // default protocol for GLSClient
  private String GLS_SECURE_PROTOCOL = "https";

  // Servlet to look for in initializing GLS window
  private static final String GLS_SERVLET = "org.cougaar.mlm.plugin.organization.GLSInitServlet";
  private CSMART csmart;
  private HostConfigurationBuilder hostConfiguration = null;
  private SocietyComponent societyComponent;
  private Experiment experiment;
  private long startExperimentTime;
  private DecimalFormat myNumberFormat;
  private javax.swing.Timer experimentTimer;
  private boolean stopping = false; // user is stopping the experiment
  private Hashtable runningNodes; // maps node names to RemoteProcesses
  private Object runningNodesLock = new Object();
  private ArrayList oldNodes; // names of nodes to destroy before running again
  private Hashtable nodeListeners; // map node name to ConsoleNodeListener
  private Hashtable nodePanes;     // map node name to ConsoleTextPane
  private Hashtable nodeToNodeInfo; // map node name to NodeInfo
  private String notifyCondition = "exception";
  private boolean notifyOnStandardError = false; // if stderr appears, notify user
  private int viewSize = DEFAULT_VIEW_SIZE; // number of characters in node view
  private ConsoleNodeOutputFilter displayFilter;
  private Date runStart = null;
  private ConsoleDesktop desktop;
  private String selectedNodeName; // node whose status lamp is selected
  private Legend legend; // the node status lamp legend
  private CSMARTConsole console;
  private GLSClient glsClient = null;
  private transient Logger log;
  private AppServerSupport appServerSupport;
  private TimerTask monitorAppServerTask;
  // set this flag when you first run an experiment
  // it's purpose is to ignore a non-null experiment if you're only
  // attaching to nodes
  private boolean usingExperiment = false;

  // gui controls
  ButtonGroup statusButtons;
  JToggleButton attachButton;
  JMenuItem attachMenuItem; // same as attachButton
  JMenuItem addGLSMenuItem; // call addGLSWindow
  JToggleButton runButton;
  JToggleButton stopButton;
  JPanel buttonPanel; // contains status buttons
  JPopupMenu nodeMenu; // pop-up menu on node status button


  JMenuItem deleteMenuItem;
  JMenuItem displayMenuItem;
  JMenuItem killAllMenuItem;

  private static Dimension HGAP10 = new Dimension(10, 1);
  private static Dimension HGAP5 = new Dimension(5, 1);
  private static Dimension VGAP30 = new Dimension(1, 30);

  // top level menus and menu items
  private static final String FILE_MENU = "File";
  private static final String EXIT_MENU_ITEM = "Close";
  private static final String VIEW_MENU = "View";
  private static final String SHOW_LOG_MENU_ITEM = "Show Entire Log";
  private static final String SET_VIEW_SIZE_MENU_ITEM = "Set View Size...";
  private static final String FILTER_MENU_ITEM = "Filter...";
  private static final String FORMAT_MENU_ITEM = "Format...";
  private static final String NOTIFY_MENU = "Notify";
  private static final String SET_NOTIFY_MENU_ITEM = "Set Notification...";
  private static final String VIEW_NOTIFY_MENU_ITEM = "View Notification";
  private static final String REMOVE_NOTIFY_MENU_ITEM = "Remove All Notifications";
  private static final String RESET_NOTIFY_MENU_ITEM = "Reset All Notifications";
  private static final String FIND_MENU = "Find";
  private static final String FIND_HOST_MENU_ITEM = "Find Host...";
  private static final String FIND_NODE_MENU_ITEM = "Find Node...";
  private static final String FIND_AGENT_MENU_ITEM = "Find Agent...";
  private static final String APP_SERVER_MENU = "Application Server";
  private static final String VIEW_APP_SERVER_ITEM = "View";
  private static final String ADD_APP_SERVER_ITEM = "Add...";
  private static final String DELETE_APP_SERVER_ITEM = "Delete...";
  private static final String ATTACH_AS_ITEM = "Attach...";
  private static final String KILL_ALL_PROCS_ITEM = "Kill Any Nodes";
  private static final String REFRESH_APP_SERVER_ITEM = "Refresh";
  private static final String SET_POLL_INTERVAL_ITEM = "Set Poll Interval";
  private static final String ADD_GLS_ITEM = "Add GLS Client";
  private static final String HELP_MENU = "Help";
  private static final String ABOUT_CONSOLE_ITEM = "About Experiment Controller";
  private static final String ABOUT_CSMART_ITEM = "About CSMART";
  private static final String LEGEND_MENU_ITEM = "Node Status Legend";
  protected static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";
  private static final String HELP_DOC = "help.html";

  // for pop-up menu on node status buttons
  private static final String ABOUT_ACTION = "Info";
  private static final String RESET_ACTION = "Reset Notification";

  // used for log file name
  private static DateFormat fileDateFormat =
      new SimpleDateFormat("yyyyMMddHHmmss");
  private static DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

  private static final int MSECS_PER_SECOND = 1000;
  private static final int MSECS_PER_MINUTE = 60000;
  private static final int MSECS_PER_HOUR = 3600000;

  private Thread nodeCreator;
  private volatile boolean stopNodeCreation;
  private volatile boolean starting; // true while we're creating nodes
  private ArrayList nodeCreationInfoList;

  // Time in milliseconds between looking for new Nodes to attach to.
  // Note that the currently selected value stays constant
  // For a given invocation of CSMART
  private static int asPollInterval = 30000;
  private transient volatile java.util.Timer asPollTimer = null;

  // Boolean checked by main CSMART UI in WindowListener to see
  // if user canceled the Exit
  public boolean dontClose = false;

  /**
   * Create and show console GUI.
   * Experiment may be null.
   * @param csmart the CSMART viewer
   * @param experiment experiment to run, may be null
   */
  public CSMARTConsole(CSMART csmart, Experiment experiment) {
    this.csmart = csmart;
    this.experiment = experiment;
    createLogger();
    console = this;
    runningNodes = new Hashtable();
    oldNodes = new ArrayList();
    nodeListeners = new Hashtable();
    nodePanes = new Hashtable();
    nodeToNodeInfo = new Hashtable();
    //    appServerSupport = new AppServerSupport();
    // appServerSupport usage is now being done through CSMARTConsoleModel
    appServerSupport = null;
    if (experiment != null)
      getAppServersFromExperiment();
    initGui();

    // Start up the Timer to poll for new AppServers
    resetASPoller();

    // FIXME: CSMART Could, optionally, kill everything running
    // when it starts up, with this call
    //    appServerSupport.killAllProcesses();
  }

  // Cancel any old AppServer poller
  // Then, if the currently desired interval is not 0,
  // Start up a new Timer to poll for new AppServers ever x milliseconds
  private void resetASPoller() {
    if (asPollTimer != null) {
      if (log.isDebugEnabled()) {
        log.debug("Canceling old ASPoller timer");
      }
      asPollTimer.cancel();
      monitorAppServerTask.cancel();
    }

    if (asPollInterval != 0) {
      if (log.isDebugEnabled()) {
        log.debug("creating new ASPoller with interval " + asPollInterval);
      }

      // contact known app servers periodically to get lists of their nodes
      // display dialog when new nodes are first detected
      monitorAppServerTask = new TimerTask() {
        public void run() {
          if (appServerSupport.haveNewNodes())
            JOptionPane.showMessageDialog(null, "There are new nodes!");
          // If there are no AppServers,
          // disable the View and Delete and Kill All menu items
          // and the attach Button
          updateASControls();
          noticeIfServerDead();
        }
      };

      asPollTimer = new java.util.Timer();
      asPollTimer.schedule(monitorAppServerTask, new Date(), asPollInterval);
    }
  }

  // Go through running nodes. If that Node's AppServer is dead,
  // then mark it dead
  private void noticeIfServerDead() {
    Enumeration nodeNames;
    synchronized (runningNodesLock) {
      nodeNames = runningNodes.keys();
    }

    while (nodeNames.hasMoreElements()) {
      String nodeName = (String) nodeNames.nextElement();
      NodeInfo ni = (NodeInfo) nodeToNodeInfo.get(nodeName);
      if (ni == null)
        continue;
      if (ni.getAppServer() == null) {
        log.warn("Lost contact with AppServer on " + ni.getHostName() + " for node " + nodeName + " (null RemoteHost in NodeInfo). Assuming it is dead.");
        markNodeDead(nodeName);
        continue;
      }
      if (!appServerSupport.isValidRemoteHost(ni.getAppServer())) {
        if (log.isWarnEnabled())
          log.warn("Lost contact with AppServer on " + ni.getHostName() + " for node " + nodeName + " (Marked as not valid remote host). Assuming it is dead.");
        // Note - this could be a timeout, so don't do it
        // so that it's not reversable
        markNodeDead(nodeName, false);
        continue;
      }

      RemoteProcess rp = null;
      synchronized (runningNodesLock) {
        rp = (RemoteProcess) runningNodes.get(nodeName);
      }

      if (rp == null) {
        if (!stopping) {
          if (log.isWarnEnabled())
            log.warn("Remote process suddenly null for " + nodeName + ". Assuming it is dead.");
          markNodeDead(nodeName);
        } else {
          if (log.isInfoEnabled())
            log.info("Remote process suddenly null for " + nodeName + ", but we're in process of stopping, so its OK.");
        }
        continue;
      }

      try {
        if (!rp.isAlive()) {
          // FIXME: see if it's now not in runningNodes?
          // If so, a timing issue, and someone killed it...
          RemoteProcess rp2 = null;
          synchronized (runningNodesLock) {
            rp2 = (RemoteProcess) runningNodes.get(nodeName);
          }

          // FIXME: This doesnt seem to help.
          // Need to be able to look up which Nodes
          // are in process of being stopped,
          // and skip those?

          // OK: We'll check the stopping flag: If set,
          // we're in the process of stopping the Nodes,
          // so it's OK if the Node claims to not be Alive.

          if (rp2 == null || stopping) {
            if (log.isInfoEnabled())
              log.info("Remote Process must have just been killed by someone else, so it's OK - no need to mark it dead.");
          } else {
            if (log.isWarnEnabled())
              log.warn("Remote Process for " + nodeName + " says it is not alive. Marking it dead.");
            markNodeDead(nodeName);
          }
          continue;
        }
      } catch (Exception e) {
        // Todd W says this should never happen
        if (log.isWarnEnabled())
          log.warn("Got exception trying to ask remote process for " + nodeName + " if it is alive. Marking it dead.", e);
        markNodeDead(nodeName, false);
        continue;
      }
    }
  }

  // Mark a node as unexpectedly dead
  private void markNodeDead(String nodeName) {
    markNodeDead(nodeName, true);
  }

  private void markNodeDead(String nodeName, boolean completely) {
    if (log.isDebugEnabled())
      log.debug("markNodeDead for " + nodeName + " doing it " + (completely ? "completely." : "partially."));

    // if there's a thread still creating nodes, stop it
    // FIXME: Really? Just cause one node had problems, we give up
    // creating all the Nodes?
    // Or do I just want to wait till all the Nodes have been created?
    stopNodeCreation = true;
    // wait for the creating thread to stop
    // note that this means blocking on that thread which waits on RMI
    // So if this method is called from the AWT thread, that's probably bad.
    // Note though that nodeCreator will try to bail out quickly if
    // stopNodeCreation is true. So unless we're in the process of doing the RMI
    // thing, this shouldn't be too bad.
    if (nodeCreator != null) {
      try {
        nodeCreator.join();
      } catch (InterruptedException ie) {
        if (log.isErrorEnabled()) {
          log.error("Exception waiting for node creation thread to die: ", ie);
        }
      }
    }

    // This is destructive - do carefully
    if (completely) {
      RemoteProcess bremoteNode = null;
      synchronized (runningNodesLock) {
        bremoteNode = (RemoteProcess) runningNodes.get(nodeName);
        if (bremoteNode != null) {
          oldNodes.add(nodeName);
          runningNodes.remove(nodeName);
        }
      } // end synchronized

      nodeStopped(nodeName);
    }

    NodeStatusButton but = getNodeStatusButton(nodeName);
    if (but != null)
      but.getMyModel().setStatus(NodeStatusButton.STATUS_NO_ANSWER);

    ConsoleInternalFrame frame =
        desktop.getNodeFrame(nodeName);
    if (frame != null) {
      if (log.isDebugEnabled())
        log.debug("markNodeDead disabling restart for node " + nodeName);
      frame.enableRestart(false);
    }
  }

  // Get from the user the new interval in milliseconds
  // between polls for new AppServers to contact
  // Return the newly desired value.
  // Note that the new value is _not_ put in the static variable -
  // the caller must do that
  private int getNewASPollInterval() {
    if (log.isDebugEnabled()) {
      log.debug("Getting new ASPoll Interval");
    }
    JPanel pollPanel = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    pollPanel.add(new JLabel("Interval in milliseconds between polls for live AppServers (0 to not poll):"),
                  new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                         GridBagConstraints.WEST,
                                         GridBagConstraints.NONE,
                                         new Insets(10, 0, 5, 5),
                                         0, 0));
    JTextField pollField =
        new JTextField(7);
    pollField.setText(String.valueOf(asPollInterval));
    pollPanel.add(pollField,
                  new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                         GridBagConstraints.WEST,
                                         GridBagConstraints.HORIZONTAL,
                                         new Insets(10, 0, 5, 0),
                                         0, 0));
    x = 0;
    int result = JOptionPane.showConfirmDialog(this, pollPanel,
                                               "Polling Interval",
                                               JOptionPane.OK_CANCEL_OPTION);
    if (result != JOptionPane.OK_OPTION)
      return asPollInterval;
    String s = pollField.getText().trim();

    if (s == null || s.length() == 0) {
      return 0;
    } else {
      int res = asPollInterval;
      try {
        res = Integer.parseInt(s);
      } catch (NumberFormatException e) {
      }
      if (res < 0)
        return asPollInterval;
      return res;
    }
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  private void updateASControls() {
    if (appServerSupport.haveValidAppServers()) {

      displayMenuItem.setEnabled(true);
      deleteMenuItem.setEnabled(true);
      if (appServerSupport.thereAreRunningNodes()) {
        if (glsClient == null)
          addGLSMenuItem.setEnabled(true);
        else {
          // Bug 2258 workaround
          //	  addGLSMenuItem.setEnabled(false);
          if (log.isInfoEnabled())
            log.info("Bug 2258 workaround. Have Valid AppServers & running nodes. But glsClient is non-null. Enabling menu item anyhow.");
          addGLSMenuItem.setEnabled(true);
        }

        killAllMenuItem.setEnabled(true);
        attachButton.setEnabled(true);
        attachMenuItem.setEnabled(true);
      } else {
        // Bug 2258 workaround
        //	addGLSMenuItem.setEnabled(false);
        if (log.isInfoEnabled())
          log.info("Bug 2258 workaround. Have valid AppServers but no running nodes. Enabling menu item anyhow. (BTW, glsClient " + ((glsClient == null) ? "is null" : "is non null") + ")");
        addGLSMenuItem.setEnabled(true);
        attachButton.setEnabled(false);
        attachMenuItem.setEnabled(false);
        killAllMenuItem.setEnabled(false);
      }
    } else {
      attachButton.setEnabled(false);
      attachMenuItem.setEnabled(false);
      displayMenuItem.setEnabled(false);
      deleteMenuItem.setEnabled(false);
      killAllMenuItem.setEnabled(false);
      // Bug 2258 work-around
      //      addGLSMenuItem.setEnabled(false);
      if (log.isInfoEnabled())
        log.info("Bug 2258 workaround. Do not have valid appservers. (BTW, glsClient " + ((glsClient == null) ? "is null" : "is non null") + ")");
      addGLSMenuItem.setEnabled(true);
    }
  }

  private void initGui() {
    String description = "";
    if (experiment != null) {
      experiment.setRunInProgress(true);
      experiment.setResultDirectory(csmart.getResultDir());
      description = experiment.getSocietyComponent().getSocietyName();
    }
    // top level menus
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.setToolTipText("Exit this tool.");
    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.setToolTipText("Exit this tool.");
    exitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        exitMenuItem_actionPerformed(e);
      }
    });
    fileMenu.add(exitMenuItem);

    JMenu viewMenu = new JMenu(VIEW_MENU);
    JMenuItem viewMenuItem = new JMenuItem(SHOW_LOG_MENU_ITEM);
    viewMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      }
    });
    viewMenuItem.setEnabled(false); // disabled cause of Swing error
    viewMenu.add(viewMenuItem);
    JMenuItem viewSizeMenuItem = new JMenuItem(SET_VIEW_SIZE_MENU_ITEM);
    viewSizeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        viewSizeMenuItem_actionPerformed();
      }
    });
    viewMenu.add(viewSizeMenuItem);
    viewMenu.addSeparator();
    JMenuItem filterMenuItem = new JMenuItem(FILTER_MENU_ITEM);
    filterMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        filterMenuItem_actionPerformed();
      }
    });
    viewMenu.add(filterMenuItem);
    JMenuItem formatMenuItem = new JMenuItem(FORMAT_MENU_ITEM);
    formatMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        formatMenuItem_actionPerformed();
      }
    });
    formatMenuItem.setEnabled(false);
    viewMenu.add(formatMenuItem);

    JMenu findMenu = new JMenu(FIND_MENU);
    JMenuItem findHostMenuItem = new JMenuItem(FIND_HOST_MENU_ITEM);
    findHostMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hostConfiguration.findHost();
      }
    });
    findMenu.add(findHostMenuItem);
    JMenuItem findNodeMenuItem = new JMenuItem(FIND_NODE_MENU_ITEM);
    findNodeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hostConfiguration.findNode();
      }
    });
    findMenu.add(findNodeMenuItem);
    JMenuItem findAgentMenuItem = new JMenuItem(FIND_AGENT_MENU_ITEM);
    findAgentMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hostConfiguration.findAgent();
      }
    });
    findMenu.add(findAgentMenuItem);
    if (experiment == null) {
      findHostMenuItem.setEnabled(false);
      findNodeMenuItem.setEnabled(false);
      findAgentMenuItem.setEnabled(false);
    }

    JMenu notifyMenu = new JMenu(NOTIFY_MENU);
    JMenuItem setNotifyMenuItem = new JMenuItem(SET_NOTIFY_MENU_ITEM);
    setNotifyMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setNotifyMenuItem_actionPerformed();
      }
    });
    notifyMenu.add(setNotifyMenuItem);
    JMenuItem viewNotifyMenuItem = new JMenuItem(VIEW_NOTIFY_MENU_ITEM);
    viewNotifyMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        viewNotifyMenuItem_actionPerformed();
      }
    });
    notifyMenu.add(viewNotifyMenuItem);
    JMenuItem removeNotifyMenuItem = new JMenuItem(REMOVE_NOTIFY_MENU_ITEM);
    removeNotifyMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeNotifyMenuItem_actionPerformed();
      }
    });
    notifyMenu.add(removeNotifyMenuItem);
    notifyMenu.addSeparator();
    JMenuItem resetNotifyMenuItem = new JMenuItem(RESET_NOTIFY_MENU_ITEM);
    resetNotifyMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetNotifyMenuItem_actionPerformed();
      }
    });
    notifyMenu.add(resetNotifyMenuItem);

    JMenu appServerMenu = new JMenu(APP_SERVER_MENU);
    appServerMenu.setToolTipText("Display, add, and delete list of Application Servers.");

    displayMenuItem = new JMenuItem(VIEW_APP_SERVER_ITEM);
    displayMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //        appServerSupport.displayAppServers();
      }
    });
    displayMenuItem.setToolTipText("Display list of Application Servers.");
    appServerMenu.add(displayMenuItem);

    JMenuItem addMenuItem = new JMenuItem(ADD_APP_SERVER_ITEM);
    addMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //        appServerSupport.addAppServer();
        updateASControls();
      }
    });
    addMenuItem.setToolTipText("Add an Application Server.");
    appServerMenu.add(addMenuItem);

    deleteMenuItem = new JMenuItem(DELETE_APP_SERVER_ITEM);
    deleteMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //        appServerSupport.deleteAppServers();
      }
    });
    deleteMenuItem.setToolTipText("Ignore Application Servers.");
    appServerMenu.add(deleteMenuItem);

    attachMenuItem = new JMenuItem(ATTACH_AS_ITEM);
    attachMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        attachButton_actionPerformed();
      }
    });
    attachMenuItem.setToolTipText("Attach to any new Running Nodes.");
    appServerMenu.add(attachMenuItem);

    killAllMenuItem = new JMenuItem(KILL_ALL_PROCS_ITEM);
    killAllMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int result = JOptionPane.showConfirmDialog(CSMARTConsole.this, "Really kill all running Nodes on all known AppServers?",
                                                   "Kill All Nodes",
                                                   JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION)
          return;

        if (log.isDebugEnabled())
          log.debug("Killing all Nodes!");

        // First, kill anything this console knows about
        if (stopButton != null && stopButton.isEnabled())
          doStop();
        // Then kill anything remaining
        appServerSupport.killAllProcesses();
      }
    });
    killAllMenuItem.setToolTipText("Kill Any Nodes on known App Servers.");
    appServerMenu.add(killAllMenuItem);

    JMenuItem refreshMenuItem = new JMenuItem(REFRESH_APP_SERVER_ITEM);
    refreshMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //        appServerSupport.refreshAppServers();
        //        updateASControls();
        // test the AS
        //        noticeIfServerDead();
      }
    });
    refreshMenuItem.setToolTipText("Refresh list of Application Servers");
    appServerMenu.add(refreshMenuItem);

    JMenuItem pollIntervalMenuItem = new JMenuItem(SET_POLL_INTERVAL_ITEM);
    pollIntervalMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Put up a dialog with the current interval.
        // If the user changes the interval, cancel the current timer
        // and create a new one
        int res = getNewASPollInterval();
        if (res != asPollInterval) {
          asPollInterval = res;
          resetASPoller();
        }
      }
    });
    pollIntervalMenuItem.setToolTipText("Change Delay Between Checking for New Application Servers");
    appServerMenu.add(pollIntervalMenuItem);

    // Menu item for popping up a new GLS Client
    addGLSMenuItem = new JMenuItem(ADD_GLS_ITEM);
    addGLSMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addGLSWindow(findServlet());
      }
    });
    addGLSMenuItem.setToolTipText("Add new GLS Client for sending GLS Init");
    appServerMenu.add(addGLSMenuItem);

    JMenu helpMenu = new JMenu(HELP_MENU);
    JMenuItem helpMenuItem = new JMenuItem(ABOUT_CONSOLE_ITEM);
    helpMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        URL help = (URL) this.getClass().getResource(HELP_DOC);
        if (help != null)
          Browser.setPage(help);
      }
    });
    helpMenu.add(helpMenuItem);

    legend = new Legend();
    JMenuItem legendMenuItem = new JMenuItem(LEGEND_MENU_ITEM);
    legendMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        legend.setVisible(true);
      }
    });
    helpMenu.add(legendMenuItem);

    JMenuItem aboutMenuItem = new JMenuItem(ABOUT_CSMART_ITEM);
    aboutMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        URL about = (URL) this.getClass().getResource(ABOUT_DOC);
        if (about != null)
          Browser.setPage(about);
      }
    });
    helpMenu.add(aboutMenuItem);

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(viewMenu);
    menuBar.add(findMenu);
    menuBar.add(notifyMenu);
    menuBar.add(appServerMenu);
    menuBar.add(helpMenu);
    getRootPane().setJMenuBar(menuBar);

    // create panel which contains
    // description panel, status button panel, and tabbed panes
    JPanel panel = new JPanel(new GridBagLayout());

    // descriptionPanel contains society name, control buttons
    JPanel descriptionPanel = createHorizontalPanel(true);
    descriptionPanel.add(Box.createRigidArea(VGAP30));
    descriptionPanel.add(Box.createRigidArea(HGAP10));
    descriptionPanel.add(new JLabel(description));
    descriptionPanel.add(Box.createRigidArea(HGAP5));

    attachButton = new JToggleButton("Attach");
    attachButton.setToolTipText("Attach to running nodes");
    attachButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        attachButton_actionPerformed();
      }
    });
    attachButton.setFocusPainted(false);
    attachButton.setEnabled(true);
    descriptionPanel.add(attachButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));
    runButton = new JToggleButton("Run");
    runButton.setToolTipText("Start running experiment");
    runButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        runButton_actionPerformed();
      }
    });
    runButton.setFocusPainted(false);
    descriptionPanel.add(runButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));

    stopButton = new JToggleButton("Stop");
    stopButton.setToolTipText("Halt experiment at end of current");
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stopButton_actionPerformed(e);
      }
    });
    stopButton.setFocusPainted(false);
    descriptionPanel.add(stopButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));


    // create progress panel for time labels
    // these are referenced elsewhere, so are created even if not displayed
    JPanel runProgressPanel = createHorizontalPanel(false);
    final JLabel experimentTimeLabel = new JLabel("Experiment: 00:00:00");
    myNumberFormat = new DecimalFormat("00");
    JPanel progressPanel = new JPanel(new GridBagLayout());
    runProgressPanel.add(experimentTimeLabel);
    progressPanel.add(runProgressPanel,
                      new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0),
                                             0, 0));

    descriptionPanel.add(Box.createRigidArea(HGAP10));
    descriptionPanel.add(progressPanel);
    descriptionPanel.add(Box.createRigidArea(HGAP10));
    // add description panel to top panel
    panel.add(descriptionPanel,
              new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.HORIZONTAL,
                                     new Insets(0, 10, 2, 10),
                                     0, 0));

    experimentTimer =
        new javax.swing.Timer(1000, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            experimentTimeLabel.setText(getElapsedTimeLabel("Experiment: ",
                                                            startExperimentTime));
          }
        });
    // create status button panel, initially with no buttons
    buttonPanel = createHorizontalPanel(true);
    buttonPanel.add(Box.createRigidArea(HGAP10));
    buttonPanel.add(new JLabel("Node Status"));
    buttonPanel.add(Box.createRigidArea(HGAP10));
    buttonPanel.add(Box.createRigidArea(VGAP30));
    JScrollPane jsp = new JScrollPane(buttonPanel);
    // ensure the layout leaves space for the scrollbar
    jsp.setMinimumSize(new Dimension(100, 50));
    panel.add(jsp,
              new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.HORIZONTAL,
                                     new Insets(0, 10, 0, 10),
                                     0, 0));

    statusButtons = new ButtonGroup();
    nodeMenu = new JPopupMenu();
    Action aboutAction = new AbstractAction(ABOUT_ACTION) {
      public void actionPerformed(ActionEvent e) {
        displayAboutNode();
      }
    };
    nodeMenu.add(aboutAction);
    Action resetAction = new AbstractAction(RESET_ACTION) {
      public void actionPerformed(ActionEvent e) {
        resetNodeStatus();
      }
    };
    Action legendAction = new AbstractAction(LEGEND_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
        legend.setVisible(true);
      }
    };
    nodeMenu.add(resetAction);
    nodeMenu.addSeparator();
    nodeMenu.add(legendAction);

    desktop = new ConsoleDesktop();
    // create tabbed panes for configuration information (not editable)
    if (experiment != null) {
      hostConfiguration = new HostConfigurationBuilder(experiment, null);
      hostConfiguration.update(); // display configuration
      hostConfiguration.addHostTreeSelectionListener(myTreeListener);
      JInternalFrame jif = new JInternalFrame(configWindowTitle,
                                              true, false, true, true);
      jif.getContentPane().add(hostConfiguration);
      jif.setSize(660, 400);
      jif.setLocation(0, 0);
      jif.setVisible(true);
      desktop.add(jif, JLayeredPane.DEFAULT_LAYER);
    }
    panel.add(desktop,
              new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.BOTH,
                                     new Insets(0, 0, 0, 0),
                                     0, 0));

    getContentPane().add(panel);

    // enable run button if have experiment with at least one host, node,
    // and agent
    initRunButton();
    stopButton.setEnabled(false);


    updateASControls();

    // add a WindowListener: Do an exit to kill the Nodes
    // If this window is closing
    // Note that the main CSMART UI handles actually disposing
    // this frame
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        exitMenuItem_actionPerformed(e);
      }
    });

    pack();
    setSize(700, 600);
    setVisible(true);
  } // end initGui

  /**
   * Create a panel whose components are layed out horizontally.
   */
  private JPanel createHorizontalPanel(boolean makeBorder) {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.setAlignmentY(TOP_ALIGNMENT);
    p.setAlignmentX(LEFT_ALIGNMENT);
    if (makeBorder)
      p.setBorder(LineBorder.createGrayLineBorder());
    return p;
  }

  /**
   * Create a button representing a node.
   */
  private NodeStatusButton createStatusButton(String nodeName, String hostName) {
    NodeStatusButton button =
        new NodeStatusButton(new ColoredCircle(NodeStatusButton.unknownStatus, 20, null));
    button.setSelectedIcon(new SelectedColoredCircle(NodeStatusButton.unknownStatus, 20, null));
    button.setToolTipText("Node " + nodeName + " (" + hostName + "), unknown");
    button.setActionCommand(nodeName);
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setMargin(new Insets(2, 2, 2, 2));
    return button;
  }

  /**
   * Add status button to status button display.
   */
  private void addStatusButton(JRadioButton button) {
    statusButtons.add(button);
    button.addMouseListener(myMouseListener);
    buttonPanel.add(button);
  }

  /**
   * Display pop-up menu with "about" menu item, which provides
   * the same functionality as the "about" menu item in the node window,
   * but from the node status lamp.
   */

  private void doPopup(MouseEvent e) {
    selectedNodeName = ((JRadioButton) e.getSource()).getActionCommand();
    nodeMenu.show((Component) e.getSource(), e.getX(), e.getY());
  }

  private void displayAboutNode() {
    ConsoleInternalFrame frame = desktop.getNodeFrame(selectedNodeName);
    if (frame != null)
      frame.displayAbout();
  }

  private void displayNodeFrame(String nodeName) {
    JInternalFrame frame = desktop.getNodeFrame(nodeName);
    if (frame == null)
      return; // frame not created yet
    try {
      frame.setIcon(false);
      frame.setSelected(true);
    } catch (PropertyVetoException exc) {
    }
  }

  private NodeStatusButton getNodeStatusButton(String nodeName) {
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      NodeStatusButton button = (NodeStatusButton) buttons.nextElement();
      if (button.getActionCommand().equals(nodeName))
        return button;
    }
    return null;
  }

  /**
   * Clears the error in the console node listener so it
   * updates the status button again.
   * Clears the notify position in the console text pane.
   */

  private void resetNodeStatus() {
    ConsoleTextPane consoleTextPane =
        (ConsoleTextPane) nodePanes.get(selectedNodeName);
    if (consoleTextPane == null)
      return;
    consoleTextPane.clearNotify();
    NodeStatusButton button = getNodeStatusButton(selectedNodeName);
    if (button != null)
      button.getMyModel().clearError();
  }

  /**
   * Listener on the node status buttons.
   * Right click pops-up a menu with the "About" node menu item.
   * Left click opens the node standard out frame,
   * and highlights the node in the configuration tree.
   */
  private MouseListener myMouseListener = new MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
      if (e.isPopupTrigger())
        doPopup(e);
      else {
        String nodeName = ((JRadioButton) e.getSource()).getActionCommand();
        displayNodeFrame(nodeName);
        selectNodeInHostTree(nodeName);
      }
    }

    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger()) doPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) doPopup(e);
    }
  };

  /**
   * Enable run button if experiment has at least one host that has at least
   * one node to run which has at least one agent to run.
   * Called from initialization.
   */
  private void initRunButton() {
    if (experiment != null) {
      HostComponent[] hosts = experiment.getHostComponents();
      for (int i = 0; i < hosts.length; i++) {
        NodeComponent[] nodes = hosts[i].getNodes();
        if (nodes != null && nodes.length > 0) {
          // Bug 1763: Perhaps allow running a society with just Nodes, no Agents?
          for (int j = 0; j < nodes.length; j++) {
            AgentComponent[] agents = nodes[j].getAgents();
            if (agents != null && agents.length > 0) {
              runButton.setEnabled(true);
              return;
            }
          }
        }
      }
    }
    runButton.setEnabled(false);
  }

  // End of basic UI setup stuff
  /////////////////////////////////////////////////


  /**
   * User selected "Run" button.
   * Start each node
   * and create a status button and
   * tabbed pane for it.
   */
  private void runButton_actionPerformed() {
    if (log.isDebugEnabled()) {
      log.debug("Hit run button");
    }
    destroyOldNodes(); // Get rid of any old stuff before creating the new
    if (experiment != null) {
      usingExperiment = true;
      initNodesFromExperiment();
    }
    runTrial();
  }

  /**
   * If the console was invoked with a non-null experiment
   * then extract NodeInfo from its nodes and save it
   * in the nodeToNodeInfo hashtable which is used to run the nodes.
   */
  private void initNodesFromExperiment() {
    HostComponent[] hostsToRunOn = experiment.getHostComponents();
    for (int i = 0; i < hostsToRunOn.length; i++) {
      String hostName = hostsToRunOn[i].getShortName();
      NodeComponent[] nodesToRun = hostsToRunOn[i].getNodes();
      for (int j = 0; j < nodesToRun.length; j++) {
        NodeComponent nodeComponent = nodesToRun[j];
        String nodeName = nodeComponent.getShortName();

        // get arguments from NodeComponent and pass them to ApplicationServer
        // note that these properties augment any properties that
        // are passed to the server in a properties file on startup
        Properties properties = getNodeMinusD(nodeComponent, hostName);
        java.util.List args = getNodeArguments(nodeComponent);

        if (experiment.getTrialID() != null) {
          properties.setProperty(Experiment.EXPERIMENT_ID,
                                 experiment.getTrialID());
        } else {
          log.error("Null trial ID for experiment!");
        }
        // get the app server to use
        RemoteHost appServer =
            appServerSupport.addAppServerForExperiment(hostName, properties);
        if (appServer == null)
          continue;

        //        nodeToNodeInfo.put(nodeName,
        //                           new NodeInfo(appServer,
        //                                        nodeName, hostName, "",
        //                                        properties, args));
        // if not running from database, need to write config files
        // writeConfigFiles()
      }
    }
  }

  // for experiments that are not run from a database
  // needs to be defined to be called from initNodesFromExperiment
//    private void writeConfigFiles() {
//      Iterator fileIter = experiment.getConfigFiles(nodesToRun);
//      RemoteFileSystem remoteFS;
//      try {
//        remoteFS = remoteAppServer.getRemoteFileSystem();
//      } catch (Exception e) {
//        if(log.isErrorEnabled()) {
//          log.error("CSMARTConsole: unable to access app-server file system on " +
//                    hostName + " : " + appServerPort, e);
//        }
//        JOptionPane.showMessageDialog(this,
//                                      "Unable to access app-server file system on " +
//                                      hostName + " : " + appServerPort +
//                                      "; check that server is running");
//        continue;
//      }

//      while(fileIter.hasNext()) {
//        String filename = (String)fileIter.next();
//        OutputStream out = null;
//        try {
//          out = remoteFS.write(filename);
//          experiment.writeContents(filename, out);
//        } catch(Exception e) {
//          if(log.isErrorEnabled()) {
//            log.error("Caught an Exception writing leaf on " +
//                      hostName + " : " + appServerPort, e);
//          }
//        } finally {
//          try {
//            out.close();
//          } catch(Exception e) {
//            if(log.isErrorEnabled()) {
//              log.error("Caught exception closing stream", e);
//            }
//          }
//        }
//      }
//    }

  /**
   * Run nodes using info in nodeToNodeInfo hashtable,
   * which maps node names to NodeInfo objects.
   * This is called both when running an experiment and when
   * restarting nodes that were attached to.
   */
  private void runTrial() {
    runStart = new Date(); // set now, cause it's used in logfilename
    nodeCreationInfoList = new ArrayList();
    Collection values = nodeToNodeInfo.values();
    for (Iterator i = values.iterator(); i.hasNext();) {
      NodeInfo ni = (NodeInfo) i.next();

      // FIXME: Right here I could check that the process name will
      // be unique, and perhaps use a different nodeName if necessary
      NodeCreationInfo nci = prepareToCreateNode(ni.getAppServer(),
                                                 ni.getNodeName(), ni.getHostName(),
                                                 ni.getProperties(), ni.getArgs());
      if (nci != null)
        nodeCreationInfoList.add(nci);
    }
    stopNodeCreation = false;
    startTimers();
    if (log.isDebugEnabled()) {
      log.debug("runTrial about to start createNodes thread");
    }

    // Create the nodes in a separate thread. Creating the nodes does RMI, so
    // may block on the network. So avoid doing this in the AWT thread.
    nodeCreator = new Thread("CreateNodes") {
      public void run() {
        createNodes();
        starting = false;
        // reset controls if no nodes started successfully
        boolean reset = false;
        synchronized (runningNodesLock) {
          if (runningNodes.isEmpty())
            reset = true;
        } // end synchronized
        if (reset) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              runButton.setSelected(false);
              runButton.setEnabled(true);
            }
          });
        } else {
          // create and display iconified GLSClient
          // if its servlet exists
          final String glsAgent = findServlet();
          if (glsAgent != null) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                addGLSWindow(glsAgent);
              }
            });
          }
        }
      }
    }; // end node creating thread


    starting = true;
    if (log.isDebugEnabled()) {
      log.debug("runTrial about to create Nodes");
    }
    try {
      nodeCreator.start();
    } catch (RuntimeException re) {
      if (log.isErrorEnabled()) {
        log.error("Exception", re);
      }
    }
  }

  /////////////////////////////////////////
  // GLS related methods

  // Add GLS Pane to desktop. Used when starting an experiment.
  // Can also be used when attaching to a society.
  private void addGLSWindow(String glsAgent) {
    if (desktop == null)
      return;

    //  This indicates we already have a GLS, I believe
    if (glsClient != null)
      return;

    // Remove the GLS window if it is there
    // use desktop.cleanFrame(frame);
    JInternalFrame[] frames = desktop.getAllFrames();
    for (int i = 0; i < frames.length; i++) {
      String s = frames[i].getTitle();
      if (s.equals(glsWindowTitle))
        desktop.removeFrame(frames[i]);
    }

    JInternalFrame jif =
        new JInternalFrame(glsWindowTitle, true, false, true, true);
    glsClient = getGLSClient(glsAgent);
    if (glsClient == null)
      return;
    jif.getContentPane().add(glsClient);
    jif.setSize(350, 400);
    jif.setLocation(0, 0);
    jif.setVisible(true);
    desktop.add(jif, JLayeredPane.DEFAULT_LAYER);
    try {
      jif.setIcon(true);
    } catch (PropertyVetoException e) {
    }
  }

  /**
   * Use experiment component data tree to find plugins which are servlets.
   * TODO: shouldn't have to hard code servlet class names;
   * should be able to get servlet class to use from client
   */
  private String findServlet() {
    // bug 2258: perhaps usingExperiment is being set to false erroneously?
    if (experiment == null || !usingExperiment)
      return null;
    ArrayList componentData = new ArrayList();
    ComponentData societyData = experiment.getSocietyComponentData();
    ComponentData[] cdata = societyData.getChildren();
    for (int i = 0; i < cdata.length; i++)
      componentData.add(cdata[i]);
    for (int i = 0; i < componentData.size(); i++) {
      Object o = componentData.get(i);
      if (o instanceof AgentComponentData) {
        AgentComponentData acd = (AgentComponentData) o;
        if (hasServlet(acd)) {
          String name = acd.getName().substring(acd.getName().lastIndexOf('.') + 1);
          if (log.isDebugEnabled()) {
            log.debug("Found GLSServlet in agent " + name);
          }
          return name;
        }
      } else {
        ComponentData[] tmp = ((ComponentData) o).getChildren();
        for (int j = 0; j < tmp.length; j++)
          componentData.add(tmp[j]);
      }
    }
    return null;
  }

  /**
   * Find the GLS servlet.
   */
  private boolean hasServlet(AgentComponentData cdata) {
    String[] names = cdata.getPluginNames();
    for (int i = 0; i < names.length; i++) {
      // Could the name ever add the OPLAN? In which case, we should
      // do an indexOf != -1
      // BUG 2258: Do indexOfIgnoreCase?
      // Or must I not rely on the PluginNames at all, and look at the class?
      if (names[i].endsWith(GLS_SERVLET))
        return true;
    }
    return false;
  }

  /**
   * Create GLS client gui.
   * Look for a definition of the https port, and if it exists,
   * use https and the port number;
   * else look for a definition of http port, and if it exists,
   * use http and the port number;
   * else use http and the default port number.
   */
  private GLSClient getGLSClient(String agent) {
    String hostName = "localhost"; // defaults
    String protocol = GLS_PROTOCOL;
    String port = String.valueOf(CSMARTUL.agentPort);
    if (agent == null || agent.equals(""))
      agent = "NCA";
    Properties arguments = null;
    if (experiment != null) {
      HostComponent[] hosts = experiment.getHostComponents();
      for (int i = 0; i < hosts.length; i++) {
        NodeComponent[] nodes = hosts[i].getNodes();
        for (int j = 0; j < nodes.length; j++) {
          AgentComponent[] agents = nodes[j].getAgents();
          for (int k = 0; k < agents.length; k++) {
            if (agents[k].getShortName().equals(agent)) {
              hostName = hosts[i].getShortName();
              arguments = nodes[j].getArguments();
              break;
            }
          }
        }
      }
    }

    if (arguments != null) {
      String s = arguments.getProperty(CSMARTUL.AGENT_HTTPS_PORT);
      if (s != null) {
        port = s;
        protocol = GLS_SECURE_PROTOCOL;
      } else {
        s = arguments.getProperty(CSMARTUL.AGENT_HTTP_PORT);
        if (s != null)
          port = s;
      }
    }
    return new GLSClient(protocol, hostName, port, agent);
  }


  //
  // End of GLS Related methods
  /////////////////////////////////////////

  /**
   * Stop all nodes.
   * If the society in the experiment is self terminating,
   * just stop after the current trial (don't start next trial),
   * otherwise stop immediately.
   */
  private void stopButton_actionPerformed(ActionEvent e) {
    doStop();
  }

  private void doStop() {
    stopButton.setSelected(true); // indicate stopping
    stopButton.setEnabled(false); // disable until experiment stops
    stopAllNodes(); // all nodes must be stopped manually for now
  }

  /**
   * Stop the nodes; called by stop.
   */
  private void stopAllNodes() {
    // if there's a thread still creating nodes, stop it
    stopNodeCreation = true;
    // wait for the creating thread to stop
    // FIXME: This means waiting on the node creator that waits on RMI
    // so this may block the UI. However, the nodeCreator does try hard
    // to bail out if stopNodeCreation is true. So unless we're in the process
    // of doing the RMI thing here, this shouldn't take too long.
    if (nodeCreator != null) {
      try {
        nodeCreator.join();
      } catch (InterruptedException ie) {
        if (log.isErrorEnabled()) {
          log.error("Exception waiting for node creation thread to die: ", ie);
        }
      }
      nodeCreator = null;
    }
    // at this point all the nodes have been created, but
    // the gui controls for the last node created may not have
    // been created (as they're created via a swing-thread-invoke-later)
    // so we can no longer assume that a gui (node frame, status button)
    // exists for every node

    // set a flag indicating that we're stopping the run
    stopping = true;
    Enumeration nodeNames;
    synchronized (runningNodesLock) {
      nodeNames = runningNodes.keys();
    } // end synchronized

    // before destroying nodes, stop the GLSClient so
    // we don't get error messages
    if (glsClient != null) {
      glsClient.stop();
      glsClient = null;
    }

    if (log.isDebugEnabled()) {
      log.debug("About to kill all Nodes");
    }

    // destroy the nodes by creating a thread to destroy each one
    // and then waiting for all those threads to finish
    ArrayList destroyerThreads = new ArrayList();
    while (nodeNames.hasMoreElements()) {
      final String nodeName = (String) nodeNames.nextElement();
      Thread nodeDestroyer = new Thread("DestroyNode " + nodeName) {
        public void run() {
          RemoteProcess remoteNode;
          synchronized (runningNodesLock) {
            remoteNode = (RemoteProcess) runningNodes.get(nodeName);
          } // end synchronized
          if (remoteNode == null) {
            if (log.isErrorEnabled()) {
              log.error("Unknown node name: " + nodeName);
            }
            markNodeDead(nodeName);
            return;
          }

          // Try to kill the node. Try up to 5 times for now.
          RemoteListenable rl = null;
          for (int retries = 5; retries > 0; retries--) {
            try {
              rl =
                  remoteNode.getRemoteListenable();
              if (rl != null) {
                rl.flushOutput();
                remoteNode.destroy();
                // Note that the above can take a while
                // Note that also calling this ends up causing the callback
                // to nodeStopped, which does
                // most of the work
                retries = 0; // Done, dont loop again
              }
              // Note that currently if rl == null and get
              // no exception (can that happen?) we will loop
              // up to 5 times, trying to get to that point
              // But if there's an exeption, and rl != null,
              // we stop looping early
            } catch (Exception ex) {

              // May get ConnectException or ConnectIOException
              //  getting the remote
              // listenable if the remote machines / network are busy
              // in which case, we really want to pause & try again,
              // else we can't really kill the node.
              // But of course we should probably limit the number of times we
              // re-try.
              if (rl == null) {
                // some sort of connect exception probably, getting
                // the RemoteListenable. pause, then try again.
                // Must I check for the particular exceptions?
                // FIXME!!

                if (log.isWarnEnabled())
                  log.warn("Never got RemoteListenable for node " + nodeName + ". Exception possibly cause of load? Will pause and retry.", ex);

                // Pause here. How long will be good?
                try {
                  // in milliseconds
                  sleep(2000);
                } catch (InterruptedException ie) {
                }

                continue;
              } else {
                // Got the RemoteListenable, but couldn't
                // destroy it
                if (log.isWarnEnabled()) {
                  log.warn("Unable to destroy node " + nodeName + ", assuming it's dead: ", ex);
                }

                // call the method that would have been called
                // had the node been stopped
                markNodeDead(nodeName);
                retries = 0; // Done with this node, dont loop again
              }
            } // end of catch block
          } // loop to retry killing the Node
        } // Thread run method
      }; // end nodeDestroyer thread
      destroyerThreads.add(nodeDestroyer);
      nodeDestroyer.start();
    }

    // FIXME: Bug 2282: Wrap here to end of method
    // in a SwingUtilities.invokeLater so the UI is not frozen?
    // But doesnt doing so mean we lose the whole point of joining
    // the threads in the first place?
    // What happens in the UI if it looks like you can keep going,
    // but the Nodes have not been killed yet?
    // To put it another way: the point of joining is so that when the stop
    // button action method completes, the Nodes are all really stopped.
    // Then the UI reflects this and the user can keep going
    // If you don't do this, I suppose the user might hit run again and be surprised
    // when funny things happen on some machines where there are Nodes still running.
    // They also might be surprised that some Nodes are still green,
    // indicating its still running, and they thought they had stopped it

    // Note that this also gets called in some circumstances when you hit Exit.
    // In this case, that other thread probably needs some of the state the exit
    // method is going to try to destroy. So maybe there I do need to block?

    // Todd suggests we don't wait, and change all the status lights
    // to Yellow or some such.

//     SwingUtilities.invokeLater(new Runnable() {
// 	public void run() {
    for (int i = 0; i < destroyerThreads.size(); i++) {
      try {
        Thread destroyer = (Thread) destroyerThreads.get(i);
        destroyer.join(); // wait for node destruction to complete
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception joining node destroyer threads", e);
        }
      }
    }

    if (log.isDebugEnabled()) {
      log.debug(".... done killing Nodes");
    }
// 	}
//       });
  } // end of stopAllNodes

  // Kill any existing output frames
  private void destroyOldNodes() {
    Iterator nodeNames = oldNodes.iterator();
    while (nodeNames.hasNext()) {
      String nodeName = (String) nodeNames.next();
      removeStatusButton(nodeName);
      nodePanes.remove(nodeName);
      oldNodes.remove(nodeNames);
    }
    JInternalFrame[] frames = desktop.getAllFrames();
    for (int i = 0; i < frames.length; i++) {
      String s = frames[i].getTitle();
      if (!s.equals(configWindowTitle)) {
        if (log.isDebugEnabled())
          log.debug("destroyOldNodes killing frame " + s);
        // FIXME: Is the title what it's under in the hash?
        // Note: this completely kills the frame, listener, textpane,
        // and document in that order
        desktop.removeFrame(frames[i]);
      }
    }
    cleanListeners(); // mostly just to clear out the array
    // it will clear out nodeListeners
  }

  /**
   * Update the gui controls when experiments (or attached nodes)
   * are started or stopped.
   */
  private void updateControls(boolean isRunning) {
    if (experiment != null && usingExperiment) {
      if (!isRunning) {
        experiment.experimentStopped();
        csmart.removeRunningExperiment(experiment);
      } else
        csmart.addRunningExperiment(experiment);
      // update society
      experiment.getSocietyComponent().setRunning(isRunning);
    }

    // if not running, enable the run button, and don't select it
    // if running, disable the run button and select it
    // FIXME: This means the run button will be disabled
    // if I attach to some nodes but have not started my experiment?
    // see end of the attach method
    runButton.setEnabled(!isRunning);
    runButton.setSelected(isRunning);

    // if running, enable the stop button and don't select it
    // if not running, disable the stop button and don't select it
    stopButton.setEnabled(isRunning);
    stopButton.setSelected(false);

    // if not running, don't allow the user to restart individual nodes
    JInternalFrame[] frames = desktop.getAllFrames();
    for (int i = 0; i < frames.length; i++) {
      String s = frames[i].getTitle();
//       if (log.isDebugEnabled()) {
// 	log.debug("Considering toggling restart for frame " + s + ". Will tell it: " + !isRunning);
//       }
      if (!s.equals(configWindowTitle) &&
          !s.equals(glsWindowTitle)) {
// 	if (log.isDebugEnabled())
// 	  log.debug("updateControls setting enableRestart to " + !isRunning + " for node " + s);
        ((ConsoleInternalFrame) frames[i]).enableRestart(!isRunning);
      }
    }
  }

  /**
   * Called by ConsoleNodeListener when node has stopped;
   * called within the Swing thread.
   * If all nodes are stopped, then run is stopped.
   * Update the gui controls.
   */
  public void nodeStopped(String nodeName) {
    if (log.isDebugEnabled())
      log.debug("nodeStopped for node " + nodeName);
    RemoteProcess remoteNode = null;
    synchronized (runningNodesLock) {
      if (runningNodes != null)
        remoteNode = (RemoteProcess) runningNodes.remove(nodeName);
      if (oldNodes != null)
        oldNodes.add(nodeName);
    } // end synchronized

    // remove the node listener
    if (remoteNode != null) {
      try {
        RemoteListenable rl = remoteNode.getRemoteListenable();
        if (rl != null) {
          rl.flushOutput();
          rl.removeListener(CSMART.getNodeListenerId());
        }
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception removing listener for remote node " + nodeName, e);
        }
        // FIXME: update AppServer list?
      }
    } // end of block to kill remote listener

    ConsoleNodeListener list = null;
    if (nodeListeners != null)
      list = (ConsoleNodeListener) nodeListeners.get(nodeName);

    if (list != null) {
      // do this only if the Node Status Button
      // indicates we got the last output, or otherwise
      // know we got it all?
      // STATUS_DESTROYED is definitely safe
      // STATUS_NO_ANSWER shouldn't happen
      // STATUS_UNKOWN I don't know about
      // but the problem is that nodeStopped
      // gets called in general _before_
      // we set the status to NO_ANSWER if there was an error
      list.closeLogFile();
//       if (list.statusButton == null || list.statusButton.status == NodeStatusButton.STATUS_NODE_DESTROYED || list.statusButton.status == NodeStatusButton.STATUS_NO_ANSWER) {
// 	list.cleanUp();
//       } else {
// 	// FIXME: Wait?
// 	if (log.isDebugEnabled()) {
// 	  log.debug("nodeStopped for node " + nodeName + " wanted to close log for listener, but status says it might still be busy: " + list.statusButton.getStatusDescription());
// 	}
//      }
    } // end of block to kill Node Listener

    // enable restart command on node output window, only if we're not stopping
    ConsoleInternalFrame frame = null;
    if (desktop != null)
      frame = desktop.getNodeFrame(nodeName);
    if (frame != null && !stopping) {
//       if (log.isDebugEnabled())
// 	log.debug("nodeStopped enabling restart for node " + nodeName);
      frame.enableRestart(true);
    }

    // ignore condition in which we temporarily have
    // no running nodes while starting
    if (starting)
      return;

    // when all nodes have stopped, save results
    // and update the gui controls
    boolean finishedRun = false;
    synchronized (runningNodesLock) {
      if (runningNodes == null || runningNodes.isEmpty())
        finishedRun = true;
//       else if (log.isDebugEnabled())
// 	log.debug("nodeStopped for node " + nodeName + " still have " + runningNodes.size() + " running nodes.");
    } // end synchronized

    if (finishedRun) {
      if (log.isDebugEnabled())
        log.debug("nodeStopped: finished run after killing node " + nodeName);
      stopping = false;
      experimentFinished();
    }
  }

  // Flush and dispose of old node listeners
  private void cleanListeners() {
    if (nodeListeners == null || nodeListeners.isEmpty())
      return;
    Collection c = nodeListeners.values();
    for (Iterator i = c.iterator(); i.hasNext();) {
      ConsoleNodeListener listener = (ConsoleNodeListener) i.next();
      if (listener == null)
        continue;
      if (listener.statusButton == null || listener.statusButton.getMyModel().getStatus() == NodeStatusButton.STATUS_NODE_DESTROYED || listener.statusButton.getMyModel().getStatus() == NodeStatusButton.STATUS_NO_ANSWER) {
        listener.cleanUp();
      } else {
        // FIXME!!
        // now what?
        // wait somehow?
        // what about STATUS_UNKNOWN
        if (log.isDebugEnabled())
          log.debug("cleanListeners found listener for " + listener.nodeName + " possibly not ready (" + listener.statusButton.getMyModel().getStatusDescription() + "), not cleaning.");
      }
    }
    nodeListeners.clear();
  }

  /**
   * The experiment is finished; disable and deselect the run button;
   * disable the restart menus in the node output frames;
   * stop the timers; and
   * unset the property values used in the experiment.
   */
  private void experimentFinished() {
    saveResults();
    updateControls(false);
    updateControls(false);
    runButton.setSelected(false);
    runButton.setEnabled(true);
    experimentTimer.stop();
    // AMH - FIXME
    //    usingExperiment = false;
  }


  /**
   * Select status button for node.
   */
  private void selectStatusButton(String nodeName) {
    NodeStatusButton button = getNodeStatusButton(nodeName);
    if (button != null)
      button.setSelected(true);
  }

  /**
   * Remove status button for node if it exists.
   */
  private void removeStatusButton(String nodeName) {
    NodeStatusButton button = getNodeStatusButton(nodeName);
    if (button != null) {
      statusButtons.remove(button);
      buttonPanel.remove(button);
    }
  }

  /**
   * Called when the user runs an experiment or
   * attaches to running nodes.
   * Creates the node listener and the output pane for the node.
   * Returns all the information needed to actually run the node
   * encapsulated in a single object.
   */
  private NodeCreationInfo prepareToCreateNode(RemoteHost appServer,
                                               String nodeName,
                                               String hostName,
                                               Properties properties,
                                               java.util.List args) {
    NodeStatusButton statusButton = createStatusButton(nodeName, hostName);
    ConsoleStyledDocument doc = new ConsoleStyledDocument();
    ConsoleTextPane textPane = new ConsoleTextPane(doc, statusButton);
    JScrollPane scrollPane = new JScrollPane(textPane);

    // create a node event listener to get events from the node
    OutputListener listener;
    String logFileName = getLogFileName(nodeName);
    try {
      listener = new ConsoleNodeListener(this,
                                         nodeName,
                                         logFileName,
                                         statusButton,
                                         doc);
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Unable to create output for: " + nodeName, e);
      }
      return null;
    }

    // Set up Node filters & notifications
    if (notifyCondition != null)
      textPane.setNotifyCondition(notifyCondition);
    ((ConsoleStyledDocument) textPane.getStyledDocument()).setBufferSize(viewSize);
    if (notifyOnStandardError)
      statusButton.getMyModel().setNotifyOnStandardError(true);
    if (displayFilter != null)
      ((ConsoleNodeListener) listener).setFilter(displayFilter);

    nodeListeners.put(nodeName, listener);
    nodePanes.put(nodeName, textPane);
    OutputPolicy outputPolicy = new OutputPolicy(10);
    return new NodeCreationInfo(appServer,
                                properties, args,
                                listener, outputPolicy,
                                nodeName, hostName,
                                scrollPane, statusButton,
                                logFileName);
  }

  /**
   * Runs in a separate thread, creates the nodes by calling the
   * appserver.
   * Uses the NodeCreationInfo object returned by prepareToCreateNodes
   * to get all the information needed to actually create the node.
   * Does RMI, so may block on the network.
   */
  private void createNodes() {
    int delay = 1;
    // get inter-node start delay
    String tmp =
        Parameters.findParameter("org.cougaar.tools.csmart.startdelay");
    if (tmp != null) {
      try {
        delay = Integer.parseInt(tmp);
      } catch (NumberFormatException nfe) {
      }
      if (delay < 1)
        delay = 1;
    }

    final int interNodeStartDelay = delay;
    String experimentName = "Experiment";
    if (experiment != null && usingExperiment)
      experimentName = experiment.getExperimentName();

    // For each Node to create
    for (int i = 0; i < nodeCreationInfoList.size(); i++) {
      // First pause
      try {
        Thread.sleep(interNodeStartDelay);
      } catch (Exception e) {
      }
      if (stopNodeCreation)
        break;

      NodeCreationInfo nci =
          (NodeCreationInfo) nodeCreationInfoList.get(i);
      RemoteProcess remoteNode;

      // Create the process description, then the proccess
      try {
        //        String procName =
        //            appServerSupport.getProcessName(experimentName, nci.nodeName);
        String procName = null;

        // Check that a process with description desc is not
        // already running, modify the process name if so
        //        while (appServerSupport.isProcessNameUsed(procName)) {
        //          if (log.isDebugEnabled()) {
        //            log.debug("ctNodes: process with name " + procName + " already running");
        //          }
        //          procName = procName + "1";
        //        }

        String groupName = "csmart";
        ProcessDescription desc =
            new ProcessDescription(procName,
                                   groupName,
                                   nci.properties,
                                   nci.args);


        RemoteListenableConfig conf =
            new RemoteListenableConfig(nci.listener,
                                       CSMART.getNodeListenerId(),
                                       null, nci.outputPolicy);

        // Last chance to bail out of creating the Node
        if (stopNodeCreation)
          break;

        // Next line does the actual creation -- including RMI stuff
        // that could take a while
        remoteNode =
            nci.remoteAppServer.createRemoteProcess(desc, conf);

        if (log.isDebugEnabled())
          log.debug("Adding listener: " +
                    CSMART.getNodeListenerId() +
                    " for: " + procName);
      } catch (Exception e) {
        // FIXME: Could look for exception: java.lang.RuntimeException:
        // Process name "Experiment for minitestconfig-MiniNode" is already
        // in use by another (running) process
        // which indicates the process is already running, basically

        if (log.isErrorEnabled()) {
          log.error("CSMARTConsole: cannot create node: " +
                    nci.nodeName, e);
        }
        final String node = nci.nodeName;
        final String host = nci.hostName;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(CSMARTConsole.this,
                                          "Cannot create node " + node + " on: " +
                                          host +
                                          "; check that server is running");
          }
        });
        continue;
      }

      synchronized (runningNodesLock) {
        runningNodes.put(nci.nodeName, remoteNode);
      } // end synchronized

      // Set up the UI for the Node
      SwingUtilities.invokeLater(new NodeCreateThread(nci, remoteNode));
    }
  }

  /**
   * Class for a thread to create the Nodes in parallel.
   * Done as a separate class to avoid declaring things
   * final.
   **/
  class NodeCreateThread implements Runnable {
    RemoteProcess remoteNode = null;
    NodeCreationInfo nci = null;

    public NodeCreateThread(NodeCreationInfo nci, RemoteProcess remoteNode) {
      this.nci = nci;
      this.remoteNode = remoteNode;
    }

    public void run() {
      // don't create gui controls if node creation has been stopped
      if (nodeCreator == null) return;
      if (stopNodeCreation)
        return;
      addStatusButton(nci.statusButton);
      ConsoleInternalFrame frame =
          new ConsoleInternalFrame(nci.nodeName,
                                   nci.hostName,
                                   nci.properties,
                                   nci.args,
                                   (ConsoleNodeListener) nci.listener,
                                   nci.scrollPane,
                                   nci.statusButton,
                                   nci.logFileName,
                                   remoteNode,
                                   console);
      frame.addInternalFrameListener(new NodeFrameListener());
      desktop.addNodeFrame(frame, nci.nodeName);
      updateControls(true);
    }
  }

  /**
   * Called from ConsoleInternalFrame (i.e. from menu on the node's output
   * window) to stop a node.
   * If this is the only node, then it is handled the same
   * as selecting the stop button on the console.
   */
  public void stopNode(String nodeName) {
    boolean doAbort = false;
    synchronized (runningNodesLock) {
      if (runningNodes.size() == 1)
        doAbort = true;
    } // end synchronized

    if (doAbort) {
      doStop();
      return;
    }

    RemoteProcess remoteNode;
    synchronized (runningNodesLock) {
      remoteNode = (RemoteProcess) runningNodes.get(nodeName);
    } // end synchronized

    if (remoteNode == null)
      return;

    // Note: Next couple lines do RMI, so could take a while.
    // Should we try to do this try/catch outside the AWT thread?
    try {
      remoteNode.getRemoteListenable().flushOutput();
      remoteNode.destroy();
    } catch (Exception ex) {
      if (log.isErrorEnabled()) {
        log.error("Unable to destroy node, assuming it's dead: ", ex);
      }

      // FIXME: May get ConnectException or ConnectIOException
      //  getting the remote
      // listenable if the remote machines / network are busy
      // in which case, we really want to pause & try again,
      // else we can't really kill the node.
      // But of course we should probably limit the number of times we
      // re-try.
      // To fix this, copy code from above in stopAllNodes

      // call the method that would have been called when the
      // ConsoleNodeListener received the node destroyed confirmation
      markNodeDead(nodeName);
    }
  }

  /**
   * Called from ConsoleInternalFrame (i.e. from menu on the node's output
   * window) to restart the node.
   * If this is the only node, then it is handled the same
   * as selecting the Run button on the console.
   */
  public RemoteProcess restartNode(String nodeName) {
    NodeInfo nodeInfo = (NodeInfo) nodeToNodeInfo.get(nodeName);
    String hostName = nodeInfo.getHostName();
    Properties properties = nodeInfo.getProperties();
    java.util.List args = nodeInfo.getArgs();
    boolean doRun = false;
    synchronized (runningNodes) {
      if (oldNodes.size() + runningNodes.size() == 1)
        doRun = true;
    } // end synchronized

    // If only have one Node, restarting the node is the same as hitting Run
    // EXCEPT: If we have an Experiment but aren't running it
    // and had attached to a single Node which we are trying to restart
    // then don't treat that the same
    // as hitting Run
    if ((experiment == null || usingExperiment) && doRun) {
      // Note that this next method is potentially time-consuming, does RMI
      runButton_actionPerformed();
      return null; // return null, caller (ConsoleInternalFrame) is going away
    }

    // Do the default think with old persistence data when restarting
    // a Node -- default is to remove old data
    properties.remove(Experiment.PERSIST_CLEAR);

    int remotePort = appServerSupport.getAppServerPort(properties);
    // Next line may do RMI -- do in non-AWT thread?
    //    RemoteHost remoteAppServer =
    //        appServerSupport.getAppServer(hostName, remotePort);
    RemoteHost remoteAppServer = null;
    if (remoteAppServer == null)
      return null;

    ConsoleStyledDocument doc = null;
    // close the log file and remove the old node event listener
    ConsoleNodeListener listener =
        (ConsoleNodeListener) nodeListeners.remove(nodeName);
    if (listener != null) {
      // reuse old document
      doc = listener.getDocument();

      // Done closing up old one, now start on new one
      // FIXME: Wait until we somehow know we got the last
      // output from the node, perhaps looking at the
      // NodeStatusButton?
      // Status STATUS_DESTROYED, STATUS_NO_ANSWER
      // definitely OK. STATUS_UNKNOWN MAYBE
      // Of course
      if (listener.statusButton == null || listener.statusButton.getMyModel().getStatus() == NodeStatusButton.STATUS_NODE_DESTROYED || listener.statusButton.getMyModel().getStatus() == NodeStatusButton.STATUS_NO_ANSWER)
        listener.cleanUp();
      else
        listener.closeLogFile();
    }

    if (doc == null) {
      //  The doc gets killed if the frame was killed
      // or the textPane was killed
      // FIXME: Must replace the whole node window
      // remove old internal frame and add a new one
//       nodePanes.remove(nodeName);
//       desktop.removeNodeFrame(nodeName);
//       doc = new ConsoleStyledDocument();
//       NodeCreationInfo nci = (NodeCreationInfo)nodeCreationInfoList.get(i);
//       ConsoleInternalFrame frame =
// 	new ConsoleInternalFrame(nci.nodeName,
// 				 nci.hostName,
// 				 nci.properties,
// 				 nci.args,
// 				 (ConsoleNodeListener)nci.listener,
// 				 nci.scrollPane,
// 				 nci.statusButton,
// 				 nci.logFileName,
// 				 remoteNode,
// 				 console);
//       frame.addInternalFrameListener(new NodeFrameListener());
//       desktop.addNodeFrame(frame, nci.nodeName);
      if (log.isWarnEnabled())
        log.warn("restartNode unable to find document for node " + nodeName + ", not restarting.");
      return null;
    }

    // create a node event listener to get events from the node
    String logFileName = getLogFileName(nodeName);
    try {
      listener = new ConsoleNodeListener(this,
                                         nodeName,
                                         logFileName,
                                         getNodeStatusButton(nodeName),
                                         doc);
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Unable to create output for: " + nodeName, e);
      }
      return null;
    }

    if (displayFilter != null)
      listener.setFilter(displayFilter);
    nodeListeners.put(nodeName, listener);

    RemoteProcess remoteNode = null;
    try {
      String experimentName = "Experiment";
      if (experiment != null && usingExperiment)
        experimentName = experiment.getExperimentName();
      //      String procName = appServerSupport.getProcessName(experimentName, nodeName);
      String procName = null;

      // Next method does RMI, could block
      // Check that a process with description desc is not
      // already running, modify the process name if so
      //      while (appServerSupport.isProcessNameUsed(procName)) {
      //        if (log.isDebugEnabled()) {
      //          log.debug("ctNodes: process with name " + procName + " already running");
      //        }
      //        procName = procName + "1";
      //      }

      String groupName = "csmart";
      ProcessDescription desc =
          new ProcessDescription(procName,
                                 groupName,
                                 properties,
                                 args);
      RemoteListenableConfig conf =
          new RemoteListenableConfig(listener, CSMART.getNodeListenerId(),
                                     null, new OutputPolicy(10));

      // Next line is where all the work happens. Does RMI & could block
      // Should I check for the stopNodeCreation flag here and bail if true?
      // Should I try to do this in a non-AWT thread?
      remoteNode =
          remoteAppServer.createRemoteProcess(desc, conf);
      if (log.isDebugEnabled())
        log.debug("Adding listener: " +
                  CSMART.getNodeListenerId() +
                  " for: " + procName);
      if (remoteNode != null) {
        synchronized (runningNodesLock) {
          runningNodes.put(nodeName, remoteNode);
          oldNodes.remove(nodeName);
        } // end synchronized
        // AMH - FIXME
        //	updateControls(true);
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Exception restarting node " + nodeName, e);
      }
      // Failed to create the remove process
      // so clean up.
      // call stopNode(nodeName)?
      markNodeDead(nodeName);
      return null;
    }
    return remoteNode;
  }

  /**
   * Get all known app servers from the experiment or from the user.
   * Display a list of the nodes that the app servers know about.
   * Allow the user to select the nodes to which to attach (or all).
   * Add listeners to the running nodes, and set up the guis for it.
   * This enters information about the node in the nodeToNodeInfo map
   * from which nodes can be restarted.
   */
  private void attachButton_actionPerformed() {
    attachButton.setSelected(false);
    // Before getting this list, make sure our list of possible
    // things to attach to is up-to-date
    //    appServerSupport.refreshAppServers();
    //    ArrayList nodesToAttach = appServerSupport.getNodesToAttach();
    ArrayList nodesToAttach = null;
        if (nodesToAttach == null) {
      // There were no Nodes to attach to
          JOptionPane.showMessageDialog(this, "No new Nodes to Attach to.",
                                        "Attach", JOptionPane.PLAIN_MESSAGE);
          return;
        } else if (nodesToAttach.isEmpty()) {
      // User just selected none to attach to
          return;
        }

    // Loop over nodes to attach to
    for (int i = 0; i < nodesToAttach.size(); i++) {
      NodeInfo nodeInfo = (NodeInfo) nodesToAttach.get(i);
      boolean haveAlready = false;
      String name = nodeInfo.getNodeName();
      synchronized (runningNodesLock) {
        if (runningNodes.get(nodeInfo.getNodeName()) != null) {
          haveAlready = true;
        }
      }

      // Are we already atached to a node of that name?
      if (haveAlready) {
        if (log.isDebugEnabled()) {
          log.debug("Already have attached node of name " + nodeInfo.getNodeName());
        }
        // So how do I get the old process name?
        NodeInfo old = (NodeInfo) nodeToNodeInfo.get(nodeInfo.getNodeName());
        //        if (old.getProcessName().equals(nodeInfo.getProcessName())) {
        //          if (log.isDebugEnabled()) {
        //            log.debug("They have the same process name: " + nodeInfo.getProcessName());
        //          }
          // FIXME: Maybe compare nodeInfo.properties too?
        //          if (log.isDebugEnabled()) {
        //            log.debug("Asked to attach to already attached node " + nodeInfo.getNodeName());
        //          }
        //          continue;
        //        } else {
          // Going to allow attaching
          // But need to pretend it has a different name or else
          // I'll have trouble stopping it and whatnot

          // Must effectively reset nodeInfo.nodeName
        //          name = name + "-attached";
        //        }
      } // end of block to see if this Node already attached

      runStart = new Date();
      NodeCreationInfo nci = prepareToCreateNode(nodeInfo.getAppServer(),
                                                 name,
                                                 nodeInfo.getHostName(),
                                                 nodeInfo.getProperties(),
                                                 nodeInfo.getArgs());
      nodeToNodeInfo.put(name, nodeInfo);
      RemoteProcess remoteNode = null;
      // add listener to node
      try {
        // FIXME: Next few lines do RMI, could block. Avoid doing it in AWT thread?
        //        remoteNode =
        //            nodeInfo.getAppServer().getRemoteProcess(nodeInfo.getProcessName());
        remoteNode = null;
        if (remoteNode != null) {
          RemoteListenable rl =
              remoteNode.getRemoteListenable();
          if (log.isDebugEnabled())
            //            log.debug("Adding listener: " +
            //                      CSMART.getNodeListenerId() +
            //                      " for: " +
            //                      nodeInfo.getProcessName());
          rl.addListener(nci.listener,
                         CSMART.getNodeListenerId());
        } else {
          if (log.isWarnEnabled())
            log.warn("Got null process from AppServer for node to attach to: " + name);
          //          throw new Exception("Null RemoteProcess for " + nodeInfo.getProcessName());
          throw new Exception("Null Remote Process");
        }
      } catch (Exception e) {
        //        if (log.isErrorEnabled()) {
        //          log.error("Exception attaching to: " + nodeInfo.getProcessName(), e);
        //        }
        NodeInfo invalid = (NodeInfo) nodeToNodeInfo.remove(name);
        // remove status button
        // kill doc, textPane, listener
        // remove from nodeListeners and nodePanes
        ((ConsoleNodeListener) nci.listener).cleanUp();
        nodeListeners.remove(name);
        ConsoleTextPane textPane = (ConsoleTextPane) nodePanes.remove(name);
        if (textPane != null)
          textPane.cleanUp();
        //	removeStatusButton(nci.statusButton);

        continue;
      }

      synchronized (runningNodesLock) {
        runningNodes.put(name, remoteNode);
      }

      addStatusButton(nci.statusButton);
      ConsoleInternalFrame frame =
          new ConsoleInternalFrame(nci.nodeName,
                                   nci.hostName,
                                   nci.properties,
                                   nci.args,
                                   (ConsoleNodeListener) nci.listener,
                                   nci.scrollPane,
                                   nci.statusButton,
                                   nci.logFileName,
                                   remoteNode,
                                   console);
      frame.addInternalFrameListener(new NodeFrameListener());
      desktop.addNodeFrame(frame, nci.nodeName);
    }
    updateControls(true);

    // If have an experiment to run
    // but have not run it yet, make that possible still
    if (!usingExperiment && experiment != null) {
      runButton.setEnabled(true);
      runButton.setSelected(false);
    }
  }

  private void startTimers() {
    startExperimentTime = new Date().getTime();
    experimentTimer.start();
  }

  /**
   * This checks the society and recipes in the experiment to determine if
   * any of them generated this metrics file.
   * Creating a new File from the filename works because acceptFile
   * just looks at the filename.
   */
  private boolean isResultFile(String filename) {
    File thisFile = new java.io.File(filename);
    // if no experiment, use default filter
    if (experiment == null || !usingExperiment)
      return new ResultsFileFilter().accept(thisFile);
    SocietyComponent societyComponent = experiment.getSocietyComponent();
    if (societyComponent != null) {
      java.io.FileFilter fileFilter = societyComponent.getResultFileFilter();
      if (fileFilter != null && fileFilter.accept(thisFile))
        return true;
    }
    int nrecipes = experiment.getRecipeComponentCount();
    for (int i = 0; i < nrecipes; i++) {
      RecipeComponent recipeComponent = experiment.getRecipeComponent(i);
      if (recipeComponent instanceof MetricComponent) {
        MetricComponent metricComponent = (MetricComponent) recipeComponent;
        java.io.FileFilter fileFilter = metricComponent.getResultFileFilter();
        if (fileFilter != null && fileFilter.accept(thisFile))
          return true;
      }
    }
    return false;
  }

  /**
   * Read remote files and copy to directory specified by experiment.
   */
  private void copyResultFiles(RemoteFileSystem remoteFS,
                               String dirname) {
    char[] cbuf = new char[1000];
    try {
      // FIXME: This reads just from the current directory,
      // but should read from wherever the BasicMetric told it to read,
      // or in general, wherever the Component says to read
      // But does the AppServer support calling list on arbitrary paths?
      // See bug 1668
      // Maybe to generalize, let this traverse sub-directories?
      String[] filenames = remoteFS.list("./");
      for (int i = 0; i < filenames.length; i++) {
        if (!isResultFile(filenames[i]))
          continue;
        File newResultFile =
            new File(dirname + File.separator + filenames[i]);
        InputStream is = remoteFS.read(filenames[i]);
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(is), 1000);
        BufferedWriter writer =
            new BufferedWriter(new FileWriter(newResultFile));
        int len = 0;
        while ((len = reader.read(cbuf, 0, 1000)) != -1) {
          writer.write(cbuf, 0, len);
        }
        reader.close();
        writer.close();
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("CSMARTConsole: copyResultFiles failed: ", e);
      }
    }
  }

  /**
   * Create a file for the results of this run.
   * Results file structure is:
   * <ExperimentName>
   *    <TrialName>
   *       Results-<Timestamp>.results
   */
  private void saveResults() {
    String dirname = makeResultDirectory();
    // Must check for null return here!?
    if (dirname == null) {
      // User didn't specify a directory or couldn't create one or something?
      if (log.isInfoEnabled())
        log.info("saveResults got no good result directory from makeResult: Using pwd.");
      // Is . really the right choice here?
      dirname = ".";
    }
    if (experiment != null && usingExperiment) {
      try {
        String myHostName = InetAddress.getLocalHost().getHostName();
        if (myHostName == null) {
          if (log.isWarnEnabled())
            log.warn("saveResults Got null host name from InetAddress? Using localhost.");
          myHostName = "localhost";
        }
        URL url = new URL("file", myHostName, dirname);
        Trial trial = experiment.getTrial();
        // FIXME: No one uses these Trial results anywhere!
        // So why bother doing this?
        if (trial != null)
          trial.addTrialResult(new TrialResult(runStart, url));
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception creating trial results URL: ", e);
        }
      }
    }
    Collection values = nodeToNodeInfo.values();
    java.util.List contactedAS = new ArrayList();
    for (Iterator i = values.iterator(); i.hasNext();) {
      NodeInfo ni = (NodeInfo) i.next();
      RemoteHost remoteAppServer = ni.getAppServer();
      // Ask appServerSupport if this guy is legit?
      if (remoteAppServer != null && !contactedAS.contains(remoteAppServer) && appServerSupport.isValidRemoteHost(remoteAppServer)) {
        contactedAS.add(remoteAppServer);
        RemoteFileSystem remoteFS = null;
        try {
          remoteFS = remoteAppServer.getRemoteFileSystem();
        } catch (Exception e) {
          final String host = ni.getHostName();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              JOptionPane.showMessageDialog(CSMARTConsole.this,
                                            "Cannot save results.  Unable to access filesystem for " +
                                            host + ".",
                                            "Unable to access file system",
                                            JOptionPane.WARNING_MESSAGE);
            }
          });
          if (log.isErrorEnabled())
            log.error("saveResults failed to get filesystem on " + ni.getHostName() + ": ", e);
          // Tell appServerSupport to see if the host is legit
          appServerSupport.haveNewNodes();
          continue;
        }
        copyResultFiles(remoteFS, dirname);
      }
    }
  }

  /**
   * Create a log file name which is of the form:
   * node name + date + .log
   * Create it in the results directory if possible.
   */
  private String getLogFileName(String nodeName) {
    String filename = nodeName + fileDateFormat.format(runStart) + ".log";
    String dirname = makeResultDirectory();
    if (dirname != null)
      filename = dirname + File.separatorChar + filename;
    return filename;
  }

  /**
   * Create a directory for the results of this run.
   * Results file structure is:
   * <ExperimentName>
   *    <TrialName>
   *       Results-<Timestamp>.results
   */
  private String makeResultDirectory() {
    // defaults, if we don't have an experiment
    File resultDir = csmart.getResultDir();
    String experimentName = "Experiment";
    String trialName = "Trial 1";
    if (experiment != null && usingExperiment) {
      resultDir = experiment.getResultDirectory();
      experimentName = experiment.getExperimentName();
      Trial trial = experiment.getTrial();
      if (trial != null) {
        trialName = trial.getShortName();
      } else {
        if (log.isWarnEnabled())
          log.warn("Null trial in experiment " + experimentName);
      }
    }
    // if user didn't specify results directory, save in local directory
    if (resultDir == null) {
      if (log.isInfoEnabled())
        log.info("No result directory specified. Should use a local dir. Returning null (in makeResultDirectory).");
      return null;
    }
    String dirname = resultDir.getAbsolutePath() + File.separatorChar +
        experimentName + File.separatorChar +
        trialName + File.separatorChar +
        "Results-" + fileDateFormat.format(runStart);
    try {
      File f = new File(dirname);
      // guarantee that directories exist
      if (!f.exists() && !f.mkdirs() && !f.exists()) {
        if (log.isWarnEnabled())
          log.warn("Unabled to create directory " + dirname + ". Should default to local directory - returning null (in makeResultDirectory)");
        return null;
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Couldn't create results directory " + dirname + ": ", e);
      }
      return null;
    }
    return dirname;
  }

  // When user hits Exit
  private void exitMenuItem_actionPerformed(AWTEvent e) {
    // Put up a window, giving three options:
    // 1) Kill listeners, not Nodes (default)
    // 2) Kill attached Nodes (old behavior)
    // 3) Cancel (return to Console, if possible)

    dontClose = false;

    // If nothing is attached, don't display this dialog
    boolean justExit = false;
    synchronized (runningNodesLock) {
      if (runningNodes == null || runningNodes.isEmpty())
        justExit = true;
    } // end synchronized

    if (!justExit) {
      JPanel exitPanel = new JPanel(new GridBagLayout());
      int x = 0;
      int y = 0;
      JLabel instr = new JLabel("Exit Console: Detach from Nodes (leaving them running), kill the Nodes, or Cancel?\n");
      JRadioButton detachButton = new JRadioButton("Detach from Society but leave running (default)");
      JRadioButton killButton = new JRadioButton("Stop all Attached Nodes (old behavior)");
      detachButton.setSelected(true);
      killButton.setSelected(false);

      ButtonGroup exitButtonGroup = new ButtonGroup();
      exitButtonGroup.add(detachButton);
      exitButtonGroup.add(killButton);

      exitPanel.add(instr,
                    new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(10, 0, 5, 5),
                                           0, 0));
      exitPanel.add(detachButton,
                    new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(10, 0, 5, 5),
                                           0, 0));
      exitPanel.add(killButton,
                    new GridBagConstraints(x, y, 1, 1, 0.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(0, 0, 5, 5),
                                           0, 0));

      int result = JOptionPane.showConfirmDialog(null,
                                                 exitPanel,
                                                 "Exit Console",
                                                 JOptionPane.OK_CANCEL_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null);
      if (result != JOptionPane.OK_OPTION) {
        //  User wants to return to the console?
        // Set boolean for CSMART main UI to check
        dontClose = true;
        return;
      }

      if (killButton.isSelected()) {
        // This next ends up calling stopAllNodes
        // which causes nodeStopped to be called for each Node,
        // but not until we've waited on the RMI / network
        // so unless I take out that wait, we're blocking here (in AWT thread)
        // on the network. But if I don't block, this method
        // will later destroy some state that the nodeStopped method
        // probably wants, and this may cause errors
        doStop();
        // don't stop experiments when exiting the console
        //    stopExperiments();
      } else {
        // this is what this method does now
        // kill any node output listeners that this instance of CSMART started

        // if there's a thread still creating nodes, stop it
        stopNodeCreation = true;
        // FIXME: This means the AWT thread will block on the Node creation
        // thread, which in turn has to wait on RMI stuff.
        // This is probably bad. However, the nodeCreator does check stopNodeCreation
        // when it can to try to bail out, so unless we're doing the RMI
        // thing now, this _shouldn't_ be too bad.
        // wait for the creating thread to stop
        if (nodeCreator != null) {
          try {
            nodeCreator.join();
          } catch (InterruptedException ie) {
            if (log.isErrorEnabled()) {
              log.error("Exception waiting for node creation thread to die: ", ie);
            }
          }
          nodeCreator = null;
        }

        stopping = true;

        // stop the GLS Client
        if (glsClient != null) {
          glsClient.stop();
          glsClient = null;
        }

        // This flushes the output and disconnects it
        appServerSupport.killListeners();

        // Maybe grab result files?
        // This will contact all the AppServers again
        saveResults();

        if (experimentTimer != null)
          experimentTimer.stop();

        updateControls(false);
      } // end of block to detach from all Nodes
    } // end of block to deal with attached running Nodes

    // Stop polling the Appservers
    if (asPollTimer != null) {
      if (log.isDebugEnabled()) {
        log.debug("Canceling old AS timer");
      }
      asPollTimer.cancel();
      // stop monitoring app servers
      monitorAppServerTask.cancel();
      asPollTimer = null;
      monitorAppServerTask = null;
    }

    // this is set when entering the console and must be cleared on exit
    if (experiment != null)
      experiment.setRunInProgress(false);

    // Garbage collect
    // These all for bug 1685: Note however that it doesnt
    // seem to help much
    // watch out for listeners that have back pointers
    // so things dont get GCed
    // or for threads that hang around with pointers

    destroyOldNodes();
    oldNodes = null;
    runningNodes = null;

    // This is the biggy: It handles the internal frames, text panes,
    // and documents
    if (desktop != null) {
      desktop.dispose();
      desktop = null;
    }
    nodePanes = null;

    if (legend != null)
      legend.dispose();
    legend = null;

    statusButtons = null;
    attachButton = null;
    runButton = null;
    stopButton = null;
    buttonPanel = null;
    nodeCreator = null;
    myMouseListener = null;

    nodeListeners = null;

    // Grab all the NodeInfos and do something?
    nodeToNodeInfo = null;

    displayFilter = null;
    nodeCreationInfoList = null;
    appServerSupport = null;
    experimentTimer = null;
    glsClient = null;
    experiment = null;
    csmart = null;
    if (hostConfiguration != null) {
      hostConfiguration.removeHostTreeSelectionListener(myTreeListener);
      myTreeListener = null;
      hostConfiguration = null;
    }
    societyComponent = null;
    console = null;

    // If this was this frame's exit menu item, we have to remove
    // the window from the list
    // if it was a WindowClose, the parent notices this as well
    if (e instanceof ActionEvent) {
      NamedFrame.getNamedFrame().removeFrame(this);
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Not doing a removeFrame: event was " + e);
      }
    }

    // remove listeners from this window
    WindowListener[] lists = getWindowListeners();
    for (int i = 0; i < lists.length; i++)
      removeWindowListener(lists[i]);

    dispose();
  }

  /////////////////////////////////////////
  // Handle Filters, View Size, Notifications, etc

  /**
   * Display dialog to set size of screen buffer for node output.
   * Return user's response.  An value of -1 means display all.
   * A value of -2 means that the user cancelled the dialog.
   */
  static int displayViewSizeDialog(int currentViewSize) {
    JPanel bufferEventsPanel = new JPanel();
    JRadioButton allButton = new JRadioButton("All");
    JRadioButton sizeButton = new JRadioButton("Buffer Size");
    JTextField sizeTF = new JTextField(8);
    Logger log = CSMART.createLogger(CSMARTConsole.class.getName());
    if (currentViewSize == -1) {
      allButton.setSelected(true);
      sizeButton.setSelected(false);
      sizeTF.setText(String.valueOf(DEFAULT_VIEW_SIZE));
    } else {
      allButton.setSelected(false);
      sizeButton.setSelected(true);
      sizeTF.setText(String.valueOf(currentViewSize));
    }
    String oldTFVal = sizeTF.getText();
    ButtonGroup bufferButtonGroup = new ButtonGroup();
    bufferButtonGroup.add(allButton);
    bufferButtonGroup.add(sizeButton);
    bufferEventsPanel.add(allButton);
    bufferEventsPanel.add(sizeButton);
    bufferEventsPanel.add(sizeTF);

    int newViewSize = 0;
    while (true) {
      int result = JOptionPane.showConfirmDialog(null,
                                                 bufferEventsPanel,
                                                 "Node View",
                                                 JOptionPane.OK_CANCEL_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null);
      if (result != JOptionPane.OK_OPTION)
        return -2; // user cancelled
      newViewSize = 0;
      if (allButton.isSelected()) {
        return -1;
      } else {
        try {
          newViewSize = Integer.parseInt(sizeTF.getText());
        } catch (NumberFormatException e) {
          if (log.isErrorEnabled()) {
            log.error("Bad new view size: " + sizeTF.getText());
          }
        }
        if (newViewSize < 1) {
          // Invalid size. Show error message.
          JOptionPane.showMessageDialog(null,
                                        "Invalid buffer size. Must be a whole number, at least 1.",
                                        "Invalid entry",
                                        JOptionPane.WARNING_MESSAGE);
          sizeTF.setText(oldTFVal);
        } else {
          // Legitimate result
          break;
        }
      }
    } // end of while loop
    return newViewSize;
  }

  private void viewSizeMenuItem_actionPerformed() {
    int newViewSize = displayViewSizeDialog(viewSize);
    if (newViewSize == -2)
      return; // ignore, user cancelled
    viewSize = newViewSize;
    Enumeration textPanes = nodePanes.elements();
    while (textPanes.hasMoreElements()) {
      JTextPane textPane = (JTextPane) textPanes.nextElement();
      ((ConsoleStyledDocument) textPane.getStyledDocument()).setBufferSize(viewSize);
    }
  }

  /**
   * Set filtering (what node output is displayed) for all nodes.
   * TODO: overwrites any filters on individual node panes; is this
   * what we want (i.e. the last filter set either here or in the node
   * output frame is the filter used)
   */
  private void filterMenuItem_actionPerformed() {
    if (displayFilter == null)
      displayFilter =
          new ConsoleNodeOutputFilter(this, null, true);
    else
      displayFilter =
          new ConsoleNodeOutputFilter(this, displayFilter.getValues(),
                                      displayFilter.isAllSelected());
    Enumeration listeners = nodeListeners.elements();
    while (listeners.hasMoreElements()) {
      ConsoleNodeListener listener =
          (ConsoleNodeListener) listeners.nextElement();
      listener.setFilter(displayFilter);
    }
  }

  /**
   * Set formatting (font size, style, color) for all nodes.
   */
  private void formatMenuItem_actionPerformed() {
    ConsoleFontChooser cfc = new ConsoleFontChooser();
    cfc.setVisible(true);
    if (log.isErrorEnabled()) {
      log.error("Font Chooser not implemented yet");
    }
  }

  /**
   * Notify (by coloring status button) when the specified
   * output is received on any node.
   */
  private void setNotifyMenuItem_actionPerformed() {
    JPanel notifyPanel = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    notifyPanel.add(new JLabel("Search string:"),
                    new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(10, 0, 5, 5),
                                           0, 0));
    JTextField notifyField =
        new JTextField(20);
    notifyField.setText(notifyCondition);
    notifyPanel.add(notifyField,
                    new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(10, 0, 5, 0),
                                           0, 0));
    x = 0;
    JCheckBox stdErrorCB =
        new JCheckBox("Notify on Standard Error", notifyOnStandardError);
    notifyPanel.add(stdErrorCB,
                    new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(10, 0, 5, 5),
                                           0, 0));
    int result = JOptionPane.showConfirmDialog(this, notifyPanel,
                                               "Notification",
                                               JOptionPane.OK_CANCEL_OPTION);
    if (result != JOptionPane.OK_OPTION)
      return;
    String s = notifyField.getText();
    if (s == null || s.length() == 0)
      notifyCondition = null;
    else
      notifyCondition = s;
    setNotification();
    notifyOnStandardError = stdErrorCB.isSelected();
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      NodeStatusButton button = (NodeStatusButton) buttons.nextElement();
      button.getMyModel().setNotifyOnStandardError(notifyOnStandardError);
    }
  }

  /**
   * Set notification in all text panes.
   * Called to set or reset notification.
   */
  private void setNotification() {
    Enumeration textPanes = nodePanes.elements();
    while (textPanes.hasMoreElements()) {
      ConsoleTextPane textPane = (ConsoleTextPane) textPanes.nextElement();
      textPane.setNotifyCondition(notifyCondition);
    }
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      NodeStatusButton button = (NodeStatusButton) buttons.nextElement();
      button.getMyModel().clearError();
    }
  }

  /**
   * Display notification string.
   */
  private void viewNotifyMenuItem_actionPerformed() {
    if (notifyCondition == null)
      JOptionPane.showMessageDialog(this,
                                    "No notification set.",
                                    "Notification",
                                    JOptionPane.PLAIN_MESSAGE);
    else
      JOptionPane.showMessageDialog(this,
                                    "Notify if any node writes: " + notifyCondition,
                                    "Notification",
                                    JOptionPane.PLAIN_MESSAGE);
  }

  /**
   * Remove all notifications.
   */
  private void removeNotifyMenuItem_actionPerformed() {
    notifyCondition = null;
    setNotification();
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      NodeStatusButton button = (NodeStatusButton) buttons.nextElement();
      button.getMyModel().clearError();
    }
  }

  /**
   * Reset status for all nodes. Resets the "notify" position
   * in the text pane and resets the error flag of the node status button.
   */
  private void resetNotifyMenuItem_actionPerformed() {
    Enumeration textPanes = nodePanes.elements();
    while (textPanes.hasMoreElements()) {
      ConsoleTextPane textPane = (ConsoleTextPane) textPanes.nextElement();
      textPane.clearNotify();
    }
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      NodeStatusButton button = (NodeStatusButton) buttons.nextElement();
      button.getMyModel().clearError();
    }
  }

  //
  //////////////////////////////////////////////

  private String getElapsedTimeLabel(String prefix, long startTime) {
    long now = new Date().getTime();
    long timeElapsed = now - startTime;
    long hours = timeElapsed / MSECS_PER_HOUR;
    timeElapsed = timeElapsed - (hours * MSECS_PER_HOUR);
    long minutes = timeElapsed / MSECS_PER_MINUTE;
    timeElapsed = timeElapsed - (minutes * MSECS_PER_MINUTE);
    long seconds = timeElapsed / MSECS_PER_SECOND;
    StringBuffer sb = new StringBuffer(20);
    sb.append(prefix);
    sb.append(myNumberFormat.format(hours));
    sb.append(":");
    sb.append(myNumberFormat.format(minutes));
    sb.append(":");
    sb.append(myNumberFormat.format(seconds));
    return sb.toString();
  }

  /**
   * TreeSelectionListener interface.
   * If user selects an agent in the "Hosts" tree,
   * then pop the pane for that node to the foreground, and
   * select the node status button.
   */
  private TreeSelectionListener myTreeListener = new TreeSelectionListener() {
    public void valueChanged(TreeSelectionEvent event) {
      TreePath path = event.getPath();
      if (path == null) return;
      DefaultMutableTreeNode treeNode =
          (DefaultMutableTreeNode) path.getLastPathComponent();
      ConsoleTreeObject cto = (ConsoleTreeObject) treeNode.getUserObject();
      if (!cto.isNode())
        return; // ignore selecting if it's not a node
      String nodeName =
          ((ConsoleTreeObject) treeNode.getUserObject()).getName();
      displayNodeFrame(nodeName);
      selectStatusButton(nodeName);
    }
  };

  /**
   * Select node in host tree; called when user selects corresponding
   * pane or status button.
   */
  private void selectNodeInHostTree(String nodeName) {
    if (hostConfiguration == null) return;
    hostConfiguration.removeHostTreeSelectionListener(myTreeListener);
    hostConfiguration.selectNodeInHostTree(nodeName);
    hostConfiguration.addHostTreeSelectionListener(myTreeListener);
  }

  //////////////////////////////////////////////////
  // Methods to get data from the Experiment

  /**
   * Get node -d arguments.
   * Substitute host name for $HOST value if it occurs.
   * @param nc for which to get the -d arguments
   * @return properties the -d arguments
   */
  public Properties getNodeMinusD(NodeComponent nc, String hostName) {
    Properties result = new Properties();
    Properties props = nc.getArguments();
    boolean foundclass = false;
    for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
      String pname = (String) e.nextElement();
      if (pname.equals(COMMAND_ARGUMENTS)) continue;
      if (pname.equals(Experiment.BOOTSTRAP_CLASS)) foundclass = true;
      String value = props.getProperty(pname);

      // Allow $HOST to stand for hostName
      int index = value.indexOf("$HOST");
      if (index != -1)
        value = value.substring(0, index) + hostName +
            value.substring(index + 5);

      // Could add other substitutions here - for the node name, for example

      result.put(pname, value);
    }
    // make sure that the classname is "Node"
    //
    // this can be removed once the CMT and all "node.props"
    // are sure to have this property.

    if (foundclass == false)
      result.put(
          Experiment.BOOTSTRAP_CLASS,
          DEFAULT_BOOTSTRAP_CLASS);
    return result;
  }

  private java.util.List getNodeArguments(NodeComponent nc) {
    Properties props = nc.getArguments();
    String commandArguments =
        props.getProperty(COMMAND_ARGUMENTS);
    if (commandArguments == null || commandArguments.trim().equals("")) {
      // Warning: If you are running the bootstrapper and supply
      // nothing here, nothing will run!
      //  So if were using the default bootstrapper and have no arguments
      // Give it an argument with the default node class
      if (props.getProperty(Experiment.BOOTSTRAP_CLASS) == null || props.getProperty(Experiment.BOOTSTRAP_CLASS).equals(DEFAULT_BOOTSTRAP_CLASS))
        return Collections.singletonList(DEFAULT_NODE_CLASS);
      return Collections.EMPTY_LIST;
    }
    StringTokenizer tokens =
        new StringTokenizer(commandArguments.trim(), "\n");
    String[] result = new String[tokens.countTokens()];
    for (int i = 0; i < result.length; i++) {
      result[i] = tokens.nextToken();
    }

    java.util.List l = Arrays.asList(result);
    return l;
  }

  /**
   * Add any app servers on the hosts and ports that the experiment
   * will use to the list of app servers we're monitoring.
   */
  private void getAppServersFromExperiment() {
    HostComponent[] hosts = experiment.getHostComponents();
    for (int i = 0; i < hosts.length; i++) {
      String hostName = hosts[i].getShortName();
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
        NodeComponent nodeComponent = nodes[j];
        Properties properties = getNodeMinusD(nodeComponent, hostName);
        appServerSupport.addAppServerForExperiment(hostName, properties);
      }
    }
    //    appServerSupport.refreshAppServers();
  }

  /**
   * Used by ConsoleInternalFrame to get values from experiment
   * if it exists.
   */
  protected Object getHostPropertyValue(String hostName, String propertyName) {
    if (experiment == null || !usingExperiment) return null;
    HostComponent[] hosts = experiment.getHostComponents();
    for (int i = 0; i < hosts.length; i++) {
      String s = hosts[i].getShortName();
      if (s.equalsIgnoreCase(hostName))
        return getPropertyValue(hosts[i], propertyName);
    }
    return null;
  }

  /**
   * Used by ConsoleInternalFrame to get values from experiment
   * if it exists.
   */
  protected Object getNodePropertyValue(String nodeName, String propertyName) {
    if (experiment == null || !usingExperiment) return null;
    HostComponent[] hosts = experiment.getHostComponents();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
        String s = nodes[j].getShortName();
        if (s.equals(nodeName))
          return getPropertyValue(nodes[j], propertyName);
      }
    }
    return null;
  }

  private Object getPropertyValue(BaseComponent component, String name) {
    Property prop = component.getProperty(name);
    if (prop == null)
      return null;
    return prop.getValue();
  }

  /**
   * Used by ConsoleInternalFrame to get values from experiment
   * if it exists.
   */
  protected ArrayList getAgentComponentDescriptions(String nodeName,
                                                    String agentName) {
    if (experiment == null || !usingExperiment)
      return null;
    ComponentData societyComponentData = experiment.getSocietyComponentData();
    if (societyComponentData == null) {
      if (log.isWarnEnabled()) {
        log.warn("CSMARTConsole: Need to save experiment");
      }
      return null;
    }
    ComponentData[] children = societyComponentData.getChildren();
    ComponentData nodeComponentData = null;
    for (int i = 0; i < children.length; i++) {
      if (children[i].getType().equals(ComponentData.HOST)) {
        ComponentData[] nodes = children[i].getChildren();
        for (int j = 0; j < nodes.length; j++) {
          if (nodes[j].getName().equals(nodeName)) {
            nodeComponentData = nodes[j];
            break;
          }
        }
      }
    }

    //  If couldn't find the node in the ComponentData, give up
    if (nodeComponentData == null)
      return null;

    ComponentData agentComponentData = null;

    // The "agent" might be a NodeAgent, in which case this is the right spot.
    if (agentName.equals(nodeComponentData.getName())) {
      agentComponentData = nodeComponentData;
    } else {
      // OK. Find the sub-Agent with the right name
      ComponentData[] agents = nodeComponentData.getChildren();
      for (int i = 0; i < agents.length; i++) {
        if (agents[i] instanceof AgentComponentData &&
            agents[i].getName().equals(agentName)) {
          agentComponentData = agents[i];
          break;
        }
      }
    }

    // If couldn't find the Agent in the ComponentData for the node, give up
    if (agentComponentData == null)
      return null;

    // Loop through the children
    ComponentData[] agentChildren = agentComponentData.getChildren();
    ArrayList entries = new ArrayList(agentChildren.length);
    for (int i = 0; i < agentChildren.length; i++) {

      // If this Agent is a NodeAgent, ignore its Agent children.
      if (agentChildren[i].getType().equals(ComponentData.AGENT))
        continue;

      // FIXME: This should use same code as ExperimentINIWriter if possible
      StringBuffer sb = new StringBuffer();
      if (agentChildren[i].getType().equals(ComponentData.AGENTBINDER)) {
        sb.append(PluginManager.INSERTION_POINT + ".Binder");
      } else if (agentChildren[i].getType().equals(ComponentData.NODEBINDER)) {
        sb.append(AgentManager.INSERTION_POINT + ".Binder");
      } else {
        sb.append(agentChildren[i].getType());
      }
      if (ComponentDescription.parsePriority(agentChildren[i].getPriority()) !=
          ComponentDescription.PRIORITY_COMPONENT) {
        sb.append("(" + agentChildren[i].getPriority() + ")");
      }
      sb.append(" = ");
      sb.append(agentChildren[i].getClassName());
      if (agentChildren[i].parameterCount() != 0) {
        sb.append("(");
        Object[] params = agentChildren[i].getParameters();
        sb.append(params[0].toString());
        for (int j = 1; j < agentChildren[i].parameterCount(); j++) {
          sb.append(",");
          sb.append(params[j].toString());
        }
        sb.append(")");
      }
      entries.add(sb.toString());
    }
    return entries;
  }

  ///////////////////////////////////////////////////////

  // For testing
  public static void main(String[] args) {
    CSMARTConsole console = new CSMARTConsole(null, null);
  }

  class NodeFrameListener implements InternalFrameListener {
    public void internalFrameClosed(InternalFrameEvent e) {
    }

    public void internalFrameClosing(InternalFrameEvent e) {
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
    }

    public void internalFrameIconified(InternalFrameEvent e) {
    }

    public void internalFrameOpened(InternalFrameEvent e) {
    }

    public void internalFrameActivated(InternalFrameEvent e) {
      frameSelected(e);
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
      frameSelected(e);
    }

    /**
     * When frame is selected,
     * select status button and node in configuration tree.
     */
    private void frameSelected(InternalFrameEvent e) {
      ConsoleInternalFrame frame = (ConsoleInternalFrame) e.getInternalFrame();
      String nodeName = frame.getNodeName();
      selectStatusButton(nodeName);
      selectNodeInHostTree(nodeName);
    }
  }

  /**
   * This contains all the information needed to create a node
   * and display its output.  It's passed to a separate thread
   * that creates the node.
   */
  class NodeCreationInfo {
    RemoteHost remoteAppServer;
    Properties properties;
    java.util.List args;
    OutputListener listener;
    OutputPolicy outputPolicy;
    String nodeName;
    String hostName;
    NodeComponent nodeComponent;
    JScrollPane scrollPane;
    NodeStatusButton statusButton;
    String logFileName;

    public NodeCreationInfo(RemoteHost remoteAppServer,
                            Properties properties,
                            java.util.List args,
                            OutputListener listener,
                            OutputPolicy outputPolicy,
                            String nodeName,
                            String hostName,
                            JScrollPane scrollPane,
                            NodeStatusButton statusButton,
                            String logFileName) {
      this.remoteAppServer = remoteAppServer;
      this.properties = properties;
      this.args = args;
      this.listener = listener;
      this.outputPolicy = outputPolicy;
      this.nodeName = nodeName;
      this.hostName = hostName;
      this.scrollPane = scrollPane;
      this.statusButton = statusButton;
      this.logFileName = logFileName;
    }
  }

  private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    createLogger();
  }

} // end of CSMARTConsole


