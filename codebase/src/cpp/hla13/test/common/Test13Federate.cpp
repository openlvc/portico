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
#include "Test13Federate.h"

// set up the static vars
const char *Test13Federate::SIMPLE_NAME = "TestNG6Federation";
int Test13Federate::OWNER_UNOWNED = -1;
int Test13Federate::OWNER_RTI = 0;

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////// Constructors ////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
Test13Federate::Test13Federate( const char* name )
{
	// store the federate name (make sure it is a copy so that we can delete it when we want)
	//this->federateName = name;
	this->federateName = new char[256];
	strcpy( this->federateName, name );
	
	this->federateHandle = 0;	
	// create the RTIambassador
	this->rtiamb = new RTI::RTIambassador();
	
	// give some default values to the existing vars
	this->fedamb = NULL;
}

Test13Federate::~Test13Federate()
{
	delete [] this->federateName;
	delete this->rtiamb;
	if( this->fedamb != NULL )
		delete this->fedamb;
}

char* Test13Federate::getFederateName()
{
	return this->federateName;
}

RTI::FederateHandle Test13Federate::getFederateHandle()
{
	return this->federateHandle;
}

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Federation Management Helpers ///////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/*
 * This method will create a new federation using a common name known to all TestNG6Federate
 * instances (held in TestNG6Federate::SIMPLE_NAME). The default testing FOM will be used.
 */
void Test13Federate::quickCreate()
{
	this->quickCreate( Test13Federate::SIMPLE_NAME );
}

/*
 * This method will create a new federation using the given federation name and the default
 * testing FOM.
 */
void Test13Federate::quickCreate( const char* federationName )
{
	try
	{
		rtiamb->createFederationExecution( federationName, "etc/testfom.fed" );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickCreate()" );
	}
}

/*
 * Join a federation named "testFederation" using a federate name that is the same as the
 * name given to the current test federate
 */
RTI::FederateHandle Test13Federate::quickJoin()
{
	// just call into quickJoin( const char* )
	return this->quickJoin( Test13Federate::SIMPLE_NAME );
}

/*
 * The same as quickJoin() except that you can specify the federation name. 
 */
RTI::FederateHandle Test13Federate::quickJoin( const char* federationName )
{
	// create the FederateAmbassador implementation, removing the current one if it exists
	if( this->fedamb != NULL )
		delete this->fedamb;
	
	this->fedamb = new Test13FederateAmbassador( this );
	
	// try and join the federation
	try
	{
		this->federateHandle =
			rtiamb->joinFederationExecution( this->federateName, federationName, this->fedamb );
		return this->federateHandle;
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickJoin()" );
		return 0;
	}
}

/*
 * This method will resign from the federation to which this federate is currently joined using
 * the resign action DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES.
 */
void Test13Federate::quickResign()
{
	this->quickResign( RTI::DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES );
}

/*
 * This method will resign from the federation to which the federate is currently joined using
 * the given resign action.
 */
void Test13Federate::quickResign( RTI::ResignAction action )
{
	try
	{
		rtiamb->resignFederationExecution( action );
	}
	catch( RTI::FederateNotExecutionMember &fnem )
	{
		// ignore this, we'll let it slide as there are situations where we wan to call this
		// but don't know if the federate has cleaned up after itself properly and resigned
		// its federates or not. In this case, if they HAVE done the right thing, we'll get
		// an exception, however, we don't trust ourselves to do the right thing ;)
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickResign()" );
	}
}

/*
 * This method will attempt to destroy and federation with the name TestNG6Federate::SIMPLE_NAME
 */
void Test13Federate::quickDestroy()
{
	this->quickDestroy( Test13Federate::SIMPLE_NAME );
}

/*
 * This method will attempt to destroy the federation of the given name.
 */
void Test13Federate::quickDestroy( const char* federationName )
{
	try
	{
		rtiamb->destroyFederationExecution( federationName );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickDestroy()" );
	}
}

/*
 * Same as quickDestroy(), only it won't fail the test if there is a problem. This is useful
 * for tearDown() methods where you want to clean things up that may already have been cleaned
 * up.
 */
void Test13Federate::quickDestroyNoFail()
{
	try
	{
		rtiamb->destroyFederationExecution( Test13Federate::SIMPLE_NAME );
	}
	catch( RTI::Exception &e )
	{
		// do nothing
	}
}

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////// Synchronization Point Helper Methods ////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/*
 * Attempt to register a federation wide synchronization point with the given label and tag. If
 * the tag is NULL, "NA" will be passed
 */
void Test13Federate::quickAnnounce( const char* label, const char* tag )
{
	// check the tag
	if( tag == NULL )
		tag = "NA";
	
	try
	{
		rtiamb->registerFederationSynchronizationPoint( label, tag );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickAnnounce()" );
	}
}

/*
 * Attempt to regsiter a restricted synchronization point with the given label and tag. The given
 * handles will be used to identify which federates should be privy to the point. If the given
 * tag is NULL, "NA" will be passed.
 */
void Test13Federate::quickAnnounce( const char* label, 
                                    int federateCount, 
                                    ... /* federate handles */ )
{
	// convert the handle set
	RTI::FederateHandleSet *handleSet = this->createFHS( federateCount );
	va_list args;
	va_start( args, federateCount );
	for( int i = 0; i < federateCount; i++ )
	{
		handleSet->add( va_arg(args,int) );
	}
	va_end( args );

	try
	{
		rtiamb->registerFederationSynchronizationPoint( label, "NA", *handleSet );
		delete handleSet;
	}
	catch( RTI::Exception &e )
	{
		delete handleSet;
		killTest( e, "quickAnnounce(int[])" );
	}
}

/*
 * Attempt to sign to the RTI that this federate has achieved the synchronization point with
 * the given label
 */
void Test13Federate::quickAchieved( const char* label )
{
	try
	{
		rtiamb->synchronizationPointAchieved( label );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickAchieved()" );
	}
}

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////// Publish and Subscribe Helper Methods ////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
// object
/*
 * Attempts to publish the provided object class with the given attributes. The attributeCount
 * parameter contains the number of parameters that are being published, while the varargs should
 * contains the names of each of the parmeters that are being published.
 */
void Test13Federate::quickPublish( int objectClass, int attributeCount, ... )
{
	// get a handle set with the attribute values
	RTI::AttributeHandleSet *ahs = this->createAHS( attributeCount );
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		ahs->add( va_arg(args,int) );
	}
	va_end( args );
	
	try
	{
		// attempt the publish
		rtiamb->publishObjectClass( objectClass, *ahs );
		delete ahs;
	}
	catch( RTI::Exception &e )
	{
		delete ahs;
		killTest( e, "quickPublish(attributes,handle-based)" );
	}
}

/*
 * Attempts to publish the specified attributes of the given object class. The attributeCount
 * parameter is the number of attributes that should be published. This method will resolve the
 * handles on behalf of the user from the given names.
 */
void Test13Federate::quickPublish( const char* objectClass, int attributeCount, ... )
{
	// resolve the handle for the class
	RTI::ObjectClassHandle classHandle = quickOCHandle( objectClass );
	// create the attribute handle set
	RTI::AttributeHandleSet *attributeHandles = this->createAHS( attributeCount );
	// fill the ahs out
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		char *attributeName = va_arg( args, char* );
		attributeHandles->add( this->quickACHandle(objectClass,attributeName) );
	}
	va_end( args );

	// attempt the do the publish
	try
	{
		rtiamb->publishObjectClass( classHandle, *attributeHandles );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickPublish(attributes,name-based)" );
	}
}

