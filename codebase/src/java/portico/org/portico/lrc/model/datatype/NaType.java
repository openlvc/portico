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
 * This is a special system datatype that represents:
 * <ol>
 *  <li>
 *      A placeholder for a datatype in an Object Model that does not support datatypes (e.g. 
 *      HLA 1.3).
 *  </li>
 *  <li>
 *      Valid places in the FOM where NA can be listed as a datatype (e.g. HLAprivelegeToDelete in 
 *      1516, {@link Alternative} datatypes).
 *  </li>
 * </ol>
 */
public class NaType implements IDatatype, Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final NaType INSTANCE = new NaType();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private NaType()
	{
		
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public String getName()
	{
		return "NA";
	}

	@Override
	public DatatypeClass getDatatypeClass()
	{
		return DatatypeClass.NA;
	}

	@Override
	public IDatatype createUnlinkedClone()
	{
		return this;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
