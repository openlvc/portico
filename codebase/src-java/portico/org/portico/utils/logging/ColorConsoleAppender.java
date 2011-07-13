/*
 *   Copyright 2009 The Portico Project
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
package org.portico.utils.logging;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * This class prints colored logging information to either Standard Error (defaut) or Standard Out.
 * 
 * Credits:
 * 	-This class is mainly based off ConsoleAppender (as found in the log4j distribution)
 * 	-The meat of the color injection method comes from a class by Nicolas Martignole
 */
public class ColorConsoleAppender extends ConsoleAppender
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
	/**
	 * We just call up the chain, nothing else.
	 */
	public ColorConsoleAppender()
	{
		super();
	}
	
	/**
	 * This constructor just calls the (Layout,String) constructor passing
	 * ConsoleAppender.SYSTEM_ERR as a default. 
	 *
	 * @param layout The layout to use for this appender
	 */
	public ColorConsoleAppender( Layout layout )
	{
		this( layout, ConsoleAppender.SYSTEM_OUT );
	}
	
	/**
	 * Set the layout and stream. If the stream isn't a valid value then a warning is printed
	 * and the default value of SYSTEM_ERR is used. 
	 *
	 * @param layout The layout to use
	 * @param stream The destination of the log messages
	 */
	public ColorConsoleAppender( Layout layout, String stream )
	{
		super( layout, stream );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Method from Log4j AppenderSkeleton that gets call for any Log4J events.
	 *
	 * Have to include a bunch of code because I can't do what I want and just replace
	 * the message with a color version and pass it to my super class
	 * 
	 * @param event The even to log
	 * @see org.apache.log4j.AppenderSkeleton
	 */
	public void append( LoggingEvent event )
	{
		// make sure we have an open stream etc...
		if( !super.checkEntryConditions() )
			return;
		
		// format and write the main message
		String message = injectColor( this.layout.format(event),
		                              event.getLevel(),
		                              event.getThrowableInformation() );
		this.qw.write( message );

		// flush if we are meant to
		if( this.immediateFlush )
		{
			this.qw.flush();
		}
	}
	
	private String injectColor( Object message, Level level, ThrowableInformation ti )
	{
		StringBuffer buffer = new StringBuffer();
        switch( level.toInt() )
		{
			case Level.ALL_INT:
				buffer.append( "\u001b[1m\u001b[30m" );
				break;
			case Level.FATAL_INT:
				buffer.append( "\u001b[1m\u001b[31m" );
				break;
			case Level.ERROR_INT:
				buffer.append( "\u001b[31m" );
				break;
			case Level.WARN_INT:
				buffer.append( "\u001b[35m" );
				break;
			case Level.INFO_INT:
				buffer.append( "\u001b[34m" );
				break;
			case Level.DEBUG_INT:
				buffer.append( "\u001b[32m" );
				break;// dark green
			case Level.TRACE_INT:
				return message.toString(); // ignore, just use the default
			default:
				buffer.append( "\u001b[32m" );
				break;// dark green
		}
        
        buffer.append( message );
        buffer.append( "\u001b[0m" );
		
        // Print in red the whole exception stack trace
		if( ti != null )
		{
			String s[] = ti.getThrowableStrRep();
			for( int i = 0; i < s.length; i++ )
			{
				buffer.append( "\n\u001b[31m" );
				buffer.append( s[i] );
				buffer.append( "\u001b[0m" );
			}
		}
		
		buffer.append( "\u001b[0m" );
		return buffer.toString();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