/**
 * Attempts to unpublish all attributes of the class with the given name. If there is an error,
 * the current test is failed.
 */
void Test13Federate::quickUnpublishOC( const char* objectClass )
{
	// resolve the handle for the class
	RTI::ObjectClassHandle classHandle = quickOCHandle( objectClass );

	try
	{
		rtiamb->unpublishObjectClass( classHandle );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickUnpublishOC()" );
	}
}

/*
 * Attempts to subscribe to the provided attributes of the given object class. attributeCount is
 * the number of attributes that are being subscribed to, while the varargs should contain the
 * names of all the attributes to subscribe to.
 */
void Test13Federate::quickSubscribe( int objectClass, int attributeCount, ... )
{
	// get a handle set with the attribute values
	RTI::AttributeHandleSet *ahs = this->createAHS( attributeCount );
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		ahs->add( va_arg(args,int) );
	}
	va_end( args );

	try
	{
		// attempt the publish
		rtiamb->subscribeObjectClassAttributes( objectClass, *ahs );
		delete ahs;
	}
	catch( RTI::Exception &e )
	{
		delete ahs;
		killTest( e, "quickSubscribe(attributes,handle-based)" );
	}
}

/*
 * Attempts to subscribe the specified attributes of the given object class. The attributeCount
 * parameter is the number of attributes that should be published. This method will resolve the
 * handles on behalf of the user from the given names.
 */
void Test13Federate::quickSubscribe( const char* objectClass, int attributeCount, ... )
{
	// resolve the handle for the class
	RTI::ObjectClassHandle classHandle = quickOCHandle( objectClass );
	// create the attribute handle set
	RTI::AttributeHandleSet *attributeHandles = this->createAHS( attributeCount );
	// fill the ahs out
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		char *attributeName = va_arg( args, char* );
		attributeHandles->add( this->quickACHandle(objectClass,attributeName) );
	}
	va_end( args );

	// attempt the subscribe
	try
	{
		rtiamb->subscribeObjectClassAttributes( classHandle, *attributeHandles );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickSubscribe(attributes,name-based)" );
	}
}

// interaction
/*
 * Attempts to publish the interaction class identified by the given handle.
 */
void Test13Federate::quickPublish( int interactionClass )
{
	// attempt the publication
	try
	{
		rtiamb->publishInteractionClass( interactionClass );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickPublish(interaction,handle-based)" );
	}
}

/*
 * Attempts to publish the interaction class identified by the given class name. This method
 * will resolve the handle on behalf of the caller.
 */
void Test13Federate::quickPublish( const char* interactionClass )
{
	// resolve the handle for the class
	RTI::InteractionClassHandle classHandle = this->quickICHandle( interactionClass );
	
	// attempt the publication
	try
	{
		rtiamb->publishInteractionClass( classHandle );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickPublish(interaction,name-based)" );
	}
}

/*
 * Attempts to subscribe to the interaction class identified by the given handle.
 */
void Test13Federate::quickSubscribe( int interactionClass )
{
	// attempt the subscription
	try
	{
		rtiamb->subscribeInteractionClass( interactionClass );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickSubscribe(interaction,handle-based)" );
	}
}

/*
 * Attempts to subscribe to the interaction class identified by the given class name. This method
 * will resolve the handle on behalf of the caller.
 */
void Test13Federate::quickSubscribe( const char* interactionClass )
{
	// resolve the handle
	RTI::InteractionClassHandle classHandle = this->quickICHandle( interactionClass );

	// attempt the subscription
	try
	{
		rtiamb->subscribeInteractionClass( classHandle );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickSubscribe(interaction,name-based)" );
	}
}

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////// Object Register/Delete Helper Methods ///////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/*
 * This method will attempt to register an object instance of the given class handle. If
 * successful, it will return the handle of the object that has been registered.
 */
RTI::ObjectHandle Test13Federate::quickRegister( int classHandle )
{
	try
	{
		return rtiamb->registerObjectInstance( classHandle );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickRegister(int)" );
		return -1;
	}
}

/*
 * The same as quickRegister(int), except that you can provide the name to use for the object
 */
RTI::ObjectHandle Test13Federate::quickRegister( int classHandle, const char* objectName )
{
	try
	{
		return rtiamb->registerObjectInstance( classHandle, objectName );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickRegister(int,char*)" );
		return -1;
	}
}

/*
 * This method will attempt to register an object instance of the given class. If successful,
 * it will return the handle of the object that has been registered. The method will resolve
 * the handle for the object class on behalf of the user.
 */
RTI::ObjectHandle Test13Federate::quickRegister( const char* className )
{
	// resolve the handle before the request
	RTI::ObjectClassHandle classHandle = this->quickOCHandle( className );
	
	try
	{
		return rtiamb->registerObjectInstance( classHandle );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickRegister(char*)" );
		return -1;
	}	
}

/*
 * The same as quickRegister(char*) except that you can provide the object name
 */
RTI::ObjectHandle Test13Federate::quickRegister( const char* className, const char* objectName )
{
	// resolve the handle before the request
	RTI::ObjectClassHandle classHandle = this->quickOCHandle( className );
	
	try
	{
		return rtiamb->registerObjectInstance( classHandle, objectName );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickRegister(char*)" );
		return -1;
	}	
}

/*
 * This method will attempt to register an object instance of the given class handle, HOWEVER,
 * it EXPECTS THIS REQUEST TO FAIL with an exeption. If it does not, the test will be killed.
 */
void Test13Federate::quickRegisterFail( int classHandle )
{
	try
	{
		rtiamb->registerObjectInstance( classHandle );
		// FAIL THE TEST, shouldn't get here if there is an exception as expected
		killTest( "Was expecting registration of class [%s] would fail", classHandle );
	}
	catch( RTI::Exception &e )
	{
		// success!
	}
}

/*
 * This method will attempt to delete the object instance identified by the given handle and
 * will pass the given tag. If the tag is NULL, it will be replaced with "NA".
 */
void Test13Federate::quickDelete( int objectHandle, const char* tag )
{
	// check the tag
	if( tag == NULL )
		tag = "NA";
	
	try
	{
		rtiamb->deleteObjectInstance( objectHandle, tag );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickDelete()" );
	}
}


//////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// Reflection and Interaction Helpers /////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/*
 * Attempt to update the object of the given handle with the provided values and tag. If the
 * tag is NULL, it will be replaced with "NA".
 */
void Test13Federate::quickReflect( int objectHandle,
                                    RTI::AttributeHandleValuePairSet *ahvps,
                                    const char* tag )
{
	// check the tag
	if( tag == NULL )
		tag = "NA";
	
	try
	{
		rtiamb->updateAttributeValues( objectHandle, *ahvps, tag );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickReflect()" );
	}
}

/*
 * Attempt to udpate the attribute values of the object with the provided handle. The attributes
 * that will be updated are those whose names are provided in the varargs. The value given to those
 * attributes will be a string that is equal to their name. The attributeCount parameter should
 * provide the number of attributes that are being updated.
 */
