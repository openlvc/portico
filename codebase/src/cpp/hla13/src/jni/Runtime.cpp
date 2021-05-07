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
#include <fstream>

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
	JavaRTI *newRTI = new JavaRTI( jvm );

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
	pair<string,string> paths = this->generatePaths();
	logger->debug( "Using Classpath   : %s", paths.first.c_str() );
	logger->debug( "Using Library Path: %s", paths.second.c_str() );

	/////////////////////////////////////////////
	// 2. check to see if a JVM already exists //
	/////////////////////////////////////////////
	// other jvm options - remember to increment the option array size
	// if you are going to add more
	string stackSize( "-Xss8m" );
	string mode = getMode();
	string compiler = getCompiler();
	string hlaVersion = getHlaVersion();
	string architecture = getArch();
	
	// before we can create or connect to the JVM, we need to specify its environment
	JavaVMInitArgs vmargs;
	JavaVMOption options[7];
	options[0].optionString = const_cast<char*>(paths.first.c_str());
	options[1].optionString = const_cast<char*>(paths.second.c_str());
	options[2].optionString = const_cast<char*>(mode.c_str());         // build mode
	options[3].optionString = const_cast<char*>(compiler.c_str());     // compiler version
	options[4].optionString = const_cast<char*>(hlaVersion.c_str());   // hla interface version
	options[5].optionString = const_cast<char*>(architecture.c_str()); // architecture
	options[6].optionString = const_cast<char*>(stackSize.c_str());
	vmargs.nOptions = 7;
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
		result = jvm->AttachCurrentThread( (void**)&jvmenv, &vmargs );
		// check the result
		if( result < 0 )
		{
			logger->fatal( "*** JVM already existed, but we failed to attach ***" );
			logger->fatal( "    result=%d", result );
			throw HLA::RTIinternalError( "*** JVM already existed, but we failed to attach ***" );
		}

		// we're all attached just fine, so let's get out of here
		this->attached = true;
		return;
	}

	//////////////////////////////////
	// 3. create a new JVM instance //
	//////////////////////////////////
	// JVM doesn't exist yet, create a new one to work with
	logger->debug( "[check] JVM doesn't exist, creating a new one" );
	result = JNI_CreateJavaVM( &jvm, (void**)&jvmenv, &vmargs );

	if( result < 0 )
	{
		logger->fatal( "*** Couldn't create a new JVM! *** result=%d", result );
		throw HLA::RTIinternalError( "*** Couldn't create a new JVM! ***" );
	}

	logger->info( "New JVM has been created" );
}

/**
 * Depending on the operating system, kicks off the generation of the class
 * and library paths. Returns these as a pair.
 * 
 * @return The classpath and library path to use when starting the JVM
 */
pair<string,string> Runtime::generatePaths() throw( HLA::RTIinternalError )
{
	// Check for the presence of RTI_HOME
	// RTI_HOME *has* to be set. No two ways about it. Fail out if this isn't the case.
	// We make all inferences about path locations based off it, give it to us!
	char *rtihome = getenv( "RTI_HOME" );
	if( !rtihome )
	{
		logger->fatal( "RTI_HOME not set: this is *REQUIRED* to point to your Portico directory" );
		throw HLA::RTIinternalError( "RTI_HOME not set: this *must* point to your Portico directory" );
	}
	else
	{
		// check to make sure it is set to a valid location
		if( pathExists(string(rtihome)) == false )
		{
			logger->fatal( "RTI_HOME doesn't exist: this is *REQUIRED* to point to your Portico directory" );
			logger->fatal( "RTI_HOME set to [%s]", rtihome );
			throw HLA::RTIinternalError( "RTI_HOME set to directory that doesn't exist" );
		}
	}

	// Get the class and library paths depending on the platform in use
	#ifdef _WIN32
		return generateWinPath( string(rtihome) );
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
pair<string,string> Runtime::generateWinPath( string rtihome ) throw( HLA::RTIinternalError )
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
		systemClasspath = ".";
	}

	// create out classpath
	stringstream classpath;
	classpath << "-Djava.class.path=.;"
	          << string(systemClasspath) << ";"      // system classpath
	          << rtihome << "\\lib\\portico.jar;";   // %RTI_HOME%\lib\portico.jar
	paths.first = classpath.str();

	////////////////////////////////
	// 2. Set up the library path //
	////////////////////////////////
	// For the RTI to operate properly, the following must be on the path used to star the JVM:
	//  * DLLs for the Portico C++ interface
	//  * DLLs for the JVM
	
	// Set to JAVA_HOME as a fallback -- only used when we're in development environments really.
	// Any distribution should have a bundled JRE
	char *javaHome = getenv( "JAVA_HOME" );
	string jrelocation;
	if( javaHome )
		string jrelocation( javaHome );
	
	// Portico ships a JRE with it, but we might be building in a development environment
	// so check to see if RTI_HOME/jre is packaged first, then fallback on JAVA_HOME from above
	string temp = string(rtihome).append( "\\jre\\bin\\java.exe" );
	if( pathExists(temp) )
	{
		jrelocation = string(rtihome).append("\\jre");
		logger->debug( "Found bundled JRE in [%s]", jrelocation.c_str() );
	}
	else
	{
		logger->warn( "WARNING Could not locate bundled JRE, falling back on %JAVA_HOME%: [%s]",
		              jrelocation.c_str() );
	}

	// Get the system path so we can ensure it is on our library path
	const char *systemPath = getenv( "PATH" );
	if( !systemPath )
		systemPath = "";

	// Create our system path
	stringstream libraryPath;
	libraryPath << "-Djava.library.path=.;"
	            << string(systemPath) << ";"
	            << rtihome << "\\bin\\"
