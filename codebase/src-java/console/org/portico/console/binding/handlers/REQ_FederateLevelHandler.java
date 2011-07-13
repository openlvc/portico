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

import com.lbf.commons.messaging.MessageContext;
import com.lbf.commons.messaging.MessagingException;
import com.lbf.commons.messaging.RequestMessage;
import com.lbf.commons.messaging.ExtendedSuccessMessage;
import org.portico.console.shared.msg.CONSOLE_GetFederateInfo;
import org.portico.console.shared.msg.CONSOLE_IsFederate;
import org.portico.console.shared.msg.CONSOLE_TerminateFederate;
import org.portico.core.fedex.Federate;
import org.portico.core.fedex.FederateState;
import org.portico.core.fedex.Federation;
import org.portico.shared.msg.EXT_Terminate;

/**
 * This class handles requests to retrieve information about a particular federation
 */
public class REQ_FederateLevelHandler extends ConsoleMessageHandler
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
	public REQ_FederateLevelHandler()
	{
		super ("REQ_FederateLevelHandler");
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void processMessage( MessageContext context ) throws Exception
	{
		RequestMessage theRequest = context.getRequest();
		
		// Select which helper method will process the call
		if ( theRequest instanceof CONSOLE_GetFederateInfo )
		{
			this.processGetFederateInfo( context );
		}
		else if ( theRequest instanceof CONSOLE_IsFederate )
		{
			this.processIsFederate( context );
		}
		else if ( theRequest instanceof CONSOLE_TerminateFederate )
		{
			this.processTerminateFederate( context );
		}
		else
		{
			// if the parsed message isn't handled then throw an error
			this.error( context, new MessagingException("Unknown message type: " 
			                                            + context.getRequest().getClass()));
		}
	}
	
	private void processGetFederateInfo( MessageContext context) throws Exception
	{
		// validate the message type
		CONSOLE_GetFederateInfo request 
			= context.getRequest( CONSOLE_GetFederateInfo.class, this );
		
		
		String requestedFederationName = request.getFederationName();
		String requestedFederateName = request.getFederateName();
		
		// If there was no federation name supplied in the request
		if ( requestedFederationName == null  || requestedFederateName == null)
		{
			// Throw back an appropriate exception
			this.error( context, 
			            new MessagingException("Federation Name or Federate Name is NULL") );
			return;
		}
		
		Federation requestedFederation 
			= this.getRtiExecution().getFederation( requestedFederationName );
		
		// If the requested federation exists
		if ( requestedFederation != null )
		{
			Federate requestedFederate = requestedFederation.getFederate( requestedFederateName );
			
			// If the requested federate exists
			if ( requestedFederate != null )
			{
				ExtendedSuccessMessage success = new ExtendedSuccessMessage();
				FederateState requestedState = requestedFederate.getState();

				success.getProperties().put( CONSOLE_GetFederateInfo.KEY_ADVANCING, 
				                             requestedState.isAdvancing() );
				success.getProperties().put( CONSOLE_GetFederateInfo.KEY_CONSTRAINED, 
				                             requestedState.isConstrained() );
				success.getProperties().put( CONSOLE_GetFederateInfo.KEY_CURRENT_TIME,
				                             requestedState.getCurrentTime() );
				success.getProperties().put( CONSOLE_GetFederateInfo.KEY_HANDLE,
				                             requestedFederate.getHandle() );
				success.getProperties().put( CONSOLE_GetFederateInfo.KEY_LOOK_AHEAD,
				                             requestedState.getLookahead() );
				success.getProperties().put( CONSOLE_GetFederateInfo.KEY_NAME,
				                             requestedFederate.getName() );
				success.getProperties().put( CONSOLE_GetFederateInfo.KEY_REGULATING,
				                             requestedState.isRegulating() );
				
				context.setResponse( success );
				
			}
			else
			{
				// otherwise set an error message to say that the requested federate
				// does not exist
				this.error( context, new MessagingException("Federate does not exist: " 
				                                    + requestedFederateName ) );
			}
		}
		else
		{
			// otherwise set an error message to say that the requested federation
			// does not exist
			this.error( context, new MessagingException("Federation does not exist: " 
			                                    + requestedFederationName ) );
		}
	}
	
	private void processIsFederate( MessageContext context) throws Exception
	{
		// validate the message type
		CONSOLE_IsFederate request 
			= context.getRequest( CONSOLE_IsFederate.class, this );
		
		
		String requestedFederationName = request.getFederationName();
		String requestedFederateName = request.getFederateName();
		
		// If there was no federation name supplied in the request
		if ( requestedFederationName == null  || requestedFederateName == null)
		{
			// Throw back an appropriate exception
			this.error( context, 
			            new MessagingException("Federation Name or Federate Name is NULL") );
			return;
		}
		
		Federation requestedFederation 
			= this.getRtiExecution().getFederation( requestedFederationName );
		
		// If the requested federation exists
		if ( requestedFederation != null )
		{
			Federate requestedFederate = requestedFederation.getFederate( requestedFederateName );
			
			// If the requested federate exists
			if ( requestedFederate != null )
			{
				this.success( context, true );
				
			}
			else
			{
				this.success( context, false );
			}
		}
		else
		{
			this.success( context, false );
		}
	}
	
	private void processTerminateFederate( MessageContext context) throws Exception
	{
		// validate the message type
		CONSOLE_TerminateFederate request 
			= context.getRequest( CONSOLE_TerminateFederate.class, this );
		
		String requestedFederationName = request.getFederationName();
		String requestedFederateName = request.getFederateName();
		
		// If there was no federation name supplied in the request
		if ( requestedFederationName == null  || requestedFederateName == null)
		{
			// Throw back an appropriate exception
			this.error( context, 
			            new MessagingException("Federation Name or Federate Name is NULL") );
			return;
		}
				
		Federation requestedFederation 
			= this.getRtiExecution().getFederation( requestedFederationName );
		
		// If the requested federation exists
		if ( requestedFederation != null )
		{
			Federate requestedFederate = requestedFederation.getFederate(requestedFederateName);
			
			// If the requested federate exists within the requested federation
			if (requestedFederate != null)
			{
				// build a terminate message
				EXT_Terminate messageToForward = new EXT_Terminate();
				messageToForward.setTargetFederate(requestedFederateName);
				
				MessageContext contextToForward = new MessageContext(messageToForward);
				
				// put the message onto the federation's request sink for processing
				requestedFederation.getRequestSink().processMessage(contextToForward);
				
				// if the removal was successful
				if (contextToForward.isSuccessResponse())
				{
					// set the response to send back to be a success
					this.success(context, "Federate " + requestedFederationName + "." 
							+ requestedFederateName + " removed successfully");
				}
				else
				{
					// otherwise if there was a failure, provide an appropriate error message
					this.error(context, new MessagingException("Federate " + requestedFederationName 
							+ "." + requestedFederateName + " could not be removed"));
				}
			}
			else
			{
				// notify the user that they have given an invalid federate name
				this.error(context, new MessagingException("The federate '" + requestedFederateName 
						+ "' is not a member of the federation '" + requestedFederationName + "'"));
			}
		}
		else
		{
			// notify the user that they have given an invalid federation name
			this.error(context,
					new MessagingException("The federation '" 
						+ requestedFederationName + "' does not exist") );
		}
		
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}


