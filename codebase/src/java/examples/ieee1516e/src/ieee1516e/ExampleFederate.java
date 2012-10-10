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
package ieee1516e;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

/**
 * This is an example federate demonstrating how to properly use the IEEE 1516-2010 (HLA Evolved)
 * Java interface supplied with Portico. The code provided here is intended to break down many
 * common actions into their atomic elemenets and form a demonstration of the processed needed to
 * perform them. As such, the scenario has been kept purposefully simple.
 * 
 * As it is intended for example purposes, this is a rather simple federate. The
 * process is goes through is as follows:
 * 
 *  1.  Create the RTIambassador
 *  2.  Connect to the RTIamsassador
 *  3.  Try to create the federation (nofail)
 *  4.  Join the federation
 *  5.  Announce a Synchronization Point (nofail)
 *  6.  Wait for the federation to Synchronized on the point
 *  7.  Enable Time Regulation and Constrained
 *  8.  Publish and Subscribe
 *  9.  Register an Object Instance
 *  10. Main Simulation Loop (executes 20 times)
 *       10.1 Update attributes of registered object
 *       10.2 Send an Interaction
 *       10.3 Advance time by 1.0
 * 11. Delete the Object Instance
 * 12. Resign from Federation
 * 13. Try to destroy the federation (nofail)
 * 14. Disconnect from the RTI
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
 * 
 * **Modular FOMs**
 * The Portico 1516e RTI supports the use of modular FOMs when creating or joining a federation.
 * This example federate uses an example set of FOM modules produced by Pitch (http://pitch.se).
 * The example FOM is taken from the standard HLA Restaurant Operations model. It is split
 * over four separate modules:
 *   1. Restaurant Operations (RestaurantProcesses.xml)
 *   2. Restaurant Food       (RestaurantFood.xml)
 *   3. Restaurant Drinks     (RestaurantDrinks.xml)
 *   4. Restaurant Soup       (RestaurantSoup.xml)
 * 
 * In the demonstration, the first three modules are loaded as part of the federation creation
 * process, with the example federate providing the Soup-based extension when it joins.
 */
