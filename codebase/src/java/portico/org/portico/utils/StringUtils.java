/*
 *   Copyright 2015 The Portico Project
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
package org.portico.utils;

import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.PorticoConstants;
import org.portico2.common.utils.ByteUnit;

public class StringUtils
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern( "eeee d MMMM, u" );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	///////////////////////////////////////////////////////////////////////////////////////
	/// Date/Time Utils   /////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	public static String formatDateTimeString( long millisSinceEpoch )
	{
		Instant instant = Instant.ofEpochMilli( millisSinceEpoch );
		LocalDateTime datetime = LocalDateTime.ofInstant( instant, ZoneId.systemDefault() );
		return datetime.format( DateTimeFormatter.RFC_1123_DATE_TIME );
	}
	
	public static String formatDateTimeString( LocalDate localdate )
	{
		return localdate.atStartOfDay(ZoneId.systemDefault()).format( LONG_DATE );
	}

	public static String formatTimeBetweenNowAndThen( long millis )
	{
		return formatDuration( System.currentTimeMillis()-millis );
	}
	
	public static String formatDuration( long millisDuration )
	{
		if( millisDuration < 1000 )
		{
			return millisDuration+"ms";
		}
		else if( millisDuration < (1000*60) )
		{
			return (millisDuration/1000)+" seconds";
		}
		else if( millisDuration < (1000*60*60) )
		{
			int seconds = (int) (millisDuration / 1000) % 60 ;
			int minutes = (int) ((millisDuration / (1000*60)) % 60);
			return String.format( "%d minutes %d seconds", minutes, seconds );
		}
		else if( millisDuration < (1000*60*60*24) )
		{
			int seconds = (int) (millisDuration / 1000) % 60 ;
			int minutes = (int) ((millisDuration / (1000*60)) % 60);
			int hours   = (int) ((millisDuration / (1000*60*60)) % 24);
			return String.format( "%d hours %d minutes %d seconds", hours, minutes, seconds );
		}
		else
		{
			int minutes = (int) ((millisDuration / (1000*60)) % 60);
			int hours   = (int) ((millisDuration / (1000*60*60)) % 24);
			int days    = (int) ((millisDuration / (1000*60*60*24)));
			return String.format( "%d days %d hours %d minutes", days, hours, minutes );
		}
	}
	

	///////////////////////////////////////////////////////////////////////////////////////
	/// General String Utils   ////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	public static String replaceTokens( String string )
	{
		String userhome = System.getProperty("user.home").replace("\\","\\\\");
		string = string.replace( "${user.home}", userhome );
		return string;
	}

	/**
	 * Return a string that is of the given `width` where the contained `text` is centered with
	 * empty padding left and right.
	 */
	public static String center( String text, int width )
	{
		int textLength = text.length();
		if( textLength > width )
			throw new IllegalArgumentException( "Cannot center text. Given text ["+text+"] wider than given width ["+width+"]" );
		
		if( textLength == width )
			return text;

		int paddingTotal = width - textLength;
		if( paddingTotal == 1 )
			return text+" ";

		int paddingLeft = Math.floorDiv( paddingTotal, 2 );
		int paddingRight = paddingTotal - paddingLeft;
		
		String formatString = "%"+paddingLeft+"s%s%"+paddingRight+"s";
		return String.format(formatString," ",text," ");
	}

	/**
	 * Converts the given string value to a boolean.
	 * <p/>
	 * Valid values are on/off, true/false, yes/no, enabled/disabled
	 */
	public static boolean stringToBoolean( String value )
	{
		value = value.trim();
		if( value.equalsIgnoreCase("true")   ||
			value.equalsIgnoreCase("on")     ||
			value.equalsIgnoreCase("yes")    ||
			value.equalsIgnoreCase("enabled") )
			return true;
		else if( value.equalsIgnoreCase("false") ||
			value.equalsIgnoreCase("off")        ||
			value.equalsIgnoreCase("no")         ||
			value.equalsIgnoreCase("disabled") )
			return false;
		else
			throw new IllegalArgumentException( value+" is not a valid binary constant (on/off, true/false, yes/no, enabled/disabled)" );
	}

	/**
	 * Returns the given string truncated to a maximum of the givne number of characters
	 */
	public static String max( String string, int characters )
	{
		return characters > string.length() ? string : string.substring(0,characters);
	}

	/**
	 * Split the given string using the given regex and then trim each of the tokens.
	 * The standard String.split() method doesn't do the trimming, so we just call it
	 * and then trim each resulting token.
	 * 
	 * @param string The string to split
	 * @param regex The regex to base the split on
	 * @return The tokens, as split by the regex, with each one trimmed for whitespace
	 */
	public static String[] splitAndTrim( String string, String regex )
	{
		String[] tokens = string.split( regex );
		for( int i = 0; i < tokens.length; i++ )
			tokens[i] = tokens[i].trim();
		
		return tokens;
	}

	/**
	 * Takes the targetFederate property from the given message and turns it into a string.
	 * Either "<all>", "<rti>", "[x, y, z]" or "x". No name substitution attempt is made.
	 */
	public static String targetHandleToString( PorticoMessage message )
	{
		return targetHandleToString( message.getTargetFederate() );
	}
	
	/**
	 * Takes the targetFederate property from the given message and turns it into a string.
	 * Either "<all>", "<rti>", "[x, y, z]" or "x". No name substitution attempt is made.
	 */
	public static String targetHandleToString( int federateHandle )
	{
		switch( federateHandle )
		{
			case PorticoConstants.TARGET_ALL_HANDLE:
				return "<all>";
			case PorticoConstants.RTI_HANDLE:
				return "<rti>";
			case PorticoConstants.TARGET_MANY_HANDLE:
				return "<multi>";//+message.getMultipleTargets().toString();
			default:
				return "" + federateHandle;
		}
	}

	/**
	 * Takes the sourceFederate property from the given message and turns it into a string.
	 * Either "<unjoined>", "<rti>", "x". No name substitution attempt is made.
	 */
	public static String sourceHandleToString( PorticoMessage message )
	{
		return sourceHandleToString( message.getSourceFederate() );
	}
	
	/**
	 * Takes the sourceFederate property from the given message and turns it into a string.
	 * Either "<unjoined>", "<rti>", "x". No name substitution attempt is made.
	 */
	public static String sourceHandleToString( int federateHandle )
	{
		switch( federateHandle )
		{
			case PorticoConstants.NULL_HANDLE:
				return "<unjoined>";
			case PorticoConstants.RTI_HANDLE:
				return "<rti>";
			default:
				return ""+federateHandle;
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////
	/// Unit Size Utils   /////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Convert the given size (in bytes) to a more human readable string. Returned values
	 * will be in the form: "16B", "16KB", "16MB", "16GB".
	 */
	public static String getSizeString( long size )
	{
		return getSizeString( size, 2 );
	}
	
	/**
	 * Convert the given size (in bytes) to a more human readable string. Returned values
	 * will be in the form: "16B", "16KB", "16MB", "16GB".
	 */
	public static String getSizeString( long bytes, int decimalPlaces )
	{
		// let's see how much we have so we can figure out the right qualifier
		double totalkb = bytes / 1000;
		double totalmb = totalkb / 1000;
		double totalgb = totalmb / 1000;
		if( totalgb >= 1 )
			return String.format( "%4."+decimalPlaces+"fGB", totalgb );
		else if( totalmb >= 1 )
			return String.format( "%4."+decimalPlaces+"fMB", totalmb );
		else if( totalkb >= 1 )
			return String.format( "%4."+decimalPlaces+"fKB", totalkb );
		else
			return bytes+"b";
	}
	
	//public static String bytesToString( long bytes )
	//{
	//	int unit = 1000;
	//	if( bytes < unit )
	//		return bytes + " B";
	//	int exp = (int)(Math.log( bytes ) / Math.log( unit ));
	//	return String.format( "%.1f %sB", bytes/Math.pow(unit,exp), "kMGTPE".charAt(exp-1) );
	//}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Size Methods   /////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Returns a human readable character string for the given number of bytes. Can specify
	 * whether this should display in SI units (decimal, 1000x) or binary (1024x).
	 * 
	 * Awesome code taken from here excellent SO answer here:
	 * http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
	 * 
	 * <pre>
	 *                               SI     BINARY
	 * 
	 *                    0:        0 B        0 B
	 *                   27:       27 B       27 B
	 *                  999:      999 B      999 B
	 *                 1000:     1.0 kB     1000 B
	 *                 1023:     1.0 kB     1023 B
	 *                 1024:     1.0 kB    1.0 KiB
	 *                 1728:     1.7 kB    1.7 KiB
	 *               110592:   110.6 kB  108.0 KiB
	 *              7077888:     7.1 MB    6.8 MiB
	 *            452984832:   453.0 MB  432.0 MiB
	 *          28991029248:    29.0 GB   27.0 GiB
	 *        1855425871872:     1.9 TB    1.7 TiB
	 *  9223372036854775807:     9.2 EB    8.0 EiB   (Long.MAX_VALUE)
	 * </pre>
	 * 
	 * @param bytes Number of bytes to convert
	 * @param si Whether to use SI format or binary
	 * @return Formatted string
	 */
	public static String humanReadableSize( long bytes, boolean si )
	{
	    int unit = si ? 1000 : 1024;
	    if( bytes < unit ) return bytes + " B";
	    int exp = (int)(Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format( "%.1f %sB", bytes / Math.pow(unit,exp), pre );
	}

	/**
	 * Calls {@link #humanReadableSize(long, boolean)} with <code>true</code> to return in SI units.
	 * @see #humanReadableSize(long, boolean)
	 */
	public static String humanReadableSize( long bytes )
	{
		return humanReadableSize( bytes, true );
	}

	/**
	 * Converts the given human readable string representing the size into its value in bytes.
	 */
	public static long bytesFromString( String humanReadable )
	{
		humanReadable = humanReadable.toLowerCase();
		String sizePortion = null;
		ByteUnit byteUnit = null;

		if( humanReadable.endsWith("mb") )
		{
			sizePortion = humanReadable.substring(0,humanReadable.length()-2).trim();
			byteUnit = ByteUnit.MEGABYTES;
		}
		else if( humanReadable.endsWith("kb") )
		{
			sizePortion = humanReadable.substring(0,humanReadable.length()-2).trim();
			byteUnit = ByteUnit.KILOBYTES;
		}
		else if( humanReadable.endsWith("gb") )
		{
			sizePortion = humanReadable.substring(0,humanReadable.length()-2).trim();
			byteUnit = ByteUnit.GIGABYTES;
		}
		else if( humanReadable.endsWith("tb") )
		{
			sizePortion = humanReadable.substring(0,humanReadable.length()-2).trim();
			byteUnit = ByteUnit.TERABYTES;
		}
		else if( humanReadable.endsWith("b") )
		{
			sizePortion = humanReadable.substring(0,humanReadable.length()-1).trim();
			byteUnit = ByteUnit.BYTES;
		}
		else
		{
			throw new IllegalArgumentException( "Value doesn't have size suffix: "+humanReadable );
		}

		if( sizePortion.contains(".") )
			return byteUnit.toBytes( Double.valueOf(sizePortion) );
		else
			return byteUnit.toBytes( Integer.valueOf(sizePortion) );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/// Char Methods   /////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////	
	public static boolean isAsciiPrintable( char ch )
	{
		return ch >= 32 && ch < 127;
	}

	public static boolean isAsciiPrintable( byte b )
	{
		// http://www.asciitable.com/
		return b >= 0x20 && b < 0x7F;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Wireshark Formatting   /////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////	
	public static String formatAsHex( int value )
	{
		return String.format( "0x%04X", value );
	}

	/**
	 * Convert the given byte[] into a "wireshark formatted" hex/ascii grid. Example output format
	 * for single row:
	 * <p/>
	 * <code>00 11 22 33 44 55 66 77  88 99 AA BB CC DD EE FF  ........ ........</code>
	 */
	public static String formatAsWireshark( byte[] bytes )
	{
		return formatAsWireshark( bytes, 0, bytes.length );
	}

	/**
	 * Convert the given byte[] into a "wireshark formatted" hex/ascii grid. Example output format
	 * for single row:
	 * <p/>
	 * <code>00 11 22 33 44 55 66 77  88 99 AA BB CC DD EE FF  ........ ........</code>
	 */
	public static String formatAsWireshark( byte[] bytes, int offset, int length )
	{
		StringBuilder builder = new StringBuilder();
		// write the first one outside the loop otherwise the modulo check
		// will pass on 0 and we'll have a leading "  "
		builder.append( String.format(" %02X",bytes[offset]) );
		
		boolean newline = true;
		for( int i = 1; i < length; i++ )
		{
			if( i % 8 == 0 )
			{
				newline = !newline;
				if( newline )
					appendRowSummary( builder, bytes, offset+i, 16 );
				else
					builder.append( "  " );
			}
			
			builder.append( String.format(" %02X",bytes[offset+i]) );
		}
		
		appendRowSummary( builder, bytes, offset+length, length % 16 );
		return builder.toString();
	}
	
	/**
	 * Append a row summary that shows the bytes printed as ASCII characters.
	 * Non-printable characters are represented with a "."
	 * 
	 * @param builder The builder to append to
	 * @param bytes The bytes to pull from
	 * @param limit Read the last 16 bytes from this value backwards (non-inclusive)
	 */
	private static void appendRowSummary( StringBuilder builder, byte[] bytes, int limit, int count )
	{
		// write some padding in first
		if( count != 16 )
		{
    		int padding = 15 - count;
    		for( int i = 0; i < padding; i++ )
    		{
    			if( i % 8 == 0 )
    				builder.append( "  " );
    
    			builder.append( "   " );
    		}
    		builder.append( " " );
		}

		// write the summary
		for( int i = (limit-count); i < limit; i++ )
		{
			if( i % 8 == 0 )
				builder.append( " " );
			
			if( StringUtils.isAsciiPrintable(bytes[i]) )
				builder.append( (char)bytes[i] );
			else
				builder.append( "." );
		}
		
		builder.append( "\n" );
	}


	public static String formatAsBinary( byte[] bytes )
	{
		StringBuilder builder = new StringBuilder();
		// write the first one outside the loop otherwise the modulo check
		// will pass on 0 and we'll have a leading "  "
		builder.append( formatAsBinary(bytes[0]) );
		builder.append( " " );
		
		
		for( int i = 1; i < bytes.length; i++ )
		{
			builder.append( formatAsBinary(bytes[i]) );
			builder.append( " " );
			
			if( i % 4 == 0 )
				builder.append('\n');
		}

		return builder.toString();
	}

	public static String formatAsBinary( byte value )
	{
		return String.format( "%8s", Integer.toBinaryString(value & 0xff) ).replace(' ', '0');
	}


	///////////////////////////////////////////////////////////////////////////////////////
	/// Properties Files Management   /////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	public static Properties propertiesFromString( String string )
	{
		Properties properties = new Properties();

		if( string == null )
			return properties;

		if( string.startsWith("{") )
			string = string.substring( 1 );
		
		if( string.endsWith("}") )
			string = string.substring( 0, string.length()-1 );
		
		string = string.replace( ", ", "\n" );
		try
		{
			properties.load( new StringReader(string) );
			return properties;
		}
		catch( IOException ioex )
		{
			throw new IllegalArgumentException( "Error turning string into properties", ioex );
		}
	}

}
