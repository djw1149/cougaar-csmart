/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       � Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */

import junit.framework.*;

/**
 * This is the class run by the nightly build process to run JUnit tests.<br>
 * Run all of the CSMART JUnit tests.<br>
 * Developers must maintain this to keep the set of tests to run up-to-date.<br>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 */
public class Regress extends TestSuite {
  public Regress() {
    super();
    addTest(Regress.suite());
  }
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite csmartTests = new TestSuite();
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.util.parser.ParserTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.util.parser.SimpleParserTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.configgen.CustomerTaskTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.configgen.XMLParserTest.class));
    return csmartTests;
  }
}
