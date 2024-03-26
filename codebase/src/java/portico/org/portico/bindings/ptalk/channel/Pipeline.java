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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.logging.log4j.Logger;
import org.portico.bindings.ptalk.Common;
import org.portico.bindings.ptalk.protocol.IProtocol;
import org.portico.bindings.ptalk.protocol.Protocol;
import org.portico.bindings.ptalk.transport.UdpTransport;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.ObjectFactory;
import org.portico.utils.annotations.AnnotationLocator;

/**
 * A {@link Pipeline} is the main message processing component in the PTalk framework. Any number of
 * {@link Worker} threads can drop messages into the pipeline for processing either before a
 * message is given to an {@link org.portico.bindings.ptalk.transport.ITransport} for sending, or
 * after one has been received from the transport for internal consumption.
 * <p/>
 * Each Pipeline contains an ordered list of 0..n {@link IProtocol}s. As a {@link Packet} is sent
 * or received, it is passed to each protocol in turn. The order in which packets are passed to
 * protocols depends on the direction of the message.
 * <p/>
 * Take a pipeline configured as follows:
 * <pre>
 * [LRC]
 * [0] Protocol1 
 * [1] Protocol2 
 * [2] Protocol3 
 * [3] Protocol4
 * [Transport] 
 * </pre>
 * For messages that are generated in the LRC (to be sent to the network), they pass through the
 * pipeline according to the order in which the protocols were added. For messages generated in the
 * transport and destined for the LRC, they pass through the pipeline in the reverse order.
 * 
 * <p/>
 * <b>Pipeline Configuration</b>
 * <p/>
 * A Pipeline draws configuration information from the system propery
 * {@link Common#PROP_STACK}. The value of this property should be a comma-separated list
 * of protocol names. The names in the list come from each Protocols {@link Protocol} annotation.
 * Any protocol that exists in Portico's classpath can be accessed. To make use of new protocols,
 * their jar files will have to be dropped in Portico's plugin directory and then their names
 * specified at the appropriate place in the stack in the RID file.
 * <p/>
 * For example, to configure a pipeline with the same setup at defined earlier, you would need an
 * entry like so in the RID file:
 * <p/>
 * <code>portico.ptalk.stack=Protocol1,Protocol2,Protocol3,Protocol4</code>
 */
