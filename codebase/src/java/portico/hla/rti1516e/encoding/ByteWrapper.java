/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e.encoding;

/**
 * Utility class for managing data in byte arrays.
 */
public class ByteWrapper {
   private static final byte[] ZERO_LENGTH_BUFFER = new byte[0];

   /** Offset for the start position in the buffer. */
   private int _offset;

   /** The current postion (or index) in the buffer. */
   private int _pos;

   /** The length of the buffer. */
   private int _limit;

   /** The backing byte array. */
   private byte[] _buffer;

   /**
    * Construct a ByteWrapper backed by a zero-length byte array.
    */
   public ByteWrapper() {
      this(ZERO_LENGTH_BUFFER);
   }

   /**
    * Construct a ByteWrapper backed by a byte array with the specified <code>length</code>.
    *
    * @param length length of the backing byte array
    */
   public ByteWrapper(int length) {
      this(new byte[length]);
   }

   /**
    * Constructs a <code>ByteWrapper</code> backed by the specified byte array. (Changes to
    * the ByteWrapper will write through to the specified byte array.)
    *
    * @param buffer backing byte array
    */
   public ByteWrapper(byte[] buffer) {
      this(buffer, 0, buffer.length);
   }

   /**
    * Constructs a <code>ByteWrapper</code> backed by the specified byte array. (Changes to
    * the ByteWrapper will write through to the specified byte array.)
    * The <code>offset</code> will be at the start position. Limit will be at <code>buffer.length</code>.
    *
    * @param buffer backing byte array
    * @param offset start position offset
    */
   public ByteWrapper(byte[] buffer, int offset) {
      this(buffer, offset, buffer.length - offset);
   }

   /**
    * Constructs a <code>ByteWrapper</code> backed by the specified byte array. (Changes to
    * the ByteWrapper will write through to the specified byte array.)
    *
    * @param buffer backing byte array
    * @param offset start position offset
    * @param length length of the segment to use
    */
   public ByteWrapper(byte[] buffer, int offset, int length) {
      setBuffer(buffer, offset, length);
   }

   /**
    * Changes the backing store used by this ByteWrapper. Changes to the
    * ByteWrapper will write through to the specified byte array.
    *
    * @param buffer backing byte array
    * @param offset start position offset
    * @param length length of the segment to use
    */
   public void reassign(byte[] buffer, int offset, int length)
   {
      setBuffer(buffer, offset, length);
   }

   private void setBuffer(byte[] buffer, int offset, int length)
   {
      checkBounds(buffer, offset, length);
      _buffer = buffer;
      _offset = offset;
      _limit = _offset + length;
      _pos = _offset;
   }

   private void checkBounds(byte[] buffer, int offset, int length)
   {
      if (offset < 0) {
         throw new ArrayIndexOutOfBoundsException("Negative offset: " + offset);
      }
      if (length < 0 || offset + length > buffer.length) {
         throw new ArrayIndexOutOfBoundsException("Offset + length (" + offset + " + " + length + ") past end of buffer: " + buffer.length);
      }
   }

   /**
    * Resets current position to the start of the ByteWrapper.
    */
   public void reset()
   {
      _pos = _offset;
   }

   /**
    * Verify that <code>length</code> bytes can be read.
    *
    * @param length number of byte to verify
    *
    * @throws ArrayIndexOutOfBoundsException if <code>length</code> bytes can not be read
    */
   public void verify(int length) {
      if (length < 0) {
         throw new ArrayIndexOutOfBoundsException(length);
      }
      if (_pos + length > _limit) {
         throw new ArrayIndexOutOfBoundsException(_pos + length);
      }
   }

   /**
    * Reads the next four byte of the ByteWrapper as a hi-endian 32-bit integer.
    * The current position is increased by 4.
    *
    * @return decoded value
    *
    * @throws ArrayIndexOutOfBoundsException if the bytes can not be read
    *
    * @noinspection PointlessBitwiseExpression
    */
   public final int getInt() {
      verify(4);
      int pos = _pos;
      byte[] buffer = _buffer;
      int value =
         (((int) buffer[pos] & 0xFF) << 24) +
         (((int) buffer[pos + 1] & 0xFF) << 16) +
         (((int) buffer[pos + 2] & 0xFF) << 8) +
         ((int) buffer[pos + 3] & 0xFF);
      _pos += 4;

      return value;
   }

   /**
    * Reads the next byte of the ByteWrapper. The current position is increased by 1.
    *
    * @return decoded value
    *
    * @throws ArrayIndexOutOfBoundsException if the bytes can not be read
    */
   public final int get() {
      verify(1);
      return (int) _buffer[_pos++] & 0xFF;
   }

