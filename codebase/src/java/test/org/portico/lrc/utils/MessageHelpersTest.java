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
package org.portico.lrc.utils;

import org.portico2.common.network2.CallType;
import org.portico2.common.services.federation.msg.RtiProbe;
import org.testng.annotations.Test;

@Test(groups={"MessageHelpersTest","messaging","utils"})
public class MessageHelpersTest
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

	@Test
	public void testDeflateInflateRtiProbe()
	{
		RtiProbe before = new RtiProbe();
		byte[] buffer = MessageHelpers.deflate2( before, CallType.ControlSync, 0 );
		RtiProbe after = MessageHelpers.inflate2( buffer, RtiProbe.class );
	}
	
	@Test
	public void testDeflateInflateResponseMessage()
	{
		
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args )
	{
		new MessageHelpersTest().testDeflateInflateRtiProbe();
	}
}
