/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e.time;

import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotDecode;

/**
 * Interface for the factory part of standardized time type HLAinteger64Time.
 */
public interface HLAinteger64TimeFactory extends LogicalTimeFactory<HLAinteger64Time, HLAinteger64Interval> {
   String NAME = "HLAinteger64Time";

   HLAinteger64Time decodeTime(byte[] buffer, int offset)
      throws CouldNotDecode;

   HLAinteger64Interval decodeInterval(byte[] buffer, int offset)
         throws CouldNotDecode;

   HLAinteger64Time makeInitial();

   HLAinteger64Time makeFinal();

   HLAinteger64Time makeTime(long value);

   HLAinteger64Interval makeZero();

   HLAinteger64Interval makeEpsilon();

   HLAinteger64Interval makeInterval(long value);

   String getName();
}