   /**
    * Reads <code>dest.length</code> bytes from the ByteWrapper into <code>dest</code>. The
    * current position is increased by <code>dest.length</code>.
    *
    * @param dest destination for the read bytes
    *
    * @throws ArrayIndexOutOfBoundsException if the bytes can not be read
    */
   public final void get(byte[] dest) {
      verify(dest.length);
      System.arraycopy(_buffer, _pos, dest, 0, dest.length);
      _pos += dest.length;
   }

   /**
    * Writes <code>value</code> to the ByteWrapper as a hi-endian 32-bit integer. The
    * current position is increased by 4.
    *
    * @param value value to write
    *
    * @throws ArrayIndexOutOfBoundsException if the bytes can not be written
    *
    * @noinspection PointlessBitwiseExpression
    */
   public void putInt(int value) {
      verify(4);
      put((value >>> 24) & 0xFF);
      put((value >>> 16) & 0xFF);
      put((value >>> 8) & 0xFF);
      put((value >>> 0) & 0xFF);
   }

   /**
    * Writes <code>byte</code> to the ByteWrapper and advances the current position
    * by 1.
    *
    * @param b byte to write
    *
    * @throws ArrayIndexOutOfBoundsException if the bytes can not be written
    */
   public void put(int b) {
      verify(1);
      _buffer[_pos++] = (byte) b;
   }

   /**
    * Writes a byte array to the ByteWrapper and advances the current
    * posisiton by the size of the byte array.
    *
    * @param src byte array to write
    *
    * @throws ArrayIndexOutOfBoundsException if the bytes can not be written
    */
   public void put(byte[] src) {
      verify(src.length);
      System.arraycopy(src, 0, _buffer, _pos, src.length);
      _pos += src.length;
   }

   /**
    * Writes a subset of a byte array to the ByteWrapper and advances the current
    * posisiton by the size of the subset.
    *
    * @param src byte array to write
    * @param offset offset of subset to write
    * @param count size of offset to write
    *
    * @throws ArrayIndexOutOfBoundsException if the bytes can not be written
    */
   public void put(byte[] src, int offset, int count)
   {
      verify(count);
      System.arraycopy(src, offset, _buffer, _pos, count);
      _pos += count;
   }

   /**
    * Returns the backing array.
    *
    * @return the backing byte array
    */
   public final byte[] array() {
      return _buffer;
   }

   /**
    * Returns the current position.
    *
    * @return the current potition within the byte array
    *
    * @see #array()
    */
   public final int getPos() {
      return _pos;
   }

   /**
    * Returns the number of remaining bytes in the byte array.
    *
    * @return the number of remaining bytes in the byte array
    */
   public int remaining() {
      return _limit - _pos;
   }

   /**
    * Advances the current position by <code>n</code>.
    *
    * @param n number of positions to advance
    *
    * @throws ArrayIndexOutOfBoundsException if the position can not be advanced
    */
   public final void advance(int n) {
      verify(n);
      _pos += n;
   }

   /**
    * Advances the current position until the specified <code>alignment</code> is
    * achieved.
    *
    * @param alignment alignment that the current position must support
    */
   public void align(int alignment) {
      while (((_pos -_offset) % alignment) != 0) {
         advance(1);
      }
   }

   /**
    * Creates a <code>ByteWrapper</code> backed by the same byte array using the current
    * position as its offset.
    *
    * @return a new <code>ByteWrapper</code> backed by the same byte array starting at the current position
    */
   public ByteWrapper slice() {
      return new ByteWrapper(_buffer, _pos);
   }

   /**
    * Creates a <code>ByteWrapper</code> backed by the same byte array using the current
    * position as its offset, and the specified <code>length</code> to mark the limit.
    *
    * @param length length of the new <code>ByteWrapper</code>
    *
    * @return a new <code>ByteWrapper</code> backed by the same byte array starting at the current position
    *         with the defined <code>length</code>
    *
    * @throws ArrayIndexOutOfBoundsException if the <code>length</code> is to long
    */
   public ByteWrapper slice(int length) {
      verify(length);
      return new ByteWrapper(_buffer, _pos, length);
   }

   /**
    * Returns a string representation of the ByteWrapper.
    *
    * @return a string representation of the ByteWrapper
    */
   public String toString() {
      return "ByteWrapper{_offset=" + _offset + ", _pos=" + _pos + ", _limit=" + _limit + ", _buffer=" + _buffer + "}";
   }
}
