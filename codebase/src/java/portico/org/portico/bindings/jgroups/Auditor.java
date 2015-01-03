/*
 *   Copyright 2015 The Portico Project
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
package org.portico.bindings.jgroups;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.portico.lrc.LRC;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.management.Federate;
import org.portico.utils.messaging.PorticoMessage;

/**
 * This class is used to log metadata about incoming/outgoing messages so that
 * they can be audited later. This information is typically used to support decisions
 * about configuration tweaks for performance tuning and to get an insight into
 * how information is flowing through a federation.
 * 
 * The class wraps a simple Log4j logger that writes its information to a separate
 * file from the main log so that is can be audited and followed without the additional
 * noise of all the other goings on inside the LRC.
 * 
 * By default the Auditor is turned off, requiring enabling in the RID file.
 */
public class Auditor
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean enabled;
	private Logger logger;
	private String receivedFormat;
	private String sentFormat;
	
	private String federationName;
	private String federateName;
	private LRC lrc;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Auditor()
	{
		this.enabled = false;
		this.logger = null;
		this.federationName = "";
		this.federateName = "";
		
		String timeFormat = "%1tH:%<tM:%<tS.%<tL";
		this.receivedFormat = "received "+timeFormat+" %30s  %5d   from %s";
		this.sentFormat     = "    sent "+timeFormat+" %30s  %5d   %s";
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public final boolean isEnabled()
	{
		return this.enabled;
	}

	public void sent( PorticoMessage message, int size )
	{
		if( !enabled )
			return;

		// who are we sending this to?
		int targetHandle = message.getTargetFederate();
		String targetName = "";
		if( targetHandle != -1 )
		{
			targetName = "  to "+lrc.getState().getKnownFederate(targetHandle).getFederateName()+
			             "("+targetHandle+")";
		}
		
		String type = message.getClass().getSimpleName();
		String entry = String.format( sentFormat,
		                              new Date(System.currentTimeMillis()),
		                              type,
		                              size,
		                              targetName );
		logger.info( entry );
	}

	public void received( PorticoMessage message, int size )
	{
		if( !enabled )
			return;

		// who sent this?
		int sourceHandle = message.getSourceFederate();
		Federate source = this.lrc.getState().getKnownFederate( sourceHandle );
		String sourceName = "unknown("+sourceHandle+")";
		if( source != null )
			sourceName = source.getFederateName()+"("+sourceHandle+")";
		
		String type = message.getClass().getSimpleName();
		String entry = String.format( receivedFormat,
		                              new Date(System.currentTimeMillis()),
		                              type,
		                              size,
		                              sourceName );
		logger.info( entry );
	}

	///////////////////////////////////////////////////////////////////////////////
	///////////////////////////////  Setup Methods  ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	/**
	 * Enable the auditor and start collecting metadata. At this point in time the log file
	 * will be created under "logs/audit-[federateName].log". 
	 *  
	 * @param federationName Used in an initial log message
	 * @param federateName Used in the audit file name
	 * @param lrc Used to convert federate names to handles for incoming/outgoing messages
	 */
	public void startAuditing( String federationName, String federateName, LRC lrc )
		throws JConfigurationException
	{
		if( this.enabled )
			return;
		
		this.federationName = federationName;
		this.federateName = federateName;
		this.lrc = lrc;
		turnLoggerOn();
		this.enabled = true;
	}

	/**
	 * Create and configure out logger and file appender so that we can start recording.
	 */
	private void turnLoggerOn() throws JConfigurationException
	{
		try
		{
			// log file is in logs/audit-federateName.log
			String logdir = System.getProperty( PorticoConstants.PROPERTY_LOG_DIR, "logs" );
			String logfile = logdir +"/audit-"+this.federateName+".log";

    		// create the appender for the logger
			String pattern = new String( "%m%n" );
    		PatternLayout layout = new PatternLayout( pattern );
    		RollingFileAppender appender = new RollingFileAppender( layout, logfile, true );
    		appender.setMaxBackupIndex( 2 );
    		appender.setMaxFileSize( "10MB" );
    		
    		// attach the appender
    		this.logger = Logger.getLogger( "auditor" );
    		this.logger.addAppender( appender );
		}
		catch( IOException ioex )
		{
			throw new JConfigurationException( "error configuring auditor log: "+ioex.getMessage(), ioex );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
