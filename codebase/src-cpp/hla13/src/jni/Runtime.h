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
#ifndef RUNTIME_H_
#define RUNTIME_H_

#include "common.h"
#include "utils/Logger.h"

// Need a forward declaration of this because the JavaRTI.h file
// includes this file (thus creating a circular reference). Without
// this, the compiler cracks the sads
class JavaRTI;
#include "JavaRTI.h"

PORTICO13_NS_START

class Runtime
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:
		static Runtime *instance;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		Logger *logger;
		
		// this is set to true if we only attach to the JVM, rather than create it
		bool attached;
		
		// the list of active rtis - the key is the id of the JavaRTI (->getId())
		std::map<int, JavaRTI*> *activeRtis;
		
		// has the PORTICO_DEBUG environment variable been set?
		bool jniCheck;

	public: // make the jvm stuff accessible easily
		JavaVM *jvm;
		JNIEnv *jvmenv; // the main environment, others are created for RTIambassadors

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private:
		// singleton - so we make the constructor private
		// the JVM will be started in the constructor
		Runtime() throw( HLA::RTIinternalError );

	public:
		virtual ~Runtime();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		JavaRTI* newRtiAmbassador() throw( HLA::RTIinternalError );
		JavaRTI* getRtiAmbassador( int id );
		void     removeRtiAmbassador( int id );

	private:
		void   initializeJVM() throw( HLA::RTIinternalError );
		char** generatePaths() throw( HLA::RTIinternalError );
		char** generateWin32Path( char* rtihome ) throw( HLA::RTIinternalError );
		char** generateUnixPath( char* rtihome )  throw( HLA::RTIinternalError );
		void   processRid() throw( HLA::RTIinternalError );
		char*  convertAndReleaseJString( jstring string );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:
		static Runtime* getRuntime() throw( HLA::RTIinternalError );
		static void shutdown();

};

PORTICO13_NS_END

#endif /*RUNTIME_H_*/
