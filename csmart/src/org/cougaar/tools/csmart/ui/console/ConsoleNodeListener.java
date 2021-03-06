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

package org.cougaar.tools.csmart.ui.console;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.server.NodeEvent;
import org.cougaar.tools.server.OutputBundle;
import org.cougaar.tools.server.OutputListener;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Writer;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Listener for "pushed" Node activities. Handles NodeStatus button color
 * changes, writing content to log file, measuring Node "idleness".
 * This is object called from the AppServer.
 */
public class ConsoleNodeListener implements OutputListener {
  private String logFileName;
  private Writer logFile;
  private SimpleAttributeSet[] atts;
  public NodeStatusButton statusButton;
  private long firsttime = 0l;
  private ConsoleStyledDocument doc;
  private ConsoleNodeOutputFilter filter = null;
  private Object logFileLock = new Object();
  private int nLogEvents = 0;
  private TimerTask logFlushTask;
  private transient Logger log;
  private NodeModel nodeModel;

  public ConsoleNodeListener(NodeModel nodeModel) {
    createLogger();
    this.nodeModel = nodeModel;

    // status button for node on console display
    this.statusButton = nodeModel.getStatusButton();

    // wrap and capture to a log
    this.logFileName = nodeModel.getLogFileName();
    // Create new log file, always appending to the end - so a Node restart
    // can write to the end of the file, not losing data
    try {
      this.logFile = new BufferedWriter(new FileWriter(logFileName, true));
    } catch (IOException ie) {
      this.logFile = null;
      System.out.println("Couldn't create log file");
    }

    // FIXME (problem Mark Barger saw once) test to make sure logFile will be writable

    // save the document that contains node output
    this.doc = nodeModel.getDoc();

    // create our attributes
    // stdout	0,0,0 (black)
    // stderr	192,64,64 (dark red)
    // Node created, Node destroyed, Agent Added	64,64,192 (dark blue)
    atts = new SimpleAttributeSet[3];
    atts[0] = new SimpleAttributeSet();
    StyleConstants.setForeground(atts[0], Color.black);
    atts[1] = new SimpleAttributeSet();
    StyleConstants.setForeground(atts[1], new Color(192, 64, 64));
    atts[2] = new SimpleAttributeSet();
    StyleConstants.setForeground(atts[2], new Color(64, 64, 192));
    // write the log file every 5 seconds
    logFlushTask = new TimerTask() {
      public void run() {
        synchronized (logFileLock) {
          try {
            logFile.flush();
          } catch (Exception e) {
          }
        }
      }
    };
    new Timer().schedule(logFlushTask, new Date(), 5000);
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Pull out the idleness number from a NodeEvent message.<br>
   * The idleness is:
   * <pre>(actual time difference between wakeups) - (expected difference) / (expected difference)</pre>
   *
   * @param nodemsg a <code>String</code> NodeEvent message to parse
   * @return a <code>double</code> idleness number, 0 on error
   */
  private double getIdleness(String nodemsg) {
    // node message is idletime:time of snapshot
    // where idletime is (actual time diff) - (expected time diff) / (expected time diff)
    // as a double
    // and time of snapshot is a long, as returned by System.currentTimeMillis()
    if (nodemsg == null || nodemsg.equals("")) {
      return 0.0;
    }
    String idle = nodemsg.substring(0, nodemsg.indexOf(':'));
    double ret = 0.0;
    try {
      ret = Double.parseDouble(idle);
    } catch (NumberFormatException e) {
      if (log.isErrorEnabled()) {
        log.error("Couldn't parse that idle string", e);
      }
    }
    return ret;
  }

  /**
   * Get the timestamp of the idleness snapshot
   *
   * @param nodemsg a <code>String</code> NodeEvent message
   * @return a <code>long</code> timestamp in milliseconds
   */
  private long getTimestamp(String nodemsg) {
    // node message is idletime:time of snapshot
    // where idletime is (actual time diff) - (expected time diff) / (expected time diff)
    // as a double
    // and time of snapshot is a long, as returned by System.currentTimeMillis()
    if (nodemsg == null || nodemsg.equals("")) {
      return 0L;
    }
    String time = nodemsg.substring(nodemsg.indexOf(':') + 1);
    long ret = 0L;
    try {
      ret = Long.parseLong(time);
    } catch (NumberFormatException e) {
      if (log.isErrorEnabled()) {
        log.error("Couldn't parse that time string", e);
      }
    }
    return ret;
  }

  /**
   * Called by appserver to stream standard output and standard err
   * to this listener.
   * @param outputBundle the standard out and err
   */
  public void handleOutputBundle(OutputBundle outputBundle) {
    // fix to use the new app-server APIs
    // see the app-server example "GuiConsole"
    java.util.List l =
        org.cougaar.tools.server.NodeEventTranslator.toNodeEvents(outputBundle);
    handleAll(l);
  }


  /**
   * Handle the specified list of node events (List of NodeEvent)
   * from a node. Same as handle, but for a list of events.
   */
  private void handleAll(java.util.List nodeEvents) {
    int n = nodeEvents.size();
    if (n <= 0) {
      return;
    }

    // write a description of the event to the log file
    synchronized (logFileLock) {
      try {
        int i = 0;
        do {
          NodeEvent nodeEvent = (NodeEvent) nodeEvents.get(i);
          if (logFile != null)
            logFile.write(getNodeEventDescription(nodeEvent));
          nLogEvents++;
          if (nLogEvents > 100) {
            if (logFile != null)
              logFile.flush();
            nLogEvents = 0;
          }
        } while (++i < n);
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception writing to Node log file " + logFileName + ": ", e);
        }
        // FIXME: Is there any way to recover? Perhaps flush the log file,
        // and then re-open it?
      }
    }

    // must use swing "invokeLater" to be thread-safe
    // collects descriptions of the same type and batch writes them to display
    // handles idle updates separately
    try {
      SwingUtilities.invokeLater(new NodeEventHandler(nodeEvents));
    } catch (RuntimeException e) {
    }
  }

