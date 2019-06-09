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
package org.portico.lrc.services.federation.handlers.outgoing;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JFederateOwnsAttributes;
import org.portico.lrc.compat.JResignAction;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.federation.msg.ResignFederation;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=ResignFederation.class)
public class ResignFederationHandler extends LRCMessageHandler
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
		ResignFederation request = context.getRequest( ResignFederation.class, this );
		
		lrcState.checkJoined();
		
		// set the federate and federation name
		String federateName = lrcState.getFederateName();
		String federateType = lrcState.getFederateType();
		String federationName = lrcState.getFederationName();
		request.setFederateName( federateName );
		request.setFederateType( federateType );
		request.setFederationName( federationName );
		
		// log the request and pass it on to the connection
		logger.debug( "ATTEMPT Resign federate ["+federateName+
		              "] from federation ["+federationName+
		              "]: action="+request.getResignAction() );

		// validate that we are in the proper state for the given resign action
		validateResignAction( request.getResignAction(), lrcState.getFederateHandle() );

		// send the resign notification to the connection and the federation
		connection.resignFederation( request );
		
		// notify the notification manager
		notificationManager.localFederateResignedFromFederation();
		context.success();
		
		logger.info( "SUCCESS Resigned federate ["+federateName+
		             "] from federation ["+federationName+
		             "]: action="+request.getResignAction() );
	}
	
	private void validateResignAction( JResignAction action, int federate ) throws JException
	{
		// if the resign action is NO_ACTION make sure we don't own any attributes
		if( action == JResignAction.NO_ACTION )
		{
			/////////////////////////
			// ACTION == NO_ACTION //
			/////////////////////////
			for( OCInstance instance : repository.getAllInstances() )
			{
				Set<ACInstance> owned = instance.getAllOwnedAttributes( federate );
				if( owned.isEmpty() )
					continue;
				
				Set<Integer> handles = new HashSet<Integer>();
				for( ACInstance attribute : owned )
					handles.add( attribute.getHandle() );
				
				throw new JFederateOwnsAttributes( "Can't resign, federate ["+moniker()+
				                                   "] owns attributes "+acMoniker(handles)+
				                                   " for object ["+instance.getHandle()+"]" );
			}
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
