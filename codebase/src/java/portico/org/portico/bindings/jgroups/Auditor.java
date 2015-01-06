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
import org.portico.lrc.LRCState;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.management.Federate;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.object.msg.DeleteObject;
import org.portico.lrc.services.object.msg.DiscoverObject;
import org.portico.lrc.services.object.msg.SendInteraction;
import org.portico.lrc.services.object.msg.UpdateAttributes;
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
	private LRCState lrcState;
	
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
		this.lrc = null;      // set in startAuditing()
		this.lrcState = null; // set in startAuditing()
		
		String timeFormat = "%1tH:%<tM:%<tS.%<tL";
		this.receivedFormat = "received "+timeFormat+" %30s  %5d   from %-15s %s";
		this.sentFormat     = "    sent "+timeFormat+" %30s  %5d   %-20s %s";
		
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
		String federateName = getFederateName( message.getTargetFederate() );
		if( federateName.startsWith("unknown") )
			federateName = " ";
		else
			federateName = "  to "+federateName+" ";

		// log our message
		String type = message.getClass().getSimpleName();
		String entry = String.format( sentFormat,
		                              new Date(System.currentTimeMillis()),
		                              type,
		                              size,
		                              federateName,
		                              getMessageSpecificString(message) );
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
		
		// log message specific information

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

		// log our entry
		String federateName = getFederateName( message.getSourceFederate() );
		String type = message.getClass().getSimpleName();
		String entry = String.format( receivedFormat,
		                              new Date(System.currentTimeMillis()),
		                              type,
		                              size,
		                              federateName,
		                              getMessageSpecificString(message) );
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
	///////////////////////////  Portico Data Methods  ////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	/**
	 * For the federate with the given handle, get a printable name. Value returned
	 * is in the format "federateName(handle)". If the federate is not known, "unknown"
	 * is returned for the name.
	 */
	private String getFederateName( int federateHandle )
	{
		Federate source = lrcState.getKnownFederate( federateHandle );
		if( source != null )
			return source.getFederateName()+"("+federateHandle+")";
		else
			return "unknown("+federateHandle+")";
	}

	/**
	 * Inspect the given PorticoMessage and if it is one of the specified types (create, update
	 * delete, interaction) then generate some additional information to print and return it. If
	 * the message isn't one of these then an empty string is returned signalling that no extra
	 * information should be printed. 
	 */
	private String getMessageSpecificString( PorticoMessage message )
	{
		if( message instanceof DiscoverObject )
		{
			DiscoverObject discover = (DiscoverObject)message;
			String className = getFom().getObjectClass(discover.getClassHandle()).getLocalName();
			String objectName = discover.getObjectName()+"("+discover.getObjectHandle()+")";
			
			return className+", "+objectName;
		}
		else if( message instanceof UpdateAttributes )
		{
			UpdateAttributes update = (UpdateAttributes)message;
			
			// To get the class name we have to get the object, and from it get the class
			// In some rare cases, we might not have processed the discover yet and as such
			// don't known the object, so be careful of that
			int objectHandle = update.getObjectId();
			OCInstance object = getObject( objectHandle );
			if( object != null )
			{
				String className = ocName( object.getRegisteredClassHandle() );
				String objectName = object.getName()+"("+objectHandle+")";
				// return in form "className, objectName(handle), 7 attributes"
				return className+", "+objectName+", "+update.getAttributes().size()+" attributes";
			}
			else
			{
				return "UnknownClass, UnknownObject, "+update.getAttributes().size()+" attributes";
			}
		}
		else if( message instanceof SendInteraction )
		{
			SendInteraction interaction = (SendInteraction)message;
			
			// get the class name
			int interactionHandle = interaction.getInteractionId();
			String className = getFom().getInteractionClass(interactionHandle).getLocalName();
			
			// return in form "className, 7 parameters"
			return className+", "+interaction.getParameters().size()+" parameters";
		}
		else if( message instanceof DeleteObject )
		{
			DeleteObject delete = (DeleteObject)message;
			
			// to get the class name we have to get the object, and from it get the class
			int objectHandle = delete.getObjectHandle();
			OCInstance object = getObject( objectHandle );
			String className = ocName(  object.getRegisteredClassHandle() );
			
			// now get the object name
			String objectName = object.getName()+"("+objectHandle+")";
			
			// return in form "className, objectName(handle)"
			return className+", "+objectName;
		}
		else
		{
			return "";
		}
	}

	/**
	 * We can't cache this at startup because we start auditing during the join call before
	 * things are fully set up.As such, we have to ask for it each time. It's cool - we won't g
	 * et a request until after join finishes and by then the code below won't return null.
	 */
	private ObjectModel getFom()
	{
		return lrcState.getFOM();
	}
	
	private final OCInstance getObject( int objectHandle )
	{
		return lrcState.getRepository().getDiscoveredOrUndiscovered( objectHandle );
	}
	
	private final String ocName( int classHandle )
	{
		return lrcState.getFOM().getObjectClass(classHandle).getLocalName();
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
		this.lrcState = this.lrc.getState();
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
