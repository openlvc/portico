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
package hlaunit.hla13.misc;

import org.testng.annotations.Test;

import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

@Test(singleThreaded=true,groups= {"profileReflections","misc","ProfilerTest"})
public class ProfilerTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	//public static final int ITERATIONS = 10000000;
	public static final int ITERATIONS = 10000;
	public static final int PRINT_EVERY = 1000;
	public static volatile boolean COMPLETED_UPDATING = false;
	public static volatile boolean RECEIVER_FINISHED = false;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Test(enabled=false)
	public void testRapidMessages()
	{
		Test13Federate sender = new Test13Federate( "sender", this );
		Test13Federate receiver = new Test13Federate( "receiver", this );
		sender.quickCreate();
		sender.quickJoin();
		receiver.quickJoin();
		
		sender.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		receiver.quickSubscribe( "ObjectRoot.A", "aa", "ab", "ac" );
		int handle = sender.quickRegister( "ObjectRoot.A" );
		receiver.fedamb.waitForDiscovery( handle );
		sender.quickTick( 0.5, 0.5 );
		receiver.quickTick( 0.5, 0.5 );
		
		// start working
		Thread senderThread = new Sender( sender, handle );
		Thread receiverThread = new Receiver( receiver, handle );
		receiverThread.start();
		senderThread.start();
		
		while( RECEIVER_FINISHED == false )
		{
			try
			{
				Thread.sleep( 100 );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		
		sender.quickResign();
		receiver.quickResign();
		sender.quickDestroy();
	}

	private int getQueueSize( Test13Federate federate )
	{
		return ((org.portico.impl.hla13.Rti13Ambassador)federate.rtiamb).getHelper().getState().getQueue().getSize();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	@Test(enabled=false)
	public static void main( String[] args )
	{
		ProfilerTest test = new ProfilerTest();
		test.testRapidMessages();
	}

	public class Sender extends Thread
	{
		private Test13Federate federate;
		private int objectHandle;
		public Sender( Test13Federate federate, int objectHandle )
		{
			this.federate = federate;
			this.objectHandle = objectHandle;
		}
		
		public void run()
		{
			long startTime = System.currentTimeMillis();
			
			for( int i = 0; i < ITERATIONS; i++ )
			{
				federate.quickReflect( objectHandle, "aa", "ab", "ac" );
				federate.quickTick();
				if( (i % PRINT_EVERY) == 0 )
				{
					System.out.println( "Completed " + i +
					                    " updates, queue="+getQueueSize(federate) );
				}
			}
			System.out.println( "(updated) " + ITERATIONS + " updates" );
			COMPLETED_UPDATING = true;
			
			long endTime = System.currentTimeMillis();
			System.out.println( " ======> sender took " + (endTime-startTime) +
			                    " millis to send " + ITERATIONS + " updates" );
		}
	}
	
	public class Receiver extends Thread
	{
		private Test13Federate federate;
		private int objectHandle;
		private int lastBlock = 0;
		public Receiver( Test13Federate federate, int objectHandle )
		{
			this.federate = federate;
			this.objectHandle = objectHandle;
		}
		
		public void run()
		{
			while( COMPLETED_UPDATING == false )
			{
				federate.quickTick();
				int current = federate.fedamb.getRoUpdateCount(objectHandle);
				if( current >= lastBlock+PRINT_EVERY )
				{
					lastBlock = current;
					System.out.println( "(received) " + current +
					                    " reflections, queue=" + getQueueSize(federate) );
				}
			}
			
			// do some more just to flush the queue
			for( int i = 0; i < 100; i++ )
			{
				federate.quickTick();
			}
			System.out.println( "(received) count: " + federate.fedamb.getUpdateCount(objectHandle) );
			RECEIVER_FINISHED = true;
		}
	}
}
