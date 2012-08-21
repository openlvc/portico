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
package org.portico.impl.hla1516e.handlers;

import hla.rti1516e.FederateAmbassador;

import java.util.Map;

import org.portico.impl.hla1516e.Impl1516eHelper;
import org.portico.lrc.LRCMessageHandler;
import org.portico.utils.messaging.MessageContext;

/**
 * Parent class for all IEEE-1516e callback handlers. Caches some useful variables and provides
 * some simple helper methods for common tasks (such as fetching the FederateAmbassador from the
 * {@link Impl1516eHelper}.
 */
public abstract class HLA1516eCallbackHandler extends LRCMessageHandler
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
	/**
	 * Basic caching of important components. All child handlers should delegate up to here.
	 */
	@Override
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
		this.helper = (Impl1516eHelper)lrc.getSpecHelper();
	}

	/**
	 * The guts of the handler is left to the actual handler.
	 */
	public abstract void process( MessageContext context ) throws Exception;

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
