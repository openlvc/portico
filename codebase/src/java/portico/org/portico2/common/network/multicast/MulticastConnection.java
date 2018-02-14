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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Message;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.configuration.ConnectionConfiguration;
import org.portico2.common.configuration.MulticastConnectionConfiguration;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.network.IConnection;
import org.portico2.common.network.IMessageReceiver;
import org.portico2.common.network.jgroups.IJGroupsListener;
import org.portico2.common.network.jgroups.JGroupsChannel;
import org.portico2.common.network.jgroups.JGroupsConfiguration;

/**
 * The {@link MulticastConnection} uses a JGroups-based multicast channel to exchange messages.
 */
public class MulticastConnection implements IConnection, IJGroupsListener
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean isConnected;
	private IMessageReceiver receiver;
	private Logger logger;
	private MulticastConnectionConfiguration configuration;
	
	private JGroupsConfiguration jgroupsConfiguration;
	private JGroupsChannel jgroupsChannel;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MulticastConnection()
	{
		this.isConnected = false;
		this.receiver = null;             // set in setMessageReceiver()
		this.logger = null;               // set in configure()
		this.configuration = null;        // set in configure()
		this.jgroupsConfiguration = null; // set in configure()
		this.jgroupsChannel = null;       // set in connect()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Extract the RID configuration and store locally. Generate teh JGroups configuration.
	 */
	@Override
	public void configure( ConnectionConfiguration connectionConfiguration, IMessageReceiver receiver )
		throws JConfigurationException
	{
		this.receiver = receiver;
		this.logger = LogManager.getFormatterLogger( receiver.getLogger().getName()+".multicast" );
		this.configuration = (MulticastConnectionConfiguration)connectionConfiguration;
		
		// create a JGroups channel configuration from what was provided
		this.jgroupsConfiguration = new JGroupsConfiguration( configuration.getName() );
		this.jgroupsConfiguration.fromRidConfiguration( configuration );
		
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
	public void connect() throws JRTIinternalError
	{
		if( this.isConnected )
			return;

		logger.debug( "Connecting to Multicast channel: portico" );
		
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
	public void disconnect() throws JRTIinternalError
	{
		if( this.isConnected == false )
			return;

		logger.info( "Disconnecting from Multicast channel" );
		this.jgroupsChannel.disconnect();
		this.isConnected = false;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Message SENDING methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Pass the request message to the {@link JGroupsChannel} and pack the response into
	 * the given message context. This method will BLOCK for a period equal to the JGroups
	 * GMS connection timeout.
	 * <p/>
	 * When JGroups joins a channel, the Group Management System sends out a request for an
	 * existing coordinator. If it doesn't find one in a certain period of time, it gives up
	 * and assumes there isn't one, and appoints itself. The timeout period we use is the same
	 * that is configured for the GMS and can be changed in the RID.  
	 * 
	 * @param context Source of request and target for response.
	 * @throws JRTIinternalError IF there is a problem processing the message.
	 */
	@Override
	public void sendControlRequest( MessageContext context ) throws JRTIinternalError
	{
		jgroupsChannel.sendControlRequest( context );
	}
	
	/**
	 * Broadcast the message to the JGroups channel.
	 */
	@Override
	public void sendDataMessage( PorticoMessage message ) throws JException
	{
		jgroupsChannel.sendDataMessage( message );
	}

	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveDataMessage( JGroupsChannel channel, Message message ) throws JRTIinternalError
	{
		PorticoMessage received = MessageHelpers.inflate( message.getRawBuffer(), PorticoMessage.class );
		receiver.receiveDataMessage( received );
	}

	@Override
	public void receiveControlRequest( JGroupsChannel channel, int requestId, Message message ) throws JException
	{
		PorticoMessage incoming = MessageHelpers.inflate( message.getRawBuffer(), PorticoMessage.class );
		
		// Should we even process this?
		if( receiver.isReceivable(incoming.getTargetFederate()) == false )
			return;

		// Wrap the incoming request in a message context and hand it off for processing
		MessageContext context = new MessageContext( incoming );
		receiver.receiveControlRequest( context );
		
		if( context.hasResponse() == false )
			logger.warn( "No response received for Control Request "+incoming.getIdentifier() );

		// If the incoming message is async, don't send a response
		if( incoming.isAsync() == false )
			channel.sendControlResponse( requestId, MessageHelpers.deflate(context.getResponse()) );
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
