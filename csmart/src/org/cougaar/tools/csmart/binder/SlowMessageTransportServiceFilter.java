/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       � Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.binder;

import java.util.List;

import org.cougaar.core.component.*;
import org.cougaar.core.society.*;

/**
 * A <code>ServiceFilter</code> to wrap the <code>MessageTransportServer</code> 
 * and simulate a degradation in I/O capacity (via increased-latency).
 * <p>
 * Specify in the Node.ini file as:<pre>
 *   Node.AgentManager = org.cougaar.tools.csmart.binder.SlowMessageTransportServiceFilter(X, Y, Z)
 * </pre>
 * See <tt>setParameter(..)</tt> for the required parameters.
 *
 * @see #setParameter(Object) required parameters for this Component
 *
 * @see SlowMessageTransportServiceProxyController controller API for further
 *    I/O degradation
 */
public class SlowMessageTransportServiceFilter 
  extends ServiceFilter 
{
  private static final boolean VERBOSE = false;

  private double samplesPerSecond;
  private double inMessagesPerSecond;
  private double outMessagesPerSecond;

  protected Class getBinderClass(Object child) {
    return SlowMessageTransportServiceFilterBinder.class;
  }

  /**
   * There are three necessary parameters:<ol>
   *   <li>a String double for     samplesPerSecond</li>
   *   <li>a String double for  inMessagesPerSecond</li>
   *   <li>a String double for outMessagesPerSecond</li>
   * </ol>.
   * <p>
   * For example, a very slow MessageTransport might use "(1, 0.5, 0.5)" to 
   * send and receive a message every other second.  More typical values
   * might be "(2, 10, 10)".
   * <p>
   * The Container calls this method when loading this ServiceFilter.
   *
   * @param o a List matching the above specification
   */
  public void setParameter(Object o) {
    try {
      List l = (List)o;
      samplesPerSecond = Double.parseDouble((String)l.get(0));
      inMessagesPerSecond = Double.parseDouble((String)l.get(1));
      outMessagesPerSecond = Double.parseDouble((String)l.get(2));
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "SlowMessageTransportServiceFilter expecting three arguments:\n"+
          "  double samplesPerSecond,\n"+
          "  double inMessagesPerSecond,\n"+
          "  double outMessagesPerSecond\n"+
          "e.g. (2, 10, 10)");
    }
  }

  /**
   * Wrap a <code>MessageTransportServer</code>.
   */
  public SlowMessageTransportServiceProxy createSlowMessageTransportServiceProxy(
      MessageTransportServer origMT,
      Object requestor) {
    // create a new wrapped MessageTransportServer
    //
    // requestor should be an Agent
    MessageReleaseScheduler mrs = 
      new MessageReleaseSchedulerImpl(
          samplesPerSecond, 
          inMessagesPerSecond, 
          outMessagesPerSecond);
    return
      new SlowMessageTransportServiceProxy(
          origMT,
          requestor,
          mrs);
  }

  //
  // Lots of Container guts...
  //

  public static class SlowMessageTransportServiceFilterBinder 
    extends ServiceFilterBinder 
  {
    private SlowMessageTransportServiceFilter 
      SlowMessageTransportServiceFilter_this;

    public SlowMessageTransportServiceFilterBinder(
        BinderFactory bf, 
        Object child) {
      super(bf, child);
      SlowMessageTransportServiceFilter_this = 
        (SlowMessageTransportServiceFilter)bf;
    }

    protected ContainerAPI createContainerProxy() {
      // parent's API is fine ... might replace later
      return getContainer();
    }

    protected ServiceBroker createFilteringServiceBroker(ServiceBroker sb) {
      return new SlowMessageTransportFilteringServiceBroker(sb);
    }

    protected class SlowMessageTransportFilteringServiceBroker
      extends FilteringServiceBroker 
    {
      private SlowMessageTransportServiceProxy smt;
      public SlowMessageTransportFilteringServiceBroker(ServiceBroker sb) {
        super(sb);
        if (VERBOSE) {
          System.out.println("created smtfsb with "+sb);
        }
      }
      public Object getService(
          Object requestor, 
          Class serviceClass,
          ServiceRevokedListener srl) {
        if (serviceClass == SlowMessageTransportServiceProxyController.class) {
          if (VERBOSE) {
            System.out.println("lookup controller!");
          }
          // request for the Controller API for our wrapped service
          return smt;
        } else {
          if (VERBOSE) {
            System.out.println("smtfsb.askSuper for "+serviceClass);
          }
          return super.getService(requestor, serviceClass, srl);
        }
      }
      protected Object getServiceProxy(
          Object service, 
          Class serviceClass, 
          Object client) {
        if (VERBOSE) {
          System.out.println("getServiceProxy");
        }
        if (service instanceof MessageTransportServer) {
          if (VERBOSE) {
            System.out.println("get mt service");
          }
          if (smt == null) {
            // create a new wrapped MessageTransportServer
            this.smt = 
              SlowMessageTransportServiceFilter_this.createSlowMessageTransportServiceProxy(
                  (MessageTransportServer)service,
                  client);
          }
          return smt;
        } else {
          if (VERBOSE) {
            System.out.println("lack other service: "+serviceClass);
          }
        }
        return null;
      }
    }
  }
}