void Test13Federate::quickReflect( int objectHandle, int attributeCount, ... )
{
	//////////////////////
	// create the AHVPS //
	//////////////////////
	// get the object class for the object instance (so we know which object
	// class handle to use when attempting to resolve attribute handles)
	char *objectClass = this->quickOCNameForInstance( objectHandle );

	va_list args;
	va_start( args, attributeCount );	
	RTI::AttributeHandleValuePairSet *ahvps = this->createAHVPS( attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		// generate the value
		char *attributeName = va_arg( args, char* );
		// get the attribute handle
		RTI::AttributeHandle attributeHandle = this->quickACHandle( objectClass, attributeName );
		// put it in the set
		ahvps->add( attributeHandle, attributeName, strlen(attributeName)+1 );
	}

	va_end( args );

	/////////////////////
	// sent the update //
	/////////////////////
	try
	{
		rtiamb->updateAttributeValues( objectHandle, *ahvps, "NA" );
		delete ahvps;
	}
	catch( RTI::Exception &e )
	{
		delete ahvps;
		killTest( e, "quickReflect()" );
	}
}

/*
 * Attempt to update the object of the given handle with the provided values and tag. If the
 * tag is NULL, it will be replaced with "NA". This method EXPECTS THE UPDATE REQUEST TO FAIL,
 * and if it doesn't, the current test will be killed.
 */
void Test13Federate::quickReflectFail( int objectHandle,
                                       RTI::AttributeHandleValuePairSet *ahvps,
                                       const char *tag )
{
	// check the tag
	const char* safeTag = tag == NULL ? "NA" : tag;
	
	// attempt the update
	try
	{
		rtiamb->updateAttributeValues( objectHandle, *ahvps, safeTag );
		killTest( "Was expecting an exception during the updateAttributeValues call" );
	}
	catch( RTI::Exception &e )
	{
		// success!
	}
}

/*
 * Attempt to send an interaction of the class that has its handle provided with the given
 * set of parameter values. If the tag is NULL, it will be replaced with "NA".
 */
void Test13Federate::quickSend( int clazz, 
                                RTI::ParameterHandleValuePairSet *phvps, 
                                const char* tag )
{
	// check the tag
	if( tag == NULL )
		tag = "NA";
	
	try
	{
		rtiamb->sendInteraction( clazz, *phvps, tag );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickSend()" );
	}
}

/*
 * Attempt to send an interaction of the type identified in classHandle. The parameters that
 * are sent will be those identified in the varargs. The values of the parameters will be a string
 * which holds the name of that parameter. The number of paramters that is being sent needs to be
 * provided in the parameterCount argument. The tag will automatically be set to "NA".
 */
void Test13Federate::quickSend( int classHandle, int parameterCount, ... )
{
	//////////////////////
	// create the PHVPS //
	//////////////////////
	// get the interaction class name for the class handle (so
	char *interactionClass = this->quickICName( classHandle );

	va_list args;
	va_start( args, parameterCount );
	RTI::ParameterHandleValuePairSet *phvps = this->createPHVPS( parameterCount );
	for( int i = 0; i < parameterCount; i++ )
	{
		// generate the value
		char *parameterName = va_arg( args, char* );
		// get the parameter handle
		RTI::ParameterHandle parameterHandle = this->quickPCHandle(interactionClass, parameterName);
		// put it in the set
		phvps->add( parameterHandle, parameterName, strlen(parameterName)+1 );
	}

	va_end( args );

	//////////////////////////
	// sent the interaction //
	//////////////////////////
	try
	{
		rtiamb->sendInteraction( classHandle, *phvps, "NA" );
		delete phvps;
	}
	catch( RTI::Exception &e )
	{
		delete phvps;
		killTest( e, "quickSend()" );
	}	
}

/*
 * Attempt to send an interaction of the type identified in classHandle. The parameters that
 * are sent will be those identified in the varargs. The values of the parameters will be a string
 * which holds the name of that parameter. The number of paramters that is being sent needs to be
 * provided in the parameterCount argument. The tag will automatically be set to "NA". Sends the
 * interaction with the provided time
 */
void Test13Federate::quickSend( int classHandle, double time, int parameterCount, ...  )
{
	//////////////////////
	// create the PHVPS //
	//////////////////////
	// get the interaction class name for the class handle (so
	char *interactionClass = this->quickICName( classHandle );

	va_list args;
	va_start( args, parameterCount );
	RTI::ParameterHandleValuePairSet *phvps = this->createPHVPS( parameterCount );
	for( int i = 0; i < parameterCount; i++ )
	{
		// generate the value
		char *parameterName = va_arg( args, char* );
		// get the parameter handle
		RTI::ParameterHandle parameterHandle = this->quickPCHandle(interactionClass, parameterName);
		// put it in the set
		phvps->add( parameterHandle, parameterName, strlen(parameterName)+1 );
	}

	va_end( args );

	//////////////////////////
	// sent the interaction //
	//////////////////////////
	try
	{
		RTIfedTime sendtime = time;
		rtiamb->sendInteraction( classHandle, *phvps, sendtime, "NA" );
		delete phvps;
	}
	catch( RTI::Exception &e )
	{
		delete phvps;
		killTest( e, "quickSend()" );
	}
}


/*
 * Attempt to send an interaction of the class identified in the classHandle. If the given
 * tag is NULL, it will be replaced with "NA". This method EXPECTS THE SEND REQUEST TO FAIL,
 * and if it doesn't, the current test will be killed.
 */
void Test13Federate::quickSendFail( int classHandle,
                                    RTI::ParameterHandleValuePairSet *phvps,
                                    const char *tag )
{
	// check the tag
	const char* safeTag = tag == NULL ? "NA" : tag;
	
	// attempt the interaction
	try
	{
		rtiamb->sendInteraction( classHandle, *phvps, safeTag );
		killTest( "Was expecting an exception during the sendInteraction call" );
	}
	catch( RTI::Exception &e )
	{
		// success!
	}
}

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////// Data Distribution Management Methods ////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/*
 * Find and return the space handle for the space of the given name. Kill the test if there is
 * an exception.
 */
RTI::SpaceHandle Test13Federate::quickSpaceHandle( const char* spaceName )
{
	try
	{
		return rtiamb->getRoutingSpaceHandle( spaceName );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickSpaceHandle()" );
		return -1;
	}
}

/*
 * Find and return the dimension handle for the dimension of the given name in the space of the
 * given name. Kill the test if there is an exception.
 */
RTI::DimensionHandle Test13Federate::quickDimensionHandle( const char* spaceName, 
                                                           const char* dimensionName )
{
	RTI::SpaceHandle spaceHandle = quickSpaceHandle( spaceName );
	try
	{
		return rtiamb->getDimensionHandle( dimensionName, spaceHandle );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickDimensionHandle()" );
		return -1;
	}
}

/*
 * Create and return a region using the given space handle and number of extents. Kill the test
 * if there is an exception.
 */
RTI::Region* Test13Federate::quickCreateRegion( RTI::SpaceHandle space, RTI::ULong extents )
{
	try
	{
		RTI::Region *region = rtiamb->createRegion( space, extents );
		return region;
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickCreateRegion()" );
		return NULL;
	}
}

