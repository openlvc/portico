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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.logging.Log4jConfiguration;
import org.portico2.common.logging.Log4jConfigurator;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.messaging.MessageType;
import org.portico2.common.messaging.ResponseMessage;
import org.portico2.common.network.CallType;
import org.portico2.common.network.Connection;
import org.portico2.common.network.Header;
import org.portico2.common.network.IApplicationReceiver;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.common.network.configuration.transport.JvmConfiguration;
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
		byte[] buffer = MessageHelpers.deflate2( before, CallType.ControlRequest, 0 );
		RtiProbe after = MessageHelpers.inflate2( buffer, RtiProbe.class );
	}
	
	@Test
	public void testHeader()
	{
		/**
		int[] requestIds = new int[] { 0, 1, 65535, 65536, 65537, Integer.MAX_VALUE };
		
		byte[] bytes = new byte[Header.HEADER_LENGTH];
		Header header = new Header( bytes, 0 );
		
		for( int i = 0; i < requestIds.length; i++ )
		{
			System.out.println( "RequestId: "+requestIds[i] );
			header.writeRequestId( requestIds[i] );
			System.out.println(" ReadId   : "+header.getRequestId() );
		}*/
	}
	
	@Test
	public void testDeflateInflateWithEncryption()
	{
		if( System.currentTimeMillis() > 1 )
			throw new RuntimeException( "Under Refactor" );

		// Create and set up the configurations
		ConnectionConfiguration configuration = new ConnectionConfiguration( "jvm" );
		configuration.setTransportConfiguration( new JvmConfiguration(configuration) );
//		configuration.getSharedKeyConfiguration().setEnabled( true );
//		configuration.getSharedKeyConfiguration().setSharedKey( "evelyn" );
		
		// Create and set up the connections
		AppReceiver outgoingReceiver = new AppReceiver();
		AppReceiver incomingReceiver = new AppReceiver();

		Connection outgoing = new Connection(null,null);
		Connection incoming = new Connection(null,null);

		outgoing.configure( configuration, outgoingReceiver );
		incoming.configure( configuration, incomingReceiver );
		
		outgoing.connect();
		incoming.connect();
		
		RtiProbe before = new RtiProbe();
		System.out.println( "Sending" );
		MessageContext context = new MessageContext( before );
		outgoing.sendControlRequest( context );
		System.out.println( "Sent" );
		if( context.isSuccessResponse() )
			System.out.println( "Success!" );
		else
			System.out.println( "Failure!" );

		RtiProbe after = (RtiProbe)incomingReceiver.receivedRequests.get( 0 );		
		System.out.println( "Before: "+before );
		System.out.println( "After : "+after );
		
		incoming.disconnect();
		outgoing.disconnect();
	}
	
	@Test
	public void testDeflateInflateResponseMessage()
	{
		
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	private static class AppReceiver implements IApplicationReceiver
	{
		private List<PorticoMessage> receivedRequests = new ArrayList<>();
		private List<PorticoMessage> receivedNotifications = new ArrayList<>();
		private List<ResponseMessage> receivedResponses = new ArrayList<>();
		
		public Logger getLogger() { return LogManager.getFormatterLogger( "portico" ); }
		public boolean isReceivable( Header header ) { return true; }

		public void receiveControlRequest( MessageContext context )
		{
			PorticoMessage request = context.getRequest();
			this.receivedRequests.add( request );
			if( request.getType() == MessageType.RtiProbe )
				context.success( "This. Is. Sparta!" );
		}

		public void receiveDataMessage( PorticoMessage message )
		{
			this.receivedRequests.add( message );
		}
		
		public void receiveNotification( PorticoMessage message )
		{
			this.receivedNotifications.add( message );
		}
	}
	
	
	public static void main( String[] args )
	{
		Log4jConfiguration logging = new Log4jConfiguration( "portico" );
		logging.turnConsoleOn();
		logging.setLevel( "TRACE" );
		Log4jConfigurator.activate( logging );
		//new MessageHelpersTest().testDeflateInflateWithEncryption();
		new MessageHelpersTest().testHeader();
	}
}
