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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.portico.lrc.model.datatype.linker.DatatypePlaceholder;

/**
 * This class contains metadata about a FOM Array data type.
 * <p/>
 * An array data type is a homogenous collection of a specified data type. Array data types may
 * be single or multi-dimensional, and each dimension may have a fixed or dynamic cardinality. 
 */
public class ArrayType implements IDatatype, Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 3112252018924L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String          name;
	private IDatatype       datatype;
	private List<Dimension> dimensions;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor for a single dimension array type with Dynamic cardinality
	 * 
	 * @param name the name of the array type
	 * @param datatype the type of data that will be stored in instances of this array
	 */
	public ArrayType( String name, IDatatype datatype )
	{
		this( name, datatype, Dimension.CARDINALITY_DYNAMIC );
	}
	
	/**
	 * Constructor for a single dimension array type with a specified cardinality value
	 * 
	 * @param name the name of the array type
	 * @param datatype the type of data that will be stored in instances of this array
	 * @param cardinality the cardinality of this array type
	 */
	public ArrayType( String name, IDatatype datatype, int cardinality )
	{
		this( name, 
		      datatype, 
		      getDimensionFor(cardinality) );
	}
	
	/**
	 * Constructor for an array type with an arbitrary number of dimensions
	 * <p/>
	 * <b>Note:</b> at least one dimension must be supplied.
	 * 
	 * @param name the name of the array type
	 * @param datatype the type of data that will be stored in instances of this array
	 * @param dimensions the dimensions this array type will contain
	 * @throws IllegalArgumentException if no dimensions are specified
	 */
	public ArrayType( String name, IDatatype datatype, Dimension...dimensions )
	{
		this( name, datatype, Arrays.asList(dimensions) );
	}
	
	/**
	 * Constructor for an array type with an arbitrary number of dimensions
	 * <p/>
	 * <b>Note:</b> at least one dimension must be supplied.
	 * 
	 * @param name the name of the array type
	 * @param datatype the type of data that will be stored in instances of this array
	 * @param dimensions the dimensions this array type will contain
	 * @throws IllegalArgumentException if no dimensions are specified
	 */
	public ArrayType( String name, IDatatype datatype, Collection<? extends Dimension> dimensions )
	{
		if( dimensions.size() == 0 )
			throw new IllegalArgumentException( "array must have at least one dimension" );
		
		this.name       = name;
		this.datatype   = datatype;
		this.dimensions = new ArrayList<Dimension>( dimensions );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public IDatatype getDatatype()
	{
		return this.datatype;
	}
	
	public void setDatatype( IDatatype datatype )
	{
		this.datatype = datatype;
	}
	
	public List<Dimension> getDimensions()
	{
		return new ArrayList<Dimension>( this.dimensions );
	}
	
	public int getCardinalityLowerBound()
	{
		// We are assured at least 1 dimension from the constructor
		Dimension first = this.dimensions.get( 0 );
		return first.getCardinalityLowerBound();
	}
	
	public int getCardinalityUpperBound()
	{
		// We are assured at least 1 dimension from the constructor
		Dimension first = this.dimensions.get( 0 );
		return first.getCardinalityUpperBound();
	}
	
	public boolean isCardinalityDynamic()
	{
		// We are assured at least 1 dimension from the constructor
		Dimension first = this.dimensions.get( 0 );
		return first.isCardinalityDynamic();
	}
	
	@Override
	public boolean equals( Object other )
	{
		boolean equal = false;
		if( other instanceof ArrayType )
		{
			ArrayType asArray = (ArrayType)other;
			equal = Objects.equals( this.name, asArray.name ) &&
			        Objects.equals( this.datatype, asArray.datatype ) &&
			        dimensions.equals( asArray.dimensions );
		}
		
		return equal;
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// IDatatype Interface ////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public DatatypeClass getDatatypeClass()
	{
		return DatatypeClass.ARRAY;
	}
	
	@Override
	public ArrayType createUnlinkedClone()
	{
		List<Dimension> dimensions = new ArrayList<Dimension>();
		for( Dimension dimension : this.getDimensions() )
		{
			Dimension clone = new Dimension( dimension.getCardinalityLowerBound(), 
			                                 dimension.getCardinalityUpperBound() ); 
			dimensions.add( clone );
		}
		
		return new ArrayType( this.name, new DatatypePlaceholder(this.datatype), dimensions );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static Dimension getDimensionFor( int cardinality )
	{
		return cardinality == Dimension.CARDINALITY_DYNAMIC ? Dimension.DYNAMIC :
		                                                      new Dimension( cardinality );
	}
}
