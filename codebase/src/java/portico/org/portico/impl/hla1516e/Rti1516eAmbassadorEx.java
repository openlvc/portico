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
package org.portico.impl.hla1516e;

import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.PCMetadata;
import org.portico.lrc.model.datatype.ArrayType;
import org.portico.lrc.model.datatype.BasicType;
import org.portico.lrc.model.datatype.EnumeratedType;
import org.portico.lrc.model.datatype.FixedRecordType;
import org.portico.lrc.model.datatype.IDatatype;
import org.portico.lrc.model.datatype.SimpleType;
import org.portico.lrc.model.datatype.VariantRecordType;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidAttributeHandle;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.InvalidParameterHandle;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;

/**
 * This interface houses custom extensions to the standard IEEE-1516 (2010) interface
 * that are currently only supported by the Portico RTI. Any {@link RTIambassador}
 * instance returned from the {@link RtiFactory} by Portico will support this extensions
 * interface.
 * <p/>
 * This interface is currently only supported by the Portico RTI.
 */
public class Rti1516eAmbassadorEx extends Rti1516eAmbassador
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
	public Rti1516eAmbassadorEx() throws RTIinternalError
	{
		super();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	///////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Ownership Management Services //////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Return an in-memory representation of the unified Federation Object Model. This contains
	 * all merged FOM modules and the standard MIM.
	 */
	public ObjectModel getFOM()
	{
		return getHelper().getFOM();
	}
	
	/**
	 * Returns the datatype of the specified Attribute.
	 * <p/>
	 * To determine the specific class of the datatype call {@link IDatatype#getDatatypeClass()}.
	 * You can then cast the return value to the corresponding implementation (e.g.
	 * {@link BasicType}, {@link SimpleType}, {@link EnumeratedType}, {@link ArrayType}, 
	 * {@link FixedRecordType}, or {@link VariantRecordType}).
	 */
	public IDatatype getAttributeDatatype( ObjectClassHandle whichClass,
	                                       AttributeHandle theHandle )
	    throws AttributeNotDefined,
	           InvalidAttributeHandle,
	           InvalidObjectClassHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		Impl1516eHelper helper = getHelper();
		helper.checkJoined();
		
		int ocHandle = HLA1516eHandle.validatedHandle( whichClass );
		int acHandle = HLA1516eHandle.validatedHandle( theHandle );
		
		OCMetadata cls = helper.getFOM().getObjectClass( ocHandle );
		if( cls == null )
		{
			throw new RTIinternalError( "handle: " + whichClass );
		}
		else
		{
			ACMetadata attribute = cls.getAttribute( acHandle );
			if( attribute == null )
			{
				throw new AttributeNotDefined( "handle: " + theHandle );
			}
			else
			{
				return attribute.getDatatype();
			}
		}
	}
	
	/**
	 * Returns the datatype of the specified Parameter.
	 * <p/>
	 * To determine the specific class of the datatype call {@link IDatatype#getDatatypeClass()}.
	 * You can then cast the return value to the corresponding implementation (e.g.
	 * {@link BasicType}, {@link SimpleType}, {@link EnumeratedType}, {@link ArrayType}, 
	 * {@link FixedRecordType}, or {@link VariantRecordType}).
	 */
	public IDatatype getParameterDatatype( InteractionClassHandle whichClass, 
	                                       ParameterHandle theHandle )
	    throws InteractionParameterNotDefined,
		       InvalidParameterHandle,
		       InvalidInteractionClassHandle,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		Impl1516eHelper helper = getHelper();
		helper.checkJoined();
		
		int icHandle = HLA1516eHandle.validatedHandle( whichClass );
		int pcHandle = HLA1516eHandle.validatedHandle( theHandle );
		ICMetadata cls = helper.getFOM().getInteractionClass( icHandle );
		if( cls == null )
		{
			throw new RTIinternalError( "handle: " + icHandle );
		}
		else
		{
			PCMetadata parameter = cls.getDeclaredParameter( pcHandle );
			if( parameter == null )
			{
				throw new InteractionParameterNotDefined( "handle: " + theHandle );
			}
			else
			{
				return parameter.getDatatype();
			}
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
