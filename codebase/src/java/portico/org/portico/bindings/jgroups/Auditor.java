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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.jgroups.Version;
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
	private boolean recording;
	private Logger logger;
	private RollingFileAppender appender; // keep a ref so we can close out safely
	private String receivedFormat;
	private String sentFormat;
	
	private String federationName;
	private String federateName;
	private LRC lrc;
	
	// counters and metrics
	private Map<String,MessageMetrics> metrics;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Auditor()
	{
		this.recording = false;
		this.logger = null;
		this.federationName = "";
		this.federateName = "";
		
		String timeFormat = "%1tH:%<tM:%<tS.%<tL";
		this.receivedFormat = "received "+timeFormat+" %30s  %5d   from %s";
		this.sentFormat     = "    sent "+timeFormat+" %30s  %5d   %s";
		
		// counters and metrics
		this.metrics = new HashMap<String,MessageMetrics>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * @return True if the auditor is currently recording content. False if it is not.
	 */
	public final boolean isRecording()
	{
		return this.recording;
	}

	/**
	 * The local federate just sent the given message. When serialized, the message was the
	 * given size in bytes.
	 * 
	 * @param message The message that was sent
	 * @param size The serialized size of the message
	 */
	public void sent( PorticoMessage message, int size )
	{
		if( !recording )
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

		// log some metrics
		MessageMetrics counter = metrics.get( type );
		if( counter == null )
		{
			counter = new MessageMetrics( type );
			metrics.put( type, counter );
		}
		
		counter.totalSent += 1;
		counter.totalSentSize += size;
	}

	/**
	 * The local federate just received the given message. When received on the wire, the
	 * message was the given size in bytes.
	 * 
	 * @param message The message that was received
	 * @param size The serialized size of the message
	 */
	public void received( PorticoMessage message, int size )
	{
		if( !recording )
			return;

		// discard our own messages as they're not coming from the network
		if( message.getSourceFederate() == lrc.getState().getFederateHandle() )
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
		
		// log some metrics
		MessageMetrics counter = metrics.get( type );
		if( counter == null )
		{
			counter = new MessageMetrics( type );
			metrics.put( type, counter );
		}
		
		counter.totalReceived += 1;
		counter.totalReceivedSize += size;
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
		if( this.recording )
			return;
		
		this.federationName = federationName;
		this.federateName = federateName;
		this.lrc = lrc;
		turnLoggerOn();
		this.recording = true;
		
		// log a startup message with some useful information
		logger.info( "Starting Audit log for federate ["+federateName+"] in federation ["+
		             federationName );
		logger.info( "Portico "+PorticoConstants.RTI_VERSION + " - JGroups "+Version.description );
		logger.info( "" );
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
    		this.appender = new RollingFileAppender( layout, logfile, true );
    		appender.setMaxBackupIndex( 2 );
    		appender.setMaxFileSize( "100MB" );
    		
    		// attach the appender
    		this.logger = Logger.getLogger( "auditor" );
    		this.logger.addAppender( appender );
		}
		catch( IOException ioex )
		{
			throw new JConfigurationException( "error configuring auditor log: "+ioex.getMessage(), ioex );
		}
	}

	/**
	 * The federation execution is now over - wrap up the auditing by printing a final summary
	 * and closing out the file.
	 */
	public void stopAuditing()
	{
		if( this.recording == false )
			return;
		
		this.federationName = "";
		this.federateName = "";
		this.lrc = null;
		this.recording = false;
		
		// print the execution summary before we leave
		logSummary();
		
		// turn the logger off
		this.logger.removeAppender( appender );
		this.appender.close();
	}
	
	private void logSummary()
	{
		// get some totals
		long sentCount = 0;
		long sentSize = 0;
		long receivedCount = 0;
		long receivedSize = 0;
		int longestName = 0;
		for( String message : metrics.keySet() )
		{
			MessageMetrics temp = metrics.get( message );
			sentCount     += temp.totalSent;
			sentSize      += temp.totalSentSize;
			receivedCount += temp.totalReceived;
			receivedSize  += temp.totalReceivedSize;
			
			if( message.length() > longestName )
				longestName = message.length();
		}
		
		// print a report
		String finishTime = String.format( "%1tH:%<tM:%<tS.%<tL", new Date(System.currentTimeMillis()) );
		String sentString = String.format( "%6d (%s)", sentCount, getSizeString(sentSize) );
		String receivedString = String.format( "%6d (%s)", receivedCount, getSizeString(receivedSize) );
		
		logger.info( "" );
		logger.info( "==============================================" );
		logger.info( "  Execution Summary" );
		logger.info( "==============================================" );
		logger.info( "     Finish Time: "+finishTime );
		logger.info( "      Total Sent: "+sentString );
		logger.info( "  Total Received: "+receivedString );
		logger.info( "" );
		
		// events table
		logger.info("  |-----------------------------|-------------------------------|-------------------------------|" );
		logger.info("  |                             |  Sent                         |  Received                     |" );
		logger.info("  |                             |---------------------------------------------------------------|" );
		logger.info("  | Message Name                |  Count  |   Size    |   Avg   |  Count  |   Size    |   Avg   |" );
		logger.info("  |-----------------------------|-------------------------------|-------------------------------|" );

		List<MessageMetrics> ordered = new ArrayList<MessageMetrics>( metrics.values() );
		Collections.sort( ordered );
		for( MessageMetrics temp : ordered )
		{
			// figure out the averages - may have received by not sent (or vv) meaning
			// we are exposed to divide-by-zero problems if we don't check first
			String sentAvg = "";
			String sentTotal = "";
			if( temp.totalSent > 0 )
			{
				sentAvg = getSizeString(temp.totalSentSize/temp.totalSent);
				sentTotal = ""+temp.totalSent;
			}

			String receivedAvg = "";
			String receivedTotal = "";
			if( temp.totalReceived > 0 )
			{
				receivedAvg = getSizeString(temp.totalReceivedSize/temp.totalReceived);
				receivedTotal = ""+temp.totalReceived;
			}

			String line = String.format( "  | %27s | %7s | %9s |%8s | %7s | %9s |%8s |",
			                             temp.className,
			                             sentTotal,
			                             getSizeString(temp.totalSentSize),
			                             sentAvg,
			                             receivedTotal,
			                             getSizeString(temp.totalReceivedSize),
			                             receivedAvg );
			logger.info( line );
		}

		logger.info("  |-----------------------------|-------------------------------|-------------------------------|" );
		logger.info( String.format("  |                       Total | %7d | %9s |%8s | %7d | %9s |%8s |",
		                           sentCount,
		                           getSizeString(sentSize),
		                           getSizeString(sentSize/sentCount),
		                           receivedCount,
		                           getSizeString(receivedSize),
		                           getSizeString(receivedSize/receivedCount)) );
		logger.info("  |-----------------------------|-------------------------------|-------------------------------|" );
		logger.info( "" );
	}

	/**
	 * Convert the given size (in bytes) to a more human readable string. Returned values
	 * will be in the form: "16B", "16KB", "16MB", "16GB".
	 */
	private String getSizeString( long size )
	{
		if( size == 0 )
			return "";
		
		double totalkb = size / 1024;
		double totalmb = totalkb / 1024;
		double totalgb = totalmb / 1024;
		if( totalgb > 1.0 )
			return String.format("%5.1f GB", totalgb );
		else if( totalmb > 1.0 )
			return String.format("%5.1f MB", totalmb );
		else if( totalkb > 1.0 )
			return String.format("%5.1f KB", totalkb );
		else
			return String.format("%5d B ", size );
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////  Private Inner Class: Message Metrics  ////////////////////
	////////////////////////////////////////////////////////////////////////////////
	/**
	 * Used to store information about the messages sent and received by the federate.
	 * This information is stored per message class.
	 */
	private class MessageMetrics implements Comparable<MessageMetrics>
	{
		public String className;
		public long totalSent;
		public long totalSentSize; // fine up to 8 exabytes
		public long totalReceived;
		public long totalReceivedSize;
		
		public MessageMetrics( String className )
		{
			this.className = className;
		}
		
		public int compareTo( MessageMetrics other )
		{
			// this comparision is reverse - as we want the large values at the front of the list
			long ours = totalSentSize + totalReceivedSize;
			long theirs = other.totalSentSize + totalReceivedSize;
			if( ours > theirs )
				return -1;
			else if( ours < theirs )
				return 1;
			else return 0;
		}
	}
}
