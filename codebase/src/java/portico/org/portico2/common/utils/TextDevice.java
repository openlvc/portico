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
package org.portico2.common.utils;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

public interface TextDevice
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
	public TextDevice printf( String fmt, Object... params );

	public String readLine();
	public String readLine( String fmt, Object... params );

	public char[] readPassword();
	public char[] readPassword( String fmt, Object... params );

	public Reader reader();
	public PrintWriter writer();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	public static TextDevice getDefaultDevice()
	{
		if( System.console() == null )
			return new CharacterTextDevice();
		else
			return new ConsoleTextDevice();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Console Implementation   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public class ConsoleTextDevice implements TextDevice
	{
		private Console console;
		public ConsoleTextDevice()
		{
			this.console = System.console();
		}
		
		public TextDevice printf( String fmt, Object... params )
		{
			this.console.printf( fmt, params );
			return this;
		}

		public String readLine()
		{
			return this.console.readLine();
		}
		
		public String readLine( String fmt, Object... params )
		{
			return this.console.readLine( fmt, params );
		}

		public char[] readPassword()
		{
			return this.console.readPassword();
		}

		public char[] readPassword( String fmt, Object... params )
		{
			return this.console.readPassword( fmt, params );
		}

		public Reader reader()
		{
			return this.console.reader();
		}

		public PrintWriter writer()
		{
			return this.console.writer();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  System.in/out Implementation   ////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public class CharacterTextDevice implements TextDevice
	{
		private BufferedReader reader;
		private PrintWriter writer;
		public CharacterTextDevice()
		{
			this.reader = new BufferedReader( new InputStreamReader(System.in) );
			this.writer = new PrintWriter( System.out, true );
		}
		
		public TextDevice printf( String fmt, Object... params )
		{
			this.writer.printf( fmt, params );
			return this;
		}

		public String readLine()
		{
			try
			{
				return this.reader.readLine();
			}
			catch( IOException ioex )
			{
				throw new RuntimeException( ioex );
			}
		}
		
		public String readLine( String fmt, Object... params )
		{
			this.writer.printf( fmt, params );
			return readLine();
		}

		public char[] readPassword()
		{
			// FIXME - Nope.
			return readLine().toCharArray();
		}

		public char[] readPassword( String fmt, Object... params )
		{
			printf( fmt, params );
			return readPassword();
		}

		public Reader reader()
		{
			return this.reader;
		}

		public PrintWriter writer()
		{
			return this.writer;
		}
	}


}
