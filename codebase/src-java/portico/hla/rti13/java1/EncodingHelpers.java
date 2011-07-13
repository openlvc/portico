package hla.rti13.java1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EncodingHelpers
{
	public static byte[] encodeBoolean( boolean value ) throws RTIinternalError
	{
		try
		{
			ByteArrayOutputStream obuffer = new ByteArrayOutputStream();
			DataOutputStream ostream = new DataOutputStream( obuffer );
			ostream.writeBoolean( value );
			ostream.flush();

			return obuffer.toByteArray();
		}
		catch( IOException e )
		{
			throw new RTIinternalError( "error encoding boolean: " + e.getMessage() );
		}
	}

	public static boolean decodeBoolean( byte[] bytes ) throws FederateInternalError
	{
		try
		{
			ByteArrayInputStream ibuffer = new ByteArrayInputStream( bytes );
			DataInputStream istream = new DataInputStream( ibuffer );

			return istream.readBoolean();
		}
		catch( IOException e )
		{
			throw new FederateInternalError( "error decoding boolean: " + e.getMessage() );
		}
	}

	public static byte[] encodeByte( byte value ) throws RTIinternalError
	{
		try
		{
			ByteArrayOutputStream obuffer = new ByteArrayOutputStream();
			DataOutputStream ostream = new DataOutputStream( obuffer );
			ostream.writeByte( value );
			ostream.flush();

			return obuffer.toByteArray();
		}
		catch( IOException e )
		{
			throw new RTIinternalError( "error encoding byte: " + e.getMessage() );
		}
	}

	public static byte decodeByte( byte[] bytes ) throws FederateInternalError
	{
		try
		{
			ByteArrayInputStream ibuffer = new ByteArrayInputStream( bytes );
			DataInputStream istream = new DataInputStream( ibuffer );

			return istream.readByte();
		}
		catch( IOException e )
		{
			throw new FederateInternalError( "error decoding byte: " + e.getMessage() );
		}
	}

	public static byte[] encodeChar( char value ) throws RTIinternalError
	{
		try
		{
			ByteArrayOutputStream obuffer = new ByteArrayOutputStream();
			DataOutputStream ostream = new DataOutputStream( obuffer );
			ostream.writeChar( value );
			ostream.flush();

			return obuffer.toByteArray();
		}
		catch( IOException e )
		{
			throw new RTIinternalError( "error encoding char: " + e.getMessage() );
		}
	}

	public static char decodeChar( byte[] bytes ) throws FederateInternalError
	{
		try
		{
			ByteArrayInputStream ibuffer = new ByteArrayInputStream( bytes );
			DataInputStream istream = new DataInputStream( ibuffer );

			return istream.readChar();
		}
		catch( IOException e )
		{
			throw new FederateInternalError( "error decoding char: " + e.getMessage() );
		}
	}

	public static byte[] encodeDouble( double value ) throws RTIinternalError
	{
		try
		{
			ByteArrayOutputStream obuffer = new ByteArrayOutputStream();
			DataOutputStream ostream = new DataOutputStream( obuffer );
			ostream.writeDouble( value );
			ostream.flush();

			return obuffer.toByteArray();
		}
		catch( IOException e )
		{
			throw new RTIinternalError( "error encoding double: " + e.getMessage() );
		}
	}

	public static double decodeDouble( byte[] bytes ) throws FederateInternalError
	{
		try
		{
			ByteArrayInputStream ibuffer = new ByteArrayInputStream( bytes );
			DataInputStream istream = new DataInputStream( ibuffer );

			return istream.readDouble();
		}
		catch( IOException e )
		{
			throw new FederateInternalError( "error decoding double: " + e.getMessage() );
		}
	}

	public static byte[] encodeFloat( float value ) throws RTIinternalError
	{
		try
		{
			ByteArrayOutputStream obuffer = new ByteArrayOutputStream();
			DataOutputStream ostream = new DataOutputStream( obuffer );
			ostream.writeFloat( value );
			ostream.flush();

			return obuffer.toByteArray();
		}
		catch( IOException e )
		{
			throw new RTIinternalError( "error encoding float: " + e.getMessage() );
		}
	}

	public static float decodeFloat( byte[] bytes ) throws FederateInternalError
	{
		try
		{
			ByteArrayInputStream ibuffer = new ByteArrayInputStream( bytes );
			DataInputStream istream = new DataInputStream( ibuffer );

			return istream.readFloat();
		}
		catch( IOException e )
		{
			throw new FederateInternalError( "error decoding float: " + e.getMessage() );
		}
	}

	public static byte[] encodeInt( int value ) throws RTIinternalError
	{
		try
		{
			ByteArrayOutputStream obuffer = new ByteArrayOutputStream();
			DataOutputStream ostream = new DataOutputStream( obuffer );
			ostream.writeInt( value );
			ostream.flush();

			return obuffer.toByteArray();
		}
		catch( IOException e )
		{
			throw new RTIinternalError( "error encoding int: " + e.getMessage() );
		}
	}

	public static int decodeInt( byte[] bytes ) throws FederateInternalError
	{
		try
		{
			ByteArrayInputStream ibuffer = new ByteArrayInputStream( bytes );
			DataInputStream istream = new DataInputStream( ibuffer );

			return istream.readInt();
		}
		catch( IOException e )
		{
			throw new FederateInternalError( "error decoding int: " + e.getMessage() );
		}
	}

	public static byte[] encodeLong( long value ) throws RTIinternalError
	{
		try
		{
			ByteArrayOutputStream obuffer = new ByteArrayOutputStream();
			DataOutputStream ostream = new DataOutputStream( obuffer );
			ostream.writeLong( value );
			ostream.flush();

			return obuffer.toByteArray();
		}
		catch( IOException e )
		{
			throw new RTIinternalError( "error encoding long: " + e.getMessage() );
		}
	}

	public static long decodeLong( byte[] bytes ) throws FederateInternalError
	{
		try
		{
			ByteArrayInputStream ibuffer = new ByteArrayInputStream( bytes );
			DataInputStream istream = new DataInputStream( ibuffer );

			return istream.readLong();
		}
		catch( IOException e )
		{
			throw new FederateInternalError( "error decoding long: " + e.getMessage() );
		}
	}

	public static byte[] encodeShort( short value ) throws RTIinternalError
	{
		try
		{
			ByteArrayOutputStream obuffer = new ByteArrayOutputStream();
			DataOutputStream ostream = new DataOutputStream( obuffer );
			ostream.writeShort( value );
			ostream.flush();

			return obuffer.toByteArray();
		}
		catch( IOException e )
		{
			throw new RTIinternalError( "error encoding short: " + e.getMessage() );
		}
	}

	public static short decodeShort( byte[] bytes ) throws FederateInternalError
	{
		try
		{
			ByteArrayInputStream ibuffer = new ByteArrayInputStream( bytes );
			DataInputStream istream = new DataInputStream( ibuffer );

			return istream.readShort();
		}
		catch( IOException e )
		{
			throw new FederateInternalError( "error decoding short: " + e.getMessage() );
		}
	}

	public static byte[] encodeString( String theString ) throws RTIinternalError
	{
		// append the null character
		return (theString + "\0").getBytes();
	}

	public static String decodeString( byte[] bytes ) throws FederateInternalError
	{
		// lop off the null character
		return new String( bytes, 0, bytes.length - 1 );
	}
}
