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
#ifndef RUNTIME_H_
#define RUNTIME_H_

#include "common.h"
#include "jni/JavaRTI.h"
#include "utils/Logger.h"

PORTICO1516E_NS_START

/*
 * The process of setting up and wiring into the JVM is a complicated one
 * that requires obtaining a bunch of references to classes and methods on
 * the Java side. This is the sort of thing we only want to do once, no
 * matter how many actual RTI instances we run. We load the environment, and
 * then into that we load individual RTIambassadors (which use the links
 * we've put in place here).
 */
class Runtime
{
	// give JavaRTI access to our bits
	friend class JavaRTI;

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:
		static Runtime* instance;
	
	public:
		static jclass JCLASS_BYTE_ARRAY;
		static jclass JCLASS_STRING_ARRAY;
		static jclass JCLASS_STRING;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		Logger *logger;
		
		// JNI-related stuff
		JNIEnv *jnienv;
		JavaVM *jvm;
		bool attachedToExisting;
		
		// RTI-instance tracking
		int idCounter; // to uniquely name each RTI
		std::map<int,JavaRTI*> *activeRtis;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private:
		Runtime() throw( RTIinternalError );

	public:
		~Runtime();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		/**
		 * Create a new RtiAmbassador and link it in with the Java side of the
		 * framework, recording it locally so we can manage multiple instances
		 * inside a single runtime.
		 */
		JavaRTI* newRtiAmbassador() throw( RTIinternalError );
		JavaRTI* getRtiAmbassador( int id );
		void     removeRtiAmbassador( int id );
		void     removeRtiAmbassador( JavaRTI* javarti );
		void 	 setSystemProperty( const char* key, const char* value );
		void 	 setSystemProperty( const char *keyAndValue );

	private:
		void initializeJVM() throw( RTIinternalError );
		void cacheGlobalHandles() throw( RTIinternalError );
		void attachToJVM() throw( RTIinternalError );
		void detachFromJVM(); /* should not be called currently */

		/**
		 * So the Java library can know what library name to use for load back we 
		 * have to tell it the compiler version, HLA interface version and architecture
		 * we are using. This helps it assemble the appropriate library names it needs
		 * to load reference back into the C++ library.
		 * 
		 * These are passed into the JVM as system properties on startup
		 */
		string getMode() throw( RTIinternalError );         // debug or release
		string getCompiler() throw( RTIinternalError );     // vc8, vc9, vc10, gcc4, ...
		string getHlaVersion() throw( RTIinternalError );   // hla13, dlc13, ieee1516e, ...
		string getArch() throw( RTIinternalError );         // x86, amd64

		/**
		 * These methods generate a pair of strings which are the paths that should be
		 * used to start the JVM. The first is the classpath that should be used, the
		 * second is the library path (so the Java side can load the C++ side back).
		 * 
		 * Both the windows and unix version work in much the same way. For the classpath,
		 * the system classpath is first loaded, with $RTI_HOME/lib/portico.jar appended
		 * to the end. For the library path, the following is constructed:
		 * 
		 *   * System path (PATH, LD_LIBRARY_PATH, DYLD_LIBRARY_PATH as appropriate)
		 *   * $PWD
		 *   * $RTI_HOME/lib                   (Linux only)
		 *   * $RTI_HOME/bin                   (Windows only)
		 *   * $JAVA_HOME/jre/lib/server       (Mac OS X 64-bit)
		 *   * $JAVA_HOME/jre/lib/i386/client  (Win/Linux 32-bit)
		 *   * $JAVA_HOME/jre/lib/amd64/server (Win/Linux 64-bit)
		 * 
		 * If JAVA_HOME isn't set, RTI_HOME is assumed so that we can link in with the
		 * JRE that Portico ships with.
		 */
		pair<string,string> generatePaths() throw( RTIinternalError );
		pair<string,string> generateWinPath( string rtihome ) throw( RTIinternalError );
		pair<string,string> generateUnixPath( string rtihome ) throw( RTIinternalError );
		
		/**
		 * Invoke the Java class to load and parse the RID file, ignore if it doesn't exist.
		 */
		void processRid() throw( RTIinternalError );
		
		// utility methods
		bool pathExists( string path );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:
		static Runtime* getRuntime() throw( RTIinternalError );
		static void shutdown();
};

PORTICO1516E_NS_END

#endif /* RUNTIME_H_ */
