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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JCouldNotOpenFED;
import org.portico.lrc.compat.JErrorReadingFED;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.federation.msg.CreateFederation;
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
		
		// try and parse the fed file
		request.setModel( parseFed(request.getFedFileLocation()) );
		
		// check that we don't have a null name
		if( request.getFederationName() == null )
			throw new JRTIinternalError( "Can't create a federation with null name" );
		
		// log the request and pass it on to the connection
		logger.debug( "ATTEMPT Create federation execution [" + request.getFederationName() + "]" );
		connection.createFederation( request );
		context.success();
		logger.info( "SUCCESS Created federation execution [" + request.getFederationName() + "]" );
	}
	
	/**
	 * This method will check what format the fed file is in and then will parse it into an
	 * {@link ObjectModel} or throw an exception if there is a problem. This will work on *either*
	 * HLA 1.3 fed files or IEEE1516 XML-based fed files. Thus, both types are supported through
	 * either interface.
	 */
	private ObjectModel parseFed( URL fedLocation ) throws Exception
	{
		try
		{
    		// make sure the file exists
			if( fedLocation == null )
				throw new JCouldNotOpenFED( "Fed file doesn't exist: file="+null );
			
			// known problem with URL.toURI() and paths with spaces, need to work around that
			File file = null;
			try
			{
	    		file = new File( fedLocation.toURI() );
			}
			catch( URISyntaxException urie )
			{
				file = new File( fedLocation.getPath() );
			}
			
    		if( file.exists() == false )
    			throw new JCouldNotOpenFED( "Fed file doesn't exist: file="+fedLocation );
    		else if( file.canRead() == false )
    			throw new JCouldNotOpenFED( "Can't open fed file for reading: file="+fedLocation );
		}
		catch( Exception e )
		{
			throw new JCouldNotOpenFED( e.getMessage(), e );
		}
		
		boolean hla13 = true;
		try
		{
			// read the first few characters from the location, if it is an XML file, use the
			// 1516 parser, otherwise use the OMT format parser
			InputStream stream = fedLocation.openStream();
			BufferedReader reader = new BufferedReader( new InputStreamReader(stream) );
			String line = reader.readLine();
			while( line.trim().equals("") ) // ignore non-interesting lines
				line = reader.readLine();
			
			if( line.startsWith("<?xml") )
				hla13 = false;
		}
		catch( Exception e )
		{
			throw new JErrorReadingFED( e.getMessage(), e );
		}
		
		if( hla13 )
		{
			// parse with HLA 1.3 parser
			logger.debug( "Parsing FED file (format=hla13): " + fedLocation );
			return org.portico.impl.hla13.fomparser.FOM.parseFOM( fedLocation );
		}
		else
		{
			// parse with 1516 parser
			logger.debug( "Parsing FED file (format=ieee1516): " + fedLocation );
			return org.portico.impl.hla1516.fomparser.FOM.parseFOM( fedLocation );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
