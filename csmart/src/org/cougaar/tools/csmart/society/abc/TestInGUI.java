/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society.abc;

import org.cougaar.tools.csmart.ui.component.*;
import javax.swing.JButton;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class TestInGUI extends TestGUI {
    ABCSociety society;

    public TestInGUI(ABCSociety society) {
        this.society = society;
        displayInFrame(society);
    }

    protected JButton[] getTestButtons() {
        JButton button1 = new JButton("Test");
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                society.getProperty(ABCSociety.PROP_COMMUNITYCOUNT)
                    .setValue(new Integer(5));
                society.getProperty(ABCSociety.PROP_LEVELCOUNT)
                    .setValue(new Integer(3));
		society.getProperty(ABCSociety.PROP_STARTTIME)
		  .setValue(new Integer(0));
		society.getProperty(ABCSociety.PROP_STOPTIME)
		  .setValue(new Integer(25000));
            }
        });
        JButton button2 = new JButton("Revert");
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                society.getProperty(ABCSociety.PROP_COMMUNITYCOUNT)
                    .setValue(new Integer(8));
                society.getProperty(ABCSociety.PROP_LEVELCOUNT)
                    .setValue(new Integer(3));
		society.getProperty(ABCSociety.PROP_STARTTIME)
		  .setValue(new Integer(0));
		society.getProperty(ABCSociety.PROP_STOPTIME)
		  .setValue(new Integer(5000));
            }
        });
        JButton button3 = new JButton("Write Ini Files");
        button3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                  try {
//                      society.writeConfigurationFiles(new File("."));
//                  } catch (Exception ex) {
//                      ex.printStackTrace();
//                  }
            }
        });
        return new JButton[] {button1, button2, button3};
    }
    public static void main(String[] args) {
        ABCSociety society = new ABCSociety("test1");
        society.initProperties();
        new TestInGUI(society);
    }
}