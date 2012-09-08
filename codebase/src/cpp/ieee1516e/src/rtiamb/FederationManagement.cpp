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

PORTICO1516E_NS_START

/////////////////////////////////////////////////////////////////////////////////
// Federation Management Services ///////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// 4.2
void PorticoRtiAmbassador::connect( FederateAmbassador & federateAmbassador,
                                    CallbackModel theCallbackModel,
                                    const std::wstring& localSettingsDesignator )
	throw( ConnectionFailed,
		   InvalidLocalSettingsDesignator,
		   UnsupportedCallbackModel,
		   AlreadyConnected,
		   CallNotAllowedFromWithinCallback,
		   RTIinternalError )
{
	
}

// 4.3
void PorticoRtiAmbassador::disconnect() throw( FederateIsExecutionMember,
                                               CallNotAllowedFromWithinCallback,
                                               RTIinternalError )
{
	
}

// 4.5
void PorticoRtiAmbassador::createFederationExecution( const std::wstring& federationName,
								                      const std::wstring& fomModule,
								                      const std::wstring& timeImplementation )
	throw( CouldNotCreateLogicalTimeFactory,
		   InconsistentFDD,
		   ErrorReadingFDD,
		   CouldNotOpenFDD,
		   FederationExecutionAlreadyExists,
		   NotConnected,
		   RTIinternalError )
{

}

void PorticoRtiAmbassador::createFederationExecution( const std::wstring& federationName,
                                                      const std::vector<std::wstring>& fomModules,
                                                      const std::wstring& timeImplementation )
	throw( CouldNotCreateLogicalTimeFactory,
		   InconsistentFDD,
		   ErrorReadingFDD,
		   CouldNotOpenFDD,
		   FederationExecutionAlreadyExists,
		   NotConnected,
		   RTIinternalError )
{
	
}

void PorticoRtiAmbassador::createFederationExecutionWithMIM(
	const std::wstring& federationName,
    const std::vector<std::wstring>& fomModules,
    const std::wstring& mimModule,
    const std::wstring& timeImplementation )
	throw( CouldNotCreateLogicalTimeFactory,
		   InconsistentFDD,
		   ErrorReadingFDD,
		   CouldNotOpenFDD,
		   DesignatorIsHLAstandardMIM,
		   ErrorReadingMIM,
		   CouldNotOpenMIM,
		   FederationExecutionAlreadyExists,
		   NotConnected,
		   RTIinternalError )
{
	
}

// 4.6
void PorticoRtiAmbassador::destroyFederationExecution( const std::wstring& federationName )
	throw( FederatesCurrentlyJoined,
		   FederationExecutionDoesNotExist,
		   NotConnected,
		   RTIinternalError )
{
	
}

// 4.7
void PorticoRtiAmbassador::listFederationExecutions() throw( NotConnected, RTIinternalError )
{
	
}

// 4.9
FederateHandle PorticoRtiAmbassador::joinFederationExecution(
	const std::wstring& federateType,
	const std::wstring& federationName,
	const std::vector<std::wstring>& fomModules )
	throw( CouldNotCreateLogicalTimeFactory,
		   FederationExecutionDoesNotExist,
		   InconsistentFDD,
		   ErrorReadingFDD, 
		   CouldNotOpenFDD,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateAlreadyExecutionMember,
		   NotConnected,
		   CallNotAllowedFromWithinCallback,
		   RTIinternalError )
{
	return FederateHandle();
}

FederateHandle PorticoRtiAmbassador::joinFederationExecution(
	const std::wstring& federateName,
	const std::wstring& federateType,
	const std::wstring& federationName,
	const std::vector<std::wstring>& additionalFomModules )
	throw( CouldNotCreateLogicalTimeFactory,
		   FederateNameAlreadyInUse,
		   FederationExecutionDoesNotExist,
		   InconsistentFDD,
		   ErrorReadingFDD,
		   CouldNotOpenFDD,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateAlreadyExecutionMember,
		   NotConnected,
		   CallNotAllowedFromWithinCallback,
		   RTIinternalError )
{
	return FederateHandle();
}

// 4.10
void PorticoRtiAmbassador::resignFederationExecution( ResignAction resignAction )
	throw( InvalidResignAction,
		   OwnershipAcquisitionPending,
		   FederateOwnsAttributes,
		   FederateNotExecutionMember,
		   NotConnected,
		   CallNotAllowedFromWithinCallback,
		   RTIinternalError )
{
	
}

PORTICO1516E_NS_END
