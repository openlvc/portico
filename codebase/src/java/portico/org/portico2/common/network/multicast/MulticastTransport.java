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
package org.portico2.common.network.multicast;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.network.Connection;
import org.portico2.common.network.ITransport;
import org.portico2.common.network.Message;
import org.portico2.common.network.ProtocolStack;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.common.network.configuration.MulticastConfiguration;
import org.portico2.common.network.configuration.TransportType;

/**
 * The {@link MulticastTransport} uses a JGroups-based multicast channel to exchange messages.
 */
public class MulticastTransport implements ITransport, IJGroupsListener
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean isConnected;
	private Logger logger;
	private MulticastConfiguration configuration;
	private ProtocolStack protocolStack;
	
	private JGroupsConfiguration jgroupsConfiguration;
	private JGroupsChannel jgroupsChannel;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MulticastTransport()
	{
		this.isConnected = false;
		this.logger = null;               // set in configure()
		this.configuration = null;        // set in configure()
		this.protocolStack = null;        // set in configure()
		this.jgroupsConfiguration = null; // set in configure()
		this.jgroupsChannel = null;       // set in connect()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Extract the RID configuration and store locally. Generate the JGroups configuration.
	 */
	@Override
	public void configure( ConnectionConfiguration configuration, Connection connection )
		throws JConfigurationException
	{
		this.logger = connection.getLogger();
		this.configuration = (MulticastConfiguration)configuration;
		this.protocolStack = connection.getProtocolStack();
		
		// create a JGroups channel configuration from what was provided
		this.jgroupsConfiguration = new JGroupsConfiguration( configuration.getName() );
		this.jgroupsConfiguration.fromRidConfiguration( this.configuration );
		
		logger.debug( "--- Multicast Configuration ---" );
		logger.debug( "  >>   NIC Address: "+jgroupsConfiguration.getBindAddress() );
		logger.debug( "  >> Group Address: "+jgroupsConfiguration.getMulticastGroupAddress() );
		logger.debug( "  >>    Group Port: "+jgroupsConfiguration.getMulticastPort() );
	}
	
	/**
	 * Connect to the JGroups channel and start exchanging messages.
	 * @throws JRTIinternalError General failure establishing a connection to the JGroups channel.
	 */
	@Override
	public void open() throws JRTIinternalError
	{
		if( this.isConnected )
			return;

		logger.trace( "Connecting to Multicast channel: portico" );
		
		// Create the channel and connect to it
		this.jgroupsChannel = new JGroupsChannel( "portico", jgroupsConfiguration, this );
		this.jgroupsChannel.connect();
		this.isConnected = true;
	}
	
	/**
	 * Disconnect from the JGroups channel.
	 * @throws JRTIinternalError General failure disconnecting from the JGroups channel.
	 */
	@Override
	public void close() throws JRTIinternalError
	{
		if( this.isConnected == false )
			return;

		logger.trace( "Disconnecting from Multicast channel" );
		this.jgroupsChannel.disconnect();
		this.isConnected = false;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Message SENDING methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void send( Message message )
	{
		this.jgroupsChannel.send( message.getBuffer() );
	}

	@Override
	public TransportType getType()
	{
		return TransportType.Multicast;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receive( JGroupsChannel channel, byte[] payload )
	{
		protocolStack.up( new Message(payload) );
	}
	
	@Override
	public Logger provideLogger()
	{
		return logger;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
