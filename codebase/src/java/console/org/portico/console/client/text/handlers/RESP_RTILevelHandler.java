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

import java.util.Date;
import java.util.Set;

import com.lbf.commons.messaging.AbstractMessageHandler;
import com.lbf.commons.messaging.ExceptionMessage;
import com.lbf.commons.messaging.MessageContext;
import com.lbf.commons.messaging.MessagingException;
import com.lbf.commons.messaging.RequestMessage;
import com.lbf.commons.messaging.ResponseMessage;
import com.lbf.commons.messaging.ExtendedSuccessMessage;
import com.lbf.commons.utils.Bag;
import org.portico.console.shared.msg.CONSOLE_GetFederationNames;
import org.portico.console.shared.msg.CONSOLE_GetRTIInfo;

/** 
 * A handler to output RTI related console response messages
 */
public class RESP_RTILevelHandler extends AbstractMessageHandler 
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
	public RESP_RTILevelHandler() 
	{
		super("RESP_RTILevelHandler");
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
			if ( initialRequest instanceof CONSOLE_GetRTIInfo )
			{
				processGetRTIInfoResponse(success);
			}
			else if ( initialRequest instanceof CONSOLE_GetFederationNames )
			{
				processGetFederationNamesResponse(success);
			}
			else
			{
				// if the parsed message isn't handled then throw an error
				throw new MessagingException("Unknown message type: " 
				                             + context.getRequest().getClass());
			}
		}
	}
	
	private void processGetRTIInfoResponse(ExtendedSuccessMessage success)
	{
		// Get properties of interest and display them on the console
		Bag<String, Object> properties = success.getProperties();
		System.out.println("CPU Count:\t" 
				+ properties.get(CONSOLE_GetRTIInfo.KEY_CPU_COUNT));
		System.out.println("IP Address:\t" 
				+ properties.get(CONSOLE_GetRTIInfo.KEY_IP_ADDRESS));
		System.out.println("Java Vendor:\t" 
				+ properties.get(CONSOLE_GetRTIInfo.KEY_JAVA_VENDOR));
		System.out.println("Java Version:\t" 
				+ properties.get(CONSOLE_GetRTIInfo.KEY_JAVA_VERSION));
		System.out.println("Launch Dir:\t" 
				+ properties.get(CONSOLE_GetRTIInfo.KEY_LAUNCH_DIR));
		System.out.println("OS:\t\t" 
				+ properties.get(CONSOLE_GetRTIInfo.KEY_OS));
		System.out.println("OS Version:\t" 
				+ properties.get(CONSOLE_GetRTIInfo.KEY_OS_VERSION));
		System.out.println("Platform:\t" 
				+ properties.get(CONSOLE_GetRTIInfo.KEY_PLATFORM));
		
		long startTime = (Long)properties.get(CONSOLE_GetRTIInfo.KEY_START_TIME);
				
		System.out.println("Start Time:\t" + new Date(startTime).toString() );
	}
	
	@SuppressWarnings("unchecked")
	private void processGetFederationNamesResponse(ExtendedSuccessMessage success)
	{
		// Get properties of interest and display them on the console
		Set federationNames = (Set)success.getResult();

		System.out.println("./");
		System.out.println("../");
		
		for (Object federationName : federationNames)
		{
			System.out.println(federationName + "/");
		}
		
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
