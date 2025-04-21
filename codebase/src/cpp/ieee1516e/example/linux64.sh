#!/bin/bash

USAGE="usage: linux64.sh [compile] [clean] [execute [federate-name]]"

################################
# check command line arguments #
################################
if [ $# = 0 ]
then
	echo $USAGE
	exit;
fi

###################
# Set up RTI_HOME #
###################
cd ../../..
RTI_HOME=$PWD
export RTI_HOME
cd examples/cpp/ieee1516e
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
	g++ -O1 -I$RTI_HOME/include/ieee1516e \
	    -DRTI_USES_STD_FSTREAM \
		-Wno-deprecated \
	    main.cpp ExampleCPPFederate.cpp ExampleFedAmb.cpp -o example-federate \
	    -L$RTI_HOME/lib/gcc8 -lrti1516e64 -lfedtime1516e64 \
	    -L$JAVA_HOME/lib/server -ljvm -ljsig
	exit;	
fi

############################################
### (target) debug #########################
############################################
if [ $1 = "debug" ]
then
	echo "starting gdb"
	gdb -x gdb-linux.env ./example-federate
	exit;
fi

############################################
### (target) execute #######################
############################################
if [ $1 = "execute" ]
then
	shift;
	LD_LIBRARY_PATH="$RTI_HOME/lib/gcc8:$JAVA_HOME/lib/server" ./example-federate $*
	exit;
fi

echo $USAGE

