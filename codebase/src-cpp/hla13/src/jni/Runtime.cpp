/*
 *   Copyright 2009 The Portico Project
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
#include "Runtime.h"

PORTICO13_NS_START

Runtime* Runtime::instance = NULL;

//----------------------------------------------------------
//                      CONSTRUCTORS
//----------------------------------------------------------
Runtime::Runtime() throw( HLA::RTIinternalError )
{
	// check for the presence of the PORTICO_DEBUG environment variable. if it
	// exists, set the global level of the Logger to TRACE.
	char *value = getenv( "PORTICO_DEBUG" );
	if( value != NULL )
	{
		Logger::setGlobalLevel( value );
	}

	// check to see if we should include JNI checking information for the JVM
	//char *jniEnvVar = getenv( "PORTICO_JNICHECK" );
	//if( jniEnvVar != NULL )
	//	this->jniCheck = true;
	//else
	//	this->jniCheck = false;

	this->logger   = new Logger( "c++" );
	this->attached = false;
	this->jvm      = NULL;
	this->jvmenv   = NULL;

	this->activeRtis = new std::map<int,JavaRTI*>();

	// fire up the JVM
	this->initializeJVM();
	this->processRid();
}

Runtime::~Runtime()
{
	delete this->logger;

	// clean up the collection and remove it
	MAP_CLEANUP( int, JavaRTI*, this->activeRtis );
	delete this->activeRtis;

	// detach from the JVM and destroy it
	if( this->attached )
	{
		// We didn't create the JVM, we only attached to it. In this
		// case, don't destroy it, just detact from it.
		logger->debug( "detaching from the JVM (no destroy)" );
		this->jvm->DetachCurrentThread();
	}
	else
	{
		// We created the JVM and thus we must destroy it
		logger->debug( "destroying the JVM" );
		this->jvm->DestroyJavaVM();
	}
}

//----------------------------------------------------------
//                    INSTANCE METHODS
//----------------------------------------------------------

JavaRTI* Runtime::newRtiAmbassador() throw( HLA::RTIinternalError )
{
	logger->info( "newRtiAmbassador: creating new JavaRTI" );

	// create the instance
	JavaRTI *newRTI = new JavaRTI();

	// store it in the map of active instances
	int id = newRTI->getId();
	(*activeRtis)[id] = newRTI;

	return newRTI;
}

/*
 * This method will return the active RTI with the given id. If there is no
 * active RTI with that id, NULL is returned
 */
JavaRTI* Runtime::getRtiAmbassador( int id )
{
	// see if the map contains the value first
	if( activeRtis->find(id) == activeRtis->end() )
		return NULL;

	// if does exist, return it
	return (*activeRtis)[id];
}

void Runtime::removeRtiAmbassador( int id )
{
	logger->info( "removeRtiAmbassador: removing JavaRTI reference" );

	// remove the instance from the list of active RTIs
	activeRtis->erase( id );
}

/*
 * This method will initialize the JVM. If there is already an active JVM
 * running, it will just attach to it. If there isn't an active JVM, it will
 * create and start a new one
 */
