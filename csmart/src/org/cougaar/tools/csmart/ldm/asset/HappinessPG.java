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
/** Primary client interface for HappinessPG.
 * wrapper for HappinessBG
 *  @see NewHappinessPG
 *  @see HappinessPGImpl
 **/

package org.cougaar.tools.csmart.ldm.asset;

import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;
import java.util.*;



public interface HappinessPG extends PropertyGroup, org.cougaar.domain.planning.ldm.dq.HasDataQuality {

  long getStartTime();
  long getEndTime();
  double getHappinessAt(long time);
  void setHappinessAt(long time, double happiness);
  double[] toArray(long startTime, long endTime, long timeIncrement);
  double[] toArray(double[] toA, long startTime, long endTime, long timeIncrement);
  String toString();
  String toString(boolean verbose);
  // introspection and construction
  /** the method of factoryClass that creates this type **/
  public static final String factoryMethod = "newHappinessPG";
  /** the (mutable) class type returned by factoryMethod **/
  public static final String mutableClass = "org.cougaar.tools.csmart.ldm.asset.NewHappinessPG";
  /** the factory class **/
  public static final Class factoryClass = org.cougaar.tools.csmart.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
  public static final Class primaryClass = org.cougaar.tools.csmart.ldm.asset.HappinessPG.class;
  public static final String assetSetter = "setHappinessPG";
  public static final String assetGetter = "getHappinessPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  public static final HappinessPG nullPG = new Null_HappinessPG();

/** Null_PG implementation for HappinessPG **/
static final class Null_HappinessPG
  implements HappinessPG, Null_PG
{
  public long getStartTime() { throw new UndefinedValueException(); }
  public long getEndTime() { throw new UndefinedValueException(); }
  public double getHappinessAt(long time) { throw new UndefinedValueException(); }
  public void setHappinessAt(long time, double happiness) { throw new UndefinedValueException(); }
  public double[] toArray(long startTime, long endTime, long timeIncrement) { throw new UndefinedValueException(); }
  public double[] toArray(double[] toA, long startTime, long endTime, long timeIncrement) { throw new UndefinedValueException(); }
  public String toString() { throw new UndefinedValueException(); }
  public String toString(boolean verbose) { throw new UndefinedValueException(); }
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
  public NewPropertyGroup unlock(Object key) { return null; }
  public PropertyGroup lock(Object key) { return null; }
  public PropertyGroup lock() { return null; }
  public PropertyGroup copy() { return null; }
  public Class getPrimaryClass(){return primaryClass;}
  public String getAssetGetMethod() {return assetGetter;}
  public String getAssetSetMethod() {return assetSetter;}
  public Class getIntrospectionClass() {
    return HappinessPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.domain.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for HappinessPG **/
public final static class Future
  implements HappinessPG, Future_PG
{
  public long getStartTime() {
    waitForFinalize();
    return _real.getStartTime();
  }
  public long getEndTime() {
    waitForFinalize();
    return _real.getEndTime();
  }
  public double getHappinessAt(long time) {
    waitForFinalize();
    return _real.getHappinessAt(time);
  }
  public void setHappinessAt(long time, double happiness) {
    waitForFinalize();
    _real.setHappinessAt(time, happiness);
  }
  public double[] toArray(long startTime, long endTime, long timeIncrement) {
    waitForFinalize();
    return _real.toArray(startTime, endTime, timeIncrement);
  }
  public double[] toArray(double[] toA, long startTime, long endTime, long timeIncrement) {
    waitForFinalize();
    return _real.toArray(toA, startTime, endTime, timeIncrement);
  }
  public String toString() {
    waitForFinalize();
    return _real.toString();
  }
  public String toString(boolean verbose) {
    waitForFinalize();
    return _real.toString(verbose);
  }
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
  public NewPropertyGroup unlock(Object key) { return null; }
  public PropertyGroup lock(Object key) { return null; }
  public PropertyGroup lock() { return null; }
  public PropertyGroup copy() { return null; }
  public Class getPrimaryClass(){return primaryClass;}
  public String getAssetGetMethod() {return assetGetter;}
  public String getAssetSetMethod() {return assetSetter;}
  public Class getIntrospectionClass() {
    return HappinessPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.domain.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private HappinessPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof HappinessPG) {
      _real=(HappinessPG) real;
      notifyAll();
    } else {
      throw new IllegalArgumentException("Finalization with wrong class: "+real);
    }
  }
  private synchronized void waitForFinalize() {
    while (_real == null) {
      try {
        wait();
      } catch (InterruptedException _ie) {}
    }
  }
}
}
