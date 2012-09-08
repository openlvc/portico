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
#include "common.h"
#include "RTI/encoding/EncodingExceptions.h"

IEEE1516E_NS_START

// Output operator for Exceptions - declaration in standard RTI/Exception.h
std::wostream& operator<< ( std::wostream& stream, const Exception& exception )
{
	return stream << exception.what();
}

//////////////////////////////////////////////////////////////////////////////////////////
// class: Exception (declared in RTI/Exception.h)                                       //
//        This is the parent of all exception types, which are defined via a macro in   //
//        the standard header. The declaration for the Exception type is as follows:    //
//                                                                                      //
// public: Exception();                                                                 //
//         Exception(const Exception& rhs);                                             //
//         Exception& operator= (const Exception& rhs);                                 //
//         virtual ~Exception();                                                        //
//         virtual std::wstring what() const throw() = 0;                               //
//////////////////////////////////////////////////////////////////////////////////////////
Exception::Exception()
{
}

Exception::~Exception()
{	
}

//Exception::Exception( const Exception& rhs )
//{
//}
//
//Exception& Exception::operator= ( const Exception& rhs )
//{
//}

//////////////////////////////////////////////////////////////////////////////////////////
// class: ? extends Exception (declared in RTI/Exception.h)                             //
//        The macro below defines the body for all the exception types that were        //
//        declared by the main macro in the standard header. The basic declaration for  //
//        these is as follow:                                                           //
//                                                                                      //
// class {ExceptionName} : public Exception                                             //
// {                                                                                    //
//     private:                                                                         //
//         std::wstring _msg                                                            //
//                                                                                      //
//     public:                                                                          //
//         {ExceptionName}( const std::wstring& message ) throw();                      //
//         std::wstring what() const throw();                                           //
// };                                                                                   //
//////////////////////////////////////////////////////////////////////////////////////////
#define EXCEPTION_BODY(Type)                             \
	Type::Type( const std::wstring& message ) throw()    \
	{                                                    \
		this->_msg = std::wstring( message );            \
	}                                                    \
	                                                     \
	std::wstring Type::what() const throw()              \
	{                                                    \
		return this->_msg;                               \
	}                                                    \

