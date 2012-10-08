/*
 *   Copyright 2006 The Portico Project
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
package org.portico.lrc.model;

import java.io.Serializable;

/**
 * This class contains metadata about a FOM attribute class 
 */
public class ACMetadata implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String        name;
	private int           handle;
	private Order         order;
	private Transport     transport;
	private OCMetadata    container;
	private Space space;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * <b>NOTE:</b> This constructor should generally not be used. If you want an instance of this
	 * class you should use the creation methods of {@link ObjectModel ObjectModel}.
	 */
	public ACMetadata( String name, int handle )
	{
		this.name      = name;
		this.handle    = handle;
		this.order     = Order.TIMESTAMP;
		this.transport = Transport.RELIABLE;
		this.container = null;
		this.space     = null;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Creates and returns an {@link ACInstance} of this type
	 */
	public ACInstance newInstance()
	{
		return new ACInstance( this );
	}
	
	/////////////////////////////////////////////////////////////
	///////////////////// Basic Get and Set /////////////////////
	/////////////////////////////////////////////////////////////
	public String getName()
	{
		return this.name;
	}
	
	public int getHandle()
	{
		return this.handle;
	}
	
	public Order getOrder()
	{
		return this.order;
	}
	
	public void setOrder( Order order )
	{
		this.order = order;
	}

	public boolean isRO()
	{
		return this.order == Order.RECEIVE;
	}
	
	public boolean isTSO()
	{
		return this.order == Order.TIMESTAMP;
	}
	
	public Transport getTransport()
	{
		return this.transport;
	}
	
	public void setTransport( Transport transport )
	{
		this.transport = transport;
	}
	
	public Space getSpace()
	{
		return this.space;
	}

	public void setSpace( Space theSpace )
	{
		this.space = theSpace;
	}

	public OCMetadata getContainer()
	{
		return this.container;
	}
	
	public void setContainer( OCMetadata container )
	{
		this.container = container;
	}

	/**
	 * @return true if the other object is another {@link ACMetadata} and has the same
	 *         name, order and transport. False otherwise.
	 */
	public boolean equals( Object other )
	{
		if( other instanceof ACMetadata )
		{
			ACMetadata otherMetadata = (ACMetadata)other;
			if( name.equals(otherMetadata.name) &&
				order.equals(otherMetadata.order) &&
				transport.equals(otherMetadata.transport) )
			{
				return true;
			}
		}

		return false;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
