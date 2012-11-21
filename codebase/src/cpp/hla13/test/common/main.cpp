/*
 *   Copyright 2007 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License (CDDL)
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 *
 */
#include "Common.h"

// CPP Unit includes
#include <cppunit/BriefTestProgressListener.h>
#include <cppunit/XmlOutputter.h>
#include <cppunit/extensions/TestFactoryRegistry.h>
#include <cppunit/TestResult.h>
#include <cppunit/TestResultCollector.h>
#include <cppunit/TestRunner.h>

// System Includes
#include <iostream>
#include <fstream>
#include <cstring>
#include <cstdlib>

using namespace CPPUNIT_NS;
using namespace std;

int main( int argc, char* argv[] )
{
	// check the command line arguments
	if( argc < 2 )
	{
		// display usage and exit
		cout << "Incorrect arguments: you must supply the output XML file name" << endl;
		exit( 1 );
	}
	
	// check the output file, making sure we can open and write to it
	char* outputFile = argv[1];
	ofstream output( outputFile ); // try and open it for writing
	if( output.fail() )
	{
		cout << "Error: couldn't open output file [" << outputFile << "] for writing" << endl;
		exit( 1 );
	}

	////////////////////////
	// run the unit tests //
	////////////////////////
	// create the event manager and test controller
	TestResult controller;

	// add a listener that collects the results
	TestResultCollector result;
	controller.addListener( &result );

	// add a listener that prints a brief progress display while running
	BriefTestProgressListener progress;
	controller.addListener( &progress );

	// add suites from the registry
	// if the TEST.GROUP environment variable is set, use that as the class to run
	TestRunner runner;
	char *value = getenv( "TEST.GROUP" );
	if( value == NULL || strcmp(value,"") == 0 || strcmp(value,"${test.group}") == 0 )
	{
		runner.addTest( TestFactoryRegistry::getRegistry().makeTest() );
	}
	else
	{
		cout << "TEST.GROUP=" << value << endl;
		runner.addTest( TestFactoryRegistry::getRegistry(value).makeTest() );
	}

	// Run the tests
	runner.run( controller );

	// Output results
	XmlOutputter outputter( &result, output );
	outputter.write();
	cout << "Results written to [" << outputFile << "]" << endl;

	// Pass back the return code
	return result.wasSuccessful() ? 0 : 1;

}
