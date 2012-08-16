/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: InteractionClassHandle.java

package hla.rti1516e;

import java.io.Serializable;

/**
 * Type-safe handle for an interaction class. Generally these are created by the
 * RTI and passed to the user.
 */

public interface InteractionClassHandle extends Serializable {

   /**
    * @return true if this refers to the same interaction class as other handle
    */
   boolean equals(Object otherInteractionClassHandle);

   /**
    * @return int. All instances that refer to the same interaction class should
    *         return the same hashcode.
    */
   int hashCode();

   int encodedLength();

   void encode(byte[] buffer, int offset);

   String toString();

}

//end InteractionClassHandle
