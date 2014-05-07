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
#include "rtiamb/PorticoRtiAmbassador.h"
#include "jni/JniUtils.h"
#include "utils/Logger.h"

PORTICO1516E_NS_START

/////////////////////////////////////////////////////////////////////////////////
// Object Management Services ///////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// 6.2
void PorticoRtiAmbassador::reserveObjectInstanceName( const std::wstring& name ) 
	throw( IllegalName,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
		logger->trace( "[Starting] reserveObjectInstanceName(): name=%ls", name.c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jstring jname = JniUtils::fromWideString( jnienv, name );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->RESERVE_NAME,
	                        jname );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jname );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
		logger->trace( "[Finished] reserveObjectInstanceName(): name=%ls", name.c_str() );
}

// 6.4
void PorticoRtiAmbassador::releaseObjectInstanceName( const std::wstring& name )
	throw( ObjectInstanceNameNotReserved,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
		logger->trace( "[Starting] releaseObjectInstanceName(): name=%ls", name.c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jstring jname = JniUtils::fromWideString( jnienv, name );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->RELEASE_NAME,
	                        jname );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jname );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
		logger->trace( "[Finished] releaseObjectInstanceName(): name=%ls", name.c_str() );
}

// 6.5
void PorticoRtiAmbassador::reserveMultipleObjectInstanceName( const std::set<std::wstring>& names )
	throw( IllegalName,
	       NameSetWasEmpty,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] reserveMultipleObjectInstanceName(): names=%ls",
		               Logger::toWString(names).c_str() );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jobjectArray jnames = JniUtils::fromSet( jnienv, names );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->RESERVE_MULTIPLE_NAMES,
	                        jnames );

	// clean up and run the exception check
	JniUtils::deleteJniArray( jnienv, jnames );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] reserveMultipleObjectInstanceName(): names=%ls",
		              Logger::toWString(names).c_str() );
	}
}

// 6.7
void PorticoRtiAmbassador::releaseMultipleObjectInstanceName( const std::set<std::wstring>& names )
	throw( ObjectInstanceNameNotReserved,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] releaseMultipleObjectInstanceName(): names=%ls",
		               Logger::toWString(names).c_str() );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jobjectArray jnames = JniUtils::fromSet( jnienv, names );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->RELEASE_MULTIPLE_NAMES,
	                        jnames );

	// clean up and run the exception check
	JniUtils::deleteJniArray( jnienv, jnames );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] releaseMultipleObjectInstanceName(): names=%ls",
		              Logger::toWString(names).c_str() );
	}
}

// 6.8
ObjectInstanceHandle PorticoRtiAmbassador::registerObjectInstance( ObjectClassHandle theClass )
	throw( ObjectClassNotPublished,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	// convert it now so we can use it in the log message - stupid, eh?
	jint jclassHandle = JniUtils::fromHandle( theClass );
	
	if( logger->isTraceEnabled() )
		logger->trace( "[Starting] registerObjectInstance(): class=%d", jclassHandle );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// call the method
	jint jobjectHandle = jnienv->CallIntMethod( javarti->jproxy,
	                                            javarti->REGISTER_OBJECT,
	                                            jclassHandle );
	
	// clean up and run the exception check
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
		logger->trace( "[Finished] registerObjectInstance(): class=%d", jclassHandle );
	
	return JniUtils::toObjectHandle( jobjectHandle );
}

ObjectInstanceHandle PorticoRtiAmbassador::registerObjectInstance( ObjectClassHandle theClass,
                                                                   const std::wstring& name )
	throw( ObjectInstanceNameInUse,
	       ObjectInstanceNameNotReserved,
	       ObjectClassNotPublished,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// convert it now so we can use it in the log message - stupid, eh? Might as well do them all
	jint jclassHandle = JniUtils::fromHandle( theClass );
	jstring jname = JniUtils::fromWideString( jnienv, name );
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] registerObjectInstance(): class=%d, name=%ls",
		               jclassHandle, name.c_str() );
	}
	
	// call the method
	jint jobjectHandle = jnienv->CallIntMethod( javarti->jproxy,
	                                            javarti->REGISTER_OBJECT_WITH_NAME,
	                                            jclassHandle,
	                                            jname );
	
	// clean up and run the exception check
	jnienv->DeleteLocalRef( jname );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] registerObjectInstance(): class=%d, name=%ls",
		              jclassHandle, name.c_str() );
	}
	
	return JniUtils::toObjectHandle( jobjectHandle );
}

