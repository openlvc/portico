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
package org.portico.impl.hla1516e.types.time;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.InvalidLookahead;

import org.portico.lrc.PorticoConstants;

/**
 * The boundary between the logical time implementation a federation was created with and the
 * double Portico uses internally for every time it stores, sends and compares.
 * <p/>
 * A federation is created with one of the two implementations the standard defines,
 * HLAfloat64Time or HLAinteger64Time, and every federate in it has to speak that one. This class
 * knows which type is acceptable for a given federation, converts it to and from the internal
 * double, and refuses the ones that do not belong to the federation.
 * <p/>
 * The internal double holds a 64 bit integer time exactly as long as its magnitude stays under
 * 2^53. Simulation timestamps are nowhere near that, but the limit is real, so a value that would
 * lose its low bits is refused rather than silently rounded.
 */
public class TimeUtils
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String FLOAT_TIME = PorticoConstants.DEFAULT_TIME_IMPLEMENTATION;
	public static final String INTEGER_TIME = PorticoConstants.INTEGER_TIME_IMPLEMENTATION;

	/** The implementation a federation gets when it is created without asking for one. */
	public static final String DEFAULT_TIME = FLOAT_TIME;

	/** 2^53, above which a double no longer holds every integer. */
	private static final double MAX_EXACT_INTEGER = 9007199254740992.0;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private TimeUtils()
	{
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Is this the name of a logical time implementation Portico can provide?
	 */
	public static boolean isSupported( String implementationName )
	{
		return FLOAT_TIME.equals( implementationName ) || INTEGER_TIME.equals( implementationName );
	}

	/**
	 * Returns the implementation name to use for a federation created with the given name. A null
	 * or blank name means the creator did not ask for one, and gets the default.
	 *
	 * @throws CouldNotCreateLogicalTimeFactory if the name is not one Portico provides
	 */
	public static String resolve( String implementationName )
		throws CouldNotCreateLogicalTimeFactory
	{
		if( implementationName == null || implementationName.trim().isEmpty() )
			return DEFAULT_TIME;

		String trimmed = implementationName.trim();
		if( isSupported(trimmed) == false )
		{
			throw new CouldNotCreateLogicalTimeFactory( "Invalid time implementation: Must be "+
			                                            "\""+FLOAT_TIME+"\" or \""+
			                                            INTEGER_TIME+"\"" );
		}

		return trimmed;
	}

	/**
	 * The same as {@link #resolve(String)}, except that an unknown name falls back to the default
	 * rather than throwing. Used on the receiving side, where the name came off the wire from a
	 * peer that may be running a Portico predating federation time implementations.
	 */
	public static String resolveOrDefault( String implementationName )
	{
		return PorticoConstants.resolveTimeImplementation( implementationName );
	}

	/**
	 * Returns true if a federation with the given implementation works with an integer time.
	 */
	public static boolean isIntegerTime( String implementationName )
	{
		return INTEGER_TIME.equals( implementationName );
	}

	/**
	 * Returns a factory for the named implementation. An unknown name yields the default factory,
	 * so that a caller which has not yet joined a federation still gets something usable.
	 */
	public static LogicalTimeFactory createFactory( String implementationName )
	{
		if( isIntegerTime(resolveOrDefault(implementationName)) )
			return new LongTimeFactory();
		else
			return new DoubleTimeFactory();
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Federate to Portico ////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Converts a logical time handed to us by the federate into the internal double, checking that
	 * it is the implementation the federation was created with.
	 */
	public static double fromTime( LogicalTime time, String implementationName )
		throws InvalidLogicalTime
	{
		String expected = resolveOrDefault( implementationName );

		if( time == null )
			throw new InvalidLogicalTime( "Expecting "+expected+", found: null" );

		if( isIntegerTime(expected) )
		{
			if( time instanceof LongTime )
			{
				long value = ((LongTime)time).getValue();
				checkExact( value );
				return (double)value;
			}
		}
		else
		{
			if( time instanceof DoubleTime )
				return ((DoubleTime)time).getValue();
		}

		throw new InvalidLogicalTime( "This federation works with "+expected+", found: "+
		                              time.getClass().getName()+". Every federate in a federation "+
		                              "has to use the logical time implementation the federation "+
		                              "was created with." );
	}

	/**
	 * Converts a logical time interval handed to us by the federate into the internal double,
	 * checking that it is the implementation the federation was created with.
	 */
	public static double fromInterval( LogicalTimeInterval interval, String implementationName )
		throws InvalidLogicalTime
	{
		String expected = resolveOrDefault( implementationName );

		if( interval == null )
			throw new InvalidLogicalTime( "Expecting an interval of "+expected+", found: null" );

		if( isIntegerTime(expected) )
		{
			if( interval instanceof LongTimeInterval )
			{
				long value = ((LongTimeInterval)interval).getValue();
				checkExact( value );
				return (double)value;
			}
		}
		else
		{
			if( interval instanceof DoubleTimeInterval )
				return ((DoubleTimeInterval)interval).getValue();
		}

		throw new InvalidLogicalTime( "This federation works with "+expected+", found an interval "+
		                              "of "+interval.getClass().getName() );
	}

	/**
	 * The same as {@link #fromInterval(LogicalTimeInterval,String)}, except that it reports a bad
	 * value as an {@link InvalidLookahead}, which is what the lookahead calls have to throw.
	 */
	public static double fromLookahead( LogicalTimeInterval interval, String implementationName )
		throws InvalidLookahead
	{
		try
		{
			return fromInterval( interval, implementationName );
		}
		catch( InvalidLogicalTime ilt )
		{
			throw new InvalidLookahead( ilt.getMessage() );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Portico to Federate ////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Turns an internal double into the logical time implementation the federation works with,
	 * ready to be handed to the federate ambassador.
	 */
	public static LogicalTime makeTime( double value, String implementationName )
	{
		if( isIntegerTime(resolveOrDefault(implementationName)) )
			return new LongTime( (long)value );
		else
			return new DoubleTime( value );
	}

	/**
	 * Turns an internal double into an interval of the implementation the federation works with.
	 */
	public static LogicalTimeInterval makeInterval( double value, String implementationName )
	{
		if( isIntegerTime(resolveOrDefault(implementationName)) )
			return new LongTimeInterval( (long)value );
		else
			return new DoubleTimeInterval( value );
	}

	/**
	 * Checks that an integer time survives the trip through the internal double intact.
	 *
	 * @throws InvalidLogicalTime if the value is too large to be held exactly
	 */
	public static void checkExact( long value ) throws InvalidLogicalTime
	{
		if( Math.abs((double)value) >= MAX_EXACT_INTEGER )
		{
			throw new InvalidLogicalTime( "Logical time "+value+" is too large: Portico holds "+
			                              "logical times in a double, which is exact only below "+
			                              "2^53 ("+(long)MAX_EXACT_INTEGER+")" );
		}
	}
}
