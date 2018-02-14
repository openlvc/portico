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
package org.portico.lrc.services.time.handlers.incoming;

import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.time.msg.EnableTimeRegulation;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=EnableTimeRegulation.class)
public class EnableRegulationIncomingHandler extends LRCMessageHandler
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
		EnableTimeRegulation request = context.getRequest( EnableTimeRegulation.class, this );
		int federate = request.getSourceFederate();
		double federateTime  = request.getFederateTime();
		double lookahead     = request.getLookahead();
		double requestedLBTS = federateTime + lookahead;

		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Federate ["+moniker(federate)+
			              "] requests ENABLE time regulation (fedtime=" +federateTime+
			              ", lookahead="+lookahead+")" );
		}

		/////////////////////////////////////
		// determine new time for federate //
		/////////////////////////////////////
		// This calculation is designed to ensure that the federate can get the federate time
		// closest to that which was requested, but still maintain the federation LBTS so that
		// we can be sure no events will be generated in the past.
		//
		// IF THERE ARE NO OTHER REGULATING FEDERATES:
		//  Use the highest logical time of all the constrained federates - requested lookahead.
		//  This way, no events can be generated in the logical past for any constrained federates.
		// IF THERE ARE OTHER REGULATING FEDERATES:
		//  -if our requested LBTS (requested time + lookahead) is greater than ANY of theirs,
		//   just use our requested values as the presence of lower values means they are safe
		//  -if our requested LBTS is LESS than all theirs, use the lowest of their LBTS - the
		//   requested lookahead. This would give us an LBTS equal to the current lowest.
		double newTime = Double.MAX_VALUE;
		
		Set<Integer> regulatingFederates = timeManager.getRegulatingFederates();
		if( regulatingFederates.isEmpty() == false )
		{
			//////////////////////////////////////////
			// there ARE other regulating federates //
			//////////////////////////////////////////
			double federationLbts = timeManager.getLBTS();
			if( requestedLBTS > federationLbts )
				newTime = federateTime;
			else
				newTime = federationLbts - lookahead;
		}
		else
		{
			//////////////////////////////////////////////
			// there ARE NOT other regulating federates //
			//////////////////////////////////////////////
			// We need an LBTS equal to the highest federate lbts of any constrained federates.
			// The default value should be the requested LBTS time so that if there are no
			// constrained federates, or they all have a time low enough, we get what we wanted.
			// NOTE: we use LBTS and not current time because even though the remote federate
			//       may have requested and been granted an advance to a higher time, we might
			//       still not have received the advance grant notice yet. If they've requested
			//       it however, we'll take note
			double highestFederateLbts = requestedLBTS; 
			for( Integer constrainedHandle : timeManager.getConstrainedFederates() )
			{
				double constrainedLbts = timeManager.getLBTS( constrainedHandle );
				if( constrainedLbts > highestFederateLbts )
					highestFederateLbts = constrainedLbts;
			}

			// make sure the time is valid for us. if the enable call passed a value for the
			// federate time that was lower than the current time we will need to bump it up
			// to our current time. i *think* this is right, but it might not be (for example,
			// if there are no other federates, perhaps our time should just change to whatever
			// we want and not be dependent on our current time status).
			newTime = highestFederateLbts - lookahead;
			if( newTime < timeManager.getTimeStatus(federate).getCurrentTime() )
				newTime = timeManager.getTimeStatus(federate).getCurrentTime();
		}
		
		///////////////////
		// set the state //
		///////////////////
		timeManager.enableRegulating( federate, newTime, lookahead );
		if( logger.isDebugEnabled() )
		{
			logger.debug( "ENABLED time regulating for ["+moniker(federate)+
			              "] (fedtime="+newTime+")" );
		}
		
		// if time regulation was enabled for THIS FEDERATE then make sure a callback can happen,
		// otherwise, if no callback is needed, just kill processing here
		if( federate != federateHandle() )
			// no need for further processing, if there is a callack to be made we will have queued it
			veto();
		else
			context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