// 6.10
void PorticoRtiAmbassador::updateAttributeValues( ObjectInstanceHandle theObject,
                                                  const AttributeHandleValueMap& attributes,
                                                  const VariableLengthData& tag )
	throw( AttributeNotOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] updateAttributeValues(): class=%ls, attributes=%s",
		               theObject.toString().c_str(),
		               Logger::toString(attributes).c_str() );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	HVPS jattributes = JniUtils::fromMap( jnienv, attributes );
	jbyteArray jtag = JniUtils::fromTag( jnienv, tag );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->UPDATE_ATTRIBUTE_VALUES,
	                        jobjectHandle,
	                        jattributes.handles,
	                        jattributes.values,
	                        jtag );

	// clean up and run the exception check
	JniUtils::deleteHVPS( jnienv, jattributes );
	jnienv->DeleteLocalRef( jtag );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] updateAttributeValues(): class=%d, attributes=%s",
		               jobjectHandle,
		               Logger::toString(attributes).c_str() );
	}
}

MessageRetractionHandle PorticoRtiAmbassador::updateAttributeValues(
		ObjectInstanceHandle theObject,
		const AttributeHandleValueMap& attributes,
		const VariableLengthData& tag,
		const LogicalTime& theTime )
	throw( InvalidLogicalTime,
		   AttributeNotOwned,
		   AttributeNotDefined,
		   ObjectInstanceNotKnown,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateNotExecutionMember,
		   NotConnected,
		   RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] updateAttributeValues(): class=%ls, attributes=%s, time=%f",
		               theObject.toString().c_str(),
		               Logger::toString(attributes).c_str(),
		               JniUtils::fromTime(theTime) );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	HVPS jattributes = JniUtils::fromMap( jnienv, attributes );
	jbyteArray jtag = JniUtils::fromTag( jnienv, tag );
	jdouble jtime = JniUtils::fromTime( theTime );
	
	// call the method
	jint retraction = jnienv->CallIntMethod( javarti->jproxy,
	                                         javarti->UPDATE_ATTRIBUTE_VALUES_WITH_TIME,
	                                         jobjectHandle,
	                                         jattributes.handles,
	                                         jattributes.values,
	                                         jtag,
	                                         jtime );

	// clean up and run the exception check
	JniUtils::deleteHVPS( jnienv, jattributes );
	jnienv->DeleteLocalRef( jtag );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] updateAttributeValues(): class=%d, attributes=%s, time=%f",
		               jobjectHandle,
		               Logger::toString(attributes).c_str(),
		               jtime );
	}
	
	return JniUtils::toRetractionHandle( retraction );
}

// 6.12
void PorticoRtiAmbassador::sendInteraction( InteractionClassHandle theInteraction,
                                            const ParameterHandleValueMap& parameters,
                                            const VariableLengthData& tag )
	throw( InteractionClassNotPublished,
	       InteractionParameterNotDefined,
	       InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] sendInteraction(): class=%ls, parameters=%s",
		               theInteraction.toString().c_str(),
		               Logger::toString(parameters).c_str() );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jinteraction = JniUtils::fromHandle( theInteraction );
	HVPS jparameters = JniUtils::fromMap( jnienv, parameters );
	jbyteArray jtag = JniUtils::fromTag( jnienv, tag );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->SEND_INTERACTION,
	                        jinteraction,
	                        jparameters.handles,
	                        jparameters.values,
	                        jtag );

	// clean up and run the exception check
	JniUtils::deleteHVPS( jnienv, jparameters );
	jnienv->DeleteLocalRef( jtag );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] jparameters(): class=%d, parameters=%s",
		               jinteraction,
		               Logger::toString(parameters).c_str() );
	}
}

