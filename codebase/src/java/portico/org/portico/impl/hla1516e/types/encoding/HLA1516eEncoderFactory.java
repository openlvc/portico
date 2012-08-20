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
		return null;
	}

	public HLAASCIIchar createHLAASCIIchar( byte b )
	{
		return null;
	}

	public HLAASCIIstring createHLAASCIIstring()
	{
		return null;
	}

	public HLAASCIIstring createHLAASCIIstring( String s )
	{
		return null;
	}

	public HLAboolean createHLAboolean()
	{
		return null;
	}

	public HLAboolean createHLAboolean( boolean b )
	{
		return null;
	}

	public HLAbyte createHLAbyte()
	{
		return null;
	}

	public HLAbyte createHLAbyte( byte b )
	{
		return null;
	}

	public <T extends DataElement> HLAvariantRecord<T> createHLAvariantRecord( T discriminant )
	{
		return null;
	}

	public HLAfixedRecord createHLAfixedRecord()
	{
		return null;
	}

	public <T extends DataElement> HLAfixedArray<T> createHLAfixedArray( DataElementFactory<T> factory,
	                                                              int size )
	{
		return null;
	}
	
	public <T extends DataElement> HLAfixedArray<T> createHLAfixedArray( T... elements )
	{
		return null;
	}

	public HLAfloat32BE createHLAfloat32BE()
	{
		return null;
	}

	public HLAfloat32BE createHLAfloat32BE( float f )
	{
		return null;
	}

	public HLAfloat32LE createHLAfloat32LE()
	{
		return null;
	}

	public HLAfloat32LE createHLAfloat32LE( float f )
	{
		return null;
	}

	public HLAfloat64BE createHLAfloat64BE()
	{
		return null;
	}

	public HLAfloat64BE createHLAfloat64BE( double d )
	{
		return null;
	}

	public HLAfloat64LE createHLAfloat64LE()
	{
		return null;
	}

	public HLAfloat64LE createHLAfloat64LE( double d )
	{
		return null;
	}

	public HLAinteger16BE createHLAinteger16BE()
	{
		return null;
	}

	public HLAinteger16BE createHLAinteger16BE( short s )
	{
		return null;
	}

	public HLAinteger16LE createHLAinteger16LE()
	{
		return null;
	}

	public HLAinteger16LE createHLAinteger16LE( short s )
	{
		return null;
	}

	public HLAinteger32BE createHLAinteger32BE()
	{
		return null;
	}

	public HLAinteger32BE createHLAinteger32BE( int i )
	{
		return null;
	}

	public HLAinteger32LE createHLAinteger32LE()
	{
		return null;
	}

	public HLAinteger32LE createHLAinteger32LE( int i )
	{
		return null;
	}

	public HLAinteger64BE createHLAinteger64BE()
	{
		return null;
	}

	public HLAinteger64BE createHLAinteger64BE( long l )
	{
		return null;
	}

	public HLAinteger64LE createHLAinteger64LE()
	{
		return null;
	}

	public HLAinteger64LE createHLAinteger64LE( long l )
	{
		return null;
	}

	public HLAoctet createHLAoctet()
	{
		return null;
	}

	public HLAoctet createHLAoctet( byte b )
	{
		return null;
	}

	public HLAoctetPairBE createHLAoctetPairBE()
	{
		return null;
	}

	public HLAoctetPairBE createHLAoctetPairBE( short s )
	{
		return null;
	}

	public HLAoctetPairLE createHLAoctetPairLE()
	{
		return null;
	}

	public HLAoctetPairLE createHLAoctetPairLE( short s )
	{
		return null;
	}

	public HLAopaqueData createHLAopaqueData()
	{
		return null;
	}

	public HLAopaqueData createHLAopaqueData( byte[] b )
	{
		return null;
	}

	public HLAunicodeChar createHLAunicodeChar()
	{
		return null;
	}

	public HLAunicodeChar createHLAunicodeChar( short c )
	{
		return null;
	}

	public HLAunicodeString createHLAunicodeString()
	{
		return null;
	}

	public HLAunicodeString createHLAunicodeString( String s )
	{
		return null;
	}

	public <T extends DataElement> HLAvariableArray<T> createHLAvariableArray( DataElementFactory<T> factory,
	                                                                    T... elements )
	{
		return null;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
