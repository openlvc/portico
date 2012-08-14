/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: LogicalTime.java

package hla.rti1516e;

import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.exceptions.InvalidLogicalTimeInterval;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.CouldNotEncode;

import java.io.Serializable;

/**
 * LogicalTime declares an interface to an immutable time value
 */

public interface LogicalTime<T extends LogicalTime<T, U>, U extends LogicalTimeInterval<U>> extends Comparable<T>, Serializable {
   /**
    * Returns true is this time is equal to the initial time.
    * @return true if initial value.
    */
   boolean isInitial();

   /**
    * Returns true is this time is equal to the final time.
    * @return true if final value.
    */
   boolean isFinal();

   /**
    * Returns a LogicalTime whose value is (this + val). The returned value shall
    * be different from this value if the specified interval != 0.
    * @param val interval to add.
    * @return new time value.
    * @throws IllegalTimeArithmetic
    * @throws InvalidLogicalTimeInterval
    */
   T add(U val)
      throws
      IllegalTimeArithmetic,
      InvalidLogicalTimeInterval;

   /**
    * Returns a LogicalTime whose value is (this - val). The returned value shall
    * be different from this value if the specified interval != 0.
    * @param val interval to subtract.
    * @return new time value.
    * @throws IllegalTimeArithmetic
    * @throws InvalidLogicalTimeInterval
    */
   T subtract(U val)
      throws
      IllegalTimeArithmetic,
      InvalidLogicalTimeInterval;

   /**
    * Returns a LogicalTimeInterval whose value is the time interval between
    * this and val.
    * @param val other time.
    * @return distance between times.
    * @throws InvalidLogicalTime
    */
   U distance(T val)
      throws
      InvalidLogicalTime;

   /**
    * Compares this object with the specified object for order.  Returns a
    * negative integer, zero, or a positive integer as this object is less
    * than, equal to, or greater than the specified object.<p>
    * @param   other the Object to be compared.
    * @return a negative integer, zero, or a positive integer as this object
    *		is less than, equal to, or greater than the specified object.
    */
   int compareTo(T other);

   /**
    * Returns true iff this and other represent the same logical time
    * Supports standard Java mechanisms.
    */
   boolean equals(Object other);

   /**
    * Two LogicalTimes for which equals() is true should yield
    * same hash code
    */
   int hashCode();

   String toString();

   /**
    * Returns the size of the buffer required to encode this object.
    * @return size of buffer.
    */
   int encodedLength();

   /**
    * Encodes this object in the specified buffer starting at the specified
    * offset.
    * @param buffer the buffer to encode into.
    * @param offset the offset where to start encoding.
    */
   void encode(byte[] buffer, int offset)
      throws CouldNotEncode;

}//end LogicalTime

