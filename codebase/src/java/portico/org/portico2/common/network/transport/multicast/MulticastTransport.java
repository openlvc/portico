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
package org.portico2.common.network.transport.multicast;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.network.Connection;
import org.portico2.common.network.Message;
import org.portico2.common.network.configuration.protocol.ProtocolConfiguration;
import org.portico2.common.network.configuration.transport.MulticastConfiguration;
import org.portico2.common.network.transport.Transport;
import org.portico2.common.network.transport.TransportType;

/**
 * The {@link MulticastTransport} uses a JGroups-based multicast channel to exchange messages.
 */
public class MulticastTransport extends Transport implements IJGroupsListener
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean isConnected;
	private MulticastConfiguration configuration;
	
	private JGroupsConfiguration jgroupsConfiguration;
	private JGroupsChannel jgroupsChannel;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MulticastTransport()
	{
		super( TransportType.Multicast );
		this.isConnected = false;
		this.configuration = null;        // set in configure()
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
	protected void doConfigure( ProtocolConfiguration protocolConfiguration, Connection connection )
		throws JConfigurationException
	{
		this.configuration = (MulticastConfiguration)protocolConfiguration;
		
		// create a JGroups channel configuration from what was provided
		this.jgroupsConfiguration = new JGroupsConfiguration( "portico" );
		this.jgroupsConfiguration.fromRidConfiguration( this.configuration );
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

		logger.trace( "--- Multicast Configuration ---" );
		logger.trace( "  >>   NIC Address: "+jgroupsConfiguration.getBindAddress() );
		logger.trace( "  >> Group Address: "+jgroupsConfiguration.getMulticastGroupAddress() );
		logger.trace( "  >>    Group Port: "+jgroupsConfiguration.getMulticastPort() );
		logger.trace( "" );
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
	public void down( Message message )
	{
		this.jgroupsChannel.send( message.getBuffer() );
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receive( JGroupsChannel channel, byte[] payload )
	{
		up( new Message(payload) );
	}

	@Override
	public Logger provideLogger()
	{
		return logger;
	}
	
	@Override
	public boolean isOpen()
	{
		return isConnected;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
