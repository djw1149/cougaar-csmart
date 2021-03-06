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

package org.cougaar.tools.csmart.ui.servlet;





import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.CommunityPG;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.util.PropertyTree;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

/**
 * Returns agent name and community name from Entity object. <br>
 * Expects no input. 
 *
 * <p>
 * Can be loaded manually by including this line in an agent's .ini configuration file: <pre/>
 *   plugin = org.cougaar.core.servlet.SimpleServletComponent(org.cougaar.tools.csmart.ui.servlet.CommunityProviderServlet, 
 *   /CSMART_CommunityProviderServlet) </pre>
 *
 * <p>
 * Is loaded from a URL on a CSMART machine, on agent 'Agent':
 *   http://localhost:port/$Agent/CSMART_CommunityProviderServlet
 */
public class CommunityProviderServlet 
  extends HttpServlet
{
  private SimpleServletSupport support;

  public void setSimpleServletSupport(SimpleServletSupport support) {
    this.support = support;
    if ( !  ( "/CSMART_CommunityProviderServlet".equals(support.getPath()) ) ) {
      support.getLog().error("Incorrect servlet path: " + support.getPath());
    }
  }

  public void doGet(
		    HttpServletRequest request,
		    HttpServletResponse response) throws IOException, ServletException
  {
    // create a new "CommunityProvider" context per request
    CommunityProvider cp = new CommunityProvider(support);
    cp.execute(request, response);  
  }
  
  public void doPost(
		     HttpServletRequest request,
		     HttpServletResponse response) throws IOException, ServletException
  {
    // create a new "CommunityProvider" context per request
    CommunityProvider cp = new CommunityProvider(support);
    cp.execute(request, response);  
  }
  
  
  /**
   * This inner class does all the work.
   * <p>
   * A new class is created per request, to keep all the
   * instance fields separate.  If there was only one
   * instance then multiple simultaneous requests would
   * corrupt the instance fields (e.g. the "out" stream).
   * <p>
   * This acts as a <b>context</b> per request.
   */
  private static class CommunityProvider {
    
    /*
     * parameters from the URL:
     */
    ServletOutputStream out; 

    /* since "ClusterProvider" is a static inner class, here
     * we hold onto the support API.
     *
     * this makes it clear that ClusterProvider only uses
     * the "support" from the outer class.
     */      
    private SimpleServletSupport support;
    private Logger log;
    
    // inner class constructor
    public CommunityProvider(SimpleServletSupport support) {
      this.support = support;
      this.log = support.getLog();
    }
   
    /**
     * Called when a request is received from a client.
     * Get the POST data; parse the request; get the objects
     * that match the request; send them to the client.
     */
    public void execute( HttpServletRequest request, 
			 HttpServletResponse response) throws IOException, ServletException 
    {
      
      /*
      this.out = response.getOutputStream();

      //There is no mode, as it only returns the list of agent
      //names, so do nothing but set up for parameter parsing.   
      */

      if( request.getQueryString() == null) {
	response.setStatus(
			   HttpServletResponse.SC_OK);
      }
      
      // check for illegal arguments
      if( request.getQueryString() != null) {
	response.sendError(
			   HttpServletResponse.SC_BAD_REQUEST,
			   "<html><body><br><font size=4>" + 
			   "<font size = 5><B>" + 
			   request.getQueryString() + 
			   "</B></font>" +
			   " Is Not A Legal Parameter List<br>" + 
			   "This servlet expects no parameters" +
			   "</font></body></html>");
      }
      
      this.out = response.getOutputStream();
      
      // need try/catch here or caller sends exceptions to client as html
      try {

        if(log.isDebugEnabled()) {
          log.debug("CSMART_CommunityProviderServlet received query..........");
        }

	StringBuffer buf = request.getRequestURL();
	Vector collection = getSelfInformation(buf);
	//out.print(collection);
	
	ObjectOutputStream p = new ObjectOutputStream(out);
	p.writeObject(collection);

        if(log.isDebugEnabled()) {
          log.debug("Sent agent urls");
        }

      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("CSMART_CommunityProviderServlet Exception", e);
        }
      }
    }

    /**
     * Get object which describes this agent and its community.
     */
    private static UnaryPredicate getSelfPred() {
      return new UnaryPredicate() {
	  public boolean execute(Object obj) {
	    if (obj instanceof Asset) {
	      Asset asset = (Asset)obj;
	      if ((asset instanceof HasRelationships) &&
		  ((HasRelationships)asset).isLocal() &&
		  asset.hasClusterPG())
		return true;
	    }
	    return false;
	  }
	};
    }
    
    /**
     * Get agent and community name for this agent.
     * Returns a vector which contains a single PropertyTree which contains the
     * properties for this agent.
     */
    private Vector getSelfInformation(StringBuffer buf) {
      Collection container = 
	support.queryBlackboard(getSelfPred());
      Iterator iter = container.iterator();
      Vector results = new Vector(1);
      //      int n = 0; // unique index for relationships
      while (iter.hasNext()) {
	Asset asset = (Asset)iter.next();
	PropertyTree properties = new PropertyTree();
	properties.put(PropertyNames.UID_ATTR, getUIDAsString(asset.getUID()));
	//      String name = asset.getItemIdentificationPG().getNomenclature();
	// THIS METHOD OF ACCESSING AGENT NAME MUST MATCH HOW OTHER ServletS
	// (ESPECIALLY CSMART_PlanServlet) ACCESS THE AGENT NAME SO COMPARISONS
	// CAN BE MADE AT THE CLIENT
	String name = support.getEncodedAgentName();
	properties.put(PropertyNames.AGENT_NAME, name);
	CommunityPG communityPG = asset.getCommunityPG(new Date().getTime());
	String communityName = null;
	if (communityPG != null) {
	  Collection communities = communityPG.getCommunities();
	  if (communities.size() > 1) 
            if(log.isWarnEnabled()) {
              log.warn("CSMART_CommunityProviderServlet: WARNING: " + 
                       "handling agents in multiple communities is not implemented.");
            }
	  Iterator i = communities.iterator();
	  while (i.hasNext()) {
	    communityName = (String)i.next();
	    break;
	  }
	} else {
	  communityName = "COUGAAR";
	}
	if (communityName != null)
	  properties.put(PropertyNames.AGENT_COMMUNITY_NAME, communityName);
      
	// reconstruct url
	String url = buf.toString();
	//URL url = psc.lookupURL(psc.getServerPluginSupport().getClusterIDAsString());
	if (url != null)
	  properties.put(PropertyNames.AGENT_URL, url.toString());
	results.add(properties);
      }
      return results;
    }
    
    private static final String getUIDAsString(final UID uid) {
      return
	((uid != null) ? uid.toString() : "null");
    }    
  }
}