/*
 * Create and return a region using the TestSpace from the FOM. The region will have a single
 * extent, with its lower and upper bounds being set to the provided values. Kill the test if
 * there is an exception. 
 */
RTI::Region* Test13Federate::quickCreateTestRegion( RTI::ULong lowerBound, RTI::ULong upperBound )
{
	try
	{
		// create the region
		RTI::Region *region = rtiamb->createRegion( quickSpaceHandle("TestSpace"), 1 );
		// put the upper/lower bound information into it
		RTI::DimensionHandle testDimension = quickDimensionHandle( "TestSpace", "TestDimension" );
		region->setRangeLowerBound( 0, testDimension, lowerBound );
		region->setRangeUpperBound( 0, testDimension, upperBound );
		// notify the RTI that the region has changed
		rtiamb->notifyAboutRegionModification( *region );
		
		return region;
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickCreateTestRegion()" );
		return NULL;
	}
}

/*
 * This is the same as quickCreateTestRegion(ULong,ULong) except that it will create a region
 * instance of the "OtherSpace" space. This will have a single extent with the given upper and
 * lower bound values for the "OtherDimension" dimension.
 */
RTI::Region* Test13Federate::quickCreateOtherRegion( RTI::ULong lowerBound, RTI::ULong upperBound )
{
	try
	{
		// create the region
		RTI::Region *region = rtiamb->createRegion( quickSpaceHandle("OtherSpace"), 1 );
		// put the upper/lower bound information into it
		RTI::DimensionHandle dimension = quickDimensionHandle( "OtherSpace", "OtherDimension" );
		region->setRangeLowerBound( 0, dimension, lowerBound );
		region->setRangeUpperBound( 0, dimension, upperBound );
		// notify the RTI that the region has changed
		rtiamb->notifyAboutRegionModification( *region );
		
		return region;
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickCreateOtherRegion()" );
		return NULL;
	}
}

/*
 * Notify the RTI that the region has changed and should be updated. If there is an exception,
 * kill the test.
 */
void Test13Federate::quickModifyRegion( RTI::Region* theRegion )
{
	try
	{
		rtiamb->notifyAboutRegionModification( *theRegion );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickModifyRegion()" );
	}
}

/*
 * Fetch the RegionToken for the given Region. If there is an exception, kill the test.
 */
RTI::RegionToken Test13Federate::quickGetRegionToken( RTI::Region* theRegion )
{
	try
	{
		return rtiamb->getRegionToken( theRegion );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickGetRegionToken()" );
		return 0; // will never happen, but compiler need it to be a happy camper
	}
}

/*
 * Get the region that is identified by the given region token and return it. If there is an
 * exception, kill the test.
 */
RTI::Region* Test13Federate::quickGetRegion( RTI::RegionToken regionToken )
{
	try
	{
		return rtiamb->getRegion( regionToken );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickGetRegion()" );
		return NULL;
	}
}

/*
 * Register an object instance, associating each of the provided attributes with the provided
 * region. If there is an exception during this process, kill the test, otherwise, return the
 * handle of the newly created instance.
 */
RTI::ObjectHandle Test13Federate::quickRegisterWithRegion( const char* objectClass,
                                                            RTI::Region* theRegion,
                                                            int attributeCount,
                                                            ... /* attribute names */ )
{
	// create the parallel region and attribute handle arrays that specify
	// to the RTI which regions to associate with which objects on registration
	RTI::AttributeHandle *attributes = new RTI::AttributeHandle[attributeCount];
	RTI::Region **regions = new RTI::Region*[attributeCount];
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		char *attributeName = va_arg( args, char* );
		attributes[i] = this->quickACHandle( objectClass, attributeName );
		regions[i] = theRegion; // same region for every attribute
	}
	va_end( args );

	// try and register the instance
	try
	{
		RTI::ObjectHandle theObject =
			rtiamb->registerObjectInstanceWithRegion( this->quickOCHandle(objectClass),
			                                          attributes,
			                                          regions,
			                                          attributeCount );
		
		// clean up and return
		delete [] attributes;
		delete [] regions;
		return theObject;
	}
	catch( RTI::Exception &e )
	{
		delete [] attributes;
		delete [] regions;
		killTest( e, "quickRegisterWithRegion()" );
		return -1;
	}
}

/*
 * This method will associate the provided attribute (names, not handles) with the given region
 * for the given object instance. If there is an exception trying to do this, the current test
 * will be killed.
 */
void Test13Federate::quickAssociateWithRegion( RTI::ObjectHandle theObject,
                                                RTI::Region* theRegion,
                                                int attributeCount,
                                                ... /* attribute names */ )
{
	// get the object class of the instance (used to resolve handle names)
	char *objectClass = this->quickOCName( quickOCHandle(theObject) );
	
	// create an attribute handle set from the given attribute handle names
	RTI::AttributeHandleSet *handleSet = this->createAHS( attributeCount );
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		char *attributeName = va_arg( args, char* );
		handleSet->add( this->quickACHandle(objectClass,attributeName) );
	}
	va_end( args );
	
	// try and do the associate call
	try
	{
		rtiamb->associateRegionForUpdates( *theRegion, theObject, *handleSet );
		delete handleSet;
	}
	catch( RTI::Exception &e )
	{
		delete handleSet;
		killTest( e, "quickAssociateWithRegion()" );
	}
}

/*
 * Unassociate any attributes of the identified object instance that are associated with the
 * given region. If there is an exception trying to do this, the current test will be killed.
 */
void Test13Federate::quickUnassociateWithRegion( RTI::ObjectHandle theObject, 
                                                 RTI::Region* theRegion )

{
	try
	{
		rtiamb->unassociateRegionForUpdates( *theRegion, theObject );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickUnassociateWithRegion()" );
	}
	
}

/*
 * Subscribe to the identified object class for each of the given attribute (handles) using the
 * given region. If there is an exception while trying to do this, the current test will be killed.
 */
void Test13Federate::quickSubscribeWithRegion( RTI::ObjectClassHandle classHandle,
                                                RTI::Region* region,
                                                int attributeCount,
                                                ... /* attribute handles */ )
{
	// turn the given parameter (handles) into an attribute handle set
	RTI::AttributeHandleSet *ahs = this->createAHS( attributeCount );
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		ahs->add( va_arg(args,int) );
	}
	va_end( args );

	// attempt the subscription
	try
	{
		rtiamb->subscribeObjectClassAttributesWithRegion( classHandle, *region, *ahs );
		delete ahs;
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickSubscribeWithRegion(object,handle)" );
	}	
}

/*
 * Subscribe to the identified object class for each of the given attribute (names) using the
 * given region. If there is an exception while trying to do this, the current test will be killed.
 */
void Test13Federate::quickSubscribeWithRegion( const char* className,
                                                RTI::Region* region,
                                                int attributeCount, 
                                                ... /* attribute names */ )
{
	// turn the given parameter (handles) into an attribute handle set
	RTI::AttributeHandleSet *ahs = this->createAHS( attributeCount );
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		char *attributeName = va_arg( args, char* );
		ahs->add( this->quickACHandle(className,attributeName) );
	}
	va_end( args );

	// attempt the subscription
	try
	{
		rtiamb->subscribeObjectClassAttributesWithRegion( quickOCHandle(className), *region, *ahs );
		delete ahs;
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickSubscribeWithRegion(object,name)" );
	}	
}

