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

//
// This file contains the basic implementation bodies for the classes declared
// in RTI/Typedefs.h.
//

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                             FederationExecutionInformation                                       
//------------------------------------------------------------------------------------------
FederationExecutionInformation::FederationExecutionInformation( 
	const std::wstring& theFederationExecutionName,
	const std::wstring& theLogicalTimeImplementationName )
	: federationExecutionName(theFederationExecutionName),
	  logicalTimeImplementationName(theLogicalTimeImplementationName)
{
	
}

//------------------------------------------------------------------------------------------
//                               FederateRestoreStatus                                       
//------------------------------------------------------------------------------------------
FederateRestoreStatus::FederateRestoreStatus( const FederateHandle& thePreHandle,
                                              const FederateHandle& thePostHandle,
                                              RestoreStatus theStatus )
	: preRestoreHandle(thePreHandle),
	  postRestoreHandle(thePostHandle),
	  status(theStatus)
{
	
}

//------------------------------------------------------------------------------------------
//                              SupplementalReflectInfo                                       
//------------------------------------------------------------------------------------------
SupplementalReflectInfo::SupplementalReflectInfo()
	: hasProducingFederate(false),
	  hasSentRegions(false),
	  producingFederate(FederateHandle()),
	  sentRegions(RegionHandleSet())
{
	
}

SupplementalReflectInfo::SupplementalReflectInfo( const FederateHandle& theFederateHandle )
	: hasProducingFederate(true),
	  hasSentRegions(false),
	  producingFederate(theFederateHandle),
	  sentRegions(RegionHandleSet())
{
	
}

SupplementalReflectInfo::SupplementalReflectInfo( const RegionHandleSet& theRegionHandleSet )
	: hasProducingFederate(false),
	  hasSentRegions(true),
	  producingFederate(FederateHandle()),
	  sentRegions(theRegionHandleSet)
{
	
}


SupplementalReflectInfo::SupplementalReflectInfo( const FederateHandle& theFederateHandle,
                                                  const RegionHandleSet& theRegionHandleSet )
	: hasProducingFederate(true),
	  hasSentRegions(true),
	  producingFederate(theFederateHandle),
	  sentRegions(theRegionHandleSet)
{
	
}

//------------------------------------------------------------------------------------------
//                              SupplementalReceiveInfo                                       
//------------------------------------------------------------------------------------------
SupplementalReceiveInfo::SupplementalReceiveInfo()
	: hasProducingFederate(false),
	  hasSentRegions(false),
	  producingFederate(FederateHandle()),
	  sentRegions(RegionHandleSet())
{
	
}

SupplementalReceiveInfo::SupplementalReceiveInfo( const FederateHandle& theFederateHandle )
	: hasProducingFederate(true),
	  hasSentRegions(false),
	  producingFederate(theFederateHandle),
	  sentRegions(RegionHandleSet())
{
	
}

SupplementalReceiveInfo::SupplementalReceiveInfo( const RegionHandleSet& theRegionHandleSet )
	: hasProducingFederate(false),
	  hasSentRegions(true),
	  producingFederate(FederateHandle()),
	  sentRegions(theRegionHandleSet)
{
	
}


SupplementalReceiveInfo::SupplementalReceiveInfo( const FederateHandle& theFederateHandle,
                                                  const RegionHandleSet& theRegionHandleSet )
	: hasProducingFederate(true),
	  hasSentRegions(true),
	  producingFederate(theFederateHandle),
	  sentRegions(theRegionHandleSet)
{
	
}

//------------------------------------------------------------------------------------------
//                              SupplementalRemoveInfo                                       
//------------------------------------------------------------------------------------------
SupplementalRemoveInfo::SupplementalRemoveInfo()
	: hasProducingFederate(false),
	  producingFederate(FederateHandle())
{
	
}

SupplementalRemoveInfo::SupplementalRemoveInfo( const FederateHandle& theFederateHandle )
	: hasProducingFederate(true),
	  producingFederate(theFederateHandle)
{
	
}

IEEE1516E_NS_END
