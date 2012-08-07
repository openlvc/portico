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
import java.util.ArrayList;
import java.util.List;

import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.ReflectedAttributes;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;

import org.portico.impl.hla13.types.DoubleTime;

/**
 * This class handles all the callbacks for the diagnostics federate. On each callback, it will
 * just create a string describing the event and add that string to a list of events that have
 * happened. At the end of processing, the contents of this list will be printed so we can see
 * exactly what events happened in what order.
 */
public class EventLoggerFedAmb extends NullFederateAmbassador
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
	
	protected List<String> events        = new ArrayList<String>();

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public EventLoggerFedAmb()
	{
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private double convertTime( LogicalTime logicalTime )
	{
		return ((DoubleTime)logicalTime).getTime();
	}
	
	private synchronized void queue( String message )
	{
		events.add( message );
	}
	
	//////////////////////////////////////////////////////////////////////////
	////////////////////////// RTI Callback Methods //////////////////////////
	//////////////////////////////////////////////////////////////////////////
	public void synchronizationPointRegistrationFailed( String label )
	{
		queue( "Failed to register sync point: " + label );
	}

	public void synchronizationPointRegistrationSucceeded( String label )
	{
		queue( "Successfully registered sync point: " + label );
	}

	public void announceSynchronizationPoint( String label, byte[] tag )
	{
		queue( "Synchronization point announced: " + label );
		if( label.equals(EventLogger.READY_TO_RUN) )
			this.isAnnounced = true;
	}

	public void federationSynchronized( String label )
	{
		queue( "Federation Synchronized: " + label );
		if( label.equals(EventLogger.READY_TO_RUN) )
			this.isReadyToRun = true;
	}

	/**
	 * The RTI has informed us that time regulation is now enabled.
	 */
	public void timeRegulationEnabled( LogicalTime theFederateTime )
	{
		this.federateTime = convertTime( theFederateTime );
		this.isRegulating = true;
		queue( "Regulation Enabled: time=" + federateTime );
	}

	public void timeConstrainedEnabled( LogicalTime theFederateTime )
	{
		this.federateTime = convertTime( theFederateTime );
		this.isConstrained = true;
		queue( "Constrained Enabled: time=" + federateTime );
	}

	public void timeAdvanceGrant( LogicalTime theTime )
	{
		this.federateTime = convertTime( theTime );
		this.isAdvancing = false;
		queue( "ADVANCE("+federateTime+") Granted: time=" + federateTime );
	}

	public void discoverObjectInstance( int theObject,
	                                    int theObjectClass,
	                                    String objectName )
	{
		queue( "DISCOVERY objectHandle=" + theObject + ", classHandle=" +
		       theObjectClass + ", name=" + objectName );
	}

	public void reflectAttributeValues( int theObject,
	                                    ReflectedAttributes theAttributes,
	                                    byte[] tag )
	{
		// just pass it on to the other method for printing purposes
		// passing null as the time will let the other method know it
		// it from us, not from the RTI
		reflectAttributeValues( theObject, theAttributes, tag, null, null );
	}

	public void reflectAttributeValues( int theObject,
	                                    ReflectedAttributes theAttributes,
	                                    byte[] tag,
	                                    LogicalTime theTime,
	                                    EventRetractionHandle retractionHandle )
	{
		String start = "REFLECTION(RO) objectHandle=";
		if( theTime != null )
			start = "REFLECTION("+theTime+") objectHandle=";

		StringBuilder builder = new StringBuilder( start );
		builder.append( theObject );
		builder.append( ", tag=" + EncodingHelpers.decodeString(tag) );
		builder.append( ", attributes=" + theAttributes.size() );
		if( theTime != null )
		{
			builder.append( ", time=" + convertTime(theTime) );
			builder.append( " {TIMESTAMPED}" );
		}
		
		queue( builder.toString() );
	}

	public void receiveInteraction( int interactionClass,
	                                ReceivedInteraction theInteraction,
	                                byte[] tag )
	{
		// just pass it on to the other method for printing purposes
		// passing null as the time will let the other method know it
		// it from us, not from the RTI
		receiveInteraction( interactionClass, theInteraction, tag, null, null );
	}

	public void receiveInteraction( int interactionClass,
	                                ReceivedInteraction theInteraction,
	                                byte[] tag,
	                                LogicalTime theTime,
	                                EventRetractionHandle eventRetractionHandle )
	{
		String start = "INTERACTION(RO) classHandle=";
		if( theTime != null )
			start = "INTERACTION("+theTime+") classHandle=";

		StringBuilder builder = new StringBuilder( start );
		builder.append( interactionClass );
		builder.append( ", tag=" + EncodingHelpers.decodeString(tag) );
		builder.append( ", parameters=" + theInteraction.size() );
		if( theTime != null )
		{
			builder.append( ", time=" + convertTime(theTime) );
			builder.append( " {TIMESTAMPED}" );
		}
		
		queue( builder.toString() );
	}

	public void removeObjectInstance( int theObject, byte[] userSuppliedTag )
	{
		queue( "REMOVE objectHandle=" + theObject );
	}

	public void removeObjectInstance( int theObject,
	                                  byte[] userSuppliedTag,
	                                  LogicalTime theTime,
	                                  EventRetractionHandle retractionHandle )
	{
		queue( "REMOVE("+theTime+") objectHandle=" + theObject +
		       ", time=" + convertTime(theTime) + " {TIMESTAMPED}");
	}


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
