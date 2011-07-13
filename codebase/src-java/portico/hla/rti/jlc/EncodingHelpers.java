/*
 *   This file is a direct copy from the SISO (http://www.sisostds.org) DLC standard for
 *   HLA 1.3 (SISO-STD-004-2004).
 * 
 *   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *   OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED.  IN NO EVENT SHALL THE DEVELOPERS OF THIS PROJECT OR
 *   ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *   USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *   SUCH DAMAGE.
 *
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 */
package hla.rti.jlc;

public class EncodingHelpers
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

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Encodes a boolean value as a big-endian 32-bit integer in a 4-byte buffer and returns the
	 * buffer.
	 * 
	 * @param value Value to encode
	 * 
	 * @return Buffer with encoded value
	 */
	public static byte[] encodeBoolean( boolean value )
	{
		byte[] buffer = new byte[4];
		encodeBoolean( value, buffer, 0 );
		return buffer;
	}

	/**
	 * Encodes a boolean value as a big-endian 32-bit integer into the specified buffer at the
	 * specified offset.
	 * 
	 * @param value Value to encode
	 * @param buffer Buffer to store value in
	 * @param offset Offset in buffer
	 */
	public static void encodeBoolean( boolean value, byte[] buffer, int offset )
	{
		encodeInt( value ? 1 : 0, buffer, offset );
	}

	/**
	 * Decodes a big-endian 32-bit integer in a buffer into a boolean.
	 * 
	 * @param buffer Buffer containing encoded value
	 * 
	 * @return Decoded value
	 */
	public static boolean decodeBoolean( byte[] buffer )
	{
		return decodeBoolean( buffer, 0 );
	}

	/**
	 * Decodes a big-endian 32-bit integer in a 4-byte buffer at the specified offset int a
	 * boolean value.
	 * 
	 * @param buffer Buffer containing encoded value
	 * @param offset Offset in buffer
	 * 
	 * @return Decoded value
	 */
	public static boolean decodeBoolean( byte[] buffer, int offset )
	{
		return decodeInt( buffer, offset ) != 0;
	}

	/**
	 * Encodes a byte in a buffer and returns the buffer.
	 * 
	 * @param value Value to encode
	 * 
	 * @return Buffer with encoded value
	 */
	public static byte[] encodeByte( int value )
	{
		byte[] buffer = new byte[4];
		encodeByte( value, buffer, 0 );
		return buffer;
	}

	/**
	 * Encodes a byte into a buffer at the specified offset.
	 * 
	 * @param value Value to encode
	 * @param buffer Buffer to store value in
	 * @param offset Offset in buffer
	 */
	public static void encodeByte( int value, byte[] buffer, int offset )
	{
		buffer[offset] = (byte)value;
	}

	/**
	 * Decodes a byte from a buffer.
	 * 
	 * @param buffer Buffer containing encoded value
	 * 
	 * @return Decoded value.
	 */
	public static byte decodeByte( byte[] buffer )
	{
		return decodeByte( buffer, 0 );
	}

	/**
	 * Decodes a byte from a buffer at the specified offset.
	 * 
	 * @param buffer Buffer containing encoded value
	 * @param offset Offset in buffer
	 * @return Decoded value.
	 */
	public static byte decodeByte( byte[] buffer, int offset )
	{
		return (byte)(buffer[offset] & 0xff);
	}

	/**
	 * Encodes a char value as a big-endian 16-bit integer into a 2-byte buffer and returns the
	 * buffer.
	 * 
	 * @param value Value to encode
	 * 
	 * @return Buffer with encoded value
	 */
	public static byte[] encodeChar( char value )
	{
		byte[] buffer = new byte[2];
		encodeChar( value, buffer, 0 );
		return buffer;
	}

	/**
	 * Encodes a char value as a big-endian 16-bit integer into a buffer at the specified offset.
	 * 
	 * @param value Value to encode
	 * @param buffer Buffer to encode value in
	 * @param offset Offset in buffer
	 */
	public static void encodeChar( char value, byte[] buffer, int offset )
	{
		int pos = offset;
		buffer[pos++] = (byte)((value >>> 8) & 0xFF);
		buffer[pos++] = (byte)((value >>> 0) & 0xFF);
	}

	/**
	 * Decodes a big-endian 16-bit integer in a buffer into a char value.
	 * 
	 * @param buffer Buffer containing encoded value
	 * 
	 * @return Decoded value.
	 */
	public static char decodeChar( byte[] buffer )
	{
		return decodeChar( buffer, 0 );
	}

	/**
	 * Decodes a big-endian 16-bit integer in a buffer at the specified offset into a char value.
	 * 
	 * @param buffer Buffer containing encoded value
	 * @param offset Offset in buffer
	 * 
	 * @return Decoded value.
	 */
	public static char decodeChar( byte[] buffer, int offset )
	{
		int value = 0;
		int pos = offset;
		value += (buffer[pos++] & 0xff) << 8;
		value += (buffer[pos++] & 0xff) << 0;
		return (char)value;
	}

	/**
	 * Converts a double value to a long and stores that long value as a big-endian 64-bit integer
	 * in an 8-byte buffer and returns that buffer.
	 * 
	 * @param value Value to encode
	 * 
	 * @return Buffer with encoded value
	 */
	public static byte[] encodeDouble( double value )
	{
		byte[] buffer = new byte[8];
		encodeDouble( value, buffer, 0 );
		return buffer;
	}

	/**
	 * Converts a double value to a long and stores that long value as a big-endian 64-bit integer
	 * into buffer at specified offset.
	 * 
	 * @param value Value to encode
	 * @param buffer Buffer to store value in
	 * @param offset Offset in buffer
	 */
	public static void encodeDouble( double value, byte[] buffer, int offset )
	{
		encodeLong( Double.doubleToLongBits( value ), buffer, offset );
	}

	/**
	 * Decodes a big-endian 64-bit integer stored in buffer into a long value and converts that to
	 * a double value, which is returned.
	 * 
	 * @param buffer Buffer containing encoded value
	 * 
	 * @return Decoded value
	 */
	public static double decodeDouble( byte[] buffer )
	{
		return decodeDouble( buffer, 0 );
	}

	/**
	 * Decodes a big-endian 64-bit integer stored in buffer at specified offset into a long value
	 * and converts that to a double value, which is returned.
	 * 
	 * @param buffer Buffer containing encoded value
	 * @param offset Offset in buffer
	 * 
	 * @return Decoded value
	 */
	public static double decodeDouble( byte[] buffer, int offset )
	{
		return Double.longBitsToDouble( decodeLong( buffer, offset ) );
	}

	/**
	 * Converts a float value to an int and stores that int value as a big-endian 32-bit integer
	 * in a 4-byte buffer and returns that buffer.
	 * 
	 * @param value Value to encode
	 * 
	 * @return Buffer with encoded value
	 */
	public static byte[] encodeFloat( float value )
	{
		byte[] buffer = new byte[4];
		encodeFloat( value, buffer, 0 );
		return buffer;
	}

	/**
	 * Converts a float value to an int and stores that int value as a big-endian 32-bit integer
	 * in the specified buffer at a specified offset.
	 * 
	 * @param value Value to encode
	 * @param buffer Buffer to store value in
	 * @param offset Offset in buffer
	 */
	public static void encodeFloat( float value, byte[] buffer, int offset )
	{
		encodeInt( Float.floatToIntBits( value ), buffer, offset );
	}

	/**
	 * Decodes a big-endian 32-bit integer stored in buffer into an int value and converts that to
	 * a float value, which is returned.
	 * 
	 * @param buffer Buffer containing encoded value
	 * 
	 * @return Decoded value
	 */
	public static float decodeFloat( byte[] buffer )
	{
		return decodeFloat( buffer, 0 );
	}

	/**
	 * Decodes a big-endian 32-bit integer stored in buffer at specified offset into an int value
	 * and converts that to a float value, which is returned.
	 * 
	 * @param buffer Buffer containing encoded value
	 * @param offset Offset in buffer
	 * 
	 * @return Decoded value
	 */
	public static float decodeFloat( byte[] buffer, int offset )
	{
		return Float.intBitsToFloat( decodeInt( buffer, offset ) );
	}

	/**
	 * Encodes an int as a big-endian 32-bit integer into a 4-byte buffer and returns the buffer.
	 * 
	 * @param value Value to encode
	 * 
	 * @return Buffer with encoded value
	 */
	public static byte[] encodeInt( int value )
	{
		byte[] buffer = new byte[4];
		encodeInt( value, buffer, 0 );
		return buffer;
	}

	/**
	 * Encodes an int as a big-endian 32-bit integer into buffer at the specified offset.
	 * 
	 * @param value Value to encode
	 * @param buffer Buffer to store value in
	 * @param offset Offset in buffer
	 */
	public static void encodeInt( int value, byte[] buffer, int offset )
	{
		int pos = offset;
		buffer[pos++] = (byte)((value >>> 24) & 0xFF);
		buffer[pos++] = (byte)((value >>> 16) & 0xFF);
		buffer[pos++] = (byte)((value >>> 8) & 0xFF);
		buffer[pos++] = (byte)((value >>> 0) & 0xFF);
	}

	/**
	 * Decodes a big-endian 32-bit integer stored in buffer into an int value.
	 * 
	 * @param buffer Buffer containing encoded value
	 * 
	 * @return Decoded value
	 */
	public static int decodeInt( byte[] buffer )
	{
		return decodeInt( buffer, 0 );
	}

	/**
	 * Decodes a big-endian 32-bit integer stored in buffer at specified offset into an int value.
	 * 
	 * @param buffer Buffer containing encoded value
	 * @param offset Offset in buffer
	 * 
	 * @return Decoded value
	 */
	public static int decodeInt( byte[] buffer, int offset )
	{
		int value = 0;
		int pos = offset;
		value += (buffer[pos++] & 0xff) << 24;
		value += (buffer[pos++] & 0xff) << 16;
		value += (buffer[pos++] & 0xff) << 8;
		value += (buffer[pos++] & 0xff) << 0;
		return value;
	}

	/**
	 * Encodes a long value as a big-endian 64-bit integer into a 8-byte buffer and returns the
	 * buffer.
	 * 
	 * @param value Value to encode
	 * 
	 * @return Buffer with encoded value
	 */
	public static byte[] encodeLong( long value )
	{
		byte[] buffer = new byte[8];
		encodeLong( value, buffer, 0 );
		return buffer;
	}

	/**
	 * Encodes a long value as a big-endian 64-bit integer into a buffer at specified offset.
	 * 
	 * @param value Value to encode
	 * @param buffer Buffer to store value in
	 * @param offset Offset in buffer
	 */
	public static void encodeLong( long value, byte[] buffer, int offset )
	{
		int pos = offset;
		buffer[pos++] = (byte)((value >>> 56) & 0xFF);
		buffer[pos++] = (byte)((value >>> 48) & 0xFF);
		buffer[pos++] = (byte)((value >>> 40) & 0xFF);
		buffer[pos++] = (byte)((value >>> 32) & 0xFF);
		buffer[pos++] = (byte)((value >>> 24) & 0xFF);
		buffer[pos++] = (byte)((value >>> 16) & 0xFF);
		buffer[pos++] = (byte)((value >>> 8) & 0xFF);
		buffer[pos++] = (byte)((value >>> 0) & 0xFF);
	}

	/**
	 * Decodes a big-endian 64-bit integer stored in buffer into a long value.
	 * 
	 * @param buffer Buffer containing encoded value
	 * 
	 * @return Decoded value
	 */
	public static long decodeLong( byte[] buffer )
	{
		return decodeLong( buffer, 0 );
	}

	/**
	 * Decodes a big-endian 64-bit integer stored in buffer at specified offset into a long value.
	 * 
	 * @param buffer Buffer containing encoded value
	 * @param offset Offset in buffer
	 * 
	 * @return Decoded value
	 */
	public static long decodeLong( byte[] buffer, int offset )
	{
		long value = 0;
		int pos = offset;
		value += ((long)buffer[pos++] & 0xff) << 56;
		value += ((long)buffer[pos++] & 0xff) << 48;
		value += ((long)buffer[pos++] & 0xff) << 40;
		value += ((long)buffer[pos++] & 0xff) << 32;
		value += ((long)buffer[pos++] & 0xff) << 24;
		value += ((long)buffer[pos++] & 0xff) << 16;
		value += ((long)buffer[pos++] & 0xff) << 8;
		value += ((long)buffer[pos++] & 0xff) << 0;
		return value;
	}

	/**
	 * Encodes a short value as a big-endian 16-bit integer into a 2-byte buffer and returns that
	 * buffer.
	 * 
	 * @param value Value to encode
	 * 
	 * @return Buffer with encoded value
	 */
	public static byte[] encodeShort( short value )
	{
		byte[] buffer = new byte[2];
		encodeShort( value, buffer, 0 );
		return buffer;
	}

	/**
	 * Encodes a short value as a big-endian 16-bit integer into a buffer at the specified offset.
	 * 
	 * @param value Value to encode
	 * @param buffer Buffer to store value in
	 * @param offset Offset in buffer
	 */
	public static void encodeShort( short value, byte[] buffer, int offset )
	{
		int pos = offset;
		buffer[pos++] = (byte)((value >>> 8) & 0xFF);
		buffer[pos++] = (byte)((value >>> 0) & 0xFF);
	}

	/**
	 * Decodes a big-endian 16-bit integer stored in buffer into a short value.
	 * 
	 * @param buffer Buffer containing encoded value
	 * 
	 * @return Decoded value
	 */
	public static short decodeShort( byte[] buffer )
	{
		return decodeShort( buffer, 0 );
	}

	/**
	 * Decodes a big-endian 16-bit integer stored in buffer at specified offset into a short
	 * value.
	 * 
	 * @param buffer Buffer containing encoded value
	 * @param offset Offset in buffer
	 * 
	 * @return Decoded value
	 */
	public static short decodeShort( byte[] buffer, int offset )
	{
		short value = 0;
		int pos = offset;
		value += (buffer[pos++] & 0xff) << 8;
		value += (buffer[pos++] & 0xff) << 0;
		return value;
	}

	/**
	 * Appends a null character to the string and returns a buffer containing the encoded string.
	 * The purpose of the null character is compatibility with federates written in C++.
	 * 
	 * @param str String to encode
	 * 
	 * @return Buffer with encoded string
	 */
	public static byte[] encodeString( String str )
	{
		return (str + "\0").getBytes();
	}

	/**
	 * Decodes a string from a buffer and strips the final null character.
	 * 
	 * @param buffer Buffer with encoded string
	 * 
	 * @return Decoded string
	 */
	public static String decodeString( byte[] buffer )
	{
		return new String( buffer, 0, buffer.length - 1 );
	}
}
