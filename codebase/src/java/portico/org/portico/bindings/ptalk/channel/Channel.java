/*
 *   Copyright 2010 The Portico Project
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
package org.portico.bindings.ptalk.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.portico.bindings.ptalk.Common;
import org.portico.bindings.ptalk.LrcConnection;
import org.portico.bindings.ptalk.protocol.GroupManagement;
import org.portico.bindings.ptalk.transport.ITransport;
import org.portico.bindings.ptalk.transport.UdpTransport;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;

/**
 * The {@link Channel} class is the messaging hub of the PTalk communications binding. It contains
 * the {@link Pipeline} and {@link ITransport}s used for message processing and communication. It
 * also contains the incoming and outgoing message queues onto which the LRC and Transports can
 * place messages before they are processed. In addition to these, it contains and manages the
 * groups of threads that are designed to work on those messages.
 * <p/>
 * All this logic has been made separate from the {@link LrcConnection} itself so that the code
 * here is focused purely on message sending, receiving and processing. The LrcConnection is
 * responsible for more higher level functions. 
 * <p/>
 * <b>Configuration</b>
 * <p/>
 * All parts of the Channel are responsible for configuring themselves (Transport, Pipeline). All
 * configuration options are accessed as system properties and the keys that are used for lookups
 * are stored in the {@link Common} class as static variables with the prefix PROP_.
 * <p/>
 * On construction, the Channel will attempt to locate, instantiate and instructure to configure
 * the transport. It will then create the {@link Pipeline} which will attempt to configure its
 * stack based on RID file data. See its javadoc for full details. 
 */
