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
#include <fstream>

PORTICO1516E_NS_START

// initialize our singleton - we'll lazy load it
Runtime* Runtime::instance = NULL;
jclass Runtime::JCLASS_BYTE_ARRAY   = 0;
jclass Runtime::JCLASS_STRING_ARRAY = 0;
jclass Runtime::JCLASS_STRING       = 0;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
Runtime::Runtime() throw( RTIinternalError )
{
	// Check for the presence of the PORTICO_DEBUG environment variable.
	// If it exists, set the global level of the Logger to the identified level.
	char *value = getenv( "PORTICO_DEBUG" );
	if( value != NULL )
		Logger::setGlobalLevel( value );

	value = getenv( "PORTICO_REDIRECT" );
	if( value != NULL )
		Logger::setRedirect( value );

	// get us a logger to work with
	this->logger = new Logger( "c++" );
	this->logger->setPrefix( "(Runtime) " );
	
	// initialize the JNI pieces
	this->jvm = NULL;
	this->attachedToExisting = false;
	this->idCounter = 0;
	this->activeRtis = new std::map<int,JavaRTI*>();

	// fire up the JVM and process the RID file
	this->initializeJVM();
	this->cacheGlobalHandles();
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

	// create the instance
	JavaRTI *newRTI = new JavaRTI( this, ++idCounter );

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


void Runtime::setSystemProperty( const char *keyAndValue )
{
	// copy the string
	// worst piece of crap ever...
	char *key = new char[strlen(keyAndValue)+1];
	strcpy( key, keyAndValue );

	const char *value = strchr( key,'=' ) + 1;
	key[value-key-1] = '\0';     // terminate the string before "="
	setSystemProperty(key+2, value); // key+2 to remove "-D"
	delete [] key;
}

void Runtime::setSystemProperty( const char* key, const char* value )
{

	// Get active environment
	JNIEnv* jnienv = attachToJVM();

	jclass clazz = jnienv->FindClass( "java/lang/System" );
	jmethodID method = jnienv->GetStaticMethodID( clazz, "setProperty",
	                       "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
	jnienv->CallStaticObjectMethod( clazz,
	                                method,
	                                jnienv->NewStringUTF(key),
	                                jnienv->NewStringUTF(value) );
}


/////////////////////////////////////////////////////////////////
//////////////////////// Private Methods ////////////////////////
/////////////////////////////////////////////////////////////////
void Runtime::initializeJVM() throw( RTIinternalError )
{
	logger->debug( "Initialize or create a new JVM" );
	JNIEnv* jnienv;

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
	string stackSize( "-Xms8m" );
	string mode = getMode();
	string compiler = getCompiler();
	string hlaVersion = getHlaVersion();
	string architecture = getArch();
	string ipv4( "-Djava.net.preferIPv4Stack=true" );

	// before we can create or connect to the JVM, we need to specify its environment
	JavaVMInitArgs vmargs;
	JavaVMOption options[8];
	options[0].optionString = const_cast<char*>(paths.first.c_str());
	options[1].optionString = const_cast<char*>(paths.second.c_str());
	options[2].optionString = const_cast<char*>(mode.c_str());         // build mode
	options[3].optionString = const_cast<char*>(compiler.c_str());     // compiler version
	options[4].optionString = const_cast<char*>(hlaVersion.c_str());   // hla interface version
	options[5].optionString = const_cast<char*>(architecture.c_str()); // architecture
	options[6].optionString = const_cast<char*>(stackSize.c_str());
	options[7].optionString = const_cast<char*>(ipv4.c_str());
	vmargs.nOptions = 8;
	vmargs.version = getJNIVersion();
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

		// Patch from JPL for MatLab
		// Since we are attaching to an existing JVM, we have to set the system
		// properties so that when we try to use them to load back the library for
		// callbacks, we load by the right name. If we had created the JVM we would
		// have passed these as command line arguments as above.
		setSystemProperty( const_cast<char*>(mode.c_str()) );         // build mode
		setSystemProperty( const_cast<char*>(compiler.c_str()) );     // compiler version
		setSystemProperty( const_cast<char*>(hlaVersion.c_str()) );   // hla interface version
		setSystemProperty( const_cast<char*>(architecture.c_str()) ); // architecture

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
	else
	{
		// check to make sure it is set to a valid location
		if( pathExists(string(rtihome)) == false )
		{
			logger->fatal( "RTI_HOME doesn't exist: this is *REQUIRED* to point to your Portico directory" );
			logger->fatal( "RTI_HOME set to [%s]", rtihome );
			throw RTIinternalError( L"RTI_HOME set to directory that doesn't exist" );
		}
	}

	// Get the class and library paths depending on the platform in use
	#ifdef OS_WINDOWS
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
pair<string,string> Runtime::generateWinPath( string rtihome ) throw( RTIinternalError )
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
	          << rtihome << "\\lib\\portico.jar;"    // %RTI_HOME%\lib\portico.jar
	          << string(systemClasspath);            // system classpath
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
	#ifdef OS_MACOSX
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
	          << jrelocation << "/lib/server:"
	          << jrelocation << "/lib/i386/client:"
	          << jrelocation << "/lib/amd64/server";
	paths.second = libraryPath.str();
	
	return paths;
}

/*
 * Return "-Dportico.cpp.mode=" debug or release
 */
string Runtime::getMode() throw( RTIinternalError )
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
string Runtime::getCompiler() throw( RTIinternalError )
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
string Runtime::getHlaVersion() throw( RTIinternalError )
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
string Runtime::getArch() throw( RTIinternalError )
{
#ifdef ARCH_X86
	return string( "-Dportico.cpp.arch=x86" );
#else
	return string( "-Dportico.cpp.arch=amd64" );
#endif
}

/*
 * Return "-Dportico.cpp.arch=" x86 or amd64
 */
jint Runtime::getJNIVersion()
{
	return JNI_VERSION_1_6;
}

/*
 * We use various JNI class references throughout the life of the VM. The role of this
 * method is to cache them in statics so that they're easily accessible to all.
 */
void Runtime::cacheGlobalHandles() throw( RTIinternalError )
{
	// Get active environment
	JNIEnv* jnienv = attachToJVM();

	jclass byteArray = jnienv->FindClass( "[B" );
	Runtime::JCLASS_BYTE_ARRAY = (jclass)jnienv->NewGlobalRef( byteArray );
	jnienv->DeleteLocalRef( byteArray ); // now that we have the global ref, we don't need this
	if( Runtime::JCLASS_BYTE_ARRAY == NULL )
	{
		throw RTIinternalError( L"RTI initialization error: Failed while caching byte[] JNI reference" );
	}
	
	jclass stringArray = jnienv->FindClass( "[Ljava/lang/String;" );
	Runtime::JCLASS_STRING_ARRAY = (jclass)jnienv->NewGlobalRef( stringArray );
	jnienv->DeleteLocalRef( stringArray );
	if( Runtime::JCLASS_STRING_ARRAY == NULL )
	{
		throw RTIinternalError( L"RTI initialization error: Failed while caching String[] JNI reference" );
	}
	
	jclass stringClass = jnienv->FindClass( "java/lang/String" );
	Runtime::JCLASS_STRING = (jclass)jnienv->NewGlobalRef( stringClass );
	jnienv->DeleteLocalRef( stringClass );
	if( Runtime::JCLASS_STRING == NULL )
	{
		throw RTIinternalError( L"RTI initialization error: Failed while caching java.lang.String JNI reference" );
	}
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

	// Get active environment
	JNIEnv* jnienv = attachToJVM();

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
JNIEnv* Runtime::attachToJVM() throw( RTIinternalError )
{
	JNIEnv* jnienv = NULL;

	int status = jvm->GetEnv((void **)&jnienv, getJNIVersion());

	if(jnienv == NULL)
	{
		logger->trace( "Attaching current thread to JVM" );
		jint result = jvm->AttachCurrentThread( (void**)&jnienv, NULL );
		if( result != 0 )
		{
			logger->fatal( "Couldn't attach current thread to JVM" );
			throw RTIinternalError( L"Couldn't thread attach to JVM" );
		}
	}

	return jnienv;
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
