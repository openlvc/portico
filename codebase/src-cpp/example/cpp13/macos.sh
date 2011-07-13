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

#####################
# test for RTI_HOME #
#####################
if [ "$RTI_HOME" = "" ]
then
	cd ../../..
	RTI_HOME=$PWD
	export RTI_HOME
	cd examples/cpp/cpp13
	echo WARNING Your RTI_HOME environment variable is not set, assuming $RTI_HOME
fi

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
	g++ -O2 -fPIC -arch i386 -I$RTI_HOME/include/ng6 \
	    -DRTI_USES_STD_FSTREAM \
		-lRTI-NG -L$RTI_HOME/lib \
		main.cpp ExampleCPPFederate.cpp ExampleFedAmb.cpp -o example-federate
	exit;	
fi

############################################
### (target) execute #######################
############################################
if [ $1 = "execute" ]
then
	shift;
	DYLD_LIBRARY_PATH="$RTI_HOME/lib" ./example-federate $*
	exit;
fi

echo $USAGE
