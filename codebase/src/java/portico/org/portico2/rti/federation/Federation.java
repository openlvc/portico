/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.rti.federation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.SecretKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JFederateNameAlreadyInUse;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.model.ObjectModel;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.PorticoConstants;
import org.portico2.common.configuration.RID;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.messaging.MessageSink;
import org.portico2.common.services.ddm.data.RegionStore;
import org.portico2.common.services.ownership.data.OwnershipManager;
import org.portico2.common.services.pubsub.data.InterestManager;
import org.portico2.rti.RTI;
import org.portico2.rti.RtiConnection;
import org.portico2.rti.services.RTIHandlerRegistry;
import org.portico2.rti.services.mom.data.FomModule;
import org.portico2.rti.services.mom.data.MomManager;
import org.portico2.rti.services.object.data.Repository;
import org.portico2.rti.services.sync.data.SyncPointManager;
import org.portico2.rti.services.time.data.TimeManager;

/**
 * This class contains the infrastructure used to support a specific federation.
 */
public class Federation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final AtomicInteger FEDERATION_HANDLE_COUNTER = new AtomicInteger(0);

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTI         rti; // the RTI we exist in
	private RID         rid; // configuration for the RTI
	private String      federationName;
	private int         federationHandle;
	private HLAVersion  federationVersion;
	private ObjectModel fom;
	
	private Logger logger;

	// Federation Management //
	// We track both the federates connected, and the unique connection instances they are using.
	// This is so that we can attempt to, where possible, only pass a single copy of a given
	// message over a connection, even if multiple federates are using it to talk to us.
	private AtomicInteger federateHandleCounter;
	private Map<Integer,Federate> federates;
	private Set<RtiConnection> federateConnections;
	
	// Auth Settings //
	public SecretKey federationKey;
	
	// Message Processing //
	private MessageSink incomingSink;
//	private Queue<PorticoMessage> incomingControlQueue;
	private BlockingQueue<PorticoMessage> outgoingQueue;
	private Thread outgoingProcessor;

	// Pub & Sub Settings //
	private InterestManager interestManager;

	// Sync Point Settings //
	private SyncPointManager syncManager;

	// Instance Repository //
	private Repository repository;

	// Time Management //
	private TimeManager timeManager;
	
	// Ownership settings //
	private OwnershipManager ownershipManager;
	
	// MOM settings
	private MomManager momManager;
	private List<FomModule> fomModules;
	
	// Save/Restore settings //
