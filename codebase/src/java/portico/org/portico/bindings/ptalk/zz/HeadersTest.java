/*
 *   Copyright 2010 The Portico Project
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
package org.portico.bindings.ptalk.zz;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.portico.bindings.ptalk.Common;
import org.portico.bindings.ptalk.channel.Header;
import org.portico.bindings.ptalk.channel.Headers;
import org.portico.bindings.ptalk.channel.Packet;
import org.portico.bindings.ptalk.protocol.GroupManagement;

public class HeadersTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
    public static final int FIRST = 16;
    public static final int SECOND = 32;
    public static final int THIRD = 64;
    public static final int FORTH = 128;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
    public static void main( String[] args ) throws Exception
	{
		// packet marshalling and unmarshalling
		Packet source = new Packet();
		source.setHeader( Header.GM, GroupManagement.MessageType.Discovery.byteValue() );
		source.setPayload( "Hello there".getBytes() );
		System.out.println( "source.headers(): "+source.getHeaders() );
		System.out.println( "source.size()   : "+source.getPayload().length );
		System.out.println( "source.payload(): "+new String(source.getPayload()) );

		byte[] buffer = source.marshal();
		Packet copy = new Packet();
		copy.unmarshal( buffer, 0 );

		System.out.println( "copy.headers(): "+copy.getHeaders() );
		System.out.println( "copy.size()   : "+copy.getPayload().length );
		System.out.println( "copy.payload(): "+new String(copy.getPayload()) );
	}
    
    public static void main2( String[] args ) throws Exception
	{
    	Headers headers = new Headers();
    	headers.setHeader( Header.SERIAL, Common.integerToByteArray(7) );
    	headers.setHeader( Header.GM, GroupManagement.MessageType.Discovery.byteValue() );

    	System.out.println( "size: " + headers.getMarshaledSize() );
    	System.out.println( "has serial: " + headers.hasHeader(Header.SERIAL) );
    	System.out.println( "has gm    : " + headers.hasHeader(Header.GM) );
    	System.out.println( "serial    : " + Common.byteArrayToInteger(headers.getHeader(Header.SERIAL)) );
    	System.out.println( "gm type   : " + GroupManagement.MessageType.valueOf(headers.getHeaderAsByte(Header.GM)) );
    	System.out.println( "=====================================" );
    	
    	byte[] buffer = new byte[headers.getMarshaledSize()];
    	headers.marshal( buffer, 0 );
    	
    	Headers headers2 = new Headers();
    	headers2.unmarshal( buffer, 0 );
    	
    	System.out.println( "size: " + headers2.getMarshaledSize() );
    	System.out.println( "has serial: " + headers2.hasHeader(Header.SERIAL) );
    	System.out.println( "has gm    : " + headers2.hasHeader(Header.GM) );
    	System.out.println( "serial    : " + Common.byteArrayToInteger(headers2.getHeader(Header.SERIAL)) );
    	System.out.println( "gm type   : " + GroupManagement.MessageType.valueOf(headers2.getHeaderAsByte(Header.GM)) );
    	
	}

	public static void main3( String[] args ) throws Exception
	{
		int flags = 0;

        flags += FIRST;
        flags += FORTH;

        boolean[] present = new boolean[]
        {
            (flags & FIRST) == FIRST,
            (flags & SECOND) == SECOND,
            (flags & THIRD) == THIRD,
            (flags & FORTH) == FORTH
        };

        System.out.println( "flags:"+Integer.toBinaryString(flags) );
        System.out.println( "array: " + java.util.Arrays.toString(present) );

        // set a value in the mask
        byte[][] bytes = new byte[1][];
        System.out.println( "bytes[0]: " + bytes[0] );


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( bytes );
        oos.close();
        System.out.println( "length: " + baos.toByteArray().length );
    }

}
