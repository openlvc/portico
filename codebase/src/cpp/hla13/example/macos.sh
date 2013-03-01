#!/bin/bash

USAGE="usage: macos.sh [compile] [clean] [execute [federate-name]]"

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

###################
# Set up RTI_HOME #
###################
cd ../../..
RTI_HOME=$PWD
export RTI_HOME
cd examples/cpp/hla13
echo RTI_HOME environment variable is set to $RTI_HOME

############################################
### (target) clean #########################
############################################
if [ $1 = "clean" ]
then
	echo "deleting example federate executable and left over logs"
	rm example-federate
	rm -Rf logs
	exit;
fi

############################################
### (target) compile #######################
############################################
if [ $1 = "compile" ]
then
	echo "compiling example federate"
	g++ -O1 -fPIC -I$RTI_HOME/include/hla13 \
	    -DRTI_USES_STD_FSTREAM \
		-lRTI-NG64 -lFedTime64 -L$RTI_HOME/lib/gcc4 \
		main.cpp ExampleCPPFederate.cpp ExampleFedAmb.cpp -o example-federate
	exit;	
fi

############################################
### (target) execute #######################
############################################
if [ $1 = "execute" ]
then
	shift;
	DYLD_LIBRARY_PATH="$RTI_HOME/lib/gcc4:$JAVA_HOME/jre/lib/server" ./example-federate $*
	exit;
fi

echo $USAGE

