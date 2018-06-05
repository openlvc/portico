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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.portico.impl.HLAVersion;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.HLA1516eParameterHandleValueMapFactory;
import org.portico.impl.hla1516e.types.encoding.HLA1516eEncoderFactory;
import org.portico.impl.hla1516e.types.time.DoubleTime;
import org.portico.impl.hla1516e.types.time.DoubleTimeInterval;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.Mom;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.PCMetadata;
import org.portico.lrc.model.datatype.IDatatype;
import org.portico.utils.bithelpers.BitHelpers;
import org.portico2.common.services.object.msg.SendInteraction;

import hla.rti1516e.ParameterHandleValueMapFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;
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
		this.encoders.put( "HLAargumentList", this::encodeUnicodeStringVariableArray );
		this.encoders.put( "HLAboolean", this::encodeBoolean );
		this.encoders.put( "HLAcount", this::encodeInt32BE );
		this.encoders.put( "HLAhandle", this::encodeHandle );
		this.encoders.put( "HLAhandleList", this::encodeHandleList );
		this.encoders.put( "HLAindex", this::encodeInt32BE );
		this.encoders.put( "HLAinteractionCounts", this::encodeInteractionCounts );
		this.encoders.put( "HLAinteractionSubList", this::encodeInteractionSubList );
		this.encoders.put( "HLAlogicalTime", this::encodeTime );
		this.encoders.put( "HLAmoduleDesignatorList", this::encodeUnicodeStringVariableArray );
		this.encoders.put( "HLAmsec", this::encodeInt32BE );
		this.encoders.put( "HLAobjectClassBasedCounts", this::encodeObjectClassBasedCounts );
		this.encoders.put( "HLAsynchPointList", this::encodeUnicodeStringVariableArray );
		this.encoders.put( "HLAtimeInterval", this::encodeTimeInterval );
		this.encoders.put( "HLAtransportationName", this::encodeUnicodeString );
		this.encoders.put( "HLAunicodeString", this::encodeUnicodeString );
		this.encoders.put( "HLAupdateRateName", this::encodeUnicodeString );
		this.encoders.put( "HLAsynchPointFederateList", this::encodeSynchPointFederateList );
		
		this.decoders = new HashMap<>();
		this.decoders.put( "HLAASCIIstring", this::decodeAsciiString );
		this.decoders.put( "HLAboolean", this::decodeBoolean );
		this.decoders.put( "HLAhandle", this::decodeHandle );
		this.decoders.put( "HLAindex", this::decodeInt32BE );
		this.decoders.put( "HLAunicodeString", this::decodeUnicodeString );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private String objectToString( Object object )
	{
		if( object instanceof byte[] )
			return byteArrayToString( (byte[])object );
		else if( object instanceof Iterable<?> )
			return iterableToString( (Iterable)object );
		else if( object instanceof Map<?,?>)
			return mapToString( (Map)object );
		
		if( object == null )
			return "<null>";
		else
			return object.toString();
	}
	private String iterableToString( Iterable<?> collection )
	{
		StringBuilder builder = new StringBuilder();
		boolean firstElement = true;
		builder.append( '[' );
		for( Object o : collection )
		{
			if( firstElement )
				firstElement = false;
			else
				builder.append( "," );
			
			builder.append( objectToString(o) );
		}
		builder.append( ']' );
		return builder.toString();
	}
	
	private String mapToString( Map<?,?> map )
	{
		StringBuilder builder = new StringBuilder();
		boolean firstElement = true;
		builder.append( '{' );
		for( Entry<?,?> entry : map.entrySet() )
		{
			if( firstElement )
				firstElement = false;
			else
				builder.append( "," );
			
			builder.append( objectToString(entry.getKey()) );
			builder.append( '=' );
			builder.append( objectToString(entry.getValue()) );
		}
		builder.append( '}' );
		return builder.toString();
	}
	
	private String byteArrayToString( byte[] array )
	{
		StringBuilder builder = new StringBuilder();
		builder.append( '[' );
		for( int i = 0 ; i < array.length ; ++i )
		{
			if( i > 0 )
				builder.append( ',' );
			short value = array[i];
			builder.append("0x");
			
			if( value < 16 )
				builder.append( '0' );
			builder.append( Integer.toString(value, 16) );
		}
		builder.append( ']' );
		
		return builder.toString();
	}
	
	public byte[] encodeUnicodeString( Object data )
	{
		HLAunicodeString hlaString = this.factory.createHLAunicodeString();
		if( data != null )
		{
			String content = objectToString( data );
			hlaString.setValue( content );
		}
		
		return hlaString.toByteArray();
	}
	
	public byte[] encodeAsciiString( Object data )
	{
		String content = objectToString( data );
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
			{
				Object element = asArray[i];
				String asString = objectToString( element );
				stringArray[i] = this.factory.createHLAunicodeString( asString );
			}

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
		if( data == null )
			return new byte[0];
		
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
		if( data == null )
			data = new int[0];
		
		if( data instanceof Collection<?> )
		{
			@SuppressWarnings("unchecked")
			Collection<Integer> asCollection = (Collection<Integer>)data;
			int[] temp = new int[asCollection.size()];
			int index = 0;
			for( int element : asCollection )
				temp[index++] = element;
			data = temp;
		}
		
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
	
	public byte[] encodeObjectClassBasedCounts( Object data )
	{
		if( data == null )
			data = new ObjectClassBasedCount[0];
		
		if( data instanceof ObjectClassBasedCount[] )
		{
			ObjectClassBasedCount[] asArray = (ObjectClassBasedCount[])data;
			HLAvariableArray<ObjectClassBasedCount> hlaArray = factory.createHLAvariableArray( null, 
			                                                                                   asArray );
			return hlaArray.toByteArray();
		}
		else
		{
			throw new JRTIinternalError( "non-ObjectClassBasedCount array type: " + data.getClass() );
		}
	}
	
	public byte[] encodeInteractionCounts( Object data )
	{
		if( data == null )
			data = new InteractionCount[0];
		
		if( data instanceof InteractionCount[] )
		{
			InteractionCount[] asArray = (InteractionCount[])data;
			HLAvariableArray<InteractionCount> hlaArray = factory.createHLAvariableArray( null, 
			                                                                              asArray );
			return hlaArray.toByteArray();
		}
		else
		{
			throw new JRTIinternalError( "non-InteractionCount array type: " + data.getClass() );
		}
	}
	
	public byte[] encodeInteractionSubList( Object data )
	{
		if( data == null )
			data = new InteractionSubscription[0];
		
		if( data instanceof InteractionSubscription[] )
		{
			InteractionSubscription[] asArray = (InteractionSubscription[])data;
			HLAvariableArray<InteractionSubscription> hlaArray = factory.createHLAvariableArray( null, 
			                                                                                     asArray );
			return hlaArray.toByteArray();
		}
		else
		{
			throw new JRTIinternalError( "non-InteractionSubscription array type: " + data.getClass() );
		}
	}
	
	public int decodeInt32BE( byte[] value ) throws DecoderException
	{
		HLAinteger32BE int32BE = this.factory.createHLAinteger32BE();
		int32BE.decode( value );
		return int32BE.getValue();
	}
	
	public int decodeHandle( byte[] value ) throws DecoderException
	{
		try
		{
			return HLA1516eHandle.decode( value );
		}
		catch( Exception e )
		{
			throw new DecoderException( e.getMessage(), e );
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
	
	public boolean decodeBoolean( byte[] value ) throws DecoderException
	{
		HLAboolean hlaBoolean = this.factory.createHLAboolean();
		hlaBoolean.decode( value );
		return hlaBoolean.getValue();
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
	
	/**
	 * Decodes the values of the specified interaction parameters and maps them against their canonical 
	 * parameter names.
	 * <p/>
	 * Values are decoded based on the {@link IDatatype} of their corresponding parameter.
	 * 
	 * @param interactionId the identifier of the interaction that the parameters belong to
	 * @param params the parameters as received from {@link SendInteraction#getParameters()}
	 * @return the decoded parameter values, mapped against the canonical name of their corresponding 
	 *         parameters 
	 * @throws MomException if a parameter value could not be decoded, or if an expected parameter entry 
	 *                      was missing from <code>params</code>
	 */
	public static Map<String,Object> decodeInteractionParameters( HLAVersion version,
	                                                              ObjectModel fom,
	                                                              int interactionId, 
	                                                              Map<Integer,byte[]> params )
		throws MomException
	{
		Map<String,Object> decoded = new HashMap<>();
		
		// Iterate over all the parameters that we expect to be provided in the request
		ICMetadata requestMetadata = fom.getInteractionClass( interactionId );
		Set<PCMetadata> paramMetadata = requestMetadata.getAllParameters();
		for( PCMetadata expectedParam : paramMetadata )
		{
			IDatatype type = expectedParam.getDatatype();
			int parameterId = expectedParam.getHandle();
			byte[] rawValue = params.get( parameterId );
			if( rawValue != null )
			{
				// Get the parameter's canonical name
				String name = Mom.getMomParameterName( version, 
				                                       parameterId );
				if( name == null )
				{
					// If we get here, then it's an internal configuration issue (e.g. we don't have
					// the parameter in our MOM tree
					throw new JRTIinternalError( "could not resolve canonical name for parameter " + 
					                             parameterId + 
					                             " of interaction " + interactionId );
				}
				
				Object value = null;
				try
				{
					value = MomEncodingHelpers.decode( type, rawValue );
				}
				catch( DecoderException de )
				{
					// Could not decode the value
					throw new MomException( "could not decode value for parameter " + 
					                        expectedParam.getName() + "[id=" + parameterId + "]",
					                        true,
					                        de );
				}
				
				decoded.put( name, value );
			}
			else
			{
				// Expected parameter was not provided
				throw new MomException( "no value provided for required parameter " + 
				                        expectedParam.getName() + "[id=" + parameterId + "]",
				                        true );
			}
		}
		
		return decoded;
	}
	
	/**
	 * Encodes the values of the specified response parameters and maps them against their corresponding 
	 * parameter handle
	 * <p/>
	 * Values are encoded based on the {@link IDatatype} of their corresponding parameter.
	 * 
	 * @param interactionMetadata the metadata of the response being sent
	 * @param params the values of the response parameters, mapped against their canonical parameter name
	 * @return the encoded parameter values, mapped against the handle of their corresponding parameters 
	 * @throws EncoderException if a parameter value could not be encoded.
	 */
	public static HashMap<Integer,byte[]> encodeInteractionParameters( HLAVersion version,
	                                                                   ICMetadata interactionMetadata,
	                                                                   Map<String,Object> params )
	{
		HashMap<Integer,byte[]> encoded = new HashMap<>();
		for( PCMetadata paramMetadata : interactionMetadata.getAllParameters() )
		{
			int paramHandle = paramMetadata.getHandle();
			IDatatype datatype = paramMetadata.getDatatype();
			String paramName = Mom.getMomParameterName( version,
			                                            paramHandle );
			if( paramName == null )
			{
				// Programmer error
				throw new JRTIinternalError( "not a canonical parameter name: " + paramName );
			}

			
			if( params.containsKey(paramName) )
			{
				Object value = params.get( paramName );
				byte[] encodedValue = MomEncodingHelpers.encode( datatype, value );
				encoded.put( paramHandle, encodedValue );
			}
			else
			{
				//logger.warn( "No value provided for MOM interaction parameter" + 
				//	         interactionMetadata.getQualifiedName() + "." + paramMetadata.getName() );
			}
		}
		
		return encoded;
	}
}
