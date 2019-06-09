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
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.services.time.msg.TimeAdvanceGrant;
import org.portico2.common.services.time.msg.TimeAdvanceRequest;
import org.portico2.rti.services.RTIMessageHandler;

public class TimeAdvanceRequestHandler extends RTIMessageHandler
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
		TimeAdvanceRequest request = context.getRequest( TimeAdvanceRequest.class, this );
		int federate = request.getSourceFederate();
		double newTime = request.getTime();

		// if this is a dummy request, just look to advance all the existing
		// federates that can now be advanced
		if( request.isDummyRequest() )
		{
			timeManager.recalculateLBTS();
			issueAllPossibleAdvances();
			context.success();
			return;
		}

		if( logger.isDebugEnabled() )
			logger.debug( "Federate ["+moniker(federate)+"] requests ADVANCE to time ["+newTime+"]" );

		// record the time advance request in the time manager
		TimeStatus status = timeManager.getTimeStatus( federate );
		status.timeAdvanceRequested( newTime );
		double federationLbts = timeManager.recalculateLBTS();
		
		//////////////////////////////////
		// Is the federate CONSTRAINED? //
		/////////////////////////////////////////////////////////////////////////////////////////
		// NOT CONSTRAINED: Schedule an immediate advancement, this federate isn't constrained //
		//                  by the regulating federates (including itself) so there is nothing //
		//                  and nobody that can hold it back!!                                 //
		//  IS CONSTRAINED: Federate is dependant on others in time advancement. However, the  //
		//                  federate will be granted an advance right away if the time is has  //
		//                  requested an advancement to is less than the current LBTS of the   //
		//                  federation (the lowest LBTS of all regulating federates).          //
		//                                                                                     //
		//                  *NOTE*: This check only occurs if the federate is NOT REGULATING.  //
		//                  If the federate is regulating, this will trigger a check for all   //
		//                  constrained federates in later processing. Thus, there is no need  //
		//                  to explicitly give it an advance now (it will be done later).      //
		/////////////////////////////////////////////////////////////////////////////////////////
		if( status.isConstrained() )
		{
			// Federate IS CONSTRAINED //
			// NOTE: If there are no regulating federates, the federation LBTS will be 0, which
			//       the requested time will obviously not be less than. However, if there are
			//       no regulating federates, we don't need to bother with the check and can just
			//       advance right away
			if( status.isRegulating() == false )
			{
				if( status.canAdvance(federationLbts) )
					advanceFederate( federate, status );
			}
		}
		else
		{
			// Federate IS NOT CONSTRAINED //
			// Schedule an immediate advance. We don't return right away as the federate
			// may be regulating, in which case there is still more work left to do.
			// **ONLY do this if we can advance, due to some weird stuff with the way disabling
			//   constrained works, we could end up with two advance requests for unconstrained
			//   federates to the same time (the real one, and a dummy one we issue after
			//   constrained is disabled to recognize that the federate is no longer held up)
			if( status.isInAdvancingState() )
				advanceFederate( federate, status );
		}
		
		/////////////////////////////////
		// Is the federate REGULATING? //
		/////////////////////////////////////////////////////////////////////////////////////////
		// NOT REGULATING: This TAR won't affect any constrained federates. In this case there //
		//                 is no need to do any more work, so we'll just return.               //
		//                                                                                     //
		//  IS REGULATING: This TAR may affect the constrained federates of the federation. In //
		//                 this case, we should check to see if any or them can now advance.   //
		/////////////////////////////////////////////////////////////////////////////////////////
		if( status.isRegulating() )
		{
			issueAllPossibleAdvances();
		}
		else
		{
			// nothing to do //
		}		
		
		context.success();
	}

	private void issueAllPossibleAdvances()
	{
		// process a potential advance for all the constrained federates //
		double federationLbts = timeManager.getLBTS();
		for( Integer constrainedHandle : timeManager.getConstrainedFederates() )
		{
			TimeStatus constrainedStatus = timeManager.getTimeStatus( constrainedHandle );
			if( constrainedStatus.canAdvance(federationLbts) )
				advanceFederate( constrainedHandle, constrainedStatus );
		}
	}

	/**
	 * This method will create a new {@link TimeAdvanceGrant} for the federate with the given
	 * federate handle and will queue it for later processing. Note that TAG messages will only
	 * be released from the queue at the appropriate time, so the amont of time between when this
	 * message is queued and when it is released could be significant.
	 * <p/>
	 * This method will queue an advance grant for all federates, regardless of whether they are
	 * local or not.
	 * 
	 * @param federateHandle Handle of the federate that is being advanced
	 * @param status The TimeStatus object representing the federates time status.
	 */
	private void advanceFederate( int federateHandle, TimeStatus status )
	{
		// mark the federate as being able to advance
		status.advanceFederate();

		// queue a callback
		TimeAdvanceGrant grant = new TimeAdvanceGrant( status.getRequestedTime() );
		queueUnicast( grant, federateHandle );

		// log the message about the advance
		if( logger.isDebugEnabled() )
		{
    		logger.debug( "ADVANCE Granted notification queued for federate [%s] to time [%f]",
    		              moniker(federateHandle),
    		              status.getRequestedTime() );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
