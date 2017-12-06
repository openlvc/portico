/*
 *   Copyright 2017 The Portico Project
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
package org.portico.lrc.model.datatype;

import java.io.Serializable;

/**
 * Implementation of the {@link IEnumerator} interface
 */
public class Enumerator implements IEnumerator, Serializable, Comparable<Enumerator>
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/**
	 * Special <code>HLAother</code> value that acts as a wild card for Variant Record alternatives 
	 */
	public static Enumerator HLA_OTHER = new Enumerator( "HLAother", Long.MAX_VALUE );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;
	private Number value;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Enumerator( String name, Number value )
	{
		this.name = name;
		this.value = value;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public String toString()
	{
		return this.name;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// IEnumerator Interface ////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	public String getName()
	{
		return this.name;
	}
	
	public Number getValue()
	{
		return this.value;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Comparable Interface ////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int compareTo( Enumerator other )
	{
		long thisValue = this.value.longValue();
		long otherValue = other.value.longValue();
		
		if( thisValue == otherValue )
			return 0;
		else if( thisValue > otherValue )
			return 1;
		else 
			return -1;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
