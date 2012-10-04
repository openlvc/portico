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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JCouldNotOpenFED;
import org.portico.lrc.compat.JErrorReadingFED;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ModelMerger;
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
		
		// try and parse each of the fed files that we have
		List<ObjectModel> foms = new ArrayList<ObjectModel>();
		for( URL module : request.getFomModules() )
			foms.add( parseFed(module) );
		
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
	
	/**
	 * This method will check what format the fed file is in and then will parse it into an
	 * {@link ObjectModel} or throw an exception if there is a problem. This will work with
	 * either HLA 1.3 files, IEEE1516-2000 XML-based files or IEEE1516e-2010 XML-based files. 
	 * Thus, both types are supported through either interface.
	 */
	private ObjectModel parseFed( URL fedLocation ) throws Exception
	{
		//////////////////////////////////////////////
		// validate file exists and open to peek at // 
		//////////////////////////////////////////////
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

		/////////////////////////////////////////////////////////////////////////
		// read the first few lines into a buffer so we can use them to figure //
		// out whether we are 1516 or 1516e                                    //
		/////////////////////////////////////////////////////////////////////////
		StringBuilder builder = new StringBuilder();
		try
		{
			// read the first few lines from the location into the buffer
			InputStream stream = fedLocation.openStream();
			BufferedReader reader = new BufferedReader( new InputStreamReader(stream) );
			for( int i = 0; i < 5; i++ )
			{
				String line = reader.readLine();
				// ignore non-interesting lines or comment lines
				while( line.trim().equals("") || line.trim().startsWith("<!") )
					line = reader.readLine();
				
				builder.append( line );
			}
		}
		catch( Exception e )
		{
			throw new JErrorReadingFED( e.getMessage(), e );
		}
		
		// direct the FOM to the approrpiate parser
		String fomheader = builder.toString();
		if( fomheader.startsWith("<?xml") == false )
		{
			// parse with the HLA 1.3 parser
			logger.debug( "Parsing FED file (format=hla13): " + fedLocation );
			return org.portico.impl.hla13.fomparser.FOM.parseFOM( fedLocation );
		}

		////////////////////////////////////////////////////
		// parse the FOM with the approrpiate 1516 parser //
		////////////////////////////////////////////////////
		// we must have one of the 1516 types, but which?
		if( fomheader.contains("IEEE1516-2010") || fomheader.contains("<modelIdentification>") )
		{
			// if its a 1516-2010 (Evolved) FOM
			logger.debug( "Parsing FED file (format=ieee1516e): " + fedLocation );
			return org.portico.impl.hla1516e.fomparser.FOM.parseFOM( fedLocation );
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
