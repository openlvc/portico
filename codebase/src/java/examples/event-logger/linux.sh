#!/bin/bash

USAGE="usage: linux.sh [compile] [clean] [execute [federate-name]]"

################################
# check command line arguments #
################################
if [ $# = 0 ]
then
	echo $USAGE
	exit;
fi

######################
# test for JAVA_HOME #
######################
JAVA=java
if [ "$JAVA_HOME" = "" ]
then
	echo WARNING Your JAVA_HOME environment variable is not set!
	#exit;
else
        JAVA=$JAVA_HOME/bin/java
fi

#####################
# test for RTI_HOME #
#####################
if [ "$RTI_HOME" = "" ]
then
	cd ../../../
	RTI_HOME=$PWD
	export RTI_HOME
	cd examples/java/event-logger
	echo WARNING Your RTI_HOME environment variable is not set, assuming $RTI_HOME
fi

############################################
### (target) clean #########################
############################################
if [ $1 = "clean" ]
then
	echo "deleting example federate jar file and left over logs"
	rm src/*.class
	rm event-logger.jar
	rm -Rf logs
	exit;
fi

############################################
### (target) compile #######################
############################################
if [ $1 = "compile" ]
then
	echo "compiling example federate"
	cd src
	javac -cp ./:$RTI_HOME/lib/portico.jar *.java
	jar -cf ../event-logger.jar *.class
	cd ../
	exit;
fi

############################################
### (target) execute #######################
############################################
if [ $1 = "execute" ]
then
	shift;
	java -cp ./event-logger.jar:$RTI_HOME/lib/portico.jar EventLogger $*
	exit;
fi

echo $USAGE

