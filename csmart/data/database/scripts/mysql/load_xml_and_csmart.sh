#!/bin/sh

# 
# <copyright>
#  Copyright 2001-2003 BBNT Solutions, LLC
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

# This script loads all data necessary for running from either XML or CSMART
# into the single named database. All of the following cougaar.rc entries
# are covered, and all should point to the single named database as provided in the argument.
# org.cougaar.database
# org.cougaar.oplan.database
# org.cougaar.configuration.database
# org.cougaar.refconfig.database
# blackjack.database
# icis.database
# fcs.database

COUGAAR_INSTALL_PATH=`dirname $0`
COUGAAR_INSTALL_PATH="${COUGAAR_INSTALL_PATH}/../../../../../"
cd $COUGAAR_INSTALL_PATH
export COUGAAR_INSTALL_PATH=`pwd`

if [ "x$3" = "x" ]; then
    echo "Load all data necessary for running from CSMART or XML into a single database."
    echo "Usage: load_xml_and_csmart.sh [DB username] [password] [database] [DB host [local]]"
    echo "   "
    echo "     Specify the hostname if the database is not local, or if you "
    echo "            want to specify the 'local' keyword "
    echo "   "
    echo "      Note: Use the 'local' keyword if running across a networked"
    echo "            drive. You must have MySQL v3.23.49 or better to use"
    echo "            the local option."
    exit
fi

user=$1
pswd=$2
database=$3
host=$4
local=$5

# If dbms directory exists then load domain data and oplan data
#  otherwise just load the xml and config databases

if [ -d "$COUGAAR_INSTALL_PATH/dbms/data/mysql" ]; then

    # The domain db (1ADDomainData including fdm tables)
    # Also loads army_spares.zip/army.txt and spares_index.txt
    if [ ! -e "$COUGAAR_INSTALL_PATH/dbms/data/mysql/load_domain_data.sh" ]; then
        echo "Cannot find $COUGAAR_INSTALL_PATH/dbms/data/mysql/load_domain_data.sh"
        exit
    else
        echo "  - Loading domain files into database $database."
        if [ "$local" = "local" ]; then
            $COUGAAR_INSTALL_PATH/dbms/data/mysql/load_domain_data.sh $user $pswd $database $host $local
        else
            $COUGAAR_INSTALL_PATH/dbms/data/mysql/load_domain_data.sh $user $pswd $database $host
        fi
    fi

   # The oplan db
    if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/load_oplan_data.sh" ]; then
        echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/load_oplan_data.sh"
        exit
    else
        echo "  - Loading oplan files into database $database."
        if [ "$local" = "local" ]; then
            $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/load_oplan_data.sh $user $pswd $database $host $local
        else
            $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/load_oplan_data.sh $user $pswd $database $host
        fi
    fi
fi

if [ -d "$COUGAAR_INSTALL_PATH/fcsua" ]; then

    # The ua domain db (fcs tables)
    if [ ! -e "$COUGAAR_INSTALL_PATH/fcsua/data/database/scripts/mysql/load_ua_domain_data.sh" ]; then
        echo "Cannot find $COUGAAR_INSTALL_PATH/fcsua/data/database/scripts/mysql/load_ua_domain_data.sh"
        exit
    else
        echo "  - Loading ua domain files into database $database."
        if [ "$local" = "local" ]; then
            $COUGAAR_INSTALL_PATH/fcsua/data/database/scripts/mysql/load_ua_domain_data.sh $user $pswd $database $host $local
        else
            $COUGAAR_INSTALL_PATH/fcsua/data/database/scripts/mysql/load_ua_domain_data.sh $user $pswd $database $host
        fi
    fi

fi

# The config db (csmart)
if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/load_csmart_data.sh" ]; then
    echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/load_csmart_data.sh"
    exit
else
    echo "  - Loading csmart config files into database $database."
    if [ "$local" = "local" ]; then
        $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/load_csmart_data.sh $user $pswd $database $host $local
    else
        $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/load_csmart_data.sh $user $pswd $database $host
    fi
fi

# Now the xml refconfig data
if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/load_ref_data.sh" ]; then
    echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/load_ref_data.sh"
    exit
else
    echo "  - Loading the xml refconfig files into database $database."
    if [ "$local" = "local" ]; then
	$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/load_ref_data.sh $user $pswd $database $host $local
    else
	$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/load_ref_data.sh $user $pswd $database $host
    fi
fi

echo "Done"
