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
 * Interface for the factory part of standardized time type HLAfloat64Time.
 */
public interface HLAfloat64TimeFactory extends LogicalTimeFactory<HLAfloat64Time, HLAfloat64Interval> {
   String NAME = "HLAfloat64Time";

   HLAfloat64Time decodeTime(byte[] buffer, int offset)
      throws CouldNotDecode;

   HLAfloat64Interval decodeInterval(byte[] buffer, int offset)
         throws CouldNotDecode;

   HLAfloat64Time makeInitial();

   HLAfloat64Time makeFinal();

   HLAfloat64Time makeTime(double value);

   HLAfloat64Interval makeZero();

   HLAfloat64Interval makeEpsilon();

   HLAfloat64Interval makeInterval(double value);

   String getName();
}