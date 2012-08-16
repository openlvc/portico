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
#include "ExceptionHacks.h"

PORTICO13_NS_START

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////// Constructors ////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
ExceptionHacks::ExceptionHacks()
{
}

ExceptionHacks::~ExceptionHacks()
{
}

//////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Instance Methods //////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Static Methods ///////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/*
 * This method will throw an exception of the given name, supplying it the given reason.
 * For example, if the name is "RTIinternalError", this method will throw an exception
 * of type HLA::RTIinternalError.
 * 
 * MEMORY MANAGEMENT NOTES: This method will DELETE both of the given parameters before
 *                          it throws the exception. The exception makes a copy of the
 *                          reason, so there is no need to keep it around. If you want
 *                          to retain the information passed in, make sure you give this
 *                          method a COPY of that data, rather than the original
 */
void ExceptionHacks::cleanAndThrow( char *name, char *reason )
{
	if( name == NULL )
	{
		return;
	}
	else if( strcmp(name,"RTIinternalError") == 0 )
	{
		CLEAN_AND_THROW( HLA::RTIinternalError );
	}
	else if( strcmp(name,"ArrayIndexOutOfBounds") == 0 )
	{
		CLEAN_AND_THROW( HLA::ArrayIndexOutOfBounds );
	}
	else if( strcmp(name,"AsynchronousDeliveryAlreadyDisabled") == 0 )
	{
		CLEAN_AND_THROW( HLA::AsynchronousDeliveryAlreadyDisabled );
	}
	else if( strcmp(name,"AsynchronousDeliveryAlreadyEnabled") == 0 )
	{
		CLEAN_AND_THROW( HLA::AsynchronousDeliveryAlreadyEnabled );
	}
	else if( strcmp(name,"AttributeAcquisitionWasNotRequested") == 0 )
	{
		CLEAN_AND_THROW( HLA::AttributeAcquisitionWasNotRequested );
	}
	else if( strcmp(name,"AttributeAcquisitionWasNotCanceled") == 0 )
	{
		CLEAN_AND_THROW( HLA::AttributeAcquisitionWasNotCanceled );
	}
	else if( strcmp(name,"AttributeAlreadyBeingAcquired") == 0 )
	{
		CLEAN_AND_THROW( HLA::AttributeAlreadyBeingAcquired );
	}
	else if( strcmp(name,"AttributeAlreadyBeingDivested") == 0 )
	{
		CLEAN_AND_THROW( HLA::AttributeAlreadyBeingDivested );
	}
	else if( strcmp(name,"AttributeAlreadyOwned") == 0 )
	{
		CLEAN_AND_THROW( HLA::AttributeAlreadyOwned );
	}
	else if( strcmp(name,"AttributeDivestitureWasNotRequested") == 0 )
	{
		CLEAN_AND_THROW( HLA::AttributeDivestitureWasNotRequested );
	}
	else if( strcmp(name,"AttributeNotDefined") == 0 )
	{
		CLEAN_AND_THROW( HLA::AttributeNotDefined );
	}
	else if( strcmp(name,"AttributeNotKnown") == 0 )
	{
		CLEAN_AND_THROW( HLA::AttributeNotKnown );
	}
	else if( strcmp(name,"AttributeNotOwned") == 0 )
	{
		CLEAN_AND_THROW( HLA::AttributeNotOwned );
	}
	else if( strcmp(name,"AttributeNotPublished") == 0 )
	{
		CLEAN_AND_THROW( HLA::AttributeNotPublished );
	}
	else if( strcmp(name,"ConcurrentAccessAttempted") == 0 )
	{
		CLEAN_AND_THROW( HLA::ConcurrentAccessAttempted );
	}
	else if( strcmp(name,"CouldNotDiscover") == 0 )
	{
		CLEAN_AND_THROW( HLA::CouldNotDiscover );
	}
	else if( strcmp(name,"CouldNotOpenFED") == 0 )
	{
		CLEAN_AND_THROW( HLA::CouldNotOpenFED );
	}
	else if( strcmp(name,"CouldNotRestore") == 0 )
	{
		CLEAN_AND_THROW( HLA::CouldNotRestore );
	}
	else if( strcmp(name,"DeletePrivilegeNotHeld") == 0 )
	{
		CLEAN_AND_THROW( HLA::DeletePrivilegeNotHeld );
	}
	else if( strcmp(name,"DimensionNotDefined") == 0 )
	{
		CLEAN_AND_THROW( HLA::DimensionNotDefined );
	}
	else if( strcmp(name,"EnableTimeConstrainedPending") == 0 )
	{
		CLEAN_AND_THROW( HLA::EnableTimeConstrainedPending );
	}
	else if( strcmp(name,"EnableTimeConstrainedWasNotPending") == 0 )
	{
		CLEAN_AND_THROW( HLA::EnableTimeConstrainedWasNotPending );
	}
	else if( strcmp(name,"EnableTimeRegulationPending") == 0 )
	{
		CLEAN_AND_THROW( HLA::EnableTimeRegulationPending );
	}
	else if( strcmp(name,"EnableTimeRegulationWasNotPending") == 0 )
	{
		CLEAN_AND_THROW( HLA::EnableTimeRegulationWasNotPending );
	}
	else if( strcmp(name,"ErrorReadingFED") == 0 )
	{
		CLEAN_AND_THROW( HLA::ErrorReadingFED );
	}
	else if( strcmp(name,"EventNotKnown") == 0 )
	{
		CLEAN_AND_THROW( HLA::EventNotKnown );
	}
	else if( strcmp(name,"FederateAlreadyExecutionMember") == 0 )
	{
		CLEAN_AND_THROW( HLA::FederateAlreadyExecutionMember );
	}
	else if( strcmp(name,"FederateInternalError") == 0 )
	{
		CLEAN_AND_THROW( HLA::FederateInternalError );
	}
	else if( strcmp(name,"FederateLoggingServiceCalls") == 0 )
	{
		CLEAN_AND_THROW( HLA::FederateLoggingServiceCalls );
	}
	else if( strcmp(name,"FederateNotExecutionMember") == 0 )
	{
		CLEAN_AND_THROW( HLA::FederateNotExecutionMember );
	}
	else if( strcmp(name,"FederateNotSubscribed") == 0 )
	{
		// this exception is thrown from RTIambassador.unsubscribeObjectClassWithRegion() and
		// translates to ObjectClassNotSubscribed. I'm not sure why the exception on the Java
		// interface differs, but it does
		CLEAN_AND_THROW( HLA::ObjectClassNotSubscribed );
	}
	else if( strcmp(name,"FederateOwnsAttributes") == 0 )
	{
		CLEAN_AND_THROW( HLA::FederateOwnsAttributes );
	}
	else if( strcmp(name,"FederateWasNotAskedToReleaseAttribute") == 0 )
	{
		CLEAN_AND_THROW( HLA::FederateWasNotAskedToReleaseAttribute );
	}
	else if( strcmp(name,"FederatesCurrentlyJoined") == 0 )
	{
		CLEAN_AND_THROW( HLA::FederatesCurrentlyJoined );
	}
	else if( strcmp(name,"FederationExecutionAlreadyExists") == 0 )
	{
		CLEAN_AND_THROW( HLA::FederationExecutionAlreadyExists );
	}
	else if( strcmp(name,"FederationExecutionDoesNotExist") == 0 )
	{
		CLEAN_AND_THROW( HLA::FederationExecutionDoesNotExist );
	}
	else if( strcmp(name,"FederationTimeAlreadyPassed") == 0 )
	{
		CLEAN_AND_THROW( HLA::FederationTimeAlreadyPassed );
	}
	else if( strcmp(name,"HandleValuePairMaximumExceeded") == 0 )
	{
		CLEAN_AND_THROW( HLA::HandleValuePairMaximumExceeded );
	}
	else if( strcmp(name,"InteractionClassNotDefined") == 0 )
	{
		CLEAN_AND_THROW( HLA::InteractionClassNotDefined );
	}
	else if( strcmp(name,"InteractionClassNotKnown") == 0 )
	{
		CLEAN_AND_THROW( HLA::InteractionClassNotKnown );
	}
	else if( strcmp(name,"InteractionClassNotPublished") == 0 )
	{
		CLEAN_AND_THROW( HLA::InteractionClassNotPublished );
	}
	else if( strcmp(name,"InteractionClassNotSubscribed") == 0 )
	{
		CLEAN_AND_THROW( HLA::InteractionClassNotSubscribed );
	}
	else if( strcmp(name,"InteractionParameterNotDefined") == 0 )
	{
		CLEAN_AND_THROW( HLA::InteractionParameterNotDefined );
	}
	else if( strcmp(name,"InteractionParameterNotKnown") == 0 )
	{
		CLEAN_AND_THROW( HLA::InteractionParameterNotKnown );
	}
	else if( strcmp(name,"InvalidExtents") == 0 )
	{
		CLEAN_AND_THROW( HLA::InvalidExtents );
	}
	else if( strcmp(name,"InvalidFederationTime") == 0 )
	{
		CLEAN_AND_THROW( HLA::InvalidFederationTime );
	}
	else if( strcmp(name,"InvalidHandleValuePairSetContext") == 0 )
	{
		CLEAN_AND_THROW( HLA::InvalidHandleValuePairSetContext );
	}
	else if( strcmp(name,"InvalidLookahead") == 0 )
	{
		CLEAN_AND_THROW( HLA::InvalidLookahead );
	}
	else if( strcmp(name,"InvalidOrderingHandle") == 0 )
	{
		CLEAN_AND_THROW( HLA::InvalidOrderingHandle );
	}
	else if( strcmp(name,"InvalidRegionContext") == 0 )
	{
		CLEAN_AND_THROW( HLA::InvalidRegionContext );
	}
	else if( strcmp(name,"InvalidResignAction") == 0 )
	{
		CLEAN_AND_THROW( HLA::InvalidResignAction );
	}
	else if( strcmp(name,"InvalidRetractionHandle") == 0 )
	{
		CLEAN_AND_THROW( HLA::InvalidRetractionHandle );
	}
	else if( strcmp(name,"InvalidTransportationHandle") == 0 )
	{
		CLEAN_AND_THROW( HLA::InvalidTransportationHandle );
	}
	else if( strcmp(name,"MemoryExhausted") == 0 )
	{
		CLEAN_AND_THROW( HLA::MemoryExhausted );
	}
	else if( strcmp(name,"NameNotFound") == 0 )
	{
		CLEAN_AND_THROW( HLA::NameNotFound );
	}
	else if( strcmp(name,"ObjectClassNotDefined") == 0 )
	{
		CLEAN_AND_THROW( HLA::ObjectClassNotDefined );
	}
	else if( strcmp(name,"ObjectClassNotKnown") == 0 )
	{
		CLEAN_AND_THROW( HLA::ObjectClassNotKnown );
	}
	else if( strcmp(name,"ObjectClassNotPublished") == 0 )
	{
		CLEAN_AND_THROW( HLA::ObjectClassNotPublished );
	}
	else if( strcmp(name,"ObjectClassNotSubscribed") == 0 )
	{
		CLEAN_AND_THROW( HLA::ObjectClassNotSubscribed );
	}
	else if( strcmp(name,"ObjectNotKnown") == 0 )
	{
		CLEAN_AND_THROW( HLA::ObjectNotKnown );
	}
	else if( strcmp(name,"ObjectAlreadyRegistered") == 0 )
	{
		CLEAN_AND_THROW( HLA::ObjectAlreadyRegistered );
	}
	else if( strcmp(name,"OwnershipAcquisitionPending") == 0 )
	{
		CLEAN_AND_THROW( HLA::OwnershipAcquisitionPending );
	}
	else if( strcmp(name,"RegionInUse") == 0 )
	{
		CLEAN_AND_THROW( HLA::RegionInUse );
	}
	else if( strcmp(name,"RegionNotKnown") == 0 )
	{
		CLEAN_AND_THROW( HLA::RegionNotKnown );
	}
	else if( strcmp(name,"RestoreInProgress") == 0 )
	{
		CLEAN_AND_THROW( HLA::RestoreInProgress );
	}
	else if( strcmp(name,"RestoreNotRequested") == 0 )
	{
		CLEAN_AND_THROW( HLA::RestoreNotRequested );
	}
	else if( strcmp(name,"SpaceNotDefined") == 0 )
	{
		CLEAN_AND_THROW( HLA::SpaceNotDefined );
	}
	else if( strcmp(name,"SaveInProgress") == 0 )
	{
		CLEAN_AND_THROW( HLA::SaveInProgress );
	}
	else if( strcmp(name,"SaveNotInitiated") == 0 )
	{
		CLEAN_AND_THROW( HLA::SaveNotInitiated );
	}
	else if( strcmp(name,"SpecifiedSaveLabelDoesNotExist") == 0 )
	{
		CLEAN_AND_THROW( HLA::SpecifiedSaveLabelDoesNotExist );
	}
	else if( strcmp(name,"SynchronizationLabelNotAnnounced") == 0 )
	{
		CLEAN_AND_THROW( HLA::SynchronizationPointLabelWasNotAnnounced );
	}
	else if( strcmp(name,"TimeAdvanceAlreadyInProgress") == 0 )
	{
		CLEAN_AND_THROW( HLA::TimeAdvanceAlreadyInProgress );
	}
	else if( strcmp(name,"TimeAdvanceWasNotInProgress") == 0 )
	{
		CLEAN_AND_THROW( HLA::TimeAdvanceWasNotInProgress );
	}
	else if( strcmp(name,"TimeConstrainedAlreadyEnabled") == 0 )
	{
		CLEAN_AND_THROW( HLA::TimeConstrainedAlreadyEnabled );
	}
	else if( strcmp(name,"TimeConstrainedWasNotEnabled") == 0 )
	{
		CLEAN_AND_THROW( HLA::TimeConstrainedWasNotEnabled );
	}
	else if( strcmp(name,"TimeRegulationAlreadyEnabled") == 0 )
	{
		CLEAN_AND_THROW( HLA::TimeRegulationAlreadyEnabled );
	}
	else if( strcmp(name,"TimeRegulationWasNotEnabled") == 0 )
	{
		CLEAN_AND_THROW( HLA::TimeRegulationWasNotEnabled );
	}
	else if( strcmp(name,"UnableToPerformSave") == 0 )
	{
		CLEAN_AND_THROW( HLA::UnableToPerformSave );
	}
	else if( strcmp(name,"ValueCountExceeded") == 0 )
	{
		CLEAN_AND_THROW( HLA::ValueCountExceeded );
	}
	else if( strcmp(name,"ValueLengthExceeded") == 0 )
	{
		CLEAN_AND_THROW( HLA::ValueLengthExceeded );
	}
	else
	{
		printf( "ERROR [exception-hacks]: Unknown Exception: %s", name );
		delete [] name;
		delete [] reason;
	}
}

PORTICO13_NS_END
