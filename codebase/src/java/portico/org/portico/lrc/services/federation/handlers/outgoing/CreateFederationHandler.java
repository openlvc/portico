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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.portico.impl.HLAVersion;
import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ModelMerger;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.federation.msg.CreateFederation;
import org.portico.utils.fom.FomParser;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=CreateFederation.class)
public class CreateFederationHandler extends LRCMessageHandler
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
		CreateFederation request = context.getRequest( CreateFederation.class, this );
		
		// list of parsed FOMs
		List<ObjectModel> foms = new ArrayList<ObjectModel>();

		// if we are 1516e, load the standard MIM first - we always want it but only for 1516e
		if( super.lrc.getSpecHelper().getHlaVersion() == HLAVersion.IEEE1516e )
		{
    		// load the standard MIM first - we always want it
    		foms.add( FomParser.parse(get1516eStandardMim()) );
		}
		
		// try and parse each of the fed files that we have
		for( URL module : request.getFomModules() )
			foms.add( FomParser.parse(module) );

		// check to make sure we have the standard MIM as well - if not, load it
		validateStandardMimPresent( foms );

		// merge all the modules together and store the grand unified fom!
		request.setModel( ModelMerger.merge(foms) );
		
		// check that we don't have a null name
		if( request.getFederationName() == null )
			throw new JRTIinternalError( "Can't create a federation with null name" );
		
		// log the request and pass it on to the connection
		logger.debug( "ATTEMPT Create federation execution [" + request.getFederationName() + "]" );
		connection.createFederation( request );
		context.success();
		logger.info( "SUCCESS Created federation execution [" + request.getFederationName() + "]" );
	}

	private void validateStandardMimPresent( List<ObjectModel> foms ) throws Exception
	{
		boolean found = false;
		for( ObjectModel model : foms )
		{
			if( model.getPrivilegeToDelete() != -1 )
			{
				found = true;
				break;
			}
		}
		
		if( found == false )
		{
			logger.debug( "Standard MIM not present - adding it" );
			URL mim = ClassLoader.getSystemResource( "etc/ieee1516e/HLAstandardMIM.xml" );
			foms.add( 0, FomParser.parse(mim) );
		}
	}

	private URL get1516eStandardMim()
	{
		return ClassLoader.getSystemResource( "etc/ieee1516e/HLAstandardMIM.xml" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
