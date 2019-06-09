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
package org.portico.impl.hla1516e.handlers2;

import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.HLA1516eRegionHandleSet;

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.RegionHandleSet;

/**
 * Due to the introduction of the 433rd refelctAttributes(), receiveInteraction() and
 * removeObject() overload, the grand high council of the API hath bequeathed us a new
 * type to contain information we sometimes care about and mostly don't. Instances of
 * this class shall now be passed to all overloads of the callbacks in question. Naturally
 * this has been implemented as three separate interfaces (it is the HLA after all), all
 * containing the same information only under a different name. My people, I give to you
 * the SupplementalInfo class!
 * <p/>
 * Hold your applause. You're really too much. I know, I know. Thank me later. 
 */
public class SupplementalInfo implements FederateAmbassador.SupplementalReceiveInfo,
                                         FederateAmbassador.SupplementalReflectInfo,
                                         FederateAmbassador.SupplementalRemoveInfo
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private FederateHandle producingFederate;
	private RegionHandleSet sentRegions;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected SupplementalInfo()
	{
		this.producingFederate = null;
		this.sentRegions = new HLA1516eRegionHandleSet();
	}

	protected SupplementalInfo( int producingFederate )
	{
		this();
		this.producingFederate = new HLA1516eHandle( producingFederate );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public FederateHandle getProducingFederate()
	{
		return this.producingFederate;
	}

	public void setProducingFederate( FederateHandle producingFederate )
	{
		this.producingFederate = producingFederate;
	}
	
	public boolean hasProducingFederate()
	{
		return this.producingFederate == null;
	}

	public RegionHandleSet getSentRegions()
	{
		return this.sentRegions;
	}
	
	public void setSentRegions( RegionHandleSet sentRegions )
	{
		this.sentRegions = sentRegions;
	}

	public boolean hasSentRegions()
	{
		return this.sentRegions == null;
	}

	@Override
	public String toString()
	{
		return "ProducingFederate="+producingFederate.toString();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
