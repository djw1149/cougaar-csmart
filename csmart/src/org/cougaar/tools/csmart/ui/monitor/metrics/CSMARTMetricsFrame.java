/* 
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.monitor.metrics;

import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.util.NamedFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class CSMARTMetricsFrame extends JFrame implements ActionListener
{
  static final String FILE_MENU = "File";
  static final String CLOSE_MENU_ITEM = "Close";
  static final String SAVE_MENU_ITEM = "Save";
  static final String SAVE_AS_MENU_ITEM = "Save As...";
  static final String PRINT_MENU_ITEM = "Print...";

  static final String VIEW_MENU = "View";
  static final String REFRESH_MENU_ITEM = "Refresh";

  protected static final String HELP_MENU = "Help";
  protected static final String HELP_DOC = "help.html";
  protected static final String HELP_MENU_ITEM = "Help";
  protected static final String ABOUT_MENU_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";

  static final String SEPARATOR = "Separator";

  String[] fileMenuItems = { 
    SAVE_MENU_ITEM, SAVE_AS_MENU_ITEM,
    PRINT_MENU_ITEM, SEPARATOR, CLOSE_MENU_ITEM
  };

  String[] viewMenuItems = { 
    REFRESH_MENU_ITEM
  };

  private String[] helpMenuItems = {
    HELP_MENU_ITEM, ABOUT_MENU_ITEM
  };

  String title;
  CSMARTMetrics metrics;
  private final JFrame myFrame;
  // menu items that are enabled/disabled
  JMenuItem saveMenuItem;

  public CSMARTMetricsFrame(String title, ArrayList names, ArrayList data ) {
    super(title);
    this.title = NamedFrame.getNamedFrame().addFrame(title, this);
    setTitle(this.title);
    this.metrics = new CSMARTMetrics(names, data);
    myFrame = this;
    init();
    saveMenuItem.setEnabled(this.metrics.inputFile != null);
  }

  public CSMARTMetricsFrame(String title, File f) {
    super(title);
    this.title = NamedFrame.getNamedFrame().addFrame(title, this);
    setTitle(this.title);
    this.metrics = new CSMARTMetrics(f);
    myFrame = this;
    init();
    saveMenuItem.setEnabled(this.metrics.inputFile != null);
  }

  private void init() {
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	NamedFrame.getNamedFrame().removeFrame(myFrame);
	e.getWindow().dispose();
      }
    });
    setSize(600, 275);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation((screenSize.width - 600)/2, 
		(screenSize.height - 275)/2);

    if (metrics != null)
      getContentPane().add("Center", metrics);
    
    JScrollPane jsp = new JScrollPane();
    jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    jsp.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

    getContentPane().add(jsp, "East");

    jsp.setViewportView(metrics);

    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = makeMenu(FILE_MENU, fileMenuItems, menuBar);
    for (int i = 0; i < fileMenu.getMenuComponentCount(); i++) {
      Component menuComponent = fileMenu.getMenuComponent(i);
      if (!(menuComponent instanceof JMenuItem))
	continue;
      JMenuItem menuItem = (JMenuItem)menuComponent;
      String s = menuItem.getText();
      if (s.equals(SAVE_MENU_ITEM))
	saveMenuItem = menuItem;
      else if (s.equals(PRINT_MENU_ITEM)) {
	menuItem.setEnabled(false);
      }
    }
    makeMenu(VIEW_MENU, viewMenuItems, menuBar);

    makeMenu(HELP_MENU, helpMenuItems, menuBar);

    setVisible(true);
  }

  private JMenu makeMenu(String menuName, String[] menuItemNames, 
			 JMenuBar menuBar) {
    JMenu menu = new JMenu(menuName);
    if (menuItemNames != null) {
      for (int i = 0; i < menuItemNames.length; i++) {
	if (menuItemNames[i].equals(SEPARATOR))
	  menu.addSeparator();
	else {
	  JMenuItem menuItem = new JMenuItem(menuItemNames[i]);
	  menuItem.addActionListener(this);
	  menu.add(menuItem);
	}
      }
    } else
      menu.addActionListener(this);
    menuBar.add(menu);
    return menu;
  }

  public void actionPerformed(ActionEvent evt) {
    String command = ((JMenuItem)evt.getSource()).getText();

    if (command.equals(CLOSE_MENU_ITEM)) {
      NamedFrame.getNamedFrame().removeFrame(this);
      this.dispose();
      return;
    }

    // save graph into file it was read from
    if (command.equals(SAVE_MENU_ITEM)) {
      metrics.saveMetrics();
      return;
    }

    if (command.equals(SAVE_AS_MENU_ITEM)) {
      metrics.saveAsMetrics();
      saveMenuItem.setEnabled(metrics.inputFile != null);
      return;
    }

    if (command.equals(REFRESH_MENU_ITEM)) {
      metrics.refresh();
      return;
    }

    if (command.equals(HELP_MENU_ITEM)) {
      URL help = (URL)getClass().getResource(HELP_DOC);
      if(help != null)
	Browser.setPage(help);
      return;
    }

    if (command.equals(ABOUT_MENU_ITEM)) {
      URL help = (URL)getClass().getResource(ABOUT_DOC);
      if(help != null)
	Browser.setPage(help);
      return;
    }

  }


}
  
