/*
 *   Copyright 2007 The Portico Project
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
package hlaunit.ieee1516.common;

import java.util.Map;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleSet;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleValueMap;

import org.portico.impl.hla1516.types.DoubleTime;
import org.portico.impl.hla1516.types.DoubleTimeInterval;
import org.portico.impl.hla1516.types.HLA1516AttributeHandle;
import org.portico.impl.hla1516.types.HLA1516AttributeHandleSet;
import org.portico.impl.hla1516.types.HLA1516AttributeHandleValueMap;
import org.portico.impl.hla1516.types.HLA1516FederateHandle;
import org.portico.impl.hla1516.types.HLA1516FederateHandleSet;
import org.portico.impl.hla1516.types.HLA1516Handle;
import org.portico.impl.hla1516.types.HLA1516InteractionClassHandle;
import org.portico.impl.hla1516.types.HLA1516ObjectClassHandle;
import org.portico.impl.hla1516.types.HLA1516ObjectInstanceHandle;
import org.portico.impl.hla1516.types.HLA1516ParameterHandle;
import org.portico.impl.hla1516.types.HLA1516ParameterHandleValueMap;
import org.testng.Assert;

public class TypeFactory
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Set/Map Creation Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public static AttributeHandleSet newAttributeSet( int... handles )
	{
		try
		{
			AttributeHandleSet set = new HLA1516AttributeHandleSet();
			for( int handle : handles )
			{
				set.add( new HLA1516AttributeHandle(handle) );
			}
			return set;
		}
		catch( Exception e )
		{
			// This should hopefully never happen, unless we're stupid and pass negative handles
			Assert.fail( "Couldn't create AttributeHandleSet: " + e.getMessage(), e );
			return null;
		}
	}
	
	public static FederateHandleSet newFederateSet( int... handles )
	{
		try
		{
			FederateHandleSet set = new HLA1516FederateHandleSet();
			for( int handle : handles )
			{
				set.add( new HLA1516FederateHandle(handle) );
			}
			return set;
		}
		catch( Exception e )
		{
			Assert.fail( "Couldn't create FederateHandleSet: " + e.getMessage(), e );
			return null;
		}
	}
	
	public static AttributeHandleValueMap newAttributeMap( Map<Integer,byte[]> values )
	{
		try
		{
			AttributeHandleValueMap map = new HLA1516AttributeHandleValueMap();
			for( Integer handle : values.keySet() )
			{
				map.put( new HLA1516AttributeHandle(handle), values.get(handle) );
			}
			return map;
		}
		catch( Exception e )
		{
			Assert.fail( "Couldn't create the AttributeHandleValueMap: " + e.getMessage(), e );
			return null;
		}
	}
	
	public static AttributeHandleValueMap newAttributeMap()
	{
		return new HLA1516AttributeHandleValueMap();
	}
	
	public static ParameterHandleValueMap newParameterMap( Map<Integer,byte[]> values )
	{
		try
		{
			ParameterHandleValueMap map = new HLA1516ParameterHandleValueMap();
			for( Integer handle : values.keySet() )
			{
				map.put( new HLA1516ParameterHandle(handle), values.get(handle) );
			}
			return map;
		}
		catch( Exception e )
		{
			Assert.fail( "Couldn't create the ParameterHandleValueMap: " + e.getMessage(), e );
			return null;
		}
	}
	
	public static ParameterHandleValueMap newParameterMap()
	{
		return new HLA1516ParameterHandleValueMap();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Time Creation Methods ////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	
	public static LogicalTime createTime( double time )
	{
		return new DoubleTime( time );
	}
	
	public static double fromTime( LogicalTime time )
	{
		try
		{
			return DoubleTime.fromTime( time );
		}
		catch( Exception e )
		{
			Assert.fail( "Couldn't convert LogicalTime->double: " + e.getMessage(), e );
			return -1.0;
		}
	}
	
	public static LogicalTimeInterval createInterval( double time )
	{
		return new DoubleTimeInterval( time );
	}
	
	public static double fromInterval( LogicalTimeInterval interval )
	{
		try
		{
			return DoubleTimeInterval.fromInterval( interval );
		}
		catch( Exception e )
		{
			Assert.fail( "Couldn't convert LogicalTime->double: " + e.getMessage(), e );
			return -1.0;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Handle Creation Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * This method will create a new Portico FederateHandle implementation for the given
	 * integer handle and will return it. It will not search the FOM.
	 */
	public FederateHandle getFederateHandle( int handle )
	{
		return new HLA1516FederateHandle( handle );
	}

	/**
	 * Convert the given federate handle into its interger representation
	 */
	public static int getFederateHandle( FederateHandle handle )
	{
		try
		{
			return HLA1516Handle.fromHandle( handle );
		}
		catch( Exception e )
		{
			Assert.fail( "Couldn't convert FederateHandle", e );
			return -1;
		}
	}
	
	/**
	 * This method will create a new Portico ObjectInstanceHandle implementation for the given
	 * integer handle and will return it. It will not search the FOM.
	 */
	public static ObjectInstanceHandle getObjectHandle( int handle )
	{
		return new HLA1516ObjectInstanceHandle( handle );
	}

	/**
	 * Convert the given object instance handle into its interger representation
	 */
	public static int getObjectHandle( ObjectInstanceHandle handle )
	{
		try
		{
			return HLA1516Handle.fromHandle( handle );
		}
		catch( Exception e )
		{
			Assert.fail( "Couldn't convert ObjectInstanceHandle", e );
			return -1;
		}
	}
	
	/**
	 * This method will create a new Portico ObjectClassHandle implementation for the given integer
	 * handle and will return it. It will not search the FOM.
	 */
	public static ObjectClassHandle getObjectClassHandle( int handle )
	{
		return new HLA1516ObjectClassHandle( handle );
	}

	/**
	 * Convert the given object class handle into its interger representation
	 */
	public static int getObjectClassHandle( ObjectClassHandle handle )
	{
		try
		{
			return HLA1516Handle.fromHandle( handle );
		}
		catch( Exception e )
		{
			Assert.fail( "Couldn't convert ObjectClassHandle", e );
			return -1;
		}
	}
	
	/**
	 * This method will create a new Portico AttributeHandle implementation for the given integer
	 * handle and will return it. It will not search the FOM.
	 */
	public static AttributeHandle getAttributeHandle( int handle )
	{
		return new HLA1516AttributeHandle( handle );
	}
	
	/**
	 * Convert the given attribute handle into its interger representation
	 */
	public static int getAttributeHandle( AttributeHandle handle )
	{
		try
		{
			return HLA1516AttributeHandle.fromHandle( handle );
		}
		catch( Exception e )
		{
			Assert.fail( "Couldn't convert AttributeHandle", e );
			return -1;
		}
	}
	
	/**
	 * This method will create a new Portico AttributeHandle implementation for the given integer
	 * handle and will return it. It will not search the FOM.
	 */
	public static ParameterHandle getParameterHandle( int handle )
	{
		return new HLA1516ParameterHandle( handle );
	}
	
	/**
	 * Convert the given parameter handle into its interger representation
	 */
	public static int getParameterHandle( ParameterHandle handle )
	{
		try
		{
			return HLA1516Handle.fromHandle( handle );
		}
		catch( Exception e )
		{
			Assert.fail( "Couldn't convert ParameterHandle", e );
			return -1;
		}
	}
	
	/**
	 * This method will create a new Portico InteractionClassHandle implementation for the given
	 * integer handle and will return it. It will not search the FOM.
	 */
	public static InteractionClassHandle getInteractionHandle( int handle )
	{
		return new HLA1516InteractionClassHandle( handle );
	}

	/**
	 * Convert the given parameter handle into its interger representation
	 */
	public static int getInteractionHandle( InteractionClassHandle handle )
	{
		try
		{
			return HLA1516Handle.fromHandle( handle );
		}
		catch( Exception e )
		{
			Assert.fail( "Couldn't convert InteractionClassHandle", e );
			return -1;
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
