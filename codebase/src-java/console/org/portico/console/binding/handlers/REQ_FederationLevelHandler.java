/*
 *   Copyright 2006 The Portico Project
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
package org.portico.console.binding.handlers;

import java.util.HashSet;
import java.util.Set;

import com.lbf.commons.messaging.MessageContext;
import com.lbf.commons.messaging.MessagingException;
import com.lbf.commons.messaging.RequestMessage;
import com.lbf.commons.messaging.ExtendedSuccessMessage;
import org.portico.console.shared.msg.CONSOLE_GetFederateNames;
import org.portico.console.shared.msg.CONSOLE_GetFederationInfo;
import org.portico.console.shared.msg.CONSOLE_IsFederation;
import org.portico.console.shared.msg.CONSOLE_TerminateFederate;
import org.portico.console.shared.msg.CONSOLE_TerminateFederation;
import org.portico.core.fedex.Federate;
import org.portico.core.fedex.Federation;

/**
 * This class handles requests to retrieve information about a particular federation
 */
public class REQ_FederationLevelHandler extends ConsoleMessageHandler
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
	public REQ_FederationLevelHandler()
	{
		super ("REQ_FederationLevelHandler");
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void processMessage( MessageContext context ) throws Exception
	{
		RequestMessage theRequest = context.getRequest();
		
		// Select which helper method will process the call
		if ( theRequest instanceof CONSOLE_GetFederationInfo )
		{
			this.processGetFederationInfo( context );
		}
		else if (theRequest instanceof CONSOLE_GetFederateNames)
		{
			this.processGetFederateNames( context );
		}
		else if (theRequest instanceof CONSOLE_IsFederation)
		{
			this.processIsFederation( context );
		}
		else if (theRequest instanceof CONSOLE_TerminateFederation )
		{
			this.processTerminateFederation( context );
		}
		else
		{
			// if the parsed message isn't handled then throw an error
			this.error( context, new MessagingException("Unknown message type: " 
			                                            + context.getRequest().getClass()));
		}
	}
	
	private void processGetFederationInfo( MessageContext context) throws Exception
	{
		// validate the message type
		CONSOLE_GetFederationInfo request 
			= context.getRequest( CONSOLE_GetFederationInfo.class, this );
		
		
		String requestedFederationName = request.getFederationName();
		
		// If there was no federation name supplied in the request
		if ( requestedFederationName == null )
		{
			// Throw back an appropriate exception
			this.error( context, new MessagingException("Federation name is NULL") );
			return;
		}
		
		Federation requestedFederation 
			= this.getRtiExecution().getFederation( requestedFederationName );
		
		// If the requested federation exists
		if ( requestedFederation != null )
		{
		
			// Create a success message in response		
			ExtendedSuccessMessage success = new ExtendedSuccessMessage();
			
			// Populate the properties of the success
			success.getProperties().put( CONSOLE_GetFederationInfo.KEY_FEDERATION_NAME, 
			                             requestedFederation.getName());
			success.getProperties().put( CONSOLE_GetFederationInfo.KEY_FEDERATION_LBTS, 
			                             requestedFederation.getState().getLBTS());
	
					
			// Set the success message as the response of the message context
			context.setResponse( success );
		}
		else
		{
			// otherwise set an error message to say that the requested federation
			// does not exist
			this.error( context, new MessagingException("Federation does not exist: " 
			                                    + requestedFederationName ) );
		}
	}
	
	private void processGetFederateNames( MessageContext context ) throws Exception
	{
		// validate the message type
		CONSOLE_GetFederateNames request 
			= context.getRequest( CONSOLE_GetFederateNames.class, this );
		
		String requestedFederationName = request.getFederationName();
		
		// If there was no federation name supplied in the request
		if ( requestedFederationName == null )
		{
			// Throw back an appropriate exception
			this.error( context, new MessagingException("Federation name is NULL") );
			return;
		}
		
		Federation requestedFederation 
			= this.getRtiExecution().getFederation( requestedFederationName );
		
		// If the requested federation exists
		if ( requestedFederation != null )
		{
			// Get a list of all of the federates running in this federation
			Set<Federate> federateCollection = requestedFederation.getAllFederates();
			Set<String> federateNames = new HashSet<String>();
	
			// Iterate through all of the federates
			for (Federate currentFederate : federateCollection)
			{
				// add the current federate's name to the list
				federateNames.add( currentFederate.getName() );
			}
			
			// Set the response to a success message with the result
			this.success( context, federateNames );

		}
		else
		{
			// otherwise set an error message to say that the requested federation
			// does not exist
			this.error( context, new MessagingException("Federation does not exist: " 
			                                    + requestedFederationName ) );
		}
	}
	
	private void processIsFederation( MessageContext context ) throws Exception
	{
		// validate the message type
		CONSOLE_IsFederation request 
			= context.getRequest( CONSOLE_IsFederation.class, this );
		
		String requestedFederationName = request.getFederationName();
		
		// If there was no federation name supplied in the request
		if ( requestedFederationName == null )
		{
			// Throw back an appropriate exception
			this.error( context, new MessagingException("Federation name is NULL") );
			return;
		}
		
		Federation requestedFederation 
			= this.getRtiExecution().getFederation( requestedFederationName );
		
		this.success( context, (requestedFederation != null) );
	}
	
	private void processTerminateFederation( MessageContext context ) throws Exception
	{
		// validate the message type
		CONSOLE_TerminateFederation request 
			= context.getRequest( CONSOLE_TerminateFederation.class, this );
		
		String requestedFederationName = request.getFederationName();
		
		// If there was no federation name supplied in the request
		if ( requestedFederationName == null )
		{
			// Throw back an appropriate exception
			this.error( context, 
			            new MessagingException("Federation Name is NULL") );
			return;
		}
				
		Federation requestedFederation 
			= this.getRtiExecution().getFederation( requestedFederationName );
		
		if (requestedFederation == null)
		{
			context.error( "Requested federation " + requestedFederationName + " does not exist" );
			return;
		}
		
		Set<Federate> theFederates = requestedFederation.getAllFederates();
		int noOfFederates = theFederates.size();
		
		for (Federate joinedFederate : theFederates )
		{
			CONSOLE_TerminateFederate forwardRequest = new CONSOLE_TerminateFederate();
			forwardRequest.setFederationName(requestedFederationName);
			forwardRequest.setFederateName(joinedFederate.getName());
			
			MessageContext forwardContext = new MessageContext(forwardRequest);
			
			this.consoleRequestSink.processMessage(forwardContext);
			
			if (!forwardContext.isSuccessResponse())
			{
				context.setResponse(forwardContext.getErrorResponse());
				return;
			}
		}
		
		this.rtiExecution.destroyFederationExecution(requestedFederationName);
		
		this.success(context, "Federation " + requestedFederationName + " removed successfully. " +
		             noOfFederates + " federate(s) affected.");
		
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}