  /**
   * Thread for handling incoming NodeEvents. Trying to avoid
   * declaring anything final.
   **/
  class NodeEventHandler implements Runnable {
    java.util.List nodeEvents = null;
    int n = 0;

    public NodeEventHandler(java.util.List nodeEvents) {
      // nodeEvents
      this.nodeEvents = nodeEvents;
      this.n = nodeEvents.size();
    }

    public void run() {
      int prevType = -1;
      String prevDescription = "";
      int nextEventIndex = -1;
      // get the first description which is not an idle update
      for (int i = 0; i < n; i++) {
        NodeEvent nodeEvent = (NodeEvent) nodeEvents.get(i);
        int nodeEventType = nodeEvent.getType();
        if (nodeEventType == NodeEvent.IDLE_UPDATE) {
          String s = nodeEvent.getMessage();
          handleIdleUpdate(getIdleness(s), getTimestamp(s));
        } else {
          updateStatus(nodeEvent);
          if (filter != null && !filter.includeEventInDisplay(nodeEvent))
            continue; // don't append events user isn't interested in
          prevType = nodeEventType;
          prevDescription = getNodeEventDescription(nodeEvent);
          nextEventIndex = i + 1;
          break;
        }
      }

      if (nextEventIndex == -1)
        return; // all the events were idle updates or ignored
      // start batching descriptions
      for (int j = nextEventIndex; j < n; j++) {
        NodeEvent nodeEvent = (NodeEvent) nodeEvents.get(j);
        int nodeEventType = nodeEvent.getType();
        String description = getNodeEventDescription(nodeEvent);
        updateStatus(nodeEvent);
        if (nodeEventType == NodeEvent.IDLE_UPDATE) {
          String s = nodeEvent.getMessage();
          handleIdleUpdate(getIdleness(s), getTimestamp(s));
        } else if (nodeEventType == prevType) {
          prevDescription += description;
        } else {
          if (filter == null || filter.includeEventTypeInDisplay(prevType)) {
            if (doc != null)
              doc.appendString(prevDescription,
                               getNodeEventStyle(prevType));
          }
          prevDescription = description;
          prevType = nodeEventType;
        }
      }
      if (filter == null || filter.includeEventTypeInDisplay(prevType)) {
        if (doc != null)
          doc.appendString(prevDescription,
                           getNodeEventStyle(prevType));
      }
    }
  }

