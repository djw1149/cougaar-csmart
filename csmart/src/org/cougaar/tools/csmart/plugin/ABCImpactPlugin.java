package org.cougaar.tools.csmart.plugin;

import java.util.*;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.tools.csmart.ldm.event.InfrastructureEvent;
import org.cougaar.tools.csmart.binder.SlowMessageTransportServiceProxyController;
import org.cougaar.tools.csmart.binder.SlowMessageTransportServiceFilter;

/**
 * Plugin that looks for <code>InfrastructureEvent</code>s and 
 * modifies the <code>SlowMessageTransportServiceProxyController</code>
 * to simulate degraded MessageTransport service.
 *
 * @see SlowMessageTransportServiceFilter must be loaded in the Node ini file
 * @see SlowMessageTransportServiceProxyController used to control the I/O
 */
public class ABCImpactPlugin extends SimplePlugIn {

  private static final boolean VERBOSE = false;

  private SlowMessageTransportServiceProxyController mtController;

  private IncrementalSubscription infEventSub;

  private UnaryPredicate createInfEventPred() {
    final ClusterIdentifier myCID = getClusterIdentifier();
    return
      new UnaryPredicate() {
        public boolean execute(Object o) {
          return 
            ((o instanceof InfrastructureEvent) &&
             (myCID.equals(((InfrastructureEvent)o).getDestination())));
        }
      };
  }

  /**
   * Find the MessageTransport controller, subscribe to InfrastructureEvents.
   */
  public void setupSubscriptions() {
    if (VERBOSE) {
      System.out.println(
          this+" setting up subscriptions");
    }

    // get the service broker
    ServiceBroker serviceBroker = getBindingSite().getServiceBroker();

    // get the MessageTransport controller 
    mtController = (SlowMessageTransportServiceProxyController)
      serviceBroker.getService(
          this, 
          SlowMessageTransportServiceProxyController.class, 
          null);
    if (mtController == null) {
      // no controller, never run!
      if (VERBOSE) {
        System.out.println(
            this+" unable to find the MessageTransport controller");
      }
      return;
    }

    if (VERBOSE) {
      System.out.println(
          this+" subscribing to InfrastructureEvents");
    }

    // subscribe to InfrastructureEvents
    infEventSub = (IncrementalSubscription)
      subscribe(createInfEventPred());
  }

  public void execute() {
    if (infEventSub.hasChanged()) {
      // for each new task
      for (Enumeration en = infEventSub.getAddedList(); 
          en.hasMoreElements(); 
          ) {
        InfrastructureEvent ie = (InfrastructureEvent)en.nextElement();
        if (VERBOSE) {
          System.out.println(
              this+" handling added InfEvent: "+ie);
        }
        if (ie.isWireType()) {
          // MessageTransport degradation
          //
          // ignore the transit time:
          //   (System.currentTimeMillis() - ie.getTime())
          // and just degrade as instructed:
          try {
            mtController.degradeReleaseRate(
                (1.0 - ie.getIntensity()),
                ie.getDuration());
            if (VERBOSE) {
              System.out.println(
                  this+" MessageTransport degraded by "+
                  ((int)(100.0 * ie.getIntensity()))+
                  "% for "+ie.getDuration()+" milliseconds");
            }
          } catch (Exception e) {
            // illegal parameters?
            System.err.println(
                this+" unable to degrade MessageTransport: "+e);
          }
        } else {
          // CPU-degradation not implemented yet
        }
      }
    }
  }
}
