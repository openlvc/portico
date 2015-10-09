#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PORTICO_HOME=$DIR/..
$PORTICO_HOME/jre/bin/java -jar $PORTICO_HOME/lib/portico.jar wanrouter $*

