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
package org.portico2.rti.services.mom.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.HLA1516eParameterHandleValueMapFactory;
import org.portico.impl.hla1516e.types.encoding.HLA1516eEncoderFactory;
import org.portico.impl.hla1516e.types.time.DoubleTime;
import org.portico.impl.hla1516e.types.time.DoubleTimeInterval;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.datatype.IDatatype;
import org.portico.utils.bithelpers.BitHelpers;

import hla.rti1516e.ParameterHandleValueMapFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAASCIIstring;
import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.encoding.HLAvariableArray;
import hla.rti1516e.exceptions.CouldNotEncode;

/**
 * Encoding helpers to convert MOM Attributes/Parameters into byte arrays.
 * <p/>
 * Developers should access this class solely through the static methods 
 * {@link #encode(IDatatype, Object)} and {@link #decode(IDatatype, byte[])}
 */
public class MomEncodingHelpers
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static MomEncodingHelpers INSTANCE = new MomEncodingHelpers();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private EncoderFactory factory;
	private ParameterHandleValueMapFactory paramMapFactory;
	private Map<String,Function<Object,byte[]>> encoders;
	private Map<String,FunctionWithException<byte[],Object,DecoderException>> decoders;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private MomEncodingHelpers()
	{
		this.factory = new HLA1516eEncoderFactory();
		this.paramMapFactory = new HLA1516eParameterHandleValueMapFactory();
		
		this.encoders = new HashMap<>();
		this.encoders.put( "HLAASCIIstring", this::encodeAsciiString );
		this.encoders.put( "HLAboolean", this::encodeBoolean );
		this.encoders.put( "HLAcount", this::encodeInt32BE );
		this.encoders.put( "HLAhandle", this::encodeHandle );
		this.encoders.put( "HLAhandleList", this::encodeHandleList );
		this.encoders.put( "HLAlogicalTime", this::encodeTime );
		this.encoders.put( "HLAmsec", this::encodeInt32BE );
		this.encoders.put( "HLAsynchPointList", this::encodeUnicodeStringVariableArray );
		this.encoders.put( "HLAtimeInterval", this::encodeTimeInterval );
		this.encoders.put( "HLAunicodeString", this::encodeUnicodeString );
		this.encoders.put( "HLAsynchPointFederateList", this::encodeSynchPointFederateList );
		
		this.decoders = new HashMap<>();
		this.decoders.put( "HLAASCIIstring", this::decodeAsciiString );
		this.decoders.put( "HLAunicodeString", this::decodeUnicodeString );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private String iterableToString( Iterable<?> collection )
	{
		StringBuilder builder = new StringBuilder();
		boolean firstElement = true;
		for( Object o : collection )
		{
			if( !firstElement )
				builder.append( "," );
			
			builder.append( o.toString() );
		}
		
		return builder.toString();
	}
	
	public byte[] encodeUnicodeString( Object data )
	{
		String content = null;
		if( data instanceof Iterable )
			content = iterableToString( (Iterable)data );
		else
			content = data.toString();
		
		HLAunicodeString hlaString = this.factory.createHLAunicodeString( content );
		return hlaString.toByteArray();
	}
	
	public byte[] encodeAsciiString( Object data )
	{
		String content = null;
		if( data instanceof Iterable )
			content = iterableToString( (Iterable)data );
		else
			content = data.toString();
		
		HLAASCIIstring hlaString = this.factory.createHLAASCIIstring( content );
		return hlaString.toByteArray();
	}
	
	public byte[] encodeUnicodeStringVariableArray( Object data )
	{
		if( data instanceof Object[] )
		{
			Object[] asArray = (Object[])data;
			int length = asArray.length;
			HLAunicodeString[] stringArray = new HLAunicodeString[length];
			for( int i = 0; i < length; ++i )
				stringArray[i] = this.factory.createHLAunicodeString( asArray[i].toString() );

			HLAvariableArray<HLAunicodeString> hlaArray =
			    this.factory.createHLAvariableArray( null, stringArray );
			return hlaArray.toByteArray();
		}
		else
		{
			throw new JRTIinternalError( "non-array type: " + data.getClass() );
		}
	}

	public byte[] encodeHandle( Object data )
	{
		if( data instanceof Number )
		{
			HLA1516eHandle handle = new HLA1516eHandle( ((Number)data).intValue() );
			return handle.getBytes();
		}
		else
		{
			throw new JRTIinternalError( "non-numeric type: " + data.getClass() );
		}
	}
	
	public byte[] encodeBoolean( Object data )
	{
		if( data instanceof Boolean )
		{
			HLAboolean hlaBool = this.factory.createHLAboolean( (Boolean)data );
			return hlaBool.toByteArray();
		}
		else
		{
			throw new JRTIinternalError( "non-boolean type: " + data.getClass() );
		}
	}
	
	public byte[] encodeTime( Object data )
	{
		if( data instanceof Number )
		{
			DoubleTime time = new DoubleTime( ((Number)data).doubleValue() );
			return time.toByteArray();
		}
		else
		{
			throw new JRTIinternalError( "non-numeric type: " + data.getClass() );
		}
	}
	
	public byte[] encodeHandleList( Object data )
	{
		if( data instanceof int[] )
		{
			int[] asIntArray = (int[])data;
			int elements = asIntArray.length;
			HLA1516eHandle[] handles = new HLA1516eHandle[elements];
			int encodedLength = 4;			// start with 4 bytes for the size
			for( int i = 0 ; i < elements ; ++i )
			{
				handles[i] = new HLA1516eHandle( asIntArray[i] );
				encodedLength += handles[i].encodedLength();
			}
			
			// Write the element count
			int offset = 0;
			byte[] bytes = new byte[encodedLength];
			BitHelpers.putIntBE( elements, bytes, offset );
			offset += 4;
			
			// Write the elements
			for( int i = 0 ; i < elements ; ++i )
			{
				BitHelpers.putByteArray( handles[i].getBytes(), bytes, offset );
				offset += handles[i].encodedLength();
			}
			
			return bytes;
		}
		else
		{
			throw new JRTIinternalError( "non-numeric array type: " + data.getClass() );
		}
	}
	
	public byte[] encodeTimeInterval( Object data )
	{
		if( data instanceof Number )
		{
			DoubleTimeInterval interval = new DoubleTimeInterval( ((Number)data).doubleValue() );
			byte[] asBytes = new byte[interval.encodedLength()];
			try
			{
				interval.encode( asBytes, 0 );
				return asBytes;
			}
			catch( CouldNotEncode cne )
			{
				throw new JRTIinternalError( cne.getMessage(), cne );
			}
		}
		else
		{
			throw new JRTIinternalError( "non-numeric type: " + data.getClass() );
		}
	}
	
	public byte[] encodeInt32BE( Object data )
	{
		if( data instanceof Number )
		{
			HLAinteger32BE int32BE = this.factory.createHLAinteger32BE( ((Number)data ).intValue() );
			return int32BE.toByteArray();
		}
		else
		{
			throw new JRTIinternalError( "non-numeric type: " + data.getClass() );
		}
	}
	
	public byte[] encodeSynchPointFederateList( Object data )
	{
		if( data == null )
			data = new SynchPointFederate[0];
		
		if( data instanceof SynchPointFederate[] )
		{
			SynchPointFederate[] asArray = (SynchPointFederate[])data;
			HLAvariableArray<SynchPointFederate> hlaArray = factory.createHLAvariableArray( null, 
			                                                                                asArray );
			return hlaArray.toByteArray();
		}
		else
		{
			throw new JRTIinternalError( "non-SynchPointFederate array type: " + data.getClass() );
		}
	}
	
	public String decodeAsciiString( byte[] value ) throws DecoderException
	{
		HLAASCIIstring asciiString = this.factory.createHLAASCIIstring();
		asciiString.decode( value );
		return asciiString.getValue();
	}
	
	public String decodeUnicodeString( byte[] value ) throws DecoderException
	{
		HLAunicodeString unicodeString = this.factory.createHLAunicodeString();
		unicodeString.decode( value );
		return unicodeString.getValue();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	@FunctionalInterface
	private interface FunctionWithException<T,R,E extends Exception>
	{
		R apply(T t) throws E;
	}
	
	/**
	 * Encodes the provided data as the specified datatype
	 * 
	 * @param datatype the datatype that the data will be represented as
	 * @param data the data to encode
	 * @return the binary representation of the data, encoded as the specified datatype
	 */
	public static byte[] encode( IDatatype datatype, Object data )
	{
		byte[] value = null;
		Function<Object,byte[]> encoder = INSTANCE.encoders.get( datatype.getName() );
		if( encoder != null )
			value = encoder.apply( data );
		else
			throw new JRTIinternalError( "unhandled parameter datatype " + datatype.getName() );
		
		return value;
	}
	
	/**
	 * Decodes the provided data using the encoding scheme of the specified datatype
	 * 
	 * @param datatype the datatype that the encoding scheme that the data is encoded with
	 * @param data the data to decode
	 * @return the Object representation of the data
	 */
	public static Object decode( IDatatype datatype, byte[] data ) throws DecoderException
	{
		Object value = null;
		FunctionWithException<byte[],Object,DecoderException> decoder = 
			INSTANCE.decoders.get( datatype.getName() );
		if( decoder != null )
			value = decoder.apply( data );
		else
			throw new JRTIinternalError( "unhandled parameter datatype " + datatype.getName() );
		
		return value;
	}
}
