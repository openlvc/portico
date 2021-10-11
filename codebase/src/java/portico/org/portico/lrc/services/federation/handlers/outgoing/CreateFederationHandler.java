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
	private static final String MIM_PATH = "etc/ieee1516e/HLAstandardMIM.xml";
	
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
	
		// we always start with the MIM as the base
		//URL mimModule = ClassLoader.getSystemResource( "" )

		//
		// 1. Try and parse each of the fed files that we have
		//
		List<ObjectModel> foms = new ArrayList<ObjectModel>();
		// For 1516e, we always add the MIM first
		if( lrc.getSpecHelper().getHlaVersion() != HLAVersion.HLA13 )
			foms.add( FomParser.parse(ClassLoader.getSystemResource(MIM_PATH)) );
		
		// Load all the provided modules
		for( URL module : request.getFomModules() )
			foms.add( FomParser.parse(module) );
		
		// -- NOT DONE ANY MORE --
		// Used to be important, but we will manually insert the MOM with handles we can control
		// check to make sure we have the standard MIM as well - if not, load it
		// validateStandardMimPresent( foms );

		// merge the modules together
		ObjectModel combinedFOM = ModelMerger.merge( foms );
		
		// dump out the MIM if it is present and then re-insert with specific handles
//		ObjectModel.mommify( combinedFOM );

		// we have our grand unified FOM!
		request.setModel( combinedFOM );
		
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

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
