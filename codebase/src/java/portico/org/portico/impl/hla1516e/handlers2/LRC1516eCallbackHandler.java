/*
 *   Copyright 2018 The Portico Project
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

import java.util.Map;

import org.portico.impl.hla1516e.Impl1516eHelper;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.lrc.LRCMessageHandler;

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.exceptions.FederateInternalError;

public abstract class LRC1516eCallbackHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Impl1516eHelper helper;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
		
		this.helper = (Impl1516eHelper)super.lrc.getSpecHelper();
	}

	@Override
	public void process( MessageContext context ) throws JException
	{
		try
		{
			callback( context );
		}
		catch( Exception e )
		{
			logger.warn( "{CALLBACK} Federate threw exception: "+e.getMessage(), e );
		}
	}
	
	public abstract void callback( MessageContext context ) throws FederateInternalError;

	/**
	 * Fetch the return the {@link FederateAmbassador} reference from the helper.
	 * Can't just pre-store this as the handlers are all created <i>before</i> we
	 * join a federation (and thus, before we have a FederateAmbassador to store).
	 */
	protected final FederateAmbassador fedamb()
	{
		return this.helper.getFederateAmbassador();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
