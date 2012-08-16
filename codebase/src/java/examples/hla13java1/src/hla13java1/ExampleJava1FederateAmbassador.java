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

import hla.rti13.java1.EncodingHelpers;
import hla.rti13.java1.EventRetractionHandle;
import hla.rti13.java1.NullFederateAmbassador;
import hla.rti13.java1.ReceivedInteraction;
import hla.rti13.java1.ReflectedAttributes;

/**
 * This class handles all incoming callbacks from the RTI regarding a particular
 * {@link ExampleJava1Federate}. It will log information about any callbacks it
 * receives, thus demonstrating how to deal with the provided callback information.
 */
public class ExampleJava1FederateAmbassador extends NullFederateAmbassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// these variables are accessible in the package
	protected double federateTime        = 0.0;
	protected double federateLookahead   = 1.0;
	
	protected boolean isRegulating       = false;
	protected boolean isConstrained      = false;
	protected boolean isAdvancing        = false;
	
	protected boolean isAnnounced        = false;
	protected boolean isReadyToRun       = false;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public ExampleJava1FederateAmbassador()
	{
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	public double convertTime( byte[] time )
	{
		try
		{
			return EncodingHelpers.decodeDouble( time );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return -1.0;
		}
	}
	
	private void log( String message )
	{
		System.out.println( "FederateAmbassador: " + message );
	}
	
	//////////////////////////////////////////////////////////////////////////
	////////////////////////// RTI Callback Methods //////////////////////////
	//////////////////////////////////////////////////////////////////////////
	public void synchronizationPointRegistrationFailed( String label )
	{
		log( "Failed to register sync point: " + label );
	}

	public void synchronizationPointRegistrationSucceeded( String label )
	{
		log( "Successfully registered sync point: " + label );
	}

	public void announceSynchronizationPoint( String label, String tag )
	{
		log( "Synchronization point announced: " + label );
		if( label.equals(ExampleJava1Federate.READY_TO_RUN) )
			this.isAnnounced = true;
	}

	public void federationSynchronized( String label )
	{
		log( "Federation Synchronized: " + label );
		if( label.equals(ExampleJava1Federate.READY_TO_RUN) )
			this.isReadyToRun = true;
	}

	/**
	 * The RTI has informed us that time regulation is now enabled.
	 */
	public void timeRegulationEnabled( byte[] theFederateTime )
	{
		this.federateTime = convertTime( theFederateTime );
		this.isRegulating = true;
	}

	public void timeConstrainedEnabled( byte[] theFederateTime )
	{
		this.federateTime = convertTime( theFederateTime );
		this.isConstrained = true;
	}

	public void timeAdvanceGrant( byte[] theTime )
	{
		this.federateTime = convertTime( theTime );
		this.isAdvancing = false;
	}

	public void discoverObjectInstance( int theObject,
	                                    int theObjectClass,
	                                    String objectName )
	{
		log( "Discoverd Object: handle=" + theObject + ", classHandle=" +
		     theObjectClass + ", name=" + objectName );
	}

	public void reflectAttributeValues( int theObject,
	                                    ReflectedAttributes theAttributes,
	                                    String tag )
	{
		// just pass it on to the other method for printing purposes
		// passing null as the time will let the other method know it
		// it from us, not from the RTI
		reflectAttributeValues( theObject, theAttributes, null, tag, null );
	}

	public void reflectAttributeValues( int theObject,
	                                    ReflectedAttributes theAttributes,
	                                    byte[] theTime,
	                                    String theTag,
	                                    EventRetractionHandle theHandle )
	{
		StringBuilder builder = new StringBuilder( "Reflection for object:" );
		
		// print the handle
		builder.append( " handle=" + theObject );
		// print the tag
		builder.append( ", tag=" + theTag );
		// print the time (if we have it) we'll get null if we are just receiving
		// a forwarded call from the other reflect callback above
		if( theTime != null )
		{
			builder.append( ", time=" + convertTime(theTime) );
		}
		
		// print the attribute information
		builder.append( ", attributeCount=" + theAttributes.size() );
		builder.append( "\n" );
		for( int i = 0; i < theAttributes.size(); i++ )
		{
			try
			{
				// print the attibute handle
				builder.append( "\tattributeHandle=" );
				builder.append( theAttributes.getHandle(i) );
				// print the attribute value
				builder.append( ", attributeValue=" );
				builder.append(
				    EncodingHelpers.decodeString(theAttributes.getValue(i)) );
				builder.append( "\n" );
			}
			catch( Exception e )
			{
				log( "Exception processing received reflection" );
				e.printStackTrace();
			}
		}
		
		log( builder.toString() );
	}

	public void receiveInteraction( int interactionClass,
	                                ReceivedInteraction theInteraction,
	                                String tag )
	{
		// just pass it on to the other method for printing purposes
		// passing null as the time will let the other method know it
		// it from us, not from the RTI
		receiveInteraction( interactionClass, theInteraction, null, tag, null );
	}

	public void receiveInteraction( int interactionClass,
	                                ReceivedInteraction theInteraction,
	                                byte[] theTime,
	                                String theTag,
	                                EventRetractionHandle eventRetractionHandle )
	{
		StringBuilder builder = new StringBuilder( "Interaction Received:" );
		
		// print the handle
		builder.append( " handle=" + interactionClass );
		// print the tag
		builder.append( ", tag=" + theTag );
		// print the time (if we have it) we'll get null if we are just receiving
		// a forwarded call from the other reflect callback above
		if( theTime != null )
		{
			builder.append( ", time=" + convertTime(theTime) );
		}
		
		// print the parameter information
		builder.append( ", parameterCount=" + theInteraction.size() );
		builder.append( "\n" );
		for( int i = 0; i < theInteraction.size(); i++ )
		{
			try
			{
				// print the parameter handle
				builder.append( "\tparamHandle=" );
				builder.append( theInteraction.getHandle(i) );
				// print the parameter value
				builder.append( ", paramValue=" );
				builder.append(
				    EncodingHelpers.decodeString(theInteraction.getValue(i)) );
				builder.append( "\n" );
			}
			catch( Exception e )
			{
				log( "Exception processing received interaction" );
				e.printStackTrace();
			}
		}

		log( builder.toString() );
	}

	public void removeObjectInstance( int theObject, String userSuppliedTag )
	{
		log( "Object Removed: handle=" + theObject );
	}

	public void removeObjectInstance( int theObject,
	                                  byte[] theTime,
	                                  String theTag,
	                                  EventRetractionHandle theHandle )
	{
		log( "Object Removed: handle=" + theObject );
	}


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
