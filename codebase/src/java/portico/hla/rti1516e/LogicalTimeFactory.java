/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e;

import hla.rti1516e.exceptions.CouldNotDecode;

import java.io.Serializable;

public interface LogicalTimeFactory<T extends LogicalTime<T, U>, U extends LogicalTimeInterval<U>> extends Serializable {

   T decodeTime(byte[] buffer, int offset)
      throws CouldNotDecode;

   U decodeInterval(byte[] buffer, int offset)
      throws CouldNotDecode;

   T makeInitial();

   T makeFinal();

   U makeZero();

   U makeEpsilon();

   String getName();
}
