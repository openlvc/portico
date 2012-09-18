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
#include "jni/Runtime.h"
#include "jni/JniUtils.h"

PORTICO1516E_NS_START

// initialize our singleton - we'll lazy load it
Runtime* Runtime::instance = NULL;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
Runtime::Runtime() throw( RTIinternalError )
{
	// Check for the presence of the PORTICO_DEBUG environment variable.
	// If it exists, set the global level of the Logger to the identified level.
	char *value = getenv( "PORTICO_DEBUG" );
	if( value != NULL )
	{
		Logger::setGlobalLevel( value );
	}
	else
	{
		Logger::setGlobalLevel( "NOISY" ); // just for debugging right now
	}

	// get us a logger to work with
	this->logger = new Logger( "c++" );
	this->logger->setPrefix( "(Runtime) " );
	
	// initialize the JNI pieces
	this->jvm = NULL;
	this->jnienv = NULL;
	this->attachedToExisting = false;
	this->idCounter = 0;
	this->activeRtis = new std::map<int,JavaRTI*>();

	// fire up the JVM and process the RID file
	this->initializeJVM();
	this->processRid();
}

Runtime::~Runtime()
{
	this->logger->info( "Shutting down the Runtime" );
	
	// loop through each of the active RTIs and kill them
	delete this->activeRtis; // should call ~JavaRTI()

	// detach from the JVM and destroy it
	if( this->attachedToExisting )
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
	
	delete this->logger;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
/**
 * Create a new JavaRTI and store it in the list of active RTIs for later destruction
 */
JavaRTI* Runtime::newRtiAmbassador() throw( RTIinternalError )
{
	logger->debug( "Attempting to create new JavaRTI" );

	// attach to the JVM (this could be a new thread that we have to register)
	attachToJVM();
	
	// create the instance
	JavaRTI *newRTI = new JavaRTI( jnienv, ++idCounter );

	// store it in the map of active instances
	int id = newRTI->getId();
	(*activeRtis)[id] = newRTI;

	logger->info( "Created new JavaRTI [id:%d]", id );
	return newRTI;
}

/**
 * @return the active RTI with the given ID, or NULL if there is no active RTI with that ID
 */
JavaRTI* Runtime::getRtiAmbassador( int id )
{
	// see if the map contains the value first
	if( activeRtis->find(id) == activeRtis->end() )
		return NULL;

	// if does exist, return it
	return (*activeRtis)[id];
}

/**
 * Remove and destroy the active RTI with the specified ID
 */
void Runtime::removeRtiAmbassador( int id )
{
	logger->info( "Removing removing JavaRTI reference [id:%d]", id );

	// remove the instance from the list of active RTIs
	activeRtis->erase( id );
}

void Runtime::removeRtiAmbassador( JavaRTI* javarti )
{
	this->removeRtiAmbassador( javarti->getId() );
}

/////////////////////////////////////////////////////////////////
//////////////////////// Private Methods ////////////////////////
/////////////////////////////////////////////////////////////////
void Runtime::initializeJVM() throw( RTIinternalError )
{
	logger->debug( "Initialize or create a new JVM" );

	///////////////////////////////////////////
	// 1. get the classpath and library path //
	///////////////////////////////////////////
	pair<string,string> paths = generatePaths();
	logger->debug( "Using Classpath   : %s", paths.first.c_str() );
	logger->debug( "Using Library Path: %s", paths.second.c_str() );

	/////////////////////////////////////////////
	// 2. check to see if a JVM already exists //
	/////////////////////////////////////////////
	// other jvm options - remember to increment the option array size
	// if you are going to add more
	string stackSize( "-Xss8m" );
	
	// before we can create or connect to the JVM, we need to specify its environment
	JavaVMInitArgs vmargs;
	JavaVMOption options[3];
	options[0].optionString = const_cast<char*>(paths.first.c_str());
	options[1].optionString = const_cast<char*>(paths.second.c_str());
	options[2].optionString = const_cast<char*>(stackSize.c_str());
	vmargs.nOptions = 3;
	vmargs.version = JNI_VERSION_1_6;
	vmargs.options = options;
	vmargs.ignoreUnrecognized = JNI_TRUE;

	// Before we create the JVM, we will check to see if one already exists or
	// not. If there is an existing one, we will just attach to it rather than
	// creating a separate one.
	jint result;
	jsize jvmCount = 0;
	result = JNI_GetCreatedJavaVMs( &jvm, 1, &jvmCount );
	if( this->jvm != NULL && jvmCount > 0 && result == JNI_OK )
	{
		///////////////////////////////////////////////////////////////
		// JVM already exists, just attach to the existing reference //
		///////////////////////////////////////////////////////////////
		logger->debug( "[check] JVM already exists, attaching to it" );
		result = jvm->AttachCurrentThread( (void**)&jnienv, &vmargs );
		// check the result
		if( result < 0 )
		{
			logger->fatal( "*** JVM already existed, but we failed to attach ***" );
			logger->fatal( "    result=%d", result );
			throw RTIinternalError( L"*** JVM already existed, but we failed to attach ***" );
		}

		// we're all attached just fine, so let's get out of here
		this->attachedToExisting = true;
		return;
	}

	//////////////////////////////////
	// 3. create a new JVM instance //
	//////////////////////////////////
	// JVM doesn't exist yet, create a new one to work with
	logger->debug( "[check] JVM doesn't exist, creating a new one" );
	result = JNI_CreateJavaVM( &jvm, (void**)&jnienv, &vmargs );

	if( result < 0 )
	{
		logger->fatal( "*** Couldn't create a new JVM! *** result=%d", result );
		throw RTIinternalError( L"*** Couldn't create a new JVM! ***" );
	}

	logger->info( "New JVM has been created" );
}

/**
 * Depending on the operating system, kicks off the generation of the class
 * and library paths. Returns these as a pair.
 * 
 * @return The classpath and library path to use when starting the JVM
 */
pair<string,string> Runtime::generatePaths() throw( RTIinternalError )
{	
	// Check for the presence of RTI_HOME
	// RTI_HOME *has* to be set. No two ways about it. Fail out if this isn't the case.
	// We make all inferences about path locations based off it, give it to us!
	char *rtihome = getenv( "RTI_HOME" );
	if( !rtihome )
	{
		logger->fatal( "RTI_HOME not set: this is *REQUIRED* to point to your Portico directory" );
		throw RTIinternalError( L"RTI_HOME not set: this *must* point to your Portico directory" );
	}

	// Get the class and library paths depending on the platform in use
	#ifdef WIN32
		return generateWin32Path( string(rtihome) );
	#else
		return generateUnixPath( string(rtihome) );
	#endif
}

/**
 * Generate and return the classpath and system path to use when loading the RTI on
 * *nix based systems. This method will construct two strings with the following:
 * 
 * (Classpath)
 *   * System classpath
 *   * $RTI_HOME\lib\portico.jar
 *   
 * (Library Path)
 *   * System path
 *   * $RTI_HOME\bin
 *   * $JAVA_HOME\jre\lib\i386\client  (32-bit JRE)
 *   * $JAVA_HOME\jre\lib\amd64\server (64-bit JRE)
 * 
 * If JAVA_HOME isn't set on the computer, RTI_HOME is used to link in with any JRE
 * that Portico has shipped with.  
 */
pair<string,string> Runtime::generateWin32Path( string rtihome ) throw( RTIinternalError )
{
	pair<string,string> paths;

	//////////////////////////////////
	// 1. Set up the Java Classpath //
	//////////////////////////////////
	// pick up the system classpath and put $RTI_HOME/lib/portico.jar on the end
	const char *systemClasspath = getenv( "CLASSPATH" );
	if( !systemClasspath )
	{
		logger->debug( "CLASSPATH not set, using ." );
		systemClasspath = "./";
	}

	// create out classpath
	stringstream classpath;
	classpath << "-Djava.class.path=.;"
	          << string(systemClasspath) << ";"
	          << rtihome << "\\lib\\portico.jar";
	paths.first = classpath.str();

	////////////////////////////////
	// 2. Set up the library path //
	////////////////////////////////
	// Get the system path
	const char *systemPath = getenv( "PATH" );
	if( !systemPath )
		systemPath = "";
	
	// Get JAVA_HOME
	// fall back to use RTI_HOME if not set
	const char *javahome = getenv( "JAVA_HOME" );
	if( !javahome )
	{
		javahome = rtihome.c_str();
		logger->warn( "WARNING Environment variable JAVA_HOME not set, assuming it is: %s\\jre",
		              rtihome.c_str() );
	}
	else
	{
		logger->debug( "JAVA_HOME set to %s", javahome );
	}

	// Create our system path
	stringstream libraryPath;
	libraryPath << "-Djava.library.path=.;"
	          << string(systemPath) << ";"
	          << rtihome << "\\bin;"
	          << string(javahome) << "\\jre\\lib\\i386\\client;"
	          << string(javahome) << "\\jre\\lib\\amd64\\server";
	paths.second = libraryPath.str();

	return paths;
}

/**
 * Generate and return the classpath and system path to use when loading the RTI on
 * *nix based systems. This method will construct two strings with the following:
 * 
 * (Classpath)
 *   * System classpath
 *   * $RTI_HOME/lib/portico.jar
 *   
 * (Library Path)
 *   * System path
 *   * $RTI_HOME/lib
 *   * $JAVA_HOME/jre/lib/server       (Mac OS X 64-bit)
 *   * $JAVA_HOME/jre/lib/i386/client  (Win/Linux 32-bit)
 *   * $JAVA_HOME/jre/lib/amd64/server (Win/Linux 64-bit)
 * 
 * If JAVA_HOME isn't set on the computer, RTI_HOME is used to link in with any JRE
 * that Portico has shipped with.  
 */
pair<string,string> Runtime::generateUnixPath( string rtihome ) throw( RTIinternalError )
{
	pair<string,string> paths;

	//////////////////////////////////
	// 1. Set up the Java Classpath //
	//////////////////////////////////
	// pick up the system classpath and put $RTI_HOME/lib/portico.jar on the end
	const char *systemClasspath = getenv( "CLASSPATH" );
	if( !systemClasspath )
	{
		logger->debug( "CLASSPATH not set, using ." );
		systemClasspath = "./";
	}

	// create out classpath
	stringstream classpath;
	classpath << "-Djava.class.path=.:"
	          << string(systemClasspath) << ":"
	          << rtihome << "/lib/portico.jar";
	paths.first = classpath.str();

	////////////////////////////////
	// 2. Set up the library path //
	////////////////////////////////
	// Get the system path
	#ifdef __APPLE__
	const char *systemPath = getenv( "DYLD_LIBRARY_PATH" );
	#else
	char *systemPath = getenv( "LD_LIBRARY_PATH" );
	#endif

	// make sure we have a system path
	if( !systemPath )
		systemPath = "";
	
	// Get JAVA_HOME
	// fall back to use RTI_HOME if not set
	const char *javahome = getenv( "JAVA_HOME" );
	if( !javahome )
	{
		javahome = rtihome.c_str();
		logger->warn( "WARNING Environment variable JAVA_HOME not set, assuming it is: %s/jre",
		              rtihome.c_str() );
	}
	else
	{
		logger->debug( "JAVA_HOME set to %s", javahome );
	}

	// Create our system path
	stringstream libraryPath;
	libraryPath << "-Djava.library.path=.:"
	          << string(systemPath) << ":"
	          << rtihome << "/lib:"
	          << string(javahome) << "/jre/lib/server:"
	          << string(javahome) << "/jre/lib/i386/client:"
	          << string(javahome) << "/jre/lib/amd64/server";
	paths.second = libraryPath.str();
	
	return paths;
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
void Runtime::processRid() throw( RTIinternalError )
{
	logger->info( "Attempting to load RID file" );

	///////////////////////////////////
	// use Java to load the RID file //
	///////////////////////////////////
	// Find the proxy ambassador class 
	jclass proxyClass = jnienv->FindClass( "org/portico/impl/cpp1516e/ProxyRtiAmbassador" );
	if( proxyClass == NULL )
	{
		logger->fatal( "Can't locate the org.portico.impl.cpp13.ProxyRtiAmbassador class" );
		throw RTIinternalError(L"Can't locate org.portico.impl.cpp1516e.ProxyRtiAmbassador class");
	}

	// find the parseCppRid method
	jmethodID loaderMethod = jnienv->GetStaticMethodID( proxyClass, "parseCppRid", "()Z" );
	if( loaderMethod == NULL )
	{
		logger->fatal( "Can't locate the ProxyRtiAmbassador.parseCppRid() method" );
		throw RTIinternalError( L"Can't locate the ProxyRtiAmbassador.parseCppRid() method" );
	}

	// call the method
	jnienv->CallStaticBooleanMethod( proxyClass, loaderMethod );

	//////////////////////////////////////////////////////////////////
	// check the environment for the variables we are interested in //
	//////////////////////////////////////////////////////////////////
	jclass systemClass = jnienv->FindClass( "java/lang/System" );
	jmethodID propertyMethod = jnienv->GetStaticMethodID( systemClass,
	                               "getProperty", "(Ljava/lang/String;)Ljava/lang/String;" );

	// is there a specific C++ log level set?
	jstring temp = jnienv->NewStringUTF( "portico.c++.loglevel" );
	jstring value = (jstring)jnienv->CallStaticObjectMethod( systemClass, propertyMethod, temp );
	jnienv->DeleteLocalRef( temp ); // clean up the string we created
	if( value == NULL )
	{
		// there was no specific C++ level, check for a default LRC level
		temp = NULL;
		temp = jnienv->NewStringUTF( "portico.loglevel" );
		value = (jstring)jnienv->CallStaticObjectMethod( systemClass, propertyMethod, temp );
		jnienv->DeleteLocalRef( temp );
	}

	// do we have a level? (be it from the c++ or lrc property)
	if( value != NULL )
	{
		// set the level on the logger
		string loglevel = JniUtils::toStringAndRelease( jnienv, value );
		logger->info( "Log Level from RID: %s", loglevel.c_str() );
		Logger::setGlobalLevel( loglevel );
	}

	logger->info( "RID file processing finished" );
}

/*
 * This method will attach the current JavaRTI to the JVM, storing a reference
 * to the JNIEnv in the instance variable. If there is an error during this
 * process, an RTIinternalError will be thrown.
 */
void Runtime::attachToJVM() throw( RTIinternalError )
{
	logger->trace( "Attaching current thread to JVM" );
	JavaVMInitArgs vmArgs;
	JNI_GetDefaultJavaVMInitArgs( &vmArgs );
	jint result = jvm->AttachCurrentThread( (void**)&jnienv, &vmArgs );
	if( result == 0 )
	{
		logger->debug( "Attached current thread to JVM" );
	}
	else
	{
		logger->fatal( "Couldn't attach current thread to JVM" );
		throw RTIinternalError( L"Couldn't thread attach to JVM" );
	}
}

/*
 * This method will call DetachCurrentThread in an effort to remove any thread
 * associated with this JavaRTI from the JVM. This should only ever be called
 * as the JavaRTI is being destructed.
 */
void Runtime::detachFromJVM()
{
	logger->trace( "Detaching current thread from JVM" );
	jint result = Runtime::getRuntime()->jvm->DetachCurrentThread();
	if( result == 0 )
		logger->debug( "Detached curren thread from JVM" );
	else
		logger->fatal( "Couldn't detach current thread from JVM" );
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------
/**
 * Return the singleton runtime, lazy loading it if we haven't already fired one up.
 */
Runtime* Runtime::getRuntime() throw( RTIinternalError )
{
	// if the instance hasn't been created yet, do so
	if( Runtime::instance == NULL )
		Runtime::instance = new Runtime();
	
	return Runtime::instance;
}

/**
 * Unwind all out connections to executing stuff and kill the whole JVM
 */
void Runtime::shutdown()
{
	// delete the runtime (causing the destructor to run)
	delete Runtime::instance;

	// reset the instance, this will allow the runtime to be restarted
	Runtime::instance = NULL;
}

PORTICO1516E_NS_END