public class Pipeline
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** Store all the available protocols found on the classpath. {@link #scanForProtocols()} will
	    initialize this store */
	private HashMap<String,Class<?>> AVAILABLE_PROTOCOLS = new HashMap<String,Class<?>>();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private Channel channel;
	private IPacketReceiver packetReceiver;
	private UdpTransport transport;
	private IProtocol[] protocols;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create and configure a new {@link Pipeline}. This will consult the system properties to get
	 * the stack configuration (see {@link Common#PROP_STACK})
	 */
	protected Pipeline( Channel channel )
		throws JConfigurationException
	{
		this.logger = Common.getLogger();
		this.channel = channel;
		this.transport = channel.getTransport();
		this.packetReceiver = channel.getPacketReceiver();
		this.protocols = new IProtocol[0];

		// if our list of available protocols isn't already set, trigger a scan now
		scanForProtocols();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This method tries to read the pipeline stack configuration from the provided properties
	 * (looking under the {@link Common#PROP_STACK} key). When found, it will attempt to
	 * instantiate and configure each of the protocols.
	 */
	protected void configure( Map<String,Object> configurationProperties )
	{
		// detemine the protocol stack to use
		String stackConfiguration = (String)configurationProperties.get( Common.PROP_STACK );
		if( stackConfiguration == null || stackConfiguration.equals("") )
		{
			stackConfiguration = Common.DEFAULT_STACK;
			logger.debug( "Using default stack. No config found in "+Common.PROP_STACK );
		}
		
		logger.debug( "(Stack) PTalk Stack: "+stackConfiguration );
		
		// break the stack down into individual components. try to instantiate each protocol
		// and add it to the pipeline
		StringTokenizer tokenizer = new StringTokenizer( stackConfiguration, "," );
		while( tokenizer.hasMoreTokens() )
		{
			String requestedProtocol = tokenizer.nextToken().trim();
			// create a new instance of the protocol, assuming we know about it
			Class<?> protocolClass = AVAILABLE_PROTOCOLS.get( requestedProtocol );
			if( protocolClass == null )
			{
				String reason = "Protocol ["+requestedProtocol+"] is unknown";
				logger.error( "Error configuring protocol stack: " + reason );
				throw new JConfigurationException( reason );
			}
			
			// instantiate and add the protocol
			try
			{
				IProtocol protocol = ObjectFactory.create( protocolClass, IProtocol.class );
				protocol.configure( channel, configurationProperties );
				addProtocol( protocol );
			}
			catch( Exception e )
			{
				logger.error( "Error configuring protocol stack: " + e.getMessage() );
				throw new JConfigurationException( "Error configuring protocol stack", e );
			}
		}

		logger.debug( "PTalk stack configured" );
	}
	
	/**
	 * This method looks up the pipeline chain to find the first instance of the Protocol of the
	 * provided type. This is useful when protocols must work together and need access to one
	 * another. It can also be used configuration to ensure that the pipeline is arranged properly.
	 * 
	 * @param protocolType The class representing the protocol we are interested in
	 * @param startPoint The protocol to start looking from
	 * @return The first instance of the provided protocol class found when jumping up the pipeline
	 * from the provided starting point
	 */
	public <T extends IProtocol> T getParentProtocol( Class<T> protocolType, IProtocol startPoint )
	{
		// find the desired protocol first, then check to make sure the start point is BELOW it
		T targetProtocol = null;
		int targetIndex = -1;
		for( int i = 0; i < protocols.length; i++ )
		{
			if( protocolType.isInstance(protocols[i]) )
			{
				// found it, break on out of funky town
				targetProtocol = protocolType.cast( protocols[i] );
				targetIndex = i;
				break;
			}
		}
		
		// make sure we found the protocol
		if( targetProtocol == null )
			return null;
		
		// if we were given a start point, make sure we have it BELOW where we are
		if( startPoint != null )
		{
			for( int i = targetIndex+1; i < protocols.length; i++ )
			{
				if( protocols[i].equals(startPoint) )
					return targetProtocol;
			}
			
			// had a start point, but specified protocol wasn't below it. fail.
			return null;
		}
		else
		{
			return targetProtocol;
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	/////////////////////// Configuration Methods ///////////////////////
	/////////////////////////////////////////////////////////////////////
	/**
	 * Scans the classpath for available {@link Protocol} implementations and stores them in the
	 * {@link #AVAILABLE_PROTOCOLS} static.
	 */
	private void scanForProtocols() throws JConfigurationException
	{
		// only scan if the protocol list is empty
		if( AVAILABLE_PROTOCOLS.isEmpty() == false )
			return;
		
		logger.debug( "Scanning Portico classpath for all PTalk Protocol implementations" );
		Set<Class<?>> located = null;
		try
		{
			located = AnnotationLocator.locateClassesWithAnnotation( Protocol.class );
		}
		catch( Exception e )
		{
			logger.error( "Error scanning for Protocol implementations: " + e.getMessage(), e );
			throw new JConfigurationException( e );
		}
		
		// loop through each of the found protocols. they should all have the annotation if they
		// were returned to us by the AnnotationLocator
		logger.debug( "Located ["+located.size()+"] PTalk Protocol Implementations" );
		for( Class<?> clazz : located )
		{
			Protocol annotation = clazz.getAnnotation( Protocol.class );
			AVAILABLE_PROTOCOLS.put( annotation.name(), clazz );
			logger.debug("  (Protocol) name="+annotation.name()+", class="+clazz.getCanonicalName());
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	///////////////////// Packet Processing Methods /////////////////////
	/////////////////////////////////////////////////////////////////////
	/**
	 * Take a packet, pass it "downwards" through the stack (from start to end) as it is about
	 * to be sent to the network for transmission.
	 */
	public void sendPacket( Packet packet ) throws JRTIinternalError
	{
		for( int i = 0; i < protocols.length; i++ )
		{
			if( protocols[i].outgoing(packet) == false )
				return;
		}
		
		transport.sendToNetwork( packet );
	}
	
	/**
	 * Take the given packet and pass it "upwards" through the stack (from end to start) as it
	 * is destined for the LRC having been received from the network.
	 */
	public void receivePacket( Packet packet ) throws JRTIinternalError
	{
		for( int i = protocols.length-1; i >= 0; i-- )
		{
			if( protocols[i].incoming(packet) == false )
				return;
		}
		
		packetReceiver.receive( packet );
	}

	/**
	 * This method will add the protocol to the pipeline. 
	 */
	public void addProtocol( IProtocol protocol )
	{
		// grow the size of the array
		IProtocol[] newArray = new IProtocol[protocols.length+1];
		for( int i = 0; i < protocols.length; i++ )
			newArray[i] = protocols[i];
		
		newArray[newArray.length-1] = protocol;
		this.protocols = newArray;
	}
	
	public List<IProtocol> getProtocols()
	{
		return Arrays.asList( this.protocols );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