MessageRetractionHandle PorticoRtiAmbassador::sendInteraction(
	InteractionClassHandle theInteraction,
	const ParameterHandleValueMap& parameters,
	const VariableLengthData& tag,
	const LogicalTime& theTime )
	throw( InvalidLogicalTime,
	       InteractionClassNotPublished,
	       InteractionParameterNotDefined,
	       InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] sendInteraction(): class=%ls, parameters=%s, time=%f",
		               theInteraction.toString().c_str(),
		               Logger::toString(parameters).c_str(),
		               JniUtils::fromTime(theTime) );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jinteraction = JniUtils::fromHandle( theInteraction );
	HVPS jparameters = JniUtils::fromMap( jnienv, parameters );
	jbyteArray jtag = JniUtils::fromTag( jnienv, tag );
	jdouble jtime = JniUtils::fromTime( theTime );
	
	// call the method
	jint retraction = jnienv->CallIntMethod( javarti->jproxy,
	                                         javarti->SEND_INTERACTION_WITH_TIME,
	                                         jinteraction,
	                                         jparameters.handles,
	                                         jparameters.values,
	                                         jtag,
	                                         jtime );

	// clean up and run the exception check
	JniUtils::deleteHVPS( jnienv, jparameters );
	jnienv->DeleteLocalRef( jtag );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] sendInteraction(): class=%d, attributes=%s, time=%f",
		               jinteraction,
		               Logger::toString(parameters).c_str(),
		               jtime );
	}
	
	return JniUtils::toRetractionHandle( retraction );
}

// 6.14
void PorticoRtiAmbassador::deleteObjectInstance( ObjectInstanceHandle theObject,
                                                 const VariableLengthData& tag )
	throw( DeletePrivilegeNotHeld,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] deleteObjectInstance(): object=%ls",
		               theObject.toString().c_str() );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jbyteArray jtag = JniUtils::fromTag( jnienv, tag );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->DELETE_OBJECT_INSTANCE,
	                        jobjectHandle,
	                        jtag );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jtag );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] deleteObjectInstance(): object=%d", jobjectHandle );
	}
}

MessageRetractionHandle PorticoRtiAmbassador::deleteObjectInstance( ObjectInstanceHandle theObject,
                                                                    const VariableLengthData& tag,
                                                                    const LogicalTime& theTime )
	throw( InvalidLogicalTime,
	       DeletePrivilegeNotHeld,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] deleteObjectInstance(): object=%ls, time=%d",
		               theObject.toString().c_str(),
		               JniUtils::fromTime(theTime) );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jbyteArray jtag = JniUtils::fromTag( jnienv, tag );
	jdouble jtime = JniUtils::fromTime( theTime );

	// call the method
	jint handle = jnienv->CallIntMethod( javarti->jproxy,
	                                     javarti->DELETE_OBJECT_INSTANCE_WITH_TIME,
	                                     jobjectHandle,
	                                     jtag,
	                                     jtime );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jtag );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] deleteObjectInstance(): object=%d, time=%d", jobjectHandle, jtime );
	}
	
	return JniUtils::toRetractionHandle( handle );
}

// 6.16
void PorticoRtiAmbassador::localDeleteObjectInstance( ObjectInstanceHandle theObject )
	throw( OwnershipAcquisitionPending,
	       FederateOwnsAttributes,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] localDeleteObjectInstance(): object=%ls",
		               theObject.toString().c_str() );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->LOCAL_DELETE_OBJECT_INSTANCE,
	                        jobjectHandle );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] localDeleteObjectInstance(): object=%d", jobjectHandle );
	}
}

// 6.19
void PorticoRtiAmbassador::requestAttributeValueUpdate( ObjectInstanceHandle theObject,
                                                        const AttributeHandleSet& attributes,
                                                        const VariableLengthData& tag )
	throw( AttributeNotDefined,
		   ObjectInstanceNotKnown,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateNotExecutionMember,
		   NotConnected,
		   RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] requestAttributeValueUpdate(): object=%ls, attributes=%s",
		               theObject.toString().c_str(),
		               Logger::toString(attributes).c_str() );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jintArray jattributes = JniUtils::fromSet( jnienv, attributes );
	jbyteArray jtag = JniUtils::fromTag( jnienv, tag );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->REQUEST_OBJECT_ATTRIBUTE_VALUE_UPDATE,
	                        jobjectHandle,
	                        jattributes,
	                        jtag );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jtag );
	jnienv->DeleteLocalRef( jattributes );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] requestAttributeValueUpdate(): object=%d, attributes=%s",
		              jobjectHandle,
		              Logger::toString(attributes).c_str() );
	}
}

