#include "ExampleCPPFederate.h"

int main( int argc, char *argv[] )
{
	// check to see if we have a federate name
	std::string defaultName = "exampleFederate";
	const char* federateName = defaultName.c_str();
	if( argc > 1 )
		federateName = argv[1];

	// create and run the federate
	ExampleCPPFederate *federate;
	federate = new ExampleCPPFederate();
	federate->runFederate( federateName );
	
	// clean up
	delete federate;
	return 0;
}
