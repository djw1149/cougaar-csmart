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

package org.cougaar.tools.csmart.ui.console;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Position;
import java.awt.*;

/**
 * A text pane that contains a ConsoleStyledDocument and supports
 * searching and highlighting that document.
 */
public class ConsoleTextPane extends JTextPane {
  ConsoleStyledDocument doc;
  String searchString;
  Position searchPosition;
  String notifyCondition;
  Position notifyPosition;
  Highlighter highlighter;
  DefaultHighlighter.DefaultHighlightPainter searchHighlight;
  DefaultHighlighter.DefaultHighlightPainter notifyHighlight;
  Object searchHighlightReference;
  Object notifyHighlightReference;
  NodeStatusButton statusButton;
  int notifyCount;
  MyDocumentListener docListener = null;

  private transient Logger log;

  public ConsoleTextPane(ConsoleStyledDocument doc, NodeStatusButton statusButton) {
    super(doc);
    createLogger();
    this.doc = doc;
    this.statusButton = statusButton;
    highlighter = getHighlighter();
    searchHighlight = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
    notifyHighlight = new DefaultHighlighter.DefaultHighlightPainter(Color.magenta);
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  private void highlightSearchString(int startOffset, int endOffset) {
    if (searchHighlightReference != null)
      highlighter.removeHighlight(searchHighlightReference);
    try {
      searchHighlightReference = highlighter.addHighlight(startOffset, endOffset, searchHighlight);
    } catch (BadLocationException ble) {
      if(log.isErrorEnabled()) {
        log.error("Bad location exception: " + ble.offsetRequested(), ble);
      }
    }
  }

  private void highlightNotifyString(int startOffset, int endOffset) {
    if (notifyHighlightReference != null)
      highlighter.removeHighlight(notifyHighlightReference);
    try {
      notifyHighlightReference = highlighter.addHighlight(startOffset, endOffset, notifyHighlight);
    } catch (BadLocationException ble) {
      if(log.isErrorEnabled()) {
      log.error("Bad location exception: " + ble.offsetRequested(), ble);
      }
    }
  }

  private void displayHighlightedText(int startOffset, int endOffset) {
    try {
      setCaretPosition(endOffset);
      Rectangle r = modelToView(startOffset);
      scrollRectToVisible(r);
    } catch (BadLocationException ble) {
      if(log.isErrorEnabled()) {
        log.error("Bad location exception: " + ble.offsetRequested(), ble);
      }
    }
  }

  /**
   * Search for a string starting at a given position.
   * If the string is found, set the position at the end of the
   * string so it can be used as the start of the next search.
   * If the string is not found, return false.
   * @param s string to search for
   * @param startPosition position in document to start search
   * @param search true for "search"; false for "notify"
   * @return boolean whether or not string was found
   * TODO: if the last text in the buffer is highlighted, then
   * the highlighting is automatically applied to any new text added,
   * how to avoid this?
   */
  private boolean worker(String s, Position startPosition, boolean search) {
    s = s.toLowerCase();
    int startOffset = startPosition.getOffset();
    try {
      String content = getText(startOffset,
                               doc.getEndPosition().getOffset() - startOffset);
      content = content.toLowerCase();
      int index = content.indexOf(s);
      if (index == -1)
        return false;
      startOffset = startOffset + index;
      int endOffset = startOffset + s.length();
      if (search) {
        highlightSearchString(startOffset, endOffset);
        searchPosition = doc.createPosition(endOffset);
      } else {
        highlightNotifyString(startOffset, endOffset);
        notifyPosition = doc.createPosition(endOffset);
      }
      displayHighlightedText(startOffset, endOffset);
    } catch (BadLocationException ble) {
      if(log.isErrorEnabled()) {
        log.error("Bad location exception: " + ble.offsetRequested(), ble);
      }
      return false;
    }
    return true;
  }

  /**
   * Specify a "notify" string. If this string
   * is detected in the output of the node, then the node status
   * button color is set to blue and remains set until the user resets it;
   * the first instance of the "notify" string is highlighted.
   * Search is case insensitive.
   * @param s string to watch for in node output
   */
  public void setNotifyCondition(String s) {
    clearNotify();
    if (s == null) {
      doc.removeDocumentListener(docListener);
      docListener = null;
      notifyCondition = s;
    } else {
      notifyCondition = s.toLowerCase();
      if (docListener == null) {
        docListener = new MyDocumentListener();
        doc.addDocumentListener(docListener);
      }
    }
  }

  /**
   * Return notify condition in use.
   */
  public String getNotifyCondition() {
    return notifyCondition;
  }

  /**
   * Clear the notify highlighting and position.  Starts searching for
   * notify conditions with new text appended after this method is called.
   * Reset the count.
   */
  public void clearNotify() {
    notifyPosition = null;
    if (notifyHighlightReference != null)
      highlighter.removeHighlight(notifyHighlightReference);
    notifyCount = 0;
  }

  /**
   * Return number of occurrences of notify string received from the
   * node.  This number is reset when a new notify string is specified
   * or when the node status is reset (via clearNotify).
   */
  public int getNotifyCount() {
    return notifyCount;
  }

  /**
   * Search for and highlight the next instance of the notify string,
   * starting at the end of the last notify string found (or at the
   * beginning of the screen buffer, if the previous notify string
   * was removed from the buffer).
   * Search is case insensitive.
   * @return true if notify string is found and false otherwise
   */
  public boolean notifyNext() {
    if (notifyCondition != null && notifyPosition != null)
      return worker(notifyCondition, notifyPosition, false);
    return false;
  }

  /**
   * Search for the string starting at the current search position or
   * at the beginning of the document, and
   * highlight it if found.
   * Search is case insensitive.
   * @param s string to search for
   * @return true if string found and false otherwise
   */
  public boolean search(String s) {
    searchString = s;
    if (searchPosition == null)
      return worker(s, doc.getStartPosition(), true);
    else
      return worker(s, searchPosition, true);
  }

  public String getSearchString() {
    return searchString;
  }

  /**
   * Search for the "current" string (i.e. the last string specified in
   * a call to the search method) starting at the end of the last
   * search string found (or at the beginning of the screen buffer,
   * if the previous search string was removed from the buffer).
   * Search is case insensitive.
   * @return true if string found and false otherwise
   */
  public boolean searchNext() {
    if (searchString != null)
      return worker(searchString, searchPosition, true);
    return false;
  }

  // for testing, print keymap with recursion
  static void printKeymap(javax.swing.text.Keymap m, int indent) {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.ui.console.ConsoleTextPane");
    javax.swing.KeyStroke[] k = m.getBoundKeyStrokes();
    for (int i = 0; i < k.length; i++) {
      for (int j = 0; j < indent; j++)
        if(log.isDebugEnabled()) {
          log.debug(" ");
          log.debug("Keystroke <" +
                           java.awt.event.KeyEvent.getKeyModifiersText(k[i].getModifiers()) +
                           " " + java.awt.event.KeyEvent.getKeyText(k[i].getKeyCode()) + "> ");
          javax.swing.Action a = m.getAction(k[i]);
          log.debug((String)a.getValue(javax.swing.Action.NAME));
        }
    }
    m = m.getResolveParent();
    if (m != null)
      printKeymap(m, indent + 2);
  }

  public static void main(String[] args) {
    ConsoleStyledDocument doc = new ConsoleStyledDocument();
    javax.swing.text.AttributeSet a =
      new javax.swing.text.SimpleAttributeSet();
    ConsoleTextPane pane =
      new ConsoleTextPane(doc, new NodeStatusButton(null));
    doc.setBufferSize(20);
    doc.appendString("abcdefghijklmnopqrstuvw", a);
    // for debugging, print the keymap for this text component
    //    printKeymap(pane.getKeymap(), 0);
    pane.setNotifyCondition("now is the time for all good men");
    doc.appendString("now is the time for all good men", a);
    JScrollPane scrollPane = new JScrollPane(pane);
    javax.swing.JDesktopPane desktop = new javax.swing.JDesktopPane();
    javax.swing.JInternalFrame internalFrame =
      new javax.swing.JInternalFrame("", true, false, true, true);
    internalFrame.getContentPane().add(scrollPane);
    internalFrame.setSize(100, 100);
    internalFrame.setLocation(10, 10);
    internalFrame.setVisible(true);
    desktop.add(internalFrame,
                javax.swing.JLayeredPane.DEFAULT_LAYER);
    javax.swing.JFrame frame = new javax.swing.JFrame();
    frame.getContentPane().add(desktop);
    frame.pack();
    frame.setSize(200, 200);
    frame.setVisible(true);
  }

  /**
   * Listen for text added to the displayed document, and highlight
   * any text matching the notify condition.
   */
  class MyDocumentListener implements DocumentListener {

    public void insertUpdate(DocumentEvent e) {
      Document doc = e.getDocument();
      try {
        String newContent = doc.getText(e.getOffset(), e.getLength());
        newContent = newContent.toLowerCase();
        int index = newContent.indexOf(notifyCondition);
        if (index != -1) {
          notifyCount++;
          // if there's already a highlighted notify condition,
          // then don't highlight a new one
          if (notifyPosition != null)
            return;
          int startOffset = doc.getEndPosition().getOffset() -
            newContent.length() + index - 1;
          int endOffset = startOffset + notifyCondition.length();
          highlightNotifyString(startOffset, endOffset);
          notifyPosition = doc.createPosition(endOffset);
          statusButton.getMyModel().setStatus(NodeStatusButton.STATUS_NOTIFY);
        }
      } catch (BadLocationException ble) {
        if(log.isErrorEnabled()) {
          log.error("DocumentListener got Exception", ble);
        }
      }
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void removeUpdate(DocumentEvent e) {
    }

  }

  /**
   * When you're done with this component, call this to free
   * up resources. This recurses down to the document itself.
   **/
  public void cleanUp() {
    if (log.isDebugEnabled())
      log.debug("TextPane.cleanUp");
    clearNotify();
    searchHighlight = null;
    notifyHighlight = null;
    if (doc != null) {
//       if (log.isDebugEnabled())
// 	log.debug(".. had a doc");
      if (docListener != null) {
// 	if (log.isDebugEnabled())
// 	  log.debug(".. had a doc listener to remove");
	doc.removeDocumentListener(docListener);
	docListener = null;
      }
      // Recurse to document? Only if not re-using...
      doc.cleanUp();
      doc = null;
    }
    statusButton = null;
    CaretListener[] lists = getCaretListeners();
    for (int i = 0; i < lists.length; i++)
      removeCaretListener(lists[i]);
    removeNotify();
    removeAll();

  }

}
