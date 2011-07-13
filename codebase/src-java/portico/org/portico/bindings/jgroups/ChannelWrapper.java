/*
 *   Copyright 2009 The Portico Project
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

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.util.DefaultThreadFactory;
import org.jgroups.util.Util;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.compat.JResignAction;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.federation.msg.ResignFederation;
import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.messaging.PorticoMessage;

/**
 * This class wraps a JGroups channel, providing a bunch of commonly used behaviour required by
 * the Portico JGroups binding. Methods that implement the sendAsyn/sendAndWait semantics of
 * Portico are provided, as well as a way to plug in custom classes to handle incoming remote
 * messages from the channel.
 */
public class ChannelWrapper
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/**
	 * A recevier that just ignores all messages. Can be useful in situations like handling
	 * create-federation requests where you just want to snoop to see if there is anything
	 * inside the federation, 
	 */
	public static final JGReceiver DEV_NULL = new JGReceiver()
	{
		public void setWrapper( ChannelWrapper wrapper ){}
		public void receiveAsynchronous( Message message ){}
		public Object receiveSynchronous( Message message ){ return null; }
	};

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String channelName;
	private Logger logger;
	
	private JChannel channel;
	private MessageDispatcher dispatcher;
	private JGReceiver receiver;
	private ChannelListener listener;
	private Roster roster;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new channel of the given name. Incoming messages should be sent to the given
	 * receiver. NOTE that the wrapper will attempt to connect to the channel during the execution
	 * of the constructor. Should there be a problem connecting, an exception will be thrown,
	 * otherwise, at the conclusion of the constructor the wrapper will be connected.
	 * 
	 * @param channelName The name of the channel to connect to
	 * @param messageReceiver The object that all incoming messages will be sent to
	 */
	public ChannelWrapper( String channelName, JGReceiver messageReceiver, Logger logger )
		throws JRTIinternalError
	{
		this.channelName = channelName;
		this.logger = logger;
		this.receiver = messageReceiver;
		this.listener = new ChannelListener();
		
		// Connect to group so we can start sending and receiving messages
		// NOTE that we use our own special message/membership listener (the inner-class
		//      ChannelListener) and that this class forwards messages on to the user provided
		//      message recevier as required. We keep our own handler to deal with big-4 messages.
		try
		{
			logger.trace( "ATTEMPT connecting to channel ["+channelName+"]" );
			this.channel = ChannelWrapper.newChannel();
			this.dispatcher = new MessageDispatcher( channel, listener, listener, listener );
			this.channel.connect( this.channelName, null, null, 0 );
			logger.debug( "SUCCESS connected to channel ["+channelName+"]" );
		}
		catch( ChannelException ce )
		{
			logger.error( "Error connecting to channel ["+channelName+"]: "+ce.getMessage(), ce );
			throw new JRTIinternalError( ce );
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Disconnects from the underlying channel
	 */
	public void shutdown()
	{
		// disconnection from the group and clean up
		this.channel.close();
		logger.debug( "SUCCESS disconnected from channel: " + channelName );
	}
	
	public String toString()
	{
		return "jgroups-connection: channel="+channelName;
	}
	
	protected JChannel getChannel()
	{
		return this.channel;
	}
	
	protected MessageDispatcher getDispatcher()
	{
		return this.dispatcher;
	}

	protected Roster getRoster()
	{
		return this.roster;
	}

	protected JGReceiver getReceiver()
	{
		return this.receiver;
	}
	
	protected void setReceiver( JGReceiver receiver )
	{
		this.receiver = receiver;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Message SENDING Methods ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Writes the given message to the channel and returned right away. If there is a problem
	 * while serializing the message or writing it to the channel, an exception will be thrown.
	 */
	public void writeAsync( PorticoMessage payload ) throws JRTIinternalError
	{
		// turn the packet into a message
		byte[] data = MessageHelpers.deflate( payload );
		Message message = new Message( null, null, data );

		// write the message
		if( logger.isTraceEnabled() )
		{
			logger.trace( "(outgoing) payload="+payload.getClass().getSimpleName()+", size="+
			              data.length+", channel="+channelName );
		}
		
		try
		{
			channel.send( message );
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Problem sending message: channel="+channelName+
			                             ", error message="+e.getMessage(), e );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Create and propertly configured new JChannel. This includes setting the channel up so that
	 * it uses a thread factory that creates Daemon threads, rather than user threads (and thus,
	 * shouldn't keep an execution alive if only JGroups IO servicing threads are left.
	 */
	private static JChannel newChannel() throws ChannelException, JRTIinternalError
	{
		// create the channel using the appropriate configuration as defined by system properties
		JChannel channel = new JChannel( "etc/jgroups-udp.xml" );
		//JChannel channel = null;
		//String stackToUse = System.getProperty( JGProperties.PROP_JGROUPS_STACK, "udp" );
		//if( stackToUse.equalsIgnoreCase("udp") )
		//	return new JChannel( "etc/jgroups-udp.xml" );
		//else if( stackToUse.equalsIgnoreCase("tcp") )
		//	return new JChannel( "etc/jgroups-tcp.xml" );
		//else
		//	throw new JRTIinternalError( JGProperties.PROP_JGROUPS_STACK+
		//	                             " value invalid. Values: \"udp\" (default) or \"tcp\"" );

		// if we're not using daemon threads, return without resetting the thread groups
		if( Boolean.valueOf(System.getProperty(JGProperties.PROP_JGROUPS_DAEMON,"true")) == false )
			return channel;
		
		// create a thread factory that will use daemons
		DefaultThreadFactory factory =
			new DefaultThreadFactory( Util.getGlobalThreadGroup(), "Incoming", true, true );

		// set the thread factory on the transport
		channel.getProtocolStack().getTransport().setThreadFactory( factory );

		// set the thread pools on the transport
		LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>( 1000 );
		
		Executor pool = new ThreadPoolExecutor(2, 10, 5000, TimeUnit.MILLISECONDS, queue, factory);
		channel.getProtocolStack().getTransport().setDefaultThreadPool( pool );
		channel.getProtocolStack().getTransport().setOOBThreadPool( pool );
		
		return channel;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Private Inner Class: IncomingHandler ///////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This class handles all the JGroups state management and message receiving methods. For
	 * methods that involve cluster state, they perform the necessary logic to keep everything
	 * up-to-date. For methods that involve the handling of incoming message, they hand off
	 * processing to the {@link JGReceiver} associated with the {@link ChannelWrapper} the
	 * listener lives inside.
	 */
	private class ChannelListener implements RequestHandler, MessageListener, MembershipListener
	{
		/////////////////////////////////////////////////////////////////////
		////////////////////// MessageListener Methods //////////////////////
		/////////////////////////////////////////////////////////////////////
		public void setState( byte[] state )
		{
			synchronized( this )
			{
				try
				{
					Roster incomingRoster = (Roster)Util.objectFromByteBuffer( state );
					incomingRoster.updateAfterGetState( channel.getLocalAddress() );
					roster = incomingRoster;
				}
				catch( Exception e )
				{
					logger.fatal( "Couldn't deserialize Roster received from cluster, "+
					              "invalid state could now cause strange behaviour", e );
				}
			}
		}
		
		public byte[] getState()
		{
			synchronized( this )
			{
				if( roster == null )
				{
					logger.error( "Cluster requested state but our Roster was null" );
					return new byte[0];
				}
				
				try
				{
					return Util.objectToByteBuffer( roster );
				}
				catch( Exception e )
				{
					logger.fatal( "Couldn't provide Roster to cluster when requested, "+
					              "invalid state could now cause strange behaviour", e );
					return new byte[0];
				}
			}
		}

		public void receive( Message message )
		{
			// log that we have an incoming message
			if( logger.isTraceEnabled() )
			{
				logger.trace( "(incoming) asynchronous, channel="+channelName+", size="+
				              message.getLength()+", source="+message.getSrc() );
			}
			
			// hand the message off to the receiver
			receiver.receiveAsynchronous( message );
		}

		//////////////////////////////////////////////////////////////////////
		/////////////////////// RequestHandler Methods ///////////////////////
		//////////////////////////////////////////////////////////////////////
		public Object handle( Message message )
		{
			// log that we have an incoming message
			if( logger.isTraceEnabled() )
			{
				logger.trace( "(incoming) synchronous, channel="+channelName+", size="+
				              message.getLength()+", source="+message.getSrc() );
			}
			
			// is the message a special type?
			if( message.isFlagSet(JGProperties.MSG_CREATE) )
				return handleMsgCreate( message );
			else if( message.isFlagSet(JGProperties.MSG_JOINED) )
				return handleMsgJoined( message );
			else if( message.isFlagSet(JGProperties.MSG_RESIGNED) )
				return handleMsgResigned( message );
			else if( message.isFlagSet(JGProperties.MSG_DESTROY) )
				return handleMsgDestroy( message );
			
			// hand the message off to the receiver
			return receiver.receiveSynchronous( message );
		}

		private String handleMsgCreate( Message message )
		{
			byte[] bytes = message.getBuffer();
			logger.trace( "Received federation creation notification: federation="+channelName+
			              ", fomSize="+bytes.length+", source=" + message.getSrc() );
			try
			{
				synchronized( this )
				{
					roster.setObjectModel( (ObjectModel)Util.objectFromByteBuffer(bytes) );
					logger.info( "Federation ["+channelName+"] has been created" );
					return "ACK";
				}
			}
			catch( Exception e )
			{
				return "fail: "+e.getMessage();
			}
		}
		
		private String handleMsgJoined( Message message )
		{
			String federateName = new String( message.getBuffer() );
			synchronized( this )
			{
				logger.trace( "Received federate join notification: federate=" +federateName+
				              ", federation="+channelName+", source=" + message.getSrc() );
				String error = roster.connectionJoined(message.getSrc(), federateName, channelName);
				if( error != null )
				{
					logger.info( error );
					return error;
				}
				
				logger.info( "Federate ["+federateName+"] joined federation ["+channelName+"]" );
				return "ACK";
			}
		}
		
		private String handleMsgResigned( Message message )
		{
			// queue the message for processing
			receiver.receiveAsynchronous( message );

			// get the federate name
			ResignFederation request = MessageHelpers.inflate( message.getBuffer(),
			                                                   ResignFederation.class );
			String federateName = request.getFederateName();
			
			// record the resignation in the roster
			synchronized( this )
			{
				logger.trace( "Received federate resign notification: federate="+federateName+
				              ", federation="+channelName+", source="+message.getSrc() );
				String error = roster.connectionResigned(message.getSrc(),federateName,channelName);
				if( error != null )
				{
					logger.info( error );
					return error;
				}
			}

			logger.info( "Federate ["+federateName+"] has resigned from ["+channelName+"]" );
			return "ACK";
		}
		
		private String handleMsgDestroy( Message message )
		{
			String federationName = new String( message.getBuffer() );
			synchronized( this )
			{
				logger.trace( "Received federate destroy notification: federation="+federationName+
				              ", source=" + message.getSrc() );
				String error = roster.connectionDestroyed( message.getSrc(), federationName );
				if( error != null )
				{
					logger.info( error );
					return error;
				}

				logger.info( "Federation ["+federationName+"] has been destroyed" );
				return "ACK";
			}
		}

		//////////////////////////////////////////////////////////////////////
		///////////////////// MembershipListener Methods /////////////////////
		//////////////////////////////////////////////////////////////////////
		public void viewAccepted( View newView )
		{
			synchronized( this ) // multiple receiving threads could call this at the same time
			{
				if( roster == null )
					roster = new Roster( channelName, channel.getLocalAddress(), newView );
				else
					roster.updateFromView( newView );
				
				////////////////////////////////////////////////////
				// handle the ungraceful exiting of any federates //
				////////////////////////////////////////////////////
				// get a list of all the federates that have gone since the last update
				Map<String,Integer> removed = roster.getGoneSinceLastUpdate();
				for( String federateName : removed.keySet() )
				{
					// for each federate that is now gone, synthesize a resign notification
					ResignFederation resign =
						new ResignFederation( JResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES );
					int federateHandle = removed.get( federateName );
					resign.setSourceFederate( federateHandle );
					resign.setFederateName( federateName );
					resign.setFederationName( channelName );
					resign.setImmediateProcessingFlag( true );
					Message resignMessage = new Message( channel.getLocalAddress(),
					                                     channel.getLocalAddress(),
					                                     MessageHelpers.deflate(resign) );
					if( logger.isInfoEnabled() )
					{
						logger.info( "Federate ["+federateName+","+federateHandle+
						             "] disconnected, synthesizing resign message" );
					}
					
					receiver.receiveAsynchronous( resignMessage );
				}
			}
		}

		public void suspect( Address suspectedDropout )
		{
			// ignore
		}

		public void block()
		{
			// ignore
		}
	}
}