//	private Serializer serializer;
//	private Manifest manifest;
//	private SaveManager saveManager;
//	private RestoreManager restoreManager;

	// DDM state entities //
	private RegionStore regionStore;
	private int latestRegionToken;
	private int maxRegionToken;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new Federation within the given RTI and using the given name, FOM and spec version.
	 * The spec version will be used to load the appropriate set of handlers from the RTI's
	 * {@link RTIHandlerRegistry}. If there is a problem, a configuration exception will be thrown.
	 * This method is only available within the package scope. If you want to create a Federation
	 * instance you should use the {@link FederationManager}.
	 * 
	 * @param rti  The RTI that we are loaded into
	 * @param name The name of the federation
	 * @param fom  The object model for the federation
	 * @param hlaVersion The spec version of the federation
	 * @throws JConfigurationException If there is a problem configuring the message handlers
	 */
	protected Federation( RTI rti, String name, ObjectModel fom, HLAVersion hlaVersion )
		throws JConfigurationException
	{
		this.rti               = rti;
		this.federationName    = name;
		this.federationHandle  = FEDERATION_HANDLE_COUNTER.incrementAndGet();
		this.federationVersion = hlaVersion;
		this.fom               = fom;
		
		this.logger = LogManager.getFormatterLogger( rti.getLogger().getName()+".{"+name+"}" );
		
		// Federation Management //
		this.federateHandleCounter = new AtomicInteger(0);
		this.federates = new HashMap<>();
		this.federateConnections = new HashSet<>();
		
		// Auth Settings //
		this.federationKey = null; // must be manually set
		
		// Message Processing //
		this.incomingSink = new MessageSink( name+"-incoming", logger );
		this.outgoingQueue = new LinkedBlockingQueue<>();
		this.outgoingProcessor = new OutgoingMessageProcessor();

		// Sync Point Settings //
		this.syncManager = new SyncPointManager( this );
		
		// Region Store //
		this.regionStore = new RegionStore();
		this.latestRegionToken = 0;
		this.maxRegionToken = 0;
		
		// Pub & Sub Settings //
		this.interestManager = new InterestManager( fom, regionStore );
		
		// Instance Repository //
		this.repository = new Repository( regionStore );
		
		// Time Management //
		this.timeManager = new TimeManager();
		
		// Ownership settings //
		this.ownershipManager = new OwnershipManager();
		
		// MOM settings //
		this.momManager = new MomManager( this );
		this.fomModules = new ArrayList<>();
		// ... TBA ...

		// Populate the Message Sinks
		// This must be done last to ensure that we have created all the manager pieces that
		// the handlers will try to extract when they get configured. Otherwise they'll be
		// full on nulls
		RTIHandlerRegistry.loadHandlers( this );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public String getFederationName() { return this.federationName; }
	public int getFederationHandle()  { return this.federationHandle; }
	public HLAVersion getHlaVersion() { return this.federationVersion; }
	public ObjectModel getFOM()       { return this.fom; }
	public Logger getLogger()         { return this.logger; }

	public SecretKey getFederationKey() { return this.federationKey; }
	public void setFedetrationKey( SecretKey key ) { this.federationKey = key; }
	
	public MessageSink getIncomingSink()
	{
		return this.incomingSink;
	}

	public InterestManager getInterestManager()
	{
		return this.interestManager;
	}

	public SyncPointManager getSyncPointManager()
	{
		return this.syncManager;
	}
	
	public Repository getRepository()
	{
		return this.repository;
	}
	
	public RegionStore getRegionStore()
	{
		return this.regionStore;
	}
	
	public TimeManager getTimeManager()
	{
		return this.timeManager;
	}
	
	public OwnershipManager getOwnershipManager()
	{
		return this.ownershipManager;
	}
	
	public MomManager getMomManager()
	{
		return this.momManager;
	}
	
	public void addRawFomModules( List<FomModule> modules )
	{
		// As per the 1516e spec, only modules that add something to the FOM are to be recorded. To keep
		// things simple, we'll just assume that if the designator is different then the module added
		// new content
		Set<String> existingDesignators = new HashSet<>();
		for( FomModule existingModule : this.fomModules )
			existingDesignators.add( existingModule.getDesignator() );
		
		for( FomModule newModule : modules )
		{
			String newDesignator = newModule.getDesignator();
			if( !existingDesignators.contains(newDesignator) )
				this.fomModules.add( newModule );
		}
	}
	
	public List<FomModule> getRawFomModules()
	{
		return new ArrayList<>( this.fomModules );
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Federate Management   ////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method is called when the federation si first created (note the past tense of the
	 * method call). It allows the federation to perform any local configuration or control it
	 * requires. It is much like a "startup" call.
	 */
	public void createdFederation()
	{
		// Start the outgoing queue processor
		this.outgoingProcessor.start();
		logger.debug( "Outgoing message processor thread started" );
	}
	
	/**
	 * This method is called AFTER the federation has been destroyed (note the past tense of the
	 * method call). It allows the federation to clean up any important resources it may have been
	 * consuming.
	 */
	public void destroyedFederation()
	{
		// Stop the outgoing queue processor
		logger.debug( "Interrupting the outgoing message processor thread" );
		this.outgoingProcessor.interrupt();
		try
		{
			this.outgoingProcessor.join( 5000 );
		}
		catch( InterruptedException ie )
		{}
	}

	/**
	 * Add the given federate to this federation.
	 * 
	 * @param federate The federate to join to this federation
	 * @throws JFederateNameAlreadyInUse If the name is already used by another federate
	 */
	public int joinFederate( Federate federate ) throws JFederateNameAlreadyInUse
	{
		// Make sure we don't have a double-up of federate names
		// TODO need to fix so that federate names don't have to be unique
		for( Federate temp : federates.values() )
		{
			if( temp.getFederateName().equalsIgnoreCase(federate.getFederateName()) )
				throw new JFederateNameAlreadyInUse( federate.getFederateName() );
		}
		
		this.addRawFomModules( federate.getRawFomModules() );
		
		// Assign the federate a handle and store it
		federate.setFederateHandle( federateHandleCounter.incrementAndGet() );
		this.federates.put( federate.getFederateHandle(), federate );
		
		// Store the connection that this federate is using
		this.federateConnections.add( federate.getConnection() );
		
		return federate.getFederateHandle();
	}

	/**
	 * Take the given federate and remove it from the federation. This will both remove the
	 * federate and also reassess the set of connections we have to see if the one that was
	 * assigned to this federate is now no longer used (removing it from the connection pool
	 * if it is not).
	 * 
	 * @param federate The federate to remove from the federation
	 * @throws JFederateNotExecutionMember If the federate isn't in the federation
	 */
	public void resignFederate( Federate federate )
	{
		// Make sure we have this federate in the federation
		if( federates.containsKey(federate.getFederateHandle()) == false )
		{
			throw new JFederateNotExecutionMember( "federate [%s] not part of federation [%s]",
			                                       federate.getFederateName(),
			                                       federationName );
		}

		// Remove the federate from our store of federates
		this.federates.remove( federate.getFederateHandle() );
		
		// Remove the connection this federate was using (unless another federate also using it)
		RtiConnection connection = federate.getConnection();
		boolean stillUsed = federates.values().stream()
		                                      .filter( temp -> connection.equals(temp.getConnection()) )
		                                      .findAny()
		                                      .isPresent();
		if( stillUsed == false )
			federateConnections.remove( connection );
	}

	public Set<Federate> getFederates()
	{
		return new HashSet<>( this.federates.values() );
	}
	
	public Federate getFederate( String name )
	{
		for( Federate federate: federates.values() )
		{
			if( federate.getFederateName().trim().equalsIgnoreCase(name) )
				return federate;
		}
		
		return null;
	}
	
	public Federate getFederate( int federateHandle )
	{
		return federates.get( federateHandle );
	}
	
	public int getFederateHandle( String name )
	{
		int value = PorticoConstants.NULL_HANDLE;
		for( Federate federate : federates.values() )
		{
			if( federate.getFederateName().trim().equalsIgnoreCase("") )
				return federate.getFederateHandle();
		}
		
		return value;		
	}
	
	public Set<Integer> getFederateHandles()
	{
		return federates.keySet();
	}
	
	public boolean containsFederate( int federateHandle )
	{
		return federates.keySet().contains( federateHandle );
	}
	
	public boolean containsFederate( String name )
	{
		for( Federate federate : federates.values() )
		{
			if( federate.getFederateName().trim().equalsIgnoreCase(name) )
				return true;
		}
		
		return false;
	}
	
	public boolean containsFederates()
	{
		return federates.isEmpty() == false;
	}

	/**
	 * @return The set of all connections that are used by federates joined to this federation.
	 *         This method will only return the unique connection instances, even if there are
	 *         multiple federates using the same single connection.
	 */
	public Set<RtiConnection> getFederateConnections()
	{
		return this.federateConnections;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Message Sending Methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Queue the given control message for sending to its target federate. This will happen later
	 * once the {@link OutgoingMessageProcessor} has had a chance to get to it.
	 *  
	 * @param message The message to queue
	 */
	public final void queueControlMessage( PorticoMessage message )
	{
		message.setIsFromRti( true );
		message.setSourceFederateIfNull( PorticoConstants.RTI_HANDLE );
		message.setTargetFederation( federationHandle );
		if( this.outgoingQueue.offer(message) == false )
			logger.warn( "Message could not be added to outgoing queue (overflow): "+message.getType() );
	}

	/**
	 * Broadcast the given message to all the connections that are linked to this federation except for
	 * the connection that sent it. Note that we keep one instance of each connection, even if multiple
	 * federates are using it. As such, if we have 10 federates spread across 3 connections, this will
	 * cause two broadcast requests to be sent.
	 * <p/>
	 * Also note, MESSAGES ARE NOT LOOPED BACK TO THE SENDER CONNECTION. If one connection is
	 * multiplexing many, it must handle broadcast to those connections internally.
	 * 
	 * @param message The message to broadcast
	 * @param sender  The connection we received the message from
	 */
	public final void queueDataMessage( PorticoMessage message, RtiConnection sender )
	{
		// Reflect data message into the message sink so that the Mom Handlers can get a go at it
		this.incomingSink.process( new MessageContext(message) );
		
		for( RtiConnection connection : federateConnections )
		{
			if( connection == sender )
				continue;
			else
				connection.sendDataMessage( message );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

	///////////////////////////////////////////////////////////////////////////////////////
	///  PRIVATE CLASS: Outgoing Message Processor   //////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This class processes the outgoing message queue - reflecting messages from the queue to
	 * each of the connections that are present for the federation.
	 */
	private class OutgoingMessageProcessor extends Thread
	{
		public OutgoingMessageProcessor()
		{
			super( federationName+"-outgoing" );
			super.setDaemon( true );
		}
		
		@Override
		public void run()
		{
			while( Thread.interrupted() == false )
			{
				try
				{
					// Get the next message
					PorticoMessage message = outgoingQueue.take();
					sendMessage( message );
				}
				catch( InterruptedException ie )
				{
					logger.warn( "Outgoing processor was interrupted, time to exit..." );
					return;
				}
			}
		}

		private void sendMessage( PorticoMessage message )
		{
			// FIXME - Do something smarter about only routing control messages to the connection
			//         that a target federate resides in
			MessageContext ctx = new MessageContext( message );
			for( RtiConnection connection : federateConnections )
			{
				try
				{
					connection.sendControlRequest( ctx );
					if( ctx.isErrorResponse() && ctx.hasResponse() )
						throw ctx.getErrorResponseException();
					
					if( logger.isTraceEnabled() )
						logger.trace( "Passed message [%s] to connection [%s]", message.getType(), connection.getName() );
				}
				catch( Exception e )
				{
					logger.warn( "Error sending message [%s] via connection [%s]",
					             message.getType(), connection.getName(), e );
				}				
			}
		}
	} // end of OutgoingMessageProcessor
}
