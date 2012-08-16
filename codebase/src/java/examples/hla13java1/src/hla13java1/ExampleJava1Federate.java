/*
 *   Copyright 2007 The Portico Project
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
package hla13java1;

import hla.rti13.java1.AttributeHandleSet;
import hla.rti13.java1.AttributeHandleSetFactory;
import hla.rti13.java1.EncodingHelpers;
import hla.rti13.java1.FederatesCurrentlyJoined;
import hla.rti13.java1.FederationExecutionAlreadyExists;
import hla.rti13.java1.FederationExecutionDoesNotExist;
import hla.rti13.java1.RTIambassador;
import hla.rti13.java1.RTIexception;
import hla.rti13.java1.ResignAction;
import hla.rti13.java1.SuppliedAttributes;
import hla.rti13.java1.SuppliedAttributesFactory;
import hla.rti13.java1.SuppliedParameters;
import hla.rti13.java1.SuppliedParametersFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This is an example federate demonstrating how to properly use the HLA 1.3 Java
 * interface supplied with Portico.
 * 
 * As it is intended for example purposes, this is a rather simple federate. The
 * process is goes through is as follows:
 * 
 *  1. Create the RTIambassador
 *  2. Try to create the federation (nofail)
 *  3. Join the federation
 *  4. Announce a Synchronization Point (nofail)
 *  5. Wait for the federation to Synchronized on the point
 *  6. Enable Time Regulation and Constrained
 *  7. Publish and Subscribe
 *  8. Register an Object Instance
 *  9. Main Simulation Loop (executes 20 times)
 *       9.1 Update attributes of registered object
 *       9.2 Send an Interaction
 *       9.3 Advance time by 1.0
 * 10. Delete the Object Instance
 * 11. Resign from Federation
 * 12. Try to destroy the federation (nofail)
 * 
 * NOTE: Those items marked with (nofail) deal with situations where multiple
 *       federates may be working in the federation. In this sitaution, the
 *       federate will attempt to carry out the tasks defined, but it won't
 *       stop or exit if they fail. For example, if another federate has already
 *       created the federation, the call to create it again will result in an
 *       exception. The example federate expects this and will not fail.
 * NOTE: Between actions 4. and 5., the federate will pause until the uses presses
 *       the enter key. This will give other federates a chance to enter the
 *       federation and prevent other federates from racing ahead.
 * 
 * 
 * The main method to take notice of is {@link #runFederate(String)}. It controls the
 * main simulation loop and triggers most of the important behaviour. To make the code
 * simpler to read and navigate, many of the important HLA activities are broken down
 * into separate methods. For example, if you want to know how to send an interaction,
 * see the {@link #sendInteraction()} method.
 * 
 * With regard to the FederateAmbassador, it will log all incoming information. Thus,
 * if it receives any reflects or interactions etc... you will be notified of them.
 * 
 * Note that all of the methods throw an RTIexception. This class is the parent of all
 * HLA exceptions. The HLA Java interface is full of exceptions, with only a handful 
 * being actually useful. To make matters worse, they're all checked exceptions, so
 * unlike C++, we are forced to handle them by the compiler. This is unnecessary in
 * this small example, so we'll just throw all exceptions out to the main method and
 * handle them there, rather than handling each exception independently as they arise.
 */
