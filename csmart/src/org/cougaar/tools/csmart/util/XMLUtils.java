/**
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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
 *  </copyright>
 */
package org.cougaar.tools.csmart.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.StringBuffer;
import org.apache.xerces.parsers.DOMParser;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;


/**
 * XMLUtils.java
 *
 *
 * Created: Wed Jun  5 10:41:48 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class XMLUtils {
  private Logger log;

  public XMLUtils() {
    log = CSMART.createLogger("org.cougaar.tools.csmart.util.XMLUtils");
  }

  public Document loadXMLFile(String filename) {
    try {
      DOMParser parser = new DOMParser();
      parser.parse(new InputSource(ConfigFinder.getInstance().open(filename)));
      return parser.getDocument();
    } catch (org.xml.sax.SAXParseException spe) {
      if (log.isErrorEnabled()) {
	log.error("Parse exception Parsing file: " + filename, spe);
      }
    } catch (org.xml.sax.SAXException se) {
      if (log.isErrorEnabled()) {
	log.error("SAX exception Parsing file: " + filename, se);
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
	log.error("Exception Parsing file: " + filename, e);
      }
    }

    return null;
  }

  /**
   * Writes the contents of the <code>Node</code> to the specified
   * file, in XML format. 
   *
   * @param configDir - Directory to write new xml file.
   * @param node - Document Node to dump to xml file.
   * @param name - Name of the new xml file.
   * @see org.w3c.dom.Node
   * @exception IOException if an error occurs
   */
  public void writeXMLFile(File configDir, Node node, String name) 
    throws IOException {
    
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.util.XMLUtils");

    if(!name.endsWith(".xml")) {
      name = name + ".xml";
    }
    PrintWriter writer = new PrintWriter(new FileWriter(new File(configDir, name)));
    try {
      writeNode(writer, node, 0);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Error writing config file: " + e);
      }
    }
    finally {
      writer.close();
    }    

  }
  
  private void writeNode(PrintWriter writer, Node node, int indent) {
    StringBuffer ibuff = new StringBuffer();
    for(int i=0; i < indent; i++) {
      ibuff.append(" ");
    }
    int type = node.getNodeType();
    switch(type) {
    case Node.DOCUMENT_NODE:
        writer.println("<?xml version=\"1.0\" encoding=\""+
                           "UTF-8" + "\"?>");  
        indent = -2;
        break;                  
    case Node.ELEMENT_NODE:
      writer.print(ibuff.substring(0) + '<' + node.getNodeName() );
      NamedNodeMap nnm = node.getAttributes();
      if(nnm != null )
        {
          int len = nnm.getLength() ;
          Attr attr;
          for ( int i = 0; i < len; i++ )
            {
              attr = (Attr)nnm.item(i);
              writer.print(' ' 
                               + attr.getNodeName()
                               + "=\""
                               + attr.getNodeValue()
                               +  '"' );
            }
        }
      writer.println('>');
      break;
      
      case Node.ENTITY_REFERENCE_NODE:
        writer.print('&' + node.getNodeName() + ';' );
        break;
      case Node.CDATA_SECTION_NODE:
        writer.print( "<![CDATA[" 
                          + node.getNodeValue()
                          + "]]>" );
        break;       
      case Node.TEXT_NODE:
        writer.print(ibuff.substring(0) + node.getNodeValue());
        break;
      case Node.PROCESSING_INSTRUCTION_NODE:
        writer.print(ibuff.substring(0) + "<?" 
                         + node.getNodeName() ) ;
        String data = node.getNodeValue();
        if ( data != null && data.length() > 0 ) {
          writer.print(' ');
          writer.print(data);
        }
        writer.println("?>");
        break;
      
    }//end of switch
    
    
    //recurse
    for(Node child = node.getFirstChild(); child != null; 
        child = child.getNextSibling()) {
      writeNode(writer, child, indent+2);
      }
    
    //without this the ending tags will miss
    if ( type == Node.ELEMENT_NODE )
      {
        writer.println(ibuff.substring(0) + "</" + node.getNodeName() + ">");
      }
  }

}// XMLUtils