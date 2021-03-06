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

package org.cougaar.tools.csmart.ui.monitor.generic;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/**
 * Borrowed from Cove.
 */
public class ComponentPrinter implements Printable, Pageable {
  private JComponent component;
  private PageFormat format;
  private PrinterJob job;
  private Dimension dimension;
  private Dimension pageDimension;
  private boolean debug = false;
  private boolean disableDoubleBuffering = true;
  private double scale;
  private transient Logger log;

  // Sadly, required for dealing with margins in NT
  private static final int x_margin_fudge_factor = 2;


  public ComponentPrinter(JComponent component, String jobName) {
    log = CSMART.createLogger(this.getClass().getName());
    this.component = component;
    job = PrinterJob.getPrinterJob();
    if (job == null) {
      if(log.isErrorEnabled()) {
	log.error("Couldn't find a printer!");
      }
    } else {
      java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
      scale = 72.0/tk.getScreenResolution();
      job.setJobName(jobName);

      // Bug 1917: See if returned object same as that input or null:
      // if so, user cancelled the window
      format = job.pageDialog(job.defaultPage());
      if (format == null || format.equals(job.defaultPage())) {
	if (log.isDebugEnabled())
	  log.debug("User cancelled print.");
	job = null;
	return;
      }
      // Hmmm. That didn't work...

      Dimension size = component.getSize();
      double frameHeight = size.height * scale;
      double frameWidth = size.width * scale;
      double pageHeight = format.getImageableHeight();
      double pageWidth = format.getImageableWidth();
      pageDimension = new Dimension((int) pageWidth, (int) pageHeight);
      dimension = new Dimension();
      dimension.width = (int) (frameWidth/pageWidth);
      if (frameWidth%pageWidth != 0) dimension.width++;
      dimension.height = (int) (frameHeight/pageHeight);
      if (frameHeight%pageHeight != 0) dimension.height++;
      if(log.isDebugEnabled()) {
	log.debug("Page layout: " + 
		  dimension.width + "," + dimension.height);
      }
      job.setPageable(this);
    }
  }


  public void disableDoubleBuffering(boolean flag) {
    disableDoubleBuffering = flag;
  }

  public void setDebug(boolean flag) {
    debug = flag;
  }

  public boolean isReady() {
    return job != null;
  }

  public void printPages() {
    if (job == null) return;
    try {
      job.print();
    } catch (PrinterException e) {
      if(log.isErrorEnabled()) {
	log.error("Print job failed: ", e);
      }
    }
  }

  // Printable

  public int print(Graphics g, PageFormat format, int page) {
    if (debug) System.out.print("Printing page " + page + "...");
    int x = page%dimension.width;
    int y = page/dimension.width;
    int xTrans = -x*pageDimension.width;
    int yTrans = -y*pageDimension.height;

    // Account for imaging margins.
    // Is this dependent on orientation?
    xTrans += x_margin_fudge_factor*format.getImageableX();
    yTrans += format.getImageableY();

    if(log.isDebugEnabled()) {
      log.debug("Translation: " + xTrans + "," + yTrans);
    }

    Graphics2D g2d = (Graphics2D) g;
    g2d.scale(scale, scale);
    g2d.translate(xTrans, yTrans);
	
    if (disableDoubleBuffering) component.setDoubleBuffered(false);
    component.print(g2d);
    if (disableDoubleBuffering) component.setDoubleBuffered(true);

    if(log.isDebugEnabled()) {
      log.debug("done");
    }

    return Printable.PAGE_EXISTS;
  }


  // This global double-buffer hack doesn't work properly with JTables.
  /*
    private void doubleBuffering(boolean flag) {
    RepaintManager currentManager = 
    RepaintManager.currentManager(component);
    currentManager.setDoubleBufferingEnabled(flag);
    }
  */



  // Pageable

  public int getNumberOfPages() {
    return dimension.width*dimension.height;
  }

  public PageFormat getPageFormat(int page) {
    return format;
  }

  public Printable getPrintable(int page) {
    return this;
  }


}

