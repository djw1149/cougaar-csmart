#!/bin/sh

# <copyright>
#  Copyright 2001 BBNT Solutions, LLC
#  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
# 
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the Cougaar Open Source License as published by
#  DARPA on the Cougaar Open Source Website (www.cougaar.org).
# 
#  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
#  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
#  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
#  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
#  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
#  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
#  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
#  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
#  PERFORMANCE OF THE COUGAAR SOFTWARE.
# </copyright>


# Main script to run CSMART on Unix
# Note that CSMART writes some files to its working directory.

os=`uname`
SEP=";"
if [ $os = "Linux" -o $os = "SunOS" ]; then SEP=":"; fi

# Next section lists Jar files required for CSMART. Note that by default CSMART uses the Cougaar
# Bootstrapper, which should find the jar files automatically (searching CIP/lib, plugins, sys, etc).
# One need only include the CSMART and core jar files

# COUGAAR bootstrapping classpath will be:
#  $COUGAAR_INSTALL_PATH/lib/core.jar
#
# Plus any explicitly added entries - for us, the CSMART Jar file
    
# once running, jar files will be searched for in (in order):
#  -Dorg.cougaar.class.path 	like classpath
#  $COUGAAR_INSTALL_PATH/lib/*
#  $COUGAAR_INSTALL_PATH/plugins/*
#  -Dorg.cougaar.system.path=$COUGAAR3RDPARTY
#  $COUGAAR_INSTALL_PATH/sys/*
#  $CLASSPATH		(Cougaar bootstrapping path from above)
#
    
# To run without the Bootstrapper, set org.cougaar.useBootstrapper=false
# The Jars peculiar to CSMART and their usual locations are listed here as a convenience

# Third party (mostly static) jar files are either in COUGAAR3RDPARTY or in COUGAAR_INSTLL_PATH/sys
if [ "$COUGAAR3RDPARTY" == "" ]; then
    COUGAAR3RDPARTY="${COUGAAR_INSTALL_PATH}/sys"
fi
    
# Add CSMART jar explicitly to get started
LIBPATHS="${COUGAAR_INSTALL_PATH}/lib/csmart.jar"
export LIBPATHS
    
# The AppServer jar must also be specified
LIBPATHS="${LIBPATHS}${SEP}${COUGAAR_INSTALL_PATH}/lib/server.jar"
    
# For now CSMART needs "core.jar" for the Bootstrapper and some
#  utility classes.  This dependency should be removed in a future
#  release of CSMART!
LIBPATHS="${LIBPATHS}${SEP}${COUGAAR_INSTALL_PATH}/lib/core.jar"

# Third party jars are in COUGAAR3RDPARTY
# Which is usually COUGAAR_INSTALL_PATH/sys
# you must add the MySQL jar file here if you want to use it
LIBPATHS="${LIBPATHS}${SEP}${COUGAAR3RDPARTY}/xerces.jar${SEP}${COUGAAR3RDPARTY}/grappa1_2_bbn.jar${SEP}${COUGAAR3RDPARTY}/jcchart451K.jar${SEP}${COUGAAR3RDPARTY}/oracle12.zip"
    
if [ "$COUGAAR_DEV_PATH" != "" ] ; then
    LIBPATHS="${COUGAAR_DEV_PATH}${SEP}${LIBPATHS}"
fi
    
# The performance analyzer uses Excel. To use it or an equivalent,
# edit the following property:
MYEXCEL=-Dorg.cougaar.tools.csmart.excelpath=\"C:/Program\\ Files/Microsoft\\ Office/Office/excel.exe\"

MYMEMORY="-Xms100m -Xmx300m"

# The delay between starting nodes, in milliseconds
MYDELAY="-Dorg.cougaar.tools.csmart.startdelay=0"

MYPROPERTIES="-Dorg.cougaar.install.path=$COUGAAR_INSTALL_PATH $MYEXCEL $MYDELAY"

# Set the config path to include the basic CSMART config files first
MYCONFIGPATH="-Dorg.cougaar.config.path=$COUGAAR_INSTALL_PATH/csmart/data/common/\;"

# Set the CLASSPATH ENV Variable
CLASSPATH="${LIBPATHS}:${CLASSPATH}"

javaargs="$MYPROPERTIES $MYMEMORY $MYCONFIGPATH -Dorg.cougaar.class.path=$LIBPATHS"

if [ "$COUGAAR_DEV_PATH" != "" ]; then
   echo java $javaargs org.cougaar.tools.csmart.ui.viewer.CSMART
fi

exec java $javaargs org.cougaar.tools.csmart.ui.viewer.CSMART
