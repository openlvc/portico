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
package org.portico2.common.network.protocol;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.network.Connection;
import org.portico2.common.network.Message;
import org.portico2.common.network.ProtocolStack;
import org.portico2.common.network.configuration.protocol.ProtocolConfiguration;
import org.portico2.common.network.transport.Transport;

/**
 * {@link Protocol} implementations sit inside a {@link ProtocolStack}, which in turn sits inside
 * a {@link Connection} object. Within the stack, Protocol objects are arranged in a chain, where
 * each is linked to the next (both forward and back).<p/>
 * 
 * Each Protocol has a connection to the next and previous implementation in the stack so that
 * it can pass messages up or down. If it wants to terminate processing on a message, it can simply
 * not pass the message any further. It can also generate additional internal messages needed to
 * support requests from the host connection (automatic action) or even to support inter-protocol
 * communication. <p/>
 * 
 * A {@link Protocol} implementation should use the methods {@link #passDown(Message)} and
 * {@link #passUp(Message)} to forward the messages on. These will handle the next/previous
 * references appropriately.
 * 
 * So, when a message is received from the application, it is passed to the ProtocolStack, which
 * in turn passes it to the first protocol it contains. That protocol then processes is and through
 * {@link #passDown(Message)} hands it off down the chain, and so on until it reaches the transport
 * where it is put on the network. <p/>
 * 
 * The same is true in reverse. When a message is received from a {@link Transport}, it is passed
 * up the stack to the protocol immediately before the transport, which in turn passes it to the
 * one before it and so on. <p/>
 */
public abstract class Protocol
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Connection hostConnection;
	protected Connection.Host hostType;
	protected Logger logger;
	
	private Protocol previous;
	private Protocol next;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected Protocol()
	{
		this.hostConnection = null;   // set in configure()
		this.hostType = null;         // set in configure()
		this.logger = null;           // set in configure()

		this.previous = null;         // set when added to ProtocolStack
		this.next = null;             // set when added to ProtocolStack
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public final void configure( ProtocolConfiguration configuration, Connection hostConnection )
	{
		this.hostConnection = hostConnection;
		this.logger = hostConnection.getLogger();
		this.hostType = hostConnection.getHost();
		this.doConfigure( configuration, hostConnection );
	}

	/**
	 * This method should be overridden in all child types, but it is never called directly
	 * by external code. Rather, the {@link #configure(ProtocolConfiguration, Connection)}
	 * is called, which in turn extracts the necessary configuration that is generic to all
	 * types before passing execution to this method, giving each specific protocol a change
	 * to configure itself.
	 * 
	 * @param configuration The configuration object for the protocol. Expected to be cast to a sub-type
	 * @param hostConnection The connection the protocol is being deployed into. Can also find out whether
	 *                       we are in the LRC, RTI or Forwarder from here.
	 * @throws JConfigurationException If the is a problem with any of the given configuration data given
	 */
	protected abstract void doConfigure( ProtocolConfiguration configuration, Connection hostConnection )
	    throws JConfigurationException;

	/**
	 * The connection is opening, so it is time for the protocol to ensure it is set up
	 * according to its configuration. 
	 */
	public abstract void open();
	
	/**
	 * The connection is closing, so it is time to close any additional connections we may
	 * have opened, close threads or files and generally clean up.
	 */
	public abstract void close();

	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Passing   /////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A message has been received from the host component and is being passed down towards
	 * the network.
	 * 
	 * @param message The message we received
	 */
	public abstract void down( Message message );
	
	/**
	 * A message has been received from the network and is being passed up towards the host.
	 * 
	 * @param message The message that was recieved.
	 */
	public abstract void up( Message message );


	protected final void passUp( Message message )
	{
		previous.up( message );
	}
	
	protected final void passDown( Message message )
	{
		next.down( message );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public abstract String getName();
	
	// Lets the protocol record who is before and after it 
	public void setNext( Protocol next ) { this.next = next; }
	public void setPrevious( Protocol previous ) { this.previous = previous; }
	public final Protocol next()     { return this.next; }
	public final Protocol previous() { return this.previous; }
	
	public final boolean hasNext() { return this.next != null; }


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
