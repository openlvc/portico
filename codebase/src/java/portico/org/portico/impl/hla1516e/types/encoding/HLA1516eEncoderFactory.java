/*
 *   Copyright 2012 The Portico Project
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
package org.portico.impl.hla1516e.types.encoding;

import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.DataElementFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAASCIIchar;
import hla.rti1516e.encoding.HLAASCIIstring;
import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.encoding.HLAbyte;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAfloat32LE;
import hla.rti1516e.encoding.HLAfloat64BE;
import hla.rti1516e.encoding.HLAfloat64LE;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.encoding.HLAinteger16LE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAinteger32LE;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.encoding.HLAinteger64LE;
import hla.rti1516e.encoding.HLAoctet;
import hla.rti1516e.encoding.HLAoctetPairBE;
import hla.rti1516e.encoding.HLAoctetPairLE;
import hla.rti1516e.encoding.HLAopaqueData;
import hla.rti1516e.encoding.HLAunicodeChar;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.encoding.HLAvariableArray;
import hla.rti1516e.encoding.HLAvariantRecord;

public class HLA1516eEncoderFactory implements EncoderFactory
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
	public HLAASCIIchar createHLAASCIIchar()
	{
		return new HLA1516eASCIIchar();
	}

	public HLAASCIIchar createHLAASCIIchar( byte b )
	{
		return new HLA1516eASCIIchar( b );
	}

	public HLAASCIIstring createHLAASCIIstring()
	{
		return new HLA1516eASCIIstring();
	}

	public HLAASCIIstring createHLAASCIIstring( String s )
	{
		return new HLA1516eASCIIstring( s );
	}

	public HLAboolean createHLAboolean()
	{
		return new HLA1516eBoolean();
	}

	public HLAboolean createHLAboolean( boolean b )
	{
		return new HLA1516eBoolean( b );
	}

	public HLAbyte createHLAbyte()
	{
		return new HLA1516eByte();
	}

	public HLAbyte createHLAbyte( byte b )
	{
		return new HLA1516eByte( b );
	}

	public <T extends DataElement> HLAvariantRecord<T> createHLAvariantRecord( T discriminant )
	{
		return new HLA1516eVariantRecord<>(discriminant);
	}

	public HLAfixedRecord createHLAfixedRecord()
	{
		return new HLA1516eFixedRecord();
	}

	public <T extends DataElement> HLAfixedArray<T>
	       createHLAfixedArray( DataElementFactory<T> factory, int size )
	{
		return new HLA1516eFixedArray<T>( factory, size );
	}

	public <T extends DataElement> HLAfixedArray<T> createHLAfixedArray( @SuppressWarnings("unchecked") T... elements )
	{
		return new HLA1516eFixedArray<T>( elements );
	}

	public HLAfloat32BE createHLAfloat32BE()
	{
		return new HLA1516eFloat32BE();
	}

	public HLAfloat32BE createHLAfloat32BE( float f )
	{
		return new HLA1516eFloat32BE( f );
	}

	public HLAfloat32LE createHLAfloat32LE()
	{
		return new HLA1516eFloat32LE();
	}

	public HLAfloat32LE createHLAfloat32LE( float f )
	{
		return new HLA1516eFloat32LE( f );
	}

	public HLAfloat64BE createHLAfloat64BE()
	{
		return new HLA1516eFloat64BE();
	}

	public HLAfloat64BE createHLAfloat64BE( double d )
	{
		return new HLA1516eFloat64BE( d );
	}

	public HLAfloat64LE createHLAfloat64LE()
	{
		return new HLA1516eFloat64LE();
	}

	public HLAfloat64LE createHLAfloat64LE( double d )
	{
		return new HLA1516eFloat64LE( d );
	}

	public HLAinteger16BE createHLAinteger16BE()
	{
		return new HLA1516eInteger16BE();
	}

	public HLAinteger16BE createHLAinteger16BE( short s )
	{
		return new HLA1516eInteger16BE( s );
	}

	public HLAinteger16LE createHLAinteger16LE()
	{
		return new HLA1516eInteger16LE();
	}

	public HLAinteger16LE createHLAinteger16LE( short s )
	{
		return new HLA1516eInteger16LE( s );
	}

	public HLAinteger32BE createHLAinteger32BE()
	{
		return new HLA1516eInteger32BE();
	}

	public HLAinteger32BE createHLAinteger32BE( int i )
	{
		return new HLA1516eInteger32BE( i );
	}

	public HLAinteger32LE createHLAinteger32LE()
	{
		return new HLA1516eInteger32LE();
	}

	public HLAinteger32LE createHLAinteger32LE( int i )
	{
		return new HLA1516eInteger32LE( i );
	}

	public HLAinteger64BE createHLAinteger64BE()
	{
		return new HLA1516eInteger64BE();
	}

	public HLAinteger64BE createHLAinteger64BE( long l )
	{
		return new HLA1516eInteger64BE( l );
	}

	public HLAinteger64LE createHLAinteger64LE()
	{
		return new HLA1516eInteger64LE();
	}

	public HLAinteger64LE createHLAinteger64LE( long l )
	{
		return new HLA1516eInteger64LE( l );
	}

	public HLAoctet createHLAoctet()
	{
		return new HLA1516eOctet();
	}

	public HLAoctet createHLAoctet( byte b )
	{
		return new HLA1516eOctet( b );
	}

	public HLAoctetPairBE createHLAoctetPairBE()
	{
		return new HLA1516eOctetPairBE();
	}

	public HLAoctetPairBE createHLAoctetPairBE( short s )
	{
		return new HLA1516eOctetPairBE( s );
	}

	public HLAoctetPairLE createHLAoctetPairLE()
	{
		return new HLA1516eOctetPairLE();
	}

	public HLAoctetPairLE createHLAoctetPairLE( short s )
	{
		return new HLA1516eOctetPairLE( s );
	}

	public HLAopaqueData createHLAopaqueData()
	{
		return new HLA1516eOpaqueData();
	}

	public HLAopaqueData createHLAopaqueData( byte[] b )
	{
		return new HLA1516eOpaqueData( b );
	}

	public HLAunicodeChar createHLAunicodeChar()
	{
		return new HLA1516eUnicodeChar();
	}

	public HLAunicodeChar createHLAunicodeChar( short c )
	{
		return new HLA1516eUnicodeChar( c );
	}

	public HLAunicodeString createHLAunicodeString()
	{
		return new HLA1516eUnicodeString();
	}

	public HLAunicodeString createHLAunicodeString( String s )
	{
		return new HLA1516eUnicodeString( s );
	}

	public <T extends DataElement> HLAvariableArray<T>
	       createHLAvariableArray( DataElementFactory<T> factory, @SuppressWarnings("unchecked") T... elements )
	{
		return new HLA1516eVariableArray<T>( factory, elements );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
