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
package org.portico2.common.network;

import java.util.ArrayList;
import java.util.List;

import org.portico.lrc.compat.JConfigurationException;

public class ProtocolStack
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Connection connection;
	private List<IProtocol> protocols;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected ProtocolStack( Connection connection )
	{
		this.connection = connection;
		this.protocols = new ArrayList<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public final void down( Message message )
	{
		// pass the message to each protocol
		for( int i = 0; i < protocols.size(); i++ )
		{
			if( protocols.get(i).down(message) == false )
				return;
		}

		// pass to the transport
		this.connection.transport.send( message );
	}
	
	public void up( Message message )
	{
		for( int i = protocols.size()-1; i >=0; i-- )
		{
			if( protocols.get(i).up(message) == false )
				return;
		}
		
		// pass to connection for final processing
		this.connection.receive( message );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Protocol Management Methods   /////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void addProtocol( IProtocol protocol ) throws JConfigurationException
	{
		// check to make sure we don't have this protocol already
		for( IProtocol temp : protocols )
		{
			if( temp.getName().equalsIgnoreCase(protocol.getName()) )
				throw new JConfigurationException( "Already have instance of protocol in stack: %s", protocol.getName() );
		}
		
		// configure the protocol and add it
		protocol.configure( connection );
		protocol.open();
		protocols.add( protocol );
	}
	
	public IProtocol removeProtocol( IProtocol protocol )
	{
		if( protocols.remove(protocol) == false )
			return null;
		
		protocol.close();
		return protocol;
	}

	/**
	 * Remove all existing protocols from the protocol stack
	 */
	protected void empty()
	{
		this.protocols.clear();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
