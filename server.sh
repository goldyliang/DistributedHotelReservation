#!/bin/bash

# Absolute path to this script, e.g. /home/user/bin/foo.sh
SCRIPT=$(readlink -f "$0")
# Absolute path this script is in, thus /home/user/bin
SCRIPTPATH=$(dirname "$SCRIPT")

echo $SCRIPTPATH

CLASSPATH="$SCRIPTPATH"/bin
echo $CLASSPATH

java -classpath $CLASSPATH HotelServer.HotelServer config.properties.$1

bash
