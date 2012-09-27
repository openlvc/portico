/*
 *   Copyright 2012 The Portico Project
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
#include "ExampleCPPFederate.h"
#include "StringHelpers.h"
#include <string>
#include <iostream>

int main( int argc, char *argv[] )
{
	// check to see if we have a federate name
	std::wstring federateName = L"exampleFederate";
	if( argc > 1 )
		federateName = toWideString( std::string(argv[1]) );
	
	// create and run the federate
	ExampleCPPFederate *federate;
	federate = new ExampleCPPFederate();
	federate->runFederate( federateName );
	
	// clean up
	delete federate;
	return 0;
}