void PorticoRtiAmbassador::requestAttributeValueUpdate( ObjectClassHandle theClass,
                                                        const AttributeHandleSet& attributes,
                                                        const VariableLengthData& tag )
	throw( AttributeNotDefined,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] requestAttributeValueUpdate(): class=%ls, attributes=%s",
		               theClass.toString().c_str(),
		               Logger::toString(attributes).c_str() );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jclassHandle = JniUtils::fromHandle( theClass );
	jintArray jattributes = JniUtils::fromSet( jnienv, attributes );
	jbyteArray jtag = JniUtils::fromTag( jnienv, tag );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->REQUEST_CLASS_ATTRIBUTE_VALUE_UPDATE,
	                        jclassHandle,
	                        jattributes,
	                        jtag );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jtag );
	jnienv->DeleteLocalRef( jattributes );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] requestAttributeValueUpdate(): class=%d, attributes=%s",
		              jclassHandle,
		              Logger::toString(attributes).c_str() );
	}
}

// 6.23
void PorticoRtiAmbassador::requestAttributeTransportationTypeChange(
		ObjectInstanceHandle theObject,
		const AttributeHandleSet& attributes,
		TransportationType theType )
	throw( AttributeAlreadyBeingChanged,
	       AttributeNotOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       InvalidTransportationType,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] requestAttributeTransportationTypeChange(): object=%ls, attributes=%s",
		               theObject.toString().c_str(),
		               Logger::toString(attributes).c_str() );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jintArray jattributes = JniUtils::fromSet( jnienv, attributes );
	jstring jtransport = JniUtils::fromTransport( jnienv, theType );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->REQUEST_ATTRIBUTE_TRANSPORT_CHANGE,
	                        jobjectHandle,
	                        jattributes,
	                        jtransport );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jattributes );
	jnienv->DeleteLocalRef( jtransport );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] requestAttributeTransportationTypeChange(): object=%d, attributes=%s",
		              jobjectHandle,
		              Logger::toString(attributes).c_str() );
	}
}

// 6.25
void PorticoRtiAmbassador::queryAttributeTransportationType( ObjectInstanceHandle theObject,
                                                             AttributeHandle theAttribute )
	throw( AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] queryAttributeTransportationType(): object=%ls, attribute=%ls",
		               theObject.toString().c_str(),
		               theAttribute.toString().c_str() );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jint jattribute = JniUtils::fromHandle( theAttribute );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->QUERY_ATTRIBUTE_TRANSPORT_TYPE,
	                        jobjectHandle,
	                        jattribute );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] queryAttributeTransportationType(): object=%d, attribute=%d",
		              jobjectHandle,
		              jattribute );
	}
}

// 6.27
void PorticoRtiAmbassador::requestInteractionTransportationTypeChange( InteractionClassHandle theClass,
                                                                       TransportationType theType )
	throw( InteractionClassAlreadyBeingChanged,
	       InteractionClassNotPublished,
	       InteractionClassNotDefined,
	       InvalidTransportationType,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] requestInteractionTransportationTypeChange(): interaction=%ls",
		               theClass.toString().c_str() );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jinteraction = JniUtils::fromHandle( theClass );
	jstring jtransport = JniUtils::fromTransport( jnienv, theType );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->REQUEST_INTERACTION_TRANSPORT_CHANGE,
	                        jinteraction,
	                        jtransport );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jtransport );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] requestInteractionTransportationTypeChange(): interaction=%d",
		              jinteraction );
	}
}

// 6.29
void PorticoRtiAmbassador::queryInteractionTransportationType( FederateHandle theFederate,
                                                               InteractionClassHandle theInteraction )
	throw( InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] queryInteractionTransportationType(): federate=%ls, interaction=%ls",
		               theFederate.toString().c_str(),
		               theFederate.toString().c_str() );
	}

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jfederate = JniUtils::fromHandle( theFederate );
	jint jinteraction = JniUtils::fromHandle( theInteraction );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->QUERY_INTERACTION_TRANSPORT_TYPE,
	                        jfederate,
	                        jinteraction );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] queryInteractionTransportationType(): federate=%d, interaction=%d",
		              jfederate,
		              jinteraction );
	}
}

PORTICO1516E_NS_END
