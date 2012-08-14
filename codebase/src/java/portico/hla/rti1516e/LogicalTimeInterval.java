/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: LogicalTimeInterval.java
package hla.rti1516e;

import hla.rti1516e.exceptions.InvalidLogicalTimeInterval;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.exceptions.CouldNotEncode;

import java.io.Serializable;

/**
 * LogicalTimeInterval declares an interface to an immutable time interval value
 */

public interface LogicalTimeInterval<U extends LogicalTimeInterval<U>> extends Comparable<U>, Serializable {
   /**
    * Returns true is this time is a zero interval.
    * @return true if zero interval.
    */
   boolean isZero();

   /**
    * Returns true is this time is an epsilon interval.
    * @return true if epsilon.
    */
   boolean isEpsilon();

   /**
    * Returns a LogicalTimeInterval whose value is (this + addend). The returned value
    * shall be different from this value if the specified addend != 0.
    * @param addend interval to add.
    * @return new interval.
    * @throws IllegalTimeArithmetic
    * @throws InvalidLogicalTimeInterval
    */
   U add(U addend)
      throws
      IllegalTimeArithmetic,
      InvalidLogicalTimeInterval;

   /**
    * Returns a LogicalTimeInterval whose value is (this - subtrahend). The returned value
    * shall be different from this value if the specified subtrahend != 0.
    * @param subtrahend interval to subtract.
    * @return new interval.
    * @throws IllegalTimeArithmetic
    * @throws InvalidLogicalTimeInterval
    */
   U subtract(U subtrahend)
      throws
      IllegalTimeArithmetic,
      InvalidLogicalTimeInterval;

   /**
    * Compares this object with the specified object for order.  Returns a
    * negative integer, zero, or a positive integer as this object is less
    * than, equal to, or greater than the specified object.<p>
    * @param   other the Object to be compared.
    * @return a negative integer, zero, or a positive integer as this object
    *		is less than, equal to, or greater than the specified object.
    */
   int compareTo(U other);

   /**
    * Returns true iff this and other represent the same time interval.
    */
   boolean equals(Object other);

   /**
    * Two LogicalTimeIntervals for which equals() is true should yield
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
}

//end LogicalTimeInterval