/*
 * Unsubscribe from the given object class with the given region. If there is an exception during
 * this process, kill the current test.
 */
void Test13Federate::quickUnsubscribeOCWithRegion( RTI::ObjectClassHandle classHandle,
                                                    RTI::Region *region )
{
	try
	{
		rtiamb->unsubscribeObjectClassWithRegion( classHandle, *region );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickUnsubscribeOCWithRegion(object,handle)" );
	}
}

/*
 * Unsubscribe from the given object class with the given region. If there is an exception during
 * this process, kill the current test.
 */
void Test13Federate::quickUnsubscribeOCWithRegion( const char* className, RTI::Region* region )
{
	try
	{
		rtiamb->unsubscribeObjectClassWithRegion( quickOCHandle(className), *region );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickUnsubscribeOCWithRegion(object,name)" );
	}
}

/*
 * Subscribe to the given interaction class using the given region. If there is an exception
 * during this process, kill the current test.
 */
void Test13Federate::quickSubscribeWithRegion( RTI::InteractionClassHandle classHandle, RTI::Region* region )
{
	try
	{
		rtiamb->subscribeInteractionClassWithRegion( classHandle, *region );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickSubscribeWithRegion(interaction,handle)" );
	}
}

void Test13Federate::quickSubscribeWithRegion( const char* className, RTI::Region* region )
{
	try
	{
		rtiamb->subscribeInteractionClassWithRegion( quickICHandle(className), *region );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickSubscribeWithRegion(interaction,name)" );
	}	
}

/*
 * Unsubscribe from the given interaction class with the given region. If there is an exception
 * during this process, kill the current test.
 */
void Test13Federate::quickUnsubscribeICWithRegion( RTI::InteractionClassHandle classHandle,
                                                    RTI::Region *region )
{
	try
	{
		rtiamb->unsubscribeInteractionClassWithRegion( classHandle, *region );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickUnsubscribeICWithRegion(interaction,handle)" );
	}
}

/*
 * Unsubscribe from the given interaction class with the given region. If there is an exception
 * during this process, kill the current test.
 */
void Test13Federate::quickUnsubscribeICWithRegion( const char* className, RTI::Region *region )
{
	try
	{
		rtiamb->unsubscribeInteractionClassWithRegion( quickICHandle(className), *region );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickUnsubscribeICWithRegion(interaction,name)" );
	}
}

/*
 * Much the same as quickSend(int,int,...) except that you can include region data. As usual,
 * if there are any problems, the current test will be killed.
 */
void Test13Federate::quickSendWithRegion( const char* interactionClass,
                                           RTI::Region *region,
                                           int parameterCount,
                                           ... /* parameter names */ )
{
	//////////////////////
	// create the PHVPS //
	//////////////////////
	va_list args;
	va_start( args, parameterCount );
	RTI::ParameterHandleValuePairSet *phvps = this->createPHVPS( parameterCount );
	for( int i = 0; i < parameterCount; i++ )
	{
		// generate the value
		char *parameterName = va_arg( args, char* );
		// get the parameter handle
		RTI::ParameterHandle parameterHandle = this->quickPCHandle(interactionClass, parameterName);
		// put it in the set
		phvps->add( parameterHandle, parameterName, strlen(parameterName)+1 );
	}

	va_end( args );

	//////////////////////////
	// sent the interaction //
	//////////////////////////
	try
	{
		rtiamb->sendInteractionWithRegion( this->quickICHandle(interactionClass),
		                                   *phvps,
		                                   "NA",
		                                   *region );
		delete phvps;
	}
	catch( RTI::Exception &e )
	{
		delete phvps;
		killTest( e, "quickSend()" );
	}
}


//////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Time Management Helper Methods ///////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/*
 * Convert the given Portico FedTime instance into the double value that it represents
 */
double Test13Federate::getTime( RTI::FedTime *time )
{
	return ((RTIfedTime*)time)->getTime();
}

/*
 * Request that time constrained be enabled and block until it is.
 */
void Test13Federate::quickEnableConstrained()
{
	try
	{
		rtiamb->enableTimeConstrained();
		fedamb->waitForConstrainedEnabled();
	}
	catch( RTI::Exception &e )
	{
		this->killTest( e, "quickEnableConstrained()" );
	}
}

/*
 * Request that time constrained be enabled, but don't wait for it to become constrained before
 * returning.
 */
void Test13Federate::quickEnabledConstrainedRequest()
{
	try
	{
		rtiamb->enableTimeConstrained();
	}
	catch( RTI::Exception &e )
	{
		this->killTest( e, "quickEnableConstrainedRequest()" );
	}
}

/*
 * Disable time constrained.
 */
void Test13Federate::quickDisableConstrained()
{
	try
	{
		rtiamb->disableTimeConstrained();
		fedamb->constrained = RTI::RTI_FALSE;
	}
	catch( RTI::Exception &e )
	{
		this->killTest( e, "quickDisableConstrained()" );
	}
}

/*
 * Enables async delivery
 */
void Test13Federate::quickEnableAsync()
{
	try
	{
		rtiamb->enableAsynchronousDelivery();
	}
	catch( RTI::Exception& e )
	{
		killTest( e, "quickEnableAsync()" );
	}
}

/*
 * Disables async delivery
 */
void Test13Federate::quickDisableAsync()
{
	try
	{
		rtiamb->disableAsynchronousDelivery();
	}
	catch( RTI::Exception& e )
	{
		killTest( e, "quickDisableAsync()" );
	}
}

/*
 * Request that time regulation be enabled using the given lookahead. Block until the federate
 * becomes regulating.
 */
void Test13Federate::quickEnableRegulating( double lookahead )
{
	RTIfedTime currentTime = fedamb->logicalTime;
	RTIfedTime lookaheadTime = lookahead;

	try
	{
		rtiamb->enableTimeRegulation( currentTime, lookaheadTime );
		fedamb->waitForRegulatingEnabled();
	}
	catch( RTI::Exception &e )
	{
		this->killTest( e, "quickEnableRegulating()" );
	}
}

/*
 * Request that time regulation be enabled using the given lookahead. Don't wait for the federate
 * to actually become regulating, just return right away.
 */
void Test13Federate::quickEnableRegulatingRequest( double lookahead )
{
	RTIfedTime currentTime = fedamb->logicalTime;
	RTIfedTime lookaheadTime = lookahead;

	try
	{
		rtiamb->enableTimeRegulation( currentTime, lookaheadTime );
	}
	catch( RTI::Exception &e )
	{
		this->killTest( e, "quickEnableRegulatingRequest()" );
	}
}

/*
 * Disable time regulation.
 */
void Test13Federate::quickDisableRegulating()
{
	try
	{
		rtiamb->disableTimeRegulation();
		fedamb->regulating = RTI::RTI_FALSE;
	}
	catch( RTI::Exception &e )
	{
		this->killTest( e, "quickDisableRegulating()" );
	}
}

/*
 * Query the RTI for the current lookahead and return it.
 */
