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
package org.portico.lrc.model.datatype.linker;

import java.io.Serializable;

import org.portico.lrc.model.datatype.Alternative;
import org.portico.lrc.model.datatype.EnumeratedType;
import org.portico.lrc.model.datatype.IEnumerator;

/**
 * This type is used as a placeholder while parsing and merging Variant Record {@link Alternative}
 * entries. At parse time, the list of available {@link EnumeratedType} values is incomplete and as
 * such we are unable to tell if the Alternative's enumerator field contains a valid enumerator 
 * value. 
 */
public class EnumeratorPlaceholder implements IEnumerator, Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 3112252018924L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	public String name;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public EnumeratorPlaceholder( String name )
	{
		this.name = name;
	}
	
	public EnumeratorPlaceholder( IEnumerator enumerator )
	{
		this.name = enumerator.getName();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public Number getValue()
	{
		return 0;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
