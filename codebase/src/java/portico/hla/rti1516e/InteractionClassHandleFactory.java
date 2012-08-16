/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: InteractionClassHandleFactory.java

package hla.rti1516e;

import hla.rti1516e.exceptions.CouldNotDecode;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;

import java.io.Serializable;

/**
 * The factory is used only (outside RTI) to create InteractionClassHandle
 * received as an attribute value or parameter value.
 */

public interface InteractionClassHandleFactory extends Serializable {
   InteractionClassHandle decode(byte[] buffer, int offset)
      throws CouldNotDecode, FederateNotExecutionMember, NotConnected, RTIinternalError;
}