void Runtime::initializeJVM() throw( HLA::RTIinternalError )
{
	logger->debug( "Initialize the JVM" );

	///////////////////////////////////////////
	// 1. get the classpath and library path //
	///////////////////////////////////////////
	char** paths = this->generatePaths();
	logger->debug( "Using Classpath   : %s", paths[0] );
	logger->debug( "Using Library Path: %s", paths[1] );

	/////////////////////////////////////////////
	// 2. check to see if a JVM already exists //
	/////////////////////////////////////////////
	// before we can create or connect to the JVM, we need to specify its environment
	JavaVMInitArgs vmArgs;
	JavaVMOption options[3];
	options[0].optionString = paths[0];
	options[1].optionString = paths[1];
	options[2].optionString = (char*)"-Xss8m";
	vmArgs.nOptions = 3;
	vmArgs.version = JNI_VERSION_1_2;
	vmArgs.options = options;
	vmArgs.ignoreUnrecognized = JNI_TRUE;

	// Before we create the JVM, we will check to see if one already exists or
	// not. If there is an existing one, we will just attach to it rather than
	// creating a separate one.
	jint result;
	jsize jvmCount = 0;
	result = JNI_GetCreatedJavaVMs( &jvm, 1, &jvmCount );
	if( this->jvm != NULL && jvmCount > 0 && result == 0 )
	{
		///////////////////////////////////////////////////////////////
		// JVM already exists, just attach to the existing reference //
		///////////////////////////////////////////////////////////////
		logger->debug( "Attaching to existing jvm" );
		result = jvm->AttachCurrentThread( (void**)&jvmenv, &vmArgs );
		// check the result
		if( result < 0 )
		{
			logger->fatal( "*** Couldn't attach to existing JVM! ***" );
			throw HLA::RTIinternalError( "*** Couldn't attach to existing JVM! ***" );
		}

		this->attached = true;
		return;
	}

	//////////////////////////////////
	// 3. create a new JVM instance //
	//////////////////////////////////
	// JVM doesn't exist yet, create a new one to work with
	logger->debug( "Creating a new JVM" );
	result = JNI_CreateJavaVM( &jvm, (void**)&jvmenv, &vmArgs );

	////////////////////////////////////////////////
	// 3a. clean up and check the creation result //
	////////////////////////////////////////////////
	// clean up before exiting
	delete[] paths[0];
	delete[] paths[1];
	delete[] paths;

	if( result < 0 )
	{
		logger->fatal( "*** Couldn't create a new JVM! *** result=%d", result );
		throw HLA::RTIinternalError( "*** Error creating a new JVM! ***" );
		return;
	}

	logger->info( "New JVM has been created" );
}

/*
 * This method will generate the classpath and library path that will be
 * used when starting the JVM. The Classpath will prefix the value of the
 * CLASSPATH environment variable to the string before adding Portico needed
 * entries. The library path will prefix the values of PATH (for windows) or
 * LD_LIBRARY_PATH (for good systems) before adding its own needed entries.
 *
 * The return value will be two char[]'s:
 *  1. The first points to the classpath information
 *  2. The second points to the library path information
 *
 * MEMORY MANAGEMENT: Note that the caller is responsible for the memory that
 *                    is created by this call and must "delete []" the arrays
 */
char** Runtime::generatePaths() throw( HLA::RTIinternalError )
{
	// check for the presence of RTI_HOME
	char *rtihome = getenv( "RTI_HOME" );

	// RTI_HOME *has* to be set. No two ways about it. Fail out if this isn't the case
	// Wwe make all inferences about path locations based off it, give it to us!
	if( !rtihome )
	{
		logger->fatal( "RTI_HOME not set: this is *REQUIRED* to point to your Portico directory" );
		throw HLA::RTIinternalError( "RTI_HOME not set: this *must* point to your Portico directory" );
	}

	/////////////////////////////////
	// 2. set up the various paths //
	/////////////////////////////////
	#ifdef WIN32
		return generateWin32Path( rtihome );
	#else
		char **returnValue = generateUnixPath( rtihome );
		return returnValue;
		//return generateUnixPath( rtihome );
	#endif
}

/*
 * Generate the java.class.path and java.library.path for Windows
 */