public class ExampleJava1Federate
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** The number of times we will update our attributes and send an interaction */
	public static final int ITERATIONS = 20;

	/** The sync point all federates will sync up on before starting */
	public static final String READY_TO_RUN = "ReadyToRun";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTIambassador rtiamb;
	private ExampleJava1FederateAmbassador fedamb;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This is just a helper method to make sure all logging it output in the same form
	 */
	private void log( String message )
	{
		System.out.println( "ExampleFederate   : " + message );
	}

	/**
	 * This method will block until the user presses enter
	 */
	private void waitForUser()
	{
		log( " >>>>>>>>>> Press Enter to Continue <<<<<<<<<<" );
		BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
		try
		{
			reader.readLine();
		}
		catch( Exception e )
		{
			log( "Error while waiting for user input: " + e.getMessage() );
			e.printStackTrace();
		}
	}

	/**
	 * Convers a double time to the expected serialized form
	 */
	private byte[] convertTime( double time )
	{
		try
		{
			// PORTICO SPECIFIC!!
			return EncodingHelpers.encodeDouble( time );
		}
		catch( Exception e )
		{
			log( "Exception during time conversion" );
			e.printStackTrace();
			return null;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	////////////////////////// Main Simulation Method /////////////////////////
	///////////////////////////////////////////////////////////////////////////

	/**
	 * This is the main simulation loop. It can be thought of as the main method of
	 * the federate. For a description of the basic flow of this federate, see the
	 * class level comments
	 */
	public void runFederate( String federateName ) throws RTIexception
	{
		/////////////////////////////////
		// 1. create the RTIambassador //
		/////////////////////////////////
		rtiamb = new RTIambassador();

		//////////////////////////////////////////
		// 2. create and join to the federation //
		//////////////////////////////////////////
		// create
		// NOTE: some other federate may have already created the federation,
		//       in that case, we'll just try and join it
		try
		{
			rtiamb.createFederationExecution( "ExampleFederation", "testfom.fed" );
			log( "Created Federation" );
		}
		catch( FederationExecutionAlreadyExists exists )
		{
			log( "Didn't create federation, it already existed" );
		}
		
		////////////////////////////
		// 3. join the federation //
		////////////////////////////
		// create the federate ambassador and join the federation
		fedamb = new ExampleJava1FederateAmbassador();
		rtiamb.joinFederationExecution( federateName, "ExampleFederation", fedamb );
		log( "Joined Federation as " + federateName );

		////////////////////////////////
		// 4. announce the sync point //
		////////////////////////////////
		// announce a sync point to get everyone on the same page. if the point
		// has already been registered, we'll get a callback saying it failed,
		// but we don't care about that, as long as someone registered it
		rtiamb.registerFederationSynchronizationPoint( READY_TO_RUN, null );
		// wait until the point is announced
		while( fedamb.isAnnounced == false )
		{
			rtiamb.tick();
		}

		// WAIT FOR USER TO KICK US OFF
		// So that there is time to add other federates, we will wait until the
		// user hits enter before proceeding. That was, you have time to start
		// other federates.
		waitForUser();

		///////////////////////////////////////////////////////
		// 5. achieve the point and wait for synchronization //
		///////////////////////////////////////////////////////
		// tell the RTI we are ready to move past the sync point and then wait
		// until the federation has synchronized on
		rtiamb.synchronizationPointAchieved( READY_TO_RUN );
		log( "Achieved sync point: " +READY_TO_RUN+ ", waiting for federation..." );
		while( fedamb.isReadyToRun == false )
		{
			rtiamb.tick();
		}

		/////////////////////////////
		// 6. enable time policies //
		/////////////////////////////
		// in this section we enable/disable all time policies
		// note that this step is optional!
		enableTimePolicy();
		log( "Time Policy Enabled" );

		//////////////////////////////
		// 7. publish and subscribe //
		//////////////////////////////
		// in this section we tell the RTI of all the data we are going to
		// produce, and all the data we want to know about
		publishAndSubscribe();
		log( "Published and Subscribed" );

		/////////////////////////////////////
		// 8. register an object to update //
		/////////////////////////////////////
		int objectHandle = registerObject();
		log( "Registered Object, handle=" + objectHandle );
		
		////////////////////////////////////
		// 9. do the main simulation loop //
		////////////////////////////////////
		// here is where we do the meat of our work. in each iteration, we will
		// update the attribute values of the object we registered, and will
		// send an interaction.
		for( int i = 0; i < ITERATIONS; i++ )
		{
			// 9.1 update the attribute values of the instance //
			updateAttributeValues( objectHandle );
			
			// 9.2 send an interaction
			sendInteraction();
			
			// 9.3 request a time advance and wait until we get it
			advanceTime( 1.0 );
			log( "Time Advanced to " + fedamb.federateTime );
		}

		//////////////////////////////////////
		// 10. delete the object we created //
		//////////////////////////////////////
		deleteObject( objectHandle );
		log( "Deleted Object, handle=" + objectHandle );

		////////////////////////////////////
		// 11. resign from the federation //
		////////////////////////////////////
		rtiamb.resignFederationExecution( ResignAction.NO_ACTION );
		log( "Resigned from Federation" );

		////////////////////////////////////////
		// 12. try and destroy the federation //
		////////////////////////////////////////
		// NOTE: we won't die if we can't do this because other federates
		//       remain. in that case we'll leave it for them to clean up
		try
		{
			rtiamb.destroyFederationExecution( "ExampleFederation" );
			log( "Destroyed Federation" );
		}
		catch( FederationExecutionDoesNotExist dne )
		{
			log( "No need to destroy federation, it doesn't exist" );
		}
		catch( FederatesCurrentlyJoined fcj )
		{
			log( "Didn't destroy federation, federates still joined" );
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Helper Methods //////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will attempt to enable the various time related properties for
	 * the federate
	 */
	private void enableTimePolicy() throws RTIexception
	{
		////////////////////////////
		// enable time regulation //
		////////////////////////////
		this.rtiamb.enableTimeRegulation( convertTime(fedamb.federateTime),
		                                  convertTime(fedamb.federateLookahead) );

		// tick until we get the callback
		while( fedamb.isRegulating == false )
		{
			rtiamb.tick();
		}
		
		/////////////////////////////
		// enable time constrained //
		/////////////////////////////
		this.rtiamb.enableTimeConstrained();
		
		// tick until we get the callback
		while( fedamb.isConstrained == false )
		{
			rtiamb.tick();
		}
	}
	
	/**
	 * This method will inform the RTI about the types of data that the federate will
	 * be creating, and the types of data we are interested in hearing about as other
	 * federates produce it.
	 */
	private void publishAndSubscribe() throws RTIexception
	{
		////////////////////////////////////////////
		// publish all attributes of ObjectRoot.A //
		////////////////////////////////////////////
		// before we can register instance of the object class ObjectRoot.A and
		// update the values of the various attributes, we need to tell the RTI
		// that we intend to publish this information

		// get all the handle information for the attributes of ObjectRoot.A
		int classHandle = rtiamb.getObjectClassHandle( "ObjectRoot.A" );
		int aaHandle    = rtiamb.getAttributeHandle( "aa", classHandle );
		int abHandle    = rtiamb.getAttributeHandle( "ab", classHandle );
		int acHandle    = rtiamb.getAttributeHandle( "ac", classHandle );

		// package the information into a handle set
		AttributeHandleSet attributes = AttributeHandleSetFactory.create( 3 );
		attributes.add( aaHandle );
		attributes.add( abHandle );
		attributes.add( acHandle );
		
		// do the actual publication
		rtiamb.publishObjectClass( classHandle, attributes );

		/////////////////////////////////////////////////
		// subscribe to all attributes of ObjectRoot.A //
		/////////////////////////////////////////////////
		// we also want to hear about the same sort of information as it is
		// created and altered in other federates, so we need to subscribe to it
		
		rtiamb.subscribeObjectClassAttributes( classHandle, attributes );

		/////////////////////////////////////////////////////
		// publish the interaction class InteractionRoot.X //
		/////////////////////////////////////////////////////
		// we want to send interactions of type InteractionRoot.X, so we need
		// to tell the RTI that we're publishing it first. We don't need to
		// inform it of the parameters, only the class, making it much simpler
		int interactionHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.X" );
		
		// do the publication
		rtiamb.publishInteractionClass( interactionHandle );

		////////////////////////////////////////////////////
		// subscribe to the InteractionRoot.X interaction //
		////////////////////////////////////////////////////
		// we also want to receive other interaction of the same type that are
		// sent out by other federates, so we have to subscribe to it first
		rtiamb.subscribeInteractionClass( interactionHandle );
	}
	
	/**
	 * This method will register an instance of the class ObjectRoot.A and will
	 * return the federation-wide unique handle for that instance. Later in the
	 * simulation, we will update the attribute values for this instance
	 */
	private int registerObject() throws RTIexception
	{
		int classHandle = rtiamb.getObjectClassHandle( "ObjectRoot.A" );
		return rtiamb.registerObjectInstance( classHandle );
	}
	
	/**
	 * This method will update all the values of the given object instance. It will
	 * set each of the values to be a string which is equal to the name of the
	 * attribute plus the current time. eg "aa:10.0" if the time is 10.0.
	 * <p/>
	 * Note that we don't actually have to update all the attributes at once, we
	 * could update them individually, in groups or not at all!
	 */
	private void updateAttributeValues( int objectHandle ) throws RTIexception
	{
		///////////////////////////////////////////////
		// create the necessary container and values //
		///////////////////////////////////////////////
		// create the collection to store the values in, as you can see
		// this is quite a lot of work
		SuppliedAttributes attributes = SuppliedAttributesFactory.create( 3 );
		
		// generate the new values
		// we use EncodingHelpers to make things nice friendly for both Java and C++
		byte[] aaValue = EncodingHelpers.encodeString( "aa:" + getLbts() );
		byte[] abValue = EncodingHelpers.encodeString( "ab:" + getLbts() );
		byte[] acValue = EncodingHelpers.encodeString( "ac:" + getLbts() );
		
		// get the handles
		// this line gets the object class of the instance identified by the
		// object instance the handle points to
		int classHandle = rtiamb.getObjectClass( objectHandle );
		int aaHandle = rtiamb.getAttributeHandle( "aa", classHandle );
		int abHandle = rtiamb.getAttributeHandle( "ab", classHandle );
		int acHandle = rtiamb.getAttributeHandle( "ac", classHandle );

		// put the values into the collection
		attributes.add( aaHandle, aaValue );
		attributes.add( abHandle, abValue );
		attributes.add( acHandle, acValue );

		//////////////////////////
		// do the actual update //
		//////////////////////////
		rtiamb.updateAttributeValues( objectHandle, attributes, generateTag() );
		
		// note that if you want to associate a particular timestamp with the
		// update. here we send another update, this time with a timestamp:
		byte[] time = convertTime( fedamb.federateTime + fedamb.federateLookahead );
		rtiamb.updateAttributeValues( objectHandle, attributes, time, generateTag() );
	}
	
	/**
	 * This method will send out an interaction of the type InteractionRoot.X. Any
	 * federates which are subscribed to it will receive a notification the next time
	 * they tick(). Here we are passing only two of the three parameters we could be
	 * passing, but we don't actually have to pass any at all!
	 */
	private void sendInteraction() throws RTIexception
	{
		///////////////////////////////////////////////
		// create the necessary container and values //
		///////////////////////////////////////////////
		// create the collection to store the values in
		SuppliedParameters parameters = SuppliedParametersFactory.create( 2 );
		
		// generate the new values
		// we use EncodingHelpers to make things nice friendly for both Java and C++
		byte[] xaValue = EncodingHelpers.encodeString( "xa:" + getLbts() );
		byte[] xbValue = EncodingHelpers.encodeString( "xb:" + getLbts() );
		
		// get the handles
		int classHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.X" );
		int xaHandle = rtiamb.getParameterHandle( "xa", classHandle );
		int xbHandle = rtiamb.getParameterHandle( "xb", classHandle );

		// put the values into the collection
		parameters.add( xaHandle, xaValue );
		parameters.add( xbHandle, xbValue );

		//////////////////////////
		// send the interaction //
		//////////////////////////
		rtiamb.sendInteraction( classHandle, parameters, generateTag() );
		
		// if you want to associate a particular timestamp with the
		// interaction, you will have to supply it to the RTI. Here
		// we send another interaction, this time with a timestamp:
		byte[] time = convertTime( fedamb.federateTime + fedamb.federateLookahead );
		rtiamb.sendInteraction( classHandle, parameters, time, generateTag() );
	}

	/**
	 * This method will request a time advance to the current time, plus the given
	 * timestep. It will then wait until a notification of the time advance grant
	 * has been received.
	 */
	private void advanceTime( double timestep ) throws RTIexception
	{
		// request the advance
		fedamb.isAdvancing = true;
		byte[] newTime = convertTime( fedamb.federateTime + timestep );
		rtiamb.timeAdvanceRequest( newTime );
		
		// wait for the time advance to be granted. ticking will tell the
		// LRC to start delivering callbacks to the federate
		while( fedamb.isAdvancing )
		{
			rtiamb.tick();
		}
	}

	/**
	 * This method will attempt to delete the object instance of the given
	 * handle. We can only delete objects we created, or for which we own the
	 * privilegeToDelete attribute.
	 */
	private void deleteObject( int handle ) throws RTIexception
	{
		rtiamb.deleteObjectInstance( handle, generateTag() );
	}

	private double getLbts()
	{
		return fedamb.federateTime + fedamb.federateLookahead;
	}

	private String generateTag()
	{
		return ""+System.currentTimeMillis();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args )
	{
		// get a federate name, use "exampleFederate" as default
		String federateName = "exampleFederate";
		if( args.length != 0 )
		{
			federateName = args[0];
		}
		
		try
		{
			// run the example federate
			new ExampleJava1Federate().runFederate( federateName );
		}
		catch( RTIexception rtie )
		{
			// an exception occurred, just log the information and exit
			rtie.printStackTrace();
		}
	}
}
