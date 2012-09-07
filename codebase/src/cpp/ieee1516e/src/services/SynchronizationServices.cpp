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
// Synchronization Services /////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// 4.11
void PorticoRtiAmbassador::registerFederationSynchronizationPoint( const std::wstring& label,
                                                                   const VariableLengthData& tag )
	throw( SaveInProgress,
		   RestoreInProgress,
		   FederateNotExecutionMember,
		   NotConnected,
		   RTIinternalError )
{
	
}

void PorticoRtiAmbassador::registerFederationSynchronizationPoint(
	const std::wstring& label,
	const VariableLengthData& tag,
	const FederateHandleSet& synchronizationSet )
	throw( InvalidFederateHandle,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateNotExecutionMember,
		   NotConnected,
		   RTIinternalError )
{
	
}

// 4.14
void PorticoRtiAmbassador::synchronizationPointAchieved( const std::wstring& label,
                                                         bool successfully )
	throw( SynchronizationPointLabelNotAnnounced,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateNotExecutionMember,
		   NotConnected,
		   RTIinternalError )
{
	
}

PORTICO1516E_NS_END
