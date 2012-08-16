/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e.time;

import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.exceptions.InvalidLogicalTimeInterval;
import hla.rti1516e.exceptions.CouldNotEncode;

/**
 * Interface for the interval part of the standardized time type HLAinteger64Time.
 */
public interface HLAinteger64Interval extends LogicalTimeInterval<HLAinteger64Interval> {
   boolean isZero();

   boolean isEpsilon();

   HLAinteger64Interval add(HLAinteger64Interval addend)
      throws
      IllegalTimeArithmetic,
      InvalidLogicalTimeInterval;

   HLAinteger64Interval subtract(HLAinteger64Interval subtrahend)
      throws
      IllegalTimeArithmetic,
      InvalidLogicalTimeInterval;

   int compareTo(HLAinteger64Interval other);

   int encodedLength();

   void encode(byte[] buffer, int offset)
      throws CouldNotEncode;

   long getValue();
}
