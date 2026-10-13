/*
 *   Copyright 2008 The Portico Project
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
package org.portico.lrc.services.time.handlers.outgoing;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.services.time.msg.QueryGalt;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * Answers queryGALT() (IEEE 1516.1-2010, 8.16): the Greatest Available Logical Time of the local
 * federate, the greatest time it could be granted given the other regulating federates. That is
 * the lowest LBTS (current time plus lookahead) of the regulating federates other than this one,
 * which is what the time advances of the federation are granted against.
 * <p/>
 * The result is a Double, or null when GALT is not defined: no other federate is regulating.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=QueryGalt.class)
public class QueryGaltHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
	}

	public void process( MessageContext context ) throws Exception
	{
		// basic state validity checks
		lrcState.checkJoined();          // FederateNotExecutionMember
		lrcState.checkSave();            // SaveInProgress
		lrcState.checkRestore();         // RestoreInProgress

		context.getRequest( QueryGalt.class, this );

		int localFederate = lrcState.getFederateHandle();
		Double galt = null;
		for( Integer federate : timeManager.getRegulatingFederates() )
		{
			if( federate == localFederate )
				continue;

			double lbts = timeManager.getLBTS( federate );
			if( galt == null || lbts < galt )
				galt = lbts;
		}

		if( logger.isDebugEnabled() )
		{
			logger.debug( "QUERY GALT for ["+moniker()+"]: "+
			              (galt == null ? "undefined (no other regulating federate)" : galt) );
		}

		context.success( galt );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
