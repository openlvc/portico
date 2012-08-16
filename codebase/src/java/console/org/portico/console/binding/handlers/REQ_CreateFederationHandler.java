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

import java.io.ByteArrayInputStream;

import com.lbf.commons.messaging.MessageContext;
import org.portico.console.shared.msg.CONSOLE_CreateFederation;
import org.portico.impl.hla13.fomparser.FOM;

public class REQ_CreateFederationHandler extends ConsoleMessageHandler
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

	public REQ_CreateFederationHandler()
	{
		super( "REQ_CreateFederationHandler" );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void processMessage( MessageContext context )
	{
		try
		{
    		CONSOLE_CreateFederation request =
    			context.getRequest( CONSOLE_CreateFederation.class, this );
    		
    		// turn the byte[] FOM contents into an ObjectModel
    		ByteArrayInputStream inStream = new ByteArrayInputStream( request.getFomContents() );
    		super.rtiExecution.createFederationExecution( request.getFederationName(),
    		                                              FOM.parseFOM(inStream) );
		}
		catch( Exception e )
		{
			context.error( e );
			return;
		}
		
		context.success();
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
