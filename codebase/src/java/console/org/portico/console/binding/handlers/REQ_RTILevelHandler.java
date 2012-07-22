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
import com.lbf.commons.messaging.ExtendedSuccessMessage;
import com.lbf.commons.utils.SystemInformation;
import org.portico.console.shared.msg.CONSOLE_GetFederationNames;
import org.portico.console.shared.msg.CONSOLE_GetRTIInfo;
import org.portico.core.fedex.Federation;

/**
 * This class handles requests to retrieve information about the RTI
 */
public class REQ_RTILevelHandler extends ConsoleMessageHandler
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
	public REQ_RTILevelHandler()
	{
		super ("REQ_RTILevelHandler");
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void processMessage( MessageContext context ) throws Exception
	{
		// Select which helper method will process the call
		if ( context.getRequest() instanceof CONSOLE_GetRTIInfo )
		{
			this.processGetRTIInfo( context );
		}
		else if ( context.getRequest() instanceof CONSOLE_GetFederationNames )
		{
			this.processGetFederationNames( context );
		}
		else
		{
			// if the parsed message isn't handled then throw an error
			this.error( context, new MessagingException("Unknown message type: " 
			                                            + context.getRequest().getClass()));
		}
	}
	
	private void processGetRTIInfo( MessageContext context) throws Exception
	{
		// validate the message type
		context.getRequest( CONSOLE_GetRTIInfo.class, this );
		
		// Create a success message in response		
		ExtendedSuccessMessage success = new ExtendedSuccessMessage();
		
		// Populate the properties of the success
		success.getProperties().put( CONSOLE_GetRTIInfo.KEY_CPU_COUNT, 
		                             SystemInformation.SYSINFO.getCPUCount());
		success.getProperties().put( CONSOLE_GetRTIInfo.KEY_IP_ADDRESS, 
		                             SystemInformation.SYSINFO.getIpAddress());
		success.getProperties().put( CONSOLE_GetRTIInfo.KEY_JAVA_VENDOR, 
		                             SystemInformation.SYSINFO.getJavaVendor());
		success.getProperties().put( CONSOLE_GetRTIInfo.KEY_JAVA_VERSION, 
		                             SystemInformation.SYSINFO.getJavaVersion());
		success.getProperties().put( CONSOLE_GetRTIInfo.KEY_LAUNCH_DIR, 
		                             SystemInformation.SYSINFO.getLaunchDir());
		success.getProperties().put( CONSOLE_GetRTIInfo.KEY_OS, 
		                             SystemInformation.SYSINFO.getOS());
		success.getProperties().put( CONSOLE_GetRTIInfo.KEY_OS_VERSION, 
		                             SystemInformation.SYSINFO.getOSVersion());
		success.getProperties().put( CONSOLE_GetRTIInfo.KEY_PLATFORM, 
		                             SystemInformation.SYSINFO.getPlatform());
		success.getProperties().put( CONSOLE_GetRTIInfo.KEY_START_TIME, 
		                             SystemInformation.SYSINFO.getRawStartupTime());
				
		// Set the success message as the response of the message context
		context.setResponse( success );
	}
	
	private void processGetFederationNames( MessageContext context ) throws Exception
	{
		// validate the message type
		context.getRequest( CONSOLE_GetFederationNames.class, this );
		
		// Get a list of all of the federations running on this RTI
		Set<Federation> federationCollection = this.getRtiExecution().getAllFederations();
		Set<String> federationNames = new HashSet<String>();
	
		// Iterate through all of the federations
		for ( Federation currentFederation : federationCollection )
		{
			// add the current federation's name to the list
			federationNames.add( currentFederation.getName() );
		}
		
		// Set the response to a success message with the result
		this.success( context, federationNames );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}