public class Channel
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private String name;
	private Pipeline pipeline;
	private IPacketReceiver packetReceiver;
	private UdpTransport transport;
	private Roster roster;
	
	// queues for incoming and outgoing messages
	private BlockingQueue<Packet> outgoingQueue;
	private BlockingQueue<Packet> incomingQueue;
	
	// calls into a channel are generally asynchronous. However, at certain time,
	// synchronous calls are supported. See the sendAndWait() method
	private Integer awaitingSerial;
	private Packet awaitingReponse;
	private long syncTimeout;
	
	// threads for processing queues
	private List<Thread> incomingWorkers;
	private List<Thread> outgoingWorkers;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create and configure a new {@link Channel}. The configuration process will consult the
	 * system properties to determine configuration information. The various properties that can
	 * be set are all listed as static variables of this class (under the PROP_ prefix).
	 */
	public Channel( String name,
	                IPacketReceiver packetReceiver,
	                Map<String,Object> configurationProperties ) throws JConfigurationException
	{
		this.logger = Common.getLogger();
		this.name = name;
		this.packetReceiver = packetReceiver;
		this.outgoingQueue = new LinkedBlockingQueue<Packet>();
		this.incomingQueue = new LinkedBlockingQueue<Packet>();
		this.awaitingSerial = null;
		this.awaitingReponse = null;
		this.syncTimeout = Common.DEFAULT_SYNC_TIMEOUT;
		this.roster = null;
		
		this.incomingWorkers = new ArrayList<Thread>();
		this.outgoingWorkers = new ArrayList<Thread>();
		
		// create and initialize all the configurable components
		if( configurationProperties == null )
			configurationProperties = PorticoConstants.getSystemPropertiesAsMap();

		// get the synchronous message wait timeout period
		String syncTimeoutString = (String)configurationProperties.get( Common.PROP_SYNC_TIMEOUT );
		if( syncTimeoutString != null )
			this.syncTimeout = Long.parseLong( syncTimeoutString );
		
		try
		{
			pushContext();
			configure( configurationProperties );
		}
		finally
		{
			popContext();
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		/////////////////////////////////////////////
		// instantiate and configure the transport //
		/////////////////////////////////////////////
		//String transportType = (String)properties.get( Common.PROP_TRANSPORT );
		//if( transportType == null )
		//	transportType = Common.DEFAULT_TRANSPORT;
		
		//try
		//{
		//	this.transport = ObjectFactory.create( transportType, ITransport.class );
		//}
		//catch( Exception e )
		//{
		//	logger.error( "Could not create PTalk transport: "+transportType, e );
		//	throw new JConfigurationException( e );
		//}
		
		this.transport = new UdpTransport();
		
		// configure the transport
		this.transport.configure( this, properties );
		
		////////////////////////////////////////////
		// instantiate and configure the pipeline //
		////////////////////////////////////////////
		// throws JConfigurationException
		// This has to be done as two separate steps so that protocol implementations can
		// gain local access to the pipeline through the channel when they're being configured/
		// Perviously, all pipeline configuration was done in the constructor, however, this meant
		// that the pipeline had not yet been assigned to the local pipline instance-var yet, so
		// when protocol configuraitons went to get it, they got null.
		this.pipeline = new Pipeline( this );
		this.pipeline.configure( properties );
		
		///////////////////////////////
		// create the worker threads //
		///////////////////////////////
		String workerCount = (String)properties.get( Common.PROP_INCOMING_WORKERS );
		if( workerCount == null )
			workerCount = ""+Common.DEFAULT_WORKER_COUNT;
		for( int i = 0; i < Integer.parseInt(workerCount); i++ )
		{
			Worker worker = new Worker( Direction.INCOMING );
			Thread thread = new Thread( worker, "IncomingWorker("+name+")-"+i );
			thread.setDaemon( true );
			this.incomingWorkers.add( thread );
		}
		
		workerCount = System.getProperty( Common.PROP_OUTGOING_WORKERS );
		if( workerCount == null )
			workerCount = ""+Common.DEFAULT_WORKER_COUNT;
		for( int i = 0; i < Integer.parseInt(workerCount); i++ )
		{
			Worker worker = new Worker( Direction.OUTGOING );
			Thread thread = new Thread( worker, "OutgoingWorker("+name+")-"+i );
			thread.setDaemon( true );
			this.outgoingWorkers.add( thread );
		}
	}

	public void queueForSending( Packet packet )
	{
		try
		{
			this.outgoingQueue.put( packet );
		}
		catch( InterruptedException ie )
		{
			return;
		}
	}

	public void queueForReceiving( Packet packet )
	{
		try
		{
			this.incomingQueue.put( packet );
		}
		catch( InterruptedException ie )
		{
			return;
		}
	}

	/**
	 * Sends the given packet and then waits for the given period of time for a response to come in.
	 * The packet is stamped with a serial number on the way out, and the method blocks until either
	 * another packet with the same serial comes in, or the given timeout occurs. If the timeout
	 * occurs, null will be returned, otherwise, the found packet will be returned.
	 * <p/>
	 * <b><i>NOTE:</i></b> The message will be passed through the pipeline before it is passed back.
	 */
	public Packet sendAndWait( Packet packet, long timeout )
	{
		// store the serial we are waiting for
		this.awaitingSerial = packet.attachSerial();
		this.awaitingReponse = null;
		
		// queue the message for sending and then wait to get a notification
		synchronized( this.awaitingSerial )
		{
			queueForSending( packet );
			try
			{
				this.awaitingSerial.wait( timeout );
			}
			catch( InterruptedException ie )
			{
				// no action required, just move on to see if the serial is here
			}
			
			// timeout is up or we were notified, if the serial is here, return it
			Packet response = this.awaitingReponse;
			this.awaitingReponse = null;
			this.awaitingSerial = null;
			return response;
		}
	}
	
	/**
	 * Same as {@link #sendAndWait(Packet, long)} except that it uses the timeout defined in the
	 * system properties (by {@link Common#PROP_SYNC_TIMEOUT} - defaulting to
	 * {@link Common#DEFAULT_SYNC_TIMEOUT}).
	 */
	public Packet sendAndWait( Packet packet )
	{
		return sendAndWait( packet, syncTimeout );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// Lifecycle Methods ////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	public void connect() throws JRTIinternalError
	{
		pushContext();
		try
		{
    		logger.debug( "Connecting to channel" );
    		
    		// fire up each of the worker threads
    		logger.trace( "Starting ["+outgoingWorkers.size()+"] outgoing worker threads" );
    		for( Thread thread : this.outgoingWorkers )
    			thread.start();
    
    		logger.trace( "Starting ["+incomingWorkers.size()+"] incoming worker threads" );
    		for( Thread thread : this.incomingWorkers )
    			thread.start();
    
    		// tell the transport to fire up
    		this.transport.connect();
    		logger.debug( "Connected to channel, searching for master" );
    		
    		// create a discovery packet and send it to the pipeline
    		Packet packet = new Packet();
    		packet.setHeader( Header.GM, GroupManagement.MessageType.Discovery.byteValue() );
    		// send the packet directly rather than queue it because we want to stall processing.
    		this.pipeline.sendPacket( packet );
    		
    		if( this.roster == null )
    		{
    			logger.error( "Connected to channel, but have no membership roster" );
    			throw new JRTIinternalError("Connected to channel, but have no membership roster");
    		}
		}
		finally
		{
			popContext();
		}
	}
	
	public void disconnect() throws JRTIinternalError
	{
		pushContext();
		try
		{
    		// shut down the transport so no more messages flow in
    		logger.trace( "Disconnecting transport" );
    		this.transport.disconnect();
    
    		// shut down the outgoing worker threads
    		logger.trace( "Shutting down outgoing worker threads" );
    		for( Thread thread : this.outgoingWorkers )
    		{
    			try
    			{
    				thread.interrupt();
    				thread.join();
    			}
    			catch( InterruptedException ie )
    			{
    				// log it and move on
    				logger.error( "Problem shutting down thread ["+thread.getName()+"]: "+
    				              ie.getMessage(), ie );
    				continue;
    			}
    		}
    
    		// shut down the outgoing worker threads
    		logger.trace( "Shutting down incoming worker threads" );
    		for( Thread thread : this.incomingWorkers )
    		{
    			try
    			{
    				thread.interrupt();
    				thread.join();
    			}
    			catch( InterruptedException ie )
    			{
    				// log it and move on
    				logger.error( "Problem shutting down thread ["+thread.getName()+"]: "+
    				              ie.getMessage(), ie );
    				continue;
    			}
    		}
    		
    		logger.debug( "Disconnected PTalk Channel" );
		}
		finally
		{
			popContext();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Get/Set Methods /////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////

	public String getName()
	{
		return this.name;
	}

	public Pipeline getPipeline()
	{
		return this.pipeline;
	}
	
	public UdpTransport getTransport()
	{
		return this.transport;
	}
	
	public IPacketReceiver getPacketReceiver()
	{
		return this.packetReceiver;
	}
	
	public void setPacketReceiver( IPacketReceiver receiver )
	{
		this.packetReceiver = receiver;
	}
	
	// for pushing and popping Log4j Nested Dialogue Context while logging
	private void pushContext()
	{
		ThreadContext.push( "   ("+name+") " );
	}
	
	private void popContext()
	{
		ThreadContext.pop();
	}
	
	/**
	 * The {@link Roster} indicates all the components that are currently in the channel. Initially
	 * it is set to null, but when the channel connects, it should be filled out by the
	 * GroupManagement protocol.
	 */
	public Roster getRoster()
	{
		return this.roster;
	}
	
	public void setRoster( Roster roster )
	{
		this.roster = roster;
	}


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Private Class: Worker //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	private enum Direction{ INCOMING, OUTGOING };

	/**
	 * This class extracts messages from a blocking queue and passes them to the Pipeline for
	 * processing.
	 */
	private class Worker implements Runnable
	{
		private Direction direction;
		
		private Worker( Direction direction )
		{
			this.direction = direction;
		}

		public void run()
		{
			while( true )
			{
				try
				{
					if( direction == Direction.INCOMING )
					{
						Packet packet = incomingQueue.take();
						pipeline.receivePacket( packet );
						
						// check to see if message is response we're waiting for
						if( awaitingSerial != null && packet.hasHeader(Header.SERIAL) )
						{
							int serial = packet.getHeaderAsInt( Header.SERIAL );
							synchronized( awaitingSerial )
							{
								if( serial == awaitingSerial )
								{
									// this is it!
									awaitingReponse = packet;
									awaitingSerial.notifyAll();
								}
							}
						}
					}
					else
					{
						Packet packet = outgoingQueue.take();
						pipeline.sendPacket( packet );
					}
				}
				catch( InterruptedException ie )
				{
					// we've been interrupted, time to get out of here
					return;
				}
				catch( Exception e )
				{
					// FIXME Log this and continue
					String theString = (direction == Direction.OUTGOING) ? "outgoing" : "incoming";
					logger.error( "Error processing "+theString+" packet: " + e.getMessage(), e );
					continue;
				}
			}
		}
	}
}
