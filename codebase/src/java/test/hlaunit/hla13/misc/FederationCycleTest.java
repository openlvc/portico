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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import org.testng.annotations.Test;

import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

@Test(singleThreaded=true,groups={"cycleFederation","misc","FederationCycleTest"})
public class FederationCycleTest extends Abstract13Test
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
	@Test(enabled=false)
	public void testFederationCycle() throws Exception
	{
		int federateCount = 10;
		List<TestFederateRunner> federates = generateFederates( federateCount );
		Test13Federate controller = federates.get(0).federate;
		CyclicBarrier readyBarrier = new CyclicBarrier( federateCount );
		CyclicBarrier joinedBarrier = new CyclicBarrier( federateCount );

		for( int i = 0; i < 100; i++ )
		{
			// create the federation
			controller.quickCreate();
			System.out.println( "     created federation" );
			
			// get everyone ready to go
			for( TestFederateRunner runner : federates )
			{
				runner.initialize( readyBarrier, joinedBarrier );
				runner.myThread.start();
			}

			for( TestFederateRunner runner : federates )
			{
				if( runner.myThread.isAlive() )
					runner.myThread.join();
			}
			
			// destroy the federation
			controller.quickDestroy();
			System.out.println( "     destroy federation" );

			// log some stuff
			if( (i%1) == 0 )
				System.out.println( "Completed " + (i+1) + " iterations" );
		}
	}

	private List<TestFederateRunner> generateFederates( int count )
	{
		List<TestFederateRunner> federates = new ArrayList<TestFederateRunner>();
		for( int i = 0; i < count; i++ )
		{
			federates.add( new TestFederateRunner( new Test13Federate("federate"+i,this)) );
		}
		
		return federates;
	}

	@Test(enabled=false)
	public void testSingleThreadLifecycle()
	{
		Test13Federate federateOne = new Test13Federate( "federateOne", this );
		Test13Federate federateTwo = new Test13Federate( "federateTwo", this );

		System.out.println( " ------> Creating federation" );
		//federateOne.quickCreate();
		System.out.println( " ------> Created federation" );
		
		System.out.println( " ------> Joining federateOne" );
		federateOne.quickJoin();
		System.out.println( "         complete: handle="+federateOne.federateHandle );

		System.out.println( " ------> Joining federateTwo" );
		federateTwo.quickJoin();
		System.out.println( "         complete: handle="+federateTwo.federateHandle );
		
		System.out.println( " ------> Resigning federateTwo" );
		federateTwo.quickResign();
		System.out.println( "         complete: handle="+federateTwo.federateHandle );

		System.out.println( " ------> Joining federateTwo (again)" );
		federateTwo.quickJoin();
		System.out.println( "         complete: handle="+federateTwo.federateHandle );

		System.out.println( " ------> Resigning federateOne" );
		federateOne.quickResign();
		System.out.println( "         complete: handle="+federateTwo.federateHandle );

		System.out.println( " ------> Resigning federateTwo" );
		federateTwo.quickResign();
		System.out.println( "         complete: handle="+federateTwo.federateHandle );

		System.out.println( " ------> Destroying federation" );
		federateOne.quickDestroy();
		System.out.println( " ------> Destroyed federation" );
	}
	

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	@Test(enabled=false)
	public static void main( String[] args ) throws Exception
	{
		FederationCycleTest test = new FederationCycleTest();
		test.testFederationCycle();
	}
	
	private class TestFederateRunner implements Runnable
	{
		protected Test13Federate federate;
		protected Thread myThread;
		protected CyclicBarrier readyBarrier;
		protected CyclicBarrier joinedBarrier;
		private boolean initialized;
		public TestFederateRunner( Test13Federate federate )
		{
			this.federate = federate;
			this.initialized = false;
		}

		public void initialize( CyclicBarrier readyBarrier, CyclicBarrier joinedBarrier )
		{
			this.myThread = new Thread( this, federate.federateName );
			this.readyBarrier = readyBarrier;
			this.joinedBarrier = joinedBarrier;
			this.initialized = true;
		}

		public void run()
		{
			if( !initialized )
				throw new RuntimeException( "Not initialized" );
			
			try
			{
				System.out.println( "     starting thread: " + federate.federateName );
				this.readyBarrier.await();
				this.federate.quickJoin();
				System.out.println( "     joined federation: " + federate.federateName );
				this.joinedBarrier.await();
				this.federate.quickResign();
				System.out.println( "     resigned federation: " + federate.federateName );
				this.initialized = false; // reset
			}
			catch( Exception e )
			{
				System.out.println( federate.federateName+": "+e.getMessage() );
				//e.printStackTrace();
			}
		}
	}
}
