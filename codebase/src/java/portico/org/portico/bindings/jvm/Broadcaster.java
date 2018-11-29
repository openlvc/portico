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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRC;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JFederateAlreadyExecutionMember;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.compat.JInconsistentFDD;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ModelMerger;
import org.portico.lrc.model.ObjectModel;
import org.portico.utils.messaging.PorticoMessage;

/**
 * For each federation the JVM binding knows about, there exists a single {@link Broadcaster}. Any
 * number of {@link JVMConnection}s can register themselves with the broadcaster (assuming they
 * each use a unique name). It is the Broadcasters responsibility to relay messages to each of the
 * connections associated with the federation.
 */
public class Broadcaster
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<FederateInfo,JVMConnection> joinedConnections;
	private ObjectModel fom;
	private volatile int FEDERATE_HANDLES = 0;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Broadcaster( ObjectModel fom )
	{
		this.fom = fom;
		this.joinedConnections = new HashMap<FederateInfo,JVMConnection>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private synchronized JVMConnection getConnection( String federateName )
	{
		for( FederateInfo info : joinedConnections.keySet() )
		{
			if( info.federateName.equals(federateName) )
				return joinedConnections.get( info );
		}
		
		return null;
	}
	
	private synchronized JVMConnection removeConnection( String federateName )
	{
		FederateInfo targetInfo = null;
		for( FederateInfo temp : joinedConnections.keySet() )
		{
			if( temp.federateName.equals(federateName) )
			{
				targetInfo = temp;
				break;
			}
		}

		return joinedConnections.remove( targetInfo );
	}

	/**
	 * Joins a {@link JVMConnection} to the federation represented by this broadcaster. The return
	 * value is a unique handle that can be used to identify the connection within the federation.
	 */
	public synchronized int joinLrc( String name, String type, LRC lrc )
		throws JFederateAlreadyExecutionMember, JRTIinternalError
	{
		if( PorticoConstants.isUniqueFederateNamesRequired() && getConnection(name) != null )
			throw new JFederateAlreadyExecutionMember( "federate with name already joined: "+name );

		// make sure we have a JVMConnection
		if( (lrc.getConnection() instanceof JVMConnection) == false )
		{
			throw new JRTIinternalError( "mixed-connection federations not yet supported: " +
			                             "JVMConnection expected, found " +
			                             lrc.getConnection().getClass().getSimpleName() );
		}
		
		int federateHandle = ++FEDERATE_HANDLES;
		FederateInfo federateInfo = new FederateInfo( federateHandle, name, type );
		this.joinedConnections.put( federateInfo, (JVMConnection)lrc.getConnection() );
		return federateHandle;
	}

	/**
	 * Removes a {@link JVMConnection} from this broadcaster. If the kernel represents a federate,
	 * this would generally be done on resignation.
	 */
	public synchronized void removeLrc( String name ) throws JFederateNotExecutionMember
	{
		if( removeConnection(name) == null )
			throw new JFederateNotExecutionMember( "federate not member: "+name );
	}

	/**
	 * Sends the given message to the federation. A clone of the message will be produced and
	 * passed to each federate registered with the broadcaster.
	 */
	public synchronized void broadcast( PorticoMessage message ) throws Exception
	{
		for( JVMConnection connection : joinedConnections.values() )
		{
			// FIX PORT-693: Clone the request so that each kernel can modify it without
			//               having to worry about affecting others
			PorticoMessage clone = message.clone( PorticoMessage.class );
			connection.lrc.getState().getQueue().offer( clone );
		}
	}
	
	/**
	 * Returns <code>true</code> if no federates have been registered with the broadcaster
	 */
	public synchronized boolean isEmpty()
	{
		return this.joinedConnections.isEmpty();
	}

	/**
	 * Gets the {@link ObjectModel} associated with the federation that this broadcaster controls
	 */
	public synchronized ObjectModel getFOM()
	{
		return this.fom;
	}

	/**
	 * Extend the existing FOM with the given join modules. This call will first do a dry-run
	 * to ensure that the modules can be happily merged before it actually goes ahead and does
	 * the merge. If there is a problem detected during this dry run, an exception will be thrown.
	 */
	public synchronized void extendFOM( List<ObjectModel> joinModules ) throws JInconsistentFDD,
	                                                                           JRTIinternalError
	{
		// do a dry run before we actually do any merge to ensure that things will work
		ModelMerger.mergeDryRun( this.fom, joinModules );
		
		// if we get here without exception, we're all good to do the proper merge
		this.fom = ModelMerger.merge( this.fom, joinModules );
	}
	
	public Set<Integer> getFederateHandles()
	{
		Set<Integer> handles = new HashSet<Integer>();
		for( JVMConnection connection : joinedConnections.values() )
			handles.add( connection.localHandle );
		
		return handles;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	private class FederateInfo
	{
		public int federateHandle;
		public String federateName;
		public String federateType;

		public FederateInfo( int federateHandle, String federateName )
		{
			// use federate name as federate type
			this( federateHandle, federateName, federateName );
		}

		public FederateInfo( int federateHandle, String federateName, String federateType )
		{
			this.federateHandle = federateHandle;
			this.federateName = federateName;
			this.federateType = federateType;
		}
		
        public int hashcode()
		{
			return federateName.hashCode();
		}
	}
}
