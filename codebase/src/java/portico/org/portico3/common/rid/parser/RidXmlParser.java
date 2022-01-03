/*
 *   Copyright 2021 The Portico Project
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
package org.portico3.common.rid.parser;

import java.io.File;
import java.util.Properties;

import org.portico3.common.compatibility.JConfigurationException;
import org.portico3.common.rid.LogSettings;
import org.portico3.common.rid.RID;
import org.portico3.common.rid.RtiSettings;
import org.portico3.common.rid.connection.ConnectionSettings;
import org.portico3.common.utils.XmlParser;
import org.portico3.common.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Read a RID file in XML format and populate a {@link RID} instance from the values provided.
 */
public class RidXmlParser
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private File file;
	private Document document;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RidXmlParser( File path )
	{
		this.file = path;
		this.document = null;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public RID parse() throws JConfigurationException
	{
		////////////////////////////////////////////
		// 1. Parse the file into an XML document // 
		////////////////////////////////////////////
		try
		{
			this.document = new XmlParser().parse( file );
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "RID file could not be parsed. ["+
			                                   file.getAbsolutePath()+"] "+e.getMessage(), e );
		}

		///////////////////////////////
		// 2. Create a new empty RID //
		///////////////////////////////
		RID rid = new RID();
		
		////////////////////////////////////////////
		// 3. Parse the major sections of the RID //
		////////////////////////////////////////////
		Element documentElement = document.getDocumentElement();
		if( documentElement.getTagName().equals("rid") == false )
			throw new JConfigurationException( "RID file must start with <rid> element: "+file.getPath() );
		
		// Parse and extract the logging section
		Element logElement = XmlUtils.getFirstChildWithTag( documentElement, "logging" );
		parseLogSettings( rid, logElement );
		
		// Parse and extract the RTI settings
		Element rtiElement = XmlUtils.getFirstChildWithTag( documentElement, "rti" );
		parseRtiSettings( rid, rtiElement );
		
		// Parse and extract the LRC settings
		Element lrcElement = XmlUtils.getFirstChildWithTag( documentElement, "lrc" );
		parseLrcSettings( rid, lrcElement );
		

		/////////////////////////////////
		// 4. Return the completed RID //
		/////////////////////////////////
		return rid;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/// Logging Settings   /////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private void parseLogSettings( RID rid, Element logElement )
	{
		LogSettings settings = rid.getLogSettings();
		
		String loglevel = XmlUtils.getFirstChildValue( logElement, "loglevel" );
		if( loglevel != null )
			settings.setLogLevel( loglevel );
		
		String logdir = XmlUtils.getFirstChildValue( logElement, "logdir" );
		if( logdir != null )
			settings.setLogDir( logdir );
		
		String printFom = XmlUtils.getFirstChildValue( logElement, "printFom" );
		if( printFom != null )
			settings.setPrintFom( Boolean.valueOf(printFom) );
		
		// Get the log-with options
		Element logWithElement = XmlUtils.getFirstChildWithTag( logElement, "logWith" );
		String objectClass = XmlUtils.getAttributeOrDefault( logWithElement, "objectClass", "handle" );
		String attributeClass = XmlUtils.getAttributeOrDefault( logWithElement, "attributeClass", "handle" );
		String interactionClass = XmlUtils.getAttributeOrDefault( logWithElement, "interactionClass", "handle" );
		String parameterClass = XmlUtils.getAttributeOrDefault( logWithElement, "parameterClass", "handle" );
		String objectInstance = XmlUtils.getAttributeOrDefault( logWithElement, "objectInstance", "name" );
		String space = XmlUtils.getAttributeOrDefault( logWithElement, "space", "handle" );
		String dimension = XmlUtils.getAttributeOrDefault( logWithElement, "dimension", "handle" );
		String federate = XmlUtils.getAttributeOrDefault( logWithElement, "federate", "name" );
		
		settings.setLogHandlesForObjectClass( objectClass.equalsIgnoreCase("handle") );
		settings.setLogHandlesForAttributeClass( attributeClass.equalsIgnoreCase("handle") );
		settings.setLogHandlesForInteractionClass( interactionClass.equalsIgnoreCase("handle") );
		settings.setLogHandlesForParameterClass( parameterClass.equalsIgnoreCase("handle") );
		settings.setLogHandlesForObjects( objectInstance.equalsIgnoreCase("handle") );
		settings.setLogHandlesForSpaces( space.equalsIgnoreCase("handle") );
		settings.setLogHandlesForDimensions( dimension.equalsIgnoreCase("handle") );
		settings.setLogHandlesForFederates( federate.equalsIgnoreCase("federate") );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// RTI Configuration Settings   ///////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private void parseRtiSettings( RID rid, Element rtiElement ) throws JConfigurationException
	{
		//
		// Parse the HLA options
		//
		Element options = XmlUtils.getFirstChildWithTag( rtiElement, "options" );
		if( options != null )
			parseRtiOptions( rid, options );

		//
		// Parse the RTI Connections
		//
		Element connections = XmlUtils.getFirstChildWithTag( rtiElement, "connections" );
		if( connections == null )
			throw new JConfigurationException( "RID is missing <connections> element of <rti>" );

		for( Element connection : XmlUtils.getChildElements(connections) )
		{
			Properties properties = XmlUtils.getChildrenAsProperties( connection );
			ConnectionSettings settings = new ConnectionSettings( connection.getTagName(),
			                                                      properties );
			
			rid.getRtiSettings().addConnectionSettings( settings );
		}
	}

	private void parseRtiOptions( RID rid, Element optionsElement )
	{
		RtiSettings rti = rid.getRtiSettings();
		
		// MOM Settings
		boolean momEnabled = true;
		Element momElement = XmlUtils.getFirstChildWithTag( optionsElement, "mom" );
		if( momElement != null )
			momEnabled = Boolean.valueOf( XmlUtils.getFirstChildValue(momElement,"enabled") );
		rti.setMomEnabled( momEnabled );
		
		// Save/Restore Directory
		Element srElement = XmlUtils.getFirstChildWithTag( optionsElement, "save-restore" );
		if( srElement != null )
			rti.setSaveDirectory( XmlUtils.getFirstChildValue(srElement,"directory") );
		
		// Unsupported Exceptions
		String exceptions = XmlUtils.getFirstChildValue( optionsElement, "unsupportedExceptions" );
		if( exceptions != null )
			rti.setUnsupportedExceptions( Boolean.valueOf(exceptions) );
		
		// Unique Names
		String uniqueNames = XmlUtils.getFirstChildValue( optionsElement, "uniqueFederateNames" );
		if( uniqueNames != null )
			rti.setUniqueFederateNames( Boolean.valueOf(uniqueNames) );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// LRC Configuration Settings   ///////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private void parseLrcSettings( RID rid, Element rtiElement )
	{
		
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
