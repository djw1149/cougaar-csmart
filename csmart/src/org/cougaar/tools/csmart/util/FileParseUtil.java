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

import java.io.FileInputStream;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.cougaar.util.ConfigFinder;
import java.nio.charset.CharacterCodingException;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;


/**
 * FileParseUtil.java
 *
 *
 * Created: Wed Mar 13 09:45:18 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class FileParseUtil {

  public FileParseUtil (){
  }

  /**
   * Searches the given file for the specified regular expression
   * pattern.  Returns true or false based on the result of the find.
   *
   * @param filename - The file to search for regex.
   * @param pattern - Regular Expression pattern to search for.
   * @return boolean indicating the result of the search.
   */
  public static final boolean containsPattern(String filename, String pattern) {
    Logger log = CSMART.createLogger("org.coguaar.tools.csmart.FileParseUtil");

    // Create the CharBuffer for the file.
    FileInputStream iStream = null;
    try {
      iStream = new FileInputStream(ConfigFinder.getInstance().locateFile(filename));
    } catch(IOException e) {
      if(log.isErrorEnabled()) {
        log.error("Exception finding file", e);
      }
    }

    FileChannel channel = iStream.getChannel();
    int length = 0;
    try {
      length = (int)channel.size();
    } catch(IOException e) {
      if(log.isErrorEnabled()) {
        log.error("Expception getting channel size", e);
      }
    }
    MappedByteBuffer buffer = null;
    try {
      buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, length);
    } catch(IOException e) {
      if(log.isErrorEnabled()) {
        log.error("Exception creating buffer", e);
      }
    }

    Charset charset = Charset.forName("ISO-8859-1");
    CharsetDecoder decoder = charset.newDecoder();
    CharBuffer cBuffer = null;
    try {
      cBuffer = decoder.decode(buffer);
    } catch(CharacterCodingException cce) {
      if(log.isErrorEnabled()) {
        log.error("Exception decoding buffer", cce);
      }
    }

    
    // Now search the CharBuffer using regex Patterns.
    Pattern ptrn = Pattern.compile(pattern);
    Matcher matcher = ptrn.matcher(cBuffer);

    return matcher.find();

  }

  /**
   * Tests the ini file to determine if it is a New ini.dat style, or
   * an older ini.dat style.  The old ini.dat style contains a 
   * [UniqueId] which does not exist in the newer style.  
   * <br>
   * There are other differences, but this difference is the first and
   * is the easiest to check existence of.
   *
   * @param filename - The file to determine if old or new style.
   */
  public static final boolean isOldStyleIni(String filename) {
    return FileParseUtil.containsPattern(filename, "UniqueId");
  }

  public static void main(String args[]) {
    String filename = args[0];
    if(FileParseUtil.isOldStyleIni(filename)) {
      System.out.println("Yes, " + filename + " is old Style");
    } else {
      System.out.println("No, " + filename + " is not old Style");
    }
  }
       
}// FileParseUtil