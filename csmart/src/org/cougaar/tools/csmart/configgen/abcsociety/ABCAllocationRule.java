/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       � Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.configgen.abcsociety;

import org.cougaar.tools.csmart.ui.component.*;

import java.io.*;
import java.lang.StringBuffer;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

public class ABCAllocationRule
  extends ConfigurableComponent
  implements Serializable 
{

  /** Defines a Tasktype Property **/
  public static final String PROP_TASKTYPE = ABCTask.PROP_TASKTYPE;
  public static final String PROP_TASKTYPE_DFLT = ABCTask.PROP_TASKTYPE_DFLT;
  public static final String PROP_TASKTYPE_DESC = ABCTask.PROP_TASKTYPE_DESC;

  /** Defines a Role Property **/
  public static final String PROP_ROLES = ABCLocalAsset.PROP_ROLES;
  public static final String[] PROP_ROLES_DFLT = ABCLocalAsset.PROP_ROLES_DFLT;
  public static final String PROP_ROLES_DESC = ABCLocalAsset.PROP_ROLES_DESC;

  private Property propTaskType;
  private Property propRoles;

  ABCAllocationRule() {
    super("rule");
  }

  /**
   * Initializes all Properties
   */
  public void initProperties() {
    propTaskType = addProperty(PROP_TASKTYPE, PROP_TASKTYPE_DFLT);
    propTaskType.setToolTip(PROP_TASKTYPE_DESC);

    propRoles = addProperty(PROP_ROLES, PROP_ROLES_DFLT);
    propRoles.setToolTip(PROP_ROLES_DESC);
  }

  /**
   * Generates a configuration line containing all values required
   * for am Allocation rule.  <br>
   * An allocation rule is in the form of:   <br>
   * rule, <Task Type>, <role>*
   *
   * @return String Configuration Line
   */  
  public String getConfigLine() {
    StringBuffer sb = new StringBuffer(40);

    sb.append("rule, ");
    sb.append((String)propTaskType.getValue());

    String[] roles = (String[])propRoles.getValue();
    for(int i=0; i < roles.length; i++) {
      sb.append(", ");
      sb.append(roles[i]);
    }

    return sb.toString();
  }
}
