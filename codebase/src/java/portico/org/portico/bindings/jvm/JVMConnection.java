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
package org.portico.bindings.jvm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.bindings.ConnectedRoster;
import org.portico.bindings.IConnection;
import org.portico.bindings.jgroups.Roster;
import org.portico.lrc.LRC;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.compat.JFederatesCurrentlyJoined;
import org.portico.lrc.compat.JFederationExecutionAlreadyExists;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.services.federation.msg.CreateFederation;
import org.portico2.common.services.federation.msg.DestroyFederation;
import org.portico2.common.services.federation.msg.JoinFederation;
import org.portico2.common.services.federation.msg.ResignFederation;

/**
 * The {@link JVMConnection} allows federates to work in a "shared memory" like environment. Rather
 * than sending messages out over a network, all messages are passed directly to the other
 * connections via direct method calls. This obviously restricts all components to operating in the
 * same execution environment (hence the "JVM" term). Generally speaking, different federates will
 * be run in separate threads within the same JVM.
 */
public class JVMConnection implements IConnection
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** The timeout value for the connection to wait for responses */
	public static final long CONNECTION_TIMEOUT = 100;

	private static Map<String,Broadcaster> FEDERATIONS = new HashMap<String,Broadcaster>();
	private static int JVM_CONNECTION_IDS = 0;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected LRC lrc;
	protected String uniqueID;
	protected Broadcaster federation;
	protected int localHandle;
	protected Logger logger;
	protected Map<Integer,PorticoMessage> responses;
	protected Lock responseLock;
	protected Condition responseCondition;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public JVMConnection()
	{
		this.localHandle = PorticoConstants.NULL_HANDLE;
		this.responses = new HashMap<Integer,PorticoMessage>();
		this.responseLock = new ReentrantLock();
		this.responseCondition = this.responseLock.newCondition();
		synchronized( JVMConnection.class )
		{
			this.uniqueID = ""+(++JVM_CONNECTION_IDS);
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// lifecycle methods //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	public void configure( LRC lrc, Map<String,Object> properties )
		throws JConfigurationException
	{
		this.lrc = lrc;
		this.logger = LogManager.getFormatterLogger( "portico.lrc.jvmconn" );
	}
	
	public void connect()
	{
		
	}
	
	public void disconnect()
	{
		
	}
	
	public String getUniqueID()
	{
		return uniqueID;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// message sending methods ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sends the request to all other federates associated with the same {@link Broadcaster}
	 * as this connection and then returns.
	 */
	public void broadcast( PorticoMessage message ) throws Exception
	{
		federation.broadcast( message );
	}

	/**
	 * Sends the request to all other federates associated with the same {@link Broadcaster}
	 * as this connection and then sleeps for a period defined in {@link #CONNECTION_TIMEOUT}
	 * milliseconds.
	 */
	public void broadcastAndSleep( PorticoMessage message ) throws Exception
	{
		federation.broadcast( message );
		PorticoConstants.sleep( CONNECTION_TIMEOUT );
	}

	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// bootstrapping methods ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	public synchronized void createFederation( CreateFederation createMessage ) throws Exception
	{
		if( FEDERATIONS.containsKey(createMessage.getFederationName()) )
			throw new JFederationExecutionAlreadyExists( createMessage.getFederationName() );
		
		FEDERATIONS.put( createMessage.getFederationName(),
		                 new Broadcaster(createMessage.getModel()) );
		logger.debug( "Created new federation [" + createMessage.getFederationName() + "]" );
	}

	public synchronized void destroyFederation( DestroyFederation destroyMessage ) throws Exception
	{
		// check that the federation exists and doesn't contain members
		String name = destroyMessage.getFederationName();
		Broadcaster broadcaster = FEDERATIONS.get( name );
		if( broadcaster == null )
		{
			throw new JFederationExecutionDoesNotExist( "federation doesn't exist: " + name );
		}
		else if( broadcaster.isEmpty() == false )
		{
			throw new JFederatesCurrentlyJoined( "federates currently joined to: " + name );
		}
		else
		{
			FEDERATIONS.remove( name );
			logger.debug( "Destroyed federation [" + name + "]" );
		}
	}
	
	public synchronized ConnectedRoster joinFederation( JoinFederation joinMessage ) throws Exception
	{
		// find the federation
		String federation = joinMessage.getFederationName();
		String federateName = joinMessage.getFederateName();
		String federateType = joinMessage.getFederateType();
		
		Broadcaster broadcaster = FEDERATIONS.get( federation );
		if( broadcaster == null )
		{
			throw new JFederationExecutionDoesNotExist( "federation doesn't exist: " + federation );
		}
		
		// Merge the provided join modules with the existing FOM - this method will
		// perform a dry-run first to ensure that things can be merged happily
		logger.debug( "Merge ["+joinMessage.getParsedJoinModules().size()+"] modules into existing FOM" );
		broadcaster.extendFOM( joinMessage.getParsedJoinModules() );

		// join the federation
		// this will check to see if there is already a federate with the same name
		this.localHandle = broadcaster.joinLrc( federateName, federateType, this.lrc );

		// store the federation broadcaster locally
		this.federation = broadcaster;
		
		// pack the FOM into the request object for use by the framework
		//  -yes, you are seeing right, that says "JGroups" roster. We're "borrowing" their impl
		logger.debug( String.format("Joined federate [%s] to federation [%s]", 
		                            federateName, federation ) );
		ConnectedRoster roster = new Roster( localHandle,
		                                     broadcaster.getFederateHandles(),
		                                     broadcaster.getFOM() );
		return roster;
	}
	
	public synchronized void resignFederation( ResignFederation resignMessage ) throws Exception
	{
		// make sure we are joined to a federation in the first place
		if( this.federation == null )
			throw new JFederateNotExecutionMember( "not joined to a federation" );

		// send the resign message to the federation so they know we're on the way out
		broadcast( resignMessage );

		// try to remove from the federation, if not joined, throw an exception
		String federate = resignMessage.getFederateName();
		String federation = resignMessage.getFederationName();
		this.federation.removeLrc( federate );
		this.federation = null;
		this.localHandle = PorticoConstants.NULL_HANDLE;
		logger.debug( "Resigned federate [" + federate + "] from federation [" + federation + "]" );
	}

	/**
	 * Returns a string array containing the names of all the currently active federations. Returns
	 * an empty array if there are none.
	 */
	public synchronized String[] listActiveFederations() throws Exception
	{
		return FEDERATIONS.keySet().toArray( new String[0] );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
