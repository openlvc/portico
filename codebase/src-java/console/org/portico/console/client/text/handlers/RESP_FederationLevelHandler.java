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
package org.portico.console.client.text.handlers;

import java.util.Set;

import com.lbf.commons.messaging.AbstractMessageHandler;
import com.lbf.commons.messaging.ExceptionMessage;
import com.lbf.commons.messaging.MessageContext;
import com.lbf.commons.messaging.MessagingException;
import com.lbf.commons.messaging.RequestMessage;
import com.lbf.commons.messaging.ResponseMessage;
import com.lbf.commons.messaging.ExtendedSuccessMessage;
import com.lbf.commons.utils.Bag;
import org.portico.console.shared.msg.CONSOLE_GetFederateNames;
import org.portico.console.shared.msg.CONSOLE_GetFederationInfo;
import org.portico.console.shared.msg.CONSOLE_TerminateFederation;

/** 
 * A handler to output Federation related console response messages
 */
public class RESP_FederationLevelHandler extends AbstractMessageHandler 
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
	public RESP_FederationLevelHandler() 
	{
		super("RESP_FederationLevelHandler");
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void processMessage(MessageContext context) throws Exception 
	{
		// Get the request and the response to this message context
		RequestMessage initialRequest = context.getRequest();
		ResponseMessage response = context.getResponse();		
		
		// If the response is flagged as an error
		if (response.isError())
		{
			// Get the exception portion of the message
			ExceptionMessage exception = (ExceptionMessage)response;
			
			// Display the error message on the console
			System.out.println("ERROR: " 
					+ exception.getCause().getMessage());
		}
		else
		{
			// Otherwise it's all good, get the success part of the response
			ExtendedSuccessMessage success = (ExtendedSuccessMessage)response;

			// Decide which private handler will take care of the message
			if ( initialRequest instanceof CONSOLE_GetFederationInfo )
			{
				processGetFederationInfoResponse(success);
			}
			else if ( initialRequest instanceof CONSOLE_GetFederateNames )
			{
				processGetFederateNamesResponse(success);
			}
			else if ( initialRequest instanceof CONSOLE_TerminateFederation )
			{
				// Just print out the response for terminatefederation
				System.out.println(success.getResult());
			}
			else
			{
				// if the parsed message isn't handled then throw an error
				throw new MessagingException("Unknown message type: " 
				                             + context.getRequest().getClass());
			}
		}
	}
	
	private void processGetFederationInfoResponse( ExtendedSuccessMessage success )
	{
		// Get properties of interest and display them on the console
		Bag<String, Object> properties = success.getProperties();
		System.out.println("Federation Name:\t" 
				+ properties.get(CONSOLE_GetFederationInfo.KEY_FEDERATION_NAME));
		System.out.println("LBTS:\t\t\t" 
				+ properties.get(CONSOLE_GetFederationInfo.KEY_FEDERATION_LBTS));
	}
	
	@SuppressWarnings("unchecked")
	private void processGetFederateNamesResponse(ExtendedSuccessMessage success)
	{
		// Get properties of interest and display them on the console
		Set federateNames = (Set)success.getResult();

		System.out.println("./");
		System.out.println("../");
		
		for (Object federateName : federateNames)
		{
			System.out.println(federateName + "/");
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
