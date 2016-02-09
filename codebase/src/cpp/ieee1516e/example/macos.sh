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

#####################
# test for RTI_HOME #
#####################
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
	g++ -g -stdlib=libstdc++ -fPIC -I$RTI_HOME/include/ieee1516e -lrti1516e64d -lfedtime1516e64d -L$RTI_HOME/lib/gcc4 \
		main.cpp ExampleCPPFederate.cpp ExampleFedAmb.cpp -o example-federate
	exit;	
fi

############################################
### (target) debug #########################
############################################
if [ $1 = "debug" ]
then
	echo "starting ggdb - we need to sudo to avoide a bunch of code-signing stuff - sorry :("
	sudo ggdb -x gdb-macos.env ./example-federate
	exit;
fi

############################################
### (target) execute #######################
############################################
if [ $1 = "execute" ]
then
	shift;
	PORTICO_DEBUG=OFF DYLD_LIBRARY_PATH="$RTI_HOME/lib/gcc4:$RTI_HOME/jre/lib/server" ./example-federate $*
	exit;
fi

echo $USAGE