EXCEPTION_BODY(AlreadyConnected)
EXCEPTION_BODY(AsynchronousDeliveryAlreadyDisabled)
EXCEPTION_BODY(AsynchronousDeliveryAlreadyEnabled)
EXCEPTION_BODY(AttributeAcquisitionWasNotCanceled)
EXCEPTION_BODY(AttributeAcquisitionWasNotRequested)
EXCEPTION_BODY(AttributeAlreadyBeingAcquired)
EXCEPTION_BODY(AttributeAlreadyBeingChanged)
EXCEPTION_BODY(AttributeAlreadyBeingDivested)
EXCEPTION_BODY(AttributeAlreadyOwned)
EXCEPTION_BODY(AttributeDivestitureWasNotRequested)
EXCEPTION_BODY(AttributeNotDefined)
EXCEPTION_BODY(AttributeNotOwned)
EXCEPTION_BODY(AttributeNotPublished)
EXCEPTION_BODY(AttributeNotRecognized)
EXCEPTION_BODY(AttributeNotSubscribed)
EXCEPTION_BODY(AttributeRelevanceAdvisorySwitchIsOff)
EXCEPTION_BODY(AttributeRelevanceAdvisorySwitchIsOn)
EXCEPTION_BODY(AttributeScopeAdvisorySwitchIsOff)
EXCEPTION_BODY(AttributeScopeAdvisorySwitchIsOn)
EXCEPTION_BODY(BadInitializationParameter)
EXCEPTION_BODY(CallNotAllowedFromWithinCallback)
EXCEPTION_BODY(ConnectionFailed)
EXCEPTION_BODY(CouldNotCreateLogicalTimeFactory)
EXCEPTION_BODY(CouldNotDecode)
EXCEPTION_BODY(CouldNotDiscover)
EXCEPTION_BODY(CouldNotEncode)
EXCEPTION_BODY(CouldNotOpenFDD)
EXCEPTION_BODY(CouldNotOpenMIM)
EXCEPTION_BODY(CouldNotInitiateRestore)
EXCEPTION_BODY(DeletePrivilegeNotHeld)
EXCEPTION_BODY(DesignatorIsHLAstandardMIM)
EXCEPTION_BODY(RequestForTimeConstrainedPending)
EXCEPTION_BODY(NoRequestToEnableTimeConstrainedWasPending)
EXCEPTION_BODY(RequestForTimeRegulationPending)
EXCEPTION_BODY(NoRequestToEnableTimeRegulationWasPending)
EXCEPTION_BODY(NoFederateWillingToAcquireAttribute)
EXCEPTION_BODY(EncoderException)
EXCEPTION_BODY(ErrorReadingFDD)
EXCEPTION_BODY(ErrorReadingMIM)
EXCEPTION_BODY(FederateAlreadyExecutionMember)
EXCEPTION_BODY(FederateHandleNotKnown)
EXCEPTION_BODY(FederateHasNotBegunSave)
EXCEPTION_BODY(FederateInternalError)
EXCEPTION_BODY(FederateIsExecutionMember)
EXCEPTION_BODY(FederateNameAlreadyInUse)
EXCEPTION_BODY(FederateNotExecutionMember)
EXCEPTION_BODY(FederateOwnsAttributes)
EXCEPTION_BODY(FederateServiceInvocationsAreBeingReportedViaMOM)
EXCEPTION_BODY(FederateUnableToUseTime)
EXCEPTION_BODY(FederatesCurrentlyJoined)
EXCEPTION_BODY(FederationExecutionAlreadyExists)
EXCEPTION_BODY(FederationExecutionDoesNotExist)
EXCEPTION_BODY(IllegalName)
EXCEPTION_BODY(IllegalTimeArithmetic)
EXCEPTION_BODY(InconsistentFDD)
EXCEPTION_BODY(InteractionClassAlreadyBeingChanged)
EXCEPTION_BODY(InteractionClassNotDefined)
EXCEPTION_BODY(InteractionClassNotPublished)
EXCEPTION_BODY(InteractionClassNotRecognized)
EXCEPTION_BODY(InteractionClassNotSubscribed)
EXCEPTION_BODY(InteractionParameterNotDefined)
EXCEPTION_BODY(InteractionParameterNotRecognized)
EXCEPTION_BODY(InteractionRelevanceAdvisorySwitchIsOff)
EXCEPTION_BODY(InteractionRelevanceAdvisorySwitchIsOn)
EXCEPTION_BODY(InTimeAdvancingState)
EXCEPTION_BODY(InvalidAttributeHandle)
EXCEPTION_BODY(InvalidDimensionHandle)
EXCEPTION_BODY(InvalidFederateHandle)
EXCEPTION_BODY(InvalidInteractionClassHandle)
EXCEPTION_BODY(InvalidLocalSettingsDesignator)
EXCEPTION_BODY(InvalidLogicalTime)
EXCEPTION_BODY(InvalidLogicalTimeInterval)
EXCEPTION_BODY(InvalidLookahead)
EXCEPTION_BODY(InvalidObjectClassHandle)
EXCEPTION_BODY(InvalidOrderName)
EXCEPTION_BODY(InvalidOrderType)
EXCEPTION_BODY(InvalidParameterHandle)
EXCEPTION_BODY(InvalidRangeBound)
EXCEPTION_BODY(InvalidRegion)
EXCEPTION_BODY(InvalidResignAction)
EXCEPTION_BODY(InvalidRegionContext)
EXCEPTION_BODY(InvalidMessageRetractionHandle)
EXCEPTION_BODY(InvalidServiceGroup)
EXCEPTION_BODY(InvalidTransportationName)
EXCEPTION_BODY(InvalidTransportationType)
EXCEPTION_BODY(InvalidUpdateRateDesignator)
EXCEPTION_BODY(JoinedFederateIsNotInTimeAdvancingState)
EXCEPTION_BODY(LogicalTimeAlreadyPassed)
EXCEPTION_BODY(MessageCanNoLongerBeRetracted)
EXCEPTION_BODY(NameNotFound)
EXCEPTION_BODY(NameSetWasEmpty)
EXCEPTION_BODY(NoAcquisitionPending)
EXCEPTION_BODY(NotConnected)
EXCEPTION_BODY(ObjectClassNotDefined)
EXCEPTION_BODY(ObjectClassNotKnown)
EXCEPTION_BODY(ObjectClassNotPublished)
EXCEPTION_BODY(ObjectClassRelevanceAdvisorySwitchIsOff)
EXCEPTION_BODY(ObjectClassRelevanceAdvisorySwitchIsOn)
EXCEPTION_BODY(ObjectInstanceNameInUse)
EXCEPTION_BODY(ObjectInstanceNameNotReserved)
EXCEPTION_BODY(ObjectInstanceNotKnown)
EXCEPTION_BODY(OwnershipAcquisitionPending)
EXCEPTION_BODY(RTIinternalError)
EXCEPTION_BODY(RegionDoesNotContainSpecifiedDimension)
EXCEPTION_BODY(RegionInUseForUpdateOrSubscription)
EXCEPTION_BODY(RegionNotCreatedByThisFederate)
EXCEPTION_BODY(RestoreInProgress)
EXCEPTION_BODY(RestoreNotInProgress)
EXCEPTION_BODY(RestoreNotRequested)
EXCEPTION_BODY(SaveInProgress)
EXCEPTION_BODY(SaveNotInProgress)
EXCEPTION_BODY(SaveNotInitiated)
EXCEPTION_BODY(SpecifiedSaveLabelDoesNotExist)
EXCEPTION_BODY(SynchronizationPointLabelNotAnnounced)
EXCEPTION_BODY(TimeConstrainedAlreadyEnabled)
EXCEPTION_BODY(TimeConstrainedIsNotEnabled)
EXCEPTION_BODY(TimeRegulationAlreadyEnabled)
EXCEPTION_BODY(TimeRegulationIsNotEnabled)
EXCEPTION_BODY(UnableToPerformSave)
EXCEPTION_BODY(UnknownName)
EXCEPTION_BODY(UnsupportedCallbackModel)
EXCEPTION_BODY(InternalError)

IEEE1516E_NS_END
