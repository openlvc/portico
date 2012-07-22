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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.StringTokenizer;
import java.util.Vector;

import org.portico.bindings.ptalk.Common;

public class Client
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Vector<InetSocketAddress> list;

	private int multicastPort;
	private InetAddress multicastIp;
	private InetSocketAddress multicastAddress;
	private MulticastSocket multicastSocket;
	
	private DatagramSocket datagramSocket;
	private InetSocketAddress datagramAddress;
	
	private boolean master;
	private InetSocketAddress masterAddress;
	private Object masterLock = new Object(); // used to synchronize access to masterAddress
	
	//private Vector<InetSocketAddress>

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Client()
	{
		this.master = false;
		this.list = new Vector<InetSocketAddress>();
		this.masterAddress = null;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private void configure() throws Exception
	{
		// set up the datagram socket for sending on
		this.datagramSocket = new DatagramSocket();
		this.datagramAddress = new InetSocketAddress( InetAddress.getLocalHost(),
		                                              datagramSocket.getLocalPort() );
		System.out.println( "Opened Datagram Socket (sending) "+ datagramAddress );

		// set up the multicast socket for listening on
		this.multicastPort = 8888;
		this.multicastIp = InetAddress.getByName( "230.1.1.1" );
		this.multicastAddress = new InetSocketAddress( multicastIp, multicastPort );
		this.multicastSocket = new MulticastSocket( multicastPort );
		this.multicastSocket.joinGroup( multicastIp );
		System.out.println( "Joined Multicast Group " + multicastAddress );

		// kick off a multicast listener if we are the master
		Thread thread = new Thread( new MulticastListener(multicastSocket) );
		thread.start();
		System.out.println( "Started MulticastListener" );
		
	}
	
	public void execute() throws Exception
	{
		// do the socket configuration
		configure();
		
		// send out the multicast request for master information
		discoverMaster();
	}

	public void discoverMaster() throws Exception
	{
		System.out.println( "Attempting to discover master" );
		
		// send out a request, looking for any masters
		byte[] request = "Master?".getBytes();
		DatagramPacket datagram = new DatagramPacket( request, request.length, multicastAddress );
		datagramSocket.send( datagram );
		
		try
		{
			Thread.sleep( 3000 );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return;
		}
		
		// has the master been found?
		if( masterAddress == null )
		{
			synchronized( masterLock )
			{
				this.master = true;
				this.masterAddress = this.datagramAddress;
				DatagramPacket listPacket = newMember( this.masterAddress );
				
				// artifical delay to allow me to purposefully screw things up
				try{ Thread.sleep(1000); }catch(Exception e){ e.printStackTrace(); }
				datagramSocket.send( listPacket );
				System.out.println( "No master present, we have become master: "+masterAddress );
			}
		}
		
	}
	
	private DatagramPacket newMember( InetSocketAddress address ) throws Exception
	{
		// this is a request for the master and we are it
		// add the client to the list of members and send it to them
		list.add( address );
		StringBuilder builder = new StringBuilder();
		builder.append( "Members: " );
		for( InetSocketAddress sa : list )
		{
			builder.append( sa.getAddress().getHostAddress() );
			builder.append( ":" );
			builder.append( sa.getPort() );
			builder.append( " " );
		}
		
		byte[] listBytes = builder.toString().getBytes();
		byte[] responsePayload = new byte[8+listBytes.length];
		Common.longToByteArray( System.currentTimeMillis(), responsePayload, 0 );
		System.arraycopy( listBytes, 0, responsePayload, 8, listBytes.length );
		
		DatagramPacket packet = new DatagramPacket( responsePayload,
		                                            responsePayload.length,
		                                            multicastAddress );
		
		return packet;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args ) throws Exception
	{
		new Client().execute();
	}

	//////////////////////////////////////////////////////////////
	///////////////////// Multicast Listener /////////////////////
	//////////////////////////////////////////////////////////////
	public class MulticastListener implements Runnable
	{
		private MulticastSocket socket;
		public MulticastListener( MulticastSocket socket )
		{
			this.socket = socket;
		}
		
		public void run()
		{
			while( true )
			{
				try
				{
    				byte[] buffer = new byte[65536];
    				DatagramPacket datagram = new DatagramPacket( buffer, 65536 );
    				socket.receive( datagram );

    				String payload = new String( datagram.getData(),
    				                             datagram.getOffset(),
    				                             datagram.getLength() );

    				System.out.println( "Received packet from "+datagram.getAddress()+":"+
    				                    datagram.getPort()+" ["+payload+"]" );

    				if( payload.startsWith("Master?") && master )
    				{
    					// this is a request for the master and we are it
    					// add the client to the list of members and send it to them
    					DatagramPacket response =
    						newMember( (InetSocketAddress)datagram.getSocketAddress() );
    					datagramSocket.send( response );
    					System.out.println( "New member added to group: " + datagram.getSocketAddress() );
    					continue;
    				}
    				else if( master )
    				{
    					continue;
    				}
    				else if( payload.startsWith("Members:") )
    				{
    					// break it down into a list, store it and store the master
    					payload = payload.replace( "Members: ", "" );
    					StringTokenizer tokenizer = new StringTokenizer( payload, " " );
    					
    					// get the master
    					masterAddress = getAddress( tokenizer.nextToken() );
    					list.add( masterAddress );

    					// get the rest of the clients
    					while( tokenizer.hasMoreTokens() )
    					{
    						String next = tokenizer.nextToken().trim();
    						if( next.trim().equals("") )
    							break;
    						else
    							list.add( getAddress(next) );
    					}
    					
    					System.out.println( "Received response from master, list is: "+list );
    				}
    				else
    				{
    					//System.out.println( "Unknown payload: ["+payload+"]" );
    				}
				}
				catch( Exception e )
				{
					e.printStackTrace();
					return;
				}
			}			
		}
		
		private InetSocketAddress getAddress( String fullString ) throws Exception
		{
			int index = fullString.indexOf( ":" );
			String addressString = fullString.substring( 0, index );
			String portString = fullString.substring( index+1 );
			InetAddress address = InetAddress.getByName( addressString );
			return new InetSocketAddress( address, Integer.parseInt(portString) );
		}

	}
}
