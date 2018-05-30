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

/**
 * {@link IProtocol} implementations sit inside a {@link ProtocolStack}, which in turn sits inside
 * a {@link Connection} object. On sending or receiving of a message, the contents are first passed
 * to each of the protocol implementations before being forwarded either to the connection to send,
 * or back up to the application for incoming messages.
 */
public interface IProtocol
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void configure( Connection hostConnection );
	public void open();
	public void close();

	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Passing   /////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public boolean down( Message message );
	public boolean up( Message message );

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public String getName();

}
