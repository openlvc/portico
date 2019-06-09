/*
 *   Copyright 2008 The Portico Project
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
package org.portico.shared;

import org.portico.impl.hla13.fomparser.FOM;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.PCMetadata;
import org.portico.lrc.utils.MessageHelpers;
import org.portico2.common.network.CallType;
import org.portico2.common.services.federation.msg.CreateFederation;
import org.portico2.common.services.federation.msg.DestroyFederation;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups={"MessageHelpersTest","shared","helpers"})
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
	/**
	 * Tests the mechanism through which messages are deflated and inflated (that is, through which
	 * message objects are converted into a byte[] and then back into a message object).
	 */
	@Test
	public void testDeflateThenInflate() throws Exception
	{
		// deflate and then inflate the CreateFederation message
		// this message is quite large, so it should be  a good test
		ObjectModel model = FOM.parseFOM( ClassLoader.getSystemResource("fom/testfom.fed") );
		CreateFederation original = new CreateFederation( "sharedTest", model );

		byte[] deflated = MessageHelpers.deflate2( original, CallType.ControlRequest, 0 );
		
		CreateFederation inflated = MessageHelpers.inflate2( deflated, CreateFederation.class );
		ObjectModel iModel = inflated.getModel();
		Assert.assertNotNull( iModel );
		
		// validate the object classes
		for( OCMetadata originalClass : model.getAllObjectClasses() )
		{
			OCMetadata inflatedClass = iModel.getObjectClass( originalClass.getHandle() );
			Assert.assertEquals( inflatedClass.getQualifiedName(),
			                     originalClass.getQualifiedName() );
			
			for( ACMetadata originalAttribute : originalClass.getDeclaredAttributes() )
			{
				ACMetadata inflatedAttribute = inflatedClass.getAttribute( originalAttribute.getHandle() );
				Assert.assertEquals( inflatedAttribute.getName(),
				                     originalAttribute.getName() );
				Assert.assertEquals( inflatedAttribute.getOrder(),
				                     originalAttribute.getOrder() );
				//Assert.assertEquals( inflatedAttribute.getSpace(),
				//                     originalAttribute.getSpace() );
				Assert.assertEquals( inflatedAttribute.getTransport(),
				                     originalAttribute.getTransport() );
			}
		}
		
		// validate the interaction classes
		for( ICMetadata originalClass : model.getAllInteractionClasses() )
		{
			ICMetadata inflatedClass = iModel.getInteractionClass( originalClass.getHandle() );
			Assert.assertEquals( inflatedClass.getQualifiedName(),
			                     originalClass.getQualifiedName() );
			Assert.assertEquals( inflatedClass.getOrder(), originalClass.getOrder() );
			Assert.assertEquals( inflatedClass.getTransport(), originalClass.getTransport() );
			
			for( PCMetadata originalParameter : originalClass.getDeclaredParameters() )
			{
				PCMetadata inflatedParameter = inflatedClass.getParameter( originalParameter.getHandle() );
				Assert.assertEquals( inflatedParameter.getName(),
				                     originalParameter.getName() );
			}
		}
	}
	
	/**
	 * Deflate a PorticoMessage, pad the front with some data, "strip" the data and then make sure
	 * the message can be inflated happily.
	 */
	@Test(dependsOnMethods="testDeflateThenInflate")
	public void testArrayStrip() throws Exception
	{
		// deflate a PorticoMessage
		DestroyFederation request = new DestroyFederation( "testFederation" );
		byte[] data = MessageHelpers.deflate2( request, CallType.ControlRequest, 0 );
		
		// pad the data
		byte[] padded = new byte[data.length+1];
		padded[0] = (byte)44;
		System.arraycopy( data, 0, padded, 1, data.length );
		
		// make sure the defalte fails now as the data should be bad
		try
		{
			MessageHelpers.inflate2( padded, DestroyFederation.class );
			Assert.fail( "No exception when inflating invalid data" );
		}
		catch( RuntimeException rtie )
		{
			// success!
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception when inflating invalid data", e );
		}
		
		// strip the data and then attempt inflation again
		byte[] stripped = MessageHelpers.stripHeader( padded, 1 );
		DestroyFederation inflated = MessageHelpers.inflate2( stripped, DestroyFederation.class );
		Assert.assertEquals( inflated.getFederationName(), request.getFederationName() );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
