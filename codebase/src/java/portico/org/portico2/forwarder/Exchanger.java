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
package org.portico2.forwarder;

import org.apache.logging.log4j.Logger;
import org.portico2.common.configuration.ForwarderConfiguration;
import org.portico2.common.configuration.RID;
import org.portico2.forwarder.firewall.Firewall;
import org.portico2.forwarder.tracking.StateTracker;

public class Exchanger
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Forwarder forwarder;
	private RID rid;
	private Logger logger;

	private ForwarderConnection upstream;
	private ForwarderConnection downstream;
	
	// Forwarders
	//
	// These sit inside a Connection and are linked to one another so that messages
	// coming through a connection can be diverted prior to them being "inflated".
	// When a message is received on the wire from upstream, the upstream forwarder
	// captures it and hands it directly to its sibling protocol that is in the downstream
	// connection to pass down and out.
	//
	// We create these here so we can populate them with the links they need. We then
	// insert them into the connections
	protected ForwardingProtocol upstreamForwarder;
	protected ForwardingProtocol downstreamForwarder;
	
	// State Tracker
	// We use this to track information about the various federations that exist
	// so that we can resolve names to handles appropriately
	protected StateTracker stateTracker;
	
	// Firewall
	// We use the firewall to help us determine whether to forward messages or not
	protected Firewall firewall;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected Exchanger( Forwarder forwarder )
	{
		this.forwarder = forwarder;
		this.rid       = forwarder.getRid();
		this.logger    = forwarder.getLogger();

		this.upstream = null;        // set in startup()
		this.downstream = null;      // set in startup()
		
		// Forwarders
		this.upstreamForwarder = null;   // set in startup()
		this.downstreamForwarder = null; // set in startup()
		
		// State Tracker
		this.stateTracker = null;    // set in startup
		
		// Firewall
		this.firewall = null;        // set in startup()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	protected void startup()
	{
		ForwarderConfiguration configuration = rid.getForwarderConfiguration();
		
		// State Tracker and Firewall
		this.stateTracker = new StateTracker( configuration, logger );
		this.firewall = new Firewall( configuration, stateTracker, logger );

		// Create the connections
		logger.debug( "Creating local and upstream connections" );
		this.upstream = new ForwarderConnection( Direction.Upstream, this, configuration.getUpstreamConfiguration() );
		this.downstream = new ForwarderConnection( Direction.Downstream, this, configuration.getDownstreamConfiguration() );
		
		// Create and insert the ForwardingProtocol implementations and insert into connections
		this.upstreamForwarder = new ForwardingProtocol( Direction.Upstream, this );
		this.downstreamForwarder = new ForwardingProtocol( Direction.Downstream, this );
		this.upstream.getConnection().getProtocolStack().addProtocol( upstreamForwarder );
		this.downstream.getConnection().getProtocolStack().addProtocol( downstreamForwarder );
		
		// Start the connections
		logger.info( "Starting UPSTREAM connection" );
		this.upstream.connect();

		logger.info( "Starting DOWNSTREAM connection" );
		this.downstream.connect();

		logger.info( "Exchanger is UP" );
	}

	protected void shutdown()
	{
		// Bring the connections down
		logger.info( "Stopping UPSTREAM connection" );
		this.upstream.disconnect();

		logger.info( "Stopping DOWNSTREAM connection" );
		this.downstream.disconnect();
		
		logger.info( "Exchanger is DOWN" );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The application ultimately represents the context that a connection sits within.
	 * As such, we often want the connection framework to use the same logging infrastructure.
	 * This method lets the application provide a logger to a connection.
	 * 
	 * @return The logger that the connection framework should use
	 */
	public Logger getLogger()
	{
		return this.logger;
	}

	public Forwarder getForwarder()
	{
		return this.forwarder;
	}
	
	
//	/*
//	 * Build up a nice, descriptive log message for each of the portico messages that pass
//	 * through the exchange.
//	 */
//	private final String requestLog( Direction direction, PorticoMessage message, Type type )
//	{
//		String from = StringUtils.sourceHandleToString( message );
//		String to = StringUtils.targetHandleToString( message );
//		
//		// Switch the to/from for reponse types; because above we take them from the REQUEST
//		if( type == Type.Response )
//		{
//			String temp = to;
//			to = from;
//			from = temp;
//		}
//		
//		return String.format( "[%s] %-14s: type=%s, from=%s, to=%s",
//		                      direction.flowDirection(),
//		                      type,
//		                      message.getType(),
//		                      from,
//		                      to );
//	}
//	
//	private final String responseLog( Direction direction, MessageContext context )
//	{
//		return requestLog(direction,context.getRequest(),Type.Response)+", result="+context.isSuccessResponse();
//	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