double Test13Federate::quickQueryLookahead()
{
	RTI::FedTime *current = NULL;
	
	try
	{
		rtiamb->queryLookahead( *current );
	}
	catch( RTI::Exception &e )
	{
		this->killTest( e, "quickQueryLookahead()" );
	}
	
	double doubleValue = getTime( current );
	delete current;
	return doubleValue;
}

/*
 * Attempt to modify the lookahead of the federate, setting it to the given value.
 */
void Test13Federate::quickModifyLookahead( double newLookahead )
{
	RTIfedTime lookaheadTime = newLookahead;

	try
	{
		rtiamb->modifyLookahead( lookaheadTime );
	}
	catch( RTI::Exception &e )
	{
		this->killTest( e, "quickModifyLookahead()" );
	}	
}

/*
 * Issue a time advance request to the given time, returning once the request has been made.
 */
void Test13Federate::quickAdvanceRequest( double newTime )
{
	RTIfedTime requestTime = newTime;

	try
	{
		rtiamb->timeAdvanceRequest( requestTime );
	}
	catch( RTI::Exception &e )
	{
		this->killTest( e, "quickAdvanceRequest()" );
	}	
}

/*
 * Issue a time advance request to the given time and then wait until the request has been granted
 * before retuning.
 */
void Test13Federate::quickAdvanceAndWait( double newTime )
{
	RTIfedTime requestTime = newTime;

	try
	{
		rtiamb->timeAdvanceRequest( requestTime );
		// wait for the advance to occur
		fedamb->waitForTimeAdvance( newTime );
	}
	catch( RTI::Exception &e )
	{
		this->killTest( e, "quickAdvanceRequestAndWait()" );
	}
}

/*
 * Issue a timeAdvanceRequestAvailable() and then return.
 */
void Test13Federate::quickAdvanceRequestAvailable( double newTime )
{
	RTIfedTime requestTime = newTime;

	try
	{
		rtiamb->timeAdvanceRequestAvailable( requestTime );
	}
	catch( RTI::Exception &e )
	{
		this->killTest( e, "quickAdvanceRequestAvailable()" );
	}	
}

/*
 * Issue a nextEventRequest() and then return.
 */
void Test13Federate::quickNextEventRequest( double newTime )
{
	RTIfedTime requestTime = newTime;

	try
	{
		rtiamb->nextEventRequest( requestTime );
	}
	catch( RTI::Exception &e )
	{
		this->killTest( e, "quickNextEventRequest()" );
	}	
}

/*
 * Issue a flushQueueRequest() and then return.
 */
void Test13Federate::quickFlushQueueRequest( double maxTime )
{
	RTIfedTime time = maxTime;
	try
	{
		rtiamb->flushQueueRequest( time );
	}
	catch( RTI::Exception &e )
	{
		this->killTest( e, "quickFlushQueueRequest()" );
	}
}

/////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////// Ownership Management Helper Methods ////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Issue a request to acquire the specified attributes of the specified object if they are
 * currently available (unowned).
 */
void Test13Federate::quickAcquireIfAvailableRequest( RTI::ObjectHandle theObject,
                                                      int attributeCount,
                                                      ... )
{
	va_list args;
	va_start( args, attributeCount );
	RTI::AttributeHandleSet *ahs = this->createAHS( attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		ahs->add( va_arg(args,int) );
	}

	va_end( args );

	//////////////////////////
	// sent the interaction //
	//////////////////////////
	try
	{
		rtiamb->attributeOwnershipAcquisitionIfAvailable( theObject, *ahs );
		delete ahs;
	}
	catch( RTI::Exception &e )
	{
		delete ahs;
		killTest( e, "quickAcquireIfAvailableRequest()" );
	}	
}

/**
 * Issue a request to acquire the specified attributes of the specified object.
 */
void Test13Federate::quickAcquireRequest( RTI::ObjectHandle theObject, int attributeCount, ... )
{
	va_list args;
	va_start( args, attributeCount );
	RTI::AttributeHandleSet *ahs = this->createAHS( attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		ahs->add( va_arg(args,int) );
	}

	va_end( args );

	//////////////////////////
	// sent the interaction //
	//////////////////////////
	try
	{
		rtiamb->attributeOwnershipAcquisition( theObject, *ahs, "NA" );
		delete ahs;
	}
	catch( RTI::Exception &e )
	{
		delete ahs;
		killTest( e, "quickAcquireRequest()" );
	}	
}

/*
 * Issue a request to unconditionally release the specified attributes of the given object. If
 * there is a problem doing the request, the current test is failed.
 */
void Test13Federate::quickUnconditionalRelease( RTI::ObjectHandle theObject,
                                                 int attributeCount,
                                                 ... )
{
	va_list args;
	va_start( args, attributeCount );
	RTI::AttributeHandleSet *ahs = this->createAHS( attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		ahs->add( va_arg(args,int) );
	}

	va_end( args );

	//////////////////////////
	// sent the interaction //
	//////////////////////////
	try
	{
		rtiamb->unconditionalAttributeOwnershipDivestiture( theObject, *ahs );
		delete ahs;
	}
	catch( RTI::Exception &e )
	{
		delete ahs;
		killTest( e, "quickUnconditionalRelease()" );
	}
}

/*
 * Issue a request to do a negotiated release the specified attributes of the given object. If
 * there is a problem doing the request, the current test is failed.
 */
void Test13Federate::quickNegotiatedRelease( RTI::ObjectHandle theObject,
                                              int attributeCount,
                                              ... )
{
	va_list args;
	va_start( args, attributeCount );
	RTI::AttributeHandleSet *ahs = this->createAHS( attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		ahs->add( va_arg(args,int) );
	}

	va_end( args );

	//////////////////////////
	// sent the interaction //
	//////////////////////////
	try
	{
		rtiamb->negotiatedAttributeOwnershipDivestiture( theObject, *ahs, "NA" );
		delete ahs;
	}
	catch( RTI::Exception &e )
	{
		delete ahs;
		killTest( e, "quickNegotiatedRelease()" );
	}
}

/*
 * Issues a request to the RTIamb to query the ownership of the specified attribute in the
 * identified object. The method will then wait for the calling informing it of the attributes
 * owner and will return the federate handle for that federate.
 * 
 * If there is a problem with the call (such as an exception) the current test will be failed.
 */
int Test13Federate::quickQueryOwnership( RTI::ObjectHandle theObject,
                                          RTI::AttributeHandle theAttribute )
{
	// issue the request
	try
	{
		rtiamb->queryAttributeOwnership( theObject, theAttribute );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickQueryOwnership()" );
	}
	
	// wait for the response
	return this->fedamb->waitForOwnershipResponse( theObject, theAttribute );
}

/*
 * Issues a release response to the RTIamb. If there is an exception during this call, the current
 * test is failed, otherwise, the method will return happily.
 */
void Test13Federate::quickReleaseResponse( RTI::ObjectHandle theObject, int attributeCount, ... )
{
	va_list args;
	va_start( args, attributeCount );
	RTI::AttributeHandleSet *ahs = this->createAHS( attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		ahs->add( va_arg(args,int) );
	}

	va_end( args );

	//////////////////////////
	// sent the interaction //
	//////////////////////////
	try
	{
		rtiamb->attributeOwnershipReleaseResponse( theObject, *ahs );
		delete ahs;
	}
	catch( RTI::Exception &e )
	{
		delete ahs;
		killTest( e, "quickReleaseResponse()" );
	}
}

