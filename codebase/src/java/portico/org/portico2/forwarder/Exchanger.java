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
import org.portico.utils.StringUtils;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.configuration.ForwarderConfiguration;
import org.portico2.common.configuration.RID;
import org.portico2.common.messaging.MessageContext;
import org.portico2.forwarder.ForwarderConnection.Direction;

/**
 * An {@link Exchanger} is the class responsible for routing messages between the local and
 * upstream sides of a {@link Forwarder}.
 */
public class Exchanger
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private enum Type
	{
		Data         { public String toString(){return "(Data)";}},
		ControlAsync { public String toString(){return "(ControlAsync)";}},
		ControlSync  { public String toString(){return "(ControlSync)";}},
		Response     { public String toString(){return "(ControlResp)";}};
	};

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Forwarder forwarder;
	private RID rid;
	private Logger logger;

	private ForwarderConnection upstream;
	private ForwarderConnection downstream;

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
		
		// Create the connections
		logger.debug( "Creating local and upstream connections" );
		this.upstream = new ForwarderConnection( Direction.UPSTREAM, this, configuration.getUpstreamConfiguration() );
		this.downstream = new ForwarderConnection( Direction.DOWNSTREAM, this, configuration.getDownstreamConfiguration() );
		
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
	///  Message Passing   /////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	protected final void dataMessageReceived( Direction receivedFrom, PorticoMessage message )
	{
		if( logger.isDebugEnabled() )
			logger.debug( requestLog(receivedFrom,message,Type.Data) );
		
		switch( receivedFrom )
		{
			case DOWNSTREAM:
				upstream.sendDataMessage( message );
				break;
			case UPSTREAM:
				downstream.sendDataMessage( message );
				break;
		}
	}
	
	protected final void controlRequestReceived( Direction receivedFrom, MessageContext context )
	{
		if( logger.isDebugEnabled() )
		{
			Type type = context.getRequest().isAsync() ? Type.ControlAsync : Type.ControlSync;
			logger.debug( requestLog(receivedFrom,context.getRequest(),type) );
		}
		
		switch( receivedFrom )
		{
			case DOWNSTREAM:
				upstream.sendControlRequest( context );
				break;
			case UPSTREAM:
				downstream.sendControlRequest( context );
				break;
		}
		
		if( logger.isDebugEnabled() && !context.getRequest().isAsync() )
			logger.debug( responseLog(receivedFrom.reverse(),context) );
		
		
		
		
//		//////////////////////////////////////////////////////////////
//		if( logger.isDebugEnabled() )
//			logger.debug( outboundLog(receivedFrom,context.getRequest(),"(Control)") );
//		
//		switch( receivedFrom )
//		{
//			case DOWNSTREAM:
//				upstream.sendControlRequest( context );
//				break;
//			case UPSTREAM:
//				downstream.sendControlRequest( context );
//				break;
//		}
//		
//		if( logger.isDebugEnabled() )
//			logger.debug( responseLog(receivedFrom.reverse(),context) );
	}

	/*
	 * Build up a nice, descriptive log message for each of the portico messages that pass
	 * through the exchange.
	 */
	private final String requestLog( Direction direction, PorticoMessage message, Type type )
	{
		String from = StringUtils.sourceHandleToString( message );
		String to = StringUtils.targetHandleToString( message );
		
		// Switch the to/from for reponse types; because above we take them from the REQUEST
		if( type == Type.Response )
		{
			String temp = to;
			to = from;
			from = temp;
		}
		
		return String.format( "[%s] %-14s: type=%s, from=%s, to=%s",
		                      direction.flowDirection(),
		                      type,
		                      message.getType(),
		                      from,
		                      to );
	}
	
	private final String responseLog( Direction direction, MessageContext context )
	{
		return requestLog(direction,context.getRequest(),Type.Response)+", result="+context.isSuccessResponse();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/// Accessor and Mutator Methods   /////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public Logger getLogger()
	{
		return forwarder.getLogger();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