char** Runtime::generateWin32Path( char* rtihome ) throw( HLA::RTIinternalError )
{
	// this is the memory that will be returned
	char** returnValue = new char*[2];

	// pick up the system classpath //
	const char *systemClasspath = getenv( "CLASSPATH" );
	if( !systemClasspath )
	{
		logger->debug( "CLASSPATH not set, using ." );
		systemClasspath = ".";
	}

	// fill out the classpath
	returnValue[0] = new char[4096+strlen(systemClasspath)];
	sprintf( returnValue[0], "-Djava.class.path=.;%s;%s\\lib\\portico.jar", systemClasspath, rtihome );

	// pick up the system path
	const char *systemPath = getenv( "PATH" );
	if( !systemPath )
		systemPath = ".";

	// get JAVA_HOME, if it isn't set, assume we have the standalone version of Portico
	// ***JAVA_HOME STRUCTURE NOTE***
	// even if JAVA_HOME is present, we have to add "JAVA_HOME\jre\bin\client" AND
	// "JAVA_HOME\bin\client" to the path because we can't really tell if they're using the
	// JDK or the JRE, and the path will be different. Easiest just to add both.
	const char *javahome = getenv( "JAVA_HOME" );
	if( !javahome )
	{
		logger->warn( "WARNING Environment variable JAVA_HOME not set, assuming it is: %s\\jre",
		              rtihome );

		returnValue[1] = new char[4096+strlen(systemPath)];
		sprintf( returnValue[1],
		         "-Djava.library.path=.;%s;%s\\bin;%s\\jre\\bin\\client",
		         systemPath,
		         rtihome,
		         rtihome );
	}
	else
	{
		logger->debug( "JAVA_HOME set to %s", javahome );
		returnValue[1] = new char[4096+strlen(systemPath)];
		sprintf( returnValue[1],
		         "-Djava.library.path=.;%s;%s\\bin;%s\\jre\\bin\\client;%s\\bin\\client",
		         systemPath,
		         rtihome,
		         javahome,
		         javahome );
	}

	return returnValue;
}

/*
 * Generate the java.class.path and java.library.path for Linux and Mac OS X
 */
char** Runtime::generateUnixPath( char* rtihome ) throw( HLA::RTIinternalError )
{
	// this is the memory that will be returned
	char** returnValue = new char*[2];

	// pick up the system classpath //
	const char *systemClasspath = getenv( "CLASSPATH" );
	if( !systemClasspath )
	{
		logger->debug( "CLASSPATH not set, using ." );
		systemClasspath = "./";
	}

	// fill out the classpath
	returnValue[0] = new char[4096+strlen(systemClasspath)];
	sprintf( returnValue[0], "-Djava.class.path=.:%s:%s/lib/portico.jar", systemClasspath, rtihome );

	// pick up the system path
	#ifdef __APPLE__
	const char *systemPath = getenv( "DYLD_LIBRARY_PATH" );
	#else
	char *systemPath = getenv( "LD_LIBRARY_PATH" );
	#endif

	// make sure we have a system path
	if( !systemPath )
		systemPath = "";

	const char *javahome = getenv( "JAVA_HOME" );
	if( !javahome )
	{
		#ifndef __APPLE__
		logger->warn( "WARNING Environment variable JAVA_HOME not set, assuming it is: %s/jre",
		              rtihome );
		#endif

		returnValue[1] = new char[4096+strlen(systemPath)];
		sprintf( returnValue[1],
		         "-Djava.library.path=.:%s:%s/lib:%s/jre/lib/i386/client:%s/jre/lib/amd64/server",
		         systemPath,
		         rtihome,
		         rtihome,
		         rtihome );
	}
	else
	{
		logger->debug( "JAVA_HOME set to %s", javahome );
		returnValue[1] = new char[4096+strlen(systemPath)];
		sprintf( returnValue[1],
		         "-Djava.library.path=.:%s:%s/lib:%s/jre/lib/i386/client:%s/jre/lib/amd64/server",
		         systemPath,
		         rtihome,
		         javahome,
		         javahome );
	}

	return returnValue;
}

/*
 * This method will locate and parse the RID file. If the file does not exist, it
 * will just return and do nothing. The actual RID parsing is done in Java. All
 * properties in the RID file are set as system properties. This method then checks
 * the system properties it is interested in.
 *
 * The Java side of this method will first check to see if the environment variable
 * RTI_RID_FILE is set. If it is, it will use that as the location for the RID file.
 * If it is not set, it will look for a file called RTI.rid in the location from
 * which the program was launched. If the file cannot be found, nothing will happen
 * and this method will return.
 *
 * If the file can't be found in the environment variable specified location, the
 * java method will fall back to ./RTI.rid
 *
 * After the RID has been parsed (assuming it exists) this method will check the java
 * system properties for anything we are interested in. At the moment this is only the
 * log level that the C++ side should use. Two properties will be checked. Firstly, the
 * "portico.c++.loglevel" will be checked, if it exists, its value will be used as the
 * global log level (see Logger.h). If it doesn't exist, the propery "portico.lrc.loglevel"
 * will also be checked for a value and it will be used as the global log level. This way,
 * users can specify a specific level for the C++ binding that is separate from the LRC if
 * they wish, but if they don't, the C++ binding will fall back on the LRC level.
 */