/*
 * This method will assert that the given federate is the owner of all the specified attributes
 * of the given object. If one of them is not, the current test will be failed. Note that you can
 * use TestNG6Federate::OWNER_UNOWNED and TestNG6Federate::OWNER_RTI as the federate handle if it
 * is appropriate for what you are trying to test.
 */
void Test13Federate::quickAssertOwnedBy( int federateHandle,
                                          RTI::ObjectHandle theObject,
                                          int attributeCount,
                                          ... )
{
	// fill the ahs out
	va_list args;
	va_start( args, attributeCount );
	RTI::AttributeHandleSet *attributeHandles = this->createAHS( attributeCount );
	int i = 0;
	for( i = 0; i < attributeCount; i++ )
	{
		attributeHandles->add( va_arg(args,int) );
	}
	va_end( args );
	
	unsigned int counter;
	for( counter = 0; counter < attributeHandles->size(); counter++ )
	{
		int owner = quickQueryOwnership( theObject, attributeHandles->getHandle(counter) );
		if( federateHandle != owner )
		{
			killTest( "Owner of attribute %i is %i, expected %i",
			          attributeHandles->getHandle(counter),
			          owner,
			          federateHandle );
		}
	}
}

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// Save/Restore Helpers ////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/**
 * This method will trigger a save request with the given label and then wait until a
 * callback initiating the save have been received. If there is a problem sending out
 * the request or the initiation isn't received in a timely fashion, the current test
 * will be failed.
 */
void Test13Federate::quickSaveInProgress( const char* saveLabel )
{
	// initiate a save from this federate using the given label
	this->quickSaveRequest( saveLabel );
	
	// wait for the callback telling us the save has been initiated
	fedamb->waitForSaveInitiated( saveLabel );
}

/*
 * Request a federation save with the given label, fail the test if there is an exception
 */
void Test13Federate::quickSaveRequest( const char* saveLabel )
{
	try
	{
		rtiamb->requestFederationSave( saveLabel );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickSaveRequest()" );
	}
}

/*
 * Tells the RTI that the federate has begun saving. If an exception is thrown, the current test
 * will be failed.
 */
void Test13Federate::quickSaveBegun()
{
	try
	{
		rtiamb->federateSaveBegun();
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickSaveBegun()" );
	}
}

/*
 * Tells the RTI that the federate has successfully completed saving. If an exception is thrown,
 * the current test will be failed.
 */
void Test13Federate::quickSaveComplete()
{
	try
	{
		rtiamb->federateSaveComplete();
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "federateSaveComplete()" );
	}
}

/*
 * Tells the RTI that the federate has unsuccessfully completed saving. If an exception is thrown,
 * the current test will be failed.
 */
void Test13Federate::quickSaveNotComplete()
{
	try
	{
		rtiamb->federateSaveNotComplete();
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "federateSaveNotComplete()" );
	}
}

/*
 * This method will handle the entire save process, triggering and completing the process
 * using the given label. A reference to each of the federates is taken from the test to which
 * this federate is attached. Note that this method will call methods on the other federates
 * as required to complete the save.
 * 
 * At the end of this process, the federation will have been successfully saved using the
 * given label. If there is a problem in any of the steps (initiating the save, starting it
 * or successfully completing in any of the federates), the current test will be failed.
 */
void Test13Federate::quickSaveToCompletion( const char* saveLabel, int federateCount, ... )
{
	// initiate a save from this federate using the given label
	quickSaveRequest( saveLabel );
	
	// get the set of federates in the federation
	std::vector<Test13Federate*> federates;
	va_list args;
	va_start( args, federateCount );
	int i = 0;
	for( i = 0; i < federateCount; i++ )
	{
		federates.push_back( va_arg(args,Test13Federate*) );
	}
	va_end( args );

	// wait for the callback telling EACH FEDERATE that the save has been initiated
	for( i = 0; i < federateCount; i++ )
		federates[i]->fedamb->waitForSaveInitiated( saveLabel );

	// have each federate signal that it has begun its save
	for( i = 0; i < federateCount; i++ )
	{
		federates[i]->quickSaveBegun();
		federates[i]->quickSaveComplete();
	}
	
	// wait for the save to be completed
	for( i = 0; i < federateCount; i++ )
	{
		federates[i]->fedamb->waitForFederationSaved();
	}
}


/*
 * Requests a federation restore from the RTIambassador and then returns. If there is an
 * exception performing this call, the current test is failed.
 */
void Test13Federate::quickRestoreRequest( const char* label )
{
	try
	{
		rtiamb->requestFederationRestore( label );
	}
	catch( RTI::Exception& e )
	{
		killTest( e, "quickRestoreRequest()" );
	}
}

/*
 * This method initiates a federation restore request (calling quickRestoreRequest(char*)) and
 * then ticks until either a success of failure notification from the RTI is received. If the
 * initiation is not a success, or there is an exception initiating it, the current test is failed.
 */
void Test13Federate::quickRestoreRequestSuccess( const char* label )
{
	quickRestoreRequest( label );
	fedamb->waitForRestoreRequestSuccess( label );
}

/*
 * Tell the RTI that the federate has completed its restore. If there is a problem, the current
 * test will be killed.
 */
void Test13Federate::quickRestoreComplete()
{
	try
	{
		rtiamb->federateRestoreComplete();
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickRestoreComplete" );
	}
}

/*
 * Tell the RTI that the federate has completed its restore *unsuccessfully*. If there is a
 * problem, the current test will be killed.
 */
void Test13Federate::quickRestoreNotComplete()
{
	try
	{
		rtiamb->federateRestoreNotComplete();
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickRestoreNotComplete" );
	}
}

/*
 * This method will trigger and complete a Save and then initiate a restore, waiting for a
 * callback indicating that the federation restore has begun. The given label is used both
 * for the save and the restore. The set of all the federates is received from the test
 * class that this federate is associated with. 
 */
void Test13Federate::quickRestoreInProgress( const char* saveLabel, int federateCount, ... )
{
	///////////////////////////////////////////////////
	////////////////// complete save //////////////////
	///////////////////////////////////////////////////
	// because varargs are such a pain in C++, I'm just putting the code for quickSaveToCompletion
	// directly in here (rather than declaring another method that take va_list or whatever rather
	// than ... stupid C++, I'm spoilt by Java vargars! (and everything else Java)
	// initiate a save from this federate using the given label
	quickSaveRequest( saveLabel );
	
	// get the set of federates in the federation
	std::vector<Test13Federate*> federates;
	va_list args;
	va_start( args, federateCount );
	int i = 0;
	for( i = 0; i < federateCount; i++ )
	{
		federates.push_back( va_arg(args,Test13Federate*) );
	}
	va_end( args );
	
	// wait for the callback telling EACH FEDERATE that the save has been initiated
	for( i = 0; i < federateCount; i++ )
		federates[i]->fedamb->waitForSaveInitiated( saveLabel );

	// have each federate signal that it has begun its save
	for( i = 0; i < federateCount; i++ )
	{
		federates[i]->quickSaveBegun();
		federates[i]->quickSaveComplete();
	}
	
	// wait for the save to be completed
	for( i = 0; i < federateCount; i++ )
	{
		federates[i]->fedamb->waitForFederationSaved();
	}
	
	///////////////////////////////////////////////////
	///////////// put restore in progress /////////////
	///////////////////////////////////////////////////
	// kick off a restore
	quickRestoreRequest( saveLabel );
	fedamb->waitForRestoreRequestSuccess( saveLabel );
	
	// wait for the "restore begun" notice
	for( i = 0; i < federateCount; i++ )
		federates[i]->fedamb->waitForFederationRestoreBegun();

	for( i = 0; i < federateCount; i++ )
		federates[i]->fedamb->waitForFederateRestoreInitiated( saveLabel );
}

