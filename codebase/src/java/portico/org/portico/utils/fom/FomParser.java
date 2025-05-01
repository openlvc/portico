/*
 *   Copyright 2012 The Portico Project
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
package org.portico.utils.fom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JCouldNotOpenFED;
import org.portico.lrc.compat.JErrorReadingFED;
import org.portico.lrc.model.ObjectModel;

/**
 * This class is a wrapper for the various specific-specific FOM parsers. It will open and
 * look at the given URL representing a FOM, determine the appropriate parser for its format
 * and then parse it into an {@link ObjectModel}. See {@link #parse(URL)}. 
 */
public class FomParser
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final FomParser INSTANCE = new FomParser();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private FomParser()
	{
		this.logger = LogManager.getFormatterLogger( "portico.lrc.fom" );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private ObjectModel parseFom( URL fedLocation )
		throws JCouldNotOpenFED, JErrorReadingFED
	{
		//////////////////////////////////////////////
		// validate file exists and open to peek at // 
		//////////////////////////////////////////////
		InputStream fedstream = openStream( fedLocation );

		/////////////////////////////////////////////////////////////////////////
		// read the first few lines into a buffer so we can use them to figure //
		// out whether we are 1516 or 1516e                                    //
		/////////////////////////////////////////////////////////////////////////
		StringBuilder builder = new StringBuilder();
		try
		{
			// read the first few lines from the location into the buffer
			BufferedReader reader = new BufferedReader( new InputStreamReader(fedstream) );
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
		finally
		{
			try{ fedstream.close(); }catch( Exception e ){ /* ignore for now*/ }
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

	private InputStream openStream( URL location ) throws JCouldNotOpenFED, JErrorReadingFED 
	{
		// make sure the file exists
		if( location == null )
			throw new JCouldNotOpenFED( "Fed file doesn't exist: file="+null );

		try
		{
			return location.openStream();
		}
		catch( IOException ioex )
		{
			throw new JCouldNotOpenFED( "Error opening fed file from ["+location+
			                            "]: "+ioex.getMessage(), ioex );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * There are a bunch of FOM parsers present in Portico for the various formats associated
	 * with the iterations of the standard. This method will try to open the given URL and
	 * interrogate it to determine which format it is in, and thus, which FOM parser should be
	 * used to read it. If this works happily, the FOM will be parsed and a populated instance
	 * of {@link ObjectModel} will be returned.
	 * <p/>
	 * This method will check what format the fed file is in and then will parse it into an
	 * {@link ObjectModel} or throw an exception if there is a problem. This will work with
	 * either HLA 1.3 files, IEEE1516-2000 XML-based files or IEEE1516e-2010 XML-based files. 
	 * Thus, both types are supported through either interface.
	 * 
	 * @param fedLocation The location of the FED file to parse.
	 * @return An {@link ObjectModel} instance containing the parsed contents of the given URL
	 * @throws JCouldNotOpenFED Could not open the fed file at the provided URL
	 * @throws JErrorReadingFED The FOM was either in an invalid format or contained errors
	 */
	public static ObjectModel parse( URL fedLocation )
		throws JCouldNotOpenFED, JErrorReadingFED
	{
		return new FomParser().parseFom( fedLocation );
	}
}