#ifdef VC11
	            << "vc11"
#endif
#ifdef VC10
	            << "vc10"
#endif
#ifdef VC9
	            << "vc9"
#endif
#ifdef VC8
	            << "vc8"
#endif

#ifdef _WIN32
	            << jrelocation << "\\lib\\i386\\client";
#else
	            << jrelocation << "\\lib\\amd64\\server";
#endif	

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
pair<string,string> Runtime::generateUnixPath( string rtihome ) throw( HLA::RTIinternalError )
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

	// Set to JAVA_HOME as a fallback -- only used when we're in development environments really.
	// Any distribution should have a bundled JRE
	char *javaHome = getenv( "JAVA_HOME" );
	string jrelocation;
	if( javaHome )
		string jrelocation( javaHome );
	
	// Portico ships a JRE with it, but we might be building in a development environment
	// so check to see if RTI_HOME/jre is packaged first, then fallback on JAVA_HOME from above
	string temp = string(rtihome).append( "/jre/bin/java" );
	if( pathExists(temp) )
	{
		jrelocation = string(rtihome).append("/jre");
		logger->debug( "Found bundled JRE in [%s]", jrelocation.c_str() );
	}
	else
	{
		logger->warn( "WARNING Could not locate bundled JRE, falling back on $JAVA_HOME: [%s]",
		              jrelocation.c_str() );
	}

	// Create our system path
	stringstream libraryPath;
	libraryPath << "-Djava.library.path=.:"
	          << string(systemPath) << ":"
	          << rtihome << "/lib/gcc4:"
	          << jrelocation << "/jre/lib/server:"
	          << jrelocation << "/jre/lib/i386/client:"
	          << jrelocation << "/jre/lib/amd64/server";
	paths.second = libraryPath.str();
	
	return paths;
}

/*
 * Return "-Dportico.cpp.mode=" debug or release
 */
string Runtime::getMode() throw( HLA::RTIinternalError )
{
#ifdef DEBUG
	return string("-Dportico.cpp.mode=debug");
#else
	return string("-Dportico.cpp.mode=release");
#endif
}

/*
 * Return "-Dportico.cpp.compiler=" vc8, vc9, vc10, vc11, gcc4, ...
 */
string Runtime::getCompiler() throw( HLA::RTIinternalError )
{
#ifdef VC11
	return string( "-Dportico.cpp.compiler=vc11" );
#elif defined(VC10)
	return string( "-Dportico.cpp.compiler=vc10" );
#elif defined(VC9)
	return string( "-Dportico.cpp.compiler=vc9" );
#elif defined(VC8)
	return string( "-Dportico.cpp.compiler=vc8" );
#else
	return string( "-Dportico.cpp.compiler=gcc4" );
#endif
}

/*
 * Return "-Dportico.cpp.hlaversion=" hla13, dlc13, ieee1516, ieee1516e, ...
 */
string Runtime::getHlaVersion() throw( HLA::RTIinternalError )
{
#ifdef BUILDING_DLC
	return string( "-Dportico.cpp.hlaversion=dlc13" );
#else
	return string( "-Dportico.cpp.hlaversion=hla13" );
#endif
}

/*
 * Return "-Dportico.cpp.arch=" x86 or amd64
 */
string Runtime::getArch() throw( HLA::RTIinternalError )
{
#ifdef ARCH_X86
	return string( "-Dportico.cpp.arch=x86" );
#else
	return string( "-Dportico.cpp.arch=amd64" );
#endif
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

/*
 * Checks to see if a file exists, and if it does, returns true. Returns false otherwise
 */
bool Runtime::pathExists( string path )
{
#ifdef _WIN32
	// if we're in windows, ifstream won't be happy if the
	// path is a directory (booo) so we'll use something else
	DWORD atts = GetFileAttributesA( path.c_str() );
	if( atts == INVALID_FILE_ATTRIBUTES )
		return false;  //something is wrong with your path!
	else
		return true;
	//if (ftyp & FILE_ATTRIBUTE_DIRECTORY)
	//return true;   // this is a directory!
#else
	// just try and open it - the object will be collected
	ifstream thefile( path.c_str() );
	return (bool)thefile;
#endif
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

