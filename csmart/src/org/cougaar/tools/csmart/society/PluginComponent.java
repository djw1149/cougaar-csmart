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

import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.Property;

/**
 * A simple interface implemented by configurable components 
 * that represent plugins. <br>
 */
public interface PluginComponent extends ModifiableComponent {

  /**
   * Gets the classname for this plugin
   *
   * @return a <code>String</code> value
   */
  String getPluginClassName();

  /**
   * Adds a Parameter to this component.
   *
   * @param param Unique Integer for this parameter
   * @return a <code>Property</code> value
   */
  Property addParameter(int param);

  /**
   * Adds a Parameter to this component
   *
   * @param param Unique string for this parameter
   * @return a <code>Property</code> value
   */
  Property addParameter(String param);

  /**
   * Add a parameter that is based on some other property (typically a
   * property of our parent).
   **/
  Property addParameter(Property prop);

}