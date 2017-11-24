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

/**
 * Represents the discrete datatypes that can be specified in the FOM.
 */
public enum DatatypeClass
{
	/** 
	 * Underpinning of all OMT datatypes
	 */
	BASIC,
	/** 
	 * Simple, scalar data items
	 */
	SIMPLE,
	/** 
	 * Data elements that can take on a finite discrete set of possible values
	 */
	ENUMERATED,
	/**
	 * Indexed homogenous collections of datatypes
	 */
	ARRAY,
	/**
	 * Heterogeneous collections of types
	 */
	FIXEDRECORD,
	/**
	 * Discriminated unions of types
	 */
	VARIANTRECORD,
	/**
	 * NA type (supports HLAprivelegeToDelete in 1516)
	 */
	NA;
	
	@Override
	public String toString()
	{
		switch( this )
		{
			case BASIC:
				return "basicData";
			case SIMPLE:
				return "simpleData";
			case ENUMERATED:
				return "enumeratedData";
			case ARRAY:
				return "arrayData";
			case FIXEDRECORD:
				return "fixedRecordData";
			case VARIANTRECORD:
				return "variantRecordData";
			default:
			case NA:
				return "NA";
		}
	}
}
