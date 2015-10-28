#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PORTICO_HOME=$DIR/..
JAVA=java
if [ -f $PORTICO_HOME/jre/bin/java ];
then
	JAVA=$PORTICO_HOME/jre/bin/java
fi

$JAVA -jar $PORTICO_HOME/lib/portico.jar wanrouter $*

