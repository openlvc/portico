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
#include "common.h"

#include <cstring>
#include <algorithm>

const char* HLA::ArrayIndexOutOfBounds::_ex = "ArrayIndexOutOfBounds";
const char* HLA::AsynchronousDeliveryAlreadyDisabled::_ex = "AsynchronousDeliveryAlreadyDisabled";
const char* HLA::AsynchronousDeliveryAlreadyEnabled::_ex = "AsynchronousDeliveryAlreadyEnabled";
const char* HLA::AttributeAcquisitionWasNotRequested::_ex = "AttributeAcquisitionWasNotRequested";
const char* HLA::AttributeAcquisitionWasNotCanceled::_ex = "AttributeAcquisitionWasNotCanceled";
const char* HLA::AttributeAlreadyBeingAcquired::_ex = "AttributeAlreadyBeingAcquired";
const char* HLA::AttributeAlreadyBeingDivested::_ex = "AttributeAlreadyBeingDivested";
const char* HLA::AttributeAlreadyOwned::_ex = "AttributeAlreadyOwned";
const char* HLA::AttributeDivestitureWasNotRequested::_ex = "AttributeDivestitureWasNotRequested";
const char* HLA::AttributeNotDefined::_ex = "AttributeNotDefined";
const char* HLA::AttributeNotKnown::_ex = "AttributeNotKnown";
const char* HLA::AttributeNotOwned::_ex = "AttributeNotOwned";
const char* HLA::AttributeNotPublished::_ex = "AttributeNotPublished";
const char* HLA::ConcurrentAccessAttempted::_ex = "ConcurrentAccessAttempted";
const char* HLA::CouldNotDiscover::_ex = "CouldNotDiscover";
const char* HLA::CouldNotOpenFED::_ex = "CouldNotOpenFED";
const char* HLA::CouldNotRestore::_ex = "CouldNotRestore";
const char* HLA::DeletePrivilegeNotHeld::_ex = "DeletePrivilegeNotHeld";
const char* HLA::DimensionNotDefined::_ex = "DimensionNotDefined";
const char* HLA::EnableTimeConstrainedPending::_ex = "EnableTimeConstrainedPending";
const char* HLA::EnableTimeConstrainedWasNotPending::_ex = "EnableTimeConstrainedWasNotPending";
const char* HLA::EnableTimeRegulationPending::_ex = "EnableTimeRegulationPending";
const char* HLA::EnableTimeRegulationWasNotPending::_ex = "EnableTimeRegulationWasNotPending";
const char* HLA::ErrorReadingFED::_ex = "ErrorReadingFED";
const char* HLA::EventNotKnown::_ex = "EventNotKnown";
const char* HLA::FederateAlreadyExecutionMember::_ex = "FederateAlreadyExecutionMember";
const char* HLA::FederateInternalError::_ex = "FederateInternalError";
const char* HLA::FederateLoggingServiceCalls::_ex = "FederateLoggingServiceCalls";
const char* HLA::FederateNotExecutionMember::_ex = "FederateNotExecutionMember";
const char* HLA::FederateOwnsAttributes::_ex = "FederateOwnsAttributes";
const char* HLA::FederateWasNotAskedToReleaseAttribute::_ex = "FederateWasNotAskedToReleaseAttribute";
const char* HLA::FederatesCurrentlyJoined::_ex = "FederatesCurrentlyJoined";
const char* HLA::FederationExecutionAlreadyExists::_ex = "FederationExecutionAlreadyExists";
const char* HLA::FederationExecutionDoesNotExist::_ex = "FederationExecutionDoesNotExist";
const char* HLA::FederationTimeAlreadyPassed::_ex = "FederationTimeAlreadyPassed";
const char* HLA::HandleValuePairMaximumExceeded::_ex = "HandleValuePairMaximumExceeded";
const char* HLA::InteractionClassNotDefined::_ex = "InteractionClassNotDefined";
const char* HLA::InteractionClassNotKnown::_ex = "InteractionClassNotKnown";
const char* HLA::InteractionClassNotPublished::_ex = "InteractionClassNotPublished";
const char* HLA::InteractionClassNotSubscribed::_ex = "InteractionClassNotSubscribed";
const char* HLA::InteractionParameterNotDefined::_ex = "InteractionParameterNotDefined";
const char* HLA::InteractionParameterNotKnown::_ex = "InteractionParameterNotKnown";
const char* HLA::InvalidExtents::_ex = "InvalidExtents";
const char* HLA::InvalidFederationTime::_ex = "InvalidFederationTime";
const char* HLA::InvalidHandleValuePairSetContext::_ex = "InvalidHandleValuePairSetContext";
const char* HLA::InvalidLookahead::_ex = "InvalidLookahead";
const char* HLA::InvalidOrderingHandle::_ex = "InvalidOrderingHandle";
const char* HLA::InvalidRegionContext::_ex = "InvalidRegionContext";
const char* HLA::InvalidResignAction::_ex = "InvalidResignAction";
const char* HLA::InvalidRetractionHandle::_ex = "InvalidRetractionHandle";
const char* HLA::InvalidTransportationHandle::_ex = "InvalidTransportationHandle";
const char* HLA::MemoryExhausted::_ex = "MemoryExhausted";
const char* HLA::NameNotFound::_ex = "NameNotFound";
const char* HLA::ObjectClassNotDefined::_ex = "ObjectClassNotDefined";
const char* HLA::ObjectClassNotKnown::_ex = "ObjectClassNotKnown";
const char* HLA::ObjectClassNotPublished::_ex = "ObjectClassNotPublished";
const char* HLA::ObjectClassNotSubscribed::_ex = "ObjectClassNotSubscribed";
const char* HLA::ObjectNotKnown::_ex = "ObjectNotKnown";
const char* HLA::ObjectAlreadyRegistered::_ex = "ObjectAlreadyRegistered";
const char* HLA::OwnershipAcquisitionPending::_ex = "OwnershipAcquisitionPending";
const char* HLA::RegionInUse::_ex = "RegionInUse";
const char* HLA::RegionNotKnown::_ex = "RegionNotKnown";
const char* HLA::RestoreInProgress::_ex = "RestoreInProgress";
const char* HLA::RestoreNotRequested::_ex = "RestoreNotRequested";
const char* HLA::RTIinternalError::_ex = "RTIinternalError";
const char* HLA::SpaceNotDefined::_ex = "SpaceNotDefined";
const char* HLA::SaveInProgress::_ex = "SaveInProgress";
const char* HLA::SaveNotInitiated::_ex = "SaveNotInitiated";
const char* HLA::SpecifiedSaveLabelDoesNotExist::_ex = "SpecifiedSaveLabelDoesNotExist";
const char* HLA::SynchronizationPointLabelWasNotAnnounced::_ex = "SynchronizationPointLabelWasNotAnnounced";
const char* HLA::TimeAdvanceAlreadyInProgress::_ex = "TimeAdvanceAlreadyInProgress";
const char* HLA::TimeAdvanceWasNotInProgress::_ex = "TimeAdvanceWasNotInProgress";
const char* HLA::TimeConstrainedAlreadyEnabled::_ex = "TimeConstrainedAlreadyEnabled";
const char* HLA::TimeConstrainedWasNotEnabled::_ex = "TimeConstrainedWasNotEnabled";
const char* HLA::TimeRegulationAlreadyEnabled::_ex = "TimeRegulationAlreadyEnabled";
const char* HLA::TimeRegulationWasNotEnabled::_ex = "TimeRegulationWasNotEnabled";
const char* HLA::UnableToPerformSave::_ex = "UnableToPerformSave";
const char* HLA::ValueCountExceeded::_ex = "ValueCountExceeded";
const char* HLA::ValueLengthExceeded::_ex = "ValueLengthExceeded";

HLA::Exception::Exception( const char* reason )
{
	if( reason != NULL )
	{
		this->_reason = new char[strlen(reason)+1];
		strcpy( this->_reason, reason );
	}
	else
	{
		this->_reason = new char[1];
		this->_reason[0] = '\0';
	}
}

HLA::Exception::Exception( const Exception& e )
{
	if( e._reason != NULL )
	{
		this->_reason = new char[strlen(e._reason)+1];
		strcpy( this->_reason, e._reason );
	}
	else
	{
		this->_reason = new char[1];
		this->_reason[0] = '\0';
	}
}

HLA::Exception& HLA::Exception::operator=(const Exception& e)
{
	delete [] this->_reason;

	if( e._reason != NULL )
	{
		this->_reason = new char[strlen(e._reason)+1];
		strcpy( this->_reason, e._reason );
	}
	else
	{
		this->_reason = new char[1];
		this->_reason[0] = '\0';
	}

	return *this;
}

HLA::Exception::~Exception()
{
	delete[] this->_reason;
}

