/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

/* @generated Tue May 15 11:07:43 EDT 2001 from csmartProps.def - DO NOT HAND EDIT */
/** Abstract Asset Skeleton implementation
 * Implements default property getters, and additional property
 * lists.
 * Intended to be extended by org.cougaar.domain.planning.ldm.asset.Asset
 **/

package org.cougaar.tools.csmart.ldm.asset;

import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;
import java.util.*;


import java.io.Serializable;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;

public abstract class AssetSkeleton extends org.cougaar.domain.planning.ldm.asset.Asset {

  protected AssetSkeleton() {}

  protected AssetSkeleton(AssetSkeleton prototype) {
    super(prototype);
  }

  /**                 Default PG accessors               **/

  /** Search additional properties for a HappinessPG instance.
   * @return instance of HappinessPG or null.
   **/
  public HappinessPG getHappinessPG()
  {
    HappinessPG _tmp = (HappinessPG) resolvePG(HappinessPG.class);
    return (_tmp==HappinessPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a HappinessPG
   **/
  public boolean hasHappinessPG() {

    return (getHappinessPG() != null);
  }

  /** Set the HappinessPG property.
   * The default implementation will create a new HappinessPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setHappinessPG(PropertyGroup aHappinessPG) {
    if (aHappinessPG == null) {
      removeOtherPropertyGroup(HappinessPG.class);
    } else {
      addOtherPropertyGroup(aHappinessPG);
    }
  }

  /** Search additional properties for a SimpleInventoryPG instance.
   * @return instance of SimpleInventoryPG or null.
   **/
  public SimpleInventoryPG getSimpleInventoryPG()
  {
    SimpleInventoryPG _tmp = (SimpleInventoryPG) resolvePG(SimpleInventoryPG.class);
    return (_tmp==SimpleInventoryPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a SimpleInventoryPG
   **/
  public boolean hasSimpleInventoryPG() {

    return (getSimpleInventoryPG() != null);
  }

  /** Set the SimpleInventoryPG property.
   * The default implementation will create a new SimpleInventoryPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setSimpleInventoryPG(PropertyGroup aSimpleInventoryPG) {
    if (aSimpleInventoryPG == null) {
      removeOtherPropertyGroup(SimpleInventoryPG.class);
    } else {
      addOtherPropertyGroup(aSimpleInventoryPG);
    }
  }

  /** Search additional properties for a RolesPG instance.
   * @return instance of RolesPG or null.
   **/
  public RolesPG getRolesPG()
  {
    RolesPG _tmp = (RolesPG) resolvePG(RolesPG.class);
    return (_tmp==RolesPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a RolesPG
   **/
  public boolean hasRolesPG() {

    return (getRolesPG() != null);
  }

  /** Set the RolesPG property.
   * The default implementation will create a new RolesPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setRolesPG(PropertyGroup aRolesPG) {
    if (aRolesPG == null) {
      removeOtherPropertyGroup(RolesPG.class);
    } else {
      addOtherPropertyGroup(aRolesPG);
    }
  }

}
