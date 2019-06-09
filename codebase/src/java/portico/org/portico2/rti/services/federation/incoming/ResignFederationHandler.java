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
package org.portico2.rti.services.federation.incoming;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.compat.JResignAction;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.federation.msg.ResignFederation;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.services.RTIMessageHandler;

public class ResignFederationHandler extends RTIMessageHandler
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
		ResignFederation request = context.getRequest( ResignFederation.class );
		String federateName = request.getFederateName();
		JResignAction action = request.getResignAction();
		
		logger.info( "ATTEMPT Resign federate [%s] from federation [%s] with action [%s]",
		             federateName, federationName(), action );
		
		// Check that the federate is joined
		Federate federate = federation.getFederate( federateName );
		if( federate == null )
		{
			logger.error( "FAILURE Federate [%s] is not part of federation [%s]", federateName, federationName() );
			throw new JFederateNotExecutionMember( "Federate [%s] not a member of federation [%s]",
			                                       federateName, federationName() );
		}
		
		// Check that the resign action is valid for the current state of the federation
		validateResignAction( action, federate );
		
		// Remove the federate
		federation.resignFederate( federate );
		context.success();
		logger.info( "SUCCESS Federate [%s] resigned from federation [%s] with action [%s]",
		             federateName, federationName(), action );

		// Do any house keeping necessary
		timeManager.resignedFederation( federate.getFederateHandle() );
		momManager.resignedFederation( federate );
	}

	private void validateResignAction( JResignAction action, Federate federate ) throws JException
	{
		// if the resign action is NO_ACTION make sure we don't own any attributes
		if( action == JResignAction.NO_ACTION )
		{
			/////////////////////////
			// ACTION == NO_ACTION //
			/////////////////////////
// FIXME - Need to bring this back when object registration is done
//			for( OCInstance instance : repository.getAllInstances() )
//			{
//				Set<ACInstance> owned = instance.getAllOwnedAttributes( federate );
//				if( owned.isEmpty() )
//					continue;
//				
//				Set<Integer> handles = new HashSet<Integer>();
//				for( ACInstance attribute : owned )
//					handles.add( attribute.getHandle() );
//				
//				throw new JFederateOwnsAttributes( "Can't resign, federate ["+moniker()+
//				                                   "] owns attributes "+acMoniker(handles)+
//				                                   " for object ["+instance.getHandle()+"]" );
//			}
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