//////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////// Handle Resolution Helper Methods //////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/*
 * Fetch the handle for the given object class
 */
RTI::ObjectClassHandle Test13Federate::quickOCHandle( const char* objectClass )
{
	try
	{
		return rtiamb->getObjectClassHandle( objectClass );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickOCHandle()" );
		return -1;
	}
}

/*
 * Fetch the handle for the given attribute contained in the given object class
 */
RTI::AttributeHandle Test13Federate::quickACHandle( const char* objectClass, 
                                                    const char* attributeName )
{
	try
	{
		return rtiamb->getAttributeHandle( attributeName, quickOCHandle(objectClass) );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickACHandle()" );
		return -1;
	}
}

/*
 * Fetch the handle for the identified interaction class
 */
RTI::InteractionClassHandle Test13Federate::quickICHandle( const char* interactionClass )
{
	try
	{
		return rtiamb->getInteractionClassHandle( interactionClass );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickICHandle()" );
		return -1;
	}
}

/*
 * Fetch the handle for the identified parameter in the given interaction class
 */
RTI::ParameterHandle Test13Federate::quickPCHandle( const char* interactionClass, 
                                                    const char* parameterName )
{
	try
	{
		return rtiamb->getParameterHandle( parameterName, quickICHandle(interactionClass) );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickPCHandle()" );
		return -1;
	}
}

/*
 * Fetch the object class handle for the object instance with the given handle
 */
RTI::ObjectClassHandle Test13Federate::quickOCHandle( int objectHandle )
{
	try
	{
		return rtiamb->getObjectClass( objectHandle );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickOCHandle(instanceHandle)" );
		return -1;
	}
}

/*
 * Returns the name of the object class represented by the given class handle
 */
char* Test13Federate::quickOCName( RTI::ObjectClassHandle classHandle )
{
	try
	{
		return rtiamb->getObjectClassName( classHandle );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickOCName()" );
		return NULL;
	}
}

/*
 * Fetch the name of the object class that the object instance with given handle is of
 */
char* Test13Federate::quickOCNameForInstance( int objectHandle )
{
	try
	{
		return rtiamb->getObjectClassName( this->quickOCHandle(objectHandle) );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickOCNameForInstance()" );
		return NULL;
	}
}

/*
 * Fetch the name of the interaction class identified by the given handle
 */
char* Test13Federate::quickICName( int interactionHandle )
{
	try
	{
		return rtiamb->getInteractionClassName( interactionHandle );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickICName()" );
		return NULL;
	}
}

//////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Container Type Helper Methods ////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

/*
 * Create a new, empty AttributeHandleSet of the given size
 */
RTI::AttributeHandleSet* Test13Federate::createAHS( int size )
{
	try
	{
		return RTI::AttributeHandleSetFactory::create( size );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "createAHS(empty)" );
		return NULL;
	}
}

/*
 * Create a new AttributeHandleSet and populate it with the given RTI::AttributeHandle instances
 * in the varargs. The number of arguments supplied is assumed to be the size of the set.
 */
RTI::AttributeHandleSet* Test13Federate::populatedAHS( int size, ... )
{
	// create the set
	RTI::AttributeHandleSet *theSet = createAHS( size );
	
	// populate it
	va_list args;
	va_start( args, size );

	for( int i = 0; i < size; i++ )
		theSet->add( va_arg(args,RTI::AttributeHandle) );
	
	va_end( args );
	
	return theSet;
}

/*
 * Create a new, empty FederateHandleSet of the given size
 */
RTI::FederateHandleSet* Test13Federate::createFHS( int size )
{
	try
	{
		return RTI::FederateHandleSetFactory::create( size );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "createFHS(empty)" );
		return NULL;
	}
}

/*
 * Create a new FederateHandleSet and populate it with the given RTI::FederateHandle instances
 * in the varargs. The number of arguments supplied is assumed to be the size of the set.
 */
RTI::FederateHandleSet* Test13Federate::populatedFHS( int size, ... )
{
	// create the set
	RTI::FederateHandleSet *theSet = createFHS( size );
	
	// populate it
	va_list args;
	va_start( args, size );

	for( int i = 0; i < size; i++ )
		theSet->add( va_arg(args,RTI::FederateHandle) );
	
	va_end( args );
	
	return theSet;
}

/*
 * Create a new, empty AttributeHandleValuePairSet of the given size
 */
RTI::AttributeHandleValuePairSet* Test13Federate::createAHVPS( int size )
{
	try
	{
		return RTI::AttributeSetFactory::create( size );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "createAHVPS(empty)" );
		return NULL;
	}
}

/*
 * Create a new, empty ParameterHandleValuePairSet of the given size
 */
RTI::ParameterHandleValuePairSet* Test13Federate::createPHVPS( int size )
{
	try
	{
		return RTI::ParameterSetFactory::create( size );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "createPHVPS(empty)" );
		return NULL;
	}
}

//////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// General Utility Helper Methods ///////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/* Fetch the RTIambassador that is being used by this test federate. */
RTI::RTIambassador* Test13Federate::getRtiAmb()
{
	return this->rtiamb;
}

/* Call rtiamb->tick(), kill the test if there is a problem */
void Test13Federate::quickTick()
{
	try
	{
		this->rtiamb->tick();
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickTick()" );
	}
}

/* Call rtiamb->tick(double,double), kill the test if there is a problem */
void Test13Federate::quickTick( double min, double max )
{
	try
	{
		this->rtiamb->tick( min, max );
	}
	catch( RTI::Exception &e )
	{
		killTest( e, "quickTick()" );
	}
}

/*
 * This method will construct an appropriate about an unexpected exception and then will use
 * CPPUNIT_FAIL to kill the active test. The activeMethod parameter should be the name of the
 * method that was active at the time of the exception.
 */
void Test13Federate::killTest( RTI::Exception &e, const char* activeMethod )
{
	// create a message notifying of the death
	char message[4096];
	sprintf( message,
	         "(%s) Unexpected exception in %s: %s", this->federateName, activeMethod, e._reason );

	// kill the test
	CPPUNIT_FAIL( message );
}

/*
 * This method will cause the currently executing test to fail, passing the message supplied
 * in printf() style as the arguments.
 */
void Test13Federate::killTest( const char *format, ... )
{
	// start the var-arg stuff 
	va_list args;
	va_start( args, format );
	
	// turn the args into a single string
	// http://www.cplusplus.com/reference/clibrary/cstdio/vsprintf.html
	char buffer[4096];
	vsprintf( buffer, format, args );
	
	// clean up the varargs
	va_end( args );

	// kill the test
	CPPUNIT_FAIL( buffer );
}

