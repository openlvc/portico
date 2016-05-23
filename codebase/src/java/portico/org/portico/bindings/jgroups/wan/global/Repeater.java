/*
 *   Copyright 2015 The Portico Project
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
package org.portico.bindings.jgroups.wan.global;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Repeater
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private List<Host> hosts;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Repeater()
	{
		this.hosts = new CopyOnWriteArrayList<Host>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void offer( WanMessage message )
	{
		for( Host host : hosts )
		{
			if( host != message.getSource() )
				host.queueForSend( message );
		}
	}

	public void addHost( Host host )
	{
		this.hosts.add( host );
	}
	
	public void removeHost( Host host )
	{
		this.hosts.remove( host );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
