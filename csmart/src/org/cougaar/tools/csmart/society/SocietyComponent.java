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
package org.cougaar.tools.csmart.society;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;

import org.cougaar.tools.csmart.core.property.ModifiableComponent;
/**
 * The interface for adding and removing nodes from a society.
 */
public interface SocietyComponent extends ModifiableComponent {

  void setName(String newName);

  /**
   * Get the name of the society.
   * @return the name of the society
   */
  String getSocietyName();

  /**
   * Get the agents, both assigned and unassigned.
   * @return array of agent components
   */
  AgentComponent[] getAgents();

  /**
   * Returns the description of this society
   *
   * @return an <code>URL</code> value
   */
  URL getDescription();

  /**
   * Set by the experiment controller to indicate that the
   * society is running.
   * The society is running from the moment that any node
   * is successfully created 
   * (via the app-server's "create" method)
   * until all nodes are terminated (aborted, self terminated, or
   * manually terminated).
   * @param flag indicating whether or not the society is running
   */
  void setRunning(boolean isRunning);

  /**
   * Returns whether or not the society is running, 
   * i.e. can be dynamically monitored. 
   * Running societies are not editable, but they can be copied,
   * and the copy can be edited.
   * @return true if society is running and false otherwise
   */
  boolean isRunning();

  /**
   * Return a file filter which can be used to fetch
   * the metrics files for this experiment. 
   * @return <code>FileFilter</code> to get metrics files for this experiment
   */
  FileFilter getResultFileFilter();

  /**
   * Return a file filter which can be used to delete
   * the files generated by this experiment.
   * @return <code>FileFilter</code> for cleanup
   */
  FileFilter getCleanupFileFilter();

  /**
   * Returns whether the society is self terminating or must
   * be manually terminated.
   * Self terminating nodes cause the app-server to send back
   * a "process-destroyed" message when the node terminates.
   * @return true if society is self terminating
   */
  boolean isSelfTerminating();

  /**
   * Save society to database.
   */

  void saveToDatabase();

}
