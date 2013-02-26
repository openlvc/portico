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
#include "utils/ExceptionHacks.h"
#include "utils/StringUtils.h"

PORTICO1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------
#define CHECK_AND_THROW(Type)                             \
	if( name.compare(#Type) == 0 )                        \
	{                                                     \
		throw Type( StringUtils::toWideString(reason) );  \
	}

/*
 * This method will throw an exception of the given name, supplying it the given reason.
 * For example, if the name is "RTIinternalError", this method will throw an exception
 * of type rti1516e::RTIinternalError.
 */
void ExceptionHacks::checkAndThrow( string name, string reason )
{
	if( name.empty() )
	{
		return;
	}
	else if( name.compare("RTIinternalError") == 0 )
	{
		throw RTIinternalError( StringUtils::toWideString(reason) );
	}
	CHECK_AND_THROW(AlreadyConnected)
	CHECK_AND_THROW(AsynchronousDeliveryAlreadyDisabled)
	CHECK_AND_THROW(AsynchronousDeliveryAlreadyEnabled)
	CHECK_AND_THROW(AttributeAcquisitionWasNotCanceled)
	CHECK_AND_THROW(AttributeAcquisitionWasNotRequested)
	CHECK_AND_THROW(AttributeAlreadyBeingAcquired)
	CHECK_AND_THROW(AttributeAlreadyBeingChanged)
	CHECK_AND_THROW(AttributeAlreadyBeingDivested)
	CHECK_AND_THROW(AttributeAlreadyOwned)
	CHECK_AND_THROW(AttributeDivestitureWasNotRequested)
	CHECK_AND_THROW(AttributeNotDefined)
	CHECK_AND_THROW(AttributeNotOwned)
	CHECK_AND_THROW(AttributeNotPublished)
	CHECK_AND_THROW(AttributeNotRecognized)
	CHECK_AND_THROW(AttributeNotSubscribed)
	CHECK_AND_THROW(AttributeRelevanceAdvisorySwitchIsOff)
	CHECK_AND_THROW(AttributeRelevanceAdvisorySwitchIsOn)
	CHECK_AND_THROW(AttributeScopeAdvisorySwitchIsOff)
	CHECK_AND_THROW(AttributeScopeAdvisorySwitchIsOn)
	CHECK_AND_THROW(BadInitializationParameter)
	CHECK_AND_THROW(CallNotAllowedFromWithinCallback)
	CHECK_AND_THROW(ConnectionFailed)
	CHECK_AND_THROW(CouldNotCreateLogicalTimeFactory)
	CHECK_AND_THROW(CouldNotDecode)
	CHECK_AND_THROW(CouldNotDiscover)
	CHECK_AND_THROW(CouldNotEncode)
	CHECK_AND_THROW(CouldNotOpenFDD)
	CHECK_AND_THROW(CouldNotOpenMIM)
	CHECK_AND_THROW(CouldNotInitiateRestore)
	CHECK_AND_THROW(DeletePrivilegeNotHeld)
	CHECK_AND_THROW(DesignatorIsHLAstandardMIM)
	CHECK_AND_THROW(RequestForTimeConstrainedPending)
	CHECK_AND_THROW(NoRequestToEnableTimeConstrainedWasPending)
	CHECK_AND_THROW(RequestForTimeRegulationPending)
	CHECK_AND_THROW(NoRequestToEnableTimeRegulationWasPending)
	CHECK_AND_THROW(NoFederateWillingToAcquireAttribute)
	CHECK_AND_THROW(ErrorReadingFDD)
	CHECK_AND_THROW(ErrorReadingMIM)
	CHECK_AND_THROW(FederateAlreadyExecutionMember)
	CHECK_AND_THROW(FederateHandleNotKnown)
	CHECK_AND_THROW(FederateHasNotBegunSave)
	CHECK_AND_THROW(FederateInternalError)
	CHECK_AND_THROW(FederateIsExecutionMember)
	CHECK_AND_THROW(FederateNameAlreadyInUse)
	CHECK_AND_THROW(FederateNotExecutionMember)
	CHECK_AND_THROW(FederateOwnsAttributes)
	CHECK_AND_THROW(FederateServiceInvocationsAreBeingReportedViaMOM)
	CHECK_AND_THROW(FederateUnableToUseTime)
	CHECK_AND_THROW(FederatesCurrentlyJoined)
	CHECK_AND_THROW(FederationExecutionAlreadyExists)
	CHECK_AND_THROW(FederationExecutionDoesNotExist)
	CHECK_AND_THROW(IllegalName)
	CHECK_AND_THROW(IllegalTimeArithmetic)
	CHECK_AND_THROW(InconsistentFDD)
	CHECK_AND_THROW(InteractionClassAlreadyBeingChanged)
	CHECK_AND_THROW(InteractionClassNotDefined)
	CHECK_AND_THROW(InteractionClassNotPublished)
	CHECK_AND_THROW(InteractionClassNotRecognized)
	CHECK_AND_THROW(InteractionClassNotSubscribed)
	CHECK_AND_THROW(InteractionParameterNotDefined)
	CHECK_AND_THROW(InteractionParameterNotRecognized)
	CHECK_AND_THROW(InteractionRelevanceAdvisorySwitchIsOff)
	CHECK_AND_THROW(InteractionRelevanceAdvisorySwitchIsOn)
	CHECK_AND_THROW(InTimeAdvancingState)
	CHECK_AND_THROW(InvalidAttributeHandle)
	CHECK_AND_THROW(InvalidDimensionHandle)
	CHECK_AND_THROW(InvalidFederateHandle)
	CHECK_AND_THROW(InvalidInteractionClassHandle)
	CHECK_AND_THROW(InvalidLocalSettingsDesignator)
	CHECK_AND_THROW(InvalidLogicalTime)
	CHECK_AND_THROW(InvalidLogicalTimeInterval)
	CHECK_AND_THROW(InvalidLookahead)
	CHECK_AND_THROW(InvalidObjectClassHandle)
	CHECK_AND_THROW(InvalidOrderName)
	CHECK_AND_THROW(InvalidOrderType)
	CHECK_AND_THROW(InvalidParameterHandle)
	CHECK_AND_THROW(InvalidRangeBound)
	CHECK_AND_THROW(InvalidRegion)
	CHECK_AND_THROW(InvalidResignAction)
	CHECK_AND_THROW(InvalidRegionContext)
	CHECK_AND_THROW(InvalidMessageRetractionHandle)
	CHECK_AND_THROW(InvalidServiceGroup)
	CHECK_AND_THROW(InvalidTransportationName)
	CHECK_AND_THROW(InvalidTransportationType)
	CHECK_AND_THROW(InvalidUpdateRateDesignator)
	CHECK_AND_THROW(JoinedFederateIsNotInTimeAdvancingState)
	CHECK_AND_THROW(LogicalTimeAlreadyPassed)
	CHECK_AND_THROW(MessageCanNoLongerBeRetracted)
	CHECK_AND_THROW(NameNotFound)
	CHECK_AND_THROW(NameSetWasEmpty)
	CHECK_AND_THROW(NoAcquisitionPending)
	CHECK_AND_THROW(NotConnected)
	CHECK_AND_THROW(ObjectClassNotDefined)
	CHECK_AND_THROW(ObjectClassNotKnown)
	CHECK_AND_THROW(ObjectClassNotPublished)
	CHECK_AND_THROW(ObjectClassRelevanceAdvisorySwitchIsOff)
	CHECK_AND_THROW(ObjectClassRelevanceAdvisorySwitchIsOn)
	CHECK_AND_THROW(ObjectInstanceNameInUse)
	CHECK_AND_THROW(ObjectInstanceNameNotReserved)
	CHECK_AND_THROW(ObjectInstanceNotKnown)
	CHECK_AND_THROW(OwnershipAcquisitionPending)
	// CHECK_AND_THROW(RTIinternalError) -- we do earlier, to short-path the most common case
	CHECK_AND_THROW(RegionDoesNotContainSpecifiedDimension)
	CHECK_AND_THROW(RegionInUseForUpdateOrSubscription)
	CHECK_AND_THROW(RegionNotCreatedByThisFederate)
	CHECK_AND_THROW(RestoreInProgress)
	CHECK_AND_THROW(RestoreNotInProgress)
	CHECK_AND_THROW(RestoreNotRequested)
	CHECK_AND_THROW(SaveInProgress)
	CHECK_AND_THROW(SaveNotInProgress)
	CHECK_AND_THROW(SaveNotInitiated)
	CHECK_AND_THROW(SpecifiedSaveLabelDoesNotExist)
	CHECK_AND_THROW(SynchronizationPointLabelNotAnnounced)
	CHECK_AND_THROW(TimeConstrainedAlreadyEnabled)
	CHECK_AND_THROW(TimeConstrainedIsNotEnabled)
	CHECK_AND_THROW(TimeRegulationAlreadyEnabled)
	CHECK_AND_THROW(TimeRegulationIsNotEnabled)
	CHECK_AND_THROW(UnableToPerformSave)
	CHECK_AND_THROW(UnknownName)
	CHECK_AND_THROW(UnsupportedCallbackModel)
	CHECK_AND_THROW(InternalError)	
	else
	{
		std::cout << "ERROR [exception-hacks]: Unknown Exception: " << name << std::endl;
		throw RTIinternalError( StringUtils::toWideString(reason) );
	}
}

PORTICO1516E_NS_END
