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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Version;
import org.portico.lrc.LRC;
import org.portico.lrc.LRCState;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.management.Federate;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.ObjectModel;
import org.portico.utils.logging.Log4jConfigurator;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.services.object.msg.DeleteObject;
import org.portico2.common.services.object.msg.DiscoverObject;
import org.portico2.common.services.object.msg.SendInteraction;
import org.portico2.common.services.object.msg.UpdateAttributes;

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
	private boolean summaryOnlyMode;
	private Logger logger;
	private String receivedFormat;
	private String sentFormat;
	
	private String federationName;
	private String federateName;
	private LRC lrc;
	private LRCState lrcState;
	
	// counters and metrics
	private Map<String,MessageMetrics> metrics;

	// filters to restrict what we log
	private List<String> directionFilters;
	private List<String> messageFilters;
	private List<String> fomtypeFilters;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Auditor()
	{
		this.recording = false;
		this.summaryOnlyMode = false;
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
		
		// filters
		this.directionFilters = new ArrayList<String>();
		this.messageFilters   = new ArrayList<String>();
		this.fomtypeFilters   = new ArrayList<String>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

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
		
		// does this need to be filtered out?
		if( shouldFilter(message,"sent") )
			return;

		// we use the type both for logging and for storing the information for summary metrics
		String type = message.getClass().getSimpleName();

		// log message details unless we are in summary-only mode
		if( this.summaryOnlyMode == false )
		{
			// who are we sending this to?
			String federateName = getFederateName( message.getTargetFederate() );
			if( federateName.startsWith("unknown") )
				federateName = " ";
			else
				federateName = "  to "+federateName+" ";

			// log our message
			String entry = String.format( sentFormat,
			                              new Date(System.currentTimeMillis()),
			                              type,
			                              size,
			                              federateName,
			                              getMessageSpecificString(message) );
			logger.info( entry );
		}

		// store some metrics
		MessageMetrics counter = metrics.get( type );
		if( counter == null )
		{
			counter = new MessageMetrics( type );
			metrics.put( type, counter );
		}
		
		counter.totalSent += 1;
		counter.totalSentSize += size;
		
		// store FOM class specific information
		MessageMetrics fomSpecific = getFomSpecificMetrics( message );
		if( fomSpecific != null )
		{
			fomSpecific.totalSent += 1;
			fomSpecific.totalSentSize += size;
		}
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

		// does this need to be filtered out?
		if( shouldFilter(message,"received") )
			return;

		// discard our own messages as they're not coming from the network
		if( message.getSourceFederate() == lrc.getState().getFederateHandle() )
			return;

		// we use the type both for logging and for storing the information for summary metrics
		String type = message.getClass().getSimpleName();

		// log message details unless we are in summary-only mode
		if( this.summaryOnlyMode == false )
		{
			// log our entry
			String federateName = getFederateName( message.getSourceFederate() );
			String entry = String.format( receivedFormat,
			                              new Date(System.currentTimeMillis()),
			                              type,
			                              size,
			                              federateName,
			                              getMessageSpecificString(message) );
			logger.info( entry );
		}
		
		// log some metrics
		MessageMetrics counter = metrics.get( type );
		if( counter == null )
		{
			counter = new MessageMetrics( type );
			metrics.put( type, counter );
		}
		
		counter.totalReceived += 1;
		counter.totalReceivedSize += size;

		// store FOM class specific information
		MessageMetrics fomSpecific = getFomSpecificMetrics( message );
		if( fomSpecific != null )
		{
			fomSpecific.totalReceived += 1;
			fomSpecific.totalReceivedSize += size;
		}
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
		if( message instanceof UpdateAttributes )
		{
			//                    //
			// Reflection Message //
			//                    //
			UpdateAttributes update = (UpdateAttributes)message;
			
			// To get the class name we have to get the object and ask it.
			// In rare cases we might now have received & processed the discover call
			// before an update comes in, so the object may not exist in the store yet
			int objectHandle = update.getObjectId();
			OCInstance object = getObject( objectHandle );
			if( object != null )
			{
				String className = getClassName( object.getRegisteredClassHandle() );
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
			//                  //
			// Send Interaction //
			//                  //
			SendInteraction interaction = (SendInteraction)message;
			
			// get the class name
			int interactionHandle = interaction.getInteractionId();
			String className = getFom().getInteractionClass(interactionHandle).getLocalName();
			
			// return in form "className, 7 parameters"
			return className+", "+interaction.getParameters().size()+" parameters";
		}
		else if( message instanceof DiscoverObject )
		{
			//                  //
			// Discover Message //
			//                  //
			DiscoverObject discover = (DiscoverObject)message;
			String className = getFom().getObjectClass(discover.getClassHandle()).getLocalName();
			String objectName = discover.getObjectName()+"("+discover.getObjectHandle()+")";
			return className+", "+objectName;
		}
		else if( message instanceof DeleteObject )
		{
			DeleteObject delete = (DeleteObject)message;
			
			// to get the class name we have to get the object, and from it get the class
			int objectHandle = delete.getObjectHandle();
			OCInstance object = getObject( objectHandle );
			if( object == null )
				return "Unknown";

			String className = getClassName( object.getRegisteredClassHandle() );
			
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
	 * Find and fetch the MessageMetrics for the specific FOM class within the metrics for the
	 * type of the given message. For example, find the MessageMetrics representing the Lifeform
	 * class from within the metrics object representing update messages.
	 * 
	 * If this class isn't one we're interested in FOM-specific metrics for (create, refelct,
	 * delete, interaction), then return null.
	 */
	private MessageMetrics getFomSpecificMetrics( PorticoMessage message )
	{
		// get the metrics object for the message type (reflect, interaction, etc...)
		// if this isn't here, we have no specific metrics so return null
		MessageMetrics parent = metrics.get( message.getClass().getSimpleName() );
		if( parent == null )
			return null;

		String className = getClassName( message );
		if( className.equals("Unknown") == false )
			return parent.getOrAdd( className );
		else
			return null;
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

	/**
	 * For the given handle, get the associated {@link OCInstance} data from the repository.
	 * This may return <code>null</code> if it comes in before the federate has had a chance
	 * to process the discovery call.
	 */
	private final OCInstance getObject( int objectHandle )
	{
		return lrcState.getRepository().getDiscoveredOrUndiscovered( objectHandle );
	}

	/**
	 * Get the name of the Object Class represented by the given handle. This will return only
	 * the local portion of the class, not its fully qualified name
	 */
	private final String getClassName( int classHandle )
	{
		return lrcState.getFOM().getObjectClass(classHandle).getLocalName();
	}

	/** Get appropriate object/interaction class name for this message type, or return "Unknown" */
	private final String getClassName( PorticoMessage message )
	{
		if( message instanceof UpdateAttributes )
		{
    		int objectHandle = ((UpdateAttributes)message).getObjectId();
    		OCInstance object = getObject( objectHandle );
    		if( object != null )
    			return getClassName( object.getRegisteredClassHandle() );
		}
		else if( message instanceof SendInteraction )
		{
			int classHandle = ((SendInteraction)message).getInteractionId();
			return getFom().getInteractionClass(classHandle).getLocalName();
		}
		else if( message instanceof DiscoverObject )
		{
			int classHandle = ((DiscoverObject)message).getClassHandle();
			return getFom().getObjectClass(classHandle).getLocalName();
		}
		else if( message instanceof DeleteObject )
		{
			OCInstance object = getObject( ((DeleteObject)message).getObjectHandle() );
			return getClassName( object.getRegisteredClassHandle() );
		}
			
		return "Unknown";
	}
	
	////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////  Filter Methods  ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return True if we should filter out this message - false otherwise
	 */
	private boolean shouldFilter( PorticoMessage message, String direction )
	{
		// The filter lists contain information about which types should be let through,
		// so if something isn't in the list, it shouldn't get through. If the list is
		// empty it means we have no filters, so let everything through
		
		// check the direction
		if( directionFilters.isEmpty() == false )
		{
			if( this.directionFilters.contains(direction) == false )
				return true; // FILTER IT!
		}
		
		// check the message type
		if( messageFilters.isEmpty() == false )
		{
    		String type = message.getClass().getSimpleName();
    		if( this.messageFilters.contains(type) == false )
    			return true; // FILTER IT!
		}
		
		if( fomtypeFilters.isEmpty() == false )
		{
    		String fomtype = getClassName(message);
    		if( this.fomtypeFilters.contains(fomtype) == false )
    			return true; // FILTER IT!
		}

		// let it through
		return false;
	}
	
	/** Populate our filter list from configuration */
	private void populateFilters()
	{
		this.directionFilters = Configuration.getAuditorDirectionFilters();
		this.messageFilters   = Configuration.getAuditorMessageFilters();
		this.fomtypeFilters   = Configuration.getAuditorFomtypeFilters();
		
		// if "all" is set, clear the filter
		if( shouldDisableFilter(directionFilters) )
			directionFilters.clear();
		if( shouldDisableFilter(messageFilters) )
			messageFilters.clear();
		if( shouldDisableFilter(fomtypeFilters) )
			fomtypeFilters.clear();
	}

	/** If the list contains "all", true is returned indicating we shouldn't filter anything */
	private boolean shouldDisableFilter( List<String> list )
	{
		for( String temp : list )
		{
			if( temp.equals("all") )
				return true;
		}

		return false;
	}
	
	///////////////////////////////////////////////////////////////////////////////
	/////////////////////////////  Lifecycle Methods  /////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	/**
	 * @return True if the auditor is currently recording content. False if it is not.
	 */
	public final boolean isRecording()
	{
		return this.recording;
	}

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

		this.summaryOnlyMode = Configuration.isAuditorSummaryOnly();
		this.federationName = federationName;
		this.federateName = federateName;
		this.lrc = lrc;
		this.lrcState = this.lrc.getState();
		turnLoggerOn();
		this.recording = true;
		
		// get our filters
		populateFilters();
		
		// log a startup message with some useful information
		String modeMessage = this.summaryOnlyMode ? "Summary Only" : "Full Message Log";
		
		logger.info( "Starting Audit log for federate ["+federateName+"] in federation ["+
		             federationName+"]" );
		logger.info( "Portico "+PorticoConstants.RTI_VERSION + " - JGroups "+Version.description );
		logger.info( String.format("%tc", new Date()) );
		logger.info( "  Detail Level: "+modeMessage );
		logger.info( "Active Filters:" );
		logger.info( "     direction: "+directionFilters );
		logger.info( "       message: "+messageFilters );
		logger.info( "       fomtype: "+fomtypeFilters );
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

			// create the auditor logger so that it has a logger config we can extend
			this.logger = LogManager.getFormatterLogger( "auditor" );
			Log4jConfigurator.addLogFileForLogger( "auditor-id", logfile, "%m%n", "auditor" );
		}
		catch( Exception ex )
		{
			throw new JConfigurationException( "error configuring auditor log: "+ex.getMessage(), ex );
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
		Log4jConfigurator.removeLogFileForLogger( "auditor-id", "auditor" );
		
		// clear the filter lists
		this.directionFilters.clear();
		this.messageFilters.clear();
		this.fomtypeFilters.clear();
	}
	
	///////////////////////////////////////////////////////////////////////////////
	//////////////////////////  Summary Logging Methods  //////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	/**
	 * Log the captured event information showing how many times each message type was captured
	 * in both the sending and receiving directions, along with the total and average size of
	 * these messages.
	 */
	private void logSummary()
	{
		// get some totals
		long sentCount = 0;
		long sentSize = 0;
		long receivedCount = 0;
		long receivedSize = 0;
		for( String message : metrics.keySet() )
		{
			MessageMetrics temp = metrics.get( message );
			sentCount     += temp.totalSent;
			sentSize      += temp.totalSentSize;
			receivedCount += temp.totalReceived;
			receivedSize  += temp.totalReceivedSize;
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
			String sentTotal = temp.totalSent == 0 ? "" : ""+temp.totalSent;
			String recvTotal = temp.totalReceived == 0 ? "" : ""+temp.totalReceived;

			String line = String.format( "  | %27s | %7s | %9s |%8s | %7s | %9s |%8s |",
			                             temp.messageClass,
			                             sentTotal,
			                             getSizeString(temp.totalSentSize),
			                             getSizeString(temp.getSentSizeAvg()),
			                             recvTotal,
			                             getSizeString(temp.totalReceivedSize),
			                             getSizeString(temp.getReceivedSizeAvg()) );
			logger.info( line );
		}

		// print the totals
		long sentAverage = sentCount == 0 ? 0 : sentSize/sentCount;
		long receivedAverage = receivedCount == 0 ? 0 : receivedSize/receivedCount;
		logger.info("  |-----------------------------|-------------------------------|-------------------------------|" );
		logger.info( String.format("  |                       Total | %7d | %9s |%8s | %7d | %9s |%8s |",
		                           sentCount,
		                           getSizeString(sentSize),
		                           getSizeString(sentAverage),
		                           receivedCount,
		                           getSizeString(receivedSize),
		                           getSizeString(receivedAverage)) );
		logger.info("  |-----------------------------|-------------------------------|-------------------------------|" );
		logger.info( "" );
		logger.info( "" );
		
		logFomSpecificSummary();
	}

	/**
	 * The standard summary logs information broken down by Portico message types. These relate
	 * to API-level actions like Discover, Reflect, etc... The Auditor also records FOM specific
	 * information for major events so that it can tell you, for example, how many objects of a
	 * particular class were registered. This method prints that as a separate summary table. 
	 */
	private void logFomSpecificSummary()
	{
		// events table
		logger.info( " FOM Specific Summary" );
		logger.info( "" );
		logger.info("  |-----------------------------|-------------------------------|-------------------------------|" );
		logger.info("  |                             |  Sent                         |  Received                     |" );
		logger.info("  |                             |---------------------------------------------------------------|" );
		logger.info("  | Message Name                |  Count  |   Size    |   Avg   |  Count  |   Size    |   Avg   |" );
		logger.info("  |-----------------------------|-------------------------------|-------------------------------|" );

		// loop through all the message types and print out any FOM specific data they have
		List<MessageMetrics> ordered = new ArrayList<MessageMetrics>( metrics.values() );
		Collections.sort( ordered );
		for( MessageMetrics type : ordered )
		{
			if( type.hasFomSpecificData() == false )
				continue;

			String sentTotal = type.totalSent == 0 ? "" : ""+type.totalSent;
			String recvTotal = type.totalReceived == 0 ? "" : ""+type.totalReceived;

			String line = String.format( "  | %-27s | %7s | %9s |%8s | %7s | %9s |%8s |",
			                             type.messageClass,
			                             sentTotal,
			                             getSizeString(type.totalSentSize),
			                             getSizeString(type.getSentSizeAvg()),
			                             recvTotal,
			                             getSizeString(type.totalReceivedSize),
			                             getSizeString(type.getReceivedSizeAvg()) );
			logger.info( line );
			
			// print the FOM specific breakdown
			List<MessageMetrics> fomtypes = new ArrayList<MessageMetrics>( type.fomspecific.values() );
			Collections.sort( fomtypes );
			for( MessageMetrics fomtype : fomtypes )
			{
				sentTotal = fomtype.totalSent == 0 ? "" : ""+fomtype.totalSent;
				recvTotal = fomtype.totalReceived == 0 ? "" : ""+fomtype.totalReceived;
				line = String.format( "  | %27s | %7s | %9s |%8s | %7s | %9s |%8s |",
				                      fomtype.messageClass,
				                      sentTotal,
				                      getSizeString(fomtype.totalSentSize),
				                      getSizeString(fomtype.getSentSizeAvg()),
				                      recvTotal,
				                      getSizeString(fomtype.totalReceivedSize),
				                      getSizeString(fomtype.getReceivedSizeAvg()) );
				logger.info( line );
			}
			
			logger.info("  |-----------------------------|-------------------------------|-------------------------------|" );

		}

		// a bit of breathing room at the end
		logger.info( "" );
		logger.info( "" );
	}
	
	/**
	 * Convert the given size (in bytes) to a more human readable string. Returned values
	 * will be in the form: "16B", "16KB", "16MB", "16GB".
	 */
	private String getSizeString( double size )
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
			return String.format("%5d B ", (int)size );
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
		public String messageClass;
		public long totalSent;
		public long totalSentSize; // fine up to 8 exabytes
		public long totalReceived;
		public long totalReceivedSize;
		
		// We use this to record FOM-specific metrics broken down by object/interaction
		// class name (rather than java type). A type that stores a set of itself? Oh god.
		// This is only used for create/reflect/delete/interaction events
		public Map<String,MessageMetrics> fomspecific;
		
		public MessageMetrics( String className )
		{
			this.messageClass = className;
			
			// create this all the time just to avoid NPE problems
			this.fomspecific = new HashMap<String,MessageMetrics>();
		}
		
		public boolean hasFomSpecificData()
		{
			return this.fomspecific.isEmpty() == false;
		}
		
		/** Get the FOM-specific MessageMetrics information for the given FOM class. If there
		    is no record of it for this type of message, add one and return it */
		public MessageMetrics getOrAdd( String fomClass )
		{
			MessageMetrics specific = fomspecific.get( fomClass );
			if( specific == null )
			{
				specific = new MessageMetrics( fomClass );
				fomspecific.put( fomClass, specific );
			}
			
			return specific;
		}
		
		public double getSentSizeAvg()
		{
			return totalSent == 0 ? 0 : totalSentSize/totalSent;
		}
		
		public double getReceivedSizeAvg()
		{
			return totalReceived == 0 ? 0 : totalReceivedSize/totalReceived;
		}
		
		public int compareTo( MessageMetrics other )
		{
			// this comparision is reverse - as we want the large values at the front of the list
			long ours = totalSent + totalReceived;
			long theirs = other.totalSent + totalReceived;
			if( ours > theirs )
				return -1;
			else if( ours < theirs )
				return 1;
			else return 0;
		}
	}
	
}