public class ExampleFederate
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
	private ExampleFederateAmbassador fedamb;  // created when we connect
	private HLAfloat64TimeFactory timeFactory; // set when we join
	protected EncoderFactory encoderFactory;     // set when we join

	// caches of handle types - set once we join a federation
	protected ObjectClassHandle sodaHandle;
	protected AttributeHandle cupsHandle;
	protected AttributeHandle flavHandle;
	protected InteractionClassHandle servedHandle;

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
	
	///////////////////////////////////////////////////////////////////////////
	////////////////////////// Main Simulation Method /////////////////////////
	///////////////////////////////////////////////////////////////////////////
	/**
	 * This is the main simulation loop. It can be thought of as the main method of
	 * the federate. For a description of the basic flow of this federate, see the
	 * class level comments
	 */
	public void runFederate( String federateName ) throws Exception
	{
		/////////////////////////////////////////////////
		// 1 & 2. create the RTIambassador and Connect //
		/////////////////////////////////////////////////
		log( "Creating RTIambassador" );
		rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		
		// connect
		log( "Connecting..." );
		fedamb = new ExampleFederateAmbassador( this );
		rtiamb.connect( fedamb, CallbackModel.HLA_EVOKED );

		//////////////////////////////
		// 3. create the federation //
		//////////////////////////////
		log( "Creating Federation..." );
		// We attempt to create a new federation with the first three of the
		// restaurant FOM modules covering processes, food and drink
		try
		{
			URL[] modules = new URL[]{
			    (new File("foms/RestaurantProcesses.xml")).toURI().toURL(),
			    (new File("foms/RestaurantFood.xml")).toURI().toURL(),
			    (new File("foms/RestaurantDrinks.xml")).toURI().toURL()
			};
			
			rtiamb.createFederationExecution( "ExampleFederation", modules );
			log( "Created Federation" );
		}
		catch( FederationExecutionAlreadyExists exists )
		{
			log( "Didn't create federation, it already existed" );
		}
		catch( MalformedURLException urle )
		{
			log( "Exception loading one of the FOM modules from disk: " + urle.getMessage() );
			urle.printStackTrace();
			return;
		}
		
		////////////////////////////
		// 4. join the federation //
		////////////////////////////
		URL[] joinModules = new URL[]{
		    (new File("foms/RestaurantSoup.xml")).toURI().toURL()
		};
		
		rtiamb.joinFederationExecution( federateName,            // name for the federate
		                                "ExampleFederateType",   // federate type
		                                "ExampleFederation",     // name of federation
		                                joinModules );           // modules we want to add

		log( "Joined Federation as " + federateName );
		
		// cache the time factory for easy access
		this.timeFactory = (HLAfloat64TimeFactory)rtiamb.getTimeFactory();

		////////////////////////////////
		// 5. announce the sync point //
		////////////////////////////////
		// announce a sync point to get everyone on the same page. if the point
		// has already been registered, we'll get a callback saying it failed,
		// but we don't care about that, as long as someone registered it
		rtiamb.registerFederationSynchronizationPoint( READY_TO_RUN, null );
		// wait until the point is announced
		while( fedamb.isAnnounced == false )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}

		// WAIT FOR USER TO KICK US OFF
		// So that there is time to add other federates, we will wait until the
		// user hits enter before proceeding. That was, you have time to start
		// other federates.
		waitForUser();

		///////////////////////////////////////////////////////
		// 6. achieve the point and wait for synchronization //
		///////////////////////////////////////////////////////
		// tell the RTI we are ready to move past the sync point and then wait
		// until the federation has synchronized on
		rtiamb.synchronizationPointAchieved( READY_TO_RUN );
		log( "Achieved sync point: " +READY_TO_RUN+ ", waiting for federation..." );
		while( fedamb.isReadyToRun == false )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}

		/////////////////////////////
		// 7. enable time policies //
		/////////////////////////////
		// in this section we enable/disable all time policies
		// note that this step is optional!
		enableTimePolicy();
		log( "Time Policy Enabled" );

		//////////////////////////////
		// 8. publish and subscribe //
		//////////////////////////////
		// in this section we tell the RTI of all the data we are going to
		// produce, and all the data we want to know about
		publishAndSubscribe();
		log( "Published and Subscribed" );

		/////////////////////////////////////
		// 9. register an object to update //
		/////////////////////////////////////
		ObjectInstanceHandle objectHandle = registerObject();
		log( "Registered Object, handle=" + objectHandle );
		
		/////////////////////////////////////
		// 10. do the main simulation loop //
		/////////////////////////////////////
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
		// 11. delete the object we created //
		//////////////////////////////////////
		deleteObject( objectHandle );
		log( "Deleted Object, handle=" + objectHandle );

		////////////////////////////////////
		// 12. resign from the federation //
		////////////////////////////////////
		rtiamb.resignFederationExecution( ResignAction.DELETE_OBJECTS );
		log( "Resigned from Federation" );

		////////////////////////////////////////
		// 13. try and destroy the federation //
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
	private void enableTimePolicy() throws Exception
	{
		// NOTE: Unfortunately, the LogicalTime/LogicalTimeInterval create code is
		//       Portico specific. You will have to alter this if you move to a
		//       different RTI implementation. As such, we've isolated it into a
		//       method so that any change only needs to happen in a couple of spots 
		HLAfloat64Interval lookahead = timeFactory.makeInterval( fedamb.federateLookahead );
		
		////////////////////////////
		// enable time regulation //
		////////////////////////////
		this.rtiamb.enableTimeRegulation( lookahead );

		// tick until we get the callback
		while( fedamb.isRegulating == false )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}
		
		/////////////////////////////
		// enable time constrained //
		/////////////////////////////
		this.rtiamb.enableTimeConstrained();
		
		// tick until we get the callback
		while( fedamb.isConstrained == false )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}
	}
	
	/**
	 * This method will inform the RTI about the types of data that the federate will
	 * be creating, and the types of data we are interested in hearing about as other
	 * federates produce it.
	 */
	private void publishAndSubscribe() throws RTIexception
	{
		///////////////////////////////////////////////
		// publish all attributes of Food.Drink.Soda //
		///////////////////////////////////////////////
		// before we can register instance of the object class Food.Drink.Soda and
		// update the values of the various attributes, we need to tell the RTI
		// that we intend to publish this information

		// get all the handle information for the attributes of Food.Drink.Soda
		this.sodaHandle = rtiamb.getObjectClassHandle( "HLAobjectRoot.Food.Drink.Soda" );
		this.cupsHandle = rtiamb.getAttributeHandle( sodaHandle, "NumberCups" );
		this.flavHandle = rtiamb.getAttributeHandle( sodaHandle, "Flavor" );
		// package the information into a handle set
		AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
		attributes.add( cupsHandle );
		attributes.add( flavHandle );
		
		// do the actual publication
		rtiamb.publishObjectClassAttributes( sodaHandle, attributes );

		////////////////////////////////////////////////////
		// subscribe to all attributes of Food.Drink.Soda //
		////////////////////////////////////////////////////
		// we also want to hear about the same sort of information as it is
		// created and altered in other federates, so we need to subscribe to it
		rtiamb.subscribeObjectClassAttributes( sodaHandle, attributes );

		//////////////////////////////////////////////////////////
		// publish the interaction class FoodServed.DrinkServed //
		//////////////////////////////////////////////////////////
		// we want to send interactions of type FoodServed.DrinkServed, so we need
		// to tell the RTI that we're publishing it first. We don't need to
		// inform it of the parameters, only the class, making it much simpler
		String iname = "HLAinteractionRoot.CustomerTransactions.FoodServed.DrinkServed";
		servedHandle = rtiamb.getInteractionClassHandle( iname );
		
		// do the publication
		rtiamb.publishInteractionClass( servedHandle );

		/////////////////////////////////////////////////////////
		// subscribe to the FoodServed.DrinkServed interaction //
		/////////////////////////////////////////////////////////
		// we also want to receive other interaction of the same type that are
		// sent out by other federates, so we have to subscribe to it first
		rtiamb.subscribeInteractionClass( servedHandle );
	}
	
	/**
	 * This method will register an instance of the Soda class and will
	 * return the federation-wide unique handle for that instance. Later in the
	 * simulation, we will update the attribute values for this instance
	 */
	private ObjectInstanceHandle registerObject() throws RTIexception
	{
		return rtiamb.registerObjectInstance( sodaHandle );
	}
	
	/**
	 * This method will update all the values of the given object instance. It will
	 * set the flavour of the soda to a random value from the options specified in
	 * the FOM (Cola - 101, Orange - 102, RootBeer - 103, Cream - 104) and it will set
	 * the number of cups to the same value as the current time.
	 * <p/>
	 * Note that we don't actually have to update all the attributes at once, we
	 * could update them individually, in groups or not at all!
	 */
	private void updateAttributeValues( ObjectInstanceHandle objectHandle ) throws RTIexception
	{
		///////////////////////////////////////////////
		// create the necessary container and values //
		///////////////////////////////////////////////
		// create a new map with an initial capacity - this will grow as required
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
		
		// create the collection to store the values in, as you can see
		// this is quite a lot of work. You don't have to use the encoding
		// helpers if you don't want. The RTI just wants an arbitrary byte[]

		// generate the value for the number of cups (same as the timestep)
		HLAinteger16BE cupsValue = encoderFactory.createHLAinteger16BE( getTimeAsShort() );
		attributes.put( cupsHandle, cupsValue.toByteArray() );
		
		// generate the value for the flavour on our magically flavour changing drink
		// the values for the enum are defined in the FOM
		int randomValue = 101 + new Random().nextInt(3);
		HLAinteger32BE flavValue = encoderFactory.createHLAinteger32BE( randomValue );
		attributes.put( flavHandle, flavValue.toByteArray() );

		//////////////////////////
		// do the actual update //
		//////////////////////////
		rtiamb.updateAttributeValues( objectHandle, attributes, generateTag() );
		
		// note that if you want to associate a particular timestamp with the
		// update. here we send another update, this time with a timestamp:
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime+fedamb.federateLookahead );
		rtiamb.updateAttributeValues( objectHandle, attributes, generateTag(), time );
	}
	
	/**
	 * This method will send out an interaction of the type FoodServed.DrinkServed. Any
	 * federates which are subscribed to it will receive a notification the next time
	 * they tick(). This particular interaction has no parameters, so you pass an empty
	 * map, but the process of encoding them is the same as for attributes.
	 */
	private void sendInteraction() throws RTIexception
	{
		//////////////////////////
		// send the interaction //
		//////////////////////////
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(0);
		rtiamb.sendInteraction( servedHandle, parameters, generateTag() );
		
		// if you want to associate a particular timestamp with the
		// interaction, you will have to supply it to the RTI. Here
		// we send another interaction, this time with a timestamp:
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime+fedamb.federateLookahead );
		rtiamb.sendInteraction( servedHandle, parameters, generateTag(), time );
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
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + timestep );
		rtiamb.timeAdvanceRequest( time );
		
		// wait for the time advance to be granted. ticking will tell the
		// LRC to start delivering callbacks to the federate
		while( fedamb.isAdvancing )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}
	}

	/**
	 * This method will attempt to delete the object instance of the given
	 * handle. We can only delete objects we created, or for which we own the
	 * privilegeToDelete attribute.
	 */
	private void deleteObject( ObjectInstanceHandle handle ) throws RTIexception
	{
		rtiamb.deleteObjectInstance( handle, generateTag() );
	}

	private short getTimeAsShort()
	{
		return (short)fedamb.federateTime;
	}

	private byte[] generateTag()
	{
		return ("(timestamp) "+System.currentTimeMillis()).getBytes();
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
			new ExampleFederate().runFederate( federateName );
		}
		catch( Exception rtie )
		{
			// an exception occurred, just log the information and exit
			rtie.printStackTrace();
		}
	}
}