  /**
   * Get Node Event description.
   */
  private final String getNodeEventDescription(final NodeEvent nodeEvent) {
    switch (nodeEvent.getType()) {
      case NodeEvent.STANDARD_OUT:
      case NodeEvent.STANDARD_ERR:
        return nodeEvent.getMessage();
      default:
        return nodeEvent.toString();
    }
  }

  /**
   * Returns attribute set for a style of output:
   * stdout, stderr, heartbeat, or default.
   */
  private final SimpleAttributeSet getNodeEventStyle(final int type) {
    switch (type) {
      case NodeEvent.STANDARD_OUT:
        return atts[0];
      case NodeEvent.STANDARD_ERR:
        return atts[1];
      default:
        return atts[2];
    }
  }

  private void handleIdleUpdate(double idleTime, long timestamp) {
    double result = 1 / Math.log(idleTime);
    result = (result + 1) * 50; // in range 0 to 100
    if (statusButton != null) {
      if (result <= 16)
        statusButton.getMyModel().setStatus(NodeStatusButton.STATUS_LOW_BUSY);
      else if (result > 16 && result <= 33)
        statusButton.getMyModel().setStatus(NodeStatusButton.STATUS_MEDIUM_LOW_BUSY);
      else if (result > 33 && result <= 50)
        statusButton.getMyModel().setStatus(NodeStatusButton.STATUS_MEDIUM_BUSY);
      else if (result > 50 && result <= 67)
        statusButton.getMyModel().setStatus(NodeStatusButton.STATUS_MEDIUM_HIGH_BUSY);
      else if (result > 67 && result <= 83)
        statusButton.getMyModel().setStatus(NodeStatusButton.STATUS_HIGH_BUSY);
      else if (result > 83)
        statusButton.getMyModel().setStatus(NodeStatusButton.STATUS_BUSY);
    }

    // reset times to 0. Maybe this looks better?
    if (firsttime > 0) {
      firsttime = timestamp;
    }

  }

  private void updateStatus(NodeEvent nodeEvent) {
    int nodeEventType = nodeEvent.getType();
    if (nodeEventType == NodeEvent.PROCESS_CREATED) {
      if (statusButton != null)
        statusButton.getMyModel().setStatus(NodeStatusButton.STATUS_NODE_CREATED);
    } else if (nodeEventType == NodeEvent.PROCESS_DESTROYED) {
      if (statusButton != null)
        statusButton.getMyModel().setStatus(NodeStatusButton.STATUS_NODE_DESTROYED);
      nodeModel.stopped();
    } else if (nodeEventType == NodeEvent.STANDARD_ERR) {
      if (statusButton != null)
        statusButton.getMyModel().setStatus(NodeStatusButton.STATUS_STD_ERROR);
    }
  }


  /**
   * Flush and close the log file.
   */
  public void closeLogFile() {
    synchronized (logFileLock) {
      try {
        if (logFile != null) {
          logFile.close();
          logFile = null;
        }
        logFlushTask.cancel();
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception closing log file: ", e);
        }
      }
    }
  }

  /**
   * Set the console node output filter used to filter events displayed.
   */
  public void setFilter(ConsoleNodeOutputFilter filter) {
    this.filter = filter;
  }

  /**
   * Get the console node output filter used to filter events displayed.
   */
  public ConsoleNodeOutputFilter getFilter() {
    return filter;
  }

  /**
   * When completely done with this guy, close out the log file,
   * and set my pointers to null for gc.
   **/
  public void cleanUp() {
    if (log.isDebugEnabled())
      log.debug("Listener.cleanup");
    closeLogFile();
    // Don't kill the document cause we call this
    // when the Node has stopped
    doc = null;
    filter = null;
    synchronized (logFileLock) {
      logFile = null;
    }
    statusButton = null;
  }

  private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    createLogger();
  }

}



