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
package org.portico2.rti.services.time.incoming;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JInvalidLookahead;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.msg.ModifyLookahead;
import org.portico2.rti.services.RTIMessageHandler;

public class ModifyLookaheadHandler extends RTIMessageHandler
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
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process( MessageContext context ) throws JException
	{
		ModifyLookahead request = context.getRequest( ModifyLookahead.class, this );
		int federate = request.getSourceFederate();
		double newLookahead = request.getLookahead();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Federate [%s] requested LOOKAHEAD MODIFICATION to [%d]",
			              moniker(federate), newLookahead );
		}

		// The modification could be a reduction. Make sure the new federate LBTS is not less
		// than the current federation LBTS
		// NOTE: this doesn't matter if there are no constrained federates, only check if there are
		double proposedLbts = timeManager.getCurrentTime(federate) + newLookahead;
		if( timeManager.hasConstrainedFederates() && proposedLbts < timeManager.getLBTS() )
		{
			logger.error( "FAILURE [%s] requested a lookahead that was lower than federation LBTS (requested=%d)",
			              moniker(federate), newLookahead );
			throw new JInvalidLookahead( "Requested lookahead of ["+newLookahead+"] was too low" );
		}

		// update the local status and do any pending advanced notifications
		timeManager.setLookahead( federate, newLookahead );

		// check to see if anyone needs an advance in light of the lookahead change
		queueDummyAdvance();
		
		// send back a success note - put the lookahead in there
		context.success( newLookahead );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
