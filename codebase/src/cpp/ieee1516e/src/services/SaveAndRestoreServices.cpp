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
#include "services/PorticoRtiAmbassador.h"

PORTICO1516E_NS_START

/////////////////////////////////////////////////////////////////////////////////
// Save and Restore Services ////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// 4.16
void PorticoRtiAmbassador::PorticoRtiAmbassador::requestFederationSave( const std::wstring& label )
	throw( SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

void PorticoRtiAmbassador::requestFederationSave( const std::wstring& label,
                                                  const LogicalTime& theTime )
	throw( LogicalTimeAlreadyPassed,
	       InvalidLogicalTime,
	       FederateUnableToUseTime,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}


// 4.18
void PorticoRtiAmbassador::federateSaveBegun()
	throw( SaveNotInitiated,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}


// 4.19
void PorticoRtiAmbassador::federateSaveComplete()
	throw( FederateHasNotBegunSave,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}


void PorticoRtiAmbassador::federateSaveNotComplete()
	throw( FederateHasNotBegunSave,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}


// 4.21
void PorticoRtiAmbassador::abortFederationSave()
	throw( SaveNotInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}


// 4.22
void PorticoRtiAmbassador::queryFederationSaveStatus()
	throw( RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}


// 4.24
void PorticoRtiAmbassador::requestFederationRestore( const std::wstring& label )
	throw( SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}


// 4.28
void PorticoRtiAmbassador::federateRestoreComplete()
	throw( RestoreNotRequested,
	       SaveInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}


void PorticoRtiAmbassador::federateRestoreNotComplete()
	throw( RestoreNotRequested,
	       SaveInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}


// 4.30
void PorticoRtiAmbassador::abortFederationRestore()
	throw( RestoreNotInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}


// 4.31
void PorticoRtiAmbassador::queryFederationRestoreStatus()
	throw( SaveInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}


PORTICO1516E_NS_END