void Runtime::processRid() throw( HLA::RTIinternalError )
{
	logger->info( "Attempting to load RID file" );

	///////////////////////////////////
	// use Java to load the RID file //
	///////////////////////////////////
	// find the proxy ambassador class
	jclass proxyClass = jvmenv->FindClass( "org/portico/impl/cpp13/ProxyRtiAmbassador" );
	if( proxyClass == NULL )
	{
		logger->fatal( "Can't locate the org.portico.impl.cpp13.ProxyRtiAmbassador class" );
		throw HLA::RTIinternalError("Can't locate org.portico.impl.cpp13.ProxyRtiAmbassador class");
	}

	// find the parseCppRid method
	jmethodID loaderMethod = jvmenv->GetStaticMethodID( proxyClass, "parseCppRid", "()Z" );
	if( loaderMethod == NULL )
	{
		logger->fatal( "Can't locate the ProxyRtiAmbassador.parseCppRid() method" );
		throw HLA::RTIinternalError( "Can't locate the ProxyRtiAmbassador.parseCppRid() method" );
	}

	// call the method
	jvmenv->CallStaticBooleanMethod( proxyClass, loaderMethod );

	//////////////////////////////////////////////////////////////////
	// check the environment for the variables we are interested in //
	//////////////////////////////////////////////////////////////////
	jclass systemClass = jvmenv->FindClass( "java/lang/System" );
	jmethodID propertyMethod = jvmenv->GetStaticMethodID( systemClass,
	                               "getProperty", "(Ljava/lang/String;)Ljava/lang/String;" );

	// is there a specific C++ log level set?
	jstring temp = jvmenv->NewStringUTF( "portico.c++.loglevel" );
	jstring value = (jstring)jvmenv->CallStaticObjectMethod( systemClass, propertyMethod, temp );
	jvmenv->DeleteLocalRef( temp ); // clean up the string we created
	if( value == NULL )
	{
		// there was no specific C++ level, check for a default LRC level
		temp = NULL;
		temp = jvmenv->NewStringUTF( "portico.loglevel" );
		value = (jstring)jvmenv->CallStaticObjectMethod( systemClass, propertyMethod, temp );
		jvmenv->DeleteLocalRef( temp );
	}

	// do we have a level? (be it from the c++ or lrc property)
	if( value != NULL )
	{
		// set the level on the logger
		char *loglevel = convertAndReleaseJString( value );
		logger->info( "Log Level from RID: %s", loglevel );
		Logger::setGlobalLevel( loglevel );
		delete[] loglevel;
	}

	logger->info( "RID file processing finished" );
}

char* Runtime::convertAndReleaseJString( jstring string )
{
	// this is just a copy from JavaRTI. These should really be somewhere that can be used by all
	const char *javaString = jvmenv->GetStringUTFChars( string, NULL );
	char *userString = new char[strlen(javaString)+1];
	strcpy( userString, javaString );

	// release the java resources
	jvmenv->ReleaseStringUTFChars( string, javaString );
	jvmenv->DeleteLocalRef( string );

	// return the userspace string
	return userString;
}

//----------------------------------------------------------
//                     STATIC METHODS
//----------------------------------------------------------
Runtime* Runtime::getRuntime() throw( HLA::RTIinternalError )
{
	// if we have already created the instance, return it
	if( Runtime::instance != NULL )
		return Runtime::instance;

	// the instance doesn't exist yet, create and return it
	Runtime::instance = new Runtime();

	return Runtime::instance;
}

void Runtime::shutdown()
{
	// delete the runtime (causing the destructor to run)
	delete Runtime::instance;

	// reset the instance, this will allow the runtime to be restarted
	Runtime::instance = NULL;
}

PORTICO13_NS_END

