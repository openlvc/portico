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

public class ProtocolStack
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Connection connection;
	private IProtocol[] protocols;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected ProtocolStack( Connection connection )
	{
		this.connection = connection;
		this.protocols = new IProtocol[0];
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public final void down( Message message )
	{
		// pass the message to each protocol
		for( int i = 0; i < protocols.length; i++ )
		{
			if( protocols[i].down(message) == false )
				return;
		}

		// pass to the transport
		this.connection.transport.send( message );
	}
	
	public void up( Message message )
	{
		for( int i = protocols.length-1; i >=0; i-- )
		{
			if( protocols[i].up(message) == false )
				return;
		}
		
		// pass to connection for final processing
		this.connection.receive( message );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Remove all existing protocols from the protocol stack
	 */
	protected void empty()
	{
		this.protocols = new IProtocol[]{};
	}
	
	
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